package com.glo.run

import org.activiti.engine.impl.bpmn.behavior.*
import org.activiti.engine.impl.bpmn.diagram.ProcessDiagramGenerator
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity
import org.activiti.engine.impl.pvm.PvmActivity
import org.activiti.engine.impl.pvm.PvmExecution
import org.activiti.engine.impl.pvm.PvmTransition
import org.activiti.engine.task.Task
import org.apache.commons.logging.LogFactory
import com.glo.ndo.*


class WorkflowService {

	private static final logr = LogFactory.getLog(this)
	def managementService
	def processEngine
	def runtimeService
	def repositoryService
	def taskService
	def messageSource
	def mongo

	static transactional = true


	/// Retrieve list of processes that user can start
	/// Return list of objects with fields:
	/// String procDefKey
	/// String procName
	def getStartProcesses(String user) {

		def result = []
		def pdes = repositoryService.createProcessDefinitionQuery().latestVersion().list()
		pdes.each { it ->
			
			ProcessDefinitionEntity pde = (ProcessDefinitionEntity) repositoryService.getDeployedProcessDefinition(it.id)
			def startAct = pde.initial.outgoingTransitions[0].destination
			def startTask = pde.taskDefinitions[startAct?.id]
			
			def process = Process.findByCategoryAndPkey(pde.category, pde.key)
			if (!process?.disabled) {
                startTask?.candidateGroupIdExpressions.each {
                    if (org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils.ifAnyGranted(it.toString()) &&
                            process?.products) {
                        result.add(pde)
                    }
                }
            }
		}

		return  result.collect {[pctg: it.category, pkey:it.key, procName: 
			messageSource.getMessage("process.category." + it.category, null, it.category, Locale.US) + " - " + it.name]}.sort{it.procName}
	}
	
	def getProcessesPerProduct(def productId) {
		
		def product = Product.get(productId)
		[product.startProcess]
	}
	
	def getTaskForTransition (String processKey, String sourceTkey, String transition) {
		
		def pde = getPde(processKey)
		def activity = pde.activities.find{ it.id == sourceTkey}
		if (!activity)
			return ""
			
		List<PvmTransition> out = activity.getOutgoingTransitions()
		
		if (out.size() == 1) {
			def node = out[0]
			if (node?.destination?.activityBehavior?.getClass() == ExclusiveGatewayActivityBehavior) {

				out = out + node.destination.getOutgoingTransitions()
			}
		}
		
		def outTrans = out.find { it?.properties?.name == transition }
		if (outTrans?.destination?.activityBehavior?.getClass() == UserTaskActivityBehavior) {
			outTrans.destination?.id
		} else {
			""
		}
	}


	def getTransitionsByTask (String processKey, String taskKey, String mode) {

		def pde = getPde(processKey)
		def activity = pde.activities.find{ it.id == taskKey}
		if (!activity)
			return []
		List<PvmTransition> out = activity.getOutgoingTransitions()

		if (out.size() == 1) {
			def node = out[0]
			if (node.destination.activityBehavior.getClass() == ExclusiveGatewayActivityBehavior) {

				out = node.destination.getOutgoingTransitions()
			}
		}
		
				
		def process = Process.findByCategoryAndPkey(pde.category, pde.key)
		def ps = getProcessStep(pde.category, processKey, taskKey)
		def ret = []
		
		// Routing script based
        def route
		if (mode != "flow_only") {
			if (ps?.routingScript?.trim()) {
				route = [id: -1, name: 'Auto-route', pctg: pde.category, taskKey: '', icon:'icon-auto-route', mpath: "yes"]
				ret += route
			}
        }

        // Add engineering move for ROLE_ADMIN
        def engMove = "none"
        if (org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils.ifAnyGranted("ROLE_OPERATION_MOVE") && ps?.disableOperationMove != true)
        {
            engMove = "-O"
        }
        if (engMove == "none" && org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils.ifAnyGranted("ROLE_ENGINEERING_MOVE") && ps?.disableEngineeringMove != true)
        {
            engMove = ""
        }

		// Process flow transitions
        if (!route || (route && engMove != "none")) {

            out = out.findAll { it.properties?.name != "" }
            out = out.sort {  it.properties?.conditionText?.trim() }

            ret += out.collect {
                def mpath = it.properties?.conditionText?.trim() == "1" || !it.properties?.conditionText ? "yes" : "no"
                [id: it.id, name:  it.properties?.name, pctg: it.processDefinition.category, taskKey: it.destination?.id, icon: 'icon-report_go', mpath: mpath]
            }
        }
		
		if (mode == "flow_only") {
			return(ret)
		}

		if (engMove == "none" && process?.engtMoveRoles)
		{
			def pkeyEngRoles = process?.engtMoveRoles.tokenize(',')
			pkeyEngRoles.each {
				if (org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils.ifAnyGranted(it.toString().trim())) {
					engMove = "-T"
				}
			}
		}
		if (engMove != "none") {
			def eng = [name: 'Engineering' + engMove, pctg: pde.category, taskKey: taskKey, icon:'icon-ruby_go']
			ret += eng
		}

		// Determine if other buttons should be shown
		if (ps?.allowSplit) {
			def split =  [name: 'Split', pctg: pde.category, taskKey: taskKey, icon:'icon-ruby_go']
			ret += split     
		}
		if (ps?.allowMerge) {
			def merge =  [name: 'Merge', pctg: pde.category, taskKey: taskKey, icon:'icon-ruby_go']
			ret += merge
		}
		if (ps?.allowSync) {
			def sync =  [name: 'Sync', pctg: pde.category, processKey: processKey, taskKey: taskKey, icon:'icon-ruby_go']
			ret += sync
		}
		if (ps?.allowImport) {
			def imp =  [name: 'Import', pctg: pde.category, processKey: processKey, taskKey: taskKey, icon:'icon-ruby_go']
			ret += imp
		}
		if (ps?.allowRework) {
			def rework =  [name: 'Rework', pctg: pde.category, processKey: processKey, taskKey: taskKey, icon:'icon-rework']
			ret += rework
		}
        if (ps?.allowCustomAction) {
            def customAction =  [name: ps.customAction ?: "Action" , pctg: pde.category, processKey: processKey, taskKey: taskKey, icon:'icon-report2']
            ret += customAction
        }
		
		def labels = ps.barcodePrinting ?: ""
		def printers = ps.barcodePrinter ?: ""
		
		if (labels != "" && printers != "") {
			def bc =  [name: 'Barcodes', pctg: pde.category, processKey: processKey, taskKey: taskKey, icon:'icon-barcode']
			ret += bc
		}
		
		def multi
		multi = [name: "Edit", pctg: pde.category, processKey: processKey, taskKey: taskKey, icon:"icon-table_edit"]
		def fileUpload
		fileUpload = [name: "File", pctg:  pde.category, processKey: processKey, taskKey: taskKey, icon:"icon-file_upload"]
		def yieldLoss
		yieldLoss = [name: "Loss", pctg: pde.category, processKey: processKey, taskKey: taskKey, icon:"icon-table_delete"]
		def bonus
		bonus = [name: "Bonus", pctg: pde.category, processKey: processKey, taskKey: taskKey, icon:"icon-shape_square_add"]
			
		ret + yieldLoss  + fileUpload + multi
	}

	def findFirstTaskDefinition(def pctg, def pkey) {

		def pde = getPde(pkey)
		def initialTask = pde.initial.getOutgoingTransitions()[0].destination

		def ret = new Expando()
		ret.tkey = initialTask.id
		ret.tname = initialTask.properties.name
		ret.pctg = pde.category
		ret.pkey = pde.key
		ret.groups = pde.taskDefinitions[initialTask.id].candidateGroupIdExpressions
		ret.users = pde.taskDefinitions[initialTask.id].candidateUserIdExpressions

		ret
	}


	def completeTasks (def tasks) {

		tasks.each { parm ->
			def task = taskService.createTaskQuery().taskId(parm.taskId).singleResult()
			runtimeService.setVariable(task.executionId, "st", parm.transition)
			PvmActivity act = getActivity(task)

			List<PvmTransition> out = act.outgoingTransitions
			if (out.size() == 1) {
				def node = out[0]
				if (node.destination.activityBehavior.getClass() == ExclusiveGatewayActivityBehavior) {
					node.destination.outgoingTransitions.each {

						//	def t = '${st == \'' + it.id + '\'}'
						//	it.setProperty("conditionText", t)

						logr.debug(it.getProperty("conditionText"))
					}
				}
			}
			taskService.complete(task.id)
		}
	}

	def getTaskDef(String processKey, String taskKey) {

		def pde = getPde(processKey)
		def td = pde.taskDefinitions.find{it.key == taskKey}
		def ret = new Expando()
		ret.tkey = td ? td.value.key : "end"
		ret.tname = td ? td.value.properties.name : "End"
		ret.pctg = pde.category
		ret.pkey = pde.key
		ret.groups = td?.value?.candidateGroupIdExpressions
		ret.users = td?.value?.candidateUserIdExpressions
		ret
	}

	def getTaskName(String processKey, String taskKey) {

		def pde = getPde(processKey)
		pde.activities.find{it.id == taskKey}.properties.name
	}

	def getTaskDefs(String processKey) {

		def pd = repositoryService.createProcessDefinitionQuery().processDefinitionKey(processKey).latestVersion().singleResult()
		ProcessDefinitionEntity pde = (ProcessDefinitionEntity) repositoryService.getDeployedProcessDefinition(pd.id)
		pde.taskDefinitions
	}

	def getProcessDefs(String category) {

		repositoryService.createProcessDefinitionQuery().processDefinitionCategory(category).latestVersion().list()
	}


	def getDiagram (def processKey, def taskKey) {

		def processDiagramGenerator = new ProcessDiagramGenerator()
		def pde = getPde(processKey)
		def acts = pde.activities.findAll{it.id == taskKey}.id
		processDiagramGenerator.generateDiagram(pde, "png", acts)
	}

	def getActivity(Task task) {

		PvmExecution exe = runtimeService.createExecutionQuery().executionId(task.getExecutionId()).singleResult()
		ProcessDefinitionEntity pde = (ProcessDefinitionEntity) repositoryService.getDeployedProcessDefinition(task.processDefinitionId)
		pde.findActivity(exe.getActivityId())
	}

	private def getPde(def pkey) {
		def pd = repositoryService.createProcessDefinitionQuery().processDefinitionKey(pkey).latestVersion().singleResult()
		(ProcessDefinitionEntity) repositoryService.getDeployedProcessDefinition(pd.id)
	}

	def getProcessStep(def pctg, def pkey, def tkey) {
		def process = Process.findByCategoryAndPkey(pctg, pkey)
		ProcessStep.findByProcessAndTaskKey(process, tkey )
	}
		
	def getPctg(def pkey) {
		def pr = Process.findByPkey(pkey)
		pr.category
	}
}
