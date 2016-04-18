package com.glo.custom

import com.mongodb.BasicDBList
import com.mongodb.BasicDBObject
import grails.converters.JSON
import org.apache.commons.logging.LogFactory
import org.apache.poi.xssf.usermodel.*

import javax.servlet.ServletOutputStream

class RelTestController extends com.glo.run.Rest {
	
	private static final logr = LogFactory.getLog(this)

	def springSecurityService
	def fileService
	def relSyncService

	def mongo

	
	def getCurrents = {
		
		logr.debug(params)
		def username =  springSecurityService.principal?.username
		
		def db = mongo.getDB("glo")
		
		def tkey =  params.tkey.replace("_board","")
		def queryTest =	new BasicDBObject("value.parentCode", params.code.tokenize("_")[0])
		queryTest.put("value.tkey", tkey)		
		
		def res = new HashSet()
		
		
		def ret = db.testData.find(queryTest, new BasicDBObject("value.data", 1)).collect{
			if (it?.value?.data[relSyncService.padZeros(0)]) {
				res.addAll(it?.value?.data[relSyncService.padZeros(0)]["setCurrent"].keySet().collect { it.toFloat()/1000 })
			}
		}

		render res.collect { v -> 
			['name':v]
		} as JSON
	}
	
	def getDevicesByGroup = {
		
		logr.debug(params)
		
		def username =  springSecurityService.principal?.username
		def ret =  getDevices(params.group?.trim(), params.tkey)
		render (ret as JSON)
	}
	
	private def getDevices (def grp , def tkey) {
		
		def db = mongo.getDB("glo")
		
		tkey =  tkey.replace("_board","")
		def query =	new BasicDBObject("value.tkey", tkey)
		query.put("value.relGroup", grp)
		
		def fields = new BasicDBObject('value.code', 1)
		fields.put("value.burning", 1)
			
		db.testData.find(query, fields).collect{
			
				def code = it["value"]["code"]
				def burn = it["value"]["burning"]
				if (burn == "YES") {
					code = code + "-B"
				}
			
				['code':code]
		}
	}
	
	
	def getHours = {
		
		logr.debug(params)
		
		def username =  springSecurityService.principal?.username
		
		def db = mongo.getDB("glo")
		
		def queryTest =	new BasicDBObject()
		def tkey =  params.tkey.replace("_board","")
		def code = params.code.replace("-B","")
		queryTest.put("value.code", code)
		queryTest.put("value.tkey", tkey)
		def ret = db.testData.find(queryTest, new BasicDBObject()).collect{
			it["value"] 
		}[0]
		
		def ret2 = ret?.data.collect { k, v -> 
			k
		}
		
		render ret2 as JSON
	}
	
	def getCharts = {

		
		def username =  springSecurityService.principal?.username
		
		def db = mongo.getDB("glo")
		
		def codes = params.deviceCodes.tokenize(",")
		codes = codes.collect { it.replace("-B","").trim()}
		
		def queryTest =	new BasicDBObject()
		def tkey =  params.tkey.replace("_board","")
		queryTest.put("value.code", new BasicDBObject('$in', codes))
		queryTest.put("value.tkey", tkey)
		def ret = db.testData.find(queryTest, new BasicDBObject()).collect{
			it
		}
		
		def current = (params.current.toFloat() * 1000).round(0).toInteger().toString()
		
		def exp = [:]
		def vars = ['EQE %':'eqePerc','LOP %':'lopPerc','VOLTAGE':'voltageDelta','PEAK':'peakDelta','FWHM':'fwhmDelta','DOMINANT':'dominantDelta']
		
		vars.each {k, v ->
			def arr = []
			def m = [:]
			ret.each { td ->
				td.value.data.each {
					if (it.value["setCurrent"][current]) {
						def val = it.value["setCurrent"][current][v]
						if (!m.containsKey(relSyncService.padZeros(it.key))) {
							m.put(relSyncService.padZeros(it.key), [:])
						}
						def code = td.value.code
						m[relSyncService.padZeros(it.key)].put(code, val)
					}
				}
			}
			
			m.keySet().sort{it}.each { k1 ->  
				m[k1].put('hours', k1)
				arr.add(m[k1])
			}
			
			exp.put(k, arr)
		}

		render (exp as JSON)
	}
	
	def getChartsIV = {
		
		def username =  springSecurityService.principal?.username
		
		def db = mongo.getDB("glo")
	
		def queryTest =	new BasicDBObject()
		def tkey =  params.tkey.replace("_board","")
		def code = params.code.replace("-B","")
		queryTest.put("value.code",  code)
		queryTest.put("value.tkey", tkey)
		def ret = db.testData.find(queryTest, new BasicDBObject()).collect{
			it["value"]
		}[0]
		
		def current = (params.current.toFloat() * 1000).toInteger().toString()
			
		def arr = []
		def x = params.x
		
		switch (params.chart) {
			
			case 'EQE':
				ret?.data?.each {k, v ->  
					v?.setCurrent?.each { k1, v1 -> 
						def exp = [:]
						exp.put(k, v1?.eqe)
						exp.put(x, k1.toFloat()/1000)
						arr.add(exp)
					}
				}
				
				arr.sort {it[x]}
			break
			
			case 'LOP':
				ret?.data?.each {k, v ->
					v?.setCurrent?.each { k1, v1 ->
						def exp = [:]
						exp.put(k, v1?.lop > 0 ? Math.log10(v1?.lop) : -8)
						exp.put(x, k1.toFloat()/1000)
						arr.add(exp)
					}
				}
				arr.sort {it[x]}
			break
			
			case 'IV CURVE':
				ret?.data?.each {k, v ->
						v?.setVoltage.each { voltValue, currentObject -> 
							if (currentObject.current.toFloat() >= 0) {
								def exp = [:]
								exp.put(k, currentObject.current.toFloat())
								exp.put(x, voltValue.toFloat()/1E6)
								arr.add(exp)	
							}		
						}
				}
				arr.sort {it[x]}			
			break
			
			case 'SPECTRA':
				ret?.data?.each {k, v ->
					if (v?.setCurrent && v?.setCurrent[current]) {
						def cnt = 0
						v?.setCurrent[current]?.spectrum?.each {  
							if (it[1] > 0) {
								def exp = [:]
								exp.put(k, it[1] * 1E6)
								exp.put('wl', it[0])
								arr.add(exp)
							}
							cnt++
						}
					}
				}
				arr.sort {it['wl']}
			break
		}
				
		render (arr as JSON)
	}
	

	def export = {
		
		def codes2 = params.codes.tokenize(",")
		def codes = codes2.collect { it.replace("-B","")}
		
		def db = mongo.getDB("glo")
		def queryTest =	new BasicDBObject()
		def tkey =  params.tkey.replace("_board","")
		queryTest.put("value.code", new BasicDBObject('$in', codes))
		queryTest.put("value.tkey", tkey)
		
		XSSFWorkbook workbook =  new XSSFWorkbook()
		XSSFSheet sheetCurrent = workbook.createSheet("setCurrent")
		XSSFSheet sheetVoltage = workbook.createSheet("setVoltage")
		
		// Add header to excel
		XSSFRow rowHeaderCurrent = sheetCurrent.createRow(0)
		XSSFRow rowHeaderVoltage = sheetVoltage.createRow(0)
		
		def fieldMap = ["code":0,
			"hours":1,
			"setCurrent":2,
			"current":3,
			"voltage":4,
			"lop":5,
			"photometric":6,
			"peak":7,
			"centroid":8,
			"dominant":9,
			"fwhm":10,
			"eqe":11,
			"lopDelta":12,
			"eqeDelta":13,
			"voltageDelta":14,
			"peakDelta":15,
			"centroidDelta":16,
			"dominantDelta":17,
			"fwhmDelta":18,
			"lopPerc":19,
			"eqePerc":20,
			"voltagePerc":21,
			"peakPerc":22,
			"centroidPerc":23,
			"dominantPerc":24,
			"fwhmPerc":25,
			"osaTime":26,
			"osaCounts":27,
			"u":28,
			"v":29,
			"burning":30,
			"burnTemperature":31,
			"burnCurrent":32]
			
		fieldMap.each { key,value ->
			if (key == "setCurrent" || key == "setVoltage") key = "Set"
			XSSFCell cellHeadCurrent = rowHeaderCurrent.createCell(value)
			cellHeadCurrent.setCellValue(new XSSFRichTextString(key))
			XSSFCell cellHeadVoltage = rowHeaderVoltage.createCell(value)
			cellHeadVoltage.setCellValue(new XSSFRichTextString(key))
		}
		
		def rc = 1
		def rv = 1
				
		db.testData.find(queryTest, new BasicDBObject()).collect{
			
			def code = it?.value?.code
			def burning = it?.value?.burning
						
			it?.value?.data?.each { k, v ->
				
				def hours = relSyncService.padZeros(k)
				
				v.each { k1, v1 ->
					Integer setCurrent = null
					Integer setVoltage = null
					if (k1 == "setCurrent") {
						v1.each {  k2, v2 ->
							
							XSSFRow rowData = sheetCurrent.createRow(rc)
							rc++
							XSSFCell cellData = rowData.createCell(0)
							cellData.setCellValue(code)
							cellData = rowData.createCell(1)
							cellData.setCellValue(hours)
							cellData = rowData.createCell(2)
							cellData.setCellValue(k2.toFloat())
							cellData = rowData.createCell((int)fieldMap["burning"])
							cellData.setCellValue(burning)
							
							v2.each {  k3, v3 ->
								if (!(k3 == "setCurrent" || k3 == "setVoltage") && v3.getClass() != BasicDBList)
								{
									cellData = rowData.createCell((int)fieldMap[k3])
									cellData.setCellValue(v3)
								}
							}
						}
					}
					if (k1 == "setVoltage") {
						v1.each { k2, v2 ->
							
							XSSFRow rowData = sheetVoltage.createRow(rv)
							rv++
							XSSFCell cellData = rowData.createCell(0)
							cellData.setCellValue(code)
							cellData = rowData.createCell(1)
							cellData.setCellValue(hours)
							cellData = rowData.createCell(2)
							cellData.setCellValue(k2.toFloat()/1E6)
							
							v2.each { k3, v3 ->
								if (!(k3 == "setCurrent" || k3 == "setVoltage") && v3.getClass() != BasicDBList)
								{
									cellData = rowData.createCell((int)fieldMap[k3])
									cellData.setCellValue(v3)
								}
							}
						}
					}
				}					
			}

		}	
		
		response.setHeader("Content-disposition", "attachment; filename=data.xlsx")
		response.contentType = "application/excel"
		ServletOutputStream f = response.getOutputStream()
		workbook.write(f)
		f.close()
	}
}
