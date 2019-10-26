/**
 *  Brewfather iSpindel - SmartApp v 1.0
 *
 *  Author: 
 *    larrywho
 *
 *  Changelog:
 *
 *    1.0 (10/26/2019 by larrywho)
 *      - Initial release
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
definition(
    name: "Brewfather iSpindel",
    author: "larrywho",
    namespace: "larrywho",
    description: "Post iSpindel data to Brewfather stream.",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences
{
    section ("Brewfather iSpindel ...")
    {
        input "ispindel", "capability.temperatureMeasurement", title: "ispindel", required: true, multiple: false
        input "brewfatherStreamID", "text", title: "Brewfather Stream ID", required: true
        input "appEnabled", "text", title: "0=Disabled, 1=Enabled", required: true, defaultValue: "0"
     }
}

def installed()
{
    logger("DEBUG", "Installed with settings: ${settings}")

    initialize()
}

def updated()
{
    logger("DEBUG", "Updated with settings: ${settings}")

    unsubscribe()
    unschedule()
    initialize()
}

def initialize()
{
    runEvery15Minutes("runApp")
}

def runApp()
{
   try
   {
      if (1 == appEnabled.toInteger())
      {
         logger("DEBUG", "calling postiSpindelData()")
         postiSpindelData()
      }
      else
      {
         logger("DEBUG", "not enabled")
      }
   }
   catch (e)
   {
      logger("ERROR", "caught an exception: $e")
   }
}

private postiSpindelData()
{
    def name = ispindel.currentValue("iname")
    def temp = ispindel.currentValue("itemperature")
    def temp_units = ispindel.currentValue("itempunits")
    def gravity = ispindel.currentValue("igravity")
    def tilt = ispindel.currentValue("itilt")
    def signal = ispindel.currentValue("iRSSI")
    def battery = ispindel.currentValue("ibattery")
    def streamURI = "http://log.brewfather.net/ispindel?id=${brewfatherStreamID}"

    try
    {
       httpPostJson(uri: streamURI, body: ["name": name,
                                           "angle": tilt,
                                           "temperature": temp, 
                                           "temp_units": temp_units,
                                           "battery": battery,
                                           "gravity": gravity,
                                           "RSSI": signal])
                   {response -> log.debug response.data}
    } catch (e)
    {
       log.debug "something went wrong: $e"
    }
    
    logger("INFO", "Thermostat Temperature = ${thermostatTemp}F")
}

private logger(level, logString)
{
    def msg = "${level} - ${logString}"
    log.info "${msg}"
}