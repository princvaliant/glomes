package com.glo.custom

import com.glo.ndo.Spc
import com.glo.ndo.SpcVariable
import org.apache.commons.logging.LogFactory

class SpcService {

	private static final logr = LogFactory.getLog(this)
	def managementService
	def runtimeService
	def repositoryService
	def taskService
	def messageSource
	def grailsApplication
	def mongo
	def persistenceInterceptor

	static transactional = false

	
	def list (def user, def parms) {

		def srch = parms.search?.trim() ?: (parms.query?.trim() ?: "")
		def filt = ""

		srch.tokenize(" ").each { filt += " and (spc.owner like '%$it%' or spc.name like '%$it%' or spc.tag like '%$it%')" }
		
		def spcs = Spc.executeQuery( """
			select spc from Spc as spc where (spc.owner = ? or spc.isPublic = true) $filt
		""" + (parms.sort ? " order by spc." + parms.sort + " " + parms.dir : "")
		, [user], [max:parms.max,offset:parms.offset] )
		
		def total = Spc.executeQuery( """
			select count(*) from Spc as spc where (spc.owner = ? or spc.isPublic = true)  $filt
		"""
		, [user])[0]
		
		[spcs,total]
	}
	
	
	def save (String user, def spc) {
		
		def dv		
		if (spc.id == "0") {
			if (Spc.findByName(spc.name)) {
				throw new RuntimeException ("Name '" + spc.name + "' already exist" )
			}
			dv = new Spc()
		} else {
			dv = Spc.get(spc.id) 
			if (user != "admin" && dv.owner != user) {
				throw new RuntimeException ("Only owner can change report" )
			}
			if (!dv) {
				throw new RuntimeException ("Spc with id '" + spc.id + "' does not exist" )
			}
		}
		
		if (spc.isPublic == null)
			dv.isPublic = false
		if (spc.showText == null)
			dv.showText = false
		dv.properties = spc
		dv.owner = user
		dv.save(failOnError: true)

	}
	
	def duplicate (String user, def spcId) {
		
		def spc = Spc.get(spcId)
		if (!spc) {
			throw new RuntimeException ("Spc with id '" + spc.id + "' does not exist" )
		}
		
		Spc newSpc = new Spc()
		newSpc.isPublic = 0
		newSpc.showText = spc.showText

        def getName = { val ->

            def append = 1
            def appendStr = ""
            while (Spc.findByName(val + " COPY" + appendStr)) {
                append += 1
                appendStr = " " + append.toString()
            }
            val + " COPY" + appendStr
        }

  		newSpc.name = getName(spc.name)
		newSpc.owner = user
		newSpc.tag = spc.tag
		newSpc.filter = spc.filter
		newSpc.save(failOnError: true)
		
		spc.spcVariables.each {
			newSpc.addToSpcVariables(
				new SpcVariable(
					idx:it.idx, title:it.title,path:it.path, variable:it.variable
				)
			)
		}
	}
	
	def recalculateCharts() {
		
		def persistenceInterceptor

		try {
			
			def u = grailsApplication.config.grails.serverURL + "/spc/chart"			
			def spcs = Spc.list()
			
			spcs.each { spc ->
				
				try {
					def url = new URL(u + "?spcId=" + spc.id.toString() + "&refresh=true&width=1100" )
					def connection = url.openConnection()
					connection.setRequestMethod("POST")
					
					if(connection.responseCode == 200 || connection.responseCode == 201){
						
					}
				}
				catch (Exception exc) {
					logr.error("ERROR SPC " + spc.id + ": " + exc.getMessage())
				}
			}

			!logr.isDebugEnabled() ?: logr.debug("Calculate spcs successfuly executed.")
		}
		catch (Exception exc) {
			logr.error(exc.getMessage())
		}

		if (persistenceInterceptor) {
			persistenceInterceptor.flush()
			persistenceInterceptor.destroy()
		}
	}
		
}
