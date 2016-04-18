

Ext.define('glo.view.dataEntry.List', {
	extend: 'Ext.panel.Panel',
	alias: 'widget.dataentrylist',

    requires: [
       'Ext.toolbar.Toolbar',
       'Ext.grid.Panel'
    ],

	title: 'List of equipments',
	collapsible: false,
	border: true,
	margins: '0 3 0 0',
	layout: 'fit',

	initComponent: function() {

		this.addEvents('dataentryselect');

		Ext.apply(this, {
			dockedItems: [{
				xtype: 'toolbar',
				items: [
                {
                    xtype: 'button',
                    text: 'Refresh',
                    enableKeyEvents: true,
                    action: 'refreshdataentrylist',
                    iconCls: 'icon-arrow_refresh'
                },
				'-'
				]
			}]
		});
		
		this.callParent(arguments);
	}
});
