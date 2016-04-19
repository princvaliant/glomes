
db.unit.update(
	{
		'code': {$in :['TC372830','TC372830-1']}
	}, 
	{
		$set:{
			'pkey':'dicing',
			'pctg':'nwLED',	              
			'tkey':'dicing_queue',
			'tname':'Dicing queue'
		}
	},
	false,
	true
)

