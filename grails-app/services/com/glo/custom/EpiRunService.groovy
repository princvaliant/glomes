package com.glo.custom

import com.glo.ndo.Equipment
import com.glo.ndo.Process
import com.glo.ndo.ProcessStep
import com.glo.ndo.Variable
import com.healthmarketscience.jackcess.Database
import com.healthmarketscience.jackcess.Table
import com.mongodb.BasicDBObject
import org.apache.commons.logging.LogFactory
import java.util.regex.Matcher
import java.util.regex.Pattern
import java.text.DateFormat
import java.text.SimpleDateFormat


class EpiRunService {

	private static final logr = LogFactory.getLog(this)
	
	def managementService
	def runtimeService
	def repositoryService
	def unitService
	def contentService
	def messageSource
	def workflowService
	def sequenceGeneratorService
	def dateCheckerService
	def mongo

	def get (def parms) {

		logr.debug(parms)
		
		def epiRuns
		def total
		def srt = parms["sort"] == "id" ? "_id" : parms["sort"]
		if (parms.id) {
			def variables = contentService.getProcessVariables('EquipmentDC', parms.code, 'dc')
			def bdo = new BasicDBObject()
			bdo.put("_id", new Long(parms.id))
			def fields = new BasicDBObject()
			variables.each {
				fields.put(it.name, 1)
			}
			fields.put("runNumber", 1)
			fields.put("equipmentName", 1)
			fields.put("code", 1)
			fields.put("lastUpdated", 1)
			fields.put("dateCreated", 1)
			fields.put("workcenter", 1)
			def db = mongo.getDB("glo")
			def temp = db.epiRun.find(bdo, fields)
			epiRuns = temp.collect{[id:it._id] + it}[0]
			total = 1
		} else if (!parms.search) {
			epiRuns = EpiRun.list(offset:parms.offset, max:parms.max, sort: srt, order: parms.dir)
			total =  EpiRun.list().size()
		} else {
			epiRuns = EpiRun.findAllByRunNumberLikeOrWorkcenterLike("%" + parms.search + "%", "%" + parms.search, [offset:parms.offset, max:parms.max, sort: srt, order: parms.dir])
			total = EpiRun.findAllByRunNumberLike("%" + parms.search + "%").size()
		}
		
		[epiRuns,total]
	}
	
	
	def save (String user, def run) {
		
		def epiRun
		boolean isNew = false
 		
		def shell = new GroovyShell(this.class.classLoader)
		
		EpiRun.withTransaction { status ->
			if (run.id == "0") {
				if (EpiRun.findByRunNumber(run.runNumber)) {
					throw new RuntimeException ("Run number '" + run.runNumber + "' already exist" )
				}
				epiRun = new EpiRun()
				isNew = true
			} else {
				epiRun = EpiRun.get(run.id) 
				if (!epiRun) {
					throw new RuntimeException ("Run number with id '" + run.id + "' does not exist" )
				}
			}
			
			epiRun.runNumber = run.runNumber
			epiRun.code = run.code
			epiRun.equipmentName = run.equipmentName
			def equip = Equipment.findByName(run.equipmentName)
			epiRun['workcenter'] = equip?.workCenter?.name
			
			def variables = contentService.getProcessVariables('EquipmentDC', run.code, 'dc')
			variables.each {
				if (it.dataType == 'int' && run[it.name])
					epiRun[it.name] = Long.parseLong(run[it.name])
				else if (it.dataType == 'float' && run[it.name] )
					epiRun[it.name] = Float.parseFloat(run[it.name])
				else  if (it.dataType == 'Equipment' && run[it.name] ) {
					epiRun[it.name] = Long.parseLong(run[it.name])
					def equip2 = Equipment.get(Long.parseLong(run[it.name]))
					epiRun['reactor'] = equip2.name
				}
				else if (it.name != "dateCreated")
					epiRun[it.name] = run[it.name]
			}
			
			def calcs = contentService.getProcessVariables('EquipmentDC', run.code, 'calc')
			calcs.each {
				if (it.evalScript) {
					epiRun[it.name]  = unitService.getCalculatedValue(epiRun.dbo, run.equipmentName, shell.parse(it.evalScript), shell)
				}
			}
			
			epiRun.save(failOnError: true)
		}

	}

	
	def importEpiData(def reactor) {
	
		def file = new File("\\\\calserver03\\Growth\\DB\\" + reactor.toUpperCase() + "RecLog\\RecLogDB_V3.mdb")
		
		// check the date on the file
		if (!file.exists()  && !dateCheckerService.modified("EPIDATA2" + reactor.toUpperCase() , file) )
			return
		
		def mdb = Database.open(file)
		
		def pctg = "EquipmentDC"
		def pkey = reactor.toLowerCase() + "ds"
		
		Table tableRuns = mdb.getTable("tRuns");
		Table tableSteps = mdb.getTable("tSteps");
		Table tableStats = mdb.getTable("tStatistics");
		
		def db = mongo.getDB("glo")
			
		for(Map<String, Object> rowRun : tableRuns) {

			def rcp = rowRun.get("fRecipeName")
			def run = rowRun.get("fProductId")
			def key = rowRun.get("Key")
            def runid = rowRun.get("fRunId")
			def start = rowRun.get("fDate")
			def end = rowRun.get("fEndTime")
			
			def code = pkey + runid.toString()
					
			def dr = db.dataReport.find(new BasicDBObject("code", code))?.collect{it}[0]
			if (!dr) {
				
				
				dr = new BasicDBObject()
				dr.put("code", code)
				dr.put("parentCode", null)
			
				def obj = new BasicDBObject()
				obj.put("active", "true" )
				obj.put("pkey", pkey )
				obj.put("recipeNumber",rcp)
				obj.put("runNumber",run)
				
				def d = start.toString()
				def e = end.toString()
				DateFormat df1 = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
				DateFormat df2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
				df1.setLenient(false);
				Date d1  = df1.parse(d)
			
				obj.put("actualStart", df2.format(d1))
				obj.put("timeStart", df2.format(d1))
				
				obj.put("updated", df2.format(d1))
				
				if (e) {
					try {
						Date e1  = df1.parse(e)
						obj.put("timeEnd", df2.format(e1))
					} catch (Exception exc) {
					
					}
				}
				
				def isOneStep = false
	
				def tags = new TreeSet()
				
				for(Map<String, Object> rowStep : tableSteps) {
									
					// FInd the latest step key and just import ones with larger value
					def keyStep = rowStep.get("Key")
					
					if(rowStep != null && rowStep.get("fRun") == key) {
	
						def stepTask = rowStep.get("fStepText").toLowerCase().replace(" ", "_")
						
						def processStep = workflowService.getProcessStep(pctg, pkey, stepTask)

                        if (!processStep) {
                            processStep  = new ProcessStep()
                            processStep.taskKey  = stepTask
                            processStep.process  = Process.findByPkey(pkey)
                            processStep.save(flush: true)
                          }

						if (processStep) {
							
							def stepObj = new BasicDBObject()
							stepObj.put("tkey", stepTask)
							stepObj.put("pkey", pkey)
							stepObj.put("actualStart", df2.format(d1))
							isOneStep = true
							
							for(Map<String, Object> rowStat : tableStats) {
								
								if(rowStat != null && rowStat.get("fStep") == keyStep) {
									
									def name = rowStat.get("fDeviceName").replace(".","_")
									def value = rowStat.get("fCurrentMean")
									def variable = Variable.findByNameAndProcessStep(name,processStep)
									if (!variable) {
										variable = new Variable()
										variable.idx = 100
										variable.dir = "dc"
										variable.name = name
										variable.title = name
										variable.dataType = "float"
										variable.allowBlank = true
										variable.processStep = processStep
										variable.save(flush: true)
									}

                                    def valueDev = rowStat.get("fCurrentDev")
                                    def nameDev = name + "_STDDEV"
                                    def variableDev = Variable.findByNameAndProcessStep(nameDev,processStep)
                                    if (!variableDev) {
                                        variableDev = new Variable()
                                        variableDev.idx = 100
                                        variableDev.dir = "dc"
                                        variableDev.name = nameDev
                                        variableDev.title = nameDev
                                        variableDev.dataType = "float"
                                        variableDev.allowBlank = true
                                        variableDev.processStep = processStep
                                        variableDev.save(flush: true)
                                    }

									stepObj.put(name, value)
                                    stepObj.put(nameDev, valueDev)
								}
							}
							
							tags.add( pctg)
							tags.add( pctg + "|" + pkey)
							tags.add( pctg+ "|" + pkey + "|" + stepTask)
							
							obj.put(stepTask, stepObj)
							
						} else {
							logr.error("Process step not defined: " + stepTask )
						}
					}
				}
				
				if (isOneStep == true) {
					
					obj.put("tags", tags)
					dr.put("value", obj)
					db.dataReport.save(dr)
				}
			}
		}

        processEpiRunData()
	}

    def processEpiRecipeData() {

        def db = mongo.getDB("glo")
        def query = new BasicDBObject()
        query.put("parentCode", null)
        query.put("value.tags", "EquipmentDC|recipeSummary")
        query.put("synced", null)
        def dr = db.dataReport.find(query).collect{it}
        dr.each {
             it?.value?.each { k, v ->
                 if (v.getClass() == com.mongodb.BasicDBObject) {
                    v.each {device, value ->
                        if (!(device in ["active", "last", "Comment"])) {
                            def record = new BasicDBObject()
                            record.put("run", it.value.runNumber)
                            record.put("step", k?.replaceAll(/[^A-Za-z0-9]/, "_"))
                            record.put("device", device?.replaceAll(/[^A-Za-z0-9]/, "_"))
                            record.put("value", value)
                            db.epiRecipe.insert(record)
                            record = null
                        }
                     }
                 }
            }
            db.dataReport.update([_id:it._id], ['$set': [synced:"1"]], false, true)
            it = null
        }
        dr = null
    }

    def processEpiRunData() {

        def db = mongo.getDB("glo")
        def query = new BasicDBObject()
        query.put("parentCode", null)
        query.put("value.tags", new BasicDBObject('$in',["EquipmentDC|d1ds", "EquipmentDC|s2ds"]))
        query.put("synced", null)
        def dr = db.dataReport.find(query).collect{it}
        dr.each {
            it?.value?.each { k, v ->
                if (v.getClass() == com.mongodb.BasicDBObject) {
                    v.each {device, value ->
                        if (!(device in ["active", "last", "tkey", "pkey", "actualStart"])) {
                            def record = new BasicDBObject()
                            record.put("run", it.value.runNumber)
                            record.put("recipe", it.value.recipeNumber)
                            record.put("step", k?.replaceAll(/[^A-Za-z0-9]/, "_"))
                            record.put("device", device?.replaceAll(/[^A-Za-z0-9]/, "_"))
                            record.put("value", value)
                            db.epiReactorRun.insert(record)
                            record = null
                        }
                    }
                }
            }
            db.dataReport.update([_id:it._id], ['$set': [synced:"1"]], false, true)
            it = null
        }
        dr = null
    }

}
