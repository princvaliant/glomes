package com.glo.run

import grails.converters.JSON
import grails.web.JSONBuilder
import javax.servlet.ServletOutputStream
import org.apache.commons.logging.LogFactory
import com.glo.ndo.*
import com.mongodb.BasicDBObject
import java.text.SimpleDateFormat
import org.apache.poi.xssf.usermodel.XSSFWorkbook

class EquipmentsController extends Rest {

	private static final logr = LogFactory.getLog(this)
	
	def springSecurityService
	def utilsService
	def equipmentService
	def mongo

	def list = {

		def equipments
		if (params.location) {
			equipments = equipmentService.getByLocation(params.location)
		} else if (params.processKey && params.taskKey) {
			equipments = equipmentService.getByPkeyAndTkey(params.processKey, params.taskKey)
		} else if (params.onlyDc == "true") {
			equipments = equipmentService.getByAllowDataCollection()
			render (['data': equipments.collect {[id:it.id, code:it.code, name:it.name]}, 'count':equipments.size()] as JSON)
			return
		} else {
			equipments = equipmentService.get(params)[0]
		}

		render (['data': equipments.collect {[it.id, it.name]}, 'count':equipments.size()] as JSON)
	}
	
	def add = {
		logr.debug(params)
		try {
			def username =  springSecurityService.principal?.username
			def equipment =  request.JSON
			equipmentService.add(username, equipment.name, equipment.comment)
			render ([ok: true, success:true] as JSON)
		}
		catch(Exception exc) {
			logr.error(exc)
			render ([ok: false, success:false, msg: exc.toString()] as JSON)
		}
	}
	
	def update = {
		
		logr.debug(params)
		try {
			def username =  springSecurityService.principal?.username
			def equipment =  request.JSON
			equipmentService.update(username, equipment)
			render ([ok: true, success:true] as JSON)
		}
		catch(Exception exc) {
			logr.error(exc)
			render ([ok: false, success:false, msg: exc.toString()] as JSON)
		}
	}


	def delete = {
		
		logr.debug(params)
		Equipment.get(params.id).delete()
		render ([success:true] as JSON)
	}
	
	def statusChange = {
		
		logr.debug(params)
		try {
			def username =  springSecurityService.principal?.username
			def str =  params.units.toString().replace("\\", "")
			def equipments = JSON.parse(str)
			equipmentService.changeStatus (username, params.status, params.subStatus, params.failureCode, params.date, params.time, params.note, equipments)
			render ([success:true] as JSON)
		}
		catch (Exception exc) {
			logr.error(exc)
			render ([success:false, msg: exc.toString()] as JSON)
		}
	}
	
	def deleteStatus = {
		try {
			def username =  springSecurityService.principal?.username
			equipmentService.deleteStatus (username, params.id)
		} catch (Exception exc) {
			logr.error(exc)
		}
		render ([success:true] as JSON)
	}
	
	
	def editComment = {
		try {
			def username =  springSecurityService.principal?.username
			equipmentService.editComment (username, params.statusId, params.note)
		} catch (Exception exc) {
			logr.error(exc)
		}
		render ([success:true] as JSON)
	}
	
	def getAll = {
		
		def equipments = equipmentService.get(params)
		render (['data': equipments[0], 'count':equipments[1]] as JSON)
	}
	
	def getStatuses = {

        def equipmentStatuses = equipmentService.getStatuses(params)
        render (['data': equipmentStatuses[0], 'count':equipmentStatuses[1]] as JSON)
    }

    def getCurrentStatus = {

        def equipmentStatus = equipmentService.getStatus(params)
        render (equipmentStatus as JSON)
    }

	def export = {
		logr.debug(params)

		def data = equipmentService.getHistory(params)
		
		XSSFWorkbook workbook = utilsService.exportExcel(data, "")

		response.setHeader("Content-disposition", "attachment; filename=data.xlsx")
		response.contentType = "application/excel"
		ServletOutputStream f = response.getOutputStream()
		workbook.write(f)
		f.close()
	}
	
	def getFailures = {
		
		def equipmentFailures = equipmentService.getFailures(params)
		render (['data': equipmentFailures, 'count':equipmentFailures.size()] as JSON)
	}
	
	def getUnscheduled = {
		
		def equipmentUnscheduled = equipmentService.getUnscheduled(params)
		render (['data': equipmentUnscheduled, 'count':equipmentUnscheduled.size()] as JSON)
	}
	
	def getMaintenance = {
		
		def equipmentMaintenance = equipmentService.getMaintenance(params)
		render (['data': equipmentMaintenance[0], 'count':equipmentMaintenance[1]] as JSON)
	}
	
	def editMaintenance = {
		try {
			logr.debug(params)
			
			def username =  springSecurityService.principal?.username
			
			equipmentService.changeMaintenance(username,params)
			
		} catch (Exception exc) {
			logr.error(exc)
		}
		render ([success:true] as JSON)
	}
	
	def deleteMaintenance = {
		try {
			def username =  springSecurityService.principal?.username
			equipmentService.deleteMaintenance (username, params.id)
		} catch (Exception exc) {
			logr.error(exc)
		}
		render ([success:true] as JSON)
	}
	
	def getFields = {
		
		def builder = new JSONBuilder()
		def results = builder.build {
			fields = array {
				var0 = {
					name = "id"
					type = "int"
				}
				var1 = {
					name = "name"
					type = "string"
				}
				var2  =  {
					name = "status"
					type = "string"
				}
				var3  =  {
					name = "subStatus"
					type = "string"
				}
				var4  =  {
					name = "failureCode"
					type = "string"
				}
				var5  =  {
					name = "dateStart"
					type = "date"
				}
				var6  =  {
					name = "department"
					type = "string"
				}
				var7  =  {
					name = "tag"
					type = "string"
				}
				var8  =  {
					name = "comment"
					type = "string"
				}
			}
			columns = array {
				var1 = {
					dataIndex = "name"
					id = "grd6equipname"
					text = "Name"
					editor = true
					flex = 3
					field = {
						xtype = 'textfield'
						allowBlank = false
					}
				}
				var2 = {
					dataIndex = "status"
					id = "grd6equipstatus"
					text = "Status"
					flex = 2
				}
				var3 = {
					dataIndex = "subStatus"
					id = "grd6equipss"
					text = "Code"
					flex = 1
				}
				var4 = {
					dataIndex = "failureCode"
					id = "grd6equipfc"
					text = "Failure"
					flex = 1
				}
				var5 = {
					dataIndex = "dateStart"
					id = "grd6equipdate"
					text = "In current status from"
					xtype = "datecolumn"
					format = "Y-m-d H:i:s"
				    flex = 3
				}
				var6 = {
					dataIndex = "comment"
					id = "grd6equipcmnt"
					text = "Comment"
					editor = true
					flex = 4
					field = {
						xtype = 'textfield'
						allowBlank = true
					}
				}
			}
		}
		render (results)
	}
	
	def chart = {
		
		def ret = []

		def str =  params.ids.toString().replace("\\", "")
		def ids = JSON.parse(str)
		if (ids) {
			def bdo = new BasicDBObject()
			def bdo2 = new BasicDBObject()
			def bdo3 = new BasicDBObject()
			bdo.put('$in', ids.collect {it.id} )
			bdo2.put('eid', bdo)
			bdo3.put('_id', bdo)
			
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			def sd = formatter.parse(params.startDate) 
			def ed = formatter.parse(params.endDate) 
			
			Calendar sc = new GregorianCalendar()
			sc.setTimeInMillis(sd.getTime());
			Calendar se = new GregorianCalendar()
			se.setTimeInMillis(ed.getTime());
			sd = sc.getTime() - sc.get(Calendar.DAY_OF_WEEK)
			ed = se.getTime() - se.get(Calendar.DAY_OF_WEEK) + 7
			def numberOfDays = ed - sd
			
			def dateRange = [:]
			Calendar cal = new GregorianCalendar()
			Calendar cal2 = new GregorianCalendar()
			
			for (int i = 1; i <= numberOfDays; i += 7) {
				cal.setTimeInMillis((sd+i).getTime());
				def w = cal.get(Calendar.WEEK_OF_YEAR)
				def y = cal.get(Calendar.YEAR)
				dateRange.put( sd+i,  w)
			}
			
			def db = mongo.getDB("glo")
			def target = 0
			ids.each {
				target +=  Equipment.get(it["id"])?.e10WeeklyTarget ?: 120
			}
			
			def periods = [:]
			def titles = [:]
			def goal = [:]
			dateRange.each { k,v ->
				periods.put(v, ['processing':0.0, 'engineering':0.0, 'idle':0.0, 'scheduled':0.0, 'unscheduled':0.0, 'nonscheduled':0.0])
				titles.put(v, [:])
				goal.put(v, target)
			}
		
			bdo2.put('dateStart',  new BasicDBObject('$gte', sd - 30))
			

			def equipmentStatuses = db.equipmentStatus.find(bdo2, new BasicDBObject()).collect{it}
			equipmentStatuses.each { equipStatus ->
				
				def   duration = 0.0
				if (equipStatus.dateStart) {
					cal.setTimeInMillis(equipStatus.dateStart.getTime())
				} 
				if (equipStatus.dateEnd) {
					cal2.setTimeInMillis(equipStatus.dateEnd.getTime())
				} else {
					cal2.setTimeInMillis(new Date().getTime())
				}
							
				for (dt in dateRange.keySet()) { 
					
					duration = 0.0
					
					// if dateStart is before and dateEnd is within current range
					if (cal.getTimeInMillis() <= dt.getTime() &&
						cal2.getTimeInMillis() > dt.getTime()   && 
						cal2.getTimeInMillis() <= (dt+7).getTime()) {
						
						duration +=  cal2.getTimeInMillis() - dt.getTime()
					}
						
					// if dateStart is before and dateEnd is not within current
					if (cal.getTimeInMillis() <= dt.getTime() &&
						cal2.getTimeInMillis() > (dt+7).getTime()) {
						duration += 604800000.0
					}
							
					// if dateStart and dateEnd fall in the same range
					if (cal.getTimeInMillis() > dt.getTime() &&
						cal.getTimeInMillis() <= (dt+7).getTime() &&
						cal2.getTimeInMillis() > dt.getTime()   &&
						cal2.getTimeInMillis() <= (dt+7).getTime()) {
							
						duration = cal2.getTimeInMillis() - cal.getTimeInMillis()
					}
						
					// if dateEnd falls out of range
					if (cal.getTimeInMillis() > dt.getTime() &&
						cal.getTimeInMillis() <= (dt+7).getTime() &&
						cal2.getTimeInMillis() > (dt+7).getTime()) {
							
						duration += (dt+7).getTime() - cal.getTimeInMillis() 
					}
						
					if (duration > 0) {
						periods[dateRange[dt]][equipStatus.status] = periods[dateRange[dt]][equipStatus.status] + duration /3600000
						if (!titles[dateRange[dt]].containsKey(equipStatus.status)) {
							titles[dateRange[dt]].put(equipStatus.status, [])
						}
						titles[dateRange[dt]][equipStatus.status].add (equipStatus.comment)
					}
				}
			}	
		
			periods.keySet().each { k ->
				def remaining = 168.0 * ids.size()
				def v = periods[k]
				v.each { k1, v1 ->
					if (k1 != 'nonscheduled')
						remaining -= v1
						if (remaining < 0) remaining = 0
				}
				v['nonscheduled'] = remaining 
				
				ret.add(['period':k] + ['titles':titles[k]] + ['goal':goal[k]] + v)
			}
		}
	
		if (!ret) {
			ret.add(['':0])
		}

		render (ret as JSON)
	}
		
		def chart2 = {
			
			logr.debug(params)
			
			def str =  params.ids.toString().replace("\\", "")
			def ids = JSON.parse(str)
			
			def bdo = new BasicDBObject()
			def bdo2 = new BasicDBObject()
			bdo.put('$in', ids.collect {it.id} )
			bdo2.put('eid', bdo)
			
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			def sd = formatter.parse(params.startDate)
			def ed = formatter.parse(params.endDate) + 1
			
			def bdodate = new BasicDBObject()
			bdodate.put('$gte', sd)
			bdodate.put('$lt', ed)
			bdo2.put('dateStart', bdodate)
			
			def db = mongo.getDB("glo")
			
			db.equipmentStatus.mapReduce(
					"""
					function map() {
						 emit({'name':this.name,'status':this.status}, {'total': this.duration ? this.duration : (new Date().getTime() - this.dateStart.getTime()) / 1000  , 'date':this.dateStart});
					}
					""",
					"""
					function reduce(key, values) {
						var total = 0;
						for (var i = 0; i < values.length; i++) {
							if (values[i].total == undefined) {
								total += (new Date().getTime() - values[i].date.getTime()) / 1000  ;
							} else {
								total += values[i].total ;
							}
						}
						return {total:total}
					}
					""",
					"erresult",
					bdo2
			)
			
			def rs = db.erresult.find().collect { it }
			def grp = rs.groupBy { it._id.name }
			def ret = []
			grp.keySet().each { k ->
				def retobj = [:]
				retobj.put('equipment', k)
				grp[k].each {
					retobj.put(it._id.status, it.value.total)
				}
				ret.add(retobj)
			}
		
		
		if (!ret) {
			ret.add(['':0])
		}

		render (ret as JSON)
	}
}
