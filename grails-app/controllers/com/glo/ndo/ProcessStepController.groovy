package com.glo.ndo

class ProcessStepController {
	
	def scaffold = true

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index = {
        redirect(action: "list", params: params)
    }

    def list = {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        [processStepInstanceList: ProcessStep.list(params), processStepInstanceTotal: ProcessStep.count()]
    }

    def create = {
        def processStepInstance = new ProcessStep()
        processStepInstance.properties = params
        return [processStepInstance: processStepInstance]
    }

    def save = {
        def processStepInstance = new ProcessStep(params)
        if (processStepInstance.save(flush: true)) {
            flash.message = "${message(code: 'default.created.message', args: [message(code: 'processStep.label', default: 'ProcessStep'), processStepInstance.toString()])}"
            redirect(action: "edit", id: processStepInstance.id)
        }
        else {
            render(view: "create", model: [processStepInstance: processStepInstance])
        }
    }

    def show = {
        def processStepInstance = ProcessStep.get(params.id)
        if (!processStepInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'processStep.label', default: 'ProcessStep'), params.id])}"
            redirect(action: "list")
        }
        else {
            [processStepInstance: processStepInstance]
        }
    }

    def edit = {
        def processStepInstance = ProcessStep.get(params.id)
        if (!processStepInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'processStep.label', default: 'ProcessStep'), params.id])}"
            redirect(action: "list")
        }
        else {
            return [processStepInstance: processStepInstance]
        }
    }

    def update = {
        def processStepInstance = ProcessStep.get(params.id)
        if (processStepInstance) {
            if (params.version) {
                def version = params.version.toLong()
                if (processStepInstance.version > version) {
                    
                    processStepInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [message(code: 'processStep.label', default: 'ProcessStep')] as Object[], "Another user has updated this ProcessStep while you were editing")
                    render(view: "edit", model: [processStepInstance: processStepInstance])
                    return
                }
            }

            processStepInstance.properties = params
            if (!processStepInstance.hasErrors() && processStepInstance.save(flush: true)) {
                flash.message = "${message(code: 'default.updated.message', args: [message(code: 'processStep.label', default: 'ProcessStep'), processStepInstance.toString()])}"
				
				def rework = processStepInstance.variables.find {
					it.name == 'reworksTotal'
				}
				
				if (processStepInstance.allowRework && !rework) {
					processStepInstance.addToVariables(new Variable([idx:77,name:'reworksTotal',title:'Reworks',dataType:'int',allowBlank:true,width:63,dir:'dc']))
				} else if (!processStepInstance.allowRework && rework){
					processStepInstance.removeFromVariables(rework)
					rework.delete()
				}
				
                redirect(action: "edit", id: processStepInstance.id)
            }
            else {
                render(view: "edit", model: [processStepInstance: processStepInstance])
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'processStep.label', default: 'ProcessStep'), params.id])}"
            redirect(action: "list")
        }
    }

    def delete = {
        def processStepInstance = ProcessStep.get(params.id)
        if (processStepInstance) {
            try {
                processStepInstance.delete(flush: true)
                flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'processStep.label', default: 'ProcessStep'), params.id])}"
                redirect(action: "list")
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'processStep.label', default: 'ProcessStep'), params.id])}"
                redirect(action: "show", id: params.id)
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'processStep.label', default: 'ProcessStep'), params.id])}"
            redirect(action: "list")
        }
    }
}
