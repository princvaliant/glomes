package com.glo.ndo

class ProductCompanyController {

	def springSecurityService

	def scaffold = true

	static navigation = [
		group:'admin',
		order:7,
		action:'list',
		title: "navigation.glo.productCompany",
		isVisible: {
			springSecurityService.isLoggedIn() &&
					org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN,ROLE_EQUIPMENT_ADMIN")
		}
	]
}
