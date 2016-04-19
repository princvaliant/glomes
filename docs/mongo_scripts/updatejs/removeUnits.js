
var h= Object;
h['AAF17150146Y-26_'] = 'AAF17150146Y-26_';
h['J255327930_'] = 'J255327930_';
h['TAB04150136P-12_'] = 'TAB04150136P-12_';
h['UN0008067_'] = 'UN0008067_';
h['un0013175_'] = 'un0013175_';





for (k in h) {

db.history.remove(
	{
		'code' : {$regex: '^' + k}
	}
	);

db.unit.remove(
	{
		'code' : {$regex: '^' + k}
	}
);

db.dataReport.remove(
	{
		'code' : {$regex: '^' + k}
	}
);
db.moves.remove(
  {
    'code' : {$regex: '^' + k}
  }
);

}

