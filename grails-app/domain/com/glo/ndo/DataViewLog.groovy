package com.glo.ndo

import com.glo.security.User


class DataViewLog  {
	
   Long dataViewId
   String name
   String owner
   String user
   Long duration
   String query
   Date runDate
   String error
  
   static constraints = {
		dataViewId blank:false
		name nullable: false
		error nullable: true
		query nullable: true
		user nullable: true
		owner nullable: true
   }
   
   String toString() {
	  name 
   }
}
