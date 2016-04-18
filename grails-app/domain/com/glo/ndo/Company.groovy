package com.glo.ndo

import com.glo.security.*

class Company implements Comparable {

	static auditable = true

	static searchable = [only: [
		'name',
		'comment'
	]]
	
	String name
	String comment

	static belongsTo = ProcessStep

	static hasMany = [
		locations: Location,
		productCompanies: ProductCompany,
		processSteps: ProcessStep,
		equipments: Equipment,
		users: User
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
