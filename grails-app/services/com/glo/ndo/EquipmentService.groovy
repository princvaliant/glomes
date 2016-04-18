package com.glo.ndo

import com.glo.run.EquipmentStatus
import com.glo.security.Role
import grails.util.Environment
import org.apache.commons.logging.LogFactory

import java.text.DateFormat
import java.text.SimpleDateFormat

class EquipmentService {

	private static final logr = LogFactory.getLog(this)
	def managementService
	def runtimeService
	def repositoryService
	def taskService
	def messageSource
	def utilsService
	def mailService
	def unitService

	static transactional = false

	def getByPkeyAndTkey (def pkey, def tkey) {

		def equipments
		if (pkey && tkey) {

			def process = Process.findByPkey (pkey)
			def processStep = ProcessStep.findByProcessAndTaskKey(process, tkey)
			equipments = processStep.equipments
		}

		if (!equipments) {
			equipments = Equipment.list()
		}
		equipments.sort()
	}
	
	def getByAllowDataCollection() {
		
		Equipment.findAllByAllowDataCollection(true).sort()
	}

	def getByLocation (def location) {

		def equipments
		if (location) {
			def loc = Location.get(location)
			equipments = loc.equipments
		}
		if (!equipments) {
			equipments = Equipment.list()
		}
		equipments.sort()
	}
	
	def get (def parms) {

		logr.debug(parms)
		
		def queryParams = [true]
		def query = "select equipment from Equipment as equipment where equipment.isE10 = ? "
		
		if (parms.search) {
			query += " and  (equipment.name like ? or equipment.status like ?) "
			queryParams.addAll([parms.search + "%",parms.search + "%"])
		}
		
		if (parms.workCenter && !(parms.workCenter == "(view all)" || parms.workCenter == "0")) {
			long wid = -1
			if (!parms.workCenter.isLong()) {
				wid = WorkCenter.findByName(parms.workCenter).id
			} else {
				wid = parms.workCenter.toLong()
			}
			query += " and equipment.workCenter.id = ? "
			queryParams.add(wid)
		}
		
		def equipments = Equipment.executeQuery( query + (parms.sort ? " order by equipment." + parms.sort + " " + parms.dir : "")
		, queryParams, [max:parms.max,offset:parms.offset] )
		def total = Equipment.executeQuery( query, queryParams).size()
		
		[equipments,total]
	}
	
	def getStatuses (def parms) {
		
		def eid = parms.eid?.toLong() ?: -1
		DateFormat  dformat = new SimpleDateFormat("yyyy-MM-dd HH:mm")
		
		def equipmentStatuses = EquipmentStatus.findAllByEid( eid, [offset:parms.offset, max:parms.max, sort: parms.sort, order: parms.dir])
		def total = EquipmentStatus.findAllByEid(eid).size()
		
		[
			equipmentStatuses.collect {
				[id: it.id, name: it.name, status: it.status, subStatus: it.subStatus, failureCode: it.failureCode, userName: it.userName, dateStart: dformat.format(it.dateStart),
				 duration:  utilsService.millisToShortDHMS(it.duration, it.dateStart), comment: it.comment]
			},
			total
		]
	}

    def getStatus (def parms) {

        def ret = [:]
        def equipment = Equipment.findByName(parms.name?.trim())
        if (equipment) {
            ret.put ('success', true)
            ret.put ('status', equipment.status)
            ret.put ('run', equipment.status == "processing" ? true : false)
        } else {
            ret.put ('success', false)
            ret.put ('msg', "Equipment '" + parms.name + "' does not exist")
        }
       ret
    }

	def getHistory (def parms) {
		
		def eid = parms.eid?.toLong() ?: -1

		SimpleDateFormat formatter = new SimpleDateFormat("MMM dd yyyy")
		def sd =  formatter.parse(parms.start[4..14])
		def ed =  formatter.parse(parms.end[4..14]) + 1			// add 1 day in order to include the 'end' day

		def equipmentStatuses = EquipmentStatus.findAllByEidAndDateStartBetween( eid, sd, ed, [sort: 'dateStart', order: 'desc'])
		
		DateFormat  dformat = new SimpleDateFormat("yyyy-MM-dd HH:mm")
		equipmentStatuses.collect {
			[Name: it.name, Status: it.status, Code: it.subStatus, Reason: it.failureCode, User: it.userName, Started: dformat.format(it.dateStart),
			 Duration:  utilsService.millisToShortDHMS(it.duration, it.dateStart), Note: it.comment]
		}
	}

	def getFailures (def parms) {
		
		def eid = parms.eid.toLong() ?: -1
		
		def equipmentFailures = EquipmentFailure.findAllByEquipmentAndStatus( Equipment.get(eid), parms.statusCode ?: 'scheduled').sort()
		if (equipmentFailures) {
			equipmentFailures.collect { [code:it.code, comment: it.comment] }
		} else {
			""
		}
	}
	
	def getUnscheduled (def parms) {
		if (!parms.status) {
			return ""
		}
		def equipmentUnscheduled = EquipmentUnscheduled.findAllByStatus(parms.status).sort()
		if (equipmentUnscheduled) {
			equipmentUnscheduled.collect { [code:it.code, name: it.toString()] }
		} else {
			""
		}
	}
	
	def getMaintenance (def parms) {
		
		def eid = parms.eid?.toLong() ?: -1
		def equipment = Equipment.get(eid)
		if (equipment) {
			def equipmentMaintenance = EquipmentMaintenance.findAllByEquipment(equipment , [offset:parms.offset, max:parms.max, sort: parms.sort, order: parms.dir])
			def total = EquipmentMaintenance.findAllByEquipment(equipment).size()
			
			[
				equipmentMaintenance,
				total
			]
		} else {
			[[], 0]
		}
	}
	
	def add (String user, def name, def comment) {
		
		if (name) {
			def	equipment = new Equipment()
			equipment.name = name
			equipment.comment = comment
			equipment.status = "idle"
			equipment.dateStart = new Date()
			equipment.save(failOnError: true)
		}
	}
	
	def changeMaintenance (String user, def parms) {
		
		def	equipmentMaintenance
		def operation = ""
		if (parms.id) {
			equipmentMaintenance =  EquipmentMaintenance.get(parms.id)
		} else {
			equipmentMaintenance = new EquipmentMaintenance()
			equipmentMaintenance.equipment = Equipment.get(parms.eid.toLong())
			operation = "ADD"
		}
		
		equipmentMaintenance.schedule = parms.schedule
		equipmentMaintenance.comment = parms.comment
		equipmentMaintenance.cycleType = parms.cycleType
		equipmentMaintenance.cycleRate = parms.cycleRate.toInteger()
		equipmentMaintenance.department = parms.department
		equipmentMaintenance.tag = parms.tag
		
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm a");
		def dateTime =  formatter.parse(parms.cycleStartDate + " " + parms.cycleStartTime)
		equipmentMaintenance.cycleStartDate = dateTime
		
		if (parms.active == "on") {
			equipmentMaintenance.active = true
		} else {
			equipmentMaintenance.active = false
		}
		
		equipmentMaintenance.save(failOnError: true)
		
		if (operation == "ADD") {
			unitService.addMaintenanceTask(equipmentMaintenance, equipmentMaintenance.cycleStartDate, [], [], equipmentMaintenance.department,equipmentMaintenance.tag,false)
		}
	}
	
	def deleteMaintenance (String user, def emid) {
		
		def	equipmentMaintenance = EquipmentMaintenance.get(emid)
		equipmentMaintenance.delete()
	}
	
	def update (String user, def equip) {
	
		def	equipment = Equipment.get(equip.id)
		if (equip.name) equipment.name = equip.name
		if (equip.comment) equipment.comment = equip.comment
		equipment.save(failOnError: true)
	}
	
	def deleteStatus (String user, def esid) {
		
		def	equipmentStatus = EquipmentStatus.get(esid)
		
		def oldsBefore =  EquipmentStatus.withCriteria {
			eq ('eid', equipmentStatus.eid)
			lt ('dateStart', equipmentStatus.dateStart)
			order('dateStart', 'desc')
			max(1)
		}[0]
		
		def oldsAfter =  EquipmentStatus.withCriteria {
			eq ('eid', equipmentStatus.eid)
			gt ('dateStart', equipmentStatus.dateStart)
			order('dateStart', 'asc')
			max(1)
		}[0]
		
		if (oldsBefore && oldsAfter) {
			
			oldsBefore.dateEnd = oldsAfter.dateStart
			oldsBefore.duration = (oldsBefore.dateEnd.getTime() - oldsBefore.dateStart.getTime()) / 1000 as Long
			oldsBefore.save()

		} else if (oldsBefore) {
			oldsBefore.dateEnd = null
			oldsBefore.duration = null
			oldsBefore.save()
		}

		equipmentStatus.delete()
	}
	
	def editComment (String user, def esid, def comment) {
	
		def	equipmentStatus = EquipmentStatus.get(esid)
		equipmentStatus.userName = user
		equipmentStatus.comment = comment
		equipmentStatus.save()
	}
	
	def changeStatus (String user, def equipmentStatus, def subStatus, def failureCode, def date, def time, def comment, def equipments) {
		
		SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy hh:mm a");
		def dateTime =  formatter.parse(date + " " + time)
	
		equipments.each { parm ->
			
			// Find if date is between existing date
			def oldsBefore =  EquipmentStatus.withCriteria {
				eq ('eid', parm.id)
				le ('dateStart', dateTime)
				order('dateStart', 'desc')
				max(1)
			}[0]
			
			def oldsAfter =  EquipmentStatus.withCriteria {
				eq ('eid', parm.id)
				ge ('dateStart', dateTime)
				order('dateStart', 'asc')
				max(1)
			}[0]
			
			
			if (oldsBefore && oldsAfter) {
				
				oldsBefore.dateEnd = dateTime
				oldsBefore.duration = (oldsBefore.dateEnd.getTime() - oldsBefore.dateStart.getTime()) / 1000 as Long
				oldsBefore.save()

				insertStatus (parm.id, equipmentStatus, subStatus, failureCode, dateTime, oldsAfter.dateStart, comment, user)
				
			} else if (oldsBefore) {
			
				oldsBefore.dateEnd = dateTime
				oldsBefore.duration = (oldsBefore.dateEnd.getTime() - oldsBefore.dateStart.getTime()) / 1000 as Long
				oldsBefore.save()
			
				insertStatus (parm.id, equipmentStatus, subStatus, failureCode, dateTime, null, comment, user)
				
			} else if (oldsAfter) {
			
				insertStatus (parm.id, equipmentStatus, subStatus, failureCode, dateTime, oldsAfter.dateStart, comment, user)
			} else {
			
				insertStatus (parm.id, equipmentStatus, subStatus, failureCode, dateTime, null, comment, user)
			}

		}
		//if (Environment.currentEnvironment != Environment.DEVELOPMENT ) {
			equipments.each {
				if (equipmentStatus == "unscheduled") {
                    def equip = it
                    def role = Role.findByAuthority("ROLE_EQUIPMENT_NOTIFICATION")
                    def users = role?.getUsers()
                    Thread.start {
                        notifyAdmins (Equipment.get(equip.id), equipmentStatus, subStatus, comment, users)
                    }
			   }
			}
		//}
	}
	
	private def insertStatus (def id, def status, def subStatus, def failureCode, def startTime, def endTime, def comment, def user) {
		
		def equipment = Equipment.get(id)
		
		def eq = new EquipmentStatus()
		eq.eid = id
		eq.name = equipment.name
		eq.status = status
		eq.subStatus = subStatus
		eq.failureCode = failureCode
		eq.dateStart = startTime
		eq.dateEnd = endTime
        eq.workCenter = equipment?.workCenter?.name

		if (endTime != null) {
			eq.duration = (endTime.getTime() - startTime.getTime()) / 1000 as Long
		}
		eq.userName = user
		eq.comment = comment
		eq.save()
		
		def equipmentStatus = EquipmentStatus.findByEid(equipment.id, [ max:1, sort: 'dateStart', order: 'DESC'])
		if (equipmentStatus) {

			equipment.comment = equipmentStatus.comment
			equipment.status = equipmentStatus.status
			equipment.subStatus = equipmentStatus.subStatus
			equipment.failureCode = equipmentStatus.failureCode
			equipment.dateStart = equipmentStatus.dateStart
			equipment.save(failOnError: true)
		}

	}
	
	private def notifyAdmins (equipment, status, subStatus, comment, users) {

		users.each {
			try {
                if (it.email) {
                    def em = it.email

                    em = em.replace("glo-usa.com", "glo.se")

                    mailService.sendMail {
                        to em
                        subject "From " +  equipment.name
                        body  status + ", " + subStatus + " - " + comment
                    }
                }
				if (it.textTo) {
                    def em = it.textTo
					mailService.sendMail {
						to em
						subject "From " +  equipment.name
						body  status + ", " + subStatus + " - " + comment
					 }
				}
			} catch (Exception exc) {
				logr.error(exc.getMessage())
			}
		}
	}
}
