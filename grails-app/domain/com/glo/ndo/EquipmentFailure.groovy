package com.glo.ndo

class EquipmentFailure implements Comparable {

	static auditable = true

	String code
	String status
	String comment

	static belongsTo = [
		equipment:Equipment
	]

	static constraints = {
		code blank:false,maxSize:100
		status blank:false, maxSize:100, inList:['idle','processing','scheduled','unscheduled','engineering','nonscheduled']
		comment nullable: true, maxSize:2000
	}

	String toString() {
		equipment?.name + ", " + status + " - " + code
	}

	public int compareTo(def other) {
		return status + "_" + code <=>  other?.status + "_" + other?.code
	}

}
