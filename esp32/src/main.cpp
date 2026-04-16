#include <Arduino.h>
#include <Wire.h>
#include <Adafruit_GFX.h>
#include <Adafruit_SSD1306.h>
#include "config.h"
#include <WiFi.h>
#include <HTTPClient.h>
#include <WiFiManager.h>
#include <Adafruit_ADS1X15.h>
#include "mpu6050_handler.h"

Adafruit_ADS1115 ads;
#define ADS_MAX   26400   // counts at 3.3 V with GAIN_ONE

// ── Display ────────────────────────────────────────────────
#define SCREEN_W  128
#define SCREEN_H   64
Adafruit_SSD1306 display(SCREEN_W, SCREEN_H, &Wire, -1);

// ── Layout ────────────────────────────────────────────────
#define WAVE_TOP    10
#define WAVE_BOTTOM 49
#define WAVE_H      (WAVE_BOTTOM - WAVE_TOP)

// ── ECG buffer ─────────────────────────────────────────────
int ecgBuffer[SCREEN_W];
int indexX = 0;

// ── Timing control  ──────
#define SAMPLE_INTERVAL 4000    // 250 Hz sampling
#define PIXEL_INTERVAL 20    // controls horizontal speed
#define DRAW_INTERVAL 40     // 25 FPS display

unsigned long lastSample = 0;
unsigned long lastPixel  = 0;
unsigned long lastDraw   = 0;

int latestY = 0;

// ── Filter  and DC Offset removal using Moving Average ────────────────────────────────────────────────
//Default 5, but 7 seems to work better. Adjust as needed.
#define FILTER_SIZE 5
int filterBuf[FILTER_SIZE];
int filterIdx = 0;


int smooth(int v) {
  filterBuf[filterIdx] = v;
  filterIdx = (filterIdx + 1) % FILTER_SIZE;
  int s = 0;
  for (int i = 0; i < FILTER_SIZE; i++) s += filterBuf[i];
  int smoothened =  s / FILTER_SIZE; // return this if you don't want DC removal


  return smoothened; // - dcAvg + 2048;  // re-center
}

// ── Adaptive scaling ──────────────────────────────────────
#define WINDOW_SIZE 100
#define DECAY 0.995f

int windowBuf[WINDOW_SIZE];
int windowIdx = 0;
float dynMin = 1900.0f, dynMax = 2200.0f;


int getScaledY(int value) {
  windowBuf[windowIdx] = value;
  windowIdx = (windowIdx + 1) % WINDOW_SIZE;

  int lMin = 32767, lMax = -32768;
  for (int i = 0; i < WINDOW_SIZE; i++) {
    if (windowBuf[i] < lMin) lMin = windowBuf[i];
    if (windowBuf[i] > lMax) lMax = windowBuf[i];
  }
  dynMin = dynMin * DECAY + lMin * (1.0f - DECAY);
  dynMax = dynMax * DECAY + lMax * (1.0f - DECAY);

  float center = (dynMin + dynMax) / 2.0f;
  float half   = (dynMax - dynMin) / 2.0f;

  // After normalisation to 12-bit, a good ECG swing is ~100–400 counts
  if (half < 30)  half = 30;    // prevent flatline zoom-in
  if (half > 400) half = 400;   // prevent noise zoom-out

  int y = map(value, center - half, center + half,
              WAVE_BOTTOM, WAVE_TOP);

  return constrain(y, WAVE_TOP, WAVE_BOTTOM);
}

// ── BPM detection ─────────────────────────────────────────
#define BPM_RR_COUNT 6
float bpmRR[BPM_RR_COUNT];
int bpmRRi = 0, bpmRRn = 0;
int bpmVal = 0;
bool aboveThr = false;
unsigned long lastPeakMs = 0;
unsigned long rr = 0;
unsigned long avg_rr = 0;

void updateBPM(int val) {
  static int prev = 0;
  static float bpmSmooth = 0;

  float thr = dynMin + 0.55 * (dynMax - dynMin);
  unsigned long now = millis();

  #define REFRACTORY_PERIOD 400  // ms (prevents double detection)

  bool isRising = (val > prev);
  bool strongPeak = (val - dynMin) > 0.6 * (dynMax - dynMin);

  if (val > thr &&
      isRising &&
      strongPeak &&
      !aboveThr &&
      (now - lastPeakMs > REFRACTORY_PERIOD)) {

    aboveThr = true;

    unsigned long rrInterval = now - lastPeakMs;

    if (lastPeakMs > 0 && rrInterval >= 300 && rrInterval <= 2000) {
      bpmRR[bpmRRi] = rrInterval;
      bpmRRi = (bpmRRi + 1) % BPM_RR_COUNT;
      if (bpmRRn < BPM_RR_COUNT) bpmRRn++;

      // Average RR
      float avg = 0;
      for (int i = 0; i < bpmRRn; i++) avg += bpmRR[i];
      avg /= bpmRRn;

      avg_rr = avg;

      int bpmRaw = constrain(60000 / avg, 30, 220);

      // 🔥 Smooth BPM (VERY IMPORTANT)
      bpmSmooth = 0.8 * bpmSmooth + 0.2 * bpmRaw;
      bpmVal = bpmSmooth;
    }

    lastPeakMs = now;
  }

  // Reset threshold crossing
  if (val < thr) {
    aboveThr = false;
  }

  // Timeout: no heartbeat detected
  if (lastPeakMs > 0 && now - lastPeakMs > 3000) {
    bpmVal = 0;
    bpmRRn = 0;
    lastPeakMs = 0;
    bpmSmooth = 0;
  }

  prev = val;  // update previous sample
} 
int getThresholdY(float thr) {
  float center = (dynMax + dynMin) / 2;
  float half = (dynMax - dynMin) / 2;

  if (half > 600) half = 600;
  if (half < 50)  half = 50;

  int y = map(thr, center - half, center + half,
              WAVE_BOTTOM, WAVE_TOP);

  return constrain(y, WAVE_TOP, WAVE_BOTTOM);
}

// ── SQI ───────────────────────────────────────────────────
int calcSQI() {
  float range = dynMax - dynMin;

  int baseSQI;
  if (range < 20)  baseSQI = 0;
  else if (range < 50)  baseSQI = 25;
  else if (range < 100) baseSQI = 50;
  else if (range < 200) baseSQI = 75;
  else baseSQI = 95;
  int finalSQI = baseSQI - getMotionPenalty();
  return constrain(finalSQI, 0, 100);
}

// ── Draw texts in screen ───────────────────────────────────────
void drawText(const char *text) {
  display.clearDisplay();
  display.setTextSize(1);
  display.setCursor(10, 20);
  display.print(text);
  display.display();
}

// ── Main UI ───────────────────────────────────────────────
void drawMain(int bpm, int sqi) {
  display.clearDisplay();

  display.setTextSize(1);

  display.setCursor(0, 0);
  display.print("HR:");
  display.print(bpm > 0 ? bpm : 0);
  display.print(" BPM");

  display.drawFastHLine(0, 9, SCREEN_W, WHITE);
  display.drawFastHLine(0, 50, SCREEN_W, WHITE);

  float thr = dynMin + 0.55 * (dynMax - dynMin);
  int thrY = getThresholdY(thr);
  for (int x = 0; x < SCREEN_W; x += 7) {
    display.drawFastHLine(x, thrY, 2, WHITE); // 2px dashed threshold line
    }
  // ECG waveform
  for (int i = 1; i < SCREEN_W; i++) {
    display.drawLine(i - 1, ecgBuffer[i - 1],
                     i,     ecgBuffer[i], WHITE);
  }

  display.setCursor(0, 53);
  display.print("SQI:");
  display.print(sqi);

  display.display();
}

// ── Supabase integration (optional) ───────────────────────────────────────────────
void sendToSupabase(int rr, int avg_rr, int bpm, int sqi) {
  if (WiFi.status() == WL_CONNECTED && sqi >= SQI_MIN) {

    HTTPClient http;
    WiFiClientSecure client;
    client.setInsecure(); //reduce security (TLS: Transport Layer Security) for simplicity
    http.begin(client,SUPABASE_URL);

    http.addHeader("Content-Type", "application/json");
    http.addHeader("apikey", SUPABASE_API_KEY);
    http.addHeader("Authorization", "Bearer " + String(SUPABASE_API_KEY));
    http.addHeader("Prefer", "return=minimal");

    String json = "{";
    json += "\"rr\":" + String(rr) + ",";
    json += "\"avg_rr\":" + String(avg_rr) + ",";
    json += "\"bpm\":" + String(bpm) + ",";
    json += "\"sqi\":" + String(sqi);
    json += "}";

    
    int code = http.POST(json);

    Serial.print("Data Uploaded with SQI of ");
    Serial.print(sqi);
    Serial.print(" Upload Status Code HTTP: ");
    Serial.println(code);

    http.end();
  }else if (sqi < SQI_MIN && WiFi.status() == WL_CONNECTED){
    Serial.println("SQI too low, skipping upload");
}
}
void supabaseTask(void *param) {
  while (true) {
    sendToSupabase(rr, avg_rr, bpmVal, calcSQI());

    vTaskDelay(6000 / portTICK_PERIOD_MS);  // every 1 sec
  }
}

// ── Setup ────────────────────────────────────────────────
void setup() {

  Wire.begin(PIN_I2C_SDA, PIN_I2C_SCL);
  delay(500);
  Serial.begin(115200);

  delay(500);
  display.begin(SSD1306_SWITCHCAPVCC, 0x3C);
  if (!display.begin(SSD1306_SWITCHCAPVCC, 0x3C)) {
      Serial.println("OLED failed");
      while (true);
  }
  display.setTextColor(WHITE);

  analogReadResolution(12);
  analogSetAttenuation(ADC_11db);

  ads.begin();
  ads.setGain(GAIN_ONE);  
  ads.setDataRate(RATE_ADS1115_250SPS);

  pinMode(PIN_ECG_LO_POS, INPUT);
  pinMode(PIN_ECG_LO_NEG, INPUT);

  xTaskCreatePinnedToCore(
  supabaseTask,     // function
  "Supabase Task",  // name
  10000,             // stack size. increase if you get "stack overflow" errors
  NULL,
  1,                // priority
  NULL,
  0                 // core 0 (keep loop on core 1)
  );
  drawText("CONNECTING TO WIFI...");
  
  delay(500);
  WiFiManager wifiManager;
  //wifiManager.resetSettings();
  bool wifiRes = wifiManager.autoConnect("ECG-WiFi");
  if (!wifiRes) {
    Serial.println("Failed to connect to WiFi. Restarting...");
    drawText("WIFI FAILED! RESTARTING...");
    delay(1000);
    ESP.restart();
    
  }else { 
    drawText("WiFi Connected!");
    delay(1000);
    Serial.println("WiFi Connected");
    Serial.println(WiFi.SSID());
  }
  wifiManager.setTimeout(180); 

  initMPU6050(); //init motion sensor
}


int removeBaseline(int x) { //HPF
  static float prev_y = 0;
  static float prev_x = 0;

  float y = 0.99 * (prev_y + x - prev_x);

  prev_y = y;
  prev_x = x;

  return (int)y;
}

// ── LOOP ─────────────────────────────────────────────────
void loop() {
  bool leadOff = digitalRead(PIN_ECG_LO_POS) ||
                 digitalRead(PIN_ECG_LO_NEG);

  // 🔹 FAST SAMPLING
  if (micros() - lastSample >= SAMPLE_INTERVAL) {
    lastSample = micros();

    int16_t rawADS = ads.readADC_SingleEnded(0);
    int raw = map(constrain((int)rawADS, 0, ADS_MAX), 0, ADS_MAX, 0, 4095);
    int centered = removeBaseline(raw);
    int filtered = smooth(centered);

    latestY = getScaledY(filtered);

  
    updateBPM(filtered);

    //Serial.println(raw);
  }
  updateMotion(); // update motion values every loop (can be optimized to run less frequently)
  int sqi = calcSQI();

  //  SLOW GRAPH MOVEMENT 
  if (millis() - lastPixel >= PIXEL_INTERVAL) {
    lastPixel += PIXEL_INTERVAL;

    ecgBuffer[indexX] = latestY;  
    indexX = (indexX + 1) % SCREEN_W;
  }

  // DRAW
      if (millis() - lastDraw >= DRAW_INTERVAL) {
    lastDraw = millis();

    if (leadOff) {
      drawText("LEAD OFF!");
    } else {
      drawMain(bpmVal, sqi);
    }
  }

}
