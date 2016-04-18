package com.glo.run

import grails.converters.JSON
import org.apache.commons.logging.LogFactory
import com.glo.ndo.*


class WorkCentersController extends Rest {

	private static final logr = LogFactory.getLog(this)

	def list = {
		logr.debug(params)

		def workCenters = WorkCenter.list()
		def wcs = workCenters.collect {[id: it.id,name:it.name, comment:it.comment]}
		wcs.add([id:0, name:'(view all)', comment:''])
		render (['data': wcs, 'count':workCenters.size()] as JSON)
	}
}
