

Ext.define('glo.view.tb.FileUploadForm', {
	extend : 'Ext.form.Panel',
	alias : 'widget.fileuploadform',
	border : false,
	rows : '',
	parentWindow : '',
	category: '',
	processKeyEng : '',
	taskKeyEng : '',
	importData : '',
	minWidth : 400,
	minHeight : 211,
	maxWidth : 600,
	maxHeight : 650,
	layout : 'anchor',
	defaultType : 'textfield',
	bodyPadding : '9 9 3 9',
	items : [{
				xtype : 'filefield',
				emptyText : '<Select a file>',
				fieldLabel : 'File',
				name : 'filePath',
				buttonText : '',
				allowBlank : false,
				anchor : '90%',
				buttonConfig : {
					iconCls : 'icon-file_upload'
				}
			}, {
				xtype : 'textfield',
				fieldLabel : 'Name',
				name : 'name',
				anchor : '90%'
			}
	],

	buttons : [{
		text : 'Upload',
		handler : function() {
			
			var form = this.up('form');
			var win = this.up('window');

			if (form.getForm().isValid()) {

				var aResult = Ext.Array.map(form.rows, function(node) {
							return {
								id : node.data.id
							};
						});
				var units = Ext.encode(aResult);

				form.getForm().submit({
					scope : this,
					method : 'POST',
					url : rootFolder + 'units/fileUpload',
					waitMsg : 'Uploading file ...',
					params : {
						units : units,
						importData : form.importData,
						pctg : form.category,
						pkey : form.processKeyEng,
						tkey : form.taskKeyEng
					},
					success : function(frm, action) {

						var obj = Ext.decode(action.response.responseText);
						
						if (obj.success == 'false') {
							Ext.Msg.alert('Failed', obj.msg);
						} else {
							if (form.importData == 'false') {
								Ext.ux.Message.msg("File successfuly uploaded and attached.","");
							} else {
								Ext.ux.Message.msg(obj.retCount + " items successfuly imported", "");
							}
							gloApp.getController('MainPanel').loadList(form.parentWindow, '', '', 'stay');
							win.destroy();
						}
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
