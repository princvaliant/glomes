var arr = ['UN0005429',
'UN0005308',
'UN0004186',
'UN0005581',
'UN0005576',
'UN0004434',
'UN0004111',
'UN0004233',
'UN0007070'];


db.history.update(
	{
		'code': {$in: arr},
		'audit': { $elemMatch: {pkey:'direct_view',tkey:'final_inspection',workCenter: null}}
	},
	{
		$set:{
			'audit.$.workCenter':'FAB',
			'audit.$.workCenterId': 2,
			'audit.$.operation':'Fab generic',
			'audit.$.operationId': 52
		}
	},
	false,
	true
);



