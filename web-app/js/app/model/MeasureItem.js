Ext.define('glo.model.MeasureItem', {
        extend: 'Ext.data.Model',
        fields: [             
             {name: 'name', type: 'string'}
        ],
    	validations: [
     		{type: 'presence',  field: 'name'}
    	]
    });



