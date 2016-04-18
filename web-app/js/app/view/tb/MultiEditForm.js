Ext.define('glo.view.tb.MultiEditForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.multieditform',
    border: false,
    rows: '',
    store: '',
    store2: '',
    parentWindow: '',
    processKeyEng: '',
    processCategoryEng: '',
    taskKeyEng: '',
    width: '100%',
    height: '100%',
    layout: 'column',
    fieldDefaults: {
        labelWidth: 200,
        labelAlign: 'right',
        width: 300,
        margin: 2
    },
    standardSubmit: false,
    bodyPadding: 2,

    initComponent: function () {

        this.store = Ext.create('Ext.data.Store', {
            fields: [
                {
                    name: 'taskKey',
                    type: 'string'
                },
                {
                    name: 'taskName',
                    type: 'string'
                }
            ],
            autoLoad: false,
            proxy: {
                type: 'rest',
                method: 'GET',
                url: rootFolder + 'taskbooks/taskDefsCompleted',
                extraParams: {
                    processKey: this.processKeyEng,
                    taskKey: this.taskKeyEng
                }
            },
            listeners: {
                scope: this,
                'load': function (store, records, options) {
                    Ext.getCmp('multiEditStepSelector').setValue(this.taskKeyEng);
                    if (Ext.getCmp('multiEditStepSelector').getValue() == undefined) {
                        Ext.getCmp('multiEditStepSelector').setValue(records[0].data.taskKey);
                    }
                    Ext.getCmp('multiEditStepSelector').fireEvent('select', this, records[0].data.taskKey);
                }
            }
        });

        var stepCombo = Ext.create('Ext.form.field.ComboBox', {
            id: 'multiEditStepSelector',
            name: 'multiEditStepSelector',
            fieldLabel: 'Select step',
            store: this.store,
            displayField: 'taskName',
            valueField: 'taskKey',
            forceSelection: true,
            triggerAction: 'all',
            queryMode: 'local',
            width: 325,
            labelWidth: 70,
            editable: false
        });

        this.store2 = Ext.create('Ext.data.Store', {
            fields: [
                {
                    name: 'procKey',
                    type: 'string'
                },
                {
                    name: 'procName',
                    type: 'string'
                }
            ],
            autoLoad: false,
            proxy: {
                type: 'rest',
                method: 'GET',
                url: rootFolder + 'taskbooks/procDefsCompleted',
                extraParams: {
                    category: this.processCategoryEng,
                    procKey: this.processKeyEng
                }
            },
            listeners: {
                scope: this,
                'load': function (records, operation, success) {
                    Ext.getCmp('multiEditProcessSelector').setValue(this.processKeyEng);
                    this.store.load();
                }
            }
        });

        var processCombo = Ext.create('Ext.form.field.ComboBox', {
            id: 'multiEditProcessSelector',
            name: 'multiEditProcessSelector',
            fieldLabel: 'Select process',
            store: this.store2,
            displayField: 'procName',
            valueField: 'procKey',
            forceSelection: true,
            triggerAction: 'all',
            queryMode: 'local',
            width: 325,
            labelWidth: 100,
            editable: false
        });


        Ext.apply(this, {
            dockedItems: [
                {
                    xtype: 'toolbar',
                    dock: 'top',
                    items: [processCombo, stepCombo]
                }
            ],
            items: [],
            buttons: [
                {
                    text: 'Change product',
                    iconCls: 'icon-table_edit',
                    handler: function () {

                        var form = this.up('form');
                        var values = {};
                        var i = 1;
                        values['updates'] = [''];
                        Ext.Object.each(form.rows, function (record) {
                            values['updates'][i] = form.rows[record].data.id;
                            i++;
                        });

                        var prodStore = Ext.create('glo.store.Products');
                        var prodFamStore = Ext.create('glo.store.ProductFamilies');
                        prodFamStore.load();

                        var productFamCombo = Ext.create('Ext.form.field.ComboBox', {
                            fieldLabel: 'Product family',
                            store: prodFamStore,
                            displayField: 'name',
                            valueField: 'id',
                            forceSelection: true,
                            triggerAction: 'all',
                            queryMode: 'local',
                            width: 465,
                            labelWidth: 130,
                            editable: false,
                            stateful: true
                        });

                        var productCombo = Ext.create('Ext.form.field.ComboBox', {
                            fieldLabel: 'Select product',
                            store: prodStore,
                            displayField: 'name',
                            valueField: 'id',
                            forceSelection: true,
                            triggerAction: 'all',
                            queryMode: 'local',
                            width: 465,
                            labelWidth: 130,
                            editable: false
                        });

                        productFamCombo.on(
                            'change',  function (combo, newValue, oldValue, opts) {
                                if (newValue) {
                                    prodStore.getProxy().extraParams.productFamily = newValue;
                                    prodStore.load();
                                }
                            });
                        productFamCombo.setValue(10);

                        var win2 = Ext.create('Ext.window.Window', {
                            title : 'Change product for selected units',
                            resizable : true,
                            modal : true,
                            bodyPadding : 6,
                            y : 50,
                            width : 488,
                            layout : 'anchor',
                            items : [productFamCombo, productCombo],
                            buttons : [{
                                text : 'Update',
                                handler : function() {

                                    values.productId = productCombo.value;
                                    Ext.Ajax.request({
                                        scope: this,
                                        method: 'POST',
                                        params: values,
                                        url: rootFolder + 'units/updateProduct',
                                        success: function (response) {
                                            var obj = Ext.decode(response.responseText);
                                            if (obj.success) {
                                                Ext.ux.Message.msg("Unit(s) product updated successfully.", "");
                                                this.up('window').destroy();
                                            } else {
                                                Ext.Msg.alert('Product update failed', obj.msg);
                                            }
                                        }
                                    });
                                }
                            }, {
                                text : 'Cancel',
                                handler : function() {
                                    this.up('window').destroy();
                                }
                            }]
                        });
                        win2.show();
                    }
                },
                {
                    xtype: 'tbfill'
                },
                {
                    text: 'Save unit data and close',
                    iconCls: 'icon-ok',
                    handler: function () {
                        var form = this.up('form');
                        var win = this.up('window');

                        if (form.getForm().isValid()) {

                            var valuesFull = form.getValues();
                            var values = {};

                            // remove values with empty value
                            for (var key in valuesFull) {
                                if (valuesFull[key] != '' || form.rows.length == 1) {
                                    values[key] = valuesFull[key];
                                }
                                if (valuesFull[key] == 'false') {
                                    values[key] = false
                                }
                                if (valuesFull[key] == 'true') {
                                    values[key] = true
                                }
                            }

                            values['processKey'] = processCombo.getValue();
                            values['processCategory'] = form.processCategoryEng;
                            values['taskKey'] = valuesFull['multiEditStepSelector'];

                            var i = 1;
                            values['updates'] = [''];
                            Ext.Object.each(form.rows, function (record) {
                                values['updates'][i] = form.rows[record].data.id;
                                i++;
                            });

                            Ext.Ajax.request({
                                scope: this,
                                method: 'POST',
                                params: values,
                                url: rootFolder + 'units/updates',
                                success: function (response) {
                                    var obj = Ext.decode(response.responseText);
                                    if (obj.success) {
                                        var grid = Ext.getCmp('contentGrid_' + this.ownerCt.ownerCt.processCategoryEng)
                                        var store = grid.store;
                                        var selected = grid.getSelectionModel().getSelection();

                                        store.load(function (records, operation, success) {

                                            var newRecordsToSelect = [];
                                            for (var i = 0; i < selected.length; i++) {
                                                record = store.getById(selected[i].getId());
                                                if (!Ext.isEmpty(record)) {
                                                    newRecordsToSelect.push(record);
                                                }
                                            }

                                            grid.getSelectionModel().select(newRecordsToSelect);
                                            Ext.ux.Message.msg("Unit(s) updated successfully.", "");
                                        });
                                        win.destroy();
                                    } else {
                                        Ext.Msg.alert('Multi edit error', obj.msg);
                                    }
                                }
                            });
                        }
                    }
                },
                {
                    text: 'Cancel',
                    iconCls: 'icon-cancel',
                    handler: function () {
                        this.up('window').destroy();
                    }
                }
            ]
        });


        processCombo.on('select', function () {
            stepCombo.store.getProxy().extraParams.processKey = processCombo.getValue();
            stepCombo.store.load();
        });

        stepCombo.on('select', function () {
            // Determine if input requires additional input attributes
            stepCombo.disabled = true;

            Ext.Ajax.request({
                scope: this,
                method: 'GET',
                params: {
                    procKey: processCombo.getValue(),
                    taskKey: this.value,
                    category: this.up('form').processCategoryEng,
                    allow: true
                },
                url: rootFolder + 'contents/dc',
                success: function (response) {
                    var obj = Ext.decode(response.responseText);
                    this.up('form').removeAll(true);
                    Ext.getCmp('htmlEditorForInstructions').setValue(obj.instructions);

                    var i = 0;
                    var fs;
                    for (ctrl in obj.controls) {
                        if (i % 15 == 0) {
                            var j = i + 15;
                            if (obj.controls.length - 15 < i)
                                j = obj.controls.length;
                            fs = Ext.create('Ext.form.FieldSet', {
                                name: 'fs' + i,
                                title: 'Edit fields ' + (i + 1).toString()
                                    + ' - ' + j.toString(),
                                style: 'padding: 0 3px 6px 3px;margin:3 3 3 3;',
                                bodyStyle: 'padding-top: '
                                    + (Ext.isIE ? '0' : '10px')
                            });

                            this.up('form').add(fs);
                        }


                        fs.add(obj.controls[ctrl]);

                        if (obj.controls[ctrl].metaName != undefined) {

                            var combo = this.up('form').form.findField(obj.controls[ctrl].name);
                            combo.store = Ext.create('glo.store.MetaItems', {
                                pageSize: 500
                            });

                            combo.store.getProxy().extraParams.metaName = obj.controls[ctrl].metaName;
                            combo.valueNotFoundText = combo.up('form').rows[0].get(obj.controls[ctrl].name);
                            combo.setValue(combo.up('form').rows[0].get(obj.controls[ctrl].name));
                        }

                        i++;
                    }

                    if (this.up('form').rows.length == 1) {

                        this.up('form').loadRecord(this.up('form').rows[0]);

                        if (!(this.value == this.up('form').taskKeyEng && this.up('form').processKeyEng == processCombo.getValue())) {
                            var actualStartField = this.up('form').form.findField('actualStart');
                            if (actualStartField)
                                actualStartField.setValue(null);
                        }
                    }

                    stepCombo.disabled = false;
                }
            });
        });

        this.callParent(arguments);

        this.store2.load();
    }

});
