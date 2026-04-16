#ifndef MPU6050_HANDLER_H
#define MPU6050_HANDLER_H

void initMPU6050();
void updateMotion();

float getMotion();
int getMotionPenalty();

#endif