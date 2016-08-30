package com.glo.custom

import com.glo.ndo.Spc
import com.glo.ndo.SpcVariable
import com.glo.ndo.Variable
import com.glo.run.Note
import com.glo.run.Unit
import com.mongodb.BasicDBObject
import grails.converters.JSON
import grails.web.JSONBuilder
import org.apache.commons.logging.LogFactory
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics
import java.text.DateFormat
import java.text.DecimalFormat
import java.text.SimpleDateFormat

class SpcController extends com.glo.run.Rest {

    private static final logr = LogFactory.getLog(this)

    def springSecurityService
    def spcService
    def historyService
    def grailsApplication

    def mongo

    def list = {

        def username = ""
        try {
             username = springSecurityService.principal?.username
        } catch (Exception exc) {

        }
        def spcs = spcService.list(username, params)
        render(['data': spcs[0], 'count': spcs[1]] as JSON)
    }

    def get = {

        def spc = Spc.get(params.id)
        if (spc.sampleSize == null || spc.sampleSize == 0)
            spc.sampleSize = 100
        render(['data': spc, 'count': 1] as JSON)
    }

    def save = {
        try {
            def username = springSecurityService.principal?.username
            def spc = params
            spcService.save(username, spc)
            render([ok: true, success: true] as JSON)
        }
        catch (Exception exc) {
            logr.error(exc)
            render([ok: false, success: false, msg: exc.toString()] as JSON)
        }
    }

    def duplicate = {
        try {
            def username = springSecurityService.principal?.username
            spcService.duplicate(username, params.id)
            render([ok: true, success: true] as JSON)
        }
        catch (Exception exc) {
            logr.error(exc)
            render([ok: false, success: false, msg: exc.toString()] as JSON)
        }
    }

    def delete = {

        try {
            def username = springSecurityService.principal?.username
            def spc = Spc.get(params.id)

            if (username != "admin" && spc.owner != username) {
                throw new RuntimeException("Only owner can delete report")
            }

            spc.delete([cascade: true])

            render([success: true] as JSON)
        }
        catch (Exception exc) {
            logr.error(exc)
            render([success: false, msg: exc.toString()] as JSON)
        }
    }

    def getFields = {

        def builder = new JSONBuilder()
        def results = builder.build {
            fields = array {
                var0 = {
                    name = "id"
                    type = "int"
                }
                var1 = {
                    name = "name"
                    type = "string"
                }
                var3 = {
                    name = "tag"
                    type = "string"
                }
                var4 = {
                    name = "isPublic"
                    type = "boolean"
                }
                var5 = {
                    name = "owner"
                    type = "string"
                }
                var6 = {
                    name = "description"
                    type = "string"
                }
                var7 = {
                    name = "sampleSize"
                    type = "int"
                }
                var8 = {
                    name = "filter"
                    type = "string"
                }
                var9 = {
                    name = "showText"
                    type = "boolean"
                }
            }
            columns = array {
                var8 = {
                    dataIndex = "name"
                    id = "spc6name"
                    text = "Name"
                    flex = 5
                }
                var10 = {
                    dataIndex = "tag"
                    id = "spc6tag"
                    text = "Tag"
                    flex = 1
                }
                var11 = {
                    dataIndex = "isPublic"
                    id = "spc6ispub"
                    text = "Public"
                    flex = 1
                }
                var12 = {
                    dataIndex = "owner"
                    id = "spc6owner"
                    text = "Owner"
                    flex = 1
                }
            }
        }
        render(results)
    }

    def attachVariable = {
        try {
            def username = springSecurityService.principal?.username
            def spcv = new SpcVariable()
            spcv.spc = Spc.get(params.spcId.toLong())

            def nextSortOrder = 10
            if (spcv.spc.spcVariables) {
                nextSortOrder = (spcv.spc.spcVariables[spcv.spc.spcVariables.size() - 1].sortOrder ?: 0) + 10
            }

            if (username != "admin" && spcv.spc.owner != username) {
                throw new RuntimeException("Only owner can delete report")
            }


            spcv.variable = Variable.get(params.variableId.toLong())
            if (spcv.variable.process) {
                spcv.path = spcv.variable.process.pkey + '.' + spcv.variable.name
            }
            if (spcv.variable.processStep) {
                spcv.path = spcv.variable.processStep.process.pkey + '.' + spcv.variable.processStep.taskKey + "." + spcv.variable.name
            }
            spcv.idx = 1
            spcv.title = spcv.variable.title ?: spcv.variable.name
            spcv.sortOrder = nextSortOrder
            spcv.save(failOnError: true)

            render([ok: true, success: true] as JSON)
        }
        catch (Exception exc) {
            logr.error(exc)
            render([ok: false, success: false, msg: exc.toString()] as JSON)
        }
    }

    def removeVariable = {
        try {
            def username = springSecurityService.principal?.username

            def var = SpcVariable.get(params.spcVarId.toLong())

            if (username != "admin" && var.spc.owner != username) {
                throw new RuntimeException("Only owner can delete report variable")
            }

            if (var) {
                var.delete([cascade: true, flush: true])
            }

            render([ok: true, success: true, name: var.path] as JSON)
        }
        catch (Exception exc) {
            logr.error(exc)
            render([ok: false, success: false, msg: exc.toString()] as JSON)
        }
    }

    def addComment = {
        try {
            def username = springSecurityService.principal?.username

            def str = params.units.toString().replace("\\", "")
            def units = JSON.parse(str)

            if (params.note) {
                units.each {
                    Unit unit = Unit.findByCode(it.code)
                    def stepName = it.path.tokenize("/")[1]?.trim()
                    def note = new Note(userName: username, comment: "'" + it.variable + "': " + params.note, stepName: stepName)
                    unit.addToNotes(note)
                    unit.nNotes = unit.notes?.size()
                    unit.save(flush: true, failOnError: true)
                    historyService.initHistory("addNote", null, unit.dbo, null)
                }
            }
            render([ok: true, success: true] as JSON)
        }
        catch (Exception exc) {
            logr.error(exc)
            render([ok: false, success: false, msg: exc.toString()] as JSON)
        }
    }

    def editVariable = {

        def spcVar = SpcVariable.get(params.spcVarId)
        def var = spcVar?.variable
        if (var) {
            var.graphLimit = params.graphLimit
            var.save()
        }

        if (spcVar) {
            spcVar.products = params.productCodes
            spcVar.filters = params.customFilter
            spcVar.sortOrder = params.sortOrder.toString().isNumber() ? params.sortOrder?.trim()?.toInteger() : null
            spcVar.save()
        }

        render([ok: true, success: true] as JSON)
    }

    def getVariable = {

        def ret = [:]
        def spcVar = SpcVariable.get(params.spcVarId)
        if (spcVar) {
            ret.products = spcVar.products
            ret.filters = spcVar.filters
            ret.sortOrder = spcVar.sortOrder
            ret.limits = spcVar.variable?.graphLimit
        }

        render([ok: true, success: true] + ret as JSON)
    }

    def getChart = {

        def graphWidth = (params.width || params.width < 100) ? Integer.parseInt(params.width - 30) : 900
        if (graphWidth > 1200) graphWidth = 1200
        def graphHeight = params.height ? Integer.parseInt(params.height) : 270

        graphWidth -= 15

        def spc = Spc.get(params.spcId.toLong())

        def db = mongo.getDB("glo")

        // Load chart data from cache if not refresh and exists
        def report = db.spcReport.find(new BasicDBObject("spcId", spc.id))?.collect { it }[0]
        if (report && params.refresh == "false") {
            render(report.data)
            return
        }

        int sampleSize = spc.sampleSize ?: 100

        if (!spc) {
            db.spcReport.remove([spcId: params.spcId.toLong()])
            render([success: false, msg: "No SPC defined for '" + params.spcId + "'"] as JSON)
            return
        }
        if (!spc.spcVariables) {
            db.spcReport.remove([spcId: params.spcId.toLong()])
            render([success: false, msg: "No SPC variables defined for '" + params.spcId + "'"] as JSON)
            return
        }

        def samples = []

        // Group variables by process
        spc.spcVariables.each {


            def path = it.path.tokenize('.')
            def prods = it.products?.tokenize(',')
            def filters = it.filters
            def sortOrder = it.sortOrder
            def proc = path[0]
            def procStep = ""
            def var = ""
            def expId = ""
            def runNumber = ""
            def slim = it.variable.graphLimit ?: it.variable.specLimit
            boolean isSpecLimit = false
            def specLimit = slim?.tokenize(",")
            if (!specLimit || specLimit.size() != 2 || !specLimit[0].isFloat() || !specLimit[1].isFloat())
                specLimit = [0, 0]
            else {
                specLimit[0] = specLimit[0].toFloat()
                specLimit[1] = specLimit[1].toFloat()
                isSpecLimit = true
            }
            if (path.size() == 2) {
                var = path[1]
                path = "value."
                expId = "value.experimentId"
                runNumber = "value.runNumber"
            }
            if (path.size() == 3) {
                procStep = path[1]
                var = path[2]
                path = "value." + path[1] + "."
                expId = "experimentId"
                runNumber = "runNumber"
            }

            def spcObj = new Expando()
            spcObj.id = it.id
            spcObj.title = it.title
            spcObj.slim = slim ?: ""
            spcObj.filters = filters ?: ""
            spcObj.products = it.products ?: ""
            spcObj.sortOrder = it.sortOrder ?: ""

            if (it.variable.process?.category == "EquipmentDC") {
                spcObj.path = proc
                spcObj.samples = getEpiSamples(db, sampleSize, spc.filter, proc, var, filters)
            } else if (proc == 'camera') {
                spcObj.path = proc
                spcObj.samples = getCameraSamples(db, sampleSize, spc.filter, proc, var, filters)
            } else if (proc == "measures" && spc.filter) {
                spcObj.path = proc
                spcObj.samples = getMeasureSamples(db, sampleSize, spc.filter, var, filters)
            } else if (!spcObj.samples) {
                spcObj.path = (procStep ? proc + " / " + procStep : proc)
                def ctg = it.variable.process?.category ?: it.variable.processStep?.process?.category
                spcObj.samples = getSamples(db, sampleSize, spc.filter, proc, path, var, expId, runNumber, prods, filters, ctg)
            }

            DescriptiveStatistics stats = new DescriptiveStatistics((double[]) spcObj.samples.collect { it[1] })

            def statsMean = stats.getMean()
            if (spcObj.mean == 0)
                spcObj.mean = (double) statsMean

            spcObj.stdDev = (double) stats.getStandardDeviation()


            spcObj.ucl3 = statsMean + 3 * spcObj.stdDev
            spcObj.lcl3 = statsMean - 3 * spcObj.stdDev

            if (specLimit[1] == 0 && specLimit[0] == 0) {
                specLimit[0] = spcObj.lcl3
                specLimit[1] = spcObj.ucl3
            }

            spcObj.mean = (double) (specLimit[1] + specLimit[0]) / 2

            spcObj.usl3 = specLimit[1]
            spcObj.lsl3 = specLimit[0]

            if (spcObj.mean != 0) {
                spcObj.usl3p = 100 * (spcObj.usl3 - spcObj.mean) / spcObj.mean
                spcObj.lsl3p = 100 * (spcObj.lsl3 - spcObj.mean) / spcObj.mean
            } else {
                spcObj.usl3p = 0
                spcObj.lsl3p = 0
            }

            spcObj.max = specLimit[1] + (specLimit[1] - spcObj.mean) * 0.8
            spcObj.min = specLimit[0] - (spcObj.mean - specLimit[0]) * 0.8

            samples.add(spcObj)
        }

        def yy = 10
        def xx = 0
        def builder = new JSONBuilder()
        def results = builder.build {
            minHeight = (graphHeight + 28) * samples.size() + 80
            charts = array {
                samples.each { sample ->
                    data = {
                        type = 'text'
                        name = 'charttitle'
                        text = sample.path + " / " + sample.title
                        font = '11px Verdana bold'
                        x = 38
                        y = yy - 4
                    }
                //    if (params.isPrint) {
                        data = {
                            type = 'image'
                            name = 'delete'
                            code = sample.id
                            src = grailsApplication.config.grails.serverURL + "/js/ext/resources/icons/delete.png"
                            x = 3
                            y = yy - 6
                            width = 16
                            height = 16
                        }
                        data = {
                            type = 'image'
                            name = 'editSpecLimit'
                            titleText = sample.path + " / " + sample.title
                            limits = sample.slim
                            sortOrder = sample.sortOrder
                            filters = sample.filters
                            products = sample.products
                            code = sample.id
                            src = grailsApplication.config.grails.serverURL + "/js/ext/resources/icons/report.png"
                            x = 3
                            y = yy + graphHeight - 15
                            width = 16
                            height = 16
                        }
                 //   }
                    data = {
                        type = 'rect'
                        fill = "#CCCCCC"
                        name = "Total: "
                        x = 38
                        y = yy + 8
                        width = graphWidth - 39
                        height = graphHeight - 5
                    }
                    data = {
                        type = 'rect'
                        fill = sample.slim ? "#FCB3B4" : "#C3FCA2"
                        name = "Total: "
                        x = 39
                        y = yy + 4
                        width = graphWidth - 36
                        height = graphHeight - 2
                    }
                    dataw = {
                        type = 'text'
                        font = '9px Tahoma'
                        text = getStringValue(sample.usl3, 3)
                        x = 2
                        y = yy + graphHeight - yPos(graphHeight, sample.usl3, sample.min, sample.max)
                    }
                    dataw = {
                        type = 'text'
                        font = '8px Verdana'
                        text = getStringValue(sample.usl3p, 2) + '%'
                        x = 2
                        y = yy + graphHeight - yPos(graphHeight, sample.usl3, sample.min, sample.max) + 10
                    }
                    dataw = {
                        type = 'text'
                        font = '8px Verdana'
                        text = getStringValue(sample.lsl3, 3)
                        x = 2
                        y = yy + graphHeight - yPos(graphHeight, sample.lsl3, sample.min, sample.max)
                    }
                    dataw = {
                        type = 'text'
                        font = '8px Verdana'
                        text = getStringValue(sample.lsl3p, 2) + '%'
                        x = 2
                        y = yy + graphHeight - yPos(graphHeight, sample.lsl3, sample.min, sample.max) - 10
                    }
                    data = {
                        type = 'rect'
                        fill = "#B1FCA2"
                        name = "Total: "
                        x = 38
                        y = yy + graphHeight - yPos(graphHeight, sample.usl3, sample.min, sample.max)
                        width = graphWidth - 35
                        height = (yPos(graphHeight, sample.usl3, sample.min, sample.max) - yPos(graphHeight, sample.lsl3, sample.min, sample.max))
                    }
                    dataw = {
                        type = 'text'
                        font = '8px Verdana'
                        text = getStringValue(sample.mean, 3)
                        x = 2
                        y = yy + graphHeight - yPos(graphHeight, sample.mean, sample.min, sample.max)
                    }
                    data = {
                        type = 'rect'
                        fill = "#000000"
                        name = "meanLine"
                        x = 36
                        y = yy + graphHeight - yPos(graphHeight, sample.mean, sample.min, sample.max)
                        width = graphWidth - 35
                        height = 1
                    }

                    int skip = sampleSize / 70
                    if (skip == 0) skip = 1
                    int counter = 0

                    int samsize = sampleSize
                    if (sample.samples && sample.samples.size() < sampleSize ) {
                        samsize = sample.samples.size()
                    }

                    sample.samples.each { k ->
                        data = {
                            type = 'circle'
                            fill = (sample.slim ? ((k[1] < sample.usl3 && k[1] > sample.lsl3) ? "#024DED" : "#FF2222") : "#024DED")
                            code = k[0]
                            p = sample.path
                            text = sample.title
                            name = "spcPoint"
                            v = getStringValue(k[1], 3)
                            e = k[3]
                            t = k[4]
                            r = k[5]
                            x = 40 + xx * ((graphWidth - 40) / samsize)
                            y = yy + 1 + graphHeight - yPos(graphHeight, k[1], sample.min, sample.max)
                            radius = 3
                        }
                        if (spc.showText == true && (counter.mod(skip) == 0 || counter + 1 == sampleSize)) {
                            if (k[3] && k[3] != "null") {
                                data = {
                                    type = 'text'
                                    font = '8px Verdana'
                                    text = k[3]
                                    rotate = {
                                        x = 39 + xx * ((graphWidth - 40) / sampleSize)
                                        degrees = 270
                                    }
                                    x = 39 + xx * ((graphWidth - 40) / sampleSize)
                                    y = yy + graphHeight + 3
                                }
                            }
                            if (k[4] && k[4].substring(5, 11) != "null") {
                                data = {
                                    type = 'text'
                                    font = '8px Verdana'
                                    text = k[4].substring(5, 11)
                                    rotate = {
                                        x = 39 + xx * ((graphWidth - 40) / sampleSize)
                                        degrees = 270
                                    }
                                    x = 39 + xx * ((graphWidth - 40) / sampleSize)
                                    y = yy + 30
                                }
                            }
                        }
                        xx = xx + 1
                        counter++
                    }

                    yy += graphHeight + 28
                    xx = 0
                }
            }

        }

        saveChart(spc.id, results.toString())

        render results
    }

    def getComments = {

        def notes = Unit.findByCode(params.code)?.notes
        def ret = []
        def stepName = params.path.tokenize("/")[1]?.trim()
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd")

        notes.each {
            if (it.stepName == stepName) {
                ret.add([df.format(it.dateCreated), it.comment])
            }
        }
        render ret as JSON
    }

    private def yPos(def graphHeight, def value, def min, def max) {
        if (value == null) value = 0
        if (max == min) return graphHeight / 2
        if (value > max) value = max
        if (value < min) value = min
        graphHeight * (value - min) / (max - min) - 2
    }

    def private getStringValue(def val, def decimals) {
        if (!val) {
            return "0"
        } else if (val.getClass() == java.lang.Double
                || val.getClass() == java.lang.Float || val.getClass() == BigDecimal) {
            def ret
            if (Math.abs(val) < 0.01 || Math.abs(val) > 999) {
                ret = new DecimalFormat("0.000E0").format(val)
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

    private def getEpiSamples(def db, def sampleSize, def filter, def process, def var, def filters) {

        def query = new BasicDBObject()
        query.put("code", process)
        if (filters) {
            def filtArray = filters.tokenize(":")
            if (filtArray.size() == 2) {
                query.put(filtArray[0].trim(), new BasicDBObject('$in', filtArray[1].tokenize(",")))
            }
        }
        if (filter) {
            def fils = filter.tokenize("=")
            if (fils.size() == 2) {
                def e = new BasicDBObject('$regex', fils[1])
                query.put(fils[0], e)
            }
        }

        def fields = new BasicDBObject()
        fields.put("code", 1)
        fields.put("dateCreated", 1)
        fields.put("runNumber", 1)
        fields.put(var, 1)
        def temp = db.epiRun.find(query, fields)

        def cnt = 0
        def ret = new TreeMap()
        def df = new SimpleDateFormat("yyyy-MM-dd HH:mm")

        temp.find { obj ->

            def val = obj[var]
            try {
                def sd = df.format(obj.dateCreated)
                if (val != null && val.toString().isNumber()) {
                    cnt++
                    ret.put(sd + "_" + obj.runNumber, [obj.runNumber, val.toDouble(), obj.dateCreated, "", sd])
                }
            } catch(Exception exc) {

            }
        }

        def ret2 = []
        def values = ret.values() as List
        def min = cnt - sampleSize
        if (min < 0) min = 0
        for (def j = min; j < cnt; j++) {
            ret2.add(values.get(j))
        }
        ret2
    }


    private def getMeasureSamples(def db, def sampleSize, def filter, def var, def filters) {

        def query = new BasicDBObject()
        def filtArray2 = filter.tokenize(",")
        filtArray2.each {
            def q = it.tokenize("=")
            if (q.size() == 2) {
                query.put(q[0].trim(), q[1])
            }
        }
        if (filters) {
            def filtArray = filters.tokenize(":")
            if (filtArray.size() == 2) {
                query.put(filtArray[0].trim(), new BasicDBObject('$in', filtArray[1].tokenize(",")))
            }
        }
        def fields = new BasicDBObject()
        fields.put("WaferID", 1)
        fields.put("DateTimeTested", 1)
        fields.put("TestType", 1)
        fields.put("experimentId", 1)
        fields.put("Current", 1)
        fields.put(var, 1)

        def temp = db.measures.find(query, fields).sort(['DateTimeTested': -1]).limit(sampleSize.toInteger())
        def ret = new TreeMap()
        def df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        temp.collect { obj ->
            def val = obj[var]
            if (val != null && val.toString().isNumber()) {
                ret.put(df.format(obj.DateTimeTested), [df.format(obj.DateTimeTested), val.toDouble(), obj.WaferID, obj.experimentId, df.format(obj.DateTimeTested),  obj.Current?.toString()])
            }
        }

        def ret2 = []
        ret.each {
            ret2.add(it.value);
        }
        ret2
    }

    private def getCameraSamples(def db, def sampleSize, def filter, def process, def var, def filters) {

        def query = new BasicDBObject()
        if (filters) {
            def filtArray = filters.tokenize(":")
            if (filtArray.size() == 2) {
                query.put(filtArray[0].trim(), new BasicDBObject('$in', filtArray[1].tokenize(",")))
            }
        }
        if (filter) {
            def fils = filter.tokenize("=")
            if (fils.size() == 2) {
                def e = new BasicDBObject('$regex', fils[1])
                query.put(fils[0], e)
            }
        }

        def fields = new BasicDBObject()
        fields.put(var, 1)
        fields.put('testId', 1)
        fields.put('code', 1)
        def temp = db.konica.find(query, fields).sort(['testId': -1]).limit(sampleSize.toInteger())

        def cnt = 0
        def ret = new TreeMap()
        def df = new SimpleDateFormat("yyyy-MM-dd HH:mm")

        temp.find { obj ->

            def val = obj[var]
            if (val != null && val.toString().isNumber()) {
                cnt++
                ret.put(obj.testId, [obj.code, val.toDouble(), obj.testId, "", obj.testId])
            }
        }

        def ret2 = []
        def values = ret.values() as List
        def min = cnt - sampleSize
        if (min < 0) min = 0
        for (def j = min; j < cnt; j++) {
            ret2.add(values.get(j))
        }
        ret2
    }

    private def getSamples(
            def db,
            def sampleSize,
            def filter, def process, def path, def var, def expId, def runNumber, def prods, def filters, def category) {

        def query = new BasicDBObject()
        def temp

        query.put("parentCode", null)
        query.put("value.active", "true")
        if (process && category) {
             query.put("value.tags", category + '|' + process)
        } else if (process) {
             query.put("value.pkey", process)
        }
        if (prods) {
            query.put("value.productCode", new BasicDBObject('$in', prods))
        }
        if (filters) {
            def filtArray = filters.tokenize(":")
            if (filtArray.size() == 2) {
                query.put("value." + filtArray[0].trim(), new BasicDBObject('$in', filtArray[1].tokenize(",")))
            }
        }

        query.put(path + var, new BasicDBObject('$exists', 1))
        query.put(path + var, new BasicDBObject('$nin', [null, '', ' ']))
        query.put(path + "actualStart", new BasicDBObject('$exists', 1))
        if (filter) {
            def fils = filter.tokenize("=")
            if (fils.size() == 2) {
                def e = new BasicDBObject('$regex', fils[1])
                query.put(path + fils[0], e)
            }
        }

        def fields = new BasicDBObject()
        fields.put("code", 1)
        fields.put("value.updated", 1)
        fields.put(path + "pkey", 1)
        fields.put(path + var, 1)
        fields.put(path + expId, 1)
        fields.put(path + runNumber, 1)
        fields.put('value.runNumber', 1)
        fields.put(path + "actualStart", 1)

        temp = db.dataReport.find(query, fields)

        def ret = new TreeMap()
        def df = new SimpleDateFormat("yyyy-MM-dd HH:mm")

        temp.find { obj ->

            def sp = (path + var).tokenize(".")
            def val
            if (sp.size() == 2)
                val = obj[sp[0]] && obj[sp[0]][sp[1]] ? obj[sp[0]][sp[1]] : null
            if (sp.size() == 3)
                val = obj[sp[0]] && obj[sp[0]][sp[1]] ? obj[sp[0]][sp[1]][sp[2]] : null

            def exps = (path + expId).tokenize(".")
            def exp
            if (exps.size() == 2)
                exp = obj[exps[0]] && obj[exps[0]][exps[1]] ? obj[exps[0]][exps[1]] : null
            if (exps.size() == 3)
                exp = obj[exps[0]] && obj[exps[0]][exps[1]] ? obj[exps[0]][exps[1]][exps[2]] : null

            def runs = (path + runNumber).tokenize(".")
            def run
            if (runs.size() == 2)
                run = obj[runs[0]] && obj[runs][runs[1]] ? obj[runs[0]][runs[1]] : null
            if (runs.size() == 3)
                run = obj[runs[0]] && obj[runs[0]][runs[1]] ? obj[runs[0]][runs[1]][runs[2]] : null
            if (!run)
                run = obj['value']['runNumber']

            def ps = (path + "pkey").tokenize(".")
            def p
            if (ps.size() == 3)
                p = obj[ps[0]] && obj[ps[0]][ps[1]] ? obj[ps[0]][ps[1]][ps[2]] : null
            if (ps.size() == 2)
                p = obj[ps[0]] && obj[ps[0]][ps[1]] ? obj[ps[0]][ps[1]] : null

            def start = (path + "actualStart").tokenize(".")
            def sd
            if (start.size() == 3) {
                def st = obj[start[0]] && obj[start[0]][start[1]] ? obj[start[0]][start[1]][start[2]] : null
                if (st != null) {
                    if (st.getClass() == String)
                        sd = st
                    else
                        sd = df.format(st)
                }
            }
            if (start.size() == 2) {
                def st = obj[start[0]] ? obj[start[0]][start[1]] : null
                if (st != null) {
                    sd = df.format(st)
                }
            }

            if (val != null && val.toString().isNumber() && p == process) {
                ret.put(sd + "_" + obj.code, [obj.code, val.toDouble(), obj?.value?.updated, exp, sd, run])
            }
            false
        }

        def ret2 = []
        def values = ret.values() as List
        def cnt = values.size()
        def min = cnt - sampleSize
        if (min < 0) min = 0
        for (def j = min; j < cnt; j++) {
            ret2.add(values.get(j))
        }
        ret2

    }

    private def saveChart(spcId, chartData) {

        def db = mongo.getDB("glo")
        def report = db.spcReport.find(new BasicDBObject("spcId", spcId))?.collect { it }[0]
        if (!report) {
            report = new BasicDBObject()
            report.put("spcId", spcId)
            report.put("data", chartData)
        } else {
            report.put("data", chartData)
        }
        report.put("updated", new Date())
        db.spcReport.save(report)
    }
}
