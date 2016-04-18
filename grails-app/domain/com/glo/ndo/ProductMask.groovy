package com.glo.ndo

class ProductMask implements Comparable {

	static auditable = true

	String name
    int dxFrom
    int dxTo
    int dyFrom
    int dyTo
    boolean isPcm
    String recipe
    float kFactorCurrent
	
	static belongsTo = [
		product: Product
	]

	static hasMany = [
		productMaskItems: ProductMaskItem
	]
	
	static constraints = {
		name blank: false, maxSize:100
        dxFrom nullable:false
        dxTo nullable:false
        dyFrom nullable:false
        dyTo nullable:false
        isPcm nullable:false
        recipe nullable:false
        kFactorCurrent nullable: false

	}

	String toString() {
		name 
	}

	public int compareTo(def other) {
		return this.toString() <=> other?.toString()
	}
}
