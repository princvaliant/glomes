

Ext.define('glo.view.dataView.ReportInputForm', {
	extend : 'Ext.form.Panel',
	alias : 'widget.reportinputform',
	border : false,
	parentWindow : '',
	dataViewId : '',
	rName : '',
	urlExportData : '',
	minWidth : 250,
	//maxWidth : 350,
	//minHeight : 180,
	layout : 'column',
	columns: 1,
	fieldDefaults : {
		labelWidth : 135,
		labelAlign : 'right',
		width : 450,
		margin : 2
	},
	standardSubmit : false,
	bodyPadding : 6,

	initComponent : function() {

		var reportId = "";
		var url = this.urlExportData;
		
		var from = url.indexOf('uid=');
		if (from >= 0) {
			var to = url.indexOf('&');
			if (to < 0) to = url.length;
			reportId = url.substring(from + 4, to);
		}
		
		var reportName = this.rName.replace(/,/g, '_');
		reportName = reportName.replace(/ /g, '_');
		from = url.indexOf('?');
		if (from >= 0)
			url += "&rName=" + reportName
		else
			url += "?rName=" + reportName

        url += "&dataviewid=" + this.dataViewId
			
		if (reportId == "") {
			//Ext.Msg.alert('Report input', url);
			window.open(url,'resizable,scrollbars');
			
			this('window').destroy();
		}
		else {
			var inputs = [];
			
			var currentDensityStore = Ext.create('Ext.data.Store', {
			    fields: ['current', 'name'],
			    data : [
			        {"current":"100", "name":"100 A/cm2"},
			        {"current":"40", "name":"40 A/cm2"},
			        {"current":"20", "name":"20 A/cm2"},
			        {"current":"10", "name":"10 A/cm2"}
			    ]
			});
			
			var varStore = Ext.create('Ext.data.Store', {
			    fields: ['var', 'name'],
			    data : [
			        {"var":"EQE", "name":"EQE"},
			        {"var":"WPE", "name":"WPE"},
			        {"var":"Pulse Voltage", "name":"Pulse Voltage"}
			    ]
			});
			
			var rangesStore = Ext.create('Ext.data.Store', {
			    fields: ['ranges', 'name'],
			    data : [
			        {"ranges":"0", "name":"490-510; 510-520; 520-535"},
			        {"ranges":"1", "name":"440-460; 460-480; 480-500; 500-520; 520-540"}
			    ]
			});
			
			var reactorStore = Ext.create('Ext.data.Store', {
			    fields: ['reactor', 'name'],
			    data : [
			        {"reactor":"", "name":"All"},
			        {"reactor":"D1", "name":"Reactor D1"},
			        {"reactor":"S2", "name":"Reactor S2"}
			    ]
			});
			
			var expStore = Ext.create('Ext.data.Store', {
			    fields: ['exp', 'name'],
			    data : [
			        {"exp":"EXP3", "name":"EXP3"},
			        {"exp":"EXP6", "name":"EXP6"}
			    ]
			});
			
			var reactorPsStore = Ext.create('Ext.data.Store', {
			    fields: ['ps', 'name'],
			    data : [
			        {"ps":"inventory_incoming", "name":"Incoming inventory"},
			        {"ps":"stock_room", "name":"Stock room"},
			        {"ps":"parts_in_reactor", "name":"Parts in reactor"},
			        {"ps":"parts_mrb", "name":"Parts MRB"},
			        {"ps":"repair", "name":"Repair"},
			        {"ps":"clean", "name":"Clean"},
			        {"ps":"inactive_parts", "name":"Inactive parts"},
			        {"ps":"archive", "name":"Archived parts"}
			    ]
			});
			
										//xtype = 'datefield'
										//format = 'Y-m-d'
			
			if (reportId == "VD") {
							inputs = [{
								xtype : 'combo',
								fieldLabel : 'Current density',
								name : 'current',
								store : currentDensityStore,
								displayField : 'name',
								valueField : 'current',
								forceSelection : true,
								editable : false,
								multiSelect : false,
								anchor : '100%',
								value : "20",
								matchFieldWidth : true
							}, {
								xtype : 'combo',
								fieldLabel : 'Average',
								name : 'var',
								store : varStore,
								displayField : 'name',
								valueField : 'var',
								forceSelection : true,
								editable : false,
								multiSelect : false,
								anchor : '100%',
								value : "EQE",
								matchFieldWidth : true
							}, {
								xtype : 'combo',
								fieldLabel : 'For top 5',
								name : 'top5',
								store : varStore,
								displayField : 'name',
								valueField : 'var',
								forceSelection : true,
								editable : false,
								multiSelect : false,
								anchor : '100%',
								value : "EQE",
								matchFieldWidth : true
							}, {
						        xtype: 'numberfield',
						        anchor: '100%',
						        name: 'days',
						        fieldLabel: 'Number of days back',
						        allowDecimals : false,
								allowBlank : false,
						        value: 180,
						        maxValue: 730,
						        minValue: 1
							}, {
						        xtype: 'checkboxgroup',
						        fieldLabel: 'Test type(s)',
						        columns: 1,
						        vertical: true,
						        items: [
						            { boxLabel: 'Test data', name: 'tdv', inputValue: '1', checked: true },
						            { boxLabel: 'Top test', name: 'ttv', inputValue: '1', checked: true },
						            { boxLabel: 'Char test', name: 'ctv', inputValue: '1', checked: true },
						            { boxLabel: 'Full test', name: 'ftv', inputValue: '1', checked: true },
                                    { boxLabel: 'NBP test', name: 'nbp', inputValue: '1', checked: true },
                                    { boxLabel: 'NBP full', name: 'nbpf', inputValue: '1', checked: true }
						        ]
							}, {
								xtype : 'combo',
								fieldLabel : 'Peak WL ranges',
								name : 'ranges',
								store : rangesStore,
								displayField : 'name',
								valueField : 'ranges',
								forceSelection : true,
								editable : false,
								multiSelect : false,
								anchor : '100%',
								value : "1",
								matchFieldWidth : true
							}];
			}
			if (reportId == "RGB") {
							inputs = [{
						        xtype: 'numberfield',
						        anchor: '100%',
						        name: 'red',
						        fieldLabel: 'Red current [mA]',
						        allowDecimals : true,
								allowBlank : false,
								decimalPrecision : 3,
						        value: 0.011,
						        maxValue: 10,
						        minValue: 0
							}, {
						        xtype: 'numberfield',
						        anchor: '100%',
						        name: 'green',
						        fieldLabel: 'Green current [mA]',
						        allowDecimals : true,
								allowBlank : false,
								decimalPrecision : 3,
						        value: 0.052,
						        maxValue: 10,
						        minValue: 0
							}, {
						        xtype: 'numberfield',
						        anchor: '100%',
						        name: 'blue',
						        fieldLabel: 'Blue current [mA]',
						        allowDecimals : true,
								allowBlank : false,
								decimalPrecision : 3,
						        value: 0.005,
						        maxValue: 10,
						        minValue: 0
							}, {
						        xtype: 'numberfield',
						        anchor: '100%',
						        name: 'diff',
						        fieldLabel: 'Within [mA]',
						        allowDecimals : true,
								allowBlank : false,
								decimalPrecision : 3,
						        value: 0.003,
						        maxValue: 10,
						        minValue: 0
							}, {
						        xtype: 'numberfield',
						        anchor: '100%',
						        name: 'top',
						        fieldLabel: 'Top devices to report',
						        allowDecimals : false,
								allowBlank : false,
						        value: 1,
						        maxValue: 5,
						        minValue: 1
							}, {
								xtype : 'textarea',
								fieldLabel : 'Builds (comma delimited)',
								name : 'bld',
								allowBlank : true,
								anchor : '90%'
							}];
			}
			if (reportId == "PH") {
							inputs = [{
								xtype : 'combo',
								fieldLabel : 'Process step(s)',
								name : 'ps',
								store : reactorPsStore,
								displayField : 'name',
								valueField : 'ps',
								forceSelection : true,
								editable : false,
								multiSelect : true,
								allowBlank: false,
								anchor : '100%',
								value : "archive",
								matchFieldWidth : true
							}];
			}
			if (reportId == "TWD") {
							inputs = [{
								xtype : 'combo',
								fieldLabel : 'Sort by Top5 Avg',
								name : 'var',
								store : varStore,
								displayField : 'name',
								valueField : 'var',
								forceSelection : true,
								editable : false,
								multiSelect : false,
								anchor : '100%',
								value : "EQE",
								matchFieldWidth : true
							}, {
						        xtype: 'numberfield',
						        anchor: '100%',
						        name: 'top',
						        fieldLabel: 'Top devices to report',
						        allowDecimals : false,
								allowBlank : false,
						        value: 1,
						        maxValue: 5,
						        minValue: 1
							}, {
								xtype : 'combo',
								fieldLabel : 'Current density',
								name : 'current',
								store : currentDensityStore,
								displayField : 'name',
								valueField : 'current',
								forceSelection : true,
								editable : false,
								multiSelect : false,
								anchor : '100%',
								value : "20",
								matchFieldWidth : true
							}, {
								xtype : 'combo',
								fieldLabel : 'Reactor',
								name : 'reactor',
								store : reactorStore,
								displayField : 'name',
								valueField : 'reactor',
								forceSelection : true,
								editable : false,
								multiSelect : false,
								anchor : '100%',
								value : "",
								matchFieldWidth : true
							}, {
						        xtype: 'numberfield',
						        anchor: '100%',
						        name: 'days',
						        fieldLabel: 'Number of days back',
						        allowDecimals : false,
								allowBlank : false,
						        value: 180,
						        maxValue: 730,
						        minValue: 1
							}, {
						        xtype: 'checkboxgroup',
						        fieldLabel: 'Test type(s)',
						        columns: 1,
						        vertical: true,
						        items: [
						            { boxLabel: 'Test data', name: 'tdv', inputValue: '1', checked: true },
						            { boxLabel: 'Top test', name: 'ttv', inputValue: '1', checked: true },
						            { boxLabel: 'Char test', name: 'ctv', inputValue: '1', checked: true },
						            { boxLabel: 'Full test', name: 'ftv', inputValue: '1', checked: true },
                                    { boxLabel: 'NBP test', name: 'nbp', inputValue: '1', checked: true },
                                    { boxLabel: 'NBP full', name: 'nbpf', inputValue: '1', checked: true }
						        ]
							}, {
						        xtype: 'numberfield',
						        anchor: '100%',
						        name: 'peakMin',
						        fieldLabel: 'Peak wl min',
						        allowDecimals : false,
								allowBlank : false,
						        value: 500,
						        maxValue: 999,
						        minValue: 1
							}, {
						        xtype: 'numberfield',
						        anchor: '100%',
						        name: 'peakMax',
						        fieldLabel: 'Peak wl max',
						        allowDecimals : false,
								allowBlank : false,
						        value: 520,
						        maxValue: 999,
						        minValue: 1
							}, {
						        xtype: 'numberfield',
						        anchor: '100%',
						        name: 'domMin',
						        fieldLabel: 'Dominant wl min',
						        allowDecimals : false,
								allowBlank : false,
						        value: 1,
						        maxValue: 999,
						        minValue: 1
							}, {
						        xtype: 'numberfield',
						        anchor: '100%',
						        name: 'domMax',
						        fieldLabel: 'Dominant wl max',
						        allowDecimals : false,
								allowBlank : false,
						        value: 999,
						        maxValue: 999,
						        minValue: 1
							}];
			}
			if (reportId == "VY") {
							inputs = [{
						        xtype: 'textarea',
						        anchor: '100%',
						        name: 'spec',
						        fieldLabel: 'Specs (comma delimited; leave blank for list of specs)',
								allowBlank : true
							}, {
								xtype : 'checkbox',
								fieldLabel : 'Expand results by filter',
								name : 'detail',
								checked : true
							}, {
								xtype : 'combo',
								fieldLabel : 'Experiments',
								name : 'exp',
								store : expStore,
								displayField : 'name',
								valueField : 'exp',
								forceSelection : true,
								editable : false,
								multiSelect : true,
								allowBlank: false,
								anchor : '100%',
								value : "EXP6",
								matchFieldWidth : true
							}, {
						        xtype: 'numberfield',
						        anchor: '100%',
						        name: 'days',
						        fieldLabel: 'Number of days back',
						        allowDecimals : false,
								allowBlank : false,
						        value: 90,
						        maxValue: 730,
						        minValue: 1
							}, {
						        xtype: 'checkboxgroup',
						        fieldLabel: 'Test type(s)',
						        columns: 1,
						        vertical: true,
						        items: [
						            { boxLabel: 'Test data', name: 'tdv', inputValue: '1', checked: true },
						            { boxLabel: 'Top test', name: 'ttv', inputValue: '1', checked: true },
						            { boxLabel: 'Char test', name: 'ctv', inputValue: '1', checked: true },
						            { boxLabel: 'Full test', name: 'ftv', inputValue: '1', checked: true },
                                    { boxLabel: 'NBP test', name: 'nbp', inputValue: '1', checked: true },
                                    { boxLabel: 'NBP full', name: 'nbpf', inputValue: '1', checked: true }
						        ]
							}];
			}
		
			Ext.apply(this, {
				items : inputs,
				buttons : [{
					text : 'Submit',
					handler : function() {
						var form = this.up('form');
						var win = this.up('window');
	
						if (form.getForm().isValid()) {
							var params = "";
							
							var values = form.getValues();
							for (var key in values) {
								if (values[key] != '')
									params += '&' + key + '=' + values[key];
							}
	
							//Ext.Msg.alert('Report input', url + params);
							window.open(url + params,'resizable,scrollbars');
							this.up('window').destroy();
						}
					}
				}, {
					text : 'Cancel',
					handler : function() {
						this.up('window').destroy();
					}
				}]
			});
		}
		
		this.callParent(arguments);
	}

});
