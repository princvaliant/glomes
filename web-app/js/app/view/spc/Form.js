

Ext.define('glo.view.spc.Form', {
			extend : 'Ext.form.Panel',
			alias : 'widget.spcform',
			border : false,
			minWidth : 380,
			minHeight : 160,
			layout : 'anchor',
			defaultType : 'textfield',
			fieldDefaults : {
				labelWidth : 85,
				labelAlign : 'right',
				width : 375,
				margin : 2
			},
			standardSubmit : false,
			bodyPadding : 6,

			initComponent : function() {

				Ext.apply(this, {
							items : [{
										fieldLabel : 'Name',
										name : 'name',
										anchor : '90%',
										allowBlank : false
									}, {
										xtype : 'checkbox',
										fieldLabel : 'Public',
										name : 'isPublic',
										 inputValue: true,
										uncheckedValue: false
									}, {
										fieldLabel : 'Tag',
										name : 'tag',
										anchor : '90%'
									}, {
										xtype : 'numberfield',
										fieldLabel : 'Sample size',
										name : 'sampleSize',
										anchor : '60%'
									}, {
										xtype : 'checkbox',
										fieldLabel : 'Text visible?',
										inputValue: true,
										uncheckedValue: false,
										name : 'showText'
									}, {
										fieldLabel : 'Filter',
										name : 'filter',
										anchor : '90%'
									}, {
										xtype : 'textarea',
										fieldLabel : 'Description',
										name : 'description',
										anchor : '90%'
									}]
						});

				this.callParent(arguments);

			},

			buttons : [{
				text : 'Save',
				handler : function() {
					var form = this.up('form');
					var runNumber = this.up('form').name;
					var win = this.up('window');

					if (form.getForm().isValid()) {

						form.submit({
									scope : this,
									method : 'POST',
									url : rootFolder + 'spc',
									submitEmptyText : false,
									waitMsg : 'Saving data ...',
									params : {
										id : form.rid
									},
									success : function(frm, action) {
										Ext.ux.Message.msg(
												"Data saved successfully.", "");

										gloApp.getController('SpcPanel').refreshData();

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
