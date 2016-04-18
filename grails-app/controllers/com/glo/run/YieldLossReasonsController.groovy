package com.glo.run

import com.glo.ndo.Process
import com.glo.ndo.ProcessStep
import grails.web.JSONBuilder
import org.apache.commons.logging.LogFactory

class YieldLossReasonsController extends Rest {

	private static final logr = LogFactory.getLog(this)

	def list = {

	}

	def getFields = {

		def process = Process.findByCategoryAndPkey(params.category, params.procKey)
		def processStep = ProcessStep.findByProcessAndTaskKey(process, params.taskKey)
		def yields = processStep?.operation?.yieldLossReasons ?: []
		yields = yields + processStep?.yieldLossReasons

		def builder = new JSONBuilder()
		def results = builder.build {
			controls = array {
				var1 = {
					xtype= 'fieldset'
					title = "Loss reason"
					padding = '2 9 2 9'
					items = {
						xtype = 'radiogroup'
						columns = 1
						allowBlank = false
						items = array {
							yields.each { p ->
								variable = {
									xtype = 'radiofield'
									name = 'lossReason'
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
					fieldLabel = 'Loss qty/unit'
					minValue = 1
					value = 1
					allowBlank = false
				}
				var3 = {
					xtype = 'textarea'
					fieldLabel = 'Loss comment'
					name = 'note'
					anchor = '100%'
                    allowBlank = false
				}
//				var4  =  {
//					xtype = 'checkbox'
//					name = 'close'
//					boxLabel = 'Remove unit if Loss qty = Total qty'
//					checked = true
//				}
			}
		}
		render (results)
	}
}
