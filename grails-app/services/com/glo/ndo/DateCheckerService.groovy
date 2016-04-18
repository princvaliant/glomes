package com.glo.ndo

import org.apache.commons.logging.LogFactory

import java.text.SimpleDateFormat

class DateCheckerService {

	private static final logr = LogFactory.getLog(this)


	def Boolean modified (def syncType, def file) {

		Boolean ret = false
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss")
		def dateMod = sdf.format(file.lastModified()).toLong()
		def dc = DateChecker.findBySyncTypeAndName(syncType, file.name.toUpperCase())
		if (!dc) {
			new DateChecker(syncType:syncType,name: file.name.toUpperCase(),value:dateMod).save() 
			ret = true
		} else if (dc.value < dateMod) {
			dc.value = dateMod
			dc.save()
			ret = true
		} 
		
		ret
	}
}
