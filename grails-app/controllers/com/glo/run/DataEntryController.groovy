package com.glo.run

import com.glo.custom.EpiRun
import com.glo.ndo.Equipment
import com.mongodb.BasicDBObject
import grails.converters.JSON
import grails.web.JSONBuilder
import org.apache.commons.logging.LogFactory

import java.text.DateFormat
import java.text.SimpleDateFormat

class DataEntryController extends com.glo.run.Rest {

	private static final logr = LogFactory.getLog(this)
	
	def springSecurityService
	def epiRunService
    def contentService
	def mongo

	
	def save = {
		logr.debug(params)
		try {
			def username =  springSecurityService.principal?.username
			def epiRun =  params
			epiRunService.save(username, epiRun)
			render ([ok: true, success:true] as JSON)
		}
		catch(Exception exc) {
			logr.error(exc)
			render ([ok: false, success:false, msg: exc.toString()] as JSON)
		}
	}
	
	def delete = {
		
		logr.debug(params)
		EpiRun.get(params.id).delete()
		render ([success:true] as JSON)
	}
	
	def get = {
		
		logr.debug(params)
		def epiRun =  epiRunService.get(params)
		render (['data':epiRun[0], 'count':epiRun[1]] as JSON)
	}

	def getAll = {
		
		def epiRuns = epiRunService.get(params)
		render (['data': epiRuns[0], 'count':epiRuns[1]] as JSON)
	}

    def copy = {

        if (!params.runTo) {
            render ([ok: false, success:false, msg: "Target run number not valid"] as JSON)
            return
        }
        if (EpiRun.findByRunNumber(params.runTo)) {
           render ([ok: false, success:false, msg: "Target run number already exist"] as JSON)
           return
        }

        def epiRun = EpiRun.findByRunNumber(params.runFrom)
        if (!epiRun) {
            render ([ok: false, success:false, msg: "Original run number is not valid"] as JSON)
            return
        }

        def er = new EpiRun()
        er.code = epiRun.code
        er.equipmentName = epiRun.equipmentName
        er.runNumber = params.runTo

        def db = mongo.getDB("glo")
        db.epiRun.find([runNumber:params.runFrom], [:]).collect{
            it.each {k,v ->
                if (!(k in ["_id","runNumber"]))
                  er[k] = v
            }
        }
        er.save(failOnError: true)

        render ([success:true, 'data': er, 'count':1] as JSON)
    }
	
	def generateRun = {

		logr.debug(params)
		def arr = ['A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','T','U','V','X','Y','Z']
		def equip = Equipment.findByCode(params.code)
		DateFormat  df = new SimpleDateFormat("yyMMdd")
		def run = equip.code + df.format(new Date())
		def ret = ""
		for (s in arr) { 
			if (!EpiRun.findByRunNumber(run + s))
			{
				ret = run + s
				break;
			}	
		}
		render (['data': ret] as JSON)
	}

	def getListFields = {
		
		def builder = new JSONBuilder()
		def results = builder.build {
			fields = array {
				var0 = {
					name = "code"
					type = "string"
				}
				var1 = {
					name = "equipment"
					type = "string"
				}
				var2 = {
					name = "workcenter"
  					type = "string"
				}
				var3 = {
					name = "qty"
 					type = "int"
				}
			}
			columns = array {
				var0 = {
					dataIndex = "code"
                    width = 60
					id = "grd6dataentrycode"
					text = "Code"
				}
				var1 = {
					dataIndex = "equipment"
                    width = 120
					id = "grd6dataentryequip"
					text = "Equipment"
				}
				var2 = {
					dataIndex = "workcenter"
                    width = 70
					id = "grd6dataentryworkcenter"
					text = "Workcenter"
				}
				var3 = {
					dataIndex = "qty"
                    width = 50
					id = "grd6dataentryqty"
					text = "Total"
				}
			}
		}
		render (results)
	}

    def getListData = {

        def db = mongo.getDB("glo")
        def s = new BasicDBObject()
        if (params.sort) {
            int ord = params.dir == "ASC" ? 1 : -1
            if (params.sort in ['qty','workcenter']) {
                s.put(params.sort, ord)
            } else {
                s.put("_id." + params.sort, ord)
            }
        } else {
            s.put('qty', -1)
        }

        def epiRuns =  db.epiRun.aggregate(
                ['$group': [ _id: [code: '$code', equipment: '$equipmentName'], qty: [$sum:1], workcenter: [$last:'$workcenter']]],
                ['$sort': s]
        ).results()

        render (['data': epiRuns.collect {[code:it._id.code,equipment:it._id.equipment,workcenter:it.workcenter,qty:it.qty]}, 'count':epiRuns.size()] as JSON)
    }

    def getContentData = {

        def db = mongo.getDB("glo")

        def query = new BasicDBObject()
        query.put("code",params.procKey)
        if (params.search) {
            def rg = new BasicDBObject()
            rg.put ('$regex', '/*' +  params.search.trim().toUpperCase() + '/*')
            query.put ('$or', [])
            query['$or'].add(new BasicDBObject('runNumber', rg))
            query['$or'].add(new BasicDBObject('technician', rg))
            query['$or'].add(new BasicDBObject('Technician', rg))
        }

        def s = new BasicDBObject()
        if (params.sort) {
            int ord = params.dir == "ASC" ? 1 : -1
            s.put(params.sort, ord)
        } else {
            s.put('dateCreated', -1)
        }

        def variables = contentService.getVariables("EquipmentDC", params.procKey, '', '')
        def specVars = variables.findAll {it.specLimit || it.graphLimit}.groupBy {it.name}

        def epiRuns =  db.epiRun.find(query, new BasicDBObject()).skip(params.offset.toInteger()).limit(params.max.toInteger()).addSpecial('$orderby', s).collect {

            def limitCheck = ""

            specVars.each { k, v ->
                def spec = it[k]
                if (spec) {
                    def var = v[0]
                    def specLimit = var.specLimit?.tokenize(",")
                    if (specLimit && specLimit.size() == 2 && var.dataType in ['int','float','scientific'] && !(it[var.name] == null || it[var.name]?.equals(null) || it[var.name].toString().trim() == "")) {
                        if (specLimit[0].toFloat() > it[var.name].toFloat() || specLimit[1].toFloat() < it[var.name].toFloat()) {
                            limitCheck = limitCheck +  "<b>" + it[var.name].toFloat().round(3) + "</b> - " + (var.title ?: var.name)  + " (" + specLimit[0] + " - " + specLimit[1] + ")<br/>"
                        }
                    }
                    def graphLimit = var.graphLimit?.tokenize(",")
                    if (graphLimit && graphLimit.size() == 2 && var.dataType in ['int','float','scientific'] && !(it[var.name] == null || it[var.name]?.equals(null) || it[var.name].toString().trim() == "")) {
                        if (graphLimit[0].toFloat() > it[var.name].toFloat() || graphLimit[1].toFloat() < it[var.name].toFloat()) {
                            limitCheck = limitCheck +  "<b>" + it[var.name].toFloat().round(3) + "</b> - " + (var.title ?: var.name)  + " (" + graphLimit[0] + " - " + graphLimit[1] + ")<br/>"
                        }
                    }
                }
            }
            if (limitCheck) {
                it.put('specs',limitCheck)
                it.put('ok', "3")
            } else {
                it.put('ok', "0")
            }

            it
        }


        render (['data': epiRuns, 'count':params.total.toInteger()] as JSON)
    }

}
