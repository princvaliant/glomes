package com.glo.custom

import com.glo.ndo.ProductMaskItem
import javax.imageio.ImageIO
import java.awt.*
import java.awt.image.BufferedImage

class UniformityImagesController {

	def fileService
	def mongo 
	def grailsApplication

	def index = {
		
		def db = mongo.getDB("glo")
		def unit = db.unit.find ([code:params.id], ["code":1, "mask":1]).collect{it}[0]
		if (!unit) {
			return [message:"Wafer ID " + params.id + " does not exists"]
		} else if (!unit.mask) {
            return [message:"Wafer ID " + params.id + " has no mask defined"]
        } else {
		
			session.waferId = unit?.code ?: ""
            session.directory = "\\\\calserver03\\Test and Applications\\cs_top_images\\" + session.waferId + "\\uniformity"
            if (params.testId) {
                session.directory += '\\' + params.testId
            } else  {

                def f2 = new File(session.directory).listFiles().grep(~/(?i).*.jpg/).sort{it.lastModified()}
                if (!f2) {
                    def dir = ''
                    new File(session.directory).eachDir {
                        dir = it
                    }
                    session.directory  = dir.path
                }
            }
            session.devices = [:]
            def f1 = new File(session.directory).listFiles().grep(~/(?i).*.jpg/).sort{it.lastModified()}
            f1.each {
                def device = it.name.tokenize("_")[1].replace(".jpg", "")

                def prodMaskItem = ProductMaskItem.executeQuery( """
				    select ps from ProductMaskItem as ps where ps.productMask.name = ? and ps.code = ?
			    """, [unit.mask, device])[0]

                session.devices.put(device, [path:session.directory,x:((prodMaskItem?.plX + 25) * 29.2).toInteger() -150 ,y:((prodMaskItem?.plY + 25) * 19).toInteger() - 60])
            }
			return [code:session.waferId, devices:session.devices]
		}
	}
	
	def view = {
    	get (params.path, params.code + "_" + params.dev, 150)
	}
	
	private def get (path, fname, xSize){
		
		try {
			Font f = new Font(Font.SANS_SERIF, Font.PLAIN, 9);
			BufferedImage img = ImageIO.read(new File(path + "\\" + fname + ".jpg"));
			Image thumbnail = img.getScaledInstance(xSize, -1, Image.SCALE_SMOOTH);
			
			BufferedImage bufferedThumbnail = new BufferedImage(thumbnail.getWidth(null),
				thumbnail.getHeight(null), BufferedImage.TYPE_3BYTE_BGR);
			Graphics g = bufferedThumbnail.getGraphics();
			g.setColor(Color.RED);
			g.setFont(f);
			g.drawImage(thumbnail, 0, 0, null);
			g.drawString(fname, 2, 12);
			g.dispose();
			ImageIO.write(bufferedThumbnail, "png", response.getOutputStream())
		} catch(Exception exc) {
		
			
		}
	}
}
