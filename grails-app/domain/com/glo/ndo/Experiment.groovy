package com.glo.ndo

import com.glo.security.User

class Experiment  {

	static auditable = true
	String code
	String name
	boolean isClosed
    
   static belongsTo = [
	   user: User
   ]
   
   static constraints = {
	    code blank:false,maxSize:100
		name blank:false,maxSize:100
		isClosed nullable: true
   }
   
   String toString() {
	  code 
   }
}
