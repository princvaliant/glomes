// JavaScript Document
Ext.define("glo.store.ProductCompanies", {
	extend: 'Ext.data.Store',
	model: 'glo.model.ProductCompany',
	autoLoad: false, 
	autoSync: true,
	pageSize: '40',
	remoteSort: true,
	remoteFilter: true,
	
	proxy : {
		type : 'rest',
		url: rootFolder + 'productCompanies',
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

