Ext.define('glo.controller.TbContent', {
    extend: 'Ext.app.Controller',
    contentGrid: '',

    views: ['glo.view.tb.Content', 'glo.view.tb.NoteList', 'glo.view.tb.PickList',
        'glo.view.tb.FileList'],
    requires: ['Ext.ux.grid.FiltersFeature', 'Ext.ux.grid.RowExpander'],

    init: function () {

        this.control({
            'taskbookcontent button[action=executeTransition]': {
                click: this.onTransitionClicked
            },
            'taskbookcontent': {
                contentselect: this.onContentSelect
            },
            'button[id=engineeringMoveId]': {
                click: this.onEngineeringMove
            },
            'taskbookcontent textfield[name=searchPerStepId]': {
                change: this.onSearchStep
            },
            'actioncolumn[name=showNotesId]': {
                click: this.onShowNotesWindow
            },
            'actioncolumn[name=showFilesId]': {
                click: this.onShowFilesWindow
            },
            'actioncolumn[name=showDetailsId]': {
                click: this.onShowDetailsWindow
            },
            'actioncolumn[name=jumpToChildrenId]': {
                click: this.onJumpToChildren
            },
            'actioncolumn[name=showPartsDetailsId]': {
                click: this.onShowPartsDetailsWindow
            },
            'actioncolumn#downloadFileId': {
                click: this.onDownloadFile
            }
        });


        this.searchStep2 = Ext.Function.createBuffered(this.searchStep, 500, this);
    },

    onEngineeringMove: function (b, e) {

        var procKey = Ext.getCmp('engineeringProcessId').getValue();
        var category = Ext.getCmp('engineeringProcessId').getRawValue();
        var s1 = category.indexOf("[");
        var s2 = category.indexOf("]");
        var cat = category.substr(s1 + 1, s2 - s1 - 1);

        var taskKey = Ext.getCmp('engineeringTaskId').getValue();

        this.processTransition(b.transition, cat, procKey,
            taskKey, b.rows, b.parentWindow, true);

        b.up('window').destroy();
    },

    onTransitionClicked: function (b, e) {

        var grid = Ext.getCmp('contentGrid_' + b.category);

        if (grid != undefined && grid.getSelectionModel().hasSelection()) {
            var rows = grid.getSelectionModel().getSelection();

            if (b.name == 'Edit') {
                this.processMultiEdit(b.category, b.processKey, b.taskKey,
                    rows, b.ownerCt.ownerCt.ownerCt);
            } else if (b.name == 'File') {
                this.processFileUpload(b.category, b.processKey, b.taskKey,
                    rows, b.ownerCt.ownerCt.ownerCt);
            } else if (b.name == 'Loss') {
                this.processYieldLoss(b.category, b.processKey, b.taskKey,
                    rows, b.ownerCt.ownerCt.ownerCt);
            } else if (b.name == 'Bonus') {
                this.processBonus(b.category, b.processKey, b.taskKey, rows,
                    b.ownerCt.ownerCt.ownerCt);
            } else if (b.name == 'Rework') {
                this.processRework(b.category, b.processKey, b.taskKey, rows,
                    b.ownerCt.ownerCt.ownerCt);
            } else if (b.name.indexOf('Engineering') >= 0) {
                this.processEngineeringTransition(b.name, b.category,
                    b.processKey, rows, b.ownerCt.ownerCt.ownerCt);
            } else if (b.name == 'Split') {
                this.processSplit(b.name, b.category, b.processKey, rows,
                    b.ownerCt.ownerCt.ownerCt);
            } else if (b.name == 'Merge') {
                this.processMerge(b.name, b.category, b.processKey, rows,
                    b.ownerCt.ownerCt.ownerCt);
            } else if (b.name == 'Sync') {
                this.processSync(b.name, b.category, b.processKey, b.taskKey,
                    rows, b.ownerCt.ownerCt.ownerCt);
            } else if (b.name == 'Import') {
                this.processImport(b.name, b.category, b.processKey, b.taskKey,
                    rows, b.ownerCt.ownerCt.ownerCt);
            } else if (b.name == 'Auto-route') {
                this.processAutoRoute(b.name, b.category, b.processKey, b.taskKey, b.taskKeySource,
                    rows, b.ownerCt.ownerCt.ownerCt);
            } else if (b.name == 'Barcodes') {
                this.processPrintBarcode(b.name, b.category, b.processKey, b.taskKey,
                    rows, b.ownerCt.ownerCt.ownerCt);
            } else if (b.iconCls == "icon-report2") {
                this.processCustomAction(b.name, b.category, b.processKey, b.taskKey,
                    rows, b.ownerCt.ownerCt.ownerCt);
            } else
            {
                this.processTransition(b.name, b.category, b.processKey,
                    b.taskKey, rows, b.ownerCt.ownerCt.ownerCt, false);
            }
        }
    },

    processTransition: function (transition, category, processKey, taskKey, rows, parentWindow, isEngineering) {

        var oneIsBadInput = false;
        var oneIsBadSpec = false;
        var oneIsBadSanity = false;

        // All processes but preventive maintenance
        if (!(category == 'PM' || (category == 'RctHdw' && processKey == 'inventory'))) {

            // processStep.allowMoveDespiteSpecFail should be checked rather than "mrb" within taskKey
            var skipValidation = (taskKey.indexOf("_mrb") >= 0) || (taskKey.indexOf("mrb_") >= 0);

            if (isEngineering == false && skipValidation == false) {
                Ext.Object.each(rows, function (record) {
                    if (rows[record].data.ok == "1") {
                        oneIsBadInput = true;
                    }
                    if (rows[record].data.ok == "2" || rows[record].data.ok == "4") {
                        oneIsBadSpec = true;
                    }
                    if (rows[record].data.ok == "3") {
                        oneIsBadSanity = true;
                    }
                });
            }
        }


        if (oneIsBadInput == true) {
            Ext.Msg
                .alert(
                'Input validation warning',
                'Please provide all the mandatory data for selected row(s).');
            return ('Please provide all the mandatory data for selected row(s).');
        }

        if (oneIsBadSanity == true) {
            Ext.Msg
                .alert(
                'Input validation warning',
                'Some row(s) contain invalid variable values (Check highlighted fields)');
            return('Some row(s) contain invalid variable values (Check highlighted fields)');
        }

        if (oneIsBadSpec == true) {
            Ext.Msg.alert(
                'Spec validation warning',
                'One of the selected row(s) did not pass SPEC LIMIT validation (Check highlighted fields)');
            return('One of the selected row(s) did not pass SPEC LIMIT validation (Check highlighted fields)');
        }

        this.processRegularTransition(transition, category, processKey,
            taskKey, rows, parentWindow, isEngineering);

        return('')
    },

    processCustomAction: function (name, category, processKey, tkey, rows, parentWindow) {

        if (rows.length > 1) {
            Ext.Msg.alert(
                name + ' warning',
                'This operation can be performed only on 1 selected unit');
            return;
        }
        if (name === 'Validate coupons') {
            this.processValidateCoupons(category, processKey, tkey, rows, parentWindow);
        } else {
            Ext.Msg.alert(
                    name + ' warning',
                'Custom action not defined');
        }
    },

    processValidateCoupons: function (category, processKey, tkey, rows, parentWindow) {

        var mainctrl = gloApp.getController('MainPanel');
        if (Ext.getCmp("mainpanel_DVD") == undefined) {
            mainctrl.createTbPanel("DVD", "dvd_assembly");
        }
        var unit = {};
        unit.pctg = 'DVD';
        unit.pkey = 'dvd_assembly';
        unit.tkey = 'coupon_on_wafer';
        unit.tname = 'Coupon on wafer';
        mainctrl.delayedSearch.delay(700, null, null, [{lastQuery:rows[0].data.code +'_'}, unit]);
    },

    processSplit: function (transition, category, processKey, rows, parentWindow) {

        Ext.MessageBox.confirm(
            'Confirm split',
            'Unit will be split based on the "Split product codes" process step parameter or "Mask" variable on the unit. Are you sure?',
            function (btn) {
                if (btn == 'yes') {
                    var selects = new Array();

                    Ext.Object.each(rows, function (record) {
                        selects[record] = rows[record].data.productCode
                            + '__' + rows[record].data.product;
                    });

                    if (Ext.Array.unique(selects).length > 1) {
                        Ext.Msg
                            .alert('Split warning',
                            'Please select the same products when splitting.');
                        return;
                    }

                    var aResult = Ext.Array.map(rows,
                        function (node) {
                            return {
                                id: node.data.id
                            };
                        });
                    var units = Ext.encode(aResult);

                    Ext.Ajax.request({
                        scope: this,
                        method: 'POST',
                        params: {
                            units: units
                        },
                        url: rootFolder + 'units/split',
                        success: function (response) {
                            var obj = Ext
                                .decode(response.responseText);
                            if (obj.success == false) {
                                Ext.Msg.alert(
                                    'Split can not complete', obj.msg);
                            } else {
                                Ext.ux.Message.msg(
                                    "Split completed successfully.","");

                                gloApp.getController('MainPanel')
                                    .loadList(parentWindow, '', '', 'stay');
                            }
                        }
                    });
                }
            });
    },

    processMerge: function (transition, category, processKey, rows, parentWindow) {

        var win = Ext.create('Ext.window.Window', {
            title: 'Merge units to ' + rows[0].data.code + ' (BOM revision ' + (rows[0].data.bomRevision || '') + ')',
            resizable: true,
            modal: true,
            y: 50,
            layout: 'fit',
            items: {
                xtype: 'picklist',
                row: rows[0],
                parentWindow: parentWindow,
                processKeyEng: processKey
            }
        });

        win.show();
    },

    processSync: function (transition, category, processKey, taskKey, rows, parentWindow) {

        Ext.MessageBox.confirm('Confirm data sync',
            'Do you want to sync data from directory?', function (btn) {
                if (btn == 'yes') {
                    Ext.Ajax.request({
                        scope: this,
                        method: 'GET',
                        params: {
                            pctg: category,
                            pkey: processKey,
                            tkey: taskKey
                        },
                        url: rootFolder + 'synchronize/sync',
                        success: function (response) {

                            var obj = Ext
                                .decode(response.responseText);
                            if (obj.success == false) {
                                Ext.Msg.alert(
                                    'Sync can not complete',
                                    obj.msg);
                            } else {
                                Ext.ux.Message.msg(
                                        "Sync started successfully for "
                                        + obj.cnt
                                        + " item(s). ", "");

                                gloApp.getController('MainPanel')
                                    .loadList(parentWindow, '', '', 'stay');
                            }
                        }
                    });
                }
            });
    },


    processPrintBarcode: function (transition, category, processKey, taskKey, rows, parentWindow) {

        var aResult = Ext.Array.map(rows, function (node) {
            return {
                id: node.data.id
            };
        });
        var units = Ext.encode(aResult);

        // Determine printing capabilities
        Ext.Ajax.request({
            scope: this,
            method: 'GET',
            params: {
                pctg: category,
                pkey: processKey,
                tkey: taskKey,
                units: units
            },
            url: rootFolder + 'units/bcCapabilities',
            success: function (response) {
                var obj = Ext.decode(response.responseText);
                if (obj.success == false) {
                    Ext.Msg.alert('This process step is not configured for barcode printing');
                } else {
                    var win = Ext.create('Ext.window.Window', {
                        title: 'Manage barcodes',
                        resizable: true,
                        modal: true,
                        width: 408,
                        y: 50,
                        layout: 'anchor',
                        items: {
                            id: 'printBarcodeFormId',
                            xtype: 'printbarcodeform',
                            units: units,
                            parentWindow: parentWindow,
                            pctg: category,
                            pkey: processKey,
                            tkey: taskKey,
                            labels: obj.labels,
                            printers: obj.printers,
                            destinations: obj.destinations,
                            ungroup: obj.ungroup
                        }
                    });

                    win.show();
                }
            }
        });
    },


    processImport: function (transition, category, processKey, taskKey, rows, parentWindow) {

        var win = Ext.create('Ext.window.Window', {
            title: 'Import data from file for ' + rows.length
                + ' unit(s)',
            resizable: true,
            modal: true,
            width: 408,
            y: 50,
            layout: 'anchor',
            items: {
                id: 'fileUploadFormId',
                xtype: 'fileuploadform',
                importData: 'true',
                rows: rows,
                parentWindow: parentWindow,
                category: category,
                processKeyEng: processKey,
                taskKeyEng: taskKey
            }
        });

        win.show();

    },

    processAutoRoute: function (transition, category, processKey, taskKey, taskKeySource, rows, parentWindow) {

        var aResult = Ext.Array.map(rows, function (node) {
            return {
                transition: transition,
                id: node.data.id
            };
        });
        var units = Ext.encode(aResult);

        Ext.Ajax.request({
            scope: this,
            method: 'GET',
            url: rootFolder + 'units/autoRoute',
            params: {
                processCategoryEng: category,
                processKeyEng: processKey,
                taskKeyEng: taskKey,
                taskKeySource: taskKeySource,
                units: units
            },
            success: function (response) {
                var obj = Ext.decode(response.responseText);
                if (obj.success == true) {
                    var cntrl = gloApp.getController('TbContent');
                    var notRouted = '';
                    Ext.Object.each(obj.data, function (key, value, myself) {
                        var res = '';
                        var pkeytkey = key.split('|');
                        if (pkeytkey.length === 2) {
                            cntrl.processEngineeringTransition('Engineering', category, pkeytkey[0],
                                 value, parentWindow, pkeytkey[1]);
                            res = '';
                        } else {
                            res = cntrl.processTransition(transition, category, processKey,
                                key, value, parentWindow, false);
                        }
                        if (res != '') {
                            notRouted += value.length + ' unit(s) to ' + key + ': ' + res + '<br>';
                        }
                    });

                    // succesfully routed units display 'move form'... therefore only not-routed units need pop-up below
                    if (notRouted != '') {
                        Ext.Msg.alert('Failed', notRouted);
                    }

                } else {
                    Ext.Msg.alert('Failed', obj.msg);
                }
            }
        });
    },


    processEngineeringTransition: function (transition, category, processKey, rows, parentWindow, taskKey) {

        var read_only = false;
        if (transition == 'Engineering-T') read_only = true;

        var win = Ext.create('Ext.window.Window', {
            title: 'Engineering move of ' + rows.length + ' unit(s) ',
            resizable: true,
            modal: true,
            bodyPadding: 6,
            y: 50,
            width: 408,
            layout: 'anchor',
            buttons: [
                {
                    text: 'Continue ...',
                    id: 'engineeringMoveId',
                    transition: transition,
                    category: category,
                    processKey: processKey,
                    rows: rows,
                    parentWindow: parentWindow
                },
                {
                    text: 'Cancel',
                    handler: function () {
                        this.up('window').destroy();
                    }
                }
            ]
        });

        var storeProc = Ext.create('Ext.data.Store', {
            fields: [
                {
                    name: 'procId',
                    type: 'long'
                },
                {
                    name: 'procKey',
                    type: 'string'
                },
                {
                    name: 'procName',
                    type: 'string'
                }
            ],
            autoDestroy: false,
            autoLoad: false,
            autoSync: false,
            proxy: {
                type: 'rest',
                method: 'GET',
                url: rootFolder + 'taskbooks/procDefs'
            },
            sorters: [
                {
                    property: 'procName',
                    direction: 'ASC'
                }
            ]
        });

        var storeTask = Ext.create('Ext.data.Store', {
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
            autoDestroy: false,
            autoLoad: false,
            autoSync: false,
            proxy: {
                type: 'rest',
                method: 'GET',
                url: rootFolder + 'taskbooks/taskDefs'
            },
            sorters: {
                property: 'taskName',
                direction: 'ASC'
            }
        });

        var procCombo = Ext.create('Ext.form.field.ComboBox', {
            fieldLabel: 'To process',
            anchor: '100%',
            name: 'engineeringProcess',
            id: 'engineeringProcessId',
            store: storeProc,
            displayField: 'procName',
            valueField: 'procKey',
            forceSelection: true,
            multiSelect: false,
            allowBlank: false,
            readOnly: read_only,
            minChars: 1,
            typeAhead: true,
            triggerAction: 'all'
        });

        var taskCombo = Ext.create('Ext.form.field.ComboBox', {
            fieldLabel: 'To step',
            anchor: '100%',
            name: 'engineeringTask',
            id: 'engineeringTaskId',
            store: storeTask,
            displayField: 'taskName',
            queryMode: 'local',
            valueField: 'taskKey',
            forceSelection: true,
            multiSelect: false,
            allowBlank: false,
            minChars: 1,
            typeAhead: true,
            triggerAction: 'all'
        });

        procCombo.on('select', function () {
            taskCombo.clearValue();
            taskCombo.getPicker().setLoading(false);
            taskCombo.store.getProxy().extraParams.processKey = this.getValue();
            taskCombo.store.load();
        });

        storeProc.getProxy().extraParams.category = category;
        storeProc.load({
            scope: this,
            callback: function (records, operation, success) {
                procCombo.setValue(processKey);
                taskCombo.getPicker().setLoading(false);
                taskCombo.store.getProxy().extraParams.processKey = processKey;
                taskCombo.store.load({
                    scope: this,
                    callback: function (records, operation, success) {
                        if (taskKey) {
                            taskCombo.setValue(taskKey);
                        }
                    }
                })
            }
        });

        win.add(procCombo);
        win.add(taskCombo);

        win.show();
    },

    processRegularTransition: function (transition, category, processKey, taskKey, rows, parentWindow, isEngineering) {

        var moveForm = Ext.create('glo.view.tb.MoveForm', {
            rows: rows,
            parentWindow: parentWindow,
            transition: transition,
            isEngineering: isEngineering,
            processCategoryEng: category,
            processKeyEng: processKey,
            taskKeyEng: taskKey
        });

        var win = Ext.create('Ext.window.Window', {
            title: 'Moving ' + rows.length + ' unit(s) to ' + taskKey,
            resizable: true,
            modal: true,
            width: 508,
            closeAction: 'destroy',
            y: 50,
            layout: 'anchor',
            items: moveForm
        });

        // get min qtyOut of all the selected items
        var minQtyOut = 0;
        Ext.Object.each(rows, function (node) {

            if (minQtyOut == 0 || (rows[node].data.qtyOut > 0 && rows[node].data.qtyOut < minQtyOut)) {
                minQtyOut = rows[node].data.qtyOut;
            }
        });

        // Determine if input requires additional input attributes
        Ext.Ajax.request({
            scope: this,
            method: 'GET',
            params: {
                procKey: processKey,
                taskKey: taskKey,
                category: category
            },
            url: rootFolder + 'contents/din',
            success: function (response) {
                var obj = Ext.decode(response.responseText);

                // Add all remaining controls
                moveForm.add(obj.controls);
                var cntrl = gloApp.getController('TbContent');

                Ext.Object.each(obj.controls, function (key, value, myself) {
                    if (value.id != undefined && value.xtype == 'combo') {
                        Ext.getCmp(value.id).on('select',
                            cntrl.onMoveFormComboSelect);
                    }

                    if (value.name == 'qtyIn') {
                        var qtyField = moveForm.query('numberfield[name=qtyIn]')[0];
                        qtyField.setValue(minQtyOut);
                        qtyField.setMaxValue(minQtyOut);
                        qtyField.setMinValue(1);
                    }
                });
                Ext.Object.each(obj.controls, function (key, value, myself) {
                    if (value.id != undefined && value.xtype == 'combo') {
                        cntrl.onMoveFormComboSelect(Ext
                                .getCmp(value.id), value.value,
                            null);
                    }
                });
            }
        });

        win.show();
    },


    processMultiEdit: function (category, processKey, taskKey, rows, parentWindow) {


        var win = Ext.create('Ext.window.Window', {
            title: 'Multiediting ' + rows.length + ' unit(s)',
            resizable: false,
            modal: true,
            y: 50,
            width: '100%',
            height: '89%',
            layout: 'fit',
            draggable: false,
            items: {xtype: 'panel',
                layout: 'border',
                items: [
                    {
                        id: 'multiEditFormId',
                        xtype: 'multieditform',
                        rows: rows,
                        region: 'center',
                        parentWindow: parentWindow,
                        processCategoryEng: category,
                        processKeyEng: processKey,
                        taskKeyEng: taskKey
                    },
                    {
                        xtype: 'panel',
                        layout: 'fit',
                        title: 'Instructions',
                        stateful: true,
                        stateId: 'htmlEditorLayoutId',
                        split: true,
                        collapsible: false,
                        region: 'east',
                        width: '30%',
                        items: {
                            xtype: 'htmleditor',
                            id: 'htmlEditorForInstructions',
                            width: '100%',
                            height: '100%',
                            enableColors: true,
                            enableAlignments: true,
                            enableSourceEdit: true,
                            listeners: {
                                afterrender: function () {

                                    this.setValue(Ext.getCmp('multiEditFormId').instructions);

                                    var btn = Ext.create('Ext.button.Button', {
                                        hidden: false,
                                        iconCls: 'icon-report_go',
                                        tooltip: 'Insert image',
                                        handler: function () {

                                            Ext.create('Ext.window.Window', {
                                                title: 'Insert image',
                                                layout: "fit",
                                                closable: true,
                                                items: {
                                                    xtype: 'form',
                                                    fileUpload: true,
                                                    autoHeight: true,
                                                    autoWidth: true,
                                                    frame: true,
                                                    bodyStyle: 'padding:5px',
                                                    labelAlign: 'right',
                                                    defaults: {
                                                         width: 320,
                                                        labelWidth: 70
                                                    },
                                                    items:
                                                     {
                                                            fieldLabel: 'Image file',
                                                            xtype: 'filefield',
                                                            name: 'imageName',
                                                            emptyText: 'Select image...',
                                                            allowBlank : false,
                                                            buttonText: '',
                                                            buttonConfig : {
                                                                iconCls : 'icon-file_upload'
                                                            }
                                                     },
                                                     buttons: {

                                                        xtype: 'button',
                                                        width: 150,
                                                        text:'Upload and Insert',
                                                        iconCls :'icon-ok',
                                                        handler : function () {
                                                            var form = this.up('form');
                                                            var win = this.up('window');

                                                            if (form.getForm().isValid()) {
                                                                form.getForm().submit({
                                                                    scope : this,
                                                                    method : 'POST',
                                                                    url : rootFolder + 'units/imageUpload',
                                                                    waitMsg : 'Uploading image ...',
                                                                    success : function(frm, action) {

                                                                        var obj = Ext.decode(action.response.responseText);
                                                                        if (obj.success == 'false') {
                                                                            Ext.Msg.alert('Failed', obj.msg);
                                                                        } else {
                                                                            var img = "http://calserver03:8081/docs/" + obj.imageName;
                                                                            Ext.getCmp('htmlEditorForInstructions').relayCmd('InsertHTML', '<img src='+ img + ' alt="" />');
                                                                            win.destroy();
                                                                        }
                                                                    }
                                                                });
                                                            }
                                                        }
                                                    }
                                                 },
                                                closable: true,
                                                width: 350,
                                                modal: true,
                                                autoHeight: true
                                            }).show();
                                        }
                                    });
                                    this.toolbar.insert(0, btn);
                                    btn.show();
                                }
                            }
                        },
                        dockedItems: {
                            xtype: 'toolbar',
                            dock: 'bottom',
                            items: ['->', {
                                xtype: 'button',
                                iconCls: 'icon-disk_multiple',
                                text: 'Save instructions',
                                handler: function () {

                                    var mef = Ext.getCmp('multiEditFormId');
                                    var value = this.up('panel').down('htmleditor').value;
                                    Ext.Ajax.request({
                                        scope: this,
                                        method: 'POST',
                                        url: rootFolder + 'units/saveInstruction',
                                        params: {
                                            pkey: mef.getValues()['multiEditProcessSelector'],
                                            tkey: mef.getValues()['multiEditStepSelector'],
                                            pctg: mef.processCategoryEng,
                                            instructions: value
                                        },
                                        success: function (response) {
                                            Ext.ux.Message.msg('Instructions for the step saved successfully','');
                                        }
                                    });
                                }
                            }]
                        }

                    }
                ]
            }
        });
        win.show();
    },

    processYieldLoss: function (category, processKey, taskKey, rows, parentWindow) {

        var win = Ext.create('Ext.window.Window', {
            title: 'Record yield loss for ' + rows.length + ' unit(s)',
            resizable: true,
            modal: true,
            width: 408,
            y: 50,
            layout: 'anchor',
            items: {
                id: 'yieldLossFormId',
                xtype: 'yieldlossform',
                rows: rows,
                parentWindow: parentWindow,
                processKeyEng: processKey,
                taskKeyEng: taskKey
            }
        });

        // get min qtyOut of all the selected items
        var minQtyOut = 0;
        Ext.Object.each(rows, function (node) {

            if (minQtyOut == 0 || (rows[node].data.qtyOut > 0 && rows[node].data.qtyOut < minQtyOut)) {
                minQtyOut = rows[node].data.qtyOut;
            }
        });

        // Determine if input requires additional input attributes
        Ext.Ajax.request({
            scope: this,
            method: 'GET',
            params: {
                procKey: processKey,
                taskKey: taskKey,
                category: category
            },
            url: rootFolder + 'yieldLosses/fields',
            success: function (response) {
                var obj = Ext.decode(response.responseText);
                Ext.getCmp('yieldLossFormId').add(obj.controls);

                var qtyField = Ext.getCmp('yieldLossFormId').query('numberfield[name=qty]')[0];
                qtyField.setValue(minQtyOut);
                qtyField.setMaxValue(minQtyOut);
                qtyField.setMinValue(1);
            }
        });

        win.show();
    },

    processBonus: function (category, processKey, taskKey, rows, parentWindow) {

        var win = Ext.create('Ext.window.Window', {
            title: 'Record bonus for ' + rows.length + ' unit(s)',
            resizable: true,
            modal: true,
            width: 408,
            y: 50,
            layout: 'anchor',
            items: {
                id: 'bonusFormId',
                xtype: 'bonusform',
                rows: rows,
                parentWindow: parentWindow,
                processKeyEng: processKey,
                taskKeyEng: taskKey
            }
        });

        // Determine if input requires additional input attributes
        Ext.Ajax.request({
            scope: this,
            method: 'GET',
            params: {
                procKey: processKey,
                taskKey: taskKey,
                category: category
            },
            url: rootFolder + 'bonuses/fields',
            success: function (response) {
                var obj = Ext.decode(response.responseText);
                Ext.getCmp('bonusFormId').add(obj.controls);
            }
        });

        win.show();
    },

    processRework: function (category, processKey, taskKey, rows, parentWindow) {

        var win = Ext.create('Ext.window.Window', {
            title: 'Rework for ' + rows.length + ' unit(s)',
            resizable: true,
            modal: true,
            width: 408,
            y: 50,
            layout: 'anchor',
            items: {
                id: 'reworkFormId',
                xtype: 'reworkform',
                rows: rows,
                parentWindow: parentWindow,
                processKeyEng: processKey,
                taskKeyEng: taskKey
            }
        });

        // Determine if input requires additional input attributes
        Ext.Ajax.request({
            scope: this,
            method: 'GET',
            params: {
                procKey: processKey,
                taskKey: taskKey,
                category: category
            },
            url: rootFolder + 'reworks/fields',
            success: function (response) {
                var obj = Ext.decode(response.responseText);
                Ext.getCmp('reworkFormId').add(obj.controls);
            }
        });

        win.show();
    },

    processFileUpload: function (category, processKey, taskKey, rows, parentWindow) {

        var win = Ext.create('Ext.window.Window', {
            title: 'Upload and attach file to ' + rows.length
                + ' unit(s)',
            resizable: true,
            modal: true,
            width: 408,
            y: 50,
            layout: 'anchor',
            items: {
                id: 'fileUploadFormId',
                xtype: 'fileuploadform',
                importData: 'false',
                rows: rows,
                category: category,
                parentWindow: parentWindow,
                processKeyEng: processKey,
                taskKeyEng: taskKey
            }
        });

        win.show();
    },

    onMoveFormComboSelect: function (field, value, options) {

        if (Ext.getCmp('equipment') != undefined && Ext.getCmp('company')) {
            if (field.id == 'equipment') {

                Ext.Ajax.request({
                    scope: this,
                    method: 'GET',
                    params: {
                        equipment: field.value
                    },
                    url: rootFolder + 'companies',
                    success: function (response) {

                        var obj = Ext.decode(response.responseText);
                        Ext.getCmp('company').store.loadData(obj.data, false);
                        if (obj.data.length == 1) {
                            Ext.getCmp('company').setValue(obj.data[0]);

                            if (Ext.getCmp('location') != undefined) {
                                Ext.Ajax.request({
                                    scope: this,
                                    method: 'GET',
                                    params: {
                                        equipment: field.value
                                    },
                                    url: rootFolder + 'locations',
                                    success: function (response) {
                                        var obj2 = Ext
                                            .decode(response.responseText);
                                        Ext.getCmp('location').store
                                            .loadData(obj2.data,
                                            false);
                                        if (obj2.data.length == 1) {
                                            Ext
                                                .getCmp('location')
                                                .setValue(obj2.data[0]);
                                        } else {
                                            Ext.getCmp('location')
                                                .setValue(null);
                                        }
                                    }
                                });
                            }

                        } else {
                            Ext.getCmp('company').setValue(null);
                        }
                    }
                });
            }
        }

        if (Ext.getCmp('company') != undefined && Ext.getCmp('location')) {
            if (field.id == 'company') {

                Ext.Ajax.request({
                    scope: this,
                    method: 'GET',
                    params: {
                        company: field.value
                    },
                    url: rootFolder + 'locations',
                    success: function (response) {
                        var obj = Ext.decode(response.responseText);
                        Ext.getCmp('location').store.loadData(obj.data,
                            false);
                        if (obj.data.length == 1) {
                            Ext.getCmp('location')
                                .setValue(obj.data[0]);
                        } else {
                            // Ext.getCmp('location')
                            // .setValue(null);
                        }
                    }
                });
            }
        }

        // if (Ext.getCmp('location') != undefined && Ext.getCmp('equipment')) {
        // if (field.id == 'location') {
        // Ext.Ajax.request({
        // scope : this,
        // method : 'GET',
        // params : {
        // location : field.value
        // },
        // url : rootFolder + 'equipments',
        // success : function(response) {
        // var obj = Ext.decode(response.responseText);
        // Ext.getCmp('equipment').store.loadData(obj.data, false);
        // if (obj.data.length == 1) {
        // Ext.getCmp('equipment').setValue(obj.data[0])
        // }
        // }
        // });
        // }
        // }
    },

    onShowNotesWindow: function (grid, cell, row, col, e) {
        var rec = grid.getStore().getAt(row);

        if (Ext.getCmp('showNotesWindowId') != undefined) {
            Ext.getCmp('showNotesWindowId').destroy();
        }

        var win = Ext.create('Ext.window.Window', {
            title: 'Notes for Unit ' + rec.get('code'),
            id: 'showNotesWindowId',
            width: 890,
            height: 600,
            bodyPadding: 5,
            y: 50,
            layout: 'fit',
            draggable: false,
            items: [
                {
                    xtype: 'notelist',
                    autoScroll: true,
                    id: 'noteListId',
                    unitId: rec.get('id')
                }
            ],
            dockedItems: [
                {
                    xtype: 'panel',
                    dock: 'bottom',
                    layout: 'hbox',
                    items: [
                        {
                            xtype: 'textarea',
                            id: 'addNoteTextId',
                            grow: false,
                            emptyText: '<type new note here>',
                            enterIsSpecial: false,
                            rows: 4,
                            allowBlank: false,
                            flex: 10
                        },
                        {
                            xtype: 'button',
                            text: 'add note',
                            height: 83,
                            flex: 1,
                            handler: function () {
                                Ext.Ajax.request({
                                    scope: this,
                                    method: 'POST',
                                    params: {
                                        unitId: rec.get('id'),
                                        comment: Ext.getCmp('addNoteTextId').value
                                    },
                                    url: rootFolder + 'units/addNote',
                                    success: function (response) {
                                        var obj = Ext.decode(response.responseText);
                                        if (obj.success == false) {
                                            Ext.Msg.alert('Add note failed', obj.msg);
                                        } else {
                                            Ext.getCmp('noteListId').loadUnit(rec.get('id'));
                                        }
                                    }
                                });
                            }
                        }
                    ]
                }
            ]
        });

        win.show();
    },

    onShowFilesWindow: function (grid, cell, row, col, e) {

        var rec = grid.getStore().getAt(row);

        if (rec.get('nFiles') == 0) {
            return;
        }

        if (Ext.getCmp('showFilesWindowId') != undefined) {
            Ext.getCmp('showFilesWindowId').destroy();
        }

        var win = Ext.create('Ext.window.Window', {
            title: 'Files attached to Unit ' + rec.get('code'),
            id: 'showFilesWindowId',
            width: 720,
            height: 500,
            bodyPadding: 4,
            y: 50,
            layout: 'fit',
            items: {
                xtype: 'filelist',
                unitId: rec.get('id')
            }
        });

        win.show();
    },

    onShowDetailsWindow: function (grid, cell, row, col, e) {

        var rec = grid.getStore().getAt(row);
        this.showDetailsWindow(rec.get('code'), null);
    },

    onJumpToChildren: function (grid, cell, row, col, e) {

        var rec = grid.getStore().getAt(row);
        var main = gloApp.getController('MainPanel');
        var o = {lastQuery: rec.get('code')};
        var u = {
            pctg: 'C' ,
            pkey: 'fabassembly',
            tname: rec.get('tname')
        }
        if (rec.get('nJumps') == 1) {
            if (Ext.getCmp('mainpanel_C') == undefined) {
                main.createTbPanel('C');
                main.delayedSearch.delay(700, null, null, [o,  u]);
            } else {
                main.delayedSearch.delay(30, null, null, [o, u]);
            }
        }
        if (rec.get('nJumps') == 2) {
            main.createTbPanel('W');
            Ext.getCmp('W').toggle();
        }
    },

    onShowPartsDetailsWindow: function (grid, cell, row, col, e) {

        var rec = grid.getStore().getAt(row);
        this.showDetailsWindow(rec.get('code'), rec.get('parentCode'));
    },

    showDetailsWindow: function (code, parentCode) {

        if (Ext.getCmp('showDetailsWindowId') != undefined) {
            Ext.getCmp('showDetailsWindowId').destroy();
        }

        var win = Ext.create('Ext.window.Window', {
            id: 'showDetailsWindowId',
            title: 'Unit details - ' + code,
            modal: false,
            constrainHeader: true,
            bodyPadding: 0,
            width: 1200,
            constrain: true,
            constrainHeader: true,
            height: 650,
            y: 2,
            layout: 'fit',
            draggable: false,
            items: {
                xtype: 'panel',
                name: 'winPanelSearchId',
                layout: 'border',
                align: 'stretch',
                autoHeight: true,
                autoWidth: true,
                items: [
                    {
                        region: 'north',
                        collapsible: false,
                        split: true,
                        border: true,
                        name: 'north',
                        items: {
                            xtype: 'detailsform',
                            id: 'detailsFormId'
                        }
                    },
                    {
                        region: 'east',
                        collapsible: false,
                        border: true,
                        layout: 'fit',
                        width: '35%',
                        name: 'east',
                        items: {
                            xtype: 'tabpanel',
                            activeTab: 0,
                            itemId: 'detailsTabPanelId',
                            items: [
                                {
                                    title: 'Data out',
                                    xtype: 'propertygrid',
                                    nameColumnWidth: 190,
                                    align: 'stretch',
                                    stateful: true,
                                    stateId: 'detailsDataDcGridId',
                                    tkey: '',
                                    id: 'detailsDataDcGridId',
                                    source: {
                                        '(name)': 'Step not selected'
                                    },
                                    listeners: {
                                        beforeedit: function () {
                                            return false; // prevent
                                            // editing
                                        },
                                        click: function () {

                                        },
                                        beforeitemdblclick: function (obj, record, item, index, e, eOpts) {

                                            if (record.data.name.indexOf('testDataIndex') >= 0) {

                                                console.log(code);


                                                testId = record.data.name.split('_')[1];
                                                gloApp.getController('WaferPanel').show(code, obj.panel.pctg, obj.panel.pkey, obj.panel.tkey, record.data.name, testId);
                                            } else if (record.data.name.indexOf('testTopIndex') >= 0) {
                                                testId = record.data.name.split('_')[1];
                                                gloApp.getController('WaferPanel').show(code, obj.panel.pctg, obj.panel.pkey, obj.panel.tkey, record.data.name, testId);
                                            } else if (record.data.name.indexOf('pl405') >= 0 || record.data.name.indexOf('pl370') >= 0) {
                                                window.open(rootFolder + 'photo?id=' + code, 'resizable,scrollbars');
                                            } else if (record.data.name.indexOf('testCharIndex') >= 0) {
                                                testId = record.data.name.split('_')[1];
                                                gloApp.getController('WaferPanel').show(code, obj.panel.pctg, obj.panel.pkey, obj.panel.tkey, record.data.name, testId);
                                            } else if (record.data.name.indexOf('testPreDbrIndex') >= 0) {
                                                testId = record.data.name.split('_')[1];
                                                gloApp.getController('WaferPanel').show(code, obj.panel.pctg, obj.panel.pkey, obj.panel.tkey, record.data.name, testId);
                                            } else if (record.data.name.indexOf('testPostDbrIndex') >= 0) {
                                                testId = record.data.name.split('_')[1];
                                                gloApp.getController('WaferPanel').show(code, obj.panel.pctg, obj.panel.pkey, obj.panel.tkey, record.data.name, testId);
                                            } else if (record.data.name.indexOf('testUniformityIndex') >= 0) {
                                                testId = record.data.name.split('_')[1];
                                                gloApp.getController('WaferPanel').show(code, obj.panel.pctg, obj.panel.pkey, obj.panel.tkey, record.data.name, testId);
                                            } else if (record.data.name.indexOf('testFaIndex') >= 0) {
                                                testId = record.data.name.split('_')[1];
                                                gloApp.getController('WaferPanel').show(code, obj.panel.pctg, obj.panel.pkey, obj.panel.tkey, record.data.name, testId);
                                            } else if (record.data.name.indexOf('testFullIndex') >= 0) {
                                                testId = record.data.name.split('_')[1];
                                                gloApp.getController('WaferPanel').show(code, obj.panel.pctg, obj.panel.pkey, obj.panel.tkey, record.data.name, testId);
                                            } else if (record.data.name.indexOf('relWaferData') >= 0) {
                                                gloApp.getController('MeasurePanel').show(code, obj.panel.pctg, obj.panel.pkey, 'wafer_rel_test', record.data.name, obj.store.data);
                                            } else if (record.data.name.indexOf('probeTestExperiment') >= 0) {
                                                gloApp.getController('ProbeTestPanel').show(code, obj.panel.pctg, obj.panel.pkey, obj.panel.tkey, record.data.name);

                                            } else if (record.data.name.indexOf('lampTestData') >= 0) {
                                                gloApp.getController('MeasurePanel').show(code, obj.panel.pctg, obj.panel.pkey, 'sphere_test_lamp', record.data.name, obj.store.data);
                                            } else if (record.data.name.indexOf('lampBoardTestData') >= 0) {
                                                gloApp.getController('MeasurePanel').show(code, obj.panel.pctg, obj.panel.pkey, 'sphere_test_lamp_board', record.data.name, obj.store.data);
                                            } else if (record.data.name.indexOf('relTestData') >= 0) {
                                                gloApp.getController('MeasurePanel').show(code, obj.panel.pctg, obj.panel.pkey, obj.panel.tkey, record.data.name, obj.store.data);
                                            } else if (record.data.name.indexOf('d65SyncedAfterTestData') >= 0) {
                                                gloApp.getController('MeasurePanel').show(code, obj.panel.pctg, obj.panel.pkey, obj.panel.tkey, record.data.name, obj.store.data);
                                            } else if (record.data.name.indexOf('d65SyncedBeforeTestData') >= 0) {
                                                gloApp.getController('MeasurePanel').show(code, obj.panel.pctg, obj.panel.pkey, obj.panel.tkey, record.data.name, obj.store.data);
                                            } else if (record.data.name.indexOf('d65RelSyncTestData') >= 0) {
                                                gloApp.getController('MeasurePanel').show(code, obj.panel.pctg, obj.panel.pkey, obj.panel.tkey, record.data.name, obj.store.data);
                                            } else if (record.data.name.indexOf('wstSyncedTestData') >= 0) {
                                                gloApp.getController('MeasurePanel').show(code, obj.panel.pctg, obj.panel.pkey, obj.panel.tkey, record.data.name, obj.store.data);
                                            } else if (record.data.name.indexOf('wstDieSyncedTestData') >= 0) {
                                                gloApp.getController('MeasurePanel').show(code, obj.panel.pctg, obj.panel.pkey, obj.panel.tkey, record.data.name, obj.store.data);

                                            } else if (record.data.name.indexOf('lightbartestSyncedTestData') >= 0) {
                                                gloApp.getController('MeasurePanel').show(code, obj.panel.pctg, obj.panel.pkey, obj.panel.tkey, record.data.name, obj.store.data);
                                            } else if (record.data.name.indexOf('ilgptestSyncedTestData') >= 0) {
                                                gloApp.getController('MeasurePanel').show(code, obj.panel.pctg, obj.panel.pkey, obj.panel.tkey, record.data.name, obj.store.data);
                                            } else if (record.data.name.indexOf('ibluspheretestSyncedTestData') >= 0) {
                                                gloApp.getController('MeasurePanel').show(code, obj.panel.pctg, obj.panel.pkey, obj.panel.tkey, record.data.name, obj.store.data);
                                            } else if (record.data.name.indexOf('iblutestSyncedTestData') >= 0) {
                                                gloApp.getController('MeasurePanel').show(code, obj.panel.pctg, obj.panel.pkey, obj.panel.tkey, record.data.name, obj.store.data);
                                            } else if (record.data.name.indexOf('blutestSyncedTestData') >= 0) {
                                                gloApp.getController('MeasurePanel').show(code, obj.panel.pctg, obj.panel.pkey, obj.panel.tkey, record.data.name, obj.store.data);
                                            } else if (record.data.name.indexOf('displaytestSyncedTestData') >= 0) {
                                                gloApp.getController('MeasurePanel').show(code, obj.panel.pctg, obj.panel.pkey, obj.panel.tkey, record.data.name, obj.store.data);
                                            } else if (record.data.name.indexOf('iLBTestData') >= 0) {
                                                gloApp.getController('MeasurePanel').show(code, obj.panel.pctg, obj.panel.pkey, obj.panel.tkey, record.data.name, obj.store.data);
                                            } else if (record.data.name.indexOf('iLGPTestData') >= 0) {
                                                gloApp.getController('MeasurePanel').show(code, obj.panel.pctg, obj.panel.pkey, obj.panel.tkey, record.data.name, obj.store.data);
                                            } else if (record.data.name.indexOf('iBLUTestData') >= 0) {
                                                gloApp.getController('MeasurePanel').show(code, obj.panel.pctg, obj.panel.pkey, obj.panel.tkey, record.data.name, obj.store.data);

                                            } else if (record.data.name.indexOf('testMWTIndex') >= 0) {
                                                gloApp.getController('MeasurePanel').show(code, obj.panel.pctg, obj.panel.pkey, obj.panel.tkey, record.data.name, obj.store.data);
                                            } else if (record.data.name.indexOf('light_bar_relSyncTestData') >= 0) {
                                                gloApp.getController('MeasurePanel').show(code, obj.panel.pctg, obj.panel.pkey, obj.panel.tkey, record.data.name, obj.store.data);
                                            } else if (record.data.name.indexOf('ilgp_relSyncTestData') >= 0) {
                                                gloApp.getController('MeasurePanel').show(code, obj.panel.pctg, obj.panel.pkey, obj.panel.tkey, record.data.name, obj.store.data);
                                            } else if (record.data.name.indexOf('iblu_relSyncTestData') >= 0) {
                                                gloApp.getController('MeasurePanel').show(code, obj.panel.pctg, obj.panel.pkey, obj.panel.tkey, record.data.name, obj.store.data);


                                            } else if (record.data.value.indexOf('<Count') >= 0 || record.data.value.indexOf('<Units>') >= 0) {
                                                gloApp.getController('WaferPanel').show(code, obj.panel.pctg, obj.panel.pkey, obj.panel.tkey, record.data.name);
                                            }
                                        }
                                    }
                                }
                                ,
                                {
                                    title: 'Data In',
                                    xtype: 'propertygrid',
                                    id: 'detailsDinGridId',
                                    source: {
                                        '(name)': 'Step not selected'
                                    },
                                    listeners: {
                                        beforeedit: function () {
                                            return false; // prevent
                                            // editing
                                        }
                                    }
                                },
                                {
                                    title: 'Spec',
                                    xtype: 'propertygrid',
                                    id: 'detailsSpecGridId',
                                    source: {
                                        '(name)': 'Step not selected'
                                    },
                                    listeners: {
                                        beforeedit: function () {
                                            return true; // prevent
                                            // editing
                                        }
                                    }
                                },
                                {
                                    title: 'Recipe',
                                    xtype: 'propertygrid',
                                    id: 'detailsDataRecipeGridId',
                                    source: {
                                        '(name)': 'Step not selected'
                                    },
                                    listeners: {
                                        beforeedit: function () {
                                            return false; // prevent
                                            // editing
                                        }
                                    }
                                },
                                {
                                    title: 'Details',
                                    xtype: 'propertygrid',
                                    stateful: true,
                                    stateId: 'detailsLocationGridId',
                                    id: 'detailsLocationGridId',
                                    source: {
                                        '(name)': 'Step not selected'
                                    },
                                    listeners: {
                                        beforeedit: function () {
                                            return false; // prevent
                                            // editing
                                        }
                                    }
                                }
                            ]
                        }
                    },
                    {
                        region: 'center',
                        layout: 'accordion',
                        border: false,
                        name: 'center',
                        items: [
                            {
                                title: 'Steps completed (recent at the top)',
                                xtype: 'grid',
                                id: 'detailsGridId',
                                sortableColumns: false,
                                store: Ext.create('Ext.data.Store', {
                                    fields: [
                                        {
                                            name: 'pctg',
                                            type: 'string'
                                        },
                                        {
                                            name: 'pkey',
                                            type: 'string'
                                        },
                                        {
                                            name: 'tname',
                                            type: 'string'
                                        },
                                        {
                                            name: 'tkey',
                                            type: 'string'
                                        },
                                        {
                                            name: 'owner',
                                            type: 'string'
                                        },
                                        {
                                            name: 'movedBy',
                                            type: 'string'
                                        },
                                        {
                                            name: 'experimentId',
                                            type: 'string'
                                        },
                                        {
                                            name: 'build_number',
                                            type: 'string'
                                        },
                                        {
                                            name: 'actualStart',
                                            type: 'date'
                                        },
                                        {
                                            name: 'start',
                                            type: 'date'
                                        },
                                        {
                                            name: 'end',
                                            type: 'date'
                                        },
                                        {
                                            name: 'duration',
                                            type: 'long'
                                        },
                                        {
                                            name: 'dc'
                                        },
                                        {
                                            name: 'din'
                                        },
                                        {
                                            name: 'spec'
                                        },
                                        {
                                            name: 'recp'
                                        },
                                        {
                                            name: 'last',
                                            type: 'boolean'
                                        },
                                        {
                                            name: 'company'
                                        },
                                        {
                                            name: 'companyId'
                                        },
                                        {
                                            name: 'location'
                                        },
                                        {
                                            name: 'locationId'
                                        },
                                        {
                                            name: 'workCenter'
                                        },
                                        {
                                            name: 'workCenterId'
                                        },
                                        {
                                            name: 'prior'
                                        },
                                        {
                                            name: 'stepIdx'
                                        },
                                        {
                                            name: 'qtyIn'
                                        },
                                        {
                                            name: 'qtyOut'
                                        }
                                    ],
                                    sorters: [
                                        {
                                            property: 'start',
                                            direction: 'DESC'
                                        }
                                    ]
//													,filters : [{
//														 property: 'last',
//	           											 value   : true
//													}]
                                }),
                                border: false,
                                autoScroll: true,
                                multiSelect: false,
                                stateful: true,
                                stateId: 'detailsGridId2',
                                viewConfig: {
                                    emptyText: 'No history to display'
                                },
                                columns: [
                                    {
                                        header: "Category",
                                        width: 60,
                                        dataIndex: 'pctg'
                                    },
                                    {
                                        header: "Process",
                                        width: 60,
                                        dataIndex: 'pkey'
                                    },
                                    {
                                        header: "Step",
                                        width: 125,
                                        dataIndex: 'tname'
                                    },
                                    {
                                        header: "Owner",
                                        width: 65,
                                        dataIndex: 'owner'
                                    },
                                    {
                                        header: "Moved by",
                                        width: 56,
                                        dataIndex: 'movedBy'
                                    },
                                    {
                                        header: "Exp#",
                                        width: 55,
                                        dataIndex: 'experimentId'
                                    },
                                    {
                                        header: "Build#",
                                        width: 60,
                                        dataIndex: 'build_number'
                                    },
                                    {
                                        header: "Started",
                                        width: 71,
                                        dataIndex: 'actualStart',
                                        xtype: 'datecolumn',
                                        format: 'Y-m-d H:i'

                                    },
                                    {
                                        header: "Ended",
                                        width: 71,
                                        dataIndex: 'end',
                                        xtype: 'datecolumn',
                                        format: 'Y-m-d H:i'
                                    },
                                    {
                                        header: "dd:hh:mm",
                                        width: 58,
                                        dataIndex: 'duration'
                                    }
                                ]

                            },
                            {
                                title: 'Change log',
                                xtype: 'grid',
                                id: 'detailsChangeLogGridId',
                                store: Ext.create(
                                    'Ext.data.Store', {
                                        fields: [
                                            {
                                                name: 'v',
                                                type: 'string'
                                            },
                                            {
                                                name: 'd',
                                                type: 'date'
                                            },
                                            {
                                                name: 'u',
                                                type: 'string'
                                            },
                                            {
                                                name: 'o',
                                                type: 'string'
                                            },
                                            {
                                                name: 'n',
                                                type: 'string'
                                            }
                                        ],
                                        sorters: [
                                            {
                                                property: 'd',
                                                direction: 'DESC'
                                            }
                                        ]
                                    }),
                                border: false,
                                autoScroll: true,
                                multiSelect: false,
                                stateful: true,
                                stateId: 'detailsChangeLogGridId',
                                viewConfig: {
                                    emptyText: 'No changes to display'
                                },
                                columns: [
                                    {
                                        header: "Field",
                                        width: 140,
                                        dataIndex: 'v'
                                    },
                                    {
                                        header: "Date",
                                        width: 129,
                                        dataIndex: 'd',
                                        xtype: 'datecolumn',
                                        format: 'Y-m-d H:i'
                                    },
                                    {
                                        header: "User",
                                        width: 70,
                                        dataIndex: 'u'
                                    },
                                    {
                                        header: "Old",
                                        width: 135,
                                        dataIndex: 'o'
                                    },
                                    {
                                        header: "New",
                                        width: 136,
                                        dataIndex: 'n'
                                    }
                                ]

                            },
                            {
                                title: 'Notes',
                                xtype: 'grid',
                                id: 'detailsNotesGridId',
                                store: Ext.create(
                                    'Ext.data.Store', {
                                        fields: [
                                            {
                                                name: 'userName',
                                                type: 'string'
                                            },
                                            {
                                                name: 'dateCreated',
                                                type: 'date'
                                            },
                                            {
                                                name: 'comment',
                                                type: 'string'
                                            },
                                            {
                                                name: 'stepName',
                                                type: 'string'
                                            }
                                        ],
                                        sorters: [
                                            {
                                                property: 'dateCreated',
                                                direction: 'DESC'
                                            }
                                        ]
                                    }),
                                border: false,
                                autoScroll: true,
                                multiSelect: false,
                                stateful: true,
                                stateId: 'detailsNotesGridId',
                                viewConfig: {
                                    emptyText: 'No notes to display'
                                },
                                columns: [
                                    {
                                        header: "Note",
                                        width: 350,
                                        dataIndex: 'comment'
                                    },
                                    {
                                        header: "Date",
                                        width: 129,
                                        dataIndex: 'dateCreated',
                                        xtype: 'datecolumn',
                                        format: 'Y-m-d H:i'
                                    },
                                    {
                                        header: "User",
                                        width: 90,
                                        dataIndex: 'userName'
                                    },
                                    {
                                        header: "Step",
                                        width: 155,
                                        dataIndex: 'stepName'
                                    }
                                ]

                            },
                            {
                                title: 'Parts',
                                xtype: 'grid',
                                id: 'detailsPartsGridId',
                                store: Ext.create(
                                    'Ext.data.Store', {
                                        fields: [
                                            {
                                                name: 'nDetails',
                                                type: 'string'
                                            },
                                            {
                                                name: 'code',
                                                type: 'string'
                                            },
                                            {
                                                name: 'parentCode',
                                                type: 'string'
                                            },
                                            {
                                                name: 'productCode',
                                                type: 'string'
                                            },
                                            {
                                                name: 'product',
                                                type: 'string'
                                            }
                                        ],
                                        sorters: [
                                            {
                                                property: 'code',
                                                direction: 'ASC'
                                            }
                                        ]
                                    }),
                                border: false,
                                autoScroll: true,
                                multiSelect: false,
                                stateful: true,
                                stateId: 'detailsPartsGridId',
                                viewConfig: {
                                    emptyText: 'No parts to display'
                                },
                                columns: [
                                    {
                                        name: 'showPartsDetailsId',
                                        xtype: 'actioncolumn',
                                        dataIndex: 'nDetails',
                                        width: 28,
                                        text: 'D',
                                        items: [
                                            {
                                                getTip: function (v, meta, rec) {
                                                    return 'All unit details';
                                                },
                                                getClass: function (v, meta, rec) {
                                                    return 'column_details';
                                                }
                                            }
                                        ]
                                    },
                                    {
                                        header: "Serial#",
                                        width: 160,
                                        dataIndex: 'code'
                                    },
                                    {
                                        header: "Product code",
                                        width: 180,
                                        dataIndex: 'productCode'
                                    },
                                    {
                                        header: "Product name",
                                        width: 180,
                                        dataIndex: 'product'
                                    }
                                ]

                            }
                        ]
                    }
                ]
            },
            buttons: [
                {
                    text: 'Merge tests',
                    handler: function () {
                        var dc = Ext.getCmp('detailsDataDcGridId');
                        var source = dc.getSource();
                        var idxs = [];
                        Ext.iterate(source, function(key, value) {
                            if (key.indexOf('testDataIndex_') !== -1) {
                                idxs.push(key.split('_')[1]);
                            }
                        });
                        Ext.MessageBox.show({
                            title: 'Merge tests',
                            msg: 'Confirm list of text indexes to merge',
                            value: idxs,
                            width:300,
                            buttons: Ext.MessageBox.OKCANCEL,
                            multiline: true,  // this property is for multiline user input.
                            fn: function(btn, text) {
                                if (btn === 'ok') {
                                    Ext.Ajax.request({
                                        scope: this,
                                        method: 'GET',
                                        params: {
                                            code: code,
                                            idxs: text
                                        },
                                        url: rootFolder + 'units/mergeTestIndexes',
                                        success: function (response) {
                                            var obj = Ext.decode(response.responseText);
                                            if (obj.success == false) {
                                                Ext.Msg.alert('Merge failed', obj.msg);
                                            } else {
                                                source['testDataIndex_' + obj.res] = '<Units> Dev: -- Area: 0';
                                                dc.setSource(source);
                                            }
                                        }
                                    });
                                }
                            }
                        });

                    }
                },
                {
                    text: 'Top devices images',
                    handler: function () {
                        window.open(rootFolder + 'topImages?id=' + code, 'resizable,scrollbars');
                    }
                },
                {
                    text: 'Uniformity images',
                    handler: function () {
                        window.open(rootFolder + 'uniformityImages?id=' + code, 'resizable,scrollbars');
                    }
                },
                {
                    text: 'NiDot images',
                    handler: function () {
                        window.open(rootFolder + 'probeTestImages?id=' + code, 'resizable,scrollbars');
                    }
                },
                {
                    text: 'EPI Dashboard',
                    handler: function () {
                        window.open(rootFolder + 'photo?id=' + code, 'resizable,scrollbars');
                    }
                },
                {
                    text: 'FAB Dashboard',
                    handler: function () {
                        window.open(rootFolder + 'epidash?id=' + code, 'resizable,scrollbars');
                    }
                },
                {
                    text: 'Revive',
                    handler: function () {
                        var win = this.up('window');

                        Ext.Ajax.request({
                            scope: this,
                            method: 'POST',
                            params: {
                                code: code
                            },
                            url: rootFolder + 'units/revive',
                            success: function (response) {
                                var obj = Ext.decode(response.responseText);
                                if (obj.success == false) {
                                    Ext.Msg.alert('Revive can not complete', obj.msg);
                                } else {
                                    Ext.ux.Message.msg(obj.msg, '');
                                }
                            }
                        });
                    }
                },
                {
                    text: 'Close',
                    handler: function () {
                        this.up('window').destroy();
                    }
                }
            ]
        });

        Ext.Ajax.request({
            scope: this,
            method: 'GET',
            url: rootFolder + 'units/' + code,
            success: function (response) {

                var obj = Ext.decode(response.responseText);
                var obj2 = obj[0]
                obj2.steps = obj2.audit.length;
                obj2.changes = obj2.dataLog.length;
                obj2.nNotes = obj2.note.length;
                obj2.nFiles = obj2.files.length;
                var record = {
                    data: obj2,
                    getData: function () {
                        return this.data;
                    }
                }

                Ext.getCmp('detailsFormId').loadRecord(record);
                Ext.getCmp('detailsGridId').store.loadData(obj[0].audit);
                Ext.getCmp('detailsChangeLogGridId').store.loadData(obj[0].dataLog);
                Ext.getCmp('detailsNotesGridId').store.loadData(obj[0].note);
                if (obj[0].parts != undefined) {
                    Ext.getCmp('detailsPartsGridId').store.loadData(obj[0].parts);
                    //	Ext.getCmp('detailsPartsGridId').expand();	
                } else {
                    Ext.getCmp('detailsGridId').expand();
                    Ext.getCmp('detailsPartsGridId').setVisible(false);
                }

                Ext.getCmp('detailsGridId').getSelectionModel().on(
                    'selectionchange',
                    function (sm, selectedRecord) {
                        if (selectedRecord.length) {
                            var dc = Ext.getCmp('detailsDataDcGridId');
                            dc.pctg = selectedRecord[0].data.pctg;
                            dc.pkey = selectedRecord[0].data.pkey;
                            dc.tkey = selectedRecord[0].data.tkey;
                            dc.setSource(selectedRecord[0].data.dc);
                            var din = Ext.getCmp('detailsDinGridId');
                            din.setSource(selectedRecord[0].data.din);
                            var spec = Ext.getCmp('detailsSpecGridId');
                            spec.setSource(selectedRecord[0].data.spec);
                            var recp = Ext.getCmp('detailsDataRecipeGridId');
                            recp.setSource(selectedRecord[0].data.recp);
                            var loc = Ext.getCmp('detailsLocationGridId');
                            loc.setSource(selectedRecord[0].data);

                        }
                    });
            }
        });

        win.show();

        if (parentCode != null) {

            win.getDockedItems()[1].insert(0, {
                xtype: 'tbfill'
            });
            win.getDockedItems()[1].insert(0, {
                text: '< Back',
                handler: function () {
                    var cntrl = gloApp.getController('TbContent');
                    cntrl.showDetailsWindow(parentCode, null);
                }
            });
        }
    },

    onDownloadFile: function (grid, cell, row, col, e) {
        var rec = grid.getStore().getAt(row);

        try {
            Ext.destroy(Ext.get('downloadIframe'));
        } catch (e) {
        }
        Ext.core.DomHelper.append(document.body, {
            tag: 'iframe',
            id: 'downloadIframe',
            frameBorder: 0,
            width: 0,
            height: 0,
            css: 'display:none;visibility:hidden;height:0px;',
            src: rootFolder + 'files/download?file='
                + rec.get('fileName')
        });
    },

    onContentSelect: function (o, selections) {
        this.contentSelect(o, selections);
    },

    onSearchStep: function (thisField, newValue, oldValue, options) {

        this.searchStep2(thisField, newValue);
    },

    searchStep: function (thisField, newValue) {

        var panel = thisField.ownerCt.ownerCt;
        var grid = panel.items.get(0);
        if (grid) {
            grid.store.currentPage = 1;
            this.loadGrid(newValue, grid.category, "SEARCH");
        }
    },

    contentSelect: function (o, selections) {

        var selected = selections[0];
        this.showHideActionButtons(o.id, selected);
    },

    loadContentGrid: function (category, taskName, taskKey, procKey, searchText) {

         if (window.start !== '1') {
            window.start = '1';

            var panel = Ext.ComponentQuery.query('#mainpanel_' + category
                + ' > taskbookcontent')[0];

            this.createGrid(category, procKey, taskKey);
        }
    },

    loadTransitionBar: function (panelId, processKey, taskKey) {

        var transtool = this.removeActionButtons(panelId);
        if (!processKey)
            return;
        Ext.Ajax.request({
            scope: this,
            method: 'GET',
            params: {
                processKey: processKey,
                taskKey: taskKey
            },
            url: rootFolder + 'transitions',
            success: function (response) {
                var obj = Ext.decode(response.responseText);

                Ext.Object.each(obj, function (key, value, myself) {

                    transtool.add({
                        name: value.name,
                        xtype: 'button',
                        text: value.name
                            ? value.name
                            : value.id,
                        toggleGroup: 'navTrans',
                        enableToggle: false,
                        pressed: false,
                        hidden: true,
                        processKey: processKey,
                        taskKey: value.taskKey,
                        taskKeySource: taskKey,
                        cls: value.mpath == 'yes'
                            ? 'mandatory-column'
                            : '',
                        category: value.pctg,
                        action: 'executeTransition',
                        iconCls: value.icon
                    });

                });

                window.start = '0';
            },
            failure: function (response) {

                window.start = '0';
            }
        });

    },

    removeActionButtons: function (panelId) {

        var transButtons = Ext.ComponentQuery.query('#' + panelId
            + ' > toolbar > button[action=executeTransition]');

        Ext.Object.each(transButtons, function (node) {

            var n = transButtons[node];

            if (n != undefined) {
                n.hide();
                n.destroy();
            }
        });

        var ret = Ext.ComponentQuery.query('#' + panelId + ' > toolbar')[0];

        return ret;
    },

    showHideActionButtons: function (panelId, selection) {

        var transButtons = Ext.ComponentQuery.query('#' + panelId
            + ' > toolbar > button[action=executeTransition]');

        Ext.Object.each(transButtons, function (node) {
            var n = transButtons[node];
            if (n != undefined) {
                if (selection == undefined) {
                    n.hide();
                } else {
                    n.show();
                }
            }
        });
    },

    loadGrid: function (searchText, pctg, panelId, pkey, tkey) {

        var grid = Ext.getCmp('contentGrid_' + pctg);

        var panel = grid.up('panel');
        if (panel != undefined) panel.setLoading(true);

        grid.store.getProxy().extraParams.category = grid.category;
        grid.store.getProxy().extraParams.procKey = grid.procKey;
        grid.store.getProxy().extraParams.taskKey = grid.taskKey;
        grid.store.getProxy().extraParams.searchText = searchText;
        grid.store.load({
            scope: this,
            callback: function (records, operation, options) {

                if (panelId != undefined) {
                    this.loadTransitionBar(panelId, pkey, tkey);
                }
                if (panel != undefined) panel.setLoading(false);
            }
        });
    },

    createGrid: function (pctg, pkey, tkey) {

        var updateAlreadyActive = false;
        var panel = Ext.ComponentQuery.query('#mainpanel_' + pctg
            + ' > taskbookcontent')[0];

        if (panel == undefined)
            return;

        Ext.Ajax.request({
            scope: this,
            method: 'GET',
            params: {
                pkey: pkey,
                tkey: tkey,
                pctg: pctg
            },
            url: rootFolder + 'contents',
            success: function (response) {

                var obj = Ext.decode(response.responseText);

                var store =  Ext.create('Ext.data.Store', {
                    fields: obj.fields,
                    autoDestroy: false,
                    autoLoad: false,
                    autoSync: true,
                    pageSize: '50',
                    remoteSort: true,
                    proxy: {
                        type: 'rest',
                        url: rootFolder + 'units',
                        actionMethods: {
                            read: 'GET',
                            create: 'POST',
                            update: 'PUT',
                            destroy: 'DELETE'
                        },
                        startParam: 'offset',
                        pageParam: 'page',
                        dirParam: 'order',
                        limitParam: 'max',
                        simpleSortMode: true,

                        reader: {
                            type: 'json',
                            root: 'data',
                            totalProperty: 'count',
                            messageProperty: 'msg'
                        },
                        writer: new Ext.data.JsonWriter({
                            encode: false
                        })
                    },
                    listeners: {
                        write: function (store, operation, eOpts) {
                            var obj = Ext.decode(operation.response.responseText);
                            if (obj.ok == false) {
                                Ext.Msg.alert('Update failed', obj.msg);
                            }
                        }
                    }
                });

                var sm = new Ext.selection.CheckboxModel({
                    listeners: {
                        scope: panel,
                        selectionchange: function (selectionModel, selected, options) {

                            this.fireEvent('contentselect', this,
                                selected);
                        }
                    }
                });

                var pagingBar = Ext.create('Ext.PagingToolbar', {
                    store: store,
                    displayInfo: true,
                    displayMsg: 'Displaying units {0} - {1} of {2}',
                    emptyMsg: "No units to display (Check filter above)",
                    items: ['-', '-', 'Per Page: ', {
                        xtype: 'combo',
                        name: 'perpage',
                        width: 56,
                        store: new Ext.data.ArrayStore({
                            fields: ['id'],
                            data: [
                                ['25'],
                                ['50'],
                                ['100'],
                                ['200'],
                                ['500'],
                                ['1000']
                            ]
                        }),
                        mode: 'local',
                        value: '50',

                        listWidth: 50,
                        triggerAction: 'all',
                        displayField: 'id',
                        valueField: 'id',
                        editable: false,
                        forceSelection: true,
                        listeners: {
                            select: function () {
                                pagingBar.pageSize = parseInt(this.value, 10);
                                pagingBar.store.pageSize = parseInt(this.value, 10);
                                pagingBar.store.load();
                            }
                        }
                    }]
                });

                var noteColumn = {
                    name: 'showNotesId',
                    xtype: 'actioncolumn',
                    dataIndex: 'nNotes',
                    stateId: 'grd6note',
                    text: 'N',
                    width: 28,
                    items: [
                        {
                            getTip: function (v, meta, rec) {
                                if (rec.get('nNotes') > 0) {
                                    return rec.get('nNotes') + ' note(s)';
                                } else {
                                    return 'Click to add note';
                                }
                            },
                            getClass: function (v, meta, rec) {

                                if (rec.get('nNotes') > 0) {
                                    return 'column_comments';
                                } else {
                                    return 'column_nocomments';
                                }
                            }
                        }
                    ]
                };

                var fileColumn = {
                    name: 'showFilesId',
                    xtype: 'actioncolumn',
                    stateId: 'grd6file',
                    dataIndex: 'nFiles',
                    text: 'F',
                    width: 28,
                    items: [
                        {
                            getTip: function (v, meta, rec) {
                                if (rec.get('nFiles') > 0) {
                                    return rec.get('nFiles') + ' file(s)';
                                } else {
                                    return 'No files';
                                }
                            },
                            getClass: function (v, meta, rec) {

                                if (rec.get('nFiles') > 0) {
                                    return 'column_files';
                                } else {
                                    return 'column_nofiles';
                                }
                            }
                        }
                    ]
                };

                var detailColumn = {
                    name: 'showDetailsId',
                    xtype: 'actioncolumn',
                    stateId: 'grd6detail',
                    dataIndex: 'nDetails',
                    width: 28,
                    text: 'D',
                    items: [
                        {
                            getTip: function (v, meta, rec) {
                                return 'All unit details';
                            },
                            getClass: function (v, meta, rec) {

                                return 'column_details';
                            }
                        }
                    ]
                };

                var jumpColumn = {
                    name: 'jumpToChildrenId',
                    xtype: 'actioncolumn',
                    stateId: 'grd7jump',
                    dataIndex: 'nJumps',
                    text: 'J',
                    width: 28,
                    items: [
                        {
                            getTip: function (v, meta, rec) {
                                if (rec.get('nJumps') == 1) {
                                    return 'Jump to coupons';
                                } else if (rec.get('nJumps') == 2) {
                                    return 'Jump to wafer';
                                } else {
                                    return '';
                                }
                            },
                            getClass: function (v, meta, rec) {
                                if (rec.get('nJumps') == 1) {
                                    return 'column_jumps_down';
                                } else if (rec.get('nJumps') == 2) {
                                    return 'column_jumps_up';
                                } else {
                                    return 'column_nofiles';
                                }
                            }
                        }
                    ]
                };


                Ext.Object.each(obj.columns, function (record) {
                    if (obj.columns[record].field != undefined && obj.columns[record].field.metaName != undefined) {

                        obj.columns[record].field.store = Ext.create('glo.store.MetaItems', {
                            pageSize: 500
                        });

                        obj.columns[record].field.store.getProxy().extraParams.metaName = obj.columns[record].field.metaName;

                        // Custom rendering template for each item
                        Ext.apply(obj.columns[record].field, {
                            listConfig: {
                                getInnerTpl: function () {
                                    return '<p>{name}</p>';
                                }
                            }
                        });

                        Ext.apply(obj.columns[record], {
                            renderer: function (value) {
                                if (value != 0 && value != "") {
                                    if (obj.columns[record].field.store.findRecord("id", value) != null)
                                        return obj.columns[record].field.store.findRecord("id", value).get('name');
                                    else
                                        return value;
                                }
                                else
                                    return "";
                            }
                        });

                    }
                });

                var filters = {
                    ftype: 'filters',
                    // encode and local configuration options defined previously for easier reuse
                    encode: true, // json encode the filter query
                    local: false  // defaults to false (remote filtering)
                };

                obj.columns[0] = detailColumn;
                obj.columns[1] = noteColumn;
                obj.columns[2] = fileColumn;
                obj.columns[3] = jumpColumn;

                var grid = Ext.create('Ext.grid.Panel', {
                    id: 'contentGrid_' + pctg,
                    store: store,
                    defaults: {
                        sortable: true
                    },
                    border: false,
                    autoScroll: true,
                    multiSelect: true,
                    stateful: true,
                    selModel: sm,
                    padding: 0,
                    stateId: 'contentUnitGrid6_' + tkey,
                    viewConfig: {
                        emptyText: 'No units to display (Check filter above)',
                        getRowClass: function (record, rowIndex, p, store) {

                            if (record.data.ok == "1")
                                return 'row_yellow_background';
                            if (record.data.ok == "2" || record.data.ok == "4") {
                                return 'row_red_background';
                            }
                            if (record.data.ok == "3") {
                                return 'row_red_background';
                            }
                            return '';
                        }
                    },
                    plugins: [
                        Ext.create('Ext.grid.plugin.CellEditing',
                        {
                            clicksToEdit: 1
                        })
//                        ,
//                        Ext.create('Ext.ux.grid.RowExpander', {
//                            rowBodyTpl: new Ext.XTemplate(
//                                '<p><b>Company:</b> sss</p>'
//                            )
//                        })
                    ],
                    features: [filters],
                    columns: obj.columns,
                    loadMask: true,
                    category: pctg,
                    procKey: pkey,
                    taskKey: tkey,
                    bbar: pagingBar,
                    listeners: {
                        'itemcontextmenu': function (grd, record, item, index, e) {
                            e.stopEvent();
                            if (record.data.ok == "2" || record.data.ok == "4") {
                                var menu = Ext.create('Ext.menu.Menu', {
                                    items: {
                                        text: '<b>NOT WITHIN SPEC LIMIT:</b><p>' + record.data.specs + '</p>'
                                    }
                                });
                                menu.showAt(e.xy);
                            }
                            if (record.data.ok == "3") {
                                var menu = Ext.create('Ext.menu.Menu', {
                                    items: {
                                        text: '<b>INVALID DATA:</b><p>' + record.data.specs + '</p>'
                                    }
                                });
                                menu.showAt(e.xy);
                            }
                        }
                    }

                });

                if (panel != undefined) {
                    Ext.suspendLayouts();
                    panel.removeAll(true);
                    panel.add(grid);
                    Ext.resumeLayouts();
                }


                var cmp = Ext.ComponentQuery.query('#mainpanel_' + pctg
                    + ' > taskbookcontent textfield[name=searchPerStepId]')[0];

                this.loadGrid(cmp.value, pctg, panel.id, pkey, tkey);
            },
            failure: function (response) {

                Ext.Msg.alert(response.responseText);
            }
        });

    }

});