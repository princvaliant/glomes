package com.glo.run

import grails.converters.JSON
import grails.web.JSONBuilder
import org.apache.commons.logging.LogFactory
import com.glo.ndo.*


class VariablesController extends Rest {

	private static final logr = LogFactory.getLog(this)

	def springSecurityService

	def getVariablesPerProcessCategory = {

		def username =  springSecurityService.principal?.username

		def builder = new JSONBuilder()
		def results = [] as JSON

		if (params.process && params.node == "process") {
			def processes = Process.findAllByCategory(params.process)
			
			processes = processes.grep {!it.disabled}
			
			def procVars = [:]
			processes.each { proc ->  
				proc.variables.each {
					if (!procVars.containsKey(it.name) && !it.readOnly) {
						procVars.put(it.name, it)
					}
				}
			}

			results = builder.build {
				text = "process"
				cls = "folder"
				expanded = false
				children = array {
					procVars.sort{it.key}.each { k, v ->
						if (v.name == "code" || v.name == "nNotes" || v.name == "movedBy" || v.name == "tkey" || 	v.name == "pkey" ||
							 v.name == "idx" || v.name == "product" || v.name == "productCode"  || v.name == "productFamily" ||  v.name == "updated" ||
							 v.name == "lotNumber" || v.name == "supplier" || v.name == "supplierProductCode" || v.name == "closed" || 
							 v.name == "yieldLoss" || v.name == "yieldLossStep" || v.name == "yieldLossDate" || v.name == "active" || 
							 v.name == "processViolations") {
							variable = {
								text = v.name
								leaf = true
								id = "W_" + v.id.toString()
								qtip = "Title: <b>" + (v.title ?: v.name) + "</b><br>Type: <b>" + v.dataType + "</b><br>Dir: <b>" + v.dir
								expanded = false
							}
						}
					} 
					processes.sort{it.pkey}.each { p ->
						process = {
							text = p.pkey
							expanded = false
							cls = "folder"
							children = array {
								p.processSteps.sort().each { ps ->	
									processStep = {
										text = ps.taskKey
										cls = "folder"
										id = "L" + ps.id.toString()
										expanded = false
									}
								}
								p.variables.each { v ->
									if (v.dir in ["dc", "din","calc"] && !(v.name in ["actualStart"])) {
										variable = {
											text = v.name
											leaf = true
											id = "Z_" + v.id.toString()
											qtip = "Title: <b>" + (v.title ?: v.name) + "</b><br>Type: <b>" + v.dataType + "</b><br>Dir: <b>" + v.dir
											expanded = false
										}
									}
								}
							}
						}
					}
				}
			}
		} else if (params.process) {
			def processStep = ProcessStep.get(params.node.substring(1).toLong())
		
			results = builder.build {
				children = array {
					processStep.variables.sort{it.name}.each { v ->
						if (v.dir in ["","dc","din","in","calc"] && !v.readOnly) {
							variable = {
								text = v.name
								leaf = true
								id = "V_" + v.id.toString()
								qtip = "Title: <b>" + (v.title ?: v.name) + "</b><br>Type: <b>" + v.dataType + "</b><br>Dir: <b>" + v.dir
								expanded = false
							}
						}
					}
					processStep.process.variables.sort{it.name}.each { v ->
						if (v.name == "actualStart" || v.name == "firstPassDate" || v.name == "owner" || v.name == "pkey" || v.name == "movedBy" || v.name == "qtyIn" || v.name == "qtyOut" ||
							v.name == "active" || v.name == "end" || v.name == "duration" || v.name == "duration_hr" || v.name == "tkeyNext" || v.name == "tkeyPrev" || v.name == "processViolations") {
							variable = {
								text = v.name
								leaf = true
								id = "V_" + processStep.id + "_" + v.id.toString()
								qtip = "Title: <b>" + (v.title ?: v.name) + "</b><br>Type: <b>" + v.dataType + "</b><br>Dir: <b>" + v.dir
								expanded = false
							}
						}
					}
				}
			}
		}

		render (results)
	}
	
	def getVariablesPerProcessCategorySpc = {

		logr.debug(params)
		def username =  springSecurityService.principal?.username

		def builder = new JSONBuilder()
		def results = [] as JSON

		if (params.process && params.node == "process") {
			def processes = Process.findAllByCategory(params.process)

            processes = processes.grep {!it.disabled}

			results = builder.build {
				text = "process"
				cls = "folder"
				expanded = false
				children = array {
					processes.sort{it.pkey}.each { p ->
						process = {
							text = p.pkey
							expanded = false
							cls = "folder"
							children = array {
								p.processSteps.sort().each { ps ->
									processStep = {
										text = ps.taskKey
										cls = "folder"
										id = "L" + ps.id.toString()
										expanded = false
									}
								}
								p.variables.each { v ->
									if (!v.readOnly && 
										v.dir in ["dc", "din", "calc"] &&
										v.dataType in ["float", "int", "scientific"]) {
				
										variable = { 
											text = v.name
											leaf = true
											id = "W_" + v.id.toString()
											qtip = "Title: <b>" + (v.title ?: v.name) + "</b><br>Type: <b>" + v.dataType + "</b><br>Dir: <b>" + v.dir
											expanded = false
										}
									}
								}
							}
						}
					}
				}
			}
		} else if (params.process) {
			def processStep = ProcessStep.get(params.node.substring(1).toLong())
		
			results = builder.build {
				children = array {
					processStep.variables.each { v ->
						if (v.dir in ["dc", "din", "calc"] && v.dataType in ["float", "int", "scientific"]) {
							variable = {
								text = v.name
								leaf = true
								id = "V_" + v.id.toString()
								qtip = "Title: <b>" + (v.title ?: v.name) + "</b><br>Type: <b>" + v.dataType + "</b><br>Dir: <b>" + v.dir
								expanded = false
							}
						}
					}
				}
			}
		}

		render (results)
	}

	
	def getVariablesPerDataView = {
	
		logr.debug(params)
		def username =  springSecurityService.principal?.username

		def builder = new JSONBuilder()
 
		if (params.dataView) {
			def dataView = DataView.get(params.dataView.toLong())
			def dataViewVariables = DataViewVariable.findAllByDataView(dataView, [sort:"idx"])

			render ([
				data: dataViewVariables.collect{
					def var = it.variable
					def path = it.path?.indexOf('.') == -1 ? "" : it.path?.substring(0, it.path?.indexOf('.'))
					[	id:it.id,
						variableId: var.id,
						dataViewId: it.dataView.id,
						idx: it.idx,
						path: it.fullPath?.replace("|", ", "),
						step: path,
						filter: it.filter,
						name: var.name,
						title: it.title ?: (var.title ?: var.name),
						type: var.dataType,
						isFormula: it.isFormula,
						formula: it.formula,
						dir: var.dir
					]
				},
				count: dataViewVariables.size()
			] as JSON)
		}

		render ([] as JSON)
	}
	
	def saveTitle = {
		
		try {
			def username =  springSecurityService.principal?.username
			def var =  request.JSON
			def dataViewVariable = DataViewVariable.get(var.id)
			
			if (username != "admin" && dataViewVariable?.dataView?.owner != username) {
				throw new RuntimeException ("Only owner can change variable" )
			}
			
			if (dataViewVariable) {
				dataViewVariable.title = var.title.replace("[", "(").replace("]", ")").replace(".", ",").replace("\"", "'")
				dataViewVariable.link = var.link.toString() == "null" ? "" :var.link
				dataViewVariable.filter = var.filter.toString() == "null" ? "" : var.filter
				
				//if (dataViewVariable.isFormula && dataViewVariable.filter) {
				//	throw new RuntimeException ("Filter on calculated variables not implemented yet." )
				//}
				
				dataViewVariable.save(failOnError: true)
			}
			
			render ([success:true] as JSON)
		} catch (Exception exc) {
			logr.error(exc)
			render ([success:false, msg: exc.toString()] as JSON)
		}
	}
	
	def deleteVariable = {
		
		try {
			def username =  springSecurityService.principal?.username
			
			def dataViewVariable = DataViewVariable.get(params.id)
			
			if (dataViewVariable) {
				
				if (username != "admin" && dataViewVariable?.dataView?.owner != username) {
					throw new RuntimeException ("Only owner can remove variable" )
				}

				dataViewVariable.delete([cascade:true])
			}
			render ([success:true] as JSON)
			
		} catch (Exception exc) {
			logr.error(exc)
			render ([success:false, msg: exc.toString()] as JSON)
		}
		
		render ([success:true] as JSON)
	}
	
	def editVariable = {
		render ([success:true] as JSON)
	}

	def deleteNode = {

		render ([success:true] as JSON)
	}
}
