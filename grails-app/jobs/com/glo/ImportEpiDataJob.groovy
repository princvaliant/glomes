package com.glo

import java.io.File;


import org.codehaus.groovy.grails.commons.*
import org.codehaus.groovy.grails.commons.cfg.*
import org.apache.log4j.LogManager
import org.apache.log4j.PropertyConfigurator
import org.apache.commons.logging.LogFactory
import com.glo.ndo.*


class ImportEpiDataJob {

	def concurrent = false
	def grailsApplication
	def quartzScheduler
	def epiRunService
	def mongo

	private static final logr = LogFactory.getLog(this)

	static triggers = {
		simple name: 'ImportEpiDataJobTrigger', group: 'ImportEpiDataJobTriggerGroup', repeatInterval: 3600000, startDelay: 120000
	
	}

	def execute() {

		if (grailsApplication.config.glo.tomcatServer == "calserver04") {
			
			def u = grailsApplication.config.grails.serverURL + "/analyser/epi/"
			def url = new URL(u)
			def connection = url.openConnection()
			connection.setRequestMethod("GET")
			if(connection.responseCode == 200 || connection.responseCode == 201){
				
			}
			
			
			u = grailsApplication.config.grails.serverURL + "/epiParser/"
			url = new URL(u)
			connection = url.openConnection()
			connection.setRequestMethod("GET")
			if(connection.responseCode == 200 || connection.responseCode == 201){
				
			}
 		}

	}
	
}
