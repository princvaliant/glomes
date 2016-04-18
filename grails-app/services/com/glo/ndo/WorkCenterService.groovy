package com.glo.ndo

import org.apache.commons.logging.LogFactory

class WorkCenterService {

	private static final logr = LogFactory.getLog(this)
	def managementService
	def runtimeService
	def repositoryService
	def taskService
	def messageSource
	def mongo

	static transactional = false

	def getByPkeyAndTkey (def pkey, def tkey) {

		def workCenters
		if (pkey && tkey) {

			def process = Process.findByPkey (pkey)
			def processStep = ProcessStep.findByProcessAndTaskKey(process, tkey)
			workCenters = [processStep?.operation?.workCenter]
		}

		if (!workCenters) {
			workCenters = WorkCenter.list()
		}
		workCenters.sort()
	}
}
