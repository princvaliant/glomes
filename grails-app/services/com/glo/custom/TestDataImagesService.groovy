package com.glo.custom

import com.mongodb.BasicDBObject
import grails.converters.JSON
import org.apache.commons.io.IOUtils
import org.apache.commons.logging.LogFactory
import org.apache.poi.xwpf.usermodel.*

import java.text.DateFormat
import java.text.SimpleDateFormat

class TestDataImagesService {

    private static final logr = LogFactory.getLog(this)

    def mongo
    def unitService
    def workflowService
    def grailsApplication

    public def processAll() {

        // Retrieve all unique tests from active list of test data image processing
        def testsToProcess = WaferSummaryDoc.executeQuery("""
				select wsd.testType, count(*) from WaferSummaryDoc as wsd
				where wsd.isActive = true group by wsd.testType order by wsd.testType
				""").collect { it[0] }
        if (testsToProcess.size() == 0) {
            throw new IllegalArgumentException("Test data images not defined")
        }

        // Get all tests that are not image processed with the latest testId
        def db = mongo.getDB("glo")
        def query = new BasicDBObject()
        query.put("value.parentCode", null)
        query.put("value.tkey", ['$in': testsToProcess])
        query.put("value.images", null)
        def df = new Date() - 90
        query.put("value.date", ['$gte': df])
        def unitsToProcess = db.testData.aggregate(
                ['$match': query],
                ['$project': [
                        '_id': 1, 'value.code': 1, 'value.tkey': 1, 'value.testId': 1
                ]],
                ['$group':
                         [_id: [code: '$value.code', tkey: '$value.tkey'], testId: ['$max': '$value.testId'], id: ['$max': '$_id']]
                ],
                ['$sort':
                         ['testId': -1]
                ]
        ).results().collect { [_id: it.id, code: it._id.code, tkey: it._id.tkey, testId: it.testId] }

        // Loop through all the tests and export images
        unitsToProcess.each {
            processEachUnitTestData(db, it)
        }
    }


    private def processEachUnitTestData(def db, def unitToProcess) {

        def unit = db.dataReport.find(
                [code: unitToProcess.code],
                ['code': 1, 'value.updated': 1, 'value.experimentId': 1, 'value.runNumber': 1, 'value.runNumberFab': 1]).collect {
            it
        }[0]

        if (unit) {
            def waferSummaryDocs = WaferSummaryDoc.findAllByTestTypeAndIsActive(unitToProcess.tkey, true)
            waferSummaryDocs.each { wsd ->
                def found = true
                def guid = ""
                def tdi = db.testDataImages.find([code: unit.code, wsd: wsd.id]).collect { it }[0]
                if (!tdi) {
                    found = false
                    guid = UUID.randomUUID().toString().replace("-", "").substring(0, 18)
                    tdi = [_id: guid, code: unit.code, wsd: wsd.id]
                } else {
                    guid = tdi._id
                }

                tdi.put('tkey', unitToProcess.tkey)
                tdi.put('testId', unitToProcess.testId)
                tdi.put('experimentId', unit.value?.experimentId)
                tdi.put('runNumber', unit.value?.runNumber)
                tdi.put('runNumberFab', unit.value?.runNumberFab)
                tdi.put('item', wsd.parameter)
                tdi.put('current', wsd.current)

                def parms = ""
                parms += "?code=" + unit.code
                parms += "&tkey=" + unitToProcess.tkey
                parms += "&testId=" + unitToProcess.testId.toString()
                parms += "&level1=" + wsd.current
                parms += "&level2=" + wsd.parameter
                parms += "&image=" + guid + ".jpg"
                def url = grailsApplication.config.webdomain ?: "http://localhost:8080/glo"

                try {
                    createUrlImage(url + "/testDataImages/index" + parms)
                    if (found == true) {
                        db.testDataImages.update([_id: guid], [$set: tdi], false, true)
                    } else {
                        db.testDataImages.insert(tdi)
                    }
                } catch (Exception exc) {
                    logr.warn(exc.message)
                }
            }
            db.testData.update([_id: unitToProcess._id], ['$set': ['value.images': 1]], false, true)
        }
    }

    private boolean createUrlImage(def url) {
        url = url.replace(" ", "%20")
        String phantomjsHome = grailsApplication.config.glo.phantomjsDirectory
        String phantomjsExe = grailsApplication.config.glo.phantomjsExe ?: "phantomjs"
        String phantomjsRasterizeScript = phantomjsHome + "page.js"
        ProcessBuilder pb = new ProcessBuilder(phantomjsHome + phantomjsExe, phantomjsRasterizeScript, url)
        ProcessBuilder.Redirect error = pb.redirectError()
        ProcessBuilder.Redirect out = pb.redirectOutput()
        ProcessBuilder.Redirect inn = pb.redirectInput()
        pb.start().waitFor()
    }
}
