package com.glo

import java.io.File;


import org.codehaus.groovy.grails.commons.*
import org.codehaus.groovy.grails.commons.cfg.*
import org.apache.log4j.LogManager
import org.apache.log4j.PropertyConfigurator
import org.apache.commons.logging.LogFactory
import com.glo.ndo.*



class TestDataToMeasuresJob {

	def concurrent = false
	def grailsApplication
    def quartzScheduler
	def dataImportService
	
	private static final logr = LogFactory.getLog(this)


	static triggers = {
       simple name: 'testdataTomeasuresJob', group: 'testdataTomeasuresJobTriggerGroup', repeatInterval: 3600000, startDelay: 120000
	}

	def execute() {
		if (grailsApplication.config.glo.tomcatServer == "calserver20") {
			dataImportService.testDataToMeasures("", "")
		}
	}
}
