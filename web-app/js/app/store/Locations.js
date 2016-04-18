// JavaScript Document
Ext.define("glo.store.Locations", {
	extend: 'Ext.data.Store',
	model: 'glo.model.Location',
	autoLoad: false, 
	autoSync: true,
	pageSize: '40',
    sorters: 
     {
         property : 'name',
         direction: 'ASC'
     }
	,
	proxy : {
		type : 'rest',
		url: rootFolder + 'locations',
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

