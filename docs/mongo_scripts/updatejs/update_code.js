var h = new Object;
//=CONCATENATE("h['",A1, "']='", A1, "-SR';")

h['HLUJl110526PS']='HLUJL110526PS';
h['HLUJl110536PS']='HLUJL110536PS';





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
