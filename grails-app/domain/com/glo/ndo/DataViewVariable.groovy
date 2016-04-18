package com.glo.ndo

class DataViewVariable implements Comparable {

	Integer idx
	String title
	String path
	String filter
	Boolean isFormula
	String formula
	String link
	String fullPath
	
	static belongsTo = [
		dataView: DataView,
		variable: Variable
	]
	
	static hasMany = [
		dataViewCharts: DataViewChart
	]

	static constraints = {
		idx blank:false
		dataView nullable: false
		variable nullable: true
		path nullable: true
		filter nullable: true
		isFormula nullable:true
		formula nullable: true
		link nullable: true
		fullPath nullable : true
	}

	String toString() {
		variable?.name + ", " + dataView?.name
	}

	public int compareTo(def other) {
		return this.toString() <=> other?.toString()
	}
}


