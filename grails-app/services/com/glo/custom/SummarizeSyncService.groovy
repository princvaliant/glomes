package com.glo.custom

import com.glo.ndo.ProductMask
import com.glo.ndo.ProductMaskItem
import com.glo.ndo.ProductMaskItemCtlm
import com.mongodb.BasicDBObject
import org.apache.commons.logging.LogFactory
import org.apache.commons.math3.analysis.interpolation.LinearInterpolator
import org.apache.commons.math3.analysis.polynomials.PolynomialFunction
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics
import org.apache.commons.math3.stat.regression.SimpleRegression
import org.apache.commons.math3.optimization.fitting.PolynomialFitter
import org.apache.commons.math3.optimization.general.LevenbergMarquardtOptimizer

class SummarizeSyncService {

    private static final logr = LogFactory.getLog(this)

    def messageSource
    def unitService
    def mongo
    def waferFilterService

    def init(def unitId, def unitCode, def bdo, def testId, def tkey, def childrenUnits) {

        if (unitCode) {
            def message = [unitId: unitId, unitCode: unitCode, bdo: bdo, testId: testId, tkey: tkey, childrenUnits: childrenUnits]
            onMessage(message)
        }
    }

    def onMessage(def args) {

        def unitId = args.get("unitId")
        def unitCode = args.get("unitCode")
        def tkey = args.get("tkey")
        def bdo = args.get("bdo")
        def childrenUnits = args.get("childrenUnits")
        def testId = args.get("testId")
        def db = mongo.getDB("glo")

        try {

            createSummaries(db, unitId, unitCode, bdo, null, null, testId, tkey, "", childrenUnits)

            db.unit.update(new BasicDBObject("code", unitCode), new BasicDBObject('$set', new BasicDBObject(getSyncVar(tkey), "YES")), false, true)

        } catch (Exception exc) {
            db.unit.update(new BasicDBObject("code", unitCode), new BasicDBObject('$set', new BasicDBObject(getSyncVar(tkey), "FAIL")), false, true)
            logr.error(unitCode?.toString() + ": " + exc)
        }
        null
    }

    def getSyncVar(def tkey) {
        if (tkey == "test_data_visualization" || tkey == "nbp_test_data_visualization" || tkey == "nbp_full_test_visualization" || tkey == "intermediate_coupon_test") {
            "testDataSynced"
        } else if (tkey == "top_test_visualization") {
            "testTopSynced"
        } else if (tkey == "char_test_visualization") {
            "testCharSynced"
        } else if (tkey == "uniformity_test_visualization") {
            "testUniformitySynced"
        } else if (tkey == "full_test_visualization") {
            "testFullSynced"
        } else if (tkey == "spc_data_visualization") {
            "spcDataSynced"
        } else if (tkey == "pre_dbr_test") {
            "testPreDbrSynced"
        } else if (tkey == "post_dbr_test") {
            "testPostDbrSynced"
        } else {
            "NODATA_"
        }
    }

    def createSummaries(
            def db,
            def unitId, def unitCode, def bdo, def pctg, def pkey, def testId, def tkey, def mask, def childrenUnits) {

        def query = new BasicDBObject()

        query.put("value.code", unitCode)
        query.put("value.tkey", tkey)
        query.put("value.parentCode", null)
        query.put("value.testId", testId)

        def tests = []
        def units = []
        def centers = []

        if (!bdo) {
            bdo = new BasicDBObject()
        }

        def maskObj = ProductMask.findByName(mask)
        if (maskObj?.isPcm && tkey in ["test_data_visualization", "nbp_test_data_visualization", "char_test_visualization", "uniformity_test_visualization", "pre_dbr_test", "post_dbr_test", "intermediate_coupon_test"]) {
            this.calculatePcmMask(db, bdo, unitCode, unitId, tkey, testId, mask)
        }

        if (tkey in ["pre_dbr_test", "post_dbr_test"]) {
            pkey = "dbr_process"
        }
        if (tkey in ["test_data_visualization", "nbp_test_data_visualization", "pre_dbr_test", "post_dbr_test", "intermediate_coupon_test"]) {
            centers = this.calculateCenter(db, bdo, unitCode, unitId, tkey, testId, mask)
        }

        def adminFilters02 = waferFilterService.getAdminFilters("Data @ 200uA")
        def adminFilters06 = waferFilterService.getAdminFilters("Data @ 600uA")
        def adminFilters1 = waferFilterService.getAdminFilters("Data @ 1mA")
        def adminFilters2 = waferFilterService.getAdminFilters("Data @ 2mA")
//        def adminFilters5 = waferFilterService.getAdminFilters("Data @ 5mA")
//        def adminFilters10 = waferFilterService.getAdminFilters("Data @ 10mA")
//        def adminFilters20 = waferFilterService.getAdminFilters("Data @ 20mA")
//        def adminFilters50 = waferFilterService.getAdminFilters("Data @ 50mA")
//        def adminFilters100 = waferFilterService.getAdminFilters("Data @ 100mA")

        def testData = db.testData.find(query, new BasicDBObject()).collect { it }[0]
        if (testData) {

            try {
                this.calculatePeakEqeSummary(db, bdo, testData["value"]["data"], unitCode, unitId, testId)
                db.testData.save(testData)
            } catch (Exception exc) {
                logr.error(exc.getMessage())
            }


            if (tkey == "top_test_visualization") {

                this.calculateTopSummary(db, bdo, testData["value"]["data"], unitCode, unitId, testId)
            } else {

                // Extract currents for current densities
                def currents = getCurrentsForDensity([100000, 500000, 1000000, 2000000, 8000000], testData["value"]["data"], maskObj);

                currents.each {density, current ->
                    prepareSummary(db, bdo, current, testData["value"]["data"], unitCode, unitId, testId, [], centers, testData["value"]["data"]["Current @ 2V"], density)
                }

                prepareSummary(db, bdo, "0.2mA", testData["value"]["data"], unitCode, unitId, testId, adminFilters02, centers, testData["value"]["data"]["Current @ 2V"], 0)
                prepareSummary(db, bdo, "0.6mA", testData["value"]["data"], unitCode, unitId, testId, adminFilters06, centers, testData["value"]["data"]["Current @ 2V"], 0)
                prepareSummary(db, bdo, "1mA", testData["value"]["data"], unitCode, unitId, testId, adminFilters1, centers, testData["value"]["data"]["Current @ 2V"], 0)
                prepareSummary(db, bdo, "2mA", testData["value"]["data"], unitCode, unitId, testId, adminFilters2, centers, testData["value"]["data"]["Current @ 2V"], 0)
//                prepareSummary(db, bdo, "5mA", testData["value"]["data"], unitCode, unitId, testId, adminFilters5, centers, testData["value"]["data"]["Current @ 2V"], 0)
//                prepareSummary(db, bdo, "10mA", testData["value"]["data"], unitCode, unitId, testId, adminFilters10, centers, testData["value"]["data"]["Current @ 2V"], 0)
//                prepareSummary(db, bdo, "20mA", testData["value"]["data"], unitCode, unitId, testId, adminFilters20, centers, testData["value"]["data"]["Current @ 2V"], 0)
//                prepareSummary(db, bdo, "50mA", testData["value"]["data"], unitCode, unitId, testId, adminFilters50, centers, testData["value"]["data"]["Current @ 2V"], 0)
//                prepareSummary(db, bdo, "100mA", testData["value"]["data"], unitCode, unitId, testId, adminFilters100, centers, testData["value"]["data"]["Current @ 2V"], 0)

                if (testData["value"]["data"]["Current @ 2V"]) {
                    createSummary(db, "Current @ 2V", testData["value"]["data"], bdo, unitCode, testData["value"]["data"]["Current @ 2V"]["NA"].collect {
                        it.key
                    }, testId, centers, testData["value"]["data"]["Current @ 2V"], 0)
                }
                if (testData["value"]["data"]["peakEqeCurrent"]) {
                    createSummary(db, "peakEqeCurrent", testData["value"]["data"], bdo, unitCode, testData["value"]["data"]["peakEqeCurrent"]["NA"].collect {
                        it.key
                    }, testId, centers, testData["value"]["data"]["peakEqeCurrent"], 0)
                }
                if (testData["value"]["data"]["peakEqe"]) {
                    createSummary(db, "peakEqe", testData["value"]["data"], bdo, unitCode, testData["value"]["data"]["peakEqe"]["NA"].collect {
                        it.key
                    }, testId, centers, testData["value"]["data"]["peakEqe"], 0)
                }
            }

            bdo.put(getSyncVar(tkey), "YES")
            bdo.put("processCategory", (pctg ?: "nwLED"))
            bdo.put("processKey", (pkey ?: "test"))
            bdo.put("taskKey", tkey)
            bdo.put("id", unitId)

            unitService.update(bdo, "admin", true)
        } else {
            db.unit.update(new BasicDBObject("code", unitCode), new BasicDBObject('$set', new BasicDBObject(getSyncVar(tkey), "FAIL")), false, false)
        }
        testData = null

        1
    }

    private def getCurrentsForDensity(densities, data, maskObj) {
        def ret = [:]
        def area = maskObj.activeArea
        if (area == 0) {
            return ret;
        }
        densities.each { density ->
            def reqCurr = density * area / 1000 ;
            TreeMap sorted = new TreeMap()
            data.each { current, values ->
                def curval = currValue(current)
                if (curval > 0) {
                    sorted.put((curval - reqCurr).abs(), current)
                }
            }
            def s = sorted.sort()
            ret.put(density, s.firstEntry().value)
        }
        return ret
    }

    private def calculateCenter(def db, def bdo, def unitCode, def unitId, def tkey, def testId, def mask) {

        def prodMask = ProductMask.findByName(mask)
        Set s = ProductMaskItem.executeQuery("""
				select ps.code from ProductMaskItem as ps where ps.productMask = ? and  SQRT(POW(ps.plX,2) + POW(ps.plY,2)) <= 6
			""", [prodMask])
        s
    }

    private def calculatePcmMask(def db, def bdo, def unitCode, def unitId, def tkey, def testId, def mask) {

        // get PCM devices from database
        // Get product mask item ctlm

        [1, 2, 3, 4, 5].each { devNo ->

            def prodMaskItems = ProductMaskItemCtlm.executeQuery("""
				select ps from ProductMaskItem as ps where ps.productMask.name = '$mask' and ps.pm like 'PM$devNo%'
			""")

            def tdRnn
            def tdRpp
            def tdRsnake
            def tdRnn2

            prodMaskItems.each { prodMaskItem ->

                if (prodMaskItem.pm == "PM" + devNo + "2") {

                    tdRnn = db.testData.find(["value.code": unitCode + "_" + prodMaskItem.code, "value.tkey": tkey, "value.testId": testId], ["value.data.Datavoltage.data": 1]).collect {
                        it.value?.data?.Datavoltage?.data
                    }[0]
                }
                if (prodMaskItem.pm == "PM" + devNo + "4") {

                    tdRpp = db.testData.find(["value.code": unitCode + "_" + prodMaskItem.code, "value.tkey": tkey, "value.testId": testId], ["value.data.Datavoltage.data": 1]).collect {
                        it.value?.data?.Datavoltage?.data
                    }[0]
                }
                if (prodMaskItem.pm == "PM" + devNo + "S") {

                    tdRsnake = db.testData.find(["value.code": unitCode + "_" + prodMaskItem.code, "value.tkey": tkey, "value.testId": testId], ["value.data.Datavoltage.data": 1]).collect {
                        it.value?.data?.Datavoltage?.data
                    }[0]
                }
                if (prodMaskItem.pm == "PM" + devNo + "A") {

                    tdRnn2 = db.testData.find(["value.code": unitCode + "_" + prodMaskItem.code, "value.tkey": tkey, "value.testId": testId], ["value.data.Datavoltage.data": 1]).collect {
                        it.value?.data?.Datavoltage?.data
                    }[0]
                }
            }

            if (tdRnn) {

                SimpleRegression sreg = new SimpleRegression()
                tdRnn.each {

                    Float val = it[1] / 1000
                    if (val >= 0.000001 && val <= 0.01) {
                        sreg.addData(it[1] / 1000, it[0])
                    }
                }
                def s = sreg.getSlope()
                if (!s.isNaN() && Math.abs(s) < 1E8) {
                    bdo.put("pcm" + devNo + "_rnn", s)
                }
                def si = sreg.getIntercept()
                if (!si.isNaN() && Math.abs(si) < 1E8) {
                    bdo.put("pcm" + devNo + "_vnn", si)
                }
            }

            if (tdRpp) {

                SimpleRegression sreg = new SimpleRegression()
                tdRpp.each {
                    Float val = it[1] / 1000
                    if (val >= 0.000001 && val <= 0.01) {
                        sreg.addData(it[1] / 1000, it[0])
                    }
                }
                def s = sreg.getSlope()
                if (!s.isNaN() && Math.abs(s) < 1E8) {
                    bdo.put("pcm" + devNo + "_rpp", s)
                }
                def si = sreg.getIntercept()
                if (!si.isNaN() && Math.abs(si) < 1E8) {
                    bdo.put("pcm" + devNo + "_vpp", si)
                }
            }

            if (tdRsnake) {

                SimpleRegression sreg = new SimpleRegression()
                tdRsnake.each {

                    Float val = it[1] / 1000
                    if (val >= 0.000001 && val <= 0.01) {
                        sreg.addData(it[1] / 1000, it[0])
                    }
                }
                def s = sreg.getSlope()
                if (!s.isNaN() && Math.abs(s) < 1E8) {
                    bdo.put("pcm" + devNo + "_rsnake", s)
                }
                def si = sreg.getIntercept()
                if (!si.isNaN() && Math.abs(si) < 1E8) {
                    bdo.put("pcm" + devNo + "_vsnake", si)
                }
            }

            if (tdRnn2) {

                SimpleRegression sreg = new SimpleRegression()
                tdRnn2.each {

                    Float val = it[1] / 1000
                    if (val >= 0.000001 && val <= 0.01) {
                        sreg.addData(it[1] / 1000, it[0])
                    }
                }
                def s = sreg.getSlope()
                if (!s.isNaN() && Math.abs(s) < 1E8) {
                    bdo.put("pcm" + devNo + "_rnn2", s)
                }
                def si = sreg.getIntercept()
                if (!si.isNaN() && Math.abs(si) < 1E8) {
                    bdo.put("pcm" + devNo + "_vnn2", si)
                }
            }
        }

        bdo
    }

    private def calculatePeakEqeSummary(def db, def bdo, def data, def unitCode, def unitId, def testId) {

        TreeMap currs = new TreeMap()
        TreeMap eqes = new TreeMap()

        data.each { current, values ->

            def curval = currValue(current)
            if (curval > 0) {
                values.EQE?.each { device, value ->

                    if (!currs[device]) {
                        currs.put(device, [])
                        eqes.put(device, new DescriptiveStatistics())
                    }

                    currs[device].add([curval, value])
                    eqes[device].addValue(value)
                }
            }
        }

        PolynomialFitter fitter = new PolynomialFitter(2, new LevenbergMarquardtOptimizer())

        data.put("peakEqeCurrent", [:])
        data["peakEqeCurrent"].put("NA", [:])
        data.put("peakEqe", [:])
        data["peakEqe"].put("NA", [:])

        currs.each { device, values ->
            fitter.clearObservations()
            def maxEqe = eqes[device].getMax()
            def currAtMax = 0

            if (!maxEqe.isNaN()) {
                def vals = values.sort { a, b -> a[0] <=> b[0] }

                for (int i = 0; i < values.size(); i++) {
                    if (vals[i][1] == maxEqe && i - 1 >= 0 && values.size() > i + 1) {
                        if (vals[i - 1][0] >= 0 && vals[i - 1][1] >= 0)
                            fitter.addObservedPoint(vals[i - 1][0], vals[i - 1][1])
                        if (vals[i][0] >= 0 && vals[i][1] >= 0)
                            fitter.addObservedPoint(vals[i][0], vals[i][1])
                        if (vals[i + 1][0] >= 0 && vals[i + 1][1] >= 0)
                            fitter.addObservedPoint(vals[i + 1][0], vals[i + 1][1])
                    }
                    if (vals[i][1] == maxEqe) {
                        currAtMax = vals[i][0]
                    }
                }
                def sob = fitter.getObservations().size()
                if (sob == 3) {
                    double[] coef = fitter.fit()
                    def currpeak = -coef[1] / (2 * coef[2])
                    def eqepeak = coef[2] * (currpeak**2) + coef[1] * currpeak + coef[0]
                    if (!(currpeak.isNaN() || eqepeak.isNaN())) {
                        data["peakEqeCurrent"]["NA"].put(device, currpeak)
                        data["peakEqe"]["NA"].put(device, eqepeak)
                    }
                } else {
                    data["peakEqeCurrent"]["NA"].put(device, currAtMax)
                    data["peakEqe"]["NA"].put(device, maxEqe)
                }
            }
        }
    }

    private def calculateTopSummary(def db, def bdo, def data, def unitCode, def unitId, def testId) {

        TreeMap eqes = new TreeMap()

        data.each { current, values ->

            values.EQE?.each { device, value ->

                def volt = values['Pulse Voltage (V)'][device] ?: 0
                eqes.put(value, [device, current, volt])
            }
        }

        def best = eqes.pollLastEntry()
        def i = 0

        DescriptiveStatistics statsEqe = new DescriptiveStatistics()
        DescriptiveStatistics statsCurr = new DescriptiveStatistics()
        DescriptiveStatistics statsVolt = new DescriptiveStatistics()

        if (best) {

            bdo.put("bestEqe", best?.getKey())
            bdo.put("currentForBestEqe", currValue(best.getValue()[1]))
            bdo.put("voltageForBestEqe", best.getValue()[2])
            bdo.put("peakWavelengthForCurrent", data[best.getValue()[1]]["Peak (nm)"][best.getValue()[0]])

            statsEqe.addValue(best?.getKey())
            statsCurr.addValue(currValue(best.getValue()[1]))
            statsVolt.addValue(best.getValue()[2])
            i++
        }

        while (i < 5) {

            def best2 = eqes.pollLastEntry()
            if (best2) {
                statsEqe.addValue(best2?.getKey())
                statsCurr.addValue(currValue(best2.getValue()[1]))
                statsVolt.addValue(best2.getValue()[2])
            }
            i++
        }

        def meanEqe = statsEqe.getMean()
        if (meanEqe.isNaN())
            meanEqe = "N/A"
        def meanCurr = statsCurr.getMean()
        if (meanCurr.isNaN())
            meanCurr = "N/A"
        def meanVolt = statsVolt.getMean()
        if (meanVolt.isNaN())
            meanVolt = "N/A"

        bdo.put("top5BestEqeMean", meanEqe)
        bdo.put("top5CurrentForBestEqeMean", meanCurr)
        bdo.put("top5VoltageForBestEqeMean", meanVolt)
    }


    private currValue(def currString) {

        def curr = currString.tokenize('@')[1]
        if (!curr) {
            return 0
        }

        Integer currVal = Integer.parseInt(curr.replaceAll("\\D+", ""))
        Float currVal2 = currVal
        if (curr.indexOf('mA') > 0) currVal2 = currVal
        if (curr.indexOf('uA') > 0) currVal2 = currVal * 1E-3
        if (curr.indexOf('nA') > 0) currVal2 = currVal * 1E-6
        if (curr.indexOf('pA') > 0) currVal2 = currVal * 1E-9
        if (curr.indexOf('fA') > 0) currVal2 = currVal * 1E-12
        currVal2

    }

    private def prepareSummary(
            def db,
            def bdo,
            def current,
            def data, def unitCode, def unitId, def testId, def adminFilters, def centers, def currentsAt2, def density) {

        def currKey = "Data @ " + current
        if (current == "0.2mA") {
            currKey = "Data @ 200uA"
        }
        if (current == "0.6mA") {
            currKey = "Data @ 600uA"
        }
        if (density > 0) {
            currKey = current
        }

        Set filtered = new HashSet()
        adminFilters.eachWithIndex { waferFilter, i ->
            if (data[waferFilter.level1]) {
                def unfiltered = (BasicDBObject) data[waferFilter.level1][waferFilter.level2]

                if (unfiltered) {
                    unfiltered.each { k2, v2 ->
                        if (waferFilter.valFrom <= v2 && waferFilter.valTo >= v2 && i == 0) {
                            filtered.add(k2)
                        } else if ((waferFilter.valFrom > v2 || waferFilter.valTo < v2) && i > 0) {
                            if (filtered.contains(k2))
                                filtered.remove(k2)
                        }
                    }
                }
            }
        }

        if (density > 0 || current in ["0.2mA", "0.6mA", "1mA", "2mA", "5mA", "20mA"]) {
            try {
                createSummary2(db, currKey, data, bdo, unitCode, filtered, testId, centers, currentsAt2, density)
            } catch (Exception exc) {

            }
        }

        createSummary(db, currKey, data, bdo, unitCode, filtered, testId, centers, currentsAt2, density)
    }

    def createSummary2(
            def db, def currKey, def data, def bdo, def code, def filtered, def testId, def centers, def currentsAt2, def density ) {

        def curr2Vs = currentsAt2 ? currentsAt2["NA"] : [:]
        def peakEqe = data["peakEqe"] ? data["peakEqe"]["NA"] : [:]
        def peakEqeCurrent = data["peakEqeCurrent"] ? data["peakEqeCurrent"]["NA"] : [:]
        def cstr = currKey.tokenize(" ")[2]
        if (density > 0) {
            cstr = densityAsString(density)
        }

        def percentile50 = { values ->
            def medianVal = values?.getPercentile(50)
            if (medianVal.isNaN())
                medianVal = "NN/AA"
            medianVal
        }

        def topn = { count, percentile ->
            int topd = (int) (count * percentile/100)
            if (topd == 0 && count > 0) {
                topd = 1
            }
            topd
        }


        def currents = [:]
        data.each { currStr, currData ->

            try {
                Integer currVal = Integer.parseInt(currStr.replaceAll("\\D+", ""))
                Float currVal2 = currVal
                if (currStr.indexOf('mA') > 0) currVal2 = currVal
                if (currStr.indexOf('uA') > 0) currVal2 = currVal * 1E-3
                if (currStr.indexOf('nA') > 0) currVal2 = currVal * 1E-6
                if (currStr.indexOf('pA') > 0) currVal2 = currVal * 1E-9
                if (currStr.indexOf('fA') > 0) currVal2 = currVal * 1E-12
                currents.put(currVal2, currStr)
            } catch (Exception exc) {

            }

            if (currStr == currKey) {

                def eqesRaw = [:]
                def eqesFiltered = [:]

                if (currData["EQE"]) {
                    currData["EQE"].each { devCode, devValue ->
                        eqesRaw.put(devCode, [:])
                        if (density == 0 && filtered.contains(devCode)) {
                            eqesFiltered.put(devCode, [:])
                        } else {
                            eqesFiltered.put(devCode, [:])
                        }
                    }
                }
                if (currData["Photometric Power"]) {
                    def nTested = 0
                    def nLighted = 0
                    currData["Photometric Power"].each { devCode, devValue ->
                        nTested += 1
                        if (devValue > 0) {
                           nLighted += 1
                        }
                    }
                    bdo.put("nTested", nTested)
                    bdo.put("nLighted", nLighted)
                }
                def rawsize = eqesRaw.size()
                def pp = "N/A"
                if (rawsize > 0) {
                    pp = 100 * eqesFiltered.size() / rawsize
                }
                bdo.put("PassPercent-" + cstr, pp)

                currData.each { varName, devices ->
                    devices.each { devCode, devValue ->
                        if (eqesFiltered[devCode] != null) {
                            eqesFiltered[devCode].put(varName, devValue)
                            eqesFiltered[devCode].put("IF@2V", curr2Vs[devCode])
                            eqesFiltered[devCode].put("peakEqe", peakEqe[devCode])
                            eqesFiltered[devCode].put("peakEqeCurrent", peakEqeCurrent[devCode])
                        }
                    }
                }

                // Calculate lumens per watt
                def dataSet = []
                eqesFiltered.each { devCode, varData ->
                    def p = (varData["Pulse Current (mA)"] ?: 0) * (varData["Pulse Voltage (V)"] ?: 0) / 1000
                    def lpw = 0
                    if (p != 0) {
                        lpw = (varData["Photometric Power"] ?: 0) / p
                    }
                    varData.put("LPW", lpw)

                    if (!dataSet) {
                        dataSet = varData
                    }
                }

                def eqesFilteredByEqe = eqesFiltered.sort { -it.value.EQE }
                def eqesFilteredByEqePeak = eqesFiltered.sort { -(it?.value?.peakEqe ?: 0)}
                def eqesFilteredByEqeCenter = eqesFilteredByEqe.findAll { centers.contains(it.key) }

                // Group statistic based best EQEs and on peak wavelength range
                [[515,520],[520,525],[525,595],[595,600],[600,605],[605,610],[610,999]].each { peak ->
                    [10].each { percentile ->
                        def statistic = [:]
                        def peaks = eqesFiltered.findAll { it.value["Peak (nm)"] >= peak[0] && it.value["Peak (nm)"] < peak[1] }
                        peaks = peaks.sort { -it.value.EQE }
                        def eqesFilteredSize = eqesFiltered.size()
                        def peakPerc
                        if (eqesFilteredSize > 0 && peaks.size() > 0)
                            peakPerc = 100 * peaks.size() / eqesFilteredSize
                        else
                            peakPerc = "NN/AA"
                        bdo.put("CB" + peak[0] + "-" + cstr + "-PassPercent", peakPerc)

                        peaks.take(topn(peaks.size(),percentile)).each { devCode, varData ->
                            varData.each { varName, value ->
                                if (!statistic[varName]) {
                                    statistic.put(varName, new DescriptiveStatistics())
                                }
                                if (value != null) {
                                    statistic[varName].addValue(value)
                                }
                            }
                        }

                        if (peakPerc == "NN/AA") {
                            dataSet.each { varName, value ->
                                bdo.put("CB" + peak[0] + "-P" + (100 - percentile) + "-" + cstr + "-" + varName, "NN/AA")
                            }
                        } else {
                            statistic.each { varName, values ->
                                bdo.put("CB" + peak[0] + "-P" + (100 - percentile) + "-" + cstr + "-" + varName, percentile50(values))
                            }
                        }
                    }
                }

                // Calculate statistics based on best EQEs
                [10, 25, 50, 75, 90].each { percentile ->
                    def statistic = [:]
                    eqesFilteredByEqe.take(topn(eqesFilteredByEqe.size(),percentile)).each { devCode, varData ->
                        varData.each { varName, value ->
                            if (!statistic[varName]) {
                                statistic.put(varName, new DescriptiveStatistics())
                            }
                            if (value != null) {
                                statistic[varName].addValue(value)
                            }
                        }
                    }
                    statistic.each { varName, values ->
                        bdo.put("P" + (100 - percentile) + "-" + cstr + "-" + varName, percentile50(values))
                    }
                }

                // Calculate statistics based on best peak EQEs
                [10].each { percentile ->
                    def statistic = [:]
                    eqesFilteredByEqePeak.take(topn(eqesFilteredByEqePeak.size(),percentile)).each { devCode, varData ->
                        varData.each { varName, value ->
                            if (!statistic[varName]) {
                                statistic.put(varName, new DescriptiveStatistics())
                            }
                            if (value != null) {
                                statistic[varName].addValue(value)
                            }
                        }
                    }
                    statistic.each { varName, values ->
                        if (varName == "peakEqe")
                            bdo.put("P" + (100 - percentile) + "-peakEqe", percentile50(values))
                        else if (varName == "peakEqeCurrent")
                            bdo.put("P" + (100 - percentile) + "-peakEqeCurrent", percentile50(values))
                        else
                            bdo.put("P" + (100 - percentile) + "-PEQE-" + varName + "-" + cstr, percentile50(values))
                    }
                }

                // Calculate statistics based on best EQEs for center devices
                [50].each { percentile ->
                    def statistic = [:]
                    eqesFilteredByEqeCenter.take(topn(eqesFilteredByEqeCenter.size(),percentile)).each { devCode, varData ->
                        varData.each { varName, value ->
                            if (!statistic[varName]) {
                                statistic.put(varName, new DescriptiveStatistics())
                            }
                            if (value != null) {
                                statistic[varName].addValue(value)
                            }
                        }
                    }
                    statistic.each { varName, values ->
                        bdo.put("P" + (100 - percentile) + "-Center-" + cstr + "-" + varName, percentile50(values))
                    }
                }

                // Calculate statistics based on best EQEs for center devices
                [90].each { percentile ->
                    def statistic = [:]
                    eqesFilteredByEqeCenter.take(topn(eqesFilteredByEqeCenter.size(),percentile)).each { devCode, varData ->
                        varData.each { varName, value ->
                            if (!statistic[varName]) {
                                statistic.put(varName, new DescriptiveStatistics())
                            }
                            if (value != null) {
                                statistic[varName].addValue(value)
                            }
                        }
                    }
                    statistic.each { varName, values ->
                        bdo.put("P" + (100 - percentile) + "-Center-" + cstr + "-" + varName, percentile50(values))
                    }
                }

                eqesFiltered = null
                eqesRaw = null
                eqesFilteredByEqe = null
                eqesFilteredByEqePeak = null
                eqesFilteredByEqeCenter = null
            }
        }

        // Find first lowest current for given peak EQE current
        def peakWL = []
        def currentsReverse = currents.sort{-it.key}
        peakEqeCurrent.each { devCode, peakCurrent ->
            for(current in currentsReverse) {
                if (peakCurrent >= current.key) {
                    if (data[current.value] && data[current.value]["Peak (nm)"]&& data[current.value]["Peak (nm)"][devCode])
                        peakWL.add(data[current.value]["Peak (nm)"][devCode])
                    break
                }
            }
        }
        def peakWLSorted = peakWL.sort().reverse()
        [10].each { percentile ->
            def statistic = new DescriptiveStatistics()
            peakWLSorted.take((int) peakWLSorted.size() * (percentile/100)).each {
                if (it != null) {
                    statistic.addValue(it)
                }
            }
            bdo.put("P" + (100 - percentile) + "-PEQE-PeakL-at-Peak", percentile50(statistic))
        }
    }

    private def createSummary(
            def db, def currKey, def data, def bdo, def code, def filtered, def testId, def centers, def currentsAt2, density) {

        def curr2Vs = currentsAt2 ? currentsAt2["NA"] : [:]

        data.each { k, v ->

            if (k == currKey) {

                def retMap1 = [:]
                def retMap2 = [:]
                def retMap3 = [:]

                def cstr = k
                if (density > 0) {
                    cstr = densityAsString(density)
                }

                v.each { k1, v1 ->

                    v1.each { k2, v2 ->

                        if (density > 0 || filtered.contains(k2)) {

                            retMap1.put(k2, v2)

                            if (centers.contains(k2)) {
                                retMap2.put(k2, v2)
                            }

                            // For Leakage currents
                            if (curr2Vs[k2] < 0.05) {
                                retMap3.put(k2, v2)
                            }
                        }
                    }


                    DescriptiveStatistics stats2 = new DescriptiveStatistics((double[]) retMap1.collect {
                        (double) (it.value == null || !it.value.toString().isNumber() ? 0 : it.value)
                    })

                    def minVal = stats2.getMin()
                    if (minVal.isNaN())
                        minVal = "N/A"

                    def maxVal = stats2.getMax()
                    if (maxVal.isNaN())
                        maxVal = "N/A"

                    def meanVal = stats2.getMean()
                    if (meanVal.isNaN())
                        meanVal = "N/A"

                    def medianVal = stats2.getPercentile(50)
                    if (medianVal.isNaN())
                        medianVal = "N/A"

                    def stdVal = stats2.getStandardDeviation()
                    if (stdVal.isNaN())
                        stdVal = "N/A"

                    def kf = cstr
                    if (density > 0) {
                        kf = "Data @ " + cstr
                    }

                    bdo.put(kf + " " + k1 + " min", minVal)
                    bdo.put(kf + " " + k1 + " max", maxVal)
                    bdo.put(kf + " " + k1 + " mean", meanVal)
                    bdo.put(kf + " " + k1 + " median", medianVal)
                    bdo.put(kf + " " + k1 + " stddev", stdVal)

                    // Calculate centers
                    DescriptiveStatistics stats22 = new DescriptiveStatistics((double[]) retMap2.collect {
                        (double) (it.value == null || !it.value.toString().isNumber() ? 0 : it.value)
                    })

                    def minVal2 = stats22.getMin()
                    if (minVal2.isNaN())
                        minVal2 = "N/A"

                    def maxVal2 = stats22.getMax()
                    if (maxVal2.isNaN())
                        maxVal2 = "N/A"

                    def meanVal2 = stats22.getMean()
                    if (meanVal2.isNaN())
                        meanVal2 = "N/A"

                    def medianVal2 = stats22.getPercentile(50)
                    if (medianVal2.isNaN())
                        medianVal2 = "N/A"

                    def stdVal2 = stats22.getStandardDeviation()
                    if (stdVal2.isNaN())
                        stdVal2 = "N/A"

                    def ky = k.replace("Data", "Center")
                    def ky2 = k1
                    if (k == "Current @ 2V") {
                        ky2 = "Center"
                    }

                    if (density > 0) {
                        ky = "Center @ " + cstr
                    }

                    bdo.put(ky + " " + ky2 + " min", minVal2)
                    bdo.put(ky + " " + ky2 + " max", maxVal2)
                    bdo.put(ky + " " + ky2 + " mean", meanVal2)
                    bdo.put(ky + " " + ky2 + " median", medianVal2)
                    bdo.put(ky + " " + ky2 + " stddev", stdVal2)
                }

                ////////////////////////////////////////////////////

                def retMapWpe = []
                def retMapEqe = []
                def retMapEqeLL = []

                v.each { k1, v1 ->

                    if (k1 == "WPE") {
                        v1.each { k2, v2 ->
                            if (filtered.contains(k2)) {
                                retMapWpe.add([v2, k2])
                            }
                        }
                    }

                    if (k1 == "EQE") {
                        v1.each { k2, v2 ->
                            if (filtered.contains(k2)) {
                                retMapEqe.add([v2, k2])
                            }
                            if (retMap3.containsKey(k2)) {
                                retMapEqeLL.add([v2, k2])
                            }
                        }
                    }
                }

                retMapWpe = retMapWpe.sort { it[0] }
                retMapEqe = retMapEqe.sort { it[0] }
                retMapEqeLL = retMapEqeLL.sort { it[0] }

                DescriptiveStatistics stats05 = new DescriptiveStatistics()
                DescriptiveStatistics stats20 = new DescriptiveStatistics()

                def ids5Wpe = []
                def ids20Wpe = []
                def ids5Eqe = []
                def ids20EqeLL = []

                (1..20).each {
                    if (retMapWpe) {
                        def pop1 = retMapWpe.pop()
                        if (pop1[0]) {

                            if (ids5Wpe.size() < 5) {
                                stats05.addValue(pop1[0])
                                ids5Wpe.add(pop1[1])
                            }

                            stats20.addValue(pop1[0])
                            ids20Wpe.add(pop1[1])
                        }
                    }
                    if (retMapEqe) {
                        def pop1 = retMapEqe.pop()
                        if (pop1[0]) {

                            if (ids5Eqe.size() < 5) {
                                ids5Eqe.add(pop1[1])
                            }
                        }
                    }
                    if (retMapEqeLL) {
                        def pop1 = retMapEqeLL.pop()
                        if (pop1[0]) {
                            ids20EqeLL.add(pop1[1])
                        }
                    }
                }

                def cc = currKey.tokenize(" ")[2]

                if (cc in ["5mA", "10mA", "20mA"]) {

                    TreeSet tops = new TreeSet()
                    if (bdo["topDevsForTest"]) {
                        bdo["topDevsForTest"].each { topId ->
                            tops.add(topId)
                        }
                    }
                    ids5Wpe.each { topId ->
                        tops.add(topId)
                    }
                    ids5Eqe.each { topId ->
                        tops.add(topId)
                    }
                    bdo.put("topDevsForTest", tops)
                }

                def top5Mean = stats05.getMean()
                if (top5Mean.isNaN()) {
                    top5Mean = "N/A"
                }
                bdo.put("top5 WPE @ " + cc, top5Mean)

                def top20Mean = stats20.getMean()
                if (top20Mean.isNaN()) {
                    top20Mean = "N/A"
                }
                bdo.put("top20 WPE @ " + cc, top20Mean)


                calculateWpe(db, currKey, ids5Wpe, v, k, "5", code, bdo, testId, " WPE ", "")
                calculateWpe(db, currKey, ids20Wpe, v, k, "20", code, bdo, testId, " WPE ", "")
                calculateWpe(db, currKey, ids20EqeLL, v, k, "20", code, bdo, testId, " EQE ", " LL")
            }
        }
    }

    private def calculateWpe(
            def db, def currKey, def arr, def v, def k, def top, def code, def bdo, def testId, def grp, def leak) {

        def cc = currKey.tokenize(" ")[2]

        if (arr && (cc == "10mA" || cc == "20mA" || cc == "5mA")) {

            DescriptiveStatistics statsEqe = new DescriptiveStatistics()
            DescriptiveStatistics statsPeak = new DescriptiveStatistics()
            DescriptiveStatistics statsCentroid = new DescriptiveStatistics()
            DescriptiveStatistics statsFwhm = new DescriptiveStatistics()
            DescriptiveStatistics statsDominant = new DescriptiveStatistics()
            DescriptiveStatistics statsVf = new DescriptiveStatistics()
            DescriptiveStatistics statsIntPower = new DescriptiveStatistics()
            DescriptiveStatistics statsPhotometricPower = new DescriptiveStatistics()


            v.each { k1, v1 ->

                if (k == currKey && k1 == "EQE") {
                    v1.each { k2, v2 ->
                        if (arr.contains(k2)) {
                            statsEqe.addValue(v2)
                        }
                    }
                }

                if (k == currKey && k1 == "Peak (nm)") {
                    v1.each { k2, v2 ->
                        if (arr.contains(k2)) {
                            statsPeak.addValue(v2)
                        }
                    }
                }

                if (k == currKey && k1 == "Centroid (nm)") {
                    v1.each { k2, v2 ->
                        if (arr.contains(k2)) {
                            statsCentroid.addValue(v2)
                        }
                    }
                }

                if (k == currKey && k1 == "FWHM (nm)") {
                    v1.each { k2, v2 ->
                        if (arr.contains(k2)) {
                            statsFwhm.addValue(v2)
                        }
                    }
                }

                if (k == currKey && k1 == "Dominant wl (nm)") {
                    v1.each { k2, v2 ->
                        if (arr.contains(k2)) {
                            statsDominant.addValue(v2)
                        }
                    }
                }

                if (k == currKey && k1 == "Pulse Voltage (V)") {
                    v1.each { k2, v2 ->
                        if (arr.contains(k2)) {
                            statsVf.addValue(v2)
                        }
                    }
                }

                if (k == currKey && k1 == "Int Power (W)") {
                    v1.each { k2, v2 ->
                        if (arr.contains(k2)) {
                            statsIntPower.addValue(v2)
                        }
                    }
                }

                if (k == currKey && k1 == "Photometric Power") {
                    v1.each { k2, v2 ->
                        if (arr.contains(k2)) {
                            statsPhotometricPower.addValue(v2)
                        }
                    }
                }
            }

            def topMean1 = "N/A"
            topMean1 = statsEqe.getMean()
            if (topMean1 && topMean1.isNaN()) {
                topMean1 = "N/A"
            } else {
                def r = statsEqe.getMax() - statsEqe.getMin()
                bdo.put("EQE R of top" + top + grp + cc + leak, r)
            }
            bdo.put("EQE avg of top" + top + grp + cc + leak, topMean1)

            def topMean2 = "N/A"
            topMean2 = statsPeak.getMean()
            if (topMean2 && topMean2.isNaN()) {
                topMean2 = "N/A"
            } else {
                def r = statsPeak.getMax() - statsPeak.getMin()
                bdo.put("Peak (nm) R of top" + top + grp + cc + leak, r)
            }
            bdo.put("Peak (nm) avg of top" + top + grp + cc + leak, topMean2)

            def topMean3 = "N/A"
            topMean3 = statsCentroid.getMean()
            if (topMean3 && topMean3.isNaN()) {
                topMean3 = "N/A"
            }
            bdo.put("Centroid (nm) avg of top" + top + grp + cc + leak, topMean3)

            def topMean4 = "N/A"
            topMean4 = statsFwhm.getMean()
            if (topMean4 && topMean4.isNaN()) {
                topMean4 = "N/A"
            }
            bdo.put("FWHM (nm) avg of top" + top + grp + cc + leak, topMean4)

            def topMean5 = "N/A"
            topMean5 = statsDominant.getMean()
            if (topMean5 && topMean5.isNaN()) {
                topMean5 = "N/A"
            }
            bdo.put("Dominant (nm) avg of top" + top + grp + cc + leak, topMean5)

            def topMean7 = "N/A"
            topMean7 = statsIntPower.getMean()
            if (topMean7 && topMean7.isNaN()) {
                topMean7 = "N/A"
            } else {
                def r = statsIntPower.getMax() - statsIntPower.getMin()
                bdo.put("Int Power (W) R of top" + top + grp + cc + leak, r)
            }
            bdo.put("Int Power (W) avg of top" + top + grp + cc + leak, topMean7)

            def topMean9 = "N/A"
            topMean9 = statsPhotometricPower.getMean()
            if (topMean9 && topMean9.isNaN()) {
                topMean9 = "N/A"
            } else {
                def r = statsPhotometricPower.getMax() - statsPhotometricPower.getMin()
                bdo.put("Photometric Power R of top" + top + grp + cc + leak, r)
            }
            bdo.put("Photometric Power avg of top" + top + grp + cc + leak, topMean9)

            def topMean6 = "N/A"
            topMean6 = statsVf.getMean()
            if (topMean6 && topMean6.isNaN()) {
                topMean6 = "N/A"
            } else {
                def r = statsVf.getMax() - statsVf.getMin()
                bdo.put("VF R of top" + top + grp + cc + leak, r)
            }
            bdo.put("VF@" + cc + " @ top" + top + grp + cc + leak, topMean6)


            def topStd1 = "N/A"
            topStd1 = statsEqe.getStandardDeviation()
            if (topStd1 && topStd1.isNaN()) {
                topStd1 = "N/A"
            }
            bdo.put("EQE stdDev of top" + top + grp + cc + leak, topStd1)

            def topStd2 = "N/A"
            topStd2 = statsPeak.getStandardDeviation()
            if (topStd2 && topStd2.isNaN()) {
                topStd2 = "N/A"
            }
            bdo.put("Peak (nm) stdDev of top" + top + grp + cc + leak, topStd2)

            def topStd3 = "N/A"
            topStd3 = statsCentroid.getStandardDeviation()
            if (topStd3 && topStd3.isNaN()) {
                topStd3 = "N/A"
            }
            bdo.put("Centroid (nm) stdDev of top" + top + grp + cc + leak, topStd3)

            def topStd4 = "N/A"
            topStd4 = statsFwhm.getStandardDeviation()
            if (topStd4 && topStd4.isNaN()) {
                topStd4 = "N/A"
            }
            bdo.put("FWHM (nm) stdDev of top" + top + grp + cc + leak, topStd4)

            def topStd5 = "N/A"
            topStd5 = statsDominant.getStandardDeviation()
            if (topStd5 && topStd5.isNaN()) {
                topStd5 = "N/A"
            }
            bdo.put("Dominant (nm) stdDev of top" + top + grp + cc + leak, topStd5)

            def topStd6 = "N/A"
            topStd6 = statsVf.getStandardDeviation()
            if (topStd6 && topStd6.isNaN()) {
                topStd6 = "N/A"
            }
            bdo.put("VF@" + cc + " @ stdDev of top" + top + grp + cc + leak, topStd6)

            def topStd7 = "N/A"
            topStd7 = statsIntPower.getStandardDeviation()
            if (topStd7 && topStd7.isNaN()) {
                topStd7 = "N/A"
            }
            bdo.put("Int Power (W) stdDev of top" + top + grp + cc + leak, topStd7)

            def topStd9 = "N/A"
            topStd9 = statsPhotometricPower.getStandardDeviation()
            if (topStd9 && topStd9.isNaN()) {
                topStd9 = "N/A"
            }
            bdo.put("Photometric Power stdDev of top" + top + grp + cc + leak, topStd9)

            DescriptiveStatistics if1 = new DescriptiveStatistics()
            DescriptiveStatistics if2 = new DescriptiveStatistics()
            DescriptiveStatistics if3 = new DescriptiveStatistics()
            DescriptiveStatistics if4 = new DescriptiveStatistics()

            arr.each { devId ->

                def query = new BasicDBObject()
                query.put("value.code", code + "_" + devId)
                query.put("value.testId", testId)
                db.testData.find(query, new BasicDBObject()).collect { item ->

                    if (item && item["value"] &&
                            item["value"]["data"] &&
                            item["value"]["data"]["Datavoltage"] &&
                            item["value"]["data"]["Datavoltage"]["data"]) {

                        def tdata = item["value"]["data"]["Datavoltage"]["data"]
                        if (tdata) {

                            def tdataSorted = tdata.sort { it[0] }.unique { it[0] }

                            List xs = tdataSorted.collect { it[0] }
                            List ys = tdataSorted.collect { it[1] }

                            if1.addValue(interpolate(1.0, xs, ys))
                            if2.addValue(interpolate(2.0, xs, ys))
                            if3.addValue(interpolate(3.0, xs, ys))
                            if4.addValue(interpolate(4.0, xs, ys))
                        }
                    }
                }
            }

            if (if1.getValues().size() > 0) {
                def dd1 = if1.getMean()
                if (dd1 < 10000)
                    bdo.put("IF@1V @ top" + top + " WPE " + cc, dd1)
            }

            if (if2.getValues().size() > 0) {
                def dd2 = if2.getMean()
                if (dd2 < 10000)
                    bdo.put("IF@2V @ top" + top + " WPE " + cc, dd2)
            }

            if (if3.getValues().size() > 0) {
                def dd3 = if3.getMean()
                if (dd3 < 10000)
                    bdo.put("IF@3V @ top" + top + " WPE " + cc, dd3)
            }

            if (if4.getValues().size() > 0) {

                def dd4 = if4.getMean()
                if (dd4 < 10000)
                    bdo.put("IF@4V @ top" + top + " WPE " + cc, dd4)
            }
        } else {

            bdo.put("EQE avg of top" + top + " WPE " + cc, "N/A")
            bdo.put("Peak (nm) avg of top" + top + " WPE " + cc, "N/A")
            bdo.put("Centroid (nm) avg of top" + top + " WPE " + cc, "N/A")
            bdo.put("FWHM (nm) avg of top" + top + " WPE " + cc, "N/A")
            bdo.put("Dominant (nm) avg of top" + top + " WPE " + cc, "N/A")

            bdo.put("IF@1V @ top" + top + " WPE " + cc, "N/A")
            bdo.put("IF@2V @ top" + top + " WPE " + cc, "N/A")
            bdo.put("IF@3V @ top" + top + " WPE " + cc, "N/A")
            bdo.put("IF@4V @ top" + top + " WPE " + cc, "N/A")

            bdo.put("VF@1mA @ top" + top + " WPE " + cc, "N/A")
            bdo.put("VF@5mA @ top" + top + " WPE " + cc, "N/A")
            bdo.put("VF@10mA @ top" + top + " WPE " + cc, "N/A")
            bdo.put("VF@20mA @ top" + top + " WPE " + cc, "N/A")

        }

    }

    def interpolate(def x, def xs, def ys) {

        int sz = xs.size()

        if (sz >= 2) {

            def _x = [];
            def _y = [];

            LinearInterpolator interpol = new LinearInterpolator()

            for (int i = 1; i < sz; i++) {
                if (xs[i-1] < xs[i]) {
                    _x.add(xs[i-1])
                    _y.add(ys[i-1])
                } else {
                    break
                }
                if (i + 1 == sz) {
                   _x.add(xs[i])
                   _y.add(ys[i])
                }
            }

            try {
                def func = interpol.interpolate(_x as double[], _y as double[])
                def vl = func.value(x)

                if (vl.isNaN())
                    return 0
                else
                    return vl
            } catch (org.apache.commons.math3.exception.NonMonotonicSequenceException exc) {
                logr.warn(exc)
                return 0
            } catch (Exception exc) {
                logr.warn(exc)
                return 0
            }

        } else {
            return 0
        }
    }

    def densityAsString(density) {
        return density/1000 + 'mAcm2'
    }
}
