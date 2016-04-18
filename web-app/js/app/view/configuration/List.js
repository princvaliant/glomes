

Ext.define('glo.view.configuration.List', {
	extend: 'Ext.panel.Panel',
	alias: 'widget.configurationlist',
    requires: [
       'Ext.toolbar.Toolbar',
       'Ext.grid.Panel'
    ],

	title: 'Configure processes',
	collapsible: false,
	margins: '3 2 3 2',
	layout: 'fit',

	initComponent: function() {

		this.addEvents('configurationselect');
		this.addEvents('configurationadd');
		this.addEvents('configurationdelete');
		this.addEvents('configurationedit');
		this.addEvents('configurationduplicate');
				
		Ext.apply(this, {
			dockedItems: [{
				xtype: 'toolbar',
				items: [
	            {
					text : 'Add',
					iconCls : 'icon-shape_square_add',
					scope: this,
					handler : function() {
						this.fireEvent('configurationadd', this);
					}
				}, {
					id : 'editConfiguration',
					text : 'Edit',
					iconCls : 'icon-table_edit',
					scope: this,
					handler : function() {
						this.fireEvent('configurationedit', this);
					},
					disabled : true
				},
				{
					id : 'removeConfiguration',
					text : 'Delete',
					iconCls : 'icon-shape_square_delete',
					scope: this,
					handler : function() {
						this.fireEvent('configurationdelete', this);
					},
					disabled : true
				},
				{
					id : 'duplicateConfiguration',
					text : 'Duplicate',
					iconCls : 'icon-disk_multiple',
					scope: this,
					handler : function() {
						this.fireEvent('configurationduplicate', this);
					},
					disabled : true
				},
				'->',
				{
				    xtype: 'textfield',
				   	id : 'searchDataViewId',
				   	emptyText: '<filter data views>',
				   	enableKeyEvents: true
				},
				'-'
				]
			}]
		});
		
		this.callParent(arguments);
	}
});
