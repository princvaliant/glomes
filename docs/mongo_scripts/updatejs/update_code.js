var h = new Object;
//=CONCATENATE("h['",A1, "']='", A1, "-SR';")

h['PUF0868PS_0204']='PUF0868PS_0002';
h['PUF0868PS_0104']='PUF0868PS_0001';
h['PUF0868PS_0203']='PUF0868PS_0004';
h['PUF0868PS_0103']='PUF0868PS_0003';
h['PUF0868PS_0202']='PUF0868PS_0006';
h['PUF0868PS_0102']='PUF0868PS_0005';

h['XB26604811_0204']='XB26604811_0002';
h['XB26604811_0104']='XB26604811_0001';
h['XB26604811_0203']='XB26604811_0004';
h['XB26604811_0103']='XB26604811_0003';
h['XB26604811_0202']='XB26604811_0006';
h['XB26604811_0102']='XB26604811_0005';

h['PUL3403PS_0204']='PUL3403PS_0002';
h['PUL3403PS_0104']='PUL3403PS_0001';
h['PUL3403PS_0203']='PUL3403PS_0004';
h['PUL3403PS_0103']='PUL3403PS_0003';
h['PUL3403PS_0202']='PUL3403PS_0006';
h['PUL3403PS_0102']='PUL3403PS_0005';

h['PUK2389PS_0204']='PUK2389PS_0002';
h['PUK2389PS_0104']='PUK2389PS_0001';
h['PUK2389PS_0203']='PUK2389PS_0004';
h['PUK2389PS_0103']='PUK2389PS_0003';
h['PUK2389PS_0202']='PUK2389PS_0006';
h['PUK2389PS_0102']='PUK2389PS_0005';




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
