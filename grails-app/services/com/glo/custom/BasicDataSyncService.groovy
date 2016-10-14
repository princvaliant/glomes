package com.glo.custom

import com.glo.ndo.ProductMaskItemCtlm
import com.mongodb.BasicDBObject
import grails.util.Environment
import org.apache.commons.logging.LogFactory
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation
import org.apache.commons.math3.stat.regression.SimpleRegression
import org.apache.commons.math3.util.MathUtils

import javax.jms.Message

class BasicDataSyncService {

	private static final logr = LogFactory.getLog(this)

	def messageSource
	def detailDataSyncService
	def summarizeSyncService
	def readFileService
	def unitService
	def relSyncService
	def probeNewSyncService
    def probeSyncService
	def fileService
	def jmsService
	def mongo
	
	static exposes = ['jms']


	def init(def syncType, def unitsToBeSunched) {

		def message = [syncType:syncType, units:unitsToBeSunched]

		if (Environment.currentEnvironment == Environment.DEVELOPMENT ) {
			onMessage(message)
		} else {
			jmsService.send(service: 'basicDataSync', 1) {Message msg ->
					msg.object = message
					msg
			}
		}
	}

	def onMessage(def args) {

		def syncType = args.get("syncType")
		def units = args.get("units")
		try {

			switch (syncType) {
				case "TESTDATA":
					testData(units)
					break
				case "RELTEST":
					relSyncService.addRelData(units[0], units[1])
					break
				case "LAMPTEST":
					relSyncService.addLampData(units[0], units[1], units[2])
					break
				case "RELWAFERTEST":
					relSyncService.addRelWaferData(units[0], units[1])
					break
				case "RELBOARDTEST":
					relSyncService.addRelBoardData(units[0], units[1])
					break
				case "LAMPBOARDTEST":
					relSyncService.addLampBoardData(units[0], units[1], units[2])
					break

			}
			
			def st = syncType.tokenize("|")
			if (st[0] == "PROBETEST" ) {
                probeSyncService.addProbeData(units,st[1])
			}
            if (st[0] == "PROBETESTNEW" ) {
                probeNewSyncService.addProbeData(units,st[1])
            }

			
		} catch(Exception exc) {
			logr.error(syncType + ": " + exc)
		}
		null
	}


    private def testData (def units) {

        def db = mongo.getDB("glo")

        !logr.isDebugEnabled() ?: logr.debug("test data sync job started.")

        // Create list of unique files grouped by wafer id
        units.keySet().each {

            def bdo = new BasicDBObject()
            bdo.put("value.code", it)
            bdo.put("value.tkey", "test_data_visualization")

            def testData = db.testData.find(bdo, new BasicDBObject()).addSpecial('$orderby', new BasicDBObject("value.testId", -1)).collect { it }[0]
            if (testData) {
                def unit = db.unit.find([code: it], new BasicDBObject()).collect{it}[0]
                unit.put("id", unit["_id"])
                bdo.put("testDataIndex", [])
                if (!unit["testDataIndex"]) {
                    bdo["testDataIndex"].add(testData.value.testId.toString().toLong())
                } else {
                    bdo["testDataIndex"].addAll(unit["testDataIndex"])
                    bdo["testDataIndex"].add(testData.value.testId.toString().toLong())
                }
                summarizeSyncService.createSummaries(db, unit._id, unit.code, bdo, null, null, testData.value.testId.toString().toLong(), testData.value.tkey, unit.mask, null)
            }
        }
    }

	
	private def testDataOld (def units) {

		def db = mongo.getDB("glo")

		!logr.isDebugEnabled() ?: logr.debug("test data sync job started.")

		// Create list of unique files grouped by wafer id
		def validFiles= [:]
		def data


		units.keySet().each {
			
			def dirloc = it.listFiles([accept:{file-> file ==~ /.*?\.txt/ }] as FileFilter)?.toList()
			
			def cde = it.getName().replace("_100PCT", "")
			validFiles.put(cde, [])
			def cntrl = ""
			
			def sortedFileList = new TreeMap()
			dirloc.each {
				def arr = it.getName().tokenize("_")
				if (arr.size() == 6) {
					def cd = arr[3].substring(0,2) + arr[3].substring(4,6) + arr[3].substring(2,4)
					sortedFileList.put(arr[0] + "_" + arr[1] + "_" + arr[2] + "_"  + cd + "_"  + arr[4] + "_"  + arr[5], it)
				}
			}			
			
			sortedFileList.reverseEach { key22, file ->
				def nameAsList = file.getName().tokenize("_")
				if  (nameAsList.size >= 3) {
					def nameStart = nameAsList[0] + nameAsList[1] + nameAsList[2]
					if (nameStart != cntrl) {
						if (nameAsList[1].indexOf("ctlm") >= 0) {
							validFiles[cde].add([
								"CTLM",
								it,
								file,
								nameAsList[0],
								nameAsList[1].getAt(4),
								nameAsList[2]
							])
						}
						if (nameAsList[1] + "_" + nameAsList[2] == "Voltage_Sweep" ) {
							validFiles[cde].add([
								"VOLTAGE_SWEEP",
								it,
								file,
								nameAsList[0]
							])
						}
						if (nameAsList[1] == "Spectrum" ) {
							Float current = nameAsList[2].isFloat() ? nameAsList[2].toFloat() : 0
							validFiles[cde].add([
								"SPECTRUM",
								it,
								file,
								nameAsList[0],
								current
							])
						}
					}
					cntrl = nameStart
				}
			}
		}

		validFiles.each { code, value ->

			def ctlms = [:]
			def query =	new BasicDBObject("code", code)
			def unit = db.unit.find(query, new BasicDBObject()).collect{it}[0]
			if (unit && unit["mask"] && value) {

				def childrenList = []
				def root = new BasicDBObject()
				def setting = new BasicDBObject()
				setting.put("default", "WPE")
				setting.put("propagate", "false")
				root.put("setting", setting)

				value.each {

					if (it[0] == "CTLM") {

						// Get product mask item ctlm
						def prodMaskItem = ProductMaskItemCtlm.executeQuery( """
							select ps from ProductMaskItemCtlm as ps where ps.productMask.name = ? and ps.location = ? and ps.setType = ? and ps.circle = ?
						""", [unit["mask"], it[4], it[3], it[5].toInteger()])[0]

						SimpleRegression sreg = new SimpleRegression()
						data = readFileService.readTestFileForGroup (it[2], "Datavoltage")
						data["data"].each {
							if (it[0] && it[1]) {
								sreg.addData(it[1] / 1000, it[0])
							}
						}

						def obj = new Expando()
						obj.code = prodMaskItem.code
						obj.grp = it[3] + "-" + it[4]
						obj.slope = sreg.getSlope()
						double cr = prodMaskItem.radius / prodMaskItem.spacing
						double crr = 1.0 + prodMaskItem.spacing / prodMaskItem.radius
						obj.correction = cr * MathUtils.log(Math.E, crr)
						obj.correctedSlope = obj.slope / obj.correction

						if (!ctlms.containsKey(obj.grp)) {
							ctlms.put(obj.grp, [])
						}
						ctlms[obj.grp].add([
							prodMaskItem.radius,
							prodMaskItem.spacing,
							obj.correctedSlope
						])

						sreg = null
					}

					if (it[0] == "SPECTRUM") {

						def grp = "Data @ "
						if (it[4] < 1E-3)
							grp += (it[4] * 1E6).round() + "uA"
						else {
							
							Float mm = it[4]*1E6
							if (mm.mod(1E3) == 0)
								grp += (mm * 1E-3).round() + "mA"
							else
								grp += mm.round() + "uA"
							
//							def m = it[4] * 1E3
//							if (m.mod() == 0)
//								grp += (it[4] * 1E3).round() + "mA"
//							else
//								grp += (it[4] * 1E6).round() + "uA"
						}

						if (!root.containsKey(grp))
							root.put(grp, new BasicDBObject())

						data = readFileService.readTestFile (it[2])
						Double lop,curr,volt,peak,centroid = 0
						data.each { k, v ->

							switch (k) {
								case "Peak Power (W/nm)":
										peak = v * 1000;break;
								case "Int Power (W)":
										lop = v;break;
								case "Pulse Current (mA)":
										curr = v;break;
								case "Pulse Voltage (V)":
										volt = v;break;
								case "Centroid (nm)":
										centroid = v;break;
							}

							if (k in [
								"Lot #",
								"Scribe #",
								"Run #",
								"Recipe #",
								"Device",
								"Operator",
								"Station",
								"x",
								"y"
							]) {} else {
								if (!root[grp].containsKey(k))
									root[grp].put(k, new BasicDBObject())
								root[grp][k].put(it[3], v)
							}

							if (volt > 0 && curr > 0 && lop > 0 && peak > 0 && centroid > 0) {
								if (!root[grp].containsKey("WPE")) {
									root[grp].put("WPE", new BasicDBObject())
								}
								def vWpe = 100000 * lop / (volt * curr)
								if (vWpe >= 100) vWpe = 0
								root[grp]["WPE"].put(it[3], vWpe)

								if (!root[grp].containsKey("EQE")) {
									root[grp].put("EQE", new BasicDBObject())
								}
								def vEqe = 80.66 * lop * centroid / curr    //  0.8066 * 100%   (e/hc)
								if (vEqe >= 100) vEqe = 0
								root[grp]["EQE"].put(it[3], vEqe )  
							}
						}
					}

					if (it[0] == "SPECTRUM" || it[0] == "VOLTAGE_SWEEP") {
						if (!childrenList.contains(it[3]))
							childrenList.add(it[3])
					}
				}

				def bdo = new BasicDBObject()

				if (ctlms.size() > 0) {
					ctlms.each { k, v ->
					
						SimpleRegression sregY = new SimpleRegression()
						SimpleRegression sregX = new SimpleRegression()
						PearsonsCorrelation pearsonCorrelation = new PearsonsCorrelation()
						def radius = 0
						def xArray = []
						def yArray = []
						v.each {
							xArray.add(it[1])
							yArray.add(it[2])
							sregY.addData(it[1], it[2])
							sregX.addData(it[2], it[1])
							radius = it[0]
						}

						def slope = sregY.getSlope()
						def pearson = pearsonCorrelation.correlation((double[])xArray,(double[])yArray)
						def resistance = Math.abs(sregY.getIntercept()/2)
						def lengthTransfer =  Math.abs(sregX.getIntercept()/2)

						def rsh = 2 * slope * Math.PI * radius
						def pc = 2 * Math.PI * radius * lengthTransfer * resistance * 1E-8

						bdo.put(k + "-Slope", slope)
						bdo.put(k + "-Pearson", pearson)
						bdo.put(k + "-Resistance", resistance)
						bdo.put(k + "-LengthTransfer", lengthTransfer)
						bdo.put(k + "-Rsh", rsh)
						bdo.put(k + "-Pc", pc)
					}
				}
				
				def nextTestId = 1 
				if ( unit["tkey"] == "full_test_visualization") {
					if (unit["testFullIndex"]) {
						nextTestId = unit["testFullIndex"].size() + 1
					}
				} else if (unit["tkey"] == "test_char_visualization") {
					if (unit["testCharIndex"]) {
						nextTestId = unit["testCharIndex"].size() + 1
					}
				} else {
					if (unit["testDataIndex"]) {
						nextTestId = unit["testDataIndex"].size() + 1
					}
				}

				if (root.size() > 0) {

					// Store graph data
					def queryTest =	new BasicDBObject("value.code", code)
					queryTest.put("value.testId", nextTestId)
					queryTest.put("value.tkey", unit["tkey"])
					def testData = db.testData.find(queryTest, new BasicDBObject()).collect{it}[0]
					def tValue = new BasicDBObject()
					if (!testData) {
						testData = new BasicDBObject()
					}
					tValue.put("code", code)
					tValue.put("testId", nextTestId)
					tValue.put("experimentId", unit["experimentId"])
					tValue.put("parentCode", null)
					tValue.put("data", root)
					tValue.put("date", new Date())
					tValue.put("tkey", unit["tkey"])
					testData.put("value", tValue)
					db.testData.save(testData)
					testData = null
				}

				bdo.put("id", unit["_id"])
				addTestId(unit, unit["tkey"], bdo)

				bdo.put("processCategory", "nwLED")
				bdo.put("processKey", "test")
				bdo.put("taskKey", unit["tkey"])

				if (childrenList.size() > 0) {
					detailDataSyncService.init(unit["_id"], unit["code"], nextTestId, value, childrenList, unit["tkey"], bdo)
				} else {
					value[0][1].deleteDir()
				}
			}
		}

		validFiles = null
		data = null

		!logr.isDebugEnabled() ?: logr.debug("test data sync job ended.")
	}



	private def addTestId(def unit, def tkey, def bdo) {
		if (tkey == "full_test_visualization") {
			bdo.put("testFullIndex", [])
			if (!unit["testFullIndex"]) {
				bdo["testFullIndex"].add(1)
			} else {
				bdo["testFullIndex"].addAll(unit["testFullIndex"])
				bdo["testFullIndex"].add(unit["testFullIndex"].size() + 1)
			}
		} else if (tkey == "test_char_visualization") {
			bdo.put("testCharIndex", [])
			if (!unit["testCharIndex"]) {
				bdo["testCharIndex"].add(1)
			} else {
				bdo["testCharIndex"].addAll(unit["testCharIndex"])
				bdo["testCharIndex"].add(unit["testCharIndex"].size() + 1)
			}
		} else {
			bdo.put("testDataIndex", [])
			if (!unit["testDataIndex"]) {
				bdo["testDataIndex"].add(1)
			} else {
				bdo["testDataIndex"].addAll(unit["testDataIndex"])
				bdo["testDataIndex"].add(unit["testDataIndex"].size() + 1)
			}
		}
	}
	
	
	
}
