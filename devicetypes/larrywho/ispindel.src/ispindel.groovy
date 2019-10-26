/**
 *  iSpindel Device Handler
 *
 *  Copyright (c) 2019 larrywho
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
 *  2019-10-25  V1.0.0  Initial release
 */

metadata {
    definition (name:"iSpindel", namespace:"larrywho", author:"larrywho") {
        capability "Temperature Measurement"
        capability "Sensor"
        
        attribute "name","string"
        attribute "temperature","string"
        attribute "temp_units","string"
        attribute "tilt","string"
        attribute "gravity","string"
        attribute "signal","string"
        attribute "battery","string"

        // custom commands
        command "parse"     // (String "temperature:<value>")
        command "set"
        command "setName"
        command "setGravity"
        command "setSignalStrength"
        command "setBattery"
        command "setTilt"
        command "setTempUnits"
    }

    tiles(scale:1) {
        valueTile("name", "device.name") {
            state("name", label:'Name\r\n ${currentValue}', defaultState: true)
        }    
        valueTile("temperature", "device.temperature") {
            state("temperature", label:'Temp\r\n ${currentValue}°', defaultState: true)
        }    
        valueTile("temp_units", "device.temp_units") {
            state("temp_units", label:'Units\r\n ${currentValue}', defaultState: true)
        }    
        valueTile("gravity", "device.gravity") {
            state("gravity", label:'Gravity\r\n ${currentValue}', defaultState: true)
        }    
        valueTile("tilt", "device.tilt") {
            state("tilt", label:'Tilt\r\n ${currentValue}°', defaultState: true)
        }    
        valueTile("signal", "device.signal") {
            state("signal", label:'Signal\r\n ${currentValue}', defaultState: true)
        }    
        valueTile("battery", "device.battery") {
            state("battery", label:'Battery\r\n ${currentValue}', defaultState: true)            
        }

        main(["gravity"])
        details(["name","temperature","temp_units","battery","tilt","signal","gravity"])
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
    if (msg.containsKey("name")) {
    def event = [
        name  : "name",
        value : msg.name,
    ]
    }
    if (msg.containsKey("temperature")) {
    def event = [
        name  : "temperature",
        value : msg.temperature,
    ]
    }
    if (msg.containsKey("temp_units")) {
    def event = [
        name  : "temp_units",
        value : msg.temp_units,
    ]
    }
    else if (msg.containsKey("gravity")) {
    def event = [
        name  : "gravity",
        value : msg.gravity,
    ]
    }
    else if (msg.containsKey("tilt")) {
    def event = [
        name  : "tilt",
        value : msg.tilt,
    ]
    }
    else if (msg.containsKey("battery")) {
    def event = [
        name  : "battery",
        value : msg.battery,
    ]
    }
    else if (msg.containsKey("signal")) {
    def event = [
        name  : "signal",
        value : msg.signal,
    ]
    }


    TRACE("event: (${event})")
    sendEvent(event)
}

def setName(value) {
    TRACE("name: ${value}")
    sendEvent(name: "name", value: "${value}")
}

def set(value) {
    TRACE("temperature: ${value}")
    sendEvent(name: "temperature", value: "${value}")
}

def setTempUnits(value) {
    TRACE("temp_units: ${value}")
    sendEvent(name: "temp_units", value: "${value}")
}

def setGravity(value) {
    TRACE("gravity: ${value}")
    sendEvent(name: "gravity", value: "${value}")
}

def setTilt(value) {
    TRACE("tilt: ${value}")
    sendEvent(name: "tilt", value: "${value}")
}

def setBattery(value) {
    TRACE("battery: ${value}")
    sendEvent(name: "battery", value: "${value}")
}
def setSignalStrength(value) {
    TRACE("signal: ${value}")
    sendEvent(name: "signal", value: "${value}")
}

private def TRACE(message) {
    log.debug message
}