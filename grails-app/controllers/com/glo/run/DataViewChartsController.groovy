package com.glo.run

import grails.converters.JSON
import grails.web.JSONBuilder

import org.apache.commons.logging.LogFactory

import com.glo.ndo.*
import com.mongodb.BasicDBObject

class DataViewChartsController extends com.glo.run.Rest {

	private static final logr = LogFactory.getLog(this)

	def springSecurityService

	def dataViewService
	def utilsService
	def mongo

	
	def getDataViewCharts = {
		
			logr.debug(params)
			def username =  springSecurityService.principal?.username
	
			def builder = new JSONBuilder()
	 
			if (params.dataViewId) {
				def dataView = DataView.get(params.dataViewId.toLong())
				def dvCharts = DataViewChart.findAllByDataView(dataView, [sort:"idx"])
			
				render ([
					data: dvCharts.collect{
						
						def var = it.dataViewVariable.variable
						def value = ""
						if (it.axis == "X" && var.dataType == "date") {
							value = it.xDateGroup
						}
						if (it.axis == "X" && var.dataType == "string") {
							value = it.xStringGroup
						}
						if (it.axis == "X" && var.dataType in ["int","float","scientific"]) {
							value = it.xNumericGroup
						}
						if (it.axis in ["Y","Z"] && var.dataType  in ["","int","float","scientific"]) {
							value = it.yAggregate
						}
						def dataType = var.dataType 
						if (var.dataType == '') {
							dataType = 'float';
						}
						if (it.axis == "S") {
							value = it.sortDir
							dataType = ""
						}
						
						[	
							dataViewChartId: it.id,
							dataViewVariableId: it.dataViewVariable.id,
							idx: it.idx,
							axis: it.axis,
							xDateGroup: it.xDateGroup,
							xNumberRange: it.xNumberRange,
							yAggregate: it.yAggregate,
							value: value,
							title: it.dataViewVariable.title ?: (var.title ?: var.name),
							dataType: dataType,
						]
					},
					count: dvCharts.size()
				] as JSON)
			} else {
				render ([] as JSON)
			}
		}
	
	def saveVariable = {
		
		logr.debug(params)
		
		DataViewChart dataViewChart
		if (params.dataViewChartId) {
			dataViewChart = DataViewChart.get(params.dataViewChartId)
		} else {
			dataViewChart = new DataViewChart()
			dataViewChart.dataViewVariable = DataViewVariable.get(params.dataViewVariableId)
			dataViewChart.dataView = dataViewChart.dataViewVariable.dataView
		}
		
		def username =  springSecurityService.principal?.username
		if (username != "admin" && dataViewChart.dataView.owner != username) {
			render ([success:false, msg:"Only owner can change report"] as JSON)
			return
		}
		
		if (params.dataType == "date" && params.axis == "X") {
			dataViewChart.xDateGroup = params.value
		}		
		if (params.dataType == "string" && params.axis == "X") {
			dataViewChart.xStringGroup = params.value
		}
		if (params.dataType in ["int","float","scientific"] && params.axis == "X") {
			dataViewChart.xNumericGroup = params.value
		}
		if (params.dataType in ["","int","float","scientific"] && params.axis in ["Y","Z"]) {
			dataViewChart.yAggregate = params.value
		}
		if (params.axis == "S") {
			dataViewChart.sortDir = params.value
		}
	
		dataViewChart.axis = params.axis
		dataViewChart.idx = 1
			
		dataViewChart.save()		
		
		render ([success:true, dataViewId:dataViewChart.dataView.id] as JSON)
	}
	
	def deleteVariable = {
		
		logr.debug(params)
		if (params.dataViewChartId) {
			def dataViewChart = DataViewChart.get(params.dataViewChartId)
			
			def username =  springSecurityService.principal?.username
			if (username != "admin" && dataViewChart.dataViewVariable.dataView.owner != username) {
				render ([success:false, msg:"Only owner can change report"] as JSON) 
				return
			}
			
			dataViewChart.delete()
			render ([success:true] as JSON)
		} else {
			render ([success:false] as JSON)
		}
	}
	
	def changeUrl = {
		
		logr.debug(params)
		
		if (params.dataViewId) {
			def dataView = DataView.get(params.dataViewId)
			dataView.urlDashboardData = params.urlValue
			
			def username =  springSecurityService.principal?.username
			if (username != "admin" && dataView.owner != username) {
				render ([success:false, msg:"Only owner can change report"] as JSON) 
				return
			}
			
			dataView.save()
			
			render ([success:true] as JSON)
		} else {
			render ([success:false] as JSON)
		}
	}

	def drawChart = {

		try {
            println(params.dataViewId)

			DataView dataView = DataView.get(params.dataViewId)
			if (!dataView) {
				render ([success:false, msg:"Select Report to refresh first"] as JSON)
				return
			}

            if (params.chartType) {
                dataView.chartType = params.chartType
                dataView.yMin = null
                dataView.yMax = null
                dataView.zMin = null
                dataView.zMax = null
                if (params.yMin && params.yMin.isFloat()) dataView.yMin = params.yMin.toFloat()
                if (params.yMax && params.yMax.isFloat()) dataView.yMax = params.yMax.toFloat()
                if (params.zMin && params.zMin.isFloat()) dataView.zMin = params.zMin.toFloat()
                if (params.zMax && params.zMax.isFloat()) dataView.zMax = params.zMax.toFloat()
                dataView.save(flush:true)
            }

			def axesCount = dataView.dataViewCharts ? dataView.dataViewCharts.size() : 0
			if (axesCount < 2 && !dataView.urlDashboardData?.trim() ) {
				render ([success:false, msg:"Click 'Configure' to fix"] as JSON)
				return
			}
			
			// If 'calc' then first calculate and save chart data to mongo collection
			if (params.refresh == "calc") {
				def status = dataViewService.drawChart(dataView)
				if (status.success == false) {
					render ([success:false, msg: status.msg] as JSON)
					return
				}
			}
			
			// Load chart data from mongo collection
			def db = mongo.getDB("glo")
			def report = db.dashboardReport.find(new BasicDBObject("dataViewId", dataView.id))?.collect{it}[0]
			if (report) {
				render (report.data.toString())
				return
			}
			render ([success:false, msg:"Click 'Refresh' to get latest data"] as JSON)
		}
		
		catch(Exception exc) {
			logr.error(exc)
			render ([success:false, msg: exc.toString()] as JSON)
		}
	}
		
}
