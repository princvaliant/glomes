package com.glo.run

class File   {
	
	static mapWith = "mongo"
	
	String fileId
	String name
	String fileName
	String userName
	Date dateCreated
	
	static belongsTo = [
		unit: Unit	
	]
	
	static constraints = {
		fileName blank:false, maxSize:500
		fileId nullable:false
		name nullable: true, maxSize:1000
		userName nullable: true
	}


}
