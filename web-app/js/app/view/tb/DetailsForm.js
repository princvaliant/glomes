Ext.define('glo.view.tb.DetailsForm', {
			extend : 'Ext.form.Panel',
			alias : 'widget.detailsform',
			defaultType : 'textfield',
			layout : {
				type : 'column',
				padding : '1 1 3 1'
			},
			defaults : {
				margin : '2 3 4 2'
			},
			initComponent : function() {
				this.callParent(arguments);
			},
			items : [{
						xtype : 'fieldset',
						columnWidth : .25,
						title : 'Unit',
						forceFit : true,
						collapsible : false,
						defaults : {
							labelWidth : 95,
							anchor: '100%',
							readOnly : true,
							fieldStyle : 'border:none;font-weight:bold;font-size:11px;'
						},
						items : [{
									xtype : 'textfield',
									fieldLabel : 'Serial#',
									name : 'code'

								}, {
									xtype : 'textfield',
									fieldLabel : 'Supplier',
									name : 'supplier'
								}, {
									xtype : 'textfield',
									fieldLabel : 'Supplier prod #',
									name : 'supplierProductCode'
								}]
					}, {
						xtype : 'fieldset',
						title : 'Product',
						columnWidth : .25,
						collapsible : false,
						defaults : {
							labelWidth : 95,
							anchor: '100%',
							readOnly : true,
							fieldStyle : 'border:none;font-weight:bold;font-size:11px;'
						},
						items : [{
									xtype : 'textfield',
									fieldLabel : 'Product',
									name : 'product'
								}, {
									xtype : 'textfield',
									fieldLabel : 'Product #',
									name : 'productCode'
								}, {
									xtype : 'textfield',
									fieldLabel : 'Product revision',
									name : 'productRevision'
								}]
					}, {
						xtype : 'fieldset',
						title : 'Process',
						columnWidth : .25,
						collapsible : false,
						defaults : {
							labelWidth : 95,
							anchor: '100%',
							readOnly : true,
							fieldStyle : 'border:none;font-weight:bold;font-size:11px;'
						},
						items : [{
									xtype : 'textfield',
									fieldLabel : 'Experiment #',
									name : 'experimentId'
								}, {
									xtype : 'textfield',
									fieldLabel : 'NIL Batch #',
									name : 'nil_batch_number'
								}, {
									xtype : 'textfield',
									fieldLabel : 'EPI Run #',
									name : 'runNumber'
								}]
					}, {
						xtype : 'fieldset',
						title : 'Statistics',
						columnWidth : .22,
						collapsible : false,
						defaults : {
							labelWidth : 120,
							anchor: '100%',
							readOnly : true,
							fieldStyle : 'border:none;font-weight:bold;font-size:11px;width100%;'
						},
						items : [{
									xtype : 'textfield',
									fieldLabel : 'Total steps',
									name : 'steps'
								}, {
									xtype : 'textfield',
									fieldLabel : 'Total changes',
									name : 'changes'
								}, {
									xtype : 'textfield',
									fieldLabel : 'Process violations',
									name : 'processViolations'
								}]
					}]
		});
