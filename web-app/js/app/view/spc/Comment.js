

Ext.define('glo.view.spc.Comment', {
			extend : 'Ext.form.Panel',
			alias : 'widget.spccommentform',
			border : false,
			minWidth : 400,
			minHeight : 160,
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
										grow: true,
										height: 200,
										anchor : '96%'
									}]
						});

				this.callParent(arguments);

			},

			buttons : [{
				text : 'Submit',
				handler : function() {
					var form = this.up('form');
					var win = this.up('window');

					if (form.getForm().isValid()) {

						var aResult = Ext.Array.map(clickedSpcItems, function(node) {
							return {
								code : node.code,
								path : node.p,
								variable : node.text
							};
						});
						var units = Ext.encode(aResult);

						form.submit({
									scope : this,
									method : 'POST',
									url : rootFolder + 'spc/comment',
									submitEmptyText : false,
									waitMsg : 'Adding comments ...',
									params : {
										units : units
									},
									success : function(frm, action) {
										
										gloApp.getController('SpcPanel').onClearSelection(this);
										Ext.ux.Message.msg("Comments added successfully.", "");
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
