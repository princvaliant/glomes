Ext.define('glo.view.measure.Panel', {
			extend : 'Ext.panel.Panel',
			alias : 'widget.chartpanel',
			layout : 'border',
			resizable : true,
			category : '',

			requires : ['glo.view.measure.List', 'glo.view.measure.Form',
					'glo.view.measure.Content','Ext.data.*', 'Ext.tab.Panel',
					'Ext.layout.container.Border'],

			initComponent : function() {
				
				Ext.apply(this, {
							items : [{
										xtype : 'chartlist',
										region : 'west',
										width : 400,
										stateful: true,
										collapsible: true,
										split: true,
										stateId: 'chartListStateId'
									}, {
								        xtype : 'chartcontent',
								        region : 'center'
									}]
						});

				this.callParent(arguments);
			}
		});
