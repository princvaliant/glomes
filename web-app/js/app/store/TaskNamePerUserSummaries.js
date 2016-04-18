// JavaScript Document
Ext.define("glo.store.TaskNamePerUserSummaries", {
	extend: 'Ext.data.Store',
	model: 'glo.model.TaskNamePerUserSummary',
	pageSize: 100,
	proxy: {
		type: 'rest',
		url: rootFolder + 'taskbooks/taskNameSummary'
	}
});

