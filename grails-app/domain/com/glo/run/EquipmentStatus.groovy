package com.glo.run

class EquipmentStatus   {
	
	static mapWith = "mongo"
	
	Long eid
	String name
	String status
	String subStatus
	String failureCode
	Date dateStart
	Date dateEnd
	Integer duration
	String userName
	String comment
    String workCenter
	
	static constraints = {
		eid nullable:false
		name blank: false
		status blank:false, maxSize:500
		subStatus nullable:true
		failureCode nullable:true
		dateStart blank: false
		dateEnd nullable: true
		duration nullable: true
		userName nullable: true
		comment nullable:true
        workCenter nullable:true
	}
}
