/**
 *  Keezer Controller - SmartApp v 1.0
 *
 *  Author: 
 *    larrywho
 *
 *  Changelog:
 *
 *    1.0 (07/25/2019 by larrywho)
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
    name: "Keezer Controller",
    author: "larrywho",
    namespace: "larrywho",
    description: "Control a keezer using a temperature sensor and a smart outlet.",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
    section ("Keezer Controller...") {
        input "thermostat", "capability.temperatureMeasurement", title: "Thermostat", required: true, multiple: false
        input "outlet", "capability.switch", title: "Outlet", required: true, multiple: false
        input "thermostatOffThresh", "decimal", title: "Off Temperature Threshold", required: true
        input "thermostatOnThresh", "decimal", title: "On Temperature Threshold", required: true
	input "offWaitTime", "decimal", title: "Unit Off Min Time", required: true, defaultValue: 15
	input "appEnabled", "text", title: "0=Disabled, 1=Enabled", required: true, defaultValue: "0"
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
    atomicState.outletOff = currentTime
	
    runEvery1Minute("runApp")
}



def runApp()
{
   try
   {
      if (1 == appEnabled.toInteger())
      {
	 def currentTime = now()
         logger("DEBUG", "calling switchControl()")
         switchControl(currentTime)
      }
      else
      {
         logger("DEBUG", "app not enabled")
      }
   }
   catch (e)
   {
      logger("ERROR", "caught an exception: $e")
   }
}

private switchControl(nowTime)
{
    def thermostatTemp = thermostat.currentValue("temperature")
    def outletState = outlet.currentSwitch
    def status = ""
    
    if ("on" == outletState)
    {
       if (thermostatTemp <= thermostatOffThresh)
       {
          outlet.off()
	  setOffTime(nowTime)

          status = "turning fan off"
       }
       else
       {
          status = "leaving fan on"
       }
    }
    else // "off" == outletState
    {
       if (thermostatTemp >= thermostatOnThresh &&
           atomicState.outletOff < (nowTime - (offWaitTime * 60000)))
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

private setOffTime(time)
{
    atomicState.outletOff = time

    while (time != atomicState.outletOff)
    {
        logger("WARN", "atomicState.outletOff set didn't work, retrying")
        atomicState.outletOff = time
    }
}

private logger(level, logString)
{
    def msg = "${level} - ${logString}"
    log.info "${msg}"
}
