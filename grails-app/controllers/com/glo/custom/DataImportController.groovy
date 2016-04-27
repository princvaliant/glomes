package com.glo.custom

import com.mongodb.BasicDBObject
import grails.converters.JSON
import grails.web.JSONBuilder
import org.apache.commons.logging.LogFactory

class DataImportController {

	private static final logr = LogFactory.getLog(this)

	def springSecurityService
	def contentService
	def unitService
	def relSyncService
	def dataImportService
    def readFileService
	def mongo

	
	def ready = {
		
		def var = request.JSON
		def db = mongo.getDB("glo")

		def unitCode = var.WaferID + "_" + var.DeviceID
		def unit = db.unit.find(new BasicDBObject("code", unitCode), new BasicDBObject()).collect{it}[0]
		if (!unit) {
			render ([success:false, msg: "Unit with serial# " + unitCode + " does not exist in MES"] as JSON)
			return
		} 
		
		render ([success:true])
	}
	
	def is_tested = {
		
		def var = request.JSON
		def db = mongo.getDB("glo")

		def query = new BasicDBObject()
		query.put("TestType", var.TestType)
		query.put("WaferID",  var.WaferID)
		if (var.DeviceID) {
			query.put("DeviceID",  var.DeviceID)
		}
        if (var.Hours) {
            query.put("HoursOn",  var.Hours.toString().toInteger())
        }
		
		def row = db.measures.find(query, new BasicDBObject('_id',1)).limit(1).collect{it}
		if (row) {
			render ([tested:true])
		} else {
			render ([tested:false])
		}
	}
	
	
	def xy_map= {
		
		def db = mongo.getDB("glo")
		
		def query = new BasicDBObject(parentCode:null)
		query.put("tkey", params.testType)
		query.put("boardId",  params.boardId)
		
		def lastUnit = db.unit.find(query, new BasicDBObject()).limit(1).
			addSpecial('$orderby', new BasicDBObject(loadNum:-1)).collect{it}[0]
		if (lastUnit) {
			query.put("loadNum",  lastUnit.loadNum)
			def units = db.unit.find(query, new BasicDBObject()).collect{it}
			if (!units) {
				render ([success:false, msg: "There are no units mapped to board " + params.boardId] as JSON)
				return
			}
		
			def builder = new JSONBuilder()
			def results = builder.build {
				success = true
				boardmaps = array {
					units.each { unit ->
						object = {
							code = unit.code
							experimentId = unit.experimentId
							loadNum = unit.loadNum?.toString() ?: "01"
							board = unit.boardId
							position = unit.posOnBoard
						}
					}
				}
			}
			render (results)
		} else {
			render ([success:false] as JSON)
		}
	}
	
	def karlsuss_ready = {
		
		try {
			def db = mongo.getDB("glo")
			def ret = unitService.inStep(db, params.code, "", params.tkey) 
			
			render ([success: ret] as JSON)
		} catch (Exception exc) {
		
			logr.error(exc)
			render ([success:false, msg: "Ready error: " + exc.getMessage()] as JSON)
		}
	}
	
	def karlsuss_upload = {
		
		try {
			
			def s =  request.JSON
			dataImportService.enqueue("KARLSUSS", s, true)
			render ([success:true] as JSON)

		} catch (Exception exc) {
			logr.error(exc)
			render ([success:false, msg: "Upload error: " + exc.getMessage()] as JSON)
		}
	}
	
	def karlsuss_upload_devices = {
		
		try {
			
			def s =  request.JSON
			dataImportService.enqueue("KARLSUSSDEVICE", s, true)
			render ([success:true] as JSON)

		} catch (Exception exc) {
			logr.error(exc)
			render ([success:false, msg: "Upload error: " + exc.getMessage()] as JSON)
		}
	}
	
	def karlsuss_top_devices  = {
		
		def db = mongo.getDB("glo")
		
		def bdo = new BasicDBObject()
		bdo.put("code", params.code)
		
		def unit = db.unit.find(bdo).collect{it}[0]
		if (!unit) {
			render ([success:false, msg:"Unit with serial# " + params.code + " does not exist in MES"] as JSON)
		} else {
			render (unit.topDevsForTest.toString())
		}
	}

	def karlsuss_spc_upload = {
		
		try {
			
			def s =  request.JSON
			dataImportService.enqueue("KARLSUSSSPC", s, true)
			render ([success:true] as JSON)

		} catch (Exception exc) {
			logr.error(exc)
			render ([success:false, msg: "Upload error: " + exc.getMessage()] as JSON)
		}
	}


	def d65_upload = {
		
		try {
			
			def s =  request.JSON
			dataImportService.enqueue("D65", s, true)
			render ([success:true] as JSON)

		} catch (Exception exc) {
			logr.error(exc)
			render ([success:false, msg: "Upload error: " + exc.getMessage()] as JSON)
		}
	}

    def iblu_rel_upload = {

        try {

            def s =  request.JSON
            dataImportService.enqueue("D65", s, true)
            render ([success:true] as JSON)

        } catch (Exception exc) {
            logr.error(exc)
            render ([success:false, msg: "Upload error: " + exc.getMessage()] as JSON)
        }
    }

    def iblu_upload = {

        try {

            def s =  request.JSON
            dataImportService.enqueue("IBLU", s, true)
            render ([success:true] as JSON)

        } catch (Exception exc) {
            logr.error(exc)
            render ([success:false, msg: "Upload error: " + exc.getMessage()] as JSON)
        }
    }

    def top200_upload = {

        try {
            def s =  request.JSON
            dataImportService.enqueue("TOP200", s, true)
            render ([success:true] as JSON)

        } catch (Exception exc) {
            logr.error(exc)
            render ([success:false, msg: "Upload error: " + exc.getMessage()] as JSON)
        }
    }
	
	def wst_upload = {
		
		try {
			def rec = request.JSON
			dataImportService.enqueue("WST",  rec, true)
			render ([success:true] as JSON)

		} catch (Exception exc) {
			logr.error(exc)
			render ([success:false, msg: "Upload error: " + exc.getMessage()] as JSON)
		}
	}

    def manual_wafer_test_upload = {

        try {
            def rec = request.JSON
            dataImportService.enqueue("MWT",  rec, true)
            render ([success:true] as JSON)

        } catch (Exception exc) {
            logr.error(exc)
            render ([success:false, msg: "Upload error: " + exc.getMessage()] as JSON)
        }
    }
	
	def wst_complete = {
		
			try {
				def vars =  request.JSON
				dataImportService.enqueue("WSTCOMPLETE",  vars, true)
				render ([success:true] as JSON)
	
			} catch (Exception exc) {
				logr.error(exc)
				render ([success:false, msg: "Complete error: " + exc.getMessage()] as JSON)
			}
		}


    def nidot_upload = {

        try {
            def rec = request.JSON
            dataImportService.enqueue("NIDOT",  rec, true)
            render ([success:true] as JSON)

        } catch (Exception exc) {
            logr.error(exc)
            render ([success:false, msg: "Nidot Upload error: " + exc.getMessage()] as JSON)
        }
    }

    def nidot_complete = {

        try {
            def vars =  request.JSON
            dataImportService.enqueue("NIDOTCOMPLETE",  vars, true)
            render ([success:true] as JSON)

        } catch (Exception exc) {
            logr.error(exc)
            render ([success:false, msg: "NiDot Complete error: " + exc.getMessage()] as JSON)
        }
    }


	
	def xy_upload = {
		
		try {
			def var = request.JSON
			if (var.TestType == "d65_rel")
				dataImportService.enqueue("D65", var, true)
			else 
				dataImportService.enqueue("XY",  var, true)
				
			render ([success:true] as JSON)

		} catch (Exception exc) {
			logr.error(exc)
			render ([success:false, msg: "Upload error: " + exc.getMessage()] as JSON)
		}
	}
	
	def lamp_upload = {
		
		try {
			
			dataImportService.enqueue("LAMP",  request.JSON, true)
			render ([success:true] as JSON)

		} catch (Exception exc) {
			logr.error(exc)
			render ([success:false, msg: "Upload error: " + exc.getMessage()] as JSON)
		}
	}
	
	
	def xy_complete = {
	
		try {
			def vars =  request.JSON
			dataImportService.enqueue("XYCOMPLETE",  vars, true)
			render ([success:true] as JSON)

		} catch (Exception exc) {
			logr.error(exc)
			render ([success:false, msg: "Complete error: " + exc.getMessage()] as JSON)
		}
	}
	
	def wafer_rel_upload = {
		
		try {
			
			dataImportService.enqueue("WAFERREL",  request.JSON, true)
			render ([success:true] as JSON)

		} catch (Exception exc) {
			logr.error(exc)
			render ([success:false, msg: "Upload error: " + exc.getMessage()] as JSON)
		}
	}
	
	def wafer_rel_devices  = {
		
		def db = mongo.getDB("glo")
		
		def bdo = new BasicDBObject()
		bdo.put("code", params.code)
		
		def unit = db.unit.find(bdo).collect{it}[0]
		if (!unit) {
			render ([success:false, msg:"Unit with serial# " + params.code + " does not exist in MES"] as JSON)
		} else {
		
			def burningPos =  unit.burningPosition
			def mask =  unit.mask
			if (burningPos && mask) {
				
				def results = relSyncService.getBurning(mask, burningPos)
				
				render ( results as JSON)
			} else {
				render ([success:false, msg:"Unit with serial# " + params.code + ": undefined mask and burn-in position"] as JSON)
			}
		}
	}

    def konica  = {

        try {
            def rec = request.JSON
            def testId = dataImportService.enqueue("KONICA",  rec, true)
            render ([success:true, testId:testId] as JSON)
        } catch (Exception exc) {
            logr.error(exc)
            render ([success:false, msg: exc.getMessage()] as JSON)
        }
    }

    def light_bar_uniformity  = {

        try {
            def rec = request.JSON
            def testId = dataImportService.enqueue("LIGHT_BAR_UNIFORMITY",  rec, true)
            render ([success:true, testId:testId] as JSON)
        } catch (Exception exc) {
            logr.error(exc)
            render ([success:false, msg: exc.getMessage()] as JSON)
        }
    }
	
	def data_upload = {
		
		try {
			
			dataImportService.enqueue("DATAUPLOAD",  request.JSON, true)
			render ([success:true] as JSON)

		} catch (Exception exc) {
			logr.error(exc)
			render ([success:false, msg: "Data upload error: " + exc.getMessage()] as JSON)
		}
	}

    def westboro = {

        def db = mongo.getDB("glo")
        String dir = grailsApplication.config.glo.westboroDataDirectory
        if (!dir) {
            logr.error("Westboro directory not specified.")
            return
        }
        java.io.File f = new java.io.File(dir)
        if (!f.exists()) {
            logr.error("Westboro directory does not exist.")
            return
        }
        def dirLoc = new File(dir).listFiles()?.toList()
        dirLoc.each { directory ->
            if (directory.isDirectory()) {
                def data = [:]
                def head = [:]
                def code
                def testId
                def date
                def files = directory.listFiles()?.toList()
                files.each { file ->
                    try {
                        def fileName = file.name
                        def fnParse = fileName.tokenize("_")
                        def test = fnParse[0]
                        code = fnParse[1]
                        date = new GregorianCalendar(fnParse[2].toInteger(), fnParse[3].toInteger(), fnParse[4].toInteger(), fnParse[5].toInteger(), fnParse[6].toInteger(), fnParse[7].toInteger()).getTime()
                        testId = (fnParse[2].substring(2) + fnParse[3] + fnParse[4] + fnParse[5] + fnParse[6] + fnParse[7]).toLong()
                        switch (test) {
                            case "135pt":
                                data["135"] = readFileService.readWestboroSpotFile(file)
                                break;
                            case "69pt":
                                data["69"] = readFileService.readWestboroSpotFile(file)
                                break;
                            case "50pt":
                                data["50"] = readFileService.readWestboroSpotFile(file)
                                break;
                            case "13pt":
                                data["13"] = readFileService.readWestboroSpotFile(file)
                                break;
                            case "Params":
                                head = readFileService.readWestboroParamsFile(file)
                                break;
                            case "RAW":
                                break;
                        }
                    } catch (Exception exc) {
                        println(exc)
                    }
                }

                dataImportService.processWestboro(code, testId, date, head, data)
            }
        }
    }

    def keyence_upload = {
        try {
            dataImportService.run()
            render ([success:true] as JSON)
        } catch (Exception exc) {
            logr.error(exc)
            render ([success:false, msg: "Upload error: " + exc.getMessage()] as JSON)
        }
    }


}
