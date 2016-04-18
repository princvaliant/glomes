

Ext.define('glo.controller.ConfigurationPanel', {
	extend : 'Ext.app.Controller',
	views : ['glo.view.configuration.List', 'glo.view.configuration.Content','glo.view.configuration.Toolbar',
			'glo.view.configuration.Variables'],

	init : function() {

		this.control({
					'configurationlist' : {
						afterrender : this.onPanelRendered,
						configurationselect : this.onDataViewSelect,
						configurationadd : this.onDataViewAdd,
						configurationedit : this.onDataViewEdit,
						configurationdelete : this.onDataViewDelete,
						configurationduplicate : this.onDataViewDuplicate
					},
					'configurationcontent' : {
						exportdataview : this.onExport,
						uploadexceltemplate: this.onImport
					},
					'configurationlist textfield[id=searchDataViewId]' : {
						change : this.onSearch
					},
					'combobox[action=configurationProcessSelect]' : {
						select : this.onProcessListSelect
					},
					'actioncolumn#deleteVariableFromDataViewId' : {
						click : this.onDeleteVariableFromDataView
					}
				});

		this.dataViewSelect = Ext.Function.createThrottled(this.dataViewSelect,
				500, this);
	},

	onPanelRendered : function(p, e) {

			if (!mxClient.isBrowserSupported())
			{
				// Displays an error message if the browser is not supported.
				mxUtils.error('Browser is not supported!', 200, false);
			}
			else
			{
				// Creates the graph inside the given container
				if (mxClient.IS_IE)
				{
					new mxDivResizer(document.getElementById('graphContainer-body'));
				}
				var model = new mxGraphModel();
				graph = new mxGraph(document.getElementById('graphContainer-body'), model);
				
				// Workaround for Internet Explorer ignoring certain styles
				if (mxClient.IS_IE)
				{
					new mxDivResizer(document.getElementById('toolbarContainer-body'));
				}
				toolbar = new mxToolbar(document.getElementById('toolbarContainer-body'));				
				toolbar.enabled = false	

				// Enables rubberband selection
				new mxRubberband(graph);
				graph.setPanning(true);
				graph.setTooltips(true);
				// Enables new connections in the graph
				graph.setConnectable(true);
				graph.setMultigraph(false);
				// Stops editing on enter or escape keypress
				var keyHandler = new mxKeyHandler(graph);
				
				this.addVertex('editors/images/rectangle.gif', 100, 40, '');
				this.addVertex('editors/images/rounded.gif', 100, 40, 'shape=rounded');
				this.addVertex('editors/images/ellipse.gif', 40, 40, 'shape=ellipse');
				this.addVertex('editors/images/rhombus.gif', 40, 40, 'shape=rhombus');
				this.addVertex('editors/images/triangle.gif', 40, 40, 'shape=triangle');
				this.addVertex('editors/images/cylinder.gif', 40, 40, 'shape=cylinder');
				this.addVertex('editors/images/actor.gif', 30, 40, 'shape=actor');

				// Gets the default parent for inserting new cells. This
				// is normally the first child of the root (ie. layer 0).
				var parent = graph.getDefaultParent();

				// Adds cells to the model in a single step
				graph.getModel().beginUpdate();
				try
				{
					var v1 = graph.insertVertex(parent, null, 'Hello11,', 20, 20, 80, 30);
					var v2 = graph.insertVertex(parent, null, 'World!', 200, 150, 80, 30);
					var v3 = graph.insertVertex(parent, null, 'Big!', 300, 10, 180, 130);
					var e1 = graph.insertEdge(parent, null, '', v1, v2);
					var e2 = graph.insertEdge(parent, null, '', v2, v3);
				}
				finally
				{
					// Updates the display
					graph.getModel().endUpdate();
				}
			}
		
		
	},
	
	addVertex : function(icon, w, h, style)
	{
		var vertex = new mxCell(null, new mxGeometry(0, 0, w, h), style);
		vertex.setVertex(true);
	
		var img = this.addToolbarItem(graph, toolbar, vertex, icon);
		img.enabled = true;
		
		graph.getSelectionModel().addListener(mxEvent.CHANGE, function()
		{
			var tmp = graph.isSelectionEmpty();
			mxUtils.setOpacity(img, (tmp) ? 100 : 20);
			img.enabled = tmp;
		});
	},
	
	addToolbarItem : function (graph, toolbar, prototype, image)
	{
		// Function that is executed when the image is dropped on
		// the graph. The cell argument points to the cell under
		// the mousepointer if there is one.
		var funct = function(graph, evt, cell, x, y)
		{
			graph.stopEditing(false);

			var vertex = graph.getModel().cloneCell(prototype);
			vertex.geometry.x = x;
			vertex.geometry.y = y;
				
			graph.addCell(vertex);
			graph.setSelectionCell(vertex);
		}
		
		// Creates the image which is used as the drag icon (preview)
		var img = toolbar.addMode(null, image, function(evt, cell)
		{
			var pt = this.graph.getPointForEvent(evt);
			funct(graph, evt, cell, pt.x, pt.y);
		});
		
		// Disables dragging if element is disabled. This is a workaround
		// for wrong event order in IE. Following is a dummy listener that
		// is invoked as the last listener in IE.
		mxEvent.addListener(img, 'mousedown', function(evt)
		{
			// do nothing
		});
		
		// This listener is always called first before any other listener
		// in all browsers.
		mxEvent.addListener(img, 'mousedown', function(evt)
		{
			if (img.enabled == false)
			{
				mxEvent.consume(evt);
			}
		});
					
		mxUtils.makeDraggable(img, graph, funct);
		
		return img;
	},


	onSearch : function(thisField, newValue, oldValue, options) {

		var panel = thisField.ownerCt.ownerCt;
		var grid = panel.items.get(0);
		grid.store.currentPage = 1;
		grid.store.getProxy().extraParams.search = newValue;
		grid.store.load()
	},

	onDataViewAdd : function(o) {

		var grid = Ext.getCmp('dataViewGridId');
		var rows = grid.getSelectionModel().getSelection();

		var win = Ext.create('Ext.window.Window', {
					title : 'Add data view',
					resizable : true,
					modal : true,
					y : 180,
					width : 400,
					layout : 'fit',
					items : {
						id : 'dataViewFormId',
						xtype : 'dataviewform',
						parentWindow : o.ownerCt.ownerCt.ownerCt,
						rid : 0
					}
				});

		win.show();
	},

	onDataViewEdit : function(o) {

		var grid = Ext.getCmp('dataViewGridId');
		var rows = grid.getSelectionModel().getSelection();

		var win = Ext.create('Ext.window.Window', {
					title : 'Edit data view',
					resizable : true,
					modal : true,
					y : 180,
					width : 400,
					layout : 'fit',
					items : {
						id : 'dataViewFormId',
						xtype : 'dataviewform',
						parentWindow : o.ownerCt.ownerCt.ownerCt,
						rid : rows[0].data.id
					}
				});

		Ext.getCmp('dataViewFormId').loadRecord(rows[0]);

		win.show();
	},

	onDataViewDelete : function(o) {

		Ext.MessageBox.confirm('Confirm delete',
				'Delete selected dataView(s)?', function(btn) {
					if (btn == 'yes') {
						var grid = Ext.getCmp('dataViewGridId');
						var sm = Ext.getCmp('dataViewGridId')
								.getSelectionModel();
						grid.store.remove(sm.getSelection());
						if (grid.store.getCount() > 0) {
							sm.select(0);
						} else {

						}
					}
				});
	},
	
	onDataViewDuplicate : function(o) {
		
		var grid = Ext.getCmp('dataViewGridId');
		var rows = grid.getSelectionModel().getSelection();

		// Determine if input requires additional input attributes
		Ext.Ajax.request({
			scope : this,
			method : 'POST',
			params : {
				id: rows[0].data.id
			},
			url : rootFolder + 'dataViews/duplicate',
			success : function(response) {
				var obj = Ext.decode(response.responseText);
				this.refreshData();
			}
		});

	},
	
	onDataViewSelect : function(o, selections) {
		this.dataViewSelect(o, selections);
	},

	dataViewSelect : function(o, selections) {

		Ext.getCmp('removeDataView').setDisabled(!selections.length);
		Ext.getCmp('editDataView').setDisabled(!selections.length);
		Ext.getCmp('duplicateDataView').setDisabled(!selections.length);
		var grid = Ext.ComponentQuery
				.query('dataviewcontent grid[name=gridVariablesPerDataView]')[0];
		var panel = Ext.ComponentQuery.query('dataviewcontent')[0];
		if (selections[0] != undefined) {
			var sel = selections[0].data;
			grid.store.getProxy().extraParams.dataView = sel.id;
			grid.store.load();
			panel.setTitle('Fields for \'' + sel.name + '\'');
		} else {
			grid.store.load();
			panel.setTitle('No selected data views');
		}
	},

	onExport : function(o) {

		var grid = Ext.ComponentQuery
				.query('dataviewcontent grid[name=gridVariablesPerDataView]')[0];
		
		window.open(rootFolder + 'dataViews/export?dataViewId=' + grid.store.getProxy().extraParams.dataView + '&start=' + Ext.getCmp('dataViewStartId').value + '&end=' + Ext.getCmp('dataViewEndId').value + '&active=',
		'resizable,scrollbars');
	},		
	
	onImport : function(o) {
		
		var grid = Ext.ComponentQuery
				.query('dataviewcontent grid[name=gridVariablesPerDataView]')[0];
	
		var win = Ext.create('Ext.window.Window', {
					title : 'Upload excel template for current data view',
					resizable : true,
					modal : true,
					width : 408,
					x: 340,
					y :  110,
					layout : 'anchor',
					items : {
						id : 'uploadExcelFormId',
						xtype : 'uploadexcelform',
						dataViewId : grid.store.getProxy().extraParams.dataView
					}
				});

		win.show();	
	},		

	onDeleteVariableFromDataView : function(grid, cell, row, col, e) {

		var rec = grid.getStore().getAt(row);
		grid.store.remove(rec);
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

	}

});