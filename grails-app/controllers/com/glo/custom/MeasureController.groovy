package com.glo.custom

import com.mongodb.BasicDBObject
import grails.converters.JSON
import org.apache.commons.logging.LogFactory
import org.apache.poi.xssf.usermodel.*

import javax.servlet.ServletOutputStream
import java.text.DateFormat
import java.text.DecimalFormat
import java.text.SimpleDateFormat

class MeasureController extends com.glo.run.Rest {

    private static final logr = LogFactory.getLog(this)

    def springSecurityService
    def fileService
    def relSyncService
    def dataImportService

    def mongo

    def groupTypes = {

        def username = springSecurityService.principal?.username

        def wafers = new TreeSet()
        def packages = new TreeSet()
        def headers = new TreeSet()
        def runs = new TreeSet()
        def exps = new TreeSet()
        def boards = new TreeSet()
        def builds = new TreeSet()

        def db = mongo.getDB("glo")
        def query = new BasicDBObject("WaferID", params.waferId)
        query.put("TestType", params.testType)

        def fields = new BasicDBObject()
        fields.put("WaferID", 1)
        fields.put("build_number", 1)
        fields.put("headerId", 1)
        fields.put("runNumber", 1)
        fields.put("experimentId", 1)
        fields.put("boardLoad", 1)
        fields.put("build", 1)

        db.measures.find(query, fields).collect {
            if (it.WaferID) wafers.add(it.WaferID)
            if (it.build_number) packages.add(it.build_number)
            if (it.headerId) headers.add(it.headerId)
            if (it.runNumber) runs.add(it.runNumber)
            if (it.experimentId) exps.add(it.experimentId)
            if (it.boardLoad) boards.add(it.boardLoad)
            if (it.build) builds.add(it.build)
        }

        def result = []

        result.add([name: "Wafer#", field: "WaferID", defaultValue: wafers.join(",")])
        result.add([name: "Device#", field: "DeviceID", defaultValue: wafers.join(",")])
        result.add([name: "Build#", field: "build_number", defaultValue: packages.join(",")])
        result.add([name: "Header#", field: "headerId", defaultValue: headers.join(",")])
        result.add([name: "Run#", field: "runNumber", defaultValue: runs.join(",")])
        result.add([name: "EXP#", field: "experimentId", defaultValue: exps.join(",")])
        result.add([name: "BoardLoad#", field: "boardLoad", defaultValue: boards.join(",")])

        render(result as JSON)
    }


    def devices = {

        def username = springSecurityService.principal?.username

        def db = mongo.getDB("glo")
        def items = params.items.tokenize(",")
        def query = new BasicDBObject()
        query.put("TestType", params.testType)

        if (params.testId) {
           query.put("sid", params.testId)
        }

        if (params.groupType == "DeviceID") {
            query.put("WaferID", items[0].tokenize("_")[0])
            query.put("DeviceID", ['$regex': '^' + items[0].tokenize("_")[1]])
        } else {
            query.put(params.groupType, new BasicDBObject('$in', items))
        }

        def fields = new BasicDBObject()
        fields.put("WaferID", 1)
        fields.put("DeviceID", 1)
        fields.put("burnin", 1)

        def results = new TreeSet()
        db.measures.find(query, fields).collect {


            if (it.DeviceID && it.burnin == "YES")
                results.add(it.WaferID + "_" + it.DeviceID + "--B")
            else if (it.DeviceID)
                results.add(it.WaferID + "_" + it.DeviceID)
            else
                results.add(it.WaferID)
        }

        render(results.collect { [name: it] } as JSON)
    }

    def activation = {

        def ret = getActiveData(params.testType, params.waferId).collect {
            ['SID': it._id.sid, 'WaferID': it._id.wafer, 'DeviceID': it._id.device, 'HourSet': it._id.hourSet, 'TempSet': it._id.tempSet, 'DateTime': it.time, 'Total': it.total, 'Selected': it.active ?: 'Y']
        }

        render(ret as JSON)
    }

    def changeActive = {

        def str = params.records.toString().replace("\\", "")
        def records = JSON.parse(str)

        def data = getActiveData(params.testType, params.waferID)
        data.each {
            def active = 'N'

            records.each { record ->
                if (it._id.sid == record.SID && it._id.hourSet == record.hourSet && it._id.tempSet == record.tempSet) {
                    active = 'Y'
                }
            }

            changeActiveData(params.testType, it._id.wafer, it._id.device, it._id.sid, it._id.hourSet, it._id.tempSet, active)
        }

        recalculateSummary(params.testType, params.SID)

        def ret = [success: true]
        render(ret as JSON)
    }

    private def recalculateSummary(def testType, def sid) {

        def db = mongo.getDB("glo")
        db.measures.find([sid: sid, active: 'Y']).collect {
            if (testType.toUpperCase().indexOf("D65") >= 0) {
                dataImportService.enqueue("D65", it, false)
            }
        }
    }

    private def getActiveData(def testType, def waferId) {

        def db = mongo.getDB("glo")
        def match = new BasicDBObject()
        match.put('TestType', testType)
        def wd = getWaferDevice(waferId)
        match.put('WaferID', wd.wafer)
        if (wd.device) {
            match.put('DeviceID', wd.device)
        }
        db.measures.aggregate(
                ['$match': match],
                ['$group': [_id: [sid: '$sid', wafer: '$WaferID', device: '$DeviceID', hourSet: '$HoursOn', tempSet: '$TempSetpoint'], total: [$sum: 1], time: [$last: '$TimeRun'], active: [$last: '$active']]],
                ['$sort': [TimeRun: -1]]
        ).results()
    }

    private def getWaferDevice(def waferId) {
        def curr = waferId.tokenize('_')
        def s = [:]
        if (curr.size() == 2) {
            s.device = curr[1]
            s.wafer = curr[0]
        } else {
            s.wafer = waferId
        }
        return s
    }

    private def changeActiveData(def testType, def waferId, def deviceId, def sid, def hourSet, def tempSet, def active) {

        def db = mongo.getDB("glo")
        def query = new BasicDBObject()
        query.put('sid', sid)
        query.put('TestType', testType)
        query.put('WaferID', waferId)
        if (deviceId) {
            query.put('DeviceID', deviceId)
        }
        query.put('HoursOn', hourSet)
        query.put('TempSetpoint', tempSet)

        db.measures.update(query, ['$set': [active: active]], false, true)
    }


    def charts = {

        try {
            def username = springSecurityService.principal?.username

            def db = mongo.getDB("glo")

            def charts = MeasureChart.findAllByTestType(params.testType, [sort: "ord"])

            render([success: true, data: charts.collect {
                [id: it.id, name: it.name, resultType: it.resultType, filterField: (it.filterTitle ?: (it.filterField ?: ""))]
            }] as JSON)
        }
        catch (Exception exc) {
            logr.error(exc)
            render([success: false, msg: exc.toString()] as JSON)
        }
    }

    def chart = {

        def username = springSecurityService.principal?.username

        if(!params.ids) {
            render ([success: false]) as JSON
            return
        }

        def measureChart = MeasureChart.get(params.oid)

        def ids = params.ids.getClass() == java.lang.String ? [params.ids] : params.ids

        def db = mongo.getDB("glo")
        def query = new BasicDBObject()

        query.put("TestType", measureChart.testType)
        query.put("ResultType", measureChart.resultType)
        query.put("active", new BasicDBObject('$ne', 'N'))

        def lstor = []
        ids.each {

            def code = it.replace("--B", "")

            def orc = new BasicDBObject()
            def sarr = code.tokenize("_")
            def waferId = sarr[0]
            orc.put("WaferID", waferId)

            def deviceId = ""
            if (sarr.size() == 2) {
                deviceId = sarr[1]
                orc.put("DeviceID", deviceId)
            }

            if (params.filter) {
                orc.put('$or',
                        [
                                new BasicDBObject(measureChart.filterField, params.filter),
                                new BasicDBObject(measureChart.filterField, params.filter.isLong() ? params.filter.toLong() : '')
                        ])
            }

            lstor.add(orc)
        }
        if (lstor) {
            query.put('$or', lstor)
        }
        def fields = new BasicDBObject()
        fields.put("WaferID", 1)
        fields.put("DeviceID", 1)
        fields.put(measureChart.xField ?: "", 1)
        fields.put(measureChart.yField ?: "", 1)
        fields.put(measureChart.groupField ?: "", 1)
        fields.put(measureChart.filterField ?: "", 1)
        fields.put("TimeRun", 1)

        def rowsMeasure = db.measures.find(query, fields).collect { it }

        def groupByX = [:]
        def retChart = [:]

        retChart.put('x', measureChart.xField)
        retChart.put('xTitle', measureChart.xTitle)

        def groupField = measureChart.groupField

        def groupByDevice = rowsMeasure.groupBy {

            it.WaferID + "_" + it.DeviceID
        }
        def chartsDef = []

        groupByDevice.each { devcode, rows ->

            // Process X axis
            if (groupField == "TimeRun") {
                DateFormat df = new SimpleDateFormat("MM/dd/yyyy")
                retChart.put('xAxisType', 'Category')
                groupByX = rows.groupBy {
                    def date = it["TimeRun"]
                    switch (measureChart.xField) {
                        case "daily":
                            date.substring(6, 10) + date.substring(0, 2) + date.substring(3, 5)
                            break
                        case "weekly":
                            date.substring(6, 10) + "-" + df.parse(date)[Calendar.WEEK_OF_YEAR].toString()
                            break
                        case "monthly":
                            date.substring(6, 10) + date.substring(0, 2)
                            break
                        case "yearly":
                            date.substring(6, 10)
                            break
                    }
                }
            }
            if (groupField in [
                    "WaferID",
                    "DeviceID",
                    "TestType",
                    "ResultType",
                    "station",
                    "operName"
            ]) {
                retChart.put('xAxisType', 'Category')
                groupByX = rows.groupBy { it[groupField] }
            } else if (!(groupField in [
                    "wavelengthPowerScan",
                    "VISwp"
            ])) {
                retChart.put('xAxisType', 'Numeric')
                groupByX = rows.groupBy { it[groupField] }
            }

            if (groupField) {
                // Process Y axis
                groupByX.keySet().sort().collect {

                    def yt = devcode + " " + measureChart.yTitle + "_" + measureChart.groupTitle + "_" + getStringNumber(it).replace(".", "_")
                    chartsDef.add([y: yt, yTitle: devcode + " " + measureChart.yTitle, yAxisType: "Numeric"])
                }
            } else {
                chartsDef.add(
                        [y: devcode + " " + measureChart.yTitle, yTitle: devcode + " " + measureChart.yTitle, yAxisType: "Numeric"]
                )
            }
        }

        retChart.put('y', chartsDef)


        def returnData = []
        def filters = new TreeSet()
        def minX = 1E18
        def maxX = -10000000
        def minY = 1E18
        def maxY = -10000000

        groupByDevice.each { devcode, rows ->

            def sortedRows = rows.sort { a, b ->
                if (measureChart.yField == "wavelengthPowerScan") {
                    a[measureChart.xField] <=> b[measureChart.xField]
                } else if (measureChart.yField == "VISwp") {
                    a[measureChart.xField] <=> b[measureChart.xField]
                } else {
                    a[measureChart.xField] <=> b[measureChart.xField] ?:
                            a[measureChart.yField] <=> b[measureChart.yField]
                }
            }

            if (measureChart.filterField) {
                sortedRows.each {
                    if (it[measureChart.filterField]?.toString()?.isLong()) {
                        filters.add(it[measureChart.filterField]?.toString()?.toLong())
                    } else {
                        filters.add(it[measureChart.filterField])
                    }
                }
            }

            def filterValue = params.filter ?: ""
            if (!filterValue && filters)
                filterValue = filters?.first()?.toString()


            sortedRows.each {

                Boolean isAdd = true

                String fd = (it[measureChart.filterField]?.toString() + ".")?.tokenize(".")[0]
                String fv = (filterValue + ".")?.tokenize(".")[0]
                if (measureChart.filterField && filterValue && fv != fd)
                    isAdd = false

                if (isAdd) {

                    def v = getStringNumber(it[groupField])
                    if (measureChart.yField == "wavelengthPowerScan") {

                        it[measureChart.yField]["data"].eachWithIndex { spectrum, i ->
                            def exp = [:]
                            if (i % 3 == 0 && spectrum[1] >= 0) {

                                exp.put(measureChart.xField, spectrum[0])
                                spectrum[0] < minX ? minX = spectrum[0] : 0
                                spectrum[0] > maxX ? maxX = spectrum[0] : 0
                                if (params.logharitmic == "true") {

                                    if (spectrum[1] == 0) spectrum[1] = 1
                                    def logsct = Math.log10(Math.abs(spectrum[1]))
                                    if (logsct != 0) {
                                        if (measureChart.groupTitle  && v )
                                            exp.put(devcode + " " + measureChart.yTitle + "_" + measureChart.groupTitle + "_" + v.toString().replace(".", "_"), logsct)
                                        else
                                            exp.put(devcode + " " + measureChart.yTitle, logsct)

                                        logsct < minY ? minY = logsct : 0
                                        logsct > maxY ? maxY = logsct : 0
                                    }
                                } else {
                                    if (measureChart.groupTitle  && v )
                                        exp.put(devcode + " " + measureChart.yTitle + "_" + measureChart.groupTitle + "_" + v.toString().replace(".", "_"), spectrum[1])
                                    else
                                        exp.put(devcode + " " + measureChart.yTitle, spectrum[1])

                                    spectrum[1] < minY ? minY = spectrum[1] : 0
                                    spectrum[1] > maxY ? maxY = spectrum[1] : 0
                                }

                                returnData.add(exp)
                            }
                        }

                    } else if (measureChart.yField == "VISwp") {

                        if (it[measureChart.yField] && it[measureChart.yField]["data"]) {
                            it[measureChart.yField]["data"].eachWithIndex { viv, i ->
                                def exp = [:]

                                exp.put(measureChart.xField, viv[0])
                                viv[0] < minX ? minX = viv[0] : 0
                                viv[0] > maxX ? maxX = viv[0] : 0
                                def bval = Math.abs(viv[1])
                                if (params.logharitmic == "true" || params.logY == "true") {
                                    bval = Math.log10(bval)
                                }
                                if (measureChart.groupTitle && v)
                                    exp.put(devcode + " " + measureChart.yTitle + "_" + measureChart.groupTitle + "_" + v.toString().replace(".", "_"), bval)
                                else
                                    exp.put(devcode + " " + measureChart.yTitle, bval)


                                bval < minY ? minY = bval : 0
                                bval > maxY ? maxY = bval : 0

                                returnData.add(exp)
                            }
                        }
                    } else {

                        def xval = Math.abs(it[measureChart.xField])
                        if (params.logX == "true") {
                            xval =  Math.log10(xval)
                        }
                        def yval = Math.abs(it[measureChart.yField])
                        if (params.logY == "true") {
                            yval =  Math.log10(yval)
                        }

                        def exp = [:]
                        exp.put(measureChart.xField, xval)
                        def d = it.TimeRun.toString()
                        exp.put('date', d.substring(0, 4) + d.substring(5, 7) + d.substring(8, 10))

                        if (yval != null) {
                            if (groupField)
                                exp.put(devcode + " " + measureChart.yTitle + "_" + measureChart.groupTitle + "_" + v.toString().replace(".", "_"), yval)
                            else
                                exp.put(devcode + " " + measureChart.yTitle, yval)

                            returnData.add(exp)

                            if (xval != null) {
                                xval < minX ? minX = xval : 0
                                xval > maxX ? maxX = xval : 0
                            }
                            yval < minY ? minY = yval : 0
                            yval > maxY ? maxY = yval : 0
                        }
                    }
                }
            }
        }

        retChart.put('minX', minX)
        retChart.put('maxX', maxX)
        retChart.put('minY', minY)
        retChart.put('maxY', maxY)

        if (params.minf != '') {
            retChart.put('minY', params.minf.toString().toFloat())
        }
        if (params.maxf != '') {
            retChart.put('maxY', params.maxf.toString().toFloat())
        }

        def decimals = 4
        if (Math.abs(minY) < 0.0001 || Math.abs(minX) < 0.001) {
            decimals = 6
        }

        retChart.put('decimals', decimals)
        retChart.put('data', returnData)


        render([success: true, filterSelect: (filters ? filters.first() : ''), filters: filters.collect {
            [it]
        }, charts: [retChart]] as JSON)
    }


    def export = {

        def codes = params.codes.tokenize(",")

        def db = mongo.getDB("glo")
        def queryTest = new BasicDBObject()
        def tkey = params.tkey

        def result = []

        codes.each {

            def code = it.replace("--B", "")

            queryTest.put("TestType", tkey)
            queryTest.put("WaferID", code.tokenize("_")[0])
            queryTest.put("DeviceID", code.tokenize("_")[1] ?: "")
            queryTest.put("active", new BasicDBObject('$ne', 'N'))

            db.measures.find(queryTest, new BasicDBObject("wavelengthPowerScan", 0)).collect { result.add(it) }
        }

        XSSFWorkbook workbook = new XSSFWorkbook()
        XSSFSheet sheetCurrent = workbook.createSheet("mesdata")
        XSSFSheet sheetVI = workbook.createSheet("voltage sweeps")

        // Add header to excel
        XSSFRow rowHeaderCurrent = sheetCurrent.createRow(0)
        XSSFRow rowHeaderVI = sheetVI.createRow(0)

        def fieldMap = [
                "WaferID": [0, ''],
                "DeviceID": [1, ''],
                "TestType": [2, ''],
                "ResultType": [3, ''],
                "Encapsulated": [4, ''],
                "HoursOn": [5, ''],
                "C": [6, 'uA'],
                "G": [7, 'uA'],
                "R": [8, 'uA'],
                "B": [9, 'uA'],
                "TempSetpoint": [10, 'C'],
                "TimeRun": [11, ''],
                "eqe": [12, ''],
                "wpe": [13, ''],
                "Current": [14, 'A'],
                "Volt": [15, 'V'],
                "radiometric": [16, 'W'],
                "photometric": [17, 'lm'],
                "PeakWavelength": [18, 'nm'],
                "PeakWavelengthIntensity": [19, 'W'],
                "Centroid": [20, 'nm'],
                "dominantWavelength": [21, 'nm'],
                "FWHM": [22, ''],
                "x": [23, ''],
                "y": [24, ''],
                "z": [25, ''],
                "u": [26, ''],
                "eqeDelta": [27, ''],
                "VoltDelta": [28, ''],
                "photometricDelta": [29, 'lm'],
                "PeakWavelengthDelta": [30, 'nm'],
                "CentroidDelta": [31, 'nm'],
                "dominantWavelengthDelta": [32, 'nm'],
                "FWHMDelta": [33, ''],
                "xDelta": [34, ''],
                "yDelta": [35, ''],
                "vDelta": [36, ''],
                "uDelta": [37, ''],

                "eqePerc": [38, ''],
                "VoltPerc": [39, ''],
                "photometricPerc": [40, ''],
                "PeakWavelengthPerc": [41, ''],
                "CentroidPerc": [42, ''],
                "dominantWavelengthPerc": [43, ''],
                "FWHMPerc": [44, ''],
                "xPerc": [45, ''],
                "yPerc": [46, ''],
                "vPerc": [47, ''],
                "uPerc": [48, ''],

                "mask": [49, ''],
                "runNumber": [50, ''],
                "recipeName": [51, ''],
                "build_number": [52, ''],
                "headerId": [53, ''],
                "tray_id": [54, ''],
                "experimentId": [55, ''],
                "polish": [56, ''],
                "supplier": [57, ''],
                "dieOrder": [58, ''],
                "kFactor": [59, ''],
                "sid": [60, ''],
                "boardLoad": [61, ''],
                "burnin": [62, ''],

                "RawPeakWavelength": [63, ''],
                "RawPeakWavelengthIntensity": [64, ''],
                "Peak2Wavelength": [65, ''],
                "Peak2WavelengthIntensity": [66, ''],
                "Peak3Wavelength": [67, ''],
                "Peak3WavelengthIntensity": [68, ''],
                "Peak4Wavelength": [69, ''],
                "Peak4WavelengthIntensity": [70, ''],
                "MaxAdcValue": [71, ''],
                "IntegrationTime": [72, ''],
                "FilterPosition": [73, '']
        ]

        def fieldMapVI = [
                "WaferID": [0, ''],
                "DeviceID": [1, ''],
                "Encapsulated": [2, ''],
                "HoursOn": [3, ''],
                "TempSetpoint": [4, 'C'],
                "Voltage": [5, 'V'],
                "Current": [6, 'A'],
                "ResultType": [7, '']
        ]

        fieldMap.each { key, value ->

            XSSFCell cellHeadCurrent = rowHeaderCurrent.createCell(value[0])
            def str = key
            if (value[1]) {
                str += " (" + value[1] + ")"
            }
            cellHeadCurrent.setCellValue(new XSSFRichTextString(str))
        }

        fieldMapVI.each { key, value ->

            XSSFCell cellHeadCurrent = rowHeaderVI.createCell(value[0])
            def str = key
            if (value[1]) {
                str += " (" + value[1] + ")"
            }
            cellHeadCurrent.setCellValue(new XSSFRichTextString(str))
        }

        def rv = 1
        def rvvi = 1
        result.each {

            XSSFRow row = sheetCurrent.createRow(rv)
            def rc = 0
            fieldMap.each { key, value ->

                XSSFCell cellData = row.createCell(rc)

                if (key == "DeviceID" && !it["DeviceID"]) {
                    def strDev = ""
                    db.dataReport.find(["parentCode":it["WaferID"]],["code":1]).collect { dev ->
                        if (strDev)
                            strDev += "," +  dev.code
                        else
                            strDev = dev.code
                    }
                    cellData.setCellValue(strDev)
                } else {
                    cellData.setCellValue(it[key])
                }
                rc++
            }

            if (it["VISwp"]) {
                it["VISwp"]["data"].eachWithIndex { viv, i ->

                    XSSFRow rowVI = sheetVI.createRow(rvvi)
                    def rcvi = 0
                    fieldMapVI.each { key, value ->
                        XSSFCell cellData = rowVI.createCell(rcvi)
                        if (key == "Voltage")
                            cellData.setCellValue(viv[0])
                        else if (key == "Current")
                            cellData.setCellValue(viv[1])
                        else
                            cellData.setCellValue(it[key])
                        rcvi++
                    }
                    rvvi++
                }
            }
            rv++
        }



        response.setHeader("Content-disposition", "attachment; filename=data.xlsx")
        response.contentType = "application/excel"
        ServletOutputStream f = response.getOutputStream()
        workbook.write(f)
        f.close()
    }

    private String getStringNumber(def value) {

        def ret = value ?: ""
        if (value.getClass() == java.lang.Double || value.getClass() == java.lang.Float || value.getClass() == BigDecimal) {
            if (Math.abs(value) < 0.0001 || Math.abs(value) > 999) {
                ret = new DecimalFormat("0.0E0").format(value)
            } else {
                ret = new DecimalFormat("0.0000").format(value)
            }
        } else {
            ret = value
        }
        ret
    }
}
