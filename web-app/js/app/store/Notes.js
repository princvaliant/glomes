// JavaScript Document
Ext.define("glo.store.Notes", {
	extend: 'Ext.data.Store',
	model: 'glo.model.Note',
	autoLoad: false, 
	autoSync: true,
	pageSize: '40',
    sorters: 
     {
         property : 'dateCreated',
         direction: 'ASC'
     }
	,
	proxy : {
		type : 'rest',
		url: rootFolder + 'notes',
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

