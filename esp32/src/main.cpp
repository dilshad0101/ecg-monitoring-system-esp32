#include <Arduino.h>

const int BUZZER_PIN = 26;
const int PWM_CHANNEL = 0;
const int RESOLUTION = 8;

// Notes (frequency in Hz)
int melody[] = {
  262, // C
  294, // D
  330, // E
  349, // F
  392, // G
  440, // A
  494, // B
  523  // High C
};

// Note durations (in ms)
int durations[] = {
  300, 300, 300, 300,
  300, 300, 300, 600
};

void setup() {
  ledcAttachPin(BUZZER_PIN, PWM_CHANNEL);
}

void loop() {
  for (int i = 0; i < 8; i++) {

    // Set frequency for note
    ledcSetup(PWM_CHANNEL, melody[i], RESOLUTION);

    // Turn ON (50% duty cycle)
    ledcWrite(PWM_CHANNEL, 128);

    delay(durations[i]);

    // Turn OFF (pause between notes)
    ledcWrite(PWM_CHANNEL, 0);
    delay(100);
  }

  delay(2000); // wait before repeating
}