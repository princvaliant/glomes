Ext.define('glo.view.configuration.Panel', {
			extend : 'Ext.panel.Panel',
			alias : 'widget.configurationpanel',
			layout : 'border',
			resizable : true,
			category : '',

			requires : ['glo.view.configuration.List', 'glo.view.configuration.Form',
					'glo.view.configuration.Content','glo.view.configuration.Toolbar','glo.view.configuration.Variables', 'Ext.data.*', 'Ext.tab.Panel',
					'Ext.layout.container.Border'],

			initComponent : function() {
				Ext.apply(this, {
							items : [{
										xtype : 'configurationlist',
										region : 'west',
										width : 400
									}, {
										xtype : 'panel',
										layout: 'border',
										region : 'center',
										title : 'Selected configuration',
										dockedItems : {
											xtype : 'toolbar',
											items : [{
														text : 'Upload excel template',
														iconCls : 'icon-file_upload',
														scope: this,
														handler : function() {
															this.fireEvent('uploadexceltemplate', this);
														}
													},'->',{
														xtype : 'checkbox',
														id : 'dataViewActiveId',
														fieldLabel: 'Only active units',
														checked: true,
														margin: '0 20 0 0'
													},{
														xtype : 'datefield',
														id : 'dataViewStartId',
														name : 'startDate',
														width : 100,
														margin : '0 5 0 0',
														allowBlank : false
													}, {
														xtype : 'datefield',
														id : 'dataViewEndId',
														name : 'endDate',
														width : 100,
														allowBlank : false
													}, {
														text : 'Export to excel',
														iconCls : 'icon-excel',
														scope: this,
														handler : function() {
															this.fireEvent('exportdataview', this);
														}
													}]
										},
										items :[{
										        xtype : 'configurationtoolbar',
										        region : 'west',
										        width : 70
										},
										{
									        xtype : 'configurationcontent',
									        region : 'center'
										}]
									}, {
										xtype : 'configurationvariables',
										region : 'east',
										width : 11
									}]
						});

				this.callParent(arguments);
			}
		});
