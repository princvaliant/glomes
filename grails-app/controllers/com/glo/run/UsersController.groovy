package com.glo.run

import grails.converters.JSON
import org.apache.commons.logging.LogFactory
import com.glo.security.*

class UsersController extends Rest {

	private static final logr = LogFactory.getLog(this)
	def springSecurityService


	def list = {
		logr.debug(params)

		def users = User.list()
		def currentUser =  springSecurityService.principal?.username
		
		render (['data': users.collect {[id: it.username,username:it.username, current:currentUser]}, 'count':users.size()] as JSON)
	}

	def getCurrent = {
		logr.debug(params)

		def username =  springSecurityService.principal?.username
		render (['data': [username:username], 'count':1] as JSON)
	}
	
	def hasRole = {
		logr.debug(params)

		def username =  springSecurityService.principal?.username
		def user = User.findByUsername(username)
		def ret = "0"
		for (def role in user?.getAuthorities()) { 
			if (role.name == params.role) {
				ret = "1"
				break;
			}
		}
		render (['ok': ret] as JSON)
	}
}
