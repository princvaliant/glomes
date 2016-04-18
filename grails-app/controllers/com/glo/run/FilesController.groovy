package com.glo.run

import grails.converters.JSON
import org.apache.commons.logging.LogFactory

class FilesController extends Rest {

	private static final logr = LogFactory.getLog(this)
	def fileService

	def list = {
		logr.debug(params)

		def files = Unit.get(new Long(params.unit))?.files

		render (['data': files, 'count':files.size()] as JSON)
	}
	
	def download = {
		logr.debug(params)

		def gf = fileService.retrieveFile(params.file)
		
		response.contentType = gf?.getContentType()
		response.setHeader("Content-disposition", "attachment;filename=" + gf?.filename)
		response.outputStream << gf?.getInputStream()
	
	}
}
