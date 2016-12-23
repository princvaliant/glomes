var h = new Object;
//=CONCATENATE("h['",A1, "']='", A1, "-SR';")

h['HM6DJ0260071']='HM6DJ0260071PS';
h['HM6DJ0260082']='HM6DJ0260082PS';


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
