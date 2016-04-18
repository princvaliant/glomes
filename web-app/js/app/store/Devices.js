// JavaScript Document
Ext.define("glo.store.Devices", {
	extend: 'Ext.data.Store',
	model: 'glo.model.Device',
	autoLoad: false, 
	remoteSort: true,
	remoteFilter: true,
	
	proxy : {
		type : 'rest',
		url: rootFolder + 'measure/devices',
		actionMethods : {read: 'GET'},
		startParam: 'offset',
		pageParam: 'page',
		dirParam: 'order',
		limitParam: 'max',
		simpleSortMode: true,
		
		reader: {
    		type: 'json',
    		root: 'data',
    		totalProperty : 'count',
    		messageProperty: 'msg'
		}
	}
	 
	
});

