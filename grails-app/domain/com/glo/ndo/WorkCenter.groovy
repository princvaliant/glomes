package com.glo.ndo

class WorkCenter implements Comparable  {

	static auditable = true

	String name
	String comment

	static hasMany = [
		operations: Operation,
		equipments: Equipment
	]

	static constraints = {
		name blank:false,maxSize:100,unique:true
		comment nullable: true, maxSize:2000
	}

	String toString() {
		name
	}

	public int compareTo(def other) {
		return this.toString()?.toUpperCase() <=> other?.toString()?.toUpperCase()
	}
}
