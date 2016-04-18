package com.glo.ndo

class ProcessCategory {

	static auditable = true

	String category
	String mongoCollection
		
	static hasMany = [
		processes: Process
	]

	static constraints = {
		
		category blank:false,maxSize:100
		mongoCollection blank:false, inList:["dataReport","measures","epiRun"]
		
	}

	String toString() {
		category 
	}
}
