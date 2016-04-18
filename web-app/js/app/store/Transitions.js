// JavaScript Document
Ext.define("glo.store.Transitions", {
	extend: 'Ext.data.Store',
	model: 'glo.model.Transition',
	pageSize: 100,
	proxy: {
		type: 'rest',
		url: rootFolder + 'taskbooks/transitions'
	}
});

