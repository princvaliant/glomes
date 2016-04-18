

Ext.define('glo.view.measure.Content', {
	extend : 'Ext.Panel',
	alias : 'widget.chartcontent',
	requires : ['Ext.ux.PreviewPlugin', 'Ext.toolbar.Toolbar',
			'Ext.form.field.ComboBox'],
	border : false,
	layout : 'card',
	activeItem: 0,
	margins : '3 2 3 2',
	title: 'No reports selected',

	initComponent : function() {
		
		this.addEvents('chartedit');
		this.addEvents('chartrefresh');

		Ext.apply(this, {
			dockedItems : {
				xtype : 'toolbar',
					items : [ {
						text : 'Configure',
						iconCls : 'icon-table_edit',
						scope: this,
						handler : function() {
							this.fireEvent('chartedit', this);
						},
						disabled : true
					},
					{
						text : 'Print',
						iconCls: 'icon-printer',
						handler : function() {

							var tab = Ext.getCmp("chartContainer").layout.getActiveItem();
							var DocumentContainer = tab.getEl().dom;
							var WindowObject = window.open(	'',
											'PrintWindow',
											'width=1200,height=670,top=10,left=50,toolbars=no,scrollbars=yes,status=no,resizable=yes');
							WindowObject.document.writeln(DocumentContainer.innerHTML);
							WindowObject.document.close();
							WindowObject.focus();
							WindowObject.print();
							WindowObject.close();
						}
					},  {
						xtype : 'combo',
						labelWidth:70,
						width: 150,
						editable: false,
						fieldLabel: 'Chart type',
						store : ['column','line','scatter','pie']
					},{  
		                text: 'Refresh',
		                iconCls: 'icon-arrow_refresh',
		                scope: this,
		                handler : function() {
							this.fireEvent('dashboardrefresh', this);
						}
		            }
					]
			},
			items: [
			 {      
				xtype:'panel',
				html:'<div style=\'font-size:16;padding:10;\'>Reports [Beta] are here. Click on the report left. </div>'
				
			 }]
					
		});

		
		this.callParent(arguments);
	}
});
