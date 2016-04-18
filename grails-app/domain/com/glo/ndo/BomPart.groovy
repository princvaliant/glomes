package com.glo.ndo

class BomPart implements Comparable {

	static auditable = true
	
	Integer quantity
	
	static belongsTo = [
		partProduct: Product,
		bom: Bom
	]
	
	static constraints = {
		partProduct nullable : false
		quantity blank: false
	}

	String toString() {
		partProduct.name + "(" + partProduct.code + ") : " + quantity?.toString()
	}

	public int compareTo(def other) {
		return this.toString()?.toUpperCase() <=> other?.toString()?.toUpperCase()
	}
}
