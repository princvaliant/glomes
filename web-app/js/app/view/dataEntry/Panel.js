Ext.define('glo.view.dataEntry.Panel', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.dataentrypanel',
    layout: 'border',
    resizable: true,
    category: '',

    requires: ['glo.view.dataEntry.List', 'glo.view.dataEntry.Form',
        'glo.view.dataEntry.Content', 'Ext.data.*', 'Ext.tab.Panel',
        'Ext.layout.container.Border'],

    initComponent: function () {
        Ext.apply(this, {
            items: [
                {
                    xtype: 'dataentrylist',
                    region: 'west',
                    width: 300,
                    stateful: true,
                    collapsible: true,
                    split: true,
                    stateId: 'dataentryListStateId'
                },
                {
                    xtype: 'dataentrycontent',
                    region: 'center'
                }
            ]
        });

        this.callParent(arguments);
    }
});
