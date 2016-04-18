

Ext.define('glo.controller.LampTestPanel', {
	extend : 'Ext.app.Controller',

	show : function(code, pctg, pkey, tkey, propertyName) {

		var value1;
		var value2;
		
		var lampGroup = code.split('_')[0];
		
		var store = Ext.create('Ext.data.Store', {
					fields : [{
								name : 'name',
								type : 'float'
							}],
					autoLoad : false,
					proxy : {
						type : 'rest',
						method : 'GET',
						url : rootFolder + 'lampTest/currents',
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
					fieldLabel : 'Current',
					labelAlign : 'top'
				});

		var storeCodes = Ext.create('Ext.data.Store', {
					fields : [{
								name : 'code',
								type : 'string'
							}],
					autoLoad : false,
					data : [{
								code : code
							}],
					proxy : {
						type : 'rest',
						method : 'GET',
						url : rootFolder + 'lampTest/devicesByGroup',
						extraParams : {
							group : lampGroup
						}
					},
					 sorters: [{
				         property: 'code',
				         direction: 'ASC'
				    }]
				});

		var filterByCodes = Ext.create('Ext.ux.form.MultiSelect', {
					store : storeCodes,
					minSelections : 0,
					maxSelections : 1,
					border : 0,
					flex : 11,
					displayField : 'code',
					valueField : 'code',
					queryMode : 'local'
				});

		var filterByGroups = Ext.create('Ext.form.field.TextArea', {
					grow : true,
					flex : 2,
					fieldLabel : 'Group #',
					labelAlign : 'top',
					anchor : '100%',
					value : lampGroup
				});

		var win = Ext.create('Ext.window.Window', {
			title : 'Lamp test data',
			resizable : false,
			modal : true,
			width : 1240,
			height : 800,
			y : 50,
			layout : {
				type : 'border',
				padding : 0
			},
			items : [{
				region : 'west',
				collapsible : false,
				width : 160,
				minWidth : 160,
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
								text : 'Get devices by wafer',
								iconCls : 'icon-arrow_refresh',
								handler : function() {
									this.loadDevices(storeCodes,
											filterByGroups.value, tkey);
								}
							}, filterByCodes, {
								xtype : 'button',
								flex : 1,
								scope : this,
								text : 'Export to excel',
								iconCls : 'icon-excel',
								handler : function() {
									this.exportData(tkey, filterByCodes);
								}
							}]
				}]
			}, {
				region : 'center',
				border : false,
				layout : 'fit',
				listeners : {
					activate : function(tab) {
						this.showCharts(code, tkey, propCombo.getValue(),
								filterByCodes.getValue(), filterByGroups
										.getValue());
					},
					scope : this
				},
				items : {
					xtype : 'tabpanel',
					id : 'lampChartsTab',
					activeTab : 0,
					plain : true,
					layout : 'fit',
					defaults : {
						bodyPadding : 0
					},
					items : [{
						title : 'EQE, LOP, Kfactor, FWHM charts',
						id : 'lampCharts1PanelId',
						layout : {
							type : 'hbox',
							align : 'stretch'
						},
						listeners : {
							activate : function(tab) {
								this.showCharts(code, tkey, propCombo
												.getValue(), filterByCodes
												.getValue(), filterByGroups
												.getValue());
							},
							scope : this
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
										xline : 'current',
										title : 'EQE',
										legend : {
											position : 'right',
											labelFont : '8px Helvetica,sans-serif',
											padding : 3,
											itemSpacing : -7
										}
									}, {
										xtype : 'chart',
										flex : 1,
										title : 'LOP',
										xline : 'current',
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
										xline : 'current',
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
										title : 'KFACTOR',
										xline : 'current',
										legend : {
											position : 'right',
											labelFont : '8px Helvetica,sans-serif',
											padding : 3,
											itemSpacing : -5
										}
									}]
						}]
					}, {
						title : 'IV, Spectra, Dominant, Peak charts',
						layout : 'fit',
						id : 'lampCharts2PanelId',
						layout : {
							type : 'hbox',
							align : 'stretch'
						},
						listeners : {
							activate : function(tab) {
								this.showCharts(code, tkey, propCombo
												.getValue(), filterByCodes
												.getValue(), filterByGroups
												.getValue());
							},
							scope : this
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
										title : 'CURRENT',
										store : chartStore,
										xline : 'voltage',
										legend : {
											position : 'right',
											labelFont : '8px Helvetica,sans-serif',
											padding : 3,
											itemSpacing : -7
										}
									},{
										xtype : 'chart',
										flex : 1,
										store : chartStore,
										xline : 'current',
										title : 'PEAK',
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
										title : 'DOMINANT',
										xline : 'current',
										legend : {
											position : 'right',
											labelFont : '8px Helvetica,sans-serif',
											padding : 3,
											itemSpacing : -7
										}
									},  {
										xtype : 'chart',
										flex : 1,
										store : chartStore,
										title : 'SPECTRA',
										xline : 'wl',
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

					var tab = Ext.getCmp("lampCharts1PanelId");
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
					propCombo.setValue(1.0);
					propCombo.fireEvent('select', this, 1.0);
				});

		storeCodes.on('load', function(ds, records, o) {
					filterByCodes.setValue(code);
					filterByCodes.fireEvent('select', this, code);
				});

		propCombo.on('change', function(combo, newValue, oldValue, opts) {

					this.showCharts(code, tkey, newValue, filterByCodes
									.getValue(), filterByGroups.getValue());

				}, this);

		filterByCodes.on('change', function(combo, newValue, oldValue, opts) {

					this.showCharts(code, tkey, propCombo.getValue(), newValue,
							filterByGroups.getValue());

				}, this);

		win.show();

		store.load();

		filterByCodes.setValue(code);
		filterByCodes.fireEvent('select', this, code);
	},

	showCharts : function(code, tkey, current, codes, groups) {

		if (current && codes && codes.length > 0) {

			var tabActive = Ext.getCmp('lampChartsTab').activeTab;

			if (tabActive.id == 'lampCharts1PanelId') {
				this.load1Charts(code, tkey, current, codes, groups);
			}

			if (tabActive.id == 'lampCharts2PanelId') {
				this.load2Charts(code, tkey, current, codes, groups);
			}

		}
	},

	loadDevices : function(store, group, tkey) {

		store.getProxy().extraParams.group = group;
		store.getProxy().extraParams.tkey = tkey;
		store.load();

	},

	load1Charts : function(code, tkey, current, devicesArray, groups) {

		if (current && devicesArray.length == 1) {

			var panel = Ext.getCmp('lampCharts1PanelId');
			panel.setLoading(true);

			code = devicesArray[0];

			var charts = panel.query('chart');

			Ext.Ajax.request({
				method : 'GET',
				params : {
					code : code,
					tkey : tkey
				},
				url : rootFolder + 'lampTest/steps',
				success : function(response) {

					var steps = Ext.decode(response.responseText);

					Ext.Object.each(charts, function(idx) {
								var chart = charts[idx];

								var vSeries = new Array();
								var vAxes = new Array();

								Ext.Array.forEach(steps, function(item) {
											Ext.Array.include(vSeries, {
														type : 'line',
														axis : 'left',
														xField : chart.xline,
														smooth : false,
														highlight : true,
														yField : item,
														tips: {
										                  trackMouse: true,
										                  width: 100,
										                  height: 28,
										                  renderer: function(storeItem, item2) {
										                    this.setTitle(storeItem.get(chart.xline) + '  :  ' +  Ext.util.Format.number(storeItem.get(item), '0.000'));
										                  }
										                },
														markerConfig : {
															type : 'circle',
															size : 2,
															radius : 2,
															'stroke-width' : 1
														}
													});
										});

								Ext.Array.include(vAxes, {
											type : 'Numeric',
											position : 'left',
											fields : steps,
											title : chart.title,
											decimals: 7,
       										grid : true
										});
								Ext.Array.include(vAxes, {
											type : 'Numeric',
											minimum : 0,
											maximum : 20,
											majorTickSteps: 3,
											minorTickSteps: 4,
											dashSize:4, 
											position : 'bottom',
											fields : [chart.xline],
											title : chart.xline,
											grid : true
										});

								chart.bindStore(Ext.create(
										'Ext.data.JsonStore', {
											fields : Ext.Array.merge(steps,
													[chart.xline])
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
												chart : chart.title,
												current : current,
												x : chart.xline
											},
											url : rootFolder
													+ 'lampTest/charts1',
											success : function(response) {

												var obj = Ext
														.decode(response.responseText);
												chart.store.loadData(obj);
												panel.setLoading(false);
											}
										});

							});
				}
			});
		}
	},

	load2Charts : function(code, tkey, current, devicesArray, groups) {

			if (current && devicesArray.length == 1) {

			var panel = Ext.getCmp('lampCharts2PanelId');
			panel.setLoading(true);

			code = devicesArray[0];

			var charts = panel.query('chart');

			Ext.Ajax.request({
				method : 'GET',
				params : {
					code : code,
					tkey : tkey
				},
				url : rootFolder + 'lampTest/steps',
				success : function(response) {

					var steps = Ext.decode(response.responseText);

					Ext.Object.each(charts, function(idx) {
								var chart = charts[idx];

								var vSeries = new Array();
								var vAxes = new Array();

								Ext.Array.forEach(steps, function(item) {
									Ext.Array.include(vSeries, {
												type : 'line',
												axis : 'left',
												xField : chart.xline,
												smooth : false,
												highlight : true,
												yField : [item],
												tips: {
										                  trackMouse: true,
										                  width: 100,
										                  height: 28,
										                  renderer: function(storeItem, item2) {
										                    this.setTitle(storeItem.get(chart.xline) + '  :  ' +  Ext.util.Format.number(storeItem.get(item), '0.000'));
										                  }
										                },
												markerConfig : {
													type : 'circle',
													size : 2,
													radius : 2,
													'stroke-width' : 1
												}
									});
								});

								Ext.Array.include(vAxes, {
											type : 'Numeric',
											position : 'left',
											fields : steps,
											title : chart.title,
											decimals: 7,
											grid : true 
										});
										
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
											minimum : min,
											maximum : max,
											majorTickSteps: majorts,
											minorTickSteps: minorts,
											dashSize:4, 
											grid:true,
											position : 'bottom',
											fields : [chart.xline],
											title : chart.xline
										});

								chart.bindStore(Ext.create(
										'Ext.data.JsonStore', {
											fields : Ext.Array.merge(steps,
													[chart.xline])
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
												chart : chart.title,
												current : current,
												x : chart.xline
											},
											url : rootFolder
													+ 'lampTest/charts2',
											success : function(response) {

												var obj = Ext
														.decode(response.responseText);
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

		window.open(rootFolder + 'lampTest/export?tkey=' + tkey + '&codes='
						+ codes.getValue(), 'resizable,scrollbars');
	}
});