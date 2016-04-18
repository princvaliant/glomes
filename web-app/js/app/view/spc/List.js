

Ext.define('glo.view.spc.List', {
	extend: 'Ext.panel.Panel',
	alias: 'widget.spclist',

    requires: [
       'Ext.toolbar.Toolbar',
       'Ext.grid.Panel'
    ],

	title: 'Process control charts [BETA]',
	collapsible: false,
	border: false,
	margins: '0 3 0 0',
	layout: 'fit',

	initComponent: function() {

		this.addEvents('spcselect');
		this.addEvents('spcadd');
		this.addEvents('spcdelete');
		this.addEvents('spcedit');
		this.addEvents('spcduplicate');
				
		Ext.apply(this, {
			dockedItems: [{
				xtype: 'toolbar',
				items: [
	            {
					text : 'Add',
					iconCls : 'icon-shape_square_add',
					scope: this,
					handler : function() {
						this.fireEvent('spcadd', this);
					}
				}, {
					id : 'editspc',
					text : 'Edit',
					iconCls : 'icon-table_edit',
					scope: this,
					handler : function() {
						this.fireEvent('spcedit', this);
					},
					disabled : true
				},
				{
					id : 'removespc',
					text : 'Delete',
					iconCls : 'icon-shape_square_delete',
					scope: this,
					handler : function() {
						this.fireEvent('spcdelete', this);
					},
					disabled : true
				},
				{
					id : 'duplicatespc',
					text : 'Duplicate',
					iconCls : 'icon-disk_multiple',
					scope: this,
					handler : function() {
						this.fireEvent('spcduplicate', this);
					},
					disabled : true
				},
				'->',
				{
				    xtype: 'textfield',
				   	id : 'searchspcId',
					stateful: true,
					stateId:'spcFilterTextbox',
				   	emptyText: '<filter spc charts>',
				   	enableKeyEvents: true
				},
				'-'
				]
			}]
		});
		
		this.callParent(arguments);
	}
});
