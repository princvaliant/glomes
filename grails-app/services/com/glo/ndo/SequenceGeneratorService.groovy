package com.glo.ndo

import org.apache.commons.logging.LogFactory

class SequenceGeneratorService {

	private static final logr = LogFactory.getLog(this)
	def managementService
	def runtimeService
	def repositoryService
	def taskService
	def messageSource
	def mongo

    def _LETTERS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";

	static transactional = false

	def next (def name) {

		def seq = SequenceGenerator.findByName(name)
		if (!seq) {
			seq = new SequenceGenerator()
			seq.name = name
			seq.value = 1
		} else {
			seq.value += 1
		}
		seq.save(failOnError: true)
		seq.value
	}
	
	def last (def name) {

		def seq = SequenceGenerator.findByName(name)
		if (!seq) {
			seq = new SequenceGenerator()
			seq.name = name
			seq.value = 1
			seq.save(failOnError: true)
		}
		seq.value
	}

    def nextc (def name) {

        def seq = next(name)
        Integer.toString((int)seq.value, 36).toUpperCase()
    }
}
