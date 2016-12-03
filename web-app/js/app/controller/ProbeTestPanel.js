

Ext.define('glo.controller.ProbeTestPanel', {
	extend : 'Ext.app.Controller',
	
	init : function() {

		this.control({

		});
	},
	
	show : function(code, pctg, pkey, tkey, propertyName) {

		var value1;
		var value2;

    	var store = Ext.create('Ext.data.Store', {
					fields : [{
								name : 'name',
								type : 'string'
							}],
					autoLoad : false,
					proxy : {
						type : 'rest',
						method : 'GET',
						url : rootFolder + 'probeTest/devices',
						extraParams : {
							code : code,
							tkey : tkey
						}
					}
				});


		var propCombo = Ext.create('Ext.ux.form.MultiSelect', {
					store : store,
					border : 0,
					displayField : 'name',
					valueField : 'name',
					triggerAction : 'all',
					queryMode : 'local'
				});


		var win = Ext.create('Ext.window.Window', {
			title : 'Probe test for serial# ' + code,
			resizable : false,
			modal : true,
			width : 1240,
			height : 810,
			y : 0,
			layout : {
				type : 'border',
				padding : 0
			},
			dockedItems : [{
						xtype : 'toolbar',
						dock : 'top',
						items : []
					}],
			items : [{
						region : 'west',
						collapsible : false,
						width : 114,
						minWidth : 100,
						minHeight : 140,
						layout: 'fit',
						items : [
							{
								xtype: 'panel',
								layout : {
									type : 'vbox',
									padding : 0,
									margin : 0,
									align: 'stretch'
								},
								items : [ {
                                    xtype: 'button',
                                    scope: this,
                                    text: 'Export to excel',
                                    iconCls : 'icon-excel',
                                    handler : function() {
                                        gloApp.getController('ProbeTestPanel').exportData(tkey, code, propCombo.value);
                                    }
                                },
									propCombo
								]
							}
						]
					}, 
					{
						region : 'center',
						border : false,
						layout : 'fit',
						id: 'probeTestPanelCenterId',
						items: [{
			             	title: 'Probe test device data',
			                layout: {
							    type: 'hbox',
							    align: 'stretch'
							},
			                listeners: {
				                    activate: function(tab){
				                    	if (propCombo.value != undefined) {
					                		gloApp.getController('ProbeTestPanel').loadTestData(code, tkey, propCombo.value);
				                    	}
				                    }
				            },
			                items:  [
			                  	{
			                  		flex:3,
			              			layout: {
									    type: 'vbox',
									    align: 'stretch'
									},
			                  		items: [
			                  			{
											xtype : 'propertygrid',
											id: 'probeDeviceDataId',
											nameColumnWidth : 180,
											height: 405,
											source : {
												'(name)' : 'Device not selected'
											},
											listeners : {
												beforeedit : function() {
													return false;
												},
                                                itemclick: function( me, record, item, index, e, eOpts ) {
                                                    var chart = Ext.getCmp('dataspectrumchartid');
                                                    if (record.data.name.indexOf('Peak (nm)') === 0) {
                                                       var curr = parseInt(record.data.name.replace(/[^0-9\.]/g, ''));
                                                        var currFile = curr;
                                                        if (curr === 200) currFile = '0.2';
                                                        if (curr === 400) currFile = '0.4';
                                                        if (curr === 600) currFile = '0.6';
                                                        if (curr === 800) currFile = '0.8';
                                                        var img = code + "_" + propCombo.value + "_" + currFile + "mA." + Ext.getCmp('probeTestImageId').tag
                                                        Ext.getCmp('probeTestImageId').setSrc(img);
                                                        Ext.each(chart.series.items, function(ser) {
                                                            var c = parseInt(ser.yField.replace(/[^0-9\.]/g, ''));
                                                            ser.hideAll();
                                                            if (c === curr) {
                                                                ser.showAll();
                                                            }
                                                        });
                                                    } else {
                                                        Ext.each(chart.series.items, function(ser) {
                                                            ser.showAll();
                                                        });
                                                    }
                                                    chart.redraw();
                                                }
											}
										},
										{
											xtype: 'image',
											id: 'probeTestImageId'
										}
			                  		]
			                  	},
							    {
							    	flex:5,
							    	layout: {
									    type: 'vbox',
									    align: 'stretch'
									},
			                  		items: [{
			                  				xtype: 'chart',
			                  				flex:2,
								            id : 'datavoltagechartid',
								            store :  new Ext.data.JsonStore({
										        	fields:['volt', 'current', 'currCorrected']
												 }),	
								            legend: {
								                position: 'right'
								            },
								            axes: [{
								                type: 'Numeric',
								                position: 'left',
								                fields: ['current', 'currCorrected'],
								                title: 'Currents (exp/mA)',
								                minorTickSteps: 1,
								                grid: true
								            }, {
								                type: 'Numeric',
								                position: 'bottom',
								                fields: ['volt'],
								                title: 'Volt (V)',
								                minimum : 0,
												maximum : 6,
												majorTickSteps: 5,
												minorTickSteps: 8,
												dashSize:4, 
												grid:true
								            }],
								            series: [{
								                type: 'line',
								                axis: 'left',
								                xField: 'volt',
								                smooth: true,
								                yField: 'current',
								                markerConfig: {
								                    type: 'cross',
								                    size: 1,
								                    radius: 1,
								                    'stroke-width': 0
								                }
								           	 }, {
								                type: 'line',
								                axis: 'left',
								                smooth: true,
								                xField: 'volt',
								                yField: 'currCorrected',
								                markerConfig: {
								                    type: 'circle',
								                    size: 1,
								                    radius: 1,
								                    'stroke-width': 0
								                }
								            }]
			                  		},{
			                  				xtype: 'chart',
			                  				flex:2,
								            id : 'datacurrentchartid',
								            store :  new Ext.data.JsonStore({
										        	fields:['current', 'wpe', 'wpec','eqe','eqec']
												 }),	
								            legend: {
								                position: 'right'
								            },
								            axes: [{
								                type: 'Numeric',
								                position: 'left',
								                fields: ['wpe', 'wpec','eqe','eqec'],
								                title: 'Efficiency (%)',
								                grid: true
								            }, {
								                type: 'Numeric',
								                position: 'bottom',
								                fields: ['current'],
								                title: 'Current (mA)',
								                minimum : 0,
												maximum : 5,
												majorTickSteps: 3,
												minorTickSteps: 4,
												dashSize:4, 
												grid:true
								            }],
								            series: [{
								                type: 'line',
								                axis: 'left',
									            smooth: true,
									            xField: 'current',
								                yField: 'wpe',
								                markerConfig: {
								                    type: 'cross',
								                    size: 1,
								                    radius: 1,
								                    'stroke-width': 0
								                }
								           	 }, {
								                type: 'line',
								                axis: 'left',
								                smooth: true,
								                xField: 'current',
								                yField: 'wpec',
								                markerConfig: {
								                    type: 'circle',
								                    size: 1,
								                    radius: 1,
								                    'stroke-width': 0
								                }
								            }, {
								                type: 'line',
								                axis: 'left',
								                smooth: true,
								                xField: 'current',
								                yField: 'eqe',
								                markerConfig: {
								                    type: 'circle',
								                    size: 1,
								                    radius: 1,
								                    'stroke-width': 0
								                }
								            }, {
								                type: 'line',
								                axis: 'left',
								                smooth: true,
								                xField: 'current',
								                yField: 'eqec',
								                markerConfig: {
								                    type: 'circle',
								                    size: 1,
								                    radius: 1,
								                    'stroke-width': 0
								                }
								            }]
			                  		},{
			                  				xtype: 'chart',
			                  				flex:3,
								            id : 'dataspectrumchartid',
								            store :  new Ext.data.JsonStore({
										        	fields:['w', 'Intens_200uA', 'Intens_400uA', 'Intens_600uA', 'Intens_800uA', 'Intens_1mA', 'Intens_2mA', 'Intens_4mA', 'Intens_5mA','Intens_10mA']
												 }),	
								            legend: {
								                position: 'right'
								            },
								            axes: [{
								                type: 'Numeric',
								                position: 'left',
								                fields: ['Intens_200uA', 'Intens_400uA', 'Intens_600uA', 'Intens_800uA', 'Intens_1mA', 'Intens_2mA', 'Intens_4mA', 'Intens_5mA','Intens_10mA'],
								                title: 'Intensity (a.u.)',
                                                minimum: 0,
								                grid: true
								            }, {
								                type: 'Numeric',
								                position: 'bottom',
								                fields: ['w'],
								                title: 'Wavelength (nm)',
								                minimum : 400,
												maximum : 750,
												majorTickSteps: 6,
												minorTickSteps: 4,
												dashSize:4, 
												grid:true
								            }],
								            series: [{
                                                type: 'line',
                                                axis: 'left',
                                                smooth: true,
                                                xField: 'w',
                                                yField: 'Intens_200uA',
                                                markerConfig: {
                                                    type: 'cross',
                                                    size: 1,
                                                    radius: 1,
                                                    'stroke-width': 0
                                                }
                                            },{
                                                type: 'line',
                                                axis: 'left',
                                                smooth: true,
                                                xField: 'w',
                                                yField: 'Intens_400uA',
                                                markerConfig: {
                                                    type: 'cross',
                                                    size: 1,
                                                    radius: 1,
                                                    'stroke-width': 0
                                                }
                                            },  {
                                                type: 'line',
                                                axis: 'left',
                                                smooth: true,
                                                xField: 'w',
                                                yField: 'Intens_600uA',
                                                markerConfig: {
                                                    type: 'cross',
                                                    size: 1,
                                                    radius: 1,
                                                    'stroke-width': 0
                                                }
                                            }, {
                                                type: 'line',
                                                axis: 'left',
                                                smooth: true,
                                                xField: 'w',
                                                yField: 'Intens_800uA',
                                                markerConfig: {
                                                    type: 'cross',
                                                    size: 1,
                                                    radius: 1,
                                                    'stroke-width': 0
                                                }
                                            }, {
								                type: 'line',
								                axis: 'left',
									            smooth: true,
									            xField: 'w',
								                yField: 'Intens_1mA',
								                markerConfig: {
								                    type: 'cross',
								                    size: 1,
								                    radius: 1,
								                    'stroke-width': 0
								                }
								           	 },{
                                                type: 'line',
                                                axis: 'left',
                                                smooth: true,
                                                xField: 'w',
                                                yField: 'Intens_2mA',
                                                markerConfig: {
                                                    type: 'cross',
                                                    size: 1,
                                                    radius: 1,
                                                    'stroke-width': 0
                                                }
                                            },{
                                                type: 'line',
                                                axis: 'left',
                                                smooth: true,
                                                xField: 'w',
                                                yField: 'Intens_4mA',
                                                markerConfig: {
                                                    type: 'cross',
                                                    size: 1,
                                                    radius: 1,
                                                    'stroke-width': 0
                                                }
                                            },{
								                type: 'line',
								                axis: 'left',
									            smooth: true,
									            xField: 'w',
								                yField: 'Intens_5mA',
								                markerConfig: {
								                    type: 'cross',
								                    size: 1,
								                    radius: 1,
								                    'stroke-width': 0
								                }
								           	 },{
								                type: 'line',
								                axis: 'left',
									            smooth: true,
									            xField: 'w',
								                yField: 'Intens_10mA',
								                markerConfig: {
								                    type: 'cross',
								                    size: 1,
								                    radius: 1,
								                    'stroke-width': 0
								                }
								           	 },{
								                type: 'line',
								                axis: 'left',
									            smooth: true,
									            xField: 'w',
								                yField: 'Intens_20mA',
								                markerConfig: {
								                    type: 'cross',
								                    size: 1,
								                    radius: 1,
								                    'stroke-width': 0
								                }
								           	 }]
			                  		}]
							    }
			                ]
						}]
					}],
			buttons : [
				{
				text : 'Print',
				handler : function() {
					
					var tab =  Ext.getCmp("waferTabPanel");
					var win = tab.getActiveTab();
					var DocumentContainer = win.items.get(0).getEl().dom;
					var WindowObject = window
							.open(
									'',
									'PrintWindow',
									'width=800,height=700,top=50,left=50,toolbars=no,scrollbars=yes,status=no,resizable=yes');
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
			
			if (records[0]) {
				propCombo.setValue(records[0].data.name);
				propCombo.fireEvent('select', this, records[0].data);
			}
		});

		propCombo.on('change', function(combo, newValue, oldValue, opts) {
			if (newValue) {
				gloApp.getController('ProbeTestPanel').loadTestData(code, tkey, newValue);		
			}
		});

	    store.load();
	    
		win.show();
	},
	
	loadTestData: function(code, tkey, device) {
		
		if (device[0]) {
			
			Ext.getCmp('probeTestPanelCenterId').setLoading(true);
			
			 Ext.Ajax.request({
				method : 'GET',
				params : {
					code: code,
					tkey: tkey,
					device: device[0]
				},
				url : rootFolder + 'probeTest/device',
				success : function(response) {
					
					var obj = Ext.decode(response.responseText);
					
					var d = Ext.getCmp('probeDeviceDataId');
					d.setSource(obj.data);
					
					Ext.getCmp('probeTestImageId').setSrc(obj.image);
                    Ext.getCmp('probeTestImageId').tag = obj.extens;

					Ext.getCmp('datavoltagechartid').store.loadData(obj.Datavoltage);

                    var sef = Ext.getCmp('datacurrentchartid').axes.items[0];
                    sef.maximum = obj.DatacurrentMax ;
                    Ext.getCmp('datacurrentchartid').store.loadData(obj.Datacurrent);

					Ext.getCmp('dataspectrumchartid').store.loadData(obj.Dataspectrum);
					
					Ext.getCmp('probeTestPanelCenterId').setLoading(false);
				}
	   	 	});

		}
	},
	
	exportData : function(tkey, code, value) {

        var sData;
        var sDomain = rootFolder + 'probeTest/export';
        sData = "<p><b>EXPORTING DATA . . . . . . . </b></p>";
        sData = sData + "<form name='f12' id='f12' action='" + sDomain + "' method='post'>";
        sData = sData + "<input type='hidden' name='tkey' value='" + tkey + "' />";
        sData = sData + "<input type='hidden' name='code' value='" + code + "' />";
        sData = sData + "<input type='hidden' name='codes' value='" + value + "' />";
        sData = sData + "</form>";
        var OpenWindow = window.open("", "newwin" + Math.random() * 16);
        OpenWindow.document.write(sData);
        OpenWindow.document.f12.submit();
	}
});