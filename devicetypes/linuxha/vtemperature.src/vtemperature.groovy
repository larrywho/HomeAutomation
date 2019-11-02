/**
 *  Virtual Temperature Tile
 *
 *  Copyright (c) 2014 Statusbits.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may
 *  not use this file except in compliance with the License. You may obtain a
 *  copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *  License  for the specific language governing permissions and limitations
 *  under the License.
 *
 *  The latest version of this file can be found at:
 *  https://github.com/statusbits/smartthings/blob/master/VirtualThings/VirtualTemperatureTile.device.groovy
 *
 *  2014-08-28  V1.1.0  parse takes 'temperature:<value>' as an argument
 *  2014-08-10  V1.0.0  Initial release
 */

metadata {
    definition (name:"vTemperature", namespace:"linuxha", author:"geko@statusbits.com") {
        capability "Temperature Measurement"
        capability "Sensor"
        
        attribute "date","string"

        // custom commands
        command "parse"     // (String "temperature:<value>")
        command "set"
        command "setDate"
    }

    tiles(scale:1) {
        valueTile("date", "device.date") {
            state("date", label:'${currentValue}', defaultState: true)
        }    
        valueTile("temperature", "device.temperature") {
            state("temperature", label:'${currentValue}°', defaultState: true)
        }    

        main(["temperature"])
        details(["date","temperature"])
    }

    simulator {
        for (int i = 20; i <= 110; i += 10) {
            status "Temperature ${i}°": "temperature:${i}"
        }
        status "Invalid message" : "foobar:100.0"
    }
}

def parse(String message) {
    TRACE("parse(${message})")

    Map msg = stringToMap(message)
    if (!msg.containsKey("temperature")) {
        log.error "Invalid message: ${message}"
        return null
    }

    Float temp = msg.temperature.toFloat()
    def event = [
        name  : "temperature",
        value : temp.round(1),
        unit  : tempScale,
    ]

    TRACE("event: (${event})")
    sendEvent(event)
}

def set(value) {
    TRACE("vTemperature: ${value}")
    sendEvent(name: "temperature", value: "${value}")
}

def setDate(value) {
    TRACE("date: ${value}")
    sendEvent(name: "date", value: "${value}")
}

private def TRACE(message) {
    log.debug message
}