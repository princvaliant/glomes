package com.glo.custom

class DieSpec   {
	
	String 		name
	Integer 	revision
	String 		username
	Date 		dateCreated

	static belongsTo = [
		product: com.glo.ndo.Product
	]
	
	static hasMany = [
		waferFilters: WaferFilter
	]
	
	static constraints = {
		name blank:false
		revision blank: false
		username blank: false
		product nullable:true
	}

	String toString() {
		name + " (Rev. " + revision + ")"
	}
}
