

Ext.define('glo.view.main.StartForm', {
	extend : 'Ext.form.Panel',
	id : 'startFormId',
	alias : 'widget.mainstartform',
	border : false,
	autoScroll : true,
	ownerId : '',
	store: '',
	columns: '',
	pctg: '',
	pkey: '',
	processKey : '',
	fieldDefaults : {
		labelWidth : 125
	},
	defaultType : 'textfield',
	layout : 'auto',

	initComponent : function() {

		var rowEditing = Ext.create('Ext.grid.plugin.RowEditing', {
					clicksToMoveEditor : 1,
					autoCancel : false,
					errorSummary : false
				});
		
		var cols = new Array();
		cols[0] = {
			header : 'Serial#',
			dataIndex : 'code',
			editor : {
				allowBlank : true
			}
		};
		Ext.Object.each(this.columns, function(record) {
			
			if (this.columns[record].field) {
				cols.push(this.columns[record]);
			}
		}, this);
		
		
		Ext.apply(this, {
			scope: this,
			items : [{
				xtype : 'fieldset',
				flex:2,
				checkboxToggle : false,
				title : 'Unit data',
				defaultType : 'textfield',
				collapsed : false,
				layout : 'hbox',
				defaults : {
					padding : 7
				},
				items : [{
					xtype : 'container',
					layout : 'anchor',
					flex:2,
					defaultType : 'textfield',
					items : [
							{
								xtype : 'combo',
								id : 'comboProductId',
								fieldLabel : 'Product',
								anchor : '100%',
								name : 'pid',
								minChars : 1,
								typeAhead : true,
								store : Ext.create('glo.store.Products'),
								listConfig: {maxHeight: 450},
								displayField : 'name',
								valueField : 'id',
								forceSelection : true,
								multiSelect : false,
								allowBlank : false
							}, {
								xtype : 'combo',
								id : 'comboCompanyId',
								fieldLabel : 'Supplier',
								anchor : '100%',
								name : 'cid',
								minChars : 1,
								typeAhead : true,
								store : Ext.create('glo.store.Companies'),
								displayField : 'name',
								valueField : 'id',
								forceSelection : true,
								multiSelect : false,
								allowBlank : false
							}, {
								id : 'prodCode',
								fieldLabel : 'Supplier product #',
								name : 'prodCode',
								anchor : '60%'
							}, {
								id : 'price',
								fieldLabel : 'Price',
								name : 'price',
								anchor : '60%'
							}, {
								id : 'deliveryTime',
								fieldLabel : 'Delivery time',
								name : 'deliveryTime',
								anchor : '60%'
							},{
								id : 'uom',
								fieldLabel : 'Unit of measure',
								name : 'uom',
								anchor : '60%'
							}, {
								id : 'lot',
								fieldLabel : 'Lot #',
								name : 'lot',
								anchor : '100%'
							}]
				}, {
					xtype : 'container',
					flex:2,
					layout : 'anchor',
					items : [{
								xtype : 'textarea',
								fieldLabel : 'Notes',
								name : 'note',
								rows: 7,
								anchor : '96%'
							},{
								xtype : 'combo',
								id : 'comboUserId',
								fieldLabel : 'User',
								anchor : '100%',
								name : 'uid',
								minChars : 1,
								typeAhead : true,
								store : Ext.create('glo.store.Users'),
								displayField : 'username',
								valueField : 'id',
								forceSelection : true,
								multiSelect : false,
								allowBlank : false
							}]
				}]
			}, {
				xtype : 'grid',
				id : 'startMainGridId',
				title : 'Individual Serial Num',
				store: this.store,
				pctg: this.pctg,
				pkey: this.pkey,
				flex:3,
				height:330,
				autoScroll : true,
				plugins : [rowEditing],
				columns : cols,
				tbar : [{
							text : 'Add Unit',
							iconCls : 'icon-shape_square_add',
							scope: this,
							handler : function() {
								rowEditing.cancelEdit();
								this.store.insert(0, [{'qty':1}]);
								rowEditing.startEdit(0, 0);
							}
						}, {
							id : 'removeUnit',
							text : 'Remove Unit',
							iconCls : 'icon-shape_square_delete',
							scope: this,
							handler : function() {
								var sm = Ext.getCmp('startMainGridId')
										.getSelectionModel();
								rowEditing.cancelEdit();
								this.store.remove(sm.getSelection());
								if (this.store.getCount() > 0) {
									sm.select(0);
								}
							},
							disabled : true
						}, '-', {
							text : 'Auto-generate serials',
							iconCls : 'icon-disk_multiple',
							scope: this,
							handler : function() {
								rowEditing.cancelEdit();
								gloApp.getController('MainStartForm')
										.showAutoGenerateWindow(this.store, this.pctg, this.pkey);
							}
						}],
				listeners : {
					'selectionchange' : function(view, records) {
						Ext.getCmp('removeUnit').setDisabled(!records.length);
					}
				}
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

				var units = new Array();
				Ext.getCmp('startMainGridId').store.each(
						function(record) {
							Ext.Array.include(units, record.data);
						});

				form.submit({
							method : 'POST',
							url : rootFolder + "units",
							submitEmptyText : false,
							waitMsg : 'Saving data...',
							params : {
								units : Ext.JSON.encode(units),
								pctg : form.pctg,
								pkey : form.pkey
							},

							success : function(frm, action) {
								Ext.ux.Message.msg(
										"Unit received successfully.", "");
								gloApp.getController('MainPanel').loadList(Ext
										.getCmp(form.ownerId).layout
										.getActiveItem(), '', '', 'stay');

								win.destroy();
							},
							failure : function(frm, action) {

								Ext.Msg
										.alert('Failed Start',
												action.result.msg);
							}
						});
			}
		}
	}]
});
