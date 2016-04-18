package com.glo.security

import grails.converters.*

class UserController {

	def springSecurityService
	def userRightService

	def scaffold = true

	static navigation = [
		group:'admin',
		order:35,
		action:'list',
		title: "navigation.security.User",
		isVisible: {
			springSecurityService.isLoggedIn() &&
			org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN")
		}
	]

	def index = {
		redirect(action: "list", params: params)
	}

	def list = {
		def userList
		def userTotal

		params.max = Math.min(params.max ? params.int('max') : 12, 240)
		if (params.q) {
			userList = User.search(params.q + "*").results
			userTotal = userList.size()
		}
		else {
			userList = User.list(params)
			userTotal = User.count()
		}

		[userInstanceList: userList, userInstanceTotal:userTotal, search:params.q]
	}

	def create = {
		def userInstance = new User()
		userInstance.properties = params
		userInstance.enabled = true
		return [userInstance: userInstance]
	}

	def save = {
		def userInstance = new User(params)
		if (params.tempPassword) {
			userInstance.password = springSecurityService.encodePassword(params.tempPassword)
		}
		if (userInstance.save(flush: true)) {
			flash.message = "${message(code: 'default.created.message', args: [message(code: 'user.label', default: 'User'), userInstance.username])}"
			redirect(action: "edit", id: userInstance.id)
		}
		else {
			render(view: "create", model: [userInstance: userInstance])
		}
	}

	def show = {
		def userInstance = User.get(params.id)
		if (!userInstance) {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'user.label', default: 'User'), params.id])}"
			redirect(action: "list")
		}
		else {
			[userInstance: userInstance]
		}
	}

	def edit = {
		def userInstance = User.get(params.id)
		if (!userInstance) {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'user.label', default: 'User'), params.id])}"
			redirect(action: "list")
		}
		else {
			return [userInstance: userInstance, authorities:Role.list().sort{it.authority}]
		}
	}

	def update = {
		def userInstance = User.get(params.id)
		if (userInstance) {
			if (params.version) {
				def version = params.version.toLong()
				if (userInstance.version > version) {

					userInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [
						message(code: 'user.label', default: 'User')]
					as Object[], "Another user has updated this User while you were editing")
					render(view: "edit", model: [userInstance: userInstance])
					return
				}
			}
			userInstance.properties = params
			if (params.tempPassword) {
				userInstance.password = springSecurityService.encodePassword(params.tempPassword)
			}
			if (!userInstance.hasErrors() && userInstance.save(flush: true)) {
				flash.message = "${message(code: 'default.updated.message', args: [message(code: 'user.label', default: 'User'), userInstance.username])}"
				redirect(action: "edit", id: userInstance.id)
			}
			else {
				render(view: "edit", model: [userInstance: userInstance])
			}
		}
		else {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'user.label', default: 'User'), params.id])}"
			redirect(action: "list")
		}
	}

	def delete = {
		def userInstance = User.get(params.id)
		if (userInstance) {
			try {
				UserRole.removeAll(userInstance)
				userInstance.delete(flush: true)
				flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'user.label', default: 'User'), params.username])}"
				redirect(action: "list")
			}
			catch (org.springframework.dao.DataIntegrityViolationException e) {
				flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'user.label', default: 'User'), params.id])}"
				redirect(action: "show", id: params.id)
			}
		}
		else {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'user.label', default: 'User'), params.id])}"
			redirect(action: "list")
		}
	}

	def addRole = {

		def role = Role.findByAuthority(params.addRoleAutoComplete)
		def user = User.get(params.id)
		if (!role) {
			flash.message = "Select role from list"
		} else {
			UserRole.create( user,  role)
		}
		redirect  action:'edit', id:user.id
	}

	def removeRole = {
		
		def role = Role.get(params.roleId)
		def user = User.get(params.userId)
		UserRole.remove(user,role)
		redirect  action:'edit', id:user.id
	}

	def rolesAutoComplete = {
		def user = User.get(params.userId)
		def roles = Role.findAllByAuthorityLike("%${params.query}%")
		roles = roles.findAll {
			!(it in user.authorities)
		}

		roles = roles.collect {
			[id: it.id, name: it.authority]
		}
		def jsonRole = [
					result: roles
				]
		render jsonRole as JSON
	}
}
