package com.glo.ndo

class Bom implements Comparable {

	static auditable = true
	
	static searchable = {
		assemblyProduct component: true
	}
	
	String revision
	String note
    Boolean toValidate
	
	static belongsTo = [
		assemblyProduct: Product
	]
	
	static hasMany = [
		bomParts: BomPart
	]
	
	static constraints = {
		assemblyProduct nullable: false, unique: true
		note nullable:true
		revision nullable:false
        toValidate nullable: true
	}

	String toString() {
		assemblyProduct.name + "(" + assemblyProduct.code + ")"
	}

	public int compareTo(def other) {
		return this.toString()?.toUpperCase() <=> other?.toString()?.toUpperCase()
	}
}
