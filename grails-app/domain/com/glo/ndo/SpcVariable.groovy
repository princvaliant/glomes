package com.glo.ndo

class SpcVariable implements Comparable {

	Integer idx
	String title
	String path
	String products
	String filters
    Integer sortOrder

	static belongsTo = [
		spc: Spc,
		variable: Variable
	]

	static constraints = {
		spc nullable: false
		variable nullable: false
		path nullable: true
		products nullable: true
		filters  nullable: true
        sortOrder nullable: true
	}

	String toString() {

        def sort = sortOrder >= 0 ? sortOrder.toString().padLeft(8, '0') : ""
        sort + "_" + variable?.name + ", " + spc?.name + ", " + path
	}

	public int compareTo(def other) {

        return this.toString() <=> other?.toString()
	}
}
