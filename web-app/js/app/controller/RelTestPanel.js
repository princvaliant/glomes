

Ext.define('glo.controller.RelTestPanel', {
	extend : 'Ext.app.Controller',

	show : function(code, pctg, pkey, tkey, propertyName, data) {

		var value1;
		var value2;
		
		var group =  data.get('relGroup').data.value
				
		var store = Ext.create('Ext.data.Store', {
					fields : [{
								name : 'name',
								type : 'float'
							}],
					autoLoad : false,
					proxy : {
						type : 'rest',
						method : 'GET',
						url : rootFolder + 'relTest/currents',
						extraParams : {
							code : code,
							tkey : tkey
						}
					},
					 sorters: [{
				         property: 'name',
				         direction: 'ASC'
				     }]
				});

		var chartStore = Ext.create('Ext.data.JsonStore', {
					fields : ['hours']
				});

		var propCombo = Ext.create('Ext.form.field.ComboBox', {
					store : store,
					border : 0,
					flex : 1,
					displayField : 'name',
					valueField : 'name',
					triggerAction : 'all',
					queryMode : 'local',
					fieldLabel : 'Current [mA]',
					labelAlign : 'top'
				});

		var storeCodes = Ext.create('Ext.data.Store', {
					fields : [{
								name : 'code',
								type : 'string'
							}],
					autoLoad : true,
					proxy : {
						type : 'rest',
						method : 'GET',
						url : rootFolder + 'relTest/devicesByGroup',
						extraParams : {
							tkey : tkey,
							group : group
						}
					},
					 sorters: [{
				         property: 'code',
				         direction: 'ASC'
				     }]
				});

		var filterByCodes = Ext.create('Ext.ux.form.MultiSelect', {
					store : storeCodes,
					minSelections : 1,
					border : 0,
					flex : 11,
					displayField : 'code',
					valueField : 'code',
					queryMode : 'local'
				});

		var filterByGroups = Ext.create('Ext.form.field.TextArea', {
					grow : true,
					flex : 2,
					fieldLabel : 'Group# or Board#',
					labelAlign : 'top',
					anchor : '100%',
					value : group
				});

		var win = Ext.create('Ext.window.Window', {
			title : 'Rel test data',
			resizable : false,
			modal : true,
			width : 1260,
			height : 800,
			y : 50,
			layout : {
				type : 'border',
				padding : 0
			},
			items : [{
				region : 'west',
				collapsible : false,
				width : 180,
				minWidth : 180,
				minHeight : 140,
				layout : 'fit',
				items : [{
					xtype : 'container',
					layout : {
						type : 'vbox',
						padding : 0,
						margin : 0,
						align : 'stretch'
					},
					items : [propCombo, filterByGroups, {
								xtype : 'button',
								flex : 1,
								scope : this,
								text : 'Devices by group/board',
								iconCls : 'icon-arrow_refresh',
								handler : function() {
									this.loadDevices(storeCodes, tkey,
											filterByGroups.value);
								}
							}, filterByCodes, {
								xtype : 'button',
								flex : 1,
								scope : this,
								text : 'View selected',
								iconCls : 'icon-arrow_refresh',
								handler : function() {
									
									this.showCharts(code, tkey, propCombo.getValue(), filterByCodes.getValue(), filterByGroups.getValue());	
								}
							}, {
								xtype : 'button',
								flex : 1,
								scope : this,
								text : 'Export all to excel',
								iconCls : 'icon-excel',
								handler : function() {
									this.exportData(tkey, storeCodes);
								}
							}]
				}]
			}, {
				region : 'center',
				border : false,
				layout : 'fit',
				listeners : {
					activate : function(tab) {
						this.showCharts(code, tkey, propCombo.getValue(), filterByCodes.getValue(), filterByGroups.getValue());	
					},
					scope: this
				},
				items : {
					xtype : 'tabpanel',
					id : 'relChartsTab',
					activeTab : 0,
					plain : true,
					layout : 'fit',
					defaults : {
						bodyPadding : 0
					},
					items : [{
						title : 'Hourly charts',
						id : 'relHourlyChartsPanelId',
						layout : {
							type : 'hbox',
							align : 'stretch'
						},
						listeners : {
							activate : function(tab) {
								this.showCharts(code, tkey, propCombo.getValue(), filterByCodes.getValue(), filterByGroups.getValue());	
							},
							scope: this
						},
						items : [{
							flex : 4,
							layout : {
								type : 'vbox',
								align : 'stretch'
							},
							items : [{
										xtype : 'chart',
										flex : 1,
										store : chartStore,
										title : 'EQE %',
										legend : {
											position : 'right',
											labelFont : '8px Helvetica,sans-serif',
											padding : 3,
											itemSpacing : -7
										}
									}, {
										xtype : 'chart',
										flex : 1,
										title : 'LOP %',
										store : chartStore,
										legend : {
											position : 'right',
											labelFont : '8px Helvetica,sans-serif',
											padding : 3,
											itemSpacing : -7
										}
									}, {
										xtype : 'chart',
										flex : 1,
										title : 'VOLTAGE',
										store : chartStore,
										legend : {
											position : 'right',
											labelFont : '8px Helvetica,sans-serif',
											padding : 3,
											itemSpacing : -7
										}
									}]
						}, {
							flex : 4,
							layout : {
								type : 'vbox',
								align : 'stretch'
							},
							items : [{
										xtype : 'chart',
										flex : 1,
										title : 'FWHM',
										store : chartStore,
										legend : {
											position : 'right',
											labelFont : '8px Helvetica,sans-serif',
											padding : 3,
											itemSpacing : -7
										}
									}, {
										xtype : 'chart',
										flex : 1,
										store : chartStore,
										title : 'PEAK',
										legend : {
											position : 'right',
											labelFont : '8px Helvetica,sans-serif',
											padding : 3,
											itemSpacing : -5
										}
									}, {
										xtype : 'chart',
										flex : 1,
										title : 'DOMINANT',
										store : chartStore,
										legend : {
											position : 'right',
											labelFont : '8px Helvetica,sans-serif',
											padding : 3,
											itemSpacing : -7
										}
									}]
						}]
					}, {
						title : 'Current/Voltage charts',
						layout : 'fit',
						id : 'relIVChartsPanelId',
						layout : {
							type : 'hbox',
							align : 'stretch'
						},
						listeners : {
							activate : function(tab) {
								this.showCharts(code, tkey, propCombo.getValue(), filterByCodes.getValue(), filterByGroups.getValue());	
							},
							scope:this
						},
						items : [{
							flex : 4,
							layout : {
								type : 'vbox',
								align : 'stretch'
							},
							items : [{
										xtype : 'chart',
										flex : 1,
										title : 'EQE',
										store : chartStore,
										xline: 'current',
										legend : {
											position : 'right',
											labelFont : '8px Helvetica,sans-serif',
											padding : 3,
											itemSpacing : -7
										}
									}, {
										xtype : 'chart',
										flex : 1,
										store : chartStore,
										title : 'LOP',
										xline: 'current',
										legend : {
											position : 'right',
											labelFont : '8px Helvetica,sans-serif',
											padding : 3,
											itemSpacing : -5
										}
									}]
						}, {
							flex : 4,
							layout : {
								type : 'vbox',
								align : 'stretch'
							},
							items : [{
										xtype : 'chart',
										flex : 1,
										store : chartStore,
										title : 'IV CURVE',
										xline: 'voltage',
										legend : {
											position : 'right',
											labelFont : '8px Helvetica,sans-serif',
											padding : 3,
											itemSpacing : -7
										}
									}, {
										xtype : 'chart',
										flex : 1,
										store : chartStore,
										xline: 'wl',
										title : 'SPECTRA',
										legend : {
											position : 'right',
											labelFont : '8px Helvetica,sans-serif',
											padding : 3,
											itemSpacing : -5
										}
									}]
						}]
					}]
				}
			}],
			buttons : [{
				text : 'Print',
				handler : function() {

					var tab = Ext.getCmp("relHourlyChartsPanelId");
					var DocumentContainer = tab.getEl().dom;
					var WindowObject = window
							.open(
									'',
									'PrintWindow',
									'width=1200,height=700,top=50,left=50,toolbars=no,scrollbars=yes,status=no,resizable=yes');
					WindowObject.document.writeln(DocumentContainer.innerHTML);
					WindowObject.document.close();
					WindowObject.focus();
					WindowObject.print();
					WindowObject.close();

				}
			}, {
				text : 'Close',
				handler : function() {
					this.up('window').destroy();
				}
			}]
		});

		store.on('load', function(ds, records, o) {
					propCombo.setValue(0.1);
					propCombo.fireEvent('select', this, 0.1);
				});

		storeCodes.on('load', function(ds, records, o) {
					filterByCodes.setValue(code);
					filterByCodes.fireEvent('select', this, code);
				});

		propCombo.on('change', function(combo, newValue, oldValue, opts) {
			
			this.showCharts(code, tkey, newValue, filterByCodes.getValue(), filterByGroups.getValue());	

		}, this);

		filterByCodes.on('change', function(combo, newValue, oldValue, opts) {

			this.showCharts(code, tkey, propCombo.getValue(), newValue, filterByGroups.getValue());
		
		}, this);

		win.show();

		store.load();
		
		filterByCodes.setValue(code);
		filterByCodes.fireEvent('select', this, code);
	},
	
	showCharts : function(code, tkey, current, codes, groups) {

		if (current && codes && codes.length > 0) {
		
			var tabActive = Ext.getCmp('relChartsTab').activeTab;
			
			if (tabActive.id == 'relHourlyChartsPanelId') {
				this.loadHourlyCharts(code, tkey, current, codes, groups);
			}
			
			if (tabActive.id == 'relIVChartsPanelId' ) {
				this.loadIVCharts(code, tkey, current, codes);
			}

		}
	},

	loadDevices : function(store, tkey, group) {

		store.getProxy().extraParams.group = group;
		store.getProxy().extraParams.tkey = tkey;
		store.load();

	},

	loadHourlyCharts : function(code, tkey, current, devicesArray, groups) {

		if (current && devicesArray.length > 0) {

			var panel = Ext.getCmp('relHourlyChartsPanelId');
			panel.setLoading(true);

			var vSeries = new Array();
			
			
			var arr = Ext.Array.map( devicesArray, function(item) {
			 	return item.replace('-B','');
			}, this);
			
			Ext.Array.forEach(arr, function(item) {
						Ext.Array.include(vSeries, {
									type : 'line',
									axis : 'left',
									xField : 'hours',
									smooth : false,
									highlight : true,
									yField : item,
									markerConfig : {
										type : 'circle',
										size : 3,
										radius : 2,
										'stroke-width' : 1
									}
								});
					});

			var charts = panel.query('chart');
			Ext.Object.each(charts, function(idx) {

						var chart = charts[idx];

						var vAxes = new Array();
						Ext.Array.include(vAxes, {
									type : 'Numeric',
									position : 'left',
									fields : arr,
									title : chart.title,
									grid : true
								});
						Ext.Array.include(vAxes, {
									type : 'Category',
									position : 'bottom',
									fields : ['hours'],
									title : 'Hours'
								});

						chart.bindStore(Ext.create('Ext.data.JsonStore', {
									fields : Ext.Array.merge(arr,
											['hours'])
								}));
						chart.surface.removeAll()
						chart.axes.clear();
						chart.axes.addAll(vAxes);
						chart.series.clear();
						chart.series.addAll(vSeries);
						chart.redraw();
					});

			Ext.Ajax.request({
						method : 'GET',
						params : {
							code : code,
							tkey : tkey,
							current : current,
							deviceCodes : [arr],
							groups : groups
						},
						url : rootFolder + 'relTest/charts',
						success : function(response) {

							var obj = Ext.decode(response.responseText);

							Ext.Object.each(charts, function(idx) {
										var chart = charts[idx];
										chart.store.loadData(obj[chart.title]);
									});
							panel.setLoading(false);
						}
					});
		}
	},

	loadIVCharts : function(code, tkey, current, devicesArray) {

		var code;

		if (devicesArray.length == 1) {

			var panel = Ext.getCmp('relIVChartsPanelId');
			panel.setLoading(true);
			
			code = devicesArray[0];

			var charts = panel.query('chart');

			Ext.Ajax.request({
						method : 'GET',
						params : {
							code : code,
							tkey : tkey
						},
						url : rootFolder + 'relTest/hours',
						success : function(response) {

							var hours = Ext.decode(response.responseText);
							
							Ext.Object.each(charts, function(idx) {
								var chart = charts[idx];
								
								var vSeries = new Array();
								var vAxes = new Array();
																
								Ext.Array.forEach(hours, function(item) {
									Ext.Array.include(vSeries, {
												type : 'line',
												axis : 'left',
												xField : chart.xline,
												smooth : false,
												highlight : true,
												yField : item,
												markerConfig : {
													type : 'circle',
													size : 3,
													radius : 2,
													'stroke-width' : 1
												}
											});
								});
								
							
								Ext.Array.include(vAxes, {
											type: 'Numeric',
											position : 'left',
											fields : hours,
											title : chart.title,
											grid : true
										}
									);
									
								var majorts = 3;
								var minorts = 4;
								var min = 0;
								var max = 20;
								if (chart.xline == 'voltage' )	{
									min = 0;
									max = 6;
									minorts = 8;
									majorts = 5;
								}
								if (chart.xline == 'wl' )	{
									min = 350;
									max = 650;
									minorts = 4;
									majorts = 5;
								}
								
								Ext.Array.include(vAxes, {
											type : 'Numeric',
											position : 'bottom',
											minimum : min,
											maximum : max,
											majorTickSteps: majorts,
											minorTickSteps: minorts,
											dashSize:4, 
											grid:true,
											fields : [chart.xline],
											title : chart.xline
										});
										
								chart.bindStore(Ext.create('Ext.data.JsonStore', {
									fields : Ext.Array.merge(hours,[chart.xline])
								}));
								
								chart.surface.removeAll()
								chart.axes.clear();
								chart.axes.addAll(vAxes);
								chart.series.clear();
								chart.series.addAll(vSeries);
									
								Ext.Ajax.request({
									method : 'GET',
									params : {
										code : code,
										tkey : tkey,
										chart: chart.title,
										current : current,
										x: chart.xline
									},
									url : rootFolder + 'relTest/chartsIV',
									success : function(response) {
			
										var obj = Ext.decode(response.responseText);
										chart.store.loadData(obj);
										panel.setLoading(false);
									}
								});
								
							});
						}
					});
		}
	},

	exportData : function(tkey, codes) {

		window.open(rootFolder + 'relTest/export?tkey=' + tkey + '&codes='
						+ codes.collect('code'), 'resizable,scrollbars');
	}
});