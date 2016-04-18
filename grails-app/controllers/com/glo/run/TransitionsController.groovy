package com.glo.run

import grails.converters.JSON
import org.apache.commons.logging.LogFactory


class TransitionsController extends Rest {

	private static final logr = LogFactory.getLog(this)

	def springSecurityService
	def identityService
	def managementService
	def processEngine
	def runtimeService
	def repositoryService
	def taskService
	def workflowService

	static activiti = true

	def getTransitions = {

		def username =  springSecurityService.principal?.username ?: 'admin'

		render (workflowService.getTransitionsByTask(params.processKey, params.taskKey, "all") as JSON)
	}

	def executeTransition = {

		try {
			def tasks = JSON.parse(params.data)
			def username =  springSecurityService.principal?.username ?: 'admin'
			workflowService.completeTasks (tasks)
			success("")
		} catch (Exception exc) {
			error(exc)
		}
	}
}
