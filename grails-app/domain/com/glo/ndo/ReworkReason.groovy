package com.glo.ndo

class ReworkReason implements Comparable  {

	static auditable = true

	String name
	SortedSet processSteps
	SortedSet operations
	
	static belongsTo = [ProcessStep, Operation]

	static hasMany = [
		processSteps:ProcessStep,
		operations: Operation
	]

	static constraints = {
		name blank:false,maxSize:1000
	}

	String toString() {
		name
	}

	public int compareTo(def other) {
		return this.toString() <=> other?.toString()
	}
}
