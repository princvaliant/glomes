package com.glo.ndo

class BomController {
	
	def springSecurityService
	
	static navigation = [
		group:'admin',
		order:4,
		action:'list',
		title: "navigation.glo.bom",
		isVisible: {
			springSecurityService.isLoggedIn() &&
					org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN,ROLE_EQUIPMENT_ADMIN")
		}
	]

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index = {
        redirect(action: "list", params: params)
    }

    def list = {
		
        def bomList
		def bomTotal

		params.max = Math.min(params.max ? params.int('max') : 12, 240)
		if (params.q) {
			bomList = Bom.search(params.q + "*").results.collect {Bom.get(it.id)}
			bomTotal = bomList.size()
		}
		else {
			bomList = Bom.list(params)
			bomTotal = Bom.count()
		}
		
		[bomInstanceList: bomList, bomInstanceTotal:bomTotal, search:params.q]
    }

    def create = {
        def bomInstance = new Bom()
        bomInstance.properties = params
        return [bomInstance: bomInstance]
    }

    def save = {
        def bomInstance = new Bom(params)

		def currentBoms = Bom.findByAssemblyProductAndRevision(Product.get(params.assemblyProduct.id),params.revision)
		if (currentBoms) {
			flash.message = "ERROR:  This Product and Revision already have defined BOM"
			redirect(action: "create")
			return
		}
        
		if (bomInstance.save(flush: true)) {
            flash.message = "${message(code: 'default.created.message', args: [message(code: 'bom.label', default: 'Bom'), bomInstance.toString()])}"
            redirect(action: "create")
        }
        else {
            render(view: "create", model: [bomInstance: bomInstance])
        }
    }

    def show = {
        def bomInstance = Bom.get(params.id)
        if (!bomInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'bom.label', default: 'Bom'), params.id])}"
            redirect(action: "list")
        }
        else {
            [bomInstance: bomInstance]
        }
    }

    def edit = {
        def bomInstance = Bom.get(params.id)
        if (!bomInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'bom.label', default: 'Bom'), params.id])}"
            redirect(action: "list")
        }
        else {
            return [bomInstance: bomInstance]
        }
    }

    def update = {
        def bomInstance = Bom.get(params.id)
        if (bomInstance) {
            if (params.version) {
                def version = params.version.toLong()
                if (bomInstance.version > version) {
                    
                    bomInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [message(code: 'bom.label', default: 'Bom')] as Object[], "Another user has updated this Bom while you were editing")
                    render(view: "edit", model: [bomInstance: bomInstance])
                    return
                }
            }
			
			def currentBoms = Bom.findByAssemblyProductAndRevision(Product.get(params.assemblyProduct.id),params.revision)
			if (currentBoms && params.id.toLong() != currentBoms.id) {
				flash.message = "ERROR:  This Product and Revision already have defined BOM"
				redirect(action: "edit", id: bomInstance.id)
				return
			}
			
			bomInstance.properties = params
			if (!bomInstance.hasErrors() && bomInstance.save(flush: true)) {
                flash.message = "${message(code: 'default.updated.message', args: [message(code: 'bom.label', default: 'Bom'), bomInstance.toString()])}"
                redirect(action: "edit", id: bomInstance.id)
            }
            else {
                render(view: "edit", model: [bomInstance: bomInstance])
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'bom.label', default: 'Bom'), params.id])}"
            redirect(action: "list")
        }
    }

    def delete = {
        def bomInstance = Bom.get(params.id)
        if (bomInstance) {
            try {
                bomInstance.delete(flush: true)
                flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'bom.label', default: 'Bom'), params.id])}"
                redirect(action: "list")
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'bom.label', default: 'Bom'), params.id])}"
                redirect(action: "show", id: params.id)
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'bom.label', default: 'Bom'), params.id])}"
            redirect(action: "list")
        }
    }
}
