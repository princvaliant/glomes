package com.glo.run

import grails.converters.JSON
import org.apache.commons.logging.LogFactory
import com.glo.ndo.*

class ProductCompaniesController extends Rest {

	private static final logr = LogFactory.getLog(this)

	def list = {
		logr.debug(params)

		def productCompany
		def product
		
		if (params.product.isLong()) {
			product = Product.get(params.product)
			def company = Company.get(params.company)
			productCompany = ProductCompany.findByProductAndCompany(product, company)
		}
		
		render (['data':  [id: productCompany?.id,vendorCode:productCompany?.vendorCode,price:productCompany?.price,deliveryTime:productCompany?.deliveryTime,uom:product?.uom], 'count':1] as JSON)
	}

	def listByVendorCode = {
		
		logr.debug(params)

		def productCompany = ProductCompany.findByVendorCode(params.vendorCode)
		
		render (['data':  [id: productCompany?.id,productId:productCompany?.product?.id,
						companyId:productCompany?.company?.id], 'count': productCompany ? 1 : 0] as JSON)
	}
}
