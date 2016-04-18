package com.glo.security

class RoleController {

	def springSecurityService
	def userRightService

	def scaffold = true

	static navigation = [
		group:'admin',
		order:40,
		action:'list',
		title: "navigation.security.Groups",
		isVisible: { 
			springSecurityService.isLoggedIn() &&
			org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN")
		}
	]

	def index = {
		redirect(action: "list", params: params)
	}

	def save = {
		def roleInstance = new Role(params)
		roleInstance.id = roleInstance.authority
		if (roleInstance.save(flush: true)) {
			flash.message = "${message(code: 'default.created.message', args: [message(code: 'user.label', default: 'User'), roleInstance.authority])}"
			redirect(action: "edit", id: roleInstance.id)
		}
		else {
			render(view: "create", model: [roleInstance: roleInstance])
		}
	}

	
	def edit = {
		
		def roleInstance = Role.get(params.id)
		if (!roleInstance) {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'role.label', default: 'Role'), params.id])}"
			redirect(action: "list")
		}
		else {
			return [roleInstance: roleInstance, 
				users: User.list().sort{p1, p2 -> p1.username.compareToIgnoreCase(p2.username)}, 
				roleUsers: roleInstance?.users?.sort{p1, p2 -> p1.username.compareToIgnoreCase(p2.username)}]
		}
	}

	
	def addUser = {
		
		def user = User.get(params.addUserAutoComplete)
		def role = Role.get(params.id)
		if (!user) {
			flash.message = "Select user from list"
		} else {
			UserRole.create( user,  role)
		}
		redirect  action:'edit', id:role.id
	}

	def removeRole = {
		
		def role = Role.get(params.roleId)
		def user = User.get(params.userId)
		UserRole.remove(user,role)
		redirect  action:'edit', id:role.id
	}
}
