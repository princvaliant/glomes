package com.glo.ndo

class Variable implements Comparable {

	static auditable = true

    Integer idx
	String grp
	String dir
	String name
	String title
	String dataType
	Boolean hidden
    Boolean editor
    Boolean readOnly
    Boolean allowBlank
	String defaultValue
	String listValues
	String evalScript
	String specLimit
	String graphLimit
	String allowedRange
	Boolean listForceSelection
	String searchPrefix
    Boolean includeInSearch
	Integer width

	static belongsTo = [
		process:Process,
		processStep:ProcessStep
	]
	
	static hasMany = [
		dataViewVariables: DataViewVariable
	]


	static constraints = {
		idx nullable: true
		grp nullable: true
		name nullable:false,maxSize:100
		title nullable: true, maxSize: 100
		dir nullable:false, inList:[
			'in',
			'din',
			'dc',
			'recp',
			'spec',
			'calc',
			''
		]
		dataType nullable:false, inList:[
			'string',
			'int',
			'float',
			'scientific',
			'date',
			'Location',
			'Company',
			'Equipment',
			'Product',
			'Part',
			'User',
			'Process',
			'object',
			'objectArray',
			'bigString',
			'DieSpec'
		]
		allowBlank nullable: true
		editor nullable: true
		readOnly nullable: true
		hidden nullable: true
		process nullable: true
		processStep nullable: true
		width nullable:true
		defaultValue nullable:true, maxSize: 100   // Contains default value
		listValues nullable:true, maxSize: 2000   // If provided contains comma delimited list of values
		listForceSelection nullable: true
		evalScript nullable:true, maxSize: 30000   // If provided contains comma delimited list of values
		allowedRange nullable: true, maxSize: 100
		specLimit nullable: true, maxSize: 100 
		graphLimit nullable: true, maxSize: 100
		includeInSearch nullable:true
		searchPrefix nullable: true

	}

	String toString() {
		dir + " - "  + (process?.toString() ?: processStep?.toString()) + " - " + idx?.toString()?.padLeft(5, ' ') + " - "+ name + " [" + dataType + "]"
	}

	public int compareTo(def other) {
		return this.toString()?.toUpperCase() <=> other?.toString()?.toUpperCase()
	}
}
