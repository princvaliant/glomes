

Ext.define('glo.view.tb.MergeForm', {
			extend : 'Ext.form.Panel',
			alias : 'widget.mergeform',
			border : false,
			rows : '',
			parentWindow : '',
			processKeyEng : '',
			taskKeyEng : '',
			minWidth : 600,
			minHeight : 201,
			maxWidth : 700,
			maxHeight : 650,
			layout : 'anchor',
			defaultType : 'textfield',
			standardSubmit : false,
			bodyPadding : '3 9 3 9',

			initComponent : function() {
				
				var productCombo = Ext.create('Ext.form.field.ComboBox', {
					fieldLabel : 'Assembly',
					name : 'productId',
					anchor : '100%',
					minChars : 1,
					typeAhead : true,
					store : Ext.create('glo.store.Products'),
					displayField : 'name',
					valueField : 'id',
					forceSelection : true,
					multiSelect : false,
					allowBlank : false
				});
				
				var procCombo = Ext.create('Ext.form.field.ComboBox', {
					id : 'mergeProcessComboId',
					name : 'processId',
					fieldLabel : 'Process',
					anchor : '100%',
					minChars : 1,
					typeAhead : true,
					store : Ext.create('glo.store.Processes'),
					displayField : 'name',
					valueField : 'id',
					forceSelection : true,
					multiSelect : false,
					allowBlank : false,
					readOnly : true
				});
				
				var procText = Ext.create('Ext.form.field.TextArea', {
					id : 'mergeProcessTextId',
					name : 'startProcess',
					fieldLabel : 'Process',
					value : 'test',
					readOnly : true,
					anchor : '100%'
				});
				
				productCombo.store.getProxy().extraParams.isBom = true
				productCombo.store.load({
					scope : this,
					callback : function(records, operation, options) {
						if (records.length == 1) {
							productCombo.setValue(records[0]);
						}
					}
				});
				
				productCombo.on('select', this.onProductCombo);
				productCombo.on('change', this.onProductCombo);
		
				
				Ext.apply(this, {
					scope: this,
					items : [{
							xtype : 'container',
							layout : 'anchor',
							defaultType : 'textfield',
							items : [
									productCombo,
									//procCombo,
									//procText,
									{
										id : 'mergeCode',
										fieldLabel : 'Serial # (specify or leave blank)',
										name : 'code',
										anchor : '60%'
									}
								]
					}]
				});

				this.callParent(arguments);
			},
			
			onProductCombo : function() {
				//var procText = Ext.getCmp('mergeProcessTextId');
				//var recordIndex = this.getStore().find('id', this.getValue());
				//var record = this.getStore().getAt(recordIndex);
				//var record = this.getStore().findRecord('id', this.getValue());
				//procText.setValue(record.get('startProcess'));
			},
			/***
			onProductCombo : function() {

				var procCombo = Ext.getCmp('mergeProcessComboId');
		
				procCombo.reset();
				procCombo.store.getProxy().extraParams.product = this.getValue();
				procCombo.store.load({
					scope : this,
					callback : function(records, operation, options) {
						if (records.length == 1) {
							procCombo.setValue(records[0]);
						}
					}
				});
			},
			***/

			buttons : [{
				text : 'Create merged unit',
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
									url : rootFolder + 'units/merge',
									submitEmptyText : false,
									waitMsg : 'Merging ...',
									params : {
										units : units
									},
									success : function(frm, action) {
										Ext.ux.Message.msg(
												"Merge successful.",
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
