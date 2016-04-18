Ext.define('glo.view.tb.Panel', {
			extend : 'Ext.panel.Panel',
			alias : 'widget.taskbookpanel',
			layout : 'border',
			resizable : true,
			category : '',

			requires : ['glo.view.tb.List', 'glo.view.tb.Content', 'glo.view.tb.YieldLossForm', 'glo.view.tb.BonusForm','glo.view.tb.DetailsForm',
			         'glo.view.tb.ReworkForm', 'glo.view.tb.MoveForm', 'glo.view.tb.MergeForm', 'glo.view.tb.ProcessStepBarcodeForm', 'glo.view.tb.MultiEditForm',
			         'glo.view.tb.PrintBarcodeForm', 'Ext.data.*', 'Ext.tab.Panel', 'Ext.layout.container.Border', 'glo.view.tb.FileUploadForm'],

			initComponent : function() {
				Ext.apply(this, {
							items : [{
										xtype : 'taskbookcontent',
										region : 'center'
									}, {
										xtype : 'taskbooklist',
										region : 'west',
										split: true,
										width : 275
									}]
						});

				this.callParent(arguments);
			}
		});
