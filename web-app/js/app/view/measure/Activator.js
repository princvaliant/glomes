Ext.define('glo.view.measure.Activator', {
    extend: 'Ext.Panel',
    alias: 'widget.measureactivator',
    requires: ['Ext.ux.PreviewPlugin', 'Ext.toolbar.Toolbar',
        'Ext.form.field.ComboBox'],
    border: false,
    layout: 'fit',
    margins: '3 2 3 2',
    oid: '',
    dockedItems: {
        xtype: 'toolbar',
        dock: 'top',
        items: {
            xtype: 'button',
            text: 'Set selected as ACTIVE',
            width: 170,
            iconCls: 'icon-arrowdown',
            handler: function (btn, event) {

                var me = btn.ownerCt.ownerCt;

                var selectedRecord = me.grid.getSelectionModel().getSelection();
                var records = Ext.encode( Ext.Array.map(selectedRecord, function(record) {
                         return {
                            SID: record.data['SID'],
                            hourSet: record.data['HourSet'],
                            tempSet: record.data['TempSet']
                        };
                    })
                );

                Ext.Ajax.request({
                    scope: this,
                    method: 'POST',
                    params: {
                        testType: me.testType,
                        waferID: me.waferId,
                        deviceID: me.deviceId,
                        records: records
                    },
                    url: rootFolder + 'measure/changeActive',
                    success: function (response) {
                        var obj = Ext.decode(response.responseText);
                        if (obj.success == true) {
                            Ext.ux.Message.msg('Active test data successfully set', '');
                        }
                    }
                });
            }
        }
    },

    initComponent: function () {

        var me = this;

        var store = Ext.create('Ext.data.Store', {
            autoLoad: false,
            fields: [
                {name: 'SID'},
                {name: 'WaferID'},
                {name: 'DeviceID'},
                {name: 'DateTime'},
                {name: 'HourSet', type: 'int'},
                {name: 'TempSet', type: 'int'},
                {name: 'Total', type: 'int'},
                {name: 'Selected'}
            ],
            proxy: {
                type: 'rest',
                url: rootFolder + 'measure/activation',
                actionMethods: {read: 'GET'},
                simpleSortMode: true,

                reader: {
                    type: 'json',
                    root: '',
                    totalProperty: 'count',
                    messageProperty: 'msg'
                }
            }
        });

        // create the Grid
        me.grid = Ext.create('Ext.grid.Panel', {
            store: store,
            multiSelect: true,
            listeners: {
                beforecellclick: function( grid, td, cellIndex, record, tr, rowIndex, e, eOpts ) {
                    if(cellIndex === 0){
                        return false;
                    }
                }
            },
            columns: [
                {
                    xtype : 'actioncolumn',
                    text : 'Images',
                    width : 48,
                    items : [{
                        stopSelection: true,
                        getClass: function (v, meta, rec) {
                            return 'icon-spc';
                        },
                        handler: function(grid, rowIndex, colIndex) {

                            var rec = grid.getStore().getAt(rowIndex);
                            window.open(rootFolder + 'relImages?WaferID=' + rec.data.WaferID + '&DeviceID=' + rec.data.DeviceID + '&SID=' + rec.data.SID + '&HourSet=' + rec.data.HourSet, 'resizable,scrollbars');
                        }
                    }]
                },
                {
                    text: 'SID',
                    flex: 4,
                    sortable: false,
                    dataIndex: 'SID'
                },
                {
                    text: 'Date time',
                    flex: 2,
                    sortable: false,
                    dataIndex: 'DateTime'
                },
                {
                    text: 'Hour set',
                    flex: 1,
                    sortable: false,
                    dataIndex: 'HourSet'
                },
                {
                    text: 'Temp set',
                    flex: 1,
                    sortable: false,
                    dataIndex: 'TempSet'
                },
                {
                    text: 'Total',
                    flex: 1,
                    sortable: false,
                    dataIndex: 'Total'
                }
            ],
            title: 'Current active test data',
            viewConfig: {
                stripeRows: true
            }
        });

        me.callParent(arguments);

        me.add(me.grid);
    },

    createChart: function (ids, filter) {

        this.waferId = ids[0];

        this.grid.store.load({
            params: {
                testType: this.testType,
                waferId: ids[0]
            },
            scope: this,
            callback: function (records, operation, success) {

                var sm = this.grid.getSelectionModel();
                Ext.each(records, function (record) {
                    if (record.data.Selected === 'Y') {
                        var row = record.index;
                        sm.select(row, true);
                    }
                });
            }
        });
    }
});
