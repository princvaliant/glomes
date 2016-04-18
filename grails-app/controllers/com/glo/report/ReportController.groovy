package com.glo.report

import com.mongodb.BasicDBObject
import grails.converters.JSON
import org.apache.commons.logging.LogFactory

class ReportController  {

	private static final logr = LogFactory.getLog(this)

	def mongo

	def index = { render """<h2>Bas
ic reports</h2> <a href='/glo/report/avgDurationPerCompany'>Avg duration in second per company</a><br/>
					<a href='/glo/report/totalLossPerCompany'>Total loss per company</a><br/>
					<a href='/glo/report/totalLossPerStep'>Total loss per step</a><br/>
					<a href='/glo/report/totalLossPerOperation'>Total loss per operation</a><br/>
					<a href='/glo/report/totalLossPerEquipment'>Total loss per equipment</a><br/>
					<a href='/glo/report/unitStatus?code=${params.code}'>Unit status</a><br/>
	
					""" }

	def avgDurationPerCompany = {

		def db = mongo.getDB("glo")

		def result = db.history.mapReduce(
				"""
				function map() {
				
					this.audit.forEach( function(x) { emit( x.company, {total: x.duration});} );
				}
			""",
				"""
				function reduce(key, values) {
					var count = 0;
					var total = 0;
					for (var i = 0; i < values.length; i++) {
						total += values[i].total;
						if (values[i].total != undefined)
							count += 1
					}
					if (count > 1) 
						return {avg:total/count};
					else
						return {avg:0}
				}
			""",
				"mrresult",
				[:]
				)

		render (db.mrresult.find().collect{it}.toString())
	}

	def totalLossPerCompany = { totalLoss ('Vendor', 'x.company') }

	def totalLossPerStep = { totalLoss ('Step', 'x.tname') }

	def totalLossPerEquipment = { totalLoss ('Equipment', 'x.equipment') }

	def totalLossPerOperation = { totalLoss ('Operation', 'x.operation') }
	
	def envVar = {
		
		render (System.getenv('VCAP_SERVICES'))
	}
	
	
	def unitStatus = { 
		def db = mongo.getDB("glo")
		def g = new BasicDBObject()
		g.put('code', params.code)
		def temp = db.history.find(g)
		render (temp.collect{it} as JSON)
	}

	private def totalLoss (def name, def grp) {
		logr.debug(params)

		def db = mongo.getDB("glo")

		def result = db.history.mapReduce(
				"""
				function map() {
				
					this.audit.forEach( 
						function(x) { 
							if (x.loss != undefined) {
								x.loss.forEach(
									function(y) {  
										emit( {'${name}': ${grp}, 'yieldLoss': y.yieldLoss}, {total: 1});
									}
								);
							}
							
						} 
					);
				}
			""",
				"""
				function reduce(key, values) {
					var total = 0;
					for (var i = 0; i < values.length; i++) {
						total += values[i].total;
					}
					return {total:total};
				}
			""",
				"mrresult",
				['audit.loss': new BasicDBObject('$exists', true)]
				)

		render (db.mrresult.find().collect{it} as JSON)
	}
}
