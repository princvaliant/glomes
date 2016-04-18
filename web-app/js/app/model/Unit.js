Ext.define('glo.model.Unit', {
        extend: 'Ext.data.Model',
        fields: [             
             {name: 'id', type: 'int'},
             
             {name: 'code', type: 'string'},
             {name: 'desc', type: 'string'},
             {name: 'locId', type: 'int'},
             {name: 'locName', type: 'string'},
             {name: 'vendId', type: 'int'},
             {name: 'vendName', type: 'string'},
             {name: 'qtyIn', type: 'int'},
             {name: 'qtyOut', type: 'int'},
             
             {name: 'pid', type: 'int'},
             {name: 'product', type: 'string'},
             {name: 'productCode', type: 'string'},
             {name: 'lotNumber', type: 'string'},
             
             {name: 'tkey', type: 'string'},
             {name: 'tname', type: 'string'},
             {name: 'pkey', type: 'string'},                    
             {name: 'pctg', type: 'string'},  
             {name: 'start', type: 'string'},  
             {name: 'due', type: 'string'},  
             {name: 'prior', type: 'int'},
             {name: 'actualStart', type: 'date'},
             {name: 'equipId', type: 'int'},

             {name: 'exper', type: 'string'},  
             {name: 'epirun', type: 'string'},  
             {name: 'fab', type: 'string'} ,
             {name: 'experAct', type: 'bool'},
             
             {name: 'noteCount', type:'int'},
             
             // data collection at receiving
             {name: 'orientation', type: 'float'},      
             {name: 'refFlat', type: 'float'},    
             {name: 'bow', type: 'int'},  
             {name: 'warp', type: 'int'},  
             {name: 'frontSideRa', type: 'int'},  
             {name: 'thickness', type: 'int'},  
             {name: 'diameter', type: 'float'},  
             {name: 'ttv', type: 'float'},  
             {name: 'tir', type: 'float'},  
             {name: 'primaryFlat', type: 'float'},  
             {name: 'secFlat', type: 'float'},  
             {name: 'backSideRa', type: 'float'},  
             {name: 'surface', type: 'string'}
             
        ]
    });



