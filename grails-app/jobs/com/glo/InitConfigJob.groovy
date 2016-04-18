package com.glo

import java.io.File;


import org.codehaus.groovy.grails.commons.*
import org.codehaus.groovy.grails.commons.cfg.*
import org.apache.log4j.LogManager
import org.apache.log4j.PropertyConfigurator
import org.apache.commons.logging.LogFactory

import com.mongodb.BasicDBObject


class InitConfigJob {
	

	def grailsApplication
	def quartzScheduler
	def mongo
	private static final logr = LogFactory.getLog(this)

	
	static triggers = {
		simple name: 'configJobTrigger', group: 'configJobTriggerGroup', repeatInterval: 60000, startDelay: 15000
    }
	
    def execute() {
		
		File configFile = new File("/usr/local/jd/conf/glo-config.properties");
		if (configFile.exists()) {
			
			// Reset log manager
			LogManager.resetConfiguration()
			PropertyConfigurator.configure(configFile.toURI().toURL())
			ConfigurationHelper.initConfig(grailsApplication.config)
		
			// Reschedule math simulator from config file
			def trigger = quartzScheduler.getTrigger("importEquipmentDCJobTrigger", "importEquipmentDCJobTriggerGroup")
			String st = grailsApplication.config.glo.math.timeout ?: "7200"
			Long s = st.isLong() ? st.toLong() : 57200
			Integer i = -1
			if (s < 1 || s > 57200) s = 57200
			trigger.repeatInterval = s*1000    // in millisecods, a long value
			Date nextFireTime = quartzScheduler.rescheduleJob(trigger.name, trigger.group, trigger)
//			
//			// Reschedule data sync from config file
//			def trigger1 = quartzScheduler.getTrigger("dataSyncTrigger", "dataSyncTriggerGroup")
//			String st1 = org.codehaus.groovy.grails.commons.ConfigurationHolder.config.glo.dataSync.timeout ?: "60"
//			Long s1 = st1.isLong() ? st1.toLong() : 60
//			Integer i1 = -1
//			if (s1 < 1 || s1 > 600) s1 = 600
//			trigger1.repeatInterval = s1 * 1000    // in millisecods, a long value
//			Date nextFireTime1 = quartzScheduler.rescheduleJob(trigger1.name, trigger1.group, trigger1)
//
//			// Reschedule dataa simulator from config file
//			def trigger2 = quartzScheduler.getTrigger("dataReportTrigger", "dataReportTriggerGroup")
//			String st2 = org.codehaus.groovy.grails.commons.ConfigurationHolder.config.glo.dataReport.mapReduce ?: "500"
//			Long s2 = st2.isLong() ? st2.toLong() : 1000
//			Integer i2 = -1
//			trigger2.repeatInterval = s2*1000    // in millisecods, a long value
//			Date nextFireTime2 = quartzScheduler.rescheduleJob(trigger2.name, trigger2.group, trigger2)
			
			!logr.isDebugEnabled() ?: logr.debug("Config reader successfuly executed.")
		}
		
		if (grailsApplication.config.glo.tomcatServer == "calserver03") {
			this.syncRunType()
		}
		
	}
	
	def syncRunType() {
		
		try {
			def db = mongo.getDB("glo")
			def runs = db.epiRun.find(new BasicDBObject(),[runNumber:1,runtype:1,dateCreated:1]).addSpecial('$orderby', [dateCreated:-1]).limit(100).collect{[it.runNumber, it.runtype]}
			runs.each {
				def query = new BasicDBObject()
				query.put('parentCode', null)
				query.put('pkey', 'epi')
				query.put('runNumber', it[0])
				query.put('$or', [
					new BasicDBObject("runType", null),
					new BasicDBObject("runType", '')
				])
				db.unit.update(query, ['$set':[runType:it[1]]], false, true)
			}
			
		} catch (Exception exc) {
			logr.error(exc.getMessage())
		}

	}
}
