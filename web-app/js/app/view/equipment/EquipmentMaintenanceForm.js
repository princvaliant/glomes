

Ext.define('glo.view.equipment.EquipmentMaintenanceForm', {
			extend : 'Ext.form.Panel',
			alias : 'widget.equipmentsmaintenanceform',
			border : false,
			minWidth : 380,
			minHeight : 160,
			layout : 'anchor',
			eid: '',
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
										fieldLabel : 'ID',
										name : 'id',
										hidden : true
									},{
										fieldLabel : 'Schedule',
										name : 'schedule',
										anchor : '90%',
										allowBlank : false
									}, {
										xtype : 'textarea',
										fieldLabel : 'Note',
										name : 'comment',
										anchor : '90%',
										allowBlank : false
									}, {
										fieldLabel : 'Cycle',
										name : 'cycleType',
										anchor : '60%',
										xtype : 'combo',
										store : [['hours','hours'],['days','days'],['weeks','weeks'],['months','months'],['years','years'],['runs','runs'],['weights','weights'],['depositions','depositions']],
										forceSelection : true,
										editable : false,
										multiSelect : false,
										allowBlank : false
									}, {
										fieldLabel : 'Rate',
										name : 'cycleRate',
										anchor : '60%',
										xtype : 'numberfield',
										hideTrigger : true,
										allowDecimals : false,
										decimalPrecision : 0,
										allowBlank : false
									}, {
										fieldLabel : 'Date start',
										name : 'cycleStartDate',
										xtype : 'datefield',
										format : 'Y-m-d',
										anchor : '60%',
										allowBlank : false
									},  {
										fieldLabel : 'Time start',
										name : 'cycleStartTime',
										xtype : 'timefield',
										minValue: '1:00 AM',
									    maxValue: '11:00 PM',
										increment: 15,
										anchor : '60%',
										allowBlank : false
									},{
										fieldLabel : 'Department',
										name : 'department',
										anchor : '90%',
										allowBlank : true
									},{
										fieldLabel : 'Tag',
										name : 'tag',
										anchor : '60%',
										allowBlank : true
									},{
										xtype : 'checkbox',
										fieldLabel : 'Active',
										checked: 1,
										name : 'active'
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
									url : rootFolder + 'equipments/maintenance',
									submitEmptyText : false,
									waitMsg : 'Saving data ...',
									params : {
										eid : form.eid
									},
									success : function(frm, action) {
										Ext.ux.Message.msg(
												"Data saved successfully.", "");

										var gridM = Ext.getCmp('equipmentMaintenanceGridId');
										gridM.store.load();

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
