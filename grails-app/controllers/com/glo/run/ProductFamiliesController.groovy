package com.glo.run

import com.glo.ndo.ProductFamily
import grails.converters.JSON

class ProductFamiliesController extends Rest {

	def list = {

		def productFamilies = ProductFamily.list()
		render (['data': productFamilies, 'count': productFamilies.size()] as JSON)
	}
}
