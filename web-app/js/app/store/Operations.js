// JavaScript Document
Ext.define("glo.store.Operations", {
	extend: 'Ext.data.Store',
	model: 'glo.model.Operation',
	autoLoad: false, 
	autoSync: false,
	pageSize: '40',
    sorters: 
     {
         property : 'name',
         direction: 'ASC'
     }
	,
	proxy : {
		type : 'rest',
		url: rootFolder + 'operations',
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

