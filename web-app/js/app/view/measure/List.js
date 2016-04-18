

Ext.define('glo.view.measure.List', {
	extend: 'Ext.panel.Panel',
	alias: 'widget.chartlist',
    requires: [
       'Ext.toolbar.Toolbar',
       'Ext.grid.Panel'
    ],

	title: 'List of reports',
	collapsible: false,
	margins: '3 2 3 2',
	layout: 'fit',

	initComponent: function() {

		this.addEvents('chartselect');
		this.addEvents('chartdelete');
		this.addEvents('chartedit');
				
		Ext.apply(this, {
			dockedItems: [{
				xtype: 'toolbar',
				items: [
				{
					text : 'Remove',
					iconCls : 'icon-shape_square_delete',
					scope: this,
					handler : function() {
						this.fireEvent('chartdelete', this);
					},
					disabled : true
				},
				'->',
				{
				    xtype: 'textfield',
				   	stateful: true,
					stateId:'chartFilterTextbox',
				   	emptyText: '<filter reports>',
				   	enableKeyEvents: true
				},
				'-'
				]
			}]
		});
		
		this.callParent(arguments);
	}
});
