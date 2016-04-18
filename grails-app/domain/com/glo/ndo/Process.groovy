package com.glo.ndo

class Process  implements Comparable {

	static auditable = true

	String category
	String pkey
	String pctgAfter
	String pkeyAfter
	String idPrefix
	SortedSet processSteps
	SortedSet variables
	String engtMoveRoles
	Boolean engMoveIn
    Boolean disabled
	
	static belongsTo = [
		processCategory:ProcessCategory
	]
	
	static hasMany = [
		variables: Variable,
		processSteps: ProcessStep,
		products: Product
	]

	static constraints = {
		pkey blank:false,unique:true,maxSize:100
		category blank:false,maxSize:100
		pctgAfter nullable: true
		pkeyAfter nullable: true
		idPrefix nullable: true
		engtMoveRoles nullable: true
		engMoveIn nullable: true
        disabled nullable: true
		processCategory nullable: true
	}

	String toString() {
		category + " - " + pkey
	}

    public int compareTo(def other) {
        return this.toString()?.toUpperCase() <=> other?.toString()?.toUpperCase()
    }
}
