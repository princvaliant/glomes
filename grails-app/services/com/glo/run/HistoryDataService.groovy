package com.glo.run

import com.mongodb.BasicDBObject
import grails.util.Environment
import org.apache.commons.logging.LogFactory

import javax.jms.Message

class HistoryDataService {

	private static final logr = LogFactory.getLog(this)
	def messageSource
	def mongo
	def jmsService

	static exposes = ['jms']

	def init(def history, def unit) {

		def message = [obj:history,unit:unit]

		if (Environment.currentEnvironment != Environment.DEVELOPMENT ) {
			jmsService.send(service: 'historyData', 1) {Message msg ->
				msg.object = message
				msg
			}
		} else {
			onMessage(message)
		}
	}

	def onMessage(def args) {

		def history = args.get("obj")	
		def unit = args.get("unit")

		try {

			def db = mongo.getDB("glo")

			def dr = db.dataReport.find(new BasicDBObject("code", history["code"]))?.collect{it}[0]
			if (!dr) {
				dr = new BasicDBObject()
				dr.put("code", history["code"])
			}
			
			def insertUnit = [:]
			unit.each { k, v ->
				
				if (!(v.getClass() == com.mongodb.BasicDBList ||
					v.getClass() == com.mongodb.DBObject ||
					v.getClass() == com.mongodb.BasicDBObject ||
					k == "_id" || k == "id" || k == "pctg" ||
					k == "pkey" || k == "tkey" || k == "tname" || k == "actualStart" || k == "start" || k == "version"
					|| k == "dateCreated" || k == "lastUpdated" || k == "genPath" || k == "movedBy" || k == "user" || k == "owner"))
				insertUnit.put(k,  v)
				
			}
			dr.put("unit",insertUnit)

			dr.put("id", history["id"])
			dr.put("parentCode", history["parentCode"])
			dr.put("value", history)
			dr["value"].remove("id")
			dr["value"].remove("_id")
			dr["value"].remove("note")
			dr["value"].remove("tags")
			dr["value"].remove("tagsCustom")
			dr["value"].remove("files")
			dr["value"].remove("dataLog")
			dr["value"].remove("code")

			def active = ""
			def pkey = ""
			def tkey = ""
			def idx = 0
			def owner = ""
			def movedBy = ""
			
			def tags = new TreeSet()

			history["audit"].each {

				if (it["last"] == true) {
					
					active = ""
					if (it["end"] == null) {
						active = "true"
						pkey = it["pkey"]
						tkey = it["tkey"]
						owner = it["owner"]
						movedBy = it["movedBy"]
						idx = it["stepIdx"] ?: 0
					}

					dr["value"].put(it["tkey"], it + it["dc"] + it["din"])
					dr["value"][it["tkey"]].put("active", active)
					dr["value"][it["tkey"]].remove("last")
					dr["value"][it["tkey"]].remove("pctg")
					dr["value"][it["tkey"]].remove("tkey")
					dr["value"][it["tkey"]].remove("tname")
					dr["value"][it["tkey"]].remove("prior")
					dr["value"][it["tkey"]].remove("usrs")
					dr["value"][it["tkey"]].remove("grps")
					dr["value"][it["tkey"]].remove("din")
					dr["value"][it["tkey"]].remove("spec")
					dr["value"][it["tkey"]].remove("recp")
					dr["value"][it["tkey"]].remove("dc")
					
					tags.add( it["pctg"])
					tags.add( it["pctg"] + "|" + it["pkey"])
					tags.add( it["pctg"] + "|" + it["pkey"] + "|" + it["tkey"])
				}
			}

			dr["value"].put("tags", tags)
			dr["value"].put("active", active)
			if (active == "true") {
				dr["value"].put("pkey", pkey)
				dr["value"].put("tkey", tkey)
				dr["value"].put("idx", idx)
				dr["value"].put("owner", owner)
				dr["value"].put("movedBy", movedBy)
			}
			dr["value"].remove("audit")

			db.dataReport.save(dr)

			dr = null
		} catch(Exception exc) {
			logr.error(history?.code + ' ' + exc)
		}
		null
	}
}
