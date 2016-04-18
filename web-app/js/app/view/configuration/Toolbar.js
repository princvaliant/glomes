

Ext.define('glo.view.configuration.Toolbar', {
	extend : 'Ext.Panel',
	id: 'toolbarContainer',
	alias : 'widget.configurationtoolbar',
	requires : ['Ext.ux.PreviewPlugin', 'Ext.toolbar.Toolbar',
			'Ext.form.field.ComboBox'],
	border : false,
	layout : 'fit',
	margins : '3 2 3 2',

	initComponent : function() {

		this.callParent(arguments);
	}
});
