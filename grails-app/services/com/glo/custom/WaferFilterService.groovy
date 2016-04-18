package com.glo.custom

import com.mongodb.BasicDBObject
import org.apache.commons.logging.LogFactory

class WaferFilterService {
	
	private static final logr = LogFactory.getLog(this)
	
	static transactional = false
	def mongo
		
	def getAdminFilters (def k) {

		WaferFilter.executeQuery( """
				select wf from WaferFilter as wf where  wf.level1 = ? and wf.isActive = 1 and wf.isAdmin = 1
			""", k)
	}
	
	def  getValidDevices(def code, def tkey, def testId, long specId) {
		
		Set all = new HashSet()
		Set failed = new HashSet()
		
		def db = mongo.getDB("glo")
		def data = [:]
		def queryTest =	new BasicDBObject()
		queryTest.put("value.code", code)
		queryTest.put("value.testId", testId)
		queryTest.put("value.tkey", tkey)
		def testData = db.testData.find(queryTest, new BasicDBObject()).collect{it}[0]
		if (testData && testData["value"] && testData["value"]["data"]) {
			data = (BasicDBObject)testData["value"]["data"]
		}
		
		def waferFilters = WaferFilter.findAllByDieSpec(DieSpec.get(specId))


		waferFilters.each { waferFilter ->

			if (data[waferFilter.level1]) {
				
				def unfiltered = (BasicDBObject)data[waferFilter.level1][waferFilter.level2]

				if (unfiltered) {
					
					unfiltered.each { k, v ->
						
						if (!all.contains(k))
							all.add(k)
						
						if ((waferFilter.valFrom > v || waferFilter.valTo < v) ) {
							if (!failed.contains(k))
								failed.add(k)
						}
					}
				}
			}
		}
		
		all.minus(failed)
	}

}
