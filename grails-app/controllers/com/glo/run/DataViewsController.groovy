package com.glo.run

import grails.converters.JSON
import grails.web.JSONBuilder

import javax.servlet.ServletOutputStream
import org.apache.commons.logging.LogFactory
import org.apache.poi.xssf.usermodel.XSSFCell
import org.apache.poi.xssf.usermodel.XSSFRichTextString
import org.apache.poi.xssf.usermodel.XSSFRow
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.apache.poi.xssf.usermodel.XSSFName
import com.glo.ndo.*
import com.mongodb.BasicDBObject
import java.text.DateFormat
import java.text.SimpleDateFormat

class DataViewsController extends com.glo.run.Rest {

	private static final logr = LogFactory.getLog(this)

	def springSecurityService

	def dataViewService
	def utilsService
	def mongo

	def list = {

		def username =  springSecurityService.principal?.username
		def dataViews= dataViewService.list(username, params)
		render (['data': dataViews[0], 'count':dataViews[1]] as JSON)
	}



	def get = {

		def dataView =  DataView.get(params.id)
		render (['data':dataView, 'count':1] as JSON)
	}

	def save = {
		try {
			def username =  springSecurityService.principal?.username
			def dataView =  params
			dataViewService.save(username, dataView)
			render ([ok: true, success:true] as JSON)
		}
		catch(Exception exc) {
			logr.error(exc)
			render ([ok: false, success:false, msg: exc.toString()] as JSON)
		}
	}

	def duplicate = {
		try {
			def username =  springSecurityService.principal?.username
			dataViewService.duplicate(username, params.id)

			render ([ok: true, success:true] as JSON)
		}
		catch(Exception exc) {
			logr.error(exc)
			render ([ok: false, success:false, msg: exc.toString()] as JSON)
		}
	}

	def delete = {
		try {
			def username =  springSecurityService.principal?.username

			DataView dv = DataView.get(params.id)

			if (!dv) {
				throw new RuntimeException ("Dataview does not exist" )
			}
			if (username != "admin" && dv.owner != username) {
				throw new RuntimeException ("Only owner can delete report" )
			}

			dv.delete([cascade:true])
			render ([ok: true, success:true] as JSON)
		}
		catch(Exception exc) {
			logr.error(exc)
			render ([success:false, msg: exc.toString()] as JSON)
		}
	}

	def attachVariable = {

		try {
			def username =  springSecurityService.principal?.username
			def dvv = new DataViewVariable()
			dvv.dataView = DataView.get(params.dataViewId.toLong())

			if (username != "admin" && dvv.dataView.owner != username) {
				throw new RuntimeException ("Only owner can change report" )
			}

			def pctg = ""
			def pkey = ""

			def var = params.variableId.tokenize('_')
			if (var.size() == 3) {
				dvv.variable = Variable.get(var[2].toLong())

				pctg = dvv.variable.process ? dvv.variable.process.category : dvv.variable.processStep.process.category
				pkey = dvv.variable.process ? dvv.variable.process.pkey : dvv.variable.processStep.process.pkey
				def procStep = ProcessStep.get(var[1].toLong())
				dvv.path = procStep.taskKey + "." + dvv.variable.name
				if (var[0] == "W") {
					dvv.fullPath = pctg
				} else {
					dvv.fullPath = pctg + "|" + pkey + "|" +  procStep.taskKey
				}
			} else {
				dvv.variable = Variable.get(var[1].toLong())

				pctg = dvv.variable.process ? dvv.variable.process.category : dvv.variable.processStep.process.category
				pkey = dvv.variable.process ? dvv.variable.process.pkey : dvv.variable.processStep.process.pkey

				if (dvv.variable.process) {
					dvv.path = dvv.variable.name
					if (var[0] == "W") {
						dvv.fullPath = pctg
					} else {
						dvv.fullPath = pctg + "|" + pkey
					}
				}
				if (dvv.variable.processStep) {
					dvv.path = dvv.variable.processStep.taskKey + "." + dvv.variable.name
					if (var[0] == "W") {
						dvv.fullPath = pctg
					} else {
						dvv.fullPath = pctg + "|" + pkey + "|" + dvv.variable.processStep.taskKey
					}
				}
			}

			// Can this variable be assigned to report
			def pctgs = dvv.dataView.dataViewVariables.collect { it.variable.process ? it.variable.process.category : it.variable.processStep?.process?.category }
			if (pctgs && !pctgs.contains(pctg)) {

				render ([ok: false, success:false, msg: "Can not mix variables from different process categories in the same report"] as JSON)
				return
			}

			dvv.idx = 1
			dvv.title = dvv.variable.title?.replace("[", "(")?.replace("]", ")")?.replace(".", ",")?.replace("\"", "'") ?: dvv.variable.name
			dvv.save(failOnError: true)

			params.draggedId = dvv.id
			reorderVariables()

			render ([ok: true, success:true] as JSON)
		}
		catch(Exception exc) {
			logr.error(exc)
			render ([ok: false, success:false, msg: exc.toString()] as JSON)
		}
	}

	def reorderVariables = {
		try {
			def username =  springSecurityService.principal?.username
			def dataView = DataView.get(params.dataViewId)

			if (username != "admin" && dataView.owner != username) {
				throw new RuntimeException ("Only owner can change report" )
			}

			def dataViewVariables = DataViewVariable.findAllByDataView(dataView, [sort:"idx"])
			def map = [:]
			dataViewVariables.each {
				if (it.id == params.droppedId.toLong() &&  params.droppedPosition == "before") {
					map.put(params.draggedId.toLong(), map.size())
					map.put(it.id, map.size())
				} else if (it.id == params.droppedId.toLong() &&  params.droppedPosition == "after"){
					map.put(it.id, map.size())
					map.put(params.draggedId.toLong(), map.size())
				} else if (it.id != params.draggedId.toLong()){
					map.put(it.id, map.size())
				}
			}
			map.each { k, v ->
				DataViewVariable.withTransaction { status ->
					def dvv = DataViewVariable.get(k)
					dvv.idx = v
					dvv.save(failOnError: true)
				}
			}

			render ([ok: true, success:true] as JSON)
		}
		catch(Exception exc) {
			logr.error(exc)
			render ([ok: false, success:false, msg: exc.toString()] as JSON)
		}
	}

	def addFormulaVariable = {
		try {
			def username =  springSecurityService.principal?.username
			def dvv = new DataViewVariable()
			dvv.dataView = DataView.get(params.dataViewId)

			if (username != "admin" && dvv.dataView.owner != username) {
				throw new RuntimeException ("Only owner can change report" )
			}

			dvv.variable = Variable.get(0)
			dvv.idx = 10000
			dvv.title = ""
			dvv.isFormula = 1
			dvv.save(failOnError: true)

			render ([ok: true, success:true] as JSON)
		}
		catch(Exception exc) {
			logr.error(exc)
			render ([ok: false, success:false, msg: exc.toString()] as JSON)
		}
	}

	def saveFormulaVariable = {

		try {
			
			def username =  springSecurityService.principal?.username
			def scriptStr = params.formula.toString().trim()

			def shell = new GroovyShell(this.class.classLoader)
			def script = shell.parse(scriptStr)

			def dataViewVariable = DataViewVariable.get(params.dataViewVariableId)

			if (username != "admin" && dataViewVariable.dataView.owner != username) {
				throw new RuntimeException ("Only owner can change report" )
			}

			if (dataViewVariable) {
				dataViewVariable.formula = scriptStr
				dataViewVariable.save(failOnError: true)
			}
			render ([success:true] as JSON)
		} catch (org.codehaus.groovy.control.CompilationFailedException  exc) {
			render ([success:false, msg: "Compilation: " + exc.toString()] as JSON)
		} catch (Exception  exc) {
			render ([success:false, msg: "Error: " + exc.toString()] as JSON)
		}
	}

	def joins = {

		def username =  springSecurityService.principal?.username


		def dataViewJoins = DataViewJoin.findAllByDataView(DataView.get(params.dataView))
		def total = dataViewJoins.size()

		render (['data': dataViewJoins.collect {
				[   id:it.id,
					dataView:it.dataView?.id,
					secondaryDataView: it.secondaryDataView?.id,
					secondaryDataViewText: it.secondaryDataView?.name,
					primaryVariable: it.primaryVariable?.id,
					primaryVariableText: it.primaryVariable?.title,
					secondaryVariable: it.secondaryVariable?.id,
					secondaryVariableText: it.secondaryVariable?.title,
					joinType: it.joinType]
			}
			, 'count': total] as JSON)
	}

	def updateJoin = {

		try {
			DataViewJoin  dvj
			if (params.id) {
				dvj = DataViewJoin.get(params.id.toLong())
			} else {
				dvj = new DataViewJoin()
			}

			dvj.dataView = DataView.get(params.dataView.toLong())
			dvj.secondaryDataView = DataView.get(params.secondaryDataView.toLong())
			dvj.primaryVariable = DataViewVariable.get(params.primaryVariable.toLong())

            if (params.secondaryVariable.isLong()) {
  			    dvj.secondaryVariable = DataViewVariable.get(params.secondaryVariable.toLong())
            } else {

                def dvv = new DataViewVariable()
                dvv.dataView = dvj.secondaryDataView
                dvv.variable = Variable.get(0)
                dvv.idx = 1
                dvv.title = params.secondaryVariable
                dvv.save(failOnError: true)
                dvj.secondaryVariable = dvv
            }
			dvj.joinType = params.joinType

			dvj.save(failOnError: true)

			render ([ok: true, success:true] as JSON)
		}
		catch(Exception exc) {
			logr.error(exc)
			render ([ok: false, success:false, msg: exc.toString()] as JSON)
		}
	}

	def removeJoin = {
		try {
			def username =  springSecurityService.principal?.username
			DataViewJoin dvj = DataViewJoin.get(params.id.toLong())

			if (!dvj) {
				throw new RuntimeException ("DataViewJoin does not exist" )
			}

			dvj.delete([cascade:true])

			render ([ok: true, success:true] as JSON)
		}
		catch(Exception exc) {
			logr.error(exc)
			render ([success:false, msg: exc.toString()] as JSON)
		}

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
				var3 = {
					name = "tag"
					type = "string"
				}
				var4 = {
					name = "isPublic"
					type = "boolean"
				}
				var5 = {
					name = "publishToDashboard"
					type = "boolean"
				}
				var6 = {
					name = "owner"
					type = "string"
				}
				var7 = {
					name = "urlDashboardData"
					type = "string"
				}
				var8 = {
					name = "description"
					type = "string"
				}
				var9 = {
					name = "chartType"
					type = "string"
				}
				var10 = {
					name = "urlExportData"
					type = "string"
				}
				var11 = {
					name = "filterByPath"
					type = "boolean"
				}
				var12 = {
					name = "yMin"
					type = "float"
				}
				var13 = {
					name = "yMax"
					type = "float"
				}
				var14 = {
					name = "zMin"
					type = "float"
				}
				var15 = {
					name = "zMax"
					type = "float"
				}
			}
			columns = array {
				var16 = {
					dataIndex = "name"
					id = "dv6name" + params.domain
					text = "Name"
					flex = 8
				}
				var17 = {
					dataIndex = "tag"
					id = "dv6tag"  + params.domain
					text = "Tag"
					flex = 2
				}
				var18 = {
					dataIndex = "isPublic"
					id = "dv6ispub"  + params.domain
					text = "Publ?"
					flex = 1
				}
				var19 = {
					dataIndex = "publishToDashboard"
					id = "dv6pubtodash"  + params.domain
					text = "Dash?"
					flex = 1
				}
				var20 = {
					dataIndex = "owner"
					id = "dv6owner"  + params.domain
					text = "Owner"
					flex = 2
				}
			}
		}
		render (results)
	}

	def getDashboards = {
        def username = ""
        try {
            username = springSecurityService.principal?.username
        } catch (Exception exc) {

        }
		def dataViews= dataViewService.getDashboards(username, params)
		render (['data': dataViews[0], 'count':dataViews[1]] as JSON)
	}

	def saveDashboard = {
		try {
			def username =  springSecurityService.principal?.username
			def dataView =  params
			dataViewService.save(username, dataView)
			render ([ok: true, success:true] as JSON)
		}
		catch(Exception exc) {
			logr.error(exc)
			render ([ok: false, success:false, msg: exc.toString()] as JSON)
		}
	}

	def removeDataViewFromDashboard = {

		try {
			DataView dataView = DataView.get(params.dataViewId)

			def username =  springSecurityService.principal?.username
			if (dataView.owner != username) {
				throw new RuntimeException ("Only owner can change report" )
			}

			dataView.publishToDashboard= false
			dataView.save()
			render ([success:true] as JSON)
		}
		catch(Exception exc) {
			logr.error(exc)
			render ([ok: false, success:false, msg: exc.toString()] as JSON)
		}

	}

	def export = {

		def dataViewLog = new DataViewLog()
		Long startTime = System.currentTimeMillis()
		Long endTime = 0

		try {
			
			def dataView = DataView.get(params.dataViewId)
			def excelTemplate = dataView.excelTemplate
			def excelName = dataView.excelName
			def name = dataView.name
			
			if (dataView) {

				dataViewLog.dataViewId = dataView.id
				dataViewLog.name = dataView.name
				dataViewLog.owner = dataView.owner
				dataViewLog.user =  springSecurityService.principal?.username
				
				def data = dataViewService.getReportData(dataView)

				def rowList = data[0]
				def codesForNotes = data[1]
				def dvVariables = data[2]
				dataViewLog.query = data[3]
                def totalRows = rowList.size()

				def r = 1
				DateFormat  df = new SimpleDateFormat("yyyy-MM-dd")

				XSSFWorkbook workbook
				XSSFSheet sheet
				XSSFSheet sheetNote
                XSSFSheet sheetCd

				if (excelTemplate != null) {
					def stream = new ByteArrayInputStream(excelTemplate)
					workbook = new XSSFWorkbook(stream)
                    def idx = workbook.getSheetIndex("mesdata")
                    if (idx >= 0) {
                        // delete template data, if any, on mesdata sheet
                        workbook.removeSheetAt(idx)
                    }
                    sheet = workbook.createSheet("mesdata")
				} else {
					workbook = new XSSFWorkbook()
					sheet = workbook.createSheet("mesdata")
				}

                org.apache.poi.xssf.usermodel.XSSFCellStyle cellStyle = workbook.createCellStyle()
                org.apache.poi.xssf.usermodel.XSSFDataFormat dateFormat = workbook.createDataFormat()
                cellStyle.setDataFormat(dateFormat.getFormat("yyyy/mm/dd hh:mm"))

				// Explode object array, if exists
				def allColumns = []

				dvVariables.sort{it.idx}.each {
					allColumns.add(it.title)
				}
				
				// Add header to excel
				XSSFRow rowHeader = sheet.createRow(0)
				def h = 0
				allColumns.each {

					XSSFCell cellHead = rowHeader.createCell((int)h)
					cellHead.setCellValue(new XSSFRichTextString(it))

                    def rangeNameStr = "Range" + it.replaceAll("[^a-zA-Z0-9]+","")
                    try {
                        def coll = org.apache.poi.ss.util.CellReference.convertNumToColString(h)
                        def s1 = '$' + coll + '$2'
                        def s2 = '$' + coll + '$' + (totalRows + 1)
                        def s3 = "'mesdata'!$s1:$s2"
                        XSSFName rangename = workbook.getName(rangeNameStr)
                        if (!rangename) {
                            rangename = workbook.createName()
                            rangename.setNameName(rangeNameStr)

                        }
                        rangename.setRefersToFormula(s3)
                    } catch (Exception exc) {

                    }

    				h++
				}

				// Add notes to output
				if (codesForNotes) {
					def r2 =1
					sheetNote = workbook.createSheet("mesnotes")
					XSSFRow rowHeader2 = sheetNote.createRow(0)
					XSSFCell cellHead2 = rowHeader2.createCell((int)0)
					cellHead2.setCellValue(new XSSFRichTextString("code"))
					cellHead2 = rowHeader2.createCell((int)1)
					cellHead2.setCellValue(new XSSFRichTextString("Note"))
					cellHead2 = rowHeader2.createCell((int)2)
					cellHead2.setCellValue(new XSSFRichTextString("Step"))
					cellHead2 = rowHeader2.createCell((int)3)
					cellHead2.setCellValue(new XSSFRichTextString("User"))
					cellHead2 = rowHeader2.createCell((int)4)
					cellHead2.setCellValue(new XSSFRichTextString("Created"))


					def fields2 = new BasicDBObject("code":1)
					fields2.put("note", 1)
					def  q = new BasicDBObject()
					q.put('$in', codesForNotes)
					def query2 = new BasicDBObject("code", q)
					def db = mongo.getDB("glo")
					def temp2 = db.history.find(query2, fields2).findAll {
						it['note']?.each { note ->
							XSSFRow rowData2 = sheetNote.createRow(r2)
							XSSFCell cellData2 = rowData2.createCell(0)
							cellData2.setCellValue(it['code'])
							cellData2 = rowData2.createCell(1)
							cellData2.setCellValue(note['comment'])
							cellData2 = rowData2.createCell(2)
							cellData2.setCellValue(note['stepName'])
							cellData2 = rowData2.createCell(3)
							cellData2.setCellValue(note['userName'])
							cellData2 = rowData2.createCell(4)
							cellData2.setCellValue(df.format(note['dateCreated']))
							r2++
						}
					}
				}

                def sheets = [:]
                def posinsheets = [:]
				rowList.each { obj ->

					XSSFRow rowData = sheet.createRow(r)
					def c = 0
					allColumns.each {

						def val = it

                        if (val == 'customdata') {
                            obj[0][val].each { prop, val2 ->
                                if (!posinsheets[prop]) posinsheets[prop] = 1
                                if (!sheets[prop]) sheets[prop] = workbook.createSheet(prop)
                                XSSFRow rowHeader3 = sheets[prop].createRow(0)
                                XSSFCell cellHead3 = rowHeader3.createCell((int)0)
                                cellHead3.setCellValue(new XSSFRichTextString("code"))
                                if (!val2.toString().isNumber() && val2[0] != null && val2[0].toString() != "NaN") {
                                     if (val2[0].toString().isNumber()) {
                                        cellHead3 = rowHeader3.createCell((int) 1)
                                        cellHead3.setCellValue(new XSSFRichTextString("value"))
                                    }
                                    else {
                                        def rr = 1
                                        val2[0].each { prop3, val3 ->
                                            println(prop3)
                                            cellHead3 = rowHeader3.createCell((int) rr)
                                            cellHead3.setCellValue(new XSSFRichTextString(prop3))
                                            rr++
                                        }
                                    }

                                    val2.each { cdv ->
                                        XSSFRow rowData3 = sheets[prop].createRow(posinsheets[prop])
                                        XSSFCell cellData3 = rowData3.createCell(0)
                                        cellData3.setCellValue(obj['WaferID'])
                                        if (cdv.toString().isNumber()) {
                                            cellData3 = rowData3.createCell((int) 1)
                                            cellData3.setCellValue(cdv)
                                        } else {
                                            def rr = 1
                                            cdv.each { prop3, val3 ->
                                                cellData3 = rowData3.createCell((int) rr)
                                                cellData3.setCellValue(val3)
                                                rr++
                                            }
                                        }
                                        posinsheets[prop]++
                                    }
                                }
                            }
                        } else {

                            XSSFCell cellData = rowData.createCell((int) c)
                            if (obj[0][val] != null) {

                                if (obj[0][val].getClass() == com.mongodb.BasicDBList) {

                                    if (obj[0][val].size() > 0) {
                                        obj[0][val] = obj[0][val][obj[0][val].size() - 1]
                                    } else {
                                        obj[0][val] = ""
                                    }
                                }

                                if (!obj[0][val]?.toString()?.trim()) {
                                    cellData.setCellValue(null)
                                } else if (obj[0][val].getClass() == java.util.Date) {
                                    cellData.setCellStyle(cellStyle);
                                    cellData.setCellValue(obj[0][val])
                                } else if (obj[0][val].getClass() == org.codehaus.groovy.grails.web.json.JSONObject$Null) {
                                    cellData.setCellValue(null)
                                } else {
                                    cellData.setCellValue(obj[0][val])
                                }
                            }
                            if (obj[1] != null && obj[1][val] != null) {

                                if (obj[1][val].getClass() == com.mongodb.BasicDBList) {

                                    if (obj[1][val].size() > 0) {
                                        obj[1][val] = obj[1][val][obj[1][val].size() - 1]
                                    } else {
                                        obj[1][val] = ""
                                    }
                                }

                                if (!obj[1][val]?.toString()?.trim()) {
                                    cellData.setCellValue(null)
                                } else
                                    cellData.setCellValue(obj[1][val])
                            }
                        }
						c++
					}
					r++
				}

                for (def i = 0 ; i < h ; i++) {
					sheet.autoSizeColumn(i)
				}

                if (totalRows >= 100000) {
                    XSSFRow rowData = sheet.createRow(r++)
                    XSSFCell cellData = rowData.createCell(0)
                    cellData.setCellValue('WARNING !!! EXPORT DATA SIZE IS LIMITED TO 100000 ROWS. USE FILTERS TO REDUCE NUMBER OF ROWS IN DATASET.')
                }

				def filename = ".xlsx"
				if (excelName) {
					def i = excelName.lastIndexOf('.')
					def j = excelName.length() - 1
					if (i >= 0) filename = excelName[i..j]
				}
				filename = name + filename
				filename = filename.replaceAll(',','_')
				filename = filename.replaceAll(' ','_')

				response.setHeader("Content-disposition",  "attachment; filename=" + filename)
				response.contentType = "application/excel"
				ServletOutputStream f = response.getOutputStream()
				workbook.write(f)
				f.close()
				
				endTime = System.currentTimeMillis()
				
			}
		}
		catch(Exception exc) {
			
			logr.error(exc)
			endTime = System.currentTimeMillis()
			dataViewLog.error = exc.getMessage()
			render ([ok: false, success:false, msg: exc.toString()] as JSON)
		} finally {
		
			dataViewLog.duration = endTime - startTime
			dataViewLog.runDate = new Date()
			dataViewLog.save(failOnError: true)
		}
	}

	def importExcel = {

		try {
			def username =  springSecurityService.principal?.username
			def f = request.getFile("filePath")
			if (!f.empty) {
				def dataView = DataView.get(params.dataViewId)
				dataView.excelTemplate = f.bytes
				dataView.excelName = f.fileItem.name
				dataView.save(failOnError: true)

			}
			render ("{success:'true',msg:'Uploaded'}")
		} catch (Exception exc) {
			logr.error(exc)
			render ("{success:'false',msg:'" + exc.toString() + "'}")
		}
	}


}
