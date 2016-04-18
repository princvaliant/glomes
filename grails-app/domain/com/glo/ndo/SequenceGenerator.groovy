package com.glo.ndo

class SequenceGenerator {

	  String name
	  Long value
	  
	  static constraints = {
			name blank:false, maxSize:100
			value nullable:false
	  }
	  
	  String toString() {
		  name 
	  }
}
