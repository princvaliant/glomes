package com.glo.ndo

class Location implements Comparable  {

	static auditable = true

	String name
	String address
	String fullName

	static belongsTo = [
		company:Company
	]

	static hasMany = [
		equipments: Equipment
	]

	static transients = ["fullName"]

	static constraints = {
		name blank:false,maxSize:100
		address nullable:true
	}

	def getFullName() {
		return company?.name + ", " + name
	}

	String toString() {
		getFullName()
	}

	public int compareTo(def other) {
		return this.toString() <=> other?.toString()
	}
}
