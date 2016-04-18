// JavaScript Document
Ext.define("glo.store.WorkCenters", {
	extend: 'Ext.data.Store',
	model: 'glo.model.WorkCenter',
	autoLoad: true, 
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
		url: rootFolder + 'workCenters',
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

