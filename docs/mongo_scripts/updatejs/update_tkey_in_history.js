
db.history.update(
	{
		'parentCode':null,
		'audit.tkey':'usertask8'
	},{
		$set:{
			'audit.$.tkey':'hf_strip'
		}
	},
	false,
	true
)

