package com.glo.run

import grails.converters.JSON
import org.apache.commons.logging.LogFactory
import com.glo.ndo.*

class LocationsController extends Rest {

	def mongo
	private static final logr = LogFactory.getLog(this)

	def list = {

		def locations = []
		if (params.company) {
			def company = Company.get(params.company)
			locations = company.locations
		} else if (params.equipment) {
			def loc = Equipment.get(params.equipment)?.location
			if (loc) {
				locations.add(loc)
			}
		} else {
			locations = Location.list()
		}

		render (['data': locations?.collect {[it.id, it.name]}, 'count':locations?.size()] as JSON)
	}	
}
