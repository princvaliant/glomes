package com.glo.custom

import com.glo.ndo.*
import com.mongodb.BasicDBObject
import grails.converters.JSON

import java.text.DateFormat
import java.text.SimpleDateFormat

class BarcodeService {

	def mongo
	def unitService
	def workflowService
	def userService
	def utilsService
	def contentService
	
	final static shell = new GroovyShell()

	public def bcCapabilities (def username, def parameters) {
		def result = [:]
		result.success = false
		
		def ps = workflowService.getProcessStep(parameters.pctg, parameters.pkey, parameters.tkey)
		
		if (ps) {
			def labels = ps.barcodePrinting ?: ""
			def printers = ps.barcodePrinter ?: ""
			
			result.labels = labels.tokenize(',').collect { it.trim() }
			result.printers = printers.tokenize(',').collect { it.trim() }
			if (result.labels.size() == 0 || result.printers.size() == 0) return (result)
			
			result.destinations = []
			if (parameters.pctg == "RctHdw" && parameters.pkey == "inventory") {
				result.destinations = workflowService.getTransitionsByTask (parameters.pkey, parameters.tkey, "flow_only").collect { it.taskKey }
				if (result.destinations.size() == 0) return (result)
			}
			
			// compile list of labels
			def str =  parameters.units.toString().replace("\\", "")
			def units = JSON.parse(str)
			
			def qty = units.size()
			def suffix = " (" + qty + (qty == 1 ? " label)" : " labels)")
			result.labels.eachWithIndex { obj, idx ->
				if (obj in ['Wafer','Part','Part-large','Part-small']) {
					result.labels[idx] += suffix
				}
			}
			result.labels.add('User')
			result.labels.add('Process move')
			result.labels.add('Help')
			
			qty = unitService.checkGroupMove(parameters.pctg, parameters.pkey, parameters.tkey, "cassetteId", units, "count")
			result.ungroup = ""
			if (qty > 0) result.ungroup = "Remove from cassette (" + qty + (qty == 1 ? " item)" : " items)")
			
			result.success = true
		}
		return (result)
	}
	
		
	public def bcPrint (def username, def parameters) {
		def result = [:]
		result.success = false
		result.cnt = 0
		result.msg = ""
		
		def db = mongo.getDB("glo")
		
		// get labelName without '(small)' or '(large)' suffix
		def labelName = parameters.label ?: ""
		def ii = labelName.indexOf('(')
		if (ii > 2) labelName = labelName[0..ii-2]
		
		def printerName = parameters.printer ?: ""
		if (labelName == "" || printerName == "") {
			result.msg = "Barcode printing not defined"
			return (result)
		}
		if (labelName in ['Cassette','Part','Tray','Part-large','Process move'] && printerName.indexOf('Small') >= 0) {
			result.msg = "Cannot print large labels on small printer"
			return (result)
		}

		def units = []
		
		DateFormat dformat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
		Date now = new Date()
		def createdDT = dformat.format(now)
		def blankDT = "                   "		//must be 19 blank spaces to ensure document size for capped collection
		
		def partOkCnt = 0, partErrorCnt = 0

		def cassetteUnits = []
		
		switch (labelName) {
			case "Process move":
				def data = []
				data.add("")
				if (parameters.fromTkey != '') {
					def fromTaskName = workflowService.getTaskName(parameters.pkey, parameters.fromTkey)
					data.add(fromTaskName)
				}

				def jsonStr = "{ bc:true"
				def qty = 0
				parameters.each {
					if (!(it.key in ['units', 'label', 'printer', 'pctg', 'prior', 'action', 'controller']))
						
						if (it.value && it.value != "") {
							switch (it.key) {
								case 'company':
									data.add(it.key + " = " + Company.get(it.value).name)
									break
								case 'location':
									data.add(it.key + " = " + Location.get(it.value).name)
									break
								case 'equipment':
									data.add(it.key + " = " + Equipment.get(it.value).name)
									break
								case 'pkey':
								case 'fromTkey':
								case 'tkey':
									break
								case 'runNumber':
									it.value = 'LAST'
									data.add(it.key + " = <LAST>")
									break
								case 'sin_batch_number':
								case 'nil_batch_number':
									it.value = it.value.toUpperCase()
									if (it.value in ['LAST','NEW'])
										data.add(it.key + " = <" + it.value + ">")
									else
										result.msg = it.key + " must be 'LAST' or 'NEW'"
									break
								case 'qtyIn':
									if (it.value.isInteger()) {
										qty = it.value.toInteger()
										data.add("Qty = " + qty.abs().toString())
									}
									else
										data.add("Qty = " + it.value)
									break
								default:
									data.add(it.key + " = " + it.value)
									break
							}
							
							// jsonStr will end up in the barcode 
							jsonStr += ", " + it.key + ":'" + it.value + "'"			// all parameters are passed as String(s)!!
						}
				}
				jsonStr += " }"
				data[0] = jsonStr
				if (parameters.fromTkey != '' && qty <= 0)
					result.msg = "Qty must be positive"
					
				if (result.msg != "")
					return (result)
				
				def insert = new BasicDBObject()
				insert.put('installName', printerName)
				if (parameters.fromTkey != '') {
					insert.put('labelName', "MoveBulk")
				}
				else {
					if (data.size() <= 3)
						insert.put('labelName', "MoveSer2")
					else if (data.size() <= 4)
						insert.put('labelName', "MoveSer3")
					else
						insert.put('labelName', "MoveSer5")
				}
				insert.put('created', createdDT)
				insert.put('printed', blankDT)
				
				def taskName = workflowService.getTaskName(parameters.pkey, parameters.tkey)
				insert.put('SN', taskName)
				insert.put('data', data)
				db.barcodeLabels.insert(insert)
				partOkCnt++
				break
		
			case "User":
				def insert = new BasicDBObject()
				insert.put('installName', printerName)
				insert.put('labelName', "User")
				insert.put('created', createdDT)
				insert.put('printed', blankDT)
				insert.put('SN', username)
				db.barcodeLabels.insert(insert)
				partOkCnt++
				break

			case "Help":
				def insert = new BasicDBObject()
				insert.put('installName', printerName)
				insert.put('labelName', "Help")
				insert.put('created', createdDT)
				insert.put('printed', blankDT)
				insert.put('SN', printerName)
				db.barcodeLabels.insert(insert)
				partOkCnt++
				break

			case "Cassette":
				def str =  parameters.units.toString().replace("\\", "")
				units = JSON.parse(str)
				if (!(units && units.size() >= 1 && units.size() <= 25)) {
					result.msg = "<br> - Max 25 wafers per cassette"
					return (result)
				}
				break

			case "Tray":
				def str =  parameters.units.toString().replace("\\", "")
				def unitsAll = JSON.parse(str)
				units.add(unitsAll[0])
				//def i = units.size() - 1
				//while (i-- > 0) units.pop()
				break
			
			case "Remove from cassette":
			default:
				def str =  parameters.units.toString().replace("\\", "")
				units = JSON.parse(str)
				break
		}
		
		if (labelName != "Remove from cassette") {
			units.each {
				def query = new BasicDBObject()
				query.put('_id', it.id)
				def unit = db.unit.find(query).collect{it}[0]
				
				if (unit) {
					switch (labelName) {
						case "Wafer":
							def insert = new BasicDBObject()
							insert.put('installName', printerName)
							insert.put('labelName', "Wafer")
							insert.put('created', createdDT)
							insert.put('printed', blankDT)
							insert.put('SN', unit.code)
							def data = []
							data[0] = unit?.nil_batch_number ?: ""
							
							def pitch = unit?.pitch ?: ""
							if (unit?.stampID) {
								def variable = null
								def processStep = workflowService.getProcessStep("nwLED", "patterning", "nil")
								if (processStep) variable = Variable.findByNameAndProcessStep("pitch", processStep)
								if (variable && variable.dir == "calc") {
									Binding binding = new Binding();
									binding.setVariable("db", mongo.getDB("glo"));
									def shell = new GroovyShell(binding)			
									def script = shell.parse(variable?.evalScript?.trim())
									pitch = unitService.getCalculatedValue(unit, unit.code, script, shell)
								}
							}
							data[1] = pitch.toString()
							
							data[2] = unit?.fid_via_size ? unit.fid_via_size.toInteger().toString() : ""
							insert.put('data', data)
							db.barcodeLabels.insert(insert)
							partOkCnt++
							break
							
						case "Cassette":
							def record = [:]
							record.code = unit.code
							record.slot = unit.cassetteSlot ?: 0
							record.nbn = unit.nil_batch_number ?: ""
							record.sin = unit.sin_batch_number ?: ""
							cassetteUnits.add(record)
							break
						
						case "Part":
						case "Part-large":
						case "Part-small":
							if (unit.isBulk == true || unit.isBulk == 'true') {
								if (unit.productCode && unit.supplier) {
									def insert = new BasicDBObject()
									insert.put('installName', printerName)
									if (labelName == "Part-small")
										insert.put('labelName', "BulkPartSmall")
									else
										insert.put('labelName', "BulkPartLarge")
									insert.put('created', createdDT)
									insert.put('printed', blankDT)
									insert.put('SN', unit.productCode + " | " + unit.supplier)
									def data = [unit.supplier, unit.supplierProductCode ?: "", unit.productCode, unit.product ?: "", "1"]
									insert.put('data', data)
									db.barcodeLabels.insert(insert)
									partOkCnt++
								}
								else {
									result.msg = "<br> - Product Code and Supplier are required for bulk inventory label"
									partErrorCnt++
								}
								break
							}
							
							def msg = unitService.checkReactorPart(unit)
							if (msg == "") {
								def insert = new BasicDBObject()
								insert.put('installName', printerName)
								insert.put('labelName', "ReactorPart")
								insert.put('created', createdDT)
								insert.put('printed', blankDT)
								insert.put('SN', unit.code)
								def data = [unit.position ?: "", unit.productCode, unit.product, "1"]
								insert.put('data', data)
								db.barcodeLabels.insert(insert)
								partOkCnt++
							}
							else {
								result.msg = msg
								partErrorCnt++
							}
							break
						
						case "Tray":
							def record = checkReactorTray(unit, "B")
							if (record.msg == "") {
								/****
								def insert = new BasicDBObject()
								insert.put('installName', printerName)
								insert.put('labelName', "ReactorTrayA")
								insert.put('created', createdDT)
								insert.put('printed', blankDT)
								insert.put('SN', unit.TrayId)
								insert.put('data', record.trayInfoA)
								db.barcodeLabels.insert(insert)
								partOkCnt++
								****/
								
								def insert = new BasicDBObject()
								insert.put('installName', printerName)
								insert.put('labelName', "ReactorTrayB")
								insert.put('created', createdDT)
								insert.put('printed', blankDT)
								insert.put('SN', unit.TrayId)
								insert.put('data', record.trayInfoB)
								db.barcodeLabels.insert(insert)
								partOkCnt++
							}
							else
								result.msg = record.msg
							break
						
						default:
							result.msg = labelName + " barcode labels are not supported."
							break;
					}
				}
			}
		}
		
		switch (labelName) {
			case "Cassette":
				// NIL comes later in process so has higher priority than SIN in checks below
				def barcodeData = []
				def unitNbns = cassetteUnits.collect { it.nbn }
//				if (unitNbns.unique().size() != 1) {
//					result.msg = "<br> - Cassette wafers must have same NIL batch number"
//					return (result)
//				}
				if (unitNbns[0] != "")
					barcodeData.add(unitNbns[0])
				else {
					def unitSins = cassetteUnits.collect { it.sin }
//					if (unitSins.unique().size() != 1) {
//						result.msg = "<br> - Cassette wafers must have same SIN batch number"
//						return (result)
//					}
					barcodeData.add(unitSins[0])
				}

				// check Slot #s and order Wafer IDs
				cassetteUnits.sort { it.slot }
				def unitSlots = cassetteUnits.collect { it.slot }
				if (unitSlots[0] == 0 || unitSlots.size() != unitSlots.unique().size()) {
					result.msg = "<br> - Cassette wafers must have unique slot numbers between 1 and 25"
					return (result)
				}
				for (def cnt=1; cnt<=25; cnt++) {
					def myUnits = cassetteUnits.findAll { it.slot == cnt }
					if (myUnits)
						barcodeData.add(myUnits[0].code)
					else
						barcodeData.add("")
				}
				
				// determine new_cassetteId, print label, and update units
				def variable = contentService.getThisVariable(parameters.pctg, parameters.pkey, parameters.tkey, "cassetteId")
				if (variable) {
					def new_cassetteId = contentService.calcThisValue(variable, "NEW")
					//new_cassetteId += '-' + units.size().toString()
					
					def insert = new BasicDBObject()
					insert.put('installName', printerName)
					insert.put('labelName', "CassetteSlot")
					insert.put('created', createdDT)
					insert.put('printed', blankDT)
					insert.put('SN', new_cassetteId)
					insert.put('data', barcodeData)
					db.barcodeLabels.insert(insert)
					partOkCnt++
	
					units.each {
						def bdoUnit = new BasicDBObject()
						bdoUnit.put("id", it.id)
						bdoUnit.put("processCategory", parameters.pctg)
						bdoUnit.put("processKey", parameters.pkey)
						bdoUnit.put("taskKey", parameters.tkey)
						bdoUnit.put("cassetteId", new_cassetteId)
						unitService.update(bdoUnit, "admin", true)
					}
				}
				else
					result.msg = "<br> - Invalid cassetteId"
				break
				
			case "Remove from cassette":
				units.each {
					def bdoUnit = new BasicDBObject()
					bdoUnit.put("id", it.id)
					bdoUnit.put("processCategory", parameters.pctg)
					bdoUnit.put("processKey", parameters.pkey)
					bdoUnit.put("taskKey", parameters.tkey)
					bdoUnit.put("cassetteId", "")
					unitService.update(bdoUnit, "admin", true)
				}
				result.success = true
				result.msg = "<br>Item(s) removed from cassette"
				break
		}
		
		if (partOkCnt != 0) {
			result.success = true
			result.cnt = partOkCnt
			result.msg = "<br>Printing started for " + partOkCnt.toString() + " barcode label(s)"
		}
		if (partErrorCnt != 0) {
			result.success = false
			result.cnt = partErrorCnt
			result.msg = "Barcode(s) not printed for " + partErrorCnt.toString() + " item(s) " + result.msg
		}

		return (result)
	}
	
	
	public def bcScan (def parameters) {
		def result = [:]
		result.success = false
		result.cnt = 0
		result.msg = ""
		
		def db = mongo.getDB("glo")
		
		def bcUser = parameters.user ?: ""
		def bcTkey = parameters.tkey ?: ""
		def bcPart = parameters.part ?: ""
		
		if (bcUser == "") result.msg = "ERROR: User not specified"
		if (bcTkey == "") result.msg = "ERROR: Tkey not specified"
		if (bcPart == "") result.msg = "ERROR: Part not specified"
		if (result.msg != "") return (result)
		
		// check User
		if (!userService.validateUser(bcUser)) {
			result.msg = "ERROR: $bcUser is not an authorized user"
			return (result)
		}

		def vars = JSON.parse(bcTkey)
		vars.pctg = workflowService.getPctg(vars.pkey)
		
		// determine type of Part, and check Part
		def project = new BasicDBObject()
		def units = []
		def mode = ""
		
		if (mode == "" && bcPart.indexOf(" | ") >= 0) {
			def tokens = bcPart.tokenize(' | ').collect { it.trim() }
			def query = new BasicDBObject()
			query.put('productCode', tokens[0])
			query.put('supplier', tokens[1])
			query.put('pctg', vars.pctg)
			query.put('pkey', vars.pkey)
			query.put('isBulk',new BasicDBObject(['$in':[true,'true']]))
			units = db.unit.find(query, project).collect{it}
			if (units) {
				mode = "Bulk"
			}
		}
		
		if (mode == "") {
			def query = new BasicDBObject()
			query.put('code', bcPart)
			query.put('pctg', vars.pctg)
			query.put('pkey', vars.pkey)
			units = db.unit.find(query, project).collect{it}
			if (units) {
				def product = Product.findByCode(units[0].productCode)
				if (product.category == "Wafer") mode = "Wafer"
				else  mode = "Part"
			}
		}
		
		if (mode == "") {
			def query = new BasicDBObject()
			query.put('pctg', vars.pctg)
			query.put('pkey', vars.pkey)
			query.put('TrayId', bcPart)
			units = db.unit.find(query, project).collect{it}
			if (units) {
				mode = "Tray"
				def record = checkReactorTray(units[0], "B")
				if (record.msg != "") {
					result.msg = "ERROR: " + record.msg
					return (result)
				}
			}
		}

		if (mode == "") {
			def variable = contentService.getThisVariable(vars.pctg, vars.pkey, vars.tkey, "cassetteId")
			if (variable) {
				if (bcPart[0..2] == variable.searchPrefix) {
					//def tokens = bcPart.tokenize('-').collect { it.trim() }
					
					def query = new BasicDBObject()
					query.put('pctg', vars.pctg)
					query.put('pkey', vars.pkey)
					query.put('cassetteId', bcPart)
					units = db.unit.find(query, project).collect {it}
					if (units) {
						mode = "Cassette"
						//if (units.size() != tokens[1].toInteger()) {
						//	result.msg = "ERROR: Label has " + tokens[1].toInteger() + " parts but MES has " + units.size() + " parts"
						//	return (result)
						//}
						def tkeys = units.collect { it.tkey }
						if (tkeys.unique().size() != 1) {
							result.msg = "ERROR: Cassette parts must be in the same process step"
							return (result)
						}
					}
				}
			}
		}

		if (mode == "") {
			result.msg = "ERROR: Part $bcPart not found"
			return (result)
		}
		
		// check scanner config under process step
		if (mode != "Bulk" || vars.tkey != "end") {
			def ps = workflowService.getProcessStep(vars.pctg, vars.pkey, vars.tkey)
			def scanTypes = (ps?.barcodeScanning && ps?.barcodeScanning != "") ? ps?.barcodeScanning.tokenize(',') : []
			if (!(mode in scanTypes)) {
				result.msg = "ERROR: $mode barcode scan moves in " + vars.tkey + " are not allowed"
				return (result)
			}
		}

		// for Bulk moves, identify actual S/N to move
		if (mode == "Bulk") {
			def qty = 0
			if (vars.qtyIn && vars.qtyIn.isInteger()) qty = vars.qtyIn.toInteger()
			def fromTkey = vars.fromTkey ?: ""
			if (fromTkey == "" || qty <= 0) {
				result.msg = "ERROR: fromTkey or Qty not specified for bulk move"
				return (result)
			}
			vars.qtyIn = qty
			
			units = units.findAll { it.tkey == fromTkey && it.qtyOut >= vars.qtyIn }.collect { it }
			if (units.size() != 1) {
				result.msg = "ERROR: Sufficient Qty not available for bulk move from " + fromTkey + " to " + vars.tkey
				return (result)
			}
		}
		
		// check process flow
		result.msg = "ERROR: Cannot move from " + units[0].tkey + " to " + vars.tkey
		if (units[0].tkey != vars.tkey) {
			def transitions = workflowService.getTransitionsByTask (units[0].pkey, units[0].tkey, "flow_only").collect { it.taskKey }
			if (vars.tkey in transitions) {
				result.msg = ""
			}
		}

		// for move to reactor, retrieve latest runNumber
		if (result.msg == "") {
			if (vars.tkey in ["parts_in_reactor", "epi_growth"]) {
				def equipment = Equipment.findById(vars.equipment)
				def epiRuns = EpiRun.findAllByEquipmentName(equipment.name)
				if (epiRuns) {
					epiRuns = epiRuns.sort { it.runNumber }.reverse()  // or by dateCreated?
					vars.runNumber = epiRuns[0].runNumber
				}
				else
					result.msg = "ERROR: epi runNumber not found in MES"
			}
			if (vars.tkey in ["pp_progress", "pp_inventory", "forward_to_packaging"])
				result.msg = "ERROR: destination is on the do-not-move list"
			if (vars.sin_batch_number && vars.sin_batch_number in ['LAST','NEW']) {
				def variable = contentService.getThisVariable(vars.pctg, vars.pkey, vars.tkey, "sin_batch_number")
				if (variable)
					vars.sin_batch_number = contentService.calcThisValue(variable, vars.sin_batch_number)
				else
					result.msg = "ERROR: sin_batch_number not found in MES"
			}
			if (vars.nil_batch_number && vars.nil_batch_number in ['LAST','NEW']) {
				def variable = contentService.getThisVariable(vars.pctg, vars.pkey, vars.tkey, "nil_batch_number")
				if (variable)
					vars.nil_batch_number = contentService.calcThisValue(variable, vars.nil_batch_number)
				else
					result.msg = "ERROR: nil_batch_number not found in MES"
			}
		}
		
		// if all good so far, check units and prepare for move
		if (result.msg == "") {
			vars.taskKeyEng = vars.tkey
			vars.processKeyEng = vars.pkey
			vars.processCategoryEng = vars.pctg
			vars.isEngineering = false
			vars.prior = 50
			vars.units = []
			
			units.each {
				def dataOk = "0"
				if (mode != "Bulk") dataOk = unitService.validate (it, null, null, [])
				if (dataOk != "0") result.msg += it.code + ", "
				
				def n = [:]
				n.put('transition', 'barcode')	// 'engineering' ?
				n.put('id', it["_id"])
				n.put('dataOk', dataOk)
				vars.units.add(n)
			}
			
			// processStep.allowMoveDespiteSpecFail should be checked rather than "mrb" within taskKey
			def skipValidation = (vars.tkey.indexOf("_mrb") >= 0) || (vars.tkey.indexOf("mrb_") >= 0)
			if (skipValidation) result.msg = ""
			
			if (result.msg != "")
				result.msg = "ERROR: items failing validation " + result.msg
		}
		
		// if all good, move units and check if indeed moved
		if (result.msg == "") {
			unitService.move (bcUser, vars)
			//unitService.checkProcessViolations (bcUser, vars)

			//check if indeed moved
			if (mode != "Bulk") {
				units.each {
					def query1 = new BasicDBObject("code", it.code)
					def unit1 = db.unit.find(query1, new BasicDBObject()).collect{it}[0]
					if (unit1?.tkey != vars.tkey)
						result.msg += it.code + ", "
				}
				if (result.msg != "")
					result.msg = "ERROR: move attempted but failed for " + result.msg
			}
		}
		
		if (result.msg == "") {
			result.cnt = units.size()
			result.msg = "SUCCESS: $bcPart (total " + result.cnt + " parts) moved"
			result.success = true
		}
		
		return (result)
	}


	def checkReactorTray(def part, def trayType) {
		def result = [:]
		
		result.msg = ""
		if (part.TrayId == null)  result.msg = "<br> - Tray# is empty"
		else if (part.TrayId == "")  result.msg = "<br> - Tray# is empty"
		if (result.msg != "")  return (result)
		
		def db = mongo.getDB("glo")
		
		def query = new BasicDBObject()
		query.put('pctg', 'RctHdw')
		query.put('TrayId', part.TrayId)
		def project = new BasicDBObject()
		def parts = db.unit.find(query, project).collect {it}
		
		def msg = ""
		def cntTkey = 0
		
		def items = []
		parts.each {
			msg += unitService.checkReactorPart(it)
			
			def item = [:]
			def product = Product.findByCode(it.productCode)
			item.category = product.category
			item.code = it.code
			item.position = it.position ?: "."
			items.add(item)
			
			if (it.tkey && it.tkey != part.tkey) cntTkey++
		}
		
		if (cntTkey != 0) msg += "<br> - $cntTkey parts not in this process step"
		
		if (trayType.contains("A")) {
			def cnt = items.findAll { obj -> obj.category == "Ceiling" }.size()
			if (cnt != 1) msg += "<br> - " + cnt + " ceilings"
			
			cnt = items.findAll { obj -> obj.category == "Cover Star" }.size()
			if (cnt != 1) msg += "<br> - " + cnt + " cover stars"
			
			cnt = items.findAll { obj -> obj.category == "First Segment" }.size()
			if (cnt != 1) msg += "<br> - " + cnt + " first segments"

			cnt = items.findAll { obj -> obj.category == "Segment" }.size()
			if (cnt != 7) msg += "<br> - " + cnt + " segments"
		}
		
		if (trayType.contains("B")) {
			def subs = items.findAll { obj -> obj.category == "Satellite" }.collect { it.position }
			def cnt = subs.size()
			if (cnt != 8) msg += "<br> - " + cnt + " satellites"
			
			if (cnt != subs.unique().size()) msg += "<br> - Satellites must have unique positions"
			
			if (trayType == "B") {
				def k = parts.size() - cnt
				if (k != 0) msg += "<br> - $k parts are not satellites <br>"
			}
		}
				
		if (msg != "")
			result.msg = part.TrayId + " is not ready:" + msg
		else {
			def infoA = []
			def infoB = []
			if (trayType.contains("A")) {
				def subs = items.findAll { obj -> obj.category == "Ceiling" }.collect { it }
				subs.add( items.findAll { obj -> obj.category == "Cover Star" }.collect { it } )
				subs.each {
					infoA.add(it.code)
				}

				subs = items.findAll { obj -> obj.category == "First Segment" }.collect { it }
				subs.add( items.findAll { obj -> obj.category == "Segment" }.collect { it } )
				subs.each {
					infoA.add(it.position)
					infoA.add(it.code)
				}
			}
			if (trayType.contains("B")) {
				def subs = items.findAll { obj -> obj.category == "Satellite" }.collect { it }
				subs.sort { it.position }
				subs.each {
					infoB.add(it.position)
					infoB.add(it.code)
				}
			}
			result.trayInfoA = infoA
			result.trayInfoB = infoB
		}
		
		return (result)
	}
}