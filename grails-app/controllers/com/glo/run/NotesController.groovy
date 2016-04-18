package com.glo.run

import grails.converters.JSON
import org.apache.commons.logging.LogFactory

class NotesController extends Rest {

	private static final logr = LogFactory.getLog(this)

	def list = {
		logr.debug(params)

		def unit = Unit.get(new Long(params.unit))
		def notes = unit?.notes

		render (['data': notes, 'count':notes.size()] as JSON)
	}
}
