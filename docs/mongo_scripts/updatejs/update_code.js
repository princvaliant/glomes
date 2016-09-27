var h = new Object;
h['UN0015563']='UN0015563-SR';
h['UN0015564']='UN0015564-SR';
h['UN0015565']='UN0015565-SR';
h['UN0015566']='UN0015566-SR';
h['UN0015567']='UN0015567-SR';
h['UN0015568']='UN0015568-SR';
h['UN0015569']='UN0015569-SR';
h['UN0015571']='UN0015571-SR';
h['UN0015572']='UN0015572-SR';
h['UN0015573']='UN0015573-SR';
h['UN0015574']='UN0015574-SR';
h['UN0015575']='UN0015575-SR';



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
