package com.glo.ndo

class EquipmentPart implements Comparable {

	static auditable = true

	String code
	String comment

	static belongsTo = [
		equipment:Equipment
	]

	static constraints = {
		code blank:false,maxSize:100
		comment nullable: true, maxSize:2000
	}

	String toString() {
		equipment?.name + ", " + code
	}

	public int compareTo(def other) {
		return code <=>  other?.code
	}

}
