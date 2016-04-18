package com.glo.ndo

class Operation implements Comparable  {

	static auditable = true

	String name
	String comment

	static belongsTo = [
		workCenter: WorkCenter
	]
	
	static hasMany = [
		processSteps: ProcessStep,
		yieldLossReasons: YieldLossReason,
		bonusReasons: BonusReason,
		reworkReasons: ReworkReason
	]

	static constraints = {
		name blank:false,maxSize:100,unique:true
		comment nullable: true, maxSize:2000
	}

	String toString() {
		name
	}

	public int compareTo(def other) {
		return this.toString() <=> other?.toString()
	}
}
