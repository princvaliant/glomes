package com.glo.ndo

class EquipmentMaintenance implements Comparable {

	static auditable = true

	String schedule
	String comment
	String cycleType
	Integer cycleRate
	Date cycleStartDate
	Boolean active	
	String department
	String tag

	static belongsTo = [
		equipment:Equipment
	]

	static constraints = {
		schedule blank:false,maxSize:200
		comment nullable: true, maxSize:2000
		equipment nullable: false
		cycleType blank:false, inList:[
			'hours',
			'days',
			'weeks',
			'months',
			'quarters',
			'years',
			'runs',
			'weights',
			'depositions'
		]
		cycleRate blank:false
		cycleStartDate blank:false
		department nullable: true
		tag nullable:true
	}

	String toString() {
		equipment?.name + ", " + schedule
	}

	public int compareTo(def other) {
		return this.toString() <=>  other?.toString()
	}

}
