

Ext.define('glo.view.tb.PrintBarcodeForm', {
			extend : 'Ext.form.Panel',
			alias : 'widget.printbarcodeform',
			border : false,
			parentWindow : '',
			units : '',
			pctg: '',
			pkey: '',
			tkey: '',
			labels: '',
			printers: '',
			destinations: '',
			ungroup: '',
			minWidth : 400,
			minHeight : 201,
			layout : 'anchor',
			defaultType: 'textfield',
			fieldDefaults : {
				labelWidth : 85
			},
			standardSubmit : false,
			bodyPadding : 9,

			initComponent : function() {
				
				var labelCombo = Ext.create('Ext.form.field.ComboBox', {
					id : 'labelCombo',
					name : 'labelCombo',
					fieldLabel : 'Select label',
					store : this.labels,
					value : this.labels[0],
					forceSelection : true,
					editable : false,
					multiSelect : false,
					triggerAction : 'all',
					queryMode : 'local',
					width : 325,
					labelWidth : 100
				});
				
				var originText = Ext.create('Ext.form.field.TextArea', {
					grow : true,
					fieldLabel : 'Move from:',
					visible : false,
					readOnly : true,
					width : 325,
					height : 25,
					labelWidth : 100
				});
				
				var destinationText = Ext.create('Ext.form.field.TextArea', {
					grow : true,
					fieldLabel : 'Move to:',
					visible : false,
					readOnly : true,
					width : 325,
					height : 25,
					labelWidth : 100
				});
				
				var destinationCombo = Ext.create('Ext.form.field.ComboBox', {
					id : 'destinationCombo',
					name : 'destinationCombo',
					fieldLabel : 'Move to:',
					store : this.destinations,
					//value : this.destinations[0],
					forceSelection : true,
					editable : false,
					multiSelect : false,
					triggerAction : 'all',
					queryMode : 'local',
					visible : false,
					width : 325,
					labelWidth : 100
				});
				
				var printerCombo = Ext.create('Ext.form.field.ComboBox', {
					id : 'printerCombo',
					name : 'printerCombo',
					fieldLabel : 'Printer',
					store : this.printers,
					value : this.printers[0],
					forceSelection : true,
					editable : false,
					multiSelect : false,
					triggerAction : 'all',
					queryMode : 'local',
					width : 325,
					labelWidth : 100
				});
				
				var ungroupHidden = true;
				if (this.ungroup != '') {
					ungroupHidden = false;
				}
				
				var items = [ labelCombo, originText, destinationText, printerCombo ];
				if (this.destinations.length == 0) {
					originText.setValue('<process flow>');
					destinationText.setValue(this.tkey);
				}
				else {
					originText.setValue(this.tkey);
					destinationCombo.setValue(this.destinations[0]);
					items = [ labelCombo, originText, destinationCombo, printerCombo ];
				}
				originText.hide();
				destinationText.hide();
				destinationCombo.hide();
				
				Ext.apply(this, {
							items: items,

							buttons : [{
								text : this.ungroup,
								hidden : ungroupHidden,
								handler : function() {
									var form = this.up('form');
									var win = this.up('window');
									var label = labelCombo.getValue();
									var printer = printerCombo.getValue();
									Ext.MessageBox.confirm(
											'Confirm',
											form.ungroup + '?',
											function(btn) {
												if (btn == 'yes') {
													form.submit({
														scope : this,
														method : 'POST',
														url : rootFolder + 'units/printer',
														params : {
															label: form.ungroup,
															printer : printer,
															units : form.units,
															pctg : form.pctg,
															pkey : form.pkey,
															fromTkey : '',
															tkey : form.tkey
														},
														success : function(frm, action) {
															Ext.ux.Message.msg(
																	"Success", action.result.msg);
															gloApp.getController('MainPanel')
																	.loadList(form.parentWindow, form.pkey, form.tkey, 'stay');
															win.destroy();
														},
														failure : function(frm, action) {
															Ext.Msg.alert('Failed',
																	action.result.msg);
														}
													});
												}
											});
									}
							}, { 
								xtype: 'tbspacer',
								width: 50
							}, {
								text : 'Print label',
								handler : function() {
									var form = this.up('form');
									var win = this.up('window');
									var label = labelCombo.getValue();
									var printer = printerCombo.getValue();
				
									if (form.getForm().isValid()) {
										
										// Process move barcode
										if (label == 'Process move') {
											var fromTkey = originText.getValue();
											var toTkey = destinationText.getValue();
											var title = 'Barcode for move to "' + toTkey + '"';
											if (fromTkey == '<process flow>')
												fromTkey = '';
											else {
												toTkey = destinationCombo.getValue();
												title = 'Barcode for move from "' + fromTkey + '" to "' + toTkey + '"';
											}
											var win2 = Ext.create('Ext.window.Window', {
												title : title,
												resizable : true,
												modal : true,
												width : 408,
												y : 50,
												layout : 'anchor',
												items : {
													id : 'processStepBarcodeFormId',
													xtype : 'processstepbarcodeform',
													parentWindow : form.parentWindow,
													printer : printer,
													pctg : form.pctg,
													pkey : form.pkey,
													fromTkey : fromTkey,
													toTkey : toTkey
												}
											});

											// Determine if input requires additional input attributes
											Ext.Ajax.request({
												scope : this,
												method : 'GET',
												params : {
													category : form.pctg,
													procKey : form.pkey,
													taskKey : toTkey
												},
												url : rootFolder + 'contents/din',
												success : function(response) {
													var obj = Ext.decode(response.responseText);
		
													// Add all remaining controls
													Ext.getCmp('processStepBarcodeFormId').add(obj.controls);
													var cntrl = gloApp.getController('TbContent');
		
													Ext.Object.each(obj.controls, function(key, value, myself) {
																if (value.id != undefined && value.xtype == 'combo') {
																	Ext.getCmp(value.id).on('select',
																			cntrl.onMoveFormComboSelect);
																}
															});
													Ext.Object.each(obj.controls, function(key, value, myself) {
																if (value.id != undefined && value.xtype == 'combo') {
																	cntrl.onMoveFormComboSelect(Ext
																					.getCmp(value.id), value.value,
																			null);
																}
															});
												}
											});
		
											win2.show();
											win.destroy();
										}
										
										// all other barcode labels
										else {
											form.submit({
												scope : this,
												method : 'POST',
												url : rootFolder + 'units/printer',
												params : {
													label: label,
													printer : printer,
													units : form.units,
													pctg : form.pctg,
													pkey : form.pkey,
													fromTkey : '',
													tkey : form.tkey
												},
												success : function(frm, action) {
													Ext.ux.Message.msg(
															"Barcode printed", action.result.msg);
													gloApp.getController('MainPanel')
															.loadList(form.parentWindow, form.pkey, form.tkey, 'stay');
													win.destroy();
												},
												failure : function(frm, action) {
													Ext.Msg.alert('Failed',
															action.result.msg);
												}
											});
										}
									}
								}
							}, {
								text : 'Cancel',
								handler : function() {
									this.up('window').destroy();
								}
							}]
				
				});

				labelCombo.on('select', function() {
					if (labelCombo.getValue() == 'Process move') {
						if (originText.getValue() == '<process flow>') {
							originText.show();
							destinationText.show();
						}
						else {
							originText.show();
							destinationCombo.show();
						}
					}
					else {
						originText.hide();
						destinationText.hide();
						destinationCombo.hide();
					}
				});
				
				this.callParent(arguments);

			},
				
		});
