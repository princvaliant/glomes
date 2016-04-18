package com.glo.run

import grails.web.JSONBuilder
import org.apache.commons.logging.LogFactory
import com.glo.ndo.*

class ReworkReasonsController extends Rest {

	private static final logr = LogFactory.getLog(this)

	def list = {

	}

	def getFields = {

		def process = Process.findByCategoryAndPkey(params.category, params.procKey)
		def processStep = ProcessStep.findByProcessAndTaskKey(process, params.taskKey)
		def reworks = processStep?.operation?.reworkReasons ?: []
		reworks = reworks + processStep?.reworkReasons

		def builder = new JSONBuilder()
		def results = builder.build {
			controls = array {
				var1 = {
					xtype= 'fieldset'
					title = "Rework reason"
					padding = '2 9 2 9'
					items = {
						xtype = 'radiogroup'
						columns = 1
						allowBlank = false
						items = array {
							reworks.each { p ->
								variable = {
									xtype = 'radiofield'
									name = 'reworkReason'
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
				var3 = {
					xtype = 'textarea'
					fieldLabel = 'Rework comment'
					name = 'note'
					anchor = '100%'
				}
			}
		}
		render (results)
	}
}
