package com.glo.ndo

class ProductController {

	def springSecurityService

	def scaffold = true

	static navigation = [
		group:'admin',
		order:2,
		action:'list',
		title: "navigation.glo.product",
		isVisible: {
			springSecurityService.isLoggedIn() &&
					org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN,ROLE_EQUIPMENT_ADMIN,ROLE_PRODUCT_ADMIN")
		}
	]
	
	def list = {
		
		def productList
		def productTotal
		

		params.max = Math.min(params.max ? params.int('max') : 20, 240)
		params.offset = params.offset ?: 0
		
		def srch = params.q?.trim() ? "%" +  params.q?.trim()+ "%" : "%"
		
		if (!params.companyId) {
			
			productList = Product.executeQuery( """
				select product from Product as product where product.code like ? or product.name like ? 
				""" + (params.sort ? " order by product." + params.sort + " " + params.dir : "")
				, [srch, srch], [max:params.max,offset:params.offset] )
			
			productTotal = Product.executeQuery( """
				select product from Product as product where product.code like ? or product.name like ? 
				"""	, [srch, srch]).size()
		} else {
		
			productList = Product.executeQuery( """
					select distinct pc.product from ProductCompany as pc where pc.company = ? and (pc.product.name like ? or pc.product.code like ?)
					""" + (params.sort ? " order by pc.product." + params.sort + " " + params.dir : "")
				, [Company.get(params.companyId.toLong()),srch, srch], [max:params.max,offset:params.offset] )
			
			productTotal = Product.executeQuery( """
					select distinct pc.product from ProductCompany as pc where pc.company = ? and (pc.product.name like ? or pc.product.code like ?)
					""" 
					, [Company.get(params.companyId.toLong()),srch, srch]).size()
		
		}

		[productInstanceList: productList, productInstanceTotal:productTotal, search:params.q]
	}
	
	def uploadFile = {
		
		def productId = params.productId
	    def f = request.getFile('fileByte')
	    if (f.empty) {
	        flash.message = 'File cannot be empty'
	    } else {
			// Save file
			def productFile = new ProductFile()
			productFile.product = Product.get(productId.toLong())
			productFile.fileBytes = f.bytes
			productFile.name = f.fileItem.name
			productFile.save(failOnError: true)

	    }
		redirect  action:'edit', id:productId
	}
	
	def removeFile = {
		
		ProductFile instance = ProductFile.get(params.productFileId.toLong())
		instance ? instance.delete(flush: true) : false

		redirect  action:'edit', id:params.productId
	}
	
	def downloadFile = {
		
		ProductFile instance = ProductFile.get(params.productFileId.toLong())
	
		if (instance) {
			response.setContentType("application/octet-stream")
			response.setHeader("Content-disposition", "filename=${instance.name}")
			response.outputStream << instance.fileBytes
		 }
	}
}
