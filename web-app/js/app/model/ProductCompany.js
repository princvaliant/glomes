Ext.define('glo.model.ProductCompany', {
        extend: 'Ext.data.Model',
        fields: [             
             {name: 'id', type: 'int'},
             {name: 'vendorCode', type: 'string'},
             {name: 'price', type: 'float'},
             {name: 'deliveryTime', type: 'int'},
             {name: 'uom', type: 'string'},
             {name: 'disabled', type: 'bool'}
         ]
    });



