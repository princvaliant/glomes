package com.glo.ndo

import com.glo.security.User


class Spc  {

   static auditable = true
	
   String name
   String description
   Boolean isPublic
   String tag
   Integer sampleSize
   String filter
   Boolean showText
   
   Date dateCreated 
   Date lastUpdated 
   String owner
   SortedSet spcVariables
  
   static hasMany = [
	   spcVariables: SpcVariable
   ]
   
   static constraints = {
		name blank:false,maxSize:100,unique:true
		tag nullable: true
		description nullable:true
		isPublic nullable:true
		sampleSize nullable:true
		filter nullable:true
		showText nullable:true
   }
   
   String toString() {
	  name 
   }
}
