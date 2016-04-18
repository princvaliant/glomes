Ext.define('glo.model.Note', {
        extend: 'Ext.data.Model',
        fields: [             
             {name: 'id', type: 'int'},
             {name: 'comment', type: 'string'},
             {name: 'userName', type: 'string'},
             {name: 'stepName', type: 'string'},
             {name: 'dateCreated', type: 'string'}
        ]
    });



