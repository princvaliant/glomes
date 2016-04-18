

Ext.define('glo.view.equipment.List', {
	extend: 'Ext.panel.Panel',
	alias: 'widget.equipmentlist',

    requires: [
       'Ext.toolbar.Toolbar',
       'Ext.grid.Panel'
    ],

	title: 'Manage E10 statuses',
	collapsible: false,
	border: false,
	margins: '0 3 0 0',
	layout: 'fit',

	initComponent: function() {
		
		this.addEvents('equipmentselect');
		this.addEvents('equipmentadd');
		this.addEvents('equipmentdelete');
		this.addEvents('equipmentchangestatus');
				
		Ext.apply(this, {
			dockedItems: [{
				xtype: 'toolbar',
				items: [
//	            {
//					text : 'Add',
//					iconCls : 'icon-shape_square_add',
//					scope: this,
//					handler : function() {
//						this.fireEvent('equipmentadd', this);
//					}
//				}, 
//				{
//					id : 'removeEquipment',
//					text : 'Delete',
//					iconCls : 'icon-shape_square_delete',
//					scope: this,
//					handler : function() {
//						this.fireEvent('equipmentdelete', this);
//					},
//					disabled : true
//				},
				{
					id : 'changeEquipmentStatus',
					text : 'Change status',
					iconCls : 'icon-report',
					scope: this,
					handler : function() {
						this.fireEvent('equipmentchangestatus', this);
					},
					disabled : true
				},
				'->',
				{
					xtype : 'combo',
					store : Ext.create('glo.store.WorkCenters', {pageSize:10}),
					width: 150,
					mode: 'remote',
					editable: false,
					stateful: true,
					id: 'workCenterFilterComboId',
					stateId:'workCenterFilteCombo',
					action : 'workCenterSelect',
					displayField:'name',
				    triggerAction : 'all',
				    emptyText: '<filter by workcenter>',
				    listConfig: {
				        loadingText: 'Searching workcenters ...',
				      	valueNotFoundText : 'No workcenters found.'
				    },
				    pageSize: 10 
			    },
				{
				    xtype: 'textfield',
				   	id : 'searchEquipmentId',
				   	emptyText: '<filter equipments>',
				   	enableKeyEvents: true
				},
				'-'
				]
			}]
		});
		
		this.callParent(arguments);
	}
});
