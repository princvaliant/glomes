
var h= Object;
//h['XB26605718'] = 'XB26605718';
h['XB26605718_0001'] = 'XB26605718_0001';
h['XB26605718_0002'] = 'XB26605718_0002';
h['XB26605718_0003'] = 'XB26605718_0003';
h['XB26605718_0004'] = 'XB26605718_0004';
h['XB26605718_0005'] = 'XB26605718_0005';
h['XB26605718_0006'] = 'XB26605718_0006';


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

