package com.glo.run

class Unit  {

	static mapWith = "mongo"

	// Mandatory fields //////////////////////////////////////////////////////////////////

	String 		code
	String 		parentCode
	String      genPath
	Integer		qtyIn
	Integer		qtyOut

	String		product
	String		productCode
	String		productRevision
	String		productFamily
	String		lotNumber
	String 		isBulk

	// Workflow status
	String pctg
	String pkey
	String tkey
	String tname
	Date start
	Date actualStart
	Integer prior
	List<Object> distribution


	// Currently assigned users and group
	List<String> 		usrs = []
	List<String> 		grps = []

	Date 		dateCreated // autoset by plugin
	Date 		lastUpdated // autoset by plugin

	// Contain array of strings that can be found using search //////////////////////////
	List<String> 		tags = []
	List<String> 		tagsCustom = []
	
	// Parent child relationships
	List<String> parentUnit = []
	List<String> childrenUnits = []

	static hasMany = [
		notes: Note,
		files: File
	]
	Integer nNotes
	Integer nFiles
	Integer processViolations

	def beforeInsert() {
		indexing ()
	}

	def beforeUpdate() {
		indexing()
	}

	def indexing() {
		tags.clear()
		if (code) tags.addAll(code.toUpperCase().split())
		if (productCode) tags.addAll(productCode.toUpperCase().split())
		if (lotNumber) tags.addAll(lotNumber.toUpperCase().split())
	}

	static constraints = {

		code blank:false
		parentCode nullable:true
		genPath blank:false

		qtyIn blank:false
		qtyOut blank:false
		product nullable: true
		productCode nullable: true
		productRevision nullable: true
		productFamily nullable: true
		isBulk nullable:true
		lotNumber nullable: true

		pctg nullable: true
		pkey nullable: true
		tkey nullable: true
		tname nullable: true
		start nullable: true
		distribution nullable: true
		actualStart nullable: true
		prior nullable:true

		usrs nullable:true
		grps nullable:true
		
		parentUnit nullable:true
		childrenUnits nullable:true

		nNotes nullable: true
		nFiles nullable:true
		processViolations nullable:true
	}

	static mapping = {
		tags index:true
		tagsCustom index:true
		parentCode index:true
		code index:true, indexAttributes: [unique:true, dropDups:true]
		pctg index:true
		pkey index:true
		genPath index: true
	}

	String toString() {
		code
	}
}
