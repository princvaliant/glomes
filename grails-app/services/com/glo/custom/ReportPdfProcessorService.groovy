package com.glo.custom

import com.glo.ndo.DataView
import com.glo.ndo.Spc
import com.mongodb.BasicDBObject
import org.apache.commons.logging.LogFactory

import java.text.DateFormat
import java.text.SimpleDateFormat

class ReportPdfProcessorService {

    private static final logr = LogFactory.getLog(this)

    def mongo
    def unitService
    def workflowService
    def grailsApplication

    public def processAll() {

        def url = grailsApplication.config.webdomain ?: "http://localhost:8080/glo"
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd-hh-mm")
        def date = df.format(new Date());

        def dataViews = DataView.executeQuery("""
			select dv.id as id, 'DIAGRAM' as type from DataView as dv where dv.tag like '%PDFPRINT%'
		""")

        def spcs = Spc.executeQuery( """
			select spc.id as id, 'SPC' as type from Spc as spc where spc.tag like '%PDFPRINT%' """)

        (dataViews + spcs).each { it ->
            try {
                createUrlPdf(url + "/reportPdfProcessor/rend?id=" + it[0] + "&type=" + it[1] + "&date=" + date)
            } catch (Exception exc) {
                logr.warn(exc.message)
            }
        }

    }
    private boolean createUrlPdf(def url) {
        url = url.replace(" ", "%20")
        String phantomjsHome = grailsApplication.config.glo.phantomjsDirectory
        String phantomjsExe = grailsApplication.config.glo.phantomjsExe ?: "phantomjs"
        String phantomjsRasterizeScript = phantomjsHome + "pdf.js"
        ProcessBuilder pb = new ProcessBuilder(phantomjsHome + phantomjsExe, phantomjsRasterizeScript, url)
        ProcessBuilder.Redirect error = pb.redirectError()
        ProcessBuilder.Redirect out = pb.redirectOutput()
        ProcessBuilder.Redirect inn = pb.redirectInput()
        pb.start().waitFor()
    }
}
