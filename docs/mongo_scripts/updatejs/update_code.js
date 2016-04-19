var h = new Object;
h['EXPSO35-1']='S035-1';
h['EXPSO35-10']='S035-10';
h['EXPSO35-10_2']='S035-10_2';
h['EXPSO35-10_5']='S035-10_5';
h['EXPSO35-11']='S035-11';
h['EXPSO35-11_2']='S035-11_2';
h['EXPSO35-11_5']='S035-11_5';
h['EXPSO35-12']='S035-12';
h['EXPSO35-12_2']='S035-12_2';
h['EXPSO35-12_5']='S035-12_5';
h['EXPSO35-1_2']='S035-1_2';
h['EXPSO35-1_5']='S035-1_5';
h['EXPSO35-2']='S035-2';
h['EXPSO35-2_2']='S035-2_2';
h['EXPSO35-2_5']='S035-2_5';
h['EXPSO35-3']='S035-3';
h['EXPSO35-3_2']='S035-3_2';
h['EXPSO35-3_5']='S035-3_5';
h['EXPSO35-4']='S035-4';
h['EXPSO35-4_2']='S035-4_2';
h['EXPSO35-4_5']='S035-4_5';
h['EXPSO35-5']='S035-5';
h['EXPSO35-5_2']='S035-5_2';
h['EXPSO35-5_5']='S035-5_5';
h['EXPSO35-6']='S035-6';
h['EXPSO35-6_2']='S035-6_2';
h['EXPSO35-6_5']='S035-6_5';
h['EXPSO35-7']='S035-7';
h['EXPSO35-7_2']='S035-7_2';
h['EXPSO35-7_5']='S035-7_5';
h['EXPSO35-8']='S035-8';
h['EXPSO35-8_2']='S035-8_2';
h['EXPSO35-8_5']='S035-8_5';
h['EXPSO35-9']='S035-9';
h['EXPSO35-9_2']='S035-9_2';
h['EXPSO35-9_5']='S035-9_5';

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
