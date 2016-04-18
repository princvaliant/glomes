package com.glo.ndo

class EquipmentUnscheduled implements Comparable {

	static auditable = true

	String code
	String status
	String name

	static constraints = {
		code blank:false,maxSize:100
		status nullable:true
		name blank:false, maxSize:100
	}

	String toString() {
		 name + " - " + code
	}
	
	public int compareTo(def other) {
		return name + "_" + code <=>  other?.name + "_" + other?.code
	}
}
