package com.glo.ndo

class ProductFamily implements Comparable {

	static auditable = true

	String name
	String comment

	static hasMany = [
		products: Product
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
