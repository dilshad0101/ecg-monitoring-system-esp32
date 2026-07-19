# ❤️ Real-Time ECG Monitoring System

A low-cost, portable, and cloud-connected ECG monitoring system designed for continuous cardiac signal acquisition, real-time visualization, and remote health monitoring.

---

## 📖 Overview

Cardiovascular diseases are among the leading causes of mortality worldwide, creating the need for accessible and continuous heart monitoring solutions. Traditional ECG systems are often expensive, bulky, and limited to clinical environments.

This project presents a **portable IoT-based ECG monitoring system** built using the **ESP32**, capable of acquiring, processing, visualizing, and remotely transmitting ECG data in real time.

The system captures ECG signals using the **AD8232 analog front-end** and a **16-bit ADS1115 external ADC** to improve signal fidelity and reduce noise. Processed ECG data, heart rate, and signal quality metrics are displayed locally on an OLED display and remotely through a companion Android application.

> **Note:** This project is intended for educational and research purposes and is **not a medical diagnostic device**.

---

# ✨ Features

* 📈 Real-time ECG signal acquisition
* ❤️ Heart Rate (BPM) calculation
* 📊 Signal Quality Index (SQI) estimation
* 🔍 Lead-off detection for electrode reliability
* 📟 Live ECG visualization on OLED display
* 🌐 Wi-Fi based cloud connectivity
* ☁️ Secure ECG data storage using Supabase
* 📱 Android application for remote monitoring
* 🏃 Motion artifact detection using MPU6050
* 🔧 Digital filtering and signal enhancement

---

# 🛠 Hardware Components

| Component             | Purpose                                      |
| --------------------- | -------------------------------------------- |
| ESP32                 | Main microcontroller and Wi-Fi communication |
| AD8232                | ECG analog front-end module                  |
| ADS1115 (16-bit ADC)  | High-resolution ECG signal conversion        |
| OLED Display (128×64) | Real-time waveform visualization             |
| MPU6050               | Motion artifact detection                    |
| ECG Electrodes        | Cardiac signal acquisition                   |
| Power Supply          | Portable system operation                    |

---

# 🧠 Signal Processing Pipeline

The acquired ECG signal undergoes multiple processing stages:

1. Raw ECG acquisition using AD8232
2. High-resolution sampling using ADS1115
3. Baseline drift removal using IIR High-Pass Filter
4. Moving Average filtering for noise reduction
5. Adaptive amplitude scaling
6. R-peak detection
7. BPM and R-R interval calculation
8. Signal Quality Index estimation
9. Visualization and cloud transmission

---

# 📡 System Architecture

```text
ECG Electrodes
       ↓
     AD8232
       ↓
    ADS1115
       ↓
      ESP32
       ↓
 ┌───────────────┐
 │ OLED Display  │
 └───────────────┘
       ↓
   Wi-Fi / HTTPS
       ↓
    Supabase
       ↓
 Android Application
```

---

# 📱 Android Application

The companion Android application was developed using:

* Kotlin
* Jetpack Compose
* Material Design 3
* Supabase Integration

### Features

* Real-time ECG data retrieval
* Heart rate visualization
* Signal quality monitoring
* Remote patient monitoring
* Historical data viewing

---

# ☁️ Cloud Integration

The system securely transmits the following data to Supabase:

* Heart Rate (BPM)
* R-R Interval
* Signal Quality Index (SQI)
* Timestamped ECG information

Communication is performed through HTTPS APIs over Wi-Fi.

---

# 📂 Project Structure

```text
ECG-Monitoring-System/
│
├── Embedded_Firmware/
│   ├── ECG Acquisition
│   ├── Signal Processing
│   ├── OLED Visualization
│   └── Cloud Communication
│
├── Android-App/
│   ├── UI
│   ├── Supabase Integration
│   └── Visualization
│
└── Documentation/
```

---

# 🔬 Technologies Used

### Embedded Systems

* ESP32
* Embedded C/C++
* I2C Communication
* HTTPS Networking

### Signal Processing

* IIR High Pass Filter
* Moving Average Filter
* R-Peak Detection
* Signal Quality Estimation

### Software

* Kotlin
* Jetpack Compose
* Supabase
* Android Studio

---

# 🎯 Applications

* Remote Patient Monitoring
* Wearable Healthcare Devices
* IoT Healthcare Systems
* Continuous Cardiac Monitoring
* Biomedical Signal Processing Research

---

# 🚀 Future Improvements

* Arrhythmia detection using Machine Learning
* Wearable form factor implementation
* Long-term ECG data analytics
* Integration with smartwatches
* Cloud dashboards and alerts
* AI-assisted cardiac anomaly detection

---

# 📸 Results

* Real-time ECG waveform visualization
* Accurate BPM estimation
* Reliable cloud synchronization
* Remote monitoring through Android application

---

# ⚠ Disclaimer

This project is developed for **educational, research, and prototyping purposes only** and should **not be used for clinical diagnosis or medical decision-making**.

---
