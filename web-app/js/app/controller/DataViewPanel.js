

Ext.define('glo.controller.DataViewPanel', {
	extend : 'Ext.app.Controller',
	views : ['glo.view.dataView.List', 'glo.view.dataView.Content', 'glo.view.dataView.ReportInputForm',
			'glo.view.dataView.Variables', 'glo.view.dataView.UploadExcelForm'],

	init : function() {

		this.control({
					'dataviewlist' : {
						afterrender : this.onPanelRendered,
						dataviewselect : this.onDataViewSelect,
						dataviewadd : this.onDataViewAdd,
						dataviewedit : this.onDataViewEdit,
						dataviewdelete : this.onDataViewDelete,
						dataviewduplicate : this.onDataViewDuplicate
					},
					'dataviewcontent' : {
						exportdataview : this.onExport,
						uploadexceltemplate : this.onImport,
						addformula : this.onAddFormula
					},
					'dataviewlist textfield[id=searchDataViewId]' : {
						change : this.onSearch
					},
					'combobox[action=dataViewProcessSelect]' : {
						select : this.onProcessListSelect
					}
				});

		this.dataViewSelect = Ext.Function.createThrottled(this.dataViewSelect,
				500, this);
	},

	onPanelRendered : function(p, e) {

		this.createGrid('dataView');

	},

	onSearch : function(thisField, newValue, oldValue, options) {

		var panel = thisField.ownerCt.ownerCt;
		var grid = panel.items.get(0);
		grid.store.currentPage = 1;
		grid.store.getProxy().extraParams.search = newValue;
		grid.store.load()
	},

	onDataViewAdd : function(o) {

		var grid = Ext.getCmp('dataViewGridId');
		var rows = grid.getSelectionModel().getSelection();

		var win = Ext.create('Ext.window.Window', {
					title : 'Add data view',
					resizable : true,
					modal : true,
					y : 180,
					width : 400,
					layout : 'fit',
					items : {
						id : 'dataViewFormId',
						xtype : 'dataviewform',
						parentWindow : o.ownerCt.ownerCt.ownerCt,
						rid : 0
					}
				});

		win.show();
	},

	onDataViewEdit : function(o) {

		var grid = Ext.getCmp('dataViewGridId');
		var rows = grid.getSelectionModel().getSelection();

		var win = Ext.create('Ext.window.Window', {
					title : 'Edit data view',
					resizable : true,
					modal : true,
					y : 230,
					width : 600,
					layout : 'fit',
					items : {
						id : 'dataViewFormId',
						xtype : 'dataviewform',
						parentWindow : o.ownerCt.ownerCt.ownerCt,
						rid : rows[0].data.id
					}
				});

		Ext.getCmp('dataViewFormId').loadRecord(rows[0]);

		win.show();
	},

	onDataViewDelete : function(o) {

		Ext.MessageBox.confirm('Confirm delete',
				'Delete selected dataView(s)?', function(btn) {
					if (btn == 'yes') {
						var grid = Ext.getCmp('dataViewGridId');
						var sm = Ext.getCmp('dataViewGridId')
								.getSelectionModel();
						grid.store.remove(sm.getSelection());
						if (grid.store.getCount() > 0) {
							sm.select(0);
						} else {

						}
					}
				});
	},

	onDataViewDuplicate : function(o) {

		var grid = Ext.getCmp('dataViewGridId');
		var rows = grid.getSelectionModel().getSelection();

		// Determine if input requires additional input attributes
		Ext.Ajax.request({
					scope : this,
					method : 'POST',
					params : {
						id : rows[0].data.id
					},
					url : rootFolder + 'dataViews/duplicate',
					success : function(response) {
						var obj = Ext.decode(response.responseText);
						this.refreshData();
					},
					failure : function(response) {
						Ext.Msg.alert(response.responseText);
					}
				});

	},

	onDataViewSelect : function(o, selections) {
		this.dataViewSelect(o, selections);
	},

	dataViewSelect : function(o, selections) {

		Ext.getCmp('removeDataView').setDisabled(!selections.length);
		Ext.getCmp('editDataView').setDisabled(!selections.length);
		Ext.getCmp('duplicateDataView').setDisabled(!selections.length);
		var grid = Ext.ComponentQuery.query('dataviewcontent grid[name=gridVariablesPerDataView]')[0];
		var panel = Ext.ComponentQuery.query('dataviewcontent')[0];
		var panelJoin = Ext.ComponentQuery.query('dataviewjoin')[0];
		this.urlExportData = '';
		if (selections[0] != undefined) {
			var sel = selections[0].data;
			if (sel.urlExportData != '') {
				this.urlExportData = sel.urlExportData;
				grid.getView().emptyText = ['<div style="margin:15px;"><p><b>Data will be exported from custom URL:</b></p><br/><p>' + sel.urlExportData + '</p></div>'];
				grid.store.getProxy().extraParams.dataView = -2;
                grid.store.getProxy().extraParams.dataView2 = sel.id;
				grid.store.load();
				grid.disable();
				Ext.getCmp('dataViewContentToolbar').enable();
			} else {
				grid.store.getProxy().extraParams.dataView = sel.id;
				grid.store.load();
				panel.setTitle('Fields for \'' + sel.name + '\'');
				grid.enable();
				grid.getView().emptyText = [ '<div style="margin:15px;"><p><b>No report variables assigned. To assign drag-drop variables from the right pane.</b></p></div>'];
				Ext.getCmp('dataViewContentToolbar').enable();
				
				panelJoin.setTitle('Reports joined to \'' + sel.name + '\'');
				panelJoin.dataView = sel.id;
				panelJoin.storePrimary.getProxy().extraParams.dataView = sel.id;
				panelJoin.storePrimary.load();
				panelJoin.storeJoins.getProxy().extraParams.dataView = sel.id;
				panelJoin.storeJoins.load();
				panelJoin.storeSecDv.load();
				Ext.getCmp('dataViewJoinToolbar').enable();

			}
		}
	},


	onExport : function(o) {

		var gridReports = Ext.getCmp('dataViewGridId');
		var rows = gridReports.getSelectionModel().getSelection();

		var grid = Ext.ComponentQuery
				.query('dataviewcontent grid[name=gridVariablesPerDataView]')[0];
		var dataViewId = grid.store.getProxy().extraParams.dataView
        if (dataViewId == -2)
            dataViewId = grid.store.getProxy().extraParams.dataView2;
		var rName = rows[0].data.name;

		if (this.urlExportData == '') {
			window.open(rootFolder + 'dataViews/export?dataViewId=' + dataViewId
					 // + '&start=' + Ext.getCmp('dataViewStartId').value +
					 // '&end=' + Ext.getCmp('dataViewEndId').value
					+ '&active=',
					'resizable,scrollbars');
		}
		else {
			var win = Ext.create('Ext.window.Window', {
				title : 'Specify input parameters for ' + rName,
				resizable : true,
				modal : true,
				y : 50,
				width : 500,
				layout : 'fit',
				items : {
					id : 'reportInputFormId',
					xtype : 'reportinputform',
					parentWindow : o.ownerCt.ownerCt.ownerCt,
					dataViewId : dataViewId,
					rName : rName,
					urlExportData : this.urlExportData
				}
			});

			win.show();
		}
	},

	
	onImport : function(o) {

		var grid = Ext.ComponentQuery
				.query('dataviewcontent grid[name=gridVariablesPerDataView]')[0];

        var dataViewId = grid.store.getProxy().extraParams.dataView
        if (dataViewId == -2)
            dataViewId = grid.store.getProxy().extraParams.dataView2;

		var win = Ext.create('Ext.window.Window', {
					title : 'Upload excel template for current data view',
					resizable : true,
					modal : true,
					width : 408,
					x : 340,
					y : 110,
					layout : 'anchor',
					items : {
						id : 'uploadExcelFormId',
						xtype : 'uploadexcelform',
						dataViewId : dataViewId
					}
				});

		win.show();
	},

	onAddFormula : function(o) {

		var grid = Ext.ComponentQuery
				.query('dataviewcontent grid[name=gridVariablesPerDataView]')[0];

		Ext.Ajax.request({
					scope : this,
					method : 'POST',
					params : {
						dataViewId : grid.store.getProxy().extraParams.dataView
					},
					url : rootFolder + 'dataViews/addFormulaVariable',
					success : function(response) {
						var obj = Ext.decode(response.responseText);

						if (obj.success == false) {
							Ext.Msg.alert('Formula field added', obj.msg);
						} else {
							grid.getStore().load();
						}
					}
				});
	},

	onProcessListSelect : function(o, selections) {

		var tree = o.ownerCt.ownerCt;
		var sel = selections[0].data;
		tree.store.getProxy().extraParams.process = sel.categoryId;
		tree.store.load({
					scope : this,
					callback : function(records, operation, success) {

					}
				});
	},

	refreshData : function() {

		var grid = Ext.getCmp('dataViewGridId');
        var sel = grid.getSelectionModel().getSelection();
        grid.getSelectionModel().clearSelections();
		grid.store.load({
            callback : function(store, records, options) {
                if (sel) {
                    grid.getSelectionModel().select(sel[0].index);
                }
            }
        });
	},

	createGrid : function(domain) {

		var panel = Ext.ComponentQuery.query('#dataViewPanel > dataviewlist')[0];

		Ext.Ajax.request({
			scope : panel,
			method : 'GET',
			params : {
				domain : 'dataView'
			},
			url : rootFolder + 'dataViews/getFields',
			success : function(response) {

				var obj = Ext.decode(response.responseText);
				var columns = obj.columns;

				var publicColumn = {
					xtype : 'actioncolumn',
					dataIndex : 'isPublic',
					text : 'Pbl',
					width : 28,
					items : [{
								getClass : function(v, meta, rec) {
									if (rec.get('isPublic') == true) {
										return 'icon-bullet_tick';
									} else {
										return '';
									}
								}
							}]
				};

				var publishToDashboardColumn = {
					xtype : 'actioncolumn',
					dataIndex : 'publishToDashboard',
					text : 'Dsh',
					width : 28,
					items : [{
								getClass : function(v, meta, rec) {
									if (rec.get('publishToDashboard') == true) {
										return 'icon-bullet_tick';
									} else {
										return '';
									}
								}
							}]
				};

				obj.columns[2] = publicColumn
				obj.columns[3] = publishToDashboardColumn

				var store = new Ext.data.Store({
							id : 'dataStoreDataView',
							fields : obj.fields,
							autoDestroy : false,
							autoLoad : false,
							autoSync : true,
							pageSize : '20',
							remoteSort : true,
							listeners : {
								update : function(store, record, operation,
										options) {

								}
							},
							proxy : {
								type : 'rest',
								url : rootFolder + 'dataViews',
								actionMethods : {
									read : 'GET',
									create : 'POST',
									update : 'PUT',
									destroy : 'DELETE'
								},
								startParam : 'offset',
								pageParam : 'page',
								dirParam : 'order',
								limitParam : 'max',
								simpleSortMode : true,

								reader : {
									type : 'json',
									root : 'data',
									totalProperty : 'count',
									messageProperty : 'msg'
								},
								listeners : {
									exception : function(proxy, response,
											operation) {
										if (operation) {
											store.rejectChanges();
											Ext.Msg.alert('Failed',
													operation.error);
										}
									}
								},
								writer : new Ext.data.JsonWriter({
											encode : false
										})
							}
						});

				var pagingBar = Ext.create('Ext.PagingToolbar', {
							store : store,
							displayInfo : true,
							displayMsg : 'Displaying data views {0} - {1} of {2}',
							emptyMsg : "No data views to display"
						});

				var sm = new Ext.selection.RowModel({
							listeners : {
								scope : panel,
								selectionchange : function(selectionModel,
										selected, options) {
									this.fireEvent('dataviewselect', this,
											selected);
								}
							}
						});

				var grid = new Ext.grid.GridPanel({
							id : 'dataViewGridId',
							store : store,
							defaults : {
								sortable : true
							},
							border : false,
							autoScroll : true,
							multiSelect : false,
							stateful : true,
							padding : 0,
							stateId : 'stateDataViewGrid',
							bbar : pagingBar,
							selModel : sm,
							viewConfig : {
								emptyText : 'No data views to display',
								forceFit : true
							},
							columns : columns
						});

				grid.store.on('load', function(store, records, options) {
							// var sm = grid.getSelectionModel();
							// if (grid.store.getCount() > 0) {
							// sm.select(0);
							// }
						}, grid);

				panel.removeAll(true);
				panel.add(grid);

				grid.store.getProxy().extraParams.search = Ext
						.getCmp('searchDataViewId').getValue();
				grid.store.load()

			}
		});

	}

});