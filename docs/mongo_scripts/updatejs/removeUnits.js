
var h= Object;
h['J255323139_'] = 'J255323139_';

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

