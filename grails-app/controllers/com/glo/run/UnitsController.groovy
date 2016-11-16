package com.glo.run

import com.glo.ndo.Product
import grails.converters.JSON
import org.apache.commons.logging.LogFactory
import com.mongodb.BasicDBObject
import java.text.DateFormat
import java.text.SimpleDateFormat

class UnitsController extends Rest {

	private static final logr = LogFactory.getLog(this)

	def springSecurityService
	def processEngine
	def identityService
	def managementService
	def runtimeService
	def repositoryService
	def taskService
	def workflowService
	def unitService
	def barcodeService
	def utilsService
	def fileService
	def importService
	def productsService
	def mongo
	def dataSourceActiviti
	def dataImportService

	static activiti = true

	def list = {

		try {
			def username =  springSecurityService.principal?.username
			def units

			if (!params.containsKey("searchText")) {
				// This is global search
				if ((params.query || params.bom) && session["globalUnitSearchQuery"] != params.query) {
					session["globalUnitSearchQuery"] = params.query
					params.offset = 0
				}
				units = unitService.getUnitsGlobal (username, session["globalUnitSearchQuery"],
						params.hist, params.page, params.offset, params.max, params.bom)
			} else {
				// Local search for each step
				units = unitService.getUnitsLocal (username, params.category, params.procKey, params.taskKey, params.searchText,
						params.page, params.offset, params.max, params.sort, params.dir, params.filter)
			}
			
			render ( units as JSON)
		}
		catch(Exception exc) {
			logr.error(exc)
			render ([success:false, msg: exc.toString()] as JSON)
		}
	}

	def get = {

		try {
			def username =  springSecurityService.principal?.username
			def unit = unitService.getUnit (username, params.id)
			render ( unit as JSON)
		}
		catch(Exception exc) {
			logr.error(exc)
			render ([success:false, msg: exc.toString()] as JSON)
		}
	}

	def update = {

	   try {
  			   def username =  springSecurityService.principal?.username
			   def unit =  request.JSON

			   def retUnit = unitService.update(unit, username, false)
			   render ([ok: true, success:true] + retUnit as JSON)
	   }
	   catch(Exception exc) {
			   logr.error(exc)
			   render ([ok: false, success:true, msg: exc.toString()] as JSON)
	   }
	}
	
	def updates = {

		try {
			def username =  springSecurityService.principal?.username
			def updated = new Expando(params)
			def okFlag = unitService.updates(updated, username)
			render ([ok: okFlag, success:true] as JSON)
		}
		catch(Exception exc) {
			logr.error(exc)
			render ([ok: "1", success:false, msg: exc.toString()] as JSON)
		}
	}

	def create = {

		try {
			def started = new Expando(params)
			def str =  params.units.toString().replace("\\", "")
			started.units = JSON.parse(str)
			unitService.start(started, params.pctg, params.pkey)
			render ([success:true] as JSON)
		}
		catch(Exception exc) {
			logr.error(exc)
			render ([success:false, msg: exc.toString()] as JSON)
		}
	}

	def delete = {

		render ([success:true] as JSON)
	}
	
	def autoRoute = {
		
		try {
			
			def username =  springSecurityService.principal?.username
			def moved = new Expando(params)
			def str =  params.units.toString().replace("\\", "")
			moved.units = JSON.parse(str)

			def data = unitService.autoRoutes (username, moved)

			render ([success:true, data:data] as JSON)
		} catch (Exception exc) {
			logr.error(exc)
			render ([success:false, msg: exc.toString()] as JSON)
		}
	}

	def move = {

		try {
			def username =  springSecurityService.principal?.username
			def moved = new Expando(params)
			def str =  params.units.toString().replace("\\", "")
			moved.units = JSON.parse(str)

			def stat = unitService.move (username, moved)
			
			//unitService.checkProcessViolations (username, moved)

			render ([success:true] + stat as JSON)
		} catch (Exception exc) {
			logr.error(exc)
			render ([success:false, msg: exc.toString()] as JSON)
		}
	}

	def loss = {
		try {
			def username =  springSecurityService.principal?.username
			def lost = new Expando(params)
			def str =  params.units.toString().replace("\\", "")
			lost.units = JSON.parse(str)

			unitService.loss (username, lost)

			render ([success:true] as JSON)
		} catch (Exception exc) {
			logr.error(exc)
			render ([success:false, msg: exc.toString()] as JSON)
		}
	}

	def bonus = {
		try {
			def username =  springSecurityService.principal?.username
			def lost = new Expando(params)
			def str =  params.units.toString().replace("\\", "")
			lost.units = JSON.parse(str)

			unitService.bonus (username, lost)

			render ([success:true] as JSON)
		} catch (Exception exc) {
			logr.error(exc)
			render ([success:false, msg: exc.toString()] as JSON)
		}
	}
	
	def rework = {
		try {
			def username =  springSecurityService.principal?.username
			def rework = new Expando(params)

			def str =  params.units.toString().replace("\\", "")
			rework.units = JSON.parse(str)

			unitService.rework (username, rework)

			render ([success:true] as JSON)
		} catch (Exception exc) {
			logr.error(exc)
			render ([success:false, msg: exc.toString()] as JSON)
		}
	}

	def split = {

		try {
			def username =  springSecurityService.principal?.username

			def str =  params.units.toString().replace("\\", "")
			def units = JSON.parse(str)

			Unit.withTransaction() { status ->
				unitService.split (username, units, true, [])
			}

			render ([success:true] as JSON)
		} catch (Exception exc) {
			logr.error(exc)
			render ([success:false, msg: exc.getMessage()] as JSON)
		}
	}
	
	def merge = {
		
		try {
			def username =  springSecurityService.principal?.username

			def str =  params.units.toString().replace("\\", "")
			def units = JSON.parse(str)

			Unit.withTransaction() { status ->
				unitService.merge (username, params.code, units)
			}

			render ([success:true] as JSON)
		} catch (Exception exc) {
			logr.error(exc)
			render ([success:false, msg: exc.getMessage()] as JSON)
		}
	}

    def unMerge = {

        try {
            def username =  springSecurityService.principal?.username

            Unit.withTransaction() { status ->
                unitService.unMerge (username, params.code)
            }

            render ([success:true] as JSON)
        } catch (Exception exc) {
            logr.error(exc)
            render ([success:false, msg: exc.getMessage()] as JSON)
        }
    }

    def children = {

        try {
            def username =  springSecurityService.principal?.username

            def db = mongo.getDB("glo")
            def query = new BasicDBObject('parentCode', params.code)
            def units = db.unit.find(query, ['code':1,'productCode':1,'product':1]).collect{it}
            render ( units as JSON)

        } catch (Exception exc) {
            logr.error(exc)
            render ([success:false, msg: exc.getMessage()] as JSON)
        }
    }

    def updateProduct = {

        try {
            def username =  springSecurityService.principal?.username

            def str =  params.updates.toString().replace("\\", "")
            def units = JSON.parse(str)
            def product = Product.get(params.productId.toLong())

            productsService.updateUnitsProductCode (units.collect{Unit.get(it)?.code}, product.code, product.revision)

            render ([success:true] as JSON)
        } catch (Exception exc) {
            logr.error(exc)
            render ([success:false, msg: exc.getMessage()] as JSON)
        }
    }
	


	def fileUpload = {

		try {
			def username =  springSecurityService.principal?.username
			def ret = 0
			def f = request.getFile("filePath")
			if (!f.empty) {

				def str =  params.units.toString().replace("\\", "")
				def units = JSON.parse(str)

				if (params.importData == "true") {

					ret = importService.execute(username, units, f, params.pctg, params.pkey, params.tkey)
				} else {

					def fileId = fileService.saveFile(f, params.name)
					def name = params.name
					def fileName =  f.getOriginalFilename()
					unitService.saveFileMeta (username, units.collect{it.id}, fileId, name, fileName)
				}
			}
		//	render ([success:true, retCount: ret.toString()] as JSON)
			render ("{success:'true', retCount:'" + ret.toString() + "'}")
		} catch (Exception exc) {
			logr.error(exc)
		//	render ([success:true, msg: exc.getMessage()] as JSON)
			render ("{success:'false', msg:'" + exc.getMessage() + "'}")
		}
	}

    def imageUpload = {

         try {
            def username =  springSecurityService.principal?.username
            def f = request.getFile("imageName")
            def imageName = ""
            if (!f.empty) {
                // def path = request.getSession().getServletContext().getRealPath("/")
                def path = "//calserver03/webapps/docs"
                f.transferTo( new java.io.File(path, f.getOriginalFilename()))
            }
            render ("{success:'true', imageName:'" + f.getOriginalFilename() + "'}")
        } catch (Exception exc) {
            logr.error(exc)
            //	render ([success:true, msg: exc.getMessage()] as JSON)
            render ("{success:'false', msg:'" + exc.getMessage() + "'}")
        }
    }

    def saveInstruction = {

        try {
            def username =  springSecurityService.principal?.username
            def step = workflowService.getProcessStep(params.pctg, params.pkey, params.tkey)
            step.instructions = params.instructions
            step.save(flush:true)
            render ([success:true] as JSON)
        } catch (Exception exc) {
            render ([success:false, msg: exc.toString()] as JSON)
        }
    }

	def revive = {
		try {
			def isValid = unitService.revive(params.code)
			def msg = isValid ? 'Revive completed successfully' : 'Item already active'
			render ([success:true, msg: msg] as JSON)
		} catch (Exception exc) {
			logr.error(exc)
			render ([success:false, msg: exc.toString()] as JSON)
		}
	}
	
	def addNote = {
		
		try {
			def username =  springSecurityService.principal?.username
			render ([success:true] as JSON)
			
			unitService.addNote(username, params.unitId, params.comment)
			
		} catch (Exception exc) {
			logr.error(exc)
			render ([success:false, msg: exc.getMessage()] as JSON)
		}
	}
	
	def barcodeCapabilities = {
		
		try {
			def username =  springSecurityService.principal?.username
			
			def result = barcodeService.bcCapabilities(username, params)

			render ([success:result.success, labels:result.labels, printers: result.printers, destinations: result.destinations, ungroup:result.ungroup] as JSON)
				
		} catch (Exception exc) {
			logr.error(exc)
			render ([success:false, msg: exc.getMessage()] as JSON)
		}
	}

	def barcodePrint = {
		
		try {
			logr.debug(params)
			
			def username =  springSecurityService.principal?.username
			
			def result = barcodeService.bcPrint(username, params)

			render ([success:result.success, cnt:result.cnt, msg: result.msg] as JSON)
				
		} catch (Exception exc) {
			logr.error(exc)
			render ([success:false, msg: exc.getMessage()] as JSON)
		}
	}


	def barcodeScan = {
		
		try {
			logr.debug(params)
			
			// username is coming in within params (via barcode scan) 
			//def username =  springSecurityService.principal?.username
			
			def result = barcodeService.bcScan(params)

			render ([success:result.success, cnt:result.cnt, msg: result.msg] as JSON)
				
		} catch (Exception exc) {
			logr.error(exc)
			render ([success:false, msg: exc.getMessage()] as JSON)
		}
	}

	
	def RRRELOADDD = {
		
		try {
			def db = mongo.getDB("glo")
			def i = 0
			def b = new BasicDBObject('pkey', 'packaging')
			b.put('tkey', 'test_rel_board')
			def units = db.unit.find(b, [:]).collect{
				
				def l = 1
				def o = new BasicDBObject("loadNum",1)
				o.put('relGroup', it.boardId + "_01")
				
				def t2 = []
				def t = it.tagsCustom
				t.each { s ->
					if (s.indexOf("_") == -1) {
						t2.add(s)
					}
				}
				t2.add(it.boardId + "_01")
				
				o.put('tagsCustom', t2)
				def u = new BasicDBObject('$set', o)

				db.unit.update(new BasicDBObject("code", it["code"]), u, false, true)
				
			}
			render ([success:true, cnt:i.toString()] as JSON)
		} catch (Exception exc) {
			logr.error(exc)
			render ([success:false, msg: exc.toString()] as JSON)
		}
	}
	
	def RRRELOADDDHIST = {
		
		try {
			def db = merge.getDB("glo")
			def i = 0
			def b = new BasicDBObject('TestType', 'test_rel_board')
			def hists = db.measures.find(b, ['_id':1,'WaferID':1,'DeviceID':1,'TimeRun':1]).collect { it }
			DateFormat dformat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss")
			
			hists.each {
				
				def o = dataImportService.addUnitData(db, it.WaferID + "_" + it.DeviceID)
				
				def u = new BasicDBObject('$set', o)
				db.measures.update(new BasicDBObject("_id", it["_id"]), u, false, true)
			}
			
			logr.debug(i)
			
			render ([success:true, cnt:i.toString()] as JSON)
			
		} catch (Exception exc) {
			logr.error(exc)
			render ([success:false, msg: exc.toString()] as JSON)
		}
	}

    def mergeTests = {
        def db = mongo.getDB("glo")
        DateFormat dformat = new SimpleDateFormat("yyyyMMddHHmmss")
        Date now = new Date()
        def newTestId = (long)dformat.format(now).toLong()
        def code = params.code
        def indexes = params.idxs.tokenize(",")
        def newTest = null
        def testData = null
        indexes.each {
            def bdo = new BasicDBObject()
            bdo.put("value.code", code)
            bdo.put("value.tkey", "test_data_visualization")
            bdo.put("value.testId", it.toLong())
            testData = db.testData.find(bdo).collect { it }[0]
            if (testData) {
                if (!newTest) {
                    newTest = testData
                    newTest.remove("_id")
                    newTest.value.testId = newTestId
                } else {
                    BasicDBObject dbo = (BasicDBObject) testData?.value?.data
                    dbo.each { ks, vs ->
                        if (ks != "setting") {
                            vs.each { k, v ->
                                if (!newTest.value.data[ks]) {
                                    newTest.value.data[ks] = new BasicDBObject()
                                }
                                if (!newTest.value.data[ks][k]) {
                                    newTest.value.data[ks][k] = new BasicDBObject()
                                }
                                newTest.value.data[ks][k] += v
                            }
                        }
                    }
                }
            }
        }
        db.unit.update([code: code], [$addToSet: [testDataIndex: newTestId]])
        db.history.update([code: code, "audit.tkey": "test_data_visualization"], [$addToSet: ['audit.$.dc.testDataIndex': newTestId]])
        db.testData.save(newTest)

        indexes.each {
            def bdo = new BasicDBObject()
            bdo.put("value.parentCode", code)
            bdo.put("value.tkey", "test_data_visualization")
            bdo.put("value.testId", it.toLong())
            db.testData.update(bdo, ['$set': ['value.testId': [it.toLong(), newTestId]]], false, true)
        }

        def unit1 = db.unit.find([code: code], new BasicDBObject()).collect{it}[0]
        summarizeSyncService.createSummaries(db, unit1._id, code, null, null, null, newTestId, "test_data_visualization", unit1.mask, null)

        render ([success:true, res: newTestId.toString()] as JSON)
    }
}
