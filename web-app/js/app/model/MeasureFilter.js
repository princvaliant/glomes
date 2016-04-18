Ext.define('glo.model.MeasureFilter', {
        extend: 'Ext.data.Model',
        requires:[
       	 	'glo.model.MeasureItem'          
    	],
        fields: [             
             {name: 'id', type: 'int'},
             {name: 'name', type: 'string'}
        ],
        hasMany: [
        	{model: 'glo.model.MeasureItem', name: 'currents'},
        	{model: 'glo.model.MeasureItem', name: 'temperatures'},
        	{model: 'glo.model.MeasureItem', name: 'hours'},
        	{model: 'glo.model.MeasureItem', name: 'currents'},
        	{model: 'glo.model.MeasureItem', name: 'encaps'},
        	{model: 'glo.model.MeasureItem', name: 'codes'},
        	{model: 'glo.model.MeasureItem', name: 'groups'}
        ],
        proxy : {
			type : 'ajax',
			method : 'GET',
			url : rootFolder + 'measure/filters',
			reader: {
            	type: 'json',
            	root: ''
        	}
		}
    });



