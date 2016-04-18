package com.glo.run

import com.glo.ndo.*
import org.apache.commons.logging.LogFactory

import java.text.DateFormat
import java.text.SimpleDateFormat

class ContentService {

    private static final logr = LogFactory.getLog(this)
    def managementService
    def runtimeService
    def repositoryService
    def taskService
    def messageSource
    def workflowService
    def sequenceGeneratorService
    def mongo

    def calcThisValue(def p, def mode) {
        def valPref = p.evalScript

        if (valPref?.indexOf("AUTOINCREMENT") >= 0) {
            if (mode == "LAST")
                valPref = valPref.replace("AUTOINCREMENT", sequenceGeneratorService.last(p.name + (p.searchPrefix ?: "")).toString().padLeft(5, '0'))
            else
                valPref = valPref.replace("AUTOINCREMENT", sequenceGeneratorService.next(p.name + (p.searchPrefix ?: "")).toString().padLeft(5, '0'))
        }
        if (valPref?.indexOf("DATE") >= 0) {
            DateFormat df = new SimpleDateFormat("yyyyMMdd")
            valPref = valPref.replace("DATE", df.format(new Date()))
        }
        if (valPref) {
            valPref = (p.searchPrefix ?: "") + valPref
        }

        if (valPref?.indexOf("AUTO3CHAR") >= 0) {
            valPref = valPref.replace("AUTO3CHAR", sequenceGeneratorService.nextc(p.name).toString().padLeft(3, '0'))
        }

        valPref
    }

    def getThisVariable(def category, def procKey, def taskKey, def varName) {
        def process = Process.findByCategoryAndPkey(category, procKey)
        def processStep = ProcessStep.findByProcessAndTaskKey(process, taskKey)

        def variable = Variable.findByNameAndProcessStep(varName, processStep)
        if (!variable) variable = Variable.findByNameAndProcess(varName, process)

        variable
    }

    def getVariables(def category, def procKey, def taskKey, def dir) {

        def variables1 = Variable.executeQuery("""
				select v from Variable as v 
				left join v.process as p
				where (p != null and p.category = ? and p.pkey = ?)
				""", [category, procKey])

        def variables2 = Variable.executeQuery("""
				select v from Variable as v
	    		left join v.processStep as ps
				left join ps.process as p
				where  ps != null and p.category = ? and p.pkey = ? and ps.taskKey = ?
				""", [category, procKey, taskKey])

        def variables = ((variables1 ?: []) + (variables2 ?: [])).sort { it.idx }

        if (dir) {
            variables = variables.findAll { it.dir in dir }
        }

        variables
    }

    def getProcessVariables(def category, def procKey, def dir) {

        def process = Process.findByCategoryAndPkey(category, procKey)
        def variables = process?.variables?.sort { it.idx }

        if (dir) {
            variables = variables.findAll {
                dir.contains(it.dir)
            }
        }
        variables
    }

    def getStepVariables(def category, def procKey, def taskKey, def dir) {

        def process = Process.findByCategoryAndPkey(category, procKey)
        def processStep = ProcessStep.findByProcessAndTaskKey(process, taskKey)
        def variables = processStep?.variables ?: []
        if (dir && variables) {
            variables = variables?.findAll {
                dir.contains(it.dir)
            }
        }
        variables
    }

    def getVariableOptions(query) {

        def parms = query.tokenize('|||')

        def variable = Variable.executeQuery("""
			select distinct v from Variable as v
			where v.processStep.process.pkey = ? and v.processStep.taskKey =  ? and v.name = ?
		""", [parms[0], parms[1], parms[2]])

        variable.getAt(0)?.listValues.tokenize(',')
    }

    def addCommonVariables(def process) {

        process.addToVariables(new Variable(idx: 1, name: "ok", dir: "", dataType: "string", hidden: true, width: 0))
        process.addToVariables(new Variable(idx: 1, name: "id", dir: "", dataType: "int", hidden: true, width: 0))
        process.addToVariables(new Variable(idx: 1, name: "ok", dir: "", dataType: "string", hidden: true, width: 0))
        process.addToVariables(new Variable(idx: 1, name: 'specs', title: '', dataType: 'string', hidden: true, dir: "", width: 0))
        process.addToVariables(new Variable(idx: 1, name: 'updated', title: '', allowBlank: false, dataType: 'date', hidden: true, dir: ""))
        process.addToVariables(new Variable(idx: 10, dir: "", name: "code", dataType: "string", allowBlank: false, hidden: false, width: 90))
        process.addToVariables(new Variable(idx: 1020, dir: "", name: "qtyIn", title: "Qty in", dataType: "int", hidden: false, editor: false, allowBlank: false, width: 50))
        process.addToVariables(new Variable(idx: 1021, dir: "", name: "qtyOut", title: "Qty out", dataType: "int", hidden: false, editor: false, allowBlank: false, width: 50))
        process.addToVariables(new Variable(idx: 30, dir: "", name: "product", title: "Product", dataType: "string", hidden: false, width: 100))
        process.addToVariables(new Variable(idx: 40, dir: "", name: "productCode", title: "Product code", dataType: "string", hidden: false, width: 100))
        process.addToVariables(new Variable(idx: 50, dir: "", name: "lotNumber", title: "Lot #", dataType: "string", hidden: false, width: 100))
        process.addToVariables(new Variable(idx: 2, dir: "", name: "nDetails", title: "Details", dataType: "int", width: 80, hidden: true))
        process.addToVariables(new Variable(idx: 3, dir: "", name: "nNotes", title: "Notes", dataType: "int", width: 80, hidden: true))
        process.addToVariables(new Variable(idx: 4, dir: "", name: "nFiles", title: "Files", dataType: "int", width: 80, hidden: true))
        process.addToVariables(new Variable(idx: 5, dir: "", name: "processViolations", title: "Process violations", dataType: "int", allowBlank: true, hidden: true, width: 80))
        process.addToVariables(new Variable(idx: 1000, name: "pctg", dir: "", dataType: "string", hidden: true, width: 90))
        process.addToVariables(new Variable(idx: 1001, dir: "", name: "pkey", dataType: "string", hidden: false, title: "Process", width: 50))
        process.addToVariables(new Variable(idx: 1002, name: "tkey", title: "Step", dir: "", dataType: "string", hidden: true))
        process.addToVariables(new Variable(idx: 1003, name: "tkeyNext", dir: "", dataType: "string", hidden: true, allowBlank: true))
        process.addToVariables(new Variable(idx: 1004, name: "tkeyPrev", dir: "", dataType: "string", hidden: true, allowBlank: true))
        process.addToVariables(new Variable(idx: 1005, dir: "", name: "tname", title: "Step", hidden: false, dataType: "string"))
        process.addToVariables(new Variable(idx: 1010, dir: "", name: "start", dataType: "date", hidden: false))
        process.addToVariables(new Variable(idx: 1011, dir: "", name: "prior", dataType: "int", hidden: false))
        process.addToVariables(new Variable(idx: 1012, dir: "", name: "closed", title: "Closed", hidden: true, dataType: "string"))
        process.addToVariables(new Variable(idx: 1015, dir: "", name: "yieldLoss", title: "Yield loss", hidden: true, dataType: "string"))
        process.addToVariables(new Variable(idx: 1016, dir: "", name: "yieldLossStep", title: "Yield loss step", hidden: true, dataType: "string"))
        process.addToVariables(new Variable(idx: 1017, dir: "", name: "yieldLossDate", title: "Yield loss date", hidden: true, dataType: "date"))
        process.addToVariables(new Variable(idx: 83, dir: "", name: "supplier", title: "Supplier", dataType: "string", hidden: true, allowBlank: true))
        process.addToVariables(new Variable(idx: 84, dir: "", name: "supplierProductCode", title: "Supplier code", dataType: "string", hidden: true, allowBlank: true))
        process.addToVariables(new Variable(idx: 87, dir: "", name: "active", title: "Active", dataType: "string", hidden: true, allowBlank: true))
        process.addToVariables(new Variable(idx: 65, dir: "", name: "actualStart", title: "Actual date", dataType: "date", editor: true, allowBlank: true, hidden: false))
        process.addToVariables(new Variable(idx: 72, dir: "", name: "firstPassDate", title: "First pass date", dataType: "date", editor: true, allowBlank: true, hidden: false))
        process.addToVariables(new Variable(idx: 66, dir: "", name: "end", title: "End date", dataType: "date", editor: false, allowBlank: true, hidden: true))
        process.addToVariables(new Variable(idx: 67, dir: "", name: "duration", dataType: "int", hidden: true, editor: false, allowBlank: true))
        process.addToVariables(new Variable(idx: 68, dir: "", name: "duration_hr", dataType: "float", hidden: true, editor: false, allowBlank: true))
        process.addToVariables(new Variable(idx: 69, dir: "", name: "movedBy", title: "Moved by", dataType: "string", editor: false, allowBlank: true))
        process.addToVariables(new Variable(idx: 70, dir: "dc", name: "experimentId", title: "Experiment#", dataType: "string", editor: false, allowBlank: true))
        process.addToVariables(new Variable(idx: 71, dir: "in", name: "owner", title: "Owner", dataType: "User", editor: false, allowBlank: true))
    }

    def assignResources(def pctg, def pkey, def tkey, def objFrom, def objTo) {

        def processStep = workflowService.getProcessStep(pctg, pkey, tkey)
        def vars = getStepVariables(pctg, pkey, tkey, ['in'])
        vars.each {
            switch (it.dataType) {
                case 'Company':
                    if (objFrom.company) {
                        objTo['companyId'] = objFrom.company
                        objTo['company'] = Company.get(objFrom.company).name
                    } else if (it.allowBlank == false)
                        throw new RuntimeException("Company is required but not defined.")
                    break

                case 'Location':
                    if (objFrom.location) {
                        objTo['locationId'] = objFrom.location
                        objTo['location'] = Location.get(objFrom.location).name
                    } else if (it.allowBlank == false)
                        throw new RuntimeException("Location is required but not defined.")
                    break

                case 'Equipment':
                    if (objFrom.equipment) {
                        objTo['equipmentId'] = objFrom.equipment
                        objTo['equipment'] = Equipment.get(objFrom.equipment).name
                    } else if (it.allowBlank == false)
                        throw new RuntimeException("Equipment is required but not defined.")
                    break
            }
        }

        objTo['estimateDuration'] = processStep?.estimateDuration
        if (processStep?.operation) {
            objTo["operation"] = processStep.operation.name
            objTo["operationId"] = processStep.operation.id

            if (processStep?.operation.workCenter) {
                objTo["workCenter"] = processStep.operation.workCenter.name
                objTo["workCenterId"] = processStep.operation.workCenter.id
            }
        }
        if (processStep?.tag) {
            objTo["processStepTag"] = processStep?.tag
        }
        objTo["isWorkInProgress"] = processStep?.isWorkInProgress

    }

    def removeResources(def obj) {

        obj["company"] = null
        obj["companyId"] = null
        obj["location"] = null
        obj["locationId"] = null
        obj["equipment"] = null
        obj["equipmentId"] = null
        obj["operation"] = null
        obj["operationId"] = null
        obj["workCenter"] = null
        obj["workCenterId"] = null
        obj["estimateDuration"] = null
        obj.dbo?.remove("yieldLoss")
        obj.dbo?.remove("yieldLossId")
        obj.dbo?.remove("bonus")
        obj.dbo?.remove("bonusId")
        obj.dbo?.remove("company")
        obj.dbo?.remove("companyId")
        obj.dbo?.remove("location")
        obj.dbo?.remove("locationId")
        obj.dbo?.remove("equipment")
        obj.dbo?.remove("equipmentId")
        obj.dbo?.remove("operation")
        obj.dbo?.remove("operationId")
        obj.dbo?.remove("workCenter")
        obj.dbo?.remove("workCenterId")

    }


}
