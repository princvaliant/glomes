package com.glo.excel

import com.glo.ndo.DataViewVariable
import com.mongodb.BasicDBObject
import grails.converters.JSON
import org.apache.commons.logging.LogFactory
import org.apache.poi.xslf.usermodel.*


class UploadQevController {
	
	def historyService
	def historyDataService
	def unitService
	def summarizeSyncService
    def syncService
    def importService
	def spcService
    def grailsApplication
    def basicDataSyncService
    def persistenceInterceptor
	def mongo
	
	private static final logr = LogFactory.getLog(this)

	def updateVars = {

		
		def dvvs =  DataViewVariable.findAllByFullPathAndIsFormula(null, null)
		
		dvvs.each { dvv ->
			
			if (dvv.variable) {
				
				def pctg = dvv.variable.process ? dvv.variable.process?.category : dvv.variable.processStep?.process?.category
				def	pkey = dvv.variable.process ? dvv.variable.process?.pkey : dvv.variable.processStep?.process?.pkey
				def tkey = dvv.variable.processStep?.taskKey 
				
				def v = dvv.variable
				if (v.name == "code" || v.name == "nNotes" || v.name == "movedBy" || v.name == "tkey" || 	v.name == "pkey" ||
							 v.name == "idx" || v.name == "product" || v.name == "productCode" ||  v.name == "updated" || 
							 v.name == "lotNumber" || v.name == "supplier" || v.name == "supplierProductCode" || v.name == "closed" || 
							 v.name == "yieldLoss" || v.name == "yieldLossStep" || v.name == "yieldLossDate" || v.name == "active" || 
							 v.name == "processViolations") {
					pkey = ""
				}
				
				if (pctg) {
					if (!pkey)
						dvv.fullPath = pctg
					if (pkey  && !tkey )
						dvv.fullPath = pctg + "|" + pkey
					else if (pkey  && tkey)
						dvv.fullPath = pctg + "|" + pkey +"|" + tkey
						
					dvv.save(failOnError: true)
				}
			}
		}
	}

    def fixNotes = {

        def db = mongo.getDB("glo")
        def units = db.unitarchive.find(new BasicDBObject(), new BasicDBObject()).limit(2000).addSpecial('$orderby', ['lastUpdated':-1]).collect {it}

        units.each { unit ->

            historyService.initHistory("addNote", null, unit, null)
        }
    }
	
	def updateFormulaVars = {
		
		def bdo = new BasicDBObject()
		def db = mongo.getDB("glo")
		bdo.put("parentCode", null)
        bdo.put("pctg", "nwLED")
		bdo.put("uv_365nm_avg", ['$exists': 1])
	
		int c = 0
	
		def temp = db.unit.find(bdo, new BasicDBObject())

		def ret = temp.collect{

            def bdoUnit = new BasicDBObject()
            bdoUnit.put("id", it["_id"])
            bdoUnit.put("processCategory", "nwLED")
            bdoUnit.put("processKey", "por_coalesce")
            bdoUnit.put("taskKey", "post_anneal")
            unitService.update(bdoUnit, "admin", true)
			c++
		}
		
		render (['cnt': c] as JSON)
	}
	
	def updateStemNumber = {
		
		def bdo = new BasicDBObject()
		def db = mongo.getDB("glo")
		def ss = [
                '150124032BH',
                '150124032DH',
                '150124033BH',
                '150124036AH',
                '150126014BH',
                '150126015AH',
                '150126016DH',
                '150126025CH',
                '150313026AH',
                '150313026BH',
                '150319024BH',
                '150319024AH',
                '150319023DH',
                '150319026CH',
                '150317015BH',
                '150313012CH',
                '150313014BH',
                '150313015AH',
                '150313013DH',
                '150313016DH',
                '150313012DH',
                '150317012DH',
                '150317012CH',
                '150317012BH',
                '150317011CH',
                '150317011BH',
                '150313063AH',
                '150313062CH',
                '150313062DH',
                '150313024CH',
                '150313024DH',
                '150313025BH',
                '150313061DH',
                '150317015AH',
                '150317014AH',
                '150317014CH',
                '150317013DH',
                '150317013CH',
                '150317013BH',
                '150313093BH',
                '150313092CH',
                '150313092BH',
                '150313026DH',
                '150319024CH',
                '150313026CH',
                '150319054AH',
                '150319045DH',
                '150319045CH',
                '150319031AH',
                '150319031CH',
                '150319031BH',
                '150319044AH',
                '150319053CH',
                '150319034AH']

	    int c = 0
	
//		def temp = db.konica.update(bdo, new BasicDBObject('$set': [is_baseline:'YES']), false, true)

		ss.each{ code ->
            def temp = db.unit.find([code:code]).collect{it}[0]
            if (temp) {
                def bdoUnit = new BasicDBObject()
                bdoUnit.put("id", temp["_id"])
                bdoUnit.put("processCategory", "Packages")
                bdoUnit.put("processKey", "iblu")
                bdoUnit.put("taskKey", "glo_camera_test")
                bdoUnit.put("is_baseline", "YES")
                unitService.update(bdoUnit, "admin", true)


                c++
            } else {
                System.out.println(code)
            }
		}
		
		render (['cnt': c] as JSON)
	}

    def updateCode = {

        def bdo = new BasicDBObject()
        def db = mongo.getDB("glo")
        bdo.put("code", params.from)

        def upd = new BasicDBObject()
        upd.put('$set', ['code', params.to])
        upd.put('$pull', ['tags', params.from])
        upd.put('$push', ['tags', params.to])
        db.history.update(bdo, upd, false, true)
        db.unit.update(bdo, upd, false, true)
        db.dataReport.update(bdo, upd, false, true)

        render (['success'] as JSON)
    }
	
	def updateFabVariables = {
		
		def bdo = new BasicDBObject()
		def db = mongo.getDB("glo")
		bdo.put("parentCode", null)
		def cnt = db.history.count(bdo)
		
		
		def temp = db.history.find(bdo, new BasicDBObject())
		int c = 0
		def ret = temp.collect {
			
			def ito = ''
				
			it["audit"].eachWithIndex { item, i ->
				
				if (item.pkey == 'test' && item.tkey == 'test_data_visualization' && item["dc"] && item["dc"]["testDataSynced"]) {
					
					item["dc"].put('testDataExperiment', [it["experimentId"]])
				}
				
//				if (item.pkey == 'nanowire' && item.tkey == 'p_contact_deposition' && item["dc"]) {
//					
//					item["dc"].put('SheetResistancePdep', item["dc"]["SheetResistanceWitness"])
//					item["dc"].remove("SheetResistanceWitness")
//					
//					item["dc"].put('ThicknessWitness4', item["dc"]["witnessSampleThicknessPcontact4"])
//					item["dc"].remove("witnessSampleThicknessPcontact4")
//					
//					item["dc"].put('ThicknessWitness5', item["dc"]["witnessSampleThicknessPcontactCenter"])
//					item["dc"].remove("witnessSampleThicknessPcontactCenter")
//					
//					item["dc"].put('ThicknessWitnessAverage', item["dc"]["AverageThickPContact "])
//					item["dc"].remove("AverageThickPContact ")
//					
//				}
			}
			
			def bdo2 = new BasicDBObject()
			bdo2.put("code", it.code)
			def unit = db.unit.find(bdo2, new BasicDBObject()).collect { it }[0]
			if (unit && unit["testDataSynced"]) {
				
				unit.put('testDataExperiment', [unit["experimentId"]])
				
//				unit.put('SheetResistancePdep', unit["SheetResistanceWitness"])
//				unit.remove("SheetResistanceWitness")
//				
//				unit.put('ThicknessWitness4', unit["witnessSampleThicknessPcontact4"])
//				unit.remove("witnessSampleThicknessPcontact4")
//				
//				unit.put('ThicknessWitness5', unit["witnessSampleThicknessPcontactCenter"])
//				unit.remove("witnessSampleThicknessPcontactCenter")
//				
//				unit.put('ThicknessWitnessAverage', unit["AverageThickPContact "])
//				unit.remove("AverageThickPContact ")
							
				db.unit.save(unit)
				
				c++
			}
			
	
			db.history.save(it)
			historyDataService.init(it)
		}
		
		render (['cnt': cnt, 'compl': c] as JSON)
	}
	
	def recalcHistory = {
		
		def bdo = new BasicDBObject()
		def db = mongo.getDB("glo")
        bdo.put("parentCode", null)
		bdo.put("actualStart", new BasicDBObject('$gt', new Date() - 90))
        bdo.put("audit.pctg", new BasicDBObject('$in', ['nwLED','RctHdw']))
		
		def temp = db.history.find(bdo, new BasicDBObject())
		int c = 0
		def ret = temp.collect{

            def end = ""
            def unit = db.unit.find(new BasicDBObject("code", it.code), new BasicDBObject()).collect{it}[0]
            if (!unit) {
                unit = db.unitarchive.find(new BasicDBObject("code", it.code), new BasicDBObject()).collect { it }[0]
                end = "END"
            }
            if (unit) {

                def firstPassDate = new TreeMap()
                def numberOfPasses = new TreeMap()

                def ct = it['audit'].size() - 1

                it["audit"].eachWithIndex { item, i ->

                    def pkeytkey = item.pkey + item.tkey
                    if (!firstPassDate[pkeytkey]) {
                        firstPassDate.put(pkeytkey, item.actualStart)
                    }
                    if (numberOfPasses[pkeytkey] == null) {
                        numberOfPasses.put(pkeytkey, 1)
                    } else {
                        numberOfPasses[pkeytkey] = numberOfPasses[pkeytkey] + 1
                    }


                    historyService.insertMove(db, unit, it["audit"][i], '')

                    if (ct == i && end == "END") {
                        historyService.insertMove(db, unit, it["audit"][i], end)
                    }
                }

                db.history.save(it)

                historyDataService.init(it, unit)

                if (end == "END") {
                    db.dataReport.update(['code':it.code], ['$set': ['value.active': '']])
                } else {
                    db.dataReport.update(['code':it.code], ['$set': ['value.active': 'true']])
                }

                c++
            }
		}
		
		render (['compl': c] as JSON)
	}

    def recalcHistory2 = {

        def bdo = new BasicDBObject()
        def db = mongo.getDB("glo")
        if (params.code) {
            bdo.put("code", params.code)
        }
        bdo.put("parentCode", null)
        bdo.put("actualStart", new BasicDBObject('$gt', new Date() - 91))
        bdo.put("audit.pctg", new BasicDBObject('$in', ['nwLED','RctHdw']))

        def temp = db.history.find(bdo, new BasicDBObject())
        def c1 = []
        def ret = temp.collect{

            def end = ""
            def unit = db.unit.find(new BasicDBObject("code", it.code), new BasicDBObject()).collect{it}[0]
            if (!unit) {
                unit = db.unitarchive.find(new BasicDBObject("code", it.code), new BasicDBObject()).collect { it }[0]
                end = "END"
            }
            if (unit) {

                def datalogSize = it.dataLog.size()
                def auditSize = it.audit.size()

                if (datalogSize > 0 && auditSize > 0) {

                    def pkeyPrev = ''
                    def ct = it['audit'].size() - 1

                    it["audit"].eachWithIndex { aud, i ->

                        if (pkeyPrev != '') {
                            aud["pkeyPrev"] = pkeyPrev
                        }

                        historyService.insertMove(db, unit, aud, '')

                        if (ct == i && end == "END") {
                            historyService.insertMove(db, unit, aud, end)
                        }

                        pkeyPrev =  aud["pkey"]
                    }

                    db.history.save(it)

                    c1.add(it.code)
                }
            }
        }

        render ( c1 as JSON)
    }

	def addVars = {
		
		
		spcService.recalculateCharts()
		
//		def processes = Process.findAll()
//		
//		processes.each {  process -> 
//			process.addToVariables(new Variable(idx: 64,  dir:"", name:"yieldLossStep", title:"Yield loss", hidden:true, dataType:"string"))
//			process.save()
//		}
		
	}
	
	def moveToArchive = {
		
		def db = mongo.getDB("glo")
		def units = db.unit.find(new BasicDBObject("qtyOut",0), new BasicDBObject()).collect { it }
		def c = 0
		def f = 0
		
		units.each {
			
			db.unitarchive.save(it)
			db.unit.remove(["_id": it["_id"]])
				
			c++
		
		}
		
		render (['compl': c, 'fail': f] as JSON)
	}
	
	
	
	def updateYieldLoss = {
		
		def db = mongo.getDB("glo")
		def c = 0, f = 0
		def hists = db.history.find(new BasicDBObject("parentCode", null))?.collect{ hist ->
			
			def unit = db.unit.find(['code':hist.code]).collect{it}[0]
			if (!unit) {
				unit = db.unitarchive.find(['code':hist.code]).collect{it}[0]
			}
			
			historyDataService.init(hist, unit)
				
			c++		
		}
		
		render (['compl': c, 'fail': f] as JSON)
	}
	
	def updateEquipmentParts = {
		
		def bdo = new BasicDBObject()
		def db = mongo.getDB("glo")
		bdo.put("parentCode", null)
	//	bdo.put("code", "TC373645")
		bdo.put("value.epi_growth.runNumber", new BasicDBObject('$exists', 1))
		def temp = db.dataReport.find(bdo, new BasicDBObject())
		
		int c = 0
		def ret = temp.collect{
			
			def run = it["value"]["epi_growth"]["runNumber"].toUpperCase()
			
			def bdo2 = new BasicDBObject("_id.run", new BasicDBObject('$lte', run))
			
			def first = ""
			def hprs = db.hardwarePerRunNumber.find(bdo2, new BasicDBObject()).limit(50).sort(["_id.run": -1]).collect { hpr ->
				
				if (first == "" && hpr["_id"]["run"])
					first = hpr["_id"]["run"]
				
				if (first != "" && first == hpr["_id"]["run"])	{
					c++
					 ["waferRun": run] + hpr
				}
			}
			
			
			hprs.each { hpr ->
				if (hpr) {
					def bdo3 = new BasicDBObject("run", hpr["waferRun"])
					bdo3.put("code", hpr["_id"]["code"])
					def hprs3= db.hardwarePerRunNumber2.find(bdo3, new BasicDBObject()).collect {it}[0]
					if (!hprs3) {
						def bdo4 = new BasicDBObject()
						bdo4.putAll(hpr["_id"])
						bdo4["run"] = hpr["waferRun"]
						
						db.hardwarePerRunNumber2.save(bdo4)
					}
				}
			}
					

		}
		
		render (['compl': c] as JSON)
	}
	
	
	def updateTestData = {
		
		def bdo = new BasicDBObject()
		def db = mongo.getDB("glo")
		
		bdo.put("parentCode", null)
		bdo.put("audit.dc.testDataExperiment", new BasicDBObject('$exists', 1))
		
		def temp = db.history.find(bdo,  new BasicDBObject())
		
		int c = 0
		def saveHist = [:]
		def ret = temp.collect{
			
			def exps = [:]
			def idx = 1
			it["audit"].each { aud ->
				aud?.dc?.testDataExperiment.each { exp ->
					if (exp && !exps.containsKey(exp)) {
						exps.put(exp, idx)
						idx++
					}
				}
				
				if (exps) {
					aud.dc.put("testDataIndex", [])
					aud.dc["testDataIndex"].addAll(exps.collect { k,v -> v })
					saveHist.put(it.code, aud.dc["testDataIndex"])
				}				
			}

//			exps.each { k, v -> 
//				
//				def queryTest =	new BasicDBObject()
//				queryTest.put("value.code", it.code)
//				queryTest.put("value.experimentId", k)
//				queryTest.put("value.tkey", "test_data_visualization")
//				queryTest.put("value.testId", new BasicDBObject('$exists', 0))
//				def testData = db.testData.find(queryTest, new BasicDBObject()).collect{ td ->
//					td
//				}
//				testData.each { td ->
//					if (td.containsKey("value")) {
//						td["value"].put("testId", v)
//						db.testData.save(td)
//					}
//				}
//				
//				logr.debug(it.code + " " + k + " " + v)
//				
//				testData = null
//			}
		}
		
		saveHist.each { k, v ->
			
			def bdo2 = new BasicDBObject()
			bdo2.put("code", k)
			def unit = db.unit.find(bdo2, new BasicDBObject()).collect { it }[0]
			if (unit) {
				unit.put("testDataIndex", v)
				db.unit.save(unit)
				
				logr.debug(k)
			}
		}

		
		render (['compl': c] as JSON)
	}
	
	
	def addToTestData = {
		
		def bdo = new BasicDBObject()
		def db = mongo.getDB("glo")
		
		
		def retList = [:]
		try {

    //        bdo.put('value.code','UN0015525');
			bdo.put("value.parentCode", null)
			bdo.put("value.tkey", "test_data_visualization")
            bdo.put("value.testId", ['$gt': 160329061409, '$lte': 161801000000])
	
			def temp = db.testData.find(bdo, new BasicDBObject('value.data', 0)).collect{
			
				def unit = db.unit.find(["code": it.value.code], ['code':1,'testDataIndex':1, mask: 1]).collect {it}[0]
				if (unit && unit.testDataIndex) {
					[tid:it._id,unitId:unit._id,code:it.value.code,testId:unit.testDataIndex[unit.testDataIndex.size()-1],mask:unit.mask]
				} else {
					[tid:-1,unitId:-1,code:'',testId:-1,mask:'']
				}
			} 
	
			int c = 0
			temp.each {
				
				if (it.tid != -1) {
					
					render(it.code + " " + it.testId + "<br/>")
	
//					def map = [:]
//					
//					def bdo2 = new BasicDBObject()
//					bdo2.put("value.parentCode", it.code)
//					bdo2.put("value.tkey", "top_test_visualization")
//					bdo2.put("value.testId", it.testId)
					
//					def flds = new BasicDBObject("value.data.Datavoltage",1)
//					flds.put("value.code", 1)
//					
//					db.testData.find(bdo2, flds).collect{ child ->
//		
//						def cd = child?.value?.code?.tokenize("_")[1]
//						if (cd) {
//							child?.value?.data?.Datavoltage?.data?.each { dt ->
//								if (dt[0] > 1.9055 && dt[0] < 2.045) {
//									
//									map.put(cd, dt[1])
//								}
//							}
//						}
//					}
//						
//					def upd = new BasicDBObject('$set', 
//						new BasicDBObject('value.data.Current @ 2V', 
//							new BasicDBObject("NA", map)))
//					
//					db.testData.update(new BasicDBObject("_id", it.tid), upd, false, false)
//					
//					Thread.sleep(10)
					

					summarizeSyncService.createSummaries(db, it.unitId, it.code, null, null, null, it.testId, "test_data_visualization", it.mask, null)
				}
			}
		} catch (Exception exc) {
			logr.error(exc.getMessage())
			render (exc.getMessage())
		}
	}


    def nidot = {

        def tkey = "ni_dot_test"

        String dir = grailsApplication.config.glo.probeTestDirectory
        String syncType = "PROBETEST|" + tkey

        if (tkey in [
                "nw_ito_dot_test",
                "ito_dot_test"
        ]) {
            dir = grailsApplication.config.glo.itoProbeTestDirectory
        }
        if (tkey in ["fa_test"]) {
            dir = grailsApplication.config.glo.faTestDirectory
        }
        if (!dir) {
            throw new RuntimeException("Probe, ITO or FA test sync directory not specified")
        }


        def db = mongo.getDB("glo")
        def query =  new BasicDBObject()
        query.put("parentCode", null)
        query.put("value.ni_dot_test.actualStart", new BasicDBObject('$exists', 1))
        def df = new Date().clearTime() - 60
        query.put("value.ni_dot_test.actualStart", new BasicDBObject('$gt', df))
        def fields = new BasicDBObject()
        fields.put("id", 1)
        fields.put("code", 1)
        def units = db.dataReport.find(query, fields).collect {it}

        int c = 0
        def unitsToBeSynched = [:]
        units.each {

            File f = new File(dir + "/" + it.code)
            if (f.exists()) {
                 unitsToBeSynched.put(f, it)
            }
        }

        basicDataSyncService.init(syncType, unitsToBeSynched)

        render (['cnt':  unitsToBeSynched.size()] as JSON)
    }


    def ipc = {
        def unts = [
                'UN0015825',
                'UN0015826',
                'UN0015827',
                'UN0015816',
                'UN0015818',
                'UN0015819',
                'UN0015820',
                'UN0015821',
                'UN0015822',
                'UN0015823',
                'UN0015824',
                'UN0015517',
                'UN0015499',
                'UN0015501',
                'UN0015502',
                'UN0015503',
                'UN0015504',
                'UN0015506',
                'UN0015507',
                'UN0015512',
                'UN0015514',
                'UN0015515',
                'UN0011236',
                'UN0011201',
                'UN0011320',
                'UN0011310',
                'UN0011315',
                'UN0011316',
                'UN0015239',
                'UN0015279',
                'UN0013378',
                'UN0015248',
                'UN0013379',
                'UN0013380',
                'UN0015043',
                'UN0015044',
                'UN0015045',
                'UN0015047',
                'UN0015048',
                'UN0015049',
                'UN0015050',
                'UN0015051',
                'UN0015052',
                'UN0015053',
                'UN0015054',
                'UN0015055',
                'UN0015598',
                'UN0015592',
                'UN0015593',
                'UN0015594',
                'UN0015595',
                'UN0015597',
                'UN0015601',
                'UN0015602',
                'UN0015674',
                'UN0015676',
                'UN0015677',
                'UN0015678',
                'UN0015679',
                'UN0015680',
                'UN0015549',
                'UN0015550',
                'UN0015551',
                'UN0015552',
                'UN0015553',
                'UN0015554',
                'UN0015527',
                'UN0015557',
                'UN0015555',
                'UN0015532',
                'UN0015525',
                'UN0015524',
                'UN0015526',
                'UN0015523',
                'UN0015530',
                'UN0015529',
                'UN0015521',
                'UN0015531',
                'UN0015642',
                'UN0015644',
                'UN0015693',
                'UN0015645',
                'UN0015647',
                'UN0015646',
                'UN0015652',
                'UN0015649',
                'UN0015653',
                'UN0015656',
                'UN0015655',
                'UN0015654',
                'UN0015460',
                'UN0015533',
                'UN0015657',
                'UN0015658',
                'UN0015659',
                'UN0015660',
                'UN0015661',
                'UN0015663',
                'UN0015664',
                'UN0015666',
                'UN0015670',
                'UN0015673',
                'UN0016500',
                'UN0016502',
                'UN0016503',
                'UN0016504',
                'UN0016505',
                'UN0016507',
                'UN0016617',
                'UN0016618',
                'UN0016619',
                'UN0016608',
                'UN0016609',
                'UN0016610',
                'UN0016611',
                'UN0016612',
                'UN0016613',
                'UN0016614',
                'UN0016615',
                'UN0016616',
                'UN0016580',
                'UN0016581',
                'UN0016582',
                'UN0016579',
                'UN0016578',
                'UN0016577',
                'UN0016576',
                'UN0016573',
                'UN0016572',
                'UN0016571',
                'UN0016568',
                'UN0016567',
                'UN0016529',
                'UN0016530',
                'UN0016531',
                'UN0016533',
                'UN0016534',
                'UN0016538',
                'UN0016549',
                'UN0016552',
                'UN0016553',
                'UN0016554',
                'UN0016555',
                'UN0016556',
                'UN0015780-R',
                'UN0015781-R',
                'UN0015782-R',
                'UN0015783-R',
                'UN0015784-R',
                'UN0015785-R',
                'UN0015792-R',
                'UN0015793-R',
                'UN0015794-R',
                'UN0015795-R',
                'UN0015796-R',
                'UN0015797-R',
                'UN0015798-R',
                'UN0015799-R',
                'UN0015800-R',
                'UN0015749-R',
                'UN0015751-R',
                'UN0015752-R',
                'UN0015753-R',
                'UN0015755-R',
                'UN0015756-R',
                'UN0015757-R',
                'UN0015759-R',
                'UN0015760-R',
                'UN0015761-R',
                'UN0015762-R',
                'UN0015763-R',
                'UN0015902',
                'UN0015903',
                'UN0015904',
                'UN0015905',
                'UN0015906',
                'UN0015907',
                'UN0015908',
                'UN0015909',
                'UN0015910',
                'UN0015911',
                'UN0015912',
                'UN0015913',
                'UN0016622',
                'UN0016626',
                'UN0016627',
                'UN0016628',
                'UN0016629',
                'UN0016631',
                'UN0016632',
                'UN0016633',
                'UN0016634',
                'UN0016635',
                'UN0016636',
                'UN0016637',
                'UN0015950',
                'UN0015951',
                'UN0015952',
                'UN0016645',
                'UN0016646',
                'UN0016647',
                'UN0016648',
                'UN0016649',
                'UN0016291',
                'UN0016292',
                'UN0016293',
                'UN0016294',
                'UN0016295',
                'UN0016296',
                'UN0016297',
                'UN0016298',
                'UN0016299',
                'UN0016301',
                'UN0016302',
                'UN0016303',
                'UN0016265',
                'UN0016266',
                'UN0016267',
                'UN0016268',
                'UN0016269',
                'UN0016270',
                'UN0016271',
                'UN0016272',
                'UN0016273',
                'UN0016274',
                'UN0016275',
                'UN0016276',
                'UN0016277',
                'UN0016278',
                'UN0016279',
                'UN0016280',
                'UN0016281',
                'UN0016282',
                'UN0016283',
                'UN0016284',
                'UN0016285',
                'UN0016286',
                'UN0016288',
                'UN0016157',
                'UN0016159',
                'UN0016160',
                'UN0016161',
                'UN0016162',
                'UN0016163',
                'XB26600714',
                'XB26600708',
                'XB26600709',
                'XB26600711',
                'XB26600712',
                'XB26600713',
                'UN0016177',
                'UN0016178',
                'UN0016180',
                'UN0016181',
                'UN0016182',
                'UN0016240',
                'UN0016241',
                'UN0016242',
                'UN0016243',
                'UN0016244',
                'UN0016245',
                'UN0016246',
                'UN0016247',
                'UN0016248',
                'UN0016249',
                'UN0016250',
                'UN0016251',
                'UN0016184',
                'UN0016185',
                'UN0016187',
                'UN0016189',
                'UN0016190',
                'UN0016191',
                'UN0016192',
                'UN0015953',
                'UN0015954',
                'UN0015955',
                'UN0015956',
                'UN0015957',
                'UN0015958',
                'UN0015959',
                'UN0015960',
                'UN0015961',
                'UN0015962',
                'UN0015964',
                'UN0015965',
                'UN0015975',
                'UN0015976',
                'UN0015977',
                'UN0016449',
                'UN0016450',
                'UN0016451',
                'UN0016452',
                'UN0016453',
                'UN0016454',
                'UN0016455',
                'UN0016456',
                'UN0016457',
                'UN0016458',
                'UN0016459',
                'UN0016460',
                'UN0016461',
                'UN0016462',
                'UN0016463',
                'UN0016464',
                'UN0016465',
                'UN0016466',
                'UN0016467',
                'UN0016468',
                'UN0016469',
                'UN0016470',
                'UN0016471',
                'UN0016472',
                'UN0015979',
                'UN0015980',
                'UN0015981',
                'UN0015982',
                'UN0015983',
                'UN0015984',
                'UN0015985',
                'UN0015986',
                'UN0015987',
                'UN0015988',
                'UN0015989',
                'UN0015990',
                'UN0013823',
                'UN0013824',
                'UN0015991',
                'UN0015992',
                'UN0015993',
                'UN0015994',
                'UN0015995',
                'UN0015996',
                'UN0015997',
                'UN0015998',
                'UN0015999',
                'UN0016000',
                'AO961329',
                'AO961331',
                'AO847132',
                'AO852572',
                'AO856138',
                'AO961351',
                'AO832344',
                'AO961348',
                'AO961346',
                'AO961343',
                'AO961339',
                'AO961337',
                'UN0015490-R',
                'UN0015491-R',
                'UN0015493-R',
                'UN0015494-R',
                'UN0015495-R',
                'UN0015496-R',
                'UN0015497-R',
                'UN0015498-R',
                'UN0016128',
                'UN0016129',
                'UN0016130',
                'UN0016133',
                'UN0016143',
                'UN0016134',
                'UN0016135',
                'UN0016137',
                'UN0016138',
                'UN0016142',
                'UN0015439-R',
                'UN0015435-R',
                'UN0015436-R',
                'UN0016121',
                'UN0016122',
                'UN0016123',
                'UN0016124',
                'UN0016125',
                'UN0016126',
                'UN0016136',
                'UN0016139',
                'UN0016140',
                'UN0016141',
                'UN0016144',
                'UN0016145',
                'UN0016146',
                'UN0016147',
                'UN0016153',
                'UN0016211',
                'UN0016150',
                'UN0016342',
                'UN0016395',
                'UN0016421',
                'UN0016677',
                'UN0016264',
                'UN0016289',
                'UN0016566',
                'UN0016595',
                'UN0016620',
                'UN0016621',
                'UN0016678',
                'UN0016679',
                'UN0016680',
                'UN0016681',
                'UN0016682',
                'UN0016683',
                'UN0016684',
                'UN0016685',
                'UN0016436',
                'UN0016424',
                'UN0016425',
                'UN0016427',
                'UN0016428',
                'UN0016429',
                'UN0016430',
                'UN0016431',
                'UN0016434',
                'UN0016435',
                'AO957412',
                'AO955152',
                'AO957411',
                'AO957416',
                'AO957419',
                'AO957421',
                'UN0015440-R',
                'UN0015441-R',
                'UN0015442-R',
                'UN0015443-R',
                'UN0015444-R',
                'UN0015445-R',
                'UN0015446-R',
                'UN0015447-R',
                'UN0015448-R',
                'UN0015451-R',
                'AO957413',
                'AO949252',
                'AO949253',
                'AO957404',
                'AO957405',
                'AO957401',
                'UN0015449-R',
                'UN0015472-R',
                'AO949237',
                'AO949238',
                'AO949240',
                'AO949243',
                'AO949246',
                'AO957083',
                'AO957089',
                'AO957091',
                'AO957092',
                'AO950486',
                'AO860494',
                'AO860497',
                'AO860503',
                'AO860509',
                'AO860505',
                'AO951257',
                'AO951230',
                'AO951226',
                'AO951214',
                'AO950488',
                'AO951212',
                'UN0015607-R',
                'UN0015608-R',
                'UN0015609-R',
                'UN0015610-R',
                'UN0015612-R',
                'AO950484',
                'AO950487',
                'AO951210',
                'AO951211',
                'AO951213',
                'AO951216',
                'AO951219',
                'AO951220',
                'AO957397',
                'AO949560',
                'AO949570',
                'XB26602704',
                'XB26602706',
                'XB26602708',
                'XB26602711',
                'XB26602713',
                'XB26602714',
                'AO957704',
                'AO957700',
                'AO957698',
                'AO945641',
                'AO860517',
                'AO943300',
                'AO945407',
                'AO945629',
                'AO945632',
                'AO945636',
                'AO949632',
                'AO957707',
                'AO957710',
                'AO957714',
                'AO957716',
                'AO957719',
                'AO950307',
                'AO950308',
                'AO950490',
                'AO950492',
                'AO950493',
                'AO950496',
                'AO950498',
                'AO950499',
                'AO950502',
                'AO950503',
                'AO955351',
                'AO955673',
                'AO855822',
                'AO855841',
                'AO855842',
                'AO855848',
                'AO856074',
                'AO954988',
                'AO955236',
                'AO955238',
                'XB26603905',
                'XB26603904',
                'XB26603903',
                'XB26603407',
                'XB26603406',
                'XB26603405',
                'XB26603404',
                'XB26603403',
                'XB26603402',
                'UN0015211',
                'UN0015208',
                'UN0015210',
                'UN0016343',
                'UN0016345',
                'AO949607',
                'AO949613',
                'AO949620',
                'AO949623',
                'AO949808',
                'AO955244',
                'AO949609',
                'AO949611',
                'AO949612',
                'AO949615',
                'AO954046',
                'AO954047',
                'AO954051',
                'AO954052',
                'AO955155',
                'AO949606',
                'AO949619',
                'AO949622',
                'AO949626',
                'AO949627',
                'AO954037',
                'AO954056',
                'AO954057',
                'AO954060',
                'XB26603408',
                'XB26603409',
                'XB26603410',
                'XB26603411',
                'XB26603412',
                'XB26603413',
                'XB26603720',
                'XB26603730',
                'XB26603731',
                'XB26603630',
                'XB26603629',
                'XB26603628',
                'XB26603627',
                'XB26603625',
                'XB26603624',
                'XB26603623',
                'XB26603620',
                'XB26603531',
                'XB26603530',
                'XB26603529',
                'XB26603528',
                'XB26603527',
                'XB26603526',
                'XB26603525',
                'XB26603524',
                'XB26603523',
                'XB26603419',
                'XB26603710',
                'XB26603709',
                'XB26603708',
                'XB26603707',
                'XB26603706',
                'XB26603705',
                'XB26603704',
                'XB26603703',
                'XB26603702',
                'AO860500',
                'AO949236',
                'AO955154',
                'AO955224',
                'AO955227',
                'AO955230',
                'AO955233',
                'AO955241',
                'AO955246',
                'UN0013803',
                'UN0013804',
                'UN0013805',
                'UN0013793',
                'UN0013794',
                'UN0013795',
                'UN0013796',
                'UN0013797',
                'UN0013798',
                'UN0013799',
                'UN0013801',
                'UN0013802',
                'UN0013763',
                'UN0013764',
                'UN0013765',
                'UN0013754',
                'UN0013755',
                'UN0013756',
                'UN0013757',
                'UN0013758',
                'UN0013759',
                'UN0013760',
                'UN0013761',
                'UN0013762',
                'UN0015535',
                'UN0015536',
                'UN0015537',
                'UN0015538',
                'UN0015539',
                'UN0015541',
                'UN0015542',
                'UN0015544',
                'UN0015545',
                'UN0015546',
                'UN0015547',
                'UN0015548',
                'UN0015681',
                'UN0015558',
                'UN0015560',
                'UN0015561',
                'UN0015683',
                'UN0015559',
                'UN0015685',
                'UN0015686',
                'UN0015687',
                'UN0015688',
                'UN0015690',
                'UN0015691',
                'UN0016508',
                'UN0016509',
                'UN0016510',
                'UN0016511',
                'UN0016512',
                'UN0016513',
                'UN0016514',
                'UN0016515',
                'UN0016516',
                'UN0016517',
                'UN0016518',
                'UN0016519',
                'UN0016528',
                'UN0016521',
                'UN0016524',
                'UN0016525',
                'UN0016526',
                'UN0016527',
                'UN0016605',
                'UN0016606',
                'UN0016607',
                'UN0016604',
                'UN0016603',
                'UN0016602',
                'UN0016601',
                'UN0016600',
                'UN0016599',
                'UN0016598',
                'UN0016597',
                'UN0016596',
                'UN0016592',
                'UN0016593',
                'UN0016594',
                'UN0016583',
                'UN0016584',
                'UN0016585',
                'UN0016586',
                'UN0016587',
                'UN0016588',
                'UN0016589',
                'UN0016590',
                'UN0016591',
                'UN0016539',
                'UN0016540',
                'UN0016544',
                'UN0016546',
                'UN0016547',
                'UN0016548',
                'UN0016557',
                'UN0016558',
                'UN0016560',
                'UN0016561',
                'UN0016562',
                'UN0016565',
                'UN0015786-R',
                'UN0015787-R',
                'UN0015788-R',
                'UN0015789-R',
                'UN0015790-R',
                'UN0015791-R',
                'UN0015764-R',
                'UN0015765-R',
                'UN0015767-R',
                'UN0015768-R',
                'UN0015769-R',
                'UN0015770-R',
                'UN0015772-R',
                'UN0015774-R',
                'UN0015771-R',
                'UN0015778-R',
                'UN0015775-R',
                'UN0015777-R',
                'UN0015914',
                'UN0015915',
                'UN0015916',
                'UN0015917',
                'UN0015918',
                'UN0015919',
                'UN0015920',
                'UN0015924',
                'UN0015925',
                'UN0015926',
                'UN0015927',
                'UN0016223',
                'UN0016224',
                'UN0016225',
                'UN0016222',
                'UN0016220',
                'UN0016218',
                'UN0016217',
                'UN0016216',
                'UN0016215',
                'UN0016214',
                'UN0016213',
                'UN0016212',
                'UN0016236',
                'UN0016237',
                'UN0016238',
                'UN0016226',
                'UN0016227',
                'UN0016228',
                'UN0016229',
                'UN0016230',
                'UN0016232',
                'UN0016233',
                'UN0016234',
                'UN0016235',
                'UN0016304',
                'UN0016305',
                'UN0016306',
                'UN0016307',
                'UN0016308',
                'UN0016309',
                'UN0016310',
                'UN0016311',
                'UN0016312',
                'UN0016314',
                'UN0016315',
                'UN0016316',
                'UN0016165',
                'UN0016166',
                'UN0016167',
                'UN0016168',
                'UN0016169',
                'UN0016170',
                'XB26600702',
                'XB26600703',
                'XB26600704',
                'XB26600705',
                'XB26600706',
                'XB26600707',
                'UN0016171',
                'UN0016172',
                'UN0016173',
                'UN0016174',
                'UN0016175',
                'UN0016176',
                'UN0016252',
                'UN0016253',
                'UN0016254',
                'UN0016255',
                'UN0016256',
                'UN0016257',
                'UN0016258',
                'UN0016259',
                'UN0016260',
                'UN0016261',
                'UN0016262',
                'UN0016263',
                'UN0016651',
                'UN0016652',
                'UN0016653',
                'UN0016654',
                'UN0016655',
                'UN0016656',
                'UN0016657',
                'UN0016658',
                'UN0016660',
                'UN0016661',
                'UN0016662',
                'UN0016663',
                'UN0016664',
                'UN0016665',
                'UN0016666',
                'UN0016667',
                'UN0016668',
                'UN0016669',
                'UN0016670',
                'UN0016671',
                'UN0016672',
                'UN0016674',
                'UN0016675',
                'UN0016676',
                'UN0016197',
                'UN0016198',
                'UN0016200',
                'UN0016202',
                'UN0016203',
                'UN0016204',
                'UN0016205',
                'UN0016206',
                'UN0016207',
                'UN0016208',
                'UN0016209',
                'UN0016210',
                'UN0015966',
                'UN0015967',
                'UN0015968',
                'UN0015969',
                'UN0015970',
                'UN0015971',
                'UN0015972',
                'UN0015973',
                'UN0015974',
                'AO847102',
                'AO852571',
                'AO954613',
                'AO961336',
                'AO961332',
                'AO847106',
                'AO847109',
                'AO961330',
                'AO947383',
                'UN0015469-R',
                'UN0015470-R',
                'UN0015471-R',
                'UN0015473-R',
                'UN0015479-R',
                'UN0015480-R',
                'UN0015481-R',
                'UN0015482-R',
                'UN0015483-R',
                'UN0015484-R',
                'UN0015486-R',
                'XB26600715',
                'XB26600717',
                'XB26600722',
                'XB26600721',
                'XB26600719',
                'XB26600718',
                'XB26600716',
                'XB26600720',
                'XB26600723',
                'XB26600724',
                'XB26600725',
                'XB26600726',
                'UN0015603-R',
                'UN0015453-R',
                'AO949244',
                'AO949249',
                'AO949230',
                'UN0015454-R',
                'XB26602525',
                'XB26602520',
                'XB26602521',
                'XB26602524',
                'XB26602523',
                'XB26602522',
                'XB26604306',
                'XB26604303',
                'XB26604304',
                'XB26604305',
                'XB26604307',
                'XB26604308',
                'XB26604309',
                'XB26604310',
                'XB26604311',
                'AO949231',
                'AO949239',
                'AO949241',
                'AO949245',
                'AO949247',
                'AO949250',
                'AO949254',
                'AO957402',
                'AO957406',
                'AO957409',
                'AO957410',
                'AO957414',
                'AO851043',
                'XB26602527',
                'XB26602528',
                'XB26602529',
                'XB26602531',
                'XB26602615',
                'XB26602616',
                'XB26602618',
                'XB26604302',
                'AO850999',
                'AO860496',
                'AO860506',
                'AO951209',
                'AO951215',
                'AO951217',
                'AO951218',
                'AO951221',
                'AO951223',
                'AO951224',
                'AO951225',
                'AO951258',
                'AO946130',
                'AO946147',
                'AO946150',
                'AO949575',
                'AO949577',
                'AO954776',
                'AO954779',
                'AO954780',
                'AO954782',
                'AO954783',
                'AO954993',
                'AO954996',
                'AO946141',
                'AO954972',
                'AO954979',
                'AO954982',
                'AO954994',
                'AO956246',
                'AO943710',
                'AO943711',
                'AO946146',
                'AO946149',
                'AO946153',
                'AO949558',
                'AO954973',
                'AO954975',
                'AO954980',
                'AO954984',
                'AO956241',
                'AO956242',
                'AO949131',
                'AO949370',
                'AO949373',
                'AO949377',
                'AO949378',
                'AO954983',
                'AO949355',
                'AO949356',
                'AO949363',
                'AO949372',
                'AO949375',
                'AO949562',
                'AO949564',
                'AO949568',
                'AO949573',
                'AO949578',
                'AO949579',
                'XB26603020',
                'XB26603021',
                'XB26603022',
                'XB26603023',
                'XB26603024',
                'XB26603025',
                'XB26603026',
                'XB26603027',
                'XB26603028',
                'XB26603029',
                'XB26603030',
                'XB26603031',
                'AO945631',
                'AO945634',
                'AO945635',
                'AO949368',
                'AO949569',
                'AO949571',
                'AO858488',
                'AO860514',
                'AO948814',
                'AO957705',
                'AO957706',
                'AO957709',
                'AO957712',
                'AO957713',
                'AO957715',
                'AO957718',
                'AO957720',
                'AO957721',
                'XB26603220',
                'XB26603221',
                'XB26603222',
                'XB26603223',
                'XB26603224',
                'XB26603225',
                'XB26603226',
                'XB26603227',
                'XB26603228',
                'XB26603229',
                'XB26603230',
                'XB26603231',
                'XB26603514',
                'XB26603512',
                'XB26603511',
                'XB26603509',
                'XB26603508',
                'XB26603507',
                'XB26603506',
                'XB26603505',
                'XB26603504',
                'XB26603503',
                'XB26603502',
                'XB26602720',
                'XB26603119',
                'XB26603102',
                'XB26603103',
                'XB26603104',
                'XB26603105',
                'XB26603111',
                'XB26603112',
                'XB26603114',
                'XB26603116',
                'XB26603117',
                'XB26603118',
                'AO957785',
                'AO955684',
                'AO955683',
                'AO955692',
                'AO955690',
                'AO955688',
                'XB26602721',
                'XB26602722',
                'XB26602723',
                'XB26602724',
                'XB26602726',
                'XB26602727',
                'XB26602728',
                'XB26602729',
                'XB26602730',
                'AO957788',
                'AO949604',
                'AO949580',
                'XB26603919',
                'XB26603918',
                'XB26603917',
                'XB26603916',
                'XB26603914',
                'XB26603913',
                'XB26603912',
                'XB26603911',
                'XB26603902',
                'AO954059',
                'AO954054',
                'AO952010',
                'AO949624',
                'AO949618',
                'AO949610',
                'AO949614',
                'AO949617',
                'AO949621',
                'AO949625',
                'AO949628',
                'AO954036',
                'AO954055',
                'AO954058',
                'AO949232',
                'AO954043',
                'AO954044',
                'AO954045',
                'AO954048',
                'AO954049',
                'AO954050',
                'AO954053',
                'AO955157',
                'XB26603729',
                'XB26603728',
                'XB26603727',
                'XB26603726',
                'XB26603725',
                'XB26603724',
                'XB26603723',
                'XB26603722',
                'XB26603721',
                'XB26604218',
                'XB26602517',
                'XB26602516',
                'XB26603414',
                'XB26603415',
                'XB26603416',
                'XB26603417',
                'XB26603418',
                'XB26603631',
                'XB26603719',
                'XB26603717',
                'XB26603716',
                'XB26603715',
                'XB26603714',
                'XB26603713',
                'XB26603712',
                'XB26603711',
                'XB26604212',
                'XB26604210',
                'XB26604209',
                'XB26604208',
                'XB26604207',
                'XB26604202',
                'XB26603922',
                'XB26603921',
                'XB26603920',
                'XB26602424',
                'XB26602425',
                'XB26602426',
                'XB26602427',
                'XB26602428',
                'XB26602429',
                'XB26604102',
                'XB26604107',
                'XB26604109',
                'XB26604111',
                'XB26604112',
                'XB26604113',
                'XB26604114',
                'XB26604115',
                'XB26604116',
                'XB26604117',
                'XB26604118',
                'XB26604119',
                'XB26604103',
                'XB26604104',
                'XB26604105',
                'XB26604412',
                'XB26604413',
                'XB26604414',
                'XB26604415',
                'XB26604416',
                'XB26604417',
                'XB26604418',
                'XB26604419'
        ]
        def tkey = "nil_etch"
        def db = mongo.getDB("glo")
        def cnt = 0
        unts.each { code ->
            def query = new BasicDBObject()
            query.put("code", code)
            def fields = new BasicDBObject()
            fields.put("id", 1)
            fields.put("code", 1)
            def units = db.unit.find(query, fields).collect { it }

            def unitsToBeSynched = [:]
            unitsToBeSynched.put('data', [])
            units.each {
                def s = [:]
                s.put('_id', it._id)
                s.put('code', it.code)
                s.put('pctg', "nwLED")
                s.put('pkey', "patterning")
                unitsToBeSynched['data'].add(s)
            }
            syncService.execute(unitsToBeSynched, null, tkey, 'admin')
            cnt += 1
            render (['cnt': cnt + ' ' + code] as JSON)
        }
    }

    def ebeam = {
        persistenceInterceptor.init()
       // importService.eBeamData(db, grailsApplication.config.glo.eBeamDirectory)
        return (importService.eBeamItoData(mongo.getDB("glo"), grailsApplication.config.glo.eBeamItoDirectory) as JSON)
    }
	
}
