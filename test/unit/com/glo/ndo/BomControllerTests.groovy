package com.glo.ndo



import org.junit.*
import grails.test.mixin.*

@TestFor(BomController)
@Mock(Bom)
class BomControllerTests {

    def populateValidParams(params) {
        assert params != null
        // TODO: Populate valid properties like...
        //params["name"] = 'someValidName'
    }

    void testIndex() {
        controller.index()
        assert "/bom/list" == response.redirectedUrl
    }

    void testList() {

        def model = controller.list()

        assert model.bomInstanceList.size() == 0
        assert model.bomInstanceTotal == 0
    }

    void testCreate() {
        def model = controller.create()

        assert model.bomInstance != null
    }

    void testSave() {
        controller.save()

        assert model.bomInstance != null
        assert view == '/bom/create'

        response.reset()

        populateValidParams(params)
        controller.save()

        assert response.redirectedUrl == '/bom/show/1'
        assert controller.flash.message != null
        assert Bom.count() == 1
    }

    void testShow() {
        controller.show()

        assert flash.message != null
        assert response.redirectedUrl == '/bom/list'

        populateValidParams(params)
        def bom = new Bom(params)

        assert bom.save() != null

        params.id = bom.id

        def model = controller.show()

        assert model.bomInstance == bom
    }

    void testEdit() {
        controller.edit()

        assert flash.message != null
        assert response.redirectedUrl == '/bom/list'

        populateValidParams(params)
        def bom = new Bom(params)

        assert bom.save() != null

        params.id = bom.id

        def model = controller.edit()

        assert model.bomInstance == bom
    }

    void testUpdate() {
        controller.update()

        assert flash.message != null
        assert response.redirectedUrl == '/bom/list'

        response.reset()

        populateValidParams(params)
        def bom = new Bom(params)

        assert bom.save() != null

        // test invalid parameters in update
        params.id = bom.id
        //TODO: add invalid values to params object

        controller.update()

        assert view == "/bom/edit"
        assert model.bomInstance != null

        bom.clearErrors()

        populateValidParams(params)
        controller.update()

        assert response.redirectedUrl == "/bom/show/$bom.id"
        assert flash.message != null

        //test outdated version number
        response.reset()
        bom.clearErrors()

        populateValidParams(params)
        params.id = bom.id
        params.version = -1
        controller.update()

        assert view == "/bom/edit"
        assert model.bomInstance != null
        assert model.bomInstance.errors.getFieldError('version')
        assert flash.message != null
    }

    void testDelete() {
        controller.delete()
        assert flash.message != null
        assert response.redirectedUrl == '/bom/list'

        response.reset()

        populateValidParams(params)
        def bom = new Bom(params)

        assert bom.save() != null
        assert Bom.count() == 1

        params.id = bom.id

        controller.delete()

        assert Bom.count() == 0
        assert Bom.get(bom.id) == null
        assert response.redirectedUrl == '/bom/list'
    }
}
