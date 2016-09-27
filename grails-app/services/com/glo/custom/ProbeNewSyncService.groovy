package com.glo.custom

import com.mongodb.BasicDBObject
import org.apache.commons.logging.LogFactory
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics
import org.apache.commons.math3.stat.regression.SimpleRegression

class ProbeNewSyncService {

    private static final logr = LogFactory.getLog(this)

    def unitService
    def fileService
    def readFileService
    def summarizeSyncService
    def mongo
    def grailsApplication
    def transactional = true

    static float deviceArray = 36057.0 / 100000000.0


    def addProbeData(units, tkey) {

        def db = mongo.getDB("glo")

        !logr.isDebugEnabled() ?: logr.debug("probe test sync job started.")

        // Create list of unique files grouped by wafer id
        def unitData = [:]
        def dt = null
        String dir = grailsApplication.config.glo.probeNewTestDirectory

        units.keySet().each {

            def res = db.measures.aggregate(
                    [
                        $match: [
                                WaferID: it.code
                        ]
                    ], [
                        $sort: [
                                TimeRun: 1,
                                DeviceID: 1,
                                CurrentSet: 1
                        ]
                    ], [
                        $group: [
                            _id : [
                                wid: '$WaferID',
                                did: '$DeviceID'
                            ],
                            data: [
                                $push: [
                                    spectrum: '$wavelengthPowerScan.data',
                                    viswp   : '$VISwp.data',
                                    virpp   : '$VIRPPSwp.data',
                                    current : '$CurrentSet',
                                    volt    : '$Volt',
                                    eqe     : '$eqe',
                                    wpe     : '$wpe',
                                    peak    : '$RawPeakWavelength',
                                    dominant: '$dominantWavelength',
                                    photometric: '$photometric',
                                    radiometric: '$radiometric',
                                    fwhm    : '$FWHM',
                                    efficacy: '$efficacy',
                                    sid: '$sid'
                                ]
                            ],
                            lastSid: [
                                $last: '$sid'
                            ]
                        ]
                    ], [
                        $project: [
                            _id: '$_id',
                            data: [
                                $filter: [
                                    input: '$data',
                                    as: 'itm',
                                    cond: [
                                        $eq: ['$$itm.sid', '$lastSid']
                                    ]
                                ]
                            ]
                        ]
                    ]).results()

            unitData.put(it.code, res)

            def cntrl = ""
            def cntrlCurrentSweep = ""
            def cntrlVoltageSweep = ""
            def cntrlRppVoltageSweep = ""

            def isAdded = [:]

            // Current_Sweep
            // Voltage sweep
            // Rpp voltage sweep
            // Spectrums

        }

        def fileArray = []
        unitData.each { code, value ->

            def query = new BasicDBObject("code", code)
            def unit = db.unit.find(query, new BasicDBObject()).collect { it }[0]
            if (unit && value) {

                // Initialize unit object to get updated with new values
                def bdoUnit = new BasicDBObject()

                DescriptiveStatistics statsVbp = new DescriptiveStatistics()
                DescriptiveStatistics statsRpp = new DescriptiveStatistics()

                DescriptiveStatistics avgWpes = new DescriptiveStatistics()
                DescriptiveStatistics avgEqes = new DescriptiveStatistics()
                DescriptiveStatistics avgJWpes = new DescriptiveStatistics()
                DescriptiveStatistics avgJEqes = new DescriptiveStatistics()

                DescriptiveStatistics avgWpe1 = new DescriptiveStatistics()
                DescriptiveStatistics avgEqe1 = new DescriptiveStatistics()
                DescriptiveStatistics avgV1 = new DescriptiveStatistics()
                DescriptiveStatistics avgPeak1 = new DescriptiveStatistics()
                DescriptiveStatistics avgFwhm1 = new DescriptiveStatistics()
                DescriptiveStatistics avgWpe4 = new DescriptiveStatistics()
                DescriptiveStatistics avgEqe4 = new DescriptiveStatistics()
                DescriptiveStatistics avgV4 = new DescriptiveStatistics()
                DescriptiveStatistics avgPeak4 = new DescriptiveStatistics()
                DescriptiveStatistics avgFwhm4 = new DescriptiveStatistics()
                DescriptiveStatistics avgWpe5 = new DescriptiveStatistics()
                DescriptiveStatistics avgEqe5 = new DescriptiveStatistics()
                DescriptiveStatistics avgV5 = new DescriptiveStatistics()
                DescriptiveStatistics avgPeak5 = new DescriptiveStatistics()
                DescriptiveStatistics avgFwhm5 = new DescriptiveStatistics()
                DescriptiveStatistics avgWpe10 = new DescriptiveStatistics()
                DescriptiveStatistics avgEqe10 = new DescriptiveStatistics()
                DescriptiveStatistics avgV10 = new DescriptiveStatistics()
                DescriptiveStatistics avgPeak10 = new DescriptiveStatistics()
                DescriptiveStatistics avgFwhm10 = new DescriptiveStatistics()
                DescriptiveStatistics avgWpe20 = new DescriptiveStatistics()
                DescriptiveStatistics avgEqe20 = new DescriptiveStatistics()
                DescriptiveStatistics avgV20 = new DescriptiveStatistics()
                DescriptiveStatistics avgPeak20 = new DescriptiveStatistics()
                DescriptiveStatistics avgFwhm20 = new DescriptiveStatistics()
                DescriptiveStatistics avgWpe50 = new DescriptiveStatistics()
                DescriptiveStatistics avgEqe50 = new DescriptiveStatistics()
                DescriptiveStatistics avgV50 = new DescriptiveStatistics()
                DescriptiveStatistics avgPeak50 = new DescriptiveStatistics()
                DescriptiveStatistics avgFwhm50 = new DescriptiveStatistics()

                DescriptiveStatistics avgWpe01 = new DescriptiveStatistics()
                DescriptiveStatistics avgEqe01 = new DescriptiveStatistics()
                DescriptiveStatistics avgV01 = new DescriptiveStatistics()
                DescriptiveStatistics avgPeak01 = new DescriptiveStatistics()
                DescriptiveStatistics avgFwhm01 = new DescriptiveStatistics()

                DescriptiveStatistics avgWpe02 = new DescriptiveStatistics()
                DescriptiveStatistics avgEqe02 = new DescriptiveStatistics()
                DescriptiveStatistics avgV02 = new DescriptiveStatistics()
                DescriptiveStatistics avgPeak02 = new DescriptiveStatistics()
                DescriptiveStatistics avgFwhm02 = new DescriptiveStatistics()

                DescriptiveStatistics avgWpe04 = new DescriptiveStatistics()
                DescriptiveStatistics avgEqe04 = new DescriptiveStatistics()
                DescriptiveStatistics avgV04 = new DescriptiveStatistics()
                DescriptiveStatistics avgPeak04 = new DescriptiveStatistics()
                DescriptiveStatistics avgFwhm04 = new DescriptiveStatistics()

                DescriptiveStatistics avgWpe06 = new DescriptiveStatistics()
                DescriptiveStatistics avgEqe06 = new DescriptiveStatistics()
                DescriptiveStatistics avgV06 = new DescriptiveStatistics()
                DescriptiveStatistics avgPeak06 = new DescriptiveStatistics()
                DescriptiveStatistics avgFwhm06 = new DescriptiveStatistics()

                DescriptiveStatistics avgWpe08 = new DescriptiveStatistics()
                DescriptiveStatistics avgEqe08 = new DescriptiveStatistics()
                DescriptiveStatistics avgV08 = new DescriptiveStatistics()
                DescriptiveStatistics avgPeak08 = new DescriptiveStatistics()
                DescriptiveStatistics avgFwhm08 = new DescriptiveStatistics()

                DescriptiveStatistics avgCurr10Center = new DescriptiveStatistics()
                DescriptiveStatistics avgCurr10All = new DescriptiveStatistics()
                DescriptiveStatistics avgPeak10Center = new DescriptiveStatistics()
                DescriptiveStatistics avgCurr4Center = new DescriptiveStatistics()
                DescriptiveStatistics avgCurr4All = new DescriptiveStatistics()
                DescriptiveStatistics avgPeak4Center = new DescriptiveStatistics()
                DescriptiveStatistics avgCurr5Center = new DescriptiveStatistics()
                DescriptiveStatistics avgCurr5All = new DescriptiveStatistics()
                DescriptiveStatistics avgPeak5Center = new DescriptiveStatistics()
                DescriptiveStatistics avgCurr20Center = new DescriptiveStatistics()
                DescriptiveStatistics avgCurr20All = new DescriptiveStatistics()
                DescriptiveStatistics avgPeak20Center = new DescriptiveStatistics()
                DescriptiveStatistics avgCurr50Center = new DescriptiveStatistics()
                DescriptiveStatistics avgCurr50All = new DescriptiveStatistics()
                DescriptiveStatistics avgPeak50Center = new DescriptiveStatistics()
                DescriptiveStatistics avgCurr1Center = new DescriptiveStatistics()
                DescriptiveStatistics avgCurr1All = new DescriptiveStatistics()
                DescriptiveStatistics avgPeak1Center = new DescriptiveStatistics()

                DescriptiveStatistics avgCurr01Center = new DescriptiveStatistics()
                DescriptiveStatistics avgCurr01All = new DescriptiveStatistics()
                DescriptiveStatistics avgPeak01Center = new DescriptiveStatistics()

                DescriptiveStatistics avgCurr02Center = new DescriptiveStatistics()
                DescriptiveStatistics avgCurr02All = new DescriptiveStatistics()
                DescriptiveStatistics avgPeak02Center = new DescriptiveStatistics()

                DescriptiveStatistics avgCurr04Center = new DescriptiveStatistics()
                DescriptiveStatistics avgCurr04All = new DescriptiveStatistics()
                DescriptiveStatistics avgPeak04Center = new DescriptiveStatistics()

                DescriptiveStatistics avgCurr06Center = new DescriptiveStatistics()
                DescriptiveStatistics avgCurr06All = new DescriptiveStatistics()
                DescriptiveStatistics avgPeak06Center = new DescriptiveStatistics()

                DescriptiveStatistics avgCurr08Center = new DescriptiveStatistics()
                DescriptiveStatistics avgCurr08All = new DescriptiveStatistics()
                DescriptiveStatistics avgPeak08Center = new DescriptiveStatistics()


                def peaks = [:]
                def peaksCenter = [:]
                def peaks1 = [:]
                def peaksCenter1 = [:]
                def peaks4 = [:]
                def peaksCenter4 = [:]
                def peaks5 = [:]
                def peaksCenter5 = [:]
                def peaks20 = [:]
                def peaksCenter20 = [:]
                def peaks50 = [:]
                def peaksCenter50 = [:]

                def peaks01 = [:]
                def peaksCenter01 = [:]
                def peaks02 = [:]
                def peaksCenter02 = [:]
                def peaks04 = [:]
                def peaksCenter04 = [:]
                def peaks06 = [:]
                def peaksCenter06 = [:]
                def peaks08 = [:]
                def peaksCenter08 = [:]

                def corr = [:]
                def corrCenter = [:]
                def corr1 = [:]
                def corrCenter1 = [:]
                def corr4 = [:]
                def corrCenter4 = [:]
                def corr5 = [:]
                def corrCenter5 = [:]
                def corr20 = [:]
                def corrCenter20 = [:]

                def corr01 = [:]
                def corrCenter01 = [:]
                def corr02 = [:]
                def corrCenter02 = [:]
                def corr04 = [:]
                def corrCenter04 = [:]
                def corr06 = [:]
                def corrCenter06 = [:]
                def corr08 = [:]
                def corrCenter08 = [:]

                File f = new File(dir + code)
                if (f.exists()) {
                    def dirloc = f.listFiles([accept: { file -> file ==~ /.*?\.jpg/ }] as FileFilter)?.toList()
                    dirloc += f.listFiles([accept: { file -> file ==~ /.*?\.png/ }] as FileFilter)?.toList()
                    dirloc.each { img ->
                        fileArray.add([
                                img,
                                code + "_" + img.getName(),
                                unit["_id"]
                        ])
                    }
                }

                value.each { listPerDevice ->

                    def root = new BasicDBObject()
                    def setting = new BasicDBObject()
                    setting.put("default", "")
                    setting.put("propagate", "false")
                    root.put("setting", setting)

                    def rpp = ''
                    def vbp = ''

                    def deviceCode = listPerDevice._id.did
                    def values = listPerDevice.data
                    def currsweep = []

                    values.each {

                        // Sync NiDot rpp data /////////////////////////////////////////////////////////

                        if (it.virpp && it.virpp.size() > 0) {
                            def dataSorted = it.virpp.sort { it[0] }
                            def sz = dataSorted.size()
                            def first = dataSorted[0][0]
                            def last = dataSorted[sz - 1][0]
                            def firstRange = dataSorted[(int) (sz / 10)][0]
                            def lastRange = dataSorted[(int) (sz - sz / 10)][0]
                            SimpleRegression sreg1 = new SimpleRegression()
                            SimpleRegression sreg2 = new SimpleRegression()
                            dataSorted.each {
                                if (it[0] && it[1] && it[1] < 10) {
                                    if (it[0] >= first && it[0] <= firstRange) {
                                        sreg1.addData(it[1] * 1000, it[0])
                                    }
                                    if (it[0] >= lastRange && it[0] <= last) {
                                        sreg2.addData(it[1] * 1000, it[0])
                                    }
                                }
                            }

                            rpp = (sreg1.getSlope() + sreg2.getSlope()) / 2
                            vbp = (Math.abs(sreg1.getIntercept()) + Math.abs(sreg2.getIntercept())) / 2
                            statsVbp.addValue(vbp)
                            statsRpp.addValue(rpp)
                        }

                        // Sync NiDot performance voltage sweep data /////////////////////////////////////////////////////////
                        if (it.viswp) {
                            root.put("Datavoltage", [data: it.viswp])
                        }

                        // Sync NiDot performance spectrum data /////////////////////////////////////////////////////////
                        def spectrum
                        if (it.spectrum) {
                            spectrum = it.spectrum.findAll{
                                it[0] > 400 && it[0] < 750
                            }
                        }

                        Double curr = it.current * 1000,
                               peak = it.peak,
                               fwhm = it.fwhm,
                               peakPower = it.photometric,
                               intPower = it.radiometric

                        currsweep.add([curr, it.volt, it.radiometric, it.eqe])

                        def grpCurr = "Data @ " + curr.round() + "mA"
                        if (curr < 1) {
                            grpCurr = "Data @ " + String.format("%d", (curr * 1000).round()) + "uA"
                        }


                        // Find wavelength where intensity is max
                        if (curr.round() == 5) {
                            DescriptiveStatistics stats = new DescriptiveStatistics((double[]) spectrum.collect {
                                (double) (it[1])
                            })
                            def maxIntensity = stats.getMax()
                            def peakWavelength = 1
                            for (def s in spectrum) {
                                if (s[1] == maxIntensity && s[0] > 0) {
                                    peakWavelength = s[0]
                                    break
                                }
                            }
                            root.put("peakWavelength", peakWavelength)

                            avgPeak5.addValue(peak)
                            peaks5.put(deviceCode, peak)

                            avgFwhm5.addValue(fwhm)
                            if (deviceCode.toUpperCase().substring(0, 4) in ["NE00", "SE00", "NW00", "SW00"]) {
                                avgPeak5Center.addValue(peak)
                                peaksCenter5.put(deviceCode, peak)
                            }
                        }
                        if (curr.round() == 1) {
                            avgPeak1.addValue(peak)
                            peaks1.put(deviceCode, peak)
                            avgFwhm1.addValue(fwhm)

                            if (deviceCode.toUpperCase().substring(0, 4) in ["NE00", "SE00", "NW00", "SW00"]) {
                                avgPeak1Center.addValue(peak)
                                peaksCenter1.put(deviceCode, peak)
                            }
                        }
                        if (curr.round() == 4) {
                            avgPeak4.addValue(peak)
                            peaks4.put(deviceCode, peak)
                            avgFwhm4.addValue(fwhm)

                            if (deviceCode.toUpperCase().substring(0, 4) in ["NE00", "SE00", "NW00", "SW00"]) {
                                avgPeak4Center.addValue(peak)
                                peaksCenter4.put(deviceCode, peak)
                            }
                        }
                        if (curr.round() == 10) {

                            avgPeak10.addValue(peak)
                            peaks.put(deviceCode, peak)

                            avgFwhm10.addValue(fwhm)

                            if (deviceCode.toUpperCase().substring(0, 4) in ["NE00", "SE00", "NW00", "SW00"]) {
                                avgPeak10Center.addValue(peak)
                                peaksCenter.put(deviceCode, peak)
                            }
                        }
                        if (curr.round() == 20) {
                            avgPeak20.addValue(peak)
                            avgFwhm20.addValue(fwhm)
                            peaks20.put(deviceCode, peak)

                            if (deviceCode.toUpperCase().substring(0, 4) in ["NE00", "SE00", "NW00", "SW00"]) {
                                avgPeak20Center.addValue(peak)
                                peaksCenter20.put(deviceCode, peak)
                            }
                        }
                        if (curr.round() == 50) {
                            avgPeak50.addValue(peak)
                            avgFwhm50.addValue(fwhm)
                            peaks50.put(deviceCode, peak)
                            if (deviceCode.toUpperCase().substring(0, 4) in ["NE00", "SE00", "NW00", "SW00"]) {
                                avgPeak50Center.addValue(peak)
                                peaksCenter50.put(deviceCode, peak)
                            }
                        }
                        if (curr == 0.1) {
                            avgPeak01.addValue(peak)
                            avgFwhm01.addValue(fwhm)
                            peaks01.put(deviceCode, peak)
                            if (deviceCode.toUpperCase().substring(0, 4) in ["NE00", "SE00", "NW00", "SW00"]) {
                                avgPeak01Center.addValue(peak)
                                peaksCenter01.put(deviceCode, peak)
                            }
                        }
                        if (curr == 0.2) {
                            avgPeak02.addValue(peak)
                            avgFwhm02.addValue(fwhm)
                            peaks02.put(deviceCode, peak)
                            if (deviceCode.toUpperCase().substring(0, 4) in ["NE00", "SE00", "NW00", "SW00"]) {
                                avgPeak02Center.addValue(peak)
                                peaksCenter02.put(deviceCode, peak)
                            }
                        }
                        if (curr == 0.4) {
                            avgPeak04.addValue(peak)
                            avgFwhm04.addValue(fwhm)
                            peaks04.put(deviceCode, peak)
                            if (deviceCode.toUpperCase().substring(0, 4) in ["NE00", "SE00", "NW00", "SW00"]) {
                                avgPeak04Center.addValue(peak)
                                peaksCenter04.put(deviceCode, peak)
                            }
                        }
                        if (curr == 0.6) {
                            avgPeak06.addValue(peak)
                            avgFwhm06.addValue(fwhm)
                            peaks06.put(deviceCode, peak)
                            if (deviceCode.toUpperCase().substring(0, 4) in ["NE00", "SE00", "NW00", "SW00"]) {
                                avgPeak06Center.addValue(peak)
                                peaksCenter06.put(deviceCode, peak)
                            }
                        }
                        if (curr == 0.8) {
                            avgPeak08.addValue(peak)
                            avgFwhm08.addValue(fwhm)
                            peaks08.put(deviceCode, peak)
                            if (deviceCode.toUpperCase().substring(0, 4) in ["NE00", "SE00", "NW00", "SW00"]) {
                                avgPeak08Center.addValue(peak)
                                peaksCenter08.put(deviceCode, peak)
                            }
                        }

                        // Store spectrum related data
                        root.put(grpCurr, new BasicDBObject())
                        root[grpCurr].put("Peak (nm)", peak)
                        root[grpCurr].put("FWHM", fwhm)
                        root[grpCurr].put("Peak Power (W/nm)", peakPower)
                        root[grpCurr].put("Int Power (W)", intPower)
                        root[grpCurr].put("Spectrum", [
                                "first" : "Wavelength (nm)",
                                "second" : "Intensity",
                                data: spectrum])
                    }

                    root.put("Datacurrent",  [data: currsweep])

                    // Insert test data
                    if (root.size() > 1) {

                        def queryTest = new BasicDBObject("value.code", code + "_" + deviceCode)
                        queryTest.put("value.tkey", tkey)
                        def testData = db.testData.find(queryTest, new BasicDBObject()).collect { it }[0]
                        def tValue = new BasicDBObject()
                        if (!testData) {
                            testData = new BasicDBObject()
                        }
                        tValue.put("code", code + "_" + deviceCode)
                        tValue.put("experimentId", "")
                        tValue.put("parentCode", code)
                        tValue.put("tkey", tkey)
                        tValue.put("date", new Date())

                        if (rpp) tValue.put("rpp", rpp)
                        if (vbp) tValue.put("vbp", vbp)

                        def leakage = calculatePerformance(deviceCode, root)

                        // Calculate summary data
                        def currents = root["Datacurrent"]
                        if (currents) {

                            def ddd = currents.data.sort { it[0] }

                            List currs = ddd.collect { (double) (it[0] ?: 0) }
                            List currsw = ddd.collect { (double) (it[0] ?: 0) }
                            List currse = ddd.collect { (double) (it[0] ?: 0) }
                            List volts = ddd.collect { (double) (it[1] ?: 0) }
                            List wpes = ddd.collect { (double) (it[6] ?: 0) }
                            List eqes = ddd.collect { (double) (it[8] ?: 0) }

                            DescriptiveStatistics statsWpe = new DescriptiveStatistics((double[]) currents.data.collect {
                                (double) (it[6])
                            })
                            DescriptiveStatistics statsEqe = new DescriptiveStatistics((double[]) currents.data.collect {
                                (double) (it[8])
                            })

                            def maxWpe = statsWpe.getMax()
                            def maxEqe = statsEqe.getMax()
                            def jWpe = ''
                            def jEqe = ''

                            for (def s in currents.data) {
                                if (s[6] == maxWpe) {
                                    jWpe = s[10]
                                }
                                if (s[8] == maxEqe) {
                                    jEqe = s[10]
                                }
                            }

                            ddd.each { currrow ->

                                if (!tValue.containsKey("wpe01") && currrow[0] > 0.08 && currrow[0] < 0.12) {
                                    tValue.put("v01", currrow[1])
                                    avgV01.addValue(currrow[1])
                                    tValue.put("wpe01", currrow[6])
                                    avgWpe01.addValue(currrow[6])
                                    tValue.put("eqe01", currrow[8])
                                    avgEqe01.addValue(currrow[8])
                                }

                                if (!tValue.containsKey("wpe02") && currrow[0] > 0.18 && currrow[0] < 0.22) {
                                    tValue.put("v02", currrow[1])
                                    avgV02.addValue(currrow[1])
                                    tValue.put("wpe02", currrow[6])
                                    avgWpe02.addValue(currrow[6])
                                    tValue.put("eqe02", currrow[8])
                                    avgEqe02.addValue(currrow[8])
                                }

                                if (!tValue.containsKey("wpe04") && currrow[0] > 0.38 && currrow[0] < 0.42) {
                                    tValue.put("v04", currrow[1])
                                    avgV04.addValue(currrow[1])
                                    tValue.put("wpe04", currrow[6])
                                    avgWpe04.addValue(currrow[6])
                                    tValue.put("eqe04", currrow[8])
                                    avgEqe04.addValue(currrow[8])
                                }

                                if (!tValue.containsKey("wpe06") && currrow[0] > 0.58 && currrow[0] < 0.62) {
                                    tValue.put("v06", currrow[1])
                                    avgV06.addValue(currrow[1])
                                    tValue.put("wpe06", currrow[6])
                                    avgWpe06.addValue(currrow[6])
                                    tValue.put("eqe06", currrow[8])
                                    avgEqe06.addValue(currrow[8])
                                }

                                if (!tValue.containsKey("wpe08") && currrow[0] > 0.78 && currrow[0] < 0.82) {
                                    tValue.put("v08", currrow[1])
                                    avgV08.addValue(currrow[1])
                                    tValue.put("wpe08", currrow[6])
                                    avgWpe08.addValue(currrow[6])
                                    tValue.put("eqe08", currrow[8])
                                    avgEqe08.addValue(currrow[8])
                                }
                            }

                            if ((currs).size() > 0) {
                                def topCurr = currs[currs.size() - 1]

                                if (!tValue.containsKey("wpe1") && (topCurr > 1.0 || 1.0 - topCurr < 0.2)) {
                                    def v = summarizeSyncService.interpolate(1.0, currs, volts)
                                    tValue.put("v1", v)
                                    avgV1.addValue(v)
                                    def wps = summarizeSyncService.interpolate(1.0, currsw, wpes)
                                    tValue.put("wpe1", wps)
                                    avgWpe1.addValue(wps)
                                    def eqs = summarizeSyncService.interpolate(1.0, currse, eqes)
                                    tValue.put("eqe1", eqs)
                                    avgEqe1.addValue(eqs)
                                }

                                if (!tValue.containsKey("wpe4") && (topCurr > 4 || 4 - topCurr < 0.3)) {
                                    def v = summarizeSyncService.interpolate(4, currs, volts)
                                    tValue.put("v4", v)
                                    avgV4.addValue(v)
                                    def wps = summarizeSyncService.interpolate(4, currsw, wpes)
                                    tValue.put("wpe4", wps)
                                    avgWpe4.addValue(wps)
                                    def eqs = summarizeSyncService.interpolate(4, currse, eqes)
                                    tValue.put("eqe4", eqs)
                                    avgEqe4.addValue(eqs)
                                }

                                if (!tValue.containsKey("wpe5") && (topCurr > 5.0 || 5.0 - topCurr < 0.3)) {
                                    def v = summarizeSyncService.interpolate(5.0, currs, volts)
                                    tValue.put("v5", v)
                                    avgV5.addValue(v)
                                    def wps = summarizeSyncService.interpolate(5.0, currsw, wpes)
                                    tValue.put("wpe5", wps)
                                    avgWpe5.addValue(wps)
                                    def eqs = summarizeSyncService.interpolate(5.0, currse, eqes)
                                    tValue.put("eqe5", eqs)
                                    avgEqe5.addValue(eqs)
                                }

                                if (!tValue.containsKey("wpe10") && (topCurr > 10.0 || 10.0 - topCurr < 0.9)) {
                                    def v = summarizeSyncService.interpolate(10.0, currs, volts)
                                    tValue.put("v10", v)
                                    avgV10.addValue(v)
                                    def wps = summarizeSyncService.interpolate(10.0, currsw, wpes)
                                    tValue.put("wpe10", wps)
                                    avgWpe10.addValue(wps)
                                    def eqs = summarizeSyncService.interpolate(10.0, currse, eqes)
                                    tValue.put("eqe10", eqs)
                                    avgEqe10.addValue(eqs)
                                }

                                if (!tValue.containsKey("wpe20") && (topCurr > 20.0 || 20.0 - topCurr < 1.9)) {
                                    def v = summarizeSyncService.interpolate(20.0, currs, volts)
                                    tValue.put("v20", v)
                                    avgV20.addValue(v)
                                    def wps = summarizeSyncService.interpolate(20.0, currsw, wpes)
                                    tValue.put("wpe20", wps)
                                    avgWpe20.addValue(wps)
                                    def eqs = summarizeSyncService.interpolate(20.0, currse, eqes)
                                    tValue.put("eqe20", eqs)
                                    avgEqe20.addValue(eqs)
                                }

                                if (!tValue.containsKey("wpe50") && (topCurr > 50.0 || 50.0 - topCurr < 2)) {
                                    def v = summarizeSyncService.interpolate(50.0, currs, volts)
                                    tValue.put("v50", v)
                                    avgV50.addValue(v)
                                    def wps = summarizeSyncService.interpolate(50.0, currsw, wpes)
                                    tValue.put("wpe50", wps)
                                    avgWpe50.addValue(wps)
                                    def eqs = summarizeSyncService.interpolate(50.0, currse, eqes)
                                    tValue.put("eqe50", eqs)
                                    avgEqe50.addValue(eqs)
                                }
                            }


                            tValue.put("Peak WPE (%)", maxWpe)
                            tValue.put("Peak EQE (%)", maxEqe)
                            tValue.put("J @ Peak WPE (A/cm2)", jWpe)
                            tValue.put("J @ Peak EQE (A/cm2)", jEqe)

                            leakage.each { k, v ->

                                if (k == 20) {

                                    if (deviceCode.toUpperCase().substring(0, 4) in ["NE00", "SE00", "NW00", "SW00"]) {
                                        avgCurr20Center.addValue(v)
                                        corrCenter20.put(v, deviceCode)
                                    }

                                    avgCurr20All.addValue(v)
                                    corr20.put(v, deviceCode)
                                }

                                if (k == 10) {

                                    if (deviceCode.toUpperCase().substring(0, 4) in ["NE00", "SE00", "NW00", "SW00"]) {
                                        avgCurr10Center.addValue(v)
                                        corrCenter.put(v, deviceCode)
                                    }

                                    avgCurr10All.addValue(v)
                                    corr.put(v, deviceCode)
                                }

                                if (k == 4) {

                                    if (deviceCode.toUpperCase().substring(0, 4) in ["NE00", "SE00", "NW00", "SW00"]) {
                                        avgCurr4Center.addValue(v)
                                        corrCenter4.put(v, deviceCode)
                                    }

                                    avgCurr4All.addValue(v)
                                    corr4.put(v, deviceCode)
                                }

                                if (k == 5) {

                                    if (deviceCode.toUpperCase().substring(0, 4) in ["NE00", "SE00", "NW00", "SW00"]) {
                                        avgCurr5Center.addValue(v)
                                        corrCenter5.put(v, deviceCode)
                                    }

                                    avgCurr5All.addValue(v)
                                    corr5.put(v, deviceCode)
                                }

                                if (k == 1) {

                                    if (deviceCode.toUpperCase().substring(0, 4) in ["NE00", "SE00", "NW00", "SW00"]) {
                                        avgCurr1Center.addValue(v)
                                        corrCenter1.put(v, deviceCode)
                                    }

                                    avgCurr1All.addValue(v)
                                    corr1.put(v, deviceCode)
                                }

                                if (k == 0.1) {

                                    if (deviceCode.toUpperCase().substring(0, 4) in ["NE00", "SE00", "NW00", "SW00"]) {
                                        avgCurr01Center.addValue(v)
                                        corrCenter01.put(v, deviceCode)
                                    }
                                    avgCurr01All.addValue(v)
                                    corr01.put(v, deviceCode)
                                    k = '01'
                                }

                                if (k == 0.2) {

                                    if (deviceCode.toUpperCase().substring(0, 4) in ["NE00", "SE00", "NW00", "SW00"]) {
                                        avgCurr02Center.addValue(v)
                                        corrCenter02.put(v, deviceCode)
                                    }
                                    avgCurr02All.addValue(v)
                                    corr02.put(v, deviceCode)
                                    k = '02'
                                }

                                if (k == 0.4) {

                                    if (deviceCode.toUpperCase().substring(0, 4) in ["NE00", "SE00", "NW00", "SW00"]) {
                                        avgCurr04Center.addValue(v)
                                        corrCenter04.put(v, deviceCode)
                                    }
                                    avgCurr04All.addValue(v)
                                    corr04.put(v, deviceCode)
                                    k = '04'
                                }

                                if (k == 0.6) {

                                    if (deviceCode.toUpperCase().substring(0, 4) in ["NE00", "SE00", "NW00", "SW00"]) {
                                        avgCurr06Center.addValue(v)
                                        corrCenter06.put(v, deviceCode)
                                    }
                                    avgCurr06All.addValue(v)
                                    corr06.put(v, deviceCode)
                                    k = '06'
                                }

                                if (k == 0.8) {

                                    if (deviceCode.toUpperCase().substring(0, 4) in ["NE00", "SE00", "NW00", "SW00"]) {
                                        avgCurr08Center.addValue(v)
                                        corrCenter08.put(v, deviceCode)
                                    }
                                    avgCurr08All.addValue(v)
                                    corr08.put(v, deviceCode)
                                    k = '08'
                                }

                                tValue.put("EQE leak WL corr @ " + k + " mA", v)
                            }

                            maxWpe?.toString().isNumber() ? avgWpes.addValue(maxWpe) : ''
                            maxEqe?.toString().isNumber() ? avgEqes.addValue(maxEqe) : ''
                            jWpe?.toString().isNumber() ? avgJWpes.addValue(jWpe) : ''
                            jEqe?.toString().isNumber() ? avgJEqes.addValue(jEqe) : ''
                        }

                        tValue.put("data", root)
                        testData.put("value", tValue)
                        db.testData.save(testData)
                        testData = null
                    }

                }

                // Calculate averages for unit
                def avg = avgWpes.getMean()
                if (!avg.isNaN())
                    bdoUnit.put("PeakWPE", avg)
                avg = avgEqes.getMean()
                if (!avg.isNaN())
                    bdoUnit.put("PeakEQE", avg)
                avg = avgJWpes.getMean()
                if (!avg.isNaN())
                    bdoUnit.put("JPeakWPE", avg)
                avg = avgJEqes.getMean()
                if (!avg.isNaN())
                    bdoUnit.put("JPeakEQE", avg)

                // Corr current or peak center
                avg = avgCurr20Center.getMean()
                if (!avg.isNaN())
                    bdoUnit.put("CorrEQE-20mA-Centepp-Avg", avg)
                avg = avgCurr20Center.getMax()
                if (!avg.isNaN()) {
                    bdoUnit.put("CorrEQE-20mA-Center-Best", avg)
                    bdoUnit.put("Peak-20mA-Center-Best", peaksCenter20[corrCenter20[avg]])
                }
                avg = avgCurr20All.getMax()
                if (!avg.isNaN()) {
                    bdoUnit.put("CorrEQE-20mA-Best", avg)
                    bdoUnit.put("Peak-20mA-Best", peaks20[corr20[avg]])
                }
                avg = avgPeak20Center.getMean()
                if (!avg.isNaN()) {
                    bdoUnit.put("Peak-20mA-Center-Avg", avg)
                }

                avg = avgCurr10Center.getMean()
                if (!avg.isNaN())
                    bdoUnit.put("CorrEQE-10mA-Center-Avg", avg)
                avg = avgCurr10Center.getMax()
                if (!avg.isNaN()) {
                    bdoUnit.put("CorrEQE-10mA-Center-Best", avg)
                    bdoUnit.put("Peak-10mA-Center-Best", peaksCenter[corrCenter[avg]])
                }
                avg = avgCurr10All.getMax()
                if (!avg.isNaN()) {
                    bdoUnit.put("CorrEQE-10mA-Best", avg)
                    bdoUnit.put("Peak-10mA-Best", peaks[corr[avg]])
                }
                avg = avgPeak10Center.getMean()
                if (!avg.isNaN()) {
                    bdoUnit.put("Peak-10mA-Center-Avg", avg)
                }
                avg = avgCurr4Center.getMean()
                if (!avg.isNaN())
                    bdoUnit.put("CorrEQE-4mA-Center-Avg", avg)
                avg = avgCurr4Center.getMax()
                if (!avg.isNaN()) {
                    bdoUnit.put("CorrEQE-4mA-Center-Best", avg)
                    bdoUnit.put("Peak-4mA-Center-Best", peaksCenter4[corrCenter4[avg]])
                    //            bdoUnit.put("Peak-4mA-Center-Best", 'N/A')
                }
                avg = avgCurr4All.getMax()
                if (!avg.isNaN()) {
                    bdoUnit.put("CorrEQE-4mA-Best", avg)
                    bdoUnit.put("Peak-4mA-Best", peaks4[corr4[avg]])
                    //             bdoUnit.put("Peak-4mA-Best",  'N/A')
                }
                avg = avgPeak4Center.getMean()
                if (!avg.isNaN()) {
                    bdoUnit.put("Peak-4mA-Center-Avg", avg)
                    //            bdoUnit.put("Peak-4mA-Center-Avg", 'N/A')
                }

                avg = avgCurr5Center.getMean()
                if (!avg.isNaN())
                    bdoUnit.put("CorrEQE-5mA-Center-Avg", avg)
                avg = avgCurr5Center.getMax()
                if (!avg.isNaN()) {
                    bdoUnit.put("CorrEQE-5mA-Center-Best", avg)
                    bdoUnit.put("Peak-5mA-Center-Best", peaksCenter5[corrCenter5[avg]])
                }
                avg = avgCurr5All.getMax()
                if (!avg.isNaN()) {
                    bdoUnit.put("CorrEQE-5mA-Best", avg)
                    bdoUnit.put("Peak-5mA-Best", peaks5[corr5[avg]])
                }
                avg = avgPeak5Center.getMean()
                if (!avg.isNaN()) {
                    bdoUnit.put("Peak-5mA-Center-Avg", avg)
                }

                avg = avgCurr1Center.getMean()
                if (!avg.isNaN())
                    bdoUnit.put("CorrEQE-1mA-Center-Avg", avg)
                avg = avgCurr1Center.getMax()
                if (!avg.isNaN()) {
                    bdoUnit.put("CorrEQE-1mA-Center-Best", avg)
                    bdoUnit.put("Peak-1mA-Center-Best", peaksCenter1[corrCenter1[avg]])
                }
                avg = avgCurr1All.getMax()
                if (!avg.isNaN()) {
                    bdoUnit.put("CorrEQE-1mA-Best", avg)
                    bdoUnit.put("Peak-1mA-Best", peaks1[corr1[avg]])
                }
                avg = avgPeak1Center.getMean()
                if (!avg.isNaN()) {
                    bdoUnit.put("Peak-1mA-Center-Avg", avg)
                }

                avg = avgCurr01Center.getMean()
                if (!avg.isNaN())
                    bdoUnit.put("CorrEQE-100uA-Center-Avg", avg)
                avg = avgCurr01Center.getMax()
                if (!avg.isNaN()) {
                    bdoUnit.put("CorrEQE-100uA-Center-Best", avg)
                    bdoUnit.put("Peak-100uA-Center-Best", peaksCenter01[corrCenter01[avg]])
                }
                avg = avgCurr01All.getMax()
                if (!avg.isNaN()) {
                    bdoUnit.put("CorrEQE-100uA-Best", avg)
                    bdoUnit.put("Peak-100uA-Best", peaks01[corr01[avg]])
                }
                avg = avgPeak01Center.getMean()
                if (!avg.isNaN()) {
                    bdoUnit.put("Peak-100uA-Center-Avg", avg)
                }

                avg = avgCurr02Center.getMean()
                if (!avg.isNaN())
                    bdoUnit.put("CorrEQE-200uA-Center-Avg", avg)
                avg = avgCurr02Center.getMax()
                if (!avg.isNaN()) {
                    bdoUnit.put("CorrEQE-200uA-Center-Best", avg)
                    bdoUnit.put("Peak-200uA-Center-Best", peaksCenter02[corrCenter02[avg]])
                }
                avg = avgCurr02All.getMax()
                if (!avg.isNaN()) {
                    bdoUnit.put("CorrEQE-200uA-Best", avg)
                    bdoUnit.put("Peak-200uA-Best", peaks02[corr02[avg]])
                }
                avg = avgPeak02Center.getMean()
                if (!avg.isNaN()) {
                    bdoUnit.put("Peak-200uA-Center-Avg", avg)
                }

                avg = avgCurr04Center.getMean()
                if (!avg.isNaN())
                    bdoUnit.put("CorrEQE-400uA-Center-Avg", avg)
                avg = avgCurr04Center.getMax()
                if (!avg.isNaN()) {
                    bdoUnit.put("CorrEQE-400uA-Center-Best", avg)
                    bdoUnit.put("Peak-400uA-Center-Best", peaksCenter04[corrCenter04[avg]])
                }
                avg = avgCurr04All.getMax()
                if (!avg.isNaN()) {
                    bdoUnit.put("CorrEQE-400uA-Best", avg)
                    bdoUnit.put("Peak-400uA-Best", peaks04[corr04[avg]])
                }
                avg = avgPeak04Center.getMean()
                if (!avg.isNaN()) {
                    bdoUnit.put("Peak-400uA-Center-Avg", avg)
                }

                avg = avgCurr06Center.getMean()
                if (!avg.isNaN())
                    bdoUnit.put("CorrEQE-600uA-Center-Avg", avg)
                avg = avgCurr06Center.getMax()
                if (!avg.isNaN()) {
                    bdoUnit.put("CorrEQE-600uA-Center-Best", avg)
                    bdoUnit.put("Peak-600uA-Center-Best", peaksCenter06[corrCenter06[avg]])
                }
                avg = avgCurr06All.getMax()
                if (!avg.isNaN()) {
                    bdoUnit.put("CorrEQE-600uA-Best", avg)
                    bdoUnit.put("Peak-600uA-Best", peaks06[corr06[avg]])
                }
                avg = avgPeak06Center.getMean()
                if (!avg.isNaN()) {
                    bdoUnit.put("Peak-600uA-Center-Avg", avg)
                }

                avg = avgCurr08Center.getMean()
                if (!avg.isNaN())
                    bdoUnit.put("CorrEQE-800uA-Center-Avg", avg)
                avg = avgCurr08Center.getMax()
                if (!avg.isNaN()) {
                    bdoUnit.put("CorrEQE-800uA-Center-Best", avg)
                    bdoUnit.put("Peak-800uA-Center-Best", peaksCenter08[corrCenter08[avg]])
                }
                avg = avgCurr08All.getMax()
                if (!avg.isNaN()) {
                    bdoUnit.put("CorrEQE-800uA-Best", avg)
                    bdoUnit.put("Peak-800uA-Best", peaks08[corr08[avg]])
                }
                avg = avgPeak08Center.getMean()
                if (!avg.isNaN()) {
                    bdoUnit.put("Peak-800uA-Center-Avg", avg)
                }

                avg = avgWpe1.getMean()
                if (!avg.isNaN())
                    bdoUnit.put("wpe1", avg)
                avg = avgEqe1.getMean()
                if (!avg.isNaN())
                    bdoUnit.put("eqe1", avg)
                avg = avgV1.getMean()
                if (!avg.isNaN())
                    bdoUnit.put("v1", avg)
                avg = avgPeak1.getMean()
                if (!avg.isNaN())
                    bdoUnit.put("peak1", avg)
                avg = avgFwhm1.getMean()
                if (!avg.isNaN())
                    bdoUnit.put("fwhm1", avg)

                avg = avgWpe4.getMean()
                if (!avg.isNaN())
                    bdoUnit.put("wpe4", avg)
                avg = avgEqe4.getMean()
                if (!avg.isNaN())
                    bdoUnit.put("eqe4", avg)
                avg = avgV4.getMean()
                if (!avg.isNaN())
                    bdoUnit.put("v4", avg)
                avg = avgPeak4.getMean()
                if (!avg.isNaN())
                    bdoUnit.put("peak4", avg)
                avg = avgFwhm4.getMean()
                if (!avg.isNaN())
                    bdoUnit.put("fwhm4", avg)

                avg = avgWpe5.getMean()
                if (!avg.isNaN())
                    bdoUnit.put("wpe5", avg)
                avg = avgEqe5.getMean()
                if (!avg.isNaN())
                    bdoUnit.put("eqe5", avg)
                avg = avgV5.getMean()
                if (!avg.isNaN())
                    bdoUnit.put("v5", avg)
                avg = avgPeak5.getMean()
                if (!avg.isNaN())
                    bdoUnit.put("peak5", avg)
                avg = avgFwhm5.getMean()
                if (!avg.isNaN())
                    bdoUnit.put("fwhm5", avg)

                avg = avgWpe10.getMean()
                if (!avg.isNaN())
                    bdoUnit.put("wpe10", avg)
                avg = avgEqe10.getMean()
                if (!avg.isNaN())
                    bdoUnit.put("eqe10", avg)
                avg = avgV10.getMean()
                if (!avg.isNaN())
                    bdoUnit.put("v10", avg)
                avg = avgPeak10.getMean()
                if (!avg.isNaN())
                    bdoUnit.put("peak10", avg)
                avg = avgFwhm10.getMean()
                if (!avg.isNaN())
                    bdoUnit.put("fwhm10", avg)

                avg = avgWpe20.getMean()
                if (!avg.isNaN())
                    bdoUnit.put("wpe20", avg)
                avg = avgEqe20.getMean()
                if (!avg.isNaN())
                    bdoUnit.put("eqe20", avg)
                avg = avgV20.getMean()
                if (!avg.isNaN())
                    bdoUnit.put("v20", avg)
                avg = avgPeak20.getMean()
                if (!avg.isNaN())
                    bdoUnit.put("peak20", avg)
                avg = avgFwhm20.getMean()
                if (!avg.isNaN())
                    bdoUnit.put("fwhm20", avg)

                avg = avgWpe50.getMean()
                if (!avg.isNaN())
                    bdoUnit.put("wpe50", avg)
                avg = avgEqe50.getMean()
                if (!avg.isNaN())
                    bdoUnit.put("eqe50", avg)
                avg = avgV50.getMean()
                if (!avg.isNaN())
                    bdoUnit.put("v50", avg)
                avg = avgPeak50.getMean()
                if (!avg.isNaN())
                    bdoUnit.put("peak50", avg)
                avg = avgFwhm50.getMean()
                if (!avg.isNaN())
                    bdoUnit.put("fwhm50", avg)

                avg = avgWpe01.getMean()
                if (!avg.isNaN())
                    bdoUnit.put("wpe01", avg)
                avg = avgEqe01.getMean()
                if (!avg.isNaN())
                    bdoUnit.put("eqe01", avg)
                avg = avgV01.getMean()
                if (!avg.isNaN())
                    bdoUnit.put("v01", avg)
                avg = avgPeak01.getMean()
                if (!avg.isNaN())
                    bdoUnit.put("peak01", avg)
                avg = avgFwhm01.getMean()
                if (!avg.isNaN())
                    bdoUnit.put("fwhm01", avg)

                avg = avgWpe02.getMean()
                if (!avg.isNaN())
                    bdoUnit.put("wpe02", avg)
                avg = avgEqe02.getMean()
                if (!avg.isNaN())
                    bdoUnit.put("eqe02", avg)
                avg = avgV02.getMean()
                if (!avg.isNaN())
                    bdoUnit.put("v02", avg)
                avg = avgPeak02.getMean()
                if (!avg.isNaN())
                    bdoUnit.put("peak02", avg)
                avg = avgFwhm02.getMean()
                if (!avg.isNaN())
                    bdoUnit.put("fwhm02", avg)

                avg = avgWpe04.getMean()
                if (!avg.isNaN())
                    bdoUnit.put("wpe04", avg)
                avg = avgEqe04.getMean()
                if (!avg.isNaN())
                    bdoUnit.put("eqe04", avg)
                avg = avgV04.getMean()
                if (!avg.isNaN())
                    bdoUnit.put("v04", avg)
                avg = avgPeak04.getMean()
                if (!avg.isNaN())
                    bdoUnit.put("peak04", avg)
                avg = avgFwhm04.getMean()
                if (!avg.isNaN())
                    bdoUnit.put("fwhm04", avg)

                avg = avgWpe06.getMean()
                if (!avg.isNaN())
                    bdoUnit.put("wpe06", avg)
                avg = avgEqe06.getMean()
                if (!avg.isNaN())
                    bdoUnit.put("eqe06", avg)
                avg = avgV06.getMean()
                if (!avg.isNaN())
                    bdoUnit.put("v06", avg)
                avg = avgPeak06.getMean()
                if (!avg.isNaN())
                    bdoUnit.put("peak06", avg)
                avg = avgFwhm06.getMean()
                if (!avg.isNaN())
                    bdoUnit.put("fwhm06", avg)

                avg = avgWpe08.getMean()
                if (!avg.isNaN())
                    bdoUnit.put("wpe08", avg)
                avg = avgEqe08.getMean()
                if (!avg.isNaN())
                    bdoUnit.put("eqe08", avg)
                avg = avgV08.getMean()
                if (!avg.isNaN())
                    bdoUnit.put("v08", avg)
                avg = avgPeak08.getMean()
                if (!avg.isNaN())
                    bdoUnit.put("peak08", avg)
                avg = avgFwhm08.getMean()
                if (!avg.isNaN())
                    bdoUnit.put("fwhm08", avg)

                def vbpMean = statsVbp.getMean()
                if (!vbpMean.isNaN())
                    bdoUnit.put("vbp", vbpMean)

                def rppMean = statsRpp.getMean()
                if (!rppMean.isNaN())
                    bdoUnit.put("rpp", rppMean)

                // Update unit if new variable initialized
                def pkey = "epi"
                if (tkey == "fa_test")
                    pkey = "test"

                if (bdoUnit.size() > 0) {
                    bdoUnit.put("id", unit["_id"])
                    bdoUnit.put("probeTestSynced", "YES")
                    bdoUnit.put("probeTestExperiment", [code])
                    def ddd = dt
                    if (!dt) {
                        bdoUnit.put("processCategory", "nwLED")
                        bdoUnit.put("processKey", pkey)
                        bdoUnit.put("taskKey", tkey)
                    } else {
                        bdoUnit.put("processCategory", "rel")
                        bdoUnit.put("processKey", "ni_dot_test")
                        bdoUnit.put("taskKey", "spc_ni_dot_test")
                    }
                    unitService.update(bdoUnit, "admin", true)
                    db.unit.update(new BasicDBObject("code", code), new BasicDBObject('$set', new BasicDBObject("probeTestSynced", "YES")), false, true)

                } else {
                    db.unit.update(new BasicDBObject("code", code), new BasicDBObject('$set', new BasicDBObject("probeTestSynced", "FAIL")), false, true)
                }
            }
        }

        // Now save files after all data is saved. This avoids transaction error.
        def name = "NiDot image"
        fileArray.each {

            def fileId = fileService.saveFileFromDisk(it[0], it[1], "image/jpg")
            fileService.saveFileMeta("admin", [it[2]], fileId, name, it[1])
        }

        !logr.isDebugEnabled() ?: logr.debug("probe test sync job ended.")
    }

    private def calculatePerformance(def deviceCode, def root) {

        def voltages = root["Datavoltage"]
        def currents = root["Datacurrent"]
        if (!voltages || !currents)
            return

        // calculate slope
        SimpleRegression sreg = new SimpleRegression()
        for (def iv in voltages.data) {
            if (iv[0] >= 0.45) {
                def icc = iv[1]
                if (icc > 10000) {
                    icc = 0
                }
                sreg.addData(icc * 1000, iv[0])
            }
            if (iv[0] > 1.55)
                break
        }
        def slopeIv = 1000 * sreg.getSlope()

        // Add corrected current to IV data
        for (def iv in voltages.data) {
            iv.add(iv[1] * 1000 - (iv[0] * 1000 / slopeIv))
        }
        voltages.put("v3", "currCorr (mA)")

        def leakageData = [:]

        def currData = currents.data.sort { it[0] }
        for (def iv in currData) {

            def iDiode = iv[0] - (iv[1] * 1000 / slopeIv)
            def pDiode = iv[1] * iDiode
            def wpeCorrected = null
            if (pDiode != 0) {
                wpeCorrected = 1000 * 100 * iv[2] / pDiode
            }
            def eqe = 0, eqeCorrected = 0

            def current
            def currentString
            if (iv[0] < 0.9) {
                current = iv[0].round(1).toFloat()
                currentString = "Data @ " + (current * 1000).round(0).toInteger() + "uA"
            } else {
                current = iv[0].round(0).toInteger()
                currentString = "Data @ " + current + "mA"
            }

            def peakWave = null
            if (root[currentString] && root[currentString]["Peak (nm)"])
                peakWave = root[currentString]["Peak (nm)"]

            if (peakWave) {
                if (iv[0] != 0) {
                    eqe = 1000 * (100 * iv[2]) / (1240 / peakWave) / iv[0]
                }
                if (iDiode != 0) {
                    eqeCorrected = 1000 * (100 * iv[2]) / (1240 / peakWave) / iDiode
                }
            }
            def j = (iv[0] / 1000) / deviceArray
            def jDiode = (iDiode / 1000) / deviceArray

            iv.add(iDiode)
            iv.add(pDiode)
            iv.add(wpeCorrected)
            iv.add(eqe)
            iv.add(eqeCorrected)
            iv.add(j)
            iv.add(jDiode)

            // Calculate leakage at 1
            if (iv[0] > 0.90 && !leakageData.containsKey(1) && root["Data @ 1mA"] && eqeCorrected > 0) {
                leakageData.put(1, calculateLeakage(eqeCorrected, root["Data @ 1mA"]["Peak (nm)"]))
            }
            // Calculate leakage at 4
            if (iv[0] > 3.90 && !leakageData.containsKey(4) && root["Data @ 4mA"] && eqeCorrected > 0) {
                leakageData.put(4, calculateLeakage(eqeCorrected, root["Data @ 4mA"]["Peak (nm)"]))
            }
            // Calculate leakage at 5
            if (iv[0] > 4.90 && !leakageData.containsKey(5) && root["Data @ 5mA"] && eqeCorrected > 0) {
                leakageData.put(5, calculateLeakage(eqeCorrected, root["Data @ 5mA"]["Peak (nm)"]))
            }
            // Calculate leakage at 10
            if (iv[0] > 9.80 && !leakageData.containsKey(10) && root["Data @ 10mA"] && eqeCorrected > 0) {
                leakageData.put(10, calculateLeakage(eqeCorrected, root["Data @ 10mA"]["Peak (nm)"]))
            }
        }
        currents.put("v5", "I diode (mA)")
        currents.put("v6", "P diode (mW)")
        currents.put("v7", "WPE corrected")
        currents.put("v8", "EQE")
        currents.put("v9", "EQE corrected")
        currents.put("v10", "J (A/cm2)")
        currents.put("v11", "J diode")

        leakageData
    }

    private def calculateLeakage(eqeCorr, peak) {

        if (peak && peak > 0) {
            (-0.00000021344 * peak**3 + 0.00036841 * peak**2 - 0.21358 * peak + 41.973) * eqeCorr
        } else {
            -1
        }
    }

}
