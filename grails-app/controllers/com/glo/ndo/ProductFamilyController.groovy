package com.glo.ndo

class ProductFamilyController {

	def springSecurityService

	def scaffold = true

	static navigation = [
		group:'admin',
		order:5,
		action:'list',
		title: "navigation.glo.productFamily",
		isVisible: {
			springSecurityService.isLoggedIn() &&
					org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN,ROLE_EQUIPMENT_ADMIN")
		}
	]
}
