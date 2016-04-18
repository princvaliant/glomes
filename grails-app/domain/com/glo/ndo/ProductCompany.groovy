package com.glo.ndo

class ProductCompany implements Comparable {

	static auditable = true

	String vendorCode
	Float price
	Integer deliveryTime
	Boolean disabled

	static belongsTo = [
		company: Company,
		product: Product
	]

	static constraints = {
		product nullable: false
		company nullable: false
		vendorCode nullable: true
		disabled nullable: true
		price nullable: true
		deliveryTime nullable: true
	}

	String toString() {
		company?.name + ' - ' + product?.name + ", " + (vendorCode ?: '')
	}

	public int compareTo(def other) {
		return this.toString()?.toUpperCase() <=> other?.toString()?.toUpperCase()
	}
}
