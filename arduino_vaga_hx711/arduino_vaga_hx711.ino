#include <Arduino.h>
#include "HX711.h"

// HX711 circuit wiring
const int LOADCELL_DOUT_PIN = 2;
const int LOADCELL_SCK_PIN = 3;

HX711 scale;

void setup() {
  Serial.begin(57600);

  scale.begin(LOADCELL_DOUT_PIN, LOADCELL_SCK_PIN);

  // Calibrating the tare 
  // !important DO NOT PUT WEIGHT ON TARE WHILE IT IS CALIBRATING
  //Serial.println("Setting up, please wait:");
  scale.read();      
  scale.read_average(20);   
  scale.get_value(5);   
   
  scale.set_scale(398.50);                      
  scale.tare();             
  scale.read();                 
  scale.read_average(20);       
  scale.get_value(5);   

  // Arduino is ready to send data
  //Serial.println("Ready!");
}

void loop() {
  float weight = scale.get_units(10); 
  Serial.println(weight,2);
  delay(50);
}