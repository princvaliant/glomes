
var h= new Object;

h['UN0011957'] = 'UN0011957';
h['UN0011956'] = 'UN0011956';


for (k in h) {

db.unit.update(
	{
		'code' : h[k]
	},
	{
		$set:{
			'tkey': 'test_queue',
			'tname' :'Test queue',
			'pkey': 'test'
		}
	},
	false,
	true
);

}

