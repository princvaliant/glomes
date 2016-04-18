// JavaScript Document
Ext.define("glo.store.Users", {
	extend: 'Ext.data.Store',
	model: 'glo.model.User',
	autoLoad: false, 
	pageSize: '40',
	
	proxy : {
		type : 'rest',
		url: rootFolder + 'users',
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

