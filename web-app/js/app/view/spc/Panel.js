Ext.define('glo.view.spc.Panel', {
			extend : 'Ext.panel.Panel',
			alias : 'widget.spcpanel',
			layout : 'border',
			resizable : true,
			category : '',

			requires : ['glo.view.spc.List', 'glo.view.spc.Form', 'glo.view.spc.Variables',
					'glo.view.spc.Content', 'Ext.data.*', 'Ext.tab.Panel',
					'Ext.layout.container.Border'],

			initComponent : function() {
				Ext.apply(this, {
							items : [{
										xtype : 'spclist',
										region : 'west',
										width : 400,
										stateful: true,
										collapsible: true,
										split: true,
										stateId: 'spcListStateId'
									}, {
										xtype : 'spccontent',
										layout : 'auto',
                                        region : 'center',
 										border : false,
										autoScroll: true
									},{
										xtype : 'spcvariables',
										region : 'east',
										width : 260
									}]
						});

				this.callParent(arguments);
			}
		});
