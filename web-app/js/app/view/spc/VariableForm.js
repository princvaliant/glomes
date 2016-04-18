

Ext.define('glo.view.spc.VariableForm', {
			extend : 'Ext.form.Panel',
			alias : 'widget.spcvariableform',
			border : false,
			layout : 'anchor',
			defaultType : 'textfield',
			fieldDefaults : {
				labelWidth : 205,
				labelAlign : 'right',
				width : 375,
				margin : 2
			},
			standardSubmit : false,
			bodyPadding : 6,
            spcVarId : '',

			initComponent : function() {

				Ext.apply(this, {
							items : [ {
                                        xtype : 'numberfield',
                                        fieldLabel : 'Sort order',
                                        name : 'sortOrder',
                                        value : this.sortOrder,
                                        anchor : '60%'
                                    },{
										fieldLabel : 'Graph limit (example 10.0,20.0)',
										name : 'graphLimit',
										anchor : '96%',
                                        value : this.limits,
										allowBlank : true
									}, {
                                        fieldLabel : 'Custom filter',
                                        name : 'customFilter',
                                        anchor : '96%',
                                        value : this.filters,
                                        allowBlank : true
                                    },{
                                        fieldLabel : 'Product codes filter',
                                        name : 'productCodes',
                                        anchor : '96%',
                                        value : this.products,
                                        allowBlank : true
                                    }]
						});

				this.callParent(arguments);

			},

			buttons : [{
				text : 'Save',
				handler : function() {
					var form = this.up('form');
					var win = this.up('window');

					if (form.getForm().isValid()) {

						form.submit({
									scope : this,
									method : 'POST',
									url : rootFolder + 'spc/editVariable',
									submitEmptyText : false,
									waitMsg : 'Saving data ...',
									params : {
                                        spcVarId : form.spcVarId
									},
									success : function(frm, action) {
										Ext.ux.Message.msg("SPC variable Data saved successfully.", "");
										win.destroy();
									},
									failure : function(frm, action) {

										Ext.Msg.alert('Failed', action.result.msg);
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
