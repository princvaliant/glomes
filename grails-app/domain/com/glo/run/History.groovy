package com.glo.run

import org.bson.types.ObjectId

class History  {

	static mapWith = "mongo"

	// Mandatory fields //////////////////////////////////////////////////////////////////

	ObjectId 	id
	String 		code
	String		product
	String		productCode
	String		productRevision
	String		lotNumber
	Integer		processViolations


	static constraints = {

		
		code blank:false
		product nullable: true
		productCode nullable: true
		productRevision nullable: true
		lotNumber nullable: true
		processViolations nullable: true
	}

	static mapping = {
		code index:true, indexAttributes: [unique:true, dropDups:true]
	}

	String toString() {
		code
	}
}
