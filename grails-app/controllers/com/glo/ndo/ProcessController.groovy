package com.glo.ndo

class ProcessController {

	def springSecurityService

	def scaffold = true

	static navigation = [
		group:'admin',
		order:20,
		action:'list',
		title: "navigation.glo.process",
		isVisible: {
			springSecurityService.isLoggedIn() &&
					org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils.ifAllGranted("ROLE_PROCESS_ADMIN")
		}
	]
}
