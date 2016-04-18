package com.glo.ndo

class BomPartController {

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index = {
        redirect(action: "list", params: params)
    }

    def list = {
        params.max = Math.min(params.max ? params.int('max') : 18, 180)
        [bomPartInstanceList: BomPart.list(params), bomPartInstanceTotal: BomPart.count()]
    }

    def create = {
        def bomPartInstance = new BomPart()
        bomPartInstance.properties = params
        return [bomPartInstance: bomPartInstance]
    }

    def save = {
        def bomPartInstance = new BomPart(params)
        if (bomPartInstance.save(flush: true)) {
            flash.message = "${message(code: 'default.created.message', args: [message(code: 'bomPart.label', default: 'BomPart'), bomPartInstance.toString()])}"
            redirect(action: "edit", id: bomPartInstance.id)
        }
        else {
            render(view: "create", model: [bomPartInstance: bomPartInstance])
        }
    }

    def show = {
        def bomPartInstance = BomPart.get(params.id)
        if (!bomPartInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'bomPart.label', default: 'BomPart'), params.id])}"
            redirect(action: "list")
        }
        else {
            [bomPartInstance: bomPartInstance]
        }
    }

    def edit = {
        def bomPartInstance = BomPart.get(params.id)
        if (!bomPartInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'bomPart.label', default: 'BomPart'), params.id])}"
            redirect(action: "list")
        }
        else {
            return [bomPartInstance: bomPartInstance]
        }
    }

    def update = {
        def bomPartInstance = BomPart.get(params.id)
        if (bomPartInstance) {
            if (params.version) {
                def version = params.version.toLong()
                if (bomPartInstance.version > version) {
                    
                    bomPartInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [message(code: 'bomPart.label', default: 'BomPart')] as Object[], "Another user has updated this BomPart while you were editing")
                    render(view: "edit", model: [bomPartInstance: bomPartInstance])
                    return
                }
            }
            bomPartInstance.properties = params
            if (!bomPartInstance.hasErrors() && bomPartInstance.save(flush: true)) {
                flash.message = "${message(code: 'default.updated.message', args: [message(code: 'bomPart.label', default: 'BomPart'), bomPartInstance.toString()])}"
                redirect(action: "edit", id: bomPartInstance.id)
            }
            else {
                render(view: "edit", model: [bomPartInstance: bomPartInstance])
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'bomPart.label', default: 'BomPart'), params.id])}"
            redirect(action: "list")
        }
    }

    def delete = {
        def bomPartInstance = BomPart.get(params.id)
        if (bomPartInstance) {
            try {
                bomPartInstance.delete(flush: true)
                flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'bomPart.label', default: 'BomPart'), params.id])}"
                redirect(action: "list")
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'bomPart.label', default: 'BomPart'), params.id])}"
                redirect(action: "show", id: params.id)
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'bomPart.label', default: 'BomPart'), params.id])}"
            redirect(action: "list")
        }
    }
}
