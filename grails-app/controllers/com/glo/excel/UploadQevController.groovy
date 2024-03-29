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
    def summarizeSyncCurrService
    def couponService
    def syncService
    def contentService
    def importService
    def spcService
    def grailsApplication
    def basicDataSyncService
    def persistenceInterceptor
    def mongo

    private static final logr = LogFactory.getLog(this)

    def updateVars = {


        def dvvs = DataViewVariable.findAllByFullPathAndIsFormula(null, null)

        dvvs.each { dvv ->

            if (dvv.variable) {

                def pctg = dvv.variable.process ? dvv.variable.process?.category : dvv.variable.processStep?.process?.category
                def pkey = dvv.variable.process ? dvv.variable.process?.pkey : dvv.variable.processStep?.process?.pkey
                def tkey = dvv.variable.processStep?.taskKey

                def v = dvv.variable
                if (v.name == "code" || v.name == "nNotes" || v.name == "movedBy" || v.name == "tkey" || v.name == "pkey" ||
                        v.name == "idx" || v.name == "product" || v.name == "productCode" || v.name == "updated" ||
                        v.name == "lotNumber" || v.name == "supplier" || v.name == "supplierProductCode" || v.name == "closed" ||
                        v.name == "yieldLoss" || v.name == "yieldLossStep" || v.name == "yieldLossDate" || v.name == "active" ||
                        v.name == "processViolations") {
                    pkey = ""
                }

                if (pctg) {
                    if (!pkey)
                        dvv.fullPath = pctg
                    if (pkey && !tkey)
                        dvv.fullPath = pctg + "|" + pkey
                    else if (pkey && tkey)
                        dvv.fullPath = pctg + "|" + pkey + "|" + tkey

                    dvv.save(failOnError: true)
                }
            }
        }
    }

    def fixNotes = {

        def db = mongo.getDB("glo")
        def units = db.unitarchive.find(new BasicDBObject(), new BasicDBObject()).limit(2000).addSpecial('$orderby', ['lastUpdated': -1]).collect {
            it
        }

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

        def ret = temp.collect {

            def bdoUnit = new BasicDBObject()
            bdoUnit.put("id", it["_id"])
            bdoUnit.put("processCategory", "nwLED")
            bdoUnit.put("processKey", "por_coalesce")
            bdoUnit.put("taskKey", "post_anneal")
            unitService.update(bdoUnit, "admin", true)
            c++
        }

        render(['cnt': c] as JSON)
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

        ss.each { code ->
            def temp = db.unit.find([code: code]).collect { it }[0]
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

        render(['cnt': c] as JSON)
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

        render(['success'] as JSON)
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

        render(['cnt': cnt, 'compl': c] as JSON)
    }

    def recalcHistory = {

        def bdo = new BasicDBObject()
        def db = mongo.getDB("glo")
        bdo.put("parentCode", null)
        bdo.put("actualStart", new BasicDBObject('$gt', new Date() - 90))
        bdo.put("audit.pctg", new BasicDBObject('$in', ['nwLED', 'RctHdw']))

        def temp = db.history.find(bdo, new BasicDBObject())
        int c = 0
        def ret = temp.collect {

            def end = ""
            def unit = db.unit.find(new BasicDBObject("code", it.code), new BasicDBObject()).collect { it }[0]
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
                    db.dataReport.update(['code': it.code], ['$set': ['value.active': '']])
                } else {
                    db.dataReport.update(['code': it.code], ['$set': ['value.active': 'true']])
                }

                c++
            }
        }

        render(['compl': c] as JSON)
    }

    def recalcHistory2 = {

        def bdo = new BasicDBObject()
        def db = mongo.getDB("glo")
        if (params.code) {
            bdo.put("code", params.code)
        }
        bdo.put("parentCode", null)
        bdo.put("actualStart", new BasicDBObject('$gt', new Date() - 91))
        bdo.put("audit.pctg", new BasicDBObject('$in', ['nwLED', 'RctHdw']))

        def temp = db.history.find(bdo, new BasicDBObject())
        def c1 = []
        def ret = temp.collect {

            def end = ""
            def unit = db.unit.find(new BasicDBObject("code", it.code), new BasicDBObject()).collect { it }[0]
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

                        pkeyPrev = aud["pkey"]
                    }

                    db.history.save(it)

                    c1.add(it.code)
                }
            }
        }

        render(c1 as JSON)
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
        def units = db.unit.find(new BasicDBObject("qtyOut", 0), new BasicDBObject()).collect { it }
        def c = 0
        def f = 0

        units.each {

            db.unitarchive.save(it)
            db.unit.remove(["_id": it["_id"]])

            c++

        }

        render(['compl': c, 'fail': f] as JSON)
    }


    def updateYieldLoss = {

        def db = mongo.getDB("glo")
        def c = 0, f = 0
        def hists = db.history.find(new BasicDBObject("parentCode", null))?.collect { hist ->

            def unit = db.unit.find(['code': hist.code]).collect { it }[0]
            if (!unit) {
                unit = db.unitarchive.find(['code': hist.code]).collect { it }[0]
            }

            historyDataService.init(hist, unit)

            c++
        }

        render(['compl': c, 'fail': f] as JSON)
    }

    def updateEquipmentParts = {

        def bdo = new BasicDBObject()
        def db = mongo.getDB("glo")
        bdo.put("parentCode", null)
        //	bdo.put("code", "TC373645")
        bdo.put("value.epi_growth.runNumber", new BasicDBObject('$exists', 1))
        def temp = db.dataReport.find(bdo, new BasicDBObject())

        int c = 0
        def ret = temp.collect {

            def run = it["value"]["epi_growth"]["runNumber"].toUpperCase()

            def bdo2 = new BasicDBObject("_id.run", new BasicDBObject('$lte', run))

            def first = ""
            def hprs = db.hardwarePerRunNumber.find(bdo2, new BasicDBObject()).limit(50).sort(["_id.run": -1]).collect { hpr ->

                if (first == "" && hpr["_id"]["run"])
                    first = hpr["_id"]["run"]

                if (first != "" && first == hpr["_id"]["run"]) {
                    c++
                    ["waferRun": run] + hpr
                }
            }


            hprs.each { hpr ->
                if (hpr) {
                    def bdo3 = new BasicDBObject("run", hpr["waferRun"])
                    bdo3.put("code", hpr["_id"]["code"])
                    def hprs3 = db.hardwarePerRunNumber2.find(bdo3, new BasicDBObject()).collect { it }[0]
                    if (!hprs3) {
                        def bdo4 = new BasicDBObject()
                        bdo4.putAll(hpr["_id"])
                        bdo4["run"] = hpr["waferRun"]

                        db.hardwarePerRunNumber2.save(bdo4)
                    }
                }
            }


        }

        render(['compl': c] as JSON)
    }


    def updateTestData = {

        def bdo = new BasicDBObject()
        def db = mongo.getDB("glo")

        bdo.put("parentCode", null)
        bdo.put("audit.dc.testDataExperiment", new BasicDBObject('$exists', 1))

        def temp = db.history.find(bdo, new BasicDBObject())

        int c = 0
        def saveHist = [:]
        def ret = temp.collect {

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
                    aud.dc["testDataIndex"].addAll(exps.collect { k, v -> v })
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


        render(['compl': c] as JSON)
    }


    def addToTestData = {

        def bdo = new BasicDBObject()
        def db = mongo.getDB("glo")


        def retList = [:]
        try {

            // Fix tags in wafer flow
            def query = new BasicDBObject()
            query.put("parentCode", null)
           // query.put("code", "AAF13150176P-01")
            def df = new Date().clearTime() - 160
            query.put("value.test_data_visualization.actualStart", new BasicDBObject('$gt', df))
            query.put("unit.productCode", "100W")
            def fields = new BasicDBObject()
            fields.put("id", 1)
            fields.put("_id", 1)
            fields.put("code", 1)
            fields.put("unit.mask", 1)
            fields.put("value.tags", 1)
            def units = db.dataReport.find(query, fields).collect { it }
//            units.each {
//                render(it.code + " " + it.mask + "<br/>")
//                def tgs = []
//                it.value.tags.each { tag ->
//                    if (tag.indexOf('W') >= 0) {
//                        tgs.add(tag)
//                    }
//                }
//                db.dataReport.update([code: it.code], [$set:  ['value.tags': tgs]])
//            }

            // Fix tags in coupon flow
            def query2 = new BasicDBObject()
            query2.put("parentCode", null)
            def df2 = new Date().clearTime() - 160
            query2.put("value.test_data_visualization.actualStart", new BasicDBObject('$gt', df))
            query2.put("unit.product", "Coupon")
            def fields2 = new BasicDBObject()
            fields2.put("id", 1)
            fields2.put("_id", 1)
            fields2.put("code", 1)
            fields2.put("unit.mask", 1)
            fields2.put("value.tags", 1)
            def units2 = db.dataReport.find(query2, fields2).collect { it }
//            units2.each {
//                render(it.code + " " + it.mask + "<br/>")
//                def tgs = []
//                it.value.tags.each { tag ->
//                    if (tag.indexOf('C') >= 0) {
//                        tgs.add(tag)
//                    }
//                }
//                db.dataReport.update([code: it.code], [$set:  ['value.tags': tgs]])
//            }

            def couponvars = contentService.getStepVariables("C", "fabassembly", "test_data_visualization", "dc");

            // Reset all coupons test data
            units2.each {
                try {
                    def bdo2 = new BasicDBObject()
                    def unt = db.unit.find(['code': it.code]).collect { it }[0]
                    if (unt) {
                        render(it.code + " " + it.unit.mask + "<br/>")
                        bdo2.put("id", unt["_id"])
                        couponvars.each { v ->
                            if (v.name != 'actualStart') {
                                bdo2.put(v.name, 'NN/AA')
                            }
                        }
                        bdo2.put("processCategory", "C")
                        bdo2.put("processKey", "fabassembly")
                        bdo2.put("taskKey", "test_data_visualization")
                        unitService.update(bdo2, 'admin', true)
                    }
                } catch (Exception exc) {
                    render('C' + exc.toString() + "<br/>")
                }
            }

            // Recalulate all test data for wafers
            def tt = new BasicDBObject('$gt', 170101000000)
            tt.put('$lt', 20000000000000)

            units.each {
                bdo.put("value.code", it.code)
                bdo.put("value.testId", tt)
                bdo.put("value.tkey", "test_data_visualization")

                def temp = db.testData.find(bdo, new BasicDBObject('value.data', 0)).addSpecial('$orderby', new BasicDBObject("value.testId", 1)).collect { td ->
                    ["code": td.value.code, unitId: it.id, "testId": td.value.testId, "mask": it.unit.mask]
                }
                temp.each { td ->
                    try {
                        render(td.code + " " + td.unitId + " " + td.testId + " " + td.mask + "<br/>")
                        System.out.println(render(td.code + " " + td.unitId + " " + td.testId + " " + td.mask + "<br/>"))
                        summarizeSyncCurrService.createSummaries(db, td.unitId, td.code, null, null, null, td.testId, "test_data_visualization", td.mask, null)
                        System.out.println('rrrr')
                        couponService.splitTestDataToCoupons(db, 'admin', 'test_data_visualization', td.code, td.testId, couponvars)
                    } catch (Exception exc) {

                        render('W' + exc.toString() + "<br/>")
                    }
                }
            }

            def tt1 = new BasicDBObject('$gt', 20000000000000)
            units.each {
                bdo.put("value.code", it.code)
                bdo.put("value.testId", tt1)
                bdo.put("value.tkey", "test_data_visualization")

                def temp = db.testData.find(bdo, new BasicDBObject('value.data', 0)).addSpecial('$orderby', new BasicDBObject("value.testId", -1)).collect { td ->
                    ["code": td.value.code, unitId: it.id, "testId": td.value.testId, "mask": it.unit.mask]
                }[0]
                temp.each { td ->
                    try {
                        render(td.code + " " + td.unitId + " " + td.testId + " " + td.mask + "<br/>")
                        System.out.println(render(td.code + " " + td.unitId + " " + td.testId + " " + td.mask + "<br/>"))
                        summarizeSyncCurrService.createSummaries(db, td.unitId, td.code, null, null, null, td.testId, "test_data_visualization", td.mask, null)
                    } catch (Exception exc) {

                        render('W' + exc.toString() + "<br/>")
                    }
                }
            }


        }
        catch (Exception exc) {
            logr.error(exc.getMessage())
            render(exc.getMessage())
        }

    }


    def nidot = {

        def tkey = "ni_dot_test"
        String syncType = "PROBETESTNEW|" + tkey

        def db = mongo.getDB("glo")
        def query = new BasicDBObject()
      //   query.put("code", 'HNC3563PS')
        query.put("parentCode", null)
        query.put("value.productCode", new BasicDBObject('$in', ['100', '100W', '101W', '110W', '105W', '111W']))
        def df = new Date().clearTime() - 12
        def dt = new Date().clearTime() - 6
        def dbo = new BasicDBObject('$gt', df)
        dbo.put('$lte', dt)
        query.put("value.ni_dot_test.actualStart", dbo)
        def fields = new BasicDBObject()
        fields.put("id", 1)
        fields.put("code", 1)

        def unitsToBeSynched = [:]
        db.dataReport.find(query, fields).collect {
            def unit = db.unit.find(new BasicDBObject('code',it.code), new BasicDBObject()).collect {it}[0]
            if (unit) {
                unitsToBeSynched.put(unit, '')
            }
        }

        basicDataSyncService.init(syncType, unitsToBeSynched)

        render(['cnt': unitsToBeSynched.size()] as JSON)
    }


    def ipc = {
        def unts = [
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
            render(['cnt': cnt + ' ' + code] as JSON)
        }
    }

    def ebeam = {
        persistenceInterceptor.init()
        // importService.eBeamData(db, grailsApplication.config.glo.eBeamDirectory)
        return (importService.eBeamItoData(mongo.getDB("glo"), grailsApplication.config.glo.eBeamItoDirectory) as JSON)
    }

}
