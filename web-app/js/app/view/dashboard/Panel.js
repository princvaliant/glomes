Ext.define('glo.view.dashboard.Panel', {
			extend : 'Ext.panel.Panel',
			alias : 'widget.dashboardpanel',
			layout : 'border',
			resizable : true,
			category : '',

			requires : ['glo.view.dashboard.List', 'glo.view.dashboard.Form',
					'glo.view.dashboard.Content','Ext.data.*', 'Ext.tab.Panel',
					'Ext.layout.container.Border'],

			initComponent : function() {
				
				Ext.apply(this, {
							items : [{
										xtype : 'dashboardlist',
										region : 'west',
										width : 400,
										stateful: true,
										collapsible: true,
										split: true,
										stateId: 'dashBoardListStateId'
									}, {
								        xtype : 'dashboardcontent',
								        region : 'center'
									}]
						});

				this.callParent(arguments);
			}
		});
