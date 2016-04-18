Ext.define('glo.model.ProcessPerUserSummary', {
        extend: 'Ext.data.Model',
        fields: [                
             {name: 'categoryId', type: 'string'},
             {name: 'categoryName', type: 'string'},
             {name: 'total', type: 'int'}
        ]

    });
