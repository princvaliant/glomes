package com.glo.custom

import com.glo.ndo.Product
import com.mongodb.BasicDBObject
import org.apache.poi.xssf.usermodel.XSSFWorkbook

import javax.servlet.ServletOutputStream

class InventoryReportController {
	
	def mongo
	def utilsService
	
	def index = {
		
		def rName = params.rName ?: "Bulk_Inventory"
		def mode = params.mode ?: "product"
		
		def db = mongo.getDB("glo")
		def results = []

		def products = Product.findAllByIsBulkAndDisabled(true, false)
		def products1 = Product.findAllByIsBulkAndDisabled(true, null)
		products.addAll(products1)
		
		products.each { product ->
			def query = new BasicDBObject()
			query.put('pkey', 'inventory')
			query.put('productCode', product.code)
			//query.put('supplier', vendor.company.name)
			def units = db.unit.find(query).collect { [it.tkey, it.supplier, it.qtyOut ?: 0] }
			
			def result = [:]
			result.productCode = product.code
			result.revision = product.revision
			result.productName = product.name
			result.minQty = product.minQty
			result.maxQty = product.maxQty
			
			def totalQty = units.collect { it[2] }.sum() ?: 0
			result.totalQtyOO = units.findAll { it[0] == 'inventory_on_order' }.collect { it[2] }.sum() ?: 0
			result.totalQtyOH = totalQty - result.totalQtyOO
			
			if (mode == "product") {
				result.inventoryAlert = ""
				if (result.minQty && totalQty <= result.minQty) {
					result.inventoryAlert = "Need to order"
				} else if (result.minQty && totalQty <= result.minQty * 1.2) {
					result.inventoryAlert = "Inventory low (within 20% of min)"
				} else if (totalQty <= 0) {
					result.inventoryAlert = "Need to order"
				}
				
				result.needLimits = ""
				if (result.minQty == null)
					result.needLimits += "minQty "
				if (result.maxQty == null)
					result.needLimits += "maxQty "
			}
			result.usedFor = product.usedFor
			
			def incl = true
			product.productCompanies.each { vendor->
				def vendorName = vendor.company?.name ?: ""
				if (vendorName in ["GLO", "Glo-USA"]) {
					incl = false
				}
				else if (mode != "product") {
					def supplierQty = units.findAll { it[1] == vendorName }.collect { it[2] }.sum() ?: 0
					if ((!vendor.disabled) || supplierQty != 0) {
						def result1 = [:]
						result1.productCode = product.code
						result1.revision = product.revision
						result1.productName = product.name
						//result1.minQty = product.minQty
						//result1.maxQty = product.maxQty
						//result1.usedFor = product.usedFor
						//result1.totalQty = result.totalQty
			
						result1.supplier = vendorName
						if (vendor.disabled) result1.supplier += " (disabled!)"
						result1.supplierCode = vendor.vendorCode
						
						result1.supplierQtyOO = units.findAll { it[0] == 'inventory_on_order' && it[1] == vendorName }.collect { it[2] }.sum() ?: 0
						result1.supplierQtyOH = supplierQty - result1.supplierQtyOO
						result1.price = vendor.price ?: ""
						result1.deliveryTime = vendor.deliveryTime ?: ""
						results.add(result1)
					}
				}
			}
			
			if (mode == "product" && incl)
				results.add(result)
		}
		
		XSSFWorkbook workbook = utilsService.exportExcel(results, "")
		
		def fheader = "attachment; filename=" + rName + ".xlsx"
		response.setHeader("Content-disposition", fheader)
		response.contentType = "application/excel"
		ServletOutputStream f = response.getOutputStream()
		workbook.write(f)
		f.close()

		//render (results as JSON)
	}
}