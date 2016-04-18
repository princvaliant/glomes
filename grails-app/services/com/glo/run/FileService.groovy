package com.glo.run

import com.mongodb.gridfs.GridFS
import org.apache.commons.logging.LogFactory
import org.bson.types.ObjectId

class FileService {

	private static final logr = LogFactory.getLog(this)
	
	def mongo
	def historyService
	
	def saveFile(def file, def alias) {
		
		def gridfs = new GridFS(mongo.getDB("glo"), 'unitFile')

		def	inputStream = file.getInputStream()
		def	contentType = file.getContentType()
		def filename = file.getOriginalFilename()

		if (gridfs.findOne(filename) == null) {
			save(gridfs, inputStream, contentType, filename, alias)
		} else {
			logr.debug("Removing old file and uploading new file")
			gridfs.remove(filename)
			save(gridfs, inputStream, contentType, filename, alias)
		}
	}
	
	def saveFileFromDisk(def file, def alias, def contentType ) {
		
		def gridfs = new GridFS(mongo.getDB("glo"), 'unitFile')

		def inputStream = new FileInputStream(file)
		def filename = alias

		if (gridfs.findOne(filename) == null) {
			save(gridfs, inputStream, contentType, filename, alias)
		} else {
			logr.debug("Removing old file and uploading new file")
			gridfs.remove(filename)
			save(gridfs, inputStream, contentType, filename, alias)
		}
	}
	
	def saveFileMeta (String user, def units, def fileId, def name, def fileName) {
		
		def retUnits = []
		units.each { parm ->
			def file = File.findByFileName(fileName)
			if (!file) {
				file = new File(userName: user, name: name, fileName: fileName, fileId: fileId.toString())
			}
			def unit = Unit.get(parm)
			unit.addToFiles(file)
			unit.nFiles = unit.files.size()
			unit.save(failOnError: true)
			retUnits.add(unit.dbo)
		}
		retUnits.each {
			historyService.initHistory ("fileUpload", null, it, null)
		}
	}
	
	private def save(def gridfs, def inputStream,def contentType,def filename, def alias) {
		def inputFile = gridfs.createFile(inputStream)
		inputFile.setContentType(contentType)
		inputFile.setFilename(filename)
		inputFile.put( "aliases", [alias])
		inputFile.save()
		inputFile._id
	}
	
	def retrieveFile(String filename) {
		def gridfs = new GridFS(mongo.getDB("glo"), 'unitFile')
		return gridfs.findOne(filename)
	}
	
	def getFileById(ObjectId _id) {
		def gridfs = new GridFS(mongo.getDB("glo"), 'unitFile')
		return gridfs.find(_id)
	}
	
	def deleteFile(String filename) {
		def gridfs = new GridFS(mongo.getDB("glo"), 'unitFile')
		gridfs.remove(filename)
	}
		
	def getFilesList() {
		def gridfs = new GridFS(mongo.getDB("glo"), 'unitFile')
		def cursor = gridfs.getFileList()
		cursor.toArray()
	}

	
}
