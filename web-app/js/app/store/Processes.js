// JavaScript Document
Ext.define("glo.store.Processes", {
	extend: 'Ext.data.Store',
	model: 'glo.model.Process',
	pageSize: 100,
	proxy: {
		type: 'rest',
		url: rootFolder + 'taskbooks/processes'
	}
});

