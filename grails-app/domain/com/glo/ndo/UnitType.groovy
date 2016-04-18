package com.glo.ndo

class UnitType {

	  String name
	  
	  static constraints = {
			name blank:false, maxSize:100
	  }
	  
	  String toString() {
		  name 
	  }
}
