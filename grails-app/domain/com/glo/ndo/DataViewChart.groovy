package com.glo.ndo

class DataViewChart implements Comparable {

	Integer idx
	String axis
	
	String xDateGroup
	String xStringGroup
	String xNumericGroup
	String xNumberRange
	String yAggregate
	String sortDir
	
	static belongsTo = [
		dataView: DataView,
		dataViewVariable:DataViewVariable
	]

	static constraints = {
		idx nullable: true
		axis blank:false, inList:[
			'X',
			'Y',
			'Z',
			'S'
		]
		xDateGroup nullable: true,inList:[
            'hourly',
			'daily',
			'weekly',
			'monthly',
			'yearly'
		]
		xStringGroup nullable: true,inList:[
			'whole'
		]
		xNumericGroup nullable: true,inList:[
			'last',
			'all'
		]
		xNumberRange nullable: true
		yAggregate nullable: true,inList:[
			'last',
			'count',
			'average',
			'sum',
			'max',
			'min',
			'median',
			'avg+sigma',
			'avg-sigma'
		]
		sortDir nullable:true,inList:[
			'ASC',
			'DESC'
		]
	}

	String toString() {
		dataView?.name + ", " +  axis + ", " + DataViewVariable.variable?.name 
	}

	public int compareTo(def other) {
		return this.toString() <=> other?.toString()
	}
}
