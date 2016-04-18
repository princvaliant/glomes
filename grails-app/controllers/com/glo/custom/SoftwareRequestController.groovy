package com.glo.custom

import com.glo.ndo.Company
import com.glo.ndo.Product
import com.mongodb.BasicDBObject
import org.apache.commons.logging.LogFactory


class SoftwareRequestController {

	private static final logr = LogFactory.getLog(this)
	def fileService
    def unitService
	def mongo 
	def grailsApplication
    def springSecurityService


	def create = {

        def recv = new Expando()
        recv.uid = springSecurityService.principal?.username
        recv.cid = Company.findByName("Unknown").id
        recv.pid = Product.findByCode("SOFTREQ").id
        recv.note = params.comment
        recv.units = []

        def m = [:]
        m.put('owner', springSecurityService.principal?.username)
        m.put('qty', 1)
        m.put('title', params.comment.length() > 50 ? params.comment.substring(0, 50) + " . . ." : params.comment)
        m.put('type', params.requestType)
        m.put('prior', params.prior.toInteger())

        recv.units.add(m)

        def code = ""
        try {
            code = unitService.start(recv, "Software", "requests")
        } catch (RuntimeException exc) {
            flash.message = "ERROR: " + exc.getMessage()
            return
        }

        def db = mongo.getDB("glo")
        def unit = db.unit.find(new BasicDBObject("code", code), [:]).collect { it }[0]

        for (int i = 1; i <= 3; i++) {
            def f = request.getFile("file" + i)
            if (f != null && !f.empty) {
                def fileId = fileService.saveFile(f, code + i)
                def fileName =  f.getOriginalFilename()
                unitService.saveFileMeta (springSecurityService.principal?.username, [unit._id], fileId, code + i, fileName)
            }
        }

        flash.message ="Successfully posted software request number " +  code + " . Please check 'Software' tab in MES userConsole for the status of the request"
	}
}
