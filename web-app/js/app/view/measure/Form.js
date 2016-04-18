

Ext.define('glo.view.measure.Form', {
			extend : 'Ext.form.Panel',
			alias : 'widget.chartform',
			border : false,
			layout: 'fit',
			dataViewId: '',
			urlDashboardData: '',
			defaultType: 'textfield',
			fieldDefaults : {
					labelWidth : 125,
					labelAlign : 'right',
					width : 875	,
					margin : 2
			},
			standardSubmit : false,
			initComponent : function() {
						
				var store = new Ext.data.Store({
					fields : ['id', 'variableId', 'dataViewId', 'idx', 'name', 'step',
							'title', 'filter', 'isFormula', 'type', 'dir'],
					autoDestroy : false,
					autoLoad : true,
					autoSync : true,
					pageSize : '25',
					remoteSort : true,
					proxy : {
						type : 'rest',
						url : rootFolder + 'variables/dataView',
						startParam : 'offset',
						pageParam : 'page',
						dirParam : 'order',
						limitParam : 'max',
						extraParams : {
							'dataView': this.dataViewId
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
				
				var grid = new Ext.grid.GridPanel({
					name : 'gridVariablesPerChart',
					store : store,
					border : true,
					flex:1,
					height: 600,
					autoScroll : true,
					multiSelect : false,
					margin: 5,
					stateful : true,
					stateId : 'gridVariablesPerChart',
					viewConfig : {
						emptyText : 'No fields for report',
						forceFit : true,
					    plugins: {
			                ddGroup: 'gridVariablesPerChart',
			                ptype: 'gridviewdragdrop',
			                enableDrop: false
			            }
					},
					columns : [{
								text : "Title",
								flex : 5,
								sortable : true,
								dataIndex : 'title',
								field : {
									xtype : 'textfield'
								}
							}, {
								text : "Type",
								flex : 2,
								sortable : true,
								dataIndex : 'type'
							}
						]
				});
				
				var panelX = Ext.create ('Ext.form.Panel', {
				    title: 'X',
	                margin: 0,
	                padding: 5,
				    flex: 3,
				    autoScroll: true,
				    listeners: {
				        render: function(panel){
				          var dropTarget = new Ext.dd.DropTarget(panel.body,{
				            ddGroup: 'gridVariablesPerChart',
				            copy: false,
				            overClass: 'over',
				            notifyEnter: function(ddSource, e, data) {
					        	panel.body.stopAnimation();
					        	panel.body.highlight();
					        },
				            notifyDrop: function(dragSource, event, data){
				              
				                var selectedRecord = dragSource.dragData.records[0];	
				                gloApp.getController('ChartPanel').addToReport('X', selectedRecord.data.dataViewId, selectedRecord.data.id, null, selectedRecord.data.title,  selectedRecord.data.type, '');
				            }
				          });
				        }
				      }
				});
				
				var panelY = Ext.create ('Ext.form.Panel', {
				    title: 'Y',
	                margin: 0,
	                padding: 5,
				    flex: 5,
				    listeners: {
				        render: function(panel){
				          var dropTarget = new Ext.dd.DropTarget(panel.body,{
				            ddGroup: 'gridVariablesPerChart',
				            copy: false,
				            overClass: 'over',
				            notifyEnter: function(ddSource, e, data) {
					        	panel.body.stopAnimation();
					        	panel.body.highlight();
					        },
				            notifyDrop: function(dragSource, event, data){
				            
				                var selectedRecord = dragSource.dragData.records[0];	
				                gloApp.getController('ChartPanel').addToReport('Y', selectedRecord.data.dataViewId, selectedRecord.data.id, null,  selectedRecord.data.title, selectedRecord.data.type, '');
				            }
				          });
				        }
				      }
				});
				
				var panelSort = Ext.create ('Ext.form.Panel', {
				    title: 'Sort',
				    id : 'chartPanelS',
	                margin: 0,
	                padding: 5,
				    flex: 2,
				    listeners: {
				        render: function(panel){
				          var dropTarget = new Ext.dd.DropTarget(panel.body,{
				            ddGroup: 'gridVariablesPerChart',
				            copy: false,
				            overClass: 'over',
				            notifyEnter: function(ddSource, e, data) {
					        	panel.body.stopAnimation();
					        	panel.body.highlight();
					        },
				            notifyDrop: function(dragSource, event, data){
				            
				                var selectedRecord = dragSource.dragData.records[0];	
				                gloApp.getController('ChartPanel').addToReport('S', selectedRecord.data.dataViewId, selectedRecord.data.id, null,  selectedRecord.data.title, selectedRecord.data.type, '');
				            }
				          });
				        }
				      }
				});
				
				var panelRight = Ext.create ('Ext.panel.Panel', {
					layout: {
		                 type: 'vbox',
		                 align:'stretch'
		            },
		            flex: 1,
		            margin: 3,
		            padding: 0,
		            border: false,
					items: [panelX,panelY,panelSort]					
				});
				
				
				Ext.apply(this, {
					items : {
						xtype : 'tabpanel',
						activeTab : 0,
						plain : true,
						layout : 'fit',
						defaults : {
							bodyPadding : 5
						},
						items : [
						{
							title : 'Pivot data',
							align : 'stretch',
							layout: {
				                 type: 'hbox',
				                 padding:'0',
				                 align:'stretch'
				            },
							items: [grid, panelRight]	
						}]
					}
									
				});
	
				this.callParent(arguments);
			},

			buttons : [{
					text : 'Refresh',
					iconCls: 'icon-arrow_refresh',
					handler : function() {
						var grid = Ext.getCmp('chartGridId');
						var rows = grid.getSelectionModel().getSelection();
						gloApp.getController('ChartPanel').refreshData(Ext.getCmp('panelChart' + rows[0].data.id), rows[0].data.id, true);
					}
				},
			    {
				text : 'Close &  Refresh',
				handler : function() {
					var grid = Ext.getCmp('chartGridId');
					var rows = grid.getSelectionModel().getSelection();
					gloApp.getController('ChartPanel').refreshData(Ext.getCmp('panelChart' + rows[0].data.id), rows[0].data.id, true);
					this.up('window').destroy();
				}
			}]
		});
