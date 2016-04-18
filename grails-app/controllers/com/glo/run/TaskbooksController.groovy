package com.glo.run


import grails.converters.JSON
import com.glo.ndo.*
import org.apache.commons.logging.LogFactory

class TaskbooksController {

	private static final logr = LogFactory.getLog(this)

	def springSecurityService
	def identityService
	def managementService
	def processEngine
	def runtimeService
	def repositoryService
	def taskService
	def workflowService
	def unitService
	def grailsApplication

	static activiti = true



	def processSummary = {

		logr.debug(params)

		def username =  springSecurityService.principal?.username 
		if (!username) throw new RuntimeException ("Session expired. Please re-login.")
		
		render (unitService.getProcessesSummary(username, params.all) as JSON)
	}

	def taskNameSummary = {

		logr.debug(params)
		def username =  springSecurityService.principal?.username 
		if (!username) throw new RuntimeException ("Session expired. Please re-login.")
		render (unitService.getStepsSummary(username, params.category) as JSON)
	}

	def startProcesses = {

		logr.debug(params)

		def username =  springSecurityService.principal?.username ?: 'admin'
		render (workflowService.getStartProcesses(username) as JSON)
	}

	def unitTasks = {

		logr.debug(params)

		def username =  springSecurityService.principal?.username ?: 'admin'
		render (workflowService.getTasksByProcCategory(username, params.category, params.taskName) as JSON)
	}

	def taskDefs = {

		logr.debug(params)

		def username =  springSecurityService.principal?.username ?: 'admin'
		def taskDefs = workflowService.getTaskDefs(params.processKey)
		render (taskDefs.collect {
			[taskKey: it.key,
						taskName:it.value.nameExpression.toString()]
		}.sort {it.taskName} as JSON)
	}
	
	def taskDefsCompleted = {
		
		logr.debug(params)
		def isAdmin = org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN,ROLE_PROCESS_ADMIN,ROLE_OPERATION_MOVE,ROLE_ENGINEERING_MOVE")
		def username =  springSecurityService.principal?.username ?: 'admin'
		def taskDefs = workflowService.getTaskDefs(params.processKey)
		def tasks
		if(!isAdmin) {
		  	tasks = taskDefs.findAll { it.key == params.taskKey }
		} else {
			tasks = taskDefs
		}
		render (tasks.collect {
			[taskKey: it.key,taskName:it.value.nameExpression.toString()]
		}.sort {it.taskName} as JSON)
	}
	
	def procDefsCompleted= {
		
		logr.debug(params)
		def isAdmin = org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN,ROLE_PROCESS_ADMIN,ROLE_OPERATION_MOVE,ROLE_ENGINEERING_MOVE")
		def username =  springSecurityService.principal?.username ?: 'admin'
		def procDefs = workflowService.getProcessDefs(params.category)
		def procs 
		if(!isAdmin) {
			procs = procDefs.findAll { it.key == params.procKey }
		  } else {
			procs = procDefs
		 }
		render (procs.collect {
			[procId: it.id, procKey: it.key, procName: it.name + " [" + it.key + "]"]
		}.sort {it.procId} as JSON)
	}
		
	
	def procDefs = {

		logr.debug(params)

		def username =  springSecurityService.principal?.username ?: 'admin'
		def procDefs = []
        workflowService.getProcessDefs(params.category)?.collect {
            def process = Process.findByPkey(it.key)
            if (process && !process?.disabled) {
                procDefs.add([procId: it.id, procKey: it.key, procName: it.name + " [" + it.category + "]"])
            }
		}
		
		def procMoveIns = []
        Process.findAllByEngMoveIn(true)?.collect {
            if (!procDefs.find{f -> f.procKey == it.pkey && !it.disabled})
                procMoveIns.add([procId: it.id, procKey: it.pkey, procName: it.pkey + " [" + it.category + "]"])
		}
				
		render ((procDefs + procMoveIns).sort {it.procId} as JSON)
	}
	
	def processes = {
		
		logr.debug(params)

		def username =  springSecurityService.principal?.username ?: 'admin'
		def procesess = []
		
		if (params.product) {
			procesess = workflowService.getProcessesPerProduct(params.product)
		} else {
			procesess = Process.list()
		}
		
		render (procesess.collect {
			[id: it.id, name: it.pkey]
		}.sort {it.name} as JSON)
	}

	def diagram = {

		logr.debug(params)
		InputStream stream
		byte[] bytes
		try {
			def username =  springSecurityService.principal?.username ?: 'admin'
			stream =  workflowService.getDiagram(params.processKey, params.taskKey)
			bytes = stream.getBytes()
		} finally {
			if (stream)
				org.activiti.engine.impl.util.IoUtil.closeSilently(stream)
		}

		response.contentType = "image/png"
		response.outputStream << bytes
	}
}
