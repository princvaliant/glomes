package com.glo.run

import com.mongodb.BasicDBObject
import grails.converters.JSON
import grails.web.JSONBuilder
import org.apache.commons.logging.LogFactory
import com.glo.ndo.*
import com.glo.security.*


class ContentsController extends Rest {

	private static final logr = LogFactory.getLog(this)
	def grailsApplication
	def contentService
	def companyService
	def equipmentService
	def operationService
	def workCenterService
	def workflowService
    def mongo


	def formFirstTaskDc = {

		// Find first task definition in process
		def taskdef = workflowService.findFirstTaskDefinition(params.pctg, params.pkey.toLowerCase())
		listForm (params.pctg, params.pkey, taskdef.tkey, ["dc","calc"], true, params.category)
	}

	def listFirstTaskDc = {

		// Find first task definition in process
		def taskdef = workflowService.findFirstTaskDefinition(params.pctg, params.pkey.toLowerCase())
		list(params.pctg, params.pkey, taskdef.tkey, ["dc","calc"])
	}


	def listByTask = {

		list(params.pctg, params.pkey, params.tkey, params.dir)
	}

	private def list (pctg, pkey, tkey, dir) {

		def variables = contentService.getVariables(pctg, pkey, tkey, dir)

		def isAdmin = org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils.ifAllGranted("ROLE_ADMIN")
		
		def isEngt = false
		def process = Process.findByCategoryAndPkey(pctg, pkey)
		if (process && process?.engtMoveRoles) {
			def pkeyEngRoles = process?.engtMoveRoles.tokenize(',')
			pkeyEngRoles.each {
				if (org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils.ifAnyGranted(it.toString().trim())) {
					isEngt = true
				}
			}
		}

		def builder = new JSONBuilder()
		def results = builder.build {
			fields = array {
				variables.each { v ->
					if (v.dataType != 'object') {
						variable = {
							name = v.name
							if (v.dataType == 'int') {
								type = 'Long'
							} else if (v.dataType == 'float') {
								type = 'Decimal'
							} else if (v.dataType == 'scientific') {
								type = 'string'
							} else if (v.dataType == 'objectArray') {
								type = 'string'
							} else if (v.dataType == 'Unit') {
                                type = 'string'
                            } else {
								type = v.dataType
							}
							if (v.dataType == "date") {
						//		dateFormat = 'Y-m-d H:i:s'
							}
						}
					}
				}
				variable = {
					name = 'specs'
					type = 'string'
				}
			}
			columns = array {
				col1 = {
				
				}
				col2 = {
					
				}
				col3 = {
					
				}
				variables.each { p ->

					// Add security check for showing fields
					def editor = p.editor
					if ((p.name == "actualStart" && !isAdmin && !isEngt) ||
					(p.dir == "din" || p.dir == "recp")) {
						editor = false
					}

					if (!p.hidden && p.dataType != 'object') {
						
						variable = {
							
							if (p.listValues) {

							} else {
								if (p.dataType == 'int') {
									xtype = 'numbercolumn'
									format= '0'
                                    filter = {type = 'numeric'}
								}
								if (p.dataType == 'float') {
									xtype = 'numbercolumn'
									format = '0.000'
                                    filter = {type = 'numeric'}
								}
								if (p.dataType == 'scientific') {
                                    filter = {type = 'numeric'}
								}
								if (p.dataType == 'date') {
									xtype = 'datecolumn'
									format = 'Y-m-d H:i'
                                    // filter = {type = 'date'}
                                }
                                if(p.dataType in ['string','User']) {
                                    filter = {type = 'string'}
                                }
							}
							if (!p.allowBlank && editor) {
								style = 'color:B40431;'
							} else if (editor) {
                                style = 'color:2805F0;'
                            }

							stateId = "gd5c" + pkey + p.name
							text = p.title ?: p.name
							dataIndex = p.name
							width = p.width ?: 90
							
							if (editor) {
								
								field = {
									
									if (p.listValues) {
										xtype = 'combo'
										forceSelection = p.listForceSelection == null ? false : p.listForceSelection
										editable = p.listForceSelection == null ? true : !p.listForceSelection
										store = comboData(p.listValues, p.dataType, pkey, tkey)
									}  else if  (p.dataType == "DieSpec") {
										xtype = 'combo'
										metaName = p.dataType
										name = 'MetaItem'
										pageSize = false
										displayField = 'name'
										valueField = 'id'
										forceSelection = true
										minChars = 3
										queryMode = 'remote'
										typeAhead = true
										emptyText = '<search all units>'
										matchFieldWidth = false
										listConfig = {
											width = 270
											loadingText    = 'Searching ...'
											valueNotFoundText = 'No matching found.'
										}
										value = new Integer(p.defaultValue ?: -1)
									} else {
									
										allowBlank = true
										if (p.dataType == 'int' || p.dataType == 'objectArray') {
											xtype = 'numberfield'
											allowDecimals = false
										}
										if (p.dataType == 'float') {
											xtype = 'numberfield'
											allowDecimals = true
											decimalPrecision = 3
 										}
										if (p.dataType == 'date') {
											xtype = 'datefield'
											format = 'Y-m-d'
 										}
										if (!p.allowBlank) {
											emptyText = '<required>'
										}
										if (p.dataType == 'scientific') {
											emptyText = '##E-##'
  										}
									}

								}
							}
						}
					}
				}
			}
		}

		render (results)
	}

	def listDc = { listForm (params.category, params.procKey, params.taskKey, ['dc'], (params.allow == "true" ? true : false), params.category) }

	def listInDin = { listForm (params.category, params.procKey, params.taskKey, ['in', 'din'], false, params.category)}

	def listRecp = { listForm (params.category, params.procKey, params.taskKey, ['recp'], false, params.category) }

	private def listForm (pctg, pkey, tkey, dir, allow, category) {

		def variables = contentService.getVariables(pctg, pkey, tkey, dir)
		
		def isEngt = false
        def process
        def processStep = workflowService.getProcessStep(pctg, pkey, tkey)
        if (processStep)
            process = processStep.process
        else
            process = Process.findByCategoryAndPkey(pctg, pkey)

		if (process && process?.engtMoveRoles) {
			def pkeyEngRoles = process?.engtMoveRoles.tokenize(',')
			pkeyEngRoles.each {
				if (org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils.ifAnyGranted(it.toString().trim())) {
					isEngt = true
				}
			}
		}

		if (!variables && dir == 'dc') {
			render ([controls: [html:"This step does not contain data collection"]] as JSON)
		} else {

			def isAdmin = org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils.ifAllGranted("ROLE_ADMIN")

			def builder = new JSONBuilder()
			def results = builder.build {
                instructions = (processStep?.instructions ?: '')
				fields = array {
					variables.each { v ->
						variable = {
							name = v.name
							type = v.dataType
							defaultValue = v.defaultValue
							allowBlank = v.allowBlank
							if (v.dataType == "date") {
								dateFormat = 'Y-m-d H:i:s'
							}
						}
					}
				}

				controls = array {
					variables.each { p ->
						
						
						def specLimit = p.specLimit?.tokenize(",")
						def allowRange = p.allowedRange?.tokenize(",")
		
						def editor = p.editor
						if (p.name == "actualStart" && !isAdmin && !isEngt) {
							editor = false
						}
						if (!p.hidden && (editor || p.dir != 'dc')) {
							if (p.evalScript)
								p.defaultValue = contentService.calcThisValue(p, "NEW")
							items = {
								if (p.listValues) {
									xtype = 'combo'
									store = comboData(p.listValues, p.dataType, pkey, tkey)
									displayField = 'name'
									valueField = 'id'
									forceSelection = p.listForceSelection == null ? false : p.listForceSelection
									editable = p.listForceSelection == null ? true : !p.listForceSelection
									name = p.name
									multiSelect = false
									anchor = '90%'
									value = p.defaultValue
									matchFieldWidth = false
									listConfig  = {
										width = 200
									}
								} else if  (p.dataType == 'Company' || p.dataType == 'Location' ||  p.dataType == 'User' ||
								p.dataType =='Equipment' || p.dataType == 'Operation' || p.dataType == 'WorkCenter') {
									xtype = 'combo'
									store = comboData(p.listValues, p.dataType, pkey, tkey)
									displayField = 'name'
									valueField = 'id'
									forceSelection = true
									multiSelect = false
									anchor = '100%'
									name = p.dataType.toLowerCase()
								    if (p.defaultValue) {
										value = new Integer(p.defaultValue ?: -1)
									}
								} else if (p.dataType == 'DieSpec') {
									xtype = 'combo'
									metaName = p.dataType
									name = p.name
									pageSize = false
									displayField = 'name'
									valueField = 'id'
									forceSelection = true
									editable = true
									minChars = 1
									width = 485
									queryMode = 'remote'
									typeAhead = true
									emptyText = '<search all units>'
									matchFieldWidth = false
									listConfig = {
										width = 490
										loadingText    = 'Searching ...'
										valueNotFoundText = 'No matching found.'
									}
								} else {
									if (p.dataType == 'string' ) {
										xtype = 'textfield'
										value = p.defaultValue
										name = p.name
                                    }
									if (p.dataType == 'bigString' ) {
										xtype = 'textarea'
										anchor = '100%'
										labelAlign = 'top'
										rows = 8
										value = p.defaultValue
										name = p.name
									}
									if (p.dataType == 'scientific') {
										xtype = 'textfield'
										value = p.defaultValue
										name = p.name
										emptyText='##E-##'
									}
									if (p.dataType == 'int') {
										xtype = 'numberfield'
										if (category == "EquipmentDC" && specLimit && specLimit.size() == 2) {
											emptyText = specLimit[0] + ' - ' + specLimit[1]
										}
										if (category == "EquipmentDC" && allowRange && allowRange.size() == 2) {
											maxValue = allowRange[1].toLong()
											minValue = allowRange[0].toLong()
										}
										hideTrigger = true
										allowDecimals = false
										decimalPrecision = 0
										value = p.defaultValue
										name = p.name
									}
									if (p.dataType == 'float') {
										xtype = 'numberfield'
										if (category == "EquipmentDC" && specLimit && specLimit.size() == 2) {
											emptyText = specLimit[0] + ' - ' + specLimit[1]
										}
										if (category == "EquipmentDC" && allowRange && allowRange.size() == 2) {
											maxValue = allowRange[1].toLong()
											minValue = allowRange[0].toLong()
										}
										hideTrigger = true
										allowDecimals = true
										decimalPrecision = 3
										value = p.defaultValue
										name = p.name
									}
									if (p.dataType == 'date') {
										xtype = 'datefield'
										format = 'Y-m-d'
										value = p.defaultValue
										name = p.name
									}
								}

								fieldLabel = p.title ?: p.name
								allowBlank = (allow ? true : p.allowBlank)
								columnWidth: 1/2
								if (!p.allowBlank) {
									labelStyle = 'color:FF0000;'
								}

							}
						}
					}
				}

			}

			render (results)
		}
	}

    private def getUnits(listValues) {
        def db = mongo.getDB("glo")
        def lst  = listValues.tokenize('|');
        def query = new BasicDBObject()
        query.put("tkey", lst[2])
        query.put("pkey", lst[1])
        query.put("pctg", lst[0])

        def fields = new BasicDBObject()
        fields.put("code", 1)
        fields.put("id", 1)

        def units = db.unit.find(query, fields).addSpecial('$orderby', new BasicDBObject('code', 1)).collect{[it.code, it.code]}
        return units
    }

	private def comboData (def listValues, def dataType, def pkey, def tkey) {

		if (dataType == 'Company') {
			def companies = companyService.getByPkeyAndTkey(pkey, tkey)
			companies.collect {[it.id, it.name]}
		} else if (dataType == 'Unit') {
            getUnits(listValues)
        } else if (dataType == 'Location') {
			def locations = companyService.getLocationsByPkeyAndTkey(pkey, tkey)
			locations.collect {[it.id, it.name]}
		} else if (dataType == 'Equipment') {
			def equipments = equipmentService.getByPkeyAndTkey(pkey, tkey)
			equipments.collect {[it.id, it.name]}
		} else if (dataType == 'Operation') {
			def operations = operationService.getByPkeyAndTkey(pkey, tkey)
			operations.collect {[it.id, it.name]}
		} else if (dataType == 'WorkCenter') {
			def workCenters = workCenterService.getByPkeyAndTkey(pkey, tkey)
			workCenters.collect {[it.id, it.name]}
		} else if (dataType == 'User') {
			def users = User.list().sort{it.username?.toUpperCase()}
			users = users.collect {it.username}
			["[UNASSIGN]"]+ users
		} else {
			def list = listValues.tokenize(",")
			def ret = list.collect {
				def val = it.trim() == "" ? "  " :
						dataType == 'int' ? new Integer(it) :
						dataType == 'float' ? new Float(it) :
						it
				[val, val]
			}

			ret
		}
	}


}
