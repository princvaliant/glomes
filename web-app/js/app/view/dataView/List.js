

Ext.define('glo.view.dataView.List', {
	extend: 'Ext.panel.Panel',
	alias: 'widget.dataviewlist',

    requires: [
       'Ext.toolbar.Toolbar',
       'Ext.grid.Panel'
    ],

	title: 'Export process data',
	collapsible: false,
	border: false,
	margins: '0 3 0 0',
	layout: 'fit',

	initComponent: function() {

		this.addEvents('dataviewselect');
		this.addEvents('dataviewadd');
		this.addEvents('dataviewdelete');
		this.addEvents('dataviewedit');
		this.addEvents('dataviewduplicate');
				
		Ext.apply(this, {
			dockedItems: [{
				xtype: 'toolbar',
				items: [
	            {
					text : 'Add',
					iconCls : 'icon-shape_square_add',
					scope: this,
					handler : function() {
						this.fireEvent('dataviewadd', this);
					}
				}, {
					id : 'editDataView',
					text : 'Edit',
					iconCls : 'icon-table_edit',
					scope: this,
					handler : function() {
						this.fireEvent('dataviewedit', this);
					},
					disabled : true
				},
				{
					id : 'removeDataView',
					text : 'Delete',
					iconCls : 'icon-shape_square_delete',
					scope: this,
					handler : function() {
						this.fireEvent('dataviewdelete', this);
					},
					disabled : true
				},
				{
					id : 'duplicateDataView',
					text : 'Duplicate',
					iconCls : 'icon-disk_multiple',
					scope: this,
					handler : function() {
						this.fireEvent('dataviewduplicate', this);
					},
					disabled : true
				},
				'->',
				{
				    xtype: 'textfield',
				   	id : 'searchDataViewId',
					stateful: true,
					stateId:'dataViewFilterTextbox',
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
