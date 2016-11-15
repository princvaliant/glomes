package com.glo.custom

import com.glo.ndo.Product
import com.glo.ndo.ProductMask
import com.glo.ndo.ProductMaskItem
import com.glo.ndo.Variable
import com.glo.run.History
import com.glo.run.Note
import com.glo.run.Unit
import com.mongodb.BasicDBObject
import grails.converters.JSON
import grails.web.JSONBuilder
import org.apache.commons.logging.LogFactory
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics
import org.apache.poi.xssf.usermodel.*

import javax.servlet.ServletOutputStream
import java.text.DecimalFormat


class WaferController extends com.glo.run.Rest {

    private static final logr = LogFactory.getLog(this)

    def springSecurityService
    def unitService
    def grailsApplication

    def mongo

    def xText = [:]
    def yText = [:]


    def getCode = { code ->

        def s = code?.tokenize("_")
        if (s.size() == 2)
            s[0]
        else
            code
    }

    def getParameters = {

        def ret = []
        def obj = getData(getCode(params.code), params.tkey, params.propertyName, params.testId)

        if (obj.getClass() == com.mongodb.BasicDBObject) {
            BasicDBObject dbo = (BasicDBObject) obj

            TreeMap tm = new TreeMap()
            dbo.keySet().each {
                def curr = it.tokenize('@')
                if (curr && curr.size() == 2) {
                    Integer currVal = Integer.parseInt(curr[1].replaceAll("\\D+", ""))
                    Float currVal2 = currVal
                    currVal2 = currVal * 1E-9
                    if (curr[1].indexOf('mA') > 0) currVal2 = currVal * 1E-3
                    if (curr[1].indexOf('uA') > 0) currVal2 = currVal * 1E-6
                    if (curr[1].indexOf('nA') > 0) currVal2 = currVal * 1E-9
                    if (curr[1].indexOf('pA') > 0) currVal2 = currVal * 1E-12
                    if (curr[1].indexOf('fA') > 0) currVal2 = currVal * 1E-15
                    tm.put((Float) currVal2, it)
                } else {
                    if (it == "peakEqe") {
                        tm.put((Float) 10000, it)
                    } else if (it == "peakEqeCurrent"){
                        tm.put((Float) 10001, it)
                    }
                    else {
                        tm.put((Float) -1, "ALL")
                    }
                }
            }
            ret = tm.collect { k, val ->
                ['propName': val]
            }
        }

        render ret as JSON
    }

    def getParameters2 = {

        def username = springSecurityService.principal?.username
        def ret = []
        def obj = getData(getCode(params.code), params.tkey, params.propertyName, params.testId)

        if (obj.getClass() == com.mongodb.BasicDBObject) {
            def lev2 = obj[params.level2]
            if (lev2.getClass() == com.mongodb.BasicDBObject) {
                BasicDBObject dbo = (BasicDBObject) lev2
                ret = dbo.keySet().sort().collect { ['propName': it] }
            }
        }

        render ret as JSON
    }

    def getFilters = {

        def username = springSecurityService.principal?.username

        def ret = WaferFilter.findAllByUserNameAndIsAdmin(username, false)

        ret = ret.findAll { it.dieSpec == null }

        render(['data': ret.collect {
            [id     : it.id, isActive: it.isActive, level1: it.level1, level2: it.level2,
             valFrom: getStringValue(it.valFrom, 3), valTo: getStringValue(it.valTo, 3)]
        }, 'count'    : ret.size()] as JSON)


    }

    def getSummaries = {

        def username = springSecurityService.principal?.username

        def ret = WaferSummaryDoc.findAllByUsername(username)

        render(['data': ret.collect {
            [id  : it.id, isActive: it.isActive, parameter: it.parameter, current: it.current?.replace("Data @", ""),
             testType: it.testType, dieSpec: it?.dieSpec?.name ?: '']
        }, 'count'    : ret.size()] as JSON)
    }


    def addSummary = {

        try {
            def username = springSecurityService.principal?.username

            def waferSummaryDoc = new WaferSummaryDoc(params)
            if (params.dieSpecId) {
                waferSummaryDoc.dieSpec = DieSpec.get(params.dieSpecId.toInteger())
            }
            waferSummaryDoc.isActive = true
            waferSummaryDoc.username = username
            waferSummaryDoc.save(failOnError: true)

            render([success: true, data: [id  : waferSummaryDoc.id, isActive: waferSummaryDoc.isActive,
                                          parameter: waferSummaryDoc.parameter, current: waferSummaryDoc.current?.replace("Data @", ""),
                                          testType: waferSummaryDoc.testType, dieSpec: waferSummaryDoc?.dieSpec?.name ?: '']] as JSON)
        } catch (Exception exc) {
            logr.error(exc)
            render([success: false, msg: exc.getMessage()] as JSON)
        }
    }

    def deleteSummary = {

        try {
            def wsd = WaferSummaryDoc.get(params.id)
            if (wsd) {
                wsd.delete([cascade: true])
            }
        } catch (Exception exc) {
            logr.error(exc)
        }
        render([success: true] as JSON)
    }

    def getComment = {

        def db = mongo.getDB("glo")
        def queryTest = new BasicDBObject()
        queryTest.put("value.code", params.code)
        queryTest.put("value.testId", params.testId.toLong())
        queryTest.put("value.tkey", params.tkey)

        def testData = db.testData.find(queryTest, new BasicDBObject('testData.value.data', 0)).collect { it }[0]
        if (testData && testData["value"]) {
            render(['comment': testData.value.comment, 'actarea': testData.value.actarea, 'devtype': testData.value.devtype] as JSON)
        } else {
            render(['comment': "", 'actarea': ""] as JSON)
        }
    }

    def editComment = {

        def db = mongo.getDB("glo")
        def queryTest = new BasicDBObject()
        queryTest.put("value.code", params.code)
        queryTest.put("value.testId", params.testId.toLong())
        queryTest.put("value.tkey", params.tkey)

        if (params.actarea) {
            db.testData.update(queryTest, ['$set': ['value.actarea': params.actarea.isFloat() ? params.actarea.toFloat() : 0]], false, true)
        }
        db.testData.update(queryTest, ['$set': ['value.comment': params.comment]], false, true)

        if (params.comment) {
            def comment = "TEST-" + params.testId + " DEV-" + params.devtype + ": " + params.comment
            def unt = Unit.findByCode(params.code)
            unitService.addNote( springSecurityService.principal?.username, unt.id, comment)
        }
        render(['success': 'true'] as JSON)
    }


    def specData = {


        def username = springSecurityService.principal?.username

        def db = mongo.getDB("glo")
        def unit = db.unit.find([code: getCode(params.code)]).collect { it }[0]

        def mask = ProductMask.findByName(unit.mask)
        def maskDevices = new HashSet()
        mask.productMaskItems.each {
            if (it.isActive && !it.pm) {
                maskDevices.add(it.code)
            }
        }

        def dieSpec = DieSpec.get(params.spec)

        def results = new HashSet()

        Set map = new TreeSet()
        def h = [10: 'A', 11: 'B', 12: 'C', 13: 'D', 14: 'E', 15: 'F', 16: 'G', 17: 'H']

        def toNum = { val ->
            def key = val.substring(0, 1)
            key = h.find { it.value == val.substring(0, 1) }?.key?.toString() ?: key
            (key + val.substring(1, 2)).toInteger()
        }

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

        params.devices.tokenize(",").each {

            def p = it?.trim()

            if (p?.length() == 4) {

                int x = toNum(p.substring(0, 2))
                int y = toNum(p.substring(2, 4))

                def dx = [0, 0]
                def dy = [0, 0]

                if (mask) {
                    dx = [mask.dxFrom, mask.dxTo]
                    dy = [mask.dyFrom, mask.dyTo]
                }

                for (int j in dy[0]..dy[1]) {
                    for (int i in dx[0]..dx[1]) {
                        if (maskDevices.contains(fromNum(x + i) + fromNum(y + j)))
                            results.add(fromNum(x + i) + "_" + fromNum(y + j))
                    }
                }
            }
        }

        String str = results.sort { it }.join(" ")

        if (params.pp) {

            db.unit.update([code: unit.code], ['$set':['bin1Id':dieSpec.id]])
            unitService.generatePickPlaceFile (unit, results)
            render ("<b>Successfully generated file containing " + results.size() + " devices.</b>" )

        } else {
            str = "rescurve lighton con (10_81 41_G0 80_81 41_10 41_80)*(wait4 V6_pos) 41_80 pause lighton lightoff filter020 (" + str
            str += ")*(" + mask?.name + "_" + mask?.recipe + ") 41_80 closespec lighton beep beep beep"

            def dss = dieSpec.name

            try {
                String path = grailsApplication.config.glo.fullTestConfigFilePath
                def file1 = new java.io.File(path + "\\${unit.code + ' ' + dss}.txt")
                file1.write(str)
            } catch (Exception exc) {
            }

            response.setHeader "Content-disposition", "attachment; filename=${unit.code + ' ' + dss}.txt"
            response.contentType = 'text-plain'
            ServletOutputStream f = response.getOutputStream()
            f << str
            f.close()
        }
    }

    def getSpecs = {

        def username = springSecurityService.principal?.username

        def ret = DieSpec.list()

        render(['data': ret.collect {
            [id: it.id, name: it.name + """ (Rev. $it.revision)""", revision: it.revision, dateCreated: it.dateCreated]
        }, 'count'    : ret.size()] as JSON)
    }

    def addSpec = {

        if (!org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils.ifAnyGranted("ROLE_TEST_ADMIN,ROLE_SPEC_ADMIN")) {
            render([success: false, msg: "You have no right to add spec"] as JSON)
        } else {

            try {
                def username = springSecurityService.principal?.username

                if (!(params.name && params.revision)) {
                    throw new RuntimeException("Name and revision is required")
                }

                def ds = DieSpec.findByNameAndRevision(params.name, params.revision.toInteger())
                if (ds) {
                    throw new RuntimeException("This name and revision already exists")
                }

                ds = new DieSpec()
                ds.name = params.name
                ds.product = Product.get(params.product.toLong())
                ds.revision = params.revision.toInteger()
                ds.username = username
                ds.dateCreated = new Date()
                ds.save()

                render([success: true, data: ds.id] as JSON)
            } catch (Exception exc) {
                logr.error(exc)
                render([success: false, msg: exc.getMessage()] as JSON)
            }
        }
    }

    def getSpecFilters = {

        def username = springSecurityService.principal?.username

        def ret = WaferFilter.findAllByDieSpec(DieSpec.get(params.spec.toLong()))

        render(['data': ret.collect {
            [id     : it.id, isActive: it.isActive, level1: it.level1, level2: it.level2,
             valFrom: getStringValue(it.valFrom, 3), valTo: getStringValue(it.valTo, 3)]
        }, 'count'    : ret.size()] as JSON)
    }

    def getAdminFilters = {

        def username = springSecurityService.principal?.username

        def ret = WaferFilter.findAllByIsAdmin(true)

        render(['data': ret.collect {
            [id     : it.id, isActive: it.isActive, level1: it.level1, level2: it.level2,
             valFrom: getStringValue(it.valFrom, 3), valTo: getStringValue(it.valTo, 3)]
        }, 'count'    : ret.size()] as JSON)
    }

    def addFilter = {

        if (params.tab == "specWaferFilterTab" && !org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils.ifAnyGranted("ROLE_TEST_ADMIN,ROLE_SPEC_ADMIN")) {
            render([success: false, msg: "You have no right to change specs"] as JSON)
        } else {

            try {
                def username = springSecurityService.principal?.username

                def waferFilter = new WaferFilter(params)
                waferFilter.valFrom = params.valFrom.toFloat()
                waferFilter.valTo = params.valTo.toFloat()
                waferFilter.isActive = false

                if (params.tab == "waferFilterTab") {
                    waferFilter.isAdmin = false
                }
                if (params.tab == "gridtestDataAdminFiltersId") {
                    waferFilter.isAdmin = true

                }
                if (params.tab == "specWaferFilterTab") {
                    waferFilter.isAdmin = false
                    if (params.spec) {
                        def dieSpec = DieSpec.get(params.spec.toLong())
                        if (dieSpec) {
                            waferFilter.dieSpec = dieSpec
                        }
                    }
                }

                waferFilter.userName = username
                waferFilter.save(failOnError: true)

                render([success: true, data: waferFilter] as JSON)
            } catch (Exception exc) {
                logr.error(exc)
                render([success: false, msg: exc.getMessage()] as JSON)
            }
        }
    }

    def deleteFilter = {


        if (params.tab == "specWaferFilterTab" && !org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils.ifAnyGranted("ROLE_TEST_ADMIN,ROLE_SPEC_ADMIN")) {
            render([success: false, msg: "You have no right to delete specs"] as JSON)

        } else {

            try {
                def wf = WaferFilter.get(params.id)
                if (wf) {
                    wf.delete([cascade: true])
                }
            } catch (Exception exc) {
                logr.error(exc)
            }
            render([success: true] as JSON)
        }
    }

    def selectFilter = {

        def selected = JSON.parse(params.selected).collect { it.toLong() }

        def username = springSecurityService.principal?.username

        WaferFilter.withSession { session ->
            if (params.isAdmin == "0") {

                session.createQuery("""update WaferFilter t set t.isActive=0 where
							t.userName = :userName and t.isAdmin = 0 """)
                        .setParameter('userName', username).executeUpdate()
                if (selected)
                    session.createQuery("""update WaferFilter t set t.isActive=1 where
								 t.userName = :userName and t.isAdmin = 0 and t.id in (:selectedList)""")
                            .setParameterList('selectedList', selected)
                            .setParameter('userName', username).executeUpdate()
            } else {
                session.createQuery("""update WaferFilter t set t.isActive=0 where
							t.isAdmin = 1""").executeUpdate()

                if (selected)
                    session.createQuery("""update WaferFilter t set t.isActive=1 where
										   t.isAdmin = 1 and t.id in (:selectedList)""")
                            .setParameterList('selectedList', selected).executeUpdate()
            }
        }

        render([success: true] as JSON)
    }


    def getMap = {

        def retMap = [:]

        // Get data from database
        def data2 = null
        def obj = null

        def field = params.level2

        def testId = -1
        if (params["testId"]?.isLong())
            testId = params["testId"].toLong()
        else
            logr.warn(getCode(params.code) + " " + params.tkey + " has no testId defined")

        def top5 = params.top5
        def bypassAdminFilter = params.bypassAdminFilter

        def db = mongo.getDB("glo")
        def queryTest = new BasicDBObject()

        queryTest.put("value.code", getCode(params.code))
        queryTest.put("value.testId", testId)
        queryTest.put("value.tkey", params.tkey)

        String comment1 = ""
        String comment2 = ""
        String actarea = ""
        String devtype = ""

        def testData = db.testData.find(queryTest, new BasicDBObject()).collect { it }[0]
        if (testData && testData["value"] && testData["value"]["data"]) {
            obj = (BasicDBObject) testData["value"]["data"]

            if (obj[params.level1])
                data2 = (BasicDBObject) obj[params.level1][params.level2]
            if (testData.value.actarea) {
                actarea = testData.value.actarea.toString()
            }
            if (testData.value.devtype) {
                devtype = testData.value.devtype
            }
            if (testData.value.comment) {

                if (testData.value.comment.length() >= 35) {
                    comment1 = testData.value.comment.substring(0, 35)
                    comment2 = testData.value.comment.substring(35)
                } else {
                    comment1 = testData.value.comment
                }
            }
        }



        def history = History.findByCode(getCode(params.code))
        def audit = history["audit"].find { it.tkey == params.tkey && it.last == true }
        def audit2 = history["audit"].find { it.tkey == 'epi_growth' && it.last == true }
        def audit3 = history["audit"].find { it.tkey == 'mesa_patterning' && it.last == true }
        def runNumber = ""
        if (audit2 && audit2["din"]) {
            runNumber = audit2["din"]["runNumber"]
        }
        if (!data2) {
            render([success: false, msg: "No data defined for wafer '" + getCode(params.code) + "' and values '" + params.level1 + "' and '" + params.level2 + "'"] as JSON)
        } else {
            // Get filters for user
            def username = springSecurityService.principal?.getClass() != String ? springSecurityService.principal?.username : "admin"
            def waferFilters = []

            def maskName = audit["din"]["mask"] ?: (audit["dc"]["mask"] ?: audit3["dc"]["mask"])
            def productMask = ProductMask.findByName(maskName)

            if (params.act == "U") {
                waferFilters = WaferFilter.executeQuery("""
					select wf from WaferFilter as wf where wf.userName = ? and wf.isActive = 1 and wf.isAdmin = 0
					""", username)
            }

            if (params.act == "S" && params.spec) {
                waferFilters = WaferFilter.findAllByDieSpec(DieSpec.get(params.spec.toLong()))
            }

            // Apply optical filter if level2 represents optical
            if (!(params.level2 in [
                    "OSA Int Time (ms)",
                    "OSA Counts",
                    "Pulse Current (mA)",
                    "Pulse Voltage (V)"
            ])) {
                def adminFilters = WaferFilter.executeQuery("""
					select wf from WaferFilter as wf where wf.level1 = 'ALL' and wf.isActive = 1 and wf.isAdmin = 1
					""")
                if (bypassAdminFilter == "false") {
                    waferFilters += adminFilters
                }
            }

            Set filtered = new HashSet()
            def thumb1 = null, thumb2 = null
            waferFilters.eachWithIndex { waferFilter, i ->

                if (obj[waferFilter.level1]) {
                    def unfiltered = (BasicDBObject) obj[waferFilter.level1][waferFilter.level2]

                    if (params.level1 == waferFilter.level1 && params.level2 == waferFilter.level2) {
                        thumb1 = waferFilter.valFrom
                        thumb2 = waferFilter.valTo
                    }

                    if (unfiltered) {
                        unfiltered.each { k, v ->
                            if (waferFilter.valFrom <= v && waferFilter.valTo >= v && i == 0) {
                                filtered.add(k)
                            } else if ((waferFilter.valFrom > v || waferFilter.valTo < v) && i > 0) {
                                if (filtered.contains(k))
                                    filtered.remove(k)
                            }
                        }
                    }
                }
            }

            // Find min and max for all data if filters null
            if (thumb1 == null && thumb2 == null) {
                DescriptiveStatistics stats = new DescriptiveStatistics((double[]) data2.collect {
                    (double) (it.value == null ? 0 : it.value)
                })
            }

            // Define map that contains histogram
            def filtSize = waferFilters.size()
            def rmap = new TreeMap()
            def maskDef = [:]
            data2.each { k, v ->

                getMaskHash(productMask, k, maskDef )

                if (filtSize == 0 || (filtered.contains(k) && maskDef[k])) {

                    if (params.level1 in ["Current @ 2V"] && v != 0) {
                        v = Math.log10(v)
                    }

                    if (top5 == "false") {
                        retMap.put(k, v)
                    } else {
                        rmap.put(v, [k, v])
                    }
                }
            }

            if (rmap) {
                def iii = 0
                rmap.reverseEach { k, v ->
                    if (iii < 5 && v)
                        retMap.put(v[0], v[1])
                    iii++
                }
            }

            DescriptiveStatistics stats2 = new DescriptiveStatistics((double[]) retMap.collect {
                (double) (it.value == null ? 0 : it.value)
            })
            def mmin = stats2.getMin()
            def mmax = stats2.getMax()
            def mean = stats2.getMean()
            double median = stats2.getPercentile(50)
            def stdDev = stats2.getStandardDeviation()
            thumb1 = mmin
            thumb2 = mmax

            if (thumb1.isNaN() || thumb2.isNaN()) {
                thumb1 = 0
                thumb2 = 0
            }

            def sortValues = stats2.getSortedValues() as List
            DescriptiveStatistics stats3 = new DescriptiveStatistics()
            (1..5).each {
                if (sortValues) {
                    def pop1 = sortValues.pop()
                    if (pop1) {
                        stats3.addValue(pop1)
                    }
                }
            }
            def top5Mean = stats3.getMean()



            def histMap = [:]
            def scale = (mmax - mmin) / 9
            if (scale == 0) scale = 1.0
            retMap.each { k, v ->
                def histVal = ((v - mmin) / scale).toLong()
                if (!histMap[histVal]) {
                    def o = new Expando()
                    o.total = 1
                    o.min = mmin + scale * histVal
                    o.max = o.min + scale
                    histMap.put(histVal, o)
                } else
                    histMap[histVal].total++
            }



            def dataSize = retMap.size()

            def builder = new JSONBuilder()
            def results = builder.build {
                spec = {
                    mins = thumb1
                    maxs = thumb2
                }
                devices = array {
                    dataw = {
                        type = 'text'
                        fill = "#111111"
                        font = " 12px Arial"
                        text = "Serial#: " + history.code
                        x = 13
                        y = 11
                    }
                    dataw = {
                        type = 'text'
                        fill = "#111111"
                        font = " 12px Arial"
                        text = "Test " + testId
                        x = 180
                        y = 11
                    }
                    dataw = {
                        type = 'text'
                        fill = "#111111"
                        font = " 12px Arial"
                        text = "Comment:"
                        x = 500
                        y = 11
                    }
                    dataw = {
                        type = 'text'
                        fill = "#111111"
                        font = " 11px Arial"
                        text = comment1
                        x = 560
                        y = 11
                    }
                    dataw = {
                        type = 'text'
                        fill = "#111111"
                        font = " 11px Arial"
                        text = comment2
                        x = 560
                        y = 25
                    }
                    dataw = {
                        type = 'text'
                        fill = "#111111"
                        font = " 12px Arial"
                        text = "Active area : " + actarea
                        x = 630
                        y = 45
                    }
                    dataw = {
                        type = 'text'
                        fill = "#111111"
                        font = " 12px Arial"
                        text = "Device type : " + devtype
                        x = 630
                        y = 59
                    }
                    dataw = {
                        type = 'text'
                        fill = "#111111"
                        font = " 12px Arial"
                        text = "Exp#: " + history["experimentId"] ?: ""
                        x = 13
                        y = 30
                    }
                    dataw = {
                        type = 'text'
                        fill = "#111111"
                        font = " 12px Arial"
                        text = "Run#: " + runNumber
                        x = 105
                        y = 30
                    }
                    dataw = {
                        type = 'text'
                        fill = "#111111"
                        font = " 12px Arial"
                        text = "FAB process: " + history["process"] ?: audit.pkey
                        x = 13
                        y = 48
                    }
                    dataw = {
                        type = 'text'
                        fill = "#111111"
                        font = " 12px Arial"
                        text = "Step: " + audit.tkey
                        x = 13
                        y = 61
                    }
                    dataw = {
                        type = 'text'
                        fill = "#111111"
                        font = " 12px Arial"
                        text = params.level1
                        x = 13
                        y = 82
                    }
                    dataw = {
                        type = 'text'
                        fill = "#111111"
                        font = " 12px Arial"
                        text = field
                        x = 13
                        y = 95
                    }
                    dataw = {
                        type = 'text'
                        fill = "#111111"
                        font = "12px Arial"
                        text = "Actual top 5 mean: " + getStringValue(top5Mean, 3)
                        x = 13
                        y = 112
                    }
                    dataw = {
                        type = 'text'
                        fill = "#111111"
                        font = "12px Arial"
                        text = "Min: " + getStringValue(mmin, 3)
                        x = 13
                        y = 126
                    }
                    dataw = {
                        type = 'text'
                        fill = "#111111"
                        font = "12px Arial"
                        text = "Max: " + getStringValue(mmax, 3)
                        x = 13
                        y = 139
                    }
                    dataw = {
                        type = 'text'
                        fill = "#111111"
                        font = "12px Arial"
                        text = "Mean: " + getStringValue(mean, 3)
                        x = 13
                        y = 152
                    }
                    dataw = {
                        type = 'text'
                        fill = "#111111"
                        font = "12px Arial"
                        text = "Median: " + getStringValue(median, 3)
                        x = 13
                        y = 165
                    }
                    dataw = {
                        type = 'text'
                        fill = "#111111"
                        font = "12px Arial"
                        text = "Std.dev.: " + getStringValue(stdDev, 3)
                        x = 13
                        y = 179
                    }
                    dataw = {
                        type = 'circle'
                        fill = "#333333"
                        radius = 314
                        x = 420
                        y = 327
                    }
                    dataw = {
                        type = 'circle'
                        fill = "#E3E3E3"
                        radius = 310
                        x = 420
                        y = 327
                    }
                    dataw = {
                        type = 'rect'
                        fill = "#FFFFFF"
                        x = 220
                        y = 616
                        width = 400
                        height = 50
                    }
                    dataw = {
                        type = 'rect'
                        fill = "#333333"
                        x = 295
                        y = 616
                        width = 250
                        height = 5
                    }
                    retMap.each { k, v ->
                        def maskDefItem = maskDef[k]
                        if (maskDefItem) {
                            if (dataSize < 200) {
                                data = {
                                    code = k
                                    name = k
                                    type = 'text'
                                    size = 6
                                    font = "11px Arial"
                                    text = getStringValue(v, 2)
                                    fill = getColor(v, thumb1, thumb2)
                                    width = calcW(maskDefItem)
                                    height = calcH(maskDefItem)
                                    x = calcX(maskDefItem)
                                    y = calcY(maskDefItem)
                                }
                            } else {
                                data = {
                                    code = k
                                    name = k
                                    text = getStringValue(v, 2)
                                    type = 'rect'
                                    fill = getColor(v, thumb1, thumb2)
                                    width = calcW(maskDefItem)
                                    height = calcH(maskDefItem)
                                    x = calcX(maskDefItem)
                                    y = calcY(maskDefItem)
                                }
                            }
                        }
                    }
                    dataw = {
                        type = 'rect'
                        fill = "#111111"
                        x = 10
                        y = 618
                        width = 100
                        height = 1
                    }
                    dataw = {
                        type = 'rect'
                        fill = "#111111"
                        x = 10
                        y = 215
                        width = 100
                        height = 1
                    }
                    dataw = {
                        type = 'text'
                        fill = "#333333"
                        font = " 11px Arial"
                        text = "Total: " + dataSize + " (100%)"
                        x = 10
                        y = 210
                    }
                    dataw = {
                        type = 'text'
                        fill = "#FF3333"
                        font = " 11px Arial"
                        text = getStringValue(mmin, 3)
                        x = 10
                        y = 625
                    }
                    dataw = {
                        type = 'text'
                        fill = "#3333FF"
                        font = " 11px Arial"
                        text = getStringValue(mmax, 3)
                        x = 95
                        y = 625
                    }
                    (0..9).each { long k ->
                        def v = histMap[k]
                        if (v != null) {
                            def h = v.total * 400 / dataSize
                            if (h > 0 && h < 1) h = 1
                            data = {
                                type = 'rect'
                                fill = getColor(k, 0, 9)
                                name = "Total: " + v.total + " (" + getStringValue(100 * v.total / retMap.size(), 2) + "%)<br/>Range: " + getStringValue(v.min, 3) + " to " + getStringValue(v.max, 3)
                                x = 10 + k * 10
                                y = 616 - h
                                width = 10
                                height = h
                            }
                        }
                    }
                }
            }

            render(results)
        }
    }

    def getChart = {

        def specs = [
                "waferVoltageSweepChart": [min1: 0.09, max1: 6.09],
                "waferCurrentSweepChart": [min1: 444444.0, max1: 0],
                "waferSpectrumChart"    : [min1: 350.01, max1: 650.01]
        ]
        def min2 = 1E12, max2 = 0, shift = 0

        def db = mongo.getDB("glo")
        def str = params.units.toString().replace("\\", "")
        def units = JSON.parse(str)
        def ids = units?.collect { getCode(params.code) + "_" + it }

        def g = new BasicDBObject()
        g.put('$in', ids)
        def query = new BasicDBObject("value.code", g)
        query.put("value.testId", params.testId.toLong())
        def data = []

        if (params.chartType == 'waferCurrentSweepChart') {

            def fields = new BasicDBObject("value.data", 1)
            fields.put("value.code", 1)

            def query2 = new BasicDBObject("value.code",  getCode(params.code))
            query2.put("value.testId", params.testId.toLong())
            query2.put("value.tkey", params.tkey)

            def rawData = db.testData.find(query2, fields).collect { it["value"]["data"] }

            def points = [:]
            rawData[0].each { k, v ->
                if (k != "setting") {

                    def curr = k.tokenize('@')[1]
                    if (curr) {
                        Integer currVal = Integer.parseInt(curr.replaceAll("\\D+", ""))
                        Float currVal2 = currVal
                        if (curr.indexOf('mA') > 0) currVal2 = currVal * 1E-3
                        if (curr.indexOf('uA') > 0) currVal2 = currVal * 1E-6
                        if (curr.indexOf('nA') > 0) currVal2 = currVal * 1E-9
                        if (curr.indexOf('pA') > 0) currVal2 = currVal * 1E-12
                        if (curr.indexOf('fA') > 0) currVal2 = currVal * 1E-15

                        if (currVal2 < specs.waferCurrentSweepChart.min1)
                            specs.waferCurrentSweepChart.min1 = currVal2
                        if (currVal2 > specs.waferCurrentSweepChart.max1) {
                            specs.waferCurrentSweepChart.max1 = currVal2
                        }

                        v[params.level].each { k1, v1 ->
                            def code =  getCode(params.code) + "_" + k1
                            if (units.contains(k1)) {
                                if (!points.containsKey(code)) {
                                    points.put(code, [])
                                }
                                if (v1 < min2)
                                    min2 = v1
                                if (v1 > max2) {
                                    max2 = v1
                                }
                                points[code].add([currVal2, v1])
                            }
                        }
                    }
                }
            }

            points.each { k, v ->
                data.add([code: k, first: "Current (A)", second: params.level, data: v.sort { it[0] }])
            }

            rawData = null

        } else if (params.chartType == 'waferOtherChart') {

            def fields = new BasicDBObject("value.data." + params.level, 1)
            fields.put("value.code", 1)

            def query2 = new BasicDBObject("value.code",  getCode(params.code))
            query2.put("value.testId", params.testId.toLong())
            query2.put("value.tkey", params.tkey)

            def rawData = db.testData.find(query2, fields).collect { it["value"]["data"][params.level] }

            def points = []

            units.each {

                if (rawData[params.levelX] && rawData[params.levelX][0] && rawData[params.levelX][0][it]) {

                    def mp = [:]
                    mp.put('code', it)
                    mp.put('x', rawData[params.levelX][0][it].round(3))
                    mp.put('y', rawData[params.levelY][0][it].round(3))
                    points.add(mp)
                }
            }

            data.add([xTitle: params.levelX, yTitle: params.levelY, data: points])

            rawData = null

            render(data as JSON)
            return

        } else {
            def fields = new BasicDBObject("value.data." + params.level, 1)
            fields.put("value.code", 1)

            data = db.testData.find(query, fields).collect {

                def points2 = it["value"]["data"][params.level]

                def point2 = []
                if (points2 != null) {
                    points2["data"].each { point ->
                        if (point[1] > 0 && point[0] > specs[params.chartType].min1 && point[0] <= specs[params.chartType].max1) {
                            if (point[1] < min2)
                                min2 = point[1]
                            if (point[1] > max2)
                                max2 = point[1]
                            point2.add(point)
                        }
                    }
                    [code: it["value"]["code"], first: points2["first"], second: points2["second"], data: point2]
                }
            }
        }

        if (params.logharitmic == "true") {

            if (params.chartType == 'waferVoltageSweepChart') {
                min2 = 1E-7
                max2 = 10
            }

            if (min2 > 0) {
                min2 = Math.log10(min2)
            }
            if (max2 > 0) {
                max2 = Math.log10(max2)
            }

            shift -= min2
            max2 = max2 + shift
            min2 = 0
        } else {
            if (params.chartType == 'waferVoltageSweepChart') {
//                min2 = 0
//               max2 = 10
            }
        }

        def fields2 = new BasicDBObject()
        fields2.put('value.epi_growth.runNumber', 1)
        fields2.put('value.experimentId', 1)
        def identifier = db.dataReport.find(new BasicDBObject("code",  getCode(params.code)), fields2).collect { it }[0]

        def builder = new JSONBuilder()
        def total = ids.size()

        def textLevel = "Level: " + params.level
        if (params.chartType  == 'waferVoltageSweepChart')
            textLevel = "X axis - Voltage [V]"
        if (params.chartType  == 'waferCurrentSweepChart')
            textLevel = "X axis - Current [A]"

        def results = builder.build {
            graphs = array {
                dataw = {
                    type = 'text'
                    fill = "#111111"
                    font = "bold 13px Arial"
                    text = "Serial#: " +  getCode(params.code)
                    x = 143
                    y = 8
                }
                dataw = {
                    type = 'text'
                    fill = "#111111"
                    font = "bold 13px Arial"
                    text = "Exp#: " + identifier.value?.experimentId
                    x = 290
                    y = 8
                }
                dataw = {
                    type = 'text'
                    fill = "#111111"
                    font = "bold 13px Arial"
                    text = "Run#: " + identifier.value?.epi_growth?.runNumber
                    x = 403
                    y = 8
                }
                dataw = {
                    type = 'text'
                    fill = "#111111"
                    font = "bold 13px Arial"
                    text = textLevel
                    x = 533
                    y = 8
                }
                dataw = {
                    type = 'text'
                    fill = "#000000"
                    font = "bold 13px Arial"
                    text = data[0]?.second ?: data[1]?.second
                    x = 7
                    y = 8
                }
                dataw = {
                    type = 'text'
                    fill = "#000000"
                    font = " 11px Arial"
                    text = data[0]?.first ?: data[1]?.first
                    x = 645
                    y = 643
                }
                data.eachWithIndex { item, idx ->
                    if (item != null) {
                        dataw = {
                            type = 'path'
                            name = item.code
                            stroke = getColor(idx, 1, total)
                            path = convertPointsToPath(item, params.logharitmic, specs[params.chartType].min1, specs[params.chartType].max1, min2, max2, shift, "D")
                        }
                    }
                }
                dataw = {
                    type = 'path'
                    stroke = "#AAAAAA"
                    path = convertPointsToPath(data[0] ?: data[1], params.logharitmic, specs[params.chartType].min1, specs[params.chartType].max1, min2, max2, shift, "X")
                }
                dataw = {
                    type = 'path'
                    stroke = "#AAAAAA"
                    opacity = 2
                    path = convertPointsToPath(data[0] ?: data[1], params.logharitmic, specs[params.chartType].min1, specs[params.chartType].max1, min2, max2, shift, "Y")
                }
                xText.each { k, v ->
                    dataw = {
                        type = 'text'
                        fill = "#444444"
                        font = "12px Arial"
                        text = getStringValue(k, 3)
                        x = v - 10
                        y = 628
                    }
                }
                yText.each { k, v ->
                    dataw = {
                        type = 'text'
                        fill = "#444444"
                        font = "12px Arial"
                        text = getStringValue(k, 2)
                        x = 3
                        y = v
                    }
                }
            }
        }

        data = null


        render(results)
    }

    def export = {

        def codes = params.codes.tokenize(",")

        def db = mongo.getDB("glo")
        def queryTest = new BasicDBObject("value.code",  getCode(params.code))
        queryTest.put("value.testId", params.testId.toLong())
        queryTest.put("value.tkey", params.tkey)
        def testData = db.testData.find(queryTest, new BasicDBObject()).collect { it }[0]
        def obj = testData["value"]
        def allCurrents = []
        def allVolts = []
        def allPeakEqe = []
        def allPeakEqeCurrent = []
        if (params.level1 == "ALL") {
            obj["data"].each { k, v ->
                if (!(k in ["setting", "Current @ 2V", "peakEqe", "peakEqeCurrent"]))
                    allCurrents.add(k)
                if (k in ["Current @ 2V"])
                    allVolts.add(k)
                if (k in ["peakEqe"])
                    allPeakEqe.add(k)
                if (k in ["peakEqeCurrent"])
                    allPeakEqeCurrent.add(k)
            }
        } else {
            if (params.level1 in ["Current @ 2V"])
                allVolts.add(params.level1)
            else if (params.level1 in ["peakEqe"])
                allPeakEqe.add(params.level1)
            else if (params.level1 in ["peakEqeCurrent"])
                allPeakEqeCurrent.add(params.level1)
            else {
                allCurrents.add(params.level1)
            }
        }


        def map = [:]
        def columns = []
        allCurrents.each {
            def data = (BasicDBObject) obj["data"][it]
            data.each { k, v ->
                if (!columns.contains(k)) {
                    columns.add(k)
                }
                v.each { k2, v2 ->
                    if (codes.size() == 0 || (codes.size() > 0 && codes.contains(k2))) {
                        def k3 = it + "__" + k2 + "__" + obj["code"] + "__" + obj["experimentId"]
                        if (!map.containsKey(k3)) {
                            map.put(k3, [:])
                        }
                        map[k3].put(k, v2)
                    }
                }
            }
        }

        def mapV = [:]
        def columnsV = []
        allVolts.each {
            def data = (BasicDBObject) obj["data"][it]
            data.each { k, v ->
                if (!columnsV.contains(k)) {
                    columnsV.add(k)
                }
                v.each { k2, v2 ->

                    if (!mapV.containsKey(k2)) {
                        mapV.put(k2, v2)
                    }
                }
            }
        }

        def mapPE = [:]
        def columnsPE = []
        allPeakEqe.each {
            def data = (BasicDBObject) obj["data"][it]
            data.each { k, v ->
                if (!columnsPE.contains(k)) {
                    columnsPE.add(k)
                }
                v.each { k2, v2 ->

                    if (!mapPE.containsKey(k2)) {
                        mapPE.put(k2, v2)
                    }
                }
            }
        }

        def mapPEC = [:]
        def columnsPEC = []
        allPeakEqeCurrent.each {
            def data = (BasicDBObject) obj["data"][it]
            data.each { k, v ->
                if (!columnsPEC.contains(k)) {
                    columnsPEC.add(k)
                }
                v.each { k2, v2 ->

                    if (!mapPEC.containsKey(k2)) {
                        mapPEC.put(k2, v2)
                    }
                }
            }
        }

        // Find unit mask
        def maskName = db.unit.find(new BasicDBObject("code",  getCode(params.code)), new BasicDBObject("mask", 1)).collect {
            it["mask"]
        }[0]
        def productMask = ProductMask.findByName(maskName)
        if (productMask) {
            columns.add("plX")
            columns.add("plY")
            columns.add("sizeX")
            columns.add("sizeY")
            columns.add("pcm")
        }

        XSSFWorkbook workbook = new XSSFWorkbook()
        XSSFSheet sheet = workbook.createSheet(params.level1)

        // Add header to excel
        XSSFRow rowHeader = sheet.createRow(0)
        def h = 4
        XSSFCell cellHead = rowHeader.createCell((int) 0)
        cellHead.setCellValue("Current")
        cellHead = rowHeader.createCell((int) 1)
        cellHead.setCellValue("Serial#")
        cellHead = rowHeader.createCell((int) 2)
        cellHead.setCellValue("Exp#")
        cellHead = rowHeader.createCell((int) 3)
        cellHead.setCellValue("date")

        columns.each {

            cellHead = rowHeader.createCell((int) h)

            if (it == "NA") {
                cellHead.setCellValue(new XSSFRichTextString("value [mA]"))
            } else {
                cellHead.setCellValue(new XSSFRichTextString(it))
            }
            h++
        }


        if (mapV) {

            XSSFSheet sheetC2V = workbook.createSheet("Current at 2V")
            def r3 = 0

            mapV.each { k, v ->

                XSSFRow rowData3 = sheetC2V.createRow(r3)
                def cellData3 = rowData3.createCell(0)
                cellData3.setCellValue(k)
                cellData3 = rowData3.createCell(1)
                cellData3.setCellValue(v)
                r3++
            }
        }

        if (mapPE) {

            XSSFSheet sheetC2V = workbook.createSheet("Peak EQEs")
            def r3 = 0

            mapPE.each { k, v ->

                XSSFRow rowData3 = sheetC2V.createRow(r3)
                def cellData3 = rowData3.createCell(0)
                cellData3.setCellValue(k)
                cellData3 = rowData3.createCell(1)
                cellData3.setCellValue(v)
                r3++
            }
        }

        if (mapPEC) {

            XSSFSheet sheetC2V = workbook.createSheet("Peak EQE currents")
            def r3 = 0

            mapPEC.each { k, v ->

                XSSFRow rowData3 = sheetC2V.createRow(r3)
                def cellData3 = rowData3.createCell(0)
                cellData3.setCellValue(k)
                cellData3 = rowData3.createCell(1)
                cellData3.setCellValue(v)
                r3++
            }
        }

        def pcms = new HashSet()
        def r = 1

        XSSFCellStyle style = workbook.createCellStyle()
        XSSFCreationHelper createHelper = workbook.getCreationHelper();
        style.setDataFormat(createHelper.createDataFormat().getFormat("MM/dd/yyyy hh:mm"))
        map.each { k, v ->
            XSSFRow rowData = sheet.createRow(r)
            def c = 0
            XSSFCell cellData = rowData.createCell(c)
            def tok = k.tokenize("__")
            cellData.setCellValue(tok[0])
            c++
            cellData = rowData.createCell(c)
            cellData.setCellValue(tok[2] + '_' + tok[1])
            c++
            cellData = rowData.createCell(c)
            cellData.setCellValue(tok[3])
            c++
            cellData = rowData.createCell(c)
            cellData.setCellValue(obj.date)
            cellData.setCellStyle(style)
            v.each { k2, v2 ->
                c++
                cellData = rowData.createCell((int) c)
                // keep creating cells until in appropriate column
                while (c - 4 < columns.size() && k2 != columns[c - 4]) {
                    c++
                    cellData = rowData.createCell((int) c)
                }
                if (v2.getClass() == java.lang.Double) {
                    v2 = v2.round(14)
                }
                cellData.setCellValue(v2)
            }


            def maskd
            def pmi = ProductMaskItem.executeQuery("""
				select v from ProductMaskItem as v where v.productMask = ? and v.code = ?
				""", [productMask, tok[1]])
            if (pmi) {
                pcms.add( getCode(params.code) + '_' + pmi[0].code)
            }

            if (maskd) {
                c++
                cellData = rowData.createCell((int) c)
                // keep creating cells until in appropriate column
                while (c - 3 < columns.size() && "plX" != columns[c - 3]) {
                    c++
                    cellData = rowData.createCell((int) c)
                }
                cellData.setCellValue(maskd.plX)
                c++
                cellData = rowData.createCell((int) c)
                cellData.setCellValue(maskd.plY)
                c++
                cellData = rowData.createCell((int) c)
                cellData.setCellValue(maskd.sizeX)
                c++
                cellData = rowData.createCell((int) c)
                cellData.setCellValue(maskd.sizeY)
                c++
                cellData = rowData.createCell((int) c)
                cellData.setCellValue(maskd.pm)
            }

            r++
        }

        for (def i = 0; i < h; i++) {
            sheet.autoSizeColumn(i)
        }

        if (pcms) {

            XSSFSheet sheetPcm = workbook.createSheet("PCM voltage sweeps")
            def r2 = 0

            pcms.each { pcm ->

                def queryTest2 = new BasicDBObject("value.code", pcm)
                queryTest2.put("value.testId", params.testId.toLong())
                queryTest2.put("value.tkey", params.tkey)

                db.testData.find(queryTest2, new BasicDBObject()).collect {

                    def viSweep = it?.value?.data?.Datavoltage?.data

                    viSweep?.each { vi ->

                        XSSFRow rowData2 = sheetPcm.createRow(r2)
                        def cellData2 = rowData2.createCell(0)
                        cellData2.setCellValue(pcm)
                        cellData2 = rowData2.createCell(1)
                        cellData2.setCellValue(vi[0])
                        cellData2 = rowData2.createCell(2)
                        cellData2.setCellValue(vi[1])
                        r2++
                    }
                }
            }
        }

        response.setHeader("Content-disposition", "attachment; filename=data.xlsx")
        response.contentType = "application/excel"
        ServletOutputStream f = response.getOutputStream()
        workbook.write(f)
        f.close()
    }

    def convertPointsToPath(def item, def isLogharitmic, def min1, def max1, def min2, def max2, def shift, def type) {

        if (item == null) return ""

        def ret = new StringBuilder()

        def x = { val ->
            def diff = max1 - min1
            if (diff == 0)
                0
            else
                (int) ((690 * val / diff) + 50 - (690 * min1 / diff))
        }

        def y = { val ->

            def diff = max2 - min2
            if (diff == 0)
                0
            else {
                if (isLogharitmic == "true") {
                    if (val > 0) {
                        val = Math.log10(val)
                        val = val + shift
                    }
                }
                (int) (620 - 600 * (val - min2) / diff)
            }
        }

        def ticks = { val1, val2, axis ->

            def d1 = val1.getClass() == BigDecimal ? val1 : val1.toString().toBigDecimal()
            def d2 = val2.getClass() == BigDecimal ? val2 : val2.toString().toBigDecimal()
            def arr = []
            int num = Math.round(d2 - d1)
            if (d2 > 32000) {
                d2 = 32000
                num = Math.round(d2 - d1)
            }
            int inc = 1
            if (num == 0) {
                def scale = (d2 - d1) / 10
                if (scale > 0) {
                    for (def d = d1; d <= d2; d = d + scale) {
                        arr.add(d)
                    }
                } else {
                    arr.add(d1)
                }
            } else {
                if (isLogharitmic == "true" && axis == "Y") {
                    def m1 = Math.round(d1 - shift)
                    def m2 = Math.round(d2 - shift)
                    for (def m = m1; m <= m2; m++) {
                        arr.add(Math.pow(10, m))
                    }
                } else {
                    if (num >= 0 && num <= 15) inc = 1
                    if (num > 15 && num <= 70) inc = 5
                    if (num > 70 && num <= 120) inc = 10
                    if (num > 120 && num <= 240) inc = 20
                    if (num > 240 && num <= 700) inc = 50
                    if (num > 700 && num <= 1200) inc = 100
                    if (num > 1200 && num <= 2400) inc = 200
                    if (num > 2400) inc = num / 10
                    if (inc > 0) {
                        for (int i = Math.round(d1); i <= Math.round(d2); i = i + inc) {
                            arr.add(i)
                        }
                    } else {
                        arr.add(d1)
                    }
                }
            }

            arr
        }

        if (type == "D") {
            if (item.data) {
                ret.append("M" + x(item.data[0][0]) + " " + y(item.data[0][1]))
            }
            item.data.eachWithIndex { it, idx ->
                //		if (idx%2 == 0) {
                ret.append(" L" + x(it[0]) + " " + y(it[1]))
                //		}
            }
        }
        if (type == "X") {
            xText.clear()
            ticks(min1, max1, "X").each {
                def calc = x(it)
                ret.append("M" + calc + " 20 L" + calc + " 623 Z")
                xText.put(it, calc)
            }
        }
        if (type == "Y") {
            yText.clear()
            ticks(min2, max2, "Y").each {
                def calc = y(it)
                ret.append("M40 " + calc + " L 730 " + calc + " Z")
                yText.put(it, calc)
            }
        }

        ret.toString()
    }


    def private getData(code, tkey, propertyName, testId) {

        def db = mongo.getDB("glo")
        def queryTest = new BasicDBObject("value.code", code)
        if (params["testId"]?.isLong())
            queryTest.put("value.testId", testId.toLong())
        else {
            testId = -1
            logr.warn( getCode(params.code) + " " + params.tkey + " has no testId defined")
        }
        queryTest.put("value.tkey", tkey)
        def testData = db.testData.find(queryTest, new BasicDBObject()).collect { it }[0]
        if (testData && testData["value"] && testData["value"]["data"]) {
            testData["value"]["data"]
        } else {
            ""
        }
    }

    def private getStringValue(def val, def decimals) {
        if (!val) {
            return "0"
        } else if (val.getClass() == java.lang.Double
                || val.getClass() == java.lang.Float || val.getClass() == BigDecimal) {
            def ret
            if (Math.abs(val) < 0.01 || Math.abs(val) > 999) {
                ret = new DecimalFormat("0.0E0").format(val)
            } else {
                if (decimals == 3)
                    ret = new DecimalFormat("0.000").format(val)
                else
                    ret = new DecimalFormat("0.00").format(val)
            }

            return ret
        } else {
            return val
        }
    }

    def private getColor(def value, def mn, def mx) {

        def ret = "#111111"
        if (value != null) {
            def diff = mx - mn
            if (diff == 0) {
                diff = 1
                value = 1
            }
            if (value == 0) {
                value = mn
            }
            double intValue = 900 * (value - mn) / diff
            if (intValue < 100) {
                ret = "#00008B"
            } else if (intValue < 200) {
                ret = "#4876FF"
            } else if (intValue < 300) {
                ret = "#00CED1"
            } else if (intValue < 400) {
                ret = "#006400"
            } else if (intValue < 500) {
                ret = "#00CD00"
            } else if (intValue < 600) {
                ret = "#CDC673"
            } else if (intValue < 700) {
                ret = "#CDAD00"
            } else if (intValue < 800) {
                ret = "#FF7F24"
            } else if (intValue < 900) {
                ret = "#EE4000"
            } else if (intValue < 1020) {
                ret = "#B22222"
            }
        }

        ret
    }

    def private toHex(def value) {
        def str = Integer.toHexString(value)
        if (str.length() == 1)
            str = "0" + str
        str
    }

    def private calcX(def maskDefItem) {
        def sz = maskDefItem.sizeX * 12
        sz = sz > 21 ? 21 : sz
        (int) (420 + maskDefItem.plX * 12 - sz / 2)
    }

    def private calcY(def maskDefItem) {
        def sz = maskDefItem.sizeY * 12
        sz = sz > 21 ? 21 : sz
        (int) (307 - maskDefItem.plY * 12 + sz / 2)
    }

    def private calcW(def maskDefItem) {
        def sz = maskDefItem.sizeX * 12
        if (sz < 3) sz = 6
        (int) (sz > 21 ? 21 : sz)
    }

    def private calcH(def maskDefItem) {
        def sz = maskDefItem.sizeY * 12
        if (sz < 3) sz = 6
        (int) (sz > 21 ? 21 : sz)
    }

    def getMaskHash(def productMask, def code, def maskDef) {
        def pmi = ProductMask.executeQuery("""
				select v from ProductMaskItem as v where v.productMask = ? and v.code = ?
				""", [productMask, code])
        if (pmi)
            maskDef.put(pmi[0].code, pmi[0])
    }
}
