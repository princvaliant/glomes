// JavaScript Document
Ext.define("glo.store.Units", {
	extend: 'Ext.data.Store',
	model: 'glo.model.Unit',
	autoLoad: false, 
	autoSync: true,
	pageSize: '50',
	remoteSort: true,
	remoteFilter: true,
	
	proxy : {
		type : 'rest',
		url: rootFolder + 'units',
		actionMethods : {read: 'GET', create:'POST',update:'PUT',destroy:'DELETE'},
		startParam: 'offset',
		pageParam: 'page',
		dirParam: 'order',
		limitParam: 'max',
		
		reader: {
    		type: 'json',
    		root: 'data',
    		totalProperty : 'count',
    		messageProperty: 'msg'
		},
		writer : new Ext.data.JsonWriter({
			encode : false
		})
	}
	 
	
});

