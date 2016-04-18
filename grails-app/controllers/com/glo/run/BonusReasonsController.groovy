package com.glo.run

import com.glo.ndo.Process
import com.glo.ndo.ProcessStep
import grails.web.JSONBuilder
import org.apache.commons.logging.LogFactory

class BonusReasonsController extends Rest {

	private static final logr = LogFactory.getLog(this)

	def list = {
	}

	def getFields = {

		def process = Process.findByCategoryAndPkey(params.category, params.procKey)
		def processStep = ProcessStep.findByProcessAndTaskKey(process, params.taskKey)
		def bonuses = processStep?.operation?.bonusReasons  ?: []
		bonuses = bonuses + processStep?.bonusReasons

		def builder = new JSONBuilder()
		def results = builder.build {
			controls = array {
				var1 = {
					xtype= 'fieldset'
					title = "Bonus reason"
					padding = '2 9 2 9'
					items = {
						xtype = 'radiogroup'
						columns = 1
						allowBlank = false
						items = array {
							bonuses.each { p ->
								variable = {
									xtype = 'radiofield'
									name = 'bonusReason'
									inputValue = p.id
									boxLabel =  p.name
									boxLabelAlign = 'after'
									anchor = '96%'
									autoScroll = true
								}
							}
						}
					}
				}
				var2  =  {
					xtype = 'numberfield'
					name = 'qty'
					fieldLabel = 'Bonus qty/unit'
					minValue = 1
					value = 1
					allowBlank = false
				}
				var3 = {
					xtype = 'textarea'
					fieldLabel = 'Bonus comment'
					name = 'note'
					anchor = '100%'
				}
			}
		}
		render (results)
	}
}
