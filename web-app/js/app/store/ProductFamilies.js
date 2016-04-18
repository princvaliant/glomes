// JavaScript Document
Ext.define("glo.store.ProductFamilies", {
	extend: 'Ext.data.Store',
	model: 'glo.model.ProductFamily',
	autoLoad: false, 
	autoSync: true,
	pageSize: '40',
	remoteSort: true,
	remoteFilter: true,
	
	proxy : {
		type : 'rest',
		url: rootFolder + 'productFamilies',
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

