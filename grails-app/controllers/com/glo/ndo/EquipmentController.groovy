package com.glo.ndo

class EquipmentController {

	def springSecurityService

	def scaffold = true

	static navigation = [
		group:'admin',
		order:25,
		action:'list',
		title: "navigation.glo.equipment",
		isVisible: {
			springSecurityService.isLoggedIn() &&
					org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN,ROLE_EQUIPMENT_ADMIN")
		}
	]
}
