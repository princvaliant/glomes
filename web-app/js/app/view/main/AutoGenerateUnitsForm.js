
Ext.define('glo.view.main.AutoGenerateUnitsForm',
				{
					extend : 'Ext.form.Panel',
					alias : 'widget.mainautogenerateunitsform',
					layout : 'auto',
					ownerStore : '',
					standardSubmit : 'false',
					fieldDefaults : {
						labelWidth : 99,
						anchor : '100%',
						labelAlign : 'right',
						width : 325	,
						margin : 2
					},
					items : [ {
						xtype : 'tabpanel',
						activeTab : 0,
						width: 480,
						height: 150,
						padding: 2,
						layout: 'anchor',
						bodyPadding : 7,
						items : [ {
							title : 'Sequence',
							items : [ {
								id : 'startLotId',
								name : 'startLotId',
								xtype : 'textfield',
								fieldLabel : 'Start code',
								anchor : '100%',
								allowBlank : true
							}, {
								id : 'startNumberId',
								name : 'startNumberId',
								xtype : 'numberfield',
								fieldLabel : 'Start number',
								minValue : 1,
								anchor : '100%',
								allowBlank : true
							}, {
								id : 'startLotCountId',
								name : 'startLotCountId',
								xtype : 'numberfield',
								fieldLabel : 'Count',
								anchor : '100%',
								minValue : 1,
								allowBlank : true
							} ]
						}, {
							title : 'Individual',
							items : {
									id : 'indId',
										name : 'indId',
										xtype:'textarea',
										fieldLabel : 'Serial #',
										allowBlank : true
							}
						} ]
					}, {
						xtype : 'fieldset',
						id : "autoGenerateItems",
						checkboxToggle : false,
						title : 'Data collection',
						defaultType : 'numberfield',
						layout : 'column',
						padding : 9,
						fieldDefaults : {
							labelWidth : 99,
							anchor : '100%',
							labelAlign : 'right',
							width : 325	,
							margin : 2
						},
						collapsed : false
					} ],
					buttons : [ {
						text : 'Generate',
						handler : function() {
							var form = this.up('form');
							var win = this.up('window');
							if (form.getForm().isValid()) {

								var lot = Ext.getCmp('startLotId').value;
								var nmb = Ext.getCmp('startNumberId').value;
								var cnt = Ext.getCmp('startLotCountId').value;
								
								var ind = Ext.getCmp('indId').value;
								
								var valuesFull = form.getValues();
								var values = {};

								// remove values with empty value
								for ( var key in valuesFull) {
									if (valuesFull[key] != ''
											&& key != 'startLotId'
											&& key != 'startNumberId'
											&& key != 'startLotCountId' 
											&& key != 'indId') {
										values[key] = valuesFull[key];
									}
								}
								
								var units = new Array();

								if (ind == undefined) {
									for (i = nmb; i < nmb + cnt; i++) {
										
										if (lot != '')
											values['code'] = lot + i;
										else 
											values['code'] = '';
										values['qty'] = 1;
										
										var r = Ext.ModelManager.create(
														values,
														Ext.getCmp('AutoGenerateUnitsFormId').ownerStore.model);
										Ext.Array.include(units, r);
									}

								} else {
									var lst = ind.split(',');
									for (j = 0; j < lst.length; j++) {
										values['code'] = lst[j]
										values['qty'] = 1
										var q = Ext.ModelManager.create(
												values,
												Ext.getCmp('AutoGenerateUnitsFormId').ownerStore.model);
										Ext.Array.include(units, q);
									}
									
								}
								
								Ext.getCmp('AutoGenerateUnitsFormId').ownerStore.loadRecords(units);
								
								win.destroy();
							}
						}
					} ]

				});
