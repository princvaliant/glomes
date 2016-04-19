var h = {};

h.J255222450 ='J255222450';
h.J255306966 ='J255306966';
h.J255306967 = 'J255306967';
h.J255306968 = 'J255306968';
h.J255306972 ='J255306972';
h.J255306973 ='J255306973';
h.J255306978 = 'J255306978';
h.J255306980 ='J255306980';
h.J255321597 = 'J255321597';
h.J255321607 ='J255321607';
h.J255321611 ='J255321611';
h.J255340318 = 'J255340318';


var old = 'C2015092500225';
var exp = 'C2015092400226';


for (var k in h){


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

  db.history.update(
  {
    'code' : k,
    'audit.tkey' : 'dicd_meas',
    'audit.din.NIL_cassette_number' : old
  },
  {
    $set : {
      'audit.$.din.NIL_cassette_number' : exp
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
      'value.dicd_meas.NIL_cassette_number' : exp
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
			'NIL_cassette_number': exp
		}
	},
	false,
	true
);
}
