package com.glo.ndo

import org.apache.commons.logging.LogFactory

class CompanyService {

	private static final logr = LogFactory.getLog(this)
	def managementService
	def runtimeService
	def repositoryService
	def taskService
	def messageSource
	def mongo

	static transactional = false

	def getByPkeyAndTkey (def pkey, def tkey) {

		def companies = []
		if (pkey && tkey) {

			def process = Process.findByPkey (pkey)
			def processStep = ProcessStep.findByProcessAndTaskKey(process, tkey)
			companies = processStep.companies
		}

		if (!companies) {
			companies = Company.list()
		}
		companies.sort()
	}

	def getByProduct(def productId) {

		def companies = []
		if (productId?.isLong()) {
			def product = Product.get(productId)
			if (product) {
				def productCompanies = ProductCompany.findAllByProduct(product)
				companies = productCompanies.collect {it.company}
			}
		}

		companies.sort()
	}
	
	def getByEquipment(def equipmentId) {
		
		def companies = []
		if (equipmentId) {
			companies.add(Equipment.get(equipmentId)?.company)
		}

		if (!companies) {
			companies = Company.list()
		}
		companies.sort()
	}

	def getLocationsByPkeyAndTkey (def pkey, def tkey) {

		def locations = []
		def companies = getByPkeyAndTkey ( pkey, tkey)
		companies.each {
			it.locations.each { locations.add(it) }
		}
		locations.sort()
	}
}
