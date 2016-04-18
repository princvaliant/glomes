// JavaScript Document
Ext.define("glo.store.ProcessPerUserSummaries", {
	extend: 'Ext.data.Store',
	model: 'glo.model.ProcessPerUserSummary',
	pageSize: 100,
	proxy: {
		type: 'rest',
		extraParams : {
			all: ['EquipmentDC', 'Test', 'EquipmentStatus', 'MoveTransaction','Metadata']
		},
		url: rootFolder + 'taskbooks/processSummary'
	}
});

