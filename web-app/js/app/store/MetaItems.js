// JavaScript Document
Ext.define("glo.store.MetaItems", {
	extend: 'Ext.data.Store',
	model: 'glo.model.MetaItem',
	autoLoad: false, 
	remoteSort: true,
	remoteFilter: true,
	
	proxy : {
		type : 'rest',
		url: rootFolder + 'metaItems',
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

