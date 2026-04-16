#pragma once

//For C3
#define PIN_ECG_OUT      32    // AD8232 OUTPUT → ADC
#define PIN_ECG_LO_POS   19     // AD8232 LO+
#define PIN_ECG_LO_NEG   18    // AD8232 LO-
#define PIN_ECG_SDN      16    // AD8232 SDN (HIGH = on)
#define PIN_I2C_SDA      21     // Shared I2C — OLED + MPU6050
#define PIN_I2C_SCL      22
#define PIN_BUZZER       4    // Active buzzer via NPN
#define PIN_BATT_ADC     34    // Voltage divider mid-point


// ── Sampling ────────────────────────────────────────────────
#define ECG_FS          360    // Hz
#define ECG_BUF         360    // 1 second

// ── OLED ────────────────────────────────────────────────────
#define OLED_W          128
#define OLED_H           64
#define OLED_ADDR       0x3c

// ── Alert thresholds ────────────────────────────────────────
#define HR_HIGH         120
#define HR_LOW           50
#define FALL_G          3.0f
#define FREEFALL_G      0.4f
#define FREEFALL_MIN    100UL
#define FREEFALL_MAX   2000UL
#define SQI_MIN          60
#define BATT_LOW         15

// ── Timing (ms) ─────────────────────────────────────────────
#define DISP_MS        1000
#define CLOUD_MS      30000
#define ALERT_COOL    60000UL
#define FALL_COOL     10000UL

#define ADC_FULL        4095

#define SUPABASE_URL "https://XXXXXXXXXXXXX.supabase.co/rest/v1/ecg"
#define SUPABASE_API_KEY XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"