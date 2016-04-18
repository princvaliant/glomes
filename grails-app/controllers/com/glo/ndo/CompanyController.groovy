package com.glo.ndo

class CompanyController {

	def springSecurityService

	def scaffold = true

	static navigation = [
		group:'admin',
		order:10,
		action:'list',
		title: "navigation.glo.company",
		isVisible: {
			springSecurityService.isLoggedIn() &&
					org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN,ROLE_EQUIPMENT_ADMIN")
		}
	]
	
	def list = {
		
		def companyList
		def companyTotal

		params.max = Math.min(params.max ? params.int('max') : 20, 240)
		
		if (params.q) {
			
			def pl = Company.search(params.q + "*", params)
			
			companyList = pl.results.collect{ Company.get(it.id)}
			companyTotal = pl.total
		}
		else {
			companyList = Company.list(params)
			companyTotal = Company.count()
		}
		
		[companyInstanceList: companyList, companyInstanceTotal:companyTotal, search:params.q]
	}
}
