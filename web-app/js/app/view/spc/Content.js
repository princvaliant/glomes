
Ext
		.define(
				'glo.view.spc.Content',
				{
					extend : 'Ext.panel.Panel',
					id : 'spcContentChartsId',
					alias : 'widget.spccontent',
					requires : [ 'Ext.ux.PreviewPlugin', 'Ext.toolbar.Toolbar',
							'Ext.form.field.ComboBox', 'Ext.dd.DropTarget' ],
					layout : 'fit',
					margins : '3 0 3 2',
                    minWidth: 800,
					title : 'Selected control charts',
					dockedItems : [ {
						xtype : 'toolbar',
						items : [
								{
									text : 'Refresh',
									iconCls : 'icon-arrow_refresh',
									handler : function() {
										this.ownerCt.ownerCt.fireEvent(
												'spcrefresh', this);
									}
								},
								{
									text : 'Print',
									iconCls : 'icon-printer',
									scope : this,
									handler : function() {

										var tab = Ext.getCmp('spcContentChartsId');
										var DocumentContainer = tab.items
												.get(0).getEl().dom;
										var WindowObject = window
												.open('',
														'PrintWindow',
														'width=800,height=600,top=50,left=50,toolbars=no,scrollbars=yes,status=no,resizable=yes');
                                        WindowObject.document.writeln(
                                            '<head>'+
                                            '<link rel="stylesheet" href="js/ext/resources/css/icons.css" />' +
                                            '</head>' + DocumentContainer.innerHTML);

                                        WindowObject.document.close();
										WindowObject.focus();
										WindowObject.print();



									}
								},
								{
									text : 'Clear checked',
									iconCls : 'icon-clear-selection',
									handler : function() {
										this.ownerCt.ownerCt.fireEvent(
												'clearspcselection', this);
									}
								},
								{
									text : 'Add comment',
									iconCls : 'icon-add-comment',
									handler : function() {
										this.ownerCt.ownerCt.fireEvent(
												'addspccomment', this);
									}
								} ]
					} ],
					listeners : {
						afterrender : function(comp, eOpts) {
							comp.dropTarget = Ext.create('Ext.dd.DropTarget',
									comp.el, {
										ddGroup : 'GridDD'
									});
							comp.dropTarget.notifyDrop = function(source, evt,
									data) {

								var info = data.records[0].data;
								if (info.id.toString().substring(0, 1) == "L"
										|| info.id == '') {
									return;
								}
								var spcId = comp.spcId;
								if (spcId != '' && spcId != undefined) {
									// New variable attach
									Ext.Ajax
											.request({
												scope : this,
												method : 'POST',
												params : {
													variableId : info.id
															.substring(2),
													spcId : spcId
												},
												url : rootFolder
														+ 'spc/attachVariable',
												success : function(response) {
													var obj = Ext
															.decode(response.responseText);

													if (obj.success == false) {
														Ext.Msg
																.alert(
																		'Can not attach variable',
																		obj.msg);
													} else {
														gloApp.getController(
																'SpcPanel')
																.loadCharts(true);
													}
												}
											});
								}

							};
						}
					},

					initComponent : function() {

						this.addEvents('addspccomment');
						this.addEvents('clearspcselection');

						this.callParent(arguments);
					}
				});
