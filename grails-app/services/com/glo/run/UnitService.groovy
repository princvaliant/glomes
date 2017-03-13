package com.glo.run

import com.glo.custom.DieSpec
import com.glo.ndo.*
import com.glo.security.User
import com.mongodb.BasicDBObject
import grails.converters.JSON
import org.apache.commons.logging.LogFactory
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics
import org.apache.commons.validator.GenericValidator

import javax.jms.Message
import java.text.DateFormat
import java.text.SimpleDateFormat

class UnitService {

    private static final logr = LogFactory.getLog(this)

    def grailsApplication
    def managementService
    def runtimeService
    def repositoryService
    def taskService
    def historyService
    def historyDataService
    def messageSource
    def workflowService
    def contentService
    def sequenceGeneratorService
    def waferFilterService
    def utilsService
    def productsService
    def fileService

    def mongo

    def inStep(def db, def code, def pkey, def tkey) {

        def query = new BasicDBObject()
        query.put("code", code)
        query.put("value." + tkey, new BasicDBObject('$exists', true))
        if (pkey != "")
            query.put("value." + tkey + ".pkey", pkey)
        def temp = db.dataReport.find(query, ['code': 1]).collect { it }

        temp ? true : false
    }

    /// Retrieve list of tasks for user, category and taskName
    /// Return object fields:
    /// String taskId
    /// String taskName
    /// Other fields
    def getUnitsLocal(String user, String category, String procKey, String taskKey, String searchText,
                      def page, def offset, def max, def srt, def dir, def filter) {

        def usr = User.findByUsername(user)
        def grps = usr?.getAuthorities()?.collect { it.id }

        def processStep = workflowService.getProcessStep(category, procKey, taskKey)
        def variables = contentService.getVariables(category, procKey, taskKey, '')

        def query = new BasicDBObject()
        query.put("tkey", taskKey)
        query.put("pkey", procKey)
        query.put("pctg", category)
        query.put("parentCode", null)

        def sec1 = new BasicDBObject()
        def g = new BasicDBObject()
        g.put('$in', grps)
        sec1.put("grps", g)
        def sec2 = new BasicDBObject()
        def u = new BasicDBObject()
        u.put('$in', [user])
        sec2.put("usrs", u)
        query.put('$or', [sec1, sec2])

        if (searchText) {
            def e = new BasicDBObject()
            e.put('$regex', '(?i)^' + searchText.toUpperCase())
            query.put('$or', [new BasicDBObject("tags", e), new BasicDBObject("tagsCustom", e)])
        }

        if (usr?.company && !grps.contains("ROLE_ADMIN")) {
            query.put("companyId", usr.company.id.toString())
        }

        if (filter) {
            JSON.parse(filter).collect {
                if (it.type == "string") {
                    query.put(it.field, new BasicDBObject('$regex', '^' + it.value?.trim()))
                }
                if (it.type == "numeric") {
                    if (it.comparison == 'eq') {
                        query.put(it.field, it.value)
                    } else {
                        if (query[it.field]) {
                            query[it.field].put('$' + it.comparison, it.value)
                        } else {
                            query.put(it.field, new BasicDBObject('$' + it.comparison, it.value))
                        }
                    }
                }
            }
        }

        def s = new BasicDBObject()
        if (srt) {
            int ord = dir == "ASC" ? 1 : -1
            s.put(srt, ord)
        }

        // Return fields required for the step
        def fields = new BasicDBObject()
        def objectArrays = []
        variables.each {
            if (it.dataType != 'object' && it.dir in ['', 'dc', 'din', 'in', 'recp', 'calc']) {
                fields.put(it.name, 1)
            }
            if (it.dataType == 'objectArray') {
                objectArrays.add(it.name)
            }
        }
        fields.put("bomId", 1)
        fields.put("bomOk", 1)
        fields.put("bomRevision", 1)

        def outVars = variables.findAll { it.dir == 'dc' || it.dir == 'din' }
        def specVars = variables.findAll { it.dir == 'spec' }
        def calcVars = variables.findAll { it.dir == 'calc' }

        def pOffset = offset
        def pMax = max
        def sortByCalc = false
        for (def calcVar in calcVars) {
            if (calcVar.name == srt) {
                pOffset = 0
                pMax = 9999
                sortByCalc = true
            }
        }

        def db = mongo.getDB("glo")
        def cnt = db.unit.count(query)
        def temp = db.unit.find(query, fields).skip(pOffset.toInteger()).limit(pMax.toInteger()).addSpecial('$orderby', s)

        def dt = temp.collect {
            objectArrays.each { oa ->
                if (it.containsKey(oa) && it[oa] != null) {
                    it[oa] = "Count: " + new Long(it[oa].size()).toString()
                }
            }

            getCalculatedValues(it, calcVars, outVars)

            if (processStep && processStep.moveChildren == true) {
                it.put('nJumps', 1)
            } else if (processStep && procKey != 'W') {
                it.put('nJumps', 2)
            } else {
                it.put('nJumps', 0)
            }

            def limitSpecs = []

            def isValid = validate(it, outVars, specVars, limitSpecs)

            String specs2 = ""
            if (isValid == "2") {
                specVars.each {
                    specs2 = specs2 + it.evalScript?.trim()
                }
            }
            if (isValid in ["3", "4"]) {
                specs2 = limitSpecs[0]
            }

            if (it.containsKey("specs"))
                it["specs"] = specs2
            else
                it.put('specs', specs2)

            it.put('id', it._id)
            it.put('ok', isValid)

            it
        }

        if (sortByCalc) {
            dt.sort { it[srt] }
            if (dir != "ASC") {
                dt = dt.reverse()
            }
            if (cnt < offset.toInteger() + max.toInteger())
                dt = dt.subList(offset.toInteger(), cnt.toInteger())
            else
                dt = dt.subList(offset.toInteger(), offset.toInteger() + max.toInteger())
        }

        [
                data : dt,
                count: cnt
        ]

    }

    def getUnitsGlobal(String user, String search, def hist, def page, def offset, def max, String bom) {

        def bdo = new BasicDBObject()
        def bdo2 = new BasicDBObject()
        def fields = new BasicDBObject()

        fields.put('id', 1)
        fields.put('code', 1)
        fields.put('product', 1)
        fields.put('productCode', 1)
        fields.put('lotNumber', 1)

        def db = mongo.getDB("glo")
        def cnt
        def temp
        def ret = new HashSet()
        def h = ''

        def isExact = false
        def productCode = ""
        search = search ?: ""
        if (search && search.substring(0, 1) == "=") {
            search = search.substring(1)
            isExact = true
        }
        if (search && search.substring(0, 1) == "!") {
            search = search.substring(1)
            productCode = search.tokenize(" ")[0]
            search = search.tokenize(" ")[1]
            if (search.trim().length() < 3)
                search = ""
        }

        if (search || bom) {
            def e
            if (isExact)
                e = search.toUpperCase()
            else {
                e = new BasicDBObject()
                e.put('$regex', '^' + search.toUpperCase())
            }

            if (hist == "true") {
                bdo.put("code", e)
                cnt = db.history.count(bdo)
                temp = db.history.find(bdo, fields).skip(offset.toInteger()).limit(max.toInteger())
                ret = temp.collect { [ok: "0"] + it }
                h = '1'
            } else {

                fields.put('pctg', 1)
                fields.put('pkey', 1)
                fields.put('tkey', 1)
                fields.put('tname', 1)
                fields.put('grps', 1)
                fields.put('qtyIn', 1)
                fields.put('qtyOut', 1)

                if (search) {
                    if (isExact) {
                        bdo.put("code", e)
                        bdo2.put("code", e)
                    } else {
                        bdo.put("tagsCustom", e)
                        bdo.put("parentCode", null)
                        bdo2.put("tags", e)
                        bdo2.put("parentCode", null)
                    }
                }
                if (productCode) {
                    bdo.put("productCode", productCode)
                    bdo2.put("productCode", productCode)
                }

                if (bom) {
                    def prodcodes = productsService.retrieveChildProductCodes(bom)
                    bdo.put("parentCode", null)
                    bdo2.put("parentCode", null)
                    bdo.put("productCode", new BasicDBObject('$in', prodcodes))
                    bdo2.put("productCode", new BasicDBObject('$in', prodcodes))
                }

                cnt = db.unit.count(bdo) + db.unit.count(bdo2)

                db.unit.find(bdo, fields).skip(offset.toInteger()).limit(max.toInteger()).collect {
                    ret.add([id: it._id, ok: "0"] + it)
                }

                db.unit.find(bdo2, fields).skip(offset.toInteger()).limit(max.toInteger()).collect {
                    ret.add([id: it._id, ok: "0"] + it)
                }
            }
        }

        if (ret) {
            [
                    data: ret.sort { it.code }, count: cnt
            ]
        } else {
            [
                    data: [id: -1, ok: "0", pctg: h, code: search], count: 0
            ]
        }
    }

    def getUnit(String user, String code) {

        def bdo = new BasicDBObject()
        bdo.put("code", code)

        def db = mongo.getDB("glo")
        def temp = db.history.find(bdo)
        def addTests = [:]
        def addProbeTests = [:]

        temp.collect {

            for (a in it.audit) {

                def toAdd = [:]
                def toRemove = []

                for (v in a.dc) {
                    toRemove.add("testDataIndex")
                    toRemove.add("testFullIndex")
                    toRemove.add("testTopIndex")
                    toRemove.add("testCharIndex")
                    toRemove.add("spcDataIndex")
                    toRemove.add("testUniformityIndex")
                    toRemove.add("testDataExperiment")
                    toRemove.add("testPreDbrIndex")
                    toRemove.add("testPostDbrIndex")

                    if (a.tkey == "test_data_visualization" || a.tkey == "nbp_test_data_visualization" || a.tkey == "nbp_full_test_visualization" || a.tkey == "intermediate_coupon_test") {
                        if (v.key.indexOf("testDataIndex") >= 0) {
                            v.value.each { v2 ->
                                def td = db.testData.find(['value.code': code, 'value.testId': v2], ['value.devtype': 1, 'value.actarea': 1]).collect {
                                    it
                                }[0]
                                if (td != null) {
                                    def devtype = td.value.devtype
                                    def actarea = td.value.actarea
                                    toAdd.put("testDataIndex_" + v2, "<Units> Dev: " + (devtype ?: "--") + "  Area: " + (actarea ?: "--"))
                                }
                            }
                        }
                    }
                    if (a.tkey == "char_test_visualization") {
                        if (v.key.indexOf("testCharIndex") >= 0) {
                            v.value.each { v2 ->
                                toAdd.put("testCharIndex_" + v2, "<Units>")
                            }
                        }
                    }
                    if (a.tkey == "pre_dbr_test") {
                        if (v.key.indexOf("testPreDbrIndex") >= 0) {
                            v.value.each { v2 ->
                                toAdd.put("testPreDbrIndex_" + v2, "<Units>")
                            }
                        }
                    }
                    if (a.tkey == "post_dbr_test") {
                        if (v.key.indexOf("testPostDbrIndex") >= 0) {
                            v.value.each { v2 ->
                                toAdd.put("testPostDbrIndex_" + v2, "<Units>")
                            }
                        }
                    }
                    if (a.tkey == "uniformity_test_visualization") {
                        if (v.key.indexOf("testUniformityIndex") >= 0) {
                            v.value.each { v2 ->
                                toAdd.put("testUniformityIndex_" + v2, "<Units>")
                            }
                        }
                    }
                    if (a.tkey == "top_test_visualization") {
                        if (v.key.indexOf("testTopIndex") >= 0) {
                            v.value.each { v2 ->
                                toAdd.put("testTopIndex_" + v2, "<Units>")
                            }
                        }
                    }
                    if (a.tkey == "full_test_visualization") {
                        if (v.key.indexOf("testFullIndex") >= 0) {
                            v.value.each { v2 ->
                                toAdd.put("testFullIndex_" + v2, "<Units>")
                            }
                        }
                    }
                    if (a.tkey == "spc_data_visualization") {
                        if (v.key.indexOf("spcDataIndex") >= 0) {
                            v.value.each { v2 ->
                                toAdd.put("spcDataIndex_" + v2, "<Units>")
                            }
                        }
                    }

                    if (a.tkey in ["ni_dot_test", "planar_ni_dot_test", "nw_ito_dot_test", "ito_dot_test", "fa_test"] && v.key.indexOf("probeTestExperiment") >= 0) {
                        toAdd.put("probeTestExperiment", "<Units>")
                    }

                    if ((a.tkey == "test_rel" || a.tkey == "test_rel_board" || a.tkey == 'spc_standard_board') && v.key.indexOf("relTestData") >= 0) {
                        toAdd.put("relTestData", "<Units>")
                    }

                    if ((a.tkey == "wafer_rel_test") && v.key.indexOf("relWaferData") >= 0) {
                        toAdd.put("relWaferData", "<Units>")
                    }

                    if ((a.tkey == "sphere_test_lamp" || a.tkey == "sphere_test2_lamp") && v.key.indexOf("lampTestData") >= 0) {
                        toAdd.put("lampTestData", "<Units>")
                    }

                    if ((a.tkey == "sphere_test_lamp_board" || a.tkey == "sphere_test2_lamp_board") && v.key.indexOf("lampBoardTestData") >= 0) {
                        toAdd.put("lampBoardTestData", "<Units>")
                    }
                    if (a.tkey.indexOf("d65_after_encap") >= 0 && v.key.indexOf("d65SyncedAfterTestData") >= 0) {
                        toAdd.put("d65SyncedAfterTestData", "<Units>")
                    }
                    if (a.tkey == "d65_before_encap" && v.key.indexOf("d65SyncedBeforeTestData") >= 0) {
                        toAdd.put("d65SyncedBeforeTestData", "<Units>")
                    }
                    if (a.tkey == "wafer_wst") {
                        toAdd.put("wstSyncedTestData", "<Units>")
                    }
                    if (a.tkey == "die_wst") {
                        toAdd.put("wstDieSyncedTestData", "<Units>")
                    }
                    if (a.tkey == "d65_rel_test") {
                        toAdd.put("d65RelSyncTestData", "<Units>")
                    }

                    if (a.tkey == "light_bar_rel_test") {
                        toAdd.put("light_bar_relSyncTestData", "<Units>")
                    }
                    if (a.tkey == "ilgp_rel_test") {
                        toAdd.put("ilgp_relSyncTestData", "<Units>")
                    }
                    if (a.tkey == "iblu_rel_test") {
                        toAdd.put("iblu_relSyncTestData", "<Units>")
                    }

                    if (a.tkey.indexOf("light_bar_test") >= 0 && v.key.indexOf("lightbartestTestData") >= 0) {
                        toAdd.put("lightbartestTestData", "<Units>")
                    }
                    if (a.tkey.indexOf("ilgp_test") >= 0 && v.key.indexOf("ilgptestTestData") >= 0) {
                        toAdd.put("ilgptestTestData", "<Units>")
                    }
                    if (a.tkey.indexOf("iblu_sphere_test") >= 0 && v.key.indexOf("ibluspheretestTestData") >= 0) {
                        toAdd.put("ibluspheretestTestData", "<Units>")
                    }
                    if (a.tkey.indexOf("iblu_test") >= 0 && v.key.indexOf("iblutestTestData") >= 0) {
                        toAdd.put("iblutestTestData", "<Units>")
                    }
                    if (a.tkey.indexOf("blu_test") >= 0 && v.key.indexOf("blutestTestData") >= 0) {
                        toAdd.put("blutestTestData", "<Units>")
                    }
                    if (a.tkey.indexOf("display_test") >= 0 && v.key.indexOf("displaytestTestData") >= 0) {
                        toAdd.put("displaytestTestData", "<Units>")
                    }
                    if (a.tkey.indexOf("ilb_overtemp_test") >= 0 && v.key.indexOf("ilbovertemptestSyncedTestData") >= 0) {
                        toAdd.put("iLBTestData", "<Units>")
                        toRemove.add("ilbovertemptestSyncedTestData")
                    }
                    if (a.tkey.indexOf("ilgp_overtemp_test") >= 0 && v.key.indexOf("ilgpovertemptestSyncedTestData") >= 0) {
                        toAdd.put("iLGPTestData", "<Units>")
                        toRemove.add("ilgpovertemptestSyncedTestData")
                    }
                    if (a.tkey.indexOf("iblu_overtemp_test") >= 0 && v.key.indexOf("ibluovertemptestSyncedTestData") >= 0) {
                        toAdd.put("iBLUTestData", "<Units>")
                        toRemove.add("ibluovertemptestSyncedTestData")
                    }

                    if (a.tkey.indexOf("manual_wafer_test") >= 0) {

                    }

                    if (v.value.getClass() == com.mongodb.BasicDBList) {
                        v.value = "<Count " + new Long(v.value.size()) + ">"
                    }
                    if (v.value.getClass() == com.mongodb.BasicDBObject) {
                        v.value = "<Units> "
                    }
                }

                if (a.tkey == "wafer_wst") {
                    toAdd.put("wstSyncedTestData", "<Units>")
                }
                if (a.tkey == "die_wst") {
                    toAdd.put("wstDieSyncedTestData", "<Units>")
                }

                toAdd.each {
                    a.dc.put(it.key, it.value)
                }
                toRemove.each {
                    a.dc.remove(it)
                }

                a.dc = a.dc.sort { k1, k2 -> k2.key <=> k1.key }

                a.duration = utilsService.millisToShortDHMS(a.duration, a.actualStart)
            }

            def fields = new BasicDBObject("code", 1)
            fields.put("productCode", 1)
            fields.put("product", 1)
            fields.put("parentCode", 1)
            def parts = db.history.find(new BasicDBObject("parentCode", code), fields).collect { hist ->
                hist + ['parentCode': code]
            }

            if (parts) {
                it.put("parts", parts)
            }

            it
        }
    }

    def getStepsSummary(String user, String category) {

        def usr = User.findByUsername(user)
        def grps = usr?.getAuthorities()?.collect { it.id }

        // Conditional query
        def cond = new BasicDBObject()

        cond.put('pctg', category)
        cond.put("parentCode", null)

        def sec1 = new BasicDBObject()
        def g = new BasicDBObject()
        g.put('$in', grps)
        sec1.put("grps", g)
        def sec2 = new BasicDBObject()
        def u = new BasicDBObject()
        u.put('$in', [user])
        sec2.put("usrs", u)
        cond.put('$or', [sec1, sec2])

        if (usr?.company && !grps.contains("ROLE_ADMIN")) {
            cond.put("companyId", usr.company.id.toString())
        }

        def db = mongo.getDB("glo")
        def temp = db.unit.aggregate(
                ['$match': cond],
                ['$group': [_id: [pctg: '$pctg', tname: '$tname', tkey: '$tkey', pkey: '$pkey'], tot: [$sum: 1]]]).results()
        // Find appropriate index
        def idxs = ProcessStep.executeQuery("""
			select  ps.process.pkey, ps.taskKey, ps.idx from ProcessStep as ps where ps.process.category = ?
		""", temp[0]._id.pctg)
        def idxs2 = [:]
        idxs.collect {
            idxs2.put(it[0] + '_' + it[1], it[2])
        }

        int i = 1
        temp.collect {
            [order  : idxs2[it._id.pkey + '_' + it._id.tkey] ?: i++, procDefCategory: it._id.pctg, taskName: it._id.tname,
             taskKey: it._id.tkey, procKey: it._id.pkey, total: it.tot]
        }
    }

    def getProcessesSummary(String user, def all) {

        def usr = User.findByUsername(user)
        def grps = usr?.getAuthorities()?.collect { it.id }

        // Conditional query
        def cond = new BasicDBObject()
        cond.put("parentCode", null)
        def sec1 = new BasicDBObject()
        def g = new BasicDBObject()
        g.put('$in', grps)
        sec1.put("grps", g)
        def sec2 = new BasicDBObject()
        def u = new BasicDBObject()
        u.put('$in', [user])
        sec2.put("usrs", u)
        cond.put('$or', [sec1, sec2])

        if (usr?.company && !grps.contains("ROLE_ADMIN")) {
            cond.put("companyId", usr.company.id.toString())
        }

        // Execute grouping
        def db = mongo.getDB("glo")
        def ret = db.unit.aggregate(
                ['$match': cond],
                ['$group': [_id: '$pctg', tot: [$sum: 1]]]).results().collect {
            [
                    categoryId   : it._id,
                    categoryName : messageSource.getMessage("process.category." + it._id, null, it._id, Locale.US),
                    categoryIndex: messageSource.getMessage("process.index." + it._id, null, "A99", Locale.US),
                    total        : it.tot
            ]
        }

        all.each {
            if (it) {
                ret.add([categoryId: it, categoryName: it, categoryIndex: "A99", total: 1])
            }
        }

        ret.sort { it.categoryName.toUpperCase() }.sort { it.categoryIndex }
    }


    def start(def recv, def pctg, def pkey, def recordMove = true, def allowSpecial = false) {

        def retCode = ''
        if (!recv?.units)
            throw new RuntimeException("Data collection and serial numbers not provided.")

        // First validate all the required fields
        def product
        def bom
        def company
        def retUnits = []
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

        product = Product.get(recv.pid)
        if (!product)
            throw new RuntimeException("Product is not specified.")
        if (!product.startProcess)
            throw new RuntimeException("Product " + product.name + " is not assigned to process workflow.")

        bom = product.boms ? product.boms.first() : null

        company = Company.get(recv.cid)

        def dup = new HashSet()

        recv.units.each {

            if (it.code?.trim() && dup.contains(it.code)) {
                throw new RuntimeException(it.code + " serial number is specified more than once.")
            }
            dup.add(it.code)

            Boolean isPrefixed = false
            if (!it.code) {

                if (product.startProcess.idPrefix) {

                    def idps = product.startProcess.idPrefix.tokenize(",")

                    for (int exloop = 0; exloop < 5; exloop++) {

                        if (idps.size() == 2) {
                            it.code = idps[0] + sequenceGeneratorService.next(idps[0]).toString().padLeft(idps[1].toInteger(), '0')
                        } else {
                            it.code = product.startProcess.idPrefix + sequenceGeneratorService.next(product.startProcess.idPrefix).toString()
                        }
                        if (!History.findByCode(it.code))
                            break;
                    }

                    isPrefixed = true
                } else {
                    throw new RuntimeException("Serial number is not provided.")
                }
            } else if (product.startProcess.idPrefix && it.code.indexOf(product.startProcess.idPrefix) != 0)
                throw new RuntimeException("Serial number needs to start with prefix '" + product.startProcess.idPrefix + "'")

            if (isPrefixed == false && allowSpecial == false) {
                def pattern = ~/[A-Za-z0-9\-]+/
                if (!pattern.matcher(it.code).matches())
                    throw new RuntimeException("Only characters, numbers and '-' are allowed in serial number.")
            }

            if (History.findByCode(it.code))
                throw new RuntimeException("Code '" + it.code + "' already exists in database.")
        }

        // Find first task definition in process
        def taskdef = workflowService.findFirstTaskDefinition(pctg, pkey.toLowerCase())
        def varsdc = []

        // Now do object save in database
        Unit.withTransaction { status ->

            recv.units.each {

                def unit = new Unit()

                retCode = it.code

                unit.code = it.code
                unit.genPath = it.code + ","

                unit.qtyIn = 1
                unit.qtyOut = 1

                if (it.qtyOut) {
                    unit.qtyIn = it.qtyOut
                    unit.qtyOut = it.qtyOut
                }


                unit.lotNumber = recv.lot?.trim()
                unit.product = product.name?.trim()
                unit.productCode = product.code?.trim()
                unit.productRevision = product.revision?.trim()
                unit.productFamily = product.productFamily?.name?.trim()
                unit.isBulk = product.isBulk
                if (product.boms?.size() > 0) {
                    unit.isBulk = "false"
                }


                def newUnit = null
                if (product.isBulk && recv.override != "true") {

                    //  Check existence of new unit
                    newUnit = Unit.withCriteria {
                        eq('pctg', taskdef.pctg)
                        eq('pkey', taskdef.pkey)
                        eq('tkey', taskdef.tkey)
                        eq('productCode', product.code?.trim())
                        eq('supplierId', recv.cid)
                    }[0]

                    if (newUnit) {

                        def oldNewUnit = deepClone(newUnit)
                        newUnit.qtyIn = newUnit.qtyIn + (!it.qtyOut ? 0 : it.qtyOut.toLong())
                        newUnit.qtyOut = newUnit.qtyOut + (!it.qtyOut ? 0 : it.qtyOut.toLong())
                        if (it.actualStart) {
                            newUnit.actualStart = df.parse(it.actualStart.replace("T", " ")) ?: new Date()
                        }
                        if (recv.note) {
                            newUnit.addToNotes(new Note(stepName: newUnit.tname, comment: recv.note, userName: recv.uid))
                            newUnit.nNotes = newUnit.notes?.size()
                        }

                        newUnit.save()

                        def options = [:]
                        options.put("processCategory", newUnit['pctg'])
                        options.put("processKey", newUnit['pkey'])
                        options.put("taskKey", newUnit['tkey'])

                        historyService.initHistory("update", oldNewUnit, newUnit.dbo, options)
                    }
                }

                if (newUnit == null) {

                    unit.pkey = taskdef.pkey
                    unit.pctg = taskdef.pctg
                    unit.tkey = taskdef.tkey
                    unit.tname = taskdef.tname
                    unit.start = new Date()
                    unit.actualStart = new Date()
                    unit.prior = 50

                    // Store supplier into unit
                    unit['supplierId'] = recv.cid
                    unit['supplier'] = (company?.name ?: "UNKNOWN").trim()
                    unit['supplierProductCode'] = recv.prodCode?.trim()
                    if (recv?.price?.isFloat())
                        unit['price'] = recv.price.toFloat()
                    if (recv?.deliveryTime?.isInteger())
                        unit['deliveryTime'] = recv.deliveryTime.toInteger()
                    if (recv?.uom)
                        unit['uom'] = recv.uom?.trim()
                    if (product.minQty != null)
                        unit['minQty'] = product.minQty
                    if (product.maxQty != null)
                        unit['maxQty'] = product.maxQty

                    // Initiate location for first step based on default values
                    def processStep = workflowService.getProcessStep(taskdef.pctg, taskdef.pkey, taskdef.tkey)
                    def vars = contentService.getStepVariables(taskdef.pctg, taskdef.pkey, taskdef.tkey, ['in'])
                    vars.each {
                        if (it.dataType == "Company") {
                            def comp = Company.get(it.defaultValue)
                            unit["company"] = comp?.name?.trim()
                            unit["companyId"] = comp?.id
                        }
                        if (it.dataType == "Location") {
                            def loc = Location.get(it.defaultValue)
                            unit["location"] = loc?.name?.trim()
                            unit["locationId"] = loc?.id
                        }
                    }

                    if (processStep?.operation) {
                        unit["operation"] = processStep.operation.name?.trim()
                        unit["operationId"] = processStep.operation.id

                        if (processStep?.operation.workCenter) {
                            unit["workCenter"] = processStep.operation.workCenter.name?.trim()
                            unit["workCenterId"] = processStep.operation.workCenter.id
                        }
                    }

                    if (processStep?.tag) {
                        unit["processStepTag"] = processStep?.tag
                    }
                    unit["isWorkInProgress"] = processStep?.isWorkInProgress

                    // Add data collection for this step

                    varsdc = contentService.getVariables(taskdef.pctg, taskdef.pkey, taskdef.tkey, ['dc'])
                    varsdc.each { var ->
                        def val = it[var.name]
                        if (!val?.equals(null) && val != null) {
                            if (var.dataType == 'int') {
                                if (val.toString().trim() != "")
                                    unit[var.name] = (long) val.toLong()
                            } else if (var.dataType == 'float') {
                                if (val.toString().trim() != "")
                                    unit[var.name] = val.toFloat()
                            } else if (var.dataType == 'date') {
                                val = val?.trim().replace("T", " ")
                                if (val.length() == 10)
                                    val = val + " 00:00:00"
                                unit[var.name] = df.parse(val?.trim()) ?: new Date()
                            } else if (var.dataType == 'scientific') {
                                unit[var.name] = val
                            } else {
                                if (val.getClass() == String)
                                    unit[var.name] = val?.trim()
                                else
                                    unit[var.name] = val
                            }
                        } else if (var.allowBlank == false && var.hidden == false) {
                            throw new RuntimeException("Please enter values for required (yellow) data fields.")
                        }
                    }

                    assignSecurity(unit, taskdef, recv.uid, null)
                    unit["movedBy"] = recv.uid
                    if (!unit['uom'])
                        unit['uom'] = product.uom?.trim() ?: ""

                    if (recv.note) {
                        def note = new Note(userName: recv.uid, comment: recv.note, stepName: taskdef.tname)
                        unit.addToNotes(note)
                        unit.nNotes = 1
                    }

                    if (bom) {
                        unit.bomId = bom.id
                        unit.bomRevision = bom.revision
                        unit.bomNote = bom.note
                        unit.bomOk = bom.toValidate
                    }

                    saveCalculateValues(unit, varsdc)
                    unit.save(failOnError: true)
                    retUnits.add(unit.dbo)
                }
            }
        }

        retUnits.each {
            historyService.initHistory("start", null, it, recordMove)
        }

        // Insert product if not already inserted
        if (recv.prodCode?.trim()) {
            def productCompany = ProductCompany.findByCompanyAndProduct(company, product)
            if (!productCompany) {
                productCompany = new ProductCompany(company: company, product: product, vendorCode: recv.prodCode)
            } else {
                productCompany.vendorCode = recv.prodCode
            }
            productCompany.save()
        }

        retCode
    }

    def update(def updated, def userName, def isMulti) {
        def db = mongo.getDB("glo")
        def queryId = new BasicDBObject("_id", updated.id)
        def unitLoc = db.unit.find(queryId, new BasicDBObject()).collect { it }[0]

        // record original version of the unit
        def oldUnit = unitLoc.clone()
        // get just data collection variables
        def variables
        def isSame = true

        if (!isMulti) {
            variables = contentService.getVariables(unitLoc['pctg'], unitLoc['pkey'], unitLoc['tkey'], ['dc', 'in', 'calc'])
        } else {
            variables = contentService.getVariables(updated.processCategory, updated.processKey, updated.taskKey, ['dc', 'in', 'calc'])
            if (!(updated.processKey == unitLoc['pkey'] && updated.taskKey == unitLoc['tkey']))
                isSame = false
        }

        def options = [:]
        options.put("isSame", isSame)
        options.put("processCategory", updated.processCategory ?: unitLoc['pctg'])
        options.put("processKey", updated.processKey ?: unitLoc['pkey'])
        options.put("taskKey", updated.taskKey ?: unitLoc['tkey'])

        // Check if we need to propagate update to children
        def processStep = workflowService.getProcessStep(updated.processCategory ?: updated.pctg, updated.processKey ?: updated.pkey, updated.taskKey ?: updated.tkey)
        if (processStep && processStep.moveChildren == true) {
            Thread.start {
                def units = db.unit.find(new BasicDBObject("parentUnit", updated.code ?: unitLoc.code)).collect {
                    updated.id = it._id
                    updated.pctg = it.pctg
                    updated.pkey = it.pkey
                    updated.tkey = it.tkey
                    update(updated, userName, isMulti)
                }
            }
        }

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd")
        if (updated["actualStart"] && updated["actualStart"].getClass() != java.util.Date) {
            isSame ? unitLoc["actualStart"] = df.parse(updated["actualStart"]) : options.put("actualStart", df.parse(updated["actualStart"]))
        }

        assignSecurity(unitLoc, null, userName, null)

        variables.each {

            if (!it.readOnly && it.dir != "calc") {
                def val = updated[it.name]
                if (val != null && !val?.equals(null)) {

                    if (val.toString().toUpperCase().trim() == "NN/AA") {
                        unitLoc.remove(it.name)
                    } else if (val.toString().toUpperCase().trim() == "N/A" || val.toString().toUpperCase().trim() == "null") {
                        unitLoc[it.name] = ""
                    } else {
                        if (it.dataType == 'int') {
                            if (val.toString().trim() != "")
                                unitLoc[it.name] = (long) val.toLong()
                        } else if (it.dataType == 'float') {
                            if (val.toString().trim() != "")
                                unitLoc[it.name] = val.toFloat()
                        } else if (it.dataType == 'date') {
                            if (val) {
                                unitLoc[it.name] = df.parse(val.toString())
                            }
                        } else if (it.dataType == 'scientific') {
                            if (val?.toString()?.trim() != "") {
                                unitLoc[it.name] = val
                            }
                        } else if (it.dataType == 'string') {
                            if (val?.toString()?.trim() != "") {
                                unitLoc[it.name] = val?.toString()?.trim()
                            }
                        } else if (it.dataType == "DieSpec") {

                            if (val?.toString().isNumber()) {
                                def dieSpec = DieSpec.get(val)
                                unitLoc[it.name] = dieSpec?.name + "(Rev. " + (dieSpec?.revision) + ")"
                                unitLoc[it.name + "Id"] = dieSpec?.id
                            }

                        } else {
                            if (it.dataType == 'objectArray')
                                unitLoc[it.name] = val
                            else if (it.dataType != 'objectArray')
                                unitLoc[it.name] = val
                        }
                    }
                }

                // Assign all active equipment part numbers to unit as array (AVOLOSCUSTOM)
                if (unitLoc["pctg"] == "nwLED" && it.dataType == "Equipment" && unitLoc["equipment"]) {
                    unitLoc["equipmentParts"] = []
                    def query = new BasicDBObject()
                    query.put("pctg", "RctHdw")
                    query.put("tkey", "parts_in_reactor")
                    query.put("equipment", unitLoc["equipment"])
                    query.put('runNumber', new BasicDBObject('$exists', 1))
                    def fields = new BasicDBObject('code', 1)
                    fields.put('position', 1)
                    fields.put('runNumber', 1)
                    fields.put('weight', 1)
                    fields.put('product', 1)
                    def temp = db.unit.find(query, fields)
                    def parts = temp.collect { it }
                    parts.each { part ->
                        if (unitLoc["runNumber"] >= part["runNumber"]) {
                            unitLoc["equipmentParts"].add([code: part.code, product: part.product, pos: part.position, weight: part["weight"], run: part["runNumber"]])
                        }
                    }
                }
            }
        }

        saveCalculateValues(unitLoc, variables)

        db.unit.save(unitLoc)

        historyService.initHistory("update", oldUnit, unitLoc, options)
        def limitSpecs = []
        def specs = ""
        def isOk = validate(unitLoc, variables, null, limitSpecs)
        if (isOk in ["3", "4"]) {
            specs = limitSpecs[0]
        }

        isMulti ? [] : [data: [id: unitLoc._id, ok: isOk, specs: specs] + unitLoc]
    }

    def updates(def updated, def userName) {
        Unit.withTransaction { status ->
            updated.updates.each { parm ->
                if (parm) {
                    def upd = updated
                    upd.id = parm.toLong()
                    update(upd, userName, true)
                }
            }
        }
    }


    def move(String user, def moved) {

        def retUnits = []
        def bulkUnits = []
        def ret = []

        def db = mongo.getDB("glo")

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd")
        def vars = contentService.getStepVariables(moved.processCategoryEng, moved.processKeyEng, moved.taskKeyEng, ['din'])

        if (moved.taskKeyEng in ["parts_in_reactor", "epi_growth"]) {
            checkReactorMove(moved.processCategoryEng, moved.processKeyEng, moved.taskKeyEng, moved.equipment, moved.units)
        }

        if (moved.printLabel && moved.printLabel == "yes") {
            ret = [showMove: "2", pctg: moved.processCategoryEng, pkey: moved.processKeyEng, tkey: moved.taskKeyEng]
        }

        Unit.withTransaction { status ->

            moved.units.each { parm ->

                // First get unit
                def unit = Unit.get(parm.id)

                // Check if current process step allows doing regular move
                def processStep = workflowService.getProcessStep(unit.pctg, unit.pkey, unit.tkey)
                def processStepTo = workflowService.getProcessStep(moved.processCategoryEng, moved.processKeyEng, moved.taskKeyEng)
                if (moved.overrideMove != true && processStep.preventRegularMove == true) {
                    throw new RuntimeException("These units are contained within parent unit and can only move with the parent.")
                }

                // Check if moving from current process step needs to move child items
                if (processStep && processStep.moveChildren == true) {
                    def childrenMoves = validateChildren(db, unit, moved, moved.isEngineering)

                    if (processStepTo && processStepTo.moveChildren != true && childrenMoves.units.size() > 0) {
                        throw new RuntimeException("Can not move unit to " + moved.taskKeyEng + " because it has children assigned.")
                    }

                    if (childrenMoves.units.size() > 0) {
                        Thread.start {
                            move(user, childrenMoves)
                        }
                    }
                }

                if (moved.taskKeyEng == "test_data_visualization" && moved.processKeyEng == "test") {
                    createCoupons(db, user, unit, "DVD", "dvd_assembly", "xy_inspection_on_wafer", "XY inspection on wafer", "CPN1000")
                }

                if (moved.taskKeyEng == "pl_visual_inspection" && moved.processKeyEng == "epifab") {
                    createCoupons(db, user, unit, "C", "fabassembly", "pl_visual_inspection", "PL visual inspection", "100C")
                }

                if (moved.taskKeyEng == "dicing_inventory" && moved.processKeyEng == "wafer_submount") {
                    // Split wafer to 2 pieces
                    ['2', '5'].each {
                        def cpnCode = unit.code + "_" + it
                        def subUnit = db.unit.find(new BasicDBObject("code", cpnCode), new BasicDBObject()).collect {
                            it
                        }[0]
                        if (!subUnit) {
                            createCoupon(db, unit, cpnCode, user, "DVD", "wafer_submount_diced", "dicing_inventory", "Dicing inventory", "SO1000")
                        }
                    }
                }

                // End parent unit wheen moving child to dicing
                if (moved.taskKeyEng == "dicing" && moved.processKeyEng == "wafer_submount_diced") {
                    def cpnCode = unit.code.tokenize("_")[0]
                    def dbo = new BasicDBObject("code", cpnCode)
                    dbo.put("tkey", "dicing_inventory")
                    def uu = db.unit.find(dbo, new BasicDBObject()).collect {
                        it
                    }[0]
                    if (uu) {
                        def buf = new Expando()
                        buf.isEngineering = true
                        buf.prior = 50
                        buf.processCategoryEng = "nwLED"
                        buf.processKeyEng = "wafer_submount"
                        buf.taskKeyEng = "end"
                        buf.units = []
                        def n = [:]
                        n.put('transition', 'engineering')
                        n.put('id', uu["_id"])
                        buf.units.add(n)
                        move("admin", buf)
                    }
                }

                def oldUnit = deepClone(unit)
                def isContinue = false

                if (unit.isBulk == "true") {

                    //  Check existence of new unit
                    def newUnit = Unit.withCriteria {
                        eq('pctg', moved.processCategoryEng)
                        eq('pkey', moved.processKeyEng)
                        eq('tkey', moved.taskKeyEng)
                        eq('productCode', unit.productCode)
                        eq('supplierId', unit.supplierId)
                    }[0]

                    if (newUnit) {

                        def oldNewUnit = deepClone(newUnit)
                        newUnit.qtyIn = newUnit.qtyIn + moved.qtyIn.toLong()
                        newUnit.qtyOut = newUnit.qtyOut + moved.qtyIn.toLong()

                        if (moved.note) {
                            newUnit.addToNotes(new Note(stepName: newUnit.tname, comment: moved.note, userName: user))
                            newUnit.nNotes = newUnit.notes?.size()
                        }

                        newUnit.save()

                        def options = [:]
                        options.put("processCategory", newUnit['pctg'])
                        options.put("processKey", newUnit['pkey'])
                        options.put("taskKey", newUnit['tkey'])

                        historyService.initHistory("update", oldNewUnit, newUnit.dbo, options)
                    }

                    if (moved.qtyIn && moved.qtyIn.toLong() < unit.qtyOut) {

                        // Update quantity to remaining
                        unit.qtyOut = unit.qtyOut - moved.qtyIn.toLong()

                        if (!newUnit) {

                            // Create new unit
                            def recv = new Expando()

                            recv.uid = user
                            recv.cid = unit.supplierId
                            recv.pid = Product.findByCode(unit.productCode).id
                            recv.units = []
                            recv.override = "true"
                            def m = [:]
                            m.put('code', "")
                            m.put('qtyOut', moved.qtyIn.toLong())
                            recv.units.add(m)
                            def newCode = start(recv, unit.pctg, unit.pkey, false)
                            def unitCreated = Unit.findByCode(newCode)

                            if (moved.processCategoryEng + moved.processKeyEng + moved.taskKeyEng != unitCreated.pctg + unitCreated.pkey + unitCreated.tkey) {

                                def buf = new Expando()
                                buf.isEngineering = true
                                buf.prior = 50
                                buf.processCategoryEng = moved.processCategoryEng
                                buf.processKeyEng = moved.processKeyEng
                                buf.taskKeyEng = moved.taskKeyEng
                                buf.note = moved.note
                                buf.qtyIn = moved.qtyIn.toLong()
                                buf.units = []
                                def n = [:]
                                n.put('transition', 'engineering')
                                n.put('id', unitCreated.id)
                                buf.units.add(n)
                                move(user, buf)
                            }
                        } else {

                            def moves = db.moves.find([code: newUnit.code, pctg: newUnit.pctg, pkey: newUnit.pkey, tkey: newUnit.tkey]).collect {
                                it
                            }[0]
                            if (moves) {
                                moves.start = moved.start ? df.parse(moved.start) : new Date()
                                moves.actualStart = moved.actualStart ? df.parse(moved.actualStart) : new Date()
                                moves["qtyIn"] = moved.qtyIn.toLong()
                                moves.remove("_id")
                                db.moves.insert(moves)
                            }
                        }
                        isContinue = true

                    } else if (moved.qtyIn && moved.qtyIn.toLong() == unit.qtyOut) {

                        if (newUnit) {

                            db.unit.remove([code: unit.code])
                            db.history.remove([code: unit.code])
                            db.dataReport.remove([code: unit.code])
                            isContinue = false
                        } else {

                            unit.qtyIn = unit.qtyOut
                            unit.pctg = moved.processCategoryEng
                            unit.pkey = moved.processKeyEng
                            unit.tkey = moved.taskKeyEng
                            isContinue = true
                        }
                    }

                } else {

                    unit.qtyIn = unit.qtyOut
                    unit.pctg = moved.processCategoryEng
                    unit.pkey = moved.processKeyEng
                    unit.tkey = moved.taskKeyEng
                    isContinue = true
                }

                if (isContinue == true) {

                    unit.tname = workflowService.getTaskName(moved.processKeyEng, unit.tkey)

                    unit.start = moved.start ? df.parse(moved.start) : new Date()
                    unit.actualStart = moved.actualStart ? df.parse(moved.actualStart) : new Date()

                    if (moved.note) {
                        unit.addToNotes(new Note(stepName: unit.tname, comment: moved.note, userName: user))
                        unit.nNotes = unit.notes?.size()
                    }

                    if (moved.prior) {
                        unit.prior = moved.prior.toInteger()
                    }

                    // Assign resources, locations and operations
                    contentService.removeResources(unit)
                    contentService.assignResources(unit.pctg, unit.pkey, unit.tkey, moved, unit)

                    def afterProcKey = ""
                    def afterProcCategory = unit.pctg
                    vars.each {
                        if (it.dataType == "date") {
                            unit[it.name] = df.parse(moved[it.name])
                        } else if (it.dataType == "Process") {
                            def proc2 = Process.findByPkey(moved[it.name])
                            afterProcKey = proc2.pkey
                            afterProcCategory = proc2.category
                            unit[it.name] = proc2.pkey
                            db.unit.update(new BasicDBObject('code', unit.code), new BasicDBObject('$set', new BasicDBObject(it.name, afterProcKey)), false, false)
                        } else {
                            unit[it.name] = moved[it.name]
                        }
                    }

                    if (unit.tkey.toUpperCase() == "END") {
                        def proc = Process.findByPkey(unit.pkey)
                        if (proc.pkeyAfter && proc.pctgAfter) {
                            afterProcKey = proc.pkeyAfter
                            afterProcCategory = proc.pctgAfter
                        }
                        if (afterProcKey && afterProcCategory) {
                            def taskdef = workflowService.findFirstTaskDefinition(afterProcCategory, afterProcKey.toLowerCase())
                            if (!taskdef) {
                                throw new RuntimeException("Following process " + afterProcKey + " is not defined.")
                            }

                            ret = [showMove: "1", pctg: taskdef.pctg, pkey: taskdef.pkey, tkey: taskdef.tkey]
                            return
                        }
                    } else {
                        assignSecurity(unit, workflowService.getTaskDef(unit.pkey, unit.tkey), user, moved.user)
                    }
                    unit["movedBy"] = user
                    unit["equipmentParts"] = null

                    saveCalculateValues(unit, vars)

                    unit.save()

                    retUnits.add([oldUnit, unit])
                }
            }
        }

        retUnits.each {

            historyService.initHistory("move", it[0], it[1].dbo, null)

            // Some custom code for handling moves
            if (it[0].pctg == "PM" && it[0].tkey in ["scheduled_time", "scheduled_runs", "scheduled_weights", "scheduled_depositions", "scheduled"] && !moved.pmMove) {

                completeMaintenanceTask(it[0].equipmentMaintenanceId, new Date(), it[1].files, it[1].notes, it[1].dbo["department"], it[1].dbo["tag"])
            }
        }

        return ret
    }


    def validateChildren(db, unit, moved, isEng) {

        def buf = new Expando()
        buf.units = []
        def vars = []
        def specVars = []
        def isVars = false
        def units = db.unit.find(new BasicDBObject("parentUnit", unit.code)).collect{
            if (isVars == false) {
                vars = contentService.getVariables(it.pctg, it.pkey, it.tkey, ['dc', 'din'])
                specVars = contentService.getVariables(it.pctg, it.pkey, it.tkey, ['spec'])
                moved.properties.each {k, v ->
                    if (k != 'units') {
                        buf[k] = v;
                    }
                }
                buf.isEngineering = true
                buf.prior = 50
                buf.processCategoryEng = it.pctg
                buf.processKeyEng = it.pkey
                buf.taskKeyEng = moved.taskKeyEng
                buf.overrideMove = true
                isVars = true
            }
            def isOk = validate(it, vars, specVars, [])
            if (isOk != "0" && isEng == "false") {
                throw new RuntimeException("Validation of children units failed. Click down arrow on the unit to jump to children.")
            } else {
                def n = [:]
                n.put('transition', 'engineering')
                n.put('id', it["_id"])
                buf.units.add(n)
            }
        }
        buf
    }

    def createCoupons(def db, def user, def unit, def pctg, def pkey, def tkey, def tname, def productCode) {
        // Split wafer to coupons according to mask definition and start coupon in new process flow
        if (!unit.mask) {
            throw new RuntimeException("Mask for this wafer is not defined.")
        }

        def productMask = ProductMask.findByName(unit.mask)
        def productMaskItems = ProductMaskItem.executeQuery("""
                        select distinct ps.cpn from ProductMaskItem as ps where ps.productMask.id = ? and ps.isActive = 1
                    """, [productMask.id])
        if (!productMaskItems) {
            throw new RuntimeException("This mask has no valid definition.")
        }
        // Create coupon unit for each subunit
        productMaskItems.each {
            if (it != null && it.trim() != '') {
                def cpnCode = unit.code + "_" + it.trim()
                def subUnit = db.unit.find(new BasicDBObject("code", cpnCode), new BasicDBObject()).collect {
                    it
                }[0]
                if (!subUnit) {
                    createCoupon(db, unit, cpnCode, user, pctg, pkey, tkey, tname, productCode)
                }
            }
        }
    }

    def createCoupon(
            def db, def unit, def subCode, def user, def pctg, def pkey, def tkey, def tname, def productCode) {

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        def newUnit = new Unit()
        def dbo = unit.dbo;
        if (!unit.dbo) {
            dbo = unit;
        }
        dbo.each { k, v ->
            if ((v.getClass() != com.mongodb.BasicDBObject && k != "_id") ||
                    (v.getClass() == com.mongodb.BasicDBObject && v.get("setting")?.get("propagate") == "true")) {
                try {
                    newUnit[k] = v
                } catch (Exception exc) {

                }
            }
        }
        newUnit.experimentId = unit.experimentId
        newUnit.code = subCode
        newUnit.genPath = unit.code + "," + subCode
        def product = Product.findByCode(productCode)
        newUnit.productCode = product.code
        newUnit.product = product.name
        newUnit.productRevision = product.revision
        newUnit.parentUnit = [unit.code]

        newUnit.pctg = pctg
        newUnit.pkey = pkey
        newUnit.tkey = tkey
        newUnit.tname = tname
        newUnit.actualStart = new Date()

        unit.notes?.each { n ->
            def note = new Note(userName: user, comment: n.comment, stepName: newUnit.tname)
            newUnit.addToNotes(note)
        }
        unit.files?.each { f ->
            def file = new File(userName: user, name: f.name, fileName: f.fileName, fileId: f.fileId)
            newUnit.addToFiles(file)
        }
        newUnit.save(failOnError: true)
        newUnit.indexing()
        def unit3 = Unit.get(newUnit.id)

        historyService.initHistory("startDerived", dbo, unit3.dbo, ["parentDelete", false])
    }


    def autoRoutes(String user, def moved) {

        def script = null
        def result = [:]
        def shell = new GroovyShell(this.class.classLoader)

        // If routing script is populated and moved.taskKeyEnd == null
        def processStep = workflowService.getProcessStep(moved.processCategoryEng, moved.processKeyEng, moved.taskKeySource)
        if (processStep?.routingScript?.trim()) {
            try {
                script = shell.parse(processStep?.routingScript?.trim())
            } catch (Exception exc) {
                throw new RuntimeException("Routing script for process step '" + moved.taskKeySource + "' has compilation errors: " + exc.getMessage())
            }
        } else {
            return result
        }

        moved.units.each { parm ->

            def unit = Unit.get(parm.id).dbo
            def transition = getCalculatedValue(unit, unit.code, script, shell)

            def transArray = transition.tokenize("|")
            if (transArray.size() == 2) {
                if (!result[transition]) {
                    result.put(transition, [])
                }
                result[transition].add([transition: transition, data: [id: parm.id, ok: true]])
            } else {
                def tkey = workflowService.getTaskForTransition(unit.pkey, unit.tkey, transition)
                if (tkey) {
                    if (!result[tkey]) {
                        result.put(tkey, [])
                    }
                    def isOk = validate(unit, null, null, null)
                    result[tkey].add([transition: transition, data: [id: parm.id, ok: isOk]])
                }
            }
        }
        result
    }


    def checkReactorPart(def part) {
        def msg = ""

        def product = Product.findByCode(part.productCode)

        switch (product.category) {
            case "First Segment":
                if (part.position == null || !(part.position in ['1']))
                    msg = "<br> - Position 1 is required for first segment"
                break

            case "Satellite":
                if (part.position == null || !(part.position in ['1', '2', '3', '4', '5', '6', '7', '8']))
                    msg = "<br> - Position 1 thru 8 is required for satellite parts"
                break
        }
        return (msg)
    }


    def checkReactorMove(def pctg, def pkey, def tkey, def equipmentId, def buf) {

        // Removing "Check" from barcodeScanning eliminates checkReactorMove restrictions
        def ps = workflowService.getProcessStep(pctg, pkey, tkey)
        def scanTypes = (ps?.barcodeScanning && ps?.barcodeScanning != "") ? ps?.barcodeScanning.tokenize(',') : []
        if (!("Check" in scanTypes)) return

        def msg = ""
        def parts

        def db = mongo.getDB("glo")

        //get existing units in reactor
        def query = new BasicDBObject()
        query.put('pctg', pctg)
        query.put('tkey', tkey)
        query.put('equipmentId', equipmentId)
        def units = db.unit.find(query, new BasicDBObject()).collect { it }

        //get new units being moved to reactor
        def unitIds = buf.collect { it['id'] }
        def query1 = new BasicDBObject()
        query1.put('pctg', pctg)
        query1.put('_id', new BasicDBObject(['$in': unitIds]))
        def newUnits = db.unit.find(query1, new BasicDBObject()).collect { it }

        units.addAll(newUnits)

        if (pctg == 'RctHdw') {
            parts = units.clone()
        } else {
            if (units.size() > 8) msg += "<br> - Cannot have more than 8 wafers in reactor"

            def query2 = new BasicDBObject()
            query2.put('pctg', 'RctHdw')
            query2.put('tkey', 'parts_in_reactor')
            query2.put('equipmentId', equipmentId)
            parts = db.unit.find(query2, new BasicDBObject()).collect { it }
        }

        def items = []
        parts.each {
            msg += checkReactorPart(it)

            def item = [:]
            def product = Product.findByCode(it.productCode)
            item.category = product.category
            item.code = it.code
            item.position = it.position ?: "."
            items.add(item)
        }

        def cnt = items.findAll { obj -> obj.category == "Tension Disc" }.size()
        if ((pctg == 'RctHdw' && cnt > 1) || (pctg != 'RctHdw' && cnt != 1)) msg += "<br> - " + cnt + " tension discs"

        cnt = items.findAll { obj -> obj.category == "Susceptor" }.size()
        if ((pctg == 'RctHdw' && cnt > 1) || (pctg != 'RctHdw' && cnt != 1)) msg += "<br> - " + cnt + " susceptors"

        cnt = items.findAll { obj -> obj.category == "Ceiling" }.size()
        if ((pctg == 'RctHdw' && cnt > 1) || (pctg != 'RctHdw' && cnt != 1)) msg += "<br> - " + cnt + " ceilings"

        cnt = items.findAll { obj -> obj.category == "Cover Star" }.size()
        if ((pctg == 'RctHdw' && cnt > 1) || (pctg != 'RctHdw' && cnt != 1)) msg += "<br> - " + cnt + " cover stars"

        cnt = items.findAll { obj -> obj.category == "First Segment" }.size()
        if ((pctg == 'RctHdw' && cnt > 1) || (pctg != 'RctHdw' && cnt != 1)) msg += "<br> - " + cnt + " first segments"

        cnt = items.findAll { obj -> obj.category == "Segment" }.size()
        if ((pctg == 'RctHdw' && cnt > 7) || (pctg != 'RctHdw' && cnt != 7)) msg += "<br> - " + cnt + " segments"

        def subs = items.findAll { obj -> obj.category == "Satellite" }.collect { it.position }
        cnt = subs.size()
        if ((pctg == 'RctHdw' && cnt > 8) || (pctg != 'RctHdw' && cnt != 8)) msg += "<br> - " + cnt + " satellites"

        if (cnt != subs.unique().size()) msg += "<br> - Satellites must have unique positions"

        if (msg != "")
            throw new RuntimeException("<br><br>Cannot move to reactor because: " + msg)
    }


    def checkGroupMove(def pctg, def pkey, def tkey, def groupLabel, def units, def mode) {
        def groups = [:]
        def totalCnt = 0

        // count units in each group (as per groupLabel)
        units.each { parm ->
            def unit = Unit.get(parm.id)
            def groupId = unit[groupLabel] ? unit[groupLabel].trim() : ""
            if (groupId != "") {
                totalCnt++
                if (groups[groupId])
                    groups[groupId].cnt++
                else
                    groups[groupId] = ['cnt': 1]
            }
        }

        if (mode == "count") return (totalCnt)

        def db = mongo.getDB("glo")

        // check each group, throw exception if partial move
        groups.each { k, v ->
            if (k != "") {
                def query = new BasicDBObject()
                query.put("pctg", pctg)
                query.put("pkey", pkey)
                query.put(groupLabel, k)
                def project = new BasicDBObject()
                project.put("code", 1)
                def cnt = db.unit.find(query, project).size()
                if (cnt != v.cnt) {
                    def msg = "<br><br>Cannot move partial Cassette"
                    msg += "<br>" + k + ": " + v.cnt + " of " + cnt + " units selected"
                    throw new RuntimeException(msg)
                }
            }
        }
    }

    def loss(String user, def lost) {
        def db = mongo.getDB("glo")
        def retUnits = []
        Unit.withTransaction { status ->
            lost.units.each { parm ->
                // First get unit
                def unit = Unit.get(parm.id)
                // record original version of the unit
                def oldUnit = deepClone(unit)
                // Check if loss qty bigger then unit qtyIn
                int qty = lost.qty.toInteger()
                if (unit.qtyIn < qty) {
                    throw new RuntimeException("Loss qty can not be larger than unit qty.")
                }
                int out = unit.qtyOut
                unit.qtyOut = out - qty
                unit["yieldLossId"] = lost.lossReason
                unit["yieldLoss"] = YieldLossReason.get(lost.lossReason).name
                if (unit.qtyOut <= 0) {
                    unit.tkey = "end"
                    unit.tname = "End"
                }

                if (lost.note) {
                    unit.addToNotes(new Note(stepName: oldUnit.tname, comment: "LOSS: " + lost.note, userName: user))
                }
                unit.nNotes = unit.notes?.size()

                unit.save()

                assignSecurity(unit, null, user, null)
                retUnits.add([oldUnit, unit.dbo])
            }
        }

        retUnits.each {
            historyService.initHistory("loss", it[0], it[1], it[1].tkey == "end" ? "true" : "")

            // Process loss on all children
            def processStep = workflowService.getProcessStep(it[0].pctg, it[0].pkey, it[0].tkey)
            if (processStep && processStep.moveChildren == true) {
                def lossChidren =  db.unit.find(new BasicDBObject("parentUnit", it[0].code)).collect{
                    return ['id': it._id]
                }
                if (lossChidren.size() > 0) {
                    Thread.start {
                        lost.units = lossChidren
                        loss(user, lost)
                    }
                }
            }
        }
    }

    def bonus(String user, def bonus) {

        def retUnits = []
        Unit.withTransaction { status ->
            bonus.units.each { parm ->
                // First get unit
                def unit = Unit.get(parm.id)
                // record original version of the unit
                def oldUnit = deepClone(unit)

                unit.qtyOut = unit.qtyOut + new Integer(bonus.qty)
                unit["bonusId"] = bonus.bonusReason
                unit["bonus"] = BonusReason.get(bonus.bonusReason).name

                if (bonus.note) {
                    unit.addToNotes(new Note(stepName: unit.tname, comment: "BONUS: " + bonus.note, userName: user))
                    unit.nNotes = unit.notes?.size()
                }

                unit.save()

                assignSecurity(unit, null, user, null)

                retUnits.add([oldUnit, unit.dbo])
            }
        }
        retUnits.each {
            historyService.initHistory("bonus", it[0], it[1], null)
        }
    }

    def rework(String user, def rework) {

        def retUnits = []
        def options = [:]

        Unit.withTransaction { status ->
            rework.units.each { parm ->
                // First get unit
                def unit = Unit.get(parm.id)
                // record original version of the unit
                def oldUnit = deepClone(unit)

                def reworkName = ReworkReason.get(rework.reworkReason).name
                unit["reworkId"] = rework.reworkReason
                unit["rework"] = reworkName
                if (unit["reworks"] == null)
                    unit["reworks"] = 1
                else
                    unit["reworks"] += 1

                if (!unit["reworksTotal"])
                    unit["reworksTotal"] = 0
                unit["reworksTotal"] += 1

                options.put("rework", reworkName)
                options.put("reworkId", rework.reworkReason)

                // Remove all DC data
                def vars = contentService.getStepVariables(unit.pctg, unit.pkey, unit.tkey, ['dc'])
                vars.each {
                    if (it.name != "reworksTotal" && it.editor == true) {
                        unit.dbo.remove(it.name)
                        unit[it.name] = null
                    }
                }
                unit.start = new Date()
                unit.actualStart = new Date()

                if (reworkName) {
                    unit.addToNotes(new Note(stepName: unit.tname, comment: "REWORK: [" + reworkName + "] " + rework.note, userName: user))
                    unit.nNotes = unit.notes?.size()
                }

                unit.save()

                assignSecurity(unit, null, user, null)

                retUnits.add([oldUnit, unit.dbo])
            }
        }
        retUnits.each {
            historyService.initHistory("rework", it[0], it[1], options)
        }
    }

    def split(String user, def units, boolean parentDelete, def listOfSubUnits) {

        def retUnits = []
        def db = mongo.getDB("glo")

        units.each { parm ->
            // First get unit
            def unit = db.unit.find(new BasicDBObject("_id", parm.id), new BasicDBObject()).collect { it }[0]

            // If it is splitting then check products to split to
            def processStep = workflowService.getProcessStep(unit.pctg, unit.pkey, unit.tkey)
            if (processStep.splitProductCodes) {

                processStep.splitProductCodes.tokenize(",").each {

                    def derivedProduct = Product.findByCode(it?.trim())
                    if (!derivedProduct)
                        throw new RuntimeException("Splitted product is not properly defined [" + it?.trim() + "]. Go to process step configuration and fix the problem.")
                    if (!derivedProduct.startProcess)
                        throw new RuntimeException("Splitted product " + derivedProduct.name + " is not assigned to the process workflow.")

                    def recv = new Expando()
                    recv.units = []
                    def m = [:]
                    m.put('code', unit.code + derivedProduct.code)
                    m.put('qty', 1)
                    recv.units.add(m)
                    recv.uid = user
                    recv.cid = unit.supplierId
                    recv.pid = derivedProduct.id
                    start(recv, derivedProduct.startProcess.category, derivedProduct.startProcess.pkey)

                }
                return []

            } else {

                if (!unit["mask"]) {
                    throw new RuntimeException("Mask for split is not defined for the unit " + unit.code)
                }

                def product = Product.findByCode(unit.productCode)
                def productMask = ProductMask.findByProductAndName(product, unit["mask"])
                if (!productMask) {
                    throw new RuntimeException("Product mask is not defined for product " + unit.productCode + " and mask " + unit["mask"])
                }

                if (!productMask.productMaskItems) {
                    throw new RuntimeException("Product mask has no defined mask array for product " + unit.productCode + " and mask " + unit["mask"])
                }

                def subUnit = unit.clone()
                def atLeastOneActive = false

                productMask.productMaskItems.each {
                    if (it.isActive && (!listOfSubUnits || listOfSubUnits.contains(unit.code + "_" + it.code))) {
                        atLeastOneActive = true

                        def unit2 = Unit.findByCode(subUnit.code + "_" + it.code)
                        if (!unit2) {

                            def newUnit = new Unit()

                            subUnit.each { k, v ->
                                if ((v.getClass() != com.mongodb.BasicDBObject && k != "_id") ||
                                        (v.getClass() == com.mongodb.BasicDBObject && v.get("setting")?.get("propagate") == "true")) {
                                    newUnit.properties.put(k, v)
                                }
                            }

                            newUnit.code = unit.code + "_" + it.code
                            newUnit.genPath = unit.code + "," + unit.code + "_" + it.code
                            newUnit.productCode = it.derivedProduct.code
                            newUnit.product = it.derivedProduct.name
                            newUnit.productRevision = it.derivedProduct.revision

                            unit.notes?.each { n ->
                                def note = new Note(userName: user, comment: n.comment, stepName: newUnit.tname)
                                newUnit.addToNotes(note)
                            }
                            unit.files?.each { f ->
                                def file = new File(userName: user, name: f.name, fileName: f.fileName, fileId: f.fileId)
                                newUnit.addToFiles(file)
                            }

                            newUnit.save(failOnError: true)

                            newUnit.indexing()

                            def unit3 = Unit.get(newUnit.id)

                            subUnit.each { k, v ->
                                if ((v.getClass() != com.mongodb.BasicDBObject && k != "_id") ||
                                        (v.getClass() == com.mongodb.BasicDBObject && v.get("setting")?.get("propagate") == "true")) {
                                    unit3[k] = v
                                }
                            }

                            unit3.code = unit.code + "_" + it.code
                            unit3.genPath = unit.code + "," + unit.code + "_" + it.code
                            unit3.productCode = it.derivedProduct.code
                            unit3.product = it.derivedProduct.name
                            unit3.productRevision = it.derivedProduct.revision

                            unit3["plX"] = it.plX
                            unit3["plY"] = it.plY
                            unit3["sizeX"] = it.sizeX
                            unit3["sizeY"] = it.sizeY
                            if (!parentDelete) {
                                unit3.parentCode = subUnit.code
                            }

                            unit3.save(failOnError: true)

                            retUnits.add([subUnit, unit3.dbo])
                        }
                    }
                }

                if (!atLeastOneActive) {
                    throw new RuntimeException("Product mask has no active array items for product " + unit.productCode + " and mask " + unit["mask"])
                }

                if (parentDelete == true) {
                    historyService.initHistory("end", subUnit, null, null)
                }
            }
        }

        def ret = []
        retUnits.each {
            ret.add(it[1])
            historyService.initHistory("startDerived", it[0], it[1], ["parentDelete", parentDelete])
        }

        ret
    }


    def merge(String user, String code, def units) {

        def db = mongo.getDB("glo")
        def unit = db.unit.find(new BasicDBObject("code", code), new BasicDBObject()).collect { it }[0]
        def bom = Bom.get(unit?.bomId)
        if (!bom) {
            throw new RuntimeException("Assembly not defined: " + bomId)
        }
        def product = bom.assemblyProduct
        if (!product) {
            throw new RuntimeException("Product not defined for assembly: " + bomId)
        }

        def company = product?.productCompanies?.iterator()?.next()?.company
        if (!company) {
            throw new RuntimeException("Company not defined for product for assembly: " + bomId)
        }

        def unitList = units.collect { unitcode ->

            def unitLoc = db.unit.find(new BasicDBObject("code", unitcode), new BasicDBObject()).collect { it }[0]
            if (!unitLoc) {
                throw new RuntimeException("Unit not defined: " + unitcode)
            }
            unitLoc
        }

        //Assign parent code
        unitList.each {

            it.parentCode = code
            it.genPath = it.genPath + "," + code
            db.unit.save(it)

            db.history.update(['code': it.code], ['$set': ['parentCode': code]])
            db.dataReport.update(['code': it.code], ['$set': ['parentCode': code]])

            def wd = it.code.tokenize("_")
            def wid = new BasicDBObject("WaferID", wd[0])
            if (wd.size() == 2) {
                wid.put("DeviceID", wd[1])
            }
            db.measures.update(wid, ['$set': ['packageCode': code]])
        }
    }

    def unMerge(String user, def code) {

        def db = mongo.getDB("glo")
        def query = new BasicDBObject("code", code)
        def unit = db.unit.find(query, new BasicDBObject()).collect { it }[0]
        if (!unit) {
            throw new RuntimeException("Unit not defined: " + parm.id)
        }

        db.history.update(query, ['$set': ['parentCode': null]], false, true)
        db.dataReport.update(query, ['$set': ['parentCode': null]], false, true)
        db.unit.update(query, ['$set': ['parentCode': null]], false, true)


        def wd = code.tokenize("_")
        def wid = new BasicDBObject("WaferID", wd[0])
        if (wd.size() == 2) {
            wid.put("DeviceID", wd[1])
        }
        db.measures.update(wid, ['$set': ['packageCode': '']])
    }

    def saveFileMeta(String user, def units, def fileId, def name, def fileName) {

        def retUnits = []
        Unit.withTransaction() {
            units.each { parm ->
                def file = File.findByFileName(fileName)
                if (!file) {
                    file = new File(userName: user, name: name, fileName: fileName, fileId: fileId.toString())
                }
                def unit = Unit.get(parm)
                unit.addToFiles(file)
                unit.nFiles = unit.files.size()
                unit.save(failOnError: true)
                retUnits.add(unit.dbo)
            }
        }
        retUnits.each {
            historyService.initHistory("fileUpload", null, it, null)
        }
    }

    private def saveCalculateValues(def unit, def variables) {

        if (variables) {

            Binding binding = new Binding();
            binding.setVariable("db", mongo.getDB("glo"));
            def shell = new GroovyShell(binding)

            if (unit.getClass() == BasicDBObject) {
                unit.each { k, v ->
                    binding.setVariable(k.replace(" ", "_"), v);
                }
            } else {
                unit.properties.each { k, v ->
                    binding.setVariable(k.replace(" ", "_"), v);
                }
            }

            for (def calc in variables) {

                if (calc.dir == "calc") {
                    def script = ""
                    try {
                        if (calc.dir == "calc" && calc.evalScript != null && calc.evalScript?.trim() != "") {
                            script = calc.evalScript?.trim()
                            if (script.toUpperCase().indexOf("AUTOINCREMENT") == -1) {
                                def result = shell.evaluate(script)

                                if (calc.dataType == "int")
                                    unit[calc.name] = (int) result
                                else if (calc.dataType == "float")
                                    unit[calc.name] = (float) result
                                else
                                    unit[calc.name] = result
                            }
                        }
                    } catch (org.codehaus.groovy.control.CompilationFailedException exc) {
                        logr.warn(calc.name + " '" + script + "': " + exc.toString())
                    } catch (Exception exc) {
                        logr.warn(calc.name + " '" + script + "': " + exc.toString())
                    } finally {
                    }

                }

                // If searchPrefix defined append field to tagsCustom so it can be found
                if (calc.includeInSearch) {
                    def pattern = ~/[A-Za-z0-9\-]+/
                    if (!(unit[calc.name] == null || unit[calc.name]?.trim() == "")) {
                        if (calc.searchPrefix && (unit[calc.name]?.indexOf(calc.searchPrefix) != 0 || !pattern.matcher(unit[calc.name]).matches())) {
                            unit[calc.name] = ""
                            throw new RuntimeException("Incorrect '" + (calc.title ?: calc.name) + "' value. Value must start with '" + calc.searchPrefix + "' and should contain only letters and numbers.")
                        } else /* if (calc.searchPrefix) */ {
                            if (unit["tagsCustom"] == null) unit["tagsCustom"] = new ArrayList<String>()
                            def ts = unit[calc.name]?.toUpperCase()
                            if (ts && !unit["tagsCustom"].contains(ts))
                                unit["tagsCustom"].add(ts)
                        }
                    }
                }
            }
        }
    }


    def getCalculatedValue(def rowObj, def title, def script, def pShell) {

        def calcRes = ""

        if (rowObj && script) {

            if (!pShell.getVariable("db"))
                pShell.setVariable("db", mongo.getDB("glo"));

            pShell.setVariable("unitobj", rowObj);

            rowObj.each { prop, value ->

                pShell.setVariable(prop.replace(" ", "_"), value);

            }
            try {
                calcRes = script.run()
            } catch (org.codehaus.groovy.control.CompilationFailedException exc) {
                throw new RuntimeException(exc.toString())
            } catch (Exception exc) {
                throw new RuntimeException(exc.toString())
            } finally {
            }
        }
        calcRes
    }


    private def getCalculatedValues(def unit, def calcVars, def outVars) {


        def calcVarsLocal = []
        calcVars.each { calcVar ->

            if (unit[calcVar.name] == null || calcVar.name == "use_count") {
                calcVarsLocal.add(calcVar)
            }
        }

        if (calcVarsLocal) {

            Binding binding = new Binding();
            binding.setVariable("db", mongo.getDB("glo"));
            binding.setVariable("unitobj", unit);
            def shell = new GroovyShell(this.class.classLoader, binding)

            for (def var in outVars) {
                binding.setVariable(var.name.replace(" ", "_"), unit[var.name]);
            }
            unit.each { k, v ->
                binding.setVariable(k.replace(" ", "_"), v);
            }
            for (def calc in calcVarsLocal) {
                def script = ""
                try {
                    if (calc.evalScript != null && calc.evalScript?.trim() != "") {
                        script = calc.evalScript?.trim()
                        if (script.toUpperCase().indexOf("AUTOINCREMENT") == -1) {
                            def calcRes = shell.evaluate(script)

                            if (calc.dataType == "int")
                                calcRes = calcRes.toInteger()
                            if (calc.dataType == "float")
                                calcRes = calcRes.toFloat()

                            unit.put(calc.name, calcRes)
                            outVars.add(calc)
                            binding.setVariable(calc.name, calcRes);
                        }
                    }
                } catch (org.codehaus.groovy.control.CompilationFailedException exc) {
                    logr.warn(calc.name + " '" + script + "': " + exc.toString())
                } catch (Exception exc) {
                    logr.warn(calc.name + " '" + script + "': " + exc.toString())
                } finally {
                }
            }
        }
    }

    private def validate(def unit, def vars, def specVars, def limitSpecs) {

        Binding binding = new Binding();

        if (vars == null) {
            vars = contentService.getVariables(unit.pctg, unit.pkey, unit.tkey, ['dc', 'din'])
        }
        if (specVars == null) {
            specVars = contentService.getVariables(unit.pctg, unit.pkey, unit.tkey, ['spec'])
        }

        def sanityCheck = ""
        def limitCheck = ""

        // Validate if this bom is valid
        if (unit.bomId && unit.bomOk == true) {
            def processStep = workflowService.getProcessStep(unit.pctg, unit.pkey, unit.tkey)
            if (processStep.allowMerge) {
                def db = mongo.getDB("glo")
                def size = db.unit.find([parentCode: unit.code], [code: 1]).collect { it }.size()
                if (size == 0) {
                    limitSpecs.add("This product does not contain valid parts. Use merge to fix the problem.")
                    return "3"
                }
            }
        }

        for (def var in vars) {

            if ((unit[var.name] == null || unit[var.name]?.equals(null) || unit[var.name].toString().trim() == "") && var.allowBlank == false && var.hidden == false && var.dir == 'dc') {
                return "1"
            }

            if (unit[var.name] != null && var.dataType in ['scientific']) {
                def pattern = ~/[-+]?[0-9]*\.?[0-9]+([eE][-+]?[0-9]+)?/
                if (!pattern.matcher(unit[var.name].toString()).matches()) {
                    return "1"
                }
            }

            if (var.allowBlank == false && var.hidden == false && var.listValues) {
                if (var.dataType in ['float', 'scientific']) {
                    // hard to know if float is in list
                } else {
                    if (var.listForceSelection && !var.listValues.tokenize(",").collect {
                        it.trim()
                    }.contains(unit[var.name].toString()))
                        return "1"
                }
            }

            def range = var.allowedRange?.tokenize(",")
            if (range && range.size() == 2 && var.dataType in ['int', 'float', 'scientific'] && !(unit[var.name] == null || unit[var.name]?.equals(null) || unit[var.name].toString().trim() == "")) {
                if (range[0].toFloat() > unit[var.name].toFloat() || range[1].toFloat() < unit[var.name].toFloat()) {
                    sanityCheck = sanityCheck + "<br/>" + (var.title ?: var.name) + " (" + range[0] + " - " + range[1] + ")"
                }
            } else if (range && range.size() == 2 && var.dataType in ['string'] && !(unit[var.name] == null || unit[var.name]?.equals(null) || unit[var.name].toString().trim() == "")) {
                if (range[0] > unit[var.name] || range[1] < unit[var.name]) {
                    sanityCheck = sanityCheck + "<br/>" + (var.title ?: var.name) + " (" + range[0] + " - " + range[1] + ")"
                }
            }

            def specLimit = var.specLimit?.tokenize(",")
            if (specLimit && specLimit.size() == 2 && var.dataType in ['int', 'float', 'scientific'] && !(unit[var.name] == null || unit[var.name]?.equals(null) || unit[var.name].toString().trim() == "")) {
                if (specLimit[0].toFloat() > unit[var.name].toFloat() || specLimit[1].toFloat() < unit[var.name].toFloat()) {
                    limitCheck = limitCheck + "<br/>" + (var.title ?: var.name) + " (" + specLimit[0] + " - " + specLimit[1] + ")"
                }
            }

            binding.setVariable(var.name.replace(" ", "_"), unit[var.name]);
        }

        if (sanityCheck) {
            limitSpecs.add(sanityCheck)
            return "3"
        }

        if (limitCheck) {
            limitSpecs.add(limitCheck)
            return "4"
        }

        // Control against spec
        if (specVars) {
            binding.setVariable("db", mongo.getDB("glo"));
            def shell = new GroovyShell(binding)
            for (def spec in specVars) {
                def script = spec.evalScript
                if (script) {
                    try {
                        def value = shell.evaluate(script)
                        if (value == false)
                            return "2"
                        else if (value == "YELLOW") {
                            return "1"
                        } else if (value == "RED") {
                            return "2"
                        }
                    } catch (org.codehaus.groovy.control.CompilationFailedException exc) {
                        logr.warn(unit.pkey + " - " + unit.tkey + " - " + spec.name + " '" + script + "': " + exc.toString())
                    } catch (Exception exc) {
                        logr.warn(unit.pkey + " - " + unit.tkey + " - " + spec.name + " '" + script + "': " + exc.toString())
                    } finally {
                    }
                }
            }
        }

        return "0"
    }

    def revive(def code) {

        def db = mongo.getDB("glo")

        def hist = History.findByCode(code)
        def unt = Unit.findByCode(code)

        if (hist && !unt) {
            def cntAudits = hist["audit"].size()
            def aud = hist.dbo["audit"][cntAudits - 1]
            hist.dbo["audit"][cntAudits - 1].remove("duration")
            hist.dbo["audit"][cntAudits - 1].remove("end")
            db.history.save(hist.dbo)


            def temp = db.unitarchive.find(new BasicDBObject("code", code))
            def u = temp.collect { it }
            if (u && u.size() == 1) {
                def u1 = u[0]
                u1.tkey = hist.dbo["audit"][cntAudits - 1].tkey
                u1.tname = hist.dbo["audit"][cntAudits - 1].tname
                u1.pkey = hist.dbo["audit"][cntAudits - 1].pkey
                u1.pctg = hist.dbo["audit"][cntAudits - 1].pctg
                u1.qtyOut = 1
                db.unit.save(u1)
                db.unitarchive.remove(["_id": u1["_id"]])

                historyDataService.init(hist.dbo, u1)

                db.moves.remove([code: u1.code, tkey: 'END'])
            }
            return true
        }
        return false
    }

    def addNote(String user, def unitId, def comment) {

        if (comment) {

            Unit unit = null

            Unit.withTransaction { status ->

                unit = Unit.get(unitId)
                def note = new Note(userName: user, comment: comment, stepName: unit.tname)

                unit.addToNotes(note)
                unit.nNotes = unit.notes?.size()
                unit.save(failOnError: true)
            }

            if (unit)
                historyService.initHistory("addNote", null, unit.dbo, null)
        }

    }

    private def copyProperties(def source, def target) {
        target.metaClass.properties.each {
            if (source.metaClass.hasProperty(source, it.name) && it.name != 'metaClass' && it.name != 'class')
                it.setProperty(target, source.metaClass.getProperty(source, it.name))
        }
    }

    private def deepClone(domainInstanceToClone) {
        def dbo = domainInstanceToClone.dbo.clone()
        dbo
    }

    private def assignSecurity(def unit, def taskdef, def uid, def assignToUser) {

        if (taskdef) {

            unit.grps = []
            taskdef.groups.each {
                unit.grps.add(it.toString())
            }
            unit.usrs = []
            taskdef.users.each {

                unit.usrs.add(it.toString())
            }
        }
        if (assignToUser?.trim()) {
            if (assignToUser == "[UNASSIGN]") {
                unit.usrs = []
                unit['owner'] = ""
            } else {
                unit.usrs = [assignToUser]
                unit['owner'] = assignToUser
            }
        }
        unit['user'] = uid
    }

// Equiment maintenance relate methods //////////////////////////////////////////////////////////

    def addMaintenanceTask(def em, def startDate, def files, def notes, def department, def tag, boolean isNext) {

        def db = mongo.getDB("glo")

        def recv = new Expando()

        if (!em)
            return

        recv.uid = "admin"

        // Determine supplier
        recv.cid = 1

        // Determine product
        def product = Product.findByCode("maintenanceTask")
        recv.pid = product.id

        // If new unit
        def e = new BasicDBObject()
        e.put('$regex', "EMID" + em.id.toString())
        def bdo = new BasicDBObject()
        bdo.put("tagsCustom", e)
        bdo.put('$or', [new BasicDBObject("tkey", 'scheduled_time'), new BasicDBObject("tkey", 'scheduled_runs'), new BasicDBObject("tkey", 'scheduled_weights'), new BasicDBObject("tkey", 'scheduled_depositions')])

        def temp = db.unit.find(bdo, new BasicDBObject()).collect { it }


        def findUnitByEm = { emid ->

            return db.unit.find([tkey: 'scheduled', equipmentMaintenanceId: emid]).collect { it }[0]
        }

        if (!temp) {
            recv.units = []
            def m = [:]
            m.put('code', "")
            m.put('qty', 1)
            m.put('equipmentMaintenanceId', "EMID" + em.id.toString())
            m.put('schedule', em.schedule)
            m.put('equipmentName', em.equipment.name)
            m.put('department', department)
            m.put('tag', tag)
            m.put('owner', 'admin')
            def ds = getDateTime(em, startDate, isNext)
            m.put('actualStart', ds[0])
            m.put('start', ds[0])
            m.put('actualTime', ds[1])

            def transition = "time"
            if (ds[2] > -1) {
                m.put('every', ds[2])
                transition = "runs"
            } else if (ds[3] > -1) {
                m.put('every', ds[3])
                transition = "weights"
            } else if (ds[4] > -1) {
                m.put('every', ds[4])
                transition = "depositions"
            }
            recv.units.add(m)
            start(recv, "PM", "maintenance")

            def unit = findUnitByEm("EMID" + em.id.toString())

            if (unit && notes) {
                notes.each {
                    def nid = db['note.next_id'].find().collect { it.next_id }[0]
                    db['note.next_id'].update([:], [$set: [next_id: nid + 1]])
                    db.note.insert([_id: nid + 1, dateCreated: new Date(), userName: it.userName, comment: it.comment, stepName: "scheduled_" + transition, unit: unit._id, version: 0])
                    if (!unit.nNotes)
                        unit.nNotes = 1;
                    else
                        unit.nNotes += 1
                }
                db.unit.update([_id: unit._id], [$set: [nNotes: unit.nNotes]])
            }

            if (unit && files) {
                files.each {
                    def fid = db['file.next_id'].find().collect { it.next_id }[0]
                    db['file.next_id'].update([:], [$set: [next_id: fid + 1]])
                    db.file.insert([_id: fid + 1, dateCreated: new Date(), userName: it.userName, name: it.name, fileName: it.fileName, fileId: it.fileId, unit: unit._id, version: 0])
                    if (!unit.nFiles)
                        unit.nFiles = 1
                    else
                        unit.nFiles += 1
                }
                db.unit.update([_id: unit._id], [$set: [nFiles: unit.nFiles]])
            }

            def buf = new Expando()
            buf.prior = 50
            buf.isEngineering = true
            buf.processCategoryEng = "PM"
            buf.processKeyEng = "maintenance"
            buf.taskKeyEng = "scheduled_" + transition
            buf.equipmentMaintenanceId = "EMID" + em.id.toString()
            buf.schedule = em.schedule
            buf.equipmentName = em.equipment.name
            buf.department = department
            buf.tag = tag
            buf.pmMove = "1"
            buf.actualStart = ds[0]
            if (!isNext && (transition == "runs" || transition == "weights" || transition == "depositions")) {
                buf.start = ds[0]
            }
            buf.units = []
            def n = [:]
            n.put('transition', 'engineering')
            n.put('id', unit["_id"])
            buf.units.add(n)
            move(unit["movedBy"], buf)

        }

    }

    private def completeMaintenanceTask(def emid, def dateStart, def files, def notes, def department, def tag) {

        def equipmentMaintenance = EquipmentMaintenance.get(emid.replace("EMID", "").toLong())

        addMaintenanceTask(equipmentMaintenance, dateStart, files, notes, department, tag, true)
    }

    private def getDateTime(def em, def startDate, boolean isNext) {

        // Get time in miliseconds of the starting time

        long start = startDate.getTime()

        long duration = -1
        long occurences = -1
        long weight = -1
        long deposition = -1

        switch (em.cycleType) {
            case 'hours':
                duration = (long) em.cycleRate * 3600000
                break;
            case 'days':
                duration = (long) em.cycleRate * (long) 86400000
                break;
            case 'weeks':
                duration = (long) em.cycleRate * (long) 604800000
                break;
            case 'months':
                duration = (long) em.cycleRate * (long) 2592000000
                break;
            case 'quarters':
                duration = (long) em.cycleRate * (long) 7776000000
                break;
            case 'years':
                duration = (long) em.cycleRate * (long) 31536000000
                break;
            case 'runs':
                occurences = (long) em.cycleRate
            case 'weights':
                weight = (long) em.cycleRate
                break;
            case 'depositions':
                deposition = (long) em.cycleRate
                break;
        }


        long due = start
        if (isNext) {
            due = due + duration
        }

        GregorianCalendar calendar = new GregorianCalendar()
        calendar.setTimeInMillis(due)

        def d = new SimpleDateFormat("yyyy-MM-dd")
        def t = new SimpleDateFormat("hh:mm a")

        [d.format(calendar.getTime()), t.format(calendar.getTime()), occurences, weight, deposition]
    }

    def createSubUnit(def db, def unit, def subCode, def user, def pctg, def pkey, def tkey, def parms, def hasParent) {

        def u2 = new Unit()
        unit.each { k, v ->
            if (k != "_id") {
                u2.properties.put(k, v)
            }
        }
        u2.code = subCode
        u2.genPath = unit.code + "," + subCode
        u2.productCode = "105"
        u2.product = "Device"
        u2.productRevision = "1"
        u2.save(failOnError: true, flush: true)
        u2.indexing()

        def u = Unit.get(u2.id)
        unit.each { k, v ->
            if (k != "_id") {
                u.dbo.put(k, v)
            }
        }
        u.dbo.code = subCode
        u.dbo.genPath = unit.code + "," + subCode

        def idx1 = parms.die_spec.indexOf("(")
        def name = parms.die_spec.substring(0, idx1).trim()
        def rev = parms.die_spec.substring(idx1 + 5, parms.die_spec.size() - 1).trim()

        def dieSpec = DieSpec.findByNameAndRevision(name, rev)

        u.dbo.productCode = dieSpec?.product?.code ?: "105"
        u.dbo.product = dieSpec?.product?.name ?: "Device"
        u.dbo.productRevision = dieSpec?.product?.revision ?: "1"
        u.dbo.productFamily = dieSpec?.product?.productFamily?.name ?: ""



        if (hasParent)
            u.dbo.put("parentCode", unit.code)
        else
            u.dbo.put("parentCode", null)
        u.dbo.pctg = pctg
        u.dbo.pkey = pkey
        u.dbo.tkey = tkey
        u.dbo.tname = workflowService.getTaskName(pkey, tkey)
        u.dbo.start = new Date()
        u.dbo.actualStart = new Date()
        u.dbo.putAll(parms)

        if (!u.dbo["tagsCustom"]) {

            u.dbo.put("tagsCustom", [])
        } else {
            def t2 = []
            u.dbo["tagsCustom"].each { s ->
                if (s.toUpperCase().indexOf("TRAY") == -1) {
                    t2.add(s)
                }
            }
            u.dbo["tagsCustom"] = t2
        }
        u.dbo.tagsCustom.add(parms["tray_id_pocket"])

        db.unit.save(u.dbo)

        historyService.initHistory("startDerived", unit, u.dbo, ["parentDelete", false])

        u.dbo
    }

    def updateSubUnit(def db, def user, def subUnit, def pctg, def tkey, def parms) {

        def subHistory = db.history.find(new BasicDBObject("code", subUnit["code"]), new BasicDBObject()).collect {
            it
        }[0]
        if (subHistory["parentCode"])
            subHistory.remove("parentCode")
        db.history.save(subHistory)

        subUnit.put("parentCode", null)
        db.unit.save(subUnit)

        def buf = new Expando()
        buf.isEngineering = true
        buf.prior = 50
        buf.processCategoryEng = pctg
        buf.processKeyEng = "packaging"
        buf.taskKeyEng = tkey
        buf.units = []
        def n = [:]
        n.put('transition', 'engineering')
        n.put('id', subUnit["_id"])
        n.putAll(parms)
        buf.units.add(n)

        move(user, buf)
    }

    def generatePickPlaceFile(unit, deviceList) {

        // Generate file based on various bins
        def h = ['A': 10, 'B': 11, 'C': 12, 'D': 13, 'E': 14, 'F': 15, 'G': 16, 'H': 17]
        def mask = unit["mask"] ?: unit.dbo["mask"]

        if (!mask) {
            throw new RuntimeException("Unit has no mask defined.")
        }


        try {
            def binsDev = [:]
            (1..8).each { bin ->

                def specId = unit['bin' + bin + 'Id']

                if (specId && specId != 10) {
                    if (!deviceList) {
                        def testId = unit['testFullIndex'] ? unit['testFullIndex'][unit['testFullIndex'].size() - 1] : unit['testDataIndex'][unit['testDataIndex']?.size() - 1]
                        def testTkey = unit['testFullIndex'] ? "full_test_visualization" : "test_data_visualization"
                        def devices = waferFilterService.getValidDevices(unit.code, testTkey, testId, specId.toLong())
                        devices.each { device ->
                            if (!binsDev.containsKey(device))
                                binsDev.put(device, bin)
                        }
                    } else {
                        deviceList.each {
                            def dev = it.replace("_", "")
                            if (!binsDev.containsKey(dev))
                                binsDev.put(dev, bin)
                        }
                    }
                }
            }

            def toNum = { val ->
                def key = val.substring(0, 1)
                key = h[key] ?: key
                (key + val.substring(1, 2)).toInteger()
            }

            ProductMask productMask = ProductMask.findByName(mask)
            def maskItems = productMask.productMaskItems.sort { it.code }
            def pickMap = []
            def sx = 0
            def sy = 0
            maskItems.each { maskItem ->

                int x = toNum(maskItem.code.substring(0, 2))
                int y = toNum(maskItem.code.substring(2, 4))
                def pickRow = x + "," + y

                if (sx == 0) sx = (maskItem.sizeX * 1000).round(0).toInteger()
                if (sy == 0) sy = (maskItem.sizeY * 1000).round(0).toInteger()

                if (maskItem.isActive) {
                    if (binsDev.containsKey(maskItem.code)) {
                        pickRow += "," + binsDev[maskItem.code] + ",1"
                    } else {
                        pickRow += ",9,1"
                    }
                } else {
                    pickRow += ",10,1"
                }
                pickRow += "," + sx + "," + sy + ",\"" + maskItem.code + "\""

                pickMap.add(pickRow)
            }

            // Create file
            def file = new java.io.File("\\\\ROYCE-1005550\\DieSortManager\\Maps\\" + unit.code + ".csv")
            file.write("")

            def str = "MAJOR_FORMAT,1\r\n"
            str += "MINOR_FORMAT,0\r\n"
            str += "BATCH_ID,xxx\r\n"
            str += "WAFER_ID," + unit.code + "\r\n"
            str += "WAFER_DIAMETER,50800\r\n"
            str += "RETICLE_PITCH_X," + sx + "\r\n"
            str += "RETICLE_PITCH_Y," + sy + "\r\n"
            str += "SUBDIE_PITCHES_X," + sx + "\r\n"
            str += "SUBDIE_PITCHES_Y," + sy + "\r\n"
            str += "WAFER_MAP_OFFSET_X,0\r\n"
            str += "WAFER_MAP_OFFSET_Y,0\r\n"
            str += "MAP_DATA\r\n"
            pickMap.each {
                str += it + "\r\n"
            }
            str += "END_MAP_DATA"
            file << str

        } catch (Exception exc) {
            throw new RuntimeException("Generate file failed: " + exc.getMessage())
        }

    }

    def generatePickPlaceFileSae(user, unit, deviceList) {

        // Generate file based on various bins
        def h = ['A': 10, 'B': 11, 'C': 12, 'D': 13, 'E': 14, 'F': 15, 'G': 16, 'H': 17]
        def mask = unit["mask"] ?: unit.dbo["mask"]

        if (!mask) {
            throw new RuntimeException("Unit has no mask defined.")
        }


        try {
            def binsDev = [:]
            def binsCount = [:]
            (1..8).each { bin ->

                def specId = unit['bin' + bin + 'Id']

                if (specId && specId != 10) {
                    if (!deviceList) {
                        def testId = unit['testFullIndex'] ? unit['testFullIndex'][unit['testFullIndex'].size() - 1] : unit['testDataIndex'][unit['testDataIndex']?.size() - 1]
                        def testTkey = unit['testFullIndex'] ? "full_test_visualization" : "test_data_visualization"
                        def devices = waferFilterService.getValidDevices(unit.code, testTkey, testId, specId.toLong())
                        devices.each { device ->
                            if (!binsDev.containsKey(device))
                                binsDev.put(device, bin)
                            if (!binsCount.containsKey(bin))
                                binsCount.put(bin, 0)
                            binsCount[bin]++
                        }
                    } else {
                        deviceList.each {
                            def dev = it.replace("_", "")
                            if (!binsDev.containsKey(dev))
                                binsDev.put(dev, bin)
                            if (!binsCount.containsKey(bin))
                                binsCount.put(bin, 0)
                            binsCount[bin]++
                        }
                    }
                }
            }

            def productMaskItems = ProductMaskItem.findAllByProductMask(
                    ProductMask.findByName(mask))

            def xList = productMaskItems.collect {
                if (it.code.length() >= 4) {
                    it.code.substring(0, 2)
                } else {
                    ''
                }
            }.unique().sort()
            def yList = productMaskItems.collect {
                if (it.code.length() >= 4) {
                    it.code.substring(2, 4)
                } else {
                    ''
                }
            }.unique().sort()

            def codes = productMaskItems.inject([:]) { ret, entry ->
                ret[entry.code] = [entry.isActive, entry.pm]
                return ret
            }

            def sb = new StringBuilder()
            int t9 = 0, t0 = 0, tX = 0
            yList.reverseEach { y ->
                def row = ""
                xList.each { x ->
                    def code = x + y
                    if (binsDev.containsKey(code)) {
                        row += binsDev[code]
                    } else if (codes.containsKey(code)) {
                        if (!codes[code][0]) {
                            row += "9"
                            t9++
                        } else if (!codes[code][1]) {
                            row += "0"
                            t0++
                        } else {
                            row += "X"
                            tX++
                        }
                    } else {
                        row += "."
                    }
                }
                row += "\r\n"
                sb.append(row)
            }
            sb.append("_EOM_\r\n")
            sb.append("\r\n")

            sb.append("Program      :   Wafermap Generator\r\n")
            sb.append("Device       :   " + unit.code + "\r\n")
            sb.append("Lot          :   \r\n")
            sb.append("Wafer        :   5f\r\n")
            sb.append("Diameter     :   2500\r\n")
            sb.append("Tester       :   Waferprobe\r\n")
            sb.append("EWS orient   :   down\r\n")
            sb.append("DiePick Notch:   down\r\n")
            sb.append("Xsize(sort)  :   " + xList.size() + "\r\n")
            sb.append("ysize(sort)  :   " + yList.size() + "\r\n")
            sb.append("xsize(pick)  :   " + xList.size() + "\r\n")
            sb.append("ysize(pick)  :   " + yList.size() + "\r\n")
            sb.append("StartRow     :   1\r\n")
            sb.append("EndRow       :   " + xList.size() + "\r\n")
            sb.append("StartColumn  :   1\r\n")
            sb.append("EndColumn    :   " + yList.size() + "\r\n")

            sb.append("Date Test    :   " + new Date().format("dd-MMM-YY") + "\r\n")
            sb.append("Date Test    :   " + new Date().format("hh:mm") + "\r\n")
            sb.append("test time    :   1\r\n")

            sb.append("Wafer Scribe :   \r\n")
            sb.append("Prim Crown P :   \r\n")
            sb.append("Sec  Crown S :   \r\n")
            sb.append("Prim notch P :   \r\n")
            sb.append("Sec  notch S :   \r\n")
            sb.append("\r\n")

            def total = 0
            binsCount.each { bin, count ->

                sb.append("GOOD  bin " + bin + "        " + count + "   PASS\r\n")
                total += count
            }
            sb.append("YIELD              " + total + "\r\n")
            sb.append("FAIL  bin 0        " + t0 + "    Visual¶Electro-optical\r\n")
            sb.append("FAIL  bin 9        " + t9 + "    Visual¶Electro-optical\r\n")
            sb.append("FAIL  bin X        " + tX + "    Visual¶Electro-optical\r\n")

            def fileName = "SAE_PP_" + unit.code + ".TXT"
            def file = new java.io.File(fileName);
            file.write(sb.toString());
            def fileId = fileService.saveFileFromDisk(file, fileName, "text/plain")
            saveFileMeta(user, [unit.id], fileId, fileName, fileName)


        } catch (Exception exc) {
            throw new RuntimeException("Generate file failed: " + exc.getMessage())
        }

    }

    private def readPickPlaceFile(user, unit) {

        def h = [10: 'A', 11: 'B', 12: 'C', 13: 'D', 14: 'E', 15: 'F', 16: 'G', 17: 'H']
        def db = mongo.getDB("glo")

        def fromNum = { val ->
            String ret = val.toString()
            if (ret.length() == 3) {
                def f = h[ret.substring(0, 2).toInteger()]

                ret = f.toString() + ret.substring(2, 3)
            } else if (ret.length() == 1) {
                ret = "0" + ret.toString()
            }
            ret
        }

        // read dyes from file
        def file = new java.io.File("\\\\ROYCE-1005550\\DieSortManager\\Run Log\\" + unit.code + ".log")
        if (file) {

            BufferedReader br = new BufferedReader(new FileReader(file))
            def line = ""
            while ((line = br.readLine()) != null) {

                def row = line.split(',')
                if (row.length == 11) {

                    def parms = new BasicDBObject()
                    def dye = fromNum(row[4]) + fromNum(row[5])
                    def waferId = unit.code + "_" + dye
                    parms.put("die_spec", unit["bin" + row[6]])
                    parms.put("tray_id", row[3].replace("\"", "") + "-" + row[7])
                    parms.put("tray_x_position", row[8].toInteger())
                    parms.put("tray_y_position", row[9].toInteger())
                    parms.put("tray_pocket_no", row[10].toInteger())
                    parms.put("tray_id_pocket", parms["tray_id"] + "_" + String.format("%03d", row[10].toInteger()))

                    parms.put("royce_bin", row[6].toInteger())

                    def subUnit = db.history.find(new BasicDBObject("code", waferId), new BasicDBObject()).collect {
                        it
                    }[0]
                    if (!subUnit) {
                        createSubUnit(db, unit.dbo, waferId, user, "Die", "packaging", "packaging_queue", parms, false)
                    }
                }
            }
        }
    }

}
