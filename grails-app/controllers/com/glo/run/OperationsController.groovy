
package com.glo.run

import grails.converters.JSON
import org.apache.commons.logging.LogFactory

class OperationsController extends Rest {

	private static final logr = LogFactory.getLog(this)

	def operationService

	def list = {

		def operations = operationService.getByPkeyAndTkey(params.processKey, params.taskKey)

		render (['data': operations.collect {[id: it.id,name:it.name]}, 'count':operations.size()] as JSON)
	}
}
