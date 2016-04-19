
var units = ['UN0000989',
'UN0001000',
'UN0000988',
'UN0001108',
'UN0001102',
'CAI46454',
'CAI46452',
'UN0002083',
'CAI46453',
'CAI46261',
'UN0002467',
'UN0001511',
'CAI46460',
'CAI46260',
'CAI45799',
'CAI48269',
'CAI47654',
'UN0001101',
'CAI46259',
'UN0002172',
'UN0000995',
'UN0001105',
'CAI45829',
'UN0001985',
'CAI45838',
'UN0000993',
'CAG91560',
'UN0001106',
'UN0001986',
'CAG91655']


// db.unit.update (
// 	{ 
// 		'code': { $in : units}
// 	},
// 	{
// 		$set: {
// 			'mask': 'MASK7'
// 		}
// 	},
// 	false,
// 	true		
// );


db.history.update (
	{ 
		'code': { $in : units},
		'audit.tkey':'inventory_test',
		'audit.dc.mask':'MASK6',
	},
	{
		$set: {
			'audit.$.dc.mask':'MASK7',
		}
	},
	false,
	true		
);

db.dataReport.update (
	{ 
		'code': { $in : units}
	},
	{
		$set: {
			'value.uniformity_test_visualization.mask': 'MASK7'
		}
	},
	false,
	true		
);