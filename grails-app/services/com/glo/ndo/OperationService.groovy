package com.glo.ndo

import org.apache.commons.logging.LogFactory

class OperationService {

	private static final logr = LogFactory.getLog(this)
	def managementService
	def runtimeService
	def repositoryService
	def taskService
	def messageSource
	def mongo

	static transactional = false

	def getByPkeyAndTkey (def pkey, def tkey) {

		def operations
		if (pkey && tkey) {

			def process = Process.findByPkey (pkey)
			def processStep = ProcessStep.findByProcessAndTaskKey(process, tkey)
			operations = [processStep.operation]
		}

		if (!operations) {
			operations = Operation.list()
		}
		operations.sort()
	}
}
