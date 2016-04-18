Ext.define('glo.model.MeasureGroupType', {
        extend: 'Ext.data.Model',
        fields: [             
             {name: 'name', type: 'string'},
             {name: 'field', type: 'string'},
             {name: 'defaultValue', type: 'string'}
        ],
    	validations: [
     		{type: 'presence',  field: 'name'}
    	]
    });
