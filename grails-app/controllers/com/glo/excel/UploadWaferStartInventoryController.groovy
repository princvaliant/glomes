package com.glo.excel

import org.apache.commons.logging.LogFactory
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.grails.plugins.excelimport.ExcelImportUtils

class UploadWaferStartInventoryController {

	def uploadService
	def mongo
	
	private static final logr = LogFactory.getLog(this)

	static Map CONFIG_UPLOAD_NEW_INVENTORY = [
		sheet:'Sheet1',
		startRow: 0,
		columnMap:  [
			'A':'code',
			'B':'supplier',
			'C':'product',
			'D':'polish',
			'E':'thickness'
		]
	]

    static Map CONFIG_COLUMN_MAP2 = [
            sheet:'Sheet1',
            startRow: 1,
            columnMap:  [
                    'A':'pctg',
                    'B':'pkey',
                    'C':'productCode',
                    'D':'productName',
                    'E':'supplier',
                    'F':'lot',
                    'G':'uom',
                    'H':'qty',
                    'I':'location',
                    'J':'minQty',
                    'K':'maxQty',
                    'L':'note'
            ]
    ]

    static Map CONFIG_COLUMN_MAP3 = [
            sheet:'Sheet1',
            startRow: 1,
            columnMap:  [
                    'A':'runNumber',
                    'B':'code',
                    'C':'recipeName',
                    'D':'Pregrowth NTR',
                    'E':'Core time',
                    'F':'Core Temp',
                    'G':'Core TEGa',
                    'H':'Core NH3',
                    'I':'Core',
                    'J':'UL1 time',
                    'K':'UL1 Temp',
                    'L':'UL1 TEGa',
                    'M':'UL1 comp',
                    'N':'UL1',
                    'O':'UL2 time',
                    'P':'UL2 Temp',
                    'Q':'UL2 TEGa',
                    'R':'UL2 comp',
                    'S':'UL2',
                    'T':'UL3',
                    'U':'Prep time',
                    'V':'Prep Temp',
                    'W':'Prep comp',
                    'X':'Prep total flow',
                    'Y':'Prep',
                    'Z':'QW count',
                    'AA':'QW time',
                    'AB':'QW Temp',
                    'AC':'QW TEGa',
                    'AD':'QW press',
                    'AE':'QW In-III',
                    'AF':'QW total flow',
                    'AG':'QW',
                    'AH':'QW Barriers',
                    'AI':'Sweep',
                    'AJ':'LT cap',
                    'AK':'HT cap',
                    'AL':'Tip rounding',
                    'AM':'Pre-purge',
                    'AN':'AlGaN time',
                    'AO':'AlGaN Temp',
                    'AP':'AlGaN Al-III',
                    'AQ':'AlGaN TMGa',
                    'AR':'AlGaN H2',
                    'AS':'pGaN',
                    'AT':'pPPGaN',
                    'AU':'Spacer'
            ]
    ]

    static Map CONFIG_COLUMN_MAP4 = [
            sheet:'Sheet1',
            startRow: 1,
            columnMap:  [
                    'A':'code',
                    'B':'SEM_length0x0y',
                    'C':'SEM_length0x15y',
                    'D':'SEM_length0x-15y'
            ]
    ]


    static Map CONFIG_COLUMN_MAP5 = [
            sheet:'Sheet1',
            startRow: 1,
            columnMap:  [
                    'B':'importance',
                    'G':'productCode',
                    'H':'productDescription',
                    'K':'productCategory',
                    'N':'supplier1',
                    'O':'vendorCode1',
                    'P':'supplier2',
                    'Q':'vendorCode2',
                    'R':'supplier3',
                    'S':'vendorCode3',
                    'X':'uom',
                    'Y':'minQty',
                    'Z':'maxQty',
                    'AC':'productFamily',
                    'AL':'productComment'
            ]
    ]

	def list = {
	}

    def uploadNewInventory = {

        def f = request.getFile("file")
        try {
            XSSFWorkbook  workbook = new XSSFWorkbook (new ByteArrayInputStream(f.bytes))
            List rows = ExcelImportUtils.convertColumnMapConfigManyRows(workbook, CONFIG_UPLOAD_NEW_INVENTORY )
            def cnt = uploadService.uploadNewInventory(rows)
            flash.message ="Successfully uploaded " + cnt.toString() + " rows"
        }
        catch (Exception exc) {
            flash.message = exc.toString()
        }

        redirect(action: "list")
    }

	def upload2 = {
		
				def f = request.getFile("file")
				try {
					XSSFWorkbook  workbook = new XSSFWorkbook (new ByteArrayInputStream(f.bytes))
		
					List rows = ExcelImportUtils.convertColumnMapConfigManyRows(workbook, CONFIG_COLUMN_MAP2 )
		
					def cnt = uploadService.upload2(rows)
		
					flash.message ="Succesfully uploaded " + cnt + " rows"
				}
				catch (Exception exc) {
					flash.message = exc.toString()
				}
		
				redirect(action: "list")
			}
			
	
	
	def upload3 = {

		def f = request.getFile("file")
		try {
			XSSFWorkbook  workbook = new XSSFWorkbook (new ByteArrayInputStream(f.bytes))

			List rows = ExcelImportUtils.convertColumnMapConfigManyRows(workbook, CONFIG_COLUMN_MAP3 )
			def cnt = uploadService.upload3(rows)

			flash.message ="Succesfully uploaded " + cnt.toString() + " rows"
		}
		catch (Exception exc) {
			flash.message = exc.toString()
		}

		redirect(action: "list")
	}
	
	def upload4 = {
		
		def f = request.getFile("file")
		try {
			XSSFWorkbook  workbook = new XSSFWorkbook (new ByteArrayInputStream(f.bytes))

			List rows = ExcelImportUtils.convertColumnMapConfigManyRows(workbook, CONFIG_COLUMN_MAP4 )
			def cnt = uploadService.upload4(rows)

			flash.message ="Succesfully uploaded " + cnt.toString() + " rows"
		}
		catch (Exception exc) {
			flash.message = exc.toString()
		}

		redirect(action: "list")
	}
	
	def upload5 = {
		
		def f = request.getFile("file")
		try {
			XSSFWorkbook  workbook = new XSSFWorkbook (new ByteArrayInputStream(f.bytes))

			List rows = ExcelImportUtils.convertColumnMapConfigManyRows(workbook, CONFIG_COLUMN_MAP5 )
			def cnt = uploadService.upload5(rows)

			flash.message ="Succesfully uploaded " + cnt.toString() + " rows"
		}
		catch (Exception exc) {
			flash.message = exc.toString()
		}

		redirect(action: "list")
	}


    def upload6 = {

        def f = request.getFile("file")
        try {
            XSSFWorkbook  workbook = new XSSFWorkbook (new ByteArrayInputStream(f.bytes))
            def cnt = uploadService.uploadCustom(workbook)
            flash.message ="Succesfully uploaded " + cnt.toString() + " custom rows"
        }
        catch (Exception exc) {
            flash.message = exc.toString()
        }

        redirect(action: "list")
    }

	
}
