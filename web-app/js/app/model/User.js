Ext.define('glo.model.User', {
        extend: 'Ext.data.Model',
        fields: [             
             {name: 'id', type: 'string'},
             {name: 'username', type: 'string'},
             {name: 'current', type: 'string'}
        ],
    	validations: [
     		{type: 'presence',  field: 'username'}
    	]
    });



