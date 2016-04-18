

Ext.define('glo.view.equipment.Content', {
	extend : 'Ext.Panel',
	alias : 'widget.equipmentcontent',

	requires : ['Ext.ux.PreviewPlugin', 'Ext.toolbar.Toolbar',
			'Ext.form.field.ComboBox'],

	border : false,
	layout : 'fit',
	margins : '0 0 0 0',
	autoScroll : false,
	bodyPadding : 0,

	initComponent : function() {
		
		this.addEvents('addequipmentmaintenance');
		this.addEvents('exportequipmenthistorystatuses')

		var fields = ['processing', 'engineering', 'idle', 'scheduled', 'unscheduled', 'nonscheduled'];
		var fields2 = ['titles', 'period', 'goal', 'processing', 'idle', 'scheduled', 'unscheduled', 'engineering', 'nonscheduled'];

		var browserStore = Ext.create('Ext.data.JsonStore', {
					fields : fields2,
					autoLoad : false,
					pageSize : '10',
					proxy : {
						type : 'rest',
						url : rootFolder + 'equipments/chart',
						actionMethods : {
							read : 'GET'
						},
						reader : {
							type : 'json',
							root : 'data',
							totalProperty : 'count',
							messageProperty : 'msg'
						}
					}
				});

		var colors = ['rgb(15, 166, 7)', 'rgb(95, 60, 12)', 'rgb(230, 226, 0)',
				'rgb(27, 32, 191)', 'rgb(209, 4, 76)', 'rgb(189, 189, 189)'];

		Ext.chart.theme.Browser = Ext.extend(Ext.chart.theme.Base, {
					constructor : function(config) {
						Ext.chart.theme.Base.prototype.constructor.call(this,
								Ext.apply({ colors : colors	}, config));
					}
				});

		var store = new Ext.data.Store({
					fields : ['id', 'name', 'status', 'subStatus', 'failureCode', 'userName', 'dateStart', 'duration', 'comment'],
					autoDestroy : false,
					autoLoad : true,
					autoSync : true,
					pageSize : '20',
					remoteSort : true,
					proxy : {
						type : 'rest',
						url : rootFolder + 'equipments/getStatuses',
						actionMethods : {
							read : 'GET'
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
						}
					}
				});

		var pagingBar = Ext.create('Ext.PagingToolbar', {
					store : store,
					displayInfo : true,
					displayMsg : 'Displaying {0} - {1} of {2}',
					emptyMsg : "No history to display"
				});
				
		var storeMaintenance = new Ext.data.Store({
			fields : [{name: 'id'}, 
					  {name: 'schedule'}, 
					  {name: 'comment'}, 
					  {name:  'cycleType'}, 
					  {name:  'cycleRate'}, 
					  {name:  'cycleStartDate', type:'date'}, 
					  {name:  'active'},
					  {name:  'department'},
					  {name:  'tag'}],
			autoDestroy : false,
			autoLoad : true,
			autoSync : true,
			pageSize : '20',
			remoteSort : true,
			proxy : {
				type : 'rest',
				url : rootFolder + 'equipments/maintenance',
				actionMethods : {
					read : 'GET'
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
				}
			}
		});
		
		var pagingBarMaintenance = Ext.create('Ext.PagingToolbar', {
				store : storeMaintenance,
				displayInfo : true,
				displayMsg : 'Displaying {0} - {1} of {2}',
				emptyMsg : "No maintenance to display"
			});

		Ext.apply(this, {
					items : {
						xtype : 'tabpanel',
						id: 'equipmentTabPanelId',
						activeTab : 0,
						plain : true,
						layout : 'fit',
						defaults : {
							bodyPadding : 5
						},
						items : [{
							title : 'Chart graph',
							align : 'stretch',
							layout : {
								type : 'vbox',
								align : 'stretch',
								pack : 'start'
							},
							items : [{
										xtype : 'fieldcontainer',
										fieldLabel : 'Date Range',
										combineErrors : true,
										msgTarget : 'side',
										layout : 'hbox',
										height : 25,
										defaults : {
											hideLabel : true
										},
										items : [{
													xtype : 'datefield',
													id : 'equipmentStatusStartId',
													name : 'startDate',
													margin : '0 5 0 0',
													allowBlank : false
												}, {
													xtype : 'datefield',
													id : 'equipmentStatusEndId',
													name : 'endDate',
													allowBlank : false
												}
//												, {
//													xtype : 'button',
//													text : 'Export to jpg',
//													handler : function() {
//														
//														debugger;
//														
//														var chart =  Ext.getCmp("chartCmp");
//														var DocumentContainer = chart.getEl().dom;
//														var WindowObject = window
//																.open(
//																		'',
//																		'ExportWindow',
//																		'width=800,height=700,top=50,left=50,toolbars=no,scrollbars=yes,status=no,resizable=yes');
//														WindowObject.document.writeln("<canvas id='thecanvas'>");
//														WindowObject.document.writeln("</canvas>");
//														
//														WindowObject.document.writeln(DocumentContainer.innerHTML);
//													}
//												}
											]
									}, {
										xtype : 'chart',
										id : 'chartCmp',
										style : 'background:#fff',
										animate : true,
										theme : 'Browser:gradients',
										store : browserStore,
										flex : 1,
										legend : {
											position : 'bottom'
										},
										axes : [{
													type : 'Numeric',
													position : 'left',
													fields : fields,
													title : false,
													grid : true,
													label : {
														renderer : function(v) {
															return v;
														}
													},
													roundToDecimal : true
												}, {
													type : 'Category',
													position : 'bottom',
													fields : ['period'],
													title : 'Period',
													label : {
														renderer : function(m) {
															return m - 1;
														}
													}
												}],
										series : [{
											type : 'bar',
											axis : 'bottom',
											column : true,
											gutter : 10,
											highlight : false,
											stacked : true,
											tips : {
												width: 360,
												height: 500,
												anchor: 'left',
												anchorToTarget: true,
												anchorOffset : 300,
														
												renderer : function(storeItem,item) {
													var g =  storeItem.data['titles'];
													if (g != '') {
														
														var comments = '<table>';
														var esColor = "green"
														if (item.yField == "idle") esColor = "yellow"
														if (item.yField == "scheduled") esColor = "blue"
														if (item.yField == "unscheduled") esColor = "red"
														if (item.yField == "engineering") esColor = "brown"
														if (item.yField == "nonscheduled") esColor = "gray"
														
														Ext.Object.each(g, function(key, value, myself) {
															if (key == item.yField) {
																Ext.Object.each(value, function(record) {
																	comments += "<tr><td width=80 style='vertical-align:top;'><font style='font-size:11;color:" + esColor + ";'>" + key + ":</font></td><td width=270><font style='font-size:11'>" + value[record] + "</font></td></tr>";
																});
															}
														});
														
														this.update('<b>' + Math.round(item.value[1]*100)/100 + '</b><br/>' + comments + '</table>');
													} 
												}
											},
											xField : ['period'],
											yField : fields
										}, {
											type : 'line',
											axis : 'left',
											smooth: true,
											xField: 'period',
               								yField: 'goal',
											highlight : false
										}]
									}]
						}, {
							title : 'Status history',
							layout : 'fit',
							dockedItems: [{
								xtype: 'toolbar',
								items: [{
										xtype : 'datefield',
										id : 'equipmentHistoryStartId',
										fieldLabel : 'Export Date Range',
										//dateFormat : 'Y-m-d',
										dateFormat : 'MM/dd/yyyy',
										name : 'startDate',
										margin : '0 5 0 0',
										allowBlank : false
									}, {
										xtype : 'datefield',
										id : 'equipmentHistoryEndId',
										//dateFormat : 'Y-m-d',
										dateFormat : 'MM/dd/yyyy',
										name : 'endDate',
										allowBlank : false
									}, {
										xtype : 'button',
										text : 'Export to excel',
										id : 'exportStatusHistoryId',
										iconCls : 'icon-excel',
										scope: this,
										handler : function() {
											this.fireEvent('exportequipmenthistorystatuses', this);
										}
									} ]
							}],
							items : [ {
								xtype : 'grid',
								id: 'equipmentStatusesGridId',
								layout : 'fit',
								store : store,
								defaults : {
									sortable : true
								},
								border : false,
								autoScroll : true,
								multiSelect : false,
								stateful : true,
								padding : 0,
								stateId : 'stateEquipStatusesGrid',
								bbar : pagingBar,
								viewConfig : {
									emptyText : 'No history to display'
								},
								columns : [
									{
										dataIndex: 'status',
										text: 'Status',
										flex: 1
									},
									{
										dataIndex: 'subStatus',
										text: 'Code',
										flex: 1
									},
									{
										dataIndex: 'failureCode',
										text: 'Reason',
										flex: 1
									},
									{
										dataIndex: 'dateStart',
										text: 'Started',
										flex: 2
									},
									{
										dataIndex: 'duration',
										text: 'Duration',
										flex: 1
									},
									{
										dataIndex: 'userName',
										text: 'User',
										flex: 1
									},
									{
										dataIndex: 'comment',
										text: 'Note',
										flex: 4
									},
									{   id : 'editEquipmentStatusId',
										xtype : 'actioncolumn',
										text : '',
										width : 22,
										items : [{
											getClass : function(v, meta, rec) {
												return 'column_edit';
											}
										}]
									},
									{   id : 'deleteEquipmentStatusId',
										xtype : 'actioncolumn',
										text : '',
										width : 22,
										items : [{
											getClass : function(v, meta, rec) {
												return 'column_delete';
											}
										}]
									}
								]
							}]
						}, {
							title : 'Maintenance',
							layout : 'fit',
							dockedItems: [{
								xtype: 'toolbar',
								items: [
								{
									id : 'addEquipmentMaintenanceId',
									text : ' Add scheduled item ',
									iconCls : 'icon-shape_square_add',
									scope: this,
									handler : function() {
										this.fireEvent('addequipmentmaintenance', this);
									}
								}]
							}],
							items : {
								xtype : 'grid',
								id: 'equipmentMaintenanceGridId',
								layout : 'fit',
								store : storeMaintenance,
								defaults : {
									sortable : true
								},
								border : false,
								autoScroll : true,
								multiSelect : false,
								stateful : true,
								padding : 0,
								stateId : 'stateEquipMaintenanceGrid1',
								bbar : pagingBarMaintenance,
								viewConfig : {
									emptyText : 'No maintenance records to display'
								},
								columns : [
									{
										dataIndex: 'schedule',
										text: 'Schedule',
										flex: 2
									},
									{
										dataIndex: 'comment',
										text: 'Note',
										flex: 3
									},
									{
										dataIndex: 'cycleType',
										text: 'Cycle',
										flex: 1
									},
									{
										dataIndex: 'cycleRate',
										text: 'Rate',
										flex: 1
									},
									{
										dataIndex: 'cycleStartDate',
										xtype: 'datecolumn',
										text: 'Date start',
										format:'Y-m-d H:i:s A',
										flex: 2
									},
									{
										dataIndex: 'active',
										text: 'Active',
										flex: 1
									},
									{   id : 'editEquipmentMaintenanceId',
										xtype : 'actioncolumn',
										text : '',
										width : 22,
										items : [{
											getClass : function(v, meta, rec) {
												return 'column_edit';
											}
										}]
									},
									{   id : 'deleteEquipmentMaintenanceId',
										xtype : 'actioncolumn',
										text : '',
										width : 22,
										items : [{
											getClass : function(v, meta, rec) {
												return 'column_delete';
											}
										}]
									}
								]
							}
						}]
					}
				});

		this.callParent(arguments);
	}
});
