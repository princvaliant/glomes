

Ext.define('glo.view.configuration.Content', {
	extend : 'Ext.Panel',
	id: 'graphContainer',
	alias : 'widget.configurationcontent',
	requires : ['Ext.ux.PreviewPlugin', 'Ext.toolbar.Toolbar',
			'Ext.form.field.ComboBox'],
	border : false,
	layout : 'fit',
	margins : '3 2 3 2',

	initComponent : function() {

		this.addEvents('exportdataview');
		this.addEvents('uploadexceltemplate');

		Ext.apply(this, {
					
				});

		
		this.callParent(arguments);
	}
});
