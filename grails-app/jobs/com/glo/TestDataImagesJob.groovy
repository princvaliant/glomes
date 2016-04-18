package com.glo

import org.apache.commons.logging.LogFactory

class TestDataImagesJob {

	def concurrent = false
	def grailsApplication
    def quartzScheduler
	def testDataImagesService
	
	private static final logr = LogFactory.getLog(this)


	static triggers = {
       cron name: 'cronTriggerTestDataImages', cronExpression: "0 0 0-23 ? * MON-SUN"
	}

	def execute() {

		if (grailsApplication.config.glo.tomcatServer == "calserver04") {
            try {
                testDataImagesService.processAll()
            } catch (Exception exc) {
                logr.warn (exc.message)
            }
		}
	}
}
