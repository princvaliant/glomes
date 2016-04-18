Ext.define('glo.model.Process', {
        extend: 'Ext.data.Model',
        fields: [             
             {name: 'id', type: 'int'},
             {name: 'name', type: 'string'}
         ],
    	validations: [
     		{type: 'presence',  field: 'name'}
    	]
    });



