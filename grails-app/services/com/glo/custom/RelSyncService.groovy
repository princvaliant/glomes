package com.glo.custom

import com.mongodb.BasicDBObject
import grails.util.Environment
import org.apache.commons.logging.LogFactory

import java.text.DecimalFormat

class RelSyncService {

	private static final logr = LogFactory.getLog(this)

	def detailDataSyncService
	def unitService
	def fileService
	def standardSpcService
	def mongo
	
	def transactional = true

	
	def addLampData (def units, def dirs, def tkey) {
		
		!logr.isDebugEnabled() ?: logr.debug("lamp test sync job started.")
		
		def db = mongo.getDB("glo")
		
		units.each { k, v ->
			
			def query =	new BasicDBObject("code", k)
			def unit = db.unit.find(query, new BasicDBObject()).collect{it}[0]
			if (unit) {
			
				def queryTest =	new BasicDBObject("value.code", k)
				queryTest.put("value.tkey", "sphere_test_lamp")
				queryTest.put("value.testId", 1)
				def savedHeader
				def savedEncap
				
				def testData = db.testData.find(queryTest, new BasicDBObject()).collect{it}[0]
				if (!testData) {
					testData = new BasicDBObject()
				} else {
					savedHeader = testData?.value?.data?.Header
					savedEncap = testData?.value?.data?.Encap
				}
				
				def tValue = new BasicDBObject()
				tValue.put("code", k)
				tValue.put("experimentId", unit?.experimentId)
				tValue.put("parentCode", k.tokenize("_")[0])
				tValue.put("testId", 1)
				tValue.put("tkey", "sphere_test_lamp")
				
				def dataAtLamp = new TreeMap()
				v.each { k1, v1 ->
					v1.each {
						def data = readTestFileForRel(it.file)
						
						if (!dataAtLamp.containsKey(k1)) {
							dataAtLamp.put(k1, new TreeMap())
						}
						if (!dataAtLamp[k1].containsKey("setCurrent")) {
							dataAtLamp[k1].put("setCurrent", new TreeMap())
						}
						if (!dataAtLamp[k1].containsKey("setVoltage")) {
							dataAtLamp[k1].put("setVoltage", new TreeMap())
						}
											
						if (data.containsKey("setCurrent")) {
							dataAtLamp[k1]["setCurrent"].put((data["setCurrent"]*1000).toInteger().toString(), data)
						} else
						if (data.containsKey("setVoltage")) {
							dataAtLamp[k1]["setVoltage"].put((data["setVoltage"]*1E6).toInteger().toString(), data)
						}
					}
				}
				
				tValue.put("data", [:])
								
				if (savedHeader) 
					tValue["data"].put("Header", savedHeader)
				if (savedEncap)  
					tValue["data"].put("Encap", savedEncap)
				
				def bdoUnit = new BasicDBObject()
					
				if (dataAtLamp["Header"]) {
					tValue["data"].put("Header", dataAtLamp["Header"])
					bdoUnit.put("lampTestSynced", "YES")
				}
				if (dataAtLamp["Encap"]) { 
					tValue["data"].put("Encap", dataAtLamp["Encap"])
					bdoUnit.put("lampTest2Synced", "YES")
				}
				
				testData.put("value", tValue)
				db.testData.save(testData)
				testData = null
				
				bdoUnit.put("id", unit["_id"])
				bdoUnit.put("lampTestData", [unit.code])
				bdoUnit.put("processCategory", "Die")
				bdoUnit.put("processKey", "packaging")
				bdoUnit.put("taskKey", tkey)
				unitService.update(bdoUnit, "admin", true)
				
				dataAtLamp = null
				tValue = null
			}
		}
		
		!logr.isDebugEnabled() ?: logr.debug("lamp test sync job ended.")
	}
	
	def addRelBoardData (def units, def dirs) {
		
		!logr.isDebugEnabled() ?: logr.debug("rel board test sync job started.")
		
		def db = mongo.getDB("glo")
		
		def codesForSpc = [:]
				
		units.each { k, v ->
			
			def unitId = -1
			def relGroup = ""
			def dataAtHours = new TreeMap()

			def queryTest =	new BasicDBObject("value.code", k)
			queryTest.put("value.tkey", "test_rel")
			queryTest.put("value.testId", 1)
			def testData = db.testData.find(queryTest, new BasicDBObject()).collect{it}[0]
			if (!testData) {
				testData = new BasicDBObject()
			} else {
				testData["value"]["data"].each { hours, data ->
					dataAtHours.put(padZeros(hours), data)
				}
			}
			
			def tValue = new BasicDBObject()
			tValue.put("code", k)
			
			def parentCode =  k.tokenize("_")[0]
			tValue.put("parentCode",parentCode)			
			tValue.put("testId", 1)
			tValue.put("tkey", "test_rel")
			
			v.each { k1, v1 ->
			
					def hour = padZeros(k1)
					v1.each {
						if (!tValue["experimentId"]) {
							relGroup = it.boardId + "_" + it.index
							unitId = it.unit["_id"]
							tValue.put("experimentId", it.unit.experimentId)
							tValue.put("boardId", it.boardId)
							tValue.put("posOnBoard", it.posOnBoard)
							tValue.put("testType", it.testType)
							tValue.put("relGroup", relGroup)
							tValue.put("index", it.index)

						}
						
						if (!codesForSpc.containsKey(relGroup))
							codesForSpc.put(relGroup, [:])
						if (it.testType == "STD" && !codesForSpc[relGroup].containsKey(hour)) {
							codesForSpc[relGroup].put(hour, it.testDate)
						}
						
						def data = readTestFileForRelBoard(it.file, it.testType)
						
						if (data.size() > 0) {
							if (!dataAtHours.containsKey(hour)) {
								dataAtHours.put(hour, new TreeMap())
							}
							
							if (!dataAtHours[hour].containsKey("setCurrent")) {
								dataAtHours[hour].put("setCurrent", new TreeMap())							
							}
							if (!dataAtHours[hour].containsKey("setVoltage")) {
								dataAtHours[hour].put("setVoltage", new TreeMap())
							}
		
							if (it.dataType == "SPECTRUM") {
								def curr = (data["setCurrent"]*1000).toInteger()
								dataAtHours[hour]["setCurrent"].put(curr.toString(), data)
							}
							if (it.dataType == "VOLTAGE" && it.testType != "STD") {
								data.setVoltage.each { volt ->
									dataAtHours[hour]["setVoltage"].put((volt[0]*1E6).toInteger().toString(), [current: volt[1]])
								}
							}
						}
					}
				}
			
			
			if (!dataAtHours.containsKey(padZeros(0)) && testData?.value?.data && testData?.value?.data[padZeros(0)]) {
				dataAtHours.put(padZeros(0), new TreeMap())
				dataAtHours[padZeros(0)].put("setCurrent", new TreeMap())
				testData?.value?.data[padZeros(0)]["setCurrent"].each { k2, v2 ->
					dataAtHours[padZeros(0)]["setCurrent"].put(k2.toString(),v2)
				}
			}
			
			dataAtHours.keySet().each {hour ->
				
				def dobj = dataAtHours[hour]["setCurrent"]
				dobj.each { val, data ->
					
					if (val && data) {
						def dataAtZero
						if (dataAtHours[padZeros(0)] && dataAtHours[padZeros(0)]["setCurrent"]) {
							dataAtZero = dataAtHours[padZeros(0)]["setCurrent"][val]
						}
						if (data && dataAtZero) {
							data.put("lopDelta", data.lop - dataAtZero.lop)
							data.put("eqeDelta", data.eqe - dataAtZero.eqe)
							data.put("voltageDelta", data.voltage - dataAtZero.voltage)
							data.put("peakDelta", data.peak - dataAtZero.peak)
							data.put("centroidDelta", data.centroid - dataAtZero.centroid)
							data.put("dominantDelta", data.dominant - dataAtZero.dominant)
							data.put("fwhmDelta", data.fwhm - dataAtZero.fwhm)
							
							data.put("lopPerc", dataAtZero.lop != 0 ? 100 * (data.lop - dataAtZero.lop)/ dataAtZero.lop : '')
							data.put("eqePerc",  dataAtZero.eqe != 0 ? 100 * (data.eqe - dataAtZero.eqe)/ dataAtZero.eqe : '')
							data.put("voltagePerc", dataAtZero.voltage != 0 ? 100 * (data.voltage - dataAtZero.voltage)/ dataAtZero.voltage : '')
							data.put("peakPerc", dataAtZero.peak != 0 ? 100 * (data.peak - dataAtZero.peak)/ dataAtZero.peak : '')
							data.put("centroidPerc", dataAtZero.centroid != 0 ? 100 * (data.centroid - dataAtZero.centroid)/ dataAtZero.centroid : '')
							data.put("dominantPerc", dataAtZero.dominant != 0 ? 100 * (data.dominant - dataAtZero.dominant)/ dataAtZero.dominant : '')
							data.put("fwhmPerc",  dataAtZero.fwhm != 0 ? 100 * (data.fwhm - dataAtZero.fwhm)/ dataAtZero.fwhm : '')
						}
					}
				}
			}

			tValue.put("data", dataAtHours)
			testData.put("value", tValue)
			db.testData.save(testData)
			testData = null
			
			def bdoUnit = new BasicDBObject()
			bdoUnit.put("id", unitId)
			bdoUnit.put("relTestSynced", "YES")
			bdoUnit.put("relTestData", [k])
			bdoUnit.put("relGroup", relGroup)
			bdoUnit.put("processCategory", "Die")
			bdoUnit.put("processKey", "packaging")
			bdoUnit.put("taskKey", "test_rel_board")
			unitService.update(bdoUnit, "admin", true)
			
			dataAtHours = null
			tValue = null
		}

		standardSpcService.init(codesForSpc)
		
		deleteDirs(dirs)
	}
	
	
	def addRelWaferData (def units, def dirs) {
		
		!logr.isDebugEnabled() ?: logr.debug("rel wafer test sync job started.")
		
		def db = mongo.getDB("glo")
		def unitList = [:]
		
		units.each { k, v ->
			
			def relGroup = ""
			def dataAtHours = new TreeMap()
			
			def queryTest =	new BasicDBObject("value.code", k)
			queryTest.put("value.tkey", "wafer_rel_test")
			queryTest.put("value.testId", 1)
			def testData = db.testData.find(queryTest, new BasicDBObject()).collect{it}[0]
			if (!testData) {
				testData = new BasicDBObject()
			} else {
				testData["value"]["data"].each { hours, data ->
										
					dataAtHours.put(padZeros(hours), data)
				}
			}
			
			def tValue = new BasicDBObject()
			tValue.put("code", k)
			tValue.put("testId", 1)
			tValue.put("tkey", "wafer_rel_test")
			
			v.each { k1, v1 ->
			
				def hour = padZeros(k1)
			
				v1.each {
					
					def burningPos =  v1?.unit[0].burningPosition
					def mask =  v1?.unit[0].mask
					if (burningPos && mask) {
					
						if (!tValue.containsKey("relGroup")) {
							tValue.put("experimentId", v1?.unit[0].experimentId)
							tValue.put("relGroup", v1?.unit[0].code)
							tValue.put("parentCode", v1?.unit[0].code)
							tValue.put("burning", isBurning(mask,burningPos,k))
						}
						
						if (!unitList.containsKey(v1.unit[0])) {
							unitList.put(v1.unit[0], dataAtHours)
						}
						
						def data = readTestFileForRelBoard(it.file, "")
						
						if (data.size() > 0) {
							if (!dataAtHours.containsKey(hour)) {
								dataAtHours.put(hour, new TreeMap())
							}
							if (!dataAtHours[hour].containsKey("setCurrent")) {
								dataAtHours[hour].put("setCurrent", new TreeMap())
							}
							if (!dataAtHours[hour].containsKey("setVoltage")) {
								dataAtHours[hour].put("setVoltage", new TreeMap())
							}
		
							if (it.dataType == "SPECTRUM") {
								dataAtHours[hour]["setCurrent"].put((it.current*1000000).round(0).toInteger().toString(), data)
							}
							if (it.dataType == "VOLTAGE") {
								data.setVoltage.each { volt ->
									dataAtHours[hour]["setVoltage"].put((volt[0]*1E6).toInteger().toString(), [current: volt[1]])
								}
							}
						}
					}
				}
			}
			
			if (!dataAtHours.containsKey(padZeros(0)) && testData?.value?.data && testData?.value?.data[padZeros(0)]) {
				dataAtHours.put(padZeros(0), new TreeMap())
				dataAtHours[padZeros(0)].put("setCurrent", new TreeMap())
				testData?.value?.data[padZeros(0)]["setCurrent"].each { k2, v2 ->
					dataAtHours[padZeros(0)]["setCurrent"].put(k2.toString(),v2)
				}
			}
			
			Boolean atleastOne = false
			dataAtHours.keySet().each {hour ->
				
				if (dataAtHours[hour]) {
					def dobj = dataAtHours[hour]["setCurrent"]
					if (dobj) {
					
						dobj.each { val, data ->
							
							atleastOne = true
							if (val && data && data.lop && data.peak && data.centroid && data.fwhm ) {
								
								def dataAtZero
								if (dataAtHours[padZeros(0)] && dataAtHours[padZeros(0)]["setCurrent"]) {
									dataAtZero = dataAtHours[padZeros(0)]["setCurrent"][val]
								}
								if (dataAtZero) {
									data.put("lopDelta", data.lop - (dataAtZero.lop ?: 0))
									data.put("eqeDelta", data.eqe - (dataAtZero.eqe ?: 0))
									data.put("voltageDelta", data.voltage - (dataAtZero.voltage ?: 0))
									data.put("peakDelta", data.peak - (dataAtZero.peak ?: 0))
									data.put("centroidDelta", data.centroid - (dataAtZero.centroid ?: 0))
									data.put("dominantDelta", data.dominant - (dataAtZero.dominant ?: 0))
									data.put("fwhmDelta", data.fwhm - (dataAtZero.fwhm ?: 0))
									
									data.put("lopPerc",  dataAtZero.lop && dataAtZero.lop != 0 ? 100 * (data.lop - dataAtZero.lop)/ dataAtZero.lop : '')
									data.put("eqePerc",  dataAtZero.eqe && dataAtZero.eqe != 0 ? 100 * (data.eqe - dataAtZero.eqe)/ dataAtZero.eqe : '')
									data.put("voltagePerc", dataAtZero.voltage && dataAtZero.voltage != 0 ? 100 * (data.voltage - dataAtZero.voltage)/ dataAtZero.voltage : '')
									data.put("peakPerc", dataAtZero.peak && dataAtZero.peak != 0 ? 100 * (data.peak - dataAtZero.peak)/ dataAtZero.peak : '')
									data.put("centroidPerc", dataAtZero.centroid && dataAtZero.centroid != 0 ? 100 * (data.centroid - dataAtZero.centroid)/ dataAtZero.centroid : '')
									data.put("dominantPerc", dataAtZero.dominant && dataAtZero.dominant != 0 ? 100 * (data.dominant - dataAtZero.dominant)/ dataAtZero.dominant : '')
									data.put("fwhmPerc", dataAtZero.fwhm &&  dataAtZero.fwhm != 0 ? 100 * (data.fwhm - dataAtZero.fwhm)/ dataAtZero.fwhm : '')
								}
							}
						}
					}
				}
			}
						
			if (atleastOne == true && dataAtHours.size() > 0) {
				tValue.put("data", dataAtHours)
				testData.put("value", tValue)
				db.testData.save(testData)
				testData = null
			}
			
			dataAtHours = null
			tValue = null
			testData = null

		}
		
		unitList.each {  unit, hours ->
			
			def bdoUnit = new BasicDBObject()
			bdoUnit.put("id", unit["_id"])
			
			def thours = hours.collect{it.key.toInteger().toString()}.join(",")
			
			bdoUnit.put("testedHours", thours)
			bdoUnit.put("relWaferSynced", "YES")
			bdoUnit.put("relWaferData", [unit["code"]])
			bdoUnit.put("relGroup", unit["code"])
			bdoUnit.put("processCategory", "nwLED")
			bdoUnit.put("processKey", "test")
			bdoUnit.put("taskKey", "wafer_rel_test")
			unitService.update(bdoUnit, "admin", true)
		}
	}
	
	def addLampBoardData (def units, def dirs, def tkey) {
		
		!logr.isDebugEnabled() ?: logr.debug("Lamp board test sync job started.")
		
		def db = mongo.getDB("glo")
		
		units.each { k, v ->
			
			def unitId = -1
			def relGroup = ""
			def dataAtHours = new TreeMap()
			
			def queryTest =	new BasicDBObject("value.code", k)
			queryTest.put("value.tkey", "sphere_test_lamp_board")
			queryTest.put("value.testId", 1)
			def testData = db.testData.find(queryTest, new BasicDBObject()).collect{it}[0]
			if (!testData) {
				testData = new BasicDBObject()
			} else {
				testData["value"]["data"].each { hours, data ->
					dataAtHours.put(hours, data)
				}
			}
			
			def tValue = new BasicDBObject()
			tValue.put("code", k)
			tValue.put("parentCode", k.tokenize("_")[0])
			tValue.put("testId", 1)
			tValue.put("tkey", "sphere_test_lamp_board")
			
			def kFactorsH = [:]
			def kFactorsE = [:]
			def eqeH = [:]
			def eqeE = [:]
			def stH = ""
			def stE = ""
			
			v.each { k1, v1 ->
			
					def hour = k1
					
					if (hour == "Header")
						stH= "1"
					if (hour == "Encap")
						stE= "1"
									
					v1.each {
						if (!tValue["experimentId"]) {
							unitId = it.unit["_id"]
							tValue.put("experimentId", it.unit.experimentId)
							tValue.put("testType", "LAMP")
						}
						
						def data = readTestFileForRelBoard(it.file, "")
						if (data.size() > 0) {
							if (!dataAtHours.containsKey(hour)) {
								dataAtHours.put(hour, new TreeMap())
							}
							if (!dataAtHours[hour].containsKey("setCurrent")) {
								dataAtHours[hour].put("setCurrent", new TreeMap())
							}
							if (!dataAtHours[hour].containsKey("setVoltage")) {
								dataAtHours[hour].put("setVoltage", new TreeMap())
							}
							
							
							if ( data["setCurrent"].toString() in ["1.0","5.0","10.0","20.0","50.0","100.0"])
							{
								def td = getLopTestData (db, k.tokenize("_")[0], k.tokenize("_")[1])
								
								Float curr = data["setCurrent"]
								if (td[curr] && td[curr] != 0) {
									float calc = data["eqe"]/td[curr]
									if (hour == "Header") {
										kFactorsH.put(curr.toInteger(), calc)
										eqeH.put(curr.toInteger(), data["eqe"])
									}
									if (hour == "Encap") {
										kFactorsE.put(curr.toInteger(), calc)
										eqeE.put(curr.toInteger(), data["eqe"])
									}
								}
							}
		
							if (it.dataType == "SPECTRUM")
								dataAtHours[hour]["setCurrent"].put((data["setCurrent"]*1000).toInteger().toString(), data)
							if (it.dataType == "VOLTAGE") {
								data.setVoltage.each { volt ->
									dataAtHours[hour]["setVoltage"].put((volt[0]*1E6).toInteger().toString(), [current: volt[1]])
								}
							}
						}
					}
				}
			
			
			tValue.put("data", dataAtHours)
			testData.put("value", tValue)
			db.testData.save(testData)
			testData = null
			
			def bdoUnit = new BasicDBObject()
			bdoUnit.put("id", unitId)
			
			bdoUnit.put("lampBoardTestData", [k])
			bdoUnit.put("processCategory", "Die")
			bdoUnit.put("processKey", "packaging")
			
			kFactorsH.each { current, kFactor ->
				bdoUnit.put("kFactor" + current.toInteger() + "_H", kFactor)
			}
			eqeH.each { current, eqe ->
				bdoUnit.put("eqe" + current.toInteger() + "_H", eqe)
			}
			if (stH == "1") {
				bdoUnit.put("lampBoardTestSynced", "YES")
				bdoUnit.put("taskKey", "sphere_test_lamp_board")
				unitService.update(bdoUnit, "admin", true)
			}
			kFactorsE.each { current, kFactor ->
				bdoUnit.put("kFactor" + current.toInteger() + "_E", kFactor)
			}
			eqeE.each { current, eqe ->
				bdoUnit.put("eqe" + current.toInteger() + "_E", eqe)
			}
			if (stE == "1") {
				bdoUnit.put("lampBoardTest2Synced", "YES")
				bdoUnit.put("taskKey", "sphere_test2_lamp_board")
				unitService.update(bdoUnit, "admin", true)
			}
			
			dataAtHours = null
			tValue = null
		}
	}

	def addRelData (def units, def dirs) {
		
		!logr.isDebugEnabled() ?: logr.debug("rel test sync job started.")
		
		
		def db = mongo.getDB("glo")
		
		units.each { k, v ->
			
			def query =	new BasicDBObject("code", k)
			def unit = db.unit.find(query, new BasicDBObject()).collect{it}[0]
			if (unit) {
			
				def dataAtHours = new TreeMap()
				def queryTest =	new BasicDBObject("value.code", k)
				queryTest.put("value.tkey", "test_rel")
				queryTest.put("value.testId", 1)
							
				def testData = db.testData.find(queryTest, new BasicDBObject()).collect{it}[0]
				if (!testData) {
					testData = new BasicDBObject()
				} else {
					testData["value"]["data"].each { hours, data -> 
						dataAtHours.put(hours.toString().toInteger(), data)
					}
				}
				
				def tValue = new BasicDBObject()
				tValue.put("code", k)
				tValue.put("experimentId", unit?.experimentId)
				tValue.put("relGroup", unit?.relGroup)
				tValue.put("parentCode", k.tokenize("_")[0])
				tValue.put("testId", 1)
				tValue.put("tkey", "test_rel")
				

				v.each { k1, v1 ->
					v1.each { 
						
						def data = readTestFileForRel(it.file)
						
						if (!dataAtHours.containsKey(k1)) {
							dataAtHours.put(k1, new TreeMap())
						}
						if (!dataAtHours[k1].containsKey("setCurrent")) {
							dataAtHours[k1].put("setCurrent", new TreeMap())
						}
						if (!dataAtHours[k1].containsKey("setVoltage")) {
							dataAtHours[k1].put("setVoltage", new TreeMap())
						}
											
						if (data.containsKey("setCurrent")) {
							dataAtHours[k1]["setCurrent"].put((data["setCurrent"]*1000).toInteger().toString(), data)
						} else 
						if (data.containsKey("setVoltage")) {
							dataAtHours[k1]["setVoltage"].put((data["setVoltage"]*1E6).toInteger().toString(), data)
						}
					}
				}
				
				if (!dataAtHours.containsKey(0) && testData?.value?.data && testData?.value?.data["0"]) {
					dataAtHours.put(0, new TreeMap())
					dataAtHours[0].put("setCurrent", new TreeMap())
					dataAtHours[0].put("setVoltage", new TreeMap())
					testData?.value?.data["0"]["setCurrent"].each { k2, v2 ->  
						dataAtHours[0]["setCurrent"].put(k2,v2)
					}
					testData?.value?.data["0"]["setVoltage"].each { k2, v2 ->
						dataAtHours[0]["setVoltage"].put(k2,v2)
					}						
				}
				
				dataAtHours.keySet().each {hour ->
					dataAtHours[hour].keySet().each { setVal ->  
						def dobj = dataAtHours[hour][setVal]
						dobj.each { val, data ->
							if (val && data) {
								def dataAtZero
								if (dataAtHours[0] && dataAtHours[0][setVal]) {
									dataAtZero = dataAtHours[0][setVal][val]
								}
								if (dataAtZero) {							
									data.put("lopDelta", data.lop - dataAtZero.lop)
									data.put("eqeDelta", data.eqe - dataAtZero.eqe)
									data.put("voltageDelta", data.voltage - dataAtZero.voltage)
									data.put("peakDelta", data.peak - dataAtZero.peak)
									data.put("centroidDelta", data.centroid - dataAtZero.centroid)
									data.put("dominantDelta", data.dominant - dataAtZero.dominant)
									data.put("fwhmDelta", data.fwhm - dataAtZero.fwhm)
									
									data.put("lopPerc", dataAtZero.lop != 0 ? 100 * (data.lop - dataAtZero.lop)/ dataAtZero.lop : '')
									data.put("eqePerc",  dataAtZero.eqe != 0 ? 100 * (data.eqe - dataAtZero.eqe)/ dataAtZero.eqe : '')
									data.put("voltagePerc", dataAtZero.voltage != 0 ? 100 * (data.voltage - dataAtZero.voltage)/ dataAtZero.voltage : '')
									data.put("peakPerc", dataAtZero.peak != 0 ? 100 * (data.peak - dataAtZero.peak)/ dataAtZero.peak : '')
									data.put("centroidPerc", dataAtZero.centroid != 0 ? 100 * (data.centroid - dataAtZero.centroid)/ dataAtZero.centroid : '')
									data.put("dominantPerc", dataAtZero.dominant != 0 ? 100 * (data.dominant - dataAtZero.dominant)/ dataAtZero.dominant : '')
									data.put("fwhmPerc",  dataAtZero.fwhm != 0 ? 100 * (data.fwhm - dataAtZero.fwhm)/ dataAtZero.fwhm : '')
								}
							}
						}
					}
				}
	
				tValue.put("data", dataAtHours)
				testData.put("value", tValue)
				db.testData.save(testData)
				testData = null
				
				def bdoUnit = new BasicDBObject()
				bdoUnit.put("id", unit["_id"])
				bdoUnit.put("relTestSynced", "YES")
				bdoUnit.put("relTestData", [unit.code])
				bdoUnit.put("processCategory", "Die")
				bdoUnit.put("processKey", "packaging")
				bdoUnit.put("taskKey", "test_rel")
				unitService.update(bdoUnit, "admin", true)
			}
		}	
		
		deleteDirs(dirs)
		
		!logr.isDebugEnabled() ?: logr.debug("rel test sync job ended.")
	}

	
	public def readTestFileForRel (def file) {
		
		FileReader fr = null
		BufferedReader br = null
		def dbo = new BasicDBObject()
		try {
			fr = new FileReader(file)
			br = new BufferedReader(fr)
			def arr = []
			def line
			def isData = false
			while ((line = br.readLine()) !=null) {
				if (isData == true) {
					def row = line.split('\t')
					if (row.length == 2) {
						def v1 = row[0]?.trim()
						def v2 = row[1]?.trim()
						if (v1 && v2 && v1.isFloat() && v2.isFloat()) {
							def v1d = v1.toDouble()
							if (v1d >= 350 && v1d <= 670) {
								arr.add([v1d, v2.toDouble()])
							}
						}
					}
				} else {
					def row2 = line.split('=')
					if (line.toUpperCase().indexOf("CURRENTSOURCE/SOURCECURRENT") >= 0 && row2.length == 2) {
						dbo.put("setCurrent", row2[1].trim().toDouble())
					}
					if (line.toUpperCase().indexOf("CURRENTSOURCE/SOURCEVOLTAGE") >= 0 && row2.length == 2) {
						dbo.put("setVoltage", row2[1].trim().toDouble())
					}
					if (line.toUpperCase().indexOf("CURRENTSOURCE/CURRENT") >= 0 && row2.length == 2) {
						dbo.put("current", row2[1].trim().toDouble())
					}
					if (line.toUpperCase().indexOf("CURRENTSOURCE/VOLTAGE") >= 0 && row2.length == 2) {
						dbo.put("voltage", row2[1].trim().toDouble())
					}
					if (line.toUpperCase().indexOf("RADIOMETRIC [W]") >= 0 && row2.length == 2) {
						dbo.put("lop", row2[1].trim().toDouble())
					}
					if (line.toUpperCase().indexOf("PHOTOMETRIC [LM]") >= 0 && row2.length == 2) {
						dbo.put("photometric", row2[1].trim().toDouble())
					}
					if (line.toUpperCase().indexOf("PEAKWAVELENGTH") >= 0 && row2.length == 2) {
						dbo.put("peak", row2[1].trim().toDouble())
					}
					if (line.toUpperCase().indexOf("CENTROIDWAVELENGTH") >= 0 && row2.length == 2) {
						dbo.put("centroid", row2[1].trim().toDouble())
					}
					if (line.toUpperCase().indexOf("DOMINANTWAVELENGTH") >= 0 && row2.length == 2) {
						dbo.put("dominant", row2[1].trim().toDouble())
					}
					if (line.toUpperCase().indexOf("WIDTH50") >= 0 && row2.length == 2) {
						dbo.put("fwhm", row2[1].trim().toDouble())
					}
				}
								
				if (line.length() == 4 && line.substring(0,4) == "Data") {
					isData = true
				}
			}
			
			if (dbo["lop"] && dbo["centroid"] && dbo["current"] && dbo["current"] != 0) {
				dbo.put("eqe", 80.66 * dbo["lop"] * dbo["centroid"] / dbo["current"])
			} else {
				dbo.put("eqe", 0)
			}
						
			dbo.put("spectrum", arr)
			
		} catch (Exception exc) {
			throw new RuntimeException(exc.getMessage())
		}
		finally {
			if (br != null)
				br.close()
			if (fr != null)
				fr.close()
			br = null
			fr = null
		}

		dbo
	}
	
	public def readTestFileForRelBoard (def file, def testType) {
		
		FileReader fr = null
		BufferedReader br = null
		def dbo = new BasicDBObject()
		try {
			fr = new FileReader(file)
			br = new BufferedReader(fr)
			def line
			def arrSpectrum = []
			def arrVoltage = []
			def isDataSpectrum = false
			def isDataVoltage = false
			while ((line = br.readLine()) !=null) {
				if (isDataSpectrum == true) {
					
					if (line.trim().indexOf("BELOW LIMIT") >= 0) {
						return new BasicDBObject()
					}
					
					def row = line.split('\t')
					if (row.length == 2) {
						def v1 = row[0]?.trim()
						def v2 = row[1]?.trim()
						if (v1 && v2 && v1.isFloat() && v2.isFloat()) {
							def v1d = v1.toDouble()
							if (v1d >= 350 && v1d <= 670 && v1d.toInteger().mod(2) == 0) {
								arrSpectrum.add([v1d, v2.toDouble()])
							}
						}
					}
				} else if (isDataVoltage == true) {
					def row = line.split('\t')
					if (row.length == 2) {
						def v1 = row[0]?.trim()
						def v2 = row[1]?.trim()
						if (v1 && v2 && v1.isFloat() && v2.isFloat()) {
							def v1d = v1.toDouble()
							arrVoltage.add([v1d, v2.toDouble()])
						}
					}
				} else {
					def row2 = line.split('\t')
					if ((line.toUpperCase().indexOf("DC CURRENT (MA)") >= 0 || line.toUpperCase().indexOf("PULSE CURRENT (MA)") >= 0) && row2.length == 2) {
						def roundCurr =row2[1].trim().toDouble().round(1)
						if (roundCurr == 0) {
							roundCurr = row2[1].trim().toDouble().round(3)
						}
						dbo.put("setCurrent", roundCurr)
					}
					if ((line.toUpperCase().indexOf("DC CURRENT (MA)") >= 0 || line.toUpperCase().indexOf("PULSE CURRENT (MA)") >= 0) && row2.length == 2) {
						dbo.put("current", row2[1].trim().toDouble())
					}
					if ((line.toUpperCase().indexOf("DC VOLTAGE (V)") >= 0 || line.toUpperCase().indexOf("PULSE VOLTAGE (V)") >= 0) && row2.length == 2) {
						dbo.put("voltage", row2[1].trim().toDouble())
					}
					if ((line.toUpperCase().indexOf("INT POWER (W)") >= 0 || line.toUpperCase().indexOf("INT. POWER (W)") >= 0) && row2.length == 2) {
						dbo.put("lop", row2[1].trim().toDouble())
					}
					if ((line.toUpperCase().indexOf("PHOTOINT POWER (LM)") >= 0 || line.toUpperCase().indexOf("PHOINT POWER (LM)") >= 0)  && row2.length == 2) {   
						dbo.put("photometric", row2[1].trim().toDouble())
					}
					if (line.toUpperCase().indexOf("PEAK (NM)") >= 0 && row2.length == 2) {
						dbo.put("peak", row2[1].trim().toDouble())
					}
					if (line.toUpperCase().indexOf("CENTROID (NM)") >= 0 && row2.length == 2) {
						dbo.put("centroid", row2[1].trim().toDouble())
					}
					if (line.toUpperCase().indexOf("DOMINANT WL (NM)") >= 0 && row2.length == 2) {
						dbo.put("dominant", row2[1].trim().toDouble())
					}
					if (line.toUpperCase().indexOf("FWHM (NM)") >= 0 && row2.length == 2) {
						dbo.put("fwhm", row2[1].trim().toDouble())
					}
					if (line.toUpperCase().indexOf("U'") >= 0 && row2.length == 2) {
						dbo.put("u", row2[1].trim().toDouble())
					}
					if (line.toUpperCase().indexOf("V'") >= 0 && row2.length == 2) {
						dbo.put("v", row2[1].trim().toDouble())
					}
					if (line.toUpperCase().indexOf("OSA INT. TIME (MS)") >= 0 && row2.length == 2) {
						dbo.put("osaTime", row2[1].trim().toDouble())
					}
					if (line.toUpperCase().indexOf("OSA COUNTS") >= 0 && row2.length == 2) {
						dbo.put("osaCounts", row2[1].trim().toDouble())
					}
					if (line.toUpperCase().indexOf("NOTE") >= 0 && row2.length == 2) {
				
						(row2[1] =~ /(\d+)C/).each { match, digit -> 
							dbo.put("burnTemperature", digit as Integer)
						}
						(row2[1] =~ /(\d+)mA/).each { match, digit ->
							dbo.put("burnCurrent", digit as Integer)
						}
					}
				}
					
				if (line.toUpperCase().indexOf("DATA - VOLTAGE SWEEP") >= 0) {
					isDataVoltage = true
				} else
				if (line.toUpperCase().indexOf("DATA") >= 0) {  //Spectrum data
					isDataSpectrum = true
				}

			}
			
			if (dbo["lop"] && dbo["centroid"] && dbo["current"] && dbo["current"] != 0) {
				dbo.put("eqe", 80.66 * dbo["lop"] * dbo["centroid"] / dbo["current"])
			} else {
				dbo.put("eqe", 0)
			}
					
			if (testType != "STD") {	
				dbo.put("spectrum", arrSpectrum)
				dbo.put("setVoltage", arrVoltage)
			}			
			
		} catch (Exception exc) {
			throw new RuntimeException(exc.getMessage())
		}
		finally {
			if (br != null)
				br.close()
			if (fr != null)
				fr.close()
			br = null
			fr = null
		}

		dbo
	}
	
	def padZeros (def value) {
		if (value != null && value.toString().isInteger()) {
			new DecimalFormat('000000').format(value.toInteger())
		} else {
			value?.toString()?.trim()
		}
	}
	
	def deleteDirs (def dirs) {
		if (Environment.currentEnvironment != Environment.DEVELOPMENT ) {
			dirs.each {
				it.deleteDir()
			}
		}
	}
	
	def isBurning (String mask, String burningPos, String code) {
		
		def map = getBurning (mask, burningPos)
		def c = code.tokenize("_")[1]
		map[c.trim()] == "B" ? "YES" : "NO"
	}
	
	def getBurning (String mask, String burningPos) {
		
		// logr.debug(mask + " " + burningPos + " " + code)
		
		burningPos = burningPos?.trim()
		if (burningPos?.length() != 4)
			return ""
			
		Map map = new TreeMap()
		def h = [10:'A',11:'B',12:'C',13:'D',14:'E',15:'F',16:'G',17:'H']
		
		def toNum = { val ->
			
			def key = val.substring(0,1)
			key = h.find { it.value == val.substring(0,1) }?.key?.toString() ?: key
			(key + val.substring(1,2)).toInteger()
		}
		
		def fromNum = { val ->
			
			String ret = val.toString()
			if (ret.length() == 3) {
				def f = h[ret.substring(0,2).toInteger()]
				ret = f.toString() + ret.substring(2,3)
			} else if (ret.length() == 1) {
				ret = "0" + ret.toString()
			}
			ret
		}
		
		int x = toNum(burningPos.substring(0,2))
		int y = toNum(burningPos.substring(2,4))
 
		def y1 = -3
		def y2 = 2
		def x1 = 0 
		def x2 = 17
		
		if (mask in ["MASK6","MASK7","MASK9","MASK12"]) {
			y1 = -17
			y2 = 0
			x1 = -2
			x2 = 3
		}
		
		for (int j in y1..y2) {
			for (int i in x1..x2) {
				
				def b = ""
				if (mask in ["MASK5","MASK8","MASK15"] && j in [0, -1]) {
					b = "B"
				} else if (mask in ["MASK6","MASK7","MASK9","MASK12"] && i in [0, 1]) {
					b = "B"
				}

                if (b == "B")
				    map.put(fromNum(x+i) + fromNum(y+j), b)
			}
		}
		map	
	}
	
	def getLopTestData (def db, def code, def devId) {
		
		def arr = new HashMap()
		if (devId) {
		
			def queryFull =	new BasicDBObject("value.code", code)
			queryFull.put("value.tkey", "full_test_visualization")
			def fulls = db.testData.find(queryFull).limit(1).addSpecial('$orderby', new BasicDBObject('value.testId',-1)).collect{it}
			def queryTest =	new BasicDBObject("value.code", code)
			queryTest.put("value.tkey", "test_data_visualization")
			def tests = db.testData.find(queryTest).limit(1).addSpecial('$orderby', new BasicDBObject('value.testId',-1)).collect{it}
            if (!tests) {
                def queryTestNbp =	new BasicDBObject("value.code", code)
                queryTestNbp.put("value.tkey", "nbp_test_data_visualization")
                tests = db.testData.find(queryTestNbp).limit(1).addSpecial('$orderby', new BasicDBObject('value.testId',-1)).collect{it}
            }
            if (!tests) {
                def queryTestNbp =	new BasicDBObject("value.code", code)
                queryTestNbp.put("value.tkey", "nbp_full_test_visualization")
                tests = db.testData.find(queryTestNbp).limit(1).addSpecial('$orderby', new BasicDBObject('value.testId',-1)).collect{it}
            }
	
			BasicDBObject dbo = (BasicDBObject)tests[0]?.value?.data
			dbo.each{ k, v ->
				if (k != "setting") {
					def currArr = k.tokenize('@')
					if (v["EQE"] && v["EQE"][devId] && currArr.size() > 1) {
						def curr = currArr[1]
						Integer currVal = Integer.parseInt(curr.replaceAll("\\D+",""))
						Float currVal2 = currVal
						if (curr.indexOf('mA') > 0) currVal2 = currVal
						if (curr.indexOf('uA') > 0) currVal2 = currVal * 1E-3
                        if (curr.indexOf('nA') > 0) currVal2 = currVal * 1E-6
                        if (curr.indexOf('pA') > 0) currVal2 = currVal * 1E-9
                        if (curr.indexOf('fA') > 0) currVal2 = currVal * 1E-12
                        if (curr.indexOf('fA') > 0) currVal2 = currVal * 1E-15
						
						arr.put(currVal2, v["EQE"][devId])
					}
				}
			}
			
			dbo = (BasicDBObject)fulls[0]?.value?.data
			dbo.each{ k, v ->
				if (k != "setting") {
					def currArr = k.tokenize('@')
					if (v["EQE"] && v["EQE"][devId] && currArr.size() > 1) {
						def curr = currArr[1]
						Integer currVal = Integer.parseInt(curr.replaceAll("\\D+",""))
						Float currVal2 = currVal
						if (curr.indexOf('mA') > 0) currVal2 = currVal
						if (curr.indexOf('uA') > 0) currVal2 = currVal * 1E-3
                        if (curr.indexOf('nA') > 0) currVal2 = currVal * 1E-6
                        if (curr.indexOf('pA') > 0) currVal2 = currVal * 1E-9
                        if (curr.indexOf('fA') > 0) currVal2 = currVal * 1E-12
						
						arr.put(currVal2, v["EQE"][devId])
					}
				}
			}
		}
		
		arr
	}
	
}
