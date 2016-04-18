// JavaScript Document
Ext.define("glo.store.WaferSpecs", {
	extend: 'Ext.data.Store',
	model: 'glo.model.WaferSpec',
	autoLoad: true, 
	autoSync: true,
	pageSize: '40',
    sorters: [
     {
         property : 'name',
         direction: 'ASC'
     },
     {
         property : 'revision',
         direction: 'ASC'
     }]
	,
	proxy : {
		type : 'rest',
		url: rootFolder + 'wafer/specs',
		actionMethods : {read: 'GET'},
		startParam: 'offset',
		pageParam: 'page',
		dirParam: 'order',
		limitParam: 'max',
		reader: {
    		type: 'json',
    		root: 'data',
    		totalProperty : 'count',
    		messageProperty: 'msg'
		}
	}
	 
	
});

