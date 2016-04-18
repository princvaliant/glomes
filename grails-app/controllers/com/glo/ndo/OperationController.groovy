package com.glo.ndo

class OperationController {

	def springSecurityService

	def scaffold = true

	static navigation = [
		group:'admin',
		order:30,
		action:'list',
		title: "navigation.glo.operation",
		isVisible: {
			springSecurityService.isLoggedIn() &&
					org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils.ifAllGranted("ROLE_ADMIN")
		}
	]
}
