package com.glo.custom

class EpiRun   {
	
	static mapWith = "mongo"
	
	String 		runNumber
	String 		code
	String 		equipmentName
	String 		workcenter
	Date 		dateCreated
	Date 		lastUpdated 

	static constraints = {
		runNumber blank:false, unique:true
		workcenter nullable:true
	}
}
