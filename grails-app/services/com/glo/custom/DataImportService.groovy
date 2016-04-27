package com.glo.custom

import java.text.DateFormat
import java.text.SimpleDateFormat
import org.apache.commons.logging.LogFactory
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics
import org.apache.commons.math3.stat.regression.SimpleRegression
import com.glo.ndo.*
import com.mongodb.BasicDBObject


class DataImportService {

    private static final logr = LogFactory.getLog(this)

    def mongo
    def unitService
    def relSyncService
    def jmsService
    def summarizeSyncService
    def sequenceGeneratorService
    def grailsApplication
    def readFileService

    DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    DateFormat df2 = new SimpleDateFormat("yyMMddHHmmss")

    static exposes = ['jms']

    def enqueue(def action, def options, def toSave) {

        // Perform history update based on old and new version through async messaging
        def message = [action: action, options: options, toSave: toSave]

        onMessage(message)
    }

    def onMessage(def args) {

        def action = args.get("action")
        def options = args.get("options")
        def toSave = args.get("toSave")

        // Method async executed to save history for 4 actions (start,update,move and loss)
        switch (action) {
            case "D65":
                processD65(options, toSave)
                break
            case "IBLU":
                processIblu(options, toSave)
                break
            case "TOP200":
                processTop200(options, toSave)
                break
            case "WST":
                processWST(options, toSave)
                break
            case "MWT":
                processMWT(options, toSave)
                break
            case "WSTCOMPLETE":
                completeWST(options, toSave)
                break
            case "NIDOT":
                processNIDOT(options, toSave)
                break
            case "NIDOTCOMPLETE":
                completeNIDOT(options, toSave)
                break
            case "XY":
                processXY(options, toSave)
                break
            case "XYCOMPLETE":
                completeXY(options, toSave)
                break
            case "KARLSUSS":
                karlSuss(options, toSave)
                break
            case "KARLSUSSDEVICE":
                karlSussDevice(options, toSave)
                break
            case "KARLSUSSSPC":
                karlSussSpc(options, toSave)
                break
            case "LAMP":
                processLamp(options, toSave)
                break
            case "WAFERREL":
                processWaferRel(options, toSave)
                break
            case "DATAUPLOAD":
                processDataUpload(options, toSave)
                break
            case "KONICA":
                processKonica(options, toSave)
                break
            case "LIGHT_BAR_UNIFORMITY":
                processLightBarUniformity(options, toSave)
                break
        }
    }

    def processKonica(def var, def toSave) {

        def db = mongo.getDB("glo")
        def tkey = "glo_camera_test"
        def unitCode = var.code
        def unit = db.unit.find(new BasicDBObject("code", unitCode), new BasicDBObject()).collect { it }[0]
        if (!unit) {
            throw new RuntimeException("Unit with serial# " + unitCode + " does not exist in MES")
        }
        if (!unitService.inStep(db, unitCode, "", tkey)) {
            throw new RuntimeException("Unit " + unitCode + " never passed through step " + tkey + ". Move the package to that step in MES.")
        }
        def testId = df2.format(new Date()).toLong()
        def bdoUnit = new BasicDBObject()
        bdoUnit.put("id", unit["_id"])
        bdoUnit.put("testDataIndex", [])
        if (!unit["testDataIndex"]) {
            bdoUnit["testDataIndex"].add(testId)
        } else {
            bdoUnit["testDataIndex"].addAll(unit["testDataIndex"])
            bdoUnit["testDataIndex"].add(testId)
        }
        var.summary.each { k, v ->
            bdoUnit.put(k, v)
        }
        bdoUnit.put("comment", var.comment)
        bdoUnit.put("testId", testId)
        bdoUnit.put("testDate", (new Date()).format("yyyy-MM-dd"))
        bdoUnit.put("testedBy", var.testedBy)
        bdoUnit.put("red_current", var.red_current)
        bdoUnit.put("green_current", var.green_current)
        bdoUnit.put("blue_current", var.blue_current)
        bdoUnit.put("red_voltage", var.red_voltage)
        bdoUnit.put("green_voltage", var.green_voltage)
        bdoUnit.put("blue_voltage", var.blue_voltage)
        bdoUnit.put("temperature", var.temperature)
        bdoUnit.put("screenSize", var.screenSize)
        bdoUnit.put("processCategory", "Packages")
        bdoUnit.put("processKey", "iblu")
        bdoUnit.put("taskKey", tkey)

        // Save last test to unit
        unitService.update(bdoUnit, "admin", true)
        if (var.comment)
            unitService.addNote("admin", unit["_id"], testId + ": " + var.comment)
        bdoUnit = null

        // Save summary data
        def konica = new BasicDBObject("code", var.code)
        konica.put("testId", testId)
        var.summary.each { k, v ->
            konica.put(k, v)
        }
        konica.put("red_current", var.red_current)
        konica.put("green_current", var.green_current)
        konica.put("blue_current", var.blue_current)
        konica.put("red_voltage", var.red_voltage)
        konica.put("green_voltage", var.green_voltage)
        konica.put("blue_voltage", var.blue_voltage)
        konica.put("temperature", var.temperature)
        konica.put("comment", var.comment)
        konica.put("testedBy", var.testedBy)
        konica.put("is_baseline", unit["is_baseline"])
        konica.put("date", new Date())
        db.konica.save(konica)

        // Save all data
        var.data.each { spot, values ->
            values.each { idx, pair ->
                def dt = new BasicDBObject("code", var.code)
                dt.put("testId", testId)
                dt.put("comment", var.comment)
                dt.put("testedBy", var.testedBy)
                dt.put("testType", spot)
                dt.put("red_current", var.red_current)
                dt.put("green_current", var.green_current)
                dt.put("blue_current", var.blue_current)
                dt.put("red_voltage", var.red_voltage)
                dt.put("green_voltage", var.green_voltage)
                dt.put("blue_voltage", var.blue_voltage)
                dt.put("temperature", var.temperature)
                dt.put("spot", Integer.parseInt(idx))
                dt.put("date", new Date())
                dt.putAll(pair)
                db.konicaData.save(dt)
            }
        }

        // Store raw data
        def raw = new BasicDBObject("code", var.code)
        raw.put("testId", testId)
        raw.put("date", new Date())
        var.raw?.each { key, values ->
            raw.put(key, values)
        }
        db.konicaRaw.save(raw)

        return testId
    }


    def processWestboro(def code, def testId, def date, def head, def data) {

        def db = mongo.getDB("glo")
        def tkey = "glo_westboro_test"
        def unit = db.unit.find(new BasicDBObject("code", code), new BasicDBObject()).collect { it }[0]
        if (!unit) {
            throw new RuntimeException("Unit with serial# " + code + " does not exist in MES")
        }
        if (!unitService.inStep(db, code, "", tkey)) {
            throw new RuntimeException("Unit " + code + " never passed through step " + tkey + ". Move the package to that step in MES.")
        }
        def bdoUnit = new BasicDBObject()
        bdoUnit.put("id", unit["_id"])
        bdoUnit.put("testDataIndex", [])
        if (!unit["testDataIndex"]) {
            bdoUnit["testDataIndex"].add(testId)
        } else {
            bdoUnit["testDataIndex"].addAll(unit["testDataIndex"])
            bdoUnit["testDataIndex"].add(testId)
        }
        def summaries = [:]
        data.each { spot, v ->
            summaries.putAll(calculateWestboroSummaries(spot, v))
        }
        bdoUnit.putAll(summaries)
        bdoUnit.put("comment", head.comment)
        bdoUnit.put("testId", testId)
        bdoUnit.put("testDate", date.format("yyyy-MM-dd"))
        bdoUnit.put("testedBy", head.testedBy)
        bdoUnit.put("red_current", head.red_current)
        bdoUnit.put("green_current", head.green_current)
        bdoUnit.put("blue_current", head.blue_current)
        bdoUnit.put("red_voltage", head.red_voltage)
        bdoUnit.put("green_voltage", head.green_voltage)
        bdoUnit.put("blue_voltage", head.blue_voltage)
        bdoUnit.put("temperature", head.temperature)
        bdoUnit.put("screenSize", head.screenSize)
        bdoUnit.put("processCategory", "Packages")
        bdoUnit.put("processKey", "iblu")
        bdoUnit.put("taskKey", tkey)

        // Save last test to unit
    //    unitService.update(bdoUnit, "admin", true)
    //    if (head.comment)
    //        unitService.addNote("admin", unit["_id"], testId + ": " + head.comment)
        bdoUnit = null

        // Save summary data
        def konica = new BasicDBObject("code", code)
        konica.put("testId", testId)
        konica.putAll(summaries)
        konica.put("red_current", head.red_current)
        konica.put("green_current", head.green_current)
        konica.put("blue_current", head.blue_current)
        konica.put("red_voltage", head.red_voltage)
        konica.put("green_voltage", head.green_voltage)
        konica.put("blue_voltage", head.blue_voltage)
        konica.put("temperature", head.temperature)
        konica.put("comment", head.comment)
        konica.put("testedBy", head.testedBy)
        konica.put("is_baseline", unit["is_baseline"])
        konica.put("camera", "w")
        konica.put("date", date)
//        db.konica.save(konica)

        // Save all data
        data.each { spot, values ->
            values.each { idx, pair ->
                def dt = new BasicDBObject("code", code)
                dt.put("testId", testId)
                dt.put("comment", head.comment)
                dt.put("testedBy", head.testedBy)
                dt.put("testType", spot + " spots")
                dt.put("red_current", head.red_current)
                dt.put("green_current", head.green_current)
                dt.put("blue_current", head.blue_current)
                dt.put("red_voltage", head.red_voltage)
                dt.put("green_voltage", head.green_voltage)
                dt.put("blue_voltage", head.blue_voltage)
                dt.put("temperature", head.temperature)
                dt.put("spot", Integer.parseInt(idx))
                dt.put("date", date)
                dt.put("camera", "w")
                dt.putAll(pair)
 //               db.konicaData.save(dt)
            }
        }

        // Store raw data
//        def raw = new BasicDBObject("code", var.code)
//        raw.put("testId", testId)
//        raw.put("date", new Date())
//        var.raw?.each { key, values ->
//            raw.put(key, values)
//        }
//        db.konicaRaw.save(raw)

    }

    def calculateWestboroSummaries(def name, def result) {
        def output = [:];
        if (name == "13")
        {
            output.put("center_lv", result[7]["Lv"]);
            output.put("center_x", result[7]["x"]);
            output.put("center_y", result[7]["y"]);
            output.put("center_u", result[7]["u"]);
            output.put("center_v", result[7]["v"]);
            output.put("center_tcp", result[7]["tcp"]);
        }
        if (name == "135")
        {
            output.put("center_lv_135pt", result[68]["Lv"]);
            output.put("center_x_135pt", result[68]["x"]);
            output.put("center_y_135pt", result[68]["y"]);
            output.put("center_u_135pt", result[68]["u"]);
            output.put("center_v_135pt", result[68]["v"]);
            output.put("center_tcp_135pt", result[68]["tcp"]);
        }

        float minLv = -1;
        float maxLv = -1;
        float minx = -1;
        float maxx = -1;
        float miny = -1;
        float maxy = -1;
        float minu = -1;
        float maxu = -1;
        float minv = -1;
        float maxv = -1;
        float mintcp = -1;
        float maxtcp = -1;
        float sum = 0;
        float sumX = 0;
        float sumZ = 0;
        float sumpur = -1;
        float sumdw = -1;

        def u = [:];
        def v = [:];
        def u84 = [:];
        def v84 = [:];
        def u126 = [:];
        def v126 = [:];

        result.eachWithIndex { key, pair ->
            if (minLv == -1 || minLv > pair["Lv"])
            {
                minLv = pair["Lv"];
            }
            if (maxLv == -1 || maxLv < pair["Lv"])
            {
                maxLv = pair["Lv"];
            }
            if (minx == -1 || minx > pair["x"])
            {
                minx = pair["x"];
            }
            if (maxx == -1 || maxx < pair["x"])
            {
                maxx = pair["x"];
            }
            if (miny == -1 || miny > pair["y"])
            {
                miny = pair["y"];
            }
            if (maxy == -1 || maxy < pair["y"])
            {
                maxy = pair["y"];
            }

            if (minu == -1 || minu > pair["u"])
            {
                minu = pair["u"];
            }
            if (maxu == -1 || maxu < pair["u"])
            {
                maxu = pair["u"];
            }
            if (minv == -1 || minv > pair["v"])
            {
                minv = pair["v"];
            }
            if (maxv == -1 || maxv < pair["v"])
            {
                maxv = pair["v"];
            }

            if (mintcp == -1 || mintcp > pair["tcp"])
            {
                mintcp = pair["tcp"];
            }
            if (maxtcp == -1 || maxtcp < pair["tcp"])
            {
                maxtcp = pair["tcp"];
            }
            sum += pair["Lv"];
      //      sumX += pair["X"];
      //      sumZ += pair["Z"];

            sumpur += pair["pur"];
            sumdw += pair["dw"];

            u.put(key, pair["u"]);
            v.put(key, pair["v"]);

            int[] s = [1, 10, 19, 28, 37, 46, 55, 64, 73, 82, 91, 100, 109, 118, 127, 9, 18, 27, 36, 45, 54, 63, 72, 81, 90, 99, 108, 117, 126, 135];
            if (key > 18 && key <= 126 && !s.contains(key)) {
                u84.put(key, pair["u"]);
                v84.put(key, pair["v"]);
            }
            if (pair.Key > 9) {
                u126.put(key, pair["u"]);
                v126.put(key, pair["v"]);
            }

        }


//        if (name == "13")
//        {
//            double avg = sum / 13;
//            double avgX = sumX / 13;
//            double avgZ = sumZ / 13;
//            double center = result[7]["Lv"];
//            double centerX = result[7]["X"];
//            double centerZ = result[7]["Z"];
//
//            double maxAvg = -1;
//            double max7 = -1;
//            double maxAvgdE = -1;
//            double max7dE = -1;
//
//            result.eachWithIndex { key, pair ->
//                        double lv = pair["Lv"];
//                        double X = pair["X"];
//                        double Z = pair.Value["Z"];
//                        if (lv > 0 && avg > 0 && center > 0)
//                        {
//                            double ysvL = 116 * System.Math.Pow((lv / avg), (1.0 / 3.0)) - 16;
//                            double ysv7 = 116 * System.Math.Pow((lv / center), (1.0 / 3.0)) - 16;
//
//                            if (System.Math.Abs(ysvL - 100) > maxAvg)
//                                maxAvg = System.Math.Abs(ysvL - 100);
//                            if (System.Math.Abs(ysv7 - 100) > max7)
//                                max7 = System.Math.Abs(ysv7 - 100);
//
//                            double ysvL1 = System.Math.Pow((lv / avg), (1.0 / 3.0));
//                            double ysv71 = System.Math.Pow((lv / center), (1.0 / 3.0));
//
//                            double xsvL = System.Math.Pow((X / avgX), (1.0 / 3.0));
//                            double xsv7 = System.Math.Pow((X / centerX), (1.0 / 3.0));
//
//                            double zsvL = System.Math.Pow((Z / avgZ), (1.0 / 3.0));
//                            double zsv7 = System.Math.Pow((Z / centerZ), (1.0 / 3.0));
//
//                            double aL = 500 * (xsvL - ysvL1);
//                            double a7 = 500 * (xsv7 - ysv71);
//
//                            double bL = 200 * (ysvL1 - zsvL);
//                            double b7 = 200 * (ysv71 - zsv7);
//
//                            double dEavg = System.Math.Pow((
//                                    System.Math.Pow((ysvL - 100), 2.0) +
//                                            System.Math.Pow(aL, 2.0) +
//                                            System.Math.Pow(bL, 2.0)
//                            ), (1.0 / 2.0));
//
//                            double dE7 = System.Math.Pow((
//                                    System.Math.Pow((ysv7 - 100), 2.0) +
//                                            System.Math.Pow(a7, 2.0) +
//                                            System.Math.Pow(b7, 2.0)
//                            ), (1.0 / 2.0));
//
//                            if (dEavg > maxAvgdE)
//                                maxAvgdE = dEavg;
//                            if (dE7 > max7dE)
//                                max7dE = dE7;
//                        }
//                    }
//
//            output.Add("dLstar7", (float)max7);
//            output.Add("dLstarAvg", (float)maxAvg);
//            output.Add("dE7", (float)max7dE);
//            output.Add("dEAvg", (float)maxAvgdE);
//        }

        String nme = name;
//        if (name == "805" || name == "807")
//        {
//            nme = "80";
//        }

        if (maxLv > 0)
            output.put("unif_" + name + "pt", 100 * minLv / maxLv);
        if (minLv > 0)
            output.put("min_" + name + "pt", minLv);
        if (sum > 0)
            output.put("avg_" + name + "pt", sum / nme.toFloat());
        if (sumpur > 0)
            output.put("pur_" + name + "pt", sumpur / nme.toFloat());
        if (sumdw > 0)
            output.put("dw_" + name + "pt", sumdw / nme.toFloat());

        output.put("ciex_" + name + "pt", maxx - minx);
        output.put("ciey_" + name + "pt", maxy - miny);
        output.put("u_" + name + "pt", maxu - minu);
        output.put("v_" + name + "pt", maxv - minv);
        output.put("tcp_" + name + "pt", maxtcp > 0 ? (float)(100 * mintcp / maxtcp) : 0);

        // Calculate arbitrary points
        output.put("color_shift_arbitrary_" + name + "pt", arbitrary(u, v, nme.toInteger()));
        // Calculate adjacent points
        output.put("color_shift_adjacent_" + name + "pt", adjacent(u, v, 9, nme.toInteger()));

        if (name == "135")
        {
            output.put("color_shift_arbitrary_126pt", arbitrary(u126, v126, nme.toInteger()));
            output.put("color_shift_adjacent_126pt", adjacent(u126, v126, 9, nme.toInteger()));
            output.put("color_shift_arbitrary_84pt", arbitrary(u84, v84, nme.toInteger()));
            output.put("color_shift_adjacent_84pt", adjacent(u84, v84, 9, nme.toInteger()));
        }

        return output;
    }

    private float arbitrary(def u, def v, def size)
    {
        float res = 0;
        for (int i = 1; i <= size; i++)
        {
            for (int j = i + 1; j <= size; j++)
            {
                float d = (float)Math.Sqrt(power(u, i, j) + power(v, i, j));
                if (d > res)
                    res = d;
            }
        }

        return res;
    }

    private float adjacent(def u, def v, short cols, def size)
    {
        float res = 0;
        for (int i = 1; i <= size; i++)
        {
            if (i + 1 <= size &&  i % cols != 0)
            {
                float d = (float) Math.sqrt(power(u, i, i + 1) + power(v, i, i + 1));
                if (d > res)
                    res = d;
            }
            if (i + cols <= size)
            {
                float d = (float) Math.sqrt(power(u, i, i + cols) + power(v, i, i + cols));
                if (d > res)
                    res = d;
            }
        }
        return res;
    }

    private float power(def u, int p1, int p2)
    {
        if (u[p1] && u[p2])
        {
            return (float)Math.pow(u[p1] - u[p2], 2);
        }
        else
        {
            return 0;
        }
    }

    def processLamp(def var, def toSave) {

        def db = mongo.getDB("glo")

        def tkey = "sphere_test_lamp_board"
        def sync = "lampBoardTestSynced"
        if (var.Encapsulated == "TRUE") {
            tkey = "sphere_test2_lamp_board"
            sync = "lampBoardTest2Synced"
        }
        def resType = var.resultType ?: var.ResultType
        if (!(resType in [
                "CurrentSweepGreen",
                "CurrentSweepBlue",
                "CurrentSweepRed"
        ])) {
            throw new RuntimeException("Lamp not valid data type: " + resType)
        }

        def unitCode = var.WaferID + (var.DeviceID ? "_" + var.DeviceID : "")
        def unit = db.unit.find(new BasicDBObject("code", unitCode), new BasicDBObject()).collect { it }[0]
        if (!unit) {
            throw new RuntimeException("Unit with serial# " + unitCode + " does not exist in MES")
        }

        if (!unitService.inStep(db, unitCode, "", tkey)) {

            throw new RuntimeException("Unit " + unitCode + " never passed through step " + tkey)
        }

        def measure
        if (toSave) {
            measure = saveMeasureRecord(db, var.WaferID, var.DeviceID, "sphere_test_lamp_board", var)
        } else {
            measure = var
        }

        // Calculate K-factor
        def kFactorsH = [:]
        def kFactorsE = [:]
        def eqeH = [:]
        def eqeE = [:]

        if (measure.C in ["5000", "10000", "20000"]) {
            def td = getLopTestData(db, measure.WaferID, measure.DeviceID)

            Float curr = measure.CurrentSet * 1000
            if (td[curr] && td[curr] != 0) {
                float calc = measure.eqe / td[curr]
                if (var.Encapsulated == "FALSE") {
                    kFactorsH.put(curr.toInteger(), calc)
                    eqeH.put(curr.toInteger(), measure.eqe)
                } else {
                    kFactorsE.put(curr.toInteger(), calc)
                    eqeE.put(curr.toInteger(), measure.eqe)
                }
            }
        }
        def bdoUnit = new BasicDBObject()

        kFactorsH.each { current, kFactor ->
            bdoUnit.put("kFactor" + current.toInteger() + "_H", kFactor)
            measure.put("kFactor", kFactor)
        }
        eqeH.each { current, eqe ->
            bdoUnit.put("eqe" + current.toInteger() + "_H", eqe)
        }
        kFactorsE.each { current, kFactor ->
            bdoUnit.put("kFactor" + current.toInteger() + "_E", kFactor)
            measure.put("kFactor", kFactor)
        }
        eqeE.each { current, eqe ->
            bdoUnit.put("eqe" + current.toInteger() + "_E", eqe)
        }

        db.measures.save(measure)
        measure = null

        bdoUnit.put("id", unit["_id"])
        bdoUnit.put(sync, "YES")
        bdoUnit.put("lampBoardTestData", [unit["code"]])
        bdoUnit.put("relGroup", var.BoardId + "_" + var.loadNum?.toString()?.padLeft(2, '0'))
        bdoUnit.put("processCategory", "Die")
        bdoUnit.put("processKey", "packaging")
        bdoUnit.put("taskKey", tkey)

        unitService.update(bdoUnit, "admin", true)

        bdoUnit = null
    }


    def processXY(def var, def toSave) {

        def db = mongo.getDB("glo")


        def tkey = var.TestType
        def pkey = "test_die"
        def sync = "relTestSynced"

        if (!(var.TestType in [
                "test_rel_board",
                "spc_standard_board"
        ])) {
            throw new RuntimeException("XY not valid test type: " + var.TestType)
        }

        def resType = var.resultType ?: var.ResultType
        if (!(resType in [
                "CurrentSweepGreen",
                "CurrentSweepBlue",
                "CurrentSweepRed"
        ])) {
            throw new RuntimeException("XY not valid data type: " + resType)
        }

        def unitCode = var.WaferID + (var.DeviceID ? "_" + var.DeviceID : "")
        def unit = db.unit.find(new BasicDBObject("code", unitCode), new BasicDBObject()).collect { it }[0]
        if (!unit) {
            throw new RuntimeException("Unit with serial# " + unitCode + " does not exist in MES")
        }

        if (!unitService.inStep(db, unitCode, "", tkey)) {

            throw new RuntimeException("Unit " + unitCode + " never passed through step " + tkey)
        }

        def measure
        if (toSave) {
            measure = saveMeasureRecord(db, var.WaferID, var.DeviceID, tkey, var)

            if (tkey == "test_rel_board") {
                saveDiffFromZeroHours(db, measure)
            }
        } else {
            measure = var
        }

        def bdoUnit = new BasicDBObject()
        bdoUnit.put("id", unit["_id"])
        bdoUnit.put(sync, "YES")
        bdoUnit.put("relTestData", [unit["code"]])
        bdoUnit.put("relTestSynced", "YES")
        bdoUnit.put("relGroup", var.BoardId + "_" + var.loadNum.toString())
        bdoUnit.put("processCategory", "Die")
        bdoUnit.put("processKey", "packaging")
        bdoUnit.put("taskKey", tkey)
        unitService.update(bdoUnit, "admin", true)

        bdoUnit = null
    }


    def completeXY(def parms, def toSave) {

        def db = mongo.getDB("glo")

        if (parms.TestType == "spc_standard_board") {

            startSpcProcess(db, parms.sid)
        }
    }

    def karlSuss(def var, def toSave) {

        def db = mongo.getDB("glo")

        def unit = db.unit.find(new BasicDBObject("code", var.value.code)).collect { it }[0]
        if (!unit) {
            throw new RuntimeException("Unit with serial# " + var.value.code + " does not exist in MES")
        }

        if (!unitService.inStep(db, var.value.code, "", var.value.tkey)) {
            if (!(var.value.tkey in ["top_test_visualization", "uniformity_test_visualization"]))
                throw new RuntimeException("Unit " + var.value.code + " never passed through step " + var.value.tkey)
        }

        def bdo = new BasicDBObject()
        bdo.put("value.code", var.value.code)
        bdo.put("value.tkey", var.value.tkey)
        bdo.put("value.testId", var.value.testId)

        def testData = db.testData.find(bdo, new BasicDBObject()).collect { it }[0]
        if (!testData) {
            testData = new BasicDBObject()
        }
        testData.put("value", var.value)

        def udbo = new BasicDBObject()
        udbo.put('devlist', var.value.devlist)
        addTestId(unit, var.value.tkey, var.value.testId, udbo)

        testData["value"].put("date", new Date())
        testData["value"].put("experimentId", unit["experimentId"])
        testData["value"].put("parentCode", null)
        testData["value"].put("syncType", "C")

        db.testData.save(testData)

        if (var.value.tkey in ["top_test_visualization", "uniformity_test_visualization"]) {

            // Move unit there
            def buf = new Expando()
            buf.isEngineering = true
            buf.prior = 50
            buf.processCategoryEng = "nwLED"
            buf.processKeyEng = "test"
            buf.taskKeyEng = var.value.tkey
            buf.units = []
            def n = [:]
            n.put('transition', 'engineering')
            n.put('id', unit["_id"])
            buf.units.add(n)
            unitService.move("admin", buf)
        }

        summarizeSyncService.createSummaries(db, unit._id, unit.code, udbo, null, null, var.value.testId.toString().toLong(), var.value.tkey, unit.mask, null)
    }


    def karlSussDevice(def var, def toSave) {

        def db = mongo.getDB("glo")

        def bdo = new BasicDBObject()
        bdo.put("value.code", var.value.code)
        bdo.put("value.tkey", var.value.tkey)
        bdo.put("value.testId", var.value.testId)

        def testData = db.testData.find(bdo, new BasicDBObject()).collect { it }[0]
        if (!testData) {
            testData = new BasicDBObject()
            testData.put("value", var.value)
        }

        testData["value"].put("date", new Date())
        testData["value"].put("syncType", "C")

        db.testData.save(testData)
    }

    def karlSussSpc(def var, def toSave) {

        def db = mongo.getDB("glo")

        def unitWafer = db.unit.find(new BasicDBObject("code", var.value.code), [:]).collect { it }[0]

        // Start instance of unit
        def recv = new Expando()
        def product = Product.findByCode("karlSussSpcData")
        recv.pid = product.id
        recv.cid = product.productCompanies.toArray()[0].id
        recv.uid = "admin"
        recv.units = []

        def m = [:]
        m.put('code', '')
        m.put('qty', 1)
        m.put('waferCode', unitWafer.code)
        m.put('experimentId', unitWafer.code)
        m.put('mask', unitWafer.mask)

        recv.units.add(m)
        def code = unitService.start(recv, "Rel", "karl_suss")

        // Save this
        def testData = new BasicDBObject()
        testData.put("value", var.value)
        testData["value"].put("code", code)
        testData["value"].put("experimentId", unitWafer.code)
        testData["value"].put("date", new Date())
        testData["value"].put("parentCode", null)
        testData["value"].put("syncType", "C")
        db.testData.save(testData)


        def unit = db.unit.find(new BasicDBObject("code", code), [:]).collect { it }[0]

        summarizeSyncService.createSummaries(db, unit._id, unit.code, unit, unit.pctg, unit.pkey, var.value.testId.toString().toLong(), unit.tkey, unitWafer.mask, null)

        this.testDataToMeasures("", "")
    }

    def processMWT(def var, def toSave) {

        def tkey = var.TestType
        def pkey
        def pctg
        if (tkey == "manual_wafer_test") {
            pkey = "test"
            pctg = "nwLED"
        } else {
            throw new RuntimeException("Invalid test type " + tkey)
        }

        def sync = "mwtSynced"

        def resType = var.resultType ?: var.ResultType
        if (!(resType in [
                "CurrentSweepGreen",
                "CurrentSweepRed",
                "CurrentSweepBlue"
        ])) {
            throw new RuntimeException("MWT not valid data type: " + resType)
        }

        def db = mongo.getDB("glo")
        if (!unitService.inStep(db, var.WaferID, "", tkey)) {
            throw new RuntimeException("Unit " + var.WaferID + " never passed through step " + tkey)
        }

        def measure
        if (toSave && var.eqe > 0 && var.eqe < 50) {
            measure = saveMeasureRecord(db, var.WaferID, var.DeviceID, tkey, var)
        } else {
            measure = var
        }

        def punit = db.unit.find(new BasicDBObject("code", var.WaferID), new BasicDBObject()).collect { it }[0]
        if (!punit[sync]) {
            def bdoUnit = new BasicDBObject()
            bdoUnit.put("id", punit["_id"])
            bdoUnit.put(sync, "YES")
            bdoUnit.put(sync + "TestData", [punit.code])
            bdoUnit.put("testMWTIndex", [])
            if (!punit["testMWTIndex"]) {
                bdoUnit["testMWTIndex"].add(var.sid)
            } else {
                bdoUnit["testMWTIndex"].addAll(punit["testMWTIndex"])
                bdoUnit["testMWTIndex"].add(var.sid)
            }
            bdoUnit.put("processCategory", pctg)
            bdoUnit.put("processKey", pkey)
            bdoUnit.put("taskKey", tkey)
            unitService.update(bdoUnit, "admin", true)
        }
    }

    def processWST(def var, def toSave) {

        def tkey = var.TestType
        def pkey
        def pctg
        if (tkey == "wafer_wst") {
            pkey = "test"
            pctg = "nwLED"
        }
        if (tkey == "die_wst") {
            pkey = "packaging"
            pctg = "Die"
        }
        if (tkey in ["package_wst", "package_wst_eng"]) {
            pkey = "final_package"
            pctg = "Packages"
        }

        def sync = "wstSynced"

        def resType = var.resultType ?: var.ResultType
        if (!(resType in [
                "WavelengthStabilityGreen",
                "WavelengthStabilityRed",
                "WavelengthStabilityBlue"
        ])) {
            throw new RuntimeException("WST not valid data type: " + resType)
        }

        def db = mongo.getDB("glo")
        def unitCode = var.WaferID + (var.DeviceID ? "_" + var.DeviceID : "")

        if (!(unitService.inStep(db, var.WaferID, "", tkey) || unitService.inStep(db, var.WaferID + "_" + var.DeviceID, "", tkey))) {

            throw new RuntimeException("Unit " + unitCode + " never passed through step " + tkey)
        }

        def measure
        if (toSave) {
            measure = saveMeasureRecord(db, var.WaferID, var.DeviceID, tkey, var)
        } else {
            measure = var
        }

        def punit
        if (tkey in ["wafer_wst", "package_wst"])
            punit = db.unit.find(new BasicDBObject("code", var.WaferID), new BasicDBObject()).collect { it }[0]
        else
            punit = db.unit.find(new BasicDBObject("code", unitCode), new BasicDBObject()).collect { it }[0]

        if (!punit[sync]) {

            def bdoUnit = new BasicDBObject()
            bdoUnit.put("id", punit["_id"])
            bdoUnit.put(sync, "YES")
            bdoUnit.put(sync + "TestData", [punit.code])
            bdoUnit.put("processCategory", pctg)
            bdoUnit.put("processKey", pkey)
            bdoUnit.put("taskKey", tkey)
            unitService.update(bdoUnit, "admin", true)
        }
    }

    def completeWST(def var, def toSave) {

        def db = mongo.getDB("glo")

        startWSTProcess(db, var.sid)
    }

    def processNIDOT(def var, def toSave) {

        def tkey = var.TestType
        def pkey
        def pctg
        if (tkey == "ni_dot_test") {
            pkey = "epi"
            pctg = "nwLED"
        }
        def sync = "probeTestSynced"

        def db = mongo.getDB("glo")
        def unitCode = var.WaferID + (var.DeviceID ? "_" + var.DeviceID : "")

        if (!unitService.inStep(db, var.WaferID, "", tkey)) {
            throw new RuntimeException("Unit " + unitCode + " never passed through step " + tkey)
        }

        def measure
        if (toSave) {
            measure = saveMeasureRecord(db, var.WaferID, var.DeviceID, tkey, var)
        } else {
            measure = var
        }

        def punit
        punit = db.unit.find(new BasicDBObject("code", var.WaferID), new BasicDBObject()).collect { it }[0]
        if (!punit[sync]) {
            def bdoUnit = new BasicDBObject()
            bdoUnit.put("id", punit["_id"])
            bdoUnit.put(sync, "YES")
            bdoUnit.put(sync + "Experiment", [punit.code])
            bdoUnit.put("processCategory", pctg)
            bdoUnit.put("processKey", pkey)
            bdoUnit.put("taskKey", tkey)
            unitService.update(bdoUnit, "admin", true)
        }
    }

    def completeNIDOT(def var, def toSave) {

        def db = mongo.getDB("glo")
        startNIDOTProcess(db, var.sid)
    }

    def processD65(def var, def toSave) {

        def pkey = "packaging"
        def pctg = "Die"

        if (!var.DeviceID) {
            pkey = "final_package"
            pctg = "Packages"
        }

        def tkey = var.Encapsulated?.toUpperCase() == "FALSE" ? "d65_before_encap" : "d65_after_encap"
        def sync = var.Encapsulated?.toUpperCase() == "FALSE" ? "d65SyncedBefore" : "d65SyncedAfter"

        if (var.TestType == "d65_rel") {
            tkey = "d65_rel_test"
            sync = "d65RelSync"
        }

        if (var.TestType == "d65_eng") {
            tkey = "d65_after_encap_eng"
            sync = "d65SyncedAfter"
        }

        if (var.TestType in ["light_bar_rel", "ilgp_rel", "iblu_rel"]) {
            pkey = "iblu_rel"
            pctg = "Packages"
            tkey = var.TestType + "_test"
            sync = var.TestType + "Sync"
        }

        if (var.TestType in ["light_bar_sml_sphere_test"]) {
            pkey = "iblu"
            pctg = "Packages"
            tkey = "light_bar_sml_sphere_test"
            sync = "LightBarSmallSync"
        }

        def resType = var.resultType ?: var.ResultType
        if (!(resType in [
                "White",
                "Green",
                "Red",
                "Blue",
                "CurrentSweepGreen",
                "CurrentSweepBlue",
                "CurrentSweepRed",
                "WhiteOperatingPoint",
                "pcRed",
                "pBlue"
        ])) {
            throw new RuntimeException("D65 not valid data type: " + resType)
        }

        def db = mongo.getDB("glo")
        def unitCode = var.WaferID + (var.DeviceID ? "_" + var.DeviceID : "")
        def unit = db.unit.find(new BasicDBObject("code", unitCode), new BasicDBObject()).collect { it }[0]
        if (!unit) {
            throw new RuntimeException("Unit with serial# " + unitCode + " does not exist in MES")
        }

        if (!unitService.inStep(db, unitCode, "", tkey)) {

            throw new RuntimeException("Unit " + unitCode + " never passed through step " + tkey)
        }

        def measure
        if (toSave) {
            measure = saveMeasureRecord(db, var.WaferID, var.DeviceID, tkey, var)
        } else {
            measure = var
        }

        def bdoUnit = new BasicDBObject()
        if (measure.ResultType == "Green") {
            bdoUnit.put("greenCurrent", measure.GreenCurr)
            bdoUnit.put("greenVoltage", measure.GreenVolt)
            bdoUnit.put("greenPeak", measure.PeakWavelength)
            bdoUnit.put("greenDominant", measure.dominantWavelength)
            bdoUnit.put("greenCentroid", measure.Centroid)
            bdoUnit.put("greenPhotometric", measure.photometric)
            bdoUnit.put("greenRadiometric", measure.radiometric)
            bdoUnit.put("greenFWHM", measure.FWHM)
            bdoUnit.put("greenCCTK", measure.CCT_K)

            if (bdoUnit.greenVoltage > 0 && bdoUnit.greenCurrent > 0 && bdoUnit.greenRadiometric > 0 && bdoUnit.greenCentroid > 0 && bdoUnit.greenPeak > 0) {

                def vWpe = 100 * bdoUnit.greenRadiometric / (bdoUnit.greenVoltage * bdoUnit.greenCurrent)
                if (vWpe >= 100) vWpe = 0
                def vEqe = 80.66 * bdoUnit.greenRadiometric * bdoUnit.greenCentroid / (bdoUnit.greenCurrent * 1000)
                //  0.8066 * 100%   (e/hc)
                if (vEqe >= 100) vEqe = 0

                bdoUnit.put("greenEqe", vEqe)
                bdoUnit.put("greenWpe", vWpe)
            }

            bdoUnit.put("greenPeakWavelengthIntensity", measure.PeakWavelengthIntensity)
        }
        if (measure.ResultType in ["Red", "pcRed"]) {

            def redRt = measure.ResultType.toLowerCase()

            bdoUnit.put(redRt + "Current", measure.RedCurr)
            bdoUnit.put(redRt + "Voltage", measure.RedVolt)
            bdoUnit.put(redRt + "Peak", measure.PeakWavelength)
            bdoUnit.put(redRt + "Dominant", measure.dominantWavelength)
            bdoUnit.put(redRt + "Centroid", measure.Centroid)
            bdoUnit.put(redRt + "Photometric", measure.photometric)
            bdoUnit.put(redRt + "Radiometric", measure.radiometric)
            bdoUnit.put(redRt + "FWHM", measure.FWHM)
            bdoUnit.put(redRt + "CCTK", measure.CCT_K)


            if (measure.ResultType == "Red" && bdoUnit.redVoltage > 0 && bdoUnit.redCurrent > 0 && bdoUnit.redRadiometric > 0 && bdoUnit.redCentroid > 0 && bdoUnit.redPeak > 0) {

                def vWpe = 100 * bdoUnit.redRadiometric / (bdoUnit.redVoltage * bdoUnit.redCurrent)
                if (vWpe >= 100) vWpe = 0
                def vEqe = 80.66 * bdoUnit.redRadiometric * bdoUnit.redCentroid / (bdoUnit.redCurrent * 1000)
                //  0.8066 * 100%   (e/hc)
                if (vEqe >= 100) vEqe = 0

                bdoUnit.put("redEqe", vEqe)
                bdoUnit.put("redWpe", vWpe)
            }

            bdoUnit.put("redPeakWavelengthIntensity", measure.PeakWavelengthIntensity)
        }
        if (measure.ResultType == "Blue") {
            bdoUnit.put("blueCurrent", measure.BlueCurr)
            bdoUnit.put("blueVoltage", measure.BlueVolt)
            bdoUnit.put("bluePeak", measure.PeakWavelength)
            bdoUnit.put("blueDominant", measure.dominantWavelength)
            bdoUnit.put("blueCentroid", measure.Centroid)
            bdoUnit.put("bluePhotometric", measure.photometric)
            bdoUnit.put("blueRadiometric", measure.radiometric)
            bdoUnit.put("blueFWHM", measure.FWHM)
            bdoUnit.put("blueCCTK", measure.CCT_K)

            if (bdoUnit.blueVoltage > 0 && bdoUnit.blueCurrent > 0 && bdoUnit.blueRadiometric > 0 && bdoUnit.blueCentroid > 0 && bdoUnit.bluePeak > 0) {

                def vWpe = 100 * bdoUnit.blueRadiometric / (bdoUnit.blueVoltage * bdoUnit.blueCurrent)
                if (vWpe >= 100) vWpe = 0
                def vEqe = 80.66 * bdoUnit.blueRadiometric * bdoUnit.blueCentroid / (bdoUnit.blueCurrent * 1000)
                //  0.8066 * 100%   (e/hc)
                if (vEqe >= 100) vEqe = 0

                bdoUnit.put("blueEqe", vEqe)
                bdoUnit.put("blueWpe", vWpe)
            }

            bdoUnit.put("bluePeakWavelengthIntensity", measure.PeakWavelengthIntensity)
        }

        if (measure.ResultType == "White") {

            bdoUnit.put("whitePhotometric", measure.photometric)
            bdoUnit.put("whiteRadiometric", measure.radiometric)
            bdoUnit.put("whiteFWHM", measure.FWHM)
            bdoUnit.put("whiteCCTK", measure.CCT_K)

            def whitePower = measure.GreenCurr * measure.GreenVolt +
                    measure.BlueCurr * measure.BlueVolt +
                    measure.RedCurr * measure.RedVolt

            bdoUnit.put("whiteElectricPower", whitePower)
            bdoUnit.put("whiteCieX", measure.x)
            bdoUnit.put("whiteCieY", measure.y)

            if (whitePower > 0) {
                bdoUnit.put("luminousEfficacy", measure.photometric / whitePower)
            }
        }

        if (measure.ResultType == "CurrentSweepGreen") {

            calculateKFactor(db, unit, bdoUnit, measure)
        }

        if (tkey == "d65_rel_test") {
            saveDiffFromZeroHours(db, measure)
        }


        if (bdoUnit.size() > 0) {

            bdoUnit.put("id", unit["_id"])
            bdoUnit.put(sync, "YES")
            bdoUnit.put(sync + "TestData", [unit.code])
            bdoUnit.put("processCategory", pctg)
            bdoUnit.put("processKey", pkey)
            bdoUnit.put("taskKey", tkey)
            unitService.update(bdoUnit, "admin", true)
        }
    }

    def processTop200(def var, def toSave) {

        def pkey = "iblu"
        def pctg = "Packages"

        if (!(var.TestType in ["ilb_top200_test", "iblu_top200_test", "display_top200_test"])) {
            throw new RuntimeException("TestType# " + var.TestType + " not valid")
        }
        def tkey = var.TestType
        def sync = var.TestType.replace("_", "") + "Synced"

        def resType = var.resultType ?: var.ResultType
        if (!(resType in [
                "Green",
                "Red",
                "Blue",
                "White"
        ])) {
            throw new RuntimeException("Top 200 not valid data type: " + resType)
        }

        def db = mongo.getDB("glo")
        def unitCode = var.WaferID
        def unit = db.unit.find(new BasicDBObject("code", unitCode), new BasicDBObject()).collect { it }[0]
        if (!unit) {
            throw new RuntimeException("Unit with serial# " + unitCode + " does not exist in MES")
        }

        if (!unitService.inStep(db, unitCode, "", tkey)) {
            throw new RuntimeException("Unit " + unitCode + " never passed through step " + tkey)
        }

        def measure
        if (toSave) {
            measure = saveMeasureRecord(db, var.WaferID, var.DeviceID, tkey, var)
        } else {
            measure = var
        }

        def bdoUnit = new BasicDBObject()
        bdoUnit.put("id", unit["_id"])
        bdoUnit.put(sync, "YES")
        bdoUnit.put(sync + "TestData", [unit.code])
        bdoUnit.put("processCategory", pctg)
        bdoUnit.put("processKey", pkey)
        bdoUnit.put("taskKey", tkey)
        unitService.update(bdoUnit, "admin", true)
    }

    def processLightBarUniformity(def var, def toSave) {
        def pkey = "iblu"
        def pctg = "Packages"
        def tkey = "light_bar_uniformity_test"
        def sync = "LightBarUniformitySynced"
        def db = mongo.getDB("glo")
        def unitCode = var.WaferID
        def unit = db.unit.find(new BasicDBObject("code", unitCode), new BasicDBObject()).collect { it }[0]
        if (!unit) {
            throw new RuntimeException("Unit with serial# " + unitCode + " does not exist in MES")
        }
        if (!unitService.inStep(db, unitCode, "", tkey)) {
            throw new RuntimeException("Unit " + unitCode + " never passed through step " + tkey)
        }
        def bdoUnit = new BasicDBObject()
        // Calculate uniformity for all customdata
        if (var.customdata) {
            var.customdata.each { prop, val ->
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
                    }
                }
            }
        }

        def measure
        if (toSave) {
            measure = saveMeasureRecord(db, var.WaferID, '', tkey, var)
        } else {
            measure = var
        }
        bdoUnit.put("id", unit["_id"])
        bdoUnit.put(sync, "YES")
        bdoUnit.put(sync + "TestData", [unit.code])
        bdoUnit.put("processCategory", pctg)
        bdoUnit.put("processKey", pkey)
        bdoUnit.put("taskKey", tkey)
        unitService.update(bdoUnit, "admin", true)
    }

    def processIblu(def var, def toSave) {

        def pkey = "iblu"
        def pctg = "Packages"

        if (!(var.TestType in ["light_bar_test", "iblu_test", "blu_test", "ilgp_test", "iblu_sphere_test", "display_test", "ilb_overtemp_test",
                               "ilgp_overtemp_test", "iblu_overtemp_test", "light_bar_spc"])) {
            throw new RuntimeException("TestType# " + var.TestType + " not valid")
        }
        def tkey = var.TestType
        def sync = var.TestType.replace("_", "") + "Synced"

        def resType = var.resultType ?: var.ResultType
        if (!(resType in [
                "White",
                "Green",
                "Red",
                "Blue",
                "CurrentSweepGreen",
                "CurrentSweepBlue",
                "CurrentSweepRed",
                "WhiteOperatingPoint",
                "pcRed",
                "pBlue",
                "WavelengthStabilityGreen",
                "WavelengthStabilityRed",
                "WavelengthStabilityBlue"
        ])) {
            throw new RuntimeException("iBLU not valid data type: " + resType)
        }

        def db = mongo.getDB("glo")
        def unitCode = var.WaferID + (var.DeviceID ? "_" + var.DeviceID : "")
        def unit = db.unit.find(new BasicDBObject("code", unitCode), new BasicDBObject()).collect { it }[0]
        if (!unit) {
            throw new RuntimeException("Unit with serial# " + unitCode + " does not exist in MES")
        }

        if (!unitService.inStep(db, unitCode, "", tkey)) {

            throw new RuntimeException("Unit " + unitCode + " never passed through step " + tkey)
        }

        def measure
        if (toSave) {
            measure = saveMeasureRecord(db, var.WaferID, var.DeviceID, tkey, var)
        } else {
            measure = var
        }

        def bdoUnit = new BasicDBObject()
        if (measure.ResultType == "Green") {
            bdoUnit.put("greenCurrent", measure.GreenCurr)
            bdoUnit.put("greenVoltage", measure.GreenVolt)
            bdoUnit.put("greenPeak", measure.PeakWavelength)
            bdoUnit.put("greenDominant", measure.dominantWavelength)
            bdoUnit.put("greenCentroid", measure.Centroid)
            bdoUnit.put("greenPhotometric", measure.photometric)
            bdoUnit.put("greenRadiometric", measure.radiometric)
            bdoUnit.put("greenFWHM", measure.FWHM)
            bdoUnit.put("greenCCTK", measure.CCT_K)

            if (bdoUnit.greenVoltage > 0 && bdoUnit.greenCurrent > 0 && bdoUnit.greenRadiometric > 0 && bdoUnit.greenCentroid > 0 && bdoUnit.greenPeak > 0) {

                def vWpe = 100 * bdoUnit.greenRadiometric / (bdoUnit.greenVoltage * bdoUnit.greenCurrent)
                if (vWpe >= 100) vWpe = 0
                def vEqe = 80.66 * bdoUnit.greenRadiometric * bdoUnit.greenCentroid / (bdoUnit.greenCurrent * 1000)
                //  0.8066 * 100%   (e/hc)
                if (vEqe >= 100) vEqe = 0

                bdoUnit.put("greenEqe", vEqe)
                bdoUnit.put("greenWpe", vWpe)
            }

            bdoUnit.put("greenPeakWavelengthIntensity", measure.PeakWavelengthIntensity)
        }
        if (measure.ResultType in ["Red", "pcRed"]) {

            def redRt = measure.ResultType.toLowerCase()

            bdoUnit.put(redRt + "Current", measure.RedCurr)
            bdoUnit.put(redRt + "Voltage", measure.RedVolt)
            bdoUnit.put(redRt + "Peak", measure.PeakWavelength)
            bdoUnit.put(redRt + "Dominant", measure.dominantWavelength)
            bdoUnit.put(redRt + "Centroid", measure.Centroid)
            bdoUnit.put(redRt + "Photometric", measure.photometric)
            bdoUnit.put(redRt + "Radiometric", measure.radiometric)
            bdoUnit.put(redRt + "FWHM", measure.FWHM)
            bdoUnit.put(redRt + "CCTK", measure.CCT_K)


            if (measure.ResultType == "Red" && bdoUnit.redVoltage > 0 && bdoUnit.redCurrent > 0 && bdoUnit.redRadiometric > 0 && bdoUnit.redCentroid > 0 && bdoUnit.redPeak > 0) {

                def vWpe = 100 * bdoUnit.redRadiometric / (bdoUnit.redVoltage * bdoUnit.redCurrent)
                if (vWpe >= 100) vWpe = 0
                def vEqe = 80.66 * bdoUnit.redRadiometric * bdoUnit.redCentroid / (bdoUnit.redCurrent * 1000)
                //  0.8066 * 100%   (e/hc)
                if (vEqe >= 100) vEqe = 0

                bdoUnit.put("redEqe", vEqe)
                bdoUnit.put("redWpe", vWpe)
            }

            bdoUnit.put("redPeakWavelengthIntensity", measure.PeakWavelengthIntensity)
        }
        if (measure.ResultType == "Blue") {
            bdoUnit.put("blueCurrent", measure.BlueCurr)
            bdoUnit.put("blueVoltage", measure.BlueVolt)
            bdoUnit.put("bluePeak", measure.PeakWavelength)
            bdoUnit.put("blueDominant", measure.dominantWavelength)
            bdoUnit.put("blueCentroid", measure.Centroid)
            bdoUnit.put("bluePhotometric", measure.photometric)
            bdoUnit.put("blueRadiometric", measure.radiometric)
            bdoUnit.put("blueFWHM", measure.FWHM)
            bdoUnit.put("blueCCTK", measure.CCT_K)

            if (bdoUnit.blueVoltage > 0 && bdoUnit.blueCurrent > 0 && bdoUnit.blueRadiometric > 0 && bdoUnit.blueCentroid > 0 && bdoUnit.bluePeak > 0) {

                def vWpe = 100 * bdoUnit.blueRadiometric / (bdoUnit.blueVoltage * bdoUnit.blueCurrent)
                if (vWpe >= 100) vWpe = 0
                def vEqe = 80.66 * bdoUnit.blueRadiometric * bdoUnit.blueCentroid / (bdoUnit.blueCurrent * 1000)
                //  0.8066 * 100%   (e/hc)
                if (vEqe >= 100) vEqe = 0

                bdoUnit.put("blueEqe", vEqe)
                bdoUnit.put("blueWpe", vWpe)
            }

            bdoUnit.put("bluePeakWavelengthIntensity", measure.PeakWavelengthIntensity)
        }

        if (measure.ResultType == "White") {

            bdoUnit.put("whitePhotometric", measure.photometric)
            bdoUnit.put("whiteRadiometric", measure.radiometric)
            bdoUnit.put("whiteFWHM", measure.FWHM)
            bdoUnit.put("whiteCCTK", measure.CCT_K)

            def whitePower = measure.GreenCurr * measure.GreenVolt +
                    measure.BlueCurr * measure.BlueVolt +
                    measure.RedCurr * measure.RedVolt

            bdoUnit.put("whiteElectricPower", whitePower)
            bdoUnit.put("whiteCieX", measure.x)
            bdoUnit.put("whiteCieY", measure.y)

            if (whitePower > 0) {
                bdoUnit.put("luminousEfficacy", measure.photometric / whitePower)
            }
        }

        if (measure.ResultType == "CurrentSweepGreen") {

            calculateKFactor(db, unit, bdoUnit, measure)
        }

        if (tkey == "light_bar_spc") {

            def recv = new Expando()
            recv.uid = "admin"
            recv.cid = Company.findByName("Unknown").id
            recv.pid = Product.findByCode("LightbarSpcData").id
            recv.units = []
            bdoUnit.put('qty', 1)
            bdoUnit.put('actualStart', df.format(new Date()))
            recv.units.add(bdoUnit)

            try {
                unitService.start(recv, "Rel", "light_bar_spc")
            } catch (RuntimeException exc) {
                logr.warn("Light bar SPC had errors: " + exc.getMessage())
            }
        }

        bdoUnit.put("id", unit["_id"])
        bdoUnit.put(sync, "YES")
        bdoUnit.put(sync + "TestData", [unit.code])
        bdoUnit.put("processCategory", pctg)
        bdoUnit.put("processKey", pkey)
        bdoUnit.put("taskKey", tkey)
        unitService.update(bdoUnit, "admin", true)

    }


    def processWaferRel(def var, def toSave) {

        def db = mongo.getDB("glo")


        def tkey = var.TestType
        def pkey = "test"
        def sync = "relWaferSynced"

        if (!(var.TestType in ["wafer_rel_test"])) {
            throw new RuntimeException("Wafer rel not valid test type: " + var.TestType)
        }

        def resType = var.resultType ?: var.ResultType
        if (!(resType in [
                "CurrentSweepGreen",
                "CurrentSweepBlue",
                "CurrentSweepRed"
        ])) {
            throw new RuntimeException("Wafer rel not valid data type: " + resType)
        }


        def unitCode = var.WaferID
        def unit = db.unit.find(new BasicDBObject("code", unitCode), new BasicDBObject()).collect { it }[0]
        if (!unit) {
            throw new RuntimeException("Unit with serial# " + unitCode + " does not exist in MES")
        }

        if (!unitService.inStep(db, unitCode, "", tkey)) {

            throw new RuntimeException("Unit " + unitCode + " never passed through step " + tkey)
        }

        def measure
        if (toSave) {
            measure = saveMeasureRecord(db, var.WaferID, var.DeviceID, tkey, var)

            if (tkey == "wafer_rel_test") {
                saveDiffFromZeroHours(db, measure)
            }
        } else {
            measure = var
        }

        def bdoUnit = new BasicDBObject()
        bdoUnit.put("id", unit["_id"])
        bdoUnit.put(sync, "YES")

        def sh = unit.testedHours?.tokenize(",") ?: []
        if (!sh.contains(var.HoursOn?.toInteger().toString())) {
            sh.add(var.HoursOn?.toInteger().toString())
        }
        bdoUnit.put("testedHours", sh.join(","))

        bdoUnit.put("relWaferData", [unit["code"]])
        bdoUnit.put("relWaferSynced", "YES")
        bdoUnit.put("processCategory", "nwLED")
        bdoUnit.put("processKey", "test")
        bdoUnit.put("taskKey", tkey)
        unitService.update(bdoUnit, "admin", true)

        bdoUnit = null
    }

    def processDataUpload(def vars, def toSave) {

        def db = mongo.getDB("glo")
        DateFormat df = new SimpleDateFormat("yyddMM")

        vars.each { var ->

            def process = Process.findByPkey(var.pkey)
            if (!process) {
                throw new RuntimeException("Process " + var.pkey + " not defined in MES")
            }

            def code
            def idps = process?.idPrefix?.tokenize(",")
            if (idps?.size() == 2) {
                code = idps[0] + sequenceGeneratorService.next(idps[0]).toString().padLeft(idps[1].toInteger(), '0')
            } else if (process.idPrefix) {
                code = process.idPrefix + sequenceGeneratorService.next(process.idPrefix).toString()
            } else {
                code = var.code
            }

            def date = df.parse(var.actualStart)

            def dr = new BasicDBObject()
            dr.put("parentCode", null)
            dr.put("code", code)


            def obj = new BasicDBObject()
            obj.put("active", "true")
            obj.put("actualStart", date)
            obj.put("tags", [process.category, process.category + "|" + process.pkey])

            var.each { k, v ->
                if (!(k in ["actualStart"])) {
                    obj.put(k, v)
                }
            }
            dr.put("value", obj)

            db.dataReport.save(dr)
        }
    }

    private def saveDiffFromZeroHours(def db, def measure) {

        def query = new BasicDBObject("WaferID", measure.WaferID)
        query.put("DeviceID", measure.DeviceID)
        query.put("TestType", measure.TestType)
        query.put("ResultType", measure.ResultType)
        query.put("HoursOn", 0)
        query.put("C", measure.C)
        def dataAtZero = db.measures.find(query, new BasicDBObject("wavelengthPowerScan", 0)).collect { it }[0]

        def bdo = new BasicDBObject()
        if (dataAtZero) {
            if (measure.photometric && dataAtZero.photometric)
                bdo.put("photometricDelta", (float) (measure.photometric - dataAtZero.photometric))
            bdo.put("eqeDelta", (float) (measure.eqe - dataAtZero.eqe))
            bdo.put("VoltDelta", (float) (measure.Volt - dataAtZero.Volt))
            if (measure.PeakWavelength && dataAtZero.PeakWavelength)
                bdo.put("PeakWavelengthDelta", (float) (measure.PeakWavelength - dataAtZero.PeakWavelength))
            if (measure.Centroid && dataAtZero.Centroid)
                bdo.put("CentroidDelta", (float) (measure.Centroid - dataAtZero.Centroid))
            if (measure.dominantWavelength && dataAtZero.dominantWavelength)
                bdo.put("dominantWavelengthDelta", (float) (measure.dominantWavelength - dataAtZero.dominantWavelength))
            if (measure.FWHM && dataAtZero.FWHM)
                bdo.put("FWHMDelta", (float) (measure.FWHM - dataAtZero.FWHM))
            bdo.put("xDelta", (float) (measure.x - dataAtZero.x))
            bdo.put("yDelta", (float) (measure.y - dataAtZero.y))
            bdo.put("zDelta", (float) (measure.z - dataAtZero.z))
            bdo.put("uDelta", (float) (measure.u - dataAtZero.u))

            if (measure.photometric && dataAtZero.photometric)
                bdo.put("photometricPerc", (float) (dataAtZero.photometric != 0 ? 100 * (measure.photometric - dataAtZero.photometric) / dataAtZero.photometric : 0))
            bdo.put("eqePerc", (float) (dataAtZero.eqe != 0 ? 100 * (measure.eqe - dataAtZero.eqe) / dataAtZero.eqe : 0))
            bdo.put("VoltPerc", (float) (dataAtZero.Volt != 0 ? 100 * (measure.Volt - dataAtZero.Volt) / dataAtZero.Volt : 0))
            if (measure.PeakWavelength && dataAtZero.PeakWavelength)
                bdo.put("PeakWavelengthPerc", (float) (dataAtZero.PeakWavelength != 0 ? 100 * (measure.PeakWavelength - dataAtZero.PeakWavelength) / dataAtZero.PeakWavelength : 0))
            if (measure.Centroid && dataAtZero.Centroid)
                bdo.put("CentroidPerc", (float) (dataAtZero.Centroid != 0 ? 100 * (measure.Centroid - dataAtZero.Centroid) / dataAtZero.Centroid : 0))
            if (measure.dominantWavelength && dataAtZero.dominantWavelength)
                bdo.put("dominantWavelengthPerc", (float) (dataAtZero.dominantWavelength != 0 ? 100 * (measure.dominantWavelength - dataAtZero.dominantWavelength) / dataAtZero.dominantWavelength : 0))
            if (measure.FWHM && dataAtZero.FWHM)
                bdo.put("FWHMPerc", (float) (dataAtZero.FWHM != 0 ? 100 * (measure.FWHM - dataAtZero.FWHM) / dataAtZero.FWHM : 0))
            bdo.put("xPerc", (float) (dataAtZero.x != 0 ? 100 * (measure.x - dataAtZero.x) / dataAtZero.x : 0))
            bdo.put("yPerc", (float) (dataAtZero.y != 0 ? 100 * (measure.y - dataAtZero.y) / dataAtZero.y : 0))
            bdo.put("zPerc", (float) (dataAtZero.z != 0 ? 100 * (measure.z - dataAtZero.z) / dataAtZero.z : 0))
            bdo.put("uPerc", (float) (dataAtZero.u != 0 ? 100 * (measure.u - dataAtZero.u) / dataAtZero.u : 0))
        }

        def u = new BasicDBObject('$set', bdo)
        db.measures.update(new BasicDBObject("_id", measure["_id"]), u, false, true)
    }

    private def startWSTProcess(def db, def sid) {

        def query = new BasicDBObject("sid", sid)

        def allMeasures = db.measures.find(query, new BasicDBObject("wavelengthPowerScan", 0)).collect { it }

        if (allMeasures) {

            def recv = new Expando()
            recv.uid = "admin"
            recv.cid = Company.findByName("Unknown").id
            recv.pid = Product.findByCode("wstData").id
            recv.units = []

            def m = [:]
            m.put('qty', 1)

            def dominantsC = new TreeMap()    // Dominant WL for 10mA to 20mA at 55C

            def vars = ["photometric": new TreeMap(),      // Photometrics at 20 mA
                        "radiometric": new TreeMap(),     // Radiometrics at 20 mA
                        "dominant"   : new TreeMap(),    // Dominant at 20 mA
                        "peak"       : new TreeMap()]     // Peak WL at 20 mA

            allMeasures.each { measure ->

                if (measure.C in ["10000", "20000"] && measure.TempSetpoint == 55) {
                    dominantsC.put(measure.CurrentSet, measure.dominantWavelength)
                }

                if (measure.C in ["20000"]) {
                    vars["photometric"].put(measure.TempSetpoint, measure.photometric)
                    vars["radiometric"].put(measure.TempSetpoint, measure.radiometric)
                    vars["dominant"].put(measure.TempSetpoint, measure.dominantWavelength)
                    vars["peak"].put(measure.TempSetpoint, measure.PeakWavelength)
                }

                if (!m["code"])
                    m.put("code", "WST-" + measure.WaferID + "-" + measure.DeviceID)
                if (!m["actualStart"])
                    m.put("actualStart", measure.TimeRun.toString().replace("T", " ").substring(0, 19))

            }

            if (dominantsC.size() == 2) {
                def l = dominantsC.values().toList()
                m.put("dominantsCD", l[0] - l[1])
            }

            vars.each { var, lst ->

                if (lst.size() > 1) {
                    def l = lst.values().toList()

                    m.put(var + "D", l[0] - l[l.size() - 1])
                    m.put(var + "Norm", (l[0] - l[l.size() - 1]) / l[0])

                    SimpleRegression sreg = new SimpleRegression()
                    lst.each { k, v ->
                        sreg.addData(k, v)
                    }
                    m.put(var + "Slope", sreg.getSlope())
                    m.put(var + "R", sreg.RSquare)
                }
            }

            recv.units.add(m)

            db.history.remove(new BasicDBObject("code", m["code"]))
            db.dataReport.remove(new BasicDBObject("code", m["code"]))
            db.unit.remove(new BasicDBObject("code", m["code"]))

            unitService.start(recv, "WST", "wst_summary")
        }
    }

    private def startNIDOTProcess(def db, def sid) {

        def query = new BasicDBObject("sid", sid)

        def allMeasures = db.measures.find(query).collect { it }

        if (allMeasures) {


        }
    }


    private def startSpcProcess(def db, def sid) {


        def currentsForCalc = [
                "100",
                "500",
                "1000",
                "5000",
                "10000",
                "20000"
        ]

        def query = new BasicDBObject("sid", sid)
        def allMeasures = db.measures.find(query, new BasicDBObject("wavelengthPowerScan", 0)).collect { it }
        def measuresByCurrents = allMeasures.groupBy { it.C }

        def recv = new Expando()
        recv.uid = "admin"
        recv.cid = Company.findByName("Unknown").id
        recv.pid = Product.findByCode("stdRelData").id
        recv.units = []

        def m = [:]
        m.put('qty', 1)
        m.put('actualStart', df.format(new Date()))

        measuresByCurrents.each { currentSet, measures ->


            if (currentsForCalc.contains(currentSet)) {

                Boolean hasData = false
                def DescriptiveStatistics lops = new DescriptiveStatistics()
                def DescriptiveStatistics voltages = new DescriptiveStatistics()
                def DescriptiveStatistics peaks = new DescriptiveStatistics()
                def DescriptiveStatistics dominants = new DescriptiveStatistics()
                def DescriptiveStatistics photometrics = new DescriptiveStatistics()
                def DescriptiveStatistics fwhms = new DescriptiveStatistics()

                measures.each { measure ->

                    hasData = true
                    lops.addValue(measure.radiometric)
                    voltages.addValue(measure.Volt)
                    peaks.addValue(measure.PeakWavelength)
                    dominants.addValue(measure.dominantWavelength)
                    photometrics.addValue(measure.photometric)
                    fwhms.addValue(measure.FWHM)
                }

                if (hasData) {
                    def num = (currentSet.toInteger() / 1000)
                    def cnum = ""
                    if (num < 1) {
                        cnum = currentSet + "uA"
                    } else {
                        cnum = num.toString() + "mA"
                    }

                    m.put('lop xbar ' + cnum, lops.getMean())
                    m.put('voltage xbar ' + cnum, voltages.getMean())
                    m.put('peak xbar ' + cnum, peaks.getMean())
                    m.put('dominant xbar ' + cnum, dominants.getMean())
                    m.put('photometric xbar ' + cnum, photometrics.getMean())
                    m.put('fwhm xbar ' + cnum, fwhms.getMean())
                    m.put('lop r ' + cnum, lops.getMax() - lops.getMin())
                    m.put('voltage r ' + cnum, voltages.getMax() - voltages.getMin())
                    m.put('peak r ' + cnum, peaks.getMax() - peaks.getMin())
                    m.put('dominant r ' + cnum, dominants.getMax() - dominants.getMin())
                    m.put('photometric r ' + cnum, photometrics.getMax() - photometrics.getMin())
                    m.put('fwhm r ' + cnum, fwhms.getMax() - fwhms.getMin())
                }
            }
        }

        recv.units.add(m)

        try {
            unitService.start(recv, "Rel", "stdrel_003")
        } catch (RuntimeException exc) {
            logr.warn(sid + " had errors: " + exc.getMessage())
        }
    }

    private def saveMeasureRecord(def db, def parentCode, def deviceCode, def tkey, def var) {

        def current = -1
        def currentSet = -1
        def volt = -1

        def C = "-1"
        def B = "-1"
        def R = "-1"
        def G = "-1"

        def resType = var.resultType ?: var.ResultType
        if (resType.toUpperCase().indexOf("GREEN") >= 0) {

            currentSet = var.GreenCurrentSetpoint ? var.GreenCurrentSetpoint.toDouble() : -1
            G = (currentSet * 1E6).toFloat().round(0).toLong().toString()
            current = var.GreenCurr ? var.GreenCurr.toDouble() : -1
            volt = var.GreenVolt ? var.GreenVolt.toDouble() : -1
        }
        if (resType.toUpperCase().indexOf("BLUE") >= 0) {

            currentSet = var.BlueCurrentSetpoint ? var.BlueCurrentSetpoint.toDouble() : -1
            B = (currentSet * 1E6).toFloat().round(0).toLong().toString()
            current = var.BlueCurr ? var.BlueCurr.toDouble() : -1
            volt = var.BlueVolt ? var.BlueVolt.toDouble() : -1
        }
        if (resType.toUpperCase().indexOf("RED") >= 0) {

            currentSet = var.RedCurrentSetpoint ? var.RedCurrentSetpoint.toDouble() : -1
            R = (currentSet * 1E6).toFloat().round(0).toLong().toString()
            current = var.RedCurr ? var.RedCurr.toDouble() : -1
            volt = var.RedVolt ? var.RedVolt.toDouble() : -1
        }

        C = (currentSet * 1E6).toDouble().round(0).toLong().toString()
        def lowCurr = (currentSet * 1E6).toDouble()
        if (lowCurr < 1) {
            if (lowCurr.toString().length() < 4)
                C = lowCurr.toString()
            else
                C = lowCurr.toString().substring(1, 4)
        }

        if (resType.toUpperCase().indexOf("WHITE") >= 0) {

            currentSet = var.GreenCurrentSetpoint ? var.GreenCurrentSetpoint.toDouble() : -1
            G = (currentSet * 1E6).toFloat().round(0).toLong().toString()
            currentSet = var.BlueCurrentSetpoint ? var.BlueCurrentSetpoint.toDouble() : -1
            B = (currentSet * 1E6).toFloat().round(0).toLong().toString()
            currentSet = var.RedCurrentSetpoint ? var.RedCurrentSetpoint.toDouble() : -1
            R = (currentSet * 1E6).toFloat().round(0).toLong().toString()
            C = -1
        }



        def query = new BasicDBObject("WaferID", parentCode)
        query.put("DeviceID", deviceCode ?: "")
        query.put("TestType", tkey)
        query.put("ResultType", resType)
        query.put("Encapsulated", var.Encapsulated ?: "FALSE")
        query.put("HoursOn", var.HoursOn == null ? -1 : var.HoursOn.toInteger())
        query.put("TempSetpoint", var.TempSetpoint == null ? -99 : var.TempSetpoint.toInteger())
        if (var.currentString) {
            query.put("CurrentString", var.currentString)
        }
        query.put("C", C)
        query.put("G", G)
        query.put("R", R)
        query.put("B", B)
        if (var.sid) {
            query.put("sid", var.sid)
        }

        def objMeasure = db.measures.find(query, new BasicDBObject()).collect { it }[0]
        if (!objMeasure) {
            objMeasure = new BasicDBObject()
            objMeasure.putAll(query)
        }

        objMeasure.put("TimeRun", var.TimeRun)
        objMeasure.put("TimeRunMs", var.TimeRunMillisecond ?: 0)

        try {

            if (!var.DateTimeTested) {
                def date = df.parse(var.TimeRun.toString().replace("T", " ").substring(0, 19))
                objMeasure.put("DateTimeTested", date)
            } else {
                objMeasure.put("DateTimeTested", var.DateTimeTested)
            }
        } catch (Exception exc) {

        }

        objMeasure.put("customdata", var.customdata)
        objMeasure.put("fileName", var.fileName)
        objMeasure.put("iniFileName", var.iniFileName)
        objMeasure.put("station", var.station)
        objMeasure.put("operName", var.operName)
        objMeasure.put("Comment", var.comment)
        objMeasure.put("SWRev", var.SWRev)
        objMeasure.put("devlist", var.devlist)
        objMeasure.put("devtype", var.devtype)
        objMeasure.put("actarea", var.actarea)

        // Electric data
        objMeasure.put("CurrentSet", currentSet)
        objMeasure.put("GreenSet", var.GreenCurrentSetpoint ? var.GreenCurrentSetpoint.toDouble() : -1)
        objMeasure.put("RedSet", var.RedCurrentSetpoint ? var.RedCurrentSetpoint.toDouble() : -1)
        objMeasure.put("BlueSet", var.BlueCurrentSetpoint ? var.BlueCurrentSetpoint.toDouble() : -1)

        objMeasure.put("CurrentString", var.currentString)
        objMeasure.put("Current", current)
        objMeasure.put("Volt", volt)
        objMeasure.put("GreenCurr", var.GreenCurr ? var.GreenCurr.toDouble() : -1)
        objMeasure.put("RedCurr", var.RedCurr ? var.RedCurr.toDouble() : -1)
        objMeasure.put("BlueCurr", var.BlueCurr ? var.BlueCurr.toDouble() : -1)
        objMeasure.put("GreenVolt", var.GreenVolt ? var.GreenVolt.toDouble() : -1)
        objMeasure.put("RedVolt", var.RedVolt ? var.RedVolt.toDouble() : -1)
        objMeasure.put("BlueVolt", var.BlueVolt ? var.BlueVolt.toDouble() : -1)

        // Performance calculated values
        def radioPower = var.radiometric ? var.radiometric.toFloat() : -1
        def centroid = var.Centroid ? var.Centroid.toFloat() : -1
        if (volt * current != 0 && !var.wpe) {
            def vWpe = 100 * radioPower / (volt * current)
            if (vWpe >= 100) vWpe = 0
            objMeasure.put("wpe", vWpe.toString().toFloat())
        } else {
            objMeasure.put("wpe", var.wpe.toString().toFloat())
        }
        if (current != 0 && !var.eqe) {
            def vEqe = 80.66 * radioPower * centroid / (current * 1000)   //  0.8066 * 100%   (e/hc)
            if (vEqe >= 100) vEqe = 0
            objMeasure.put("eqe", vEqe.toString().toFloat())
        } else {
            objMeasure.put("eqe", var.eqe.toString().toFloat())
        }

        // Optical data
        objMeasure.put("IntegrationTime", var.IntegrationTime)
        objMeasure.put("FilterPosition", var.FilterPosition)
        objMeasure.put("MaxAdcValue", var.MaxAdcValue)
        objMeasure.put("radiometric", radioPower)
        objMeasure.put("photometric", var.photometric ? var.photometric.toFloat() : -1)
        objMeasure.put("efficacy", var.efficacy ? var.efficacy.toFloat() : -1)
        objMeasure.put("x", var.x ? var.x.toFloat() : -1)
        objMeasure.put("y", var.y ? var.y.toFloat() : -1)
        objMeasure.put("z", var.z ? var.z.toFloat() : -1)
        objMeasure.put("u", var.u ? var.u.toFloat() : -1)
        objMeasure.put("v1960", var.v1960)
        objMeasure.put("CCT_K", var.CCT_K)
        objMeasure.put("Duv", var.Duv)
        objMeasure.put("burnin", var.burnin)
        objMeasure.put("optTarget", var.optTarget)
        objMeasure.put("PeakWavelength", var.PeakWavelength ? var.PeakWavelength.toFloat() : -1)
        objMeasure.put("PeakWavelengthIntensity", var.PeakWavelengthIntensity ? var.PeakWavelengthIntensity.toFloat() : -1)
        objMeasure.put("RawPeakWavelength", var.RawPeakWavelength ? var.RawPeakWavelength.toFloat() : -1)
        objMeasure.put("RawPeakWavelengthIntensity", var.RawPeakWavelengthIntensity ? var.RawPeakWavelengthIntensity.toFloat() : -1)

        objMeasure.put("Centroid", centroid)
        objMeasure.put("dominantWavelength", var.dominantWavelength ? var.dominantWavelength.toFloat() : -1)
        objMeasure.put("FWHM", var.FWHM ? var.FWHM : -1)
        objMeasure.put("CRI", var.CRI)

        if (var.Peak2Wavelength) {
            objMeasure.put("Peak2Wavelength", var.Peak2Wavelength ? var.Peak2Wavelength.toFloat() : -99)
            objMeasure.put("Peak2WavelengthIntensity", var.Peak2WavelengthIntensity ? var.Peak2WavelengthIntensity.toFloat() : -99)
        }
        if (var.Peak3Wavelength) {
            objMeasure.put("Peak3Wavelength", var.Peak3Wavelength ? var.Peak3Wavelength.toFloat() : -99)
            objMeasure.put("Peak3WavelengthIntensity", var.Peak3WavelengthIntensity ? var.Peak3WavelengthIntensity.toFloat() : -99)
        }
        if (var.Peak4Wavelength) {
            objMeasure.put("Peak4Wavelength", var.Peak4Wavelength ? var.Peak4Wavelength.toFloat() : -99)
            objMeasure.put("Peak4WavelengthIntensity", var.Peak4WavelengthIntensity ? var.Peak4WavelengthIntensity.toFloat() : -99)
        }

        // Environment data
        objMeasure.put("Temp", var.Temp ? var.Temp.toFloat() : -99)
        objMeasure.put("OvenTemp", var.OvenTemp ? var.OvenTemp.toFloat() : -99)
        objMeasure.put("OvenRH", var.OvenRH ? var.OvenRH.toFloat() : -99)

        // Additional added fields
        if (var.RedPhosBlueLeakRatio)
            objMeasure.put("RedPhosBlueLeakRatio", var.RedPhosBlueLeakRatio ? var.RedPhosBlueLeakRatio.toFloat() : -99)
        if (var.RedPhosBluePeaknm)
            objMeasure.put("RedPhosBluePeaknm", var.RedPhosBluePeaknm ? var.RedPhosBluePeaknm.toFloat() : -99)

        if (var.wavelengthPowerScan)
            objMeasure.put("wavelengthPowerScan", var.wavelengthPowerScan)
        if (var.VISwp)
            objMeasure.put("VISwp", var.VISwp)

        objMeasure.putAll(addUnitData(db, parentCode + "_" + deviceCode))

        db.measures.save(objMeasure)
        objMeasure
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
        fields.put("product", 1)
        fields.put("productCode", 1)
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


    def calculateKFactor(db, unit, bdoUnit, measure) {

        int cntG = 0
        float current = 0
        int currentM = 0

        if (unit.dieOrder) {

            cntG = unit.dieOrder.count("G")
            if (cntG > 0) {
                //Miljenko: this is wrong! K factor is based on the current density, i.e. active die area. For now ignore, but we need to fix this
                def mask = ProductMask.findByName(unit.mask)
                current = cntG * mask.kFactorCurrent
                currentM = mask.kFactorCurrent * 1000
            }

            if (current == measure.GreenSet) {

                def greenCurr = measure.GreenCurr
                if (greenCurr > 0) {

                    def vEqe = 80.66 * measure.radiometric * measure.Centroid / (greenCurr * 1000)
                    //  0.8066 * 100%   (e/hc)
                    if (vEqe >= 100) vEqe = 0

                    def eqeTest = 0
                    def cnt = 0
                    [
                            measure.DeviceID,
                            unit.secondGreen,
                            unit.thirdGreen
                    ].each {

                        def lopData = relSyncService.getLopTestData(db, measure.WaferID, it)
                        if (lopData) {
                            eqeTest += lopData[currentM] ?: 0
                            cnt++
                        }
                    }
                    if (cnt > 0 && eqeTest > 0) {
                        eqeTest = eqeTest / cnt
                        bdoUnit.put("greenKfactor", vEqe / eqeTest)
                        bdoUnit.put("eqeTest", eqeTest)
                    }
                }
            }
        }
    }

    private def addTestId(def unit, def tkey, def testId, def bdo) {

        int ret = 1

        if (tkey == "full_test_visualization") {
            bdo.put("testFullIndex", [])
            if (!unit["testFullIndex"]) {
                bdo["testFullIndex"].add(testId)
            } else {
                bdo["testFullIndex"].addAll(unit["testFullIndex"])
                bdo["testFullIndex"].add(testId)
            }
            ret = bdo["testFullIndex"].size()
        } else if (tkey == "char_test_visualization") {
            bdo.put("testCharIndex", [])
            if (!unit["testCharIndex"]) {
                bdo["testCharIndex"].add(testId)
            } else {
                bdo["testCharIndex"].addAll(unit["testCharIndex"])
                bdo["testCharIndex"].add(testId)
            }
            ret = bdo["testCharIndex"].size()
        } else if (tkey == "uniformity_test_visualization") {
            bdo.put("testUniformityIndex", [])
            if (!unit["testUniformityIndex"]) {
                bdo["testUniformityIndex"].add(testId)
            } else {
                bdo["testUniformityIndex"].addAll(unit["testUniformityIndex"])
                bdo["testUniformityIndex"].add(testId)
            }
            ret = bdo["testUniformityIndex"].size()
        } else if (tkey == "top_test_visualization") {
            bdo.put("testTopIndex", [])
            if (!unit["testTopIndex"]) {
                bdo["testTopIndex"].add(testId)
            } else {
                bdo["testTopIndex"].addAll(unit["testTopIndex"])
                bdo["testTopIndex"].add(testId)
            }
            ret = bdo["testTopIndex"].size()
        } else if (tkey == "pre_dbr_test") {
            bdo.put("testPreDbrIndex", [])
            if (!unit["testPreDbrIndex"]) {
                bdo["testPreDbrIndex"].add(testId)
            } else {
                bdo["testPreDbrIndex"].addAll(unit["testPreDbrIndex"])
                bdo["testPreDbrIndex"].add(testId)
            }
            ret = bdo["testPreDbrIndex"].size()
        } else if (tkey == "post_dbr_test") {
            bdo.put("testPostDbrIndex", [])
            if (!unit["testPostDbrIndex"]) {
                bdo["testPostDbrIndex"].add(testId)
            } else {
                bdo["testPostDbrIndex"].addAll(unit["testPostDbrIndex"])
                bdo["testPostDbrIndex"].add(testId)
            }
            ret = bdo["testPostDbrIndex"].size()
        } else {
            bdo.put("testDataIndex", [])
            if (!unit["testDataIndex"]) {
                bdo["testDataIndex"].add(testId)
            } else {
                bdo["testDataIndex"].addAll(unit["testDataIndex"])
                bdo["testDataIndex"].add(testId)
            }
            ret = bdo["testDataIndex"].size()
        }

        ret
    }

    private def getLopTestData(def db, def code, def devId) {

        def arr = new HashMap()
        if (devId) {

            def queryFull = new BasicDBObject("value.code", code)
            queryFull.put("value.tkey", "full_test_visualization")
            def fulls = db.testData.find(queryFull).limit(1).addSpecial('$orderby', new BasicDBObject('value.testId', -1)).collect {
                it
            }
            def queryTest = new BasicDBObject("value.code", code)
            queryTest.put("value.tkey", "test_data_visualization")
            def tests = db.testData.find(queryTest).limit(1).addSpecial('$orderby', new BasicDBObject('value.testId', -1)).collect {
                it
            }
            if (!tests) {
                def queryTestNbp = new BasicDBObject("value.code", code)
                queryTestNbp.put("value.tkey", "nbp_test_data_visualization")
                tests = db.testData.find(queryTestNbp).limit(1).addSpecial('$orderby', new BasicDBObject('value.testId', -1)).collect {
                    it
                }
            }
            if (!tests) {
                def queryTestNbp = new BasicDBObject("value.code", code)
                queryTestNbp.put("value.tkey", "nbp_full_test_visualization")
                tests = db.testData.find(queryTestNbp).limit(1).addSpecial('$orderby', new BasicDBObject('value.testId', -1)).collect {
                    it
                }
            }


            BasicDBObject dbo = (BasicDBObject) tests[0]?.value?.data
            dbo.each { k, v ->
                if (k != "setting") {
                    def currArr = k.tokenize('@')
                    if (v["EQE"] && v["EQE"][devId] && currArr.size() > 1) {
                        def curr = currArr[1]
                        Integer currVal = Integer.parseInt(curr.replaceAll("\\D+", ""))
                        Float currVal2 = currVal
                        if (curr.indexOf('mA') > 0) currVal2 = currVal
                        if (curr.indexOf('uA') > 0) currVal2 = currVal * 1E-3
                        if (curr.indexOf('nA') > 0) currVal2 = currVal * 1E-6
                        if (curr.indexOf('pA') > 0) currVal2 = currVal * 1E-9
                        if (curr.indexOf('fA') > 0) currVal2 = currVal * 1E-12
                        arr.put(currVal2, v["EQE"][devId])
                    }
                }
            }

            dbo = (BasicDBObject) fulls[0]?.value?.data
            dbo.each { k, v ->
                if (k != "setting") {
                    def currArr = k.tokenize('@')
                    if (v["EQE"] && v["EQE"][devId] && currArr.size() > 1) {
                        def curr = currArr[1]
                        Integer currVal = Integer.parseInt(curr.replaceAll("\\D+", ""))
                        Float currVal2 = currVal
                        if (curr.indexOf('mA') > 0) currVal2 = currVal
                        if (curr.indexOf('uA') > 0) currVal2 = currVal * 1E-3
                        if (curr.indexOf('nA') > 0) currVal2 = currVal * 1E-6
                        if (curr.indexOf('pA') > 0) currVal2 = currVal * 1E-9
                        if (curr.indexOf('fA') > 0) currVal2 = currVal * 1E-12

                        arr.put(currVal2, v["EQE"][devId])
                    }
                }
            }
        }

        arr
    }

    def testDataToMeasures(def code, def tkey) {

        def db = mongo.getDB("glo")
        def i = 0

        def p = ["Peak Power (W/nm)" : "PeakWavelengthIntensity",
                 "Peak Power"        : "PeakWavelengthIntensity",
                 "EQE"               : "eqe",
                 "WPE"               : "wpe",
                 "Int Power (W)"     : "radiometric",
                 "Int Power"         : "radiometric",
                 "Photometric Power" : "photometric",
                 "efficacy"          : "efficacy",
                 "Peak"              : "PeakWavelength",
                 "Peak (nm)"         : "PeakWavelength",
                 "RawPeak (nm)"      : "RawPeakWavelength",
                 "Peak2 (nm)"        : "Peak2Wavelength",
                 "Peak3 (nm)"        : "Peak3Wavelength",
                 "Peak4 (nm)"        : "Peak4Wavelength",
                 "FWHM (nm)"         : "FWHM",
                 "FWHM"              : "FWHM",
                 "Centroid (nm)"     : "Centroid",
                 "Pulse Current (mA)": "GreenCurr",
                 "v"                 : "v1960",
                 "u"                 : "u",
                 "Pulse Voltage (V)" : "GreenVolt",
                 "Dominant wl (nm)"  : "dominantWavelength",
                 "OSA Filter"        : "FilterPosition",
                 "OSA Int Time (ms)" : "IntegrationTime",
                 "OSA Counts"        : "MaxAdcValue"]

        def bdo = new BasicDBObject()
        bdo.put("value.parentCode", null)
        if (code) {
            bdo.put("value.code", code)
        }
        if (tkey) {
            bdo.put("value.tkey", tkey)
        } else {
            bdo.put("value.tkey", new BasicDBObject('$in', [
                    "test_data_visualization",
                    "intermediate_coupon_test",
                    "nbp_test_data_visualization",
                    "nbp_full_test_visualization",
                    "top_test_visualization",
                    "char_test_visualization",
                    "uniformity_test_visualization",
                    "full_test_visualization",
                    "spc_data_visualization",
                    "spc_ni_dot_test",
                    "pre_dbr_test",
                    "post_dbr_test"
            ]))
        }
        bdo.put("value.sync", new BasicDBObject('$ne', "1"))

        def testIds = db.testData.find(bdo, new BasicDBObject("value.code", 1)).collect { it }


        testIds.each { testId ->

            def val = db.testData.find(new BasicDBObject("_id", testId._id), new BasicDBObject()).collect {
                it.value
            }[0]
            if (val) {
                db.testData.update(new BasicDBObject("_id", testId._id), new BasicDBObject('$set', new BasicDBObject("value.sync", "1")))

                def measures = new TreeMap()
                def waferId = val.code

                val.data.each { current, currentData ->

                    def curr = current.tokenize('@')[1]
                    if (curr && current.indexOf("2V") < 0) {

                        Long currVal = Long.parseLong(curr.replaceAll("\\D+", ""))
                        Double currVal2 = currVal
                        if (curr.indexOf('mA') > 0) currVal2 = currVal * 1E-3
                        if (curr.indexOf('uA') > 0) currVal2 = currVal * 1E-6
                        if (curr.indexOf('nA') > 0) currVal2 = currVal * 1E-9
                        if (curr.indexOf('pA') > 0) currVal2 = currVal * 1E-12
                        if (curr.indexOf('fA') > 0) currVal2 = currVal * 1E-15

                        currentData.each { parameter, devices ->

                            devices.each { dev, v ->

                                def key = waferId + "|" + dev + "|" + val.tkey + "|" + val.testId + "|" + current
                                def measure = measures[key]
                                if (!measure) {
                                    measure = new BasicDBObject()
                                    measures.put(key, measure)
                                }

                                measure.WaferID = waferId
                                measure.DeviceID = dev
                                measure.TestType = val.tkey
                                measure.ResultType = "CurrentSweepGreen"
                                measure.Encapsulated = "FALSE"
                                measure.HoursOn = -1
                                measure.currentString = current
                                measure.TempSetpoint = -99
                                measure.actarea = val.actarea
                                measure.devtype = val.devtype
                                if (val.testId.toString().length() > 5) {
                                    measure.TimeRun = val.date ?: df2.parse(val.testId.toString())
                                    measure.put("DateTimeTested", val.date ?: df2.parse(val.testId.toString()))
                                } else {
                                    measure.TimeRun = new Date()
                                    measure.put("DateTimeTested", new Date())
                                }
                                measure.TimeRunMs = 0
                                measure.put("sid", val.testId ? val.testId?.toLong().toString() : "1")
                                measure.GreenCurrentSetpoint = currVal2
                                measure.RedCurrentSetpoint = -1
                                measure.BlueCurrentSetpoint = -1
                                measure.devlist = val.devlist
                                measure.put(p[parameter], v)
                            }
                        }
                    }
                }

                measures.each { key, measure ->

                    //					def bdo2 = new BasicDBObject()
                    //					bdo2.put("value.code", measure.WaferID + "_" + measure.DeviceID)
                    //					bdo2.put("value.tkey", measure.TestType)
                    //					bdo2.put("value.testId", measure.sid.toLong())
                    //
                    //					def tddev = db.testData.find(bdo2, new BasicDBObject()).collect{it}[0]
                    //					if (tddev) {
                    //
                    //						def curr = key.tokenize("|")[4]
                    //						measure.put("wavelengthPowerScan", tddev.value?.data[curr])
                    //						measure.put("VISwp",  tddev.value?.data?.Datavoltage)
                    //					}

                    def mes = saveMeasureRecord(db, measure.WaferID, measure.DeviceID, measure.TestType, measure)
                    mes = null
                    measure = null
                }

                measures = null
                val = null

                i++

            }
        }

        i
    }


    def importKonikaFromMeasures(def code) {

        def db = mongo.getDB("glo")
        def match = new BasicDBObject()
        DateFormat df = new SimpleDateFormat("yyMMddHHmmss")

        match.put('TestType', ['$in': ['display_top200_test','iblu_top200_test']])
        match.put('syncToKonica', ['$ne': 'YES'])
        def meas = db.measures.aggregate(
                ['$match': match],
                ['$group': [_id: [sid: '$sid', code: '$WaferID'], total: [$sum: 1], operName: [$last: '$operName'], time: [$last: '$DateTimeTested'], red: [$last: '$RedCurr'], green: [$last: '$GreenCurr'], blue: [$last: '$BlueCurr'], redv: [$last: '$RedVolt'], greenv: [$last: '$GreenVolt'], bluev: [$last: '$BlueVolt'],  comment: [$last:'$Comment']]],
                ['$sort': [TimeRun: -1]]
        ).results().collect { [sid: it._id.sid, code: it._id.code, date: it.time, total: it.total, operName: it.operName, red: it.red * 1000, green:it.green * 1000, blue: it.blue * 1000, redv: it.redv, greenv: it.greenv, bluev: it.bluev, comment: it.comment] }

        def spots = [F:135, C:50, E:69, P:13]

        def calc = { lst, p1, p2 ->
            def du = 0
            if (lst[p1]?.toString()?.isNumber() && lst[p2]?.toString()?.isNumber()) {
                du = Math.pow(lst[p1] - lst[p2], 2)
            }
            du
        }

        def arbitrary = { us, vs ->
            def res = 0.0
            def size = 135
            for (int i = 1; i <= size; i++) {
                for (int j = i + 1; j <= size; j++) {
                    def d = Math.sqrt(calc(us, i, j) + calc(vs, i ,j))
                    if (d > res)
                        res = d
                }
            }
            res
        }

        def adjacent = { us, vs ->
            def size = 135
            def cols = 9
            def res = 0.0
            for (int i = 1; i <= size; i++) {
                if (i + 1 <= size && i.mod(cols) != 0) {
                    def d = Math.sqrt(calc(us, i, i + 1) + calc(vs, i , i + 1))
                    if (d > res)
                        res = d
                }
                if (i + cols <= size) {
                    def d = Math.sqrt(calc(us, i, i + cols) + calc(vs, i , i + cols))
                    if (d > res)
                        res = d
                }
            }
            res
        }

        meas.each {

            def dataToCalc = [:]
            def output = [:]
            output.put ('code', it.code)
            output.put ('testId', df.format(it.date).toLong())
            output.put ('date', it.date)
            output.put ('sid', it.sid)
            output.put ('testedBy', it.operName)
            output.put ('red_current', it.red)
            output.put ('green_current', it.green)
            output.put ('blue_current', it.blue)
            output.put ('Vr', it.redv)
            output.put ('Vb', it.bluev)
            output.put ('Vg', it.greenv)
            output.put ('comment', it.comment)
            output.put("top200", true);

            def q = new BasicDBObject()
            q.put('WaferID', it.code)
            q.put('sid', it.sid)
            db.measures.find(q, [wavelengthPowerScan:0]).sort({DeviceID:1}).collect{ rec ->

                if (rec.DeviceID) {
                    def m = rec.DeviceID.substring(0, 1)
                    if (!dataToCalc[m])
                        dataToCalc.put(m, [])
                    dataToCalc[m].add(rec)
                }
            }

            def isValid = true
            spots.each { c, n ->
                if (dataToCalc[c] != null && dataToCalc[c].size() < n) {
                    isValid = false
                }
            }

            if (isValid == true) {


                dataToCalc.each { s, rows ->

                    def spot = spots[s]
                    def u = new TreeMap();
                    def v = new TreeMap();
                    def u84 = new TreeMap();
                    def v84 = new TreeMap();
                    def u126 = new TreeMap();
                    def v126 = new TreeMap();

                    float minLv = -1;
                    float maxLv = -1;
                    float minLv13 = 1000000;
                    float maxLv13 = -1;
                    float minx = -1;
                    float maxx = -1;
                    float miny = -1;
                    float maxy = -1;
                    float minu = -1;
                    float maxu = -1;
                    float minv = -1;
                    float maxv = -1;
                    float mintcp = -1;
                    float maxtcp = -1;
                    float sum = 0;

                    if (rows.size() == spot) {

                        def i = 1
                        for (row in rows) {

                            if (i == 7 && spot == 13) {
                                output.put("center_lv", row["photometric"]);
                                output.put("center_x", row["x"]);
                                output.put("center_y", row["y"]);
                                output.put("center_u", row["u"]);
                                output.put("center_v", row["v1960"]);
                                output.put("center_tcp", row["CCT_K"]);
                                output.put("center_lv_13pt", row["photometric"]);
                                output.put("center_x_13pt", row["x"]);
                                output.put("center_y_13pt", row["y"]);
                                output.put("center_u_13pt", row["u"]);
                                output.put("center_v_13pt", row["v1960"]);
                                output.put("center_tcp_13pt", row["CCT_K"]);
                            }

                            if (i == 68 && spot == 135) {
                                output.put("center_lv_135pt", row["photometric"]);
                                output.put("center_x_135pt", row["x"]);
                                output.put("center_y_135pt", row["y"]);
                                output.put("center_u_135pt", row["u"]);
                                output.put("center_v_135pt", row["v1960"]);
                                output.put("center_tcp_135pt", row["CCT_K"]);
                            }

                            if (spot == 135 && i in [119,122,125,93,97,65,68,71,39,43,11,14,17]) {
                                if (row["photometric"] < minLv13)
                                    minLv13 = row["photometric"]
                                if (row["photometric"] > maxLv13)
                                    maxLv13 = row["photometric"]
                            }


                            if (minLv == -1 || minLv > row.photometric) {
                                minLv = row.photometric
                            }
                            if (maxLv == -1 || maxLv < row.photometric) {
                                maxLv = row.photometric
                            }
                            if (minx == -1 || minx > row.x) {
                                minx = row.x
                            }
                            if (maxx == -1 || maxx < row.x) {
                                maxx = row.x
                            }
                            if (miny == -1 || miny > row.y) {
                                miny = row.y
                            }
                            if (maxy == -1 || miny < row.y) {
                                maxy = row.y
                            }
                            if (minu == -1 || minu > row.u) {
                                minu = row.u
                            }
                            if (maxu == -1 || maxu < row.u) {
                                maxu = row.u
                            }
                            if (minv == -1 || minv > row.v1960) {
                                minv = row.v1960
                            }
                            if (maxv == -1 || maxv < row.v1960) {
                                maxv = row.v1960
                            }
                            if (mintcp == -1 || mintcp > row.CCT_K) {
                                mintcp = row.CCT_K
                            }
                            if (maxtcp == -1 || maxtcp < row.CCT_K) {
                                maxtcp = row.CCT_K
                            }
                            sum += row.photometric;

                            u.put(i, row.u);
                            v.put(i, row.v1960);

                            if (spot == 135) {
                                 if (i > 18 && i <= 126 && !(i in [1, 10, 19, 28, 37, 46, 55, 64, 73, 82, 91, 100, 109, 118, 127, 9, 18, 27, 36, 45, 54, 63, 72, 81, 90, 99, 108, 117, 126, 135])) {
                                    u84.put((int) i, row.u)
                                    v84.put((int) i, row.v1960)
                                 }
                                 if (i > 9) {
                                    u126.put((int) i, row.u)
                                    v126.put((int) i, row.v1960)
                                 }
                            }

                            def kd = [:]
                            kd.put('code', output.code)
                            kd.put('testId', output.testId)
                            kd.put('sid', output.sid)
                            kd.put('testType', spot + " spots")
                            kd.put('date', output.date)
                            kd.put('spot', i)
                            kd.put('testedBy', output.testedBy)
                            kd.put('red_current', output.red_current)
                            kd.put('green_current', output.green_current)
                            kd.put('blue_current', output.blue_current)
                            kd.put("top200", true);
                            kd.put("u", row.u);
                            kd.put("v", row.v1960);
                            kd.put("x", row.x);
                            kd.put("y", row.y);
                            kd.put("tcp", row.CCT_K);
                            kd.put("Lv", row.photometric);
                            db.konicaData.save(kd)

                            i++
                        }

                        if (maxLv > 0)
                            output.put("unif_" + spot + "pt", 100 * minLv / maxLv);
                        if (sum > 0)
                            output.put("avg_" + spot + "pt", sum / spot);

                        if (maxLv13 > 0) {
                            output.put("min_13pt_from135", minLv13);
                            output.put("unif_13pt_from135", 100 * minLv13 / maxLv13);
                        }

                        output.put("ciex_" + spot + "pt", maxx - minx);
                        output.put("ciey_" + spot + "pt", maxy - miny);
                        output.put("u_" + spot + "pt", maxu - minu);
                        output.put("v_" + spot + "pt", maxv - minv);
                        output.put("tcp_" + spot + "pt", maxtcp > 0 ? (float) (100 * mintcp / maxtcp) : 0);

                        if (spot == 135) {
                            output.put("color_shift_arbitrary_135pt", arbitrary(u, v));
                            output.put("color_shift_adjacent_135pt", adjacent(u, v));
                            output.put("color_shift_arbitrary_84pt", arbitrary(u84, v84));
                            output.put("color_shift_adjacent_84pt", adjacent(u84, v84));
                            output.put("color_shift_arbitrary_126pt", arbitrary(u126, v126));
                            output.put("color_shift_adjacent_126pt", adjacent(u126, v126));
                        }
                    }
                }

                def q2 = new BasicDBObject()
                q2.put('code', it.code)
                q2.put('sid', it.sid)
                if (db.konica.find(q2).collect { it }.size() == 0) {
                    db.konica.save(output)
                    db.measures.update(q, new BasicDBObject('$set', new BasicDBObject("syncToKonica", "YES")), false, true)
                }
            }
        }
    }
}
