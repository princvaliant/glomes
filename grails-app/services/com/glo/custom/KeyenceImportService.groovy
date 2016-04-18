package com.glo.custom

import com.glo.ndo.Company
import com.glo.ndo.Process
import com.glo.ndo.Product
import com.glo.ndo.ProductMask
import com.mongodb.BasicDBObject
import org.apache.commons.logging.LogFactory
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics
import org.apache.commons.math3.stat.regression.SimpleRegression

import java.text.DateFormat
import java.text.SimpleDateFormat

class KeyenceImportService {

    private static final logr = LogFactory.getLog(this)

    def mongo

    DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    DateFormat df2 = new SimpleDateFormat("yyMMddHHmmss")

    def run() {

        def db = mongo.getDB("glo")

        !logr.isDebugEnabled() ?: logr.debug("keyence import started.")

        new File("/Users/aleksandarvolos/projects/meteor_packages/extjs61/resources/images").eachDirRecurse() { dir ->
            def files = dir.listFiles().toList()
            files.each { file ->
                println "api.addAssets('" + file.getPath().replace("/Users/aleksandarvolos/projects/meteor_packages/extjs61/", "") + "', 'client');"
            }
        }

        // Create list of unique files grouped by wafer id
        def unitFiles = [:]
        def dt = null

        units.keySet().each {

            def realCode = it.getName()


                def realDateTime
                if (units[it].waferCode) {
                    realCode = units[it].code
                    dt = new Date().format("yyMMdd")
                    realDateTime = Date.parse("yyMMdd", dt);
                } else {
                    dt = null
                }
                unitFiles.put(realCode, [:])

                def dirloc = it.listFiles([accept: { file -> file ==~ /.*?\.txt/ }] as FileFilter)?.toList()
                dirloc += it.listFiles([accept: { file -> file ==~ /.*?\.jpg/ }] as FileFilter)?.toList()

                if (dt) {
                    dirloc = dirloc.findAll { it.getName().indexOf(dt) > 0 }
                }

                def cntrl = ""
                def cntrlCurrentSweep = ""
                def cntrlVoltageSweep = ""
                def cntrlRppVoltageSweep = ""
                def cntrlRnnVoltageSweep = ""
                def cntrlRddVoltageSweep = ""
                def cntrlRitoVoltageSweep = ""

                def isAdded = [:]

                dirloc.reverse().each { file ->
                }

        }
    }
}
