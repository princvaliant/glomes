

Ext.define('glo.view.equipment.EquipmentStatusForm', {
			extend : 'Ext.form.Panel',
			alias : 'widget.equipmentstatusform',
			border : false,
			rows : '',
			eid: '',
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
				
				
				var storeFailures = Ext.create('Ext.data.Store', {
							fields : [{
										name : 'code',
										type : 'string'
									}, {
										name : 'comment',
										type : 'string'
									}],
							autoDestroy : false,
							autoLoad : false,
							autoSync : false,
							proxy : {
								type : 'rest',
								method : 'GET',
								reader : {
									type : 'json',
									root : 'data',
									totalProperty : 'count',
									messageProperty : 'msg'
								},
								url : rootFolder + 'equipments/failures'
							}
						});
				storeFailures.getProxy().extraParams.eid = this.eid;
				
				var storeUnscheduled = Ext.create('Ext.data.Store', {
					fields : [{
								name : 'code',
								type : 'string'
							}, {
								name : 'name',
								type : 'string'
							}],
					autoDestroy : false,
					autoLoad : true,
					autoSync : false,
					proxy : {
						type : 'rest',
						method : 'GET',
						reader : {
							type : 'json',
							root : 'data',
							totalProperty : 'count',
							messageProperty : 'msg'
						},
						url : rootFolder + 'equipments/unscheduled'
					}
				});
				
							
				var statusCombo = Ext.create('Ext.form.field.ComboBox', {
					name : 'status',
					anchor : '96%',
					store : ['idle', 'scheduled', 'processing', 'unscheduled','engineering', 'nonscheduled'] ,
					displayField : 'name',
					valueField : 'id',
					forceSelection : true,
					fieldLabel : 'Status',
					allowBlank : false,
					triggerAction: 'all',
					queryMode:'local'
				});
				
				var failureCombo = Ext.create('Ext.form.field.ComboBox', {
					name : 'failureCode',
					anchor : '96%',
					store : storeFailures ,
					width : 100,
					displayField : 'code',
					disabled: true,
					valueField : 'code',
					forceSelection : true,
					fieldLabel : 'Reason code',
					allowBlank : true,
					triggerAction: 'all',
					queryMode:'local'
				});
				
				var subStatus = Ext.create('Ext.form.field.ComboBox', {
					name : 'subStatus',
					xtype : 'combo',
					anchor : '96%',
					width : 200,
					store : storeUnscheduled,
					displayField : 'name',
					valueField : 'code',
					disabled: true,
					forceSelection : true,
					fieldLabel : 'Status code',
					allowBlank : true,
					triggerAction: 'all',
					queryMode:'local'
				});


				statusCombo.on('select', function() {
					failureCombo.clearValue();    
					failureCombo.getPicker().setLoading(false);
					failureCombo.store.getProxy().extraParams.statusCode = this.getValue();
					
					subStatus.clearValue();    
					subStatus.getPicker().setLoading(false);
					subStatus.store.getProxy().extraParams.status = this.getValue();
					subStatus.store.load({
		  				callback: function(records, operation, success){
							if (records[0] == undefined) {
								subStatus.setDisabled(true);
							} else {
								subStatus.setDisabled(false);
                                subStatus.allowBlank = false;
							}
		  				}
					});
					
					failureCombo.store.load({
		  				callback: function(records, operation, success){
							if (records[0] == undefined) {
								failureCombo.setDisabled(true);
  							} else {
								failureCombo.setDisabled(false);
                                failureCombo.allowBlank = false;
							}
		  				}
					});
				});
				
				failureCombo.on('select', function() {
					
					var comment = this.findRecord(this.valueField, this.getValue()).data.comment;
					Ext.getCmp('equipmentChangeStatusCommentId').setValue(comment);
				});
							
				Ext.apply(this, {
							items: [ 	statusCombo,
							         	subStatus,
										failureCombo,										 
									{
										name : 'date',
										fieldLabel : 'Date',
										xtype : "datefield",
										value: new Date(),
										dateFormat : 'Y-m-d',
										allowBlank : false
									}, {
										name : 'time',
										fieldLabel : 'Time',
										value: new Date(),
										xtype : "timefield",
										dateFormat : "H:i A",
										allowBlank : false
									},{
										xtype : 'textarea',
										id: 'equipmentChangeStatusCommentId',
										fieldLabel : 'Comment',
										name : 'note',
										anchor : '96%'
									}]
						});

				this.callParent(arguments);

			},

			buttons : [{
				text : 'Apply',
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
									url : rootFolder + 'equipments/status',
									submitEmptyText : false,
									waitMsg : 'Change equipment(s) status ...',
									params : {
										units : units
									},
									success : function(frm, action) {
										Ext.ux.Message.msg(
												"Status changed successfully.", "");

										gloApp.getController('EquipmentPanel')
												.refreshData();

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
