

Ext.define('glo.controller.DashboardPanel', {
	extend : 'Ext.app.Controller',
	views : ['glo.view.dashboard.List', 'glo.view.dashboard.Content'],

	init : function() {

		this.control({
					'dashboardlist' : {
						afterrender : this.onPanelRendered,
						dashboardselect : this.onSelect,
						dashboarddelete : this.onDelete
					},
					'dashboardform' : {
						afterrender : this.onFormPanelRendered
					},
					'dashboardcontent' : {
						dashboardedit : this.onConfigure,
						dashboardrefresh : this.onRefresh
					},
					'dashboardlist textfield[id=searchDashboardId]' : {
						change : this.onSearch
					}
				});

		this.dashboardSelect = Ext.Function.createThrottled(this.dashboardSelect,
				500, this);
	},

	onPanelRendered : function(p, e) {

		this.createGrid('dashboard');
	},
	
	onSearch : function(thisField, newValue, oldValue, options) {

		var panel = thisField.ownerCt.ownerCt;
		var grid = panel.items.get(0);
		grid.store.currentPage = 1;
		grid.store.getProxy().extraParams.search = newValue;
		grid.store.load();
	},

	onConfigure : function(o) {

		var grid = Ext.getCmp('dashboardGridId');
		var rows = grid.getSelectionModel().getSelection();

		var dataViewId = rows[0].data.id;
		var rName = rows[0].data.name;
		var urlExport = rows[0].data.urlDashboardData;
		
		//if (urlExport == '') {
		if (true) {
			var win = Ext.create('Ext.window.Window', {
					title : 'Configure',
					resizable : true,
					modal : true,
					y : 20,
					layout : 'auto',
					items : {
						id : 'dashboardFormId',
						xtype : 'dashboardform',
						dataViewId: dataViewId,
						urlDashboardData: rows[0].data.urlDashboardData,
						parentWindow : o.ownerCt.ownerCt.ownerCt
					}
				});

			win.show();
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
					outFormat : 'chart',
					urlExport : urlExport
				}
			});

			win.show();
		}
	},
	
	onRefresh : function(o) {

		var grid = Ext.getCmp('dashboardGridId');
		if(grid != undefined) {
			var rows = grid.getSelectionModel().getSelection();
			if (rows[0]) {
				this.refreshData(Ext.getCmp('panelChart' + rows[0].data.id), rows[0].data.id, 'calc');
			}
		}
	},
	
	urlChanged : function(dataViewId, value) {
		
		Ext.Ajax.request({
			scope : this,
			method : 'POST',
			params : {
				dataViewId : dataViewId,
				urlValue: value
			},
			url : rootFolder + 'dataViewCharts/changeUrl',
			success : function(response) {
				
				var obj = Ext.decode(response.responseText);
				if (obj.success == false) {
					Ext.Msg.alert('Change failed', obj.msg);
				} else {
					Ext.ux.Message.msg('Change applied', '');
				}
			}
		});
	},

	onDelete : function(o) {

		Ext.MessageBox.confirm('Confirm remove',
				'Remove selected reports(s)?', function(btn) {
					if (btn == 'yes') {
						
					var grid = Ext.getCmp('dashboardGridId');
					var rows = grid.getSelectionModel().getSelection();
					Ext.Ajax.request({
						method : 'POST',
						params : {
							dataViewId : rows[0].data.id
						},
						url : rootFolder + 'dataViews/removeFromDashboard',
						success : function(response) {
							
							var obj = Ext.decode(response.responseText);
							if (obj.success == false) {
								Ext.Msg.alert('Change failed', obj.msg);
							} else {
								Ext.ux.Message.msg('Change applied', '');
								grid.store.load();
							}
						}
					});
				}
			});
	},
	

	onSelect : function(o, selections) {
		this.select(o, selections);
	},

	select : function(o, selections) {

		Ext.getCmp('removeDashboard').setDisabled(!selections.length);
		Ext.getCmp('editDashboard').setDisabled(!selections.length);

		var panel = Ext.ComponentQuery.query('dashboardcontent')[0];
		
		if (selections[0] != undefined) {

			var sel = selections[0].data;
			panel.setTitle(sel.name);

			var chartPanelId = 'panelChart' + sel.id;
            var panelChart = Ext.getCmp(chartPanelId);
			if (panelChart == undefined) {
				panelChart = Ext.create('Ext.panel.Panel', {
					id: chartPanelId,
					layout: 'fit'
				});
            }

            panel.body.update('');
            panel.add(panelChart);
            panel.layout.setActiveItem(chartPanelId);
				
			if (sel.chartType == undefined || sel.chartType == '' || sel.chartType == null) sel.chartType = 'column';
			Ext.getCmp('dashboardChartTypeId').setValue(sel.chartType);
			Ext.getCmp('dashboardYMinId').setValue(sel.yMin);
			Ext.getCmp('dashboardYMaxId').setValue(sel.yMax);
			Ext.getCmp('dashboardZMinId').setValue(sel.zMin);
			Ext.getCmp('dashboardZMaxId').setValue(sel.zMax);
				
			this.refreshData(panelChart, sel.id, 'load');

		} else {
			panel.setTitle('No selected reports');
		}
	},
	
	refreshData : function (panelChart, dataViewId, refreshMode, date) {

		panelChart.setLoading(true);
		panelChart.removeAll(true);

        var type = null;
        var yMin = null;
        var yMax = null;
        var zMin = null;
        var zMax = null;
        if (Ext.getCmp('dashboardChartTypeId') !== undefined) {
             type = Ext.getCmp('dashboardChartTypeId').getValue();
             yMin = Ext.getCmp('dashboardYMinId').getValue();
             yMax = Ext.getCmp('dashboardYMaxId').getValue();
             zMin = Ext.getCmp('dashboardZMinId').getValue();
             zMax = Ext.getCmp('dashboardZMaxId').getValue();
        }

		Ext.Ajax.request({
					scope : this,
					method : 'GET',
					params : {
						dataViewId : dataViewId,
						chartType : type,
						yMin : yMin,
						yMax : yMax,
						zMin : zMin,
						zMax : zMax,
						refresh : refreshMode
					},
					url : rootFolder + 'dataViewCharts/draw',
					success : function(response) {
						
						try {
							var obj = Ext.decode(response.responseText);
						} catch(e) {
							Ext.ux.Message.msg('Error getting data', 'Invalid data format from service');
							panelChart.setLoading(false);
							return;
						}

						if (obj.success == false) {
							Ext.ux.Message.msg('Error getting data', obj.msg);
							panelChart.setLoading(false);
							return;
						}

				        Ext.Array.each(obj, function(chartDef) {

                            var type = chartDef.type;
                            var yMin = chartDef.yMin == 0 ? null : chartDef.yMin ;
                            var yMax = chartDef.yMax == 0 ? null : chartDef.yMax ;
                            var zMin = chartDef.zMin == 0 ? null : chartDef.zMin ;
                            var zMax = chartDef.zMax == 0 ? null : chartDef.zMax ;

				        	var allFields = new Array();
				        	var yFields = new Array();
				        	var zFields = new Array();
				        	
				        	var allAxes = new Array();
				        	var allSeries = new Array();
				        	
				        	var font = '10px Tahoma,sans-serif';
				        	var rotate = 270;
				        	
				        	var totalData = chartDef.data.length;
				        	if (totalData <= 20) {
				        		font = '14px Tahoma,sans-serif';
				        		rotate = 315;
				        	}
				        	if (totalData > 20 && totalData <= 40) {
				        		font = '12px Tahoma,sans-serif';
				        		//rotate = 300;
				        	}
				        	if (totalData > 40 && totalData < 60) {
				        		font = '11px Tahoma,sans-serif';
				        	}				        	
				        	
				        	if (type != 'pie') {
					        	Ext.Array.include(allAxes, {
									type: chartDef.xAxisType,
									position : 'bottom',
									fields : [chartDef.x],
									title : chartDef.xTitle,
									grid : true,
									label: {
										font : font,
								        rotate: {
								            degrees: rotate
								        }
								    }
								});
				        	}
				        	
				        	Ext.Array.include(allFields, chartDef.x);
				        	
				        	Ext.Array.each(chartDef.y, function(yObj) {
				        		Ext.Array.include(allFields, yObj.y);
				        		if (yObj.yAxis == 'left') {
				        			Ext.Array.include(yFields, yObj.y);
				        		}
				        		else {
				        			Ext.Array.include(zFields, yObj.y);
				        		}
				        	});
				        	var yFieldsSize = yFields.length;
				        	var zFieldsSize = zFields.length;
				        	
				        	Ext.Array.each(chartDef.tip, function(tipObj) {
				        		Ext.Array.include(allFields, tipObj);
				        	});

				        	if (type == 'column') {
				        		if (yFieldsSize > 0) {
						        	Ext.Array.include(allSeries, {
										type : type,
										axis : 'left',
										xField : chartDef.x,
										smooth : true,
										highlight : true,
										yField : yFields,
										tips: {
							                  trackMouse: true,
							                  width: 220,
							                  height: 58,
							                  renderer: function(storeItem, item) {
							                    this.setTitle(item.yField + '<br/>' + item.value[0] + ': ' + item.value[1]);
							                  }
							           }
									});
				        		}
				        		if (zFieldsSize > 0) {
						        	Ext.Array.include(allSeries, {
										type : type,
										axis : 'right',
										xField : chartDef.x,
										smooth : true,
										highlight : true,
										yField : zFields,
										tips: {
							                  trackMouse: true,
							                  width: 220,
							                  height: 58,
							                  renderer: function(storeItem, item) {
							                    this.setTitle(item.yField + '<br/>' + item.value[0] + ': ' + item.value[1]);
							                  }
							           }
									});
				        		}
				        		
				        	} else if (type == 'pie') {
				        		Ext.Array.each(chartDef.y, function(yObj) {
					        		Ext.Array.include(allSeries, {
										type : type,
										field : yObj.y,
										smooth : false,
										highlight : true,
									
										tips: {
							                  trackMouse: true,
							                  width: 220,
							                  height: 58,
							                  renderer: function(storeItem, item) {
							                	  
							                	  this.setTitle(storeItem.get(chartDef.x) + ': ' + storeItem.get( yObj.y));
							                  }
							            },
							            label: {
						                    field: chartDef.x,
						                    display: 'rotate',
						                    contrast: true
						                }
									});
				        		});
				        		
				        	}  else if (type == 'bar') {
				        		if (yFieldsSize > 0) {
					        		Ext.Array.include(allSeries, {
											type : type,
											axis : 'left',
											xField : [chartDef.x],
											column : true,
											gutter : 10,
											highlight : false,
											stacked : true,
											yField : yFields,
											tips: {
								                  trackMouse: true,
								                  width: 220,
								                  height: 58,
								                  renderer: function(storeItem, item) {
								                    this.setTitle(item.yField + '<br/>' + item.value[0] + ': ' + item.value[1]);
								                  }
								           }
					        		});
				        		}
				        		if (zFieldsSize > 0) {
					        		Ext.Array.include(allSeries, {
											type : type,
											axis : 'right',
											xField : [chartDef.x],
											column : true,
											gutter : 10,
											highlight : false,
											stacked : true,
											yField : zFields,
											tips: {
								                  trackMouse: true,
								                  width: 220,
								                  height: 58,
								                  renderer: function(storeItem, item) {
								                    this.setTitle(item.yField + '<br/>' + item.value[0] + ': ' + item.value[1]);
								                  }
								           }
					        		});
				        		}
				        		
				        	} else {
				        		Ext.Array.each(chartDef.y, function(yObj) {
				        			Ext.Array.include(allSeries, {
										type : type,
										axis : yObj.yAxis,
										xField : chartDef.x,
										smooth : false,
										highlight : true,
										yField : [yObj.y],

										tips: {
							                  trackMouse: true,
							                  width: 320,
							                  height: 128,
							                  renderer: function(storeItem, item) {
							                	  if (storeItem.data.tip == undefined) 
							                		  this.setTitle(storeItem.data[storeItem.fields.keys[0]] + ': ' + item.value[1] );
							                	  else
							                		  this.setTitle(storeItem.data[storeItem.fields.keys[0]] + ': ' + item.value[1] + '</br>' + storeItem.data.tip);
							                  }
							           }
									});
				        		});
				        	}
				        	
				        	if (type != 'pie') {
				        		if (yFieldsSize > 0) {
						        	if (yMin != null && yMax != null) {
				        				Ext.Array.include(allAxes, {
											type : 'Numeric',
											position : 'left',
											fields : yFields,
											minimum : yMin,
											maximum : yMax,
											grid : true
										});
						        	}
						        	else if (yMin != null && yMax == null) {
				        				Ext.Array.include(allAxes, {
											type : 'Numeric',
											position : 'left',
											fields : yFields,
											minimum : yMin,
											//maximum : yMax,
											grid : true
										});
						        	}
						        	else if (yMin == null && yMax != null) {
				        				Ext.Array.include(allAxes, {
											type : 'Numeric',
											position : 'left',
											fields : yFields,
											//minimum : yMin,
											maximum : yMax,
											grid : true
										});
						        	}
						        	else if (yMin == null && yMax == null) {
				        				Ext.Array.include(allAxes, {
											type : 'Numeric',
											position : 'left',
											fields : yFields,
											//minimum : yMin,
											//maximum : yMax,
											grid : true
										});
						        	}
				        		}
				        		if (zFieldsSize > 0) {
				        			var zGrid = false;
				        			if (yFieldsSize == 0) zGrid = true;
				        			
						        	if (zMin != null && zMax != null) {
						        		Ext.Array.include(allAxes, {
											type : 'Numeric',
											position : 'right',
											fields : zFields,
											minimum : zMin,
											maximum : zMax,
											grid : zGrid
										});
						        	}
						        	else if (zMin != null && zMax == null) {
						        		Ext.Array.include(allAxes, {
											type : 'Numeric',
											position : 'right',
											fields : zFields,
											minimum : zMin,
											//maximum : zMax,
											grid : zGrid
										});
						        	}
						        	else if (zMin == null && zMax != null) {
						        		Ext.Array.include(allAxes, {
											type : 'Numeric',
											position : 'right',
											fields : zFields,
											//minimum : zMin,
											maximum : zMax,
											grid : zGrid
										});
						        	}
						        	else if (zMin == null && zMax == null) {
						        		Ext.Array.include(allAxes, {
											type : 'Numeric',
											position : 'right',
											fields : zFields,
											//minimum : zMin,
											//maximum : zMax,
											grid : zGrid
										});
						        	}
				        		}
				        	}
			        		        	
							var chart = Ext.create('Ext.chart.Chart', {
								flex:1,
								align: 'stretch',
								store : Ext.create('Ext.data.JsonStore', {
									fields : allFields
								}),
								legend : {
									position : 'bottom',
									labelFont : '11px Helvetica,sans-serif',
									padding : 0,
									itemSpacing: 0
								},
								axes : allAxes,
								series : allSeries
							});

							Ext.suspendLayouts();
							chart.store.loadData(chartDef.data);
							Ext.resumeLayouts(true);
							panelChart.add(chart);
                            panelChart.setLoading(false);

                            gloApp.fireEvent('chartloaded', dataViewId, date, panelChart, chart, 'graph');
				        });
					}
				});
	},
	
	
	onFormPanelRendered : function(p, e) {

		this.redrawForm(p.dataViewId);
	},
	
	redrawForm : function(dataViewId) {
		
		Ext.getCmp('dashboardPanelX').removeAll(true);
		Ext.getCmp('dashboardPanelY').removeAll(true);
		Ext.getCmp('dashboardPanelZ').removeAll(true);
		Ext.getCmp('dashboardPanelS').removeAll(true);

		var store = new Ext.data.Store({
				fields : ['dataViewChartId','dataViewVariableId','idx','axis','xDateGroup','xNumberRange','yAggregate','title','dataType','value'],
				autoDestroy : false,
				autoLoad : true,
				autoSync : true,
				pageSize : '25',
				remoteSort : true,
				proxy : {
					type : 'rest',
					url : rootFolder + 'dataViewCharts',
					startParam : 'offset',
					pageParam : 'page',
					dirParam : 'order',
					limitParam : 'max',
					extraParams : {
						'dataViewId': dataViewId
					},
					simpleSortMode : true,
					reader : {
						type : 'json',
						root : 'data',
						totalProperty : 'count',
						messageProperty : 'msg'
					}
				}
			});
			
			store.on('load', function() {
				store.each(function(rec){
					gloApp.getController('DashboardPanel').addToReport(rec.get("axis"), dataViewId, rec.get("dataViewVariableId"),  rec.get("dataViewChartId"),  rec.get("title"), rec.get("dataType"), rec.get("value"));
				});
			});
		
	},
	
	addToReport : function(axis, dataViewId, dataViewVariableId, dataViewChartId, title,  dataType,  value) {
		
		 var array = new Array();	
		 
		 var panel = Ext.getCmp('dashboardPanel' + axis)
 
		 if (axis == 'X') {
			 if (dataType == 'date') {
			 	 Ext.Array.include( array, {
	            		xtype: 'combo',
						store : ['hourly', 'daily', 'weekly', 'monthly', 'yearly'],
						forceSelection: true,
						queryMode: 'local',
	            		name: 'xDateGroup',
	            		emptyText: 'select one',
	            		fieldLabel: 'Date grouping',
	            		flex:10,									
						margin: 7,
	            		value: value,
	            		listeners: {
						    select: function(combo, record, index) {

								 Ext.Ajax.request({
									scope : this,
									method : 'POST',
									params : {
										dataViewChartId: dataViewChartId,
										dataViewVariableId : dataViewVariableId,
										axis : axis,
										dataType: dataType,
										title : title,
										value : combo.getValue()
									},
									url : rootFolder + 'dataViewCharts/saveVariable',
									success : function(response) {
										var obj = Ext.decode(response.responseText);
										if (obj.success == false) {
											Ext.Msg.alert('Change failed', obj.msg);
										} else {
											Ext.ux.Message.msg('Change applied', '');
										}
										gloApp.getController('DashboardPanel').redrawForm(dataViewId);
									}
								});						    
							}
						}
	        		});
			 }
			 
			 if (dataType == 'string' || dataType == '') {
				 Ext.Array.include( array, {
	            		xtype: 'combo',
						store : ['whole'],
						forceSelection: true,
						queryMode: 'local',
	            		name: 'xStringGroup',
	            		emptyText: 'select one',
	            		fieldLabel: 'String grouping',
	            		flex:10,									
						margin: 7,
	            		value: value,
	            		listeners: {
						    select: function(combo, record, index) {

								 Ext.Ajax.request({
									scope : this,
									method : 'POST',
									params : {
										dataViewChartId: dataViewChartId,
										dataViewVariableId : dataViewVariableId,
										axis : axis,
										dataType: dataType,
										title : title,
										value : combo.getValue()
									},
									url : rootFolder + 'dataViewCharts/saveVariable',
									success : function(response) {
										
										var obj = Ext.decode(response.responseText);
										if (obj.success == false) {
											Ext.Msg.alert('Change failed', obj.msg);
										} else {
											Ext.ux.Message.msg('Change applied', '');
										}
										gloApp.getController('DashboardPanel').redrawForm(dataViewId);
									}
								});						    
							}
						}
	        		});
			 }
			 
			 if (dataType == 'float' || dataType == 'int' || dataType == 'scientific') {
				 Ext.Array.include( array, {
	            		xtype: 'combo',
						store : ['last','all'],
						forceSelection: true,
						queryMode: 'local',
	            		name: 'xNumericGroup',
	            		fieldLabel: 'Numeric grouping',
	            		emptyText: 'select one',
	            		flex:10,									
						margin: 7,
	            		value: value,
	            		listeners: {
						    select: function(combo, record, index) {

								 Ext.Ajax.request({
									scope : this,
									method : 'POST',
									params : {
										dataViewChartId: dataViewChartId,
										dataViewVariableId : dataViewVariableId,
										axis : axis,
										dataType: dataType,
										title : title,
										value : combo.getValue()
									},
									url : rootFolder + 'dataViewCharts/saveVariable',
									success : function(response) {
										
										var obj = Ext.decode(response.responseText);
										if (obj.success == false) {
											Ext.Msg.alert('Change failed', obj.msg);
										} else {
											Ext.ux.Message.msg('Change applied', '');
										}
										gloApp.getController('DashboardPanel').redrawForm(dataViewId);
									}
								});						    
							}
						}
	        		});
			 }
		 }
		 
		  if (axis == 'Y' || axis == 'Z') {
			  if (dataType == 'float' || dataType == 'int' || dataType == 'scientific' || dataType == '') {
			 	 Ext.Array.include( array, {
	            		xtype: 'combo',
						store : ['last','count','average','sum','max','min','median','avg+sigma','avg-sigma'],
						forceSelection: true,
	            		name: 'yCalculation',
	            		fieldLabel: 'Calculation',
	            		emptyText: 'select one',
	            		flex:10,									
						margin: 7,
	            		value: value,
	            		listeners: {
						    select: function(combo, record, index) {
						    	 Ext.Ajax.request({
									scope : this,
									method : 'POST',
									params : {
										dataViewVariableId : dataViewVariableId,
										dataViewChartId: dataViewChartId,
										axis : axis,
										dataType: dataType,
										title : title,
										value : combo.getValue()
									},
									url : rootFolder + 'dataViewCharts/saveVariable',
									success : function(response) {
										var obj = Ext.decode(response.responseText);
										if (obj.success == false) {
											Ext.Msg.alert('Change failed', obj.msg);
										} else {
											Ext.ux.Message.msg('Change applied', '');
										}
										gloApp.getController('DashboardPanel').redrawForm(dataViewId);
									}
								});					
						    }
						}
	        		});
			 }
		 }
		  
		 if (axis == 'S') {
		 	 Ext.Array.include( array, {
            		xtype: 'combo',
					store : ['ASC', 'DESC'],
					forceSelection: true,
            		name: 'sortCalculation',
            		fieldLabel: 'Direction',
            		emptyText: 'select one',
            		flex:10,									
					margin: 7,
            		value: value,
            		listeners: {
					    select: function(combo, record, index) {
					    	 Ext.Ajax.request({
								scope : this,
								method : 'POST',
								params : {
									dataViewVariableId : dataViewVariableId,
									dataViewChartId: dataViewChartId,
									axis : axis,
									dataType: dataType,
									title : title,
									value : combo.getValue()
								},
								url : rootFolder + 'dataViewCharts/saveVariable',
								success : function(response) {
									var obj = Ext.decode(response.responseText);
									if (obj.success == false) {
										Ext.Msg.alert('Change failed', obj.msg);
									} else {
										Ext.ux.Message.msg('Change applied', '');
									}
									gloApp.getController('DashboardPanel').redrawForm(dataViewId);
								}
							});					
					    }
					}
        		});
		 }

		 
		 Ext.Array.include( array, {
    		xtype: 'button',
    		text: 'Remove',
    		margin: 7,
    		width: 22,
    		iconCls: 'column_delete',
    		handler: function() {
		       Ext.Ajax.request({
					scope : this,
					method : 'POST',
					params : {
						dataViewChartId : dataViewChartId
					},
					url : rootFolder + 'dataViewCharts/deleteVariable',
					success : function(response) {
						var obj = Ext.decode(response.responseText);
						if (obj.success == false) {
							Ext.Msg.alert('Delete failed', obj.msg);
						} else {
							Ext.ux.Message.msg('Change applied', '');
						}
						gloApp.getController('DashboardPanel').redrawForm(dataViewId);
					}
				});				
		    }
    	});
		
		
		var tf = Ext.create('Ext.form.Panel', {	
        	border: false,
        	plain: true,
        	padding: 5,
        	layout: {
            	type: 'vbox',
            	align: 'stretch'  // Child items are stretched to full width
        	},
            items: [{
            	xtype: 'fieldset' ,
            	layout: 'hbox',
            	title: title,
            	items: array
        	}]
        });
				                
		panel.add(tf);
	},
	

	createGrid : function(domain) {

		var panel = Ext.ComponentQuery.query('#dashboardPanel > dashboardlist')[0];

		Ext.Ajax.request({
			scope : panel,
			method : 'GET',
			params : {
				domain : 'dashboard'
			},
			url : rootFolder + 'dataViews/getFields',
			success : function(response) {

				var obj = Ext.decode(response.responseText);
				var columns = obj.columns;

				var store = new Ext.data.Store({
							id : 'dataStoreDashboard',
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
								url : rootFolder + 'dataViews/dashboards',
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

				var pagingBar = Ext.create('Ext.PagingToolbar', {
							store : store,
							displayInfo : true,
							displayMsg : 'Displaying reports {0} - {1} of {2}',
							emptyMsg : "No reports to display"
						});

				var sm = new Ext.selection.RowModel({
							listeners : {
								scope : panel,
								selectionchange : function(selectionModel,
										selected, options) {
									this.fireEvent('dashboardselect', this,
											selected);
								}
							}
						});
				
				Ext.Array.erase(columns,2,2);

				var grid = new Ext.grid.GridPanel({
							id : 'dashboardGridId',
							store : store,
							defaults : {
								sortable : true
							},
							border : false,
							autoScroll : true,
							multiSelect : false,
							stateful : true,
							padding : 0,
							stateId : 'stateDashboardGrid',
							bbar : pagingBar,
							selModel : sm,
							viewConfig : {
								emptyText : 'No reports to display',
								forceFit : true
							},
							columns : columns
						});
						
				panel.removeAll(true);
				panel.add(grid);
				
				grid.store.getProxy().extraParams.search = Ext.getCmp('searchDashboardId').getValue();
				grid.store.load()
		
			}
		});

	}


});