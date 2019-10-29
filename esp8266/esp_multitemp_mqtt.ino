#include <ESP8266WiFi.h>
#include <PubSubClient.h>
#include <OneWire.h>
#include <DallasTemperature.h>

// Data wire is plugged into port 2 on the Arduino
#define ONE_WIRE_BUS 2

// Setup a oneWire instance to communicate with any OneWire devices (not just Maxim/Dallas temperature ICs)
OneWire oneWire(ONE_WIRE_BUS);

// Pass our oneWire reference to Dallas Temperature. 
DallasTemperature sensors(&oneWire);

uint8_t keezerAirThermometer[8] = { 0x28, 0xFF, 0x71, 0xCB, 0x6B, 0x18, 0x03, 0x59 };
uint8_t keezerCanThermometer[8]   = { 0x28, 0xFF, 0xF5, 0xB2, 0x77, 0x18, 0x01, 0xFB };
uint8_t fermenterThermometer[8]   = { 0x28, 0xFF, 0x87, 0x86, 0x77, 0x18, 0x01, 0x53 };

//wifi
const char* ssid = "REPLACE";
const char* password = "REPLACE";

//mqtt
const char* mqtt_server = "192.168.1.34";
//const char* mqtt_username = "<MQTT_BROKER_USERNAME>";
//const char* mqtt_password = "<MQTT_BROKER_PASSWORD>";

//mqtt topics
const char* keezerAirTopic = "smartthings/Brew House Keezer Air Temperature MQTT/temperature";
const char* keezerCanTopic = "smartthings/Brew House Keezer Can Temperature MQTT/temperature";
const char* fermenterTopic = "smartthings/Brew House Fermenter Temperature MQTT/temperature";

WiFiClient espClient;
PubSubClient client(espClient);

void setup(void)
{
  // start serial port
  Serial.begin(115200);
  Serial.println("Brew House Temperature Monitor");

  // setup WiFi
  setup_wifi();

  // setup MQTT
  setup_mqtt();

  // Start up the library
  sensors.begin();
  
  // locate devices on the bus
  Serial.print("Found ");
  Serial.print(sensors.getDeviceCount(), DEC);
  Serial.println(" devices.");

 
  // show the addresses we found on the bus
  Serial.print("Device 0 Address: ");
  printAddress(keezerAirThermometer);
  Serial.println();
  
  Serial.print("Device 1 Address: ");
  printAddress(keezerCanThermometer);
  Serial.println();

  Serial.print("Device 2 Address: ");
  printAddress(fermenterThermometer);
  Serial.println();
}

void setup_wifi()
{
  delay(10);

  // We start by connecting to a WiFi network
  Serial.println();
  Serial.print("Connecting to ");
  Serial.println(ssid);

  WiFi.begin(ssid, password);

  while (WiFi.status() != WL_CONNECTED)
  {
    delay(500);
    Serial.print(".");
  }

  Serial.println("");
  Serial.println("WiFi connected");
  Serial.println("IP address: ");
  Serial.println(WiFi.localIP());
}

void setup_mqtt()
{
   client.setServer(mqtt_server, 1883);
   client.setCallback(callback);
}

void reconnect()
{
  // Loop until we're reconnected
  while (!client.connected())
  {
    Serial.print("Attempting MQTT connection...");

    // Attempt to connect
    //if (client.connect("ESP8266Client", mqtt_username, mqtt_password))
    {
       if (client.connect("ESP8266Client"))
       {
          Serial.println("connected");
          client.publish("ESP8266Client","connected");
       }
       else
       {
          Serial.print("failed, rc=");
          Serial.print(client.state());
          Serial.println(" try again in 5 seconds");
          // Wait 5 seconds before retrying
          delay(5000);
       }
    }
  }
}

// function to print a device address
void printAddress(DeviceAddress deviceAddress)
{
  for (uint8_t i = 0; i < 8; i++)
  {
    if (deviceAddress[i] < 16) Serial.print("0");
    Serial.print(deviceAddress[i], HEX);
  }
}

void callback(char* topic, byte* payload, unsigned int length)
{
  Serial.print("Message arrived [");
  Serial.print(topic);
  Serial.print("] ");
  for (int i = 0; i < length; i++)
  {
    Serial.print((char)payload[i]);
  }
  Serial.println();
}

// function to print the temperature for a device
void printTemperature(DeviceAddress deviceAddress,
                      const char*   topic)
{
  Serial.print("Device Address: ");
  printAddress(deviceAddress);

  float tempF = sensors.getTempF(deviceAddress);
  String temperature(tempF, 2);

  Serial.print(" Temp F: ");
  Serial.print(temperature);
  Serial.print(" for Topic: ");
  Serial.print(topic);

  if (client.publish(topic, (char*)temperature.c_str()))
  {
      Serial.println("Publish ok  : ");
      Serial.print(topic);
      Serial.print(":");
      Serial.println(temperature);
  }
  else
  {
      Serial.println("Publish failed");
  }

  Serial.println();
}


void loop(void)
{ 
  if (!client.connected())
  {
    reconnect();
  }

  client.loop();

  // call sensors.requestTemperatures() to issue a global temperature 
  // request to all devices on the bus
  Serial.print("Requesting temperatures...");
  sensors.requestTemperatures();
  Serial.println("DONE");


  // show the temperatures:
  printTemperature(keezerAirThermometer, keezerAirTopic);
  printTemperature(keezerCanThermometer, keezerCanTopic);
  printTemperature(fermenterThermometer, fermenterTopic);

  delay(30000);
}
