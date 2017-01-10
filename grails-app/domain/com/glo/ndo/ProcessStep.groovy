package com.glo.ndo

class ProcessStep implements Comparable {

	static auditable = true

	Integer idx
	String taskKey
    String tag
	Boolean allowSplit
	Boolean allowMerge
	Boolean allowSync
	Boolean allowImport
	Boolean allowRework
    Boolean allowCustomAction
    String customAction
	Boolean filterByCompany
    Boolean isWorkInProgress
	Integer estimateDuration
    SortedSet companies
    SortedSet equipments
	SortedSet variables
	SortedSet yieldLossReasons
	SortedSet bonusReasons
	SortedSet reworkReasons
	String routingScript
	String splitProductCodes
    String barcodePrinter
	String barcodePrinting
	String barcodeScanning
	Boolean disableEngineeringMove
	Boolean disableOperationMove
	Boolean allowMoveDespiteSpecFail
    Boolean moveChildren
    Boolean preventRegularMove

    String instructions

	static belongsTo = [
		process: Process,
		operation: Operation
	]

	static hasMany = [
		variables: Variable,
		companies: Company,
		equipments: Equipment,
		yieldLossReasons: YieldLossReason,
		bonusReasons: BonusReason,
		reworkReasons: ReworkReason
	]

	static constraints = {
		idx nullable: true
		taskKey blank:false,maxSize:100
        tag nullable:true
		estimateDuration nullable: true
		process blank:false
		operation nullable:true
        moveChildren nullable:true
        preventRegularMove nullable:true
		companies  nullable:true
		equipments nullable:true
		variables nullable:true
		yieldLossReasons  nullable:true
		bonusReasons  nullable:true
		reworkReasons  nullable:true
		allowSplit nullable: true
		allowMerge nullable: true
		allowSync nullable:true
		allowImport nullable:true
		allowRework nullable:true
        allowCustomAction nullable: true
        customAction nullable: true
        isWorkInProgress nullable:true
		filterByCompany nullable:true
		routingScript nullable:true, maxSize: 20000
		splitProductCodes nullable:true
		barcodePrinter nullable:true
		barcodePrinting nullable: true
		barcodeScanning nullable: true
		disableEngineeringMove nullable:true
		disableOperationMove nullable:true
		allowMoveDespiteSpecFail nullable:true
        instructions nullable:true
	}
	

	String toString() {
		process?.toString() + " - " + taskKey
	}

	public int compareTo(def other) {
		return String.format("%07d",this.idx) + this.taskKey <=> String.format("%07d",other?.idx) + other?.taskKey
	}
}
