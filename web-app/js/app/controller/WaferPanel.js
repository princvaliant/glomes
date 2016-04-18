var clickedItems = new Array();
var cachedRange = new Array();
var filteredItems = new Array();
var acTT = '';

Ext
    .define(
    'glo.controller.WaferPanel', {
        extend: 'Ext.app.Controller',

        init: function () {

            this.control({
                'actioncolumn#deleteWaferFilterId': {
                    click: this.onDeleteWaferFilter
                },
                'actioncolumn#deleteAdminWaferFilterId': {
                    click: this.onDeleteWaferFilter
                },
                'actioncolumn#deleteSpecWaferFilterId': {
                    click: this.onDeleteWaferFilter
                },
                'actioncolumn#deleteWaferSummaryId': {
                    click: this.onDeleteWaferSummary
                },
                'grid': {
                    waferfilterselect: this.onWaferFilterSelect
                }
            });
        },

        show: function (code, pctg, pkey, tkey, propertyName, testId) {

            var value1;
            var value2;

            Ext.define('glopoint', {
                extend: 'Ext.data.Model',
                fields: [
                    {
                        name: 'voltage',
                        convert: function (value, record) {
                            return value;
                        }
                    },
                    {
                        name: 'current',
                        convert: function (value, record) {
                            return value;
                        }
                    }
                ]
            });

            var store = Ext.create('Ext.data.Store', {
                fields: [
                    {
                        name: 'propName',
                        type: 'string'
                    }
                ],
                autoLoad: false,
                proxy: {
                    type: 'rest',
                    method: 'GET',
                    url: rootFolder + 'wafer/parameters',
                    extraParams: {
                        code: code,
                        tkey: tkey,
                        testId: testId,
                        propertyName: propertyName
                    }
                }
            });

            var store2 = Ext.create('Ext.data.Store', {
                fields: [
                    {
                        name: 'propName',
                        type: 'string'
                    }
                ],
                autoLoad: false,
                proxy: {
                    type: 'rest',
                    method: 'GET',
                    url: rootFolder + 'wafer/parameters2',
                    extraParams: {
                        code: code,
                        tkey: tkey,
                        testId: testId,
                        propertyName: propertyName
                    }
                }
            });

            var storeFilter = Ext.create('Ext.data.Store', {
                fields: ['id', 'isActive', 'level1', 'level2',
                    'valFrom', 'valTo'
                ],
                autoLoad: true,
                proxy: {
                    type: 'rest',
                    method: 'GET',
                    url: rootFolder + 'wafer/filters',
                    extraParams: {
                        code: code,
                        tkey: tkey,
                        propertyName: propertyName
                    },
                    reader: {
                        type: 'json',
                        root: 'data',
                        totalProperty: 'count',
                        messageProperty: 'msg'
                    }
                }
            });

            var storeAdminFilter = Ext.create('Ext.data.Store', {
                fields: ['id', 'isActive', 'level1', 'level2',
                    'valFrom', 'valTo'
                ],
                autoLoad: true,
                proxy: {
                    type: 'rest',
                    method: 'GET',
                    url: rootFolder + 'wafer/adminFilters',
                    extraParams: {
                        code: code,
                        tkey: tkey,
                        propertyName: propertyName
                    },
                    reader: {
                        type: 'json',
                        root: 'data',
                        totalProperty: 'count',
                        messageProperty: 'msg'
                    }
                }
            });

            var storeSummaryFilter = Ext.create('Ext.data.Store', {
                fields: ['id', 'isActive', 'parameter', 'current', 'testType', 'dieSpec' ],
                autoLoad: true,
                proxy: {
                    type: 'rest',
                    method: 'GET',
                    url: rootFolder + 'wafer/summaries',
                    extraParams: {
                    },
                    reader: {
                        type: 'json',
                        root: 'data',
                        totalProperty: 'count',
                        messageProperty: 'msg'
                    }
                }
            });

            var storeSpecFilter = Ext.create('Ext.data.Store', {
                fields: ['id', 'level1', 'level2', 'valFrom',
                    'valTo'
                ],
                autoLoad: false,
                proxy: {
                    type: 'rest',
                    method: 'GET',
                    url: rootFolder + 'wafer/specFilters',
                    extraParams: {
                        code: code,
                        tkey: tkey,
                        propertyName: propertyName
                    },
                    reader: {
                        type: 'json',
                        root: 'data',
                        totalProperty: 'count',
                        messageProperty: 'msg'
                    }
                }
            });

            storeFilter.on(
                'load',
                function (store, records, options) {
                    if (store.getTotalCount() != undefined) {
                        Ext.Object
                            .each(
                            records,
                            function (record) {
                                if (records[record].data.isActive == true) {
                                    Ext
                                        .getCmp(
                                        'gridtestDataFiltersId')
                                        .getSelectionModel()
                                        .select(
                                        records[record],
                                        true,
                                        true);
                                }
                            });
                    }
                });

            storeAdminFilter.on(
                'load',
                function (store, records, options) {
                    if (store.getTotalCount() != undefined) {
                        Ext.getCmp(
                            'waferFiltersTabPanel')
                            .setActiveTab(1);
                        Ext.Object
                            .each(
                            records,
                            function (record) {
                                if (records[record].data.isActive == true && Ext
                                    .getCmp('gridtestDataAdminFiltersId') != undefined) {
                                    if (records[record].data.isActive == true) {
                                        Ext
                                            .getCmp(
                                            'gridtestDataAdminFiltersId')
                                            .getSelectionModel()
                                            .select(
                                            records[record],
                                            true,
                                            true);
                                    }

                                }
                            });
                        Ext.getCmp(
                            'waferFiltersTabPanel')
                            .setActiveTab(0);
                    }
                });

            var propCombo2 = Ext.create('Ext.ux.form.MultiSelect', {
                store: store2,
                maxSelections: 1,
                minSelections: 1,
                border: 1,
                displayField: 'propName',
                valueField: 'propName',
                queryMode: 'local',
                flex: 3,
                id: 'propLevel2Id'
            });

            var propCombo3 = Ext.create('Ext.form.field.ComboBox', {
                store: store2,
                border: 0,
                flex: 1,
                displayField: 'propName',
                valueField: 'propName',
                triggerAction: 'all',
                queryMode: 'local',
                stateful: true,
                align: 'right',
                fieldLabel: 'X axis',
                id: 'propLevelXId'
            });

            var propCombo = Ext.create('Ext.ux.form.MultiSelect', {
                store: store,
                maxSelections: 1,
                minSelections: 1,
                border: 1,
                displayField: 'propName',
                valueField: 'propName',
                queryMode: 'local',
                flex: 2,
                id: 'propLevel1Id'
            });

            var win = Ext.create(
                'Ext.window.Window', {
                    title: 'Wafer layout for serial# ' + code,
                    resizable: false,
                    modal: true,
                    width: 1260,
                    height: 725,
                    y: 0,
                    draggable: false,
                    layout: {
                        type: 'border',
                        padding: 0
                    },
                    dockedItems: [
                        {
                            xtype: 'toolbar',
                            dock: 'top',
                            items: []
                        }
                    ],
                    items: [
                        {
                            region: 'west',
                            collapsible: false,
                            width: 124,
                            layout: {
                                type: 'vbox',
                                padding: 0,
                                margin: 0,
                                align: 'stretch'
                            },
                            dockedItems: {
                                xtype: 'button',
                                scope: this,
                                text: 'Export to excel',
                                iconCls: 'icon-excel',
                                handler: function () {
                                    gloApp
                                        .getController(
                                        'WaferPanel')
                                        .exportData(
                                        tkey,
                                        code,
                                        testId,
                                        propertyName,
                                        propCombo.value);
                                }
                            },
                            items: [propCombo,
                                propCombo2
                            ]
                        },
                        {
                            region: 'center',
                            border: false,
                            layout: 'fit',
                            items: {
                                xtype: 'tabpanel',
                                id: 'waferTabPanel',
                                activeTab: 0,
                                plain: true,
                                layout: 'fit',
                                defaults: {
                                    bodyPadding: 0
                                },
                                items: [
                                    {
                                        title: 'Wafer map',
                                        name: 'm',
                                        layout: 'fit',
                                        listeners: {
                                            activate: function (tab) {
                                                if (propCombo.value != undefined && propCombo2
                                                    .getValue() != undefined) {
                                                    gloApp
                                                        .getController(
                                                        'WaferPanel')
                                                        .drawMap(
                                                        "LIST",
                                                        code,
                                                        tkey,
                                                        testId,
                                                        propertyName,
                                                        propCombo
                                                            .getValue(),
                                                        propCombo2
                                                            .getValue(),
                                                        null,
                                                        null)
                                                }
                                            }
                                        },
                                        items: {
                                            xtype: 'panel',
                                            id: 'waferMapId',
                                            align: 'stretch',
                                            layout: 'fit',
                                            devices: ''
                                        }
                                    },
                                    {
                                        title: 'Spectrum chart',
                                        name: 's',
                                        align: 'stretch',
                                        layout: 'fit',
                                        listeners: {
                                            activate: function (tab) {
                                                gloApp
                                                    .getController(
                                                    'WaferPanel')
                                                    .drawChart(
                                                    code,
                                                    tkey,
                                                    testId,
                                                    propertyName);
                                            }
                                        },
                                        items: [
                                            {
                                                xtype: 'panel',
                                                id: 'waferSpectrumChart',
                                                align: 'stretch',
                                                layout: 'fit',
                                                devices: ''
                                            }
                                        ]
                                    },
                                    {
                                        title: 'Voltage sweep chart',
                                        name: 'v',
                                        layout: 'fit',
                                        listeners: {
                                            activate: function (tab) {
                                                gloApp
                                                    .getController(
                                                    'WaferPanel')
                                                    .drawChart(
                                                    code,
                                                    tkey,
                                                    testId,
                                                    propertyName);
                                            }
                                        },
                                        items: {
                                            xtype: 'panel',
                                            id: 'waferVoltageSweepChart',
                                            align: 'stretch',
                                            layout: 'fit',
                                            devices: ''
                                        }
                                    },
                                    {
                                        title: 'Current dependence',
                                        name: 'c',
                                        layout: 'fit',
                                        listeners: {
                                            activate: function (tab) {
                                                gloApp
                                                    .getController(
                                                    'WaferPanel')
                                                    .drawChart(
                                                    code,
                                                    tkey,
                                                    testId,
                                                    propertyName);
                                            }
                                        },
                                        items: {
                                            xtype: 'panel',
                                            id: 'waferCurrentSweepChart',
                                            align: 'stretch',
                                            layout: 'fit',
                                            devices: ''
                                        }
                                    },
                                    {
                                        title: 'Other dependenies',
                                        name: 'o',
                                        layout: 'vbox',
                                        padding: 7,
                                        listeners: {
                                            activate: function (tab) {
                                                gloApp
                                                    .getController(
                                                    'WaferPanel')
                                                    .drawChart(
                                                    code,
                                                    tkey,
                                                    testId,
                                                    'otherDependencies');
                                            }
                                        },
                                        items: [
                                            {
                                                xtype: 'panel',
                                                id: 'waferOtherChart',
                                                flex: 15,
                                                width: 750,
                                                layout: 'fit',
                                                align: 'stretch',
                                                devices: '',
                                                items: [
                                                    {
                                                        xtype: 'chart',
                                                        id: 'chartOtherDepId',
                                                        align: 'stretch',
                                                        store: Ext
                                                            .create(
                                                            'Ext.data.Store', {
                                                                fields: [code]
                                                            }),
                                                        insetPadding: 20,
                                                        axes: [
                                                            {
                                                                type: 'Numeric',
                                                                position: 'left',
                                                                fields: ['y'],
                                                                title: '',
                                                                grid: true,
                                                                decimals: 3
                                                            },
                                                            {
                                                                type: 'Numeric',
                                                                position: 'bottom',
                                                                fields: ['x'],
                                                                grid: true,
                                                                title: propCombo3.value,
                                                                decimals: 3
                                                            }
                                                        ],
                                                        series: [
                                                            {
                                                                type: 'scatter',
                                                                axis: false,
                                                                xField: 'x',
                                                                yField: 'y',
                                                                color: '#ccc',
                                                                markerConfig: {
                                                                    type: 'circle',
                                                                    radius: 2,
                                                                    size: 2
                                                                },
                                                                tips: {
                                                                    trackMouse: false,
                                                                    width: 180,
                                                                    height: 28,
                                                                    renderer: function (storeItem, item) {
                                                                        this.setTitle(storeItem.data.code + ', x= ' + storeItem.data.x + ', y=' + storeItem.data.y);
                                                                    }
                                                                }
                                                            }
                                                        ],
                                                        listeners: {
                                                            scope: this,
                                                            click: function (e, eOpt) {

                                                                console
                                                                    .log(e);

                                                            }
                                                        }
                                                    }
                                                ]
                                            },
                                            propCombo3
                                        ]
                                    }
                                ]
                            }
                        },
                        {
                            region: 'east',
                            collapsible: false,
                            width: 365,
                            minWidth: 115,
                            minHeight: 140,
                            autoScroll: false,
                            layout: {
                                type: 'vbox',
                                padding: 0,
                                align: 'stretch',
                                pack: 'start'
                            },
                            items: [
                                {
                                    xtype: 'fieldcontainer',
                                    height: 28,
                                    layout: {
                                        type: 'hbox',
                                        padding: 5
                                    },
                                    items: [
                                        {
                                            id: 'waferRange1Id',
                                            xtype: 'numberfield',
                                            hideTrigger: true,
                                            width: 85,
                                            decimalPrecision: 10
                                        },
                                        {
                                            id: 'waferRange2Id',
                                            xtype: 'numberfield',
                                            hideTrigger: true,
                                            width: 85,
                                            decimalPrecision: 10
                                        },
                                        {
                                            xtype: 'button',
                                            text: 'Add filter',
                                            iconCls: 'icon-arrowdown',
                                            handler: function () {

                                                Ext.Ajax
                                                    .request({
                                                        scope: this,
                                                        method: 'POST',
                                                        params: {
                                                            level1: propCombo.value,
                                                            level2: propCombo2.value,
                                                            valFrom: Ext
                                                                .getCmp('waferRange1Id').value,
                                                            valTo: Ext
                                                                .getCmp('waferRange2Id').value,
                                                            spec: Ext
                                                                .getCmp('waferSpecComboId') != undefined ? Ext
                                                                .getCmp('waferSpecComboId').value : '',
                                                            tab: Ext
                                                                .getCmp(
                                                                'waferFiltersTabPanel')
                                                                .getActiveTab().id
                                                        },
                                                        url: rootFolder + 'wafer/addFilter',
                                                        success: function (response) {
                                                            var obj = Ext
                                                                .decode(response.responseText);
                                                            if (obj.success == false) {
                                                                Ext.Msg
                                                                    .alert(
                                                                    'Add filter failed',
                                                                    obj.msg);
                                                            } else {
                                                                var t = Ext
                                                                    .getCmp(
                                                                    'waferFiltersTabPanel')
                                                                    .getActiveTab().id;
                                                                if (t == 'waferFilterTab')
                                                                    storeFilter
                                                                        .add(obj.data);
                                                                if (t == 'gridtestDataAdminFiltersId')
                                                                    storeAdminFilter
                                                                        .add(obj.data);
                                                                if (t == 'specWaferFilterTab')
                                                                    storeSpecFilter
                                                                        .add(obj.data);
                                                            }
                                                        }
                                                    });
                                            }
                                        },
                                        {
                                            xtype: 'button',
                                            text: 'Add to dashboard',
                                            iconCls: 'icon-calculator_add',
                                            handler: function () {


                                                var win = Ext
                                                    .create('Ext.window.Window', {
                                                        title: 'Add chart to summary file',
                                                        resizable: true,
                                                        modal: true,
                                                        width: 350,
                                                        y: 80,
                                                        layout: 'anchor',
                                                        bodyPadding: 8,
                                                        items: [
                                                            {
                                                                xtype: 'combo',
                                                                fieldLabel: 'Select spec',
                                                                store: Ext.create('glo.store.WaferSpecs', {
                                                                    pageSize: 25
                                                                }),
                                                                width: 300,
                                                                editable: false,
                                                                displayField: 'name',
                                                                valueField: 'id',
                                                                triggerAction: 'all',
                                                                emptyText: '<no spec>',
                                                                listConfig: {
                                                                    width: 300,
                                                                    loadingText: 'Searching specs ...',
                                                                    valueNotFoundText: 'No specs found.'
                                                                },
                                                                pageSize: 25
                                                            }
                                                        ],
                                                        buttons: [
                                                            {
                                                                text: 'Save',
                                                                handler: function () {
                                                                    var win = this.up('window');
                                                                    Ext.Ajax.request({
                                                                        scope: this,
                                                                        method: 'POST',
                                                                        params: {
                                                                            parameter: propCombo2.value,
                                                                            current: propCombo.value,
                                                                            dieSpecId: win.down('combo').value,
                                                                            testType: tkey,
                                                                            tab: Ext.getCmp(
                                                                                'waferFiltersTabPanel')
                                                                                .getActiveTab().id
                                                                        },
                                                                        url: rootFolder + 'wafer/addSummary',
                                                                        success: function (response) {
                                                                            var obj = Ext.decode(response.responseText);
                                                                            if (obj.success == false) {
                                                                                Ext.Msg.alert(
                                                                                    'Add to summary failed',
                                                                                    obj.msg);
                                                                            } else {
                                                                                storeSummaryFilter.add(obj.data);
                                                                                Ext.getCmp('waferFiltersTabPanel').setActiveTab(3);
                                                                            }
                                                                            win.destroy()
                                                                        }
                                                                    });


                                                                }
                                                            },
                                                            {
                                                                text: 'Cancel',
                                                                handler: function () {
                                                                    this
                                                                        .up(
                                                                        'window')
                                                                        .destroy();
                                                                }
                                                            }
                                                        ]

                                                    });

                                                win.show();
                                            }
                                        }
                                    ]
                                },
                                {
                                    xtype: 'tabpanel',
                                    id: 'waferFiltersTabPanel',
                                    activeTab: 0,
                                    layoutOnTabChange: true,
                                    layout: 'fit',
                                    defaults: {
                                        bodyPadding: 0
                                    },
                                    listeners: {
                                        tabchange: function (tabPanel, newTab, oldTab, index) {

                                            var s = acTT;

                                            acTT = "A";
                                            if (newTab.id == 'specWaferFilterTab')
                                                acTT = "S";
                                            if (newTab.id == 'waferFilterTab')
                                                acTT = "U";

                                            if (s != '' && newTab.id != 'gridTestSummaryId') {
                                                gloApp
                                                    .getController(
                                                    'WaferPanel')
                                                    .drawMap(
                                                    "SLIDER",
                                                    code,
                                                    tkey,
                                                    testId,
                                                    propertyName,
                                                    propCombo
                                                        .getValue(),
                                                    propCombo2
                                                        .getValue(),
                                                    null,
                                                    null)
                                            }
                                        }
                                    },
                                    items: [
                                        {
                                            title: 'User filters',
                                            align: 'stretch',
                                            id: 'waferFilterTab',
                                            layout: 'fit',
                                            items: {
                                                xtype: 'grid',
                                                id: 'gridtestDataFiltersId',
                                                store: storeFilter,
                                                autoScroll: true,
                                                border: false,
                                                height: 560,
                                                multiSelect: true,
                                                stateful: true,
                                                stateId: 'gridtestDataFilters2',
                                                viewConfig: {
                                                    emptyText: 'No filters defined'
                                                },
                                                selModel: new Ext.selection.CheckboxModel({
                                                    listeners: {
                                                        scope: this,
                                                        selectionchange: function (selectionModel, selected, obj) {
                                                            this
                                                                .onWaferFilterSelect(
                                                                obj,
                                                                selected,
                                                                code,
                                                                tkey,
                                                                testId,
                                                                propertyName,
                                                                propCombo
                                                                    .getValue(),
                                                                propCombo2
                                                                    .getValue(),
                                                                Ext
                                                                    .getCmp('waferRange1Id').value,
                                                                Ext
                                                                    .getCmp('waferRange2Id').value,
                                                                0);

                                                        }
                                                    }
                                                }),
                                                columns: [
                                                    {
                                                        header: 'Current',
                                                        dataIndex: 'level1',
                                                        width: 90
                                                    },
                                                    {
                                                        header: 'Parameter',
                                                        dataIndex: 'level2',
                                                        width: 105
                                                    },
                                                    {
                                                        header: 'From',
                                                        dataIndex: 'valFrom',
                                                        width: 58
                                                    },
                                                    {
                                                        header: 'To',
                                                        dataIndex: 'valTo',
                                                        width: 59
                                                    },
                                                    {
                                                        id: 'deleteWaferFilterId',
                                                        xtype: 'actioncolumn',
                                                        text: 'N',
                                                        width: 22,
                                                        items: [
                                                            {
                                                                getClass: function (v, meta, rec) {
                                                                    return 'column_delete';
                                                                }
                                                            }
                                                        ]
                                                    }
                                                ]
                                            }
                                        },
                                        {
                                            title: 'Admin filters',
                                            height: 560,
                                            xtype: 'grid',
                                            id: 'gridtestDataAdminFiltersId',
                                            store: storeAdminFilter,
                                            deferRowRender: true,
                                            border: false,
                                            multiSelect: true,
                                            autoScroll: true,
                                            stateful: true,
                                            stateId: 'gridtestDataAdminFilters2',
                                            viewConfig: {
                                                emptyText: 'No admin filters defined'
                                            },
                                            selModel: new Ext.selection.CheckboxModel({
                                                listeners: {
                                                    scope: this,
                                                    selectionchange: function (selectionModel, selected, obj) {
                                                        this
                                                            .onWaferFilterSelect(
                                                            obj,
                                                            selected,
                                                            code,
                                                            tkey,
                                                            testId,
                                                            propertyName,
                                                            propCombo
                                                                .getValue(),
                                                            propCombo2
                                                                .getValue(),
                                                            Ext
                                                                .getCmp('waferRange1Id').value,
                                                            Ext
                                                                .getCmp('waferRange2Id').value,
                                                            1);

                                                    }
                                                }
                                            }),
                                            columns: [
                                                {
                                                    header: 'Current',
                                                    dataIndex: 'level1',
                                                    width: 90
                                                },
                                                {
                                                    header: 'Parameter',
                                                    dataIndex: 'level2',
                                                    width: 105
                                                },
                                                {
                                                    header: 'From',
                                                    dataIndex: 'valFrom',
                                                    width: 58
                                                },
                                                {
                                                    header: 'To',
                                                    dataIndex: 'valTo',
                                                    width: 59
                                                },
                                                {
                                                    id: 'deleteAdminWaferFilterId',
                                                    xtype: 'actioncolumn',
                                                    text: 'N',
                                                    width: 22,
                                                    items: [
                                                        {
                                                            getClass: function (v, meta, rec) {
                                                                return 'column_delete';
                                                            }
                                                        }
                                                    ]
                                                }
                                            ]
                                        },
                                        {
                                            title: 'Spec revs.',
                                            xtype: 'panel',
                                            id: 'specWaferFilterTab',
                                            items: [
                                                {
                                                    xtype: 'fieldset',
                                                    height: 40,
                                                    fieldLabel: 'Die selection specs',
                                                    combineErrors: false,
                                                    layout: 'hbox',
                                                    defaults: {
                                                        hideLabel: true,
                                                        margin: '4 5 0 1'
                                                    },
                                                    items: [
                                                        {
                                                            xtype: 'button',
                                                            text: 'Add spec',
                                                            iconCls: 'icon-calculator_add',
                                                            handler: function () {

                                                                var prodStore = Ext
                                                                    .create('glo.store.Products');
                                                                prodStore
                                                                    .getProxy().extraParams.productFamily = 10;

                                                                var win = Ext
                                                                    .create(
                                                                    'Ext.window.Window', {
                                                                        title: 'Create new spec',
                                                                        resizable: true,
                                                                        modal: true,
                                                                        width: 408,
                                                                        y: 50,
                                                                        layout: 'anchor',
                                                                        bodyPadding: 8,
                                                                        items: [
                                                                            {
                                                                                xtype: 'textfield',
                                                                                allowBlank: false,
                                                                                fieldLabel: 'Name',
                                                                                name: 'name',
                                                                                anchor: '100%'
                                                                            },
                                                                            {
                                                                                xtype: 'numberfield',
                                                                                allowBlank: false,
                                                                                fieldLabel: 'Revision',
                                                                                name: 'revision',
                                                                                anchor: '50%'
                                                                            },
                                                                            {
                                                                                xtype: 'combo',
                                                                                name: 'pid',
                                                                                fieldLabel: 'Product',
                                                                                anchor: '100%',
                                                                                minChars: 1,
                                                                                typeAhead: true,
                                                                                store: prodStore,
                                                                                listConfig: {
                                                                                    maxHeight: 450
                                                                                },
                                                                                displayField: 'name',
                                                                                valueField: 'id',
                                                                                forceSelection: true,
                                                                                multiSelect: false,
                                                                                allowBlank: false
                                                                            }
                                                                        ],
                                                                        buttons: [
                                                                            {
                                                                                text: 'Save',
                                                                                handler: function () {
                                                                                    var win = this
                                                                                        .up('window');

                                                                                    var name = win
                                                                                        .down('textfield').value;
                                                                                    var revision = win
                                                                                        .down('numberfield').value;
                                                                                    var product = win
                                                                                        .down('combo').value;

                                                                                    Ext.Ajax
                                                                                        .request({
                                                                                            scope: this,
                                                                                            method: 'POST',
                                                                                            params: {
                                                                                                code: code,
                                                                                                name: name,
                                                                                                revision: revision,
                                                                                                product: product
                                                                                            },
                                                                                            url: rootFolder + 'wafer/specs',
                                                                                            success: function (response) {
                                                                                                var obj = Ext
                                                                                                    .decode(response.responseText);
                                                                                                if (obj.success == false) {
                                                                                                    Ext.Msg
                                                                                                        .alert(
                                                                                                        'Spec can not bi saved',
                                                                                                        obj.msg);
                                                                                                } else {
                                                                                                    Ext.ux.Message
                                                                                                        .msg(
                                                                                                        'Spec saved completed successfully',
                                                                                                        '');
                                                                                                    Ext
                                                                                                        .getCmp('waferSpecComboId').store
                                                                                                        .load({
                                                                                                            callback: function (store, records, options) {
                                                                                                                Ext
                                                                                                                    .getCmp(
                                                                                                                    'waferSpecComboId')
                                                                                                                    .setValue(
                                                                                                                    obj.data);
                                                                                                            }
                                                                                                        });
                                                                                                    this
                                                                                                        .up(
                                                                                                        'window')
                                                                                                        .destroy();
                                                                                                }
                                                                                            }
                                                                                        });
                                                                                }
                                                                            },
                                                                            {
                                                                                text: 'Close',
                                                                                handler: function () {
                                                                                    this
                                                                                        .up(
                                                                                        'window')
                                                                                        .destroy();
                                                                                }
                                                                            }
                                                                        ]

                                                                    });

                                                                win
                                                                    .show();
                                                            }
                                                        },
                                                        {
                                                            xtype: 'combo',
                                                            id: 'waferSpecComboId',
                                                            store: Ext
                                                                .create(
                                                                'glo.store.WaferSpecs', {
                                                                    pageSize: 20
                                                                }),
                                                            width: 180,
                                                            editable: false,
                                                            displayField: 'name',
                                                            valueField: 'id',
                                                            action: 'waferSpecSelect',
                                                            triggerAction: 'all',
                                                            emptyText: '<select spec>',
                                                            listConfig: {
                                                                width: 250,
                                                                loadingText: 'Searching specs ...',
                                                                valueNotFoundText: 'No specs found.'
                                                            },
                                                            pageSize: 20,
                                                            listeners: {
                                                                change: function (combo) {
                                                                    storeSpecFilter
                                                                        .getProxy().extraParams.spec = combo.value;
                                                                    storeSpecFilter
                                                                        .load({
                                                                            callback: function (store, records, options) {
                                                                                gloApp
                                                                                    .getController(
                                                                                    'WaferPanel')
                                                                                    .drawMap(
                                                                                    "SLIDER",
                                                                                    code,
                                                                                    tkey,
                                                                                    testId,
                                                                                    propertyName,
                                                                                    propCombo
                                                                                        .getValue(),
                                                                                    propCombo2
                                                                                        .getValue(),
                                                                                    null,
                                                                                    null)
                                                                            }
                                                                        });
                                                                },
                                                                afterrender: function (combo) {
                                                                    var recordSelected = combo
                                                                        .getStore()
                                                                        .getAt(
                                                                        0);
                                                                    if (recordSelected != undefined)
                                                                        combo
                                                                            .setValue(recordSelected
                                                                                .get('id'));
                                                                }
                                                            }
                                                        },
                                                        {
                                                            xtype: 'button',
                                                            text: 'Refresh',
                                                            iconCls: 'icon-rework',
                                                            handler: function () {
                                                                gloApp
                                                                    .getController(
                                                                    'WaferPanel')
                                                                    .drawMap(
                                                                    "SLIDER",
                                                                    code,
                                                                    tkey,
                                                                    testId,
                                                                    propertyName,
                                                                    propCombo
                                                                        .getValue(),
                                                                    propCombo2
                                                                        .getValue(),
                                                                    null,
                                                                    null)
                                                            }
                                                        }
                                                    ]
                                                },
                                                {
                                                    xtype: 'grid',
                                                    height: 450,
                                                    id: 'gridtestSpecFiltersId',
                                                    store: storeSpecFilter,
                                                    border: false,
                                                    multiSelect: false,
                                                    stateful: true,
                                                    stateId: 'gridtestDataSpecFilters',
                                                    viewConfig: {
                                                        emptyText: 'No specs defined'
                                                    },
                                                    columns: [
                                                        {
                                                            header: 'Current',
                                                            dataIndex: 'level1',
                                                            width: 90
                                                        },
                                                        {
                                                            header: 'Parameter',
                                                            dataIndex: 'level2',
                                                            width: 105
                                                        },
                                                        {
                                                            header: 'From',
                                                            dataIndex: 'valFrom',
                                                            width: 58
                                                        },
                                                        {
                                                            header: 'To',
                                                            dataIndex: 'valTo',
                                                            width: 59
                                                        },
                                                        {
                                                            id: 'deleteSpecWaferFilterId',
                                                            xtype: 'actioncolumn',
                                                            text: 'N',
                                                            width: 22,
                                                            items: [
                                                                {
                                                                    getClass: function (v, meta, rec) {
                                                                        return 'column_delete';
                                                                    }
                                                                }
                                                            ]
                                                        }
                                                    ]
                                                },
                                                {
                                                    xtype: 'button',
                                                    height: 60,
                                                    scope: this,
                                                    text: 'Export dies for spec',
                                                    iconCls: 'icon-report',
                                                    handler: function () {

                                                        window
                                                            .open(
                                                                rootFolder + 'wafer/specData?tkey=' + tkey + '&code=' + code + '&testId=' + testId + '&spec=' + Ext
                                                                .getCmp('waferSpecComboId').value + '&devices=' + filteredItems,
                                                            'resizable,scrollbars');

                                                    }
                                                },
                                                {
                                                    xtype: 'button',
                                                    height: 60,
                                                    scope: this,
                                                    text: 'Export dies for pick/place',
                                                    iconCls: 'icon-report',
                                                    handler: function () {

                                                        window
                                                            .open(
                                                                rootFolder + 'wafer/specData?pp=1&tkey=' + tkey + '&code=' + code + '&testId=' + testId + '&spec=' + Ext
                                                                .getCmp('waferSpecComboId').value + '&devices=' + filteredItems,
                                                            'resizable,scrollbars');

                                                    }
                                                }
                                            ]
                                        },
                                        {
                                            title: 'Dashboard',
                                            height: 560,
                                            xtype: 'grid',
                                            id: 'gridTestSummaryId',
                                            store: storeSummaryFilter,
                                            deferRowRender: true,
                                            border: false,
                                            multiSelect: true,
                                            autoScroll: true,
                                            stateful: true,
                                            stateId: 'gridtestDataSummary4',
                                            viewConfig: {
                                                emptyText: 'No summaries defined'
                                            },
                                            columns: [
                                                {
                                                    header: 'Test type',
                                                    dataIndex: 'testType',
                                                    width: 70
                                                },
                                                {
                                                    header: 'Spec',
                                                    dataIndex: 'dieSpec',
                                                    width: 134
                                                },
                                                {
                                                    header: 'Param',
                                                    dataIndex: 'parameter',
                                                    width: 68
                                                },
                                                {
                                                    header: 'Current',
                                                    dataIndex: 'current',
                                                    width: 56
                                                },
                                                {
                                                    id: 'deleteWaferSummaryId',
                                                    xtype: 'actioncolumn',
                                                    text: 'N',
                                                    width: 22,
                                                    items: [
                                                        {
                                                            getClass: function (v, meta, rec) {
                                                                return 'column_delete';
                                                            }
                                                        }
                                                    ]
                                                }
                                            ]
                                        }
                                    ]
                                },
                                {
                                    xtype: 'fieldset',
                                    margin: '10 0 0 0',
                                    items: [
                                        {
                                            xtype: 'checkboxfield',
                                            boxLabel: 'Logharitmic scale',
                                            inputValue: '1',
                                            id: 'isLogharitmic',
                                            listeners: {
                                                change: function (checkbox, checked) {
                                                    gloApp
                                                        .getController(
                                                        'WaferPanel')
                                                        .drawChart(
                                                        code,
                                                        tkey,
                                                        testId,
                                                        propertyName);
                                                }
                                            }
                                        }
                                    ]
                                }
                            ]
                        }
                    ],
                    buttons: [
                        {
                            xtype: 'checkboxfield',
                            boxLabel: 'Show only top 5',
                            inputValue: '1',
                            id: 'isTop5',
                            listeners: {
                                change: function (checkbox, checked) {

                                    gloApp
                                        .getController(
                                        'WaferPanel')
                                        .drawMap(
                                        "SLIDER",
                                        code,
                                        tkey,
                                        testId,
                                        propertyName,
                                        propCombo
                                            .getValue(),
                                        propCombo2
                                            .getValue(),
                                        null,
                                        null);

                                }
                            }
                        },
                        {
                            xtype: 'checkboxfield',
                            boxLabel: 'Bypass admin filters',
                            inputValue: '1',
                            id: 'bypassAdminFilter',
                            listeners: {
                                change: function (checkbox, checked) {

                                    gloApp
                                        .getController(
                                        'WaferPanel')
                                        .drawMap(
                                        "SLIDER",
                                        code,
                                        tkey,
                                        testId,
                                        propertyName,
                                        propCombo
                                            .getValue(),
                                        propCombo2
                                            .getValue(),
                                        null,
                                        null);

                                }
                            }
                        },
                        {
                            text: 'Save as image',
                            handler: function () {

                                var tab = Ext.getCmp("waferTabPanel");
                                var win = tab.getActiveTab();
                                var documentContainer = win.items.get(0).getEl().dom;

                                var fileName = win.name
                                    .toUpperCase() + '_' + propCombo.value
                                    .toString()
                                    .split(
                                    ' ')[2]
                                    .toUpperCase() + '_' + propCombo2.value
                                    .toString()
                                    .split(
                                    ' ')[0]
                                    .toUpperCase();

                                if (win.name == 'o') {
                                    fileName += '_' + propCombo3.value
                                        .toString()
                                        .split(
                                        ' ')[0]
                                        .toUpperCase()
                                }
                                if (Ext
                                    .getCmp('isTop5').value == true) {
                                    fileName += '_TOP5'
                                }
                                fileName += '.png'

                                canvg(document.getElementById('canvas'),
                                    documentContainer.childNodes[0].childNodes[0].innerHTML)

                                var data = Canvas2Image
                                    .saveAsPNG(document.getElementById('canvas'), false);

                                Ext.Ajax
                                    .request({
                                        method: 'POST',
                                        params: {
                                            image: data
                                        },
                                        url: rootFolder + 'epidash/postImage?code=' + code + '&fileName=' + fileName,
                                        success: function (response) {

                                            var obj = Ext
                                                .decode(response.responseText);
                                            if (obj.success == false) {
                                                Ext.Msg
                                                    .alert(
                                                    'Save failed can not complete',
                                                    obj.msg);
                                            } else {
                                                Ext.ux.Message
                                                    .msg(
                                                        "Image '" + code + "/" + fileName + "' saved",
                                                    "");
                                            }
                                        }
                                    });
                            }
                        },
                        {
                            xtype: 'tbfill'
                        },
                        {
                            text: 'Comment and Area',
                            handler: function () {

                                // Determine if input requires additional input attributes
                                Ext.Ajax.request({
                                    scope: this,
                                    method: 'GET',
                                    params: {
                                        code: code,
                                        testId: testId,
                                        tkey: tkey
                                    },
                                    url: rootFolder + 'wafer/comment',
                                    success: function (response) {
                                        var obj = Ext.decode(response.responseText);

                                        var win = Ext.create('Ext.form.Panel', {
                                            title: 'Edit comment and Area',
                                            floating: true,
                                            closable : true,
                                            modal: true,
                                            width: 408,
                                            height: 150,
                                            bodyPadding: 8,
                                            y: 50,
                                            layout: 'anchor',
                                            items: [{
                                                xtype : 'textarea',
                                                fieldLabel : 'Comment',
                                                maxLength: 70,
                                                name : 'comment',

                                                value : obj.comment,
                                                anchor : '100%'
                                            }, {
                                                xtype : 'numberfield',
                                                fieldLabel : 'Active Area',
                                                name : 'actarea',
                                                value : obj.actarea,
                                                anchor : '50%'
                                            }],
                                            buttons : [    {
                                                    text: 'Save',
                                                    handler: function () {

                                                        var form = this.up('form');
                                                        form.submit({
                                                            scope: this,
                                                            method: 'POST',
                                                            url: rootFolder + 'wafer/comment',
                                                            submitEmptyText: false,
                                                            waitMsg: 'Saving comment ...',
                                                            params: {
                                                                code: code,
                                                                testId: testId,
                                                                tkey: tkey,
                                                                devtype: obj.devtype
                                                            },
                                                            success: function (frm, action) {
                                                                win.close();
                                                                gloApp
                                                                    .getController(
                                                                    'WaferPanel')
                                                                    .drawMap(
                                                                    "SLIDER",
                                                                    code,
                                                                    tkey,
                                                                    testId,
                                                                    propertyName,
                                                                    propCombo
                                                                        .getValue(),
                                                                    propCombo2
                                                                        .getValue(),
                                                                    null,
                                                                    null)
                                                            }
                                                        });
                                                    }
                                            }]
                                        });

                                        win.show();


                                    }
                                });
                            }
                        },
                        {
                            text: 'Chart analysis',
                            handler: function () {
                                gloApp.getController('MeasurePanel').show(code, pctg, pkey, tkey, propertyName, clickedItems);
                            }
                        },
                        {
                            text: 'Uniformity images',
                            handler: function () {
                                window.open(rootFolder + 'uniformityImages?id=' + code + '&testId=' + testId, 'resizable,scrollbars');
                            }
                        },
                        {
                            text: 'Top devices images',
                            handler: function () {
                                window
                                    .open(
                                        rootFolder + 'topImages?id=' + code,
                                    'resizable,scrollbars');
                            }
                        },
                        {
                            text: 'Print',
                            handler: function () {

                                var tab = Ext
                                    .getCmp("waferTabPanel");
                                var win = tab
                                    .getActiveTab();
                                var DocumentContainer = win.items
                                    .get(0)
                                    .getEl().dom;
                                var WindowObject = window
                                    .open(
                                    '',
                                    'PrintWindow',
                                    'width=800,height=700,top=50,left=50,toolbars=no,scrollbars=yes,status=no,resizable=yes');
                                WindowObject.document
                                    .writeln(DocumentContainer.innerHTML);
                                WindowObject.document
                                    .close();
                                WindowObject
                                    .focus();
                                WindowObject
                                    .print();
                                WindowObject
                                    .close();

                            }
                        },
                        {
                            text: 'Close',
                            handler: function () {
                                this.up('window')
                                    .destroy();
                            }
                        }
                    ]
                });

            store.on('load', function (ds, records, o) {

                if (records[1] !== undefined && records[1].data !== undefined) {
                    propCombo.setValue(records[1].data.propName,
                        records[1].data.propName);
                }
            });

            propCombo
                .on(
                'change',
                function (combo, newValue, oldValue, opts) {

                    if (newValue[0]) {

                        clickedItems = new Array();
                        var value2
                        if (propCombo2.getValue())
                            value2 = propCombo2
                                .getValue()[0];
                        store2.getProxy().extraParams.level2 = newValue[0];
                        store2
                            .load({
                                callback: function (store, records, options) {

                                    if (newValue[0] == 'Current @ 2V') {

                                        // propCombo2.setValue('[NOT
                                        // APPLICABLE]');
                                        propCombo2
                                            .setValue('NA');

                                        gloApp
                                            .getController(
                                            'WaferPanel')
                                            .drawMap(
                                            "LIST",
                                            code,
                                            tkey,
                                            testId,
                                            propertyName,
                                            propCombo
                                                .getValue(),
                                            null,
                                            null,
                                            null);

                                    } else if (value2) {
                                        propCombo2
                                            .setValue(value2);
                                    }
                                }
                            });

                    }
                });

            propCombo2.on('change', function (combo, newValue, oldValue, opts) {

                if (newValue[0]) {

                    clickedItems = new Array();

                    gloApp.getController('WaferPanel').drawMap(
                        "LIST", code, tkey, testId,
                        propertyName, propCombo.getValue(),
                        newValue[0], null, null);
                }

            });

            propCombo3.on('change', function (combo, newValue, oldValue, opts) {

                if (newValue[0]) {
                    gloApp.getController('WaferPanel')
                        .drawChart(code, tkey, testId,
                        'otherDependencies');
                }

            });

            Ext.Ajax
                .request({
                    method: 'GET',
                    params: {
                        role: 'ROLE_TEST_ADMIN'
                    },
                    url: rootFolder + 'users/hasRole',
                    success: function (response) {
                        var obj = Ext
                            .decode(response.responseText);
                        if (obj.ok == '0') {
                            var tab = Ext
                                .getCmp('gridtestDataAdminFiltersId');
                            Ext.getCmp('waferFiltersTabPanel')
                                .remove(tab);
                        }
                    }
                });

            // Ext.Ajax.request({
            // method : 'GET',
            // params : {
            // role : 'ROLE_SPEC_ADMIN'
            // },
            // url : rootFolder + 'users/hasRole',
            // success : function(response) {
            // var obj = Ext.decode(response.responseText);
            // if (obj.ok == '0') {
            // var tab = Ext.getCmp('specWaferFilterTab');
            // Ext.getCmp('waferFiltersTabPanel').remove(tab);
            // }
            // }
            // });

            win.show();

            store.load();

        },

        exportData: function (tkey, code, testId, propertyName, level1) {

            var sData;
            var sDomain = rootFolder + 'wafer/export';
            sData = "<p><b>EXPORTING DATA . . . . . . . </b></p>";
            sData = sData + "<form name='f11' id='f11' action='" + sDomain + "' method='post'>";
            sData = sData + "<input type='hidden' name='tkey' value='" + tkey + "' />";
            sData = sData + "<input type='hidden' name='code' value='" + code + "' />";
            sData = sData + "<input type='hidden' name='testId' value='" + testId + "' />";
            sData = sData + "<input type='hidden' name='propertyName' value='" + propertyName + "' />";
            sData = sData + "<input type='hidden' name='level1' value='" + level1 + "' />";
            sData = sData + "<input type='hidden' name='codes' value='" + filteredItems + "' />";
            sData = sData + "</form>";
            var OpenWindow = window.open("", "newwin" + Math.random() * 16);
            OpenWindow.document.write(sData);
            OpenWindow.document.f11.submit();

        },

        drawMap: function (source, code, tkey, testId, propertyName, level1, level2, minSpec, maxSpec) {

            var panel = Ext.getCmp("waferMapId");
            var tab = Ext.getCmp("waferTabPanel");
            tab.setActiveTab(0);
            panel.setLoading(true);

            var rangeKey = level1 + '_' + level2;
            if (source == 'SLIDER' && cachedRange[rangeKey] == undefined && minSpec != null) {
                cachedRange[rangeKey] = [minSpec, maxSpec];
            }
            if (source == 'LIST') {
                if (cachedRange[rangeKey] != undefined) {
                    minSpec = cachedRange[rangeKey][0];
                    maxSpec = cachedRange[rangeKey][1];
                }
            }

            var spc = '';
            if (Ext.getCmp('waferSpecComboId') != undefined)
                spc = Ext.getCmp('waferSpecComboId').value;

            Ext.Ajax
                .request({
                    scope: this,
                    method: 'GET',
                    params: {
                        source: source,
                        code: code,
                        tkey: tkey,
                        testId: testId,
                        propertyName: propertyName,
                        level1: level1,
                        level2: level2,
                        minSpec: minSpec,
                        maxSpec: maxSpec,
                        act: acTT,
                        spec: spc,
                        top5: Ext.getCmp('isTop5').value,
                        bypassAdminFilter: Ext.getCmp('bypassAdminFilter').value
                    },
                    url: rootFolder + 'wafer/map',
                    success: function (response) {
                        var obj = Ext
                            .decode(response.responseText);
                        if (obj.success == false) {
                            Ext.ux.Message.msg('Wafer map',
                                obj.msg);
                        } else {
                            panel.removeAll(true);
                            var drawComponent = Ext.create(
                                'Ext.draw.Component', {
                                    viewBox: false,
                                    align: 'stretch',
                                    items: obj.devices
                                });

                            drawComponent.on(
                                'render',
                                function (cmp) {
                                    filteredItems = new Array();
                                    for (var item in cmp.surface.items) {
                                        var sprites = cmp.surface.items[item];
                                        if (sprites instanceof Array) {
                                            for (var i in sprites) {
                                                var sprite = sprites[i];
                                                if (sprite.name != undefined || sprite.name == '') {
                                                    Ext.create(
                                                        'Ext.tip.ToolTip', {
                                                            html: sprite.name + '<br/>' + sprite.text,
                                                            target: sprite.id,
                                                            trackMouse: true,
                                                            showDelay: 10
                                                        });

                                                    if (sprite.name.indexOf("Total:") == -1) {
                                                        Ext.Array.include(
                                                            filteredItems,
                                                            sprite.name);
                                                    }

                                                    if (Ext.Array.contains(
                                                        clickedItems,
                                                            code + '_' + sprite.name)) {
                                                        sprite.setAttributes({
                                                                'stroke': '#000000'
                                                            },
                                                            true);
                                                        if (sprite.type == 'rect')
                                                            sprite.setAttributes({
                                                                    'stroke-width': 3
                                                                },
                                                                true);
                                                        else
                                                            sprite.setAttributes({
                                                                    'stroke-width': 1
                                                                },
                                                                true);
                                                        sprite.setAttributes({
                                                                'font': '9px Arial'
                                                            },
                                                            true);
                                                    }

                                                    if (sprite instanceof Ext.draw.Sprite) {
                                                        sprite.on(
                                                            'click',
                                                            function (b, e) {
                                                                Ext.Array.include(
                                                                    clickedItems,
                                                                        code + '_' + b.name);
                                                                b.setAttributes({
                                                                        'stroke': '#000000'
                                                                    },
                                                                    true);
                                                                if (b.type == 'rect')
                                                                    b.setAttributes({
                                                                            'stroke-width': 3
                                                                        },
                                                                        true);
                                                                else b.setAttributes({
                                                                        'stroke-width': 1
                                                                    },
                                                                    true);
                                                                b.setAttributes({
                                                                        'font': '9px Arial'
                                                                    },
                                                                    true);
                                                            });
                                                    }
                                                }
                                            }
                                        }
                                    }
                                });

                            if (source == 'LIST') {
                                // mapMultiSlide.setMinValue(obj.spec.min);
                                // mapMultiSlide.setMaxValue(obj.spec.max);
                                // mapMultiSlide.setValue(0,
                                // obj.spec.mins, true);
                                // mapMultiSlide.setValue(1,
                                // obj.spec.maxs, true);
                                Ext.getCmp('waferRange1Id').setValue(obj.spec.mins);
                                Ext.getCmp('waferRange2Id').setValue(obj.spec.maxs);
                            }

                            panel.add(drawComponent);
                            panel.devices = obj.devices;
                        }
                        panel.setLoading(false);
                    }
                });
        },

        drawChart: function (code, tkey, testId, propertyName) {

            var level, levelX, levelY;

            var tab = Ext.getCmp("waferTabPanel");
            var win = tab.getActiveTab();
            var chartType = win.items.get(0).id;

            if (chartType == 'waferSpectrumChart') {
                level = Ext.getCmp('propLevel1Id').value;
            }
            if (chartType == 'waferVoltageSweepChart') {
                level = 'Datavoltage';
            }
            if (chartType == 'waferCurrentSweepChart') {
                level = Ext.getCmp('propLevel2Id').value;
            }
            if (chartType == 'waferOtherChart') {
                level = Ext.getCmp('propLevel1Id').value;
                levelY = Ext.getCmp('propLevel2Id').value;
                levelX = Ext.getCmp('propLevelXId').value;
            }

            if (level == undefined) {
                return;
            }

            var panel = Ext.getCmp('waferMapId');
            var devices = new Array();
            for (var device in panel.devices) {
                if (panel.devices[device].code != undefined) {
                    Ext.Array.include(devices,
                        panel.devices[device].code);
                }
            }

            if (devices.length == 0) {
                return;
            }

            var panel2 = Ext.getCmp(chartType);
            panel2.setLoading(true);

            Ext.Ajax
                .request({
                    scope: this,
                    method: 'POST',
                    params: {
                        chartType: chartType,
                        code: code,
                        tkey: tkey,
                        testId: testId,
                        propertyName: propertyName,
                        level: level,
                        levelX: levelX,
                        levelY: levelY,
                        logharitmic: Ext.getCmp('isLogharitmic').value,
                        units: Ext.JSON.encode(devices)
                    },
                    url: rootFolder + 'wafer/chart',
                    success: function (response) {
                        var obj = Ext
                            .decode(response.responseText);
                        if (obj.success == false) {
                            Ext.ux.Message.msg('Wafer chart',
                                obj.msg);
                        } else {

                            if (chartType != 'waferOtherChart') {

                                panel2.removeAll(true);
                                Ext.suspendLayouts();
                                var drawComponent = Ext.create(
                                    'Ext.draw.Component', {
                                        viewBox: false,
                                        align: 'stretch',
                                        items: obj.graphs
                                    });
                                Ext.resumeLayouts();

                                drawComponent
                                    .on(
                                    'render',
                                    function (cmp) {
                                        Ext.suspendLayouts();
                                        for (var item in cmp.surface.items) {
                                            var sprites = cmp.surface.items[item];
                                            if (sprites instanceof Array) {
                                                for (var i in sprites) {
                                                    var sprite = sprites[i];
                                                    if (sprite.name != undefined || sprite.name == '') {
                                                        Ext
                                                            .create(
                                                            'Ext.tip.ToolTip', {
                                                                html: sprite.name,
                                                                target: sprite.id,
                                                                trackMouse: true,
                                                                showDelay: 10
                                                            });
                                                        if (sprite instanceof Ext.draw.Sprite) {
                                                            sprite
                                                                .setAttributes({
                                                                    'stroke-width': 1
                                                                },
                                                                true);
                                                            sprite
                                                                .on(
                                                                'click',
                                                                function (b, e) {
                                                                    Ext.Array
                                                                        .include(
                                                                        clickedItems,
                                                                        b.name);
                                                                    b
                                                                        .setAttributes({
                                                                            'stroke': '#111111'
                                                                        },
                                                                        true);
                                                                    b
                                                                        .setAttributes({
                                                                            'stroke-width': 3
                                                                        },
                                                                        true);
                                                                });

                                                            if (Ext.Array
                                                                .contains(
                                                                clickedItems,
                                                                sprite.name)) {
                                                                sprite
                                                                    .setAttributes({
                                                                        'stroke': '#111111'
                                                                    },
                                                                    true);
                                                                sprite
                                                                    .setAttributes({
                                                                        'stroke-width': 3
                                                                    },
                                                                    true);
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        Ext.resumeLayouts();
                                    });

                                panel2.add(drawComponent);
                            } else {

                                var chart = Ext
                                    .getCmp('chartOtherDepId');
                                chart.bindStore(Ext.create(
                                    'Ext.data.JsonStore', {
                                        fields: ['code',
                                            'x', 'y'
                                        ]
                                    }));
                                chart.store
                                    .loadData(obj[0].data);
                                chart.refresh();

                                chart.axes
                                    .get('left')
                                    .setTitle(obj[0].yTitle);
                                chart.axes
                                    .get('bottom')
                                    .setTitle(obj[0].xTitle);

                            }
                        }
                        panel2.setLoading(false);

                    }
                });
        },

        onWaferFilterSelect: function (o, selections, code, tkey, testId, propertyName, level1, level2, minSpec, maxSpec, isAdmin) {
            var selects = new Array();
            Ext.Object.each(selections, function (record) {
                selects[record] = selections[record].data.id;
            });
            Ext.Ajax.request({
                scope: this,
                method: 'POST',
                params: {
                    selected: Ext.encode(selects),
                    isAdmin: isAdmin
                },
                url: rootFolder + 'wafer/selectFilter',
                success: function (response) {
                    var obj = Ext.decode(response.responseText);
                    if (obj.success == false) {
                        Ext.Msg.alert('Select filter failed',
                            obj.msg);
                    } else {
                        gloApp.getController('WaferPanel').drawMap(
                            "SLIDER", code, tkey, testId,
                            propertyName, level1, level2,
                            minSpec, maxSpec);
                    }
                }
            });
        },

        onDeleteWaferFilter: function (grid, cell, row, col, e) {

            var rec = grid.getStore().getAt(row);
            var t = Ext.getCmp('waferFiltersTabPanel')
                .getActiveTab().id;
            Ext.Ajax.request({
                scope: this,
                method: 'POST',
                params: {
                    id: rec.get('id'),
                    tab: t
                },
                url: rootFolder + 'wafer/deleteFilter',
                success: function (response) {
                    var obj = Ext.decode(response.responseText);
                    if (obj.success == false) {
                        Ext.Msg.alert('Delete filter failed',
                            obj.msg);
                    } else {
                        grid.store.remove(rec);
                    }
                }
            });
        },

        onDeleteWaferSummary: function (grid, cell, row, col, e) {

            var rec = grid.getStore().getAt(row);
            var t = Ext.getCmp('waferFiltersTabPanel')
                .getActiveTab().id;
            Ext.Ajax.request({
                scope: this,
                method: 'POST',
                params: {
                    id: rec.get('id')
                },
                url: rootFolder + 'wafer/deleteSummary',
                success: function (response) {
                    var obj = Ext.decode(response.responseText);
                    if (obj.success == false) {
                        Ext.Msg.alert('Delete summary failed',
                            obj.msg);
                    } else {
                        grid.store.remove(rec);
                    }
                }
            });
        }
    });