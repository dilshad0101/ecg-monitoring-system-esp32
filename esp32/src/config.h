#pragma once

// ── Toggle simulation ───────────────────────────────────────
#define USE_SIMULATION  false   // false when real hardware connected

// ── Pins ───────────────────────────────────────────────────
#define PIN_ECG_OUT      10    // AD8232 OUTPUT → ADC
#define PIN_ECG_LO_POS   4     // AD8232 LO+
#define PIN_ECG_LO_NEG   5     // AD8232 LO-
#define PIN_ECG_SDN      6     // AD8232 SDN (HIGH = on)
#define PIN_I2C_SDA      8     // Shared I2C — OLED + MPU6050
#define PIN_I2C_SCL      9
#define PIN_BUZZER       15    // Active buzzer via NPN
#define PIN_BATT_ADC      2    // Voltage divider mid-point

// ── Sampling ────────────────────────────────────────────────
#define ECG_FS          360    // Hz
#define ECG_BUF         360    // 1 second

// ── OLED ────────────────────────────────────────────────────
#define OLED_W          128
#define OLED_H           64
#define OLED_ADDR       0x3C

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

// ── Battery ─────────────────────────────────────────────────
#define BATT_FULL       4.20f
#define BATT_EMPTY      3.00f
#define BATT_DIV        2.0f
#define ADC_FULL        4095




#define WIFI_SSID "KERALA VISION -KV FI"
#define WIFI_PASSWORD "dilshad@9096"

#define SUPABASE_URL "https://mjsasefqkpsoozpubfvu.supabase.co/rest/v1/ecg"
#define SUPABASE_API_KEY "sb_publishable_Vz3coi_RFVuiHI1N_NxHcQ_chg6IMzY"