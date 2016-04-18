package com.glo.run

import com.glo.ndo.DataView
import com.glo.ndo.DataViewJoin
import com.glo.ndo.DataViewVariable
import com.mongodb.BasicDBObject
import grails.converters.JSON
import org.apache.commons.logging.LogFactory
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics
import org.springframework.transaction.interceptor.TransactionAspectSupport
import java.text.DateFormat
import java.text.SimpleDateFormat


class DataViewService {

    private static final logr = LogFactory.getLog(this)
    def managementService
    def runtimeService
    def repositoryService
    def taskService
    def contentService
    def unitService
    def messageSource
    def mongo
    def grailsApplication
    def persistenceInterceptor


    def list(def user, def parms) {

        def srch = parms.search?.trim() ?: (parms.query?.trim() ?: "")
        def filt = ""

        srch.tokenize(" ").each {
            filt += " and (dv.owner like '%$it%' or dv.name like '%$it%' or dv.tag like '%$it%')"
        }

        def dataViews = DataView.executeQuery("""
			select dv.id as id,dv.name as name,dv.isPublic as isPublic,id.tag as tag,id.owner as owner,dv.publishToDashboard as publishToDashboard, dv.filterByPath as filterByPath,
			dv.urlDashboardData as urlDashboardData, dv.description as description, dv.chartType as chartType, dv.urlExportData as urlExportData, 
			dv.yMin as yMin, dv.yMax as yMax, dv.zMin as zMin, dv.zMax as zMax from DataView as dv where 
			(dv.owner = ? or dv.isPublic = true)  $filt """
                + (parms.sort ? " order by dv." + parms.sort + " " + parms.dir : "")
                , [user], [max: parms.max, offset: parms.offset])

        def total = DataView.executeQuery("""
			select count(*) from DataView as dv where (dv.owner = ? or dv.isPublic = true)  $filt
		"""
                , [user])[0]

        [
                dataViews.collect {
                    [id         : it[0], name: it[1], isPublic: it[2], tag: it[3], owner: it[4], publishToDashboard: it[5], filterByPath: it[6], urlDashboardData: it[7],
                     description: it[8], chartType: it[9], urlExportData: it[10], yMin: it[11], yMax: it[12], zMin: it[13], zMax: it[14]]
                },
                total
        ]
    }

    def getDashboards(def user, def parms) {

        def srch = parms.search?.trim() ?: ""
        def filt = ""
        srch.tokenize(" ").each {
            filt += " and (dv.owner like '%$it%' or dv.name like '%$it%' or dv.tag like '%$it%')"
        }

        def dataViews = DataView.executeQuery("""
			select dv.id as id, dv.name as name, id.tag as tag,id.owner as owner, dv.urlDashboardData as urlDashboardData, dv.description as description, dv.chartType as chartType,
			dv.yMin as yMin, dv.yMax as yMax, dv.zMin as zMin, dv.zMax as zMax from DataView as dv where dv.publishToDashboard = true $filt
		""" + (parms.sort ? " order by dv." + parms.sort + " " + parms.dir : "")
                , [], [max: parms.max, offset: parms.offset])

        def total = DataView.executeQuery("""
			select count(*) from DataView as dv  where dv.publishToDashboard = true $filt
		""", [])[0]

        [
                dataViews.collect
                        {
                            [id: it[0], name: it[1], tag: it[2], owner: it[3], urlDashboardData: it[4], description: it[5], chartType: it[6], yMin: it[7], yMax: it[8], zMin: it[9], zMax: it[10]]
                        },
                total
        ]
    }


    def save(String user, def dataView) {

        def dv
        if (dataView.id == "0") {
            if (DataView.findByName(dataView.name)) {
                throw new RuntimeException("Name '" + dataView.name + "' already exist")
            }
            dv = new DataView()
        } else {
            dv = DataView.get(dataView.id)
            if (user != "admin" && dv.owner != user) {
                throw new RuntimeException("Only owner can change report")
            }
            if (!dv) {
                throw new RuntimeException("Data view with id '" + dataView.id + "' does not exist")
            }
        }


        dv.properties = dataView
        if (dataView.isPublic == null)
            dv.isPublic = 0
        if (dataView.publishToDashboard == null)
            dv.publishToDashboard = 0
        if (dataView.filterByPath == null)
            dv.filterByPath = 0
        dv.owner = user
        dv.save(failOnError: true)
    }

    def duplicate(String user, def dataViewId) {

        DataView.withTransaction { status ->
            def dataView = DataView.get(dataViewId)
            if (!dataView) {
                throw new RuntimeException("Data view with id '" + dataView.id + "' does not exist")
            }

            def getName = { val ->

                def append = 1
                def appendStr = ""
                while (DataView.findByName(val + " COPY" + appendStr)) {
                    append += 1
                    appendStr = " " + append.toString()
                }
                val + " COPY" + appendStr
            }

            DataView newDataView = new DataView()
            newDataView.isPublic = false
            newDataView.publishToDashboard = false
            newDataView.filterByPath = dataView.filterByPath
            newDataView.name = getName(dataView.name)
            newDataView.urlExportData = dataView.urlExportData
            newDataView.urlDashboardData = dataView.urlDashboardData
            newDataView.description = dataView.description
            newDataView.excelName = dataView.excelName
            newDataView.excelTemplate = (dataView.excelTemplate == null ? null : dataView.excelTemplate.clone())
            newDataView.owner = user
            newDataView.tag = dataView.tag
            newDataView.save(failOnError: true)

            dataView.dataViewVariables.each
            {
                def dvv = new DataViewVariable(
                        dataView: newDataView, idx: it.idx, title: it.title, path: it.path, variable: it.variable, filter: it.filter, isFormula: it.isFormula, formula: it.formula, fullPath: it.fullPath
                )
                dvv.save()


                def dataViewJoins = DataViewJoin.findAllByDataViewAndPrimaryVariable(dataView, it)
                dataViewJoins.each { dv ->
                    def dvj = new DataViewJoin(
                            dataView: newDataView,
                            secondaryDataView: dv.secondaryDataView,
                            primaryVariable: dvv,
                            secondaryVariable: dv.secondaryVariable,
                            joinType: dv.joinType)
                    dvj.save()
                }
            }
        }
    }

    def getReportData(def dataView) {

        def status = TransactionAspectSupport.currentTransactionStatus()
        status.setRollbackOnly()

        def primaryResult = getReportData(dataView, false)

        def result = []
        def colIdx = 1000
        result.add([])
        result.add(primaryResult[1])
        result.add(primaryResult[2])
        result.add(primaryResult[3])

        if (dataView.dataViewJoins) {

            dataView.dataViewJoins.sort { it.id }.each { joinDv ->

                def primName = joinDv.primaryVariable?.title
                def secName = joinDv.secondaryVariable?.title
                def joinedDv = joinDv.secondaryDataView

                def resultToUse = result[0] ?: primaryResult[0]
                def primaryGroup = resultToUse.groupBy { it[primName] ?: "" }

                result[0] = []

                def secondaryResult = getReportData(joinedDv, false)
                result[1].addAll(secondaryResult[1])
                secondaryResult[2].removeAll { it.title == secName }
                secondaryResult[2].each { it.idx += colIdx }
                colIdx += 1000
                result[2].addAll(secondaryResult[2])

                def isBetween = false
                def secondaryGroup = secondaryResult[0].groupBy {

                    if (!isBetween && it[0][secName].indexOf("between") >= 0) {
                        isBetween = true
                    }
                    it[secName] ?: ""
                }

                // Combine primary and secondary group
                primaryGroup.each { k, v ->

                    v.each { prims ->
                        if (prims) {

                            def prim = prims[0]
                            if (prim.getClass() == Expando)
                                prim = prim.getProperties()

                            def secResult
                            if (isBetween) {
                                secResult = secondaryGroup.findAll {
                                    def tok = it.key[0].tokenize("between")
                                    if (tok[0] && tok[1])
                                        tok[0].trim() <= k[0] && tok[1].trim() >= k[0]
                                    else
                                        false
                                }.values()
                                secResult = secResult[0]

                            } else {
                                secResult = secondaryGroup[k]
                            }

                            if (secResult) {
                                secResult.each { secs ->
                                    if (secs) {

                                        def sec = secs[0]
                                        if (sec.properties) {
                                            sec?.properties?.remove(secName)
                                            result[0].add([prim + sec?.getProperties(), null])
                                        } else {
                                            sec?.remove(secName)
                                            result[0].add([prim + sec, null])
                                        }
                                    }
                                }
                            } else {
                                result[0].add([prim, null])
                            }

                            if (result[0].size() > 500000) {
                                throw new RuntimeException("To many data rows (>500000) in joined report. Please use filters to reduce data in primary and secondary report(s).")
                            }
                        }
                    }
                }
            }
        } else {
            result[0] = primaryResult[0]
        }

        result
    }

    def getReportData(def dataView, def active) {

        // Get data from datareport table
        def fields = new BasicDBObject()

        def notes = []
        def rowList = []
        def codesForNotes = []
        def dvVariables = []
        def dvFormulas = []
        def nameTitleMap = [:]

        def shell = new GroovyShell(this.class.classLoader)

        def db = mongo.getDB("glo")
        shell.setVariable("db", db);
        def mongoDocument = null

        if (dataView.urlExportData) {
            def delim = "?"
            if (dataView.urlExportData.indexOf("?") > 0)
                delim = "&"

            def url = new URL(dataView.urlExportData + delim + "json=true")
            def connection = url.openConnection()
            connection.setRequestMethod("GET")
            if (connection.responseCode == 200 || connection.responseCode == 201) {
                def jsonText = connection.content.text.replace("\r\n", "")

                def pos1 = jsonText.indexOf("{")
                def pos2 = jsonText.indexOf("}")
                if (pos1 >= 0 && pos2 >= 0) {
                    def vars = jsonText.substring(pos1, pos2)?.replace("\"", "")?.tokenize(",")?.collect {
                        it.tokenize(":")[0]
                    }
                    vars.eachWithIndex { k, i ->
                        dvVariables.add([idx: i + 1, title: k])
                    }
                    def json = JSON.parse(jsonText)
                    rowList = json.collect {
                        [it, null]
                    }
                }
            } else {
                throw new RuntimeException("Invalid response from data service")
            }

            [
                    rowList,
                    [],
                    dvVariables,
                    new BasicDBObject()
            ]
        } else {


            def dataViewVariables = DataViewVariable.findAllByDataView(dataView, [sort: "idx"])

            // Determine mongo db collection to be queried
            for (dvv in dataViewVariables) {
                if (dvv.variable.id > 0) {
                    def process = dvv.variable.process ?: dvv.variable.processStep.process

                    if (process && !(process.category in ['EquipmentDC'] && dvv.variable.dir == '')) {
                        if (process.processCategory?.mongoCollection == "dataReport") {
                            mongoDocument = db.dataReport
                        }
                        if (process.processCategory?.mongoCollection == "measures") {
                            mongoDocument = db.measures
                        }
                        if (process.processCategory?.mongoCollection == "epiRun") {
                            mongoDocument = db.epiRun
                        }
                        if (process.processCategory?.mongoCollection == "equipmentStatus") {
                            mongoDocument = db.equipmentStatus
                        }
                        if (process.processCategory?.mongoCollection == "moves") {
                            mongoDocument = db.moves
                        }
                        if (process.processCategory?.category == "Metadata") {
                            mongoDocument = process.pkey
                        }
                        if (process.processCategory?.category == "Metadata") {
                            mongoDocument = process.pkey
                        }
                        if (process.pkey == "camera" && dvv.variable.processStep.taskKey == "spots data") {
                            mongoDocument = db.konicaData
                        }
                        if (process.pkey == "camera" && dvv.variable.processStep.taskKey == "summary data") {
                            mongoDocument = db.konica
                        }
                        if (mongoDocument) break
                    }
                }
            }

            if (!mongoDocument) {
                throw new RuntimeException("Sorry, cannot determine source collection for your report.")
            }

            // Determine if formulas exist so they can be evaluated
            dataViewVariables.each {

                def scriptStr
                def path

                if (it.isFormula) {
                    scriptStr = it.formula.toString()?.trim()
                    path = it.title.replaceAll(' ', '_')
                } else if (it.variable.dir == "calc") {
                    scriptStr = it.variable.evalScript.trim()
                    path = it.path?.indexOf('.') == -1 ? it.variable.name : it.path?.substring(0, it.path?.indexOf('.')) + '_' + it.variable.name
                }
                if (scriptStr) {
                    if (scriptStr?.indexOf("unit.") != 0)
                        dvFormulas.add([idx: it.idx, dataType: "", title: it.title, filter: it.filter, path: path, script: shell.parse(scriptStr)])
                    dvVariables.add([idx: it.idx, title: it.title])
                }

                // Determine is notes are included in report
                if (it.variable.name == "nNotes") {
                    notes.add([
                            it.path?.replace(".nNotes", ""),
                            "comment"
                    ])
                }
            }

            def temp = []
            def query = new BasicDBObject()

            if (mongoDocument.getClass() == String) {

                def clazz = grailsApplication.getDomainClass("com.glo.ndo." + mongoDocument).clazz
                temp = clazz.findAll()
                dataViewVariables.each {

                    def sp = [it.variable.name]
                    if (it.variable.grp) {
                        sp = it.variable.grp.tokenize(".")
                    }
                    if (!it.isFormula) {
                        fields.put(it.title, sp)
                        dvVariables.add([idx: it.idx, title: it.title])
                    }
                }

                temp.collect { obj ->

                    def rowObj = new Expando()
                    fields.each { k, v ->
                        def val
                        if (v.size() == 1)
                            val = obj[v[0]]
                        if (v.size() == 2)
                            val = obj[v[0]] && obj[v[0]][v[1]] ? obj[v[0]][v[1]] : null
                         if (v.size() == 3)
                            val = obj[v[0]] && obj[v[0]][v[1]] ? obj[v[0]][v[1]][v[2]] : null

                        rowObj[k] = val
                    }

                    dvFormulas.each { formObj ->

                        try {
                             def calcValue = unitService.getCalculatedValue((rowObj.properties ?: [:]), formObj.title, formObj.script, shell)
                             rowObj[formObj.title] = calcValue
                        } catch (Exception exc) {
                        }
                    }


                    rowList.add([rowObj, null])
                }

            } else {

                // Determine which fields will be selected for the report depending on collection
                dataViewVariables.each {

                    if (!it.isFormula && it.fullPath) {

                        def field
                        if (mongoDocument == db.dataReport) {

                            if (it.variable.name == "code")
                                field = "code"
                            else if (it.variable.name == "productFamily")
                                field = "unit.productFamily"
                            else
                                field = "value." + it.path

                            if (dvFormulas) {
                                fields.put("unit", 1)
                            }
                        }

                        if (mongoDocument == db.measures ||
                                mongoDocument == db.epiRun ||
                                mongoDocument == db.equipmentStatus ||
                                mongoDocument == db.moves ||
                                mongoDocument == db.konica ||
                                mongoDocument == db.konicaData) {

                            field = it.variable.name
                        }

                        fields.put(field, 1)

                        dvVariables.add([idx: it.idx, title: it.title])
                        nameTitleMap.put(field, it.title)
                    } else {
                        def scriptStr = it.formula?.toString()?.trim() ?: ""
                        if (scriptStr?.indexOf('unit.') == 0) {
                            fields.put(scriptStr, 1)
                            nameTitleMap.put(scriptStr, it.title)
                        }
                    }
                }

                def dateFormats = [:]
                query.putAll(getFilter(dataViewVariables, mongoDocument == db.dataReport, dateFormats))

                if (query.size() <= 2 && (mongoDocument == db.measures || mongoDocument == db.konica || mongoDocument == db.konicaData)) {
                    throw new RuntimeException("Please define at least 3 filters for test/measures data report.")
                } else if (query.size() < 1) {
                    throw new RuntimeException("Please define at least 1 filter for data report.")
                }

                temp = mongoDocument.find(query, fields).limit(100000)


                temp.collect { obj ->

                    def rowObj = new Expando()
                    def rowObjF = new Expando()
                    def rowObjRaw = new Expando()

                    fields.each { k, v ->

                        def sp = k.tokenize(".")
                        def val
                        def valF
                        def varName
                        def varNameF
                        def varNameRaw

                        if (sp.size() == 1) {
                            val = obj[sp[0]]
                            valF = obj[sp[0]]
                            varName = sp[0]
                            varNameF = sp[0]
                            varNameRaw = sp[0]
                        }
                        if (sp.size() == 2 || k?.indexOf('unit.') == 0) {
                            val = obj[sp[0]] && obj[sp[0]][sp[1]] ? obj[sp[0]][sp[1]] : null
                            valF = obj[sp[0]] && obj[sp[0]][sp[1]] ? obj[sp[0]][sp[1]] : null
                            varName = sp[1]
                            varNameF = sp[1]
                            varNameRaw = sp[1]
                        }
                        if (sp.size() == 3) {
                            val = obj[sp[0]] && obj[sp[0]][sp[1]] ? obj[sp[0]][sp[1]][sp[2]] : null
                            valF = obj[sp[0]] && obj[sp[0]][sp[1]] ? obj[sp[0]][sp[1]][sp[2]] : null
                            varName = sp[2]
                            varNameF = sp[1] + "_" + sp[2]
                            varNameRaw = sp[2]
                        }

                        if (val.getClass() == com.mongodb.BasicDBList) {
                            if (val.size() > 0) {
                                val = val[val.size() - 1] + " "
                            }
                        }

                        if (val.getClass() == java.util.Date) {
                            if (dateFormats[k]) {
                                DateFormat df = new SimpleDateFormat(dateFormats[k])
                                val = df.format(val)
                            }
                        }
                        if (val.getClass() == java.lang.Double) {
                            if (val >= 0.001)
                                val = val.round(6)
                            else
                                val = val.round(9)
                        }

                        if (val != null) {
                            if (notes && varName == "code") {
                                codesForNotes.add(val)
                            }
                        } else {
                            val = ""
                            valF = ""
                        }

                        rowObj[nameTitleMap[k] ?: varName] = val
                        rowObjF[varNameF] = valF
                        rowObjRaw[varNameRaw] = valF
                    }


                    dvFormulas.each { formObj ->

                        try {
                            if (obj[formObj.path] == null) {
                                def calcValue = unitService.getCalculatedValue(rowObjF.properties + rowObjRaw.properties + (rowObj['unit'] ?: [:]), formObj.title, formObj.script, shell)
                                rowObj[formObj.title] = calcValue
                                rowObjF[formObj.path] = calcValue
                                rowObjRaw[formObj.path] = calcValue
                                if (!rowObj['unit']) {
                                    rowObj['unit'] = [:]
                                }
                                rowObj['unit'].put(formObj.title[0].toLowerCase() + formObj.title.substring(1), rowObj[formObj.title])
                            }
                        } catch (Exception exc) {

                            rowObj[formObj.title] = ""
                            rowObjF[formObj.path] = null
                            rowObjRaw[formObj.path] = null
                            logr.warn("Calculation: " + dataView.id.toString() + ", " + formObj.title + ", " + exc.getMessage())
                        }
                    }

                    rowObj.properties.remove('unit')

                    rowList.add([rowObj, null])
                }

                rowList = getFormulaFilter(dvFormulas, rowList)
            }

            [
                    rowList,
                    codesForNotes,
                    dvVariables,
                    query
            ]
        }
    }

    def recalculateCharts() {

        def persistenceInterceptor

        try {
            def dataViews = DataView.findAllByPublishToDashboard(true)
            dataViews.each { dataView ->
                try {
                    drawChart(dataView)
                }
                catch (Exception exc) {
                    logr.error(exc.getMessage())
                }
            }

            !logr.isDebugEnabled() ?: logr.debug("Calculate reports successfuly executed.")
        }
        catch (Exception exc) {
            logr.error(exc.getMessage())
        }

        if (persistenceInterceptor) {
            persistenceInterceptor.flush()
            persistenceInterceptor.destroy()
        }
    }


    def drawChart(def dataView) {

        def returnObj = [:]
        def storeObj = []

        try {

            if (dataView.urlDashboardData) {

                if (dataView.urlDashboardData.indexOf('dataViewCharts') == -1) {
                    def url = new URL(dataView.urlDashboardData)
                    def connection = url.openConnection()
                    connection.setRequestMethod("GET")
                    if (connection.responseCode == 200 || connection.responseCode == 201) {
                        def json = JSON.parse(connection.content.text.replace("\r\n", ""))
                        saveChart(dataView.id, json)
                        returnObj.put('success', true)
                    } else {
                        returnObj.put('success', false)
                        returnObj.put('msg', 'Invalid response from data service')
                    }
                }
            } else {

                DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH")
                def data = getReportData(dataView)
                def rows = data[0]

                def sortChart = dataView.dataViewCharts.find { it.axis == "S" }

                def xCharts = dataView.dataViewCharts.findAll { it.axis == "X" }

                xCharts.each { xChart ->

                    def groupByX = [:]
                    def retChart = [:]
                    def xTitle = ""

                    if (sortChart) {
                        retChart.put('s', "s" + sortChart.dataViewVariable.id.toString())
                    }
                    retChart.put('x', "x" + xChart.dataViewVariable.id.toString())


                    if (xChart.dataViewVariable.variable.dataType == "date") {
                        retChart.put('xAxisType', 'Category')
                        xTitle = xChart.xDateGroup

                        groupByX = rows.groupBy {

                            def date = df.format(it[0][xChart.dataViewVariable.title])
                            switch (xTitle) {
                                case "hourly":
                                    date.substring(0, 13)
                                    break
                                case "daily":
                                    date.substring(0, 10)
                                    break
                                case "weekly":
                                    def dt = df.parse(date)
                                    def year = dt[Calendar.WEEK_OF_YEAR] == 1 ?
                                            (dt[Calendar.WEEK_OF_MONTH] == 1 ? dt[Calendar.YEAR] : dt[Calendar.YEAR] + 1) :
                                            dt[Calendar.YEAR]
                                    def wy = dt[Calendar.WEEK_OF_YEAR].toString().padLeft(2, '0')
                                    year + "-" + wy
                                    break
                                case "monthly":
                                    date.substring(0, 7)
                                    break
                                case "yearly":
                                    date.substring(0, 4)
                                    break
                            }
                        }
                    }

                    if (xChart.dataViewVariable.variable.dataType == "string") {
                        retChart.put('xAxisType', 'Category')
                        groupByX = rows.groupBy {
                            it[0][xChart.dataViewVariable.title]
                        }
                    }
                    if (xChart.dataViewVariable.variable.dataType in ["int", "float", "scientific"]) {
                        retChart.put('xAxisType', 'Numeric')

                        if (xChart.xNumericGroup == 'all') {
                            def pcnt = 1
                            rows.each {
                                if (it[0][xChart.dataViewVariable.title]?.toString()?.isNumber())
                                    groupByX.put(pcnt++ + "_" + it[0][xChart.dataViewVariable.title], [it])
                            }
                        } else {
                            groupByX = rows.groupBy {
                                it[0][xChart.dataViewVariable.title]
                            }
                        }

                    }

                    if (xChart.dataViewVariable.isFormula) {
                        retChart.put('xAxisType', 'Category')
                        groupByX = rows.groupBy {
                            it[0][xChart.dataViewVariable.title]
                        }
                    }
                    retChart.put('xTitle', xTitle.toUpperCase() + " " + xChart.dataViewVariable.title)

                    // Process Y and Z axis
                    retChart.put('y', [])

                    def dvYCharts = dataView.dataViewCharts.findAll { it.axis in ["Y", "Z"] }.sort { it.id }
                    def counter = 1

                    dvYCharts.each { yChart ->
                        def yAxis = yChart.axis == "Y" ? 'left' : 'right'
                        def retY = [:]
                        retY.put('yAxis', yAxis)
                        retY.put('y', yChart.yAggregate?.toUpperCase() + " " + yChart.dataViewVariable.title)
                        retY.put('yTitle', yChart.yAggregate?.toUpperCase() + " " + yChart.dataViewVariable.title)
                        retY.put('varName', yChart.dataViewVariable.title)
                        retY.put('yAggregate', yChart.yAggregate)

                        if (yChart.dataViewVariable.isFormula == true || yChart.dataViewVariable.variable.dataType in ["int", "float", "scientific"]) {

                            retY.put('yAxisType', 'Numeric')
                        }
                        retChart['y'].add(retY)
                        counter++
                    }

                    def returnData = []
                    groupByX.each { grp, elements ->

                        def exp = [:]
                        if (xChart.xNumericGroup == 'all') {
                            exp.put(retChart.x, grp.tokenize("_")[1].toFloat())
                        } else {
                            exp.put(retChart.x, grp)
                        }

                        retChart['y'].each { yChart ->
                            def values = elements.findAll {
                                it[0][yChart.varName] != null && it[0][yChart.varName].toString().isNumber()
                            }.collect {
                                Double.parseDouble(it[0][yChart.varName].toString())
                            }

                            if (values) {

                                DescriptiveStatistics stats = new DescriptiveStatistics((double[]) values)

                                if (!stats.getSum().isNaN()) {

                                    switch (yChart.yAggregate) {
                                        case 'count':
                                            exp.put(yChart.y, stats.getN())
                                            break
                                        case 'average':
                                            exp.put(yChart.y, stats.getMean().round(3))
                                            break
                                        case 'sum':
                                            exp.put(yChart.y, stats.getSum().round(3))
                                            break
                                        case 'max':
                                            exp.put(yChart.y, stats.getMax().round(3))
                                            break
                                        case 'min':
                                            exp.put(yChart.y, stats.getMin().round(3))
                                            break
                                        case 'last':
                                            exp.put(yChart.y, values[values.size() - 1])
                                            break
                                        case 'median':
                                            exp.put(yChart.y, stats.getPercentile(50).round(3))
                                            break
                                        case 'avg-sigma':
                                            exp.put(yChart.y, (stats.getMean() - stats.getStandardDeviation()).round(3))
                                            break
                                        case 'avg+sigma':
                                            exp.put(yChart.y, (stats.getMean() + stats.getStandardDeviation()).round(3))
                                            break
                                    }
                                }
                            } else {
                                exp.put(yChart.y, 0)
                            }
                        }

                        if (sortChart) {
                            def p = sortChart?.dataViewVariable?.path ?: ""
                            def t = sortChart?.dataViewVariable?.title ?: ""
                            def el = elements[0][0]
                            def valueSorted = el[p] ?: el[t]

                            exp.put("sort", valueSorted)
                        } else {
                            if (xChart.xNumericGroup == 'all')
                                exp.put("sort", grp.tokenize("_")[1].toFloat())
                            else
                                exp.put("sort", grp)
                        }

                        if (elements[0] && elements[0][0]) {
                            exp.put("tip", elements[0][0]["code"])
                        }

                        returnData.add(exp)
                    }

                    returnData = returnData.sort { it.sort }
                    if (sortChart?.sortDir == "DESC")
                        returnData = returnData.reverse()

                    retChart.put("tip", ["tip"])
                    retChart.put('type', dataView.chartType ?: 'column')
                    retChart.put('yMin', dataView.yMin ?: 0)
                    retChart.put('yMax', dataView.yMax ?: 0)
                    retChart.put('zMin', dataView.zMin ?: 0)
                    retChart.put('zMax', dataView.zMax ?: 0)
                    retChart.put('data', returnData)

                    storeObj.add(retChart)
                }

                saveChart(dataView.id, storeObj)

                returnObj.put('success', true)
            }
        }
        catch (Exception exc) {
            logr.error(exc)
            returnObj.put('success', false)
            returnObj.put('msg', exc.toString())
        }

        return returnObj
    }

    private def saveChart(dataViewId, chartData) {

        def db = mongo.getDB("glo")
        def report = db.dashboardReport.find(new BasicDBObject("dataViewId", dataViewId))?.collect { it }[0]
        if (!report) {
            report = new BasicDBObject()
            report.put("dataViewId", dataViewId)
            report.put("data", chartData)
        } else {
            report.put("data", chartData)
        }
        report.put("updated", new Date())
        db.dashboardReport.save(report)
    }


    private def getFilter(def dataViewVariables, def isDataReport, def dateFormats) {
        def filters = new BasicDBObject()
        if (isDataReport) {

            filters.put("parentCode", null)

            Boolean filterByPath = false
            if (dataViewVariables.size() > 0) {
                filterByPath = dataViewVariables[0].dataView.filterByPath
            }

            if (!filterByPath) {
                for (dvv in dataViewVariables) {

                    if (dvv.fullPath) {
                        def ff = dvv.fullPath?.tokenize("|")[0]
                        filters.put("value.tags", new BasicDBObject('$in', [ff]))
                    }
                }
            } else {

                def groupPaths = dataViewVariables.groupBy { it?.fullPath?.tokenize("|")?.size() ?: 0 }
                if (groupPaths)
                    filters.put("value.tags", new BasicDBObject('$in', []))

                groupPaths[3].each {
                    if (!filters["value.tags"]['$in'].contains(it.fullPath))
                        filters["value.tags"]['$in'].add(it.fullPath)
                }
                groupPaths[2].each { g ->

                    def canAdd = filters["value.tags"]['$in'].grep { it.indexOf(g.fullPath) == 0 }
                    if (!canAdd && !filters["value.tags"]['$in'].contains(g.fullPath))
                        filters["value.tags"]['$in'].add(g.fullPath)
                }
                groupPaths[1].each { g ->
                    if (g.fullPath) {
                        def canAdd = filters["value.tags"]['$in'].grep { it.indexOf(g.fullPath) == 0 }
                        if (!canAdd && !filters["value.tags"]['$in'].contains(g.fullPath))
                            filters["value.tags"]['$in'].add(g.fullPath)
                    }
                }
            }

            filters.put("value.dummy", ['$exists':0])
        } else {

            filters.put("active", new BasicDBObject('$ne', 'N'))
        }

        def fillFilter = { pFilters, pIsOR, key, value ->

            if (pIsOR) {
                pFilters['$or'].add(new BasicDBObject(key, value))
            } else {
                if (key == '$or') {
                    if (!pFilters.containsKey('$and')) {
                        pFilters.put('$and', [])
                    }
                    pFilters['$and'].add(['$or': value])
                } else if (key == '$nor') {
                    if (!pFilters.containsKey('$and')) {
                        pFilters.put('$and', [])
                    }
                    pFilters['$and'].add(['$nor': value])
                } else {
                    pFilters.put(key, value)
                }
            }
        }

        dataViewVariables.each
                {

                    def filt = it.filter?.trim()
                    def dtype = it.variable.dataType
                    def prefix = ""
                    def path = it.path
                    if (isDataReport) {
                        prefix = "value."
                    } else {
                        path = it.variable.name
                    }
                    if (it.variable.name == "code") {
                        prefix = ""
                    }

                    if (filt && dtype) {

                        if (dtype == "date") {
                            // Try to extract format
                            def tokdate = filt.tokenize("/")
                            if (tokdate.size() > 1) {
                                dateFormats.put(prefix + path, tokdate[1])
                                filt = tokdate[0].trim()
                            }
                        }

                        def isOR = false
                        if (filt.length() > 3 && filt.indexOf("|| ") == 0) {
                            isOR = true
                            filt = filt.substring(3)
                            if (!filters.containsKey('$or')) {
                                filters.put('$or', [])
                            }
                        }
                        if (filt.length() > 3 && filt.substring(0, 2) == "in") {
                            def s = filt.substring(2)
                            def t = s.tokenize(",").collect
                                    { it?.trim() }
                            if (dtype == 'objectArray') {
                                t.each { itm ->
                                    fillFilter(filters, isOR, prefix + path + ".code", itm)
                                }
                            } else {
                                fillFilter(filters, isOR, prefix + path, new BasicDBObject('$in', t.collect
                                        { get(it, dtype) }))
                            }
                        } else if (filt.length() > 6 && filt.substring(0, 6) == "not in") {
                            def s = filt.substring(6)
                            def t = s.tokenize(",").collect
                                    { it?.trim() }
                            fillFilter(filters, isOR, prefix + path, new BasicDBObject('$nin', t.collect
                                    { get(it, dtype) }))

                        } else if (filt.length() > 8 && filt.substring(0, 7) == "between") {
                            def s = filt.substring(7)
                            def t = s.tokenize(",").collect
                                    { it?.trim() }
                            if (t.size() == 2) {
                                fillFilter(filters, isOR, prefix + path, new BasicDBObject(['$gte': get(t[0], dtype), '$lte': get(t[1], dtype)]))
                            }
                        } else if (filt.length() > 10 && filt.substring(0, 10) == "begin with") {
                            def s = filt.substring(10)
                            def t = s.tokenize(",").collect { it?.trim() }
                            if (t) {
                                def sec1 = new BasicDBObject('$or', [])
                                t.each { itm ->
                                    sec1['$or'].add(new BasicDBObject(prefix + path, new BasicDBObject('$regex', "^" + itm)))
                                }
                                if (!isOR) {
                                    if (!filters['$and']) filters.put('$and', [])
                                    filters['$and'].add(sec1)
                                } else {
                                    filters['$or'].add(sec1)
                                }
                            }
                        } else if (filt.length() > 4 && filt.substring(0, 4) == "last" && dtype == "date") {
                            if (filt[-4..-1] == "days") {
                                def s = filt.tokenize(" ")
                                def df = new Date().clearTime() - s[1].toInteger()
                                def dt = new Date() + 1
                                fillFilter(filters, isOR, prefix + path, new BasicDBObject(['$gte': df, '$lte': dt]))
                            } else {
                                def s = filt.tokenize(" ")
                                def days = new Date()[Calendar.DAY_OF_WEEK]        // 1 for Sun... 7 for Sat
                                def df = new Date().clearTime() - (days - 1) - (s[1].toInteger() - 1) * 7
                                def dt = new Date() + 1
                                fillFilter(filters, isOR, prefix + path, new BasicDBObject(['$gte': df, '$lte': dt]))
                            }
                        } else if (filt.length() > 4 && filt.substring(0, 4) == "next" && dtype == "date") {
                            def s = filt.tokenize(" ")
                            def df = new Date() - 700
                            def dt = new Date() + s[1].toInteger()
                            fillFilter(filters, isOR, prefix + path, new BasicDBObject(['$gte': df, '$lte': dt]))
                        } else if (filt.length() > 2 && filt.substring(0, 1) == ">") {
                            def s = filt.substring(1)?.trim()
                            if (s) {
                                fillFilter(filters, isOR, prefix + path, new BasicDBObject(['$gt': get(s, dtype)]))
                            }
                        } else if (filt.length() > 2 && filt.substring(0, 1) == "<") {
                            def s = filt.substring(1)?.trim()
                            if (s) {
                                fillFilter(filters, isOR, prefix + path, new BasicDBObject(['$lt': get(s, dtype)]))
                            }
                        } else if (filt.substring(0, 5) == "blank") {
                            def sec1 = new BasicDBObject(prefix + path, "")
                            def sec2 = new BasicDBObject(prefix + path, new BasicDBObject('$type', 6))
                            def sec3 = new BasicDBObject(prefix + path, new BasicDBObject('$type', 10))
                            def sec4 = new BasicDBObject(prefix + path, new BasicDBObject('$exists', false))
                            def sec5 = new BasicDBObject(prefix + path, " ")
                            def sec6 = new BasicDBObject(prefix + path, "  ")
                            fillFilter(filters, isOR, '$or', [
                                    sec1,
                                    sec2,
                                    sec3,
                                    sec4,
                                    sec5,
                                    sec6
                            ])
                        } else if (filt.substring(0, 9) == "not blank") {
                            def sec1 = new BasicDBObject(prefix + path, "")
                            def sec2 = new BasicDBObject(prefix + path, new BasicDBObject('$type', 6))
                            def sec3 = new BasicDBObject(prefix + path, new BasicDBObject('$type', 10))
                            def sec4 = new BasicDBObject(prefix + path, new BasicDBObject('$exists', false))
                            def sec5 = new BasicDBObject(prefix + path, " ")
                            def sec6 = new BasicDBObject(prefix + path, "  ")
                            fillFilter(filters, isOR, '$nor', [
                                    sec1,
                                    sec2,
                                    sec3,
                                    sec4,
                                    sec5,
                                    sec6
                            ])
                        }
                    }
                }

        filters
    }

    private def getFormulaFilter(def formulas, def data) {
        formulas.each
                {
                    def filt = it.filter?.trim()
                    //def dtype = it.variable.dataType

                    if (filt /* && dtype */) {
                        def title = it.title

                        if (filt.length() > 3 && filt.substring(0, 2) == "in") {
                            def s = filt.substring(2)
                            def t = s.tokenize(",").collect { it?.trim() }
                            data = data.findAll { it[0][title].toString() in t }
                            //filters.put(prefix + path, new BasicDBObject('$in', t.collect { get(it, dtype) } ))
                        } else if (filt.length() > 6 && filt.substring(0, 6) == "not in") {
                            def s = filt.substring(6)
                            def t = s.tokenize(",").collect { it?.trim() }
                            data = data.findAll { !(it[0][title].toString() in t) }
                            //filters.put(prefix + path, new BasicDBObject('$nin', t.collect { get(it, dtype) }))
                        } else if (filt.length() > 8 && filt.substring(0, 7) == "between") {
                            def s = filt.substring(7)
                            def t = s.tokenize(",").collect { it?.trim() }
                            if (t.size() == 2) {
                                if (t[0].isNumber() && t[1].isNumber()) {
                                    def t0 = get(t[0], 'float')
                                    def t1 = get(t[1], 'float')
                                    data = data.findAll {
                                        it[0][title] && it[0][title].toFloat() && it[0][title] >= t0 && it[0][title] <= t1
                                    }
                                    //filters.put(prefix + path, new BasicDBObject(['$gte': get(t[0], dtype), '$lte': get(t[1], dtype)]))
                                } else if (get(t[0], 'date') && get(t[1], 'date')) {
                                    def t0 = get(t[0], 'date')
                                    def t1 = get(t[1], 'date')
                                    data = data.findAll { it[0][title] && it[0][title] >= t0 && it[0][title] <= t1 }
                                } else {
                                    data = data.findAll {
                                        it[0][title] && it[0][title].toString() >= t[0] && it[0][title].toString() <= t[1]
                                    }
                                }
                            }
                        } else if (filt.length() > 10 && filt.substring(0, 10) == "begin with") {
                            def s = filt.substring(10)
                            def t = s.tokenize(",").collect { it?.trim() }
                            def unions = []
                            t.each { itm ->
                                unions.addAll(data.findAll { it[0][title].toString().indexOf(itm) == 0 })
                                //filters['$or'].add(new BasicDBObject(prefix + path, new BasicDBObject('$regex', "^" + itm)))
                            }
                            data = unions.unique()
                            unions = null
                        } else if (filt.length() > 4 && filt.substring(0, 4) == "last" /* && dtype == "date" */) {
                            if (filt[-4..-1] == "days") {
                                def s = filt.tokenize(" ")
                                def df = new Date().clearTime() - s[1].toInteger()
                                def dt = new Date() + 1
                                data = data.findAll { it[0][title] && it[0][title] >= df && it[0][title] <= dt }
                                //filters.put(prefix + path, new BasicDBObject(['$gte':df, '$lte': dt]))
                            } else {
                                def s = filt.tokenize(" ")
                                def days = new Date()[Calendar.DAY_OF_WEEK]        // 1 for Sun... 7 for Sat
                                def df = new Date().clearTime() - (days - 1) - (s[1].toInteger() - 1) * 7
                                def dt = new Date() + 1
                                data = data.findAll { it[0][title] && it[0][title] >= df && it[0][title] <= dt }
                                //filters.put(prefix + path, new BasicDBObject(['$gte':df, '$lte': dt]))
                            }
                        } else if (filt.length() > 4 && filt.substring(0, 4) == "next" /* && dtype == "date" */) {
                            def s = filt.tokenize(" ")
                            def df = new Date() - 700
                            def dt = new Date() + s[1].toInteger()
                            data = data.findAll { it[0][title] && it[0][title] >= df && it[0][title] <= dt }
                            //filters.put(prefix + path, new BasicDBObject(['$gte':df, '$lte': dt]))
                        } else if (filt.length() > 2 && filt.substring(0, 1) == ">") {
                            def s = filt.substring(1)?.trim()
                            if (s) {
                                if (s.isNumber()) {
                                    def t = get(s, 'float')
                                    data = data.findAll { it[0][title] && it[0][title].toFloat() && it[0][title] > t }
                                    //filters.put(prefix + path, new BasicDBObject(['$gt': get(s, dtype)]))
                                } else {
                                    data = data.findAll { it[0][title] && it[0][title].toString() > s }
                                }
                            }
                        } else if (filt.length() > 2 && filt.substring(0, 1) == "<") {
                            def s = filt.substring(1)?.trim()
                            if (s) {
                                if (s.isNumber()) {
                                    def t = get(s, 'float')
                                    data = data.findAll { it[0][title] && it[0][title].toFloat() && it[0][title] < t }
                                    //filters.put(prefix + path, new BasicDBObject(['$lt': get(s, dtype)]))
                                } else {
                                    data = data.findAll { it[0][title] && it[0][title].toString() < s }
                                }
                            }
                        } else if (filt.substring(0, 5) == "blank") {
                            data = data.findAll { it[0][title] == null || it[0][title] == "" }
                        } else if (filt.substring(0, 9) == "not blank") {
                            data = data.findAll { it[0][title] != null && it[0][title] != "" }
                        }
                    }
                }


        data
    }


    private def get(def var, def dtype) {
        def ret = null
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd")

        try {
            if (dtype == 'int') {
                ret = new Integer(var)
            } else if (dtype == 'float') {
                ret = new Float(var)
            } else if (dtype == 'scientific') {
                ret = new Float(var)
            } else if (dtype == 'date') {
                ret = df.parse(var)
            } else {
                ret = var.trim()
            }
        } catch (Exception exc) {
            ret = null
            logr.warn(exc.getMessage())
        }
        finally {
            return ret
        }
    }

    private def getTitle(def title) {

        title.replaceAll('[^a-zA-Z0-9]+', '_')
    }
}
