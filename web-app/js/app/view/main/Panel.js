

Ext.define('glo.view.main.Panel', {
			extend : 'Ext.panel.Panel',
			alias : 'widget.mainpanel',
			id: 'mainHeader',
			layout : 'card',
			activeItem : 0,
			bodyPadding: 0,
			bodyMargin: 0,
            constrain: true,
            constrainHeader: true,
            autoScroll: true,
			border : false,
            draggable: false,
			requires : ['Ext.toolbar.Toolbar', 'glo.view.tb.Panel', 'Ext.ModelManager'],	
			dockedItems : [{
						xtype : 'toolbar',
						id : 'mainToolbarId',
						dock : 'top',
						items: {}
					}]
		});
