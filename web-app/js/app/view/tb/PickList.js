Ext.define('glo.view.tb.PickList', {
    extend: 'Ext.Panel',
    alias: 'widget.picklist',
    requires: ['Ext.ux.PreviewPlugin', 'Ext.toolbar.Toolbar', 'Ext.form.field.ComboBox'],
    border: false,
    width: 900,
    height: 550,
    layout: {
        type: 'hbox',
        align: 'stretch',
        padding: 5
    },
    autoScroll: false,

    initComponent: function () {

        this.search2 = Ext.Function.createBuffered(this.search, 550, this);

        var ddGroup = this.id + 'group1';
        var columns = [
                {
                    text: 'Serial#',
                    flex: 2,
                    sortable: true,
                    dataIndex: 'code'
                },
                {
                    text: 'Prod. code',
                    flex: 1,
                    sortable: true,
                    dataIndex: 'productCode'
                },
                {
                    text: 'Product',
                    flex: 2,
                    sortable: true,
                    dataIndex: 'product'
                }
            ];

        var columns2 = Ext.clone(columns);
        columns2[3] = {id : 'deleteBomChildId',
            xtype : 'actioncolumn',
            text : '',
            width : 22,
            items : [{
                getClass : function(v, meta, rec) {
                    return 'column_delete';
                }
            }],
            listeners : {
                click: function(grid, cell, row, col, e) {

                    var rec = grid.getStore().getAt(row);
                    Ext.Ajax.request({
                        scope : this,
                        method : 'POST',
                        params : {
                            code  : rec.get('code')
                        },
                        url : rootFolder + 'units/unmerge',
                        success : function(response) {
                            var obj = Ext.decode(response.responseText);
                            if (obj.success == false) {
                                Ext.Msg.alert( 'UnMerge failed: ',obj.msg);
                            } else {
                                grid.store.load();
                                Ext.ux.Message.msg( 'UnMerge completed.', '');
                            }
                        }
                    });
                }
            }
        };

        var partsStore = new Ext.data.Store({
            model: 'glo.model.Unit',
            autoDestroy : false,
            autoLoad : false,
            autoSync : false,
            remoteSort : false,
            pageSize: 300,
            proxy : {
                type : 'rest',
                url : rootFolder + 'units/children',
                actionMethods : {
                    read : 'GET'
                },
                startParam : 'offset',
                pageParam : 'page',
                dirParam : 'order',
                limitParam : 'max',
                simpleSortMode : true,
                reader : {
                    type : 'json',
                    root : 'data',
                    totalProperty : 'count',
                    messageProperty : 'msg'
                }
            }
        });

        var searchStore = new Ext.data.Store({
            model: 'glo.model.Unit',
            autoDestroy : false,
            autoLoad : false,
            autoSync : false,
            remoteSort : false,
            pageSize: 300,
            proxy : {
                type : 'rest',
                url : rootFolder + 'units',
                actionMethods : {
                    read : 'GET'
                },
                startParam : 'offset',
                pageParam : 'page',
                dirParam : 'order',
                limitParam : 'max',
                simpleSortMode : true,

                reader : {
                    type : 'json',
                    root : 'data',
                    totalProperty : 'count',
                    messageProperty : 'msg'
                }
            }
        });


        Ext.apply(this, {
            items: [
                {
                    name: 'unitParts',
                    flex: 1,
                    xtype: 'grid',
                    multiSelect: true,
                    viewConfig: {
                        plugins: {
                            ptype: 'gridviewdragdrop',
                            ddGroup: ddGroup
                        },
                        listeners: {
                            drop: function (node, data) {
                                Ext.ux.Message.msg('', 'Attached unit ' + data.records[0].get('code'));
                            }
                        }
                    },
                    store: partsStore,
                    columns: columns2,
                    stripeRows: true,
                    title: 'Merged units',
                    margins: '0 5 0 0'
                },
                {
                    name: 'searchGrid',
                    flex: 1,
                    xtype: 'grid',
                    store: searchStore,
                    multiSelect : true,
                    viewConfig: {
                        plugins: {
                            ptype: 'gridviewdragdrop',
                            ddGroup: ddGroup,
                            enableDrop: false
                        }
                    },
                    tools: [
                        {
                            xtype: 'textfield',
                            emptyText: '<filter current>',
                            enableKeyEvents: true,
                            width: 140,
                            listeners: {
                                scope: this,
                                change: function(field) {
                                    this.search2(field.value || '');
                                }
                            }
                        }
                    ],
                    columns: columns,
                    margins: '0 0 0 5',
                    stripeRows: true,
                    title: 'Search units'
                }
            ],
            buttons : [{
                text : 'Submit merge',
                handler : function() {

                    var win = this.up('panel');
                    var grid = win.down('grid[name=unitParts]');
                    var aResult = new Array();
                    var i = 0;
                    grid.store.data.each( function(node) {
                        aResult[i] = node.data.code;
                        i++;
                    });
                    var units = Ext.encode(aResult);
                    Ext.Ajax.request({
                            method : 'POST',
                            url : rootFolder + 'units/merge',
                            submitEmptyText : false,
                            waitMsg : 'Merging ...',
                            params : {
                                code: win.row.data.code,
                                units : units
                            },
                            success : function(response) {
                                var obj = Ext.decode(response.responseText);
                                if (obj.success) {
                                    Ext.ux.Message.msg("Merge successful.", "");
                                    gloApp.getController('MainPanel').loadList(win.parentWindow, '', '', 'stay');
                                    win.up('window').destroy();
                                } else {
                                    Ext.Msg.alert('Failed', action.result.msg);
                                }
                            },
                            failure : function(frm, action) {
                                Ext.Msg.alert('Failed', action.result.msg);
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

        this.callParent(arguments);

        this.loadPartsGrid( this.row.data.code);
        this.loadGrid('');
    },

    loadGrid: function(searchText) {

        var grid = this.down('grid[name=searchGrid]');
        grid.store.getProxy().extraParams.bom = this.row.data.productCode + "," + this.row.data.bomRevision;
        grid.store.getProxy().extraParams.query = searchText;
        grid.store.load({
            scope : this,
            callback : function(records, operation, success) {

                if (records.length === 1 && records[0].data.id === -1) {
                    grid.store.removeAll();
                }
            }
        });
    },

    loadPartsGrid: function(code) {

        var grid = this.down('grid[name=unitParts]');
        grid.store.getProxy().extraParams.code = this.row.data.code;
        grid.store.load();
    },

    search: function(searchText) {
        this.loadGrid(searchText);
    }
});
