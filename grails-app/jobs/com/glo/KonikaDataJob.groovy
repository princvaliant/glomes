package com.glo

import org.apache.commons.logging.LogFactory

class KonikaDataJob {

    def concurrent = false
    def grailsApplication
    def quartzScheduler
    def dataImportService

    private static final logr = LogFactory.getLog(this)


    static triggers = {
        cron name: 'cronTriggerKonikaData', cronExpression: "0 0 0-23 ? * MON-SUN"
    }

    def execute() {

        if (grailsApplication.config.glo.tomcatServer == "calserver20") {
            try {
           //     dataImportService.importKonikaFromMeasures()
            } catch (Exception exc) {
                logr.warn (exc.message)
            }
        }
    }
}
