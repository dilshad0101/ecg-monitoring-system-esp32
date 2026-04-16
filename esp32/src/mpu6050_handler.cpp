#include <Wire.h>
#include <MPU6050.h>
#include "mpu6050_handler.h"

MPU6050 mpu;

static float motionAvg = 0;
static int motionPenalty = 0;

void initMPU6050() {
  mpu.initialize();

  if (!mpu.testConnection()) {
    Serial.println("MPU6050 connection failed!");
  } else {
    Serial.println("MPU6050 connected");
  }
}

void updateMotion() {
  int16_t ax, ay, az;
  mpu.getAcceleration(&ax, &ay, &az);

  float ax_g = ax / 16384.0;
  float ay_g = ay / 16384.0;
  float az_g = az / 16384.0;

  float accMag = sqrt(ax_g * ax_g + ay_g * ay_g + az_g * az_g);

  float motion = abs(accMag - 1.0); // remove gravity(in rest position, accMag should be ~1g)

  // smoothing
  motionAvg = 0.9 * motionAvg + 0.1 * motion; // simple low-pass filter to smooth motion values

  Serial.print("Motion: ");
  Serial.println(motionAvg);
  
  // penalty mapping
  motionPenalty = (motionAvg <= 0.06) ? 0 : constrain(pow(motionAvg * 2.0, 1.2) * 60, 0, 100); // maps motion to 0-100 penalty
}

float getMotion() {
  return motionAvg;
}

int getMotionPenalty() {
  return motionPenalty;
}