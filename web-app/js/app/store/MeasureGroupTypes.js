// JavaScript Document
Ext.define("glo.store.MeasureGroupTypes", {
		extend: 'Ext.data.Store',
		model: 'glo.model.MeasureGroupType',
		autoLoad : false,
        proxy : {
			type : 'ajax',
			method : 'GET',
			url : rootFolder + 'measure/groupTypes',
			reader: {
            	type: 'json',
            	root: ''
        	}
		}
});

