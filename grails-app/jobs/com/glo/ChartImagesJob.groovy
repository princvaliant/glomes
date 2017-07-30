package com.glo

import org.apache.commons.logging.LogFactory

class ChartImagesJob {

	def concurrent = false
	def grailsApplication
    def quartzScheduler
	def reportPdfProcessorService
	
	private static final logr = LogFactory.getLog(this)


	static triggers = {
       cron name: 'cronTriggerChartImages', cronExpression: "0 0 2 ? * MON-SUN"
	}

	def execute() {

		if (grailsApplication.config.glo.tomcatServer == "calserver20") {
            try {
       //         reportPdfProcessorService.processAll()
            } catch (Exception exc) {
                logr.warn (exc.message)
            }
		}
	}
}
