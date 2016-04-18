package com.glo.ndo



import org.junit.*
import grails.test.mixin.*

@TestFor(BomPartController)
@Mock(BomPart)
class BomPartControllerTests {

    def populateValidParams(params) {
        assert params != null
        // TODO: Populate valid properties like...
        //params["name"] = 'someValidName'
    }

    void testIndex() {
        controller.index()
        assert "/bomPart/list" == response.redirectedUrl
    }

    void testList() {

        def model = controller.list()

        assert model.bomPartInstanceList.size() == 0
        assert model.bomPartInstanceTotal == 0
    }

    void testCreate() {
        def model = controller.create()

        assert model.bomPartInstance != null
    }

    void testSave() {
        controller.save()

        assert model.bomPartInstance != null
        assert view == '/bomPart/create'

        response.reset()

        populateValidParams(params)
        controller.save()

        assert response.redirectedUrl == '/bomPart/show/1'
        assert controller.flash.message != null
        assert BomPart.count() == 1
    }

    void testShow() {
        controller.show()

        assert flash.message != null
        assert response.redirectedUrl == '/bomPart/list'

        populateValidParams(params)
        def bomPart = new BomPart(params)

        assert bomPart.save() != null

        params.id = bomPart.id

        def model = controller.show()

        assert model.bomPartInstance == bomPart
    }

    void testEdit() {
        controller.edit()

        assert flash.message != null
        assert response.redirectedUrl == '/bomPart/list'

        populateValidParams(params)
        def bomPart = new BomPart(params)

        assert bomPart.save() != null

        params.id = bomPart.id

        def model = controller.edit()

        assert model.bomPartInstance == bomPart
    }

    void testUpdate() {
        controller.update()

        assert flash.message != null
        assert response.redirectedUrl == '/bomPart/list'

        response.reset()

        populateValidParams(params)
        def bomPart = new BomPart(params)

        assert bomPart.save() != null

        // test invalid parameters in update
        params.id = bomPart.id
        //TODO: add invalid values to params object

        controller.update()

        assert view == "/bomPart/edit"
        assert model.bomPartInstance != null

        bomPart.clearErrors()

        populateValidParams(params)
        controller.update()

        assert response.redirectedUrl == "/bomPart/show/$bomPart.id"
        assert flash.message != null

        //test outdated version number
        response.reset()
        bomPart.clearErrors()

        populateValidParams(params)
        params.id = bomPart.id
        params.version = -1
        controller.update()

        assert view == "/bomPart/edit"
        assert model.bomPartInstance != null
        assert model.bomPartInstance.errors.getFieldError('version')
        assert flash.message != null
    }

    void testDelete() {
        controller.delete()
        assert flash.message != null
        assert response.redirectedUrl == '/bomPart/list'

        response.reset()

        populateValidParams(params)
        def bomPart = new BomPart(params)

        assert bomPart.save() != null
        assert BomPart.count() == 1

        params.id = bomPart.id

        controller.delete()

        assert BomPart.count() == 0
        assert BomPart.get(bomPart.id) == null
        assert response.redirectedUrl == '/bomPart/list'
    }
}
