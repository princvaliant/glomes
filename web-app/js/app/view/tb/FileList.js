
Ext.define('glo.view.tb.FileList', {
	extend: 'Ext.grid.Panel',
	alias: 'widget.filelist',
	unitId: '',

 	initComponent: function(){
        Ext.apply(this, {
            
            store: Ext.create('glo.store.Files'),
            viewConfig: {
                itemId: 'view',
                plugins: [{
                    pluginId: 'preview',
                    ptype: 'preview',
                    expanded: true
                }]
            },
            columns: [{
                text: 'Name',
                dataIndex: 'name',
                width: 140
            }, {
                text: 'File name',
                dataIndex: 'fileName',
                width: 270
            },
            {
                text: 'owner',
                dataIndex: 'userName',
                width: 90
            },
            {
                text: 'Date uploaded',
                dataIndex: 'dateCreated',
                width: 130
            }, {
	        	id : 'downloadFileId',
				xtype : 'actioncolumn',
				width : 50,
				items : [{
					getClass : function(v, meta, rec) {
						this.items[0].tooltip = 'Download file';
						return 'column_download';
					}
				}]
            }]
        });
        
        this.loadUnit(this.unitId);
        
        this.callParent(arguments);
    },


    loadUnit: function(unitId){
        var store = this.store;
        store.getProxy().extraParams.unit = unitId;
        store.load();
    }
});

