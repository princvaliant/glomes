package com.glo.excel

import org.apache.commons.logging.LogFactory
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.grails.plugins.excelimport.ExcelImportUtils

class UploadWaferStartInventoryController {

	def uploadService
	def mongo
	
	private static final logr = LogFactory.getLog(this)

	static Map CONFIG_UPLOAD_SANAN = [
		sheet:'Sheet1',
        startRow: 3,
         columnMap:  [
                    'A':'cassetteId',
                    'B':'cassetteSlot',
                    'C':'code',
                    'D':'thickness',
                    'E':'tsmStatisticsAverage',
                    'F':'tsmStatisticsUniformity',
                    'G':'XRD_002FWHM',
                    'H':'XRD_102FWHM',
                    'I':'bow',
                    'J':'supplier'
            ]
	]

	static Map CONFIG_UPLOAD_GENERIC_BARE = [
		sheet:'Sheet1',
		startRow: 1,
		columnMap:  [
			'A':'code',
			'B':'supplier',
			'C':'product',
			'D':'qty',
			'E':'polish',
			'F':'cassetteId',
			'G':'cassetteSlot'
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

    static Map CONFIG_UPLOAD_UNID = [
            sheet:'Data 2',
            startRow: 4,
            columnMap:  [
                    'A':'cassetteId',
                    'B':'cassetteSlot',
                    'C':'code',
                    'D':'thickness',
                    'E':'tsmStatisticsAverage',
                    'F':'tsmStatisticsUniformity',
                    'G':'XRD_002FWHM',
                    'H':'XRD_102FWHM',
                    'I':'bow',
                    'J':'supplier'
            ]
    ]

    static Map CONFIG_UPLOAD_EPI_SANAN = [
            sheet:'Certificate of lnspection',
            startRow: 8,
            columnMap:  [
                    'C':'code',
                    'E':'vfavg',
                    'F':'lvavg',
                    'G':'wavelength',
                    'H':'area',
                    'K':'substratefactories'
            ]
    ]

    static Map CONFIG_UPLOAD_OEM_PACKAGE_TEST_DATA = [
            sheet:'Full brightness',
            startRow: 5,
            columnMap:  [
                    'A':'supplier',
                    'B':'product',
                    'C':'code',
                    'D':'nits_mean_full',
                    'E':'nits_center_full',
                    'F':'uniformity_full',
                    'G':'hotspot_full',
                    'H':'ciex_ center_full',
                    'I':'ciey_ center_full',
                    'J':'delt_x_full',
                    'K':'delt_y_full',
                    'L':'ciex_ center_half',
                    'M':'ciey_ center_half',
                    'N':'uniformity_half',
                    'O':'sensor_45deg',
                    'P':'i_r',
                    'Q':'i_g',
                    'R':'i_b',
                    'S':'v_r',
                    'T':'v_g',
                    'U':'v_b',
                    'V':'power',
                    'W':'nit_1',
                    'X':'nit_2',
                    'Y':'nit_3',
                    'Z':'nit_4',
                    'AA':'nit_5',
                    'AB':'nit_6',
                    'AC':'nit_7',
                    'AD':'nit_8',
                    'AE':'nit_9',
                    'AF':'nit_10',
                    'AG':'nit_11',
                    'AH':'nit_12',
                    'AI':'nit_13',
                    'AJ':'nits_mean',
                    'AK':'uniformity_13',
                    'AL':'cx_1',
                    'AM':'cx_2',
                    'AN':'cx_3',
                    'AO':'cx_4',
                    'AP':'cx_5',
                    'AQ':'cx_6',
                    'AR':'cx_center',
                    'AS':'cx_8',
                    'AT':'cx_9',
                    'AU':'cx_10',
                    'AV':'cx_11',
                    'AW':'cx_12',
                    'AX':'cx_13',
                    'AY':'cy_1',
                    'AZ':'cy_2',
                    'BA':'cy_3',
                    'BB':'cy_4',
                    'BC':'cy_5',
                    'BD':'cy_6',
                    'BE':'cy_center',
                    'BF':'cy_8',
                    'BG':'cy_9',
                    'BH':'cy_10',
                    'BI':'cy_11',
                    'BJ':'cy_12',
                    'BK':'cy_13',
                    'BL':'nit69_1',
                    'BM':'nit69_2',
                    'BN':'nit69_3',
                    'BO':'nit69_4',
                    'BP':'nit69_5',
                    'BQ':'nit69_6',
                    'BR':'nit69_7',
                    'BS':'nit69_8',
                    'BT':'nit69_9',
                    'BU':'nit69_10',
                    'BV':'nit69_11',
                    'BW':'nit69_12',
                    'BX':'nit69_13',
                    'BY':'nit69_14',
                    'BZ':'nit69_15',
                    'CA':'nit69_16',
                    'CB':'nit69_17',
                    'CC':'nit69_18',
                    'CD':'nit69_19',
                    'CE':'nit69_20',
                    'CF':'nit69_21',
                    'CG':'nit69_22',
                    'CH':'nit69_23',
                    'CI':'nit69_24',
                    'CJ':'nit69_25',
                    'CK':'nit69_26',
                    'CL':'nit69_27',
                    'CM':'nit69_28',
                    'CN':'nit69_29',
                    'CO':'nit69_30',
                    'CP':'nit69_31',
                    'CQ':'nit69_32',
                    'CR':'nit69_33',
                    'CS':'nit69_34',
                    'CT':'nit69_35',
                    'CU':'nit69_36',
                    'CV':'nit69_37',
                    'CW':'nit69_38',
                    'CX':'nit69_39',
                    'CY':'nit69_40',
                    'CZ':'nit69_41',
                    'DA':'nit69_42',
                    'DB':'nit69_43',
                    'DC':'nit69_44',
                    'DD':'nit69_45',
                    'DE':'nit69_46',
                    'DF':'nit69_47',
                    'DG':'nit69_48',
                    'DH':'nit69_49',
                    'DI':'nit69_50',
                    'DJ':'nit69_51',
                    'DK':'nit69_52',
                    'DL':'nit69_53',
                    'DM':'nit69_54',
                    'DN':'nit69_55',
                    'DO':'nit69_56',
                    'DP':'nit69_57',
                    'DQ':'nit69_58',
                    'DR':'nit69_59',
                    'DS':'nit69_60',
                    'DT':'nit69_61',
                    'DU':'nit69_62',
                    'DV':'nit69_63',
                    'DW':'nit69_64',
                    'DX':'nit69_65',
                    'DY':'nit69_66',
                    'DZ':'nit69_67',
                    'EA':'nit69_68',
                    'EB':'nit69_69',
                    'EC':'uniformity69_min'
            ]
    ]

    static Map CONFIG_UPLOAD_LIGHTBAR = [
            sheet:'Sheet1',
            startRow: 1,
            columnMap:  [
                    'A':'supplier',
                    'B':'product',
                    'C':'testType',
                    'D':'buildNumber',
                    'E':'code',
                    'F':'pcb',
                    'G':'green',
                    'H':'blue',
                    'I':'red',
                    'J':'whiteCieX',
                    'K':'whiteCieY',
                    'L':'whitePhotometric',
                    'M':'whiteCCTK',
                    'N':'greenVoltage',
                    'O':'blueVoltage',
                    'P':'redVoltage',
                    'Q':'greenCurrent',
                    'R':'blueCurrent',
                    'S':'redCurrent',
                    'T':'whiteElectricPower',
                    'U':'luminousEfficacy',
                    'V':'thickness'
            ]
    ]



    static Map CONFIG_UPLOAD_PACKAGE = [
            sheet:'Sheet1',
            startRow: 1,
            columnMap:  [
                    'A':'supplier',
                    'B':'product',
                    'C':'code',
                    'D':'stepIn',
                    'E':'lgp',
                    'F':'red',
                    'G':'green',
                    'H':'greenglo',
                    'I':'blue'
            ]
    ]



    static Map CONFIG_UPLOAD_PACKAGE_DATA = [
            sheet:'Sheet1',
            startRow: 1,
            columnMap:  [
                    'A':'code',
                    'B':'purpose',
                    'C':'testedAt',
                    'D':'product',
                    'E':'iblu_assembly',
                    'F':'loopType',
                    'G':'lgp',
                    'H':'die_attach_iblu',
                    'I':'red',
                    'J': 'green',
                    'K': 'blue',
                    'L': 'wirebond_iblu',
                    'M': 'mold_prep',
                    'N': 'encapsulation_iblu',
                    'O': 'saw_singulate',
                    'P': 'fpc_attach',
                    'Q': 'fpc',
                    'R': 'epoxy_cure',
                    'S': 'ilgp_assembly ',
                    'U': 'sfp',
                    'V': 'dummy',
                    'W': 'esr',
                    'X': 'defusor',
                    'Y': 'bbef',
                    'Z': 'tbef' ,
                    'AA' : 'bwtape'
            ]
    ]


	def list = {
	}

    def uploadOemPackageTestData = {

        def f = request.getFile("file")
        try {
            XSSFWorkbook  workbook = new XSSFWorkbook (new ByteArrayInputStream(f.bytes))

            List rows = ExcelImportUtils.convertColumnMapConfigManyRows(workbook, CONFIG_UPLOAD_OEM_PACKAGE_TEST_DATA )
            def cnt = uploadService.uploadOemPackageTestData(rows)

            flash.message ="Successfully uploaded " + cnt.toString() + " rows"
        }
        catch (Exception exc) {
            flash.message = exc.toString()
        }

        redirect(action: "list")
    }

    def uploadOemPackage = {

        def f = request.getFile("file")
        try {
            XSSFWorkbook  workbook = new XSSFWorkbook (new ByteArrayInputStream(f.bytes))

            List rows = ExcelImportUtils.convertColumnMapConfigManyRows(workbook, CONFIG_UPLOAD_PACKAGE )
            def cnt = uploadService.uploadOemPackage(rows)

            flash.message ="Successfully uploaded " + cnt.toString() + " rows"
        }
        catch (Exception exc) {
            flash.message = exc.toString()
        }

        redirect(action: "list")
    }

    def uploadPackageData = {

        def f = request.getFile("file")
        try {
            XSSFWorkbook  workbook = new XSSFWorkbook (new ByteArrayInputStream(f.bytes))

            List rows = ExcelImportUtils.convertColumnMapConfigManyRows(workbook, CONFIG_UPLOAD_PACKAGE_DATA )
            def cnt = uploadService.uploadPackageData(rows)

            flash.message ="Successfully uploaded " + cnt.toString() + " rows"
        }
        catch (Exception exc) {
            flash.message = exc.toString()
        }

        redirect(action: "list")
    }

    def uploadLightBar = {

        def f = request.getFile("file")
        try {
            XSSFWorkbook  workbook = new XSSFWorkbook (new ByteArrayInputStream(f.bytes))

            List rows = ExcelImportUtils.convertColumnMapConfigManyRows(workbook, CONFIG_UPLOAD_LIGHTBAR )
            def cnt = uploadService.uploadLightBar(rows)

            flash.message ="Successfully uploaded " + cnt.toString() + " rows"
        }
        catch (Exception exc) {
            flash.message = exc.toString()
        }

        redirect(action: "list")
    }


    def uploadNganSanan = {

		def f = request.getFile("file")
		try {
			XSSFWorkbook  workbook = new XSSFWorkbook (new ByteArrayInputStream(f.bytes))

			List rows = ExcelImportUtils.convertColumnMapConfigManyRows(workbook, CONFIG_UPLOAD_SANAN)
			def cnt = uploadService.uploadNgan(rows, "Sanan")

			flash.message ="Succesfully uploaded " + cnt + " rows"
		}
		catch (Exception exc) {
			flash.message = exc.toString()
		}

		redirect(action: "list")
	}

    def uploadEpiSanan = {

        def f = request.getFile("file")
        try {
            XSSFWorkbook  workbook = new XSSFWorkbook (new ByteArrayInputStream(f.bytes))

            List rows = ExcelImportUtils.convertColumnMapConfigManyRows(workbook, CONFIG_UPLOAD_EPI_SANAN )
            def cnt = uploadService.uploadEpiSanan(rows, "Sanan")

            flash.message ="Succesfully uploaded " + cnt + " rows"
        }
        catch (Exception exc) {
            flash.message = exc.toString()
        }

        redirect(action: "list")
    }

    def uploadNganUnid = {

        def f = request.getFile("file")
        try {
            XSSFWorkbook  workbook = new XSSFWorkbook (new ByteArrayInputStream(f.bytes))

            List rows = ExcelImportUtils.convertColumnMapConfigManyRows(workbook, CONFIG_UPLOAD_UNID )
            def cnt = uploadService.uploadNgan(rows, "UNID" )

            flash.message ="Successfully uploaded " + cnt + " rows"
        }
        catch (Exception exc) {
            flash.message = exc.toString()
        }

        redirect(action: "list")
    }

	def uploadBare = {

		def f = request.getFile("file")
		try {
			XSSFWorkbook  workbook = new XSSFWorkbook (new ByteArrayInputStream(f.bytes))

			List rows = ExcelImportUtils.convertColumnMapConfigManyRows(workbook, CONFIG_UPLOAD_GENERIC_BARE )

			def cnt = uploadService.uploadBare(rows)

			flash.message ="Succesfully uploaded " + cnt + " rows"
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

	
}
