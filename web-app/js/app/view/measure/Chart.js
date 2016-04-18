

Ext.define('glo.view.measure.Chart', {
	extend : 'Ext.Panel',
	alias : 'widget.measurechart',
	requires : ['Ext.ux.PreviewPlugin', 'Ext.toolbar.Toolbar',
			'Ext.form.field.ComboBox'],
	border : false,
	layout: 'fit',
	margins : '3 2 3 2',
	oid: '',

	initComponent : function() {
		
		Ext.apply(this, {
			dockedItems : {
				xtype : 'toolbar',
					items : [ 
					{
						text : 'Print',
						iconCls: 'icon-printer',
						handler : function() {

							var chart = Ext.ComponentQuery.query('panel[oid=' + this.oid + '] chart')[0];
							var DocumentContainer = chart.getEl().dom;
							var WindowObject = window.open(	'',
											'PrintWindow',
											'width=1200,height=670,top=10,left=50,toolbars=no,scrollbars=yes,status=no,resizable=yes');
							WindowObject.document.writeln(DocumentContainer.innerHTML);
							WindowObject.document.close();
							WindowObject.focus();
							WindowObject.print();
							WindowObject.close();
						}
					}
					]
			}
		});
		
		this.callParent(arguments);
	},
	
	createChart : function(ids, filter, logharitmic, logX, logY, minf, maxf) {

		this.setLoading(true);
		
		Ext.Ajax.request({
			scope : this,
			method : 'GET',
			params : {
				ids : ids,
				oid : this.oid,
				filter : (filter != undefined ? filter.value : ''),
                logharitmic : logharitmic != undefined ? logharitmic.value : 'false',
                logX : logX != undefined ? logX.value : 'false' ,
                logY : logY != undefined ? logY.value : 'false' ,
                minf : minf != undefined ? minf.value : '' ,
                maxf : maxf != undefined ? maxf.value : ''
			},
			url : rootFolder + 'measure/chart',
			success : function(response) {

                var obj;

                Ext.suspendLayouts();

				try {
					obj = Ext.decode(response.responseText);
				} catch (e) {
					Ext.ux.Message.msg('Error getting data',
							'Invalid data format from service');
                    Ext.resumeLayouts(true);
					return;
				}

				if (obj.success == false) {
					Ext.ux.Message.msg('Error getting data', obj.msg);
                    Ext.resumeLayouts(true);
					return
				}
				
				if (filter != undefined) {
					if (filter.value == undefined) {
						filter.store.loadData(obj.filters);
						filter.setValue(obj.filterSelect);
					} 
				}
				
				var type = 'line';

				Ext.Array.each(obj.charts, function(chartDef) {

					var allFields = new Array();
					var yFields = new Array();

					var allAxes = new Array();
					var allSeries = new Array();

					if (type != 'pie') {
						Ext.Array.include(allAxes, {
									type : chartDef.xAxisType,
									position : 'bottom',
                                    decimals: chartDef.decimals,
                                    minimum : chartDef.minX,
									maximum : chartDef.maxX,
									majorTickSteps: 8,
									minorTickSteps: 9,
                                    fields : [chartDef.x],
									title : chartDef.xTitle,
									grid : true,
									label : {
										font : '13px Arial,sans-serif',
										rotate : {
											degrees : 45
										}
									}
								});
					}

					Ext.Array.include(allFields, chartDef.x);

					Ext.Array.each(chartDef.y, function(yObj) {
								Ext.Array.include(allFields, yObj.y);
								Ext.Array.include(yFields, yObj.y);
							});

					Ext.Array.each(chartDef.tip, function(tipObj) {
								Ext.Array.include(allFields, tipObj);
							});

					if (type == 'column') {
						Ext.Array.include(allSeries, {

									type : type,
									axis : 'left',
									xField : chartDef.x,
									smooth : true,
									highlight : true,
									yField : yFields,
									tips : {
										trackMouse : true,
										width : 220,
										height : 58,
										renderer : function(storeItem, item) {
												this.setTitle(item.yField + '<br/>'
													+ item.value[0] + ': '
													+ item.value[1]);
										}
									}
								});
					} else if (type == 'pie') {

						Ext.Array.each(chartDef.y, function(yObj) {
							Ext.Array.include(allSeries, {

								type : type,
								field : yObj.y,
								smooth : false,
								highlight : true,

								tips : {
									trackMouse : true,
									width : 220,
									height : 58,
									renderer : function(storeItem, item) {

										this.setTitle(storeItem.get(chartDef.x)
												+ ': ' + storeItem.get(yObj.y));
									}
								},
								label : {
									field : chartDef.x,
									display : 'rotate',
									contrast : true
								}
							});
						});

					} else {

						Ext.Array.each(chartDef.y, function(yObj) {

    						Ext.Array.include(allSeries, {

								type : type,
								axis : 'left',
								xField : chartDef.x,
								smooth : false,
								highlight : true,
								yField : yObj.y,

								tips : {
									trackMouse : true,
									width : 320,
									height : 128,
									renderer : function(storeItem, item) {
										if (storeItem.data.tip == undefined) {
											this
													.setTitle(storeItem.data[storeItem.fields.keys[0]]
															+ ': '
															+ item.value[1]);
										} else
											this
													.setTitle(storeItem.data[storeItem.fields.keys[0]]
															+ ': '
															+ item.value[1]
															+ '</br>'
															+ storeItem.data.tip);
									}
								}
							});
						});
					}

					if (type != 'pie') {
						Ext.Array.include(allAxes, {
									type : 'Numeric',
									adjustMinimumByMajorUnit: true,
									adjustMaximumByMajorUnit: true,
									constrain : false,
									minimum : chartDef.minY,
									maximum : chartDef.maxY,
									decimals: chartDef.decimals,
									position : 'left',
									fields : yFields,
									grid : true
								});
					}

					var chart = Ext.create('Ext.chart.Chart', {
								flex : 1,
								align : 'stretch',
   								store : Ext.create('Ext.data.JsonStore', {
											fields : allFields
										}),
								legend : {
									position : 'left',
									labelFont : 'bold 13px Tahoma,sans-serif',
									padding : 0,
									itemSpacing : 0
								},
								axes : allAxes,
								series : allSeries	
							});


					this.insert(0, chart);
                    Ext.resumeLayouts(true);
                    chart.store.loadData(chartDef.data);
					this.setLoading(false);
					
				}, this);

                Ext.resumeLayouts(true);

            }
		});
	}
});
