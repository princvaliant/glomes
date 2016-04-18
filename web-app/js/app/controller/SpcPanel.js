clickedSpcItems = new Array();

Ext.define('glo.controller.SpcPanel', {
	extend : 'Ext.app.Controller',
	views : ['glo.view.spc.List', 'glo.view.spc.Content',
			'glo.view.spc.Variables', 'glo.view.spc.Comment', 'glo.view.spc.VariableForm'],

	init : function() {

		this.control({
					'spclist' : {
						afterrender : this.onPanelRendered,
						spcselect : this.onSpcSelect,
						spcadd : this.onSpcAdd,
						spcedit : this.onSpcEdit,
						spcdelete : this.onSpcDelete,
						spcduplicate : this.onSpcDuplicate
					},
					'spccontent' : {
						exportspc : this.onExport,
						addspccomment : this.onAddSpcComment,
						clearspcselection : this.onClearSelection,
						spcrefresh : this.onSpcRefresh
					},
					'spclist textfield[id=searchspcId]' : {
						change : this.onSearch
					},
					'combobox[action=spcProcessSelect]' : {
						select : this.onProcessListSelect
					}
				});

		this.spcSelect = Ext.Function
				.createThrottled(this.spcSelect, 500, this);
	},

	onPanelRendered : function(p, e) {

		this.createGrid('spc');
	},

	onSearch : function(thisField, newValue, oldValue, options) {

		var panel = thisField.ownerCt.ownerCt;
		var grid = panel.items.get(0);
		grid.store.currentPage = 1;
		grid.store.getProxy().extraParams.search = newValue;
		grid.store.load()
	},

	onSpcAdd : function(o) {

		var grid = Ext.getCmp('spcGridId');
		var rows = grid.getSelectionModel().getSelection();

		var win = Ext.create('Ext.window.Window', {
					title : 'Add SPC',
					resizable : true,
					modal : true,
					y : 180,
					width : 400,
					layout : 'fit',
					items : {
						id : 'spcFormId',
						xtype : 'spcform',
						parentWindow : o.ownerCt.ownerCt.ownerCt,
						rid : 0
					}
				});

		win.show();
	},

	onSpcEdit : function(o) {

		var grid = Ext.getCmp('spcGridId');
		var rows = grid.getSelectionModel().getSelection();

		var win = Ext.create('Ext.window.Window', {
					title : 'Edit data view',
					resizable : true,
					modal : true,
					y : 180,
					width : 400,
					layout : 'fit',
					items : {
						id : 'spcFormId',
						xtype : 'spcform',
						parentWindow : o.ownerCt.ownerCt.ownerCt,
						rid : rows[0].data.id
					}
				});

		Ext.getCmp('spcFormId').loadRecord(rows[0]);

		win.show();
	},

	onSpcDelete : function(o) {

		Ext.MessageBox.confirm('Confirm delete', 'Delete selected spc(s)?',
				function(btn) {
					if (btn == 'yes') {
						var grid = Ext.getCmp('spcGridId');
						var sm = Ext.getCmp('spcGridId').getSelectionModel();
						grid.store.remove(sm.getSelection());
						if (grid.store.getCount() > 0) {
							sm.select(0);
						} else {

						}
					}
				});
	},

	onSpcDuplicate : function(o) {

		var grid = Ext.getCmp('spcGridId');
		var rows = grid.getSelectionModel().getSelection();

		// Determine if input requires additional input attributes
		Ext.Ajax.request({
					scope : this,
					method : 'POST',
					params : {
						id : rows[0].data.id
					},
					url : rootFolder + 'spc/duplicate',
					success : function(response) {
						var obj = Ext.decode(response.responseText);
						this.refreshData();
					}
				});

	},

	onSpcSelect : function(o, selections) {
		this.spcSelect(o, selections);
	},
	
	onAddSpcComment : function (o) {
		
		if (clickedSpcItems.length == 0)
			return;
		
		if(Ext.getCmp('spccommentformId') != undefined) {
			Ext.getCmp('spccommentformId').destroy();
		}

		var win = Ext.create('Ext.window.Window', {
					title : 'Add comment for ' + clickedSpcItems.length + ' unit(s)',
					id: 'spccommentformId',
					bodyPadding : 4,
					width: 410,
					layout : 'fit',
					items : {
						xtype : 'spccommentform',
						parentWindow : o.ownerCt.ownerCt
					}
				});

		win.show();
	},
	
	onSpcRefresh : function (o) {
		
		this.loadCharts(true);
	},
	
	onClearSelection : function (o) {
		
		for (var item in clickedSpcItems) {
			clickedSpcItems[item].setAttributes({'stroke': clickedSpcItems[item].fill}, true);
			clickedSpcItems[item].setAttributes({'stroke-width': 0}, true);
		}
		clickedSpcItems = new Array();
	},

	spcSelect : function(o, selections) {

		Ext.getCmp('removespc').setDisabled(!selections.length);
		Ext.getCmp('editspc').setDisabled(!selections.length);
		Ext.getCmp('duplicatespc').setDisabled(!selections.length);
		var panel = Ext.ComponentQuery.query('spccontent')[0];
		if (selections[0] != undefined) {
			var sel = selections[0].data;
			panel.spcId = sel.id;
			panel.setTitle('Control charts for \'' + sel.name + '\'');
		} else {
			panel.setTitle('No selected SPCs');
			panel.spcId = '';
		}
		this.loadCharts(false);
	},

	loadCharts : function(refresh, panel, date) {

        if (panel == undefined) {
		    panel = Ext.ComponentQuery.query('spccontent')[0];
        }
		
		panel.setLoading(true);

		if (panel.spcId == '') {
			panel.removeAll(true);
			return;
		}
		
		Ext.Ajax.request({
			scope : this,
			method : 'POST',
			params : {
				spcId : panel.spcId,
				refresh : refresh,
				width : panel.getWidth()
			},
			url : rootFolder + 'spc/chart',
			success : function(response) {
				var obj = Ext.decode(response.responseText);
				panel.removeAll(true);
				if (obj.success == false) {
					Ext.ux.Message.msg('Wafer chart', obj.msg);
				} else {

                    Ext.suspendLayouts();
  					var drawComponent = Ext.create('Ext.draw.Component', {
								viewBox : false,
								items : obj.charts
							});

					drawComponent.on('render', function(cmp) {

                        if (date === undefined) {

                            for (var item in cmp.surface.items) {
                                var sprites = cmp.surface.items[item];

                                if (sprites instanceof Array) {
                                    for (var i in sprites) {
                                        var sprite = sprites[i];

                                        if (sprite.name == 'spcPoint') {
                                            Ext.create('Ext.tip.ToolTip', {
                                                html: sprite.text,
                                                target: sprite.id,
                                                code: sprite.code,
                                                path: sprite.p,
                                                text: sprite.text,
                                                v: sprite.v,
                                                e: sprite.e,
                                                t: sprite.t,
                                                r: sprite.r,
                                                minWidth: 500,
                                                maxWidth: 700,
                                                trackMouse: true,
                                                showDelay: 10,
                                                listeners: {
                                                    beforeshow: function (b, e) {
                                                        Ext.Ajax.request({
                                                            scope: this,
                                                            method: 'GET',
                                                            params: {
                                                                code: b.code,
                                                                path: b.path
                                                            },
                                                            url: rootFolder + 'spc/comment',
                                                            success: function (response) {

                                                                var obj = Ext.decode(response.responseText);
                                                                var txt = b.code;
                                                                txt += "<br/>Value: " + b.v;
                                                                txt += "<br/>Exp#: " + b.e;
                                                                txt += "<br/>Run#: " + b.r;
                                                                txt += "<br/>Time: " + b.t;
                                                                if (obj.length > 0) {
                                                                    txt += "<br/>---------- NOTES ----------------";
                                                                }
                                                                for (var o in obj) {
                                                                    txt += "<br/><b>" + obj[o][0] + ":</b> " + obj[o][1];
                                                                }

                                                                this.update(txt);
                                                            }
                                                        });
                                                    }
                                                }
                                            });
                                            if (sprite instanceof Ext.draw.Sprite) {

                                                sprite.on('click', function (b, e) {

                                                    if (b.attr['stroke-width'] == 2) {
                                                        gloApp.getController('TbContent').showDetailsWindow(b.code);
                                                    }

                                                    if (!Ext.Array.contains(clickedSpcItems, b))
                                                        Ext.Array.include(clickedSpcItems, b);

                                                    if (Ext.Array.contains(clickedSpcItems, b)) {
                                                        b.setAttributes({'stroke': '#000000'}, true);
                                                        b.setAttributes({'stroke-width': 2}, true);
                                                    }
                                                });
                                            }
                                        }

                                        if (sprite.type == 'text') {
                                            if (sprite.name == 'charttitle') {
                                                sprite.addCls('pagebreak');
                                            } else {
                                                sprite.addCls('spcvtext');
                                            }
                                        }

                                        if (sprite.name == 'delete') {

                                            sprite.on('click', function (b1, e) {
                                                if (b1 != undefined)
                                                    Ext.Ajax.request({
                                                        method: 'POST',
                                                        params: {
                                                            spcVarId: b1.code
                                                        },
                                                        url: rootFolder
                                                            + 'spc/removeVariable',
                                                        success: function (response3) {
                                                            var obj3 = Ext.decode(response3.responseText);

                                                            if (obj3.success == false) {
                                                                Ext.ux.Message.msg(obj3.msg, '');
                                                            } else {
                                                                gloApp.getController('SpcPanel').loadCharts(true);
                                                            }
                                                        }
                                                    });
                                            });
                                        }
                                        if (sprite.name == 'editSpecLimit') {
                                            sprite.on('click', function (b1, e) {

                                                Ext.Ajax.request({
                                                    method: 'GET',
                                                    params: {
                                                        spcVarId: b1.code
                                                    },
                                                    url: rootFolder
                                                        + 'spc/getVariable',
                                                    success: function (response3) {
                                                        var obj3 = Ext.decode(response3.responseText);

                                                        if (obj3.success == false) {
                                                            Ext.ux.Message.msg(obj3.msg, '');
                                                        } else {

                                                            var win = Ext.create('Ext.window.Window', {
                                                                title: 'Variable "' + b1.titleText + '" properties',
                                                                width: 550,
                                                                height: 185,
                                                                bodyPadding: 4,
                                                                y: 150,
                                                                layout: 'fit',
                                                                items: {
                                                                    xtype: 'spcvariableform',
                                                                    spcVarId: b1.code,
                                                                    limits: obj3.limits,
                                                                    filters: obj3.filters,
                                                                    products: obj3.products,
                                                                    sortOrder: obj3.sortOrder
                                                                }
                                                            });

                                                            win.show();
                                                        }
                                                    }
                                                });


                                            });
                                        }
                                    }
                                }
                            }
                        }
					});

					panel.add(drawComponent);
					panel.setHeight(obj.minHeight);
                    Ext.resumeLayouts(true);

                    gloApp.fireEvent('chartloaded', panel.spcId, date, panel, drawComponent, 'spc');
				}
				
				panel.setLoading(false);
				
			}
		});
	},

	onExport : function(o) {

		var grid = Ext.ComponentQuery
				.query('spccontent grid[name=gridVariablesPerspc]')[0];

		window.open(rootFolder + 'spcs/export?spcId='
						+ grid.store.getProxy().extraParams.spc + '&start='
						+ Ext.getCmp('spcStartId').value + '&end='
						+ Ext.getCmp('spcEndId').value + '&active='
						+ Ext.getCmp('spcActiveId').value,
				'resizable,scrollbars');
	},

	onProcessListSelect : function(o, selections) {

		var tree = o.ownerCt.ownerCt;
		var sel = selections[0].data;
		tree.store.getProxy().extraParams.process = sel.categoryId;
		tree.store.load({
					scope : this,
					callback : function(records, operation, success) {

					}
				});
	},

	refreshData : function() {

        var grid = Ext.getCmp('spcGridId');
        var sel = grid.getSelectionModel().getSelection();
        grid.getSelectionModel().clearSelections();
        grid.store.load({
            callback : function(store, records, options) {
                if (sel) {
                    grid.getSelectionModel().select(sel[0].index);
                }
            }
        });
	},

	createGrid : function(domain) {

		var panel = Ext.ComponentQuery.query('#spcPanel > spclist')[0];

		Ext.Ajax.request({
			scope : panel,
			method : 'GET',
			params : {
				domain : 'spc'
			},
			url : rootFolder + 'spc/getFields',
			success : function(response) {

				var obj = Ext.decode(response.responseText);
				var columns = obj.columns;

				var store = new Ext.data.Store({
							id : 'dataStoreSpc',
							fields : obj.fields,
							autoDestroy : false,
							autoLoad : false,
							autoSync : true,
							pageSize : '25',
							remoteSort : true,
							proxy : {
								type : 'rest',
								url : rootFolder + 'spc',
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
								listeners : {
									exception : function(proxy, response,
											operation) {
										if (operation) {
											store.rejectChanges();
											Ext.Msg.alert('Failed',
													operation.error);
										}
									}
								},
								writer : new Ext.data.JsonWriter({
											encode : false
										})
							}
						});

				var pagingBar = Ext.create('Ext.PagingToolbar', {
							store : store,
							displayInfo : true,
							displayMsg : 'Displaying SPCs {0} - {1} of {2}',
							emptyMsg : "No SPCs to display"
						});

				var sm = new Ext.selection.RowModel({
							listeners : {
								scope : panel,
								selectionchange : function(selectionModel,
										selected, options) {
									this.fireEvent('spcselect', this, selected);
								}
							}
						});
						
				var publicColumn = {
					xtype : 'actioncolumn',
					dataIndex : 'isPublic',
					text : 'Pbl',
					width : 28,
					items : [{
						getClass : function(v, meta, rec) {
							if (rec.get('isPublic') == true) {
								return 'icon-bullet_tick';
							} else {
								return '';
							}
						}
					}]
				};
				
				obj.columns[2] = publicColumn

				var grid = new Ext.grid.GridPanel({
							id : 'spcGridId',
							store : store,
							defaults : {
								sortable : true
							},
							border : false,
							autoScroll : true,
							multiSelect : false,
							stateful : true,
							padding : 0,
							stateId : 'stateSpcGrid',
							bbar : pagingBar,
							selModel : sm,
							viewConfig : {
								emptyText : 'No SPCs to display',
								forceFit : true
							},
							columns : columns
						});

				grid.store.on('load', function(store, records, options) {
							
						}, grid);

				panel.removeAll(true);
				panel.add(grid);
				
				grid.store.getProxy().extraParams.search = Ext.getCmp('searchspcId').getValue();
				grid.store.load()
			}
		});

	}

});