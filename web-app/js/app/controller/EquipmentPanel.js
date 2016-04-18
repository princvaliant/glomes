

Ext.define('glo.controller.EquipmentPanel', {
	extend : 'Ext.app.Controller',
	views : ['glo.view.equipment.List', 'glo.view.equipment.Content', 'glo.view.equipment.EquipmentStatusForm', 'glo.view.equipment.EquipmentStatusComment'],

	init : function() {
		
		this.control({
					'equipmentlist' : {
						afterrender : this.onPanelRendered,
						equipmentselect : this.onEquipmentSelect,
						equipmentadd : this.onEquipmentAdd,
						equipmentdelete : this.onEquipmentDelete,
						equipmentchangestatus : this.onEquipmentChangeStatus
					},
					'equipmentlist textfield[id=searchEquipmentId]' : {
						change : this.onSearch
					},
					'equipmentcontent' : {
						afterrender : this.onContentPanelRendered,
						addequipmentmaintenance: this.onAddEquipmentMaintenance,
						exportequipmenthistorystatuses: this.onExportEquipmentHistoryStatuses
					},
					'equipmentcontent  datefield[id=equipmentStatusStartId]' : {	
						change : this.onDateChange
					},
					'equipmentcontent  datefield[id=equipmentStatusEndId]' : {	
						change : this.onDateChange
					},
					'actioncolumn#deleteEquipmentStatusId' : {
						click : this.onEquipmentStatusDelete
					},
					'actioncolumn#editEquipmentStatusId' : {
						click : this.onEquipmentStatusEdit
					},
					'actioncolumn#deleteEquipmentMaintenanceId' : {
						click : this.onEquipmentMaintenanceDelete
					},
					'actioncolumn#editEquipmentMaintenanceId' : {
						click : this.onEquipmentMaintenanceEdit
					},
					'combobox[action=workCenterSelect]' : {
						select : this.onWorkCenterSelect
					}
				});

		this.equipmentSelect = Ext.Function.createThrottled(this.equipmentSelect,
				500, this);
	},

	onPanelRendered : function(p, e) {
		
		this.createGrid('equipment');
	},
	
	onContentPanelRendered : function(p, e) {
		
		Ext.getCmp('equipmentStatusStartId').setValue(new Date(new Date() - 63 * 24 * 60 * 60 * 1000));
		Ext.getCmp('equipmentStatusEndId').setValue(new Date());

		Ext.getCmp('equipmentHistoryStartId').setValue(new Date(new Date() - 63 * 24 * 60 * 60 * 1000));
		Ext.getCmp('equipmentHistoryEndId').setValue(new Date());
	},
	
	onExportEquipmentHistoryStatuses : function(o) {
		var sm = Ext.getCmp('equipmentGridId').getSelectionModel();
		var selected = sm.getSelection();
		var eid = selected[0].data.id

		var start = Ext.getCmp('equipmentHistoryStartId').getValue();
		var end = Ext.getCmp('equipmentHistoryEndId').getValue();

		window.open(rootFolder + 'equipments/export' + '?viewId=status' + '&eid=' + eid + '&start=' + start + '&end=' + end, 'resizable,scrollbars');
	},

	onAddEquipmentMaintenance : function(o) {

		var win = Ext.create('Ext.window.Window', {
					title : 'Add maintenance scheduled item',
					resizable : true,
					modal : true,
					y : 180,
					width : 400,
					layout : 'fit',
					items : {
						id : 'equipmentMaintenanceFormId',
						xtype : 'equipmentsmaintenanceform',
						parentWindow : o.ownerCt.ownerCt.ownerCt,
						eid :  Ext.getCmp("equipmentTabPanelId").eid
					}
				});

		win.show();
	},
	
	onSearch : function(thisField, newValue, oldValue, options) {
		
		var panel = thisField.ownerCt.ownerCt;
		var grid = panel.items.get(0);
		grid.store.currentPage = 1;
		grid.store.getProxy().extraParams.search = newValue;
		grid.store.load()
	},
	
	onWorkCenterSelect : function(o, selections) {
		
		var panel = o.ownerCt.ownerCt;
		var grid = panel.items.get(0);
		var sel = selections[0].data;
		
		grid.store.currentPage = 1;
		grid.store.getProxy().extraParams.workCenter = sel.id;
		grid.store.load()
	},
	
	onDateChange : function(thisField, newValue, oldValue, options) {
		
		var chart = Ext.getCmp('chartCmp');
		if (chart.store.getProxy().extraParams.ids != undefined) {
			chart.store.getProxy().extraParams.startDate = Ext.getCmp('equipmentStatusStartId').getValue();
			chart.store.getProxy().extraParams.endDate = Ext.getCmp('equipmentStatusEndId').getValue();
			chart.store.load();
		}
	},

	onEquipmentAdd : function(o) {
	
		var grid =  Ext.getCmp('equipmentGridId');
		var rowEditing = grid.plugins[0];

		rowEditing.cancelEdit();
		grid.store.insert(0, [{'STATUS':'IDLE'}]);
		rowEditing.startEdit(0, 0);
	},
	
	onEquipmentDelete : function(o) {
		
		 Ext.MessageBox.confirm('Confirm delete', 'Delete selected equipment(s)?', function(btn)
		 {
			if (btn == 'yes') {
				var grid =  Ext.getCmp('equipmentGridId');
				var rowEditing = grid.plugins[0];
				
				var sm = Ext.getCmp('equipmentGridId').getSelectionModel();
				rowEditing.cancelEdit();
				grid.store.remove(sm.getSelection());
				if (grid.store.getCount() > 0) {
					sm.select(0);
				}
			}
		});
	},
	
	onEquipmentChangeStatus : function(o) {
		
		var sm = Ext.getCmp('equipmentGridId').getSelectionModel();
		var selected = sm.getSelection();
		
		if(Ext.getCmp('equipmentStatusWindowId') != undefined) {
			Ext.getCmp('equipmentStatusWindowId').destroy();
		}

		var win = Ext.create('Ext.window.Window', {
					title : 'Change status for ' + selected.length + ' equipment(s)',
					id: 'equipmentStatusWindowId',
					bodyPadding : 4,
					y: 50,
					minWidth: 420,
					layout : 'auto',
					selected: selected,
					items : {
						id : 'equipmentStatusId',
						xtype : 'equipmentstatusform',
						rows : selected,
						eid : selected[0].data.id
					}
				});

		win.show();
	},
	
	onEquipmentStatusDelete : function(grid, cell, row, col, e) {
		
		var rec = grid.getStore().getAt(row);
		Ext.MessageBox.confirm('Confirm delete', 'Delete "' + rec.get('status') + '" status?', function(btn) {
			if (btn == 'yes') {
				Ext.Ajax.request({
					scope : this,
					method : 'POST',
					params : {
						id : rec.get('id')
					},
					url : rootFolder + 'equipments/deleteStatus',
					success : function(response) {
						var obj = Ext.decode(response.responseText);
						if (obj.success == false) {
							Ext.Msg.alert('Delete status failed', obj.msg);
						} else {
							grid.store.remove(rec);
						}
					}
				});
			}
		});
	},
	
	onEquipmentStatusEdit : function(grid, cell, row, col, e) {
		
		var rec = grid.getStore().getAt(row);
		
		if(Ext.getCmp('equipmentstatuscommentId') != undefined) {
			Ext.getCmp('equipmentstatuscommentId').destroy();
		}

		var win = Ext.create('Ext.window.Window', {
					title : 'Edit status comment',
					id: 'equipmentstatuscommentId',
					bodyPadding : 4,
					width: 410,
					layout : 'fit',
					items : {
						xtype : 'equipmentstatuscomment',
						statusId: rec.data.id,
						comment : rec.data.comment,
						gridStore : grid.getStore(),
						parentWindow : grid.ownerCt.ownerCt
					}
				});

		win.show();
	},
	
	onEquipmentMaintenanceDelete : function(grid, cell, row, col, e) {
		
		var rec = grid.getStore().getAt(row);
		
		Ext.MessageBox.confirm('Confirm delete', 'Delete "' + rec.data.schedule + '" maintenance item?', function(btn) {
			if (btn == 'yes') {
				Ext.Ajax.request({
					scope : this,
					method : 'POST',
					params : {
						id : rec.data.id
					},
					url : rootFolder + 'equipments/deleteMaintenance',
					success : function(response) {
						var obj = Ext.decode(response.responseText);
						if (obj.success == false) {
							Ext.Msg.alert('Delete status failed', obj.msg);
						} else {
							grid.store.remove(rec);
						}
					}
				});
			}
		});
	},
	
	onEquipmentMaintenanceEdit : function(grid, cell, row, col, e) {
		
		var rec = grid.getStore().getAt(row);

		if(Ext.getCmp('equipmentsMaintenanceId') != undefined) {
			Ext.getCmp('equipmentsMaintenanceId').destroy();
		}

		var win = Ext.create('Ext.window.Window', {
					title : 'Edit maintenance',
					id: 'equipmentsMaintenanceId',
					bodyPadding : 4,
					width: 410,
					layout : 'fit',
					items : {
						xtype : 'equipmentsmaintenanceform',
						id: 'equipmentsMaintenanceFormId',
						statusId: rec.data.id,
						comment : rec.data.comment,
						gridStore : grid.getStore(),
						parentWindow : grid.ownerCt.ownerCt
					}
				});

		Ext.getCmp('equipmentsMaintenanceFormId').loadRecord(rec);
		
		win.show();
	},
	
	
	onEquipmentSelect : function(o, selections) {
		
		
		this.equipmentSelect(o, selections);
	},
	
	equipmentSelect : function(o, selections) {
		
//		Ext.getCmp('removeEquipment').setDisabled(!selections.length);
		Ext.getCmp('changeEquipmentStatus').setDisabled(!selections.length);	
		var chart = Ext.getCmp('chartCmp');
		var aResult = Ext.Array.map(selections, function(node) {
			return {
				id: node.data.id
			};
		});
		chart.store.getProxy().extraParams.ids =  Ext.encode(aResult);
		chart.store.getProxy().extraParams.startDate = Ext.getCmp('equipmentStatusStartId').getValue();
		chart.store.getProxy().extraParams.endDate = Ext.getCmp('equipmentStatusEndId').getValue();
		
		var grid = Ext.getCmp('equipmentStatusesGridId');
		var gridM = Ext.getCmp('equipmentMaintenanceGridId');
		var eid = selections.length ? aResult[0] : -1;
		if (eid != -1) {
			
			var tab =  Ext.getCmp("equipmentTabPanelId");
			tab.eid = eid;
			var old = tab.getActiveTab();
			tab.setActiveTab(0);
			tab.setActiveTab(old);
			
			chart.store.load();
			grid.store.getProxy().extraParams.eid = eid ;
			grid.store.load();
			gridM.store.getProxy().extraParams.eid = eid ;
			gridM.store.load();
		}
	},
	
	refreshData : function() {
		var grid =  Ext.getCmp('equipmentGridId');
		grid.store.load();
	},
	
	createGrid : function(domain) {

		var panel = Ext.ComponentQuery.query('#equipmentpanel > equipmentlist')[0];

		Ext.Ajax.request({
			scope : panel,
			method : 'GET',
			params : {
				domain : 'equipment'
			},
			url : rootFolder + 'equipments/getFields',
			success : function(response) {

				var obj = Ext.decode(response.responseText);
				var columns = obj.columns;
				var store = new Ext.data.Store({
					id : 'dataStoreEquipment',
					fields : obj.fields,
					autoDestroy : false,
					autoLoad : false,
					autoSync : true,
					pageSize : '25',
					remoteSort : true,
					listeners : {
						update : function(store, record, operation, options) {
							
						}
					},
					proxy : {
						type : 'rest',
						url : rootFolder + 'equipments/getAll',
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
						writer : new Ext.data.JsonWriter({
									encode : false
								})
					}
				});
				
				var sm = new Ext.selection.CheckboxModel({
					listeners : {
						scope : panel,
						selectionchange : function(selectionModel,
								selected, options) {
							this.fireEvent('equipmentselect', this,
									selected);
						}
					}
				});
				
				var pagingBar = Ext.create('Ext.PagingToolbar', {
					store : store,
					displayInfo : true,
					displayMsg : 'Displaying equipments {0} - {1} of {2}',
					emptyMsg : "No equipments to display"
				});

				
				var grid = new Ext.grid.GridPanel({
					id : 'equipmentGridId',
					store : store,
					defaults : {
						sortable : true
					},
					border : false,
					autoScroll : true,
					multiSelect : true,
					stateful : true,
					padding : 0,
					stateId : 'stateEquipmentGrid2',
					selModel : sm,
					bbar : pagingBar,
					viewConfig : {
						emptyText : 'No equipments to display',
						forceFit : true,
						getRowClass : function(record, rowIndex, p,
								store) {
							if (record.data.ok == undefined
									|| !record.data.ok)
								return '';
							return '';
						}
					},
					plugins : [Ext.create('Ext.grid.plugin.RowEditing',
							{
								clicksToMoveEditor : 1,
								clicksToEdit : 3,
								autoCancel : true,
								errorSummary : false
							})],
					columns : columns
				});

				panel.removeAll(true);
				panel.add(grid);
				
				var combo = Ext.getCmp('workCenterFilterComboId');
				var wid = combo.value;
				if (wid !=  undefined) {
					grid.store.getProxy().extraParams.workCenter = wid;
				}
				grid.store.load();

			}
		});
		

	}

});