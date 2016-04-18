package com.glo.ndo

import com.glo.security.User


class DataViewJoin  {

   static auditable = true
	
   static belongsTo = [
	   dataView: DataView
   ]
   
   DataView secondaryDataView
   String joinType
   
   DataViewVariable primaryVariable
   DataViewVariable secondaryVariable
   
   static constraints = {
	    primaryVariable blank:false
	    secondaryVariable blank:false
		secondaryDataView blank:false
		joinType blank:false, inList:["Left join"]

   }
   
   String toString() {
	  secondaryDataView.name 
   }
}
