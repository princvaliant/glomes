Ext.define('glo.model.MetaItem', {
        extend: 'Ext.data.Model',
        fields: [             
             {name: 'id', type: 'int'},
             {name: 'name', type: 'string'},
             {name: 'code', type: 'string'}
        ],
    	validations: [
     		{type: 'presence',  field: 'name'}
    	]
    });



