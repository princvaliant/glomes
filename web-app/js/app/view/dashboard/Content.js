

Ext.define('glo.view.dashboard.Content', {
	extend : 'Ext.panel.Panel',
	id: 'dashboardContainer',
	alias : 'widget.dashboardcontent',
	requires : ['Ext.ux.PreviewPlugin', 'Ext.toolbar.Toolbar',
			'Ext.form.field.ComboBox'],
	border : false,
	layout : 'card',
	activeItem: 0,
	margins : '3 2 3 2',
	title: 'No reports selected',

	initComponent : function() {
		
		this.addEvents('dashboardedit');
		this.addEvents('dashboardrefresh');

		Ext.apply(this, {
			dockedItems : {
				xtype : 'toolbar',
					items : [ {
						id : 'editDashboard',
						text : 'Configure',
						iconCls : 'icon-table_edit',
						scope: this,
						handler : function() {
							this.fireEvent('dashboardedit', this);
						},
						disabled : true
					/***
					}, {
						id : 'formatDashboard',
						text : 'Format chart',
						iconCls : 'icon-table_edit',
						scope: this,
						handler : function() {
							this.fireEvent('dashboardformat', this);
						},
						disabled : true
					***/
					}, {  
						xtype : 'tbspacer',
		                width: 10
					}, {
						xtype : 'combo',
						id: 'dashboardChartTypeId',
						labelWidth: 60,
				        labelAlign : 'right',
				        inputWidth: 70,
						editable: false,
						fieldLabel: 'Chart type',
						store : ['column','line','scatter','pie','bar']
					}, {  
						xtype : 'tbspacer',
		                width: 10
					}, {
				        xtype: 'numberfield',
                        id: 'dashboardYMinId',
				        name: 'yMin',
				        fieldLabel: 'Y-LEFT  Min',
				        labelWidth : 70,
				        labelAlign : 'right',
				        inputWidth : 70,
				        emptyText : 'auto',
				        submitEmptyText : false,
						hideTrigger: true,
				        allowDecimals : true,
						allowBlank : true
					}, {
				        xtype: 'numberfield',
                        id: 'dashboardYMaxId',
				        name: 'yMax',
				        fieldLabel: 'Max',
				        labelWidth : 30,
				        labelAlign : 'right',
				        inputWidth : 70,
				        emptyText : 'auto',
				        submitEmptyText : false,
						hideTrigger: true,
				        allowDecimals : true,
						allowBlank : true
					}, {  
						xtype : 'tbspacer',
		                width: 10
					}, {
				        xtype: 'numberfield',
                        id: 'dashboardZMinId',
				        name: 'zMin',
				        fieldLabel: 'Y-RIGHT  Min',
				        labelWidth : 80,
				        labelAlign : 'right',
				        inputWidth : 70,
				        emptyText : 'auto',
				        submitEmptyText : false,
						hideTrigger: true,
				        allowDecimals : true,
						allowBlank : true
					}, {
				        xtype: 'numberfield',
                        id: 'dashboardZMaxId',
				        name: 'zMax',
				        fieldLabel: 'Max',
				        labelWidth : 30,
				        labelAlign : 'right',
				        inputWidth : 70,
				        emptyText : 'auto',
				        submitEmptyText : false,
						hideTrigger: true,
				        allowDecimals : true,
						allowBlank : true
					}, {  
						xtype : 'tbspacer',
		                width: 10
					}, {  
		                text: 'Refresh',
		                iconCls: 'icon-arrow_refresh',
		                scope: this,
		                handler : function() {
							this.fireEvent('dashboardrefresh', this);
						}
					}, {  
						xtype : 'tbspacer',
		                width: 10
					}, {
						text : 'Print',
						iconCls: 'icon-printer',
						handler : function() {

							var tab = Ext.getCmp("dashboardContainer").layout.getActiveItem();
							var DocumentContainer = tab.getEl().dom;
							var WindowObject = window.open(	'',
											'PrintWindow',
											'width=1200,height=670,top=10,left=10,toolbars=no,scrollbars=yes,status=no,resizable=yes');
							WindowObject.document.writeln(DocumentContainer.innerHTML);
							WindowObject.document.close();
							WindowObject.focus();
							WindowObject.print();
							WindowObject.close();
						}
		            }
					]
			},
			items: [
			 {      
				xtype:'panel',
				html:'<div style=\'font-size:14;padding:10;\'>Select dashboard report on the left, then configure the chart with controls above. </div>'
				
			 }]
					
		});

		
		this.callParent(arguments);
	}
});
