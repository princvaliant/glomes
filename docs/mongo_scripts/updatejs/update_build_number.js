var h = new Object;

h['082615-036S-5A']='082615-036S-5A';


var old = 'PBLD09261503';
var exp = 'PBLD09251504';


for (k in h){


  db.unit.update(
	{
		'code' : k
	},
	{
		$set : {
			'build_number' : exp
		}
	},
	false,
	true
  );


  db.unit.update(
	{
		'code' : k
	},
	{
		$pull : {
			'tagsCustom' : old
		}
	},
	false,
	true
  );


}
