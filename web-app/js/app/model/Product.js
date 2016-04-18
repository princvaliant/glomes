Ext.define('glo.model.Product', {
        extend: 'Ext.data.Model',
        fields: [             
             {name: 'id', type: 'int'},
             {name: 'name', type: 'string'},
             {name: 'isFrozen', type: 'bool'}
         ],
    	validations: [
     		{type: 'presence',  field: 'name'}
    	]
    });



