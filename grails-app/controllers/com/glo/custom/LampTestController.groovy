package com.glo.custom

import com.mongodb.BasicDBList
import com.mongodb.BasicDBObject
import grails.converters.JSON
import org.apache.commons.logging.LogFactory
import org.apache.poi.xssf.usermodel.*

import javax.servlet.ServletOutputStream

class LampTestController extends com.glo.run.Rest {
	
	private static final logr = LogFactory.getLog(this)

	def springSecurityService
	def fileService
	def relSyncService

	def mongo

	
	def getCurrents = {
		
		logr.debug(params)
		def username =  springSecurityService.principal?.username
		
		def db = mongo.getDB("glo")
		
		def queryTest =	new BasicDBObject("value.code", params.code)
		queryTest.put("value.tkey", params.tkey)	
		
		def fields = new BasicDBObject("value.data.Header.setCurrent", 1)
		fields.put("value.data.Encap.setCurrent", 1)	
		
		def ret = db.testData.find(queryTest, fields).collect{
			if (it?.value?.data["Header"])
				it?.value?.data["Header"]["setCurrent"] 
			else if (it?.value?.data["Encap"])
				it?.value?.data["Encap"]["setCurrent"]
			else
				0
		}
		
		render ret[0].collect { k, v -> 
			['name':k.toFloat()/1000]
		} as JSON
	}
	
	def getDevicesByGroup = {
		
		logr.debug(params)
		
		def username =  springSecurityService.principal?.username
		
		def db = mongo.getDB("glo")
		
		def query =	new BasicDBObject()
		query.put("value.parentCode", params.group?.trim())
		query.put("value.tkey", params.tkey)
				
		def ret = db.testData.find(query, new BasicDBObject('value.code', 1)).collect{
			['code':it["value"]["code"]]
		}
		
		render ret as JSON
	}
	
	def getSteps = {
		
		logr.debug(params)
		
		def username =  springSecurityService.principal?.username
		
		def ret = ["Header", "Encap"]
				
		render ret as JSON
	}
	
	def getCharts1 = {
		
		def username =  springSecurityService.principal?.username
		
		def db = mongo.getDB("glo")
	
		def queryTest =	new BasicDBObject()
		queryTest.put("value.code",  params.code)
		queryTest.put("value.tkey", params.tkey)
		def ret = db.testData.find(queryTest, new BasicDBObject()).collect{
			it["value"]
		}[0]
		
	
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
//						exp.put(k, v1?.lop > 0 ? Math.log10(v1?.lop) : -8)
						exp.put(k, v1?.lop > 0 ? v1?.lop * 1e4 : -8)
						exp.put(x, k1.toFloat()/1000)
						arr.add(exp)
					}
				}
				arr.sort {it[x]}
			break
			
			case 'FWHM':
				ret?.data?.each {k, v ->
					v?.setCurrent?.each { k1, v1 ->
						def exp = [:]
						if (v1?.current > 0) {
							exp.put(k, v1?.fwhm)
							exp.put(x, k1.toFloat()/1000)
							arr.add(exp)
						}
					}
				}
				arr.sort {it[x]}			
			break
			
			case 'KFACTOR':
			
				def testData = relSyncService.getLopTestData (db, ret?.parentCode, ret?.code?.tokenize("_")[1])
				
				ret?.data?.each {stage, v ->
					
					v?.setCurrent?.each {current, currentData ->
						def exp = [:]
						
						float curr = current.toInteger()/1000

						if (testData[curr]) {
							float calc = currentData["eqe"]/testData[curr]
							exp.put(stage, calc)
							exp.put(x, curr)
							arr.add(exp)
						}
					}
				}
				arr.sort {it[x]}
			break
		}
				
		render (arr as JSON)
	}
	
	def getCharts2 = {
		
		def username =  springSecurityService.principal?.username
		
		def db = mongo.getDB("glo")
	
		def queryTest =	new BasicDBObject()
		queryTest.put("value.code",  params.code)
		queryTest.put("value.tkey",  params.tkey)
		def ret = db.testData.find(queryTest, new BasicDBObject()).collect{
			it["value"]
		}[0]
		
	
		def arr = []
		def x = params.x
		
		switch (params.chart) {
			
			case 'DOMINANT':
				ret?.data?.each {k, v ->
					v?.setCurrent?.each { k1, v1 ->
						def exp = [:]
						exp.put(k, v1?.dominant)
						exp.put(x, k1.toFloat()/1000)
						arr.add(exp)
					}
				}
				
				arr.sort {it[x]}
			break
			
			case 'PEAK':
				ret?.data?.each {k, v ->
					v?.setCurrent?.each { k1, v1 ->
						def exp = [:]
						exp.put(k, v1?.peak)
						exp.put(x, k1.toFloat()/1000)
						arr.add(exp)
					}
				}
				arr.sort {it[x]}
			break
			
			case 'CURRENT':
			
				ret?.data?.each {k, v ->
						v?.setVoltage.each { voltValue, currentObject -> 
							def exp = [:]
							exp.put(k, currentObject.current.toFloat())
							exp.put(x, voltValue.toFloat()/1E6)
							arr.add(exp)			
						}
				}
			
				arr.sort {it[x]}
			break
			
			case 'SPECTRA':
			
				def current = (params.current.toFloat() * 1000).toInteger().toString()
			
				ret?.data?.each {k, v ->
					if (v?.setCurrent && v?.setCurrent[current]) {
						def cnt = 0
						v?.setCurrent[current]?.spectrum?.each {
							if (it[1] > 0) {
								def exp = [:]
								exp.put(k, it[1] * 1e6)
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
		
		def codes = params.codes.tokenize(",")
		codes = codes.collect { it.trim()}
		
		def db = mongo.getDB("glo")
		def queryTest =	new BasicDBObject()
		queryTest.put("value.code", new BasicDBObject('$in', codes))
		queryTest.put("value.tkey", params.tkey)
		
		XSSFWorkbook workbook =  new XSSFWorkbook()
		XSSFSheet sheetCurrent = workbook.createSheet("setCurrent")
		
		// Add header to excel
		XSSFRow rowHeaderCurrent = sheetCurrent.createRow(0)
		
		def fieldMap = ["Code":0,
			"Steps":1,
			"Set":2,
			"current":3,
			"voltage":4,
			"lop":5,
			"peak":6,
			"centroid":7,
			"dominant":8,
			"fwhm":9,
			"eqe":10,
			"kFactor":11,
			"photometric":12]
		
		fieldMap.each { key,value ->
			if (key == "setCurrent" || key == "setVoltage") key = "Set"
			XSSFCell cellHeadCurrent = rowHeaderCurrent.createCell(value)
			cellHeadCurrent.setCellValue(new XSSFRichTextString(key))
		}
		
		
		def rc = 1
		
		db.testData.find(queryTest, new BasicDBObject()).collect{
			
			def code = it?.value?.code
			
			def testVis = relSyncService.getLopTestData (db, it?.value?.parentCode, code.tokenize("_")[1])
			
			it?.value?.data?.each { k, v ->
				
				def steps = k
				
				v.each { k1, v1 ->
					Integer setCurrent = null
					if (k1 == "setCurrent") {
						v1.each {  k2, v2 ->
							
							XSSFRow rowData = sheetCurrent.createRow(rc)
							rc++
							XSSFCell cellData = rowData.createCell(0)
							cellData.setCellValue(code)
							cellData = rowData.createCell(1)
							cellData.setCellValue(steps)
							cellData = rowData.createCell(2)
							cellData.setCellValue(k2.toFloat())
							
							v2.each {  k3, v3 ->
								if (!(k3 == "setCurrent") && v3.getClass() != BasicDBList)
								{
									cellData = rowData.createCell((int) (fieldMap[k3] ?: 50))
									cellData.setCellValue(v3)

								}
							}
							
							float curr = k2.toFloat()/1000
							if (testVis[curr] >= 0) {
								float calc = v2["eqe"]/testVis[curr]
								cellData = rowData.createCell(11)
								cellData.setCellValue(calc)
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
