package com.glo

import java.text.DateFormat
import java.text.SimpleDateFormat

import org.apache.commons.io.IOUtils
import org.apache.commons.logging.LogFactory
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics
import org.apache.commons.validator.GenericValidator
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.grails.plugins.excelimport.*

import com.glo.ndo.*
import com.mongodb.BasicDBObject


class ImportEquipmentDCJob {

    def concurrent = false
    def unitService
    def sequenceGeneratorService
    def quartzScheduler
    def grailsApplication
    def persistenceInterceptor
    def importService
    def mongo

    private static final logr = LogFactory.getLog(this)

    static triggers = {
        cron name: 'cronTriggerEquipmentData', cronExpression: "0 0 3 ? * MON-SUN"
    }

    static Map CONFIG_COLUMN_MAP = [
            sheet    : 'Sheet1',
            startRow : 1,
            columnMap: [
                    'A': 'time',
                    'B': 'temperature',
                    'C': 'humidity'
            ]
    ]

    def execute() {

        if (grailsApplication.config.glo.tomcatServer == "calserver04") {

            def db = mongo.getDB("glo")

            try {
                omegaSensor(db)
            }
            catch (Exception exc) {
                logr.error(exc.getMessage())
            }
            try {
                eBeamData(db)
            }
            catch (Exception exc) {
                logr.error(exc.getMessage())
            }
            try {
                eBeamItoData(db)
            }
            catch (Exception exc) {
                logr.error(exc.getMessage())
            }
            try {
                tempHumidity(db)
            }
            catch (Exception exc) {
                logr.error(exc.getMessage())
            }

            try {
                tigerOptics(db)
            }
            catch (Exception exc) {
                logr.error(exc.getMessage())
            }


        }
    }

    def tigerOptics(def db) {

        persistenceInterceptor.init()

        String dir = grailsApplication.config.glo.tigerOpticsDirectory
        if (!dir) {
            logr.error("Tiger optics sync directory not specified.")
            return
        }

        File f = new File(dir)
        if (!f.exists()) {
            logr.error("Tiger optics directory '" + dir + "' does not exist.")
            return
        }

        def files = f.listFiles([accept: { file -> file ==~ /.*?\.txt/ }] as FileFilter)?.toList()

        if (!files) {
            files = f.listFiles([accept: { file -> file ==~ /.*?\.TXT/ }] as FileFilter)?.toList()
        }


        files.each { file ->
            FileReader fr = null
            BufferedReader br = null
            try {
                fr = new FileReader(file)
                br = new BufferedReader(fr)

                def varName = file.getName().replace(".txt", "").toLowerCase()

                def bdo = new BasicDBObject()
                bdo.put("pctg", "DC")
                bdo.put("tkey", "tiger_optics")
                bdo.put(varName, new BasicDBObject('$exists', 1))
                def unit = db.unit.find(bdo, new BasicDBObject()).addSpecial('$orderby', new BasicDBObject("_id", -1)).limit(1).collect {
                    it
                }[0]
                int lastHour = 0
                def lastDate = "2011-01-01"
                if (unit) {
                    lastHour = unit["hourOfDay"]
                    lastDate = unit["actualStart"].format("yyyy-MM-dd")
                }

                def resMap = [:]

                def line

                while ((line = br.readLine()) != null) {
                    if (line?.trim() != "") {
                        def row = line.split('\t')
                        if (row.length == 2) {
                            def v1 = row[0]?.trim()
                            def v2 = row[1]?.trim()
                            if (v1 && v2) {
                                def dt = v1.tokenize(' ')
                                if (v2.isFloat()) {
                                    float vf = v2.toFloat()
                                    if (dt.size() == 2) {
                                        def dtt = dt[0].tokenize("-")
                                        def actualStart = "20" + dtt[2] + "-" + dtt[0] + "-" + dtt[1]
                                        def dthm = dt[1].tokenize(":")
                                        int dth = dthm[0].toInteger()
                                        int dtm = dthm[1].toInteger()

                                        if (actualStart + "_" + String.format("%02d", dth) > lastDate + "_" + String.format("%02d", lastHour)) {
                                            if (!resMap.containsKey([actualStart, dth])) {
                                                resMap.put([actualStart, dth], [])
                                            }
                                            resMap[[actualStart, dth]].add([dtm, vf])
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                def recv = new Expando()
                def product = Product.findByCode("equipmentData")
                recv.pid = product.id
                recv.cid = product.productCompanies.toArray()[0].id
                recv.uid = "admin"

                resMap.each {

                    DescriptiveStatistics stats = new DescriptiveStatistics()
                    def values = []
                    it.value.each { val ->
                        stats.addValue(val[1])
                        values.add(new BasicDBObject(val[0].toString(), val[1]))
                    }

                    if (GenericValidator.isDate(it.key[0], "yyyy-MM-dd", true) && it.key[1] >= 0) {
                        recv.units = []
                        def m = [:]
                        m.put('code', '')
                        m.put('qty', 1)
                        m.put(varName, stats.getMean())
                        m.put(varName + "_max", stats.getMax())
                        m.put('hourOfDay', it.key[1])
                        m.put('actualStart', it.key[0])
                        m.put('valuePerMinute', values)
                        recv.units.add(m)
                        unitService.start(recv, "DC", "equipment")
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

        if (persistenceInterceptor) {
            persistenceInterceptor.flush()
            persistenceInterceptor.destroy()
        }
    }


    def eBeamData(db) {
        persistenceInterceptor.init()
        importService.eBeamData(db, grailsApplication.config.glo.eBeamDirectory)
        if (persistenceInterceptor) {
            persistenceInterceptor.flush()
            persistenceInterceptor.destroy()
        }
    }

    def eBeamItoData(db) {
        persistenceInterceptor.init()
        importService.eBeamItoData(db, grailsApplication.config.glo.eBeamItoDirectory)
        if (persistenceInterceptor) {
            persistenceInterceptor.flush()
            persistenceInterceptor.destroy()
        }
    }




    def omegaSensor(db) {

        persistenceInterceptor.init()

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
                bdo.put("value.tags", ['$in:'[
                        "EquipmentStatus",
                        "omega_sensor",
                        room]
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
                                                def id = 'OSS' + room + actst[0] + actst[1] + actst[2] + dth + dtm;
                                                def code = "OSS" + sequenceGeneratorService.next("omega_sensor").toString().padLeft(6, '0')
                                                dr.put("code", code)
                                                dr.put("_id", id)

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
                                                obj.put('dateStart', new Date().parse("yyyy-MM-dd HH:mm", actualStart + " " + dth + ":" + dtm))
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


    def tempHumidity(def db) {

        persistenceInterceptor.init()

        String dir = grailsApplication.config.glo.hvacDirectory
        if (!dir) {
            logr.error("HVAC sync directory not specified.")
            return
        }

        File f = new File(dir)
        if (!f.exists()) {
            logr.error("HVAC directory '" + dir + "' does not exist.")
            return
        }

        DateFormat df = new SimpleDateFormat("MM/dd/yy hh:mm:ss aa")

        def processes = Process.findAllByCategory("EquipmentDC")
        processes.each { process ->

            File d = new File(dir + "\\" + process.pkey)
            if (d.exists()) {

                def files = d.listFiles([accept: { file -> file ==~ /.*?\.xlsx/ }] as FileFilter)?.toList()
                files.each { file ->

                    def stream = new ByteArrayInputStream(file.bytes)
                    XSSFWorkbook workbook = new XSSFWorkbook(stream)
                    List rows = ExcelImportUtils.convertColumnMapConfigManyRows(workbook, CONFIG_COLUMN_MAP)
                    IOUtils.closeQuietly(stream);

                    rows.each {

                        def code = process.idPrefix + sequenceGeneratorService.next(process.idPrefix).toString()
                        def time = df.parse(it.time)

                        def dr = db.dataReport.find(new BasicDBObject("code", code))?.collect { it }[0]
                        if (!dr) {
                            dr = new BasicDBObject()
                            dr.put("code", code)
                            dr.put("parentCode", null)
                        }
                        def obj = new BasicDBObject()
                        obj.put("active", "true")
                        obj.put("time", time)
                        obj.put("actualStart", time)
                        obj.put("temperature", it.temperature)
                        obj.put("humidity", it.humidity)
                        obj.put("pkey", process.pkey)
                        dr.put("value", obj)

                        db.dataReport.save(dr)
                    }

                    File dirProcessed = new File(dir + "\\" + process.pkey + "\\processed");
                    File fproc = new File(dirProcessed, file.name)
                    if (fproc.exists())
                        fproc.delete()
                    boolean fileMoved = file.renameTo(new File(dirProcessed, file.name));
                }
            }
        }

        if (persistenceInterceptor) {
            persistenceInterceptor.flush()
            persistenceInterceptor.destroy()
        }
    }
}
