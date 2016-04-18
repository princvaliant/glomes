
Ext.define('glo.view.tb.NoteList', {
	extend: 'Ext.grid.Panel',
	alias: 'widget.notelist',
	unitId: '',

 	initComponent: function(){
        Ext.apply(this, {
            
            store: Ext.create('glo.store.Notes'),
            viewConfig: {
                itemId: 'view',
                plugins: [{
                    pluginId: 'preview',
                    ptype: 'preview',
                    expanded: true
                }]
            },
            columns: [{
                text: 'Comment',
                dataIndex: 'comment',
                width: 550,
                renderer: this.formatComment
            }, {
                text: 'userName',
                dataIndex: 'userName',
                hidden: true,
                width: 200
           },
            {
                text: 'Step',
                dataIndex: 'stepName',
                width: 140

            }, {
                text: 'Date created',
                dataIndex: 'dateCreated',
				xtype : 'datecolumn',
				format : 'Y-m-d H:i',
                width: 130
            }]
        });
        
        this.loadUnit(this.unitId);
        
        this.callParent(arguments);
    },


    loadUnit: function(unitId){
        var store = this.store;
        store.getProxy().extraParams.unit = unitId;
        store.load();
    },

    formatComment: function(value, p, record){
        return Ext.String.format('<div><b>By {0}</b></div><div class="wordwrap">{1}</div>', record.get('userName') || "Unknown", value);
    }
});

