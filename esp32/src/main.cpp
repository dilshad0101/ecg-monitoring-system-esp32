#include <Arduino.h>
#include <Wire.h>
#include <Adafruit_GFX.h>
#include <Adafruit_SSD1306.h>
#include "config.h"
#include <WiFi.h>
#include <HTTPClient.h>

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
#define SAMPLE_INTERVAL 4    // 250 Hz sampling
#define PIXEL_INTERVAL 20    // controls horizontal speed
#define DRAW_INTERVAL 40     // 25 FPS display

unsigned long lastSample = 0;
unsigned long lastPixel  = 0;
unsigned long lastDraw   = 0;

int latestY = 0;

// ── Filter  and DC Offset removal using Moving Average ────────────────────────────────────────────────
//Default 5, but 7 seems to work better. Adjust as needed.
#define FILTER_SIZE 7
int filterBuf[FILTER_SIZE];
int filterIdx = 0;

#define DC_FILTER_SIZE 200
int dcBuffer[DC_FILTER_SIZE];
int dcIndex = 0;
long dcSum = 0;

int smooth(int v) {
  filterBuf[filterIdx] = v;
  filterIdx = (filterIdx + 1) % FILTER_SIZE;
  int s = 0;
  for (int i = 0; i < FILTER_SIZE; i++) s += filterBuf[i];
  int smoothened =  s / FILTER_SIZE; // return this if you don't want DC removal
  dcSum -= dcBuffer[dcIndex];
  dcBuffer[dcIndex] = smoothened;
  dcSum += smoothened;
  dcIndex = (dcIndex + 1) % DC_FILTER_SIZE;

  int dcAvg = dcSum / DC_FILTER_SIZE;

  return smoothened - dcAvg + 2000;  // re-center
}

// ── Adaptive scaling ──────────────────────────────────────
#define WINDOW_SIZE 100
#define DECAY 0.995f

int windowBuf[WINDOW_SIZE];
int windowIdx = 0;
float dynMin = 2000, dynMax = 2100;


int getScaledY(int value) {
  windowBuf[windowIdx] = value;
  windowIdx = (windowIdx + 1) % WINDOW_SIZE;

  int lMin = 4095, lMax = 0;
  for (int i = 0; i < WINDOW_SIZE; i++) {
    if (windowBuf[i] < lMin) lMin = windowBuf[i];
    if (windowBuf[i] > lMax) lMax = windowBuf[i];
  }

  dynMin = dynMin * DECAY + lMin * (1 - DECAY);
  dynMax = dynMax * DECAY + lMax * (1 - DECAY);

  float range = dynMax - dynMin;
  if (range < 50) range = 50;

  float center = (dynMax + dynMin) / 2;
  float half = range / 2;

  if (half > 600) half = 600;
  if (half < 50) half = 50;

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
  float thr = dynMin + 0.55 * (dynMax - dynMin);
  unsigned long now = millis();

  #define REFRACTORY_PERIOD 250  // ms

  if (val > thr && !aboveThr && (now - lastPeakMs > REFRACTORY_PERIOD)) {
    aboveThr = true;
    rr = now - lastPeakMs;

    if (lastPeakMs > 0 && rr >= 300 && rr <= 2000) {
      bpmRR[bpmRRi] = rr;
      bpmRRi = (bpmRRi + 1) % BPM_RR_COUNT;
      if (bpmRRn < BPM_RR_COUNT) bpmRRn++;

      float avg = 0;
      for (int i = 0; i < bpmRRn; i++) avg += bpmRR[i];
      avg /= bpmRRn;
      avg_rr = avg;
      bpmVal = constrain(60000 / avg, 30, 220);
    }

    lastPeakMs = now;
  }

  if (val < thr) aboveThr = false;

  if (lastPeakMs > 0 && now - lastPeakMs > 3000) {
    bpmVal = 0;
    bpmRRn = 0;
    lastPeakMs = 0;
  }
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
  if (range < 30) return 0;
  if (range < 80) return 25;
  if (range < 150) return 50;
  if (range < 300) return 75;
  return 95;
}

// ── Battery ───────────────────────────────────────────────
int readBattPct() {
  int raw = analogRead(PIN_BATT_ADC);
  float v = raw * (3.3f / 4095.0f) * 2;
  int pct = (v - 3.0f) / (4.2f - 3.0f) * 100;
  return constrain(pct, 0, 100);
}

// ── Lead off screen ───────────────────────────────────────
void drawLeadOff() {
  display.clearDisplay();
  display.setTextSize(2);
  display.setCursor(10, 20);
  display.print("DETACHED!");
  display.display();
}


// ── Main UI ───────────────────────────────────────────────
void drawMain(int bpm, int batt, int sqi) {
  display.clearDisplay();

  display.setTextSize(1);

  display.setCursor(0, 0);
  display.print("HR:");
  display.print(bpm > 0 ? bpm : 0);
  display.print(" BPM");

  display.setCursor(80, 0);
  display.print("Bat:");
  display.print(batt);
  display.print("%");

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
  if (WiFi.status() == WL_CONNECTED) {

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

    Serial.print("HTTP: ");
    Serial.println(code);

    http.end();
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
  Serial.begin(115200);

  Wire.begin(PIN_I2C_SDA, PIN_I2C_SCL);

  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);

while (WiFi.status() != WL_CONNECTED) {
  delay(500);
  Serial.print(".");
}
Serial.println("WiFi Connected");

  display.begin(SSD1306_SWITCHCAPVCC, 0x3C);
  display.setTextColor(WHITE);

  analogReadResolution(12);

  pinMode(PIN_ECG_LO_POS, INPUT);
  pinMode(PIN_ECG_LO_NEG, INPUT);
  pinMode(PIN_ECG_SDN, OUTPUT);
  digitalWrite(PIN_ECG_SDN, HIGH);

  xTaskCreatePinnedToCore(
  supabaseTask,     // function
  "Supabase Task",  // name
  10000,             // stack size. increase if you get "stack overflow" errors
  NULL,
  1,                // priority
  NULL,
  0                 // core 0 (keep loop on core 1)
);
}

// ── LOOP ─────────────────────────────────────────────────
void loop() {
  bool leadOff = digitalRead(PIN_ECG_LO_POS) ||
                 digitalRead(PIN_ECG_LO_NEG);

  // 🔹 FAST SAMPLING
  if (millis() - lastSample >= SAMPLE_INTERVAL) {
    lastSample = millis();

    int raw = analogRead(PIN_ECG_OUT);
    int filtered = smooth(raw);

    latestY = getScaledY(filtered);

    updateBPM(filtered);

   // Serial.println(raw);
  }
  int sqi = calcSQI();

  //  SLOW GRAPH MOVEMENT 
  if (millis() - lastPixel >= PIXEL_INTERVAL) {
    lastPixel = millis();

    ecgBuffer[indexX] = latestY;
    indexX = (indexX + 1) % SCREEN_W;
  }

  // DRAW
  if (millis() - lastDraw >= DRAW_INTERVAL) {
    lastDraw = millis();

    if (leadOff) {
      drawLeadOff();
    } else {
      drawMain(bpmVal, readBattPct(), sqi);
    }
  }
  static unsigned long lastUpload = 0;

  if (millis() - lastUpload > 1000) {  // every 1 sec
    lastUpload = millis();

    //sendToSupabase(rr, avg_rr, bpmVal, sqi); removed from loop because it is blocking and causes display lag. Now handled in separate task.
}
}
