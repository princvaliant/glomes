var h = new Object;
//=CONCATENATE("h['",A1, "']='", A1, "-SR';")
h['AQ106468']='AQ106468PS';
h['QK6EJ9230007']='QK6EJ9230007PS';
h['YUK1130302']='YUK1130302PS';
h['HCK0160413']='HCK0160413PS';
h['HCK0160408']='HCK0160408PS';
h['HCK0160405']='HCK0160405PS';
h['HCK0160395']='HCK0160395PS';
h['HCK0160386']='HCK0160386PS';
h['HCK0160419']='HCK0160419PS';
h['HCK0160402']='HCK0160402PS';
h['HUJ8300561']='HUJ8300561PS';
h['HUJ8310393']='HUJ8310393PS';
h['HUJ8300531']='HUJ8300531PS';
h['HUJK272448']='HUJK272448PS';
h['HUJK272436']='HUJK272436PS';
h['XUK8023862']='XUK8023862PS';
h['QK6EJ9230013']='QK6EJ9230013PS';
h['XUK5140605']='XUK5140605PS';
h['XUK5140679']='XUK5140679PS';
h['XUK5140821']='XUK5140821PS';
h['YUK1130323']='YUK1130323PS';
h['HCK0160414']='HCK0160414PS';
h['HCK0160409']='HCK0160409PS';
h['HCK0160460']='HCK0160460PS';
h['HCK0160397']='HCK0160397PS';
h['HCK0160393']='HCK0160393PS';
h['HCK0160420']='HCK0160420PS';
h['HCK0160403']='HCK0160403PS';
h['AQ100541']='AQ100541PS';
h['HUJ8300556']='HUJ8300556PS';
h['HUJ8310470']='HUJ8310470PS';
h['HJK9020324']='HJK9020324PS';
h['HUJ8300567']='HUJ8300567PS';
h['XUK7312113']='XUK7312113PS';
h['XUK8023552']='XUK8023552PS';
h['XUK5110148']='XUK5110148PS';
h['XUK5232429']='XUK5232429PS';
h['HCK0160417']='HCK0160417PS';
h['HCK0160404']='HCK0160404PS';
h['HCK0160418']='HCK0160418PS';
h['AQ100472']='AQ100472PS';
h['YUK1090622']='YUK1090622PS';
h['XUK8023666']='XUK8023666PS';
h['YUK1161052']='YUK1161052PS';
h['HCK0160416']='HCK0160416PS';
h['HCK0160407']='HCK0160407PS';
h['HCK0160337']='HCK0160337PS';
h['HCK0160382']='HCK0160382PS';
h['HCK0160411']='HCK0160411PS';
h['AQ100475']='AQ100475PS';
h['HUJ8300551']='HUJ8300551PS';
h['HUJ8300545']='HUJ8300545PS';
h['YUK1090387']='YUK1090387PS';
h['XUK5201113']='XUK5201113PS';



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
