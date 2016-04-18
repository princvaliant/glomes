

Ext.define('glo.view.spc.Variables', {
			extend : 'Ext.Panel',
			alias : 'widget.spcvariables',
			requires : ['Ext.ux.PreviewPlugin', 'Ext.toolbar.Toolbar', 'Ext.form.field.ComboBox'],
			layout: 'fit',
			margins: '0 0 0 0',
			collapsible: true,
            collapsed: true,
			title: 'Available data collections',

			initComponent : function() {
				
				 var store = Ext.create('Ext.data.TreeStore', {
				        proxy: {
				            type: 'rest',
				        	method : 'GET',
				            url:  rootFolder + 'variables/processCategorySpc'
				        },
				        root: {
				            id: 'process'
				        },
				        folderSort: true
				    });
				 
				 var tree = Ext.create('Ext.tree.Panel', {
				        store: store,
				        name: 'treeVariablesPerProcessSpc',
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
									store : Ext.create('glo.store.ProcessPerUserSummaries', {pageSize:10}),
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
								    pageSize: 10
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
