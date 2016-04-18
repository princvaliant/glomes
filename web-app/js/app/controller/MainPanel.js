

Ext.define('glo.controller.MainPanel', {
	extend : 'Ext.app.Controller',

	views : ['main.Panel', 'tb.Panel'],
	models : ['TaskNamePerUserSummary', 'Unit', 'Product',
			'ProductCompany', 'Location', 'Equipment', 'Company', 'Transition',
			'Note', 'File', 'ProcessPerUserSummary', 'User', 'Operation'],
	stores : ['TaskNamePerUserSummaries', 'ProcessPerUserSummaries', 'Units',
			'ProductCompanies', 'Products', 'Locations', 'Equipments', 'Companies',
			'Transitions', 'Users', 'Notes', 'Files', 'Operations'],

	refs : [{
				ref : 'mainPanel',
				selector : 'mainpanel'
			}],
	delayedSearch: '',

	init : function() {

		this.control({
					'mainpanel' : {
						afterrender : this.onPanelRendered
					},
					'mainpanel combobox[action=searchAll]' : {
						select : this.onSearchListSelect,
						change : this.onSearchListChange
					},
					'#mainToolbarId button[action=showProcess]' : {
						click : this.onShowProcessClicked
					},
					'propertygrid' : {
						beforepropertychange : this.onPropertyChanged
					},
					'taskbookpanel' : {
						afterrender : this.onTaskbookPanelRendered
					},
					'taskbookpanel > taskbooklist > panel[xtype=grid]' : {
						beforeselect : this.onBeforeSelect,
						selectionchange : this.onListSelect,
						scope : this
					},
					'taskbookpanel > taskbooklist button[action=refresh]' : {
						click : this.onRefreshClicked
					},
					'button[name=showDiagramButtonId]' : {
						click : this.onShowDiagram
					},
					'mainpanel menuitem[action=start]' : {
						click : this.onStartClicked
					},
					'mainpanel menuitem[action=dataView]' : {
						click : this.onDataViewClicked
					},
					'mainpanel menuitem[action=spc]' : {
						click : this.onControlChartsClicked
					},
					'taskbookpanel > taskbookcontent' : {
						contentselect : this.onContentSelect
					}
				});
		
		this.delayedSearch =  new Ext.util.DelayedTask(this.onSearchListSelect2, this);
	},

	onTaskbookPanelRendered : function(o, e) {

		this.loadList(o, '', '', 'stay');
	},

	onPanelRendered : function() {

		this.loadTopBar(0);  
	},

	onShowProcessClicked : function(b, e) {
		
		this.createTbPanel(b.id, b.text);
	},

	onRefreshClicked : function() {

		var panel = this.getMainPanel().layout.getActiveItem();
		this.loadList(panel, '', '', 'stay');
	},
	
	onShowDiagram : function(b, e) {

		var panel = this.getMainPanel().layout.getActiveItem();
		var grds = Ext.ComponentQuery.query('#' + panel.id
				+ ' > taskbooklist > panel[xtype=grid]')
		var grid = grds[0];
		var sel = grid.getSelectionModel( ).getSelection()[0];
		if (sel != undefined) {
			var win = Ext.create('Ext.window.Window', {
						title : 'Diagram for current step',
						modal : true,
						plain : true,
						draggable : false,
						headerPosition : 'bottom',
						layout : 'fit',
						width : 1000,
						height : 650,
						y: 20,
						items : {
							xtype : 'box',
							width : '100%',
							height : '100%',
							autoEl : {
								tag : 'img',
								src : rootFolder
										+ 'taskbooks/diagram?processKey='
										+ sel.data.procKey
										+ '&taskKey='
										+ sel.data.taskKey
							}
						}
					});

			win.show();
		}
	},

	onStartClicked : function(b, e) {

		var startCntrl = gloApp.controllers.get('MainStartForm');
		
		startCntrl.showStartWindow(this.getMainPanel(), b.text, b.pctg, Ext.util.Format
			.lowercase(b.id))
	},

	onContentSelect : function(o, selections) {

	},
	
	onSearchListChange : function(o,  newValue,  oldValue,  eOpts ) {
		Ext.getCmp('searchAllUnitsId').store.getProxy().extraParams.hist = '';
	},

	onSearchListSelect : function(o, selections, opts) {

		var unit = selections[0].data;
		
		if (unit.id == -1) {
			Ext.getCmp('searchAllUnitsId').store.getProxy().extraParams.hist = 'true';
			Ext.getCmp('searchAllUnitsId').store.load({
				scope : Ext.getCmp('searchAllUnitsId'),
				callback : function(store, records, options) {
					this.expand();
					this.select(null);
				}
			});	

		} else {

			if (unit.pctg != '' &&  Ext.getCmp(unit.pctg) !== undefined) {
				if (Ext.getCmp("mainpanel_" + unit.pctg) == undefined) {
					
					this.createTbPanel(unit.pctg, unit.pkey);
					
					this.delayedSearch.delay(700, null, null, [o, unit]);

				} else {
					this.delayedSearch.delay(30, null, null, [o, unit]);
				}
			} else
			{
				gloApp.getController('TbContent').showDetailsWindow(unit.code);
			}
		}
	},
	
	onSearchListSelect2 : function(o, unit) {

        var isFound = false;
        var pctgPanel = Ext.getCmp(unit.pctg);
        if (pctgPanel !== undefined) {

		    this.getMainPanel().layout.setActiveItem("mainpanel_" + unit.pctg);
		    Ext.getCmp(unit.pctg).toggle();
		
            var grd = Ext.ComponentQuery.query('#mainpanel_' + unit.pctg
                    + ' > taskbooklist > panel[xtype=grid]')[0]

            grd.store.each(function(record) {
                    if (record.data.taskName == unit.tname && record.data.procKey == unit.pkey) {
                         grd.getSelectionModel().select(record.index);
                         isFound = true;
                    }
            }, this);
        }
		
		if (!isFound) {
			gloApp.getController('TbContent').showDetailsWindow(unit.code);
		} else {
			var panel = this.getMainPanel().layout.getActiveItem();
			var cmp = Ext.ComponentQuery.query('#' + panel.id
					+ ' > taskbookcontent textfield[name=searchPerStepId]')[0];
			cmp.setValue(o.lastQuery.replace("=", ""));
			Ext.getCmp('searchAllUnitsId').clearValue( );
		}
	},
	
	onPropertyChanged : function(source, recordId, value, oldValue, eOpts ) {
		

	},
	
	onBeforeSelect: function ( obj, record, index, eOpts ) {
	
		var contrl = gloApp.getController('TbContent');
		if (contrl.start === '1') {
			return false;
		}
		return true;
	},

	onListSelect : function(model, selections) {

		var sel = selections[0];
		if (sel != undefined) {
			var contrl = gloApp.getController('TbContent');
			contrl.loadContentGrid(sel.data.procDefCategory, sel.data.taskName,
					sel.data.taskKey, sel.data.procKey);
		}
	},
	
	loadTopBar : function(sel) {

		var t = Ext.getCmp('mainToolbarId');

		Ext.Ajax.request({
			scope : this,
			url : rootFolder + 'taskbooks/startProcesses',
			success : function(response) {
				var obj = Ext.decode(response.responseText);
				t.insert(0, '-');
				var btn = t.insert(1, {
							text : 'Start',
							iconCls : 'icon-start',
							menu : {
							}
						});
				Ext.Object.each(obj, function(key, value, myself) {
                    btn.menu.add({
                                text : value.procName,
                                pctg: value.pctg,
                                id : value.pkey,
                                iconCls : 'icon-start',
                                action : 'start'
                            });
                });
                Ext.getCmp('searchAllUnitsId').emptyText  = ['<search all units>'];
                Ext.getCmp('searchAllUnitsId').applyEmptyText();
			}
		});

        t.insert(2, '-');
        t.insert(3, {
            xtype : 'combo',
            store : Ext.create('glo.store.Units', {pageSize:5}),
            minChars : 3,
            width: 335,
            mode: 'remote',
            action : 'searchAll',
            id : 'searchAllUnitsId',
            displayField:'code',
            hideLabel: true,
            hideTrigger:true,
            typeAhead:true,
            listConfig: {
                loadingText: 'Searching units ...',
                valueNotFoundText : 'No matching units found.',

                // Custom rendering template for each item
                getInnerTpl: function() {
                    return '<tpl if="id!=\'-1\'&&pctg==\'\'">' +
                        '<div class="x-combo-list-item" style="line-height:130%;width:270px;margin:3 6 3 8;font-style:tahoma;font-size:11px;color:#990033" ext:qtip="{code}. ({id})"> Serial#: <b>{code}</b> <br/> Product: <b>{productCode} | {product} | {lotNumber}</b></div>' +
                        '</tpl>' +
                        '<tpl if="id!=\'-1\'&&pctg!=\'\'">' +
                        '<div class="x-combo-list-item" style="line-height:130%;width:270px;margin:3 6 3 8;font-style:tahoma;font-size:11px;" ext:qtip="{code}. ({id})"> Serial#: <b>{code}</b> <br/>Product: <b>{productCode} | {product} | {lotNumber}</b><br/>Process: <b> {pctg}</b> Step: <b>{tname}</b></div>' +
                        '</tpl>' +
                        '<tpl if="id==\'-1\'&&pctg==\'\'">' +
                        '<div class="x-combo-list-item" style="line-height:130%;width:270px;margin:9 6 9 8;font-style:tahoma;font-size:11px;"><b>NOT FOUND OR NOT ASSIGNED <br/>click here to search archive<b><br/><br/></div>' +
                        '</tpl>' +
                        '<tpl if="id==\'-1\'&&pctg!=\'\'">' +
                        '<div class="x-combo-list-item" style="line-height:130	%; width:270px;margin:9 6 9 8;font-style:tahoma;font-size:11px;"><b>NOT FOUND IN ARCHIVE<b></div>' +
                        '</tpl>';
                }
            },
            pageSize: 5
        });
        t.insert(4, '-');


		Ext.Ajax.request({
					scope : this,
					url : rootFolder + 'taskbooks/processSummary',
					success : function(response) {
						var obj = Ext.decode(response.responseText);
						var i = 0;

						Ext.Object.each(obj, function(key, value, myself) {

									var pressed = (i == sel) ? true : false;
									var title = value.categoryName + "."
											+ value.total;

									t.add({
												id : value.categoryId,
												xtype : 'button',
												text : title,
												toggleGroup : 'navGrp',
												enableToggle : true,
												pressed : pressed,
												action : 'showProcess',
												iconCls : 'icon-pctg'
											});
									t.add('-');

									if (i == 0) {
										var win = gloApp
												.getController('MainPanel');
										win.createTbPanel(value.categoryId,
												title);
									}

									i = i + 1;
								});
						t.add('-');

					}
				});

	},

	createTbPanel : function(cid, ctext) {
		
		if (!Ext.getCmp('mainpanel_' + cid)) {
			
			var tb = Ext.create('glo.view.tb.Panel', {
						id : "mainpanel_" + cid,
						category : cid
					});
			
			var tas = Ext.ComponentQuery.query('#mainpanel_' + cid	+ ' > taskbooklist')[0];
			tas.title = cid + ' steps';
					

			this.getMainPanel().add(tb);
		}
		
		this.getMainPanel().layout.setActiveItem("mainpanel_" + cid);
	},
	
	loadList : function(o, pkey, tkey, mv) {

		if (o == undefined)
			return;

		var grds = Ext.ComponentQuery.query('#' + o.id
				+ ' > taskbooklist > panel[xtype=grid]')
		var grd = grds[0];
		if (grd == undefined)
			return;

		var pl = Ext.ComponentQuery.query('#' + o.id	+ ' > taskbooklist')[0]
		pl.title = o.category + ' steps';

		var sel = grd.getSelectionModel().getSelection()[0];

		grd.store.getProxy().extraParams.category = o.category;

		grd.store.load({
					scope : grd,
					callback : function(store, records, options) {

						if (sel != undefined) {
							var select = sel.data.taskKey;
							var index = -1;
						
							this.store.each(function(record) {
								if (record.data.taskKey == sel.data.taskKey && record.data.procKey == sel.data.procKey) {
									index = record.index;
								}
							}, this);
							if (mv == 'go' || index == -1) {
								this.store.each(function(record) {
									if (record.data.taskKey == tkey && record.data.procKey == pkey) {
										index = record.index;
									}
								}, this);
							}
							if (index >= 0) {
 								this.getSelectionModel().select(index);
							}
						}

						if (this.store.data.length == 0) {
							var contrl = gloApp.getController('TbContent');
							contrl.loadContentGrid(o.category, 'NO_DATA','NO_DATA','NO_DATA', 'A');
						} else {
							if (!this.getSelectionModel().hasSelection()) {
                                if (Ext.getCmp('searchAllUnitsId') === undefined || !Ext.getCmp('searchAllUnitsId').lastQuery)
								    this.getSelectionModel().select(0);
							}
						}
					}
				});
	}
});