package com.glo.custom

import com.glo.ndo.ProductMask
import com.glo.ndo.ProductMaskItem
import com.glo.run.Unit
import com.mongodb.BasicDBObject
import org.apache.commons.logging.LogFactory
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics
import org.apache.poi.xssf.usermodel.XSSFRow
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.grails.plugins.excelimport.ExcelImportUtils

class CouponService {

    private static final logr = LogFactory.getLog(this)

    def mongo
    def unitService
    def historyService
    def workflowService
    def contentService
    def summarizeSyncCurrService

    def splitTestDataToCoupons(db, user, tkey, code, testId) {

        // Check input parameters
        if (tkey != "test_data_visualization")
            return

        if (!code) {
            logr.error("code not specified.")
            return
        }
        // Check if unit exists
        def unit = db.unit.find(new BasicDBObject("code", code), new BasicDBObject()).collect {
            it
        }[0]
        if (!unit) {
            throw new RuntimeException("Wafer for code " + code  + " not found.")
        }
        // Check if mask for unit is specified
        if (!unit.mask) {
            throw new RuntimeException("Mask for this wafer is not defined.")
        }
        // Retrieve mask for this unit
        def productMask = ProductMask.findByName(unit.mask)
       // Retrieve unique coupon codes for this mask
        def productMaskItems = ProductMaskItem.executeQuery("""
							select distinct ps.cpn, substring(ps.code, 1 ,4) from ProductMaskItem as ps where ps.productMask.id = ? and ps.isActive = 1 order by ps.pm
						""", [productMask.id])
        if (!productMaskItems) {
            throw new RuntimeException("This mask has no valid definition.")
        }
        def moveBack = []
        def coupons = []
        productMaskItems.each {
            coupons.add(it)
            def cpnCode = unit.code + "_" + it[0].trim()
            def subUnit = db.unit.find(new BasicDBObject("code", cpnCode), new BasicDBObject()).collect {
                it
            }[0]
//            if (subUnit &&  subUnit.tkey != "test_data_visualization") {
//                // Move coupon to test_data_visualization step and record previous step where it needs to be moved back
//                moveBack.add([_id: subUnit._id, pctg: subUnit.pctg, pkey: subUnit.pkey, tkey: subUnit.tkey ])
//                moveCoupon(subUnit._id , "DVD" , "dvd_assembly" , "test_data_visualization")
//            } else {
//                // Create new coupon and put it in test_data_visualization step
//                unitService.createCoupon(db, unit, cpnCode, user, "DVD", "dvd_assembly", "test_data_visualization", "Test data visualization", "CPN1000")
//            }
        }

        // Split test data from the wafers to coupons
        def bdo = new BasicDBObject()
        bdo.put("value.code", code)
        bdo.put("value.testId", testId)
        bdo.put("value.tkey", "test_data_visualization")
        def testData = db.testData.find(bdo).collect { it.value }[0]
        if (!testData) {
            throw new RuntimeException("Test data not found")
        }

        coupons.each { coupon ->
            def td = [:]
            def codeSize = coupon[0].size()
            td.syncType = testData.syncType
            td.experimentId = testData.experimentId
            td.SWRev = testData.SWRev
            td.code = testData.code + '_' + coupon[0]
            td.testId = testData.testId
            td.tkey = testData.tkey
            td.actarea = testData.actarea
            td.devtype = testData.devtype
            td.devlist = testData.devlist
            td.comment = testData.comment
            td.date = testData.date
            td.images = testData.images
            td.sync = "1"
            td.data = [:]

            def hasData = false;
            testData.data.each { current, currentData ->
                if (current != "setting") {
                    td.data.put(current, [:])
                    currentData.each { parameter, devices ->
                        td.data[current].put(parameter, [:])
                        devices.each { dev, v ->
                            if (dev.substring(0, codeSize) == coupon[1]) {
                                td.data[current][parameter].put(dev, v)
                                hasData = true
                            }
                        }
                    }
                }
            }

            if (hasData == true) {
                def bdo2 = new BasicDBObject()
                bdo2.put("value.code", td.code)
                bdo2.put("value.testId", testId)
                bdo2.put("value.tkey", "test_data_visualization")
                def testCouponData = db.testData.findOne(bdo2)
                if (testCouponData) {
                    db.testData.save([_id: testCouponData._id, value: td])
                } else {
                    db.testData.save([value: td])
                }


                def subUnit = db.unit.find(new BasicDBObject("code", td.code), new BasicDBObject()).collect {
                    it
                }[0]

                bdo2.put("testDataIndex", [])
                if (!subUnit["testDataIndex"]) {
                    bdo2["testDataIndex"].add(td.testId.toString().toLong())
                } else {
                    bdo2["testDataIndex"].addAll(subUnit["testDataIndex"])
                    bdo2["testDataIndex"].add(td.testId.toString().toLong())
                }
                summarizeSyncCurrService.createSummaries(db, subUnit._id, subUnit.code, bdo2, null, null, td.testId.toString().toLong(), td.tkey, unit.mask, null)
            }
        }

        // If necessary move coupons back
//        moveBack.each {
//            moveCoupon(it._id , it.pctg , it.pkey , it.tkey)
//        }
    }

    def moveCoupon(_id, pctg, pkey, tkey) {
        def buf = new Expando()
        buf.isEngineering = true
        buf.prior = 50
        buf.processCategoryEng = pctg
        buf.processKeyEng = pkey
        buf.taskKeyEng = tkey
        buf.units = []
        def n = [:]
        n.put('transition', 'engineering')
        n.put('id', _id)
        buf.units.add(n)
        unitService.move("admin", buf)
    }
}
