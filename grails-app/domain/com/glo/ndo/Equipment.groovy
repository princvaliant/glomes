package com.glo.ndo

class Equipment implements Comparable  {

	static auditable = true

	String name
	String code
	String status
	String subStatus
	String failureCode
	String comment
	Boolean allowDataCollection
	Boolean isE10
	Integer e10WeeklyTarget
	Date dateStart

	static belongsTo = ProcessStep

	static hasMany = [
		equipmentParts: EquipmentPart,
		equipmentActiveParts: EquipmentActivePart,
		processSteps: ProcessStep,
		equipmentFailures: EquipmentFailure,
	]

	static hasOne = [
		company: Company,
		location: Location,
		workCenter: WorkCenter
	]

	static constraints = {
		name blank:false,maxSize:100,unique:true
		status blank:false, maxSize:100, inList:['idle','processing','scheduled','unscheduled','engineering','nonscheduled']
		subStatus nullable:true
		failureCode nullable:true
		company nullable:true
		workCenter nullable:true
		code nullable: true
		location nullable:true
		dateStart nullable:true
		comment nullable: true, maxSize:2000
		allowDataCollection nullable: true
		e10WeeklyTarget nullable:true
		isE10 nullable: true
	}

	String toString() {
		name
	}

	public int compareTo(def other) {
		return this.toString() <=> other?.toString()
	}
}
