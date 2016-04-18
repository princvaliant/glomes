

Ext.define('glo.view.tb.BonusForm', {
			extend : 'Ext.form.Panel',
			alias : 'widget.bonusform',
			border : false,
			rows : '',
			parentWindow : '',
			processKeyEng : '',
			taskKeyEng : '',
			minWidth : 400,
			minHeight : 201,
			maxWidth : 600,
			maxHeight : 650,
			layout : 'anchor',
			defaultType : 'textfield',
			standardSubmit : false,
			bodyPadding : '3 9 3 9',

			initComponent : function() {

				this.callParent(arguments);
			},

			buttons : [{
				text : 'Apply bonus',
				handler : function() {
					var form = this.up('form');
					var win = this.up('window');

					if (form.getForm().isValid()) {

						var aResult = Ext.Array.map(form.rows, function(node) {
									return {
										id : node.data.id
									};
								});
						var units = Ext.encode(aResult);

						form.submit({
									scope : this,
									method : 'POST',
									url : rootFolder + 'units/bonus',
									submitEmptyText : false,
									waitMsg : 'Recording bonus ...',
									params : {
										units : units
									},
									success : function(frm, action) {
										Ext.ux.Message.msg(
												"Bonus successfully recorded.",
												"");
										gloApp.getController('MainPanel')
												.loadList(form.parentWindow, '', '', 'stay');
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
