

Ext.define('glo.controller.MainStartForm', {
	extend : 'Ext.app.Controller',

	views : ['glo.view.main.StartForm',
			'glo.view.main.AutoGenerateUnitsForm'],

	init : function() {

		this.control({
					'mainstartform' : {
						afterrender : this.onStartFormRendered
					}
				});
	},

	// Main function for initializing starting form
	onStartFormRendered : function(p, e) {

		var combo1 = Ext.getCmp('comboProductId');
		var combo2 = Ext.getCmp('comboCompanyId');
		var combo3 = Ext.getCmp('comboUserId');
		var prodCode = Ext.getCmp('prodCode');

		combo1.store.getProxy().extraParams.pctg = p.pctg
		combo1.store.getProxy().extraParams.pkey = p.pkey
		combo1.store.load({
			scope : this,
			callback : function(records, operation, options) {
				if (records.length == 1) {
					combo1.setValue(records[0]);
				}
			}
		});

		combo1.on('select', this.onProductCombo);
		combo1.on('change', this.onProductCombo);

		combo2.on('select', this.onCompanyCombo);
		combo2.on('change', this.onCompanyCombo);

		prodCode.on('blur', function(field, options) {

					Ext.Ajax.request({
								scope : this,
								method : 'GET',
								params : {
									vendorCode : field.value
								},
								url : rootFolder
										+ 'productCompanies/vendorCode',
								success : function(response) {
									var obj = Ext.decode(response.responseText);
									if (obj && obj.count > 0) {
										combo1.setValue(obj.data.productId);
									}
								}
							});
				});

		combo3.store.getProxy().extraParams.processKey = this.processKey;
		combo3.store.load({
					scope : this,
					callback : function(records, operation, options) {
						combo3.setValue(records[0].data.current);
					}
				});
				
		prodCode.focus();
	},

	onProductCombo : function() {

		var combo1 = Ext.getCmp('comboProductId');
		var combo2 = Ext.getCmp('comboCompanyId');

		combo2.reset();
		combo2.store.getProxy().extraParams.product = combo1.getValue();
		combo2.store.load({
					scope : this,
					callback : function(records, operation, options) {
						if (records && records.length == 1) {
							combo2.setValue(records[0]);
						}
					}
				});
	},

	onCompanyCombo : function() {
		var store2 = Ext.create('glo.store.ProductCompanies');
		store2.getProxy().extraParams.product = Ext.getCmp('comboProductId')
				.getValue();
		store2.getProxy().extraParams.company = Ext.getCmp('comboCompanyId')
				.getValue();
		store2.load({
					scope : this,
					callback : function() {

						Ext.getCmp('startFormId').getForm().setValues({
									prodCode : store2.getAt(0).data.vendorCode,
									price : store2.getAt(0).data.price,
									deliveryTime : store2.getAt(0).data.deliveryTime,
									uom : store2.getAt(0).data.uom
						});
					}
				});
	},

	showStartWindow : function(panel, title, pctg, pkey) {
		
		// Add columns to grid
		Ext.Ajax.request({
			scope : this,
			method : 'GET',
			params : {
				pkey : pkey,
				pctg : pctg
			},
			url : rootFolder + 'contents/first/dc',
			success : function(response) {

				var obj = Ext.decode(response.responseText);
				
				var store = Ext.create('Ext.data.Store', {
					fields:  [{name: 'code', type: 'string'}].concat( obj.fields),
					autoDestroy : true,
					autoLoad : false,
					autoSync : false
				});
				
				obj.columns.splice(0,3);
				
				var win = Ext.create('Ext.window.Window', {
					title : 'Start "' + title + '"',
					width : 1180,
					height : 648,
					draggable : false,
					x : 40,
					y : 20,
					resizable : true,
					modal : true,
					layout: 'fit',
					items : {
						xtype : 'mainstartform',
						ownerId : panel.id,
						store: store,
						columns: obj.columns,
						pctg : pctg,
						pkey : pkey
					}
				});

				win.show();
			}
		});

	},

	showAutoGenerateWindow : function(store, pctg, pkey) {

		var win = Ext.create('Ext.window.Window', {
					title : 'Auto generate serials',
					layout: 'auto',
					width : 400,
					x : 210,
					y : 40,
					resizable : true,
					modal : true,
					items : {
						xtype : 'mainautogenerateunitsform',
						id : 'AutoGenerateUnitsFormId',
						ownerStore : store
					}
				});
		
		Ext.Ajax.request({
			scope : this,
			method : 'GET',
			params : {
				pkey : pkey,
				pctg : pctg
			},
			url : rootFolder + 'contents/first/form/dc',
			success : function(response) {
				var obj = Ext.decode(response.responseText);
				Ext.getCmp('autoGenerateItems').add(obj.controls);
				win.doLayout();
			}
		});

		win.show();
	}
});