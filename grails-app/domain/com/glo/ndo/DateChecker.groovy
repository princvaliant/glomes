package com.glo.ndo

class DateChecker {

		String syncType 
		String name
		Long value
	  
	  static constraints = {
		  	syncType blank:false
			name blank:false, maxSize:1000
			value nullable:false
	  }
	  
	  String toString() {
		  name 
	  }
}
