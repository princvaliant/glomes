
db.history.update(
	{
		'audit.pkey':'sub_mounts'
	},
	{
		$set:{
			'audit.$.pkey':'direct_view'
		}
	},
	false,
	true
);

db.history.update(
	{
		'audit.pkeyPrev':'sub_mounts'
	},
	{
		$set:{
			'audit.$.pkeyPrev':'direct_view'
		}
	},
	false,
	true
);

db.unit.update(
	{
		'pkey':'sub_mounts'
	},
	{
		$set:{
			'pkey':'direct_view'
		}
	},
	false,
	true
);

db.moves.update(
	{
		'pkey':'sub_mounts'
	},
	{
		$set:{
			'pkey':'direct_view'
		}
	},
	false,
	true
);


db.moves.update(
	{
		'pkeyPrev':'sub_mounts'
	},
	{
		$set:{
			'pkeyPrev':'direct_view'
		}
	},
	false,
	true
);


