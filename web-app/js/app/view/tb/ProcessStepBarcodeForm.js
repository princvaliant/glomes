

Ext.define('glo.view.tb.ProcessStepBarcodeForm', {
			extend : 'Ext.form.Panel',
			alias : 'widget.processstepbarcodeform',
			border : false,
			parentWindow : '',
			printer: '',
			pctg: '',
			pkey: '',
			fromTkey: '',
			toTkey: '',
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
						
				Ext.apply(this, {
							items: [{
										xtype : 'textarea',
										fieldLabel : 'Comment',
										name : 'note',
										anchor : '100%'
									}, {
										xtype : 'numberfield',
										fieldLabel : 'Priority',
										name : 'prior',
										value : 50,
										maxValue : 100,
										minValue : 1,
										anchor : '40%'
									}]
						});

				this.callParent(arguments);

			},

			buttons : [{
				text : 'Print',
				handler : function() {
					var form = this.up('form');
					var win = this.up('window');

					if (form.getForm().isValid()) {
						form.submit({
							scope : this,
							method : 'POST',
							url : rootFolder + 'units/printer',
							params : {
								label: 'Process move',
								printer : form.printer,
								units : '',
								pctg : form.pctg,
								pkey : form.pkey,
								fromTkey : form.fromTkey,
								tkey : form.toTkey
							},
							success : function(frm, action) {
								Ext.ux.Message.msg(
										"Barcode printed.", action.result.msg);
								//gloApp.getController('MainPanel')
								//		.loadList(form.parentWindow, form.pkey, form.tkey, 'stay');
								win.destroy();
							},
							failure : function(frm, action) {
								Ext.Msg.alert('Failed',
										action.result.msg);
							}
						});
						
					}
				}
			}, {
				text : 'Cancel',
				handler : function() {
					this.up('window').destroy();
				}
			}]
		});
