package com.glo.custom

import com.glo.run.Unit
import com.mongodb.BasicDBObject
import com.sun.corba.se.impl.orbutil.ObjectStreamClass_1_3_1
import org.apache.commons.logging.LogFactory
import org.apache.poi.xssf.usermodel.XSSFRow
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.grails.plugins.excelimport.ExcelImportUtils
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics

class ImportService {

    private static final logr = LogFactory.getLog(this)

    def mongo
    def unitService
    def historyService
    def workflowService
    def contentService

    public static Map DATA_VISUALIZATION_MAP = [
            sheet    : 'Summary',
            startRow : 2,
            columnMap: [
                    'B': 'waferId',
                    'C': 'deviceId',
                    'F': 'voltage',
                    'G': 'lop',
                    'H': 'wpe',
                    'I': 'eqe'
            ]
    ]

    public static Map KONIKA_SUMMARY_DATA_MAP = [
            sheet    : 'Sheet1',
            startRow : 1,
            columnMap: [
                    'A': 'position',
                    'B': 'lv',
                    'C': 'x',
                    'D': 'y',
                    'E': 'tcp',
                    'F': 'u',
                    'G': 'v',
                    'H': 'X',
                    'I': 'Y',
                    'J': 'Z'

            ]
    ]

    public static Map PATTERNING_MAP = [
            sheet    : 'Sheet1',
            startRow : 2,
            columnMap: [
                    'A' : 'waferId',
                    'B' : 'comment',
                    'C' : 'imprint_date',
                    'D' : 'SiNThickness',
                    'E' : 'stampId',
                    'F' : 'stampNumber',
                    'G' : 'imprint',
                    'H' : 'slot',
                    'I' : 'patternResist',
                    'J' : 'resistBatchNumber',
                    'K' : 'dispenseRpm',
                    'L' : 'dispenseAccn',
                    'M' : 'spinRpm',
                    'N' : 'spinAccn',
                    'O' : 'spinTime',
                    'P' : 'bakeTemperature',
                    'Q' : 'bakeTime',
                    'R' : 'loadTemperature',
                    'S' : 'vacuumPressure',
                    'T' : 'vacuumTimer',
                    'U' : 'alignedAngle',
                    'V' : 'pistonForce',
                    'W' : 'imprintTime',
                    'X' : 'highTemperature',
                    'Y' : 'hiTempTime',
                    'Z' : 'coolDownTemp',
                    'AA': 'afmDepthCenter1',
                    'AB': 'afmDepthCenter2',
                    'AC': 'afmDepthCenter3',
                    'AD': 'afmDepthEdge1',
                    'AE': 'afmDepthEdge2',
                    'AF': 'afmDepthEdge3',
                    'AG': 'nil_resist_witness_thickness',
                    'AH': 'nil_room_temp',
                    'AI': 'nil_particle_count'
            ]
    ]

    public static Map CL_MAP = [
            sheet    : 'Sheet1',
            startRow : 7,
            columnMap: [
                    'D': 'waferId',
                    'E': 'no_wires',
                    'F': 'pmt_voltage',
                    'G': 'wire_top_wl',
                    'H': 'wire_eave_wl',
                    'I': 'wire_middle_wl',
                    'J': 'wire_foot_wl'
            ]
    ]

    public static Map TAPE_INVENTORY_MAP = [
            sheet    : 'Sheet1',
            startRow : 1,
            columnMap: [
                    'A': 'waferId',
                    'B': 'deviceId',
                    'C': 'headerId',
                    'D': 'processStep'
            ]
    ]


    def execute(def username, def units, def f, def pctg, def pkey, def tkey) {

        if (tkey == "test_data_visualization") {
            return testDataVisuzalization(username, units, f)
        }
        if (tkey == "nil") {
            return nil(username, units, f)
        }
        if (tkey == "tape_inventory") {
            return tapeInventory(username, units, f)
        }
        if (tkey == "c_luminesece" || tkey == "cl_inventory") {
            return cl(username, units, f)
        }
        if (tkey in ["iblu_color_uniformity", "display_color_uniformity"]) {
            return colorUniformity(username, units, tkey, f)
        }
    }

    def colorUniformity(def username, def units, def tkey, def f) {

        if (units.size() > 1) {
            throw new RuntimeException("You can import data only for one unit at the time.")
        }
        def unit = Unit.get(units[0].id.toInteger())
        if (unit) {
            def db = mongo.getDB("glo")

            def stage = 'S01'
            def summary = ''
            def fn = f.fileItem.fileName.toUpperCase().tokenize(" ")
            if (fn.size() >= 2) {
                stage = fn[1]
                for (int i = 2; i < fn.size(); i++)
                    summary += fn[i] + ' '
            }
            summary.replaceAll("XLSX", "")

            XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(f.bytes))
            List rows = ExcelImportUtils.columns(workbook, KONIKA_SUMMARY_DATA_MAP)
            def us = [:]
            def vs = [:]
            def spots = 0
            for (row in rows) {

                if (row.position == "Max.") {
                    break;
                }
                spots++

                us.put((int) row.position, row.u)
                vs.put((int) row.position, row.v)

                def bdo = new BasicDBObject()
                bdo.put("packageId", unit.code)
                bdo.put("stage", stage)
                bdo.put("spots", spots)
                bdo.put("tkey", tkey)
                bdo.put("date", unit.actualStart)
                bdo.put("position", row.position)
                def kds = db.konikaDataSum.find(bdo).collect { it }[0]
                if (kds) {
                    bdo = kds
                }
                for (obj in KONIKA_SUMMARY_DATA_MAP.columnMap) {
                    bdo.put(obj.value, row[obj.value])
                }
                db.konikaDataSum.save(bdo)
            }

            def calc = { lst, p1, p2 ->
                def du = 0
                if (lst[p1]?.toString()?.isNumber() && lst[p2]?.toString()?.isNumber()) {
                    du = Math.pow(lst[p1] - lst[p2], 2)
                }
                du
            }

            def calculateArbitraryDiff = {
                def res = 0.0
                def size = us.size()
                for (int i = 1; i <= size; i++) {
                    for (int j = i + 1; j <= size; j++) {
                        def d = Math.sqrt(calc(us, i, j) + calc(vs, i, j))
                        if (d > res)
                            res = d
                    }
                }
                res
            }

            def calculateAdjacentDiff = { ->
                def size = us.size()
                def cols = 9
                def res = 0.0
                for (int i = 1; i <= size; i++) {
                    if (i + 1 <= size && i.mod(cols) != 0) {
                        def d = Math.sqrt(calc(us, i, i + 1) + calc(vs, i, i + 1))
                        if (d > res)
                            res = d
                    }
                    if (i + cols <= size) {
                        def d = Math.sqrt(calc(us, i, i + cols) + calc(vs, i, i + cols))
                        if (d > res)
                            res = d
                    }
                }
                res
            }

            def colorShift2Points = calculateArbitraryDiff()
            def colorShift2AdjPoints = calculateAdjacentDiff()
            def colorShift2PointsCIE1976LAB = 0.0

            def bdo2 = new BasicDBObject()
            bdo2.put("id", unit["_id"])
            bdo2.put(stage + "_summary", summary)
            bdo2.put(stage + "_colorShift2Points", colorShift2Points)
            bdo2.put(stage + "_colorShift2AdjPoints", colorShift2AdjPoints)
            bdo2.put(stage + "_colorShift2PointsCIE1976LAB", colorShift2PointsCIE1976LAB)
            bdo2.put("spots", spots)
            bdo2.put("processCategory", "Packages")
            bdo2.put("processKey", "iblu")
            bdo2.put("taskKey", tkey)
            unitService.update(bdo2, username, true)
        }
        1
    }

    private def testDataVisuzalization(def username, def units, def f) {

        def ret = 0
        def db = mongo.getDB("glo")

        if (units.size() > 1) {
            throw new RuntimeException("You can import data only for one unit at the time.")
        }
        def unit = Unit.get(units[0].id)

        def variable = null
        def variables = contentService.getVariables(unit['pctg'], unit['pkey'], unit['tkey'], ['dc'])
        for (def var in variables) {
            if (f.fileItem.fileName.toUpperCase().indexOf(var.name.toUpperCase()) >= 0) {
                variable = var
                break;
            }
        }

        if (variable == null) {
            throw new RuntimeException("Data collection variable for step '" + unit['tkey'] + "' with name matching beginning of file name '" + f.fileItem.fileName + "' is not defined.")
        }

        XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(f.bytes))
        XSSFSheet sheet = (XSSFSheet) workbook.getSheet("Summary");

        def grpCnt = 13
        def startCol = 3
        def waferCol = 1
        def deviceCol = 2
        def heads = []

        def root = new BasicDBObject()

        for (def i = 0; i < 17; i++) {

            XSSFRow row0 = (XSSFRow) sheet.getRow(0)
            def grp = row0.getCell(startCol + (i * grpCnt)).getStringCellValue()
            def lev = new BasicDBObject()
            boolean atLeastOne = false

            XSSFRow row1 = (XSSFRow) sheet.getRow(1)

            for (def j = startCol + (grpCnt * i); j < startCol + grpCnt + (grpCnt * i); j++) {
                def hval = row1.getCell(j).getStringCellValue().replace("\n", " ").replace(".", " ").replace("'", "")
                lev.put(hval, new BasicDBObject())
                ret = 0
                for (def row = 2; row <= 10000; row++) {
                    XSSFRow row2 = (XSSFRow) sheet.getRow(row)
                    def waferId = row2.getCell(waferCol).getStringCellValue()
                    def deviceId = row2.getCell(deviceCol).getStringCellValue()
                    if (waferId == null || waferId.trim() == "") {
                        break;
                    }

                    def cellval = row2.getCell(j).getRawValue()
                    if (cellval && cellval.isFloat()) {
                        Double fval = new Double(cellval)
                        lev[hval].put(deviceId, fval)
                        atLeastOne = true
                    }
                    ret++
                }
            }

            if (atLeastOne) {
                root.put(grp, lev)
            }
        }

        unit.dbo.put(variable.name, root)
        unit.dbo.put("id", unit["_id"])

        unitService.update(unit.dbo, username, false)

        ret
    }

    def nil(def username, def units, def f) {

        def ret = 0
        def db = mongo.getDB("glo")
        XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(f.bytes))
        List rows = ExcelImportUtils.columns(workbook, PATTERNING_MAP)
        for (row in rows) {

            if (row["waferId"] == null || row["waferId"].toString().trim() == "")
                continue

            def bdo = new BasicDBObject()
            for (obj in PATTERNING_MAP.columnMap) {
                bdo.put(obj.value, row[obj.value])
            }

            def query = new BasicDBObject();
            query.put("code", row["waferId"].toString().trim())
            def temp = db.unit.find(query, new BasicDBObject())
            def unit = temp.collect { it }[0]
            if (unit) {

                bdo.put("id", unit["_id"])
                bdo.put("processCategory", "nwLED")
                bdo.put("processKey", "patterning")
                bdo.put("taskKey", "nil")
                unitService.update(bdo, username, true)

                unitService.addNote(username, unit["_id"], row["comment"])

                ret++
            }
        }
        ret
    }

    def cl(def username, def units, def f) {

        def ret = 0
        def db = mongo.getDB("glo")
        XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(f.bytes))
        List rows = ExcelImportUtils.columns(workbook, CL_MAP)
        for (row in rows) {

            if (row["waferId"] == null || row["waferId"].toString().trim() == "")
                continue

            def bdo = new BasicDBObject()
            for (obj in CL_MAP.columnMap) {
                if (row[obj.value]) {
                    bdo.put(obj.value, row[obj.value])
                }
            }

            def query = new BasicDBObject();
            query.put("code", row["waferId"].toString().trim() + "CL")
            def temp = db.unit.find(query, new BasicDBObject())
            def unit = temp.collect { it }
            if (unit) {
                bdo.put("id", unit["_id"][0])
                bdo.put("processCategory", "nwLED")
                bdo.put("processKey", "cl")
                bdo.put("taskKey", "c_luminesece")
                unitService.update(bdo, username, false)
                ret++
            }
        }
        ret
    }

    def tapeInventory(def username, def units, def f) {

        def ret = 0
        def db = mongo.getDB("glo")
        XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(f.bytes))
        List rows = ExcelImportUtils.columns(workbook, TAPE_INVENTORY_MAP)

        for (row in rows) {

            if (row["waferId"] == null || row["waferId"].toString().trim() == "")
                continue
            if (row["deviceId"] == null || row["deviceId"].toString().trim() == "")
                continue

            def wid = row["waferId"].toString().trim().replace(".0", "")
            def did = row["deviceId"].toString().trim().replace(".0", "")

            if (!row["headerId"]) {
                throw new RuntimeException("Header ID is not supplied for wafer " + wid + " and device " + did)
            }

            if (!row["processStep"]) {
                throw new RuntimeException("Process step should be die_attach_lamp, die_attach_rel or die_attach_samples")
            }


            def unit = db.unit.find(new BasicDBObject("code", wid), new BasicDBObject()).collect { it }[0]
            if (!unit) {
                throw new RuntimeException("Wafer does not exist for id " + wid)
            }

            def subUnit = db.unit.find(new BasicDBObject("code", wid + "_" + did), new BasicDBObject()).collect {
                it
            }[0]
            if (!subUnit) {
                createSubUnit(db, unit, wid + "_" + did, "admin", "Die", row["processStep"], row["headerId"], false)
            } else {
                updateSubUnit(db, subUnit, "Die", row["processStep"], row["headerId"])
            }

            ret++
        }

        ret
    }

    def createSubUnit(def db, def unit, def subCode, def user, def pctg, def tkey, def headerId) {

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
        u2.save(failOnError: true)
        u2.indexing()

        def u = Unit.get(u2.id)
        unit.each { k, v ->
            if (k != "_id") {
                u.dbo.put(k, v)
            }
        }
        u.dbo.code = subCode
        u.dbo.genPath = unit.code + "," + subCode
        u.dbo.productCode = "105"
        u.dbo.product = "Device"
        u.dbo.productRevision = "1"
        u.dbo.put("parentCode", null)
        u.dbo.put("headerId", headerId)
        u.dbo.pctg = pctg
        u.dbo.pkey = "packaging"
        u.dbo.tkey = tkey
        u.dbo.tname = workflowService.getTaskName("packaging", tkey)
        u.dbo.start = new Date()
        u.dbo.actualStart = new Date()
        db.unit.save(u.dbo)

        historyService.initHistory("startDerived", unit, u.dbo, ["parentDelete", false])

    }

    def updateSubUnit(def db, def subUnit, def pctg, def tkey, def headerId) {

        def subHistory = db.history.find(new BasicDBObject("code", subUnit["code"]), new BasicDBObject()).collect {
            it
        }[0]
        if (subHistory["parentCode"])
            subHistory.remove("parentCode")
        subHistory.put("headerId", headerId)
        db.history.save(subHistory)

        subUnit.put("headerId", headerId)
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
        buf.units.add(n)

        unitService.move("admin", buf)
    }

    def uploadQev(def rows) {

        def i = 0
        def db = mongo.getDB("glo")

        rows.each { row ->

            def wid = row["waferId"]?.trim()
            def query = new BasicDBObject()
            query.put("waferId", wid.toString())
            def fields = new BasicDBObject()
            def temp = db.qev.find(query, fields)
            def qev = temp.collect { it }
            if (!qev) {
                qev = new BasicDBObject()
            } else {
                qev = qev[0]
            }

            row.each { k, v ->
                def val
                if (!v?.toString()?.isNumber()) {
                    val = v?.toString()?.trim()
                    if (val?.toUpperCase() == "N/A") {
                        val = null
                    }
                } else {
                    val = v
                }
                qev.put(k, val)
            }
            db.qev.save(qev)
            i++
        }
        i
    }


    def eBeamData(db, dir) {

        if (!dir) {
            logr.error("eBeam directory not specified.")
            return
        }

        java.io.File f = new java.io.File(dir)
        if (!f.exists()) {
            logr.error("eBeam '" + dir + "' does not exist.")
            return
        }

        def files = f.listFiles([accept: { file -> file ==~ /.*?\.TXT/ }] as FileFilter)?.toList()
        files.each { file ->
            def fn = file.getName().replace(".TXT", "").toUpperCase().tokenize("-")
            def query = new BasicDBObject("code", fn[0])
            def unit = db.dataReport.find(query, new BasicDBObject()).collect { it }[0]
            def dateModified = new Date(file.lastModified());
            if (!unit) {
                FileReader fr = null
                BufferedReader br = null
                try {
                    fr = new FileReader(file)
                    br = new BufferedReader(fr)
                    def resMap = [:]
                    def line
                    def depositCount = 0
                    def previousStatus = ''
                    while ((line = br.readLine()) != null) {
                        if (line != "") {
                            def row = line.split("\t")
                            if (row.length > 10) {
                                def ebeamrun = row[1]?.trim()
                                def time = row[2]?.trim()
                                def rate = row[3]?.trim()
                                def power = row[5]?.trim()
                                def temp = row[7]?.trim()
                                def status = row[23]?.trim()
                                if (status == "Deposit") {
                                    if (previousStatus != "Deposit") {
                                        depositCount += 1
                                    }
                                    if (rate.isFloat() && power.isFloat() && temp.isInteger()) {
                                        float fRate = rate.toFloat()
                                        float fPower = power.toFloat()
                                        int iTemp = temp.toInteger()
                                        if (!resMap[depositCount]) {
                                            resMap.put(depositCount, [])
                                        }
                                        resMap[depositCount].add([time, fPower, iTemp])
                                    }
                                }
                                previousStatus = status
                            }
                        }
                    }

                    // Process resMap
                    def dr = new BasicDBObject()
                    dr.put("parentCode", null)
                    dr.put("code", fn[0])
                    def obj = new BasicDBObject()
                    obj.put("active", "true")
                    obj.put("ebeam_run_id", fn[0])
                    obj.put("experimentId", fn[1])
                    obj.put("tags", [
                            "EquipmentStatus|ebeam_rundata",
                            fn[1],
                            fn[0]
                    ])
                    obj.put("pkey", "ebeam_rundata")
                    obj.put('actualStart', new Date())
                    obj.put('dateStart', dateModified)

                    resMap.each { idx, values ->
                        DescriptiveStatistics pows = new DescriptiveStatistics()
                        DescriptiveStatistics temps = new DescriptiveStatistics()
                        values.each { val ->
                            pows.addValue(val[1])
                            temps.addValue(val[2])
                        }
                        // Init temperature aggegations
                        obj.put("deposit_" + idx + "_temp_begin", values[0][2])
                        obj.put("deposit_" + idx + "_temp_end", values[values.size() - 1][2])
                        def t = temps.getStandardDeviation()
                        if (t && t.isNaN()) {
                        } else {
                            obj.put("deposit_" + idx + "_temp_stddev", t)
                        }
                        t = temps.getMean()
                        if (t && t.isNaN()) {
                        } else {
                            obj.put("deposit_" + idx + "_temp_avg", t)
                        }

                        // Init power aggegations
                        obj.put("deposit_" + idx + "_power_begin", values[0][1])
                        obj.put("deposit_" + idx + "_power_end", values[values.size() - 1][1])
                        t = pows.getStandardDeviation()
                        if (t && t.isNaN()) {
                        } else {
                            obj.put("deposit_" + idx + "_power_stddev", t)
                        }
                        t = pows.getMean()
                        if (t && t.isNaN()) {
                        } else {
                            obj.put("deposit_" + idx + "_power_avg", t)
                        }

                    }
                    dr.put("value", obj)
                    db.dataReport.save(dr)

                } catch (Exception exc) {
                    logr.warn(file?.getName() + ": " + exc.toString())
                }
                finally {
                    if (br != null)
                        br.close()
                    if (fr != null)
                        fr.close()
                    br = null
                    fr = null
                }
            }
        }
    }


    def eBeamItoData(db, dir) {

        if (!dir) {
            logr.error("eBeam ito directory not specified.")
            return
        }

        java.io.File f = new java.io.File(dir)
        if (!f.exists()) {
            logr.error("eBeam '" + dir + "' does not exist.")
            return
        }

        def files = f.listFiles([accept: { file -> file ==~ /.*?\.DAT/ }] as FileFilter)?.toList()
        files.each { file ->
            def fn = file.getName().replace(".DAT", "").toUpperCase().tokenize("-_ ")
            def query = new BasicDBObject("code", fn[0])
            def unit = db.dataReport.find(query, new BasicDBObject()).collect { it }[0]
            def dateModified = new Date(file.lastModified());
            if (!unit) {
                FileReader fr = null
                BufferedReader br = null
                try {
                    fr = new FileReader(file)
                    br = new BufferedReader(fr)
                    def resMap = [:]
                    def line
                    def depositCount = 0
                    def previousLine = ''
                    def obj = new BasicDBObject()
                    while ((line = br.readLine()) != null) {
                        if (line != "") {
                            if (previousLine.length() > 182 && line.length() > 182) {
                                try {
//                                    def num = previousLine.substring(1, 8).trim()
//                                    if (num.isFloat()) {
//                                        num = num.toFloat();
//                                    }
                                    def step = line.substring(182).trim()
                                    if (step == "Deposit#1" && !obj.temp_before_deposition) {
                                        obj.temp_before_deposition = previousLine.substring(51, 59).trim().toFloat()
                                        obj.pressure_before_deposition = previousLine.substring(152, 162).trim().toFloat()
                                    }
                                    if (step == "Process complet" && !obj.temp_end_deposition) {
                                        obj.temp_end_deposition = previousLine.substring(51, 59).trim().toFloat()
                                        obj.pressure_end_deposition = previousLine.substring(152, 162).trim().toFloat()
                                        obj.thickness_end_deposition = previousLine.substring(100, 110).trim().toFloat()
                                    }
                                } catch (Exception exc){
                                    def g = 0;
                                }
                            }
                            previousLine = line
                        }
                    }

                    // Process resMap
                    def dr = new BasicDBObject()
                    dr.put("parentCode", null)
                    dr.put("code", fn[0])
                    obj.put("active", "true")
                    obj.put("ebeam_ito_run_id", fn[0])
                    obj.put("experimentId", fn[1])
                    obj.put("tags", [
                            "EquipmentStatus|ebeam_ito_rundata",
                            fn[1],
                            fn[0]
                    ])
                    obj.put("pkey", "ebeam_ito_rundata")
                    obj.put('actualStart', new Date())
                    obj.put('dateStart', dateModified)
                    dr.put("value", obj)
                    db.dataReport.save(dr)

                } catch (Exception exc) {
                    logr.warn(file?.getName() + ": " + exc.toString())
                }
                finally {
                    if (br != null)
                        br.close()
                    if (fr != null)
                        fr.close()
                    br = null
                    fr = null
                }
            }
        }
    }
}
