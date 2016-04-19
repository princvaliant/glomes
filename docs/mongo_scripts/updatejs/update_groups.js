db.unit.update (
	{ 
		'parentCode': null,
		'tkey': { '$in' :['test_data_visualization','test_queue']}
	},
	{
		$set:{
			'grps': ['ROLE_TEST_ADMIN']
		}
	},
	false,
	true		
);

