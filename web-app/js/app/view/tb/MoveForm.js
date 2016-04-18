

Ext.define('glo.view.tb.MoveForm', {
			extend : 'Ext.form.Panel',
			alias : 'widget.moveform',
			border : false,
			rows : '',
			parentWindow : '',
			transition: '',
			isEngineering: '',
			processId: '',
			processCategoryEng: '',
			processKeyEng: '',
			taskKeyEng: '',
			minWidth : 500,
			minHeight : 201,
			layout : 'anchor',
			defaultType: 'textfield',
			fieldDefaults : {
				labelWidth : 85
			},
			standardSubmit : false,
			bodyPadding : 9,

			initComponent : function() {
				Ext.apply(this, {
							items: [{
										xtype : 'textarea',
										fieldLabel : 'Comment',
										name : 'note',
										anchor : '100%'
									}, {
										xtype : 'numberfield',
										fieldLabel : 'Priority',
										name : 'prior',
										value : 50,
										maxValue : 100,
										minValue : 1,
										anchor : '40%'
									}]
						});
				
				this.callParent(arguments);
			},

			buttons : [{
				xtype : 'checkboxfield',
				name: 'printLabelAfterMove',
				fieldLabel : 'Print label after move',
				labelWidth : 140
			}, {
				xtype: 'tbspacer',
				width: 30
			}, {
				text : 'Move and stay',
				handler : function() {
					var form = this.up('form');
					var win = this.up('window');
					var printLabel = "no";
                    var printLabelButton = Ext.ComponentQuery.query("moveform  checkboxfield[name=printLabelAfterMove]")[0];
					if (printLabelButton.value == true) printLabel = "yes";

					if (form.getForm().isValid()) {

						var aResult = Ext.Array.map(form.rows, function(node) {
							return {
								transition : form.transition,
								id : node.data.id,
								dataOk : node.data.ok
							};
						});
						
						var units = Ext.encode(aResult);

						form.submit({
									scope : this,
									method : 'POST',
									url : rootFolder + 'units/move',
									submitEmptyText : false,
									waitMsg : 'Moving units ...',
									params : {
										isEngineering: form.isEngineering,
										processCategoryEng: form.processCategoryEng,
										processKeyEng: form.processKeyEng,
										taskKeyEng: form.taskKeyEng,
										taskKeySource: form.taskKeySource,
										units : units,
										printLabel : printLabel
									},

									success : function(frm, action) {
										
										if (action.result.showMove == '1') {
											
											win.destroy();
											
											gloApp.getController('TbContent').processRegularTransition('', action.result.pctg,  action.result.pkey,
													action.result.tkey, form.rows, form.parentWindow, true);
														

										} else {
											
											Ext.ux.Message.msg("Unit(s) moved successfully.", "");
			
											gloApp.getController('MainPanel')
												.loadList(form.parentWindow, form.processKeyEng, form.taskKeyEng, 'stay');
											
											win.destroy();
											
											if (action.result.showMove == '2') {
												gloApp.getController('TbContent').processPrintBarcode('', action.result.pctg,  action.result.pkey,
													action.result.tkey, form.rows, form.parentWindow);
											}
										}


									},
									failure : function(frm, action) {

										Ext.Msg.alert('Failed',
												action.result.msg);
									}
								});
					}
				}
			}, {
				text : 'Move and go there',
				handler : function() {
					var form = this.up('form');
					var win = this.up('window');
					var printLabel = "no";
                    var printLabelButton = Ext.ComponentQuery.query("moveform  checkboxfield[name=printLabelAfterMove]")[0];
                    if (printLabelButton.value == true) printLabel = "yes";

					if (form.getForm().isValid()) {

						var aResult = Ext.Array.map(form.rows, function(node) {
							return {
								transition : form.transition,
								id : node.data.id,
								dataOk : node.data.ok
							};
						});
						var units = Ext.encode(aResult);

						form.submit({
									scope : this,
									method : 'POST',
									url : rootFolder + 'units/move',
									submitEmptyText : false,
									waitMsg : 'Moving units ...',
									params : {
										isEngineering: form.isEngineering,
										processCategoryEng: form.processCategoryEng,
										processKeyEng: form.processKeyEng,
										taskKeyEng: form.taskKeyEng,
										taskKeySource: form.taskKeySource,
										units : units,
										printLabel : printLabel
									},

									success : function(frm, action) {
										if (action.result.showMove == '1') {

    										win.destroy();
											
											gloApp.getController('TbContent').processRegularTransition('', action.result.pctg,  action.result.pkey,
													action.result.tkey, form.rows, form.parentWindow, true);
														

										} else {
											
											Ext.ux.Message.msg("Unit(s) moved successfully.", "");
			
											gloApp.getController('MainPanel').loadList(form.parentWindow, form.processKeyEng, form.taskKeyEng, 'go');
											
											win.destroy();

											if (action.result.showMove == '2') {
												gloApp.getController('TbContent').processPrintBarcode('', action.result.pctg,  action.result.pkey,
													action.result.tkey, form.rows, form.parentWindow);
											}
										}
									},
									failure : function(frm, action) {

										Ext.Msg.alert('Failed',
												action.result.msg);
									}
								});
					}
				}
			}, {
				text : 'Cancel',
				handler : function() {
					this.up('window').destroy();
				}
			}]
		});
