

Ext.define('glo.view.dataEntry.Form', {
    extend : 'Ext.form.Panel',
    alias : 'widget.dataentryform',
    border : false,
    minWidth : 400,
    minHeight : 160,
    layout : 'column',
    defaultType: 'textfield',
    fieldDefaults : {
        labelWidth : 135,
        labelAlign : 'right',
        width : 225	,
        margin : 2
    },
    standardSubmit : false,
    bodyPadding : 6,

    initComponent : function() {

        Ext.apply(this, {
            items: [

            ],
            dockedItems : [{
                xtype : 'toolbar',
                dock : 'top',
                items: [
                    {
                        xtype : 'combo',
                        id : 'equipmentDcSelectId',
                        store : Ext.create('glo.store.Equipments', {
                            pageSize:100
                        }),
                        width: 250,
                        mode: 'remote',
                        editable: false,
                        displayField:'name',
                        action : 'equipmentDcSelect',
                        triggerAction : 'all',
                        emptyText: '<select equipment>',
                        listConfig: {
                            loadingText: 'Searching equipments ...',
                            valueNotFoundText : 'No equipments found.'
                        },
                        pageSize: 100
                    }   ,
                    {
                        xtype: 'textfield',
                        id:'equipmentDcRunNumber',
                        labelAlign : 'right',
                        labelWidth : 85,
                        fieldLabel : 'Run#',
                        readOnly: true
                    }
                ]
            }]
        });

        this.callParent(arguments);

        var p = Ext.getCmp('equipmentDcSelectId').store.getProxy();
        p.extraParams.onlyDc = 'true';
        Ext.getCmp('equipmentDcSelectId').store.load({
            callback: function(records, operation, success) {
               var name = this.dataEntryName;
               if (name != undefined) {
                    Ext.each(records, function (record) {
                        if (record.data.name.toLowerCase() === name.toLowerCase()) {
                              Ext.getCmp('equipmentDcSelectId').setValue(name);
                              Ext.getCmp('equipmentDcSelectId').fireEvent('select', Ext.getCmp('equipmentDcSelectId'), [record]);
                        }
                    });
                }
            },
            scope: this
        });
    },

    buttons : [
        {
            text: 'Copy to new ...',
            handler: function () {

                var runFrom = Ext.getCmp('equipmentDcRunNumber').value;

                var dlg = Ext.MessageBox.prompt('New run number', 'Enter run number to copy to:', function(btn, text){
                    if (btn == 'ok'){

                        var runTo = text;

                        Ext.Ajax
                            .request({
                                method: 'POST',
                                params: {
                                    runFrom: runFrom,
                                    runTo: runTo
                                },
                                url: rootFolder + 'dataEntry/copy',
                                success: function (response) {

                                    var obj = Ext.decode(response.responseText);
                                    if (obj.success == false) {
                                        Ext.Msg
                                            .alert(
                                            'Copy failed',
                                            obj.msg);
                                    } else {
                                        Ext.ux.Message.msg(
                                                "Copy from " + runFrom + " to " + runTo + " complete.",
                                            "");
                                    }
                                }
                            });

                    }
                });
            }
        },
        {
            xtype: 'tbfill'
        },
        {
        text : 'Save',
        handler : function() {
            var form = this.up('form');
            var runNumber = Ext.getCmp('equipmentDcRunNumber').value;
            var code = Ext.getCmp('equipmentDcSelectId').displayTplData[0].code;
            var name = Ext.getCmp('equipmentDcSelectId').displayTplData[0].name;
            var win = this.up('window');

            if (form.getForm().isValid()) {

                form.submit({
                    scope : this,
                    method : 'POST',
                    url : rootFolder + 'dataEntry/getAll',
                    submitEmptyText : false,
                    waitMsg : 'Saving data ...',
                    params : {
                        id: form.rid,
                        runNumber: runNumber,
                        code: code,
                        equipmentName: name
                    },
                    success : function(frm, action) {
                        Ext.ux.Message.msg(
                            "Data saved successfully.", "");

                        if (form.rid == 0) {
                            gloApp.getController('DataEntryPanel').refreshListData();
                        }
                        gloApp.getController('DataEntryPanel').refreshContentData();
                         win.destroy();
                    },
                    failure : function(form, action) {

                        Ext.Msg.alert('Failed',
                            action.result.msg);
                    }
                });
            }
        }
    }, {
        text : 'Cancel',
        handler : function() {
            this.up('window').destroy();
        }
    }]
});
