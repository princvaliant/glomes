package com.glo.ndo

class ProductMaskItemCtlm implements Comparable {

	static auditable = true

	String code
	String location
	String setType
	Integer circle
	Float spacing
	Float radius
	Boolean isActive	
	
	static belongsTo = [
		productMask: ProductMask,
		derivedProduct: Product
	]

	static constraints = {
		code blank: false, maxSize:100
		location nullable:false
		setType nullable:false
		circle nullable:false
		spacing nullable:false
		radius nullable:false
		isActive nullable:true
	}

	String toString() {
		code + ", " + derivedProduct?.name
	}

	public int compareTo(def other) {
		return this.toString() <=> other?.toString()
	}
}
