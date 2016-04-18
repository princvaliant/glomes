Ext.define('glo.model.WaferSpec', {
        extend: 'Ext.data.Model',
        fields: [             
             {name: 'id', type: 'int'},
             {name: 'name', type: 'string'},
             {name: 'revision', type: 'int'},
             {name: 'dateCreated', type: 'date'}
        ]
    });



