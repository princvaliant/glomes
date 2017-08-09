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
                            TimeRun   : 1,
                            DeviceID  : 1,
                            CurrentSet: 1
                    ]
            ], [
                    $group: [
                            _id    : [
                                    wid: '$WaferID',
                                    did: '$DeviceID'
                            ],
                            data   : [
                                    $push: [
                                            spectrum   : '$wavelengthPowerScan.data',
                                            viswp      : '$VISwp.data',
                                            virpp      : '$VIRPPSwp.data',
                                            current    : '$CurrentSet',
                                            volt       : '$Volt',
                                            eqe        : '$eqe',
                                            wpe        : '$wpe',
                                            eqec       : '$eqec',
                                            wpec       : '$wpec',
                                            peak       : '$RawPeakWavelength',
                                            dominant   : '$dominantWavelength',
                                            photometric: '$photometric',
                                            radiometric: '$radiometric',
                                            fwhm       : '$FWHM',
                                            efficacy   : '$efficacy',
                                            sid        : '$sid'
                                    ]
                            ],
                            lastSid: [
                                    $last: '$sid'
                            ]
                    ]
            ], [
                    $project: [
                            _id : '$_id',
                            data: [
                                    $filter: [
                                            input: '$data',
                                            as   : 'itm',
                                            cond : [
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

            try {

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
                    DescriptiveStatistics avgWpec1 = new DescriptiveStatistics()
                    DescriptiveStatistics avgEqec1 = new DescriptiveStatistics()

                    DescriptiveStatistics avgWpe2 = new DescriptiveStatistics()
                    DescriptiveStatistics avgEqe2 = new DescriptiveStatistics()
                    DescriptiveStatistics avgV2 = new DescriptiveStatistics()
                    DescriptiveStatistics avgPeak2 = new DescriptiveStatistics()
                    DescriptiveStatistics avgFwhm2 = new DescriptiveStatistics()
                    DescriptiveStatistics avgWpec2 = new DescriptiveStatistics()
                    DescriptiveStatistics avgEqec2 = new DescriptiveStatistics()

                    DescriptiveStatistics avgWpe4 = new DescriptiveStatistics()
                    DescriptiveStatistics avgEqe4 = new DescriptiveStatistics()
                    DescriptiveStatistics avgV4 = new DescriptiveStatistics()
                    DescriptiveStatistics avgPeak4 = new DescriptiveStatistics()
                    DescriptiveStatistics avgFwhm4 = new DescriptiveStatistics()
                    DescriptiveStatistics avgWpec4 = new DescriptiveStatistics()
                    DescriptiveStatistics avgEqec4 = new DescriptiveStatistics()

                    DescriptiveStatistics avgWpe5 = new DescriptiveStatistics()
                    DescriptiveStatistics avgEqe5 = new DescriptiveStatistics()
                    DescriptiveStatistics avgV5 = new DescriptiveStatistics()
                    DescriptiveStatistics avgPeak5 = new DescriptiveStatistics()
                    DescriptiveStatistics avgFwhm5 = new DescriptiveStatistics()
                    DescriptiveStatistics avgWpec5 = new DescriptiveStatistics()
                    DescriptiveStatistics avgEqec5 = new DescriptiveStatistics()

                    DescriptiveStatistics avgWpe10 = new DescriptiveStatistics()
                    DescriptiveStatistics avgEqe10 = new DescriptiveStatistics()
                    DescriptiveStatistics avgV10 = new DescriptiveStatistics()
                    DescriptiveStatistics avgPeak10 = new DescriptiveStatistics()
                    DescriptiveStatistics avgFwhm10 = new DescriptiveStatistics()
                    DescriptiveStatistics avgWpec10 = new DescriptiveStatistics()
                    DescriptiveStatistics avgEqec10 = new DescriptiveStatistics()

                    DescriptiveStatistics avgWpe20 = new DescriptiveStatistics()
                    DescriptiveStatistics avgEqe20 = new DescriptiveStatistics()
                    DescriptiveStatistics avgV20 = new DescriptiveStatistics()
                    DescriptiveStatistics avgPeak20 = new DescriptiveStatistics()
                    DescriptiveStatistics avgFwhm20 = new DescriptiveStatistics()
                    DescriptiveStatistics avgWpec20 = new DescriptiveStatistics()
                    DescriptiveStatistics avgEqec20 = new DescriptiveStatistics()

                    DescriptiveStatistics avgWpe50 = new DescriptiveStatistics()
                    DescriptiveStatistics avgEqe50 = new DescriptiveStatistics()
                    DescriptiveStatistics avgV50 = new DescriptiveStatistics()
                    DescriptiveStatistics avgPeak50 = new DescriptiveStatistics()
                    DescriptiveStatistics avgFwhm50 = new DescriptiveStatistics()
                    DescriptiveStatistics avgWpec50 = new DescriptiveStatistics()
                    DescriptiveStatistics avgEqec50 = new DescriptiveStatistics()

                    DescriptiveStatistics avgWpe01 = new DescriptiveStatistics()
                    DescriptiveStatistics avgEqe01 = new DescriptiveStatistics()
                    DescriptiveStatistics avgV01 = new DescriptiveStatistics()
                    DescriptiveStatistics avgPeak01 = new DescriptiveStatistics()
                    DescriptiveStatistics avgFwhm01 = new DescriptiveStatistics()
                    DescriptiveStatistics avgWpec01 = new DescriptiveStatistics()
                    DescriptiveStatistics avgEqec01 = new DescriptiveStatistics()

                    DescriptiveStatistics avgWpe02 = new DescriptiveStatistics()
                    DescriptiveStatistics avgEqe02 = new DescriptiveStatistics()
                    DescriptiveStatistics avgV02 = new DescriptiveStatistics()
                    DescriptiveStatistics avgPeak02 = new DescriptiveStatistics()
                    DescriptiveStatistics avgFwhm02 = new DescriptiveStatistics()
                    DescriptiveStatistics avgWpec02 = new DescriptiveStatistics()
                    DescriptiveStatistics avgEqec02 = new DescriptiveStatistics()

                    DescriptiveStatistics avgWpe04 = new DescriptiveStatistics()
                    DescriptiveStatistics avgEqe04 = new DescriptiveStatistics()
                    DescriptiveStatistics avgV04 = new DescriptiveStatistics()
                    DescriptiveStatistics avgPeak04 = new DescriptiveStatistics()
                    DescriptiveStatistics avgFwhm04 = new DescriptiveStatistics()
                    DescriptiveStatistics avgWpec04 = new DescriptiveStatistics()
                    DescriptiveStatistics avgEqec04 = new DescriptiveStatistics()

                    DescriptiveStatistics avgWpe06 = new DescriptiveStatistics()
                    DescriptiveStatistics avgEqe06 = new DescriptiveStatistics()
                    DescriptiveStatistics avgV06 = new DescriptiveStatistics()
                    DescriptiveStatistics avgPeak06 = new DescriptiveStatistics()
                    DescriptiveStatistics avgFwhm06 = new DescriptiveStatistics()
                    DescriptiveStatistics avgWpec06 = new DescriptiveStatistics()
                    DescriptiveStatistics avgEqec06 = new DescriptiveStatistics()

                    DescriptiveStatistics avgWpe08 = new DescriptiveStatistics()
                    DescriptiveStatistics avgEqe08 = new DescriptiveStatistics()
                    DescriptiveStatistics avgV08 = new DescriptiveStatistics()
                    DescriptiveStatistics avgPeak08 = new DescriptiveStatistics()
                    DescriptiveStatistics avgFwhm08 = new DescriptiveStatistics()
                    DescriptiveStatistics avgWpec08 = new DescriptiveStatistics()
                    DescriptiveStatistics avgEqec08 = new DescriptiveStatistics()

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
                    DescriptiveStatistics avgCurr2Center = new DescriptiveStatistics()
                    DescriptiveStatistics avgCurr2All = new DescriptiveStatistics()
                    DescriptiveStatistics avgPeak2Center = new DescriptiveStatistics()

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
                    def peaks2 = [:]
                    def peaksCenter2 = [:]
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
                    def corr2 = [:]
                    def corrCenter2 = [:]
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
                        if (deviceCode.size() < 4) {
                            deviceCode = 'DUMMY'
                        }
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
                                spectrum = it.spectrum.findAll {
                                    it[0] > 400 && it[0] < 750
                                }
                            }

                            Double curr = it.current * 1000,
                                   peak = it.peak,
                                   fwhm = it.fwhm,
                                   peakPower = it.photometric,
                                   intPower = it.radiometric

                            currsweep.add([curr, it.volt, it.radiometric, it.wpe, it.fwhm, 0, it.wpec, it.eqe, it.eqec])
                          //  System.out.println([curr, it.volt, it.radiometric, it.wpe, it.fwhm, 0, it.wpec, it.eqe, it.eqec])
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
                            if (curr > 0.9 && curr.round() == 1) {
                                avgPeak1.addValue(peak)
                                peaks1.put(deviceCode, peak)
                                avgFwhm1.addValue(fwhm)

                                if (deviceCode.toUpperCase().substring(0, 4) in ["NE00", "SE00", "NW00", "SW00"]) {
                                    avgPeak1Center.addValue(peak)
                                    peaksCenter1.put(deviceCode, peak)
                                }
                            }
                            if (curr > 1.4 && curr.round() == 2) {
                                avgPeak2.addValue(peak)
                                peaks2.put(deviceCode, peak)
                                avgFwhm2.addValue(fwhm)

                                if (deviceCode.toUpperCase().substring(0, 4) in ["NE00", "SE00", "NW00", "SW00"]) {
                                    avgPeak2Center.addValue(peak)
                                    peaksCenter2.put(deviceCode, peak)
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
                                    "second": "Intensity",
                                    data    : spectrum])
                        }

                        root.put("Datacurrent", [data: currsweep])

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

                                //     currsweep.add([curr, it.volt, it.radiometric, it.wpe, it.fwhm, 0, it.wpec, it.eqe, it.eqec])

                                List currs = ddd.collect { (double) (it[0] ?: 0) }
                                List currsw = ddd.collect { (double) (it[0] ?: 0) }
                                List currse = ddd.collect { (double) (it[0] ?: 0) }
                                List volts = ddd.collect { (double) (it[1] ?: 0) }
                                List fwhms = ddd.collect { (double) (it[4] ?: 0) }
                                List wpes = ddd.collect { (double) (it[3] ?: 0) }
                                List eqes = ddd.collect { (double) (it[7] ?: 0) }
                                List wpecs = ddd.collect { (double) (it[6] ?: 0) }
                                List eqecs = ddd.collect { (double) (it[8] ?: 0) }

                                DescriptiveStatistics statsWpe = new DescriptiveStatistics((double[]) currents.data.collect {
                                    if (it[3] == null) 0
                                    else
                                        (double) (it[3] ?: 0)
                                })
                                DescriptiveStatistics statsEqe = new DescriptiveStatistics((double[]) currents.data.collect {
                                    if (it[7] == null) 0
                                    else
                                        (double) (it[7] ?: 0)
                                })

                                def maxWpe = statsWpe.getMax()
                                def maxEqe = statsEqe.getMax()
                                def jWpe = ''
                                def jEqe = ''



                                ddd.each { currrow ->

                                    if (!tValue.containsKey("wpe01") && currrow[0] > 0.08 && currrow[0] < 0.12) {
                                        tValue.put("v01", currrow[1])
                                        avgV01.addValue(currrow[1])
                                        tValue.put("fwhm01", currrow[4])
                                        avgFwhm01.addValue(currrow[4])
                                        if (currrow[3] > 0  && currrow[7] > 0) {
                                            tValue.put("wpe01", currrow[3])
                                            avgWpe01.addValue(currrow[3])
                                            tValue.put("eqe01", currrow[7])
                                            avgEqe01.addValue(currrow[7])
                                            tValue.put("wpec01", currrow[6])
                                            avgWpec01.addValue(currrow[6])
                                            tValue.put("eqec01", currrow[8])
                                            avgEqec01.addValue(currrow[8])
                                        }
                                    }

                                    if (!tValue.containsKey("wpe02") && currrow[0] > 0.18 && currrow[0] < 0.22) {
                                        tValue.put("v02", currrow[1])
                                        avgV02.addValue(currrow[1])
                                        tValue.put("fwhm02", currrow[4])
                                        avgFwhm02.addValue(currrow[4])
                                        if (currrow[3] > 0  && currrow[7] > 0) {
                                            tValue.put("wpe02", currrow[3])
                                            avgWpe02.addValue(currrow[3])
                                            tValue.put("eqe02", currrow[7])
                                            avgEqe02.addValue(currrow[7])

                                            tValue.put("wpec02", currrow[6])
                                            avgWpec02.addValue(currrow[6])
                                            tValue.put("eqec02", currrow[8])
                                            avgEqec02.addValue(currrow[8])
                                        }
                                    }

                                    if (!tValue.containsKey("wpe04") && currrow[0] > 0.38 && currrow[0] < 0.42) {
                                        tValue.put("v04", currrow[1])
                                        avgV04.addValue(currrow[1])
                                        tValue.put("fwhm04", currrow[4])
                                        avgFwhm04.addValue(currrow[4])
                                        if (currrow[3] > 0  && currrow[7] > 0) {
                                            tValue.put("wpe04", currrow[3])
                                            avgWpe04.addValue(currrow[3])
                                            tValue.put("eqe04", currrow[7])
                                            avgEqe04.addValue(currrow[7])
                                            tValue.put("wpec04", currrow[6])
                                            avgWpec04.addValue(currrow[6])
                                            tValue.put("eqec04", currrow[8])
                                            avgEqec04.addValue(currrow[8])
                                        }
                                    }

                                    if (!tValue.containsKey("wpe06") && currrow[0] > 0.58 && currrow[0] < 0.62) {
                                        tValue.put("v06", currrow[1])
                                        avgV06.addValue(currrow[1])
                                        tValue.put("fwhm06", currrow[4])
                                        avgFwhm06.addValue(currrow[4])
                                        if (currrow[3] > 0  && currrow[7] > 0) {
                                        tValue.put("wpe06", currrow[3])
                                        avgWpe06.addValue(currrow[3])
                                        tValue.put("eqe06", currrow[7])
                                        avgEqe06.addValue(currrow[7])

                                            tValue.put("wpec06", currrow[6])
                                            avgWpec06.addValue(currrow[6])
                                            tValue.put("eqec06", currrow[8])
                                            avgEqec06.addValue(currrow[8])
                                        }
                                    }

                                    if (!tValue.containsKey("wpe08") && currrow[0] > 0.78 && currrow[0] < 0.82) {
                                        tValue.put("v08", currrow[1])
                                        avgV08.addValue(currrow[1])
                                        tValue.put("fwhm08", currrow[4])
                                        avgFwhm08.addValue(currrow[4])
                                        if (currrow[3] > 0  && currrow[7] > 0) {
                                            tValue.put("wpe08", currrow[3])
                                            avgWpe08.addValue(currrow[3])
                                            tValue.put("eqe08", currrow[7])
                                            avgEqe08.addValue(currrow[7])

                                            tValue.put("wpec08", currrow[6])
                                            avgWpec08.addValue(currrow[6])
                                            tValue.put("eqec08", currrow[8])
                                            avgEqec08.addValue(currrow[8])
                                        }
                                    }

                                    if (!tValue.containsKey("wpe1") && currrow[0] > 0.88 && currrow[0] < 1.2) {
                                        tValue.put("v1", currrow[1])
                                        avgV1.addValue(currrow[1])
                                        tValue.put("fwhm1", currrow[4])
                                        avgFwhm1.addValue(currrow[4])
                                        if (currrow[3] > 0  && currrow[7] > 0) {
                                            tValue.put("wpe1", currrow[3])
                                            avgWpe1.addValue(currrow[3])
                                            tValue.put("eqe1", currrow[7])
                                            avgEqe1.addValue(currrow[7])
                                            tValue.put("wpec1", currrow[6])
                                            avgWpec1.addValue(currrow[6])
                                            tValue.put("eqec1", currrow[8])
                                            avgEqec1.addValue(currrow[8])
                                        }
                                    }

                                    if (!tValue.containsKey("wpe2") && currrow[0] > 1.88 && currrow[0] < 2.2) {
                                        tValue.put("v2", currrow[1])
                                        avgV2.addValue(currrow[1])
                                        tValue.put("fwhm2", currrow[4])
                                        avgFwhm2.addValue(currrow[4])
                                        if (currrow[3] > 0 && currrow[7] > 0) {
                                            tValue.put("wpe2", currrow[3])
                                            avgWpe2.addValue(currrow[3])
                                            tValue.put("eqe2", currrow[7])
                                            avgEqe2.addValue(currrow[7])

                                            tValue.put("wpec2", currrow[6])
                                            avgWpec2.addValue(currrow[6])
                                            tValue.put("eqec2", currrow[8])
                                            avgEqec2.addValue(currrow[8])
                                        }
                                    }

                                    if (!tValue.containsKey("wpe4") && currrow[0] > 3.78 && currrow[0] < 4.22) {
                                        tValue.put("v4", currrow[1])
                                        avgV4.addValue(currrow[1])
                                        tValue.put("fwhm4", currrow[4])
                                        avgFwhm4.addValue(currrow[4])
                                        if (currrow[3] > 0 && currrow[7] > 0) {
                                            tValue.put("wpe4", currrow[3])
                                            avgWpe4.addValue(currrow[3])
                                            tValue.put("eqe4", currrow[7])
                                            avgEqe4.addValue(currrow[7])
                                            tValue.put("wpec4", currrow[6])
                                            avgWpec4.addValue(currrow[6])
                                            tValue.put("eqec4", currrow[8])
                                            avgEqec4.addValue(currrow[8])
                                        }
                                    }

                                    if (!tValue.containsKey("wpe5") && currrow[0] > 4.78 && currrow[0] < 5.22) {
                                        tValue.put("v5", currrow[1])
                                        avgV5.addValue(currrow[1])
                                        tValue.put("fwhm5", currrow[4])
                                        avgFwhm5.addValue(currrow[4])
                                        if (currrow[3] > 0 && currrow[7] > 0) {
                                            tValue.put("wpe5", currrow[3])
                                            avgWpe5.addValue(currrow[3])
                                            tValue.put("eqe5", currrow[7])
                                            avgEqe5.addValue(currrow[7])

                                            tValue.put("wpec5", currrow[6])
                                            avgWpec5.addValue(currrow[6])
                                            tValue.put("eqec5", currrow[8])
                                            avgEqec5.addValue(currrow[8])
                                        }
                                    }


                                    if (!tValue.containsKey("wpe10") && currrow[0] > 9.78 && currrow[0] < 10.42) {
                                        tValue.put("v10", currrow[1])
                                        avgV10.addValue(currrow[1])
                                        tValue.put("fwhm10", currrow[4])
                                        avgFwhm10.addValue(currrow[4])
                                        if (currrow[3] > 0 && currrow[7] > 0) {
                                            tValue.put("wpe10", currrow[3])
                                            avgWpe10.addValue(currrow[3])
                                            tValue.put("eqe10", currrow[7])
                                            avgEqe10.addValue(currrow[7])

                                            tValue.put("wpec10", currrow[6])
                                            avgWpec10.addValue(currrow[6])
                                            tValue.put("eqec10", currrow[8])
                                            avgEqec10.addValue(currrow[8])
                                        }
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

                                    if (k == 2) {

                                        if (deviceCode.toUpperCase().substring(0, 4) in ["NE00", "SE00", "NW00", "SW00"]) {
                                            avgCurr2Center.addValue(v)
                                            corrCenter2.put(v, deviceCode)
                                        }

                                        avgCurr2All.addValue(v)
                                        corr2.put(v, deviceCode)
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

                                maxWpe?.toString()?.isNumber() ? avgWpes.addValue(maxWpe) : ''
                                maxEqe?.toString()?.isNumber() ? avgEqes.addValue(maxEqe) : ''
                                jWpe?.toString()?.isNumber() ? avgJWpes.addValue(jWpe) : ''
                                jEqe?.toString()?.isNumber() ? avgJEqes.addValue(jEqe) : ''
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

                    avg = avgCurr2Center.getMean()
                    if (!avg.isNaN())
                        bdoUnit.put("CorrEQE-2mA-Center-Avg", avg)
                    avg = avgCurr2Center.getMax()
                    if (!avg.isNaN()) {
                        bdoUnit.put("CorrEQE-2mA-Center-Best", avg)
                        bdoUnit.put("Peak-2mA-Center-Best", peaksCenter2[corrCenter2[avg]])
                    }
                    avg = avgCurr2All.getMax()
                    if (!avg.isNaN()) {
                        bdoUnit.put("CorrEQE-2mA-Best", avg)
                        bdoUnit.put("Peak-2mA-Best", peaks2[corr2[avg]])
                    }
                    avg = avgPeak2Center.getMean()
                    if (!avg.isNaN()) {
                        bdoUnit.put("Peak-2mA-Center-Avg", avg)
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
                    avg = avgWpec1.getMean()
                    if (!avg.isNaN())
                        bdoUnit.put("wpec1", avg)
                    avg = avgEqec1.getMean()
                    if (!avg.isNaN())
                        bdoUnit.put("eqec1", avg)
                    avg = avgV1.getMean()
                    if (!avg.isNaN())
                        bdoUnit.put("v1", avg)
                    avg = avgPeak1.getMean()
                    if (!avg.isNaN())
                        bdoUnit.put("peak1", avg)
                    avg = avgFwhm1.getMean()
                    if (!avg.isNaN())
                        bdoUnit.put("fwhm1", avg)

                    avg = avgWpe2.getMean()
                    if (!avg.isNaN())
                        bdoUnit.put("wpe2", avg)
                    avg = avgEqe2.getMean()
                    if (!avg.isNaN())
                        bdoUnit.put("eqe2", avg)
                    avg = avgWpec2.getMean()
                    if (!avg.isNaN())
                        bdoUnit.put("wpec2", avg)
                    avg = avgEqec2.getMean()
                    if (!avg.isNaN())
                        bdoUnit.put("eqec2", avg)
                    avg = avgV2.getMean()
                    if (!avg.isNaN())
                        bdoUnit.put("v2", avg)
                    avg = avgPeak2.getMean()
                    if (!avg.isNaN())
                        bdoUnit.put("peak2", avg)
                    avg = avgFwhm2.getMean()
                    if (!avg.isNaN())
                        bdoUnit.put("fwhm2", avg)

                    avg = avgWpe4.getMean()
                    if (!avg.isNaN())
                        bdoUnit.put("wpe4", avg)
                    avg = avgEqe4.getMean()
                    if (!avg.isNaN())
                        bdoUnit.put("eqe4", avg)
                    avg = avgWpec4.getMean()
                    if (!avg.isNaN())
                        bdoUnit.put("wpec4", avg)
                    avg = avgEqec4.getMean()
                    if (!avg.isNaN())
                        bdoUnit.put("eqec4", avg)
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
                    avg = avgWpec5.getMean()
                    if (!avg.isNaN())
                        bdoUnit.put("wpec5", avg)
                    avg = avgEqec5.getMean()
                    if (!avg.isNaN())
                        bdoUnit.put("eqec5", avg)
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
                    avg = avgWpec10.getMean()
                    if (!avg.isNaN())
                        bdoUnit.put("wpec10", avg)
                    avg = avgEqec10.getMean()
                    if (!avg.isNaN())
                        bdoUnit.put("eqec10", avg)
                    avg = avgV10.getMean()
                    if (!avg.isNaN())
                        bdoUnit.put("v10", avg)
                    avg = avgPeak10.getMean()
                    if (!avg.isNaN())
                        bdoUnit.put("peak10", avg)
                    avg = avgFwhm10.getMean()
                    if (!avg.isNaN())
                        bdoUnit.put("fwhm10", avg)


                    avg = avgWpe01.getMean()
                    if (!avg.isNaN())
                        bdoUnit.put("wpe01", avg)
                    avg = avgEqe01.getMean()
                    if (!avg.isNaN())
                        bdoUnit.put("eqe01", avg)
                    avg = avgWpec01.getMean()
                    if (!avg.isNaN())
                        bdoUnit.put("wpec01", avg)
                    avg = avgEqec01.getMean()
                    if (!avg.isNaN())
                        bdoUnit.put("eqec01", avg)
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
                    avg = avgWpec02.getMean()
                    if (!avg.isNaN())
                        bdoUnit.put("wpec02", avg)
                    avg = avgEqec02.getMean()
                    if (!avg.isNaN())
                        bdoUnit.put("eqec02", avg)
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
                    avg = avgWpec04.getMean()
                    if (!avg.isNaN())
                        bdoUnit.put("wpec04", avg)
                    avg = avgEqec04.getMean()
                    if (!avg.isNaN())
                        bdoUnit.put("eqec04", avg)
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
                    avg = avgWpec06.getMean()
                    if (!avg.isNaN())
                        bdoUnit.put("wpec06", avg)
                    avg = avgEqec06.getMean()
                    if (!avg.isNaN())
                        bdoUnit.put("eqec06", avg)
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
                    avg = avgWpec08.getMean()
                    if (!avg.isNaN())
                        bdoUnit.put("wpec08", avg)
                    avg = avgEqec08.getMean()
                    if (!avg.isNaN())
                        bdoUnit.put("eqec08", avg)
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
                    if (bdoUnit.size() > 0) {
                        bdoUnit.put("id", unit["_id"])
                        bdoUnit.put("probeTestSynced", "YES")
                        bdoUnit.put("probeTestExperiment", [code])
                        def ddd = dt
                        if (!dt) {
                            bdoUnit.put("processCategory", unit.pctg)
                            bdoUnit.put("processKey", unit.pkey)
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
            } catch (Exception exc) {
                System.out.println(exc);
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
