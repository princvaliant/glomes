package com.glo

import org.apache.commons.logging.LogFactory

class ImportExperimentDataJob {

	def concurrent = false
	def grailsApplication
	def quartzScheduler
	def experimentDataService
	def mongo

	private static final logr = LogFactory.getLog(this)

    static triggers = {
        cron name: 'cronTriggerExperimentData', cronExpression: "0 0 3 ? * MON-SUN"
    }

	def execute() {
		if (grailsApplication.config.glo.tomcatServer == "calserver04") {
            try {
                experimentDataService.importFiles()
            } catch (Exception exc) {
                logr.warn (exc.message)
            }
		}
	}
}
