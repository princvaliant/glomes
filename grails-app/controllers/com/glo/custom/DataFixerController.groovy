package com.glo.custom

import grails.converters.JSON
import java.text.*

import com.glo.ndo.*
import com.mongodb.BasicDBObject
import org.bson.types.ObjectId
import org.apache.commons.logging.LogFactory

class DataFixerController {

    def dataImportService
    def summarizeSyncService
    def testDataImagesService
    def sequenceGeneratorService
    def unitService
    def syncService
    def mongo

    private static final logr = LogFactory.getLog(this)

    def index = {

        redirect(action: "list", params: params)
    }

    def list = {

        def db = mongo.getDB("glo")
        def i = 0
        render(i.toString())
    }

    def importICP = {

        def db = mongo.getDB("glo")
        def query = new BasicDBObject()
        query.put("parentCode", null)
        query.put("value.active", "true")
        query.put("value.nil_etch.actualStart", new BasicDBObject('$exists', 1))
        def fields = new BasicDBObject()
        fields.put("id", 1)
        fields.put("code", 1)
        fields.put("value.nil_etch.movedBy", 1)

        def units = [:]
        units.put("data", [])

        db.dataReport.find(query, fields).collect {

            def unit = [:]
            unit.put('_id', it.id)
            unit.put('code', it.code)
            unit.put('pctg', "nwLED")
            unit.put('pkey', "patterning")
            units["data"].add(unit)
        }

        try {
            def ret = syncService.execute(units, null, params.tkey, "admin")
            render ret.toString()
        } catch (Exception exc) {
            render(exc.getMessage())
        }
    }

    private def saveDiffFromZeroHours(def db, def measure) {

        def query = new BasicDBObject("WaferID", measure.WaferID)
        if (measure.DeviceID)
            query.put("DeviceID", measure.DeviceID)
        query.put("TestType", measure.TestType)
        query.put("ResultType", measure.ResultType)
        query.put("HoursOn", 0)
        query.put("C", measure.C)
        def dataAtZero = db.measures.find(query, new BasicDBObject("wavelengthPowerScan", 0)).collect { it }[0]
        if (dataAtZero) {

            if (measure.photometric && dataAtZero.photometric)
                measure.put("photometricDelta", (float) (measure.photometric - dataAtZero.photometric))
            measure.put("eqeDelta", (float) (measure.eqe - dataAtZero.eqe))
            measure.put("VoltDelta", (float) (measure.Volt - dataAtZero.Volt))
            measure.put("PeakWavelengthDelta", (float) (measure.PeakWavelength - dataAtZero.PeakWavelength))
            measure.put("CentroidDelta", (float) (measure.Centroid - dataAtZero.Centroid))
            measure.put("dominantWavelengthDelta", (float) (measure.dominantWavelength - dataAtZero.dominantWavelength))
            measure.put("FWHMDelta", (float) (measure.FWHM - dataAtZero.FWHM))
            measure.put("xDelta", (float) (measure.x - dataAtZero.x))
            measure.put("yDelta", (float) (measure.y - dataAtZero.y))
            measure.put("zDelta", (float) (measure.z - dataAtZero.z))
            measure.put("uDelta", (float) (measure.u - dataAtZero.u))

            if (measure.photometric && dataAtZero.photometric)
                measure.put("photometricPerc", (float) (dataAtZero.photometric != 0 ? 100 * (measure.photometric - dataAtZero.photometric) / dataAtZero.photometric : 0))
            measure.put("eqePerc", (float) (dataAtZero.eqe != 0 ? 100 * (measure.eqe - dataAtZero.eqe) / dataAtZero.eqe : 0))
            measure.put("VoltPerc", (float) (dataAtZero.Volt != 0 ? 100 * (measure.Volt - dataAtZero.Volt) / dataAtZero.Volt : 0))
            measure.put("PeakWavelengthPerc", (float) (dataAtZero.PeakWavelength != 0 ? 100 * (measure.PeakWavelength - dataAtZero.PeakWavelength) / dataAtZero.PeakWavelength : 0))
            measure.put("CentroidPerc", (float) (dataAtZero.Centroid != 0 ? 100 * (measure.Centroid - dataAtZero.Centroid) / dataAtZero.Centroid : 0))
            measure.put("dominantWavelengthPerc", (float) (dataAtZero.dominantWavelength != 0 ? 100 * (measure.dominantWavelength - dataAtZero.dominantWavelength) / dataAtZero.dominantWavelength : -1))
            measure.put("FWHMPerc", (float) (dataAtZero.FWHM != 0 ? 100 * (measure.FWHM - dataAtZero.FWHM) / dataAtZero.FWHM : 0))
            measure.put("xPerc", (float) (dataAtZero.x != 0 ? 100 * (measure.x - dataAtZero.x) / dataAtZero.x : 0))
            measure.put("yPerc", (float) (dataAtZero.y != 0 ? 100 * (measure.y - dataAtZero.y) / dataAtZero.y : 0))
            measure.put("zPerc", (float) (dataAtZero.z != 0 ? 100 * (measure.z - dataAtZero.z) / dataAtZero.z : 0))
            measure.put("uPerc", (float) (dataAtZero.u != 0 ? 100 * (measure.u - dataAtZero.u) / dataAtZero.u : 0))
        }
        db.measures.save(measure)
    }

    def fixDateInMeasures = {

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        DateFormat df2 = new SimpleDateFormat("yyMMddHHmmss")
        DateFormat df3 = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss")
        def i = 0

        def db = mongo.getDB("glo")
        def ms = db.measures.find([TestType: 'char_test_visualization', DateTimeTested: null], [TimeRun: 1, sid: 1]).limit(500000).collect {
            it
        }

        ms.each {

            i++

            try {
                def date = df.parse(it.TimeRun.toString().replace("T", " ").substring(0, 19))

                db.measures.update(['_id': it._id], ['$set': new BasicDBObject("DateTimeTested", date)], false, true)
            } catch (Exception exc) {

                try {
                    def date2 = df2.parse(it.sid)
                    db.measures.update(['_id': it._id], ['$set': new BasicDBObject("DateTimeTested", date2)], false, true)
                } catch (Exception exc2) {
                }

                try {
                    def date3 = df3.parse(it.TimeRun.toString())
                    db.measures.update(['_id': it._id], ['$set': new BasicDBObject("DateTimeTested", date3)], false, true)
                } catch (Exception exc3) {

                    db.measures.update(['_id': it._id], ['$set': new BasicDBObject("DateTimeTested", it.TimeRun)], false, true)
                }
            }
        }

        render(i.toString())
    }


    def xy = {


        def db = mongo.getDB("glo")

        def i = 0
        def bdo = new BasicDBObject()
        bdo.put("value.code", "")

        def results = []

        DateFormat dformat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss")

//		def temp = db.testData.find(bdo, new BasicDBObject()).collect{
//
//			if (it.value.burning == "YES") {
//
//				def wid = it.value.code.tokenize("_")
//
//				db.measures.update(["WaferID":wid[0], "DeviceID": wid[1], "TestType":"wafer_rel_test"], ['$set':['burnin':'YES']], false, true)
//				i++
//			}
//		}

        def temp = db.testData.find(bdo, new BasicDBObject()).collect {

            def val = it.value
            if (val) {

                val.data.each { k, v ->

                    try {

                        def vsweep = []
                        def hours = k.toLong()

                        def VISwp = []

                        v?.setVoltage.each { voltValue, currentObject ->
                            if (currentObject?.current?.toFloat() >= 0) {

                                VISwp.add([voltValue.toFloat() / 1E6, currentObject.current.toFloat()])
                            }
                        }

                        v?.setCurrent?.each { curr, data ->

                            def result = new BasicDBObject()

                            // Indexing data
                            def wid = val.code.tokenize("_")
                            result.put("WaferID", wid[0])
                            if (wid.size() == 2) {
                                result.put("DeviceID", wid[1])
                            }
                            result.put("TestType", "wafer_rel_test")
                            result.put("ResultType", "CurrentSweepGreen")
                            result.put("NNEEWW", "true")
                            result.put("HoursOn", hours)
                            result.put("TempSetpoint", data.burnTemperature)

                            def C = curr.toFloat().round(0).toInteger().toString()

                            result.put("C", C)
                            result.put("G", C)
                            result.put("R", -1)
                            result.put("B", -1)

                            def exs = db.measures.find(result, [:]).collect { it }
                            if (exs) {
                                result.put("_id", exs._id)
                            }


                            result.put("CurrentSet", C.toFloat() / 1000)
                            result.put("GreenSet", C.toFloat() / 1000)
                            result.put("RedSet", -1)
                            result.put("BlueSet", -1)

                            // Meta data
                            result.put("TimeRun", dformat.format(new Date()))
                            result.put("TimeRunMs", 0)
                            result.put("fileName", '')
                            result.put("iniFileName", '')
                            result.put("station", '')
                            result.put("operName", '')
                            result.put("Comment", '')

                            // Electric data
                            result.put("Current", (data.current / 1000).toFloat())
                            result.put("Volt", data.voltage)
                            result.put("GreenCurr", (data.current / 1000).toFloat())
                            result.put("RedCurr", 0)
                            result.put("BlueCurr", 0)
                            result.put("GreenVolt", data.voltage)
                            result.put("RedVolt", 0)
                            result.put("BlueVolt", 0)
                            result.put("eqe", data.eqe)

                            // Optical data
                            result.put("IntegrationTime", 0)
                            result.put("FilterPosition", 0)
                            result.put("MaxAdcValue", 0)
                            result.put("radiometric", data.lop)
                            result.put("x", 0)
                            result.put("y", 0)
                            result.put("z", 0)
                            result.put("u", 0)
                            result.put("v1960", 0)
                            result.put("CCT_K", 0)
                            result.put("Duv", 0)
                            result.put("PeakWavelength", data.peak)
                            result.put("PeakWavelengthIntensity", 0)
                            result.put("Centroid", data.centroid)
                            result.put("dominantWavelength", data.dominant)
                            result.put("FWHM", data.fwhm)
                            result.put("CRI", 0)

                            // Environment data
                            result.put("Temp", -99)

                            result.put("wavelengthPowerScan", ['data': data.spectrum])
                            result.put("VISwp", ['data': VISwp])

                            result.putAll(addUnitData(db, wid[0] + "_" + wid[1]))

                            db.measures.save(result)

                            results.add(result)

                        }

                    } catch (Exception exc) {

                    }


                    i++

                }
            }
        }

        results.each {

            saveDiffFromZeroHours(db, it)
        }

        render i.toString()
    }


    def testfix = {


        def db = mongo.getDB("glo")

        def cs = ['UN0005568',
                  'UN0004546',
                  'UN0003338',
                  'UN0010403',
                  'UN0010398',
                  'AAF13150178P-21',
                  'UN0003539',
                  'UN0010309',
                  'AAF1450087P-29'
        ]


        def i = 0
        def bdo = new BasicDBObject()
        bdo.put("value.code", ['$in': cs])

        def results = []

        def temp = db.testData.find(bdo, new BasicDBObject()).collect {

            def val = it.value
            if (val) {
                val.data.each { k, v ->

                    def curr = k.tokenize('@')
                    if (curr && curr.size() == 2) {
                        Integer currVal = Integer.parseInt(curr[1].replaceAll("\\D+", ""))
                        Float currVal2 = currVal
                        currVal2 = currVal * 1E-9
                        if (curr[1].indexOf('mA') > 0) currVal2 = currVal * 1E-3
                        if (curr[1].indexOf('uA') > 0) currVal2 = currVal * 1E-6
                        if (curr[1].indexOf('nA') > 0) currVal2 = currVal * 1E-9
                        if (curr[1].indexOf('pA') > 0) currVal2 = currVal * 1E-12
                        if (curr[1].indexOf('fA') > 0) currVal2 = currVal * 1E-15
                        try {
                            def eqes = [:]
//                            v["Pulse Current (mA)"]?.each { p ->
//                                if (p.value < currVal2 * 800 || p.value > currVal2 * 1200) {
//                                    eqes.put(p.key)
//                                }
//                            }
                            v["EQE"]?.each { p ->
                                if (p.value > 30) {
                                    p.value = 0
                                }
                            }
                        } catch (Exception exc) {

                        }
                    }
                 }

                 results.add(it)
                 i++
            }
        }

        results.each {
            it.value.sync = ''
            db.testData.save(it)
        }

        results.each {
    //        dataImportService.testDataToMeasures(it.value.code, it.value.tkey)
        }

        render i.toString()
    }


    def xyb = {

        def db = mongo.getDB("glo")
        def i = 0

        db.unit.find(['boardId': ['$exists': 1]], new BasicDBObject()).collect {

            def relGroup = it.boardId + "_" + (it.loadNum?.toString() ?: "1")

            def wid = it.code.tokenize("_")
            def result = new BasicDBObject()
            result.put("WaferID", wid[0])
            if (wid.size() == 2) {
                result.put("DeviceID", wid[1])
            }

            db.measures.update(result, ['$set': new BasicDBObject("boardLoad", relGroup)], false, true)

            i++
        }

        render i.toString()
    }

    def plfix = {

        def db = mongo.getDB("glo")
        // Get directory
        String dir = grailsApplication.config.glo.bufferGrowthDirectory

        def set = new TreeMap()

        def dirloc = new File(dir).listFiles()?.toList()
        dirloc.each { directory ->

            if (directory.isDirectory()) {

                def files = directory.listFiles([accept: { file -> file ==~ /.*?\.txt/ }] as FileFilter)?.toList()

                files.each { file ->
                    try {
                        def fileName = file.path.substring(file.path.lastIndexOf("\\") + 1)
                        def modified = file.lastModified()
                        def ldate = new GregorianCalendar(2013, Calendar.JUNE, 11, 11, 0, 0).timeInMillis

                        if (modified > ldate && fileName.indexOf("_02_spl.txt") >= 0) {

                            set.put(fileName, file)
                        }

                    } catch (Exception exc) {
                        println(exc)
                    }
                }
            }
        }

        def output = []
        set.each { fn, file ->

            def line
            def obj = [:]

            obj.put("File name", fn)

            BufferedReader br = new BufferedReader(new FileReader(file))
            while ((line = br.readLine()) != null) {
                line = line + " "
                def row = line.split(':')
                if (row.length == 2) {

                    if (row[0]?.trim() == "Recipe") {
                        obj.put(row[0]?.trim(), row[1]?.trim())
                    }
                    if (row[0]?.trim() == "Slit width") {
                        obj.put(row[0]?.trim(), row[1]?.trim())
                    }
                    if (row[0]?.trim() == "Power") {
                        obj.put(row[0]?.trim(), row[1]?.trim())
                    }
                }
            }

            db.pldata.save(obj)
        }
    }


    def xyc = {

        def db = mongo.getDB("glo")
        def i = 0
        def result = new BasicDBObject()
        result.put("eqe", new BasicDBObject('$exists', true))
        db.measures.find(result, [:]).collect {

            def radioPower = it.radiometric ? it.radiometric.toFloat() : -1
            def centroid = it.Centroid ? it.Centroid.toFloat() : -1

            def current = -1
            def volt = -1

            if (it.ResultType.toUpperCase().indexOf("GREEN") >= 0) {

                current = it.GreenCurr ? it.GreenCurr.toFloat() : -1
                volt = it.GreenVolt ? it.GreenVolt.toFloat() : -1
            }
            if (it.ResultType.toUpperCase().indexOf("BLUE") >= 0) {

                current = it.BlueCurr ? it.BlueCurr.toFloat() : -1
                volt = it.BlueVolt ? it.BlueVolt.toFloat() : -1
            }
            if (it.ResultType.toUpperCase().indexOf("RED") >= 0) {

                current = it.RedCurr ? it.RedCurr.toFloat() : -1
                volt = it.RedVolt ? it.RedVolt.toFloat() : -1
            }



            if (volt * current > 0) {
                def vWpe = 100 * radioPower / (volt * current)
                if (vWpe >= 100) vWpe = 0
                it["wpe"] = vWpe
            }
            if (current > 0) {
                def vEqe = 80.66 * radioPower * centroid / (current * 1000)   //  0.8066 * 100%   (e/hc)
                if (vEqe >= 100) vEqe = 0
                it["eqe"] = vEqe
            }

            saveDiffFromZeroHours(db, it)
            i++
        }

        render i.toString()
    }

    def xyfix = {

        def db = mongo.getDB("glo")
        def i = 0
        def result = new BasicDBObject()
        result.put("TestType", "wafer_rel_test")
        db.measures.find(result, [:]).collect { measure ->

            try {
                saveDiffFromZeroHours(db, measure)
            } catch (Exception exc) {

            }
            i++
        }

        render i.toString()
    }


    def ws = {

        def db = mongo.getDB("glo")

        def bdo = new BasicDBObject()
        bdo.put("value.tkey", "wafer_level")
        bdo.put("value.type", "WavelengthStability")

        def temp = db.testData.find(bdo, new BasicDBObject()).collect { it }
        def i = 0
        temp.each {

            def val = it.value
            if (val) {

                def result = new BasicDBObject()

                // Indexing data
                def wid = val.code.tokenize("_")
                result.put("WaferID", wid[0])
                if (wid.size() == 2) {
                    result.put("DeviceID", wid[1])
                }
                result.put("TestType", "wafer_wst")
                result.put("ResultType", val.type)
                result.put("Encapsulated", val.data.Encapsulated ?: false)
                result.put("CurrentSet", val.data.GreenCurrentSetpoint ?: -1)
                result.put("GreenSet", val.data.GreenCurrentSetpoint ?: -1)
                result.put("RedSet", val.data.RedCurrentSetpoint ?: -1)
                result.put("BlueSet", val.data.BlueCurrentSetpoint ?: -1)
                result.put("HoursOn", val.data.HoursOn ?: -1)
                result.put("TempSetpoint", val.data.TempSetpoint ?: -99)
                result.put("Revision", val.data.Revision ?: 1)

                if (!db.measures.find(result, [:]).collect { it }) {

                    // Meta data
                    result.put("TimeRun", val.data.TimeRun)
                    result.put("TimeRunMs", val.data.TimeRunMillisecond ?: 0)
                    result.put("fileName", val.data.fileName)
                    result.put("iniFileName", val.data.iniFileName)
                    result.put("station", val.data.station)
                    result.put("operName", val.data.operName)
                    result.put("Comment", val.data.comment)

                    // Electric data
                    result.put("Current", val.data.GreenCurr)
                    result.put("Volt", val.data.GreenVolt)
                    result.put("GreenCurr", val.data.GreenCurr)
                    result.put("RedCurr", val.data.RedCurr)
                    result.put("BlueCurr", val.data.BlueCurr)
                    result.put("GreenVolt", val.data.GreenVolt)
                    result.put("RedVolt", val.data.RedVolt)
                    result.put("BlueVolt", val.data.BlueVolt)

                    if (val.data.GreenVolt * val.data.GreenCurr != 0) {
                        def vWpe = 100 * val.data.radiometric / (val.data.GreenVolt * val.data.GreenCurr)
                        if (vWpe >= 100) vWpe = 0
                        result.put("wpe", vWpe)
                    }
                    if (val.data.GreenCurr != 0) {
                        def vEqe = 80.66 * val.data.radiometric * val.data.Centroid / (val.data.GreenCurr * 1000)
                        //  0.8066 * 100%   (e/hc)
                        if (vEqe >= 100) vEqe = 0
                        result.put("eqe", vEqe)
                    }

                    // Optical data
                    result.put("IntegrationTime", val.data.IntegrationTime)
                    result.put("FilterPosition", val.data.FilterPosition)
                    result.put("MaxAdcValue", val.data.MaxAdcValue)
                    result.put("radiometric", val.data.radiometric)
                    result.put("photometric", val.data.photometric)
                    result.put("x", val.data.x)
                    result.put("y", val.data.y)
                    result.put("z", val.data.z)
                    result.put("u", val.data.u)
                    result.put("v1960", val.data.v1960)
                    result.put("CCT_K", val.data.CCT_K)
                    result.put("Duv", val.data.Duv)
                    result.put("PeakWavelength", val.data.PeakWavelength)
                    result.put("PeakWavelengthIntensity", val.data.PeakWavelengthIntensity)
                    result.put("Centroid", val.data.Centroid)
                    result.put("dominantWavelength", val.data.dominantWavelength)
                    result.put("FWHM", val.data.FWHM)
                    result.put("CRI", val.data.CRI)

                    // Environment data
                    result.put("Temp", val.data.Temp)

                    result.put("wavelengthPowerScan", val.data.wavelengthPowerScan)
                    result.put("VISwp", val.data.VISwp)

                    result.putAll(addUnitData(db, val.code))

                    db.measures.save(result)

                    i++
                }


            }
        }

        render i.toString()
    }


    def experimentfix = {

        def db = mongo.getDB("glo")
        def bdo = new BasicDBObject()
        bdo.put("pkey", "dvd_assembly")
        db.unit.find(bdo, new BasicDBObject()).collect { unit ->

            def exp = '';
            for (def e in unit.tagsCustom) {
                if (e.indexOf("EXP") >= 0) {
                    exp = e;
                }
            }

            db.unit.update(
                    new BasicDBObject("code", unit.code),
                    new BasicDBObject('$set', new BasicDBObject("experimentId", exp)))

            println(exp)
        }
    }

    def d65fix = {

        def db = mongo.getDB("glo")
        DateFormat df2 = new SimpleDateFormat("yyMMddHHmmss")

        def bdo = new BasicDBObject()
        bdo.put("tkey", "glo_camera_test")
        bdo.put("pkey", "iblu")

        db.unit.find(bdo, new BasicDBObject()).collect { unit ->

            def bdoUnit = new BasicDBObject()
            bdoUnit.put("id", unit["_id"])


            bdoUnit.put("processCategory", "Packages")
            bdoUnit.put("processKey", "iblu")
            bdoUnit.put("taskKey", "glo_camera_test")

            def tid = unit.testDataIndex ? unit.testDataIndex[unit.testDataIndex.size() - 1] : 0
            if (tid > 150000000000) {

                bdoUnit.put("testId", tid)
                def d = df2.parse(tid.toString())
                bdoUnit.put("testDate", d.format("yyyy-MM-dd"))

                println(bdoUnit);

                unitService.update(bdoUnit, "admin", true)
            }
        }
    }

    def getVI = {

        def db = mongo.getDB("glo")

        def bdo = new BasicDBObject()
        bdo.put("value.tkey", "test_data_visualization")
        bdo.put("value.testId", new BasicDBObject('$gt', 1000))

        def devs = new TreeSet()

        def f = new BasicDBObject("value.code", 1)
        f.put("value.testId", 1)
        f.put("value.parentCode", 1)

        db.testData.find(bdo, f).collect {

            if (it.value.parentCode == null && !(it.value.code in [
                    "A23100042",
                    "A23180099",
                    "A23180057"
            ])) {
                devs.add(it.value.code)
            }

        }

        devs.each {

            def bdo2 = new BasicDBObject()
            bdo2.put("code", it)
            def temp = db.history.find(bdo2, new BasicDBObject())

            temp.collect {

                def saveHist = []
                it["audit"].each { aud ->

                    if (!(aud.tkey in [
                            "test_data_visualization",
                            "char_test_visualization",
                            "top_test_visualization",
                            "inventory_test",
                            "pre_dbr_test",
                            "post_dbr_test"
                    ])) {

                        saveHist.add(aud)
                    }
                }

                it["audit"] = saveHist

                db.history.save(it)


                db.unit.update(bdo2, new BasicDBObject('$set', new BasicDBObject("testDataIndex", [])))
                db.unit.update(bdo2, new BasicDBObject('$set', new BasicDBObject("testTopIndex", [])))
                db.unit.update(bdo2, new BasicDBObject('$set', new BasicDBObject("testCharIndex", [])))
            }
        }

        render(devs as JSON)
    }


    def d65 = {

        def db = mongo.getDB("glo")

        def bdo = new BasicDBObject()
        bdo.put("value.tkey", "d65_after_encap")

        def i = 0
        def temp = db.testData.find(bdo, new BasicDBObject()).collect {

            def val = it.value
            if (val) {

                def result = new BasicDBObject()

                // Indexing data
                def wid = val.code.tokenize("_")
                result.put("WaferID", wid[0])
                if (wid.size() == 2) {
                    result.put("DeviceID", wid[1])
                }

                // Meta data
                Float gs = val.data.GreenCurrentSetpoint ? val.data.GreenCurrentSetpoint.toFloat() : -1
                Float bs = val.data.BlueCurrentSetpoint ? val.data.BlueCurrentSetpoint.toFloat() : -1
                Float rs = val.data.RedCurrentSetpoint ? val.data.RedCurrentSetpoint.toFloat() : -1
                Float gc = val.data.GreenCurr ? val.data.GreenCurr.toFloat() : -1
                Float bc = val.data.BlueCurr ? val.data.BlueCurr.toFloat() : -1
                Float rc = val.data.RedCurr ? val.data.RedCurr.toFloat() : -1
                if (gs > 0.17) gs = gs / 1000
                if (bs > 0.17) bs = bs / 1000
                if (rs > 0.17) rs = rs / 1000
                if (gc > 0.17) gc = gc / 1000
                if (bc > 0.17) bc = bc / 1000
                if (rc > 0.17) rc = rc / 1000

                def current = -1
                def currentSet = -1
                def volt = -1
                if (val.type == "CurrentSweepGreen") {

                    currentSet = gs
                    current = gc
                    volt = val.data.GreenVolt ? val.data.GreenVolt.toFloat() : -1
                }
                if (val.type == "CurrentSweepBlue") {

                    currentSet = bs
                    current = bc
                    volt = val.data.BlueVolt ? val.data.BlueVolt.toFloat() : -1
                }
                if (val.type == "CurrentSweepRed") {

                    currentSet = rs
                    current = rc
                    volt = val.data.RedVolt ? val.data.RedVolt.toFloat() : -1
                }

                result.put("TestType", "d65_after_encap")
                result.put("ResultType", val.type)
                result.put("Encapsulated", val.data.Encapsulated ?: false)
                result.put("CurrentSet", currentSet)
                result.put("GreenSet", gs)
                result.put("RedSet", rs)
                result.put("BlueSet", bs)
                result.put("HoursOn", val.data.HoursOn ?: -1)
                result.put("TempSetpoint", val.data.TempSetpoint ?: -99)
                result.put("Revision", val.data.Revision ?: 1)
                result.put("TimeRun", val.data.TimeRun)
                result.put("TimeRunMs", val.data.TimeRunMillisecond ?: 0)

                if (!db.measures.find(result, [:]).collect { it }) {

                    result.put("fileName", val.data.fileName)
                    result.put("iniFileName", val.data.iniFileName)
                    result.put("station", val.data.station)
                    result.put("operName", val.data.operName)
                    result.put("Comment", val.data.comment)

                    // Electric data
                    result.put("Current", current)
                    result.put("Volt", volt)
                    result.put("GreenCurr", gc)
                    result.put("RedCurr", rc)
                    result.put("BlueCurr", bc)
                    result.put("GreenVolt", val.data.GreenVolt)
                    result.put("RedVolt", val.data.RedVolt)
                    result.put("BlueVolt", val.data.BlueVolt)

                    if (volt * current != 0) {
                        def vWpe = 100 * val.data.radiometric / (volt * current)
                        if (vWpe >= 100) vWpe = 0
                        result.put("wpe", vWpe)
                    }
                    if (current != 0) {
                        def vEqe = 80.66 * val.data.radiometric * val.data.Centroid / (current * 1000)
                        //  0.8066 * 100%   (e/hc)
                        if (vEqe >= 100) vEqe = 0
                        result.put("eqe", vEqe)
                    }

                    // Optical data
                    result.put("IntegrationTime", val.data.IntegrationTime)
                    result.put("FilterPosition", val.data.FilterPosition)
                    result.put("MaxAdcValue", val.data.MaxAdcValue)
                    result.put("radiometric", val.data.radiometric)
                    result.put("photometric", val.data.photometric)
                    result.put("x", val.data.x)
                    result.put("y", val.data.y)
                    result.put("z", val.data.z)
                    result.put("u", val.data.u)
                    result.put("v1960", val.data.v1960)
                    result.put("CCT_K", val.data.CCT_K)
                    result.put("Duv", val.data.Duv)
                    result.put("PeakWavelength", val.data.PeakWavelength)
                    result.put("PeakWavelengthIntensity", val.data.PeakWavelengthIntensity)
                    result.put("Centroid", val.data.Centroid)
                    result.put("dominantWavelength", val.data.dominantWavelength)
                    result.put("FWHM", val.data.FWHM)
                    result.put("CRI", val.data.CRI)

                    // Environment data
                    result.put("Temp", val.data.Temp)

                    result.put("wavelengthPowerScan", val.data.wavelengthPowerScan)
                    result.put("VISwp", val.data.VISwp)

                    result.putAll(this.addUnitData(db, val.code))

                    db.measures.save(result)

                    i++
                }


            }
        }

        render i.toString()
    }

    def pcm = {

        def db = mongo.getDB("glo")
        summarizeSyncService.createSummaries(db, 600582, "CAH88382", null, null, null, 130930143500, "top_test_visualization", "MASK6", null)

        //	toAdd.put("testDataIndex_130524120005", "<Units>")

        render "S"
    }


    def measures = {

        def i = dataImportService.testDataToMeasures((params.code ?: ""), (params.tkey ?: ""))

        render i.toString()
    }

    def unif = {
        def db = mongo.getDB("glo")
        def pkey = "iblu"
        def pctg = "Packages"
        def tkey = "light_bar_uniformity_test"
        def bdo1 = new BasicDBObject()
        bdo1.put("TestType", "light_bar_uniformity_test")
        def temp = db.measures.find(bdo1).collect {

            def unit = db.unit.find(new BasicDBObject("code", it.WaferID), new BasicDBObject()).collect { it }[0]
            def bdoUnit = new BasicDBObject()

            it.customdata.each { prop, val ->
                if (val.getClass() == com.mongodb.BasicDBList) {
                    def mi = 1000000000
                    def ma = 0
                    def su = 0
                    def sz = val.size()
                    val.each { itm ->
                        if (itm.getClass() == Double || itm.getClass() == Integer) {
                            if (itm < mi)
                                mi = itm
                            if (itm > ma)
                                ma = itm
                            su += itm
                        }
                    }
                    if (sz > 0 && su > 0) {
                        def unif = (ma - mi) / (su / sz)
                        bdoUnit.put(prop + "Unif", unif)
                        bdoUnit.put(prop + "Sum", su)
                    }
                }
            }

            bdoUnit.put("id", unit["_id"])
            bdoUnit.put("processCategory", pctg)
            bdoUnit.put("processKey", pkey)
            bdoUnit.put("taskKey", tkey)
            unitService.update(bdoUnit, "admin", true)
        }
        render  'OK'
    }



    def konika = {

        def i = dataImportService.importKonikaImages((params.code ?: ""))

        render i.toString()
    }


    def getNonKarlsussFromMeasures = {

        def db = mongo.getDB("glo")

        def bdo = new BasicDBObject()

        def testType = ["test_data_visualization",
                        "nbp_test_data_visualization",
                        "top_test_visualization",
                        "char_test_visualization",
                        "uniformity_test_visualization",
                        "full_test_visualization",
                        "spc_data_visualization",
                        "pre_dbr_test",
                        "post_dbr_test",
                        "intermediate_coupon_test"]
        bdo.put("TestType", ['$nin': testType])
        bdo.put("photometric", ['$nin': [null, 0, -1]])
        bdo.put("photometric", ['$gt': (float) 0])
        bdo.put("radiometric", ['$gt': (float) 0.000001])

        def fields = new BasicDBObject("photometric", 1)
        fields.put("radiometric", 1)

        def i = 0
        def temp = db.measures.find(bdo, fields).limit(1000000).collect {

            float pr = (float) ((it.photometric ?: 0) / (it.radiometric ?: 1))

            [p: it.photometric, r: it.radiometric, pr: pr]
        }.findAll { it.pr < 1 }

        if (!temp)
            temp = ["NONE"]

        render(temp as JSON)


    }


    def epi = {

        def db = mongo.getDB("glo")

        def i = 0
        def temp = db.epiRun.find(new BasicDBObject(), new BasicDBObject()).collect { it }

        temp.each { run ->

            def equip = Equipment.findByName(run.equipmentName)

            if (equip) {

                run.put('workcenter', equip.workCenter?.name)
                db.epiRun.save(run)
            }
        }

        render("success" as JSON)

    }


    def wstGet = {

        def db = mongo.getDB("glo")

        def bdo = new BasicDBObject()
        bdo.put("TestType", "wafer_wst")
        bdo.put("RawPeakWavelength", new BasicDBObject('$exists', false))

        def i = 0
        def temp = db.measures.find(bdo, new BasicDBObject("wavelengthPowerScan", 1)).limit(1).collect {
            [id: it._id.toString(), data: it.wavelengthPowerScan.data]
        }[0]

        if (!temp)
            temp = "NONE"

        render(temp as JSON)

    }

    def wstSave = {

        def db = mongo.getDB("glo")

        def param = request.JSON

        def bdo = new BasicDBObject()
        bdo.put("_id", new ObjectId(param.id))

        def temp = db.measures.find(bdo).collect { it }[0]
        if (temp) {

            def upd = new BasicDBObject()

            upd.put("RawPeakWavelength", temp.PeakWavelength)
            upd.put("RawPeakWavelengthIntensity", temp.PeakWavelengthIntensity)
            upd.put("PeakWavelength", param.PeakWavelength)
            upd.put("PeakWavelengthIntensity", param.PeakWavelengthIntensity)
            upd.put("Peak2Wavelength", param.Peak2Wavelength)
            upd.put("Peak2WavelengthIntensity", param.Peak2WavelengthIntensity)
            upd.put("Peak3Wavelength", param.Peak3Wavelength)
            upd.put("Peak3WavelengthIntensity", param.Peak3WavelengthIntensity)
            upd.put("Peak4Wavelength", param.Peak4Wavelength)
            upd.put("Peak4WavelengthIntensity", param.Peak4WavelengthIntensity)

            db.measures.update(bdo, new BasicDBObject('$set', upd))
        }

        render "success"
    }


    def packageCodeInMeasures = {

        def db = mongo.getDB("glo")

        def bdo = new BasicDBObject()
        bdo.put("code", "TAB01140519P-08")
        def temp = db.dataReport.find(bdo).collect { it }[0]

        def bdo1 = new BasicDBObject()
        bdo1.put("code", "TAB01140519P-04")
        def temp1 = db.dataReport.find(bdo1).collect { it }[0]

        temp.value.epi_growth.runNumber = "S2950226D"
        temp1.value.epi_growth.runNumber = "S2950226D"
        temp.value.ni_dot_test.runNumber = "S2950226D"
        temp1.value.ni_dot_test.runNumber = "S2950226D"
        temp.value.ni_dot_inventory.runNumber = "S2950226D"
        temp1.value.ni_dot_inventory.runNumber = "S2950226D"

        db.dataReport.save(temp)
        db.dataReport.save(temp1)

        render "success"
    }

    def konicaDataFix = {

        def db = mongo.getDB("glo")

        def kons = db.konica.find([testId: ['$gt': 150602000000]]).sort([code: 1, testId: 1]).collect { it }


        int i = 0;
        kons.each { kon ->

            def minLv = 1000000
            def maxLv = -1
            db.konicaData.find([code: kon.code, testId: kon.testId, testType: '135 spots']).collect {
                if (it.spot in [119, 122, 125, 93, 97, 65, 68, 71, 39, 43, 11, 14, 17]) {
                    if (it.Lv < minLv)
                        minLv = it.Lv
                    if (it.Lv > maxLv)
                        maxLv = it.Lv
                }
            }
            if (minLv < 1000000 && maxLv != 0) {
                db.konica.update([_id: kon._id], ['$set': ['unif_13pt_from135': 100 * minLv / maxLv, 'min_13pt_from135': minLv]])
            }

            println(kon.code + ' ' + kon.testId)
        }

        render "success " + i
    }

    def konicaDataFix3 = {

        def db = mongo.getDB("glo")

        def kons = db.konica.find([code: ['$in': ['dsd']], top200: ['$ne': true]]).sort([code: 1, testId: 1]).collect {
            it
        }

        int i = 0;
        kons.each { kon ->

            println(kon.code + ' ' + kon.testId)

            def kds = db.konicaData.find([code: kon.code, testId: kon.testId, testType: '135 spots', spot: [$gte: 0]]).sort([spot: 1]).collect {
                it
            }
            kds.each {
                int j = it.spot / 9.001
                def r = 1 + (it.spot - 1) % 9
                def m = 10 - r

                def rspot = j * 9 + m

                db.konicaData.update([_id: it._id], ['$set': [spot: rspot]])

            }

            def kds2 = db.konicaData.find([code: kon.code, testId: kon.testId, testType: '69 spots', spot: [$gte: 0]]).sort([spot: 1]).collect {
                it
            }
            kds2.each {

                int j = it.spot / 23.001
                def r = 1 + (it.spot - 1) % 23
                def m = 24 - r

                def rspot = j * 23 + m

                db.konicaData.update([_id: it._id], ['$set': [spot: rspot]])
            }

            i++
        }
        render "success " + i
    }


    def calculate126and84pts = {

        def db = mongo.getDB("glo")

        def kons = db.konica.find([top200: ['$ne': true]]).sort([code: 1, testId: 1, sid: 1]).collect { it }


        int i = 0;
        kons.each { kon ->

            def kds = db.konicaData.find([code: kon.code, testId: kon.testId, sid: kon.sid, testType: '135 spots']).sort([spot: 1]).collect {
                it
            }

            if (kds.size() > 0) {

                def sum = colorUniformity(kds, 84) + colorUniformity(kds, 126)


                kon.putAll(sum)
                db.konica.save(kon)

                def unit = db.unit.find(new BasicDBObject("code", kon.code), new BasicDBObject()).collect {
                    it
                }[0]
                if (unit) {
                    i++
                    sum.put("id", unit["_id"])
                    sum.put("processCategory", "Packages")
                    sum.put("processKey", "iblu")
                    sum.put("taskKey", 'glo_camera_test')
                    unitService.update(sum, "admin", true)
                }
            }
        }

        render "success " + i
    }

    def measureToKonica = {

        def ret = dataImportService.importKonikaFromMeasures()

        render ret as JSON
    }

    def colorUniformity(def rows, def pts) {

        def us = [:]
        def vs = [:]
        def size = rows.size()

        if (pts == 84) {
            for (row in rows) {
                int s = row.spot.toInteger()
                if (s > 18 && s <= 126 && !(s in [1, 10, 19, 28, 37, 46, 55, 64, 73, 82, 91, 100, 109, 118, 127, 9, 18, 27, 36, 45, 54, 63, 72, 81, 90, 99, 108, 117, 126, 135])) {
                    us.put((int) s, row.u)
                    vs.put((int) s, row.v)
                }
            }
        }
        if (pts == 126) {
            for (row in rows) {
                int s = row.spot.toInteger()
                if (s > 9) {
                    us.put((int) s, row.u)
                    vs.put((int) s, row.v)
                }
            }
        }
        us = us.sort()
        vs = vs.sort()

        def calc = { lst, p1, p2 ->
            def du = 0
            if (lst[p1]?.toString()?.isNumber() && lst[p2]?.toString()?.isNumber()) {
                du = Math.pow(lst[p1] - lst[p2], 2)
            }
            du
        }

        def calculateArbitraryDiff = {
            def res = 0.0
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
        return ["""color_shift_arbitrary_${pts}pt""": colorShift2Points, """color_shift_adjacent_${
            pts
        }pt"""                                      : colorShift2AdjPoints]
    }


    private def addUnitData(def db, def code) {

        def fields = new BasicDBObject()
        fields.put("mask", 1)
        fields.put("runNumber", 1)
        fields.put("recipeName", 1)
        fields.put("build_number", 1)
        fields.put("headerId", 1)
        fields.put("tray_id", 1)
        fields.put("experimentId", 1)
        fields.put("polish", 1)
        fields.put("supplier", 1)
        fields.put("dieOrder", 1)
        fields.put("posOnBoard", 1)
        fields.put("boardId", 1)
        fields.put("loadNum", 1)

        def unit = db.unit.find(new BasicDBObject("code", code), fields).collect { it }[0]
        if (!unit) {
            unit = db.unitarchive.find(new BasicDBObject("code", code), fields).collect { it }[0]
            if (!unit) {
                unit = db.unit.find(new BasicDBObject("code", code.tokenize("_")[0]), fields).collect { it }[0]
                if (!unit) {
                    unit = db.unitarchive.find(new BasicDBObject("code", code.tokenize("_")[0]), fields).collect {
                        it
                    }[0]
                }
            }
        }


        if (!unit)
            unit = [:]
        else
            unit.remove("_id")

        if (unit.boardId && unit.loadNum)
            unit.put("boardLoad", unit.boardId + "_" + unit.loadNum)

        unit
    }

    def testtest = {
        println(params);
        def result = []
        for (def i = 0 ; i < 300; i++) {
            result.add([_id:i,name: 'Sasha' +i, category: 'Category1'])
        }
        render "${params.callback}(${result as JSON})"
    }


    def omega = {

        def db = mongo.getDB("glo")

        String dir = grailsApplication.config.glo.omegaSensorDirectory
        if (!dir) {
            logr.error("Omega sensor directory not specified.")
            return
        }

        java.io.File f = new java.io.File(dir)
        if (!f.exists()) {
            logr.error("Omega sensor '" + dir + "' does not exist.")
            return
        }

        def files = f.listFiles([accept: { file -> file ==~ /.*?\.txt/ }] as FileFilter)?.toList()
        files.each { file ->
            def sensor = file.getName().replace(".txt", "").toLowerCase()
            def room = sensor.tokenize(" ")[0]
            if (room == "room304" || room == "room306") {

                def bdo = new BasicDBObject()
                bdo.put("parentCode", null)
                bdo.put("value.tags", [
                        "EquipmentStatus",
                        "omega_sensor",
                        room
                ])
                bdo.put("value.pkey", "omega_sensor")
                def lastEntry = db.dataReport.find(bdo, new BasicDBObject()).addSpecial('$orderby', new BasicDBObject("code", -1)).limit(1).collect {
                    it
                }[0]
                int hourOfDay = 0
                def lastDate = "2016-01-01"
                if (lastEntry) {
                    hourOfDay = lastEntry.value.hourOfDay
                    lastDate = lastEntry.value.actualStart.format("yyyy-MM-dd")
                }


                FileReader fr = null
                BufferedReader br = null
                try {

                    fr = new FileReader(file)
                    br = new BufferedReader(fr)

                    def resMap = [:]
                    def resMap1 = [:]

                    def line

                    while ((line = br.readLine()) != null) {
                        if (line?.trim() != "") {
                            def row = line.split(',')
                            if (row.length > 2) {
                                def datetime = row[1]?.trim()
                                def temperature = row[2]?.trim()
                                def humidity = row[3]?.trim()
                                if (datetime && temperature && humidity) {
                                    def dt = datetime.tokenize(' ')
                                    if (temperature.isFloat() && humidity.isFloat()) {
                                        float fT = temperature.toFloat()
                                        float fH = humidity.toFloat()
                                        if (dt.size() == 2) {
                                            def actst = dt[0].tokenize("-")
                                            def actualStart =
                                                    String.format("%02d", actst[0].toInteger()) + "-" +
                                                            String.format("%02d", actst[1].toInteger()) + "-" +
                                                            String.format("%02d", actst[2].toInteger())
                                            def dthm = dt[1].tokenize(":")
                                            int dth = dthm[0].toInteger()
                                            int dtm = dthm[1].toInteger()

                                            if (actualStart + "_" + String.format("%02d", dth) >= lastDate + "_" + String.format("%02d", hourOfDay)) {

                                                def dr = new BasicDBObject()
                                                dr.put("parentCode", null)
                                                def code = "OSS" + sequenceGeneratorService.next("omega_sensor").toString().padLeft(6, '0')
                                                dr.put("code", code)

                                                def obj = new BasicDBObject()
                                                obj.put("active", "true")
                                                obj.put("tags", [
                                                        "EquipmentStatus",
                                                        "omega_sensor",
                                                        room
                                                ])
                                                obj.put("pkey", "omega_sensor")
                                                obj.put("sensor", room)
                                                obj.put("temperature", fT)
                                                obj.put("temperature_max", fT)
                                                obj.put("humidity", fH)
                                                obj.put("humidity_max", fH)
                                                obj.put('actualStart', new Date().parse("yyyy-MM-dd HH:mm", actualStart + " " + dth + ":" + dtm))
                                                obj.put('hourOfDay', dth)
                                                obj.put('valuePerMinute', dtm)

                                                dr.put("value", obj)
                                                db.dataReport.save(dr)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

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

    def recurse = {

        new File("/Users/aleksandarvolos/projects/meteor_packages/extjs61/resources/images").eachDirRecurse() { dir ->
            def files = dir.listFiles().toList()
            files.each { file ->
                println "api.addAssets('" + file.getPath().replace("/Users/aleksandarvolos/projects/meteor_packages/extjs61/", "") + "', 'client');"
            }
        }

    }




}
