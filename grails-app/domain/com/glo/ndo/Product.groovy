package com.glo.ndo

class Product implements Comparable {

	static auditable = true
	
	static searchable = [only: [
		'code',
		'name',
		'category'
	]]

	String code
	String name
	String revision
	Boolean isBulk
	Boolean disabled
	String comment
	String category
	String uom
	Long minQty
	Long maxQty
	String usedFor
	
	static belongsTo = [
		startProcess: Process,
		productFamily: ProductFamily
	]
	static hasMany = [
		productCompanies: ProductCompany,
		productMasks: ProductMask,
		boms: Bom,
		bomParts: BomPart,
		productFiles: ProductFile
	]

	static constraints = {
		code blank:false,maxSize:100
		name blank:false,maxSize:100
		revision nullable: true
		comment nullable: true, maxSize:2000
		isBulk nullable:true
		disabled nullable: true
		startProcess nullable: true
		productFamily blank:false
		category nullable: true
		uom nullable: true
		minQty nullable:true
		maxQty nullable:true
		usedFor nullable:true
	}
	
	String toString() {
		name + " (" + code + ")"
	}

	public int compareTo(def other) {
		return this.toString()?.toUpperCase() <=> other?.toString()?.toUpperCase()
	}
}
