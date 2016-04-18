

Ext.define('glo.view.tb.Content', {
			extend : 'Ext.Panel',
			alias : 'widget.taskbookcontent',

			requires : ['Ext.ux.PreviewPlugin', 'Ext.toolbar.Toolbar', 'Ext.form.field.ComboBox'],

			border : false,
			layout: 'fit',
			autoScroll : false,

			initComponent : function() {

				this.addEvents('contentselect');

				Ext.apply(this, {
							dockedItems : [{
										xtype : 'toolbar',
										dock : 'top',
										items : [
											'-',
												{
											    xtype: 'textfield',
											   	name : 'searchPerStepId',
											   	emptyText: '<filter current>',
											   	enableKeyEvents: true,
											   	width: 130
											},
											'-',
											{text:""}
											
										]
									}]
						});

				this.callParent(arguments);
			}
		});
