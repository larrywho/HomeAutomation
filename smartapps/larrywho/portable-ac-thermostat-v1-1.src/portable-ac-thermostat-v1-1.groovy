/**
 *  Portable AC Thermostat - SmartApp v 1.0
 *
 *  Author: 
 *    larrywho
 *
 *  Changelog:
 *
 *    1.0 (03/31/2017 by larrywho)
 *      - Initial release
 *    1.1 (11/6/2017 by larrywho)
 *      - Modified to turn off less often
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
    name: "Portable AC Thermostat v1.1",
    author: "larrywho",
    namespace: "larrywho",
    description: "Control a portable AC unit using a temperature sensor and a smart outlet.",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
    section ("Portable AC Thermostat Parameters...") {
        input "sensor", "capability.temperatureMeasurement", title: "Temp Sensor", required: true, multiple: false
        input "meter", "capability.powerMeter", title: "Meter", multiple: false, required: true
        input "outlet", "capability.switch", title: "Outlet", required: true, multiple: false
        input "powerThresh", "decimal", title: "Low Power Threshold", required: true
        input "highTempThresh", "decimal", title: "High Temperature Threshold", required: true
        input "fanWaitTime", "decimal", title: "Fan On Max Time", required: true, defaultValue: 30
        input "offWaitTime", "decimal", title: "Unit Off Max Time", required: true, defaultValue: 15
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

    // initialize outlet first time through
    def currentTime = now()
    atomicState.outletOn = currentTime
    atomicState.outletOff = currentTime
    atomicState.lastState = "initialize"
    atomicState.acState = "off"
    atomicState.acTimeInState = currentTime
	
    // turn the outlet on
    turnOutletOn(currentTime)
	
    runEvery1Minute("runApp")
}



def runApp()
{
   try
   {
      def currentTime = now()

      //logger("DEBUG", "calling sensor.refresh()")
      //sensor.refresh()
      meter.refresh()

      logger("DEBUG", "calling outletControl()")
      outletControl(currentTime)
   }
   catch (e)
   {
      logger("ERROR", "caught an exception: $e")
   }
}

private outletControl(nowTime)
{
    def currTemp = sensor.currentValue("temperature")
    def currPower = meter.currentValue("power")
    def switchState = outlet.currentSwitch
    def status = "no status"

    determineAcState(nowTime, currPower)

    // If power less that 10, and the outlet is on, the unit is off by the remote,
    // so leave the outlet on to allow the unit to be turned back on by the remote.
    // The AC unit draws a little power in this state.
    if (currPower < 10 &&
        "on" == switchState)
    {
        logger("DEBUG", "in currPower < 10")
        atomicState.lastState = "unit_off"
        status = "leaving outlet on (AC off)"
    }

    // If power under threshold, and outlet is on, and AC has been in fan state for at least 15 minutes,
    // turn the outlet off.
    // This indicates that the compressor is off, but the fan is running.
    else if (currPower <= powerThresh &&
             "on" == switchState &&
             "fan" == atomicState.acState &&
             atomicState.acTimeInState < (nowTime - (fanWaitTime * 60000)))
    {
        logger("DEBUG", "in currPower <= powerThresh")
        turnOutletOff(nowTime)
        atomicState.lastState = "turning_outlet_off_fan"
        status = "turning outlet off (killing fan)"
    }

    // If temp over threshold, and outlet is off, and has been off for at least 15 minutes,
    // turn the outlet on.
    else if (currTemp > highTempThresh &&
             "off" == switchState &&
             atomicState.outletOff < (nowTime - (offWaitTime * 60000)))
    {
        logger("DEBUG", "in currTemp > highTempThresh")
        turnOutletOn(nowTime)
        atomicState.lastState = "turning_outlet_on"
        status = "turning outlet on"
    }
    
    // Nothing has changed, so leave everything alone.
    else
    {
        logger("DEBUG", " in leave alone")
        status = "leaving outlet ${switchState}"
        atomicState.lastState = "leaving_outlet_alone"
    }

    def timeInState
	
    // compute time in current state for the outlet
    if ("on" == switchState)
    {
       timeInState = ((nowTime - atomicState.outletOn)/1000)/60
    }
    else
    {
       timeInState = ((nowTime - atomicState.outletOff)/1000)/60
    }

    logger("INFO", "Temperature = ${currTemp}F, Power = ${currPower}W, State = ${switchState}, Time = ${timeInState}m, Status = ${status}")
    logger("INFO", "Fan Time = ${nowTime - (fanWaitTime * 60000)}")
    logger("INFO", "Off Time = ${nowTime - (offWaitTime * 60000)}")

}

private setOnTime(time)
{
    atomicState.outletOn = time
    
    while (time != atomicState.outletOn)
    {
        logger("WARN", "atomicState.outletOn set didn't work, retrying")
        atomicState.outletOn = time
    }
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

private turnOutletOn(time)
{
    outlet.on()
    setOnTime(time)
    atomicState.acState = "fan"
    atomicState.acTimeInState = time
}

private turnOutletOff(time)
{
    outlet.off()
    setOffTime(time)
    atomicState.acState = "off"
    atomicState.acTimeInState = time
}

private determineAcState(time, power)
{
    def previousAcState = atomicState.acState
    def previousAcTimeInState = atomicState.acTimeInState

    if (atomicState.acState == "off")
    {
        if (power < 10)
        {
            // do nothing
        }
        else if (power <= powerThresh)
        {
            atomicState.acState = "fan"
            atomicState.acTimeInState = time
        }
        else
        {
            atomicState.acState = "compressor"
            atomicState.acTimeInState = time
        }
    }
    else if (atomicState.acState == "fan")
    {
        if (power < 10)
        {
            atomicState.acState = "off"
            atomicState.acTimeInState = time
        }
        else if (power <= powerThresh)
        {
            // do nothing
        }
        else
        {
            atomicState.acState = "compressor"
            atomicState.acTimeInState = time
        }
    }
    else
    {
        if (power < 10)
        {
            atomicState.acState = "off"
            atomicState.acTimeInState = time
        }
        else if (power <= powerThresh)
        {
            atomicState.acState = "fan"
            atomicState.acTimeInState = time
        }
        else
        {
            // do nothing
        }
    }
    
    def timeInState = ((time - atomicState.acTimeInState)/1000)/60

    logger("INFO", "Previous AC State = ${previousAcState}, Previous AC Time In State = ${previousAcTimeInState}, Current AC State = ${atomicState.acState}, Current AC Time In State = ${atomicState.acTimeInState}, AC Time In State = ${timeInState}m")
}