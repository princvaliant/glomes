package com.glo.custom

import com.mongodb.BasicDBObject
import org.apache.commons.io.IOUtils
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.grails.plugins.excelimport.ExcelImportUtils
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics

class ReadFileService {

    def mongo
    def unitService

    public def readTestFile(def file) {

        FileReader fr = null
        BufferedReader br = null
        def dbo = new BasicDBObject()
        try {
            fr = new FileReader(file)
            br = new BufferedReader(fr)

            def line
            while ((line = br.readLine()) != null) {
                if (line.length() >= 4 && line.substring(0, 4) == "Data")
                    break;
                def row = line.split('\t')
                if (row.length >= 2) {
                    def v1 = row[0]?.trim()
                    def v2 = row[1]?.trim()
                    if (!v2 && row.length == 3) {
                        v2 = row[2]?.trim()
                    }
                    if (v1 && v2) {
                        def variable = v1.replace("\n", " ").replace(".", "").replace("'", "")
                        def value = v2.isFloat() ? v2.toDouble() : v2
                        dbo.put(variable, value)
                    }
                }
            }
        } catch (Exception exc) {
            throw new RuntimeException(exc.getMessage())
        }
        finally {
            if (br != null)
                br.close()
            if (fr != null)
                fr.close()
            br = null
            fr = null
        }
        dbo
    }

    public def readTestFileCtlm(def file) {

        FileReader fr = null
        BufferedReader br = null
        def dbo = new BasicDBObject()
        try {
            fr = new FileReader(file)
            br = new BufferedReader(fr)
            def arrVolt = []
            def arrCurr = []
            def line
            def isData = false
            while ((line = br.readLine()) != null) {
                if (isData == true) {
                    def row = line.split('\t')
                    if (row.length == 2) {
                        def v1 = row[0]?.trim()
                        def v2 = row[1]?.trim()
                        if (v1 && v2 && v1.isFloat() && v2.isFloat()) {
                            def v1d = v1.toDouble()
                            arrVolt.add(v1d)
                            arrCurr.add(v2.toDouble())
                        } else {
                            dbo.put("first", v1)
                            dbo.put("second", v2)
                        }
                    }
                }
                if (line.length() >= 4 && line.substring(0, 4) == "Data") {
                    isData = true
                }
            }
            dbo.put("arrVolt", arrVolt)
            dbo.put("arrCurr", arrCurr)
        } catch (Exception exc) {
            throw new RuntimeException(exc.getMessage())
        }
        finally {
            if (br != null)
                br.close()
            if (fr != null)
                fr.close()
            br = null
            fr = null
        }

        dbo
    }


    public def readTestFileForGroup(def file, def grp) {

        FileReader fr = null
        BufferedReader br = null
        def dbo = new BasicDBObject()
        try {
            fr = new FileReader(file)
            br = new BufferedReader(fr)
            def arr = []
            def line
            def isData = false
            while ((line = br.readLine()) != null) {
                if (isData == true) {
                    def row = line.split('\t')

                    if (row.length == 2) {
                        def v1 = row[0]?.trim()
                        def v2 = row[1]?.trim()
                        if (v1 && v2 && v1.isFloat() && v2.isFloat()) {
                            def v1d = v1.toDouble()
                            if (grp != "Datavoltage" && v1d >= 400 && v1d <= 750) {
                                arr.add([v1d, v2.toDouble()])
                            } else if (grp == "Datavoltage") {
                                arr.add([v1d, v2.toDouble()])
                            }
                        } else {
                            dbo.put("first", v1)
                            dbo.put("second", v2)
                        }
                    }

                    if (row.length == 4) {
                        def v1 = row[0]?.trim()
                        def v2 = row[1]?.trim()
                        def v3 = row[2]?.trim()
                        def v4 = row[3]?.trim()
                        if (v1 && v2 && v3 && v4 && v1.isFloat() && v2.isFloat() && v3.isFloat() && v4.isFloat()) {
                            if (grp == "Datacurrent" && v1.toDouble() > 0) {
                                arr.add([v1.toDouble(), v2.toDouble(), v3.toDouble(), v4.toDouble()])
                            }
                        } else {
                            dbo.put("v1", v1)
                            dbo.put("v2", v2)
                            dbo.put("v3", v3)
                            dbo.put("v4", v4)
                        }
                    }

                }
                if (line.length() >= 4 && line.substring(0, 4) == "Data") {
                    isData = true
                }
            }
            dbo.put("data", arr)
        } catch (Exception exc) {
            throw new RuntimeException(exc.getMessage())
        }
        finally {
            if (br != null)
                br.close()
            if (fr != null)
                fr.close()
            br = null
            fr = null
        }

        dbo
    }

    //Bruno
    //Input: CDSEM's filepath + filename (.XLS file)
    //Reads file and parses data into 'filedata' structure
    //Return: 'filedata'
    public def readTestFileCDSEM(def file) {

        HashSet fileData = new HashSet()

        for ( int i = 1; i <= 12; i++) {

            Map CONFIG_BOOK_CELL_MAP = [
                    sheet: 'Slot ' + i,
                    cellMap: ['C5': 'MeasurementDate',
                            'C6': 'Operator',
                            'C7': 'Stamp',
                            'C8': 'Process',
                            'C10': 'WaferId',
                            'C11': 'Comment',
                            'F7': 'pos1',
                            'F8': 'pos2',
                            'F9': 'pos3',
                            'F10': 'pos4',
                            'F11': 'pos5',
                            'G7': 'od1',
                            'G8': 'od2',
                            'G9': 'od3',
                            'G10': 'od4',
                            'G11': 'od5',
                            'I7': 'id1',
                            'I8': 'id2',
                            'I9': 'id3',
                            'I10': 'id4',
                            'I11': 'id5'
                    ]
            ]

            try {
                def stream = new ByteArrayInputStream(file.bytes)
                HSSFWorkbook workbook = new HSSFWorkbook(stream)
                def data = ExcelImportUtils.cells(workbook, CONFIG_BOOK_CELL_MAP)
                if (data.Process) {
                    fileData.add(data)
                }
                IOUtils.closeQuietly(stream)
            } catch (IllegalArgumentException exc) {

            }
            catch (Exception exc) {
                throw new RuntimeException(exc.getMessage())
            }
        }

        fileData
    }


    public def processTestFileCDSEM(def fileData) {

        if (!fileData.WaferId)
            throw new RuntimeException("WaferID not specified")

        def errorStr = "Wafer " + fileData.WaferId + " "

        switch (fileData.Process.toLowerCase()) {
            case "dicd":
                fileData.taskKey = "dicd_meas"
                break
            case "post_etch_cd_sem_1":
                fileData.taskKey = "post_etch_cd_sem_1"
                break
            case "post_etch_cd_sem_2":
                fileData.taskKey =  "post_etch_cd_sem_2"
                break
            case "ficd":
                fileData.taskKey =  "ficd_meas"
                if (!fileData.Stamp)
                    throw new RuntimeException(errorStr + "MSR file: Stamp not specified")
                break
            default:
                throw new RuntimeException(errorStr + "MSR file: invalid process step " + fileData.Process.toLowerCase())
        }

        def posList = []
        if (fileData.pos1) posList.add([fileData.pos1, fileData.od1, fileData.id1])
        if (fileData.pos2) posList.add([fileData.pos2, fileData.od2, fileData.id2])
        if (fileData.pos3) posList.add([fileData.pos3, fileData.od3, fileData.id3])
        if (fileData.pos4) posList.add([fileData.pos4, fileData.od4, fileData.id4])
        if (fileData.pos5) posList.add([fileData.pos5, fileData.od5, fileData.id5])

        if (!(posList.size() in [3, 5])) {
            throw new RuntimeException(errorStr + "XLS file: Measurement data have no 3 or 5 positions")
        }

        float medianID = 0
        float medianOD = 0
        float innerAreaID = 0
        float innerAreaOD = 0

        posList.each {

            def weight = 0
            def innerWeight = 0

            switch (it[0] + "-" + posList.size()) {
                case "07,10-3":
                case "07,04-3":
                    weight = 0.44
                    innerWeight = 0.44
                    break
                case "07,07-3":
                    weight = 0.12
                    innerWeight = 0.12
                    break
                case "07,13-5":
                case "07,01-5":
                    weight = 0.2775
                    break
                case "07,10-5":
                case "07,04-5":
                    weight = 0.1665
                    innerWeight = 0.44
                    break
                case "07,07-5":
                    weight = 0.112
                    innerWeight = 0.12
                    break
            }

            medianOD += weight * (it[1] ?: 0)
            medianID += weight * (it[2] ?: 0)
            innerAreaOD += innerWeight * (it[1] ?: 0)
            innerAreaID += innerWeight * (it[2] ?: 0)
        }

        fileData.medianOD = medianOD.round(1)
        fileData.medianID = medianID.round(1)
        fileData.innerareaOD = innerAreaOD.round(1)
        fileData.innerareaID = innerAreaID.round(1)
    }


    def storeTestFileCDSEM(def fileName, def fileData, def admin) {

        def db = mongo.getDB("glo")

        HashMap map = new HashMap()
        map.put(fileData.WaferId, "YES")
        if (fileData.Comment) {
            fileData.Comment.tokenize(" ").each {
                map.put(it.trim(), "NO")
            }
        }

        map.each { code, synced ->
            def query1 = new BasicDBObject("code", code)
            def unit1 = db.unit.find(query1, new BasicDBObject()).collect { it }[0]
            if (unit1 && unit1?.tkey == fileData.taskKey) {
                if (unit1?.stampID?.trim() != fileData.Stamp) {
                    throw new RuntimeException("XLS file: Stamp ID for wafer " + fileData.WaferId + " does not match Stamp ID in MES")
                }
                if (!unitService.inStep(db, fileData.WaferId, "patterning", fileData.taskKey)) {
                    throw new RuntimeException("Wafer " + fileData.WaferId + " not in " + fileData.taskKey + " process step in MES")
                }
            } else {
                throw new RuntimeException("Wafer " + fileData.WaferId + " not in " + fileData.taskKey + " process step in MES")
            }

            def bdoUnit = new BasicDBObject()
            bdoUnit.put("id", unit1._id)
            bdoUnit.put("processCategory", unit1["pctg"])
            bdoUnit.put("processKey",unit1["pkey"])
            bdoUnit.put("taskKey", fileData.taskKey)

            switch (fileData.taskKey) {
                case "dicd_meas":
                    bdoUnit.put("did_via_size", fileData.medianID)
                    bdoUnit.put("od_via_size", fileData.medianOD)
                    bdoUnit.put("did_inner_area_via_size", fileData.innerareaID)
                    bdoUnit.put("dod_inner_area_via_size", fileData.innerareaOD)
                    bdoUnit.put("dicd_measurement_date", fileData.MeasurementDate)
                    bdoUnit.put("dicdViaFilename", fileName)
                    bdoUnit.put("dicd_measured", synced)
                    bdoUnit.put("dicdViaSizeRawData", fileData)
                    break

                case "post_etch_cd_sem_1":
                    bdoUnit.put("cdsem1_id_via_size", fileData.medianID)
                    bdoUnit.put("cdsem1_od_via_size", fileData.medianOD)
                    bdoUnit.put("cdsem1_measurement_date", fileData.MeasurementDate)
                    bdoUnit.put("cdsem1ViaFilename", fileName)
                    bdoUnit.put("cdsem1_measured", synced)
                    bdoUnit.put("cdsem1ViaSizeRawData", fileData)
                   break

                case "post_etch_cd_sem_2":
                    bdoUnit.put("cdsem2_id_via_size", fileData.medianID)
                    bdoUnit.put("cdsem2_od_via_size", fileData.medianOD)
                    bdoUnit.put("cdsem2_measurement_date", fileData.MeasurementDate)
                    bdoUnit.put("cdsem2ViaFilename", fileName)
                    bdoUnit.put("cdsem2_measured", synced)
                    bdoUnit.put("cdsem2ViaSizeRawData", fileData)
                    break

                case "ficd_meas":
                    bdoUnit.put("fid_via_size", fileData.medianID)
                    bdoUnit.put("fod_via_size", fileData.medianOD)
                    if (fileData.medianOD >= 190 && fileData.medianOD < 200) {
                        bdoUnit.put("fod_bin", "A")
                    }
                    if (fileData.medianOD >= 180 && fileData.medianOD < 190) {
                        bdoUnit.put("fod_bin", "B")
                    }
                    if (fileData.medianOD >= 200 && fileData.medianOD < 210) {
                        bdoUnit.put("fod_bin", "C")
                    }
                    if (fileData.medianOD >= 210) {
                        bdoUnit.put("fod_bin", "D")
                    }
                    bdoUnit.put("fid_inner_area_via_size", fileData.innerareaID)
                    bdoUnit.put("fod_inner_area_via_size", fileData.innerareaOD)
                    bdoUnit.put("ficd_measurement_date", fileData.MeasurementDate)
                    bdoUnit.put("ficdViaFilename", fileName)
                    bdoUnit.put("ficd_measured", synced)
                    bdoUnit.put("ficdViaSizeRawData", fileData)
                    break
            }

            //update db
            unitService.update(bdoUnit, "admin", true)
        }
    }

    public def readWestboroSpotFile(def file) {

        FileReader fr = null
        BufferedReader br = null
        def rows = []
        try {
            fr = new FileReader(file)
            br = new BufferedReader(fr)
            def line
            DescriptiveStatistics stats = new DescriptiveStatistics()
            while ((line = br.readLine()) != null) {
                def arr = line.split(',')
                def obj = [:]
                if (arr[0] && arr[0] != 'Name') {
                    obj.Lv = arr[1].toFloat();
                    obj.x = arr[2].toFloat();
                    obj.y = arr[3].toFloat();
                    obj.u = arr[4].toFloat();
                    obj.v = arr[5].toFloat();
                    obj.dw = arr[6].toFloat();
                    obj.tcp = arr[7].toFloat();
                    obj.pur = arr[8].toFloat();
                    rows.add(obj);
                }
            }
            return rows;
        } catch (Exception exc) {
            throw new RuntimeException(exc.getMessage())
        }
        finally {
            if (br != null)
                br.close()
            if (fr != null)
                fr.close()
            br = null
            fr = null
            return rows;
        }
    }

    public def readWestboroParamsFile(def file) {

        FileReader fr = null
        BufferedReader br = null
        def obj = [:]
        try {
            fr = new FileReader(file)
            br = new BufferedReader(fr)
            def line
            DescriptiveStatistics stats = new DescriptiveStatistics()
            while ((line = br.readLine()) != null) {
                def arr = line.split(',')
                obj.testedBy = arr[0].split(":")[1].trim();
                obj.screenSize = arr[1].split(":")[1].trim();
                obj.red_current = arr[2].split(":")[1].trim().toFloat();
                obj.green_current = arr[3].split(":")[1].trim().toFloat();
                obj.blue_current = arr[4].split(":")[1].trim().toFloat();
                obj.red_voltage = arr[5].split(":")[1].trim().toFloat();
                obj.green_voltage = arr[6].split(":")[1].trim().toFloat();
                obj.blue_voltage = arr[7].split(":")[1].trim().toFloat();
                obj.comment = arr[8].split(":")[1].trim();
            }
            return obj;
        } catch (Exception exc) {
            throw new RuntimeException(exc.getMessage())
        }
        finally {
            if (br != null)
                br.close()
            if (fr != null)
                fr.close()
            br = null
            fr = null
            return obj;
        }
    }
}
