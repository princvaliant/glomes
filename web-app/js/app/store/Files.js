// JavaScript Document
Ext.define("glo.store.Files", {
	extend: 'Ext.data.Store',
	model: 'glo.model.File',
	autoLoad: false, 
	autoSync: true,
	pageSize: '40',
    sorters: 
     {
         property : 'date',
         direction: 'DESC'
     }
	,
	proxy : {
		type : 'rest',
		url: rootFolder + 'files',
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

