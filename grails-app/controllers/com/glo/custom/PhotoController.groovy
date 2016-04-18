package com.glo.custom

import org.apache.commons.logging.LogFactory

import javax.imageio.ImageIO
import java.awt.*
import java.awt.image.BufferedImage

class PhotoController {

	private static final logr = LogFactory.getLog(this)
	def fileService
	def mongo
	
	static def FOLDER = "\\\\calserver03\\Growth\\Characterization\\"
	static def FOLDER2 = "\\\\calserver03\\Characterization\\Linda\\CL\\"

	def index = {
		
		def db = mongo.getDB("glo")
		def unit = db.dataReport.find ([code:params.id], [:]).collect{it}[0]
		
		if (!unit) {
			return [message:"Wafer ID " + params.id + " does not exists"]
		} else {
		
			session.waferId = unit?.code ?: ""
			session.runNumber =  unit?.value?.pl?.runNumber?.toUpperCase() ?: (unit?.value?.epi_growth?.runNumber?.toUpperCase() ?: "")
			session.runDate =  unit?.value?.pl?.actualStart
			session.reactor =  session.runNumber?.substring(0,2)
			
			def epiRun = db.epiRun.find ([runNumber:session.runNumber], [note:1]).collect{it}[0]
			def note = epiRun?.note?.replace("\n", "<br/>")
			
			
			return [code:session.waferId,
				runNumber: session.runNumber,
				pitch:unit?.value?.nil?.pitch,
				note:note]
		}
	}
	
	def viewSem = {
		
		def g = params
			
		def file = FOLDER + "SEM\\" + session.reactor + "\\" + session.runNumber + "\\"
		file += session.runNumber + "_" + session.waferId + "_50k_30deg_"
		switch (params.orient) {
		
			case "CENTER":
				file += "0x0y.jpg"
				break
			case "TOP":
				file += "0x15y.jpg"
				break
			case "BOTTOM":
			file += "0x-15y.jpg"
				break
		}
		
		get (file, "", 300)
	}
	
	def viewPl = {
		
		def g = params
		
		def calendar = new GregorianCalendar(2013,8,11)
		def date = calendar.getTime()
					
		def file = FOLDER + "PL\\" + session.runNumber + "\\"
		
		def orient = params.orient
		
		if (orient == "spm_1.BMP" || orient == "spm_2.BMP") {
			
			if (date > session.runDate)
				orient = "_01" + orient
				
		} else if (orient == ".BMP" ) {
			
			if (date > session.runDate)
				orient = "_01" + orient
		}
		
		file += session.runNumber + "_" + session.waferId + orient
		
		get (file, "", params.sz.toInteger())
	}
	
	def viewCl = {
		
		def g = params
			
		def file = FOLDER2 + session.runNumber
		try {
			File dir = new File(file)
			dir.eachFile { f ->
				
				if (params.orient == "1" && f.name.indexOf(session.waferId + "_SerialCL") >= 0)
					get (file + "\\" + f.name, f.name.replace(session.runNumber + "_" + session.waferId + "_SerialCL", ""), 420)
				if (params.orient == "2" && f.name.indexOf(session.waferId + "_MonoCL_1_") >= 0)
					get (file + "\\" + f.name, f.name.replace(session.runNumber + "_" + session.waferId + "_MonoCL_1_", ""),  220)
				if (params.orient == "3" && f.name.indexOf(session.waferId + "_MonoCL_2_") >= 0)
					get (file + "\\" + f.name, f.name.replace(session.runNumber + "_" + session.waferId + "_MonoCL_2_", ""),  220)
				if (params.orient == "4" && f.name.indexOf(session.waferId + "_MonoCL_3_") >= 0)
					get (file + "\\" + f.name, f.name.replace(session.runNumber + "_" + session.waferId + "_MonoCL_3_", ""),  220)
			}
		} catch (Exception exc) {
		
		}
	}
	
	private def get (fileName, fname, xSize){
		
		try {
			Font f = new Font(Font.SANS_SERIF, Font.PLAIN, 10);
			BufferedImage img = ImageIO.read(new File(fileName));
			Image thumbnail = img.getScaledInstance(xSize, -1, Image.SCALE_SMOOTH);
			
			BufferedImage bufferedThumbnail = new BufferedImage(thumbnail.getWidth(null),
				thumbnail.getHeight(null), BufferedImage.TYPE_3BYTE_BGR);
			Graphics g = bufferedThumbnail.getGraphics();
			g.setColor(Color.RED);
			g.setFont(f);
			g.drawImage(thumbnail, 0, 0, null);
			g.drawString(fname, 2, 195);
			g.dispose();
			ImageIO.write(bufferedThumbnail, "png", response.getOutputStream())
		} catch(Exception exc) {
		
			
		}
	}
}
