package com.glo.excel

import org.apache.commons.logging.LogFactory

class AnalyserController {

	private static final logr = LogFactory.getLog(this)

	def springSecurityService
	def contentService
	def unitService
	def epiRunService
	def mongo
	
	def epi = {
		
		def i = 0
		
		logr.debug('EPI import in progress')
		
		try {
			 epiRunService.importEpiData('S2')
		} catch(Exception exc) {
			logr.error(exc.getMessage())
		}
		try {
			 epiRunService.importEpiData('D1')
		} catch(Exception exc) {
			logr.error(exc.getMessage())
		}
		render i.toString()
	}
	
}
