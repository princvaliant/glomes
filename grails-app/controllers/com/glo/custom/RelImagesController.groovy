package com.glo.custom

import com.glo.ndo.ProductMaskItem

import javax.imageio.ImageIO
import java.awt.*
import java.awt.image.BufferedImage

class RelImagesController {

	def fileService
	def mongo 
	def grailsApplication

	def index = {
		
	    session.WaferID = params.WaferID
        session.HourSet = params.HourSet
        session.directory = "\\\\calserver03\\Test and Applications\\cs_top_images\\" + session.WaferID + "\\waferrel\\" + params.SID
        session.devices = [:]

        def f1 = new File(session.directory).listFiles().grep(~/(?i).*.jpg/).sort{it.name}
        def x = 20, y = 100
        f1.each {
            def device = it.name.tokenize("_")[1].replace(".jpg", "")
            session.devices.put(device, [path:session.directory,x:x ,y:y])
            x += 151
            if (x > 800) {
                x = 20
                y += 131
            }
        }
	    return [code:session.WaferID, devices:session.devices, hour: params.HourSet]
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
