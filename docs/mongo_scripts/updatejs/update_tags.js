
var h= Object;

h['CAG91653'] = 'CAG91553TMP';

for (k in h) {

db.unit.update(
	{
		'code' : k
	}, 
	{
		$pull : {
			'tags' : h[k]
		}		
	},
	false,
	true
);


}

