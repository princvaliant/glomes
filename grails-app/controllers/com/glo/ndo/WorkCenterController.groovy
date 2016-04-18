package com.glo.ndo

class WorkCenterController {

	def springSecurityService

	def scaffold = true

	static navigation = [
		group:'admin',
		order:31,
		action:'list',
		title: "navigation.glo.workCenter",
		isVisible: {
			springSecurityService.isLoggedIn() &&
					org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils.ifAllGranted("ROLE_ADMIN")
		}
	]
}
