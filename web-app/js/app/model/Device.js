Ext.define('glo.model.Device', {
        extend: 'Ext.data.Model',
        fields: [             
             {name: 'name', type: 'string'}
        ],
    	validations: [
     		{type: 'presence',  field: 'name'}
    	]
    });



