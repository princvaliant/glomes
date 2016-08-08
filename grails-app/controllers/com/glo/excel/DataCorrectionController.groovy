package com.glo.excel

import com.glo.ndo.*
import com.mongodb.BasicDBObject
import com.mongodb.DBCollection
import org.apache.commons.logging.LogFactory
import org.apache.poi.xssf.usermodel.XSSFWorkbook

import javax.servlet.ServletOutputStream
import java.text.DateFormat
import java.text.SimpleDateFormat

class DataCorrectionController {

	def springSecurityService
	def workflowService
	def mongo
	def utilsService
	def unitService
	def dataViewService
	def historyService
    def epiRunService
	def sequenceGeneratorService
    def experimentDataService

	private static final logr = LogFactory.getLog(this)

	def list = {
		
	}

    def createYaml = {

        def pid = params.pid ? params.pid.toLong() : null
        def psid = params.psid ? params.psid.toLong() : null
        if (pid == null && psid == null) {
            render ("Provide pid or psid parameters")
            return
        }

        def vars = []
        if (pid != null) {
            vars = Variable.findAllByProcess(Process.get(pid))
        } else {
            vars = Variable.findAllByProcessStep(ProcessStep.get(psid))
        }

        if (vars) {

            def text = "    - id:\n"
            text += "      :source: _id\n"
            text += "      :type: TEXT\n"

            vars.each {

                def t = "TEXT"
                if (it.dataType == "int") t = "INTEGER"
                if (it.dataType == "float") t = "FLOAT"
                if (it.dataType == "date") t = "TIMESTAMP"

                text += "    - " + it.name.replaceAll(/[^A-Za-z0-9]/, "_").toLowerCase() + ":\n"
                text += "      :source: " + it.name + "\n"
                text += "      :type: " + t + "\n"
            }

            println(text)
            render(text)
        } else {
            render ("There are no variables defined for pid or psid parameters")
        }
    }

    def epiRecipes = {

        epiRunService.processEpiRecipeData()
        render("OK")
    }

    def epiReactorRuns = {

        epiRunService.processEpiRunData()
        render("OK")
    }

	def moveUpdateReturn (def db, def code, def pctg, def pkey, def tkey, def varName, def newValue, def back) {
		
		def active = "true"
		def query = new BasicDBObject("code", code)
		def unit = db.unit.find(query, new BasicDBObject()).collect{it} [0]
		if (!unit) {
			active = ""
			unit = db.unitarchive.find(query, new BasicDBObject()).collect{it} [0]
		}
		
		if (unit && unit.pctg == pctg) {
			def username =  springSecurityService.principal?.username
		
			def newUnit = unit.clone()
			newUnit.start = new Date()
			newUnit.actualStart = new Date()
			newUnit.movedBy = username
			
			newUnit.pkey = pkey
			newUnit.tkey = tkey
			newUnit.tname = workflowService.getTaskName(pkey, tkey)
			if (varName) newUnit[varName] = newValue

			historyService.initHistory ("move", unit, newUnit, null)
			
			if (back) {
				Thread.sleep(1000)
				unit.start = new Date()
				unit.actualStart = new Date()
				unit.movedBy = username

				historyService.initHistory ("move", newUnit, unit, null)
			
				db.dataReport.update(['code':unit.code], ['$set':['value.active':active]])
			}
			return (true)
		}
		
		return (false)
	}



	
	def upload = {
		def pctg = params.pctg ?: ""
		def pkey = params.pkey ?: ""
		def tkey = params.tkey ?: ""
		def insertStep = params.insertStep ?: false
		
		def varName = params.varName ?: ""
		def digits = params.varDigits ?: ""
		def varDigits = digits.isInteger() ? digits.toInteger() : -1
		
		// Identify and inspect the Variable
		def varDir = "<unknown>"
		def varDataType = "<unknown>"
		def varString = "<unknown>"
		
		def process = Process.findByCategoryAndPkey(pctg, pkey)
		
		if (varName in ["product", "productCode", "supplier", "supplierProductCode"]) {
			varDir = ""
			varDataType = "string"
			varString = varName
		}
		else if (varName in ["processViolations"]) {
			varDir = ""
			varDataType = "int"
			varString = varName
		}
		else {
			def variable = null
			if (process) {
				pctg = process.category
				pkey = process.pkey
				if (tkey == "") {
					variable = Variable.findByNameAndProcess(varName,process)
				}
				else {
					def processStep = ProcessStep.findByProcessAndTaskKey(process, tkey)
					if (processStep) {
						tkey = processStep.taskKey
						if (varName.toLowerCase() == "stepidx")
							variable = 1
						else
							variable = Variable.findByNameAndProcessStep(varName,processStep)
					}
				}
			}
	
			if (!variable) {
				flash.message = "ERROR: variable not found"
				redirect(action: "list")
				return
			}

			if (varName.toLowerCase() == "stepidx") {
				varName = "stepIdx"
				varDir = "dc"
				varDataType = "int"
				varString = pctg + " - " + pkey + " - " + tkey + " - " + varName
			}
			else {
				varName = variable.name
				varDir = variable.dir
				varDataType = variable.dataType
				varString = variable.toString()
			}
		}
		
		if (varDir == "in" && !(varDataType in ['Location', 'Equipment', 'Company'])) {
			flash.message = "ERROR: datatype for 'in' variable must be Location, Equipment or Company" + varString
			redirect(action: "list")
			return
		}
		if (varDir != "in" && varDataType in ['Location', 'Equipment', 'Company']) {
			flash.message = "ERROR: dir for Location, Equipment or Company variable must be 'in'" + varString
			redirect(action: "list")
			return
		}

		if (!( (varDataType in ['string', 'int', 'float','date','scientific'] && varDir in ['', 'din', 'dc']) || (varDir == "in" && varDataType in ['Location', 'Equipment', 'Company']) )) {
			flash.message = "ERROR: invalid variable configuration for: " + varString
			redirect(action: "list")
			return
		}
		
		// Read input file
		def file = request.getFile("file")
		def rName = file.fileItem.fileName.toLowerCase()
		if (rName.indexOf(".csv") < 0) {
			flash.message = "ERROR: invalid file"
			redirect(action: "list")
			return
		}
		rName = varName + ".xlsx"
		rName.replaceAll(' ', '_')
		
		def dataType = (varDir == 'in') ? 'string' : varDataType
		def results = readWaferFile (file, dataType, varDigits)
		
		def db = mongo.getDB("glo")
		
		results.each { el ->
			if (el.result == "") {
				
				if (process.processCategory?.mongoCollection == "epiRun") {
					
					
					// specifying "code" in column A and dateCreated in column B results in new Runs created in MES
					/***
					if (varName == "dateCreated") {
						DateFormat  df = new SimpleDateFormat("yyMMdd")
						
						EpiRun.withTransaction { status ->
							 def new_dt = df.format(el.newValue)
							 def letter = 'A'
							 def runNumber = el.code + new_dt + letter
			 
							 while (EpiRun.findByRunNumber(runNumber)) {
								 letter++
								 runNumber = el.code + new_dt + letter
							 }
							 
							 def epiRun = new EpiRun()
							 epiRun.code = el.code
							 epiRun.dateCreated = el.newValue
							 epiRun.lastUpdated = el.newValue
							 
							 def equipment = Equipment.findByCode(el.code)
							 epiRun.equipmentName = equipment.name
							 epiRun.workcenter = equipment.workCenter?.name
							 epiRun.runNumber = runNumber
							 
							 epiRun.save(failOnError: true)
							 el.code = runNumber
						}
					}
					***/
					
					def coll = 'epiRun'
					DBCollection collection = db.getCollection(coll)

					def query = new BasicDBObject()
					query.put('code', pkey)
					query.put('runNumber', el.code)
					def unit = collection.find(query, new BasicDBObject()).collect{it}[0]
					if (unit) {
						def update = new BasicDBObject()
						def path = varName
						update.put('$set', new BasicDBObject(path, el.newValue))
						collection.update(query, update)
						el.detail += " | " + coll + "." + path
						el.result += "  " + coll + "(1)"
					}
					else
						el.result = "NONE: not found in MES"
				}
				
				else if (varDir == "") {
					def collections = ['unit', 'unitarchive', 'history', 'dataReport', 'dataReport']		//'testData', 'measures'
					def codepath = ['code', 'code', 'code', 'code', 'code']									//'value.code', 'WaferID'
					def varpath = ['', '', '', 'value.', 'unit.']
					
					def newValue1 = null
					if (varName == "supplier") {
						def obj = Company.findByName(el.newValue)
						if (obj) newValue1 = obj.id.toString()
						else el.result = "ERROR: invalid supplierId"
					}
					
					if (el.result == "") {
						collections.eachWithIndex {  coll, idx ->
							DBCollection collection = db.getCollection(coll)
							def query = new BasicDBObject()
							query.put(codepath[idx], el.code)
							def unit = collection.find(query, new BasicDBObject()).collect{it}[0]
							if (unit) {
								def update = new BasicDBObject()
								def path = varpath[idx] + varName
								update.put('$set', new BasicDBObject(path, el.newValue))
								collection.update(query, update)
								el.detail += " | " + coll + "." + path
								if (varName == "supplier") {
									def update1 = new BasicDBObject()
									def path1 = varpath[idx] + "supplierId"
									update1.put('$set', new BasicDBObject(path1, newValue1))
									collection.update(query, update1)
									el.detail += " | " + coll + "." + path1
								}
								el.result += "  " + coll + "(1)"
							}
						}
					}
					
					if (el.result == "")
						el.result = "NONE: not found in MES"
				}
				
				// UPDATE INDIVIDUAL TABLES (unitService.update does not work for archived items)
				else {
					def varName1 = null
					def newValue1 = null
					if (varDir == 'in') {
						varName1 = varName + 'Id'
						switch (varDataType) {
							case 'Company':
								def obj = Company.findByName(el.newValue)
								if (obj) newValue1 = obj.id.toString()
								break
							case 'Location':
								def obj = Location.findByName(el.newValue)
								if (obj) newValue1 = obj.id.toString()
								break
							case 'Equipment':
								def obj = Equipment.findByName(el.newValue)
								if (obj) newValue1 = obj.id.toString()
								break
						}
					}
					if (varName1 != null && newValue1 == null) {
						el.result = "ERROR: invalid " + varName1
					}
					else {
						if (tkey != "" && insertStep) {
							if (!unitService.inStep(db, el.code, pkey, tkey)) {
								if (moveUpdateReturn (db, el.code, pctg, pkey, tkey, varName, el.newValue, true)) {
									el.insertStep = "moved and returned"
								}
								else {
									el.result = "NONE: could not move to " + pkey + "-" + tkey
								}
							}
						}
						
						def cnt = -1
						def query = new BasicDBObject("code", el.code)
						def unit = db.history.find(query, new BasicDBObject()).collect{it}[0]
						
						if (unit) {
							cnt = 0
							unit.audit.eachWithIndex { obj, idx ->
								if (obj.pctg == pctg && obj.pkey == pkey) {
									if (tkey == "" || obj.tkey == tkey) {
										if (obj.last == true || varName == "stepIdx") {
											def update = new BasicDBObject()
											def path = "audit." + idx
											if (tkey != "" && varName != "stepIdx" && varDir in ['dc', 'din']) path += "." + varDir
											update.put('$set', new BasicDBObject(path + "." + varName, el.newValue))
											db.history.update(query, update)
											el.detail += " | history." + path + "." + varName
											if (varName1) {
												def update1 = new BasicDBObject()
												update1.put('$set', new BasicDBObject(path + "." + varName1, newValue1))
												db.history.update(query, update1)
												el.detail += " | history." + path + "." + varName1
											}
											cnt++
										}
									}
								}
							}
							if (cnt > 0) {
								if (tkey == "") {
									def update = new BasicDBObject()
									update.put('$set', new BasicDBObject(varName, el.newValue))
									db.history.update(query, update)
									el.detail += " | history." + varName
									if (varName1) {
										def update1 = new BasicDBObject()
										update1.put('$set', new BasicDBObject(varName1, newValue1))
										db.history.update(query, update1)
										el.detail += " | history." + varName1
									}
									cnt++
								}
							}
							el.result += "  history(" + cnt + ")"
						}
						
						if (cnt < 0)
							el.result = "NONE: not found in MES"
						else if (cnt == 0)
							el.result = "NONE: never in " + pkey + "-" + tkey
						else {
							el.result = "UPDATED:" + el.result
							
							unit = db.dataReport.find(query, new BasicDBObject()).collect{it}[0]
							if (unit) {
								cnt = 0
								if (tkey == "") {
									def update = new BasicDBObject()
									update.put('$set', new BasicDBObject('value.' + varName, el.newValue))
									db.dataReport.update(query, update)
									el.detail += " | dataReport." + 'value.' + varName
									if (varName1) {
										def update1 = new BasicDBObject()
										update1.put('$set', new BasicDBObject('value.' + varName1, newValue1))
										db.dataReport.update(query, update1)
										el.detail += " | dataReport." + 'value.' + varName1
									}
									cnt++
								}
								if (varName == "stepIdx" && unit.value.tkey == tkey) {
									def update = new BasicDBObject()
									def path = 'value.idx'
									update.put('$set', new BasicDBObject(path, el.newValue))
									db.dataReport.update(query, update)
									el.detail += " | dataReport." + path
									cnt++
								}
								unit.value.each { k, v ->
									if (v.getClass() == BasicDBObject) {
										if (v?.pkey == pkey) {
											if (tkey == "" || tkey == k) {
												def update = new BasicDBObject()
												def path = 'value.' + k
												update.put('$set', new BasicDBObject(path + '.' + varName, el.newValue))
												db.dataReport.update(query, update)
												el.detail += " | dataReport." + path + '.' + varName
												if (varName1) {
													def update1 = new BasicDBObject()
													update1.put('$set', new BasicDBObject(path + "." + varName1, newValue1))
													db.dataReport.update(query, update1)
													el.detail += " | dataReport." + path + "." + varName1
												}
												cnt++
											}
										}
									}
								}
								el.result += "  dataReport(" + cnt + ")"
							}
							
							if (varName != "stepIdx") {
								unit = db.unit.find(query, new BasicDBObject()).collect{it} [0]
								if (unit && varName1 == null) {
									def update = new BasicDBObject()
									update.put('$set', new BasicDBObject(varName, el.newValue))
									db.unit.update(query, update)
									el.detail += " | unit." + varName
									if (varName1) {
										def update1 = new BasicDBObject()
										update1.put('$set', new BasicDBObject(varName1, newValue1))
										db.unit.update(query, update1)
										el.detail += " | unit." + varName1
									}
									el.result += "  unit(1)"
								}
								unit = db.unitarchive.find(query, new BasicDBObject()).collect{it} [0]
								if (unit && varName1 == null) {
									def update = new BasicDBObject()
									update.put('$set', new BasicDBObject(varName, el.newValue))
									db.unitarchive.update(query, update)
									el.detail += " | unitarchive." + varName
									if (varName1) {
										def update1 = new BasicDBObject()
										update1.put('$set', new BasicDBObject(varName1, newValue1))
										db.unitarchive.update(query, update1)
										el.detail += " | unitarchive." + varName1
									}
									el.result += "  unitarchive(1)"
								}
							}
						}
					}
				}
			}
		}
			
		XSSFWorkbook workbook = utilsService.exportExcel(results, "")

		//FileOutputStream out = new FileOutputStream(dir + "\\" + rName);
		//workbook.write(out);
		//out.close();
		
		def fHeader = "attachment; filename=" + rName
		response.setHeader("Content-disposition", fHeader)
		response.contentType = "application/excel"
		ServletOutputStream f = response.getOutputStream()
		workbook.write(f)
		f.close()
		
		//flash.message = "Processed: see " + rName + " for results on " + varString
		//redirect(action: "list")
	}

	
	private def checkDataViews() {
		
		def results = []
		def dataViews = DataView.findAll()
		
		dataViews.each { dataView ->
		
			def custom = false
			if (dataView.urlExportData && dataView.urlExportData != "") custom = true
			if (dataView.urlDashboardData && dataView.urlDashboardData != "") custom = true
			
			if (!custom) {
				def buckets = [:]
				def bucketsMsg = ""
				
				def dataViewVariables = DataViewVariable.findAllByDataView(dataView, [sort:"idx"])
				dataViewVariables.each {
					it.link = dataViewService.determineDvvLink(dvv)
					
					if (!buckets[it.link]) {
						buckets[it.link] = createBucket(it.link)
						bucketsMsg += it.link + " | "
					}
				}
				
				def rr = dataViewService.validateDataview (dataView)
				
				if (buckets.size() != 1 || rr.errorMsg != "") {
					def record = [:]
					record.name = dataView.name
					record.dateCreated = dataView.dateCreated
					record.lastUpdated = dataView.lastUpdated
					record.isPublic = dataView.isPublic
					record.owner = dataView.owner
					record.urlExportData = dataView.urlExportData ?: ""
					record.urlDashboardData = dataView.urlDashboardData ?: ""
					record.bucketsCnt = buckets.size()
					record.buckets = bucketsMsg
					record.errorMsg = rr.errorMsg
					results.add(record)
				}
			}
		}

		XSSFWorkbook workbook = utilsService.exportExcel(results, "")
		response.setHeader("Content-disposition",  "attachment; filename=checkDataViews.xlsx")
		response.contentType = "application/excel"
		ServletOutputStream f = response.getOutputStream()
		workbook.write(f)
		f.close()
	}

    def updateMaintenance = {

        def db = mongo.getDB("glo")

        def ems = EquipmentMaintenance.executeQuery(
                "select em from EquipmentMaintenance as em where em.cycleType in ('hours','days','weeks','months','quarters','years')")

        ems.each {

            if (!db.unit.find([tkey: 'scheduled_time', equipmentMaintenanceId: "EMID" + it.id]).collect{it}[0]) {

                 render (it.id + "<br/>")

                 unitService.addMaintenanceTask(it, it.cycleStartDate, [], [], it.department, it.tag, false)
            }
        }

    }


	def check = {
		def results = []
		def variables = Variable.findAll()
		
		variables.each { variable ->
			// dc or din: no restrictions
			
			// "": must be process variable
			if (variable.dir == "") {
				if (!variable.process)
					addRecord(results, variable, "'process' is null")
				if (variable.processStep)
					addRecord(results, variable, "'processStep' exists")
			}
			
			// "in": must be Location, Equipment, Company, or User - and vice versa
			if (variable.dir == "in") {
				if (!(variable.dataType in ['Location', 'Equipment', 'Company', 'User']))
					addRecord(results, variable, "'dataType' is not 'Location', 'Equipment', 'Company' or 'User'")
			}
			else if (variable.dataType in ['Location', 'Equipment', 'Company', 'User'])
				addRecord(results, variable, "'dataType' is 'Location', 'Equipment', 'Company' or 'User'")

			// "spec" or "calc": eval_script must exist
			// eval_script exists: must be "spec", "calc", "dc" or "din"
			if (variable.dir in ["spec","calc"]) {
				if (!(variable.evalScript))
					addRecord(results, variable, "'evalScript' is null")
			}
			else if (variable.evalScript) {
				if (!(variable.dir in ["dc","din"]))
					addRecord(results, variable, "'evalScript' exists")
			}
		}
		
		XSSFWorkbook workbook = utilsService.exportExcel(results, "")

		def fHeader = "attachment; filename=checkVariables.xlsx"
		response.setHeader("Content-disposition", fHeader)
		response.contentType = "application/excel"
		ServletOutputStream f = response.getOutputStream()
		workbook.write(f)
		f.close()
	}

    def expData = {
        experimentDataService.importFiles()
    }
	

	private def addRecord (def results, def variable, def issue) {
		def record = [:]
		record.variable = variable.toString()
		record.issue = "'dir' is '" + variable.dir + "' but " + issue
		results.add(record)
		
		results
	}
	
		
	private def readWaferFile (def file, def dataType, def varDigits) {
		
		DateFormat  df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
		
		//FileReader fr = null
		//BufferedReader br = null
		ByteArrayInputStream br = null
		def arr = []
		
		try {
			//fr = new FileReader(file)
			//br = new BufferedReader(fr)
			br = new ByteArrayInputStream(file.bytes)
			
			br.eachLine { line->
				def el = [:]
				el.code = line
				el.newValue = ""
				el.insertStep = ""
				el.result = "ERROR: value not specified"
				el.detail = ""
				
				def delim = line.indexOf(',')
 				if (delim >= 1) {
					el.code = line[0..delim-1]
					def len = line.length()
					if (delim+1 <= len-1) {
						def val = line[delim+1..len-1]
						val = val.replaceAll('"','')
						if (val.toLowerCase() == "null") val = null
						
						el.result = ""
						switch (dataType) {
							case "date":
								if (val == null) el.newValue = val
								else el.newValue = df.parse(val)
								//else el.result  = "ERROR: value not date"
								break
							
							case "string":
								el.newValue = val
								break
							
							case "int":
								if (val == null) el.newValue = val
								else if (val.isInteger()) el.newValue = val.toInteger()
								else el.result  = "ERROR: value not integer"
								break
							
							case "scientific":
							case "float":
								if (val == null) el.newValue = val
								else if (val.isFloat()) {
									el.newValue = val.toFloat()
									if (varDigits >= 0) {
										el.newValue = el.newValue.round(varDigits)
										//def k = 10 ^ varDigits
										//el.newValue = (el.newValue * k).round()
										//el.newValue = el.newValue / k
									}
								}
								else el.result  = "ERROR: value not float"
								break
							
							default:
								el.result  = "ERROR: invalid data type"
						}
					}
				}
				arr.add(el)
			}
		} catch (Exception exc) {
			throw new RuntimeException(exc.getMessage())
		}
		finally {
			if (br != null)	br.close()
			br = null
			//if (fr != null) fr.close()
			//fr = null
		}

		arr
	}






}