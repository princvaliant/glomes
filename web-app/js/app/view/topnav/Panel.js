

Ext.define('glo.view.topnav.Panel', {
			extend : 'Ext.panel.Panel',
			alias : 'widget.topnavpanel',
			id: 'topnavHeader',
			layout : 'card',
			activeItem : 0,
			bodyPadding: 0,
			border : false,
            constrain: true,
            constrainHeader: true,
            draggable: false,
            autoScroll: true,
			requires : ['Ext.toolbar.Toolbar'],	
			dockedItems : [{
				xtype : 'toolbar',
				id : 'topnavToolbarId',
				dock : 'top',
				style: 'background: #2A2B18;height:40',
				items: [{
	                scale: 'large',
	                xtype : 'button',
	                toggleGroup : 'navTop',
	                padding: 0,
					enableToggle : true,
					pressed : true,
					tooltip: 'Process execution' ,
	                iconCls: 'icon-flowchart',
	                iconAlign: 'left',
	                action : 'main'
	            }, {
	                scale: 'large',
	                xtype : 'button',
	                padding: 0,
					toggleGroup : 'navTop',
					enableToggle : true,
					pressed : false,
					tooltip: 'Equipments status and maintenance schedule' ,
	                iconCls: 'icon-equipmentlarge',
	                iconAlign: 'left',
	                action : 'e10'
	            }, {
	                scale: 'large',
	                xtype : 'button',
	                padding: 0,
					toggleGroup : 'navTop',
					enableToggle : true,
					pressed : false,
					tooltip: 'Manage equipment runs' ,
	                iconCls: 'icon-play',
	                iconAlign: 'left',
	                action : 'dataentryrun'
	            }, '-', {
	                scale: 'large',
	                xtype : 'button',
	                padding: 0,
	                toggleGroup : 'navTop',
					enableToggle : true,
					pressed : false,
					tooltip: 'Data export',
	                iconCls: 'icon-excel32',
	                iconAlign: 'left',
	                action : 'report'
	            }, {
	                scale: 'large',
	                xtype : 'button',
	                padding: 0,
	                toggleGroup : 'navTop',
					enableToggle : true,
					pressed : false,
					tooltip: 'Statistical process control',
	                iconCls: 'icon-test',
	                iconAlign: 'left',
	                action : 'spc'
	            },
	            {
	                scale: 'large',
	                xtype : 'button',
	                padding: 0,
					toggleGroup : 'navTop',
					enableToggle : true,
					pressed : false,
					tooltip: 'Chart reports' ,
	                iconCls: 'icon-dashboard',
	                iconAlign: 'left',
	                action : 'dashboard'
	            }, 
	            '->', 
	            {
	                scale: 'large',
	                xtype : 'button',
	                padding: 0,
					toggleGroup : 'navTop',
					enableToggle : true,
					pressed : false,
					tooltip: 'System configuration' ,
	                iconCls: 'icon-configuration',
	                iconAlign: 'left',
	                action : 'home'
	            }, 
	            {
	            	xtype : 'button',
	                scale: 'large',
	                tooltip: 'Logout' ,
		            iconCls: 'icon-logout',
		            iconAlign: 'left',
		            padding: 0,
		            action : 'logout'
	            }	, {
            	    xtype: 'image',
            	    width: 160,
            	    height: 35,
            	    src:  rootFolder + 'js/ext/resources/icons/logo.png'
            	}   	
	            ]
			}]
		});
