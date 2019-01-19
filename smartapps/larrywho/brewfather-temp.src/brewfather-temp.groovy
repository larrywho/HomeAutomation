/**
 *  Brewfather Temperature - SmartApp v 1.0
 *
 *  Author: 
 *    larrywho
 *
 *  Changelog:
 *
 *    1.0 (01/19/2019 by larrywho)
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
    name: "Brewfather Temperature",
    author: "larrywho",
    namespace: "larrywho",
    description: "Post temperature to Brewfather stream.",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
    section ("Brewfather Temperature ...") {
        input "thermostat", "capability.temperatureMeasurement", title: "Thermostat", required: true, multiple: false
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

    runEvery15Minutes("runApp")
}



def runApp()
{
   try
   {
      logger("DEBUG", "calling postTemperatureData()")
      postTemperatureData()
   }
   catch (e)
   {
      logger("ERROR", "caught an exception: $e")
   }
}

private postTemperatureData()
{
    def thermostatTemp = thermostat.currentValue("temperature")

    def params = [
    uri: "http://postcatcher.in/catchers/<yourUniquePath>",
    body: [
        param1: [subparam1: "subparam 1 value",
                 subparam2: "subparam2 value"],
        param2: "param2 value"
    ]
]

try {
    httpPostJson(params) { resp ->
        resp.headers.each {
            log.debug "${it.name} : ${it.value}"
        }
        log.debug "response contentType: ${resp.contentType}"
    }
} catch (e) {
    log.debug "something went wrong: $e"
}
    logger("INFO", "Thermostat Temperature = ${thermostatTemp}F")
}

private logger(level, logString)
{
    def msg = "${level} - ${logString}"
    log.info "${msg}"
}
