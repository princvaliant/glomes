package com.glo.run

class Note   {
	
	static mapWith = "mongo"
	
	String userName
	String comment
	String stepName
	Date dateCreated
	
	static belongsTo = [
		unit: Unit	
	]
	
	static constraints = {
		userName blank:false, maxSize:100
		comment nullable: true, maxSize:3000
		stepName nullable: true
	}


}
