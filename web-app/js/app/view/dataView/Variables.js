

Ext.define('glo.view.dataView.Variables', {
			extend : 'Ext.Panel',
			alias : 'widget.dataviewvariables',
			requires : ['Ext.ux.PreviewPlugin', 'Ext.toolbar.Toolbar', 'Ext.form.field.ComboBox'],
			layout: 'fit',
			margins: '3 2 3 2',
			title: 'Available data collections',
			collapsible: true,

			initComponent : function() {
				
				 var store = Ext.create('Ext.data.TreeStore', {
				        proxy: {
				            type: 'rest',
				        	method : 'GET',
				            url:  rootFolder + 'variables/processCategory'
				        },
				        root: {
				            id: 'process'
				        },
				        folderSort: true
				    });
				 
				 var tree = Ext.create('Ext.tree.Panel', {
				        store: store,
				        name: 'treeVariablesPerProcess',
				        rootVisible:false,
			            lines:false,
			            bodyPadding: 4,
			            sortableColumns:false,
				        viewConfig: {
				            plugins: {
				                ptype: 'treeviewdragdrop',
				                ddGroup : 'GridDD',
				                enableDrop: false
				            }
				        },
				        dockedItems : [{
							xtype : 'toolbar',
							dock : 'top',
							items: [
								{
									xtype : 'combo',
									store : Ext.create('glo.store.ProcessPerUserSummaries', 
									{	
										pageSize:100
									}),
									width: 250,
									mode: 'remote',
									editable: false,
									action : 'dataViewProcessSelect',
									displayField:'categoryName',
								    triggerAction : 'all',
								    emptyText: '<select process>',
								    listConfig: {
								        loadingText: 'Searching processes ...',
								      	valueNotFoundText : 'No processes found.'
								    },
								    pageSize: 100
								}        
							]
						}]
				    });
		 

				Ext.apply(this, {
					items: 	tree
				});

				this.callParent(arguments);
			}
		});
