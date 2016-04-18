

Ext.define('glo.view.equipment.EquipmentStatusComment', {
			extend : 'Ext.form.Panel',
			alias : 'widget.equipmentstatuscomment',
			border : false,
			minWidth : 400,
			minHeight : 160,
			statusId : '',
			comment : '',
			gridStore : '',
			layout : 'anchor',
			defaultType: 'textfield',
			fieldDefaults : {
				labelWidth : 85
			},
			standardSubmit : false,
			bodyPadding : 8,

			initComponent : function() {
					
				Ext.apply(this, {
							items: [{
										xtype : 'textarea',
										fieldLabel : 'Comment',
										value: this.comment,
										name : 'note',
										grow: true,
										height: 200,
										anchor : '98%'
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

						form.submit({
									scope : this,
									method : 'POST',
									url : rootFolder + 'equipments/comment',
									submitEmptyText : false,
									waitMsg : 'Adding comments ...',
									params : {
										statusId : form.statusId
									},
									success : function(frm, action) {
										Ext.ux.Message.msg("Comments changed successfully.", "");
										form.gridStore.load();
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
