Ext.define('glo.view.dataView.Content', {
    extend: 'Ext.Panel',
    alias: 'widget.dataviewcontent',
    requires: ['Ext.ux.PreviewPlugin', 'Ext.toolbar.Toolbar', 'glo.view.dataView.FormulaForm',
        'Ext.form.field.ComboBox'],
    border: true,
    layout: 'fit',
    margins: '3 2 3 2',
    title: 'Selected data view fields',

    initComponent: function () {

        var store = new Ext.data.Store({
            fields: ['id', 'variableId', 'dataViewId', 'idx', 'name', 'path', 'step',
                'title', 'filter', 'type', 'isFormula', 'dir', 'formula'],
            autoDestroy: false,
            autoLoad: false,
            autoSync: true,
            pageSize: '25',
            remoteSort: true,
            proxy: {
                type: 'rest',
                url: rootFolder + 'variables/dataView',
                actionMethods: {
                    read: 'GET',
                    create: 'POST',
                    update: 'PUT'
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
                listeners: {
                    exception: function (proxy, response, operation) {
                        if (operation) {
                            store.rejectChanges();
                            Ext.Msg.alert('Failed', operation.error);
                        }
                    }
                },
                writer: new Ext.data.JsonWriter({
                    encode: false
                })
            }
        });

        var deleteColumn = {
            id: 'deleteVariableFromDataViewId',
            xtype: 'actioncolumn',
            text: 'N',
            width: 28,
            items: [
                {
                    getClass: function (v, meta, rec) {
                        return 'column_delete';
                    }
                }
            ]
        };

        var editColumn = {
            id: 'editVariableFromDataViewId',
            xtype: 'actioncolumn',
            text: 'F',
            width: 28,
            items: [
                {
                    getClass: function (v, meta, rec) {
                        if (rec.get('isFormula') == true) {
                            return 'column_edit';
                        }
                    }
                }
            ],
            handler: function (grid, rowIndex, colIndex) {

                var rec = grid.getStore().getAt(rowIndex);

                var win = Ext.create('Ext.window.Window', {
                    title: 'Formula editor',
                    modal: true,
                    y: 180,
                    width: 1200,
                    layout: 'fit',
                    items: {
                        id: 'dataViewFormulaFormId',
                        xtype: 'formulaform',
                        store: grid.getStore(),
                        dataViewVariableId: rec.get('id'),
                        formula: rec.get('formula'),
                        formulaTitle: rec.get('title'),
                        parentWindow: grid.ownerCt.ownerCt.ownerCt
                    }
                });

                win.show();


            }
        };

        var grid = new Ext.grid.GridPanel({
            name: 'gridVariablesPerDataView',
            store: store,
            border: false,
            minHeight: 400,
            autoScroll: true,
            multiSelect: false,
            disabled: true,
            stateful: false,
            stateId: 'gridVariablesPerDataView',
            viewConfig: {
                emptyText: 'No fields for data view',
                forceFit: true,
                plugins: {
                    ptype: 'gridviewdragdrop',
                    allowCopy: true,
                    copy: true,
                    dragText: 'Drag and drop to reorganize',
                    ddGroup: 'GridDD'
                },
                preserveScrollOnRefresh: true,
                listeners: {

                    beforedrop: function (node, data, overModel, dropRec, dropPosition) {

                        var info = data.records[0].data;
                        if (info.id.toString().substring(0, 1) == "L"
                            || info.id == '' || (info.qtip != undefined && info.qtip.indexOf('<b>object</b>') > 0)) {
                            return false;
                        }
                        return 0;
                    },
                    drop: function (node, data, dropRec, dropPosition) {

                        var info = data.records[0].data;
                        if (info.variableId != undefined) {
                            Ext.Ajax.request({
                                scope: this,
                                method: 'POST',
                                params: {
                                    droppedId: dropRec.data.id,
                                    draggedId: info.id,
                                    droppedPosition: dropPosition,
                                    dataViewId: this.store.getProxy().extraParams.dataView
                                },
                                url: rootFolder + 'dataViews/reorderVariables',
                                success: function (response) {
                                    var obj = Ext.decode(response.responseText);

                                    if (obj.success == false) {
                                        Ext.Msg.alert(
                                            'Can not reorder variables',
                                            obj.msg);
                                    } else {
                                        this.store.load();
                                    }
                                }
                            });

                        } else {
                            // New variable attach
                            Ext.Ajax.request({
                                scope: this,
                                method: 'POST',
                                params: {
                                    droppedId: dropRec != undefined
                                        ? dropRec.data.id
                                        : 0,
                                    droppedPosition: dropPosition,
                                    variableId: info.id,
                                    dataViewId: this.store.getProxy().extraParams.dataView
                                },
                                url: rootFolder + 'dataViews/attachVariable',
                                success: function (response) {
                                    var obj = Ext.decode(response.responseText);

                                    if (obj.success == false) {
                                        Ext.Msg.alert(
                                            'Can not attach variable',
                                            obj.msg);
                                    } else {
                                        this.store.load();
                                    }
                                }
                            });
                        }
                        return 0;
                    }
                }
            },
            plugins: [Ext.create('Ext.grid.plugin.RowEditing', {
                clicksToMoveEditor: 1,
                autoCancel: false,
                errorSummary: false
            })],
            columns: [
                {
                    text: "Name",
                    flex: 3,
                    sortable: false,
                    dataIndex: 'name'
                },
                {
                    text: "Path",
                    flex: 3,
                    sortable: false,
                    dataIndex: 'path'
                },
                {
                    text: "Title",
                    flex: 3,
                    sortable: false,
                    dataIndex: 'title',
                    field: {
                        xtype: 'textfield',
                        allowBlank: false
                    }
                },
                {
                    text: "Filter",
                    flex: 4,
                    sortable: false,
                    dataIndex: 'filter',
                    field: {
                        xtype: 'combo',
                        store: ['between x,y', '> x', '< x', 'not blank', 'blank', 'in x,y..', 'not in x,y..', 'begin with x,y..', 'between yyyy-mm-dd,yyyy-mm-dd', 'last x days', 'next x days', 'last x work weeks', '']
                    }
                },
                editColumn,
                deleteColumn
            ]
        });

        var pbar = Ext.create('Ext.ProgressBar', {
            id: 'pbar3',
            anchor: '95%'
        });

        this.addEvents('exportdataview');
        this.addEvents('uploadexceltemplate');
        this.addEvents('addformula');

        Ext.apply(this, {
            items: [grid],
            dockedItems: {
                xtype: 'toolbar',
                id: 'dataViewContentToolbar',
                disabled: true,
                items: [
                    {
                        text: 'Upload excel template',
                        iconCls: 'icon-file_upload',
                        scope: this,
                        handler: function () {
                            this.fireEvent('uploadexceltemplate', this);
                        }
                    },
                    {
                        text: 'Add formula field',
                        iconCls: 'icon-calculator_add',
                        scope: this,
                        handler: function () {

                            this.fireEvent('addformula', this);
                        }
                    },
                    '->',
                    {
                        text: 'Export to excel',
                        iconCls: 'icon-excel',
                        scope: this,
                        handler: function () {
                            this.fireEvent('exportdataview', this);
                        }
                    }
                ]
            }
        });


        this.callParent(arguments);
    }
});
