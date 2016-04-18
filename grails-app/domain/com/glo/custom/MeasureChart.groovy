package com.glo.custom

class MeasureChart   {
	
	Integer		ord
	String 		name
	String 		testType
	String 		resultType
	String	 	revision
	String 		filterField
	String 		filterTitle
	String 		groupField
	String 		groupTitle
	String 		xField
	String 		xTitle
	String 		yField
	String 		yTitle
	
	
	static constraints = {
		ord blank:false
		name blank:false
		testType blank:false
		resultType blank: false
	}
}
