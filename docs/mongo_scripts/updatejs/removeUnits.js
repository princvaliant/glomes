
var h= Object;
h['ALEXWC1'] = 'ALEXWC1';
h['ALEXWC1_0001'] = 'ALEXWC1_0001';
h['ALEXWC1_0002'] = 'ALEXWC1_0002';
h['ALEXWC1_0003'] = 'ALEXWC1_0003';
h['ALEXWC1_0004'] = 'ALEXWC1_0004';
h['ALEXWC1_0005'] = 'ALEXWC1_0005';
h['ALEXWC1_0006'] = 'ALEXWC1_0006';


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

