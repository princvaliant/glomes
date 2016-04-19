var h = new Object;

h['J255335033']='J255335033';
h['J255222442']='J255222442';


var old = 'S2-PLN-RED8-RSHLD-15-TFB-1';
var exp = 'S2-PLN-RED8-RSHLD-10-TFB-1';


for (k in h){


  db.unit.update(
	{
		'code' : k
	},
	{
		$set:{
			'recipeName': exp
		},
		$pull : {
			'tagsCustom' : old
		}
	},
	false,
	true
  );

  db.history.update(
  {
    'code' : k,
    'audit.tkey' : 'epi_growth',
    'audit.din.recipeName' : old
  },
  {
    $set : {
      'audit.$.din.recipeName' : exp
    }
  },
  false,
  true
  );

  db.dataReport.update(
  {
    'code' : k
  },
  {
    $set : {
      'value.epi_growth.recipeName' : exp
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
    $push : {
      'tagsCustom' : exp
    }
  },
  false,
  true
  );

  db.moves.update(
	{
		'code' : k
	},
	{
		$set:{
			'recipeName': exp
		}
	},
	false,
	true
);
}
