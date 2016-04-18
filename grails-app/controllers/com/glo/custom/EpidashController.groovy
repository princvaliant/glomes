package com.glo.custom

import grails.converters.JSON
import org.apache.commons.logging.LogFactory

import javax.imageio.ImageIO
import java.awt.*
import java.awt.image.BufferedImage

class EpidashController {

	private static final logr = LogFactory.getLog(this)
	def fileService
	def mongo 
	
	static def FOLDER = "\\\\calserver03\\Operations\\FABDASHBOARD\\"

	def index = {
		
		def db = mongo.getDB("glo")
		def unit = db.dataReport.find ([code:params.id], [:]).collect{it}[0]
		
		if (!unit) {
			return [message:"Wafer ID " + params.id + " does not exists"]
		} else {
		
			session.waferId = unit?.code ?: ""
			session.runNumber =  unit?.value?.pl?.runNumber?.toUpperCase() ?: ""
			session.experimentId =  unit?.value?.experimentId ?: ""
			session.runDate =  unit?.value?.pl?.actualStart
			session.reactor =  unit?.value?.pl?.runNumber?.substring(0,2)
			session.fabProcess =  unit?.value?.process
			session.pitch =  unit?.value?.nil?.pitch
			
			return [code:session.waferId,
				runNumber: session.runNumber, 
				experimentId: session.experimentId,
				fabProcess: session.fabProcess,
				pitch:session.pitch]
		}
	}
	
	def postImage = {
		
		try {

			def fdir = FOLDER + params.code
			File f = new File(fdir)
			if (!f.exists()){
				f.mkdir()
			}
			
			OutputStream out= new FileOutputStream(fdir + "\\" + params.fileName)
			String str = params.image.tokenize(",")[1];
			byte[] decodedBytes =  str.decodeBase64()
			out.write(decodedBytes)
			out.close()

			render ([success:true] as JSON)
		}
		catch(Exception exc) {
				logr.error(exc)
				render ([success:false, msg: exc.toString()] as JSON)
		}
	}
	
	
	def viewMap = {
		
		def file = FOLDER +  session.waferId + "\\" + params.f  + ".png" 
		
		get (file, "", 300)
	}
	

	private def get (fileName, fname, xSize){
		
		try {
			Font f = new Font(Font.SANS_SERIF, Font.PLAIN, 10);
			BufferedImage img = ImageIO.read(new File(fileName));
			ImageIO.write(img, "png", response.getOutputStream())
		} catch(Exception exc) {
		
			
		}
	}
}
