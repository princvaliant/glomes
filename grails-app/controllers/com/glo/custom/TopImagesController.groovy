package com.glo.custom

import org.apache.commons.logging.LogFactory

import javax.imageio.ImageIO
import java.awt.*
import java.awt.image.BufferedImage


class TopImagesController {

	private static final logr = LogFactory.getLog(this)
	def fileService
	def mongo 
	def grailsApplication

	def index = {
		
		def db = mongo.getDB("glo")
		def unit = db.unit.find ([code:params.id], ["code":1,"topDevsForTest":1]).collect{it}[0]
		
		if (!unit) {
			return [message:"Wafer ID " + params.id + " does not exists"]
		} else {
		
			session.waferId = unit?.code ?: ""
			session.topDevsForTest = unit?.topDevsForTest ?: []
			
			return [code:session.waferId, tops:session.topDevsForTest, pcms: ["8572","8856","8540","8824","8808"]]
		}
	}
	
	def view = {
		
		def g = params
			
		def FOLDER = grailsApplication.config.glo.TopImagesDirectory ?: "\\\\calserver03\\Test and Applications\\cs_top_images\\"
		def file = FOLDER + params.code + "\\" + params.code + "_" + params.dev + ".jpg" 
		get (file, params.code + "_" + params.dev, 800)
	}
	
	private def get (fileName, fname, xSize){
		
		try {
			Font f = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
			BufferedImage img = ImageIO.read(new File(fileName));
			Image thumbnail = img.getScaledInstance(xSize, -1, Image.SCALE_SMOOTH);
			
			BufferedImage bufferedThumbnail = new BufferedImage(thumbnail.getWidth(null),
				thumbnail.getHeight(null), BufferedImage.TYPE_3BYTE_BGR);
			Graphics g = bufferedThumbnail.getGraphics();
			g.setColor(Color.RED);
			g.setFont(f);
			g.drawImage(thumbnail, 0, 0, null);
			g.drawString(fname, 14, 35);
			g.dispose();
			ImageIO.write(bufferedThumbnail, "png", response.getOutputStream())
		} catch(Exception exc) {
		
			
		}
	}
}
