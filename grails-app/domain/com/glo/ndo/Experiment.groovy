package com.glo.ndo

class Experiment  {

	static auditable = true
	String code
	String name
	boolean isClosed
    
    static constraints = {
	    code blank:false,maxSize:1000,unique:true
		name blank:false,maxSize:1000
		isClosed nullable: true
    }
   
   String toString() {
	  code 
   }
}
