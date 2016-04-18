
Ext.define('glo.view.dataView.Join', {
	extend : 'Ext.Panel',
	alias : 'widget.dataviewjoin',
	requires : [ 'Ext.ux.PreviewPlugin', 'Ext.toolbar.Toolbar',
			'Ext.form.field.ComboBox' ],
	border : true,
	layout : 'fit',
	height : 150,
	dataView: '',
	margins : '3 2 3 2',
	title : 'Joined reports',

	initComponent : function() {

		this.storeJoins  = new Ext.data.Store({
			fields : [ 'id', 'dataView', 'secondaryDataView', 'secondaryDataViewText', 'joinType', 'primaryVariable', 'primaryVariableText', 'secondaryVariable', 'secondaryVariableText' ],
			autoDestroy : false,
			autoLoad : false,
			autoSync : false,
			pageSize : '5',
			remoteSort : true,
			proxy : {
				type : 'rest',
				url : rootFolder + 'dataViews/joins',
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
					exception : function(proxy, response, operation) {
						if (operation) {
							this.storeJoins.rejectChanges();
							Ext.Msg.alert('Failed', operation.error);
						}
					}
				},
				writer : new Ext.data.JsonWriter({
					encode : false
				})
			}
		});
		
		this.storeSecDv =  new Ext.data.Store({
			fields : ['id', 'name', 'tag', 'owner'],
			autoDestroy : false,
			autoLoad : false,
			autoSync : false,
			proxy : {
				type : 'rest',
				url : rootFolder + 'dataViews',
				startParam : 'offset',
				pageParam : 'page',
				dirParam : 'order',
				limitParam : 'max',
				queryParam: 'search',
				reader : {
					type : 'json',
					root : 'data',
					totalProperty : 'count',
					messageProperty : 'msg'
				}
			}
		});

		var deleteColumn = {

			xtype : 'actioncolumn',
			text : '',
			width : 28,
			items : [ {
				getClass : function(v, meta, rec) {
					return 'column_delete';
				}
			} ],
			handler : function(grid, rowIndex, colIndex) {
				var rec = grid.getStore().getAt(rowIndex);

				Ext.Ajax.request({
					scope : this,
					method : 'DELETE',
					params : {
						id : rec.get('id')
					},
					url : rootFolder + 'dataViews/removeJoin/' + rec.get('id'),
					success : function(response) {

						var obj = Ext.decode(response.responseText);
						if (obj.success == false) {
							this.ownerCt.ownerCt.ownerCt.storeJoins.rejectChanges();
							Ext.Msg.alert('Can not unjoin report', obj.msg);
						} else {
							this.ownerCt.ownerCt.ownerCt.storeJoins.load();
							Ext.ux.Message.msg('Report detached', '');
						}
					}
				});
			}
		};

		var rowEditing = Ext.create('Ext.grid.plugin.RowEditing', {
			clicksToMoveEditor : 1,
			autoCancel : false,
			errorSummary : false,
			listeners: {
				edit: function(editor,e,opt){
					
					var changed = true;

					if (e.newValues.secondaryDataView === e.originalValues.secondaryDataView &&
					    e.newValues.primaryVariable === e.originalValues.primaryVariable &&
						e.newValues.secondaryVariable === e.originalValues.secondaryVariable &&
						e.newValues.joinType === e.originalValues.joinType) {
						changed = false;
					}
					
					if (changed === true) {
						
						Ext.Ajax.request({
							method : 'POST',
							params : {
								id : e.originalValues.id,
								dataView : e.newValues.dataView ? e.newValues.dataView : e.originalValues.dataView,
								secondaryDataView : e.newValues.secondaryDataView,
								primaryVariable : e.newValues.primaryVariable,
								secondaryVariable : e.newValues.secondaryVariable,
								joinType: e.newValues.joinType
							},
							url : rootFolder + 'dataViews/joins',
							success : function(response) {
								
								var obj = Ext.decode(response.responseText);
								if (obj.success == false) {
									Ext.Msg.alert('Update failed', obj.msg);
								} else {
									Ext.ux.Message.msg('Report joined', '');
									grid.store.load();
								}
							}
						});
					}				
				}
			}
		});
		
		this.storePrimary = new Ext.data.Store({
			fields : ['id', 'title'],
			autoDestroy : false,
			autoLoad : false,
			autoSync : false,
			pageSize : 15,
			proxy : {
				type : 'rest',
				url : rootFolder + 'variables/dataView' ,
				reader : {
					type : 'json',
					root : 'data'
				}
			}
		});
		
		this.storeSecondary = new Ext.data.Store({
			fields : ['id', 'title'],
			autoDestroy : false,
			autoLoad : false,
			autoSync : false,
			pageSize : 15,
			proxy : {
				type : 'rest',
				url : rootFolder + 'variables/dataView' ,
				reader : {
					type : 'json',
					root : 'data'
				}
			}
		});
	
		var grid = new Ext.grid.GridPanel({
			name : 'gridJoinsPerDataView',
			store : this.storeJoins,
			border : false,
			minHeight : 400,
			autoScroll : true,
			multiSelect : false,
			disabled : false,
			stateful : false,
			stateId : 'gridJoinsPerDataView',
			viewConfig : {
				emptyText : 'No fields for data view',
				forceFit : true
			},
			plugins : [ rowEditing ],
			columns : [ {dataIndex : 'id', hidden: true},
			     {dataIndex : 'dataView', hidden: true},
			    {
				text : "Joined report",
				flex : 6,
				sortable : false,
				dataIndex : 'secondaryDataView',
				editor : {
					
					xtype : 'combo',
					store : this.storeSecDv,
		            pageSize: 6,
					minChars : 2,
					width: 335,
					mode: 'remote',
					displayField:'name',
					valueField: 'id',
					hideLabel: true,
			        hideTrigger:false,
			        typeAhead:true,
			        allowBlank: false,
			        emptyText: '<select report to join>',
		            listConfig: {
		            	loadingText: 'Searching reports ...',
		              	valueNotFoundText : 'No matching reports found.',

		                // Custom rendering template for each item
		                getInnerTpl: function() {
		                    return '<div class="x-combo-list-item" style="width:210px;margin:3 6 3 8;font-style:tahoma;font-size:11px;color:#111111" ext:qtip="{name}. ({id})"><b>{name}</b><br/>tag: <b>{tag}</b>, owner: <b>{owner}</b></div>';
		                }
		            },
		            listeners: {
			            select: function(combo, record, index) {
			            	secondaryDataViewSelected = combo.value;
			            }
			        }    
				},
				renderer: function(value, metaData, record, rowIndex, colIndex, store) {
					return record.data.secondaryDataViewText;
				}
			}, {
				text : "Primary variable",
				flex : 3,
				sortable : false,
				dataIndex : 'primaryVariable',
				editor : {
					xtype : 'combo',
					store : this.storePrimary,
		            pageSize: 15,
		            forceSelection : true,
					displayField:'title',
					valueField: 'id',
                    queryMode: 'local',
                    allowBlank: false,
			        emptyText: '<select primary variable>'
				},
				renderer: function(value, metaData, record, rowIndex, colIndex, store) {
					return record.data.primaryVariableText;
				}
			}, {
				text : "Secondary variable",
				flex : 3,
				sortable : false,
				dataIndex : 'secondaryVariable',
				editor : {
					xtype : 'combo',
					store : this.storeSecondary,
		            pageSize: 15,
		            forceSelection : false,
					displayField:'title',
					valueField: 'id',
                    queryMode: 'local',
                    allowBlank: true,
			        emptyText: '<select secondary variable>',
			        listeners: {
 						expand: function(field, eOpts) {
 							if (this.store !== undefined) {
 								this.store.getProxy().extraParams.dataView = secondaryDataViewSelected;
 								this.store.load();
 							}
				        }
			        }    
				},
				renderer: function(value, metaData, record, rowIndex, colIndex, store) {
					return record.data.secondaryVariableText;
				}
			}, {
				text : "Join type",
				flex : 2,
				sortable : false,
				dataIndex : 'joinType',
				field : {
					xtype : 'combo',
					store : [ 'Left join' ],
					allowBlank: false
				}
			}, deleteColumn ]
		});

		Ext.apply(this, {
			items : [ grid ],
			dockedItems : {
				xtype : 'toolbar',
				id : 'dataViewJoinToolbar',
				disabled : true,
				items : [ {
					text : 'Join report',
					iconCls : 'icon-calculator_add',
					handler : function() {

						rowEditing.cancelEdit();
						grid.store.insert(0, [ {
							'joinType' : 'Left join',
							'dataView' : this.ownerCt.ownerCt.dataView
						} ]);
						rowEditing.startEdit(0, 0);
					}
				} ]
			}
		});

		this.callParent(arguments);
	}
});
