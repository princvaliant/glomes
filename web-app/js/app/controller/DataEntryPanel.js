Ext.define('glo.controller.DataEntryPanel', {
    extend: 'Ext.app.Controller',
    views: ['glo.view.dataEntry.Content'],

    init: function () {

        this.control({
            'dataentrylist': {
                afterrender: this.onPanelRendered,
                dataentryselect: this.onDataEntrySelect
            },
            'dataentrylist button[action=refreshdataentrylist]': {
                click:  this.onRefreshDataEntryList
            },
            'dataentrylist textfield[id=searchDataEntryList]': {
                change: this.onListSearch
            },
            'dataentrycontent textfield[name=searchDataEntryContent]': {
                change: this.onContentSearch
            },
            'dataentrycontent': {
                dataentryadd: this.onDataEntryAdd
            },
            'combobox[action=equipmentDcSelect]': {
                select: this.onEquipmentDcSelect
            },
            'actioncolumn[name=editDataEntryId]': {
                click: this.onDataEntryEdit
            },
            'actioncolumn[name=deleteDataEntryId]': {
                click: this.onDataEntryDelete
            }
        });
    },


    onPanelRendered: function (p, e) {

        this.createListGrid();
    },

    onListSearch: function (thisField, newValue, oldValue, options) {

        var panel = thisField.ownerCt.ownerCt;
        var grid = panel.items.get(0);

        grid.store.currentPage = 1;
        grid.store.getProxy().extraParams.search = newValue;
        grid.store.load()
    },

    onContentSearch: function (thisField, newValue, oldValue, options) {

        var grid = Ext.ComponentQuery.query('#dataentrypanel > dataentrycontent gridpanel')[0];
        if (grid !== undefined) {
             grid.store.currentPage = 1;
            grid.store.getProxy().extraParams.search = newValue;
            grid.store.load();
        }
    },

    onDataEntrySelect: function (o, selections) {

        if (selections[0] !== undefined) {
            var sel = selections[0].data;
            this.createContentGrid(sel.code, sel.equipment, sel.qty);
        }
    },

    onRefreshDataEntryList: function (btn) {

        this.refreshListData();
    },

    onEquipmentDcSelect: function (o, selections) {

        var sel = selections[0].data;

        // Determine if input requires additional input attributes
        Ext.Ajax.request({
            scope: this,
            method: 'GET',
            params: {
                code: sel.code
            },
            url: rootFolder + 'dataEntry/generateRun',
            success: function (response) {
                var obj = Ext.decode(response.responseText);
                Ext.getCmp('equipmentDcRunNumber').setValue(obj.data);
            }
        });
        Ext.Ajax.request({
            scope: this,
            method: 'GET',
            params: {
                procKey: sel.code,
                taskKey: '',
                category: 'EquipmentDC',
                allow: false
            },
            url: rootFolder + 'contents/dc',
            success: function (response) {
                var obj = Ext.decode(response.responseText);

                Ext.suspendLayouts();

                this.addControls(obj, Ext.getCmp('dataEntryFormId'));

                Ext.resumeLayouts(true);
            }
        });
    },


    onDataEntryAdd: function (btn) {

        var grid = Ext.ComponentQuery.query('#dataentrypanel > dataentrycontent gridpanel')[0];

        var win = Ext.create('Ext.window.Window', {
            title: 'Add equipment Run data',
            resizable: true,
            draggable: false,
            modal: true,
            y: 20,
            x: 50,
            width: 1000,
            layout: 'fit',
            items: {
                id: 'dataEntryFormId',
                xtype: 'dataentryform',
                dataEntryName: grid ? grid.dataEntryName : '',
                dataEntryCode: grid ? grid.dataEntryCode : '',
                parentWindow: btn.ownerCt.ownerCt.ownerCt,
                rid: 0
            }
        });

        win.show();
    },

    onDataEntryEdit: function (grid, cell, row, col, e) {

        var grid2 = Ext.ComponentQuery.query('#dataentrypanel > dataentrycontent gridpanel')[0];
        var rec = grid.getStore().getAt(row);

        var win = Ext.create('Ext.window.Window', {
            title: 'Edit equipment Run data',
            resizable: true,
            modal: true,
            draggable: false,
            y: 20,
            x: 50,
            width: 1000,
            layout: 'fit',
            items: {
                id: 'dataEntryFormId',
                xtype: 'dataentryform',
                parentWindow: grid.ownerCt.ownerCt,
                rid: rec.data._id
            }
        });

        // Determine if input requires additional input attributes
        Ext.Ajax.request({
            scope: this,
            method: 'GET',
            params: {
                procKey: grid2.dataEntryCode,
                taskKey: '',
                category: 'EquipmentDC',
                allow: false
            },
            url: rootFolder + 'contents/dc',
            success: function (response) {
                var obj = Ext.decode(response.responseText);

                Ext.suspendLayouts();

                this.addControls(obj, Ext.getCmp('dataEntryFormId'));

                Ext.resumeLayouts(true);

                Ext.Ajax.request({
                    scope: this,
                    method: 'GET',
                    params: {
                        id: rec.data._id,
                        code: grid2.dataEntryCode
                    },
                    url: rootFolder + 'dataEntry/get',
                    success: function (response) {
                        var obj = Ext.decode(response.responseText);
                        obj.getData = function () {
                            return this.data;
                        };
                        Ext.getCmp('equipmentDcSelectId').setValue(obj.data.equipmentName);
                        Ext.getCmp('equipmentDcSelectId').readOnly = true;
                        Ext.getCmp('equipmentDcRunNumber').setValue(obj.data.runNumber);
                        Ext.getCmp('dataEntryFormId').name = obj.data.runNumber;
                        Ext.getCmp('dataEntryFormId').tag = obj.data.runNumber;
                        Ext.getCmp('dataEntryFormId').loadRecord(obj);

                    }
                });
            }
        });

        win.show();
    },

    onDataEntryDelete: function (grid, cell, row, col, e) {

        var rec = grid.getStore().getAt(row);

        Ext.MessageBox.confirm('Confirm delete', 'Delete selected equipment Run(s)?',
            function (btn) {
                if (btn == 'yes') {

                    Ext.Ajax.request({
                        scope: this,
                        method: 'DELETE',
                        params: {
                            id: rec.data._id
                        },
                        url: rootFolder + 'dataEntry/getContentData/' + rec.data._id,
                        success: function (response) {
                            var obj = Ext.decode(response.responseText);
                            if (obj.success == false) {
                                Ext.Msg.alert('Delete status failed', obj.msg);
                            } else {
                                grid.store.remove(rec);
                                gloApp.getController('DataEntryPanel').refreshListData();
                            }
                        }
                    });
                }
            });
    },

    createListGrid: function () {

        var panel = Ext.ComponentQuery.query('#dataentrypanel > dataentrylist')[0];

        Ext.Ajax.request({
            scope: panel,
            method: 'GET',
            params: {
                domain: 'dataEntry'
            },
            url: rootFolder + 'dataEntry/getListFields',
            success: function (response) {

                var obj = Ext.decode(response.responseText);
                var columns = obj.columns;
                var store = new Ext.data.Store({
                    id: 'dataStoreDataENtry',
                    fields: obj.fields,
                    autoDestroy: false,
                    autoLoad: true,
                    autoSync: true,
                    pageSize: '25',
                    remoteSort: true,
                    proxy: {
                        type: 'rest',
                        url: rootFolder + 'dataEntry/getListData',
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
                        }
                    }
                });

                var sm = new Ext.selection.RowModel({
                    listeners: {
                        scope: panel,
                        selectionchange: function (selectionModel, selected, options) {
                            this.fireEvent('dataentryselect', this, selected);
                        }
                    }
                });

                var grid = new Ext.grid.GridPanel({
                    id: 'dataEntryListGridId',
                    store: store,
                    defaults: {
                        sortable: true
                    },
                    border: false,
                    autoScroll: true,
                    multiSelect: false,
                    stateful: true,
                    padding: 0,
                    stateful: true,
                    stateId: 'stateDataEntryListGrid',
                    selModel: sm,
                    viewConfig: {
                        emptyText: 'No data entries to display'
                    },
                    columns: columns
                });

                panel.removeAll(true);
                panel.add(grid);
            }
        });

    },

    createContentGrid: function (code, equipment, qty) {

        var panel = Ext.ComponentQuery.query('#dataentrypanel > dataentrycontent')[0];
        if (panel == undefined)
            return;
        panel.setLoading(true);
        panel.setTitle( '"' + equipment + '" runs ');

        Ext.Ajax.request({
            scope: this,
            method: 'GET',
            params: {
                pkey: code,
                tkey: '',
                pctg: 'EquipmentDC'
            },
            url: rootFolder + 'contents',
            success: function (response) {

                var obj = Ext.decode(response.responseText);
                obj.fields.push({name: 'dateCreated', dataType: 'date'});
                obj.fields.push({name: '_id'});
                obj.fields.push({name: 'ok'});
                obj.fields.push({name: 'specs'});

                var store = new Ext.data.Store({
                    fields: obj.fields,
                    autoDestroy: false,
                    autoLoad: false,
                    autoSync: false,
                    pageSize: '25',
                    remoteSort: true,
                    proxy: {
                        type: 'rest',
                        url: rootFolder + 'dataEntry/getContentData',
                        actionMethods: {
                            read: 'GET'
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
                        }
                    }
                });

                var sm = new Ext.selection.RowModel({
                    listeners: {
                        scope: panel,
                        selectionchange: function (selectionModel, selected, options) {
                        }
                    }
                });

                var pagingBar = Ext.create('Ext.PagingToolbar', {
                    store: store,
                    displayInfo: true,
                    displayMsg: 'Displaying entries {0} - {1} of {2}',
                    emptyMsg: "No entries to display (Check filter above)",
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
                        value: '25',

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

                obj.columns[0] = {
                    id: 'editDataEntryId',
                    name: 'editDataEntryId',
                    xtype: 'actioncolumn',
                    text: '',
                    width: 22,
                    items: [
                        {
                            getClass: function (v, meta, rec) {
                                return 'column_edit';
                            }
                        }
                    ]
                };
//                obj.columns[1] = {
//                    id: 'deleteDataEntryId',
//                    name: 'deleteDataEntryId',
//                    xtype: 'actioncolumn',
//                    text: '',
//                    width: 22,
//                    items: [
//                        {
//                            getClass: function (v, meta, rec) {
//                                return 'column_delete';
//                            }}
//                    ]
//                };
                obj.columns[2] = {
                    id: 'showDateCreated',
                    name: 'showDateCreated',
                    stateId: 'grd6dataentrydateCreated',
                    dataIndex: 'dateCreated',
                    width: 100,
                    text: 'Date created',
                    xtype: 'datecolumn',
                    format: 'Y-m-d H:i'
                };

                var grid = Ext.create('Ext.grid.Panel', {
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
                    dataEntryCode: code,
                    dataEntryName: equipment,
                    stateId: 'stateDataEntryContentGrid6' + code,
                    viewConfig: {
                        emptyText: 'No entries to display (Check filter above)',
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
                    columns: obj.columns,
                    category: 'EquipmentDC',
                    procKey: code,
                    taskKey: '',
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
                    panel.removeAll(true);
                    panel.add(grid);
                }

                var cmp = Ext.ComponentQuery.query('#dataentrypanel > dataentrycontent  textfield[name=searchDataEntryContent]')[0];

                panel.setLoading(false);

                this.loadContentGrid(cmp.value, 'EquipmentDC', panel.id, code, qty);

            },
            failure: function (response) {

                Ext.Msg.alert(response.responseText);
            }
        });

    },

    loadContentGrid: function (searchText, pctg, panelId, pkey, total) {

        var grid = Ext.ComponentQuery.query('#dataentrypanel > dataentrycontent > gridpanel')[0];
        var panel = grid.up('panel');
        if (panel != undefined) panel.setLoading(true);

        grid.store.getProxy().extraParams.category = pctg;
        grid.store.getProxy().extraParams.procKey = pkey;
        grid.store.getProxy().extraParams.searchText = searchText;
        grid.store.getProxy().extraParams.total = total;
        grid.store.load({
            scope: this,
            callback: function (records, operation, options) {

                if (panel != undefined) panel.setLoading(false);
            }
        });
    },

    addControls: function (obj, frm) {

        var i = 0;
        var fs;
        frm.removeAll(true);
        frm.visible = false;

        for (ctrl in obj.controls) {
            if (i % 18 == 0) {
                var j = i + 18;
                if (obj.controls.length - 18 < i)
                    j = obj.controls.length;
                fs = Ext.create('Ext.form.FieldSet', {
                    name: 'fs' + i,
                    title: 'Edit fields ' + (i + 1).toString() + ' - '
                        + j.toString(),
                    style: 'padding: 0 3px 6px 3px;margin:3 3 3 3;',
                    bodyStyle: 'padding-top: '
                        + (Ext.isIE ? '0' : '10px')
                });
                frm.add(fs);
            }
            fs.add(obj.controls[ctrl]);
            i++;
        }
        frm.visible = true;
    },

    refreshContentData: function () {
        var grid = Ext.ComponentQuery.query('#dataentrypanel > dataentrycontent gridpanel')[0];
        grid.store.load();
    },
    refreshListData: function () {
        var grid = Ext.getCmp('dataEntryListGridId');
        grid.store.load();
    }

})
;