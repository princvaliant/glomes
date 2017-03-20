package com.glo.custom

import com.mongodb.BasicDBObject
import org.apache.commons.logging.LogFactory

class DetailDataSyncService {

	private static final logr = LogFactory.getLog(this)
	
	def messageSource
	def mongo
	def readFileService
	def unitService
	def summarizeSyncCurrService

	def init(def unitId, def unitCode,  def testId, def value, def childrenUnits, def tkey, def bdo) {
		
		if (unitCode) {
			def message = [unitId:unitId, unitCode:unitCode, value:value, testId:testId, childrenUnits:childrenUnits, tkey:tkey, bdo:bdo]
			onMessage(message)
		}
	}

	def onMessage(def args) {

		def unitId = args.get("unitId")
		def unitCode = args.get("unitCode")
		def value = args.get("value")
		def tkey = args.get("tkey")
		def testId = args.get("testId")
		def bdo = args.get("bdo")
		def childrenUnits = args.get("childrenUnits")
		def db = mongo.getDB("glo")
		
		String syncVar
		if (tkey ==  "test_data_visualization") {
			syncVar= "testDataSynced"
		}
        if (tkey ==  "nbp_test_data_visualization") {
            syncVar= "testDataSynced"
        }
        if (tkey ==  "nbp_full_test_visualization") {
            syncVar= "testDataSynced"
        }
        if (tkey == "intermediate_coupon_test") {
            syncVar = "testDataSynced"
        }
		if (tkey ==  "top_test_visualization") {
			syncVar= "testTopSynced"
		}
		if (tkey == "char_test_visualization" ) {
			syncVar= "testCharSynced"
		}
		if (tkey == "fa_test" ) {
			syncVar= "testFaSynced"
		}
		if (tkey ==  "full_test_visualization" ) {
			syncVar= "testFullSynced"
		}
        if (tkey ==  "pre_dbr_test" ) {
            syncVar= "testPreDbrSynced"
        }
        if (tkey ==  "post_dbr_test" ) {
            syncVar= "testPostDbrSynced"
        }

		try {
			
			importData(db, unitCode, testId, value, tkey, childrenUnits)

            summarizeSyncCurrService.init(unitId, unitCode, bdo, testId, tkey, childrenUnits)
		} 
		catch(Exception exc) {
			db.unit.update(new BasicDBObject("code", unitCode), new BasicDBObject('$set', new BasicDBObject(syncVar, "FAIL")), false, true)
			logr.error(unitCode?.toString() + ": " + exc)
		}
		null
	}
	
	private def importData(def db, def unitCode, def testId, def files, def tkey, def childrenUnits) {
		

		def groups = files.groupBy {it[3]}
		groups.each { code, file ->
			
			if (childrenUnits.contains(code)) {
				
				def root = new BasicDBObject()
				def setting = new BasicDBObject()
				setting.put("default", "")
				setting.put("propagate", "false")
				root.put("setting", setting)

				file.each {
					def grp = "Data"
					if (it[4]) {
						if (it[4] < 1E-3)
							grp += " @ " + (it[4] * 1E6).round() + "uA"
						else
							grp += " @ " + (it[4] * 1E3).round() + "mA"
					} else {
						grp += "voltage"
					}
							 
					root.put(grp, readFileService.readTestFileForGroup (it[2], grp))
				}
					
				def queryTest =	new BasicDBObject("value.code",  unitCode + "_" + code)
				queryTest.put("value.testId", testId)
				queryTest.put("value.tkey", tkey)
				def testData = db.testData.find(queryTest, new BasicDBObject()).collect{it}[0]
				def tValue = new BasicDBObject()
				if (!testData) {
					testData = new BasicDBObject()
				}
				tValue.put("code", unitCode + "_" + code)
				tValue.put("testId", testId)
				tValue.put("parentCode", unitCode)
				tValue.put("tkey", tkey)
				tValue.put("data", root)
				testData.put("value", tValue)
				db.testData.save(testData)
				
				root = null
				testData = null
				
				Thread.sleep(3)
			}
		}
	}
}
