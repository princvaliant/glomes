package com.glo.custom

import com.glo.ndo.Product
import com.glo.security.User
import com.mongodb.BasicDBObject
import org.apache.commons.logging.LogFactory
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics

import java.text.SimpleDateFormat

class SyncService {

    private static final logr = LogFactory.getLog(this)

    def mongo
    def unitService
    def basicDataSyncService
    def relSyncService
    def dateCheckerService
    def readFileService
    def utilsService
    def grailsApplication

    def execute(def units, def variables, def tkey, def username) {
        if (tkey == "ngan_inspection")
            return ngan_inspection(units, variables)
        if (tkey == "pl") {

            return pl(units, variables)
        }
        if (tkey == "sem")
            return sem(units, variables)
        if (tkey == "test_data_visualization" || tkey == "nbp_test_data_visualization" || tkey == "nbp_full_test_visualization" || tkey == "full_test_visualization" || tkey == "char_test_visualization" || tkey == "pre_dbr_test" || tkey == "post_dbr_test" || tkey == "intermediate_coupon_test")
            return test_data_visualization(tkey, units, variables)
        if (tkey in [
                "ni_dot_test",
                "planar_ni_dot_test",
                "nw_ito_dot_test",
                "ito_dot_test",
                "fa_test"
        ])
            return probe_test(tkey, units, variables)
        if (tkey == "ni_dot_spc_test")
            return ni_dot_spc_test(tkey, units, variables)
        if (tkey == "wafer_rel_test")
            return test_rel_wafer(tkey, units, variables)

        // OLD individual syncs
        if (tkey == "test_rel")
            return test_rel(tkey, units, variables)
        if (tkey == "sphere_test_lamp" || tkey == "sphere_test2_lamp")
            return test_lamp(tkey, units, variables)

        if (tkey == "test_rel_board" || tkey == "spc_standard_board")
            return test_rel_board(tkey, units, variables)

        if (tkey == "sphere_test_lamp_board" || tkey == "sphere_test2_lamp_board")
            return test_lamp_board(tkey, units, variables)

        // CDSEM file import
        if (tkey in [
                "dicd_meas",
                "ficd_meas",
                "post_etch_cd_sem_1",
                "post_etch_cd_sem_2"
        ])
            return cdsem_file_import(tkey, units, variables, username)

        // ICP file import
        if (tkey in [
                "nil_etch",
                "nil_etch_qual",
                "mesa_etch",
                "isolation_etch"
        ])
            return icp_import(tkey, units, variables, username)
    }


    private def icp_import(def tkey, def units, def variables, def username) {

        def dirs = [grailsApplication.config.glo.icpDataLogDirectory, grailsApplication.config.glo.icpDataLogDirectory2]
        def ret = 0

        dirs.each { dir ->

            if (!dir) {
                throw new RuntimeException("ICP data log directory not specified")
            }
            File f = new File(dir)
            if (!f.exists())
                throw new RuntimeException("ICP ERROR: cannot access " + dir + " directory specified in glo.properties config file")


            def stepName = { fileName ->
                if ((tkey == "nil_etch" || tkey == "nil_etch_qual") && fileName.indexOf("SiN_") == 0)
                    tkey
                else if (tkey == "isolation_etch" && fileName.indexOf("ISO40") == 0)
                    "isolation_etch"
                else if (tkey == "mesa_etch" && fileName.indexOf("CpGaN") == 0 || fileName.indexOf("GaN_etch_HighPower") == 0)
                    "mesa_etch"
                else
                    ""
            }

            units.data.each { unit ->

                def lastFiles = [:]
                def prefix = """(.*)${unit.code.toUpperCase()}(.*)"""
                def unitFiles = new File(dir).listFiles({ directory, file ->
                    file ==~ /${prefix}/
                } as FilenameFilter).sort { it.lastModified() }

                unitFiles.each {
                    lastFiles.put(stepName(it.name), it)
                }
                lastFiles.each { step, file ->

                    //Take column R of a Data log and determine the first entry which has greater than 250 watts  (RFBiasGeneratorForwardPower)
                    //Take column R of a Data log and determine the last entry which has greater than 250 watts (RFBiasGeneratorForwardPower)
                    //Take an average of (PowerOn+5 entries down) to PowerOff
                    //Of:
                    //Column J (GasVacuumSystemTurboThrottleValvePosition)
                    //Column V (RFBiasMatchNetworkC1Position)                       //This is known as the tune capacitor
                    //Column X (RFBiasMatchNetworkC2Position)                       //This is known as the Load capacitor
                    //Column Z (RFBiasMatchNetworkDCBiasSensor)

                    if (step) {
                        def historicalDataPassed = false
                        def line
                        def counter = 0
                        def lastUpdated
                        ret++
                        DescriptiveStatistics throttle = new DescriptiveStatistics()
                        DescriptiveStatistics tune = new DescriptiveStatistics()
                        DescriptiveStatistics load = new DescriptiveStatistics()
                        DescriptiveStatistics dcBias = new DescriptiveStatistics()
                        BufferedReader br = new BufferedReader(new FileReader(file))
                        while ((line = br.readLine()) != null) {
                            if (historicalDataPassed) {
                                def row = line.split('\t')
                                if (row.length > 70) {
                                    counter++
                                    if (counter > 5) {
                                        if (row[15].isNumber()) throttle.addValue((double) Double.parseDouble(row[15]))
                                        if (row[35].isNumber()) tune.addValue((double) Double.parseDouble(row[35]))
                                        if (row[39].isNumber()) load.addValue((double) Double.parseDouble(row[39]))
                                        if (row[43].isNumber()) dcBias.addValue((double) Double.parseDouble(row[43]))
                                    }
                                } else  if (row.length > 25) {
                                    counter++
                                    if (counter > 5) {
                                        if (row[9].isNumber()) throttle.addValue((double) Double.parseDouble(row[9]))
                                        if (row[21].isNumber()) tune.addValue((double) Double.parseDouble(row[21]))
                                        if (row[23].isNumber()) load.addValue((double) Double.parseDouble(row[23]))
                                        if (row[25].isNumber()) dcBias.addValue((double) Double.parseDouble(row[25]))
                                    }
                                }
                            }
                            if (line.indexOf("HistoricalData:") >= 0)
                                historicalDataPassed = true
                            def lastUpdatedIndex = line.indexOf("Start:(")
                            def lastUpdatedIndexE = line.indexOf(")")
                            if (lastUpdatedIndex >= 0 && lastUpdatedIndexE > 0) {
                                lastUpdated = line.substring(lastUpdatedIndex + 7, lastUpdatedIndexE)
                                try {
                                    def icpDate = new SimpleDateFormat("MMMM dd, yyyy h:mm:ss a").parse(lastUpdated)
                                    lastUpdated = new SimpleDateFormat("YYYY-MM-dd HH:mm").format(icpDate)
                                } catch (Exception exc) {
                                    def icpDate = new SimpleDateFormat("MMMM dd, yyyy HH:mm:ss").parse(lastUpdated)
                                    lastUpdated = new SimpleDateFormat("YYYY-MM-dd HH:mm").format(icpDate)
                                }
                            }
                        }

                        def bdoUnit = new BasicDBObject()
                        bdoUnit.put("id", unit["_id"])
                        bdoUnit.put("processCategory", unit.pctg)
                        bdoUnit.put("processKey", unit.pkey)
                        bdoUnit.put("taskKey", step)
                        bdoUnit.put(unit.pkey + tkey + "Synced", "YES")
                        bdoUnit.put("ICPDate", lastUpdated)
                        bdoUnit.put("ICP_equipment", dir == grailsApplication.config.glo.icpDataLogDirectory ? "ICP" : "ICP 2")
                        def res = throttle.getMean()
                        if (!(res && res.isNaN())) bdoUnit.put("ThrottleAvg", res)
                        res = tune.getMean()
                        if (!(res && res.isNaN())) bdoUnit.put("TuneAvg", res)
                        res = load.getMean()
                        if (!(res && res.isNaN())) bdoUnit.put("LoadAvg", res)
                        res = dcBias.getMean()
                        if (!(res && res.isNaN())) bdoUnit.put("DCBiasAvg", res)
                        unitService.update(bdoUnit, username, true)
                    }
                }
            }
        }
        return ret
    }


    private def cdsem_file_import(def tkey, def units, def variables, def username) {

        //String dir = ConfigurationHolder.config.glo.CDSEMDirectory
        String dir = grailsApplication.config.glo.CDSEMDirectory
        if (!dir)
            throw new RuntimeException("CDSEM ERROR: file directory not specified in glo.properties config file")

        // Loop through all the files in the directory
        File f = new File(dir)
        if (!f.exists())
            throw new RuntimeException("CDSEM ERROR: cannot access " + dir + " directory specified in glo.properties config file")

        def fileCntSuccess = 0
        def fileCntError = 0
        def strError = ""

        def usr = User.findByUsername(username)
        def grps = usr?.getAuthorities()?.collect { it.id }
        def admin = grps.contains("ROLE_PROCESS_ADMIN")

        def f1 = new File(dir).listFiles().grep(~/(?i).*.xls/).sort { it.lastModified() }
        f1.each {

            def data
            try {
                data = readFileService.readTestFileCDSEM(it)
                def taskKey
                data.each { fileData ->
                    if (fileData.Process) {

                        readFileService.processTestFileCDSEM(fileData)
                        readFileService.storeTestFileCDSEM(it.name, fileData, admin)
                        taskKey = fileData.taskKey
                        fileCntSuccess++
                    }
                }

                if (taskKey) {
                    def dirProcessed = dir + "\\processed_" + taskKey
                    File file = new File(dirProcessed, it.name)
                    if (file.exists())
                        file.delete()
                    it.renameTo(new File(dirProcessed, it.name))
                }

            } catch (Exception exc) {

                strError += "<br/>&nbsp;&nbsp;&nbsp;" + it.name + ": " + exc.getMessage()
                fileCntError++
            }


        }

        if (fileCntSuccess == 0 && fileCntError == 0)
            throw new RuntimeException("CDSEM: no files found for processing")

        if (fileCntError) {
            def str = "CDSEM: " + fileCntSuccess + " file(s) processed successfully" // + strSuccess
            str += "<br/><br/>ERRORS: " + fileCntError + " file(s) NOT processed" + strError
            throw new RuntimeException(str)
        }
        return fileCntSuccess
    }


    private def test_data_visualization(def tkey, def units, def variables) {

        String dir = grailsApplication.config.glo.testDataDirectory
        if (!dir) {
            throw new RuntimeException("Test data sync directory not specified")
        }

        // Determine which units contain folder to be synched
        def db = mongo.getDB("glo")
        def unitsToBeSynched = [:]

        units.data.each {

            String syncVar
            String appendForFull = ""
            if (tkey == "test_data_visualization" || tkey == "nbp_test_data_visualization" || tkey == "nbp_full_test_visualization" || tkey == "intermediate_coupon_test") {
                syncVar = "testDataSynced"
            }
            if (tkey == "top_test_visualization") {
                syncVar = "testTopSynced"
            }
            if (tkey == "char_test_visualization") {
                syncVar = "testCharSynced"
            }
            if (tkey == "pre_dbr_test") {
                syncVar = "testPreDbrSynced"
            }
            if (tkey == "post_dbr_test") {
                syncVar = "testPostDbrSynced"
            }
            if (tkey == "full_test_visualization") {
                syncVar = "testFullSynced"
                appendForFull = "_100PCT"
            }
            String isSync = it[syncVar] != null ? it[syncVar].trim() : ""

            if (it["mask"] && it["experimentId"] && isSync == "SYNC") {

                File f = new File(dir + "\\" + it["mask"] + "\\" + (it["experimentId"] ?: "EXPERIMENT_NOT_DEFINED") + "\\" + it.code + appendForFull)

                if (f.exists() && it.tkey == tkey) {
                    db.unit.update(new BasicDBObject("code", it.code), new BasicDBObject('$set', new BasicDBObject(syncVar, "---")), false, true)
                    unitsToBeSynched.put(f, it)
                }
            }
        }

        basicDataSyncService.init("TESTDATA", unitsToBeSynched)
        return unitsToBeSynched.size()
    }

    private def probe_test(def tkey, def units, def variables) {

        String dir = grailsApplication.config.glo.probeTestDirectory
        String syncType = "PROBETEST|" + tkey
        String syncTypeNew = "PROBETESTNEW|" + tkey

        if (tkey in [
                "nw_ito_dot_test",
                "ito_dot_test"
        ]) {
            dir = grailsApplication.config.glo.itoProbeTestDirectory
        }
        if (tkey in ["fa_test"]) {
            dir = grailsApplication.config.glo.faTestDirectory
        }
        if (!dir) {
            throw new RuntimeException("Probe, ITO or FA test sync directory not specified")
        }

        // Determine which units contain folder to be synched
        def db = mongo.getDB("glo")
        def unitsToBeSynched = [:]
        def unitsToBeSynchedNew = [:]
        units.data.each {

            File f = new File(dir + it.code)
            String isSync = it["probeTestSynced"] != null ? it["probeTestSynced"].trim() : ""
            if (f.exists() && it.tkey == tkey && isSync == "SYNC") {
                unitsToBeSynched.put(f, it)
            }
            if (isSync == "SYNC") {
                db.unit.update(new BasicDBObject("code", it.code), new BasicDBObject('$set', new BasicDBObject("probeTestSynced", "---")), false, true)
                unitsToBeSynchedNew.put(it, '')
            }
        }

        basicDataSyncService.init(syncType, unitsToBeSynched)
        basicDataSyncService.init(syncTypeNew, unitsToBeSynchedNew)
        unitsToBeSynchedNew.size()
    }


    private def ni_dot_spc_test (def tkey, def units, def variables) {

        String dir = grailsApplication.config.glo.probeTestDirectory
        def db = mongo.getDB("glo")
        String syncType = "PROBETEST|ni_dot_test"
        def unitsToBeSynched = [:]
        units.data.each {

            // Start instance of unit
            def recv = new Expando()
            def product = Product.findByCode("niDotSpcData")
            recv.pid = product.id
            recv.cid = product.productCompanies.toArray()[0].id
            recv.uid = "admin"
            recv.units = []

            def m = [:]
            m.put('code', '')
            m.put('qty', 1)
            m.put('waferCode', it.code)
            m.put('experimentId', it.experimentId)
            m.put('mask', it.mask)

            recv.units.add(m)
            def code = unitService.start (recv, "Rel", "ni_dot_test" )
            def unit = db.unit.find([code:code]).collect{it}[0]
            File f = new File(dir + "/" + it.code)
            if (f.exists()) {
                unitsToBeSynched.put(f, unit)
            }
        }
        basicDataSyncService.init(syncType, unitsToBeSynched)
        unitsToBeSynched.size()
    }

    // Board reliability test dat sync /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private def test_rel_board(def tkey, def units, def variables) {

        String dir = grailsApplication.config.glo.boardRelTestDirectory
        if (!dir) {
            throw new RuntimeException("Board Rel test sync directory not specified")
        }

        if (tkey == "test_rel_board") {
            dir = dir + "\\REL"
        } else {
            dir = dir + "\\STD"
        }

        // Determine which units contain folder to be synched
        def db = mongo.getDB("glo")
        def unitsToBeSynched = new TreeMap()
        def isUnit = [:]
        def dirs = []
        def cnt = 0

        // Loop through all the files in sub directories
        File f = new File(dir)
        if (f.exists()) {

            f.eachDir {

                def dirNameArray = it.name.tokenize("_")
                def testType = dirNameArray[0]

                if (dirNameArray.size() == 5 && testType in ["STD", "REL"]) {

                    if (dateCheckerService.modified("BOARDREL", it)) {

                        String boardId = dirNameArray[1]
                        def index = dirNameArray[2]
                        def hours = dirNameArray[3]
                        def testDate = dirNameArray[4]

                        cnt++

                        if (testType == "STD") {
                            hours = dirNameArray[4] + "_" + relSyncService.padZeros(dirNameArray[3])
                        }

                        it.eachFile { file ->
                            def fileNameArray = file.name.tokenize("_")
                            if (fileNameArray.size() >= 4 &&
                                    (fileNameArray[1].toUpperCase() == "SPECTRUM" || fileNameArray[1].toUpperCase() == "VOLTAGE")) {

                                // Find unit based on board position
                                def query = new BasicDBObject("tkey", tkey)
                                query.put("boardId", boardId)
                                query.put("posOnBoard", fileNameArray[0])

                                def unit
                                if (isUnit.containsKey(boardId + "_" + fileNameArray[0])) {
                                    unit = isUnit[boardId + "_" + fileNameArray[0]]
                                } else {
                                    unit = db.unit.find(query, new BasicDBObject()).collect { it }[0]
                                    isUnit.put(boardId + "_" + fileNameArray[0], unit)
                                }
                                if (!unit) {
                                    if (testType == "REL") {
                                        throw new RuntimeException("Device is not mapped in MES for board " + boardId + " and position " + fileNameArray[0])
                                    }
                                } else {

                                    if (!unitsToBeSynched.containsKey(unit.code)) {
                                        unitsToBeSynched.put(unit.code, new TreeMap())
                                    }

                                    if (!unitsToBeSynched[unit.code].containsKey(hours)) {
                                        unitsToBeSynched[unit.code].put(hours, [])
                                    }

                                    def obj = [:]
                                    obj.dataType = fileNameArray[1].toUpperCase()
                                    obj.unit = unit
                                    obj.boardId = boardId
                                    obj.posOnBoard = fileNameArray[0]
                                    obj.index = index
                                    obj.testDate = testDate
                                    obj.hours = hours
                                    obj.testType = testType
                                    obj.file = file

                                    unitsToBeSynched[unit.code][obj.hours].add(obj)
                                }
                            }
                        }
                    }
                }
            }
        }

        basicDataSyncService.init("RELBOARDTEST", [unitsToBeSynched, dirs])

        cnt
    }

    // Wafer reliability test dat sync /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private def test_rel_wafer(def tkey, def units, def variables) {

        String dir = grailsApplication.config.glo.waferRelTestDirectory
        if (!dir) {
            throw new RuntimeException("Board Rel test sync directory not specified")
        }

        // Determine which units contain folder to be synched
        def db = mongo.getDB("glo")
        def unitsToBeSynched = new TreeMap()
        def cnt = 0
        def dirs = []

        units.data.each { unit ->

            String isSync = unit["relWaferSynced"]
            Boolean isGoingToSync = false

            if (unit["mask"] && unit["experimentId"] && unit["burningPosition"] && isSync == "SYNC") {

                def folder = dir + "\\" + unit["mask"] + "\\" + (unit["experimentId"] ?: "EXPERIMENT_NOT_DEFINED")

                File f = new File(folder)

                if (f.exists()) {

                    f.eachDir {

                        if (dateCheckerService.modified("WAFERREL", it)) {

                            isGoingToSync == true
                            cnt++

                            def dirNameArray = it.name.tokenize("_")
                            if (dirNameArray.size() == 2) {

                                def waferCode = dirNameArray[0]
                                if (dirNameArray[1].toUpperCase().indexOf("HRS") > 0) {

                                    def hours = dirNameArray[1].toUpperCase().replace("HRS", "")

                                    def dirloc = it.listFiles([accept: { file -> file ==~ /.*?\.txt/ }] as FileFilter)?.toList()
                                    def sortedFileList = new TreeMap()
                                    dirloc.each {
                                        def arr = it.getName().tokenize("_")
                                        if (arr.size() == 6 && (arr[1].toUpperCase() == "SPECTRUM" || arr[1].toUpperCase() == "VOLTAGE")) {
                                            def cd = arr[3].substring(0, 2) + arr[3].substring(4, 6) + arr[3].substring(2, 4)
                                            sortedFileList.put(arr[0] + "_" + arr[1] + "_" + arr[2] + "_" + cd + "_" + arr[4] + "_" + arr[5], it)
                                        }
                                    }

                                    def validFiles = new TreeMap()
                                    sortedFileList.reverseEach { key22, file ->

                                        def nameAsList = key22.tokenize("_")
                                        if (nameAsList.size >= 3 && !validFiles.containsKey(nameAsList[0] + nameAsList[1] + nameAsList[2])) {
                                            validFiles.put(nameAsList[0] + nameAsList[1] + nameAsList[2], [
                                                    file,
                                                    relSyncService.padZeros(hours)
                                            ])
                                        }
                                    }

                                    validFiles.each { key, lst ->

                                        def hoursSorted = lst[1]
                                        def file = lst[0]

                                        def fileNameArray = file.name.tokenize("_")
                                        if (fileNameArray.size >= 2) {
                                            def deviceCode = waferCode + "_" + fileNameArray[0]

                                            if (!unitsToBeSynched.containsKey(deviceCode)) {
                                                unitsToBeSynched.put(deviceCode, new TreeMap())
                                            }

                                            if (!unitsToBeSynched[deviceCode].containsKey(hoursSorted)) {
                                                unitsToBeSynched[deviceCode].put(hoursSorted, [])
                                            }

                                            def obj = [:]
                                            obj.put('dataType', fileNameArray[1].toUpperCase())
                                            if (fileNameArray[1].toUpperCase() == "SPECTRUM") {
                                                obj.put('current', fileNameArray[2].toFloat())
                                            }
                                            obj.put('unit', unit)
                                            obj.put('deviceCode', deviceCode)
                                            obj.put('hours', hoursSorted)
                                            obj.put('file', file)

                                            unitsToBeSynched[deviceCode][hoursSorted].add(obj)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (isGoingToSync) {
                db.unit.update(new BasicDBObject("code", it.code), new BasicDBObject('$set', new BasicDBObject("relWaferSynced", "---")), false, true)
            }
        }

        if (unitsToBeSynched.size() > 0) {
            basicDataSyncService.init("RELWAFERTEST", [unitsToBeSynched, dirs])
        }

        cnt
    }

    // Die lamp board test data sync /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    private def test_lamp_board(def tkey, def units, def variables) {

        String dir = grailsApplication.config.glo.boardLampTestDirectory
        if (!dir) {
            throw new RuntimeException("Board Lamp test sync directory not specified")
        }

        // Determine which units contain folder to be synched
        def db = mongo.getDB("glo")
        def unitsToBeSynched = new TreeMap()
        def isUnit = [:]
        def dirs = []
        def cnt = 0

        // Loop through all the files in sub directories
        File f = new File(dir)
        if (f.exists()) {
            f.eachDir { it ->
                def dirNameArray = it.name.tokenize("_")
                if (dirNameArray.size() == 4) {

                    if (dateCheckerService.modified("BOARDLAMP", it)) {

                        cnt++

                        def expId = dirNameArray[0]
                        def waferId = dirNameArray[1]
                        def testType = dirNameArray[2] == "H" ? "Header" : "Encap"

                        it.eachFile { file ->
                            def fileNameArray = file.name.tokenize("_")
                            if (fileNameArray.size() >= 4 &&
                                    (fileNameArray[1].toUpperCase() == "SPECTRUM" || fileNameArray[1].toUpperCase() == "VOLTAGE")) {

                                def deviceId = waferId + "_" + fileNameArray[0].replace("Dev", "")

                                def unit
                                if (isUnit.containsKey(deviceId)) {
                                    unit = isUnit[deviceId]
                                } else {
                                    def query = new BasicDBObject("code", deviceId)
                                    unit = db.unit.find(query, new BasicDBObject()).collect { it }[0]
                                    isUnit.put(deviceId, unit)
                                }
                                if (!unit) {
                                    throw new RuntimeException("Device " + deviceId + " is not defined in MES")
                                } else {
                                    if (!unitsToBeSynched.containsKey(unit.code)) {
                                        unitsToBeSynched.put(unit.code, new TreeMap())
                                    }

                                    if (!unitsToBeSynched[unit.code].containsKey(testType)) {
                                        unitsToBeSynched[unit.code].put(testType, [])
                                    }

                                    def obj = [:]
                                    obj.dataType = fileNameArray[1].toUpperCase()
                                    obj.unit = unit
                                    obj.hours = testType
                                    obj.file = file

                                    unitsToBeSynched[unit.code][obj.hours].add(obj)
                                }
                            }
                        }
                    }
                }
            }
        }

        basicDataSyncService.init("LAMPBOARDTEST", [unitsToBeSynched, dirs, tkey])

        cnt
    }

    // Individual style testing /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private def test_rel(def tkey, def units, def variables) {

        String dir = grailsApplication.config.glo.relTestDirectory
        if (!dir) {
            throw new RuntimeException("Rel test sync directory not specified")
        }

        // Determine which units contain folder to be synched
        def db = mongo.getDB("glo")
        def unitsToBeSynched = new TreeMap()
        def dirs = []

        // Loop through all the files in sub directories
        File f = new File(dir)
        f.traverse {
            if (it.isFile()) {
                def fileNameArray = it.name.tokenize("_")
                if (fileNameArray.size() >= 4) {
                    def extension = fileNameArray[3].substring(fileNameArray[3].indexOf(".") + 1)
                    if (extension?.toUpperCase() == "ISD") {
                        def obj = [:]
                        obj.deviceCode = fileNameArray[0] + "_" + fileNameArray[1]
                        obj.index = fileNameArray[3].substring(0, fileNameArray[3].indexOf("."))
                        obj.hours = fileNameArray[2].substring(0, fileNameArray[2].indexOf("HRS")).toInteger()
                        obj.file = it

                        if (!unitsToBeSynched.containsKey(obj.deviceCode)) {
                            unitsToBeSynched.put(obj.deviceCode, new TreeMap())
                            db.unit.update(new BasicDBObject("code", obj.deviceCode), new BasicDBObject('$set', new BasicDBObject("relTestSynced", "---")), false, true)
                        }
                        if (!unitsToBeSynched[obj.deviceCode].containsKey(obj.hours)) {
                            unitsToBeSynched[obj.deviceCode].put(obj.hours, [])
                        }
                        unitsToBeSynched[obj.deviceCode][obj.hours].add(obj)
                    }
                }
            }
        }
        f.eachDir { dirs.add(it) }


        basicDataSyncService.init("RELTEST", [unitsToBeSynched, dirs])

        unitsToBeSynched.size()
    }

    private def test_lamp(def tkey, def units, def variables) {

        String dir = grailsApplication.config.glo.lampTestDirectory
        if (!dir) {
            throw new RuntimeException("Lamp test sync directory not specified")
        }

        // Determine which units contain folder to be synched
        def db = mongo.getDB("glo")
        def unitsToBeSynched = new TreeMap()
        def dirs = []

        // Loop through all the files in sub directories
        File f = new File(dir)
        f.traverse {
            if (it.isFile()) {
                def fileNameArray = it.name.tokenize("_")
                if (fileNameArray.size() >= 2) {
                    def extension = fileNameArray[1].substring(fileNameArray[1].indexOf(".") + 1)
                    if (extension?.toUpperCase() == "ISD") {
                        def obj = [:]
                        def dirArray = it.parent.tokenize("\\")
                        if (dirArray.size() >= 2) {
                            obj.deviceCode = dirArray[dirArray.size() - 2] + "_" + fileNameArray[0].substring(3)
                            obj.index = fileNameArray[1].substring(0, fileNameArray[1].indexOf("."))
                            obj.lampTest = dirArray[dirArray.size() - 1]
                            obj.file = it

                            if (!unitsToBeSynched.containsKey(obj.deviceCode)) {
                                unitsToBeSynched.put(obj.deviceCode, new TreeMap())
                                db.unit.update(new BasicDBObject("code", obj.deviceCode), new BasicDBObject('$set', new BasicDBObject("lampTestSynced", "---")), false, true)
                            }
                            if (!unitsToBeSynched[obj.deviceCode].containsKey(obj.lampTest)) {
                                unitsToBeSynched[obj.deviceCode].put(obj.lampTest, [])
                            }
                            unitsToBeSynched[obj.deviceCode][obj.lampTest].add(obj)
                        }
                    }
                }
            }
        }


        basicDataSyncService.init("LAMPTEST", [unitsToBeSynched, dirs, tkey])

        unitsToBeSynched.size()
    }

    private def ngan_inspection(def units, def variables) {

        int ret = 0
        def db = mongo.getDB("glo")
        // Get directory
        String dir = grailsApplication.config.glo.bufferGrowthDirectory
        if (!dir) {
            throw new RuntimeException("Buffer growth file directory not specified")
        }

        def groupRunNumber = units.data.groupBy { it.runNumber1 }
        groupRunNumber.each { k, v ->
            def grp, line, val
            def output = [:]

            def dirloc = new File(dir + k).listFiles([accept: { file -> file ==~ /.*?\.txt/ }] as FileFilter)?.toList()
            dirloc.each { file ->

                try {
                    output.clear()
                    grp = ""

                    BufferedReader br = new BufferedReader(new FileReader(file))
                    while ((line = br.readLine()) != null) {
                        line = line + " "
                        def row = line.split(':')
                        if (row.length == 2) {
                            if (!row[1]?.trim() && row[0]?.trim() != "Description" && row[0]?.trim() != "Operator") {
                                grp = row[0]?.trim()
                                val = ""
                            } else {
                                val = row[1]?.trim()
                            }
                            output.put("tsm" + cap((grp ?: "") + " " + row[0]), val)
                        }
                    }

                    // Create data collection structure
                    def bdo = new BasicDBObject()
                    variables.each {
                        if (output.containsKey(it.name)) {
                            def value = output[it.name]
                            if (it.dataType == "int" || it.dataType == "float") {
                                def row = value.split(' ')
                                if (row.length >= 2) {
                                    value = (it.dataType == "int") ? row[0].toLong() : ""
                                    value = (it.dataType == "float") ? row[0].toFloat() : ""
                                }
                            }

                            bdo.put(it.name, value)
                        }
                    }

                    def wid = output["tsmWaferId"]?.trim()
                    if (wid?.size() >= 10) {

                        def fields = new BasicDBObject()
                        def query = new BasicDBObject()
                        def unit

                        def tokens = wid.tokenize("_")
                        if (tokens.size() >= 2) {
                            def unitId = tokens[1]
                            def scribeId = unitId
                            if (tokens.size() >= 3) {
                                scribeId = tokens[1] + "_" + tokens[2]
                            }
                            query.put('$or', [
                                    new BasicDBObject("code", unitId),
                                    new BasicDBObject("code", scribeId)
                            ])
                            def temp = db.unit.find(query, fields)
                            unit = temp.collect { it }
                        }

                        if (!unit) {
                            query.clear()
                            query.put("runNumber1", wid.substring(0, 6))
                            query.put("satellite", wid.substring(7, 8))
                            query.put("pocket", wid.substring(9, 10).isLong() ? wid.substring(9, 10).toLong() : -1)
                            def temp = db.unit.find(query, fields)
                            unit = temp.collect { it }
                        }

                        if (unit) {
                            bdo.put("id", unit["_id"][0])
                            bdo.put("runNumber1", wid.substring(0, 6))
                            unitService.update(bdo, "admin", false)
                            ret += 1
                        }
                    } else {
                        logr.error("EPI buffer Wafer ID " + wid + " is not valid")
                    }
                }
                catch (Exception exc) {
                    logr.error(file.path + ": " + exc)
                }
            }
        }

        ret
    }

    private def pl(def units, def variables) {

        int ret = 0
        def db = mongo.getDB("glo")
        // Get directory
        String dir = grailsApplication.config.glo.bufferGrowthDirectory
        if (!dir) {
            throw new RuntimeException("LED growth file directory not specified")
        }

        def groupRunNumber = units.data.groupBy { it.runNumber }

        // Process SPC for PL on PLREF
        groupRunNumber.keySet().each { k ->

            groupRunNumber[k].add(plRef(k))
        }

        groupRunNumber.each { k, v ->

            def grp, line, val

            def unitFiles = [:]
            def unitData = [:]
            def dirloc = new File(dir + k).listFiles([accept: { file -> file ==~ /.*?\.txt/ }] as FileFilter)?.toList()

            v.each { unit ->

                def code = "PLREF"
                if (unit.code.indexOf("PLSPC") == -1) {
                    code = unit.code
                }

                if (!unitData.containsKey(code)) {

                    unitFiles.put(code, [])
                    unitData.put(code, unit)
                }
            }

            dirloc.each { file ->

                def fileName = file.path.substring(file.path.lastIndexOf("\\") + 1)
                if (fileName.indexOf("PLREF") >= 0) {
                    if (unitFiles["PLREF"] != null) unitFiles["PLREF"].add(file)
                } else {
                    def tokens = fileName.tokenize("_")
                    if (tokens.size() >= 2 && unitFiles[tokens[1]] != null) {
                        unitFiles[tokens[1]].add(file)
                    }
                }
            }

            unitFiles.each { code, files ->

                for (file in files) {
                    try {
                        def fileName = file.path.substring(file.path.lastIndexOf("\\") + 1)
                        def suffix = ""
                        def output = [:]
                        grp = ""

                        if (fileName.indexOf("_spm") > 0)
                            suffix = "spm"
                        if (fileName.indexOf("_01_spm") > 0)
                            suffix = "spm"
                        else if (fileName.indexOf("_01_spl") > 0)
                            suffix = "spl"
                        else if (fileName.indexOf("_02_spl") > 0)
                            suffix = "spl"
                        else if (fileName.indexOf("_03_spl") > 0)
                            suffix = "spl"
                        else if (fileName.indexOf("_04_spl") > 0)
                            suffix = "spl"
                        else if (fileName.indexOf("_05_spl") > 0)
                            suffix = "spl"
                        else if (fileName.indexOf("_06_spl") > 0)
                            suffix = "spl"
                        else if (fileName.indexOf("_07_spl") > 0)
                            suffix = "spl"
                        else if (fileName.indexOf("_08_spl") > 0)
                            suffix = "spl"

                        BufferedReader br = new BufferedReader(new FileReader(file))
                        while ((line = br.readLine()) != null) {
                            line = line + " "
                            def row = line.split(':')
                            if (row.length == 2) {
                                if (!row[1]?.trim() && row[0]?.trim() != "Description" && row[0]?.trim() != "Operator") {
                                    grp = row[0]?.trim()
                                    val = ""
                                } else {
                                    val = row[1]?.trim()
                                }
                                output.put(cap((grp ?: "") + " " + row[0]), val)
                            }
                            if (row[0]?.trim() == "STATISTICS")
                                break;
                        }

                        def laserPower
                        if (suffix == "spm") {
                            def pwr = output["LaserParametersPower"]?.replace(" mW", "")?.toDouble()
                            if (pwr >= 3.2 && pwr <= 4.8 && output["LaserParametersWavelength"]?.indexOf("405") >= 0)
                                suffix = "405"
                        } else if (suffix == "spl") {
                            def pwr = output["LaserParametersPower"]?.replace(" mW", "")?.toDouble()
                            suffix = pwr.round(0).toInteger() + "mW"
                            laserPower = pwr
                        } else {
                            suffix = ""
                        }


                        System.println(suffix);

                        if (suffix in ["405", "1mW", "2mW", "4mW", "25mW"]) {

                            def unit = unitData[code]
                            if (unit) {

                                def bdo2 = new BasicDBObject()
                                bdo2.put("id", unit["_id"])
                                if (unit.productCode == "100") {
                                    bdo2.put("processCategory", "nwLED")
                                    bdo2.put("processKey", "epi")
                                    bdo2.put("taskKey", "pl")
                                } else {
                                    bdo2.put("processCategory", "Rel")
                                    bdo2.put("processKey", "pl_spc")
                                    bdo2.put("taskKey", "pl_spc_data")
                                }
                                bdo2.put("runNumber", unit.runNumber)

                                if (suffix == "405") {

                                    def lineStatsHeader = br.readLine() + "                          "
                                    lineStatsHeader = lineStatsHeader.replace('+', ' P')
                                    lineStatsHeader = lineStatsHeader.replace('-', ' M')
                                    def columns = [14, 14, 14, 15]
                                    def pos = 15
                                    def headers = []
                                    columns.each {
                                        headers.add(cap(lineStatsHeader.substring(pos, pos + it)))
                                        pos += it
                                    }

                                    grp = "Statistics"
                                    def output2 = [:]
                                    while ((line = br.readLine()) != null) {
                                        line = line + " "
                                        def row = line.split(':')
                                        if (row.length == 2) {
                                            if (!row[1]?.trim()) {
                                                grp = row[0]?.trim()
                                                val = ""
                                            } else {
                                                val = row[1] + "                                    "
                                            }
                                            if (val.size() > 50) {
                                                pos = 2
                                                def arr = []
                                                columns.each {
                                                    arr.add(val.substring(pos, pos + it))
                                                    pos += it
                                                }
                                                output2.put(cap((grp ?: "") + " " + row[0]), arr)
                                            }
                                        }
                                    }

                                    def p = 0
                                    headers.each {
                                        def arr2 = [:]
                                        output2.each { key, value ->
                                            def val1 = value[p]?.trim()?.split(' ')
                                            if (val1.length >= 2) {
                                                arr2.put(key, val1[0].toFloat())
                                            } else {
                                                arr2.put(key, value[p]?.trim())
                                            }
                                        }
                                        output.put(it, arr2)
                                        p++
                                    }

                                    // Create data collection structure
                                    def bdo = new BasicDBObject()
                                    output.each { k1, v1 ->
                                        def value
                                        def row
                                        if (v1 instanceof java.util.Map) {
                                            value = new BasicDBObject()
                                            v1.each { k2, v2 ->
                                                value.put(k2, v2)
                                            }
                                        } else {
                                            row = v1.split(' ')
                                            if (row.length >= 2 && k1 != "LaserParametersName") {
                                                value = (row[0].isLong()) ? row[0].toLong() : ""
                                                value = (row[0].isFloat()) ? row[0].toFloat() : ""
                                            } else {
                                                value = v1.trim()
                                            }
                                        }

                                        bdo.put(k1, value)
                                    }


                                    bdo2.put("pl" + suffix, bdo)
                                    bdo2.put("peakLambdaAvg" + suffix, bdo.PeakLambda.StatisticsAverage)
                                    bdo2.put("peakLambdaStdDev" + suffix, bdo.PeakLambda.StatisticsStdDev)
                                    bdo2.put("peakIntAvg" + suffix, bdo.PeakInt.StatisticsAverage)
                                    bdo2.put("peakIntMax" + suffix, bdo.PeakInt.StatisticsMax)
                                    bdo2.put("peakIntMin" + suffix, bdo.PeakInt.StatisticsMin)
                                    bdo2.put("peakIntStdDev" + suffix, bdo.PeakInt.StatisticsStdDev)
                                    bdo2.put("fwhmAvg" + suffix, bdo.Fwhm.StatisticsAverage)
                                    bdo2.put("fwhmStdDev" + suffix, bdo.Fwhm.StatisticsStdDev)
                                    bdo2.put("laserName" + suffix, bdo.LaserParametersName)
                                }

                                if (suffix in ["1mW", "2mW", "4mW", "25mW"]) {

                                    br.readLine()
                                    br.readLine()
                                    def lineSpectrumData = br.readLine()

                                    def peak1 = lineSpectrumData.substring(31, 40)
                                    def peak2 = lineSpectrumData.substring(41, 49)
                                    def diff = lineSpectrumData.substring(50, 59)
                                    def height = lineSpectrumData.substring(60, 69)
                                    def fwhm = lineSpectrumData.substring(70, 77)

                                    peak1?.isFloat() ? bdo2.put("specLine_peak1_" + suffix, peak1.toFloat()) : ''
                                    peak2?.isFloat() ? bdo2.put("specLine_peak2_" + suffix, peak2.toFloat()) : ''
                                    diff?.isFloat() ? bdo2.put("specLine_diff_" + suffix, diff.toFloat()) : ''
                                    height?.isFloat() ? bdo2.put("specLine_height_" + suffix, height.toFloat()) : ''
                                    fwhm?.isFloat() ? bdo2.put("specLine_fwhm_" + suffix, fwhm.toFloat()) : ''
                                    laserPower ? bdo2.put("specLine_laserPower_" + suffix, laserPower) : ''

                                    def height2 = getSecondPeakIntensity(file.path.replace(".txt", ".dat"), peak2?.trim())
                                    height2?.isFloat() ? bdo2.put("specLine_height2_" + suffix, height2.toFloat()) : ''
                                }

                                // For PL recalculation disable SPC wafers and also comment plRef call above
                                //         if (bdo2["processCategory"] != "Rel") {
                                unitService.update(bdo2, "admin", true)
                                ret += 1
                                //        }
                            }
                        }
                    }
                    catch (Exception exc) {

                        logr.error(file.path + ": " + exc)
                    }
                }
            }
        }

        ret
    }


    def plRef(def runNumber) {

        def db = mongo.getDB("glo")

        // Start instance of unit
        def recv = new Expando()
        def product = Product.findByCode("plSpcData")
        recv.pid = product.id
        recv.cid = product.productCompanies.toArray()[0].id
        recv.uid = "admin"
        recv.units = []

        def m = [:]
        m.put('code', '')
        m.put('qty', 1)
        m.put('waferCode', "PLREF")
        m.put('experimentId', runNumber)
        m.put('runNumber', runNumber)
        recv.units.add(m)

        def code = unitService.start(recv, "Rel", "pl_spc")

        def unit = db.unit.find(new BasicDBObject("code", code), [:]).collect { it }[0]

        unit
    }


    private def getSecondPeakIntensity(def fileName, def peak2) {

        def ret = ""
        def line

        def file = new File(fileName)
        if (file.exists()) {

            BufferedReader br = new BufferedReader(new FileReader(file))
            while ((line = br.readLine()) != null) {

                def row = line.split(',')
                if (row.length == 3) {

                    if (row[0]?.trim() == peak2) {

                        ret = row[1]?.trim()
                        break
                    }
                }
            }
        }

        ret
    }

    private def sem(def units, def variables) {

        int ret = 0
        def db = mongo.getDB("glo")
        // Get directory
        String dir = grailsApplication.config.glo.semDirectory
        if (!dir) {
            throw new RuntimeException("Post inventory led growth file directory not specified")
        }

        def groupRunNumber = units.data.groupBy { it.runNumber }
        groupRunNumber.each { k, v ->
            def grp, line, val
            def output = [:]

            def dirloc = new File(dir + "\\" + k).listFiles([accept: { file -> file ==~ /.*?\.txt/ }] as FileFilter)?.toList()
            dirloc.each { file ->

                try {
                    def fileName = file.path.substring(file.path.lastIndexOf("\\") + 1)

                    output.clear()

                    BufferedReader br = new BufferedReader(new FileReader(file))
                    while ((line = br.readLine()) != null) {
                        def row = line.split(':')
                        if (row.length == 2) {
                            output.put(row[0]?.trim(), row[1]?.trim())
                        }
                    }

                    // Create data collection structure
                    def bdo = new BasicDBObject()
                    variables.each {
                        if (output.containsKey(it.name)) {
                            def value = output[it.name]
                            def value2
                            if (it.dataType == "int" || it.dataType == "float") {
                                value2 = (it.dataType == "int") ? value.toLong() : ""
                                value2 = (it.dataType == "float") ? value.toFloat() : ""
                            } else {
                                value2 = value
                            }
                            bdo.put(it.name, value2)
                        }
                    }

                    def query = new BasicDBObject();
                    query.put("code", fileName.substring(0, fileName.lastIndexOf(".")))
                    def temp = db.unit.find(query, new BasicDBObject())
                    def unit = temp.collect { it }
                    if (unit) {
                        bdo.put("id", unit["_id"][0])
                        bdo.put("runNumber", k)
                        unitService.update(bdo, "admin", false)
                        ret++
                    }

                }
                catch (Exception exc) {
                    logr.error(file.path + ": " + exc)
                }
            }
        }

        ret
    }


    private def cap(str) {

        String ret = "";
        if (str) {
            str = str.replaceAll("[.-]", " ");
            def lst = str.split(' ')
            lst.each {
                if (it.trim()) {
                    ret = ret + it.trim().toLowerCase().capitalize()
                }
            }
        }
        ret
    }

}
