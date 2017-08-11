package com.glo.custom

import com.glo.ndo.ProductMaskItem
import com.mongodb.BasicDBObject
import com.sun.org.apache.xerces.internal.impl.xpath.regex.Match
import grails.converters.JSON
import org.apache.commons.logging.LogFactory
import org.apache.poi.xssf.usermodel.XSSFCell
import org.apache.poi.xssf.usermodel.XSSFRichTextString
import org.apache.poi.xssf.usermodel.XSSFRow
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook

import javax.servlet.ServletOutputStream

class ProbeTestController extends com.glo.run.Rest {
	
	private static final logr = LogFactory.getLog(this)

	def springSecurityService
	def fileService
    def utilsService

	def mongo

	def xText = [:]
	def yText = [:]
	
	def getDevices = {

		def username =  springSecurityService.principal?.username
		
		def db = mongo.getDB("glo")
		
		def queryTest =	new BasicDBObject("value.parentCode", params.code)
		queryTest.put("value.tkey", params.tkey)		
		
		def ret = db.testData.find(queryTest, new BasicDBObject("value.code", 1)).collect{
			[name : it["value"]["code"].tokenize("_")[1]]
		}
		
		render ret as JSON
	}
	
	def get = {
		
		logr.debug(params)
		
		def username =  springSecurityService.principal?.username
		
		def db = mongo.getDB("glo")
		
		def queryTest =	new BasicDBObject("value.code", params.code + "_" + params.device)
		queryTest.put("value.tkey", params.tkey)
		
		def ret = db.testData.find(queryTest, new BasicDBObject()).collect{it}
		
		def exp = [:]
		def path = request.getSession().getServletContext().getRealPath("/")
		['0_1', '0_2','0_4','0_6','0_8', '0.2','0.4','0.6','0.8', '1', '2', '4','5','10'].each {
			def fn = params.code + "_" + params.device + "_" + it + "mA.jpg"
			def file = fileService.retrieveFile(fn)
			if (file) {
				file.writeTo(path + params.code + "_" + params.device + "_" + it + "mA.jpg")
			}
            def fn1 = params.code + "_" + params.device + "_" + it + "mA.png"
            def file1 = fileService.retrieveFile(fn)
            if (file1) {
                file1.writeTo(path + params.code + "_" + params.device + "_" + it + "mA.png")
            }
        }
        if (ret.value.data && ret.value.data["Data @ 1mA"] && ret.value.data["Data @ 1mA"]["Peak (nm)"] &&
                ret.value.data["Data @ 1mA"]["Peak (nm)"][0]) {
            def pwl = ret.value.data["Data @ 1mA"]["Peak (nm)"][0].toFloat();
            if (pwl > 570) {
                exp.put("image", params.code + "_" + params.device + "_1mA.png")
            } else {
                exp.put("image", params.code + "_" + params.device + "_0.6mA.png")
            }
        } else {
            exp.put("image", params.code + "_" + params.device + "_1mA.png")
        }
        exp.put('extens', 'png')

        exp.put("data", [:])

        def spectrums = [:]
        [100, 200, 400, 600, 800, 1, 2, 4, 5, 10].each { curr ->
            def cnt = 0
            def currStr = ''
            if (curr < 50) {
                currStr = curr + "mA"
            } else {
                currStr = curr + "uA"
            }

            exp["data"].put("Peak (nm) @ " + currStr, ret.value.data["Data @ " + currStr]["Peak (nm)"][0])

            ret.value.data["Data @ " + currStr].Spectrum.data[0].collect {
                if (!spectrums.containsKey(it[0]))
                    spectrums.put(it[0], [:])
                if (cnt % 3 == 0) {
                    spectrums[it[0]].put("Intens_" + currStr,it[1])
                }
                cnt++
            }
        }

		ret["value"][0].each { k, v ->  
			
			if (k != "data" && v) {
				exp["data"].put(k, v)
			}	
		}
		
		def dataVoltages = []
		ret.value.data.Datavoltage.data[0].each {  
			if (it[1] >= 0 && it[2] >= 0) 
				dataVoltages.add(
					[volt:it[0].round(2),current: Math.log10(1000 * it[1]),currCorrected: Math.log10(it[2])]
				)
		}
		exp.put("Datavoltage", dataVoltages)
		
		def dataCurrents = []
        def max = 0;
		ret.value.data.Datacurrent.data[0].each {
            if (it[3] >= 0 && it[3] < 30 && it[6] >= 0 && it[6] < 30 &&  it[7] >= 0 && it[7] < 30 && it[8] >= 0 && it[8] < 30) {
                dataCurrents.add([current: it[0].round(3), wpe: it[3], wpec: it[6], eqe: it[7], eqec: it[8]])
                max =  Math.max(max, Math.max((double)Math.max((double)it[3], (double)it[6]), (double)Math.max((double)it[7], (double)it[8])))
            }
		}

		exp.put("Datacurrent", dataCurrents)
        exp.put("DatacurrentMax", max * 1.01)

		def arr = []
		spectrums.each { k, v -> 
			def map = [:]
			map.put("w", k)
			v.each { k1, v1 -> 
				map.put(k1, v1* 1000000000)
			}
			arr.add(map)
		}
		exp.put("Dataspectrum", arr)
		

		render (exp as JSON)
	}

    def exportData = {

        def code = params.code
        def codes = params.codes.tokenize(",")
        def tkey = params.tkey
        def arr = []
        codes.each {
            arr.add(getProp(code, it, tkey))
        }

        def cols = [
                'parentCode',
                'code',
                'tkey',
                'date',
                'rpp',
                'vbp',
                'v02',
                'wpe02',
                'eqe02',
                'v04',
                'wpe04',
                'eqe04',
                'v06',
                'wpe06',
                'eqe06',
                'v08',
                'wpe08',
                'eqe08',
                'v1',
                'wpe1',
                'eqe1',
                'v2',
                'wpe2',
                'eqe2',
                'v4',
                'wpe4',
                'eqe4',
                'v5',
                'wpe5',
                'eqe5',
                'Peak WPE (%)',
                'Peak EQE (%)',
                'J @ Peak WPE (A/cm2)',
                'J @ Peak EQE (A/cm2)',
                'EQE leak WL corr @ 1 mA',
                'EQE leak WL corr @ 4 mA',
                'EQE leak WL corr @ 5 mA',
                'Peak (nm) @ 200uA',
                'Peak (nm) @ 400uA',
                'Peak (nm) @ 600uA',
                'Peak (nm) @ 800uA',
                'Peak (nm) @ 1mA',
                'Peak (nm) @ 2mA',
                'Peak (nm) @ 4mA',
                'Peak (nm) @ 5mA']



        XSSFWorkbook workbook = utilsService.exportExcel(arr, "", cols)
        response.setHeader("Content-disposition", "attachment; filename=NiDot_" + params.code + ".xlsx")
        response.contentType = "application/excel"
        ServletOutputStream f = response.getOutputStream()
        workbook.write(f)
        f.close()
    }

    private def getProp(code, device, tkey) {

        def db = mongo.getDB("glo")

        def queryTest =	new BasicDBObject("value.code", code + "_" + device)
        queryTest.put("value.tkey", tkey)
        def ret = db.testData.find(queryTest, new BasicDBObject()).collect{it}

        def data = [:]
        ret["value"][0].each { k, v ->
            if (k != "data" && v) {
                if (k == "code") {
                    v = v.tokenize("_")[1]
                }
                data.put(k, v)
            }
        }
        [100, 200, 400, 600, 800, 1, 2, 4, 5, 10].each { curr ->
            def cnt = 0
            def currStr = ''
            if (curr < 50) {
                currStr = curr + "mA"
            } else {
                currStr = curr + "uA"
            }
            data.put("Peak (nm) @ " + currStr, ret.value.data["Data @ " + currStr]["Peak (nm)"][0])
        }
        return data
    }
}
