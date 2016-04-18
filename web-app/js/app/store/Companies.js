// JavaScript Document
Ext.define("glo.store.Companies", {
	extend: 'Ext.data.Store',
	model: 'glo.model.Company',
	autoLoad: false, 
	pageSize: '8',
	
	proxy : {
		type : 'rest',
		url: rootFolder + 'companies',
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
		},
		listeners : {
			exception : function(objProxy, objResponse, objOperation) {
			
			}
		}
	}
	 
	
});

