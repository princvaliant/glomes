package com.glo.run

import grails.converters.JSON
import org.apache.commons.logging.LogFactory

class CompaniesController extends Rest {

	private static final logr = LogFactory.getLog(this)
	def companyService

	def list = {
		logr.debug(params)

		def companies = []
		if (params.product) {
			companies = companyService.getByProduct(params.product)
			render (['data': companies.collect {[id: it.id,name:it.name]}, 'count':companies.size()] as JSON)
		} else if (params.processKey && params.taskKey) {
			companies = companyService.getByPkeyAndTkey(params.processKey,params.taskKey)
			render (['data': companies.collect {[id: it.id,name:it.name]}, 'count':companies.size()] as JSON)
		} else if (params.equipment) {
			companies = companyService.getByEquipment(params.equipment)
			render (['data': companies.collect {[it.id,it.name]}, 'count':companies.size()] as JSON)
		} else {
			render (['data': [:], 'count':0] as JSON)
		}
		
	}
}
