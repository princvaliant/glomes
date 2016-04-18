package com.glo.custom

import com.mongodb.BasicDBObject
import org.apache.commons.logging.LogFactory

import javax.imageio.ImageIO
import java.awt.*
import java.awt.image.BufferedImage


class ProbeTestImagesController {

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

            def queryTest =	new BasicDBObject("value.parentCode", params.id)
            queryTest.put("value.tkey", new BasicDBObject('$in', ["ni_dot_test","ito_dot_test", "nw_ito_dot_test"]))

            session.devices  = db.testData.find(queryTest, new BasicDBObject("value.code", 1)).collect{
                [code : it["value"]["code"].tokenize("_")[1]]
            }
            return [code:session.waferId, devices:session.devices]
		}
	}
	
	def view = {
		
		def g = params

        def path = request.getSession().getServletContext().getRealPath("/")
        [4,5,10,20].each {

            def fn = params.code + "_" + params.device + "_" + it + "mA.jpg"
            def file = fileService.retrieveFile(fn)
            if (file) {
                file.writeTo(path + params.code + "_" + params.device + "_" + it + "mA.jpg")
                get (path + params.code + "_" + params.device + "_" + it + "mA.jpg", params.code + "_" + params.device, 500)
            }
        }
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
