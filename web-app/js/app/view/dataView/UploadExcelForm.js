

Ext.define('glo.view.dataView.UploadExcelForm', {
	extend : 'Ext.form.Panel',
	alias : 'widget.uploadexcelform',
	border : false,
	dataViewId : '',
	minWidth : 400,
	minHeight : 211,
	maxWidth : 600,
	maxHeight : 650,
	layout : 'anchor',
	defaultType : 'textfield',
	bodyPadding : '9 9 3 9',
	items : [{
				xtype : 'filefield',
				emptyText : '<Select excel file>',
				fieldLabel : 'Excel file',
				name : 'filePath',
				buttonText : '',
				allowBlank : false,
				anchor : '90%',
				buttonConfig : {
					iconCls : 'icon-file_upload'
				}
			}],

	buttons : [{
		text : 'Upload',
		handler : function() {
			var form = this.up('form');
			var win = this.up('window');

			if (form.getForm().isValid()) {
				form.getForm().submit({
					scope : this,
					method : 'POST',
					url : rootFolder + 'dataViews/importExcel',
					waitMsg : 'Uploading excel template ...',
					params : {
						dataViewId : form.dataViewId
					},
					success : function(frm, action) {
						
						var obj = Ext.decode(action.response.responseText);
						if (obj.success == 'true') {
							Ext.ux.Message.msg("Excel template successfuly uploaded.","");
							win.destroy();
						} else {
							Ext.Msg.alert('Upload Failed',	obj.msg);
						}
					},
					failure : function(frm, action) {
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
