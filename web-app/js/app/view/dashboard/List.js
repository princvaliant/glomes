

Ext.define('glo.view.dashboard.List', {
	extend: 'Ext.panel.Panel',
	alias: 'widget.dashboardlist',
    requires: [
       'Ext.toolbar.Toolbar',
       'Ext.grid.Panel'
    ],

	title: 'List of reports',
	collapsible: false,
	border: false,
	margins: '0 3 0 0',
	layout: 'fit',

	initComponent: function() {

		this.addEvents('dashboardselect');
		this.addEvents('dashboarddelete');
		this.addEvents('dashboardedit');
				
		Ext.apply(this, {
			dockedItems: [{
				xtype: 'toolbar',
				items: [
				{
					id : 'removeDashboard',
					text : 'Remove',
					iconCls : 'icon-shape_square_delete',
					scope: this,
					handler : function() {
						this.fireEvent('dashboarddelete', this);
					},
					disabled : true
				},
				'->',
				{
				    xtype: 'textfield',
				   	id : 'searchDashboardId',
				   	stateful: true,
					stateId:'dashboardFilterTextbox',
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
