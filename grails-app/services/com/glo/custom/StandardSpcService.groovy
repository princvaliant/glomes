package com.glo.custom

import com.glo.ndo.Company
import com.glo.ndo.Product
import com.mongodb.BasicDBObject
import grails.util.Environment
import org.apache.commons.logging.LogFactory
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics

import javax.jms.Message
import java.text.DateFormat
import java.text.SimpleDateFormat

class StandardSpcService {

	private static final logr = LogFactory.getLog(this)

	def messageSource
	def unitService
	def jmsService
	def mongo
	
	static exposes = ['jms']
	
	static String TKEY = "test_rel"
	static String CURRENT = "5000"
	static String TESTTYPE = "STD"
	
	def init(def codesForSpc) {

		def message = [codesForSpc:codesForSpc]

		if (Environment.currentEnvironment == Environment.DEVELOPMENT ) {
			onMessage(message)
		} else {
			jmsService.send(service: 'standardSpc', 1) {Message msg ->
					msg.object = message
					msg
			}
		}
	}

	def onMessage(def args) {

		def db = mongo.getDB("glo")
		DateFormat  df = new SimpleDateFormat("yyyyMMdd")
		
		def codesForSpc = args.get("codesForSpc")
		
		def recv = new Expando()
		recv.uid = "admin"
		recv.cid = Company.findByName("Unknown").id
		recv.pid = Product.findByCode("stdRelData").id
		recv.units = []
		
		def currentsForCalc = ["100", "500","1000","5000","10000","20000"]
		
		// Calculate XBAR and R
		codesForSpc.each { relGroup, measures -> 
	
			def query =	new BasicDBObject("value.tkey", TKEY)
			query.put("value.relGroup", relGroup)
			query.put("value.testType", TESTTYPE)
			def group = relGroup.tokenize("_")[0]
			
			measures.each {  measure, testDate ->
				
				def code = "STDREL" + measure.replace("_","")
				def unit = db.unit.find(new BasicDBObject("code", code), new BasicDBObject()).collect{it}
				if (!unit) {
					def m = [:]
					m.put('code', code)
					m.put('qty', 1)
					m.put('relGroup', relGroup)
				
					recv.units.clear()
				
					currentsForCalc.each {  current ->
						
						Boolean hasData = false
						
						def fields = new BasicDBObject("value.data." + measure + ".setCurrent." + current, 1)
						fields.put("value.code", 1)
						
						def DescriptiveStatistics lops = new DescriptiveStatistics()
						def DescriptiveStatistics voltages = new DescriptiveStatistics()
						def DescriptiveStatistics peaks = new DescriptiveStatistics()
						def DescriptiveStatistics dominants = new DescriptiveStatistics()
						def DescriptiveStatistics photometrics = new DescriptiveStatistics()
						def DescriptiveStatistics fwhms = new DescriptiveStatistics()
					
						def testData = db.testData.find(query, fields).collect{
							
							if (it.value.data[measure]["setCurrent"][current]) {
								hasData = true
								lops.addValue(it.value.data[measure]["setCurrent"][current].lop)
								voltages.addValue(it.value.data[measure]["setCurrent"][current].voltage)
								peaks.addValue(it.value.data[measure]["setCurrent"][current].peak)
								dominants.addValue(it.value.data[measure]["setCurrent"][current].dominant)		
								photometrics.addValue(it.value.data[measure]["setCurrent"][current].photometric)
								fwhms.addValue(it.value.data[measure]["setCurrent"][current].fwhm)
							}
						}
						
						if (hasData) {		
											
							def num = (current.toInteger()/1000) 
							def cnum = ""
							if (num < 1) {
								cnum = current + "uA"
							} else {
								cnum = num.toString() + "mA"
							}
						
							m.put('lop xbar '+ cnum, lops.getMean())
							m.put('voltage xbar '+ cnum, voltages.getMean())
							m.put('peak xbar '+ cnum, peaks.getMean())
							m.put('dominant xbar '+ cnum, dominants.getMean())
							m.put('photometric xbar '+ cnum , photometrics.getMean())
							m.put('fwhm xbar '+ cnum , fwhms.getMean())
							m.put('lop r '+ cnum, lops.getMax() - lops.getMin())
							m.put('voltage r '+ cnum, voltages.getMax() - voltages.getMin())
							m.put('peak r '+ cnum , peaks.getMax() - peaks.getMin())
							m.put('dominant r '+ cnum , dominants.getMax() - dominants.getMin())
							m.put('photometric r '+ cnum, photometrics.getMax() - photometrics.getMin())
							m.put('fwhm r '+ cnum, fwhms.getMax() - fwhms.getMin())
						}						
					}
					
					recv.actualStart = df.parse("20" + testDate.trim())
					recv.units.add(m)
					
					try {
						unitService.start (recv, "Rel", "stdrel_" + group )
					} catch (RuntimeException exc) {
						logr.warn( measure + " had errors: " + exc.getMessage())
					}
				}		
			}
		}
	}
}
