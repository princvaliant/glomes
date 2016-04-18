

Ext.define('glo.controller.MeasurePanel', {
	extend : 'Ext.app.Controller',
	views : ['glo.view.measure.Chart', 'glo.view.measure.Activator'],

	show : function(code, pctg, pkey, tkey, propertyName, data) {

		var value1;
		var value2;

        if (propertyName !== undefined) {
            var props = propertyName.split('_');
            if (props.length = 2) {
                this.toSelect = data;
                this.testId = props[1];
            }
        }
		this.testType = tkey;
		this.waferId = code.split('_');
		if (this.waferId.length = 2) {
			this.waferId = this.waferId[0]; 
		}

		var storeGroups = Ext.create('glo.store.MeasureGroupTypes');
		storeGroups.load({
					params : {
						waferId : this.waferId,
						testType : this.testType
					}
				});

		this.groupTypes = Ext.create('Ext.form.field.ComboBox', {
					store : storeGroups,
					fieldLabel : 'List by',
					labelAlign : 'top',
					border : 0,
					triggerAction : 'all',
					queryMode : 'local',
					displayField : 'name',
					valueField : 'name'
				});

		this.groupValues = Ext.create('Ext.form.field.TextArea', {
					grow : true,
					flex : 3,
					fieldLabel : 'containing',
					labelAlign : 'top',
					anchor : '100%',
					text : this.waferId
				});

		var storeDevices = Ext.create('glo.store.Devices');

		this.listOfCodes = Ext.create('Ext.ux.form.MultiSelect', {
					store : storeDevices,
					labelAlign : 'top',
					minSelections : 0,
					border : 0,
					flex : 17,
					displayField : 'name',
					valueField : 'name',
					queryMode : 'local'
				});

		this.groupTypes.on('change', function(combo, newValue, oldValue, opts) {

					var record = this.groupTypes.findRecordByValue(newValue);

					this.groupValues.setValue(record.data.defaultValue);
					this.loadDevices();

				}, this);

		this.listOfCodes.on('change',
				function(combo, newValue, oldValue, opts) {
			
					if (newValue != '') {
						
						if (this.contentTab.activeTab == undefined) {
							
							this.contentTab.setActiveTab(0);
						} else {

							this.loadData();
						}
					}
				}, this);

		this.createContentTab();
		this.createWestPanel();
		this.createWindow();

		this.groupTypes.setValue('Wafer#');
		this.createTabs();
	},

	loadDevices : function() {

		var record = this.groupTypes.findRecordByValue(this.groupTypes.value);

		this.listOfCodes.store.load({
					params : {
						groupType : record.data.field,
						testType : this.testType,
                        testId: this.testId,
						items : this.groupValues.value
					},
                    scope: this,
                    callback: function(records, operation, success) {
                        if (success) {
                            this.listOfCodes.setValue(this.toSelect);
                        }
                    }
				});
	},

	loadData : function() {
		
		var tab = this.contentTab.activeTab;
        if (!tab) {
            return;
        }
		var filter = Ext.getCmp('measureFilterCombo' + tab.oid);
        var checkLogharitmic = Ext.getCmp('logharitmicCheckbox' + tab.oid);
        var checkLogX = Ext.getCmp('logharitmicCheckboxX' + tab.oid);
        var checkLogY = Ext.getCmp('logharitmicCheckboxY' + tab.oid);
        var minf = Ext.getCmp('minf' + tab.oid);
        var maxf = Ext.getCmp('maxf' + tab.oid);

        tab.createChart(this.listOfCodes.getValue(), filter, checkLogharitmic, checkLogX, checkLogY, minf, maxf);
	},

	createControls : function(record) {

		this.createWestPanel();
		this.createContentTab();
		this.createWindow();
	},

	createWestPanel : function() {

		this.westPanel = Ext.create('Ext.container.Container', {
					layout : {
						type : 'vbox',
						padding : 0,
						margin : 0,
						align : 'stretch'
					},
					items : [this.groupTypes, this.groupValues, {
								xtype : 'button',
								flex : 1,
								scope : this,
								text : 'Load devices',
								iconCls : 'icon-arrow_refresh',
								handler : function() {
									this.loadDevices();
								}
							}, this.listOfCodes, {
								xtype : 'button',
								flex : 1,
								scope : this,
								text : 'Export selected',
								iconCls : 'icon-excel',
								handler : function() {
									this.exportData(this.testType, this.listOfCodes.store);
								}
							}]
				});
	},

	createTabs : function() {

		Ext.Ajax.request({
					scope : this,
					method : 'GET',
					params : {
						testType : this.testType
					},
					url : rootFolder + 'measure/charts',
					success : function(response) {

						var obj = Ext.decode(response.responseText);
						if (obj.success == true) {

							this.contentTab.removeAll(true);

							Ext.Array.each(obj.data, function(panDef) {

										var panel = Ext.create(
												'glo.view.measure.Chart', {
													title : panDef.name,
													oid : panDef.id
												});

										this.contentTab.add(panel);

										if (panDef.filterField != '') {

											var filterCombo = Ext.create(
													'Ext.form.field.ComboBox',
													{
														id : 'measureFilterCombo'
																+ panDef.id,
														labelWidth : 110,
														displayField : 'id',
														valueField : 'id',
														forceSelection : true,
														multiSelect : false,
														width : 290,
														editable : false,
														fieldLabel : 'Filter by '
																+ panDef.filterField,
														store: [ ],
														listeners : {
															'select' : function(
																	combo,
																	newValue,
																	oldValue,
																	eOpts) {

																this.loadData();

															},
															scope : this
														}
													});
											panel.dockedItems.first()
													.add(filterCombo);
										}

                                        if (panDef.name.indexOf("VI") == 0 || panDef.name.indexOf("spectrum") >= 0) {

                                            var check = Ext.create('Ext.form.field.Checkbox', {
                                                id : 'logharitmicCheckbox' + panDef.id,
                                                name : 'Logharitmic scale',
                                                boxLabel : 'Logharitmic scale',
                                                checked : true,
                                                listeners: {
                                                    change: function(checkBox, newValue, oldValue, eOpts){
                                                        this.loadData();
                                                    }, scope:this
                                                }
                                            });

                                            panel.dockedItems.first().add(check);
                                        } else {

                                            var check1 = Ext.create('Ext.form.field.Checkbox', {
                                                id : 'logharitmicCheckboxX' + panDef.id,
                                                name : 'Log X',
                                                boxLabel : 'Log X',
                                                checked : false,
                                                listeners: {
                                                    change: function(checkBox, newValue, oldValue, eOpts){
                                                        this.loadData();
                                                    }, scope:this
                                                }
                                            });
                                            var check2 = Ext.create('Ext.form.field.Checkbox', {
                                                id : 'logharitmicCheckboxY' + panDef.id,
                                                name : 'Log Y',
                                                boxLabel : 'Log Y',
                                                checked : false,
                                                listeners: {
                                                    change: function(checkBox, newValue, oldValue, eOpts){
                                                        this.loadData();
                                                    }, scope:this
                                                }
                                            });
                                            var minf = Ext.create('Ext.form.field.Number', {
                                                id : 'minf' + panDef.id,
                                                name : 'Min',
                                                fieldLabel: 'Min Y',
                                                hideTrigger: true,
                                                labelAlign: 'right',
                                                labelWidth : 40,
                                                width: 130,
                                                stateful: true,
                                                stateId:  'MinfState' + panDef.id,
                                                listeners: {
                                                    change: function(checkBox, newValue, oldValue, eOpts){
                                                        this.loadData();
                                                    }, scope:this
                                                }
                                            });
                                            var maxf = Ext.create('Ext.form.field.Number', {
                                                id : 'maxf' + panDef.id,
                                                name : 'Max',
                                                fieldLabel: 'Max Y',
                                                labelAlign: 'right',
                                                labelWidth : 40,
                                                width: 130,
                                                hideTrigger: true,
                                                stateful: true,
                                                stateId:  'MaxfState' + panDef.id,
                                                listeners: {
                                                    change: function(checkBox, newValue, oldValue, eOpts){
                                                        this.loadData();
                                                    }, scope:this
                                                }
                                            });
                                            panel.dockedItems.first().add(check1);
                                            panel.dockedItems.first().add(check2);
                                            panel.dockedItems.first().add(minf);
                                            panel.dockedItems.first().add(maxf);
                                        }

                            }, this);

						} else {
							Ext.ux.Message.msg(
									'Error getting chart definitions', obj.msg);
						}

                        var panel = Ext.create(
                            'glo.view.measure.Activator', {
                                title : 'Activate tests',
                                testType : this.testType,
                                waferId : this.waferId
                            });

                        this.contentTab.add(panel);


					}
				});
	},

	createContentTab : function() {

		this.contentTab = Ext.create('Ext.tab.Panel', {
					scope : this,
					plain : true,
					layout : 'fit',
					defaults : {
						bodyPadding : 0
					},
					listeners : {
						'tabchange' : function(tabPanel, newCard, oldCard,
								eOpts) {

								this.loadData();
						},
						scope : this
					}
				});
	},

	createWindow : function() {

		var win = Ext.create('Ext.window.Window', {
					title : 'Data visualization for step: ' + this.testType,
					resizable : false,
					modal : true,
					width : 1460,
					height : 800,
					x : 0,
					y : 0,
					layout : {
						type : 'border',
						padding : 0
					},
					items : [{
								region : 'west',
								collapsible : false,
								width : 170,
								minWidth : 140,
								minHeight : 140,
								layout : 'fit',
								items : this.westPanel
							}, {
								region : 'center',
								border : false,
								layout : 'fit',
								items : this.contentTab
							}],
					buttons : [{
								text : 'Close',
								handler : function() {
									this.up('window').destroy();
								}
							}]
				});

		win.show();
	},

	exportData : function(tkey, codes) {
		
		window.open(rootFolder + 'measure/export?tkey=' + tkey + '&codes='
						+ this.listOfCodes.getValue(), 'resizable,scrollbars');
	}
});