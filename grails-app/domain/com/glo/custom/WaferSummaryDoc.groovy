package com.glo.custom

class WaferSummaryDoc {
	
	String 		testType
	String 		username
    String 		parameter
    String      current
	Date 		dateCreated
    Boolean     isActive

	static belongsTo = [
		dieSpec: DieSpec
	]

	static constraints = {
		testType blank:false
		dieSpec nullable: true
		username blank: false
        parameter blank: false
        current blank: false
        isActive nullable: true
	}

	String toString() {
		testType + " " + parameter + " at " + current
	}
}

