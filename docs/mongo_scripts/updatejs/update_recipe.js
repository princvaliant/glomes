var h = new Object;

h['HM6JKK230129PS']='HM6JKK230129PS';
h['HM6JKK230130PS']='HM6JKK230130PS';
h['HM6JKK230134PS']='HM6JKK230134PS';

var old = 'S2-PLN-R25-BIL-HT-1p0T-TFB2B-G4+0p7U';
var exp = 'S2-PLN-R25-BIL-LT-1p0T-TFB2B-G4+0p7U';

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
