package com.glo.run

import com.glo.ndo.ProcessStep
import com.mongodb.BasicDBObject
import grails.util.Environment
import org.apache.commons.logging.LogFactory

import javax.jms.Message
import java.text.DateFormat
import java.text.SimpleDateFormat

class HistoryService {

	private static final logr = LogFactory.getLog(this)
	def managementService
	def runtimeService
	def repositoryService
	def taskService
	def messageSource
	def workflowService
	def contentService
	def historyDataService
	def mongo
	def persistenceInterceptor
	def jmsService

	static exposes = ['jms']

	def initHistory(def action, def oldUnit, def newUnit, def options) {

		// Perform history update based on old and new version through async messaging
		def message =  [action:action, oldUnit: oldUnit, newUnit: newUnit, options: options]
		
		if (Environment.currentEnvironment != Environment.DEVELOPMENT ) {

  			jmsService.send(service: 'history', 1) {Message msg ->
				msg.object = message
				msg
			}
		} else {
			onMessage(message)
		}
	}

	def onMessage(def args) {

		try {
			persistenceInterceptor.init()

			def action = args.get("action")
			def oldUnit = args.get("oldUnit")
			def newUnit = args.get("newUnit")
			def options = args.get("options")

			def db = mongo.getDB("glo")

			// Method async executed to save history for 4 actions (start,update,move and loss)
			switch (action) {
				case "addNote":
					saveNote(db, newUnit)
					break
				case "start":
					saveStart(db, newUnit, options)
					break
				case "startDerived":
					saveStartDerived(db, oldUnit, newUnit)
					break
				case "end":
					saveEnd(db, oldUnit)
					break
				case "update":
					saveUpdate(db, oldUnit, newUnit, options)
					break
				case "move":
					saveMove(db, oldUnit, newUnit)
  					break
				case "rework":
					saveRework(db, oldUnit, newUnit, options)
					break
				case "loss":
					saveLoss(db, oldUnit, newUnit, options)
					break
				case "bonus":
					saveBonus(db, oldUnit, newUnit)
					break
				case "fileUpload":
					createFile(db, newUnit)
					break
			}
		} catch(Exception exc) {

			def newUnit = args.get("newUnit")

			logr.error(newUnit?.code + ' ' + exc)
		}
		finally {
			if (persistenceInterceptor) {
				persistenceInterceptor.flush()
				persistenceInterceptor.destroy()
			}
		}
		null
	}

	private def createFile(def db, def newUnit) {

		def hist = db.history.find(new BasicDBObject("id", newUnit["_id"]))?.collect{it}[0]
		def files = db.file.find(new BasicDBObject('unit', newUnit["_id"]))?.collect{it}
		hist["file"] = []
		files.each {
			def bdo = new BasicDBObject()
			bdo.put("userName", it["userName"])
			bdo.put("fileId", it["fileId"])
			bdo.put("name", it["name"])
			bdo.put("fileName", it["fileName"])
			bdo.put("dateCreated", it["dateCreated"])
			hist["file"].add(bdo)
		}
		hist.put("actualStart", new Date())
		db.history.save(hist)
		historyDataService.init(hist, newUnit)
	}

	private def saveStart(def db, def newUnit, def recordMove) {

		def hist = createHistUnit(newUnit)
		hist.put("dataLog", [])
		hist.put("files", [])
		def newAudit = createAudit(db, hist, newUnit, true)
        if (recordMove == true) {
            newAudit.put("firstPassDate", newAudit.actualStart)
            newAudit.put("numberOfPasses", 1)
            insertMove(db, newUnit, newAudit, "")
        }
		newAudit.put("last", true)
		hist.put("audit", [newAudit])
		createNote(db, hist, newUnit)

		hist.put("actualStart", new Date())
        if (newUnit.dummy) {
            hist.put('dummy','1')
        }
		db.history.save(hist)
		historyDataService.init(hist, newUnit)	
		hist = null	
	}

	private def saveStartDerived(def db, def oldUnit, def newUnit) {

		def hist = createHistUnit(newUnit)
		def histOld = db.history.find(new BasicDBObject("id", oldUnit._id))?.collect{it}[0]
		if (histOld != null) {
            hist.put("dataLog", [])
            hist.put("files", [])

            def prevAudit = histOld["audit"][histOld["audit"].size() - 1]
            prevAudit.get("dc")?.each { k, v ->
                if (v.getClass() == com.mongodb.BasicDBObject && v.get("setting")?.get("propagate") == "false") {
                    histOld["audit"][histOld["audit"].size() - 1].("dc").put(k, "")
                }
            }

            def newAudit = createAudit(db, hist, newUnit, true)
            newAudit.put("firstPassDate", newAudit.actualStart)
            newAudit.put("numberOfPasses", 1)
            insertMove(db, newUnit, newAudit, "")

            newAudit.put("last", true)
            hist.put("audit", histOld.audit + [newAudit])
            createNote(db, hist, newUnit)
            upsertDc(hist, null, newUnit, false)
            upsertDin(hist, null, newUnit, false)
            upsertRecp(hist, null, newUnit, false)
            upsertSpec(hist, newUnit)
            hist.put("actualStart", new Date())
            if (newUnit.dummy) {
                hist.put('dummy', '1')
            }
            db.history.save(hist)
            historyDataService.init(hist, newUnit)
            hist = null
        }
	}


	private def saveUpdate(def db, def oldUnit, def newUnit, def options) {

		DateFormat  df = new SimpleDateFormat("yyyy-MM-dd")
		def hist = db.history.find(new BasicDBObject("id", oldUnit._id))?.collect{it}[0]
		if (!hist) {
			hist = db.history.find(new BasicDBObject("code", oldUnit.code))?.collect{it}[0]
		}

		// Get all audits latest audits for this particular pctg, pkey and tkey
		hist["audit"].eachWithIndex { item, i ->
			if (item.tkey == options.taskKey && item.last == true) {
				
				def bdo = new BasicDBObject()
				
				if (newUnit["equipmentParts"] != null && newUnit["equipmentParts"].size() > 0) {
					item.put("equipmentParts", newUnit["equipmentParts"])
				}

				def vars = contentService.getStepVariables(options.processCategory, options.processKey, options.taskKey, ["dc", "calc"])
				vars.each {
					if (newUnit[it.name] != null && !it.readOnly) {
						bdo.put(it.name, newUnit[it.name])
						createLog(hist, "dc", it.name, it.dataType, oldUnit ? oldUnit[it.name] : null, newUnit[it.name], newUnit["user"],
								options.processCategory, options.processKey, options.taskKey)
					}
				}
				vars = contentService.getProcessVariables(options.processCategory, options.processKey, ["dc", "calc"])
				vars.each {
					if (newUnit[it.name] != null && !it.readOnly) {
						
						if (it.name == "actualStart" && options["isSame"] == false) {
							if (options["actualStart"])
								item.put("actualStart", options["actualStart"] )
						} else {
							item.put(it.name, newUnit[it.name])
							hist.put(it.name, newUnit[it.name])
							createLog(hist, "dc", it.name, it.dataType, oldUnit ? oldUnit[it.name] : null, newUnit[it.name], newUnit["user"],
									options.processCategory, options.processKey, options.taskKey)
						}
					}
				}
				item.put("dc", bdo)
			}
		}

		hist.put("actualStart", new Date())
        if (newUnit.dummy) {
            hist.put('dummy','1')
        }
		db.history.save(hist)
		historyDataService.init(hist, newUnit)
		hist = null
	}

	def saveMove(def db, def oldUnit, def newUnit) {

		def hist = db.history.find(new BasicDBObject("id", oldUnit._id))?.collect{it}[0]
		if (!hist) {
			hist = db.history.find(new BasicDBObject("code", oldUnit.code))?.collect{it}[0]
		}

		// Update previous audit as needed
		def prevAudit = hist["audit"][hist["audit"].size() - 1]

		def endDate = new Date()
		def duration = (endDate.getTime() - prevAudit["start"].getTime()) / 1000 as Long
		def duration_hr = (duration.toFloat()) / 3600
		prevAudit.put("end", endDate)
		prevAudit.put("duration", duration)
		prevAudit.put("duration_hr", duration_hr)
		prevAudit.put("spec", upsertSpec(hist, oldUnit))
		prevAudit.put('tkeyNext', newUnit["tkey"] )

		// Insert new audit or if ended delete row in unit table
		if (newUnit.tkey.toUpperCase() == "END") {
			db.unitarchive.save(newUnit)
			db.unit.remove(["_id": newUnit["_id"]])
            def pauditclone = prevAudit.clone()
            pauditclone.put("firstPassDate", prevAudit.actualStart)
            pauditclone.put("numberOfPasses", 1)
            insertMove(db, oldUnit, pauditclone, "END")
		} else {

			def newAudit = createAudit(db, hist, newUnit, true)
  			hist["audit"].eachWithIndex { item, i ->

				if (item.tkey == newAudit.tkey && item.pkey == newAudit.pkey) {
					hist["audit"][i].put("last", false)
					hist["audit"][i].get("dc")?.each { k, v ->
						if (v.getClass() == com.mongodb.BasicDBObject && v.get("setting")?.get("propagate") == "false") {
							hist["audit"][i].get("dc").put(k, "")
						}
					}
				}
			}
			newAudit.put('tkeyPrev', prevAudit["tkey"] )
            newAudit.put('pkeyPrev', prevAudit["pkey"] )
			newAudit.put("last", true)

			hist["audit"].add(newAudit)
            if (newUnit.dummy) {
                hist.put('dummy','1')
            }

            def firstPassDate = new TreeMap()
            def numberOfPasses = new TreeMap()
            hist["audit"].eachWithIndex { item, i ->

                def pkeytkey = item.pkey + item.tkey
                if (!firstPassDate[pkeytkey]) {
                    firstPassDate.put(pkeytkey, item.actualStart)
                }
                if (numberOfPasses[pkeytkey] == null) {
                    numberOfPasses.put(pkeytkey, 1)
                } else {
                    numberOfPasses[pkeytkey] = numberOfPasses[pkeytkey] + 1
                }

                hist["audit"][i].put("firstPassDate", firstPassDate[pkeytkey])
                hist["audit"][i].put("numberOfPasses", numberOfPasses[pkeytkey])
            }

            insertMove(db, newUnit, newAudit, "")

            createNote(db, hist, newUnit)
        }

		hist.put("actualStart", new Date())
		db.history.save(hist)
		historyDataService.init(hist, newUnit)
		hist = null
	}
	
	private def saveNote(def db, def newUnit) {
		
		def hist = db.history.find(new BasicDBObject("id", newUnit._id))?.collect{it}[0]
        if (hist) {
            createNote(db, hist, newUnit)
            db.history.save(hist)
        }
	}
	
	private def saveRework(def db, def oldUnit, def newUnit, def options) {
		
		def hist = db.history.find(new BasicDBObject("id", oldUnit._id))?.collect{it}[0]

		// Update previous audit end date and duration
		def prevAudit = hist["audit"][hist["audit"].size() - 1]

		def endDate = new Date()
		def duration = (endDate.getTime() - prevAudit["start"].getTime()) / 1000 as Long
		prevAudit.put("end", endDate)
		prevAudit.put("duration", duration)
		prevAudit.put("spec", upsertSpec(hist, oldUnit))
		prevAudit.put("rework", options["rework"])
		prevAudit.put("reworkId", options["reworkId"])

		// Insert new audit or if ended delete row in unit table
		def newAudit = createAudit(db, hist, newUnit,false)
        newAudit.put("firstPassDate", prevAudit.firstPassDate ?: prevAudit.actualStart)
        newAudit.put("numberOfPasses", prevAudit.numberOfPasses ? prevAudit.numberOfPasses + 1 : 2)
        insertMove(db,newUnit,newAudit, "")

		hist["audit"].eachWithIndex { item, i ->
			if (item.tkey == newAudit.tkey) {
				hist["audit"][i].put("last", false)
			}
		}
		if (hist["reworksTotal"] == null)
			hist["reworksTotal"] = 1
		else
			hist["reworksTotal"] += 1
			
		newAudit.put("last", true)
		hist["audit"].add(newAudit)
		createNote(db, hist, newUnit)
		
		hist.put("actualStart", new Date())
		db.history.save(hist)
		historyDataService.init(hist, newUnit)
	}

	private def saveEnd(def db, def oldUnit) {

		def hist = db.history.find(new BasicDBObject("id", oldUnit["_id"]))?.collect{it}[0]
		def prevAudit = hist["audit"][hist["audit"].size() - 1]
		def endDate = new Date()
		def duration = (endDate.getTime() - prevAudit["start"].getTime()) / 1000 as Long
		prevAudit.put("end", endDate)
		prevAudit.put("duration", duration)
		db.unitarchive.save(oldUnit)
		db.unit.remove(["_id": oldUnit["_id"]])

		hist.put("actualStart", new Date())
		db.history.save(hist)
		historyDataService.init(hist, null)
	}

	private def saveLoss(def db, def oldUnit, def newUnit, def closed) {

		def hist = db.history.find(new BasicDBObject("id", oldUnit._id))?.collect{it}[0]

		// Update previous audit end date and duration
		def prevAudit = hist["audit"][hist["audit"].size() - 1]
		if (!prevAudit["loss"]) {
			prevAudit.put("loss", [])
		}

		def bdo = new BasicDBObject()
		bdo.put("lossQty", oldUnit["qtyOut"] - newUnit["qtyOut"])
		bdo.put("yieldLoss", newUnit["yieldLoss"])
		bdo.put("yieldLossId", newUnit["yieldLossId"] )
		bdo.put("date", new Date() )
		prevAudit["loss"].add(bdo)
		prevAudit["qtyOut"] =  newUnit["qtyOut"]

		if (newUnit.tkey.toUpperCase() == "END") {
			def endDate = new Date()
			def duration = (endDate.getTime() - prevAudit["start"].getTime()) / 1000 as Long
			prevAudit.put("end", endDate)
			prevAudit.put("duration", duration)
			newUnit.tkey = prevAudit["tkey"]
			db.unitarchive.save(newUnit)
			db.unit.remove(["_id": newUnit["_id"]])

            def pauditclone = prevAudit.clone()
            pauditclone.put("firstPassDate", prevAudit.actualStart)
            pauditclone.put("numberOfPasses", 1)
            insertMove(db, newUnit, pauditclone, "END")
		}

		createNote(db, hist, newUnit)

		hist.put("actualStart", new Date())
		hist.put("closed", closed)
		hist.put("qtyOut", newUnit["qtyOut"])
		
		if (!hist["yieldLossQty"])
			hist["yieldLossQty"] = 0
		hist["yieldLossQty"] += oldUnit["qtyOut"] - newUnit["qtyOut"]
			
		hist.put("yieldLoss", newUnit["yieldLoss"])
		hist.put("yieldLossId", newUnit["yieldLossId"] )
		hist.put("yieldLossStep", prevAudit["tkey"])
		hist.put("yieldLossDate", new Date() )
		db.history.save(hist)
		historyDataService.init(hist, newUnit)
	}

	private def saveBonus(def db, def oldUnit, def newUnit) {

		def hist = db.history.find(new BasicDBObject("id", oldUnit._id))?.collect{it}[0]

		// Update previous audit end date and duration
		def prevAudit = hist["audit"][hist["audit"].size() - 1]
		if (!prevAudit["bonus"]) {
			prevAudit.put("bonus", [])
		}

		def bdo = new BasicDBObject()
		bdo.put("bonusQty", newUnit["qtyOut"] - oldUnit["qtyOut"])
		bdo.put("bonus", newUnit["bonus"])
		bdo.put("bonusId", newUnit["bonusId"] )
		bdo.put("date", new Date() )
		prevAudit["bonus"].add(bdo)
		prevAudit["qtyOut"] =  newUnit["qtyOut"]

		createNote(db, hist, newUnit)

		hist.put("actualStart", new Date())
		db.history.save(hist)
		historyDataService.init(hist, newUnit)
	}

	private def createHistUnit(def newUnit) {

		def bdo = new BasicDBObject()
		bdo.put("id", newUnit["_id"])
		bdo.put("genPath", newUnit["genPath"] ?: newUnit["code"] + ",")
		bdo.put("code", newUnit["code"] )
		bdo.put("tags", newUnit["tags"] )
		bdo.put("tagsCustom", newUnit["tagsCustom"] )
		bdo.put("product", newUnit["product"] )
		bdo.put("productCode", newUnit["productCode"] )
		bdo.put("productRevision", newUnit["productRevision"] )
		bdo.put("lotNumber", newUnit["lotNumber"] )
		bdo.put("supplierId", newUnit["supplierId"] )
		bdo.put("supplier", newUnit["supplier"] )
		bdo.put("supplierProductCode", newUnit["supplierProductCode"] )
		if (newUnit["parentCode"]) {
			bdo.put("parentCode", newUnit["parentCode"])
		}
		bdo
	}


	def createAudit(def db, def hist, def newUnit, def logit) {

		ProcessStep ps = workflowService.getProcessStep(newUnit["pctg"], newUnit["pkey"],  newUnit["tkey"])
		def vars = contentService.getProcessVariables(newUnit["pctg"], newUnit["pkey"], ["dc", "calc"])

		def bdo = new BasicDBObject()
		bdo.put("pctg", newUnit["pctg"])
		bdo.put("pkey", newUnit["pkey"])
		bdo.put("tkey", newUnit["tkey"])
		bdo.put("tname", newUnit["tname"])
		bdo.put("prior", newUnit["prior"])
		bdo.put("usrs", newUnit["usrs"])
		bdo.put("grps", newUnit["grps"])
		bdo.put("owner", newUnit["owner"])
		bdo.put("movedBy", newUnit["movedBy"])
		bdo.put("stepIdx", ps?.idx)
		bdo.put("estimateDuration", ps?.estimateDuration)
		vars.each {
			bdo.put(it.name, newUnit[it.name])
		}

		if (newUnit["company"])
			bdo.put("company", newUnit["company"])
		if (newUnit["companyId"])
			bdo.put("companyId", newUnit["companyId"])
		if (newUnit["location"])
			bdo.put("location", newUnit["location"])
		if (newUnit["locationId"])
			bdo.put("locationId", newUnit["locationId"])
		if (newUnit["equipment"]) 
			bdo.put("equipment", newUnit["equipment"])
		if (newUnit["equipmentId"])
			bdo.put("equipmentId", newUnit["equipmentId"])
		if (newUnit["operation"])
			bdo.put("operation", newUnit["operation"])
		if (newUnit["operationId"])
			bdo.put("operationId", newUnit["operationId"])
		if (newUnit["workCenter"])
			bdo.put("workCenter", newUnit["workCenter"])
		if (newUnit["workCenterId"])
			bdo.put("workCenterId", newUnit["workCenterId"])
		if (newUnit["estimateDuration"])
			bdo.put("estimateDuration", newUnit["estimateDuration"])
        if (newUnit["processStepTag"])
            bdo.put("processStepTag", newUnit["processStepTag"])
        bdo.put("isWorkInProgress", newUnit["isWorkInProgress"])
		bdo.put("qtyIn", newUnit["qtyIn"])
		bdo.put("qtyOut", newUnit["qtyOut"])
		bdo.put("start", newUnit["start"])
		bdo.put("actualStart", newUnit["actualStart"])
		bdo.put("dc",  upsertDc(hist, null, newUnit, logit))
		bdo.put("din", upsertDin(hist, null, newUnit, logit))
		bdo.put("recp", upsertRecp(hist, null, newUnit, logit))
		bdo.put("spec", upsertSpec(hist, newUnit))

		bdo
	}

	private def createNote(def db, def hist, def newUnit) {

        hist["note"] = []
        def notes = db.note.find([unit:newUnit._id]).collect {
            def bdo = new BasicDBObject()
            bdo.put("userName", it["userName"])
            bdo.put("comment", it["comment"])
            bdo.put("stepName", it["stepName"])
            bdo.put("dateCreated", it["dateCreated"])
            hist["note"].add(bdo)
        }

	}

	private def upsertDc(def hist, def old, def unit, def logit) {

		def bdo = new BasicDBObject()
		def vars = contentService.getStepVariables(unit.pctg, unit.pkey, unit.tkey, ["dc", "calc"])
		vars.each {
			if (unit[it.name] != null) {
				bdo.put(it.name, unit[it.name])
				if (logit)
					createLog(hist, "dc", it.name, it.dataType, old ? old[it.name] : null, unit[it.name], unit["user"],
						unit["pctg"], unit["pkey"], unit["tkey"])
			}
		}
		vars = contentService.getProcessVariables(unit.pctg, unit.pkey, ["dc", "calc"])
		vars.each {
			if (unit[it.name] != null) {
				hist.put(it.name, unit[it.name])
				if (logit)
					createLog(hist, "dc", it.name, it.dataType, old ? old[it.name] : null, unit[it.name], unit["user"],
						unit["pctg"], unit["pkey"], unit["tkey"])
			}
		}
		bdo
	}

	private def upsertDin(def hist, def old, def unit, def logit) {

		def bdo = new BasicDBObject()
		def vars = contentService.getStepVariables(unit.pctg, unit.pkey, unit.tkey, ["din"])
		vars.each {
			if (unit[it.name] != null) {
				bdo.put(it.name, unit[it.name])
				if (logit)
					createLog(hist, "din", it.name, it.dataType, old ? old[it.name] : null, unit[it.name], unit["user"],
						unit["pctg"], unit["pkey"], unit["tkey"])
			}
		}
		vars = contentService.getProcessVariables(unit.pctg, unit.pkey, ["din"])
		vars.each {
			if (unit[it.name] != null) {
				hist.put(it.name, unit[it.name])
				if (logit)
					createLog(hist, "din", it.name, it.dataType, old ? old[it.name] : null, unit[it.name], unit["user"],
						unit["pctg"], unit["pkey"], unit["tkey"])
			}
		}
		bdo
	}
	
	private def upsertSpec(def hist, def old) {
		
		def bdo = new BasicDBObject()
		def vars = contentService.getStepVariables(old.pctg, old.pkey, old.tkey, ["spec"])
		vars.each {
			if (it.evalScript != null) {
				bdo.put(it.name, it.evalScript)
			}
		}
		vars = contentService.getProcessVariables(old.pctg, old.pkey, ["spec"])
		vars.each {
			if (it.evalScript && hist[it.name] != it.evalScript) {
				hist.put(it.name, it.evalScript)
			}
		}
		bdo
	}

	private def upsertRecp(def hist, def old, def unit, def logit) {

		def bdo = new BasicDBObject()
		def vars = contentService.getStepVariables(unit.pctg, unit.pkey, unit.tkey, ["recp"])
		vars.each {
			if (it.defaultValue) {
				bdo.put(it.name, it.defaultValue)
				if (logit)
					createLog(hist, "recp", it.name, it.dataType, null, it.defaultValue, unit["user"],
						unit["pctg"], unit["pkey"], unit["tkey"])
			}
		}
		vars = contentService.getProcessVariables(unit.pctg, unit.pkey, ["recp"])
		vars.each {
			if (it.defaultValue && hist[it.name] != it.defaultValue) {
				hist.put(it.name, it.defaultValue)
				if (logit)
					createLog(hist, "recp", it.name, it.dataType, null, it.defaultValue, unit["user"],
						unit["pctg"], unit["pkey"], unit["tkey"])
			}
		}
		bdo
	}

	private def createLog(def hist, def dir, def varName, def varType, def valueOld, def valueNew, def user, def pctg, def pkey, def tkey) {

		if ((valueOld != null && valueOld == valueNew) || (valueOld == null && !valueNew)
		|| varType == "object" || varType == "objectArray")
			return

		def bdo = new BasicDBObject()
		bdo.put("k", dir)
		bdo.put("v", varName)
		bdo.put("c", pctg)
		bdo.put("p", pkey)
		bdo.put("t", tkey)
		if (valueOld) bdo.put("o", valueOld)
		bdo.put("n", valueNew)
		bdo.put("d", new Date())
		bdo.put("u", user)

        // Take last 1000 i shiftaj dolje
        hist["dataLog"] = hist["dataLog"].takeRight(2000)
		hist["dataLog"].add(bdo)
	}

    def insertMove(def db, def unit, def audit, def end) {

        def s = [:]
        s.put("code", unit.code)
        s.put("lotNumber", unit.lotNumber)
        s.put("productCode", unit.productCode)
        s.put("product", unit.product)
        s.put("productRevision", unit.productRevision)
        s.put("productFamily", unit.productFamily)
        s.put("isBulk", unit.isBulk)
        s.put("supplier", unit.supplier)
        s.put("supplierProductCode", unit.supplierProductCode)
        s.putAll(audit)
        if (end == "END") {
            s["tkey"] = "END"
        }

        s.remove("usrs")
        s.remove("grps")
        s.remove("dc")
        s.remove("din")
        s.remove("recp")
        s.remove("spec")
        s.remove("qtyOut")
        s.remove("equipmentParts")
        db.moves.insert(s)
    }
}
