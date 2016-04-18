Ext.define('glo.view.dataEntry.Content', {
    extend: 'Ext.Panel',
    alias: 'widget.dataentrycontent',
    requires: ['Ext.ux.PreviewPlugin', 'Ext.toolbar.Toolbar', 'Ext.form.field.ComboBox'],
    border: false,
    layout: 'fit',
    margins: '0 0 0 0',
    title: 'Select entry on left side',

    initComponent: function () {

        this.addEvents('dataentryadd');

        Ext.apply(this, {
            dockedItems: {
                xtype: 'toolbar',
                items: [
                    {
                        xtype: 'textfield',
                        name : 'searchDataEntryContent',
                        emptyText: '<filter runs>',
                        enableKeyEvents: true
                    },
                    {
                        text : 'Add',
                        iconCls : 'icon-shape_square_add',
                        scope: this,
                        handler : function() {
                            this.fireEvent('dataentryadd', this);
                        }
                    }
                ]
            }
        });


        this.callParent(arguments);
    }
});
