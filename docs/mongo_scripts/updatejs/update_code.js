var h = new Object;
//=CONCATENATE("h['",A1, "']='", A1, "-SR';")

h['PUK0454PS_0204']='PUK0454PS_0002';
h['PUK0454PS_0104']='PUK0454PS_0001';
h['PUK0454PS_0203']='PUK0454PS_0004';
h['PUK0454PS_0103']='PUK0454PS_0003';
h['PUK0454PS_0202']='PUK0454PS_0006';
h['PUK0454PS_0102']='PUK0454PS_0005';

h['AO956162_0204']='AO956162_0002';
h['AO956162_0104']='AO956162_0001';
h['AO956162_0203']='AO956162_0004';
h['AO956162_0103']='AO956162_0003';
h['AO956162_0202']='AO956162_0006';
h['AO956162_0102']='AO956162_0005';


for (k in h){

  db.history.update({
     'code' : k
    },
    {
       $set:{
    			'code': h[k]
		   },
		   $pull : {
			   'tags' : k
		   }
	  },
	  false,
	  true
  );

   db.history.update({
     'code' : h[k]
    },
    {
       $push : {
          'tags' : h[k]
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
		$set:{
			'code': h[k]
		},
		$pull : {
			'tags' : k
		}
	},
	false,
	true
  );

   db.unit.update(
  {
    'code' : h[k]
  },
  {
    $push : {
      'tags' : h[k]
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
		$set:{
			'code': h[k]
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
			'code': h[k]
		}
	},
	false,
	true
);
}
