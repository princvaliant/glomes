

Ext.define('glo.view.dataView.Form', {
			extend : 'Ext.form.Panel',
			alias : 'widget.dataviewform',
			border : false,
			minWidth : 500,
			minHeight : 310,
			layout : 'anchor',
			defaultType : 'textfield',
			fieldDefaults : {
				labelWidth : 135,
				labelAlign : 'right',
				width : 410,
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
										name : 'isPublic'
									}, {
										xtype : 'checkbox',
										fieldLabel : 'Publish to dashboard',
										name : 'publishToDashboard'
									}, {
										fieldLabel : 'Tag',
										name : 'tag',
										anchor : '90%'
									}, {
										xtype : 'textarea',
										fieldLabel : 'Description',
										name : 'description',
										anchor : '90%'
									},{
										xtype : 'textarea',
										fieldLabel : 'Custom report URL',
										name : 'urlExportData',
                                        height: 120,
										anchor : '90%'
									}, {
										xtype : 'checkbox',
										fieldLabel : 'Filter by path',
										name : 'filterByPath'
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
									url : rootFolder + 'dataViews',
									submitEmptyText : false,
									waitMsg : 'Saving data ...',
									params : {
										id : form.rid
									},
									success : function(frm, action) {
										Ext.ux.Message.msg(
												"Data saved successfully.", "");

										gloApp.getController('DataViewPanel').refreshData();

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
