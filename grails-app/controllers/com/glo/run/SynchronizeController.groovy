package com.glo.run

import com.mongodb.BasicDBObject
import grails.converters.JSON
import org.apache.commons.logging.LogFactory


class SynchronizeController {

	private static final logr = LogFactory.getLog(this)

	def springSecurityService
	def contentService
	def unitService
	def syncService
	def summarizeSyncService
	def basicDataSyncService
	def grailsApplication
	def mongo

	def sync = {
		try {
			logr.debug(params)

			def userName =  springSecurityService.principal?.username
			def units = unitService.getUnitsLocal(userName, params.pctg, params.pkey, params.tkey, "", 0, 0, 65000, "", "", "")
			def variables = contentService.getVariables(params.pctg, params.pkey, params.tkey, ["dc"])
			def ret = syncService.execute(units, variables, params.tkey, userName)
			render ([success:true, cnt: ret.toString()] as JSON)
		} catch (Exception exc) {
			logr.error(exc)
			render ([success:false, msg: exc.getMessage()] as JSON)
		}
	}
	
	def RRRELOADDDPL = {
		try {
			logr.debug(params)

			def userName =  springSecurityService.principal?.username
			
			// Get directory
			String dir = grailsApplication.config.glo.bufferGrowthDirectory
	
			def ret = 0
			def variables = contentService.getVariables("nwLED", "epi", "pl", ["dc"])
			def dirloc = new File(dir).listFiles()?.toList()
			def db = mongo.getDB("glo")
			
			dirloc.each { directory ->

				def fileName = directory.path.substring(directory.path.lastIndexOf("\\") + 1).toUpperCase()

                if ((fileName >= "D1131001A" || fileName >= "S2131001A")) {

                    def units = db.unit.find([runNumber:fileName], [:]).collect{[data:it]}
                    if (units) {

                        syncService.execute(units, variables, "pl", userName)
                        ret++
                    }
                }
			}
			

			
			render ([success:true, cnt: ret.toString()] as JSON)
		} catch (Exception exc) {
			logr.error(exc)
			render ([success:false, msg: exc.getMessage()] as JSON)
		}
	}

	def RRRELOADDD = {
		try {
			def db = mongo.getDB("glo")

			def tkey = params.tkey
			def ret = []
			def query =	new BasicDBObject()

            if (params.code) {
                if (params.code != 'ALL') {
                    query.put("code", ['$in': params.code.tokenize(",")])
                } else {

                    def tq = new BasicDBObject()
                    tq.put("value.parentCode", null)
                    tq.put("value.tkey", tkey)
                    tq.put("value.data.Data @ 600uA", new BasicDBObject('$exists',1))
                    def codes = db.testData.find(tq, ['value.code':1]).collect {it.value.code}
                    query.put("code", ['$in': codes])
                }
                query.put("parentCode", null)
                query.put("pctg", "nwLED")
                query.put("productCode", "100")
                if (tkey in ["test_data_visualization", "nbp_test_data_visualization", "nbp_full_test_visualization", "intermediate_coupon_test"]) {
                    query.put("testDataIndex", new BasicDBObject('$exists',1))
                    db.unit.find(query).each {
                        if (it.testDataIndex && it.testDataIndex.size() > 0) {
                            ret.add(["id": it["_id"], "code":it.code, "tid" : it.testDataIndex[it.testDataIndex.size()-1], mask: it.mask])
                        }
                    }
                }
                if (tkey == "full_test_visualization") {
                    query.put("testFullIndex", new BasicDBObject('$exists',1))
                    db.unit.find(query).each {
                        if (it.testFullIndex && it.testFullIndex.size() > 0) {
                            ret.add(["id": it["_id"], "code":it.code, "tid" : it.testFullIndex[it.testFullIndex.size()-1], mask: it.mask])
                        }
                    }
                }

                def cnt = 1
                ret.each {
                    cnt++
                    summarizeSyncService.createSummaries(db, it.id, it.code, null, null, null, it.tid, tkey, it.mask, null)
                }

            }

			render ([success:true, cnt: ret.size().toString()] as JSON)
		} catch (Exception exc) {
			logr.error(exc)
			render ([success:false, msg: exc.getMessage()] as JSON)
		}
	}

	def PROBERELOAD = {

		String dir = grailsApplication.config.glo.probeTestDirectory
		if (!dir) {
			throw new RuntimeException("Probe test sync directory not specified")
		}

		// Determine which units contain folder to be synched
		def db = mongo.getDB("glo")
		def unitsToBeSynched = [:]

//		def bd = new BasicDBObject('parentCode', null)
//		bd.put('probeTestSynced', 'YES')
//
//		def temp = db.unit.find(bd, new BasicDBObject()).collect {
//
//			File f = new File(dir + "\\" + it.code)
//			if (f.exists()) {
//				unitsToBeSynched.put(f, it)
//			}
//		}
		
		def cnt = 0
		def bd = new BasicDBObject('parentCode', null)
        bd.put('code', new BasicDBObject('$in', params.code.tokenize(",")))
		bd.put('value.ni_dot_test', new BasicDBObject('$exists',1))
	
		db.dataReport.find(bd, new BasicDBObject()).collect {
	    	def unit = db.unit.find(new BasicDBObject('code',it.code), new BasicDBObject()).collect {it}[0]
			if (unit) {
					unitsToBeSynched.put(unit, '')
			}
		}

		
		basicDataSyncService.init("PROBETEST|ni_dot_test", unitsToBeSynched)

		render(unitsToBeSynched.size())
	}

	def PLLOAD = {

		// Determine which units contain folder to be synched
		def db = mongo.getDB("glo")

		def exps = [
			"EXP3AW",
			"EXP3AP",
			"EXP3AQ",
			"EXP3AR",
			"EXP3AS",
			"EXP3AT",
			"EXP3AU",
			"EXP6C8",
			"EXP6C9",
			"EXP6CA",
			"EXP3AH",
			"EXP3AJ",
			"EXP3AM",
			"EXP6AK",
			"EXP6AH",
			"EXP6AC",
			"EXP6AG",
			"EXP6AA",
			"EXP6AB",
			"EXP6A8",
			"EXP6A5",
			"EXP6A3"
		]



		def bd = new BasicDBObject('parentCode', null)
		bd.put('experimentId', new BasicDBObject('$in', exps))

		def fs = new BasicDBObject()
		fs.put('code', 1)

		int i = 0

		def temp = db.unit.find(bd, fs).collect {

			def bdo = new BasicDBObject()
			bdo.put("processCategory", "nwLED")
			bdo.put("processKey", "epi")
			bdo.put("taskKey", "pl")
			bdo.put("id", it._id)
			bdo.put("ops", "YES")

			unitService.update(bdo, "admin", true)

			i++
		}

		render(i.toString())
	}


	def TESTDATAFIX = {

		// Determine which units contain folder to be synched
		def db = mongo.getDB("glo")

		def query = new BasicDBObject()
		
		def cs = [['A22A06197','130808153430'],
				['A22A06197','130808174449'],
				['A23100035','130808104609'],
				['A23181026','130808125558'],
				['A23181026','130808141439'],
				['A23181046','130808114843'],
				['A23180325','130725104516'],
				['A23182003','130724175104'],
				['A23180244','130730140834'],
				['A23180244','130731142817'],
				['LA124001773-R','130816133016'],
				['LA124013887-R','130816123623'],
				['LA124013847-R','130727204906'],
				['LA124013847-R','130730184237'],
				['LA124013684-R','130725131404'],
				['LABC7070166-R','130725115613'],
				['LABC7070236-R','130727214743'],
				['LABC7070236-R','130730171551'],
				['LA125017614-R','130727224524'],
				['LA125017614-R','130730120248'],
				['LABC7070162-R','130730144251'],
				['LABC7070162-R','130730180634'],
				['LABC7070237-R','130730164913'],
				['LABC7070237-R','130731132224'],
				['A23180237','130801175404'],
				['A23180237','130801184638'],
				['LABC7070232-R','130801133906'],
				['LA118900058-R','130801163242'],
				['LA118900058-R','130801171031'],
				['A23181071','130801143810'],
				['LA124013553-R','130727200559'],
				['LA124013553-R','130731161207'],
				['LABC7070026-R','130730111507'],
				['LABC7070026-R','130731151330'],
				['E01W2B39444-E','130725151101'],
				['UN0000111','130725155724']]

		
		cs.each { c ->
			
			query.put("code", c[0])

			def fields = new BasicDBObject()
			fields.put("testDataIndex",1)
			fields.put("testCharIndex",1)

			def unit = db.unit.find(query, fields).collect{it}[0]
			
			def tdi = []
			def tci = []
			unit.testCharIndex?.each {
				
					tci.add(it)
			}
			
			unit.testDataIndex.each {
			
				if (it != c[1].toLong())
					tdi.add(it)
				else
					tci.add(it)
			}
		
			def bdo = new BasicDBObject()
			bdo.put("testDataIndex", tdi)
			bdo.put("processCategory", "nwLED")
			bdo.put("processKey", "test")
			bdo.put("taskKey", "test_data_visualization")
			bdo.put("id", unit._id)
			unitService.update(bdo, "admin", true)

			def bdo2 = new BasicDBObject()
			bdo2.put("testCharIndex", tci)
			bdo2.put("processCategory", "nwLED")
			bdo2.put("processKey", "test")
			bdo2.put("taskKey", "char_test_visualization")
			bdo2.put("id", unit._id)
			unitService.update(bdo2, "admin", true)


		}



		render ('o')
	}
}
