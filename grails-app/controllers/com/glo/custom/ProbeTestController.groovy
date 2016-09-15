package com.glo.custom

import com.mongodb.BasicDBObject
import com.sun.org.apache.xerces.internal.impl.xpath.regex.Match
import grails.converters.JSON
import org.apache.commons.logging.LogFactory

class ProbeTestController extends com.glo.run.Rest {
	
	private static final logr = LogFactory.getLog(this)

	def springSecurityService
	def fileService

	def mongo

	def xText = [:]
	def yText = [:]
	
	def getDevices = {
		
		logr.debug(params)
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
		['0_2','0_4','0_6','0_8', '0.2','0.4','0.6','0.8', '1', '4','5','10'].each {
			
			def fn = params.code + "_" + params.device + "_" + it + "mA.jpg"
			def file = fileService.retrieveFile(fn)
			if (file) {
				file.writeTo(path + params.code + "_" + params.device + "_" + it + "mA.jpg")
				exp.put("image", params.code + "_" + params.device + "_" + it + "mA.jpg")
                exp.put('extens', 'jpg')
			}
            def fn1 = params.code + "_" + params.device + "_" + it + "mA.png"
            def file1 = fileService.retrieveFile(fn)
            if (file1) {
                file1.writeTo(path + params.code + "_" + params.device + "_" + it + "mA.png")
                exp.put("image", params.code + "_" + params.device + "_" + it + "mA.png")
                exp.put('extens', 'png')
            }
        }
        exp.put("data", [:])

        def spectrums = [:]
        [200, 400, 600, 800, 1, 4, 5, 10].each { curr ->
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
}
