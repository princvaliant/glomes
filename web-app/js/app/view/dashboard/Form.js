

Ext.define('glo.view.dashboard.Form', {
			extend : 'Ext.form.Panel',
			alias : 'widget.dashboardform',
			border : false,
			layout: 'auto',
			width: 1000,
			height: 600,
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
					name : 'gridVariablesPerDashboard',
					store : store,
					border : true,
					flex:1,
					height: 500,
					autoScroll : true,
					multiSelect : false,
					margin: 5,
					stateful : true,
					stateId : 'gridVariablesPerDashboard',
					viewConfig : {
						emptyText : 'No fields for report',
						forceFit : true,
					    plugins: {
			                ddGroup: 'gridVariablesPerDashboard',
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
								flex : 1,
								sortable : true,
								dataIndex : 'type'
							}
						]
				});
				
				var panelX = Ext.create ('Ext.form.Panel', {
				    title: 'X',
				    id : 'dashboardPanelX',
	                margin: 0,
	                padding: 5,
				    flex: 2,
				    autoScroll: true,
				    listeners: {
				        render: function(panel){
				          var dropTarget = new Ext.dd.DropTarget(panel.body,{
				            ddGroup: 'gridVariablesPerDashboard',
				            copy: false,
				            overClass: 'over',
				            notifyEnter: function(ddSource, e, data) {
					        	panel.body.stopAnimation();
					        	panel.body.highlight();
					        },
				            notifyDrop: function(dragSource, event, data){
				              
				                var selectedRecord = dragSource.dragData.records[0];	
				                gloApp.getController('DashboardPanel').addToReport('X', selectedRecord.data.dataViewId, selectedRecord.data.id, null, selectedRecord.data.title,  selectedRecord.data.type, '');
				            }
				          });
				        }
				      }
				});
				
				var panelY = Ext.create ('Ext.form.Panel', {
				    title: 'Y - LEFT',
				    id : 'dashboardPanelY',
	                margin: 0,
	                padding: 5,
				    flex: 5,
				    autoScroll: true,
				    listeners: {
				        render: function(panel){
				          var dropTarget = new Ext.dd.DropTarget(panel.body,{
				            ddGroup: 'gridVariablesPerDashboard',
				            copy: false,
				            overClass: 'over',
				            notifyEnter: function(ddSource, e, data) {
					        	panel.body.stopAnimation();
					        	panel.body.highlight();
					        },
				            notifyDrop: function(dragSource, event, data){
				            
				                var selectedRecord = dragSource.dragData.records[0];	
				                gloApp.getController('DashboardPanel').addToReport('Y', selectedRecord.data.dataViewId, selectedRecord.data.id, null,  selectedRecord.data.title, selectedRecord.data.type, '');
				            }
				          });
				        }
				      }
				});
				
				var panelZ = Ext.create ('Ext.form.Panel', {
				    title: 'Y - RIGHT',
				    id : 'dashboardPanelZ',
	                margin: 0,
	                padding: 5,
				    flex: 5,
				    autoScroll: true,
				    listeners: {
				        render: function(panel){
				          var dropTarget = new Ext.dd.DropTarget(panel.body,{
				            ddGroup: 'gridVariablesPerDashboard',
				            copy: false,
				            overClass: 'over',
				            notifyEnter: function(ddSource, e, data) {
					        	panel.body.stopAnimation();
					        	panel.body.highlight();
					        },
				            notifyDrop: function(dragSource, event, data){
				            
				                var selectedRecord = dragSource.dragData.records[0];	
				                gloApp.getController('DashboardPanel').addToReport('Z', selectedRecord.data.dataViewId, selectedRecord.data.id, null,  selectedRecord.data.title, selectedRecord.data.type, '');
				            }
				          });
				        }
				      }
				});
				
				var panelSort = Ext.create ('Ext.form.Panel', {
				    title: 'X - SORT',
				    id : 'dashboardPanelS',
	                margin: 0,
	                padding: 5,
				    flex: 2,
				    listeners: {
				        render: function(panel){
				          var dropTarget = new Ext.dd.DropTarget(panel.body,{
				            ddGroup: 'gridVariablesPerDashboard',
				            copy: false,
				            overClass: 'over',
				            notifyEnter: function(ddSource, e, data) {
					        	panel.body.stopAnimation();
					        	panel.body.highlight();
					        },
				            notifyDrop: function(dragSource, event, data){
				            
				                var selectedRecord = dragSource.dragData.records[0];	
				                gloApp.getController('DashboardPanel').addToReport('S', selectedRecord.data.dataViewId, selectedRecord.data.id, null,  selectedRecord.data.title, selectedRecord.data.type, '');
				            }
				          });
				        }
				      }
				});
				
				var panelCenter = Ext.create ('Ext.panel.Panel', {
					layout: {
		                 type: 'vbox',
		                 align:'stretch'
		            },
		            flex: 1,
		            margin: 3,
		            padding: 0,
					height: 500,
		            border: false,
					items: [panelY,panelX]					
				});
				
				var panelRight = Ext.create ('Ext.panel.Panel', {
					layout: {
		                 type: 'vbox',
		                 align:'stretch'
		            },
		            flex: 1,
		            margin: 3,
		            padding: 0,
					height: 500,
		            border: false,
					items: [panelZ,panelSort]					
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
							title : 'Drag report data fields to chart',
							align : 'stretch',
							layout: {
				                 type: 'hbox',
				                 padding:'0',
				                 align:'stretch'
				            },
							items: [grid, panelCenter, panelRight]	
						},
						{
							title : 'Specify URL for chart data',
							items: 
							[{
							    xtype: 'textfield',
							   	id : 'urlDashboardData',
							   	value: this.urlDashboardData,
							   	emptyText: '<enter URL to dashboard data>',
							   	enableKeyEvents: true,
							    listeners:{
							    	 blur: function (comp, obj, event) {
							    		 var grid = Ext.getCmp('dashboardGridId');
									  	 var rows = grid.getSelectionModel().getSelection();
							    		 gloApp.getController('DashboardPanel').urlChanged(rows[0].data.id, comp.value);
		                             } 
			                    }
							}]
						}]
					}
									
				});
	
				this.callParent(arguments);
			},

			buttons : [{
					text : 'Refresh',
					iconCls: 'icon-arrow_refresh',
					handler : function() {
						var grid = Ext.getCmp('dashboardGridId');
						var rows = grid.getSelectionModel().getSelection();
						gloApp.getController('DashboardPanel').refreshData(Ext.getCmp('panelChart' + rows[0].data.id), rows[0].data.id, 'calc');
					}
				},
			    {
				text : 'Close &  Refresh',
				handler : function() {
					var grid = Ext.getCmp('dashboardGridId');
					var rows = grid.getSelectionModel().getSelection();
					gloApp.getController('DashboardPanel').refreshData(Ext.getCmp('panelChart' + rows[0].data.id), rows[0].data.id, 'calc');
					this.up('window').destroy();
				}
			}]
		});
