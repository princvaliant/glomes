package com.glo.run

import com.glo.custom.DieSpec
import grails.converters.JSON
import org.apache.commons.logging.LogFactory

class MetaItemsController extends Rest {

	private static final logr = LogFactory.getLog(this)
	
	def springSecurityService
	def mongo

	def list = {

		def dieSpecs = DieSpec.list(params)

		render (['data': dieSpecs.collect {[id:it.id, name:it.name + "(Rev. " + (it?.revision) + ")"]}.sort{it.name}, 'count':DieSpec.list( ).size()] as JSON)
	}
}
