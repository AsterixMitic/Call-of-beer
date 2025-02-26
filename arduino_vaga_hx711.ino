#include <Arduino.h>
#include "HX711.h"

// HX711 circuit wiring
const int LOADCELL_DOUT_PIN = 2;
const int LOADCELL_SCK_PIN = 3;

HX711 scale;

void setup() {
  Serial.begin(57600);

  scale.begin(LOADCELL_DOUT_PIN, LOADCELL_SCK_PIN);

  Serial.println("Setting up, please wait:");

   Serial.println(scale.read());      

   Serial.println(scale.read_average(20));   

  Serial.println(scale.get_value(5));   
   
  scale.set_scale(398.50);                      
  scale.tare();             
   
   Serial.println(scale.read());                 

   Serial.println(scale.read_average(20));       

   Serial.println(scale.get_value(5));   

   Serial.println("Start:");
}

void loop() {
  float weight = scale.get_units(10); 
  Serial.println(weight, 3);
  delay(100);
}