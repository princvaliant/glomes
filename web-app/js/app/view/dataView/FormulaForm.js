

Ext.define('glo.view.dataView.FormulaForm', {
			extend : 'Ext.form.Panel',
			alias : 'widget.formulaform',
			border : false,
			autoScroll: true,
			config : {
				store: '',
				dataViewVariableId: '',
				formula: '',
				formulaTitle: ''
			},
			layout: {
                 type: 'hbox',
                 padding:'0',
                 align:'stretch'
            },
			dataViewId: '',
			defaultType: 'textfield',
			fieldDefaults : {
					labelWidth : 125,
					labelAlign : 'right',
					width : 875	,
					margin : 2
			},
			standardSubmit : false,
			initComponent : function() {
						

				var grid = new Ext.grid.GridPanel({
					name : 'gridVariablesFormula',
					store : this.store,
					border : true,
					flex:3,
					autoScroll : true,
					multiSelect : false,
					margin: 5,
					height: 500,
					stateful : true,
					stateId : 'gridVariablesFormula',
					viewConfig : {
						emptyText : 'No fields for report',
						forceFit : true,
					    plugins: {
			                ddGroup: 'gridVariablesFormula',
			                ptype: 'gridviewdragdrop',
			                enableDrop: false
			            }
					},
					columns : [{
								text : "Name",
								flex : 2,
								sortable : true,
								dataIndex : 'name'
							}, {
								text : "Path",
								flex : 4,
								sortable : true,
								dataIndex : 'path'
							}, {
								text : "Title",
								flex : 3,
								sortable : true,
								dataIndex : 'title'
							}, {
								text : "Type",
								flex : 1,
								sortable : true,
								dataIndex : 'type'
							}
						]
				});
				
				var panelEdit = Ext.create ('Ext.form.Panel', {
				    title: 'Formula for: ' + this.formulaTitle,
	                margin: 3,
	                border: false,
	                padding: 1,
	                layout: 'fit',
				    flex: 2,
				    listeners: {
				        render: function(panel){
				          var dropTarget = new Ext.dd.DropTarget(panel.body,{
				            ddGroup: 'gridVariablesFormula',
				            copy: false,
				            overClass: 'over',
				            notifyEnter: function(ddSource, e, data) {
					        	panel.body.stopAnimation();
					        	panel.body.highlight();
					        },
				            notifyDrop: function(dragSource, event, data){
				            	
				            	 panel.body.highlight("#00FF00");
					              var selectedRecord = dragSource.dragData.records[0];
					              var value = Ext.getCmp('dataViewFormulaEditId').getValue();
					              var step = selectedRecord.data.step;
					              var name = selectedRecord.data.name;
					              if (!(selectedRecord.data.step == undefined || selectedRecord.data.step == null || selectedRecord.data.step == '')) {
					                	step = step + '_';
					              }
					              if (step == 'measuredata_') {
					            	  step = '';
					              }
					              if (step == null && name == '[Formula]') {
					            	  step = '';
					            	  name = selectedRecord.data.title.replace(/\ /g,'_');
					              }
					              
					              Ext.getCmp('dataViewFormulaEditId').setValue(value + ' ' + step + name);
				            }
				          });
				        }
				      },
				      items: [{
				        xtype     : 'textareafield',
				        grow      : false,
				        margin : 0,
				        id      : 'dataViewFormulaEditId',
				        anchor    : '100%',
				        value:  this.formula,
				        blankText : '<Drag and drop row from the left here>'
				    }]
				});
						
				Ext.apply(this, {
					items: [grid, panelEdit]					
				});
	
				this.callParent(arguments);

			},

			buttons : [
			    {
				text : 'Save',
				handler : function() {
					
					var dataViewVariableId = this.up('form').dataViewVariableId;
					var formula =  Ext.getCmp('dataViewFormulaEditId').getValue();
					   
                    Ext.Ajax.request({
						scope : this,
						method : 'POST',
						params : {
							dataViewVariableId : dataViewVariableId,
							formula : formula
						},
						url : rootFolder + 'dataViews/saveFormulaVariable',
						success : function(response) {
							
							var obj = Ext.decode(response.responseText);

							if (obj.success == false) {
								Ext.Msg.alert(
										'No valid formula',
										obj.msg);
							} else {
								Ext.ux.Message.msg('Valid formula saved','');
								this.up('form').store.load();
							}
						}
					});
				}},   {
				text : 'Close',
				handler : function() {
					this.up('window').destroy();
				}
			}]
		});
