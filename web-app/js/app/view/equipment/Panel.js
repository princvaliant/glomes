Ext.define('glo.view.equipment.Panel', {
			extend : 'Ext.panel.Panel',
			alias : 'widget.equipmentpanel',
			layout : 'border',
			resizable : true,
			category : '',

			requires : ['glo.view.equipment.List', 'glo.view.equipment.Content', 'glo.view.equipment.EquipmentMaintenanceForm', 'Ext.data.*', 'Ext.tab.Panel',
					'Ext.layout.container.Border'],

			initComponent : function() {
				Ext.apply(this, {
							items : [{
										xtype : 'equipmentlist',
										region : 'center'
										
									},{
										xtype : 'equipmentcontent',
										region : 'east',
										width : 710
									}]
						});

				this.callParent(arguments);
			}
		});
