package com.glo.ndo

import java.util.zip.ZipInputStream
import org.activiti.engine.repository.Deployment
import org.apache.commons.logging.LogFactory


class DeployController {

	private static final logr = LogFactory.getLog(this)
	def springSecurityService
	def repositoryService
	def contentService
	def workflowService


	def index = {
	}

	def list = {

		def deploys = repositoryService.createDeploymentQuery()
				.deploymentNameLike("%")
				.list()

		[deployments: deploys, deploymentsTotal: deploys.size()]
	}

	def deploy = {
		
		def f = request.getFile("file")

		try {
			Deployment deployment
			if (f.fileItem.fileName.lastIndexOf('xml') <= 0) {

				ZipInputStream inputStream = new ZipInputStream(new ByteArrayInputStream(f.getBytes()))
				deployment = repositoryService.createDeployment()
						.name(f.fileItem.fileName)
						.addZipInputStream(inputStream)
						.deploy()
			} else {
				InputStream inputStream = new ByteArrayInputStream(f.getBytes())
				deployment = repositoryService.createDeployment()
						.name(f.fileItem.fileName)
						.addInputStream( f.fileItem.fileName,inputStream)
						.deploy()
			}

			def pdef = repositoryService.createProcessDefinitionQuery().deploymentId(deployment.id).singleResult()

			def processCategory = ProcessCategory.findByCategory(pdef.category)
			if (!processCategory) {
				processCategory = new ProcessCategory(category:pdef.category, mongoCollection:'dataReport')
				processCategory.save(failOnError: true)
			}
			
			def process = Process.findByProcessCategoryAndPkey(processCategory, pdef.key)
			if (!process) {
				process = new Process(processCategory: processCategory, category:pdef.category, pkey:pdef.key)
				process.save(failOnError: true)
  			}

			// add tasks
			def taskDefs = workflowService.getTaskDefs(process.pkey)
			def idx = 1
			taskDefs.each {
				if (!workflowService.getProcessStep(process.category, process.pkey, it.key)) {
					def processStep  = new ProcessStep(taskKey:it.key, idx: it.value?.taskFormHandler?.formKey?.isLong() ? it.value?.taskFormHandler?.formKey.toLong() : idx )
					process.addToProcessSteps(processStep)
					idx += 1
				}
			}
            if (!process.variables) {
     //           contentService.addCommonVariables(process)
            }

            flash.message ="Succesfully uploaded"
		}
		catch (Exception exc) {
			flash.message = exc.toString()
		}
		redirect(action: "list")
	}

	def delete = {

		try {
			repositoryService.deleteDeploymentCascade(params.did)

			flash.message ="Deployment succesfully deleted"
		}
		catch (Exception exc) {
			flash.message = exc.toString()
		}
		redirect(action: "list")
	}
}
