package com.glo.run

import grails.converters.JSON
import org.apache.commons.logging.LogFactory
import com.glo.ndo.*

class ProductsController extends Rest {

	private static final logr = LogFactory.getLog(this)

	def list = {
		

		def products = []

		if (params.pctg && params.pkey) {
			
			def process = Process.findByCategoryAndPkey(params.pctg, params.pkey)
			
			if (params.query) {
				
				products= Product.executeQuery( """
					select product from Product as product where product.startProcess = ? and product.disabled = false and (product.name like ? or product.code like ?) 
				    order by product.name """, [process, "%" + params.query + "%",  "%" + params.query + "%"])

			} else {
			
				products= Product.executeQuery( """
						select product from Product as product where product.startProcess = ? and product.disabled = false  
					    order by product.name """, [process])
			}
		} else if (params.productFamily) {

            products = Product.executeQuery( """
					select product from Product as product where product.productFamily = ?
				    order by product.name """, [ProductFamily.get(params.productFamily)])

        } else if (params.isBom) {
		
			Bom.list().each {
				products.add([id:it.id, code:it.assemblyProduct.code, revision: it.revision, name: it.assemblyProduct.name, startProcess: it.assemblyProduct.startProcess.pkey])
			}
			
			products = products.sort { it.code }
			
		} else {
		
			def prodFamily = ""
			if (params.productFamily) {
				prodFamily = " and product.productFamily.id = " + params.productFamily
			}
		
			if (params.query) {
				
				products= Product.executeQuery( """
					select product from Product as product where  product.disabled = false $prodFamily and (product.name like ? or product.code like ?) 
				    order by product.name """, ["%" + params.query + "%",  "%" + params.query + "%"])

			} else {
			
				products= Product.executeQuery( """
						select product from Product as product where  product.disabled = false $prodFamily 
					    order by product.name """, [])
			}
		}
		
		products
		render (['data': products.collect {
			def rev = it.revision ? " [rev. "  + it.revision + "] " : ": "
			[id: it.id, name:it.code + rev + it.name]
		}, 'count':products.size()] as JSON)
	}
}
