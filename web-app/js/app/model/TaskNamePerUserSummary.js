Ext.define('glo.model.TaskNamePerUserSummary', {
        extend: 'Ext.data.Model',
        fields: [             
             {name: 'order', type: 'int'},
             {name: 'procDefCategory', type: 'string'},
             {name: 'taskName', type: 'string'},
             {name: 'taskKey', type: 'string'},
             {name: 'procKey', type: 'string'},
             {name: 'total', type: 'int'}
        ]

    });
