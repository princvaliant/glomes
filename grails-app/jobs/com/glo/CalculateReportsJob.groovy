package com.glo

import java.io.File;


import org.codehaus.groovy.grails.commons.*
import org.codehaus.groovy.grails.commons.cfg.*
import org.apache.log4j.LogManager
import org.apache.log4j.PropertyConfigurator
import org.apache.commons.logging.LogFactory
import com.glo.ndo.*



class CalculateReportsJob {

	def concurrent = false
	def grailsApplication
	def quartzScheduler
	def dataViewService
	def spcService
	
	private static final logr = LogFactory.getLog(this)


	static triggers = {
	//simple name: 'calculateReportsJobTrigger', group: 'calculateReportsJobTriggerGroup', repeatInterval: 60000, startDelay: 20000
		cron name: 'cronTrigger', cronExpression: "0 0 4 ? * MON-FRI"
	}

	def execute() {

		if (grailsApplication.config.glo.tomcatServer == "calserver04") {
			dataViewService.recalculateCharts()
			spcService.recalculateCharts()
		}
	}
}
