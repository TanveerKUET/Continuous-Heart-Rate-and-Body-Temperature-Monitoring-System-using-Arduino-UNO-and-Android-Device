#include <FreqMeasure.h>
#include <SoftwareSerial.h>

SoftwareSerial bluetooth(0,1);
void setup() {
  
  bluetooth.begin(9600);
  FreqMeasure.begin();
  }

double sum=0;
int count=0;
int BPM=0;
int temp = 0;
int tempf = 0;
void loop() {
  //Now I am measuring temperature
  
  temp  =analogRead(A0);
                temp=temp * 0.48828125;
               tempf=(temp*1.8)+32;
              
               
               /*Again measuring tem when freq is available*/
  if (FreqMeasure.available()>0) {
    // average several reading together
    sum = sum + FreqMeasure.read();
    count = count + 1;
   
  temp  =analogRead(A0);
  temp=temp * 0.48828125;
  tempf=(temp*1.8)+32;
              
    if (count > 30) {
      double frequency = F_CPU / (sum / count);
      BPM=frequency*60;
      sum = 0;
      count = 0;
      
      char buf[4];
      if(tempf<100){
      sprintf(buf, "t%d ", tempf);   //String Concatanation with string and int value
      }
      else{
      sprintf(buf, "t%d", tempf);   //String Concatanation with string and int value
      }
      
      char buf2[4];
      if(BPM<100){
      sprintf(buf2,"b%d ",BPM);
      }-
      else{
      sprintf(buf2,"b%d",BPM);
      }
      char mainstr[8];
      sprintf(mainstr,"%s",buf,buf2);
      bluetooth.println(mainstr);
      delay(2000);
    }
  }
}
