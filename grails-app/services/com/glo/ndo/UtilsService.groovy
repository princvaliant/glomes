package com.glo.ndo

import com.mongodb.BasicDBObject
import org.apache.commons.logging.LogFactory
import org.apache.poi.xssf.usermodel.*

class UtilsService {
	
	def mongo
	
	private static final logr = LogFactory.getLog(this)

	def millisToShortDHMS (Long duration, Date actualStart) {
		
		if (duration == null) {
			duration = (new Date().getTime() - actualStart.getTime()) / 1000
		}
		
		if (duration > 0) {
			
			int days = duration/86400
			int hours = (duration % 86400) / 3600
			int minutes = (duration % 86400 % 3600) / 60
			if (days == 0 && hours == 0 && minutes == 0) minutes = 1
			String.format("%02d:%02d:%02d", days, hours, minutes)
		} else {
			""
		}
	}

	def exportExcel (List objList, String formatting, def cols) {
		
		XSSFWorkbook workbook = new XSSFWorkbook()
		XSSFSheet sheet = workbook.createSheet("mesdata")
		
		if (objList) {
			XSSFCellStyle style = workbook.createCellStyle()
			XSSFCreationHelper createHelper = workbook.getCreationHelper();
			style.setDataFormat(createHelper.createDataFormat().getFormat("MM/dd/yyyy"))

			//NORMAL
			if (!formatting.contains('x')) {
				XSSFRow rowHeader = sheet.createRow(0)
				def h = 0
                if (!cols) {
                    objList[0].each { name, value ->
                        XSSFCell cellHead = rowHeader.createCell((int) h)
                        cellHead.setCellValue(new XSSFRichTextString(name))
                        h++
                    }
                } else {
                    cols.each { name ->
                        XSSFCell cellHead = rowHeader.createCell((int) h)
                        cellHead.setCellValue(new XSSFRichTextString(name))
                        h++
                    }
                }
				//objList[0].keySet().each {
				//	cellHead.setCellValue(new XSSFRichTextString(it))
		
				def r = 1
				objList.each { obj ->
									
					XSSFRow rowData = sheet.createRow(r)
					def c = 0
                    if (!cols) {
                        obj.each { name, value ->
                            XSSFCell cellData = rowData.createCell((int) c)
                            if (value != null) cellData.setCellValue(value)
                            if (name.toLowerCase().contains("date")) {
                                cellData.setCellStyle(style)
                            }
                            c++
                        }
                    } else {
                        cols.each { name ->
                            XSSFCell cellData = rowData.createCell((int) c)
                            def value = obj[name]
                            if (value != null) cellData.setCellValue(value)
                            if (name.toLowerCase().contains("date")) {
                                cellData.setCellStyle(style)
                            }
                            c++
                        }
                    }
					r++
				}
			}
			
			// TRANSPOSE
			else {
				//put map keys in the first column in the spreadsheet
				int r = 0
				objList[0].keySet().each {
					XSSFRow rowData = sheet.createRow(r)
					XSSFCell cellData = rowData.createCell(0)
					cellData.setCellValue(new XSSFRichTextString(it))
					r++
				}
				
				//put map values in other columns in the spreadsheet
				int c = 1
				objList.each { obj ->
					r = 0
					obj.each { k, v ->
						XSSFRow rowData = sheet.getRow(r)
						XSSFCell cellData = rowData.createCell(c)
						if (v != null ) cellData.setCellValue(v)
						r++
					}
					c++
				}
			}

			//auto-size all columns
			XSSFRow rowData = sheet.getRow(0)
			int lastColumn = rowData.getLastCellNum()
			for (int colNum = 0; colNum < lastColumn; colNum++)  sheet.autoSizeColumn(colNum)
		}
		
		return(workbook)
	}

}