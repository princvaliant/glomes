// JavaScript Document
Ext.define("glo.store.Equipments", {
	extend: 'Ext.data.Store',
	model: 'glo.model.Equipment',
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
		url: rootFolder + 'equipments',
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

