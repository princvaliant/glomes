package com.glo.custom

import com.glo.ndo.Variable
import com.mongodb.BasicDBObject
import grails.converters.JSON
import org.apache.poi.xssf.usermodel.XSSFWorkbook

class EpiParserController {
	
	def mongo
	def utilsService
	def workflowService
	def grailsApplication
    def epiRunService
	
	def index = {
		
		def results = []
		def processingMode = params.mode ?: 'auto'
		
		String dir = grailsApplication.config.glo.EPIParserDirectory
		if (!dir || dir.size() == 0) {
			render (['result':'EPI parser ERROR: file directory not specified in glo.properties config file'])  as JSON
			return
		}
		
		if (processingMode != 'auto') dir += "\\manual"
		
		File f = new File(dir)
		if (!f.exists()) {
			render (['result':'EPI parser ERROR: cannot access  ' + dir + '  directory'])  as JSON
			return
		}

		// Loop through all the files in the directory
		def f1 = new File(dir).listFiles().grep(~/(?i).*.EPI/)
		
		def cntOk = 0
		def cntWarning = 0
		def cntError = 0
		
		f1.each {
			def reactor = it.name[0..1].toUpperCase()
			if ((reactor == "D1" || reactor == "S2") && it.name.indexOf('_') >= 5) {
				def runNumber = it.name.tokenize("_")[0]
				if (processingMode != 'auto') runNumber = ""
				
				results = processEPIFile (reactor, runNumber, dir + "\\" + it.name)
				
				if (results.size() == 0)
					cntError++
				else {
					if (results[1].Comment && results[1].Comment != '') cntWarning++
					else cntOk++
					
					if (processingMode == 'auto') {
						def dirProcessed = dir + "\\processed"
						File fproc = new File(dirProcessed, it.name)
						if (fproc.exists())
							fproc.delete()
						boolean fileMoved = it.renameTo(new File(dirProcessed, it.name))
					}
					else {
						XSSFWorkbook workbook = utilsService.exportExcel(results, "x")
				
						FileOutputStream out = new FileOutputStream(dir + "\\" + it.name[0..-5] + ".xlsx");
						workbook.write(out);
						out.close();
					}
				}
			}
			else
				cntError++
		}
		
		def msg = "No EPI files found."
		if (cntOk + cntWarning + cntError != 0) {
			msg = ""
			if (cntOk != 0) msg += cntOk.toString() + " file(s) processed OK.  "
			if (cntWarning != 0) msg += cntWarning.toString() +" file(s) processed with Warning(s).  "
			if (cntError != 0) msg += cntError.toString() +" file(s) NOT processed."
		}

        epiRunService.processEpiRecipeData()

		render (['result': msg])  as JSON
	}
	

	private def processEPIFile (def reactor, def runNumber, def file) {
		
		def results = []
		
		def obj = new BasicDBObject()
		def db = mongo.getDB("glo")
		
		def recordRef = initRecord(reactor, "ref")
		def recordDef = initRecord(reactor, "def")
		def record = initRecord(reactor, "")
		
		def hashTag = '#$'
		def hashMode = 0		// 0 if looking for hashTag, or 1 if looking for semi-colon
		def line
		def lineNo = 0
		def ln = 0
		def warningCnt = 0
		
		FileInputStream fr = null
		InputStreamReader br = null
		try {
			def encoding = determineFileEncoding(file)
			fr = new FileInputStream(file)
			br = new InputStreamReader(fr, encoding)
			
			while ((line = br.readLine()) != null) {
				lineNo++
				ln = (lineNo + 1) / 2
				
				// look for HashTag sign, pound sign that is not part of HashTag sign, and left quotes
				def hT = line.indexOf(hashTag)
				def pound = line.indexOf('#')
				if (pound >= 0 && pound == hT)  pound = line.indexOf('#', hT+1)
				def quoteStart = line.indexOf('"')
				
				// if left quote precedes pound sign: take out the comment between quotes
				while (quoteStart >= 0 && (quoteStart < pound || pound < 0)) {
					def quoteEnd = line.indexOf('"', quoteStart+1)
					if (quoteEnd >= 0) {
						line -= line[quoteStart..quoteEnd]
						hT = line.indexOf(hashTag)
						pound = line.indexOf('#')
						if (pound >= 0 && pound == hT)  pound = line.indexOf('#', hT+1)
						quoteStart = line.indexOf('"')
					}
					else {
						record['Comment'] += "[$ln] Found only one quote sign *** "
						quoteStart = -1
					}
				}
				
				// if pound sign is not hashTag sign: truncate the line
				if (pound >= 0) {
					if (hT < 0 || pound < hT) {
						if (pound > 0) line = line[0..pound - 1]
						else line = ""
					}
					else {
						if (pound == hT) pound = line.indexOf('#', pound + 1)
						if (pound > 0) line = line[0..pound - 1]
					}
				}
				
				// process hashTag
				hT = line.indexOf(hashTag)
				if (hT >= 0) {
					if (hashMode == 0) {
						def hTa = hT + hashTag.length()
						if (hTa < line.length()) {
							def candidate = line[hTa..-1].tokenize()[0]
							
							// accept only pre-defined HashTags
							if (validateHashTag(reactor, candidate)) {
								record['HashTag'] = candidate
								hashMode = 1
								record['hLine'] = ln
								if (hT > 0) {
									def tok = line[0..hT-1].tokenize()
									def siz = tok.size()
									record['Time'] = formatTime(tok[siz-1])
									if (record['Time'] == "UNKNOWN")
										record['Comment'] += "[$ln] Invalid Time format *** "
								}
								else {
									record['Time'] = "UNKNOWN"
									record['Comment'] += "[$ln] Found $hashTag without Time *** "
								}
							}
							else record['Comment'] += "[$ln] Found unapproved HashTag $candidate *** "
						}
						else record['Comment'] += "[$ln] Found $hashTag without HashTag *** "
					}
					else record['Comment'] += "[$ln] Found another $hashTag instead of semi-colon *** "
				}
				
				// for each control and valve variable, look for last declaration in the line, and corresponding value
				if (line.lastIndexOf('until') < 0)
					recordRef.each { k, v ->
						if (v == 'control' || v == 'valve') {
							def c = line.lastIndexOf(k)
							if (c >= 0) {
								c += k.length()
								if (v == 'control') c = controlOp(line, c)
								else if (v == 'valve') c = valveOp(line, c)
								else c = -1
								if (c >= 0)
									record[k] = c
								else
									record['Comment'] += "[$ln] $k found without value *** "
							}
						}
					}
				
				// look for semi-colon
				if (hashMode == 1) {
					def c = line.indexOf(';')
					if (c >= 0) {
						record['sLine'] = ln
						if (results.size() == 0) {
							results.add(recordRef)
							results.add(recordDef)
							def str = ""
							recordRef.each { k, v ->
								if (v == 'control' || v == 'valve')
									if (record[k] < 0) {
										str += " $k,"
										record[k] = recordDef[k]
									}
							}
							//if (str != "")
							//	record['Comment'] += "[$ln] Variables not initialized: $str *** "
						}
						
						record = calcRecord(reactor, true, record)
						results.add(record)
						warningCnt += dbStore (db, obj, record, runNumber)
						
						record = calcRecord(reactor, false, record)
						hashMode = 0
					}
				}
			}
			
			if (hashMode == 1) {
				record['Comment'] += "[$ln] Found end-of-file instead of last hashTag's semi-colon *** "
				record = calcRecord(reactor, true, record)
				results.add(record)
				warningCnt += dbStore (db, obj, record, runNumber)
			}
			/***
			else {
				record['HashTag'] = "UNKNOWN"
				record['Comment'] += "Variables declared after last hashTag's semi-colon *** "
				record = calcRecord(reactor, true, record)
				results.add(record)
				warningCnt += dbStore (db, obj, record, runNumber)
			}
			***/
			
			def warningStr = ''
			if (warningCnt != 0) {
				warningStr = warningCnt.toString() + " warning(s)"
				results[1].Comment = warningStr
			}
				
			if (runNumber != "") {
				def dr = db.dataReport.find(new BasicDBObject("code", runNumber))?.collect{it}[0]
				if (!dr) {
					dr = new BasicDBObject()
					dr.put("code", runNumber)
					dr.put("parentCode", null)
				}
				obj.put("recipeWarningCnt", warningCnt)
				dr.put("value", obj)
				
				db.dataReport.save(dr)
			}
		
		} catch (Exception exc) {
			throw new RuntimeException(exc.getMessage() + " in line $ln >>$line<<")
		}
		
		finally {
			if (br != null)
				br.close()
			if (fr != null)
				fr.close()
			br = null
			fr = null
		}

		results
	}

	private def determineFileEncoding(def file) {
		FileInputStream fr = null
		InputStreamReader br = null
		
		fr = new FileInputStream(file)
		br = new InputStreamReader(fr)
		def line = br.readLine()
		if (br != null)
			br.close()
		if (fr != null)
			fr.close()
		br = null
		fr = null
		
		if (line[0] >= 128)
			return("UTF-16")
		else
			return("UTF-8")
	}
	
	private validateHashTag (def reactor, def hashTag) {
		
		def processStep = workflowService.getProcessStep("EquipmentDC", "recipeSummary", hashTag)

		if (!processStep) {
			return false
			//processStep  = new ProcessStep(taskKey:record.HashTag)
			//def proc = Process.findByPkey("recipeSummary")
			//proc.addToProcessSteps(processStep)
			//processStep.save()
		}

		if (processStep.taskKey != hashTag) return false
		
		def codes = processStep.equipments.collect { it.code }
		if (reactor in codes) return true
		
		return false
	}	
	
	private int dbStore (def db, def obj, def record, def runNumber) {
		
		def warningCnt = 0
		if (record['Comment'] && record['Comment'] != '') warningCnt = 1
		
		if (runNumber == "") return(warningCnt)
		
		// in 'auto' mode, store "record" in dataReport collection 
		obj.put("runNumber", runNumber)
		obj.put("actualStart", new Date())
		obj.put("active", "true")
		obj.put("pkey", "recipeSummary")
		obj.put(record.HashTag, new BasicDBObject())
		obj[record.HashTag].put("active", "")
		obj[record.HashTag].put("last", "true")
		
		if (!obj["tags"]) {
			obj["tags"] = new TreeSet()
		}
		obj["tags"].add( "EquipmentDC")
		obj["tags"].add( "EquipmentDC|recipeSummary")
		obj["tags"].add( "EquipmentDC|recipeSummary|" + record.HashTag)
		
		def processStep = workflowService.getProcessStep("EquipmentDC", "recipeSummary", record.HashTag)
		def idx = 0
		
		record.each { k, v ->
			idx++
			
			if(k != null && k != "HashTag") {
				
				def varName = k.replace(".","-")
				varName = varName.replace("/","-")
				varName = varName.replace(" ","_")
				
				def dtype = ""
				if (varName in ["Time","Comment"]) {
					
					dtype = "string"
				//} else if (v == null && v.getClass() == java.lang.Double) {
				//
				//	dtype = "float"
				} else if (v.getClass() == java.lang.Integer) {
				
					dtype = "int"
				} else /* if (v.getClass() == java.math.BigDecimal) */ {
				
					dtype = "float"
					if (v != null) v = v.floatValue() 
				}
				obj[record.HashTag].put(varName, v)
									
				def variable = Variable.findByNameAndProcessStep(varName,processStep)
				
				if (!variable) {
					variable = new Variable()
					variable.idx = idx
					variable.dir = "dc"
					variable.name = varName
					variable.title = k
					variable.dataType  = dtype
					variable.allowBlank = true
					variable.processStep = processStep
					variable.save()
				}
			}
		}
		
		return(warningCnt)
	}
	
	
	private controlOp(String line, def first) {
		def eqOps = ['=', 'to']

		def c = first
		while (c < line.length() && line[c] == ' ') c++
		
		for (int i=0; i<eqOps.size(); i++) {
			def last = c + eqOps[i].length() - 1
			if (last < line.length())
				if (line[c..last] == eqOps[i]) {
					c = last + 1
					while (c < line.length() && line[c] == ' ') c++
					last = c
					while (last < line.length() && line[last] in '0'..'9') last++
					if (c < last)
						return(line[c..last-1].toInteger())
					return(-1)
				}
		}
		return(-1)
	}
	
	
	private valveOp(def line, def first) {
		def onOps = ['open', 'on']
		def offOps = ['close', 'off']

		def c = first
		while (c < line.length() && line[c] == ' ') c++
		if (c < line.length() && line[c] == '=') c++
		while (c < line.length() && line[c] == ' ') c++
		
		if (first < c) {
			for (int i=0; i<onOps.size(); i++) {
				def last = c + onOps[i].length() - 1
				if (last < line.length())
					if (line[c..last] == onOps[i]) return(1)
			}
			for (int i=0; i<offOps.size(); i++) {
				def last = c + offOps[i].length() - 1
				if (last < line.length())
					if (line[c..last] == offOps[i]) return(0)
			}
		}
		return(-1)
	}
	
	def formatTime(def inStr) {
		def outStr = ""
		int hours=0, minutes=0, seconds=0
		
		def el = inStr.tokenize(':')
		switch (el.size()) {
			case 1: if (el[0].isInteger()) seconds = el[0].toInteger()
					else outStr = "UNKNOWN" 
					break
			
			case 2: if (el[0].isInteger()) minutes = el[0].toInteger()
					else outStr = "UNKNOWN" 
					if (el[1].isInteger()) seconds = el[1].toInteger()
					else outStr = "UNKNOWN" 
					break
			
			case 3: if (el[0].isInteger()) hours = el[0].toInteger()
					else outStr = "UNKNOWN"
					if (el[1].isInteger()) minutes = el[1].toInteger()
					else outStr = "UNKNOWN"
					if (el[2].isInteger()) seconds = el[2].toInteger()
					else outStr = "UNKNOWN"
					break
					
			default: outStr = "UNKNOWN"
		}

		if (outStr == "UNKNOWN") return ("UNKNOWN")
		
		//return (String.format("%02d:%02d:%02d", hours, minutes, seconds))
		
		return (hours * 60 * 60 + minutes * 60 + seconds)
	}
	
	def safeAdd (def add1, def add2) {
		if (add1 != null && add2 != null)  return(add1 + add2)
		return(null)
	}

	def safeSub (def sub1, def sub2) {
		if (sub1 != null && sub2 != null)  return(sub1 - sub2)
		return(null)
	}

	def safeMul (def mul1, def mul2) {
		if (mul1 != null && mul2 != null)  return(mul1 * mul2)
		return(null)
	}

	def safeDiv (def div1, def div2) {
		if (div1 != null && div2 != null && div2 != 0)  return(div1 / div2)
		return(null)
	}

	private calcRecord (def reactor, def mode, def initValue) {
		def record = [:]
		def a1, a2, common, interimSum
		
		initValue.each { k, v ->
			record[k] = v
		}
		
		if (mode == false) {
			record['HashTag'] = mode ? 'header' : ''
			record['Time'] = mode ? 'header' : ''
			record['hLine'] = mode ? 'header' : ''
			record['sLine'] = mode ? 'header' : ''
			record['Comment'] = mode ? 'header' : ''
			return(record)
		}
		
		record['NH3_1.source_fpt'] = record['NH3_1.source'] * record['NH3_1.line'] * record['NH3_1.run']
		record['NH3_2.source_fpt'] = record['NH3_2.source'] * record['NH3_2.line'] * record['NH3_2.run']
		record['NH3_3.source_fpt'] = record['NH3_3.source'] * record['NH3_3.line'] * record['NH3_3.run']
		record['NH3_4.source_fpt'] = record['NH3_4.source'] * record['NH3_4.line'] * record['NH3_4.run']
		record['NH3_1.push_fpt'] = record['NH3_1.push'] * record['NH3_1.line'] * record['NH3_1.run']
		record['NH3_2.push_fpt'] = record['NH3_2.push'] * record['NH3_2.line'] * record['NH3_2.run']
		record['NH3_3.push_fpt'] = record['NH3_3.push'] * record['NH3_3.line'] * record['NH3_3.run']
		record['NH3_4.push_fpt'] = record['NH3_4.push'] * record['NH3_4.line'] * record['NH3_4.run']
		record['SiH4_2.source_fpt'] = record['SiH4_2.source'] * record['SiH4_2.line'] * record['SiH4_2.run']
		record['SiH4_2.dilute_fpt'] = record['SiH4_2.dilute'] * record['SiH4_2.line'] * record['SiH4_2.run']
		record['SiH4_2.inject_fpt'] = record['SiH4_2.inject'] * record['SiH4_2.line'] * record['SiH4_2.run']
		record['SiH4_2.push_fpt'] = record['SiH4_2.push'] * record['SiH4_2.line'] * record['SiH4_2.run']
		record['SiH4_2.press_fpt'] = record['SiH4_2.press']
		record['TMGa_1.source_fpt'] = record['TMGa_1.source'] * record['TMGa_1.line'] * record['TMGa_1.run']
		record['TMGa_1.push_fpt'] = record['TMGa_1.push'] * record['TMGa_1.line'] * record['TMGa_1.run']
		record['TMGa_1.press_fpt'] = record['TMGa_1.press']
		record['TMAl_1.source_fpt'] = record['TMAl_1.source'] * record['TMAl_1.line'] * record['TMAl_1.run']
		record['TMAl_1.push_fpt'] = record['TMAl_1.push'] * record['TMAl_1.line'] * record['TMAl_1.run']
		record['TMAl_1.press_fpt'] = record['TMAl_1.press']
		record['Cp2Mg_1.source_fpt'] = record['Cp2Mg_1.source'] * record['Cp2Mg_1.line'] * record['Cp2Mg_1.run']
		record['Cp2Mg_1.push_fpt'] = record['Cp2Mg_1.push'] * record['Cp2Mg_1.line'] * record['Cp2Mg_1.run']
		record['Cp2Mg_1.press_fpt'] = record['Cp2Mg_1.press']
		
		if (reactor == "S2") {
			record['Cp2Mg_2.source_fpt'] = record['Cp2Mg_2.source'] * record['Cp2Mg_2.line'] * record['Cp2Mg_2.run']
			record['Cp2Mg_2.push_fpt'] = record['Cp2Mg_2.push'] * record['Cp2Mg_2.line'] * record['Cp2Mg_2.run']
			record['Cp2Mg_2.press_fpt'] = record['Cp2Mg_2.press']
			interimSum = record['Cp2Mg_2.source_fpt'] + record['Cp2Mg_2.push_fpt']
		}
		else {
			record['TMGa_2.source_fpt'] = record['TMGa_2.source'] * record['TMGa_2.line'] * record['TMGa_2.run']
			record['TMGa_2.push_fpt'] = record['TMGa_2.push'] * record['TMGa_2.line'] * record['TMGa_2.run']
			record['TMGa_2.press_fpt'] = record['TMGa_2.press']
			interimSum = record['TMGa_2.source_fpt'] + record['TMGa_2.push_fpt']
		}
		
		record['TEGa_2.source_fpt'] = record['TEGa_2.source'] * record['TEGa_2.line'] * record['TEGa_2.run']
		record['TEGa_2.push_fpt'] = record['TEGa_2.push'] * record['TEGa_2.line'] * record['TEGa_2.run']
		record['TEGa_2.press_fpt'] = record['TEGa_2.press']
		record['TEGa_3.source_fpt'] = record['TEGa_3.source'] * record['TEGa_3.line'] * record['TEGa_3.run']
		record['TEGa_3.push_fpt'] = record['TEGa_3.push'] * record['TEGa_3.line'] * record['TEGa_3.run']
		record['TEGa_3.press_fpt'] = record['TEGa_3.press']
		record['TMIn_1.source_fpt'] = record['TMIn_1.source'] * record['TMIn_1.line'] * record['TMIn_1.run']
		record['TMIn_1.push_fpt'] = record['TMIn_1.push'] * record['TMIn_1.line'] * record['TMIn_1.run']
		record['TMIn_1.press_fpt'] = record['TMIn_1.press']
		record['TMIn_2.source_fpt'] = record['TMIn_2.source'] * record['TMIn_2.line'] * record['TMIn_2.run']
		record['TMIn_2.push_fpt'] = record['TMIn_2.push'] * record['TMIn_2.line'] * record['TMIn_2.run']
		record['TMIn_2.press_fpt'] = record['TMIn_2.press']
		record['DummyMO1_fpt'] = record['DummyMO1.source'] * record['DummyMO1.run']
		record['DummyMO2_fpt'] = record['DummyMO2.source'] * record['DummyMO2.run']
		record['DummyHyd_fpt'] = record['DummyHyd.source'] * record['DummyHyd.run']
		
		record['SumMO_fpt'] = record['RunMO1'] + record['RunMO2'] + record['RunMOBypass1'] + record['RunMOBypass2'] + record['TMGa_1.source_fpt'] +
							record['TMGa_1.push_fpt'] + record['TMAl_1.source_fpt'] + record['TMAl_1.push_fpt'] + record['Cp2Mg_1.source_fpt'] + 
							record['Cp2Mg_1.push_fpt']  + record['TEGa_2.source_fpt'] + record['TEGa_2.push_fpt'] + record['TEGa_3.source_fpt'] + 
							record['TEGa_3.push_fpt'] + record['TMIn_2.source_fpt'] + record['TMIn_2.push_fpt'] + record['SiH4_2.inject_fpt'] + 
							record['SiH4_2.push_fpt'] + interimSum
			
		record['SumHyd_fpt'] = record['RunHydBot'] + record['RunHydTop'] + record['NH3_1.source_fpt'] + record['NH3_2.source_fpt'] + 
							record['NH3_3.source_fpt'] + record['NH3_4.source_fpt'] + record['NH3_1.push_fpt'] + record['NH3_2.push_fpt'] + 
							record['NH3_3.push_fpt'] + record['NH3_4.push_fpt'] + record['DummyHyd_fpt']		//******* 1 of 3 last changes
		
		record['Vp TMGa 0C [mbar]'] = 10 ** (8.07-1703 / (0+273.15)) * 1.3333
		record['Vp TMAl 17C [mbar]'] = 10 ** (8.224-2134.83 / (17+273.15)) * 1.3333
		record['Vp Cp2Mg 17C [mbar]'] = 10 ** (10-3372 / (17+273.15)) * 1.3333
		record['Vp TEGa 17C [mbar]'] = 10 ** (8.083-2162 / (273+17)) * 1.3333
		record['Vp TMIn 17C [mbar]'] = 10 ** (10.52-3014 / (17+273.15)) * 1.33333
		
		// starting here we have first divisions... therefore all math using 'calc' variables must be "safe"
		record['TMGa flow [sccm]'] = safeDiv (record['TMGa_1.source_fpt'] * record['Vp TMGa 0C [mbar]'], record['TMGa_1.press_fpt'] - record['Vp TMGa 0C [mbar]'])
		record['TMAl flow [sccm]'] = safeDiv (record['TMAl_1.source_fpt'] * record['Vp TMAl 17C [mbar]'], record['TMAl_1.press_fpt'] - record['Vp TMAl 17C [mbar]'])
		
		a1 = safeDiv (record['Cp2Mg_1.source_fpt'] * record['Vp Cp2Mg 17C [mbar]'], record['Cp2Mg_1.press_fpt'] - record['Vp Cp2Mg 17C [mbar]'])
		if (reactor == "S2")
			a2 = safeDiv (record['Cp2Mg_2.source_fpt'] * record['Vp Cp2Mg 17C [mbar]'], record['Cp2Mg_2.press_fpt'] - record['Vp Cp2Mg 17C [mbar]'])  //******* 2 of 3 last changes
		else
			a2 = safeDiv (record['TMGa_2.source_fpt'] * record['Vp Cp2Mg 17C [mbar]'], record['TMGa_2.press_fpt'] - record['Vp Cp2Mg 17C [mbar]'])
		
		record['Cp2Mg flow [sccm]'] = safeAdd (a1, a2)
		record['TEGa flow [sccm]'] = safeDiv (record['TEGa_2.source_fpt'] * record['Vp TEGa 17C [mbar]'], record['TEGa_2.press_fpt'] - record['Vp TEGa 17C [mbar]']) +
							safeDiv (record['TEGa_3.source_fpt'] * record['Vp TEGa 17C [mbar]'], record['TEGa_3.press_fpt'] - record['Vp TEGa 17C [mbar]'])
		
		a1 = safeDiv (record['TMIn_2.source_fpt'] * record['Vp TMIn 17C [mbar]'], record['TMIn_2.press_fpt'] - record['Vp TMIn 17C [mbar]'])
		a2 = safeDiv (record['TMIn_1.source_fpt'] * record['Vp TMIn 17C [mbar]'], record['TMIn_1.press_fpt'] - record['Vp TMIn 17C [mbar]'])
		record['TMIn flow [sccm]'] = safeAdd (a1, a2)
		record['SiH4 flow [sccm]'] = safeDiv (0.0001 * record['SiH4_2.inject_fpt'] * record['SiH4_2.source_fpt'], record['SiH4_2.source_fpt'] + record['SiH4_2.dilute_fpt'])
		
		a1 = safeAdd (record['TMGa flow [sccm]'], record['TMAl flow [sccm]'])
		a2 = safeAdd (record['TEGa flow [sccm]'], record['TMIn flow [sccm]'])
		common = safeAdd (a1, a2)

		a1 = record['NH3_1.source_fpt'] + record['NH3_2.source_fpt'] + record['NH3_3.source_fpt'] + record['NH3_4.source_fpt']
		a2 = safeAdd (common, record['Cp2Mg flow [sccm]'])
		record['V/III'] = safeDiv (a1, a2)
		
		record['Si/III'] = safeDiv (record['SiH4 flow [sccm]'], common)

		record['Est. Si level'] = safeMul (record['Si/III'], 8.9E+22 / 2)
		record['In/III'] = safeDiv (record['TMIn flow [sccm]'], common)
		
		record['Al/III'] = safeDiv (record['TMAl flow [sccm]'], common)
		record['Mg/III'] = safeDiv (record['Cp2Mg flow [sccm]'], common)
		
		record['TotalFlow'] = record['RunMO1'] + record['RunMO2'] + record['RunMOBypass1'] + record['RunMOBypass2'] + record['RunHydBot'] + record['RunHydTop'] +
							record['NH3_1.source_fpt'] + record['NH3_2.source_fpt'] + record['NH3_3.source_fpt'] + record['NH3_4.source_fpt'] + record['NH3_1.push_fpt'] +
							record['NH3_2.push_fpt'] + record['NH3_3.push_fpt'] + record['NH3_4.push_fpt'] + record['SiH4_2.inject_fpt'] + record['SiH4_2.push_fpt'] +
							record['TMGa_1.source_fpt'] + record['TMGa_1.push_fpt'] + record['TMAl_1.source_fpt'] + record['TMAl_1.push_fpt'] + record['Cp2Mg_1.source_fpt'] +
							record['Cp2Mg_1.push_fpt'] + interimSum + record['TEGa_2.source_fpt'] + record['TEGa_2.push_fpt'] +
							record['TEGa_3.source_fpt'] + record['TEGa_3.push_fpt'] + record['TMIn_1.source_fpt'] + record['TMIn_1.push_fpt'] + record['TMIn_2.source_fpt'] + 
							record['TMIn_2.push_fpt'] + record['DummyMO1_fpt'] + record['DummyMO2_fpt'] + record['DummyHyd_fpt']
		
		a1 = (record['RunMO1'] + record['RunMO2'] + record['RunHydBot'] + record['RunHydTop']) * record['H2.run']
		a2 = (record['NH3_1.push_fpt'] + record['NH3_2.push_fpt'] + record['NH3_3.push_fpt'] + record['NH3_4.push_fpt'] + record['SiH4_2.inject_fpt'] + 
							record['SiH4_2.push_fpt'] + record['DummyHyd_fpt']) * record['H2.hyd']
		record['H2Flow'] = a1 + a2
		
		a1 = (record['TMGa_1.source_fpt'] + record['TMGa_1.push_fpt'] + record['TMAl_1.source_fpt'] + record['TMAl_1.push_fpt'] + record['Cp2Mg_1.source_fpt'] +
							record['Cp2Mg_1.push_fpt'] + interimSum + record['DummyMO1_fpt']) * record['H2.line1']
		a2 = (record['TEGa_2.source_fpt'] + record['TEGa_2.push_fpt'] + record['TEGa_3.source_fpt'] + record['TEGa_3.push_fpt'] + 
							record['TMIn_1.source_fpt'] + record['TMIn_1.push_fpt'] +			//******* 3 of 3 last changes 
							record['TMIn_2.source_fpt'] + record['TMIn_2.push_fpt'] + record['DummyMO2_fpt']) * record['H2.line2']
		record['H2Flow'] += a1 + a2
		
		if (reactor == "S2") {
			a1 = record['RunMOBypass1'] * record['H2MOPurge']
			a2 =  record['RunMOBypass2'] * record['N2MOPurge']
		}
		else {
			a1 = record['RunMOBypass1'] * record['MO1.push']
			a2 =  record['RunMOBypass2'] * record['MO2.push']
		}
		record['H2Flow'] += a1 + a2
		
		record['N2Flow'] = record['TotalFlow'] - record['H2Flow'] - record['NH3_1.source_fpt'] - record['NH3_2.source_fpt']
		
		record['H2% (H2/(H2+N2))'] = safeDiv (record['H2Flow'], record['H2Flow'] + record['N2Flow'])
		
		record['TMGa flow [umol/min]'] = safeMul (record['TMGa flow [sccm]'], 1000 / 22.4)
		record['TMAl flow [umol/min]'] = safeMul (record['TMAl flow [sccm]'], 1000 / 22.4)
		record['Cp2Mg flow [umol/min]'] = safeMul (record['Cp2Mg flow [sccm]'], 1000 / 22.4)
		record['TEGa flow [umol/min]'] = safeMul (record['TEGa flow [sccm]'], 1000 / 22.4)
		record['TMIn flow [umol/min]'] = safeMul (record['TMIn flow [sccm]'], 1000 / 22.4)
		record['SiH4 flow [umol/min]'] = safeMul (record['SiH4 flow [sccm]'], 1000 / 22.4)
		
		return(record)
	}

	
	private initRecord (def reactor, def mode) {
		def record = [:]
		
		record['HashTag'] = mode=='ref' ? 'header' : 'defaults'
		record['Time'] = mode=='ref' ? 'header' : ''
		record['hLine'] = mode=='ref' ? 'header' : ''
		record['sLine'] = mode=='ref' ? 'header' : ''
		record['Comment'] = mode=='ref' ? 'header' : ''
		
		record['ReactorPress'] = mode=='ref' ? 'control' : mode=='def' ? 133 : -1
		record['ReactorTemp'] = mode=='ref' ? 'control' : mode=='def' ? 850 : -1
		record['RunMO1'] = mode=='ref' ? 'control' : mode=='def' ? 4900 : -1
		record['RunMO2'] = mode=='ref' ? 'control' : mode=='def' ? 4900 : -1
		record['RunMOBypass1'] = mode=='ref' ? 'control' : mode=='def' ? 2450 : -1
		record['RunMOBypass2'] = mode=='ref' ? 'control' : mode=='def' ? 2450 : -1
		record['RunHydBot'] = mode=='ref' ? 'control' : mode=='def' ? 5000 : -1
		record['RunHydTop'] = mode=='ref' ? 'control' : mode=='def' ? 1000 : -1
		record['NH3_1.source'] = mode=='ref' ? 'control' : mode=='def' ? 10000 : -1
		record['NH3_2.source'] = mode=='ref' ? 'control' : mode=='def' ? 10000 : -1
		
		if (reactor == "S2") {
			record['NH3_3.source'] = mode=='ref' ? 'control' : mode=='def' ? 100 : -1
			record['NH3_4.source'] = mode=='ref' ? 'control' : mode=='def' ? 23 : -1
			record['NH3_1.push'] = mode=='ref' ? 'control' : mode=='def' ? 50 : -1
			record['NH3_2.push'] = mode=='ref' ? 'control' : mode=='def' ? 50 : -1
			record['NH3_3.push'] = mode=='ref' ? 'control' : mode=='def' ? 1000 : -1
			record['NH3_4.push'] = mode=='ref' ? 'control' : mode=='def' ? 477 : -1
		}
		else {
			record['NH3_3.source'] = mode=='ref' ? 'control' : mode=='def' ? 46 : -1
			record['NH3_4.source'] = mode=='ref' ? 'control' : mode=='def' ? 100 : -1
			record['NH3_1.push'] = mode=='ref' ? 'control' : mode=='def' ? 0 : -1
			record['NH3_2.push'] = mode=='ref' ? 'control' : mode=='def' ? 0 : -1
			record['NH3_3.push'] = mode=='ref' ? 'control' : mode=='def' ? 454 : -1
			record['NH3_4.push'] = mode=='ref' ? 'control' : mode=='def' ? 1000 : -1
		}
		
		record['SiH4_2.source'] = mode=='ref' ? 'control' : mode=='def' ? 10 : -1
		record['SiH4_2.dilute'] = mode=='ref' ? 'control' : mode=='def' ? 100 : -1
		record['SiH4_2.inject'] = mode=='ref' ? 'control' : mode=='def' ? 20 : -1
		record['SiH4_2.push'] = mode=='ref' ? 'control' : mode=='def' ? 10 : -1
		record['SiH4_2.press'] = mode=='ref' ? 'control' : mode=='def' ? 1800 : -1
		record['TMGa_1.source'] = mode=='ref' ? 'control' : mode=='def' ? 200 : -1
		record['TMGa_1.push'] = mode=='ref' ? 'control' : mode=='def' ? 300 : -1
		record['TMGa_1.press'] = mode=='ref' ? 'control' : mode=='def' ? 1000 : -1
		record['TMAl_1.source'] = mode=='ref' ? 'control' : mode=='def' ? 500 : -1
		record['TMAl_1.push'] = mode=='ref' ? 'control' : mode=='def' ? 500 : -1
		record['TMAl_1.press'] = mode=='ref' ? 'control' : mode=='def' ? 1000 : -1
		record['Cp2Mg_1.source'] = mode=='ref' ? 'control' : mode=='def' ? 100 : -1
		record['Cp2Mg_1.push'] = mode=='ref' ? 'control' : mode=='def' ? 100 : -1
		record['Cp2Mg_1.press'] = mode=='ref' ? 'control' : mode=='def' ? 1200 : -1
		
		if (reactor == "S2") {
			record['Cp2Mg_2.source'] = mode=='ref' ? 'control' : mode=='def' ? 1000 : -1
			record['Cp2Mg_2.push'] = mode=='ref' ? 'control' : mode=='def' ? 500 : -1
			record['Cp2Mg_2.press'] = mode=='ref' ? 'control' : mode=='def' ? 1000 : -1
			record['TEGa_2.source'] = mode=='ref' ? 'control' : mode=='def' ? 0 : -1
			record['TEGa_2.push'] = mode=='ref' ? 'control' : mode=='def' ? 0 : -1
			record['TEGa_2.press'] = mode=='ref' ? 'control' : mode=='def' ? 0 : -1
		}
		else {
			record['TMGa_2.source'] = mode=='ref' ? 'control' : mode=='def' ? 1000 : -1
			record['TMGa_2.push'] = mode=='ref' ? 'control' : mode=='def' ? 500 : -1
			record['TMGa_2.press'] = mode=='ref' ? 'control' : mode=='def' ? 1000 : -1
			record['TEGa_2.source'] = mode=='ref' ? 'control' : mode=='def' ? 100 : -1
			record['TEGa_2.push'] = mode=='ref' ? 'control' : mode=='def' ? 100 : -1
			record['TEGa_2.press'] = mode=='ref' ? 'control' : mode=='def' ? 1000 : -1
		}
		
		record['TEGa_3.source'] = mode=='ref' ? 'control' : mode=='def' ? 217 : -1
		record['TEGa_3.push'] = mode=='ref' ? 'control' : mode=='def' ? 283 : -1
		record['TEGa_3.press'] = mode=='ref' ? 'control' : mode=='def' ? 1000 : -1
		record['TMIn_1.source'] = mode=='ref' ? 'control' : mode=='def' ? 100 : -1
		record['TMIn_1.push'] = mode=='ref' ? 'control' : mode=='def' ? 50 : -1
		record['TMIn_1.press'] = mode=='ref' ? 'control' : mode=='def' ? 1000 : -1
		record['TMIn_2.source'] = mode=='ref' ? 'control' : mode=='def' ? 50 : -1
		record['TMIn_2.push'] = mode=='ref' ? 'control' : mode=='def' ? 100 : -1
		record['TMIn_2.press'] = mode=='ref' ? 'control' : mode=='def' ? 1000 : -1
		record['DummyMO1.source'] = mode=='ref' ? 'control' : mode=='def' ? 500 : -1
		record['DummyMO2.source'] = mode=='ref' ? 'control' : mode=='def' ? 2000 : -1
		
		if (reactor == "S2")
			record['DummyHyd.source'] = mode=='ref' ? 'control' : mode=='def' ? 2950 : -1
		else
			record['DummyHyd.source'] = mode=='ref' ? 'control' : mode=='def' ? 0 : -1
		
		record['H2.run'] = mode=='ref' ? 'valve' : mode=='def' ? 0 : -1
		record['H2.hyd'] = mode=='ref' ? 'valve' : mode=='def' ? 0 : -1
		record['H2.line1'] = mode=='ref' ? 'valve' : mode=='def' ? 1 : -1
		record['H2.line2'] = mode=='ref' ? 'valve' : mode=='def' ? 0 : -1
		
		if (reactor == "S2") {
			record['H2MOPurge'] = mode=='ref' ? 'valve' : mode=='def' ? 0 : -1
			record['N2MOPurge'] = mode=='ref' ? 'valve' : mode=='def' ? 0 : -1
		}
		else {
			record['MO1.push'] = mode=='ref' ? 'valve' : mode=='def' ? 0 : -1
			record['MO2.push'] = mode=='ref' ? 'valve' : mode=='def' ? 0 : -1
		}
		
		record['NH3_1.line'] = mode=='ref' ? 'valve' : mode=='def' ? 1 : -1
		record['NH3_1.run'] = mode=='ref' ? 'valve' : mode=='def' ? 1 : -1
		record['NH3_2.line'] = mode=='ref' ? 'valve' : mode=='def' ? 1 : -1
		record['NH3_2.run'] = mode=='ref' ? 'valve' : mode=='def' ? 1 : -1
		
		if (reactor == "S2") {
			record['NH3_3.line'] = mode=='ref' ? 'valve' : mode=='def' ? 0 : -1
			record['NH3_3.run'] = mode=='ref' ? 'valve' : mode=='def' ? 0 : -1
			record['NH3_4.line'] = mode=='ref' ? 'valve' : mode=='def' ? 1 : -1
			record['NH3_4.run'] = mode=='ref' ? 'valve' : mode=='def' ? 1 : -1
		}
		else {
			record['NH3_3.line'] = mode=='ref' ? 'valve' : mode=='def' ? 1 : -1
			record['NH3_3.run'] = mode=='ref' ? 'valve' : mode=='def' ? 1 : -1
			record['NH3_4.line'] = mode=='ref' ? 'valve' : mode=='def' ? 0 : -1
			record['NH3_4.run'] = mode=='ref' ? 'valve' : mode=='def' ? 0 : -1
		}
		
		record['SiH4_2.line'] = mode=='ref' ? 'valve' : mode=='def' ? 0 : -1
		record['SiH4_2.run'] = mode=='ref' ? 'valve' : mode=='def' ? 0 : -1
		record['TMGa_1.line'] = mode=='ref' ? 'valve' : mode=='def' ? 0 : -1
		record['TMGa_1.run'] = mode=='ref' ? 'valve' : mode=='def' ? 0 : -1
		record['TMAl_1.line'] = mode=='ref' ? 'valve' : mode=='def' ? 0 : -1
		record['TMAl_1.run'] = mode=='ref' ? 'valve' : mode=='def' ? 0 : -1
		record['Cp2Mg_1.line'] = mode=='ref' ? 'valve' : mode=='def' ? 0 : -1
		record['Cp2Mg_1.run'] = mode=='ref' ? 'valve' : mode=='def' ? 0 : -1
		
		if (reactor == "S2") {
			record['Cp2Mg_2.line'] = mode=='ref' ? 'valve' : mode=='def' ? 0 : -1
			record['Cp2Mg_2.run'] = mode=='ref' ? 'valve' : mode=='def' ? 0 : -1
		}
		else {
			record['TMGa_2.line'] = mode=='ref' ? 'valve' : mode=='def' ? 0 : -1
			record['TMGa_2.run'] = mode=='ref' ? 'valve' : mode=='def' ? 0 : -1
		}

		record['TEGa_2.line'] = mode=='ref' ? 'valve' : mode=='def' ? 0 : -1
		record['TEGa_2.run'] = mode=='ref' ? 'valve' : mode=='def' ? 0 : -1
		record['TEGa_3.line'] = mode=='ref' ? 'valve' : mode=='def' ? 0 : -1
		record['TEGa_3.run'] = mode=='ref' ? 'valve' : mode=='def' ? 0 : -1
		record['TMIn_1.line'] = mode=='ref' ? 'valve' : mode=='def' ? 0 : -1
		record['TMIn_1.run'] = mode=='ref' ? 'valve' : mode=='def' ? 0 : -1
		record['TMIn_2.line'] = mode=='ref' ? 'valve' : mode=='def' ? 0 : -1
		record['TMIn_2.run'] = mode=='ref' ? 'valve' : mode=='def' ? 0 : -1
		record['DummyHyd.run'] = mode=='ref' ? 'valve' : mode=='def' ? 0 : -1
		record['DummyMO1.run'] = mode=='ref' ? 'valve' : mode=='def' ? 0 : -1
		record['DummyMO2.run'] = mode=='ref' ? 'valve' : mode=='def' ? 1 : -1
		
		if (reactor == "S2")
			record['CeilingTemp'] = mode=='ref' ? 'control' : mode=='def' ? 190 : -1
		else
			record['CeilingTemp_Optris'] = mode=='ref' ? 'control' : mode=='def' ? 190 : -1
		
		record['NH3_1.source_fpt'] = mode=='ref' ? 'fpt' : -1
		record['NH3_2.source_fpt'] = mode=='ref' ? 'fpt' : -1
		record['NH3_3.source_fpt'] = mode=='ref' ? 'fpt' : -1
		record['NH3_4.source_fpt'] = mode=='ref' ? 'fpt' : -1
		record['NH3_1.push_fpt'] = mode=='ref' ? 'fpt' : -1
		record['NH3_2.push_fpt'] = mode=='ref' ? 'fpt' : -1
		record['NH3_3.push_fpt'] = mode=='ref' ? 'fpt' : -1
		record['NH3_4.push_fpt'] = mode=='ref' ? 'fpt' : -1
		record['SiH4_2.source_fpt'] = mode=='ref' ? 'fpt' : -1
		record['SiH4_2.dilute_fpt'] = mode=='ref' ? 'fpt' : -1
		record['SiH4_2.inject_fpt'] = mode=='ref' ? 'fpt' : -1
		record['SiH4_2.push_fpt'] = mode=='ref' ? 'fpt' : -1
		record['SiH4_2.press_fpt'] = mode=='ref' ? 'fpt' : -1
		record['TMGa_1.source_fpt'] = mode=='ref' ? 'fpt' : -1
		record['TMGa_1.push_fpt'] = mode=='ref' ? 'fpt' : -1
		record['TMGa_1.press_fpt'] = mode=='ref' ? 'fpt' : -1
		record['TMAl_1.source_fpt'] = mode=='ref' ? 'fpt' : -1
		record['TMAl_1.push_fpt'] = mode=='ref' ? 'fpt' : -1
		record['TMAl_1.press_fpt'] = mode=='ref' ? 'fpt' : -1
		record['Cp2Mg_1.source_fpt'] = mode=='ref' ? 'fpt' : -1
		record['Cp2Mg_1.push_fpt'] = mode=='ref' ? 'fpt' : -1
		record['Cp2Mg_1.press_fpt'] = mode=='ref' ? 'fpt' : -1
		
		if (reactor == "S2") {
			record['Cp2Mg_2.source_fpt'] = mode=='ref' ? 'fpt' : -1
			record['Cp2Mg_2.push_fpt'] = mode=='ref' ? 'fpt' : -1
			record['Cp2Mg_2.press_fpt'] = mode=='ref' ? 'fpt' : -1
		}
		else {
			record['TMGa_2.source_fpt'] = mode=='ref' ? 'fpt' : -1
			record['TMGa_2.push_fpt'] = mode=='ref' ? 'fpt' : -1
			record['TMGa_2.press_fpt'] = mode=='ref' ? 'fpt' : -1
		}
		
		record['TEGa_2.source_fpt'] = mode=='ref' ? 'fpt' : -1
		record['TEGa_2.push_fpt'] = mode=='ref' ? 'fpt' : -1
		record['TEGa_2.press_fpt'] = mode=='ref' ? 'fpt' : -1
		record['TEGa_3.source_fpt'] = mode=='ref' ? 'fpt' : -1
		record['TEGa_3.push_fpt'] = mode=='ref' ? 'fpt' : -1
		record['TEGa_3.press_fpt'] = mode=='ref' ? 'fpt' : -1
		record['TMIn_1.source_fpt'] = mode=='ref' ? 'fpt' : -1
		record['TMIn_1.push_fpt'] = mode=='ref' ? 'fpt' : -1
		record['TMIn_1.press_fpt'] = mode=='ref' ? 'fpt' : -1
		record['TMIn_2.source_fpt'] = mode=='ref' ? 'fpt' : -1
		record['TMIn_2.push_fpt'] = mode=='ref' ? 'fpt' : -1
		record['TMIn_2.press_fpt'] = mode=='ref' ? 'fpt' : -1
		record['DummyMO1_fpt'] = mode=='ref' ? 'fpt' : -1
		record['DummyMO2_fpt'] = mode=='ref' ? 'fpt' : -1
		record['DummyHyd_fpt'] = mode=='ref' ? 'fpt' : -1
		
		record['SumMO_fpt'] = mode=='ref' ? 'fpt' : -1
		record['SumHyd_fpt'] = mode=='ref' ? 'fpt' : -1

		record['Vp TMGa 0C [mbar]'] = mode=='ref' ? 'calc' : -1
		record['Vp TMAl 17C [mbar]'] = mode=='ref' ? 'calc' : -1
		record['Vp Cp2Mg 17C [mbar]'] = mode=='ref' ? 'calc' : -1
		record['Vp TEGa 17C [mbar]'] = mode=='ref' ? 'calc' : -1
		record['Vp TMIn 17C [mbar]'] = mode=='ref' ? 'calc' : -1
		
		record['TMGa flow [sccm]'] = mode=='ref' ? 'calc' : -1
		record['TMAl flow [sccm]'] = mode=='ref' ? 'calc' : -1
		record['Cp2Mg flow [sccm]'] = mode=='ref' ? 'calc' : -1
		record['TEGa flow [sccm]'] = mode=='ref' ? 'calc' : -1
		record['TMIn flow [sccm]'] = mode=='ref' ? 'calc' : -1
		record['SiH4 flow [sccm]'] = mode=='ref' ? 'calc' : -1
		
		record['V/III'] = mode=='ref' ? 'calc' : -1
		
		record['Si/III'] = mode=='ref' ? 'calc' : -1
		record['Est. Si level'] = mode=='ref' ? 'calc' : -1
		record['In/III'] = mode=='ref' ? 'calc' : -1
		record['Al/III'] = mode=='ref' ? 'calc' : -1
		record['Mg/III'] = mode=='ref' ? 'calc' : -1
		
		record['TotalFlow'] = mode=='ref' ? 'calc' : -1
		record['H2Flow'] = mode=='ref' ? 'calc' : -1
		record['N2Flow'] = mode=='ref' ? 'calc' : -1
		record['H2% (H2/(H2+N2))'] = mode=='ref' ? 'calc' : -1
		
		record['TMGa flow [umol/min]'] = mode=='ref' ? 'calc' : -1
		record['TMAl flow [umol/min]'] = mode=='ref' ? 'calc' : -1
		record['Cp2Mg flow [umol/min]'] = mode=='ref' ? 'calc' : -1
		record['TEGa flow [umol/min]'] = mode=='ref' ? 'calc' : -1
		record['TMIn flow [umol/min]'] = mode=='ref' ? 'calc' : -1
		record['SiH4 flow [umol/min]'] = mode=='ref' ? 'calc' : -1
		
		return(record)
	}
}