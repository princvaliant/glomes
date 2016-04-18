package com.glo.run

import com.glo.ndo.Bom
import com.glo.ndo.Product
import com.mongodb.BasicDBObject
import com.mongodb.DBCollection

class ProductsService {

    def mongo
    def springSecurityService
	def workflowService
	def utilsService

    def updateUnitsProductCode (List unitCodes, String codeNew, String revisionNew) {

        if (revisionNew?.trim() == "") revisionNew = null

        if (!unitCodes)
            throw new RuntimeException("Unit list to update product code is empty")

        def productNew = Product.findByCodeAndRevision(codeNew, revisionNew)
        if (!productNew)
            throw new RuntimeException("New product code / revision is not valid")

        Product productOld
        def db = mongo.getDB("glo")
        unitCodes.each {unitCode ->

            def fields = new BasicDBObject()
            fields.put("productCode",1)
            fields.put("productRevision",1)
            def history = db.history.find(new BasicDBObject("code", unitCode), fields).collect{it}[0]
            if (history) {
                productOld = Product.findByCode(history.productCode)
                if (!productOld)
                    throw new RuntimeException("""Unit '${unitCode}' assigned product does not exist""")
            }
         }

        performUpdate(db, unitCodes, productOld, productNew)
    }

    def retrieveChildProductCodes (String bom) {

       def ret = []
       def productCode = bom.tokenize(",")[0]
       def revision = bom.tokenize(",")[1]
       def products = Product.findAllByCode(productCode)

        def localBom
        products.each {
            if (!localBom)
                localBom = Bom.findByAssemblyProductAndRevision(it, revision)
        }

       if (localBom) {
           localBom.bomParts.each {
               if (it.partProduct?.code)
                   ret.add(it.partProduct?.code)
           }
       }
       ret
    }

	private def performUpdate(def db, List unitCodes, Product productOld, Product productNew) {

        def collections = [['unit',''], ['unitarchive',''], ['history',''], ['dataReport','value.'],['dataReport','unit.']]

        unitCodes.each {unitCode ->

            collections.each { key, value ->

                DBCollection collection = db.getCollection(key)
                def query = new BasicDBObject("code", unitCode)
                def update = new BasicDBObject('$set', new BasicDBObject())
                update['$set'].put(value + "productCode", productNew.code)
                update['$set'].put(value + "product", productNew.name)
                update['$set'].put(value + "productRevision", productNew.revision ?: null)
                update['$set'].put(value + "productFamily", productNew.productFamily?.name?.trim())
                update.put('$push', new BasicDBObject(value + "tags", productNew.code))
                collection.update(query, update, false, false)

                def lst = (productOld?.name?.toUpperCase() + ' ' + productOld.code?.toUpperCase())?.split(' ')
                def update2 = new BasicDBObject('$pullAll', new BasicDBObject(value + "tags", lst ))
                collection.update(query, update2, false, false)
            }
        }
	}
}