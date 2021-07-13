/*
blebox tempSensorPro information driver.
*/
//	===== Definitions, Installation and Updates =====
metadata {
	definition (name: "data dump",
				namespace: "davegut",
				author: "Dave Gutheinz",
				importUrl: "https://raw.githubusercontent.com/DaveGut/HubitatActive/master/bleBoxDevices/Drivers/tempSensor.groovy"
			   ) {
		capability "Refresh"
	}
	preferences {
		input ("device_IP", "text", title: "Device IP")
	}
}

def installed() {
	runIn(2, updated)
}

def updated() {
	if (!device_IP) {
		logWarn("updated:  device_IP  is not set.")
		return
	}

	refresh()
}

def refresh() {
	logInfo("STARTING SEQUENCE")
	logInfo("multiSensor[0]")
	sendPostCmd("/api/settings/set",
				"""{"settings":{"multiSensor[0]": """ +
				"""{"settings":{"userTempOffset":10}}}}""", 
				"commandParse")
	pauseExecution(2000)

	logInfo("[settings:{id: 0, offset")
	sendPostCmd("/api/settings/set",
				"""{"settings":{"multiSensor": """ +
				"""[{"id":1,{"settings":{"userTempOffset":20}}}]}}""", 
				"commandParse")
	pauseExecution(2000)
			
	logInfo("[settings:{id: 0, offset, EXPANDED")
	sendPostCmd("/api/settings/set",
				"""{"settings":{"multiSensor": """ +
				"""[{"id":0},""" +
				"""{"id":1,{"settings":{"userTempOffset":30}}},""" +
				"""{"id":2},""" +
				"""{"id":3}]}}""", 
				"commandParse")
	pauseExecution(2000)
			
	logInfo("[settings:{[{},{}], offset, EXPANDED")
	sendPostCmd("/api/settings/set",
				"""{"settings":{"multiSensor": """ +
				"""[{},""" +
				"""{"id":1,{"settings":{"userTempOffset":40}}}""" +
				"""{},""" +
				"""{}]}}""", 
				"commandParse")
	pauseExecution(2000)
			
/*
	logInfo("INFO")
	sendGetCmd("/info", "commandParse")
	pauseExecution(2000)
	logInfo("STATE")
	sendGetCmd("/state", "commandParse")
	pauseExecution(2000)
	logInfo("SETTINGS")
	sendGetCmd("/api/settings/state", "commandParse")
*/
}

def commandParse(response) {
	def cmdResponse = parseInput(response)
	logDebug("commandParse: ${cmdResponse}")
	logWarn("")
}

//	===== Communications =====
private sendGetCmd(command, action){
	logDebug("sendGetCmd: ${command} / ${action} / ${device_IP}")
	sendHubCommand(new hubitat.device.HubAction("GET ${command} HTTP/1.1\r\nHost: ${device_IP}\r\n\r\n",
				   hubitat.device.Protocol.LAN, null,[callback: action]))
}

private sendPostCmd(command, body, action){
	logDebug("sendPostCmd: ${command} / ${body} / ${action})}")
	def parameters = [ method: "POST",
					  path: command,
					  protocol: "hubitat.device.Protocol.LAN",
					  body: body,
					  headers: [
						  Host: getDataValue("deviceIP")
					  ]]
	sendHubCommand(new hubitat.device.HubAction(parameters, null, [callback: action]))
}

def parseInput(response) {
	unschedule(setCommsError)
	try {
		def jsonSlurper = new groovy.json.JsonSlurper()
		return jsonSlurper.parseText(response.body)
	} catch (error) {
		logWarn("CommsError: ${error}.")
		logWarn("")
	}
}

//	===== Utility Methods =====
def logInfo(msg) { log.info "${msg}" }

def logDebug(msg){ log.debug "${msg}" }

def logWarn(msg){ log.warn "${msg}" }

//	end-of-file


