
Ext.define('glo.view.Viewport', {
    extend: 'Ext.container.Viewport',
	layout: 'fit',
	bodyPadding: 0,
    bodyMargin: 0,
    draggable: false,
	requires: [
        'glo.view.topnav.Panel'
    ],

     items: { 
     	xtype:'topnavpanel'
     }

});