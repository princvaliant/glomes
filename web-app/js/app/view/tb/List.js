

Ext.define('glo.view.tb.List', {
	extend: 'Ext.panel.Panel',
	alias: 'widget.taskbooklist',

    requires: [
       'Ext.toolbar.Toolbar',
       'Ext.grid.Panel'
    ],

	title: 'Assigned process steps',
	collapsible: true,
	animCollapse: true,
	border: false,
	margins: '0 3 0 0',
	layout: 'fit',

	initComponent: function() {
		Ext.apply(this, {
			items: {		
				    xtype: 'grid',
			        store: Ext.create('glo.store.TaskNamePerUserSummaries'),
			        multiSelect: false,
			        autoScroll : true,
					stateful: true,
				    stateId: 'stateListTaskNameGrid',
			        style: 'cursor:pointer',
			        viewConfig: {
			            emptyText: 'No steps to display'
			        },
	
			        columns: [{
			            text: 'Idx',
			            stateId : 'grd6idx',
			            flex: 15,
			            dataIndex: 'order'
			        },{
			            text: 'Proc',
			            stateId : 'grd6proc',
			            flex: 19,
			            dataIndex: 'procKey'
			        },{
			            text: 'Step name',
			            stateId : 'grd6taskname',
			            flex: 40,
			            dataIndex: 'taskName'
			        },{
			            text: 'Total',
			            stateId : 'grd6total',
			            dataIndex: 'total',
			            align: 'right',
			            flex: 17
			        }]
			},

			dockedItems: [{
				xtype: 'toolbar',
				items: [{  
	                xtype: 'button',
	                text: 'Refresh',
	                enableKeyEvents: true,
	                action: 'refresh',
	                iconCls: 'icon-arrow_refresh'
	            },
	            {
					xtype : 'button',
	            	name : 'showDiagramButtonId',
					text : 'Diagram',
					enableToggle : false,
					pressed : false,
					action : 'showDiagram',
					iconCls : 'icon-chart_organisation'
	            }]
			}]
		});

		this.callParent(arguments);
	}
});
