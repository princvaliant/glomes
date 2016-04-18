Ext.define('glo.view.dataView.Panel', {
			extend : 'Ext.panel.Panel',
			alias : 'widget.dataviewpanel',
			layout : 'border',
			resizable : true,
			category : '',

			requires : ['glo.view.dataView.List', 'glo.view.dataView.Form',  'glo.view.dataView.Join', 'glo.view.dataView.ReportInputForm',
					'glo.view.dataView.Content', 'glo.view.dataView.Variables', 'Ext.data.*', 'Ext.tab.Panel',
					'Ext.layout.container.Border'],

			initComponent : function() {
				Ext.apply(this, {
						items : [{
								xtype : 'dataviewlist',
								region : 'west',
								width : 400,
								stateful: true,
								collapsible: true,
								split: true,
								stateId: 'dataviewListStateId'
							}, {
                                xtype: 'panel',
								layout : 'border',
								region : 'center',
                                resizable : true,
								items : [{
									xtype : 'dataviewcontent',
									region : 'center'
								},{
									xtype : 'dataviewjoin',
									region : 'south',
                                    collapsible: true,
                                    split: true
								}]
							}, {
								xtype : 'dataviewvariables',
								region : 'east',
								width : 260
							}]
						});

				this.callParent(arguments);
			}
		});
