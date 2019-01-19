/**
 *  Fan Controller - SmartApp v 1.0
 *
 *  Author: 
 *    larrywho
 *
 *  Changelog:
 *
 *    1.0 (09/16/2018 by larrywho)
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
    name: "Fan Controller",
    author: "larrywho",
    namespace: "larrywho",
    description: "Control a fan using a temperature sensor and a smart outlet.",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
    section ("Fan Controller...") {
        input "thermostat", "capability.temperatureMeasurement", title: "Thermostat", required: true, multiple: false
        input "outlet", "capability.switch", title: "Outlet", required: true, multiple: false
        input "thermostatOffThresh", "decimal", title: "Off Temperature Threshold", required: true
        input "thermostatOnThresh", "decimal", title: "On Temperature Threshold", required: true
    }
}

def installed() {
    logger("DEBUG", "Installed with settings: ${settings}")

    initialize()
}

def updated() {
    logger("DEBUG", "Updated with settings: ${settings}")

    unsubscribe()
    unschedule()
    initialize()
}

def initialize() {

    // turn the switch off
    outlet.off()
	
    runEvery1Minute("runApp")
}



def runApp()
{
   try
   {
      logger("DEBUG", "calling switchControl()")
      switchControl()
   }
   catch (e)
   {
      logger("ERROR", "caught an exception: $e")
   }
}

private switchControl()
{
    def thermostatTemp = thermostat.currentValue("temperature")
    def outletState = outlet.currentSwitch
    def status = ""
    
    if ("on" == outletState)
    {
       if (thermostatTemp <= thermostatOffThresh)
       {
          outlet.off()
          status = "turning fan off"
       }
       else
       {
          status = "leaving fan on"
       }
    }
    else
    {
       if (thermostatTemp >= thermostatOnThresh)
       {
          outlet.on()
          status = "turning fan on"
       }
       else
       {
          status = "leaving fan off"
       }
    }    
    
    logger("INFO", "Thermostat Temperature = ${thermostatTemp}F, State = ${outletState}, Status = ${status}")
}

private logger(level, logString)
{
    def msg = "${level} - ${logString}"
    log.info "${msg}"
}
