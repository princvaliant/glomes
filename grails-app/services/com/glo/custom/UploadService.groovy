package com.glo.custom

import com.glo.ndo.*
import com.glo.run.Unit
import com.mongodb.BasicDBObject
import org.apache.commons.logging.LogFactory
import org.grails.plugins.excelimport.ExcelImportUtils
import org.joda.time.format.*

class UploadService {

    private static final logr = LogFactory.getLog(this)

    def mongo
    def unitService
    def workflowService
    def transactional = false

    def uploadOemPackageTestData(def rows) {

        def i = 0
        rows.each {

            String code
            try {
                code = it.code?.trim()
            } catch (Exception exc) {
                code = ((Long) it.code).toString()
            }

            if (code) {
                def recv = new Expando()

                recv.uid = "admin"

                // Determine supplier
                if (it.supplier) {
                    def supplier = Company.findByName(it.supplier.toString())
                    recv.cid = supplier?.id
                }
                // Determine product
                if (it.product) {
                    def product = Product.findByCode(it.product)
                    if (!product)
                        throw new RuntimeException("Product '" + it.product + "' not found.")
                    recv.pid = product.id
                }

                // If new unit
                def unit = Unit.findByCode(code)
                if (!unit) {
                    recv.units = []
                    def m = [:]
                    m.put('code', code)
                    m.put('qty', 1)
                    recv.units.add(m)
                    unitService.start(recv, "Packages", "iblu")
                    unit = Unit.findByCode(code)

                    ['die_attach_iblu', 'wirebond_iblu', 'encapulation_iblu', 'fpc_attach', 'ilgp_assembly', 'ilgp_test', "iblu_sphere_test", 'iblu_assembly', 'iblu_test'].each { stepName ->
                        def buf = new Expando()
                        buf.isEngineering = true
                        buf.prior = 50
                        buf.processCategoryEng = "Packages"
                        buf.processKeyEng = "iblu"
                        buf.taskKeyEng = stepName
                        buf.units = []
                        buf.company = recv.cid
                        def n = [:]
                        n.put('transition', 'engineering')
                        n.put('id', unit.id)
                        buf.units.add(n)
                        unitService.move("admin", buf)
                    }

                    def bufu = new Expando()
                    it.each { k, v ->
                        if (!(k in ['code', 'supplier', 'product'])) {
                            bufu[k] = v
                        }
                    }
                    bufu.id = unit.id
                    unitService.update(bufu, "admin", false)

                }
                i++
            }
        }
        return i
    }

    def uploadOemPackage(def rows) {

        def i = 0
        rows.each {

//            'A':'supplier',
//            'B':'product',
//            'C':'code',
//            'D':'stepIn',
//            'E':'lgp',
//            'F':'red',
//            'G':'green',
//            'H':'greenglo',
//            'I':'blue'

            String code
            try {
                code = it.code?.trim()
            } catch (Exception exc) {
                code = ((Long) it.code).toString()
            }

            if (code) {
                def recv = new Expando()

                recv.uid = "admin"

                // Determine supplier
                if (it.supplier) {
                    def supplier = Company.findByName(it.supplier.toString())
                    recv.cid = supplier?.id
                }
                // Determine product
                if (it.product) {
                    def product = Product.findByCode(it.product)
                    if (!product)
                        throw new RuntimeException("Product '" + it.product + "' not found.")
                    recv.pid = product.id
                }

                // If new unit
                def unit = Unit.findByCode(code)
                if (!unit) {
                    recv.units = []
                    def m = [:]
                    m.put('code', code)
                    m.put('qty', 1)
                    m.put('od green die', it.green)
                    m.put('od red die', it.red)
                    m.put('od blue die', it.blue)
                    m.put('glo green part #', it.greenglo)
                    recv.units.add(m)
                    unitService.start(recv, "Packages", "iblu")
                    unit = Unit.findByCode(code)

                    def arr = []
                    if (it.stepIn == "iLGP") {
                        arr = ['die_attach_iblu', 'wirebond_iblu', 'encapulation_iblu', 'fpc_attach', 'ilgp_assembly', 'ilgp_test']
                    } else {
                        arr = ['die_attach_iblu', 'wirebond_iblu', 'encapulation_iblu', 'fpc_attach', 'ilgp_assembly', 'ilgp_test', 'iblu_assembly', 'iblu_test']
                    }

                    arr.each { stepName ->
                        def buf = new Expando()
                        buf.isEngineering = true
                        buf.prior = 50
                        buf.processCategoryEng = "Packages"
                        buf.processKeyEng = "iblu"
                        buf.taskKeyEng = stepName
                        buf.units = []
                        buf.company = recv.cid
                        def n = [:]
                        n.put('transition', 'engineering')
                        n.put('id', unit.id)
                        if (stepName == "ilgp_assembly")
                            buf['lgpModelNumber'] = it.lgp
                        buf.units.add(n)
                        unitService.move("admin", buf)
                    }
                }
                i++
            }
        }
        return i
    }

    def uploadPackageData(def rows) {

        def i = 0
        rows.each {

            String code
            try {
                code = it.code?.trim()
            } catch (Exception exc) {
                code = ((Long) it.code).toString()
            }

            if (code) {
                def recv = new Expando()

                recv.uid = "admin"

                // Determine supplier
                if (it.dieAttach) {
                    def supplier = Company.findByName(it.dieAttach)
                    recv.cid = supplier?.id
                }
                // Determine product
                if (it.product) {
                    def product = Product.findByCode(it.product)
                    if (!product)
                        throw new RuntimeException("Product '" + it.product + "' not found.")
                    recv.pid = product.id
                }

                def currTkey = ''
                // If new unit
                def unit = Unit.findByCode(code)

                System.out.println(code)

                if (!unit) {
                    recv.units = []
                    def m = [:]
                    m.put('code', code)
                    m.put('qty', 1)
                    m.put('green_die', it.green)
                    m.put('red_die', it.red)
                    m.put('blue_die', it.blue)
                    m.put('lgp', it.lgp)
                    m.put('fpc', it.fpc)
                    m.put('sfp', it.sfp)
                    m.put('esr', it.esr)
                    m.put('defusor', it.defusor)
                    m.put('bbef', it.bbef)
                    m.put('tbef', it.tbef)
                    m.put('bwtape', it.bwtape)
                    m.put('purpose', it.purpose)
                    recv.units.add(m)
                    unitService.start(recv, "Packages", "iblu")
                    unit = Unit.findByCode(code)
                } else {
                    currTkey = unit.tkey

                    def buf = new Expando()
                    buf.isEngineering = true
                    buf.prior = 50
                    buf.processCategoryEng = "Packages"
                    buf.processKeyEng = "iblu"
                    buf.taskKeyEng = "iblu_incomming_inspection"
                    buf.units = []
                    def n = [:]
                    n.put('transition', 'engineering')
                    n.put('id', unit.id)
                    buf.units.add(n)
                    unitService.move("admin", buf)

                    def bufu = [:]
                    bufu.put('green_die', it.green)
                    bufu.put('red_die', it.red)
                    bufu.put('blue_die', it.blue)
                    bufu.put('lgp', it.lgp)
                    bufu.put('fpc', it.fpc)
                    bufu.put('sfp', it.sfp)
                    bufu.put('esr', it.esr)
                    bufu.put('defusor', it.defusor)
                    bufu.put('bbef', it.bbef)
                    bufu.put('tbef', it.tbef)
                    bufu.put('bwtape', it.bwtape)
                    bufu.put('purpose', it.purpose)
                    bufu.id = unit.id
                    unitService.update(bufu, "admin", false)
                }

                def arr = ['die_attach_iblu', 'wirebond_iblu', 'mold_prep', 'encapsulation_iblu', 'saw_singulate', 'fpc_attach', 'epoxy_cure', 'ilgp_assembly', 'iblu_assembly']

                if (currTkey) {
                    arr.add(currTkey)
                }
                arr.each { stepName ->
                    def buf = new Expando()
                    buf.isEngineering = true
                    buf.prior = 50
                    buf.processCategoryEng = "Packages"
                    buf.processKeyEng = "iblu"
                    buf.taskKeyEng = stepName
                    buf.units = []
                    if (it[stepName]) {
                        def cc = Company.findByName(it[stepName])
                        buf.company = cc?.id
                    }
                    def n = [:]
                    n.put('transition', 'engineering')
                    n.put('id', unit.id)
                    buf.units.add(n)
                    unitService.move("admin", buf)
                }

                i++
            }
        }
        return i
    }


    def uploadLightBar(def rows) {

        def i = 0
        rows.each {

            String code
            try {
                code = it.code?.trim()
            } catch (Exception exc) {
                code = ((Long) it.code).toString()
            }

            if (code) {
                def recv = new Expando()

                recv.uid = "admin"

                // Determine supplier
                if (it.supplier) {
                    def supplier = Company.findByName(it.supplier.toString())
                    recv.cid = supplier?.id
                }
                // Determine product
                if (it.product) {
                    def product = Product.findByCode(it.product)
                    if (!product)
                        throw new RuntimeException("Product '" + it.product + "' not found.")
                    recv.pid = product.id
                }

                // If new unit
                def unit = Unit.findByCode(code)
                if (!unit) {
                    recv.units = []
                    def m = [:]
                    m.put('code', code)
                    m.put('qty', 1)
                    m.put('pcb', it.pcb)
                    m.put('green_die', it.green)
                    m.put('red_die', it.red)
                    m.put('blue_die', it.blue)
                    m.put('build_number', it.buildNumber)
                    recv.units.add(m)
                    unitService.start(recv, "Packages", "iblu")
                    unit = Unit.findByCode(code)

                    ['die_attach_iblu', 'wirebond_iblu', 'encapsulation_iblu', 'fpc_attach', 'ilgp_assembly', it.testType].each { stepName ->
                        def buf = new Expando()
                        buf.isEngineering = true
                        buf.prior = 50
                        buf.processCategoryEng = "Packages"
                        buf.processKeyEng = "iblu"
                        buf.taskKeyEng = stepName
                        buf.units = []
                        buf.company = recv.cid
                        def n = [:]
                        n.put('transition', 'engineering')
                        n.put('id', unit.id)
                        buf.units.add(n)
                        unitService.move("admin", buf)
                    }

                    def bufu = new Expando()
                    it.each { k, v ->
                        if (!(k in ['code', 'supplier', 'product', 'testType', 'pcb', 'green', 'blue', 'red', 'buildNumber'])) {
                            bufu[k] = v
                        }
                    }
                    bufu.id = unit.id
                    unitService.update(bufu, "admin", false)

                }
                i++
            }
        }
        return i
    }

    def uploadNgan(def rows, def supplierName) {

        def i = 0
        rows.each {

            String code
            try {
                code = it.code?.trim()
            } catch (Exception exc) {
                code = ((Long) it.code).toString()
            }

            if (code) {
                def recv = new Expando()

                recv.uid = "admin"

                // Determine supplier
                if (it.supplier) {
                    def supplier = Company.findByName(it.supplier.toString())
                    recv.cid = supplier?.id
                } else if (supplierName) {
                    def supplier = Company.findByName(supplierName)
                    recv.cid = supplier?.id
                }

                // Determine product
                if (it.product) {
                    def product = Product.findByCode(((Long) it.product).toString())
                    if (!product)
                        throw new RuntimeException("Product '" + it.product + "' not found.")
                    recv.pid = product.id
                } else {
                    def product = Product.findByCode("100")
                    recv.pid = product.id
                }

                // If new unit
                def unit = Unit.findByCode(code)
                if (!unit) {
                    recv.units = []
                    def m = [:]
                    m.put('code', code)
                    m.put('qty', 1)
                    recv.units.add(m)
                    unitService.start(recv, "nwLED", "patterning")

                    unit = Unit.findByCode(code)

                    def buf = new Expando()
                    buf.isEngineering = true
                    buf.prior = 50
                    buf.processCategoryEng = "nwLED"
                    buf.processKeyEng = "patterning"
                    buf.taskKeyEng = "inventory_incoming_ngan"
                    buf.units = []
                    def n = [:]
                    n.put('transition', 'engineering')
                    n.put('id', unit.id)
                    buf.units.add(n)
                    unitService.move("admin", buf)

                    def bufu = new Expando()
                    bufu.tsmStatisticsAverage = it.tsmStatisticsAverage
                    bufu.tsmStatisticsUniformity = it.tsmStatisticsUniformity
                    bufu.thickness = it.thickness

                    bufu.polish = it.polish
                    bufu.bow = it.bow

                    if (it.XRD_002FWHM) {
                        bufu.XRD_002FWHM = it.XRD_002FWHM
                        bufu.XRD_102FWHM = it.XRD_102FWHM
                    }

                    bufu.stemBatchNumber = it.lot
                    bufu.cassetteId = it.cassetteId
                    bufu.cassetteSlot = it.cassetteSlot
                    bufu.id = unit.id
                    unitService.update(bufu, "admin", false)

                }
                i++
            }
        }
        return i
    }

    def uploadEpiSanan(def rows, def supplierName) {

        def i = 0
        rows.each {

            String code
            try {
                code = it.code?.trim()
            } catch (Exception exc) {
                code = ((Long) it.code).toString()
            }

            if (code) {
                def recv = new Expando()

                recv.uid = "admin"

                // Determine supplier
                if (it.supplier) {
                    def supplier = Company.findByName(it.supplier.toString())
                    recv.cid = supplier?.id
                } else if (supplierName) {
                    def supplier = Company.findByName(supplierName)
                    recv.cid = supplier?.id
                }

                // Determine product
                if (it.product) {
                    def product = Product.findByCode(((Long) it.product).toString())
                    if (!product)
                        throw new RuntimeException("Product '" + it.product + "' not found.")
                    recv.pid = product.id
                } else {
                    def product = Product.findByCode("100")
                    recv.pid = product.id
                }

                // If new unit
                def unit = Unit.findByCode(code)
                if (!unit) {
                    recv.units = []
                    def m = [:]
                    m.put('code', code)
                    m.put('qty', 1)
                    m.put('runNumber', "N/A")
                    m.put('recipeName', "N/A")
                    m.put('company', recv.cid)
                    m.put('equipment', 2)
                    m.put('satellite', 1)
                    m.put('pocket', 1)
                    recv.units.add(m)
                    unitService.start(recv, "nwLED", "epi")

                    unit = Unit.findByCode(code)

                    def buf = new Expando()
                    buf.isEngineering = true
                    buf.prior = 50
                    buf.processCategoryEng = "nwLED"
                    buf.processKeyEng = "epi"
                    buf.taskKeyEng = "epi_incoming"
                    buf.units = []
                    def n = [:]
                    n.put('transition', 'engineering')
                    n.put('id', unit.id)
                    buf.units.add(n)
                    unitService.move("admin", buf)

                    def bufu = new Expando()
                    bufu.vfavg = it.vfavg
                    bufu.lvavg = it.lvavg
                    bufu.wavelength = it.wavelength
                    bufu.area = it.area
                    bufu.substratefactories = it.substratefactories
                    bufu.id = unit.id
                    unitService.update(bufu, "admin", false)
                }
                i++
            }
        }
        return i
    }

    def uploadBare(def rows) {

        def i = 0
        rows.each {

            String code
            try {
                code = it.code?.trim()
            } catch (Exception exc) {
                code = ((Long) it.code).toString()
            }

            if (code) {
                def recv = new Expando()

                recv.uid = "admin"

                // Determine supplier
                if (it.supplier) {
                    def supplier = Company.findByName(it.supplier.toString())
                    recv.cid = supplier?.id
                }

                // Determine product
                def product = Product.findByCode(((Long) it.product).toString())
                if (!product)
                    throw new RuntimeException("Product '" + it.product + "' not found.")
                recv.pid = product.id

                // If new unit

                recv.polish = it.polish

                def unit = Unit.findByCode(code)
                if (!unit) {
                    recv.units = []
                    def m = [:]
                    m.put('code', code)
                    m.put('qty', 1)
                    m.put('polish', it.polish)
                    m.put('cassetteId', it.cassetteId)
                    m.put('cassetteSlot', it.cassetteSlot)
                    recv.units.add(m)
                    unitService.start(recv, "nwLED", "bare_wafers")
                    i++
                }
            }
        }
        return i
    }

    def upload2(def rows) {

        def i = 0

        rows.each {

            if (it.pctg && it.pkey) {
                def recv = new Expando()

                recv.uid = "admin"

                // Determine supplier
                def supplier = Company.findByName(it.supplier?.toString())
                if (!supplier)
                    throw new RuntimeException("Supplier '" + it.supplier + "' not found.")

                recv.cid = supplier?.id

                // Determine product
                def product = Product.findByCode(it.productCode?.toString())
                if (!product)
                    throw new RuntimeException("Product code '" + it.productCode + "' not found.")

                recv.pid = product.id
                recv.lot = it.lot
                recv.note = it.note
                recv.uom = it.uom
                recv.prodCode = it.productCode

                recv.units = []
                def m = [:]
                m.put('code', "")
                m.put('qtyOut', it.qty)
                m.put('location', it.location)
                m.put('minQty', it.minQty)
                m.put('maxQty', it.maxQty)
                recv.units.add(m)
                unitService.start(recv, it.pctg, it.pkey)
                i++
            }
        }
        return i
    }

    def upload3(def rows) {

        def i = 0
        def db = mongo.getDB("glo")

        rows.each {

            def run = it.runNumber?.trim() ?: ''
            def recp = it.recipeName?.trim() ?: ''

            try {

                def filter = new BasicDBObject()
                filter.put('parentCode', null)
                filter.put('productCode', "100")
                filter.put('runNumber', run)
                filter.put("recipeName", recp)

                def fields = new BasicDBObject("code", 1)

                def units = db.unit.find(filter, fields).collect { it }

                units.each { unit ->

                    def bdoUnit = new BasicDBObject()
                    bdoUnit.put("id", unit["_id"])
                    bdoUnit.put("processCategory", "nwLED")
                    bdoUnit.put("processKey", "epi")
                    bdoUnit.put("taskKey", "epi_growth")

                    bdoUnit.put("Pregrowth NTR", it["Pregrowth NTR"])
                    bdoUnit.put("Core time", it["Core time"])
                    bdoUnit.put("Core Temp", it["Core Temp"])
                    bdoUnit.put("Core TEGa", it["Core TEGa"])
                    bdoUnit.put("Core NH3", it["Core NH3"])
                    bdoUnit.put("Core", it["Core"])
                    bdoUnit.put("UL1 time", it["UL1 time"])
                    bdoUnit.put("UL1 Temp", it["UL1 Temp"])
                    bdoUnit.put("UL1 TEGa", it["UL1 TEGa"])
                    bdoUnit.put("UL1 comp", it["UL1 comp"])
                    bdoUnit.put("UL1", it["UL1"])
                    bdoUnit.put("UL2 time", it["UL2 time"])
                    bdoUnit.put("UL2 Temp", it["UL2 Temp"])
                    bdoUnit.put("UL2 TEGa", it["UL2 TEGa"])
                    bdoUnit.put("UL2 comp", it["UL2 comp"])
                    bdoUnit.put("UL2", it["UL2"])
                    bdoUnit.put("UL3", it["UL3"])
                    bdoUnit.put("Prep time", it["Prep time"])
                    bdoUnit.put("Prep Temp", it["Prep Temp"])
                    bdoUnit.put("Prep comp", it["Prep comp"])
                    bdoUnit.put("Prep total flow", it["Prep total flow"])
                    bdoUnit.put("Prep", it["Prep"])
                    bdoUnit.put("QW count", it["QW count"])
                    bdoUnit.put("QW time", it["QW time"])
                    bdoUnit.put("QW Temp", it["QW Temp"])
                    bdoUnit.put("QW TEGa", it["QW TEGa"])
                    bdoUnit.put("QW press", it["QW press"])
                    bdoUnit.put("QW In-III", it["QW In-III"])
                    bdoUnit.put("QW total flow", it["QW total flow"])
                    bdoUnit.put("QW", it["QW"])
                    bdoUnit.put("QW Barriers", it["QW Barriers"])
                    bdoUnit.put("Sweep", it["Sweep"])
                    bdoUnit.put("LT cap", it["LT cap"])
                    bdoUnit.put("HT cap", it["HT cap"])
                    bdoUnit.put("Tip rounding", it["Tip rounding"])
                    bdoUnit.put("Pre-purge", it["Pre-purge"])
                    bdoUnit.put("AlGaN time", it["AlGaN time"])
                    bdoUnit.put("AlGaN Temp", it["AlGaN Temp"])
                    bdoUnit.put("AlGaN Al-III", it["AlGaN Al-III"])
                    bdoUnit.put("AlGaN TMGa", it["AlGaN TMGa"])
                    bdoUnit.put("AlGaN H2", it["AlGaN H2"])
                    bdoUnit.put("pGaN", it["pGaN"])
                    bdoUnit.put("pPPGaN", it["pPPGaN"])
                    bdoUnit.put("Spacer", it["Spacer"])

                    unitService.update(bdoUnit, "admin", true)

                    i++
                }


            } catch (Exception exc) {

                logr.warn("Not retrieved unit for " + run + " " + recp + " " + exc.getMessage())
            }
        }



        i
    }


    def upload4(def rows) {

        def i = 0
        def db = mongo.getDB("glo")

        rows.each {

            try {

                def filter = new BasicDBObject()
                filter.put('code', it.code.trim())

                def fields = new BasicDBObject("code", 1)

                def units = db.unit.find(filter, fields).collect { it }

                units.each { unit ->

                    def bdoUnit = new BasicDBObject()
                    bdoUnit.put("id", unit["_id"])
                    bdoUnit.put("processCategory", "nwLED")
                    bdoUnit.put("processKey", "epi")
                    bdoUnit.put("taskKey", "sem")

                    bdoUnit.put("SEM_length0x0y", it["SEM_length0x0y"])
                    bdoUnit.put("SEM_length0x15y", it["SEM_length0x15y"])
                    bdoUnit.put("SEM_length0x-15y", it["SEM_length0x-15y"])

                    unitService.update(bdoUnit, "admin", true)

                    i++
                }


            } catch (Exception exc) {

                logr.warn("Not retrieved unit for " + it?.code?.trim() + " " + exc.getMessage())
            }
        }



        i.toString()
    }

    def upload5(def rows) {

        def i = 0

        def process = Process.get(48)

        rows.each {

            try {
                def product = null

                Product.withTransaction { status ->

                    def productFamily = ProductFamily.findByName(it.productFamily)
                    if (!productFamily) {
                        productFamily = new ProductFamily()
                        productFamily.name = it.productFamily
                        productFamily.save(failOnError: true)
                    }

                    product = Product.findByCode(it.productCode)

                    if (!product) {

                        product = new Product()
                        product.code = it.productCode
                        product.productFamily = productFamily
                        product.startProcess = process

                        product.name = it.productDescription
                        product.category = it.productCategory
                        product.uom = it.uom
                        if (it.minQty)
                            product.minQty = it.minQty
                        if (it.maxQty)
                            product.maxQty = it.maxQty
                        product.comment = it.productComment
                        product.isBulk = true

                        product.save(failOnError: true)
                    }
                }

                Company.withTransaction { status ->

                    if (it.supplier1) {

                        Company company = Company.findByName(it.supplier1)
                        if (!company) {

                            company = new Company()
                            company.name = it.supplier1
                        }
                        company.save(failOnError: true)

                        ProductCompany pc = ProductCompany.findByCompanyAndProduct(company, product)
                        if (!pc) {

                            pc = new ProductCompany()
                            pc.company = company
                            pc.product = product
                        }
                        pc.vendorCode = it.vendorCode1
                        pc.save(failOnError: true)
                    }


                    if (it.supplier2) {

                        Company company = Company.findByName(it.supplier2)
                        if (!company) {

                            company = new Company()
                            company.name = it.supplier1
                            company.save()
                        }

                        ProductCompany pc = ProductCompany.findByCompanyAndProduct(company, product)
                        if (!pc) {

                            pc = new ProductCompany()
                            pc.company = company
                            pc.product = product
                        }
                        pc.vendorCode = it.vendorCode2
                        pc.save()
                    }

                    if (it.supplier3) {

                        Company company = Company.findByName(it.supplier3)
                        if (!company) {

                            company = new Company()
                            company.name = it.supplier1
                            company.save()
                        }

                        ProductCompany pc = ProductCompany.findByCompanyAndProduct(company, product)
                        if (!pc) {

                            pc = new ProductCompany()
                            pc.company = company
                            pc.product = product
                        }
                        pc.vendorCode = it.vendorCode3
                        pc.save()
                    }
                }


            } catch (Exception exc) {

                logr.warn(exc.getMessage())
            }
        }



        i.toString()
    }


    def uploadQev(def rows) {

        def i = 0
        def db = mongo.getDB("glo")

        rows.each { row ->

            def wid = row["waferId"]?.trim()
            def query = new BasicDBObject()
            query.put("waferId", wid.toString())
            def fields = new BasicDBObject()
            def temp = db.qev.find(query, fields)
            def qev = temp.collect { it }
            if (!qev) {
                qev = new BasicDBObject()
            } else {
                qev = qev[0]
            }

            row.each { k, v ->
                def val
                if (!v?.toString()?.isNumber()) {
                    val = v?.toString()?.trim()
                    if (val?.toUpperCase() == "N/A") {
                        val = null
                    }
                } else {
                    val = v
                }
                qev.put(k, val)
            }
            db.qev.save(qev);
            i++
        }
        i
    }


    def assignCompany(def location) {

        def move = new Expando()

        move.company = 1
        move.location = 1

        if (location == "EPI Desicator") {
            move.company = 1
            move.location = 2
        }

        if (location == "Lund") {
            move.company = 13
            move.location = 15
        }

        if (location == "Scrap") {
            move.company = 1
            move.location = 17
        }

        if (location == "Refridge") {
            move.company = 1
            move.location = 18
        }

        if (location == "EVG") {
            move.company = 4
            move.location = 6
        }

        if (location == "Julia" ||
                location == "Cynthia" ||
                location == "Daniel" ||
                location == "YingLan" ||
                location == "Patrik" ||
                location == "Iris" ||
                location == "Holly") {
            move.user = location
        }

        move
    }

    def uploadCustom(workbook) {
        def db = mongo.getDB("glo")
        def process = 'direct_view_baseline'
        def category = 'nwLED'
        Map map = [
                sheet    : 'Sheet1',
                startRow : 1,
                columnMap: [
                        'A': 'code',
                        'D': 'ito_ebeam_deposition|actualStart',
                        'E': 'ito_ebeam_deposition|ITOEbeamRunNumber',
                        'F': 'ito_ebeam_deposition|ITOrecipe',

                        'G': 'ito_mesa_patterning|actualStart',
                        'H': 'ito_mesa_patterning|mask',
                        'I': 'ito_mesa_patterning|resist_thickness',

                        'J': 'mesa_etch|actualStart',
                        'K': 'mesa_etch|EtchDuration',
                        'L': 'mesa_etch|IcpMesaRecipe',
                        'M': 'mesa_etch|icp_equipment',

                        'N': 'ito_wet_etch|actualStart',
                        'O': 'ito_wet_etch|hardbake_temperature_ito',
                        'P': 'ito_wet_etch|hardbake_duration_ito',
                        'Q': 'ito_wet_etch|solution_ito',
                        'R': 'ito_wet_etch|concentration_ito',
                        'S': 'ito_wet_etch|duration_ito',

                        'T': 'mesa_etch_pr_strip|actualStart',
                        'U': 'mesa_etch_pr_strip|PlasmaAsherRunNumber',
                        'V': 'mesa_etch_pr_strip|plasmaAsherRecipe',
                        'W': 'mesa_etch_pr_strip|mesa_height_1',
                        'X': 'mesa_etch_pr_strip|mesa_height_3',
                        'Y': 'mesa_etch_pr_strip|ito_pad_size_max',
                        'Z': 'mesa_etch_pr_strip|ito_pad_size_min',
                        'AA': 'mesa_etch_pr_strip|total_strip_time',

                        'AB': 'post_anneal|actualStart',
                        'AC': 'post_anneal|PostAnnealRunNumber',
                        'AD': 'post_anneal|post_aneal_recipe',
                        'AE': 'post_anneal|witness_sheet_resistance',
                        'AF': 'post_anneal|transmission',

                        'AG': 'lto_ald_deposition|actualStart',
                        'AH': 'lto_ald_deposition|lto_recipe',
                        'AI': 'lto_ald_deposition|lto_dep_run_number',
                        'AJ': 'lto_ald_deposition|lto_witness_thickness',

                        'AK': 'lto_ald_wet_etch|actualStart',
                        'AL': 'lto_ald_wet_etch|hardbake_temperature_ald',
                        'AM': 'lto_ald_wet_etch|hardbake_duration_ald',
                        'AN': 'lto_ald_wet_etch|solution_ald',
                        'AO': 'lto_ald_wet_etch|concentration_ald',
                        'AP': 'lto_ald_wet_etch|duration_ald',

                        'AQ': 'lto_ald_pr_strip|actualStart',
                        'AR': 'lto_ald_pr_strip|total_pr_strip_time',
                        'AS': 'lto_ald_pr_strip|lto_size_opening',
                        'AT': 'lto_ald_pr_strip|lto_opening_missalignment',
                        'AU': 'lto_ald_pr_strip|PlasmaAshRecipe',
                        'AV': 'lto_ald_pr_strip|PlasmaAshRunNumber',

                        'AW': 'metal_patterning|actualStart',
                        'AX': 'metal_patterning|mask',
                        'AY': 'metal_patterning|pattern_depth1',
                        'AZ': 'metal_patterning|pattern_depth3',
                        'BA': 'metal_patterning|pattern_depth6',

                        'BD': 'metal_deposition|actualStart',
                        'BE': 'metal_deposition|metal_stack',
                        'BF': 'metal_deposition|metal_ebeam_run_number',
                        'BG': 'metal_deposition|metal_ebeam_recipe_number',

                        'BH': 'metal_liftoff|actualStart',
                        'BI': 'metal_liftoff|resist_total_strip_time_liftoff',

                        'BJ': 'post_bond_pad_inspection|actualStart',
                        'BK': 'post_bond_pad_inspection|pbpdefects001',
                        'BL': 'post_bond_pad_inspection|pbpdefects002',
                        'BM': 'post_bond_pad_inspection|pbpdefects003',
                        'BN': 'post_bond_pad_inspection|pbpdefects004',
                        'BO': 'post_bond_pad_inspection|pbpdefects005',
                        'BP': 'post_bond_pad_inspection|pbpdefects006',
                        'BQ': 'post_bond_pad_inspection|pbpgrade001',
                        'BR': 'post_bond_pad_inspection|pbpgrade002',
                        'BS': 'post_bond_pad_inspection|pbpgrade003',
                        'BT': 'post_bond_pad_inspection|pbpgrade004',
                        'BU': 'post_bond_pad_inspection|pbpgrade005',
                        'BV': 'post_bond_pad_inspection|pbpgrade006',

                        'BW': 'isolation_patterning|actualStart',
                        'BX': 'isolation_patterning|resist_thickness_center',
                        'BY': 'isolation_patterning|resist_thickness_OD',
                        'BZ': 'isolation_patterning|hardbake_temp',

                        'CA': 'isolation_etch|actualStart',
                        'CB': 'isolation_etch|ICPIsolationEtchRecipe',
                        'CC': 'isolation_etch|ICP_equipment',

                        'CD': 'backside_cleaning|actualStart',
                        'CE': 'backside_cleaning|backside_cleaning_duration',
                        'CF': 'backside_cleaning|backside_cleaning_recipe',
                        'CG': 'backside_cleaning|inspection_result',

                        'CH': 'isolation_pr_strip|actualStart',
                        'CI': 'isolation_pr_strip|total_strip_time',

                        'CJ': 'post_isolation_bond_pad_inspection|actualStart',
                        'CK': 'post_isolation_bond_pad_inspection|pibpdefects001',
                        'CL': 'post_isolation_bond_pad_inspection|pibpdefects002',
                        'CM': 'post_isolation_bond_pad_inspection|pibpdefects003',
                        'CN': 'post_isolation_bond_pad_inspection|pibpdefects004',
                        'CO': 'post_isolation_bond_pad_inspection|pibpdefects005',
                        'CP': 'post_isolation_bond_pad_inspection|pibpdefects006',
                        'CQ': 'post_isolation_bond_pad_inspection|pibpgrade001',
                        'CR': 'post_isolation_bond_pad_inspection|pibpgrade002',
                        'CS': 'post_isolation_bond_pad_inspection|pibpgrade003',
                        'CT': 'post_isolation_bond_pad_inspection|pibpgrade004',
                        'CU': 'post_isolation_bond_pad_inspection|pibpgrade005',
                        'CV': 'post_isolation_bond_pad_inspection|pibpgrade006'


                ]
        ]
        List rows = ExcelImportUtils.convertColumnMapConfigManyRows(workbook, map)
        def fmt = DateTimeFormat.forPattern("yyyy-MM-dd");

        def allVars = []
        rows.each { row ->
            def varsPerStep = [:]
            row.each { k, v ->
                def spl = k.tokenize('|')
                def step = 'PROCESS';
                def param = k;
                if (spl.size() == 2) {
                    step = spl[0]
                    param = spl[1]
                }
                if (!varsPerStep[step]) {
                    varsPerStep.put(step, [:])
                }
                if (v != null) {
                    varsPerStep[step].put(param, v)
                }
            }
            allVars.add(varsPerStep)
        }

        allVars.each{ obj ->
            def bdoUnit = new BasicDBObject()
            obj.each {proc, params ->
                if (proc == 'PROCESS') {
                    def filter = new BasicDBObject()
                    filter.put('code', params.code.trim())
                    def fields = new BasicDBObject("code", 1)
                    def unit = db.unit.find(filter, fields).collect { it }[0]
                    if (!unit) {
                        unitService.revive(params.code.trim());
                        sleep(3000)
                        unit = db.unit.find(filter, fields).collect { it }[0]
                    }
                    bdoUnit.put("id", unit["_id"])
                } else {
                    params.each { k, v ->
                        bdoUnit.put(k, v)
                    }
                    // First move to that step
                    def buf = new Expando()
                    buf.isEngineering = true
                    buf.prior = 50
                    buf.processCategoryEng = category
                    buf.processKeyEng = process
                    buf.taskKeyEng = proc
                    buf.actualStart = bdoUnit["actualStart"] ? fmt.print(bdoUnit["actualStart"]) : null
                    buf.units = []
                    def n = [:]
                    n.put('transition', 'engineering')
                    n.put('id', bdoUnit["id"])
                    buf.units.add(n)

                    System.out.println(buf);

                    unitService.move("admin", buf)

                    // Than update data
                    bdoUnit.remove("actualStart")
                    bdoUnit.put("processCategory", category)
                    bdoUnit.put("processKey", process)
                    bdoUnit.put("taskKey", proc)

                    System.out.println(bdoUnit);
                    unitService.update(bdoUnit, "admin", true)
                }
            }
        }
    }
}
