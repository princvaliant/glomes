
var h= Object;
//h['DaveEPIFAB'] = 'DaveEPIFAB';
h['DaveEPIFAB_0001'] = 'DaveEPIFAB_0001';
h['DaveEPIFAB_0002'] = 'DaveEPIFAB_0002';
h['DaveEPIFAB_0003'] = 'DaveEPIFAB_0003';
h['DaveEPIFAB_0004'] = 'DaveEPIFAB_0004';
h['DaveEPIFAB_0005'] = 'DaveEPIFAB_0005';
h['DaveEPIFAB_0006'] = 'DaveEPIFAB_0006';
h['DaveEPIFAB_0007'] = 'DaveEPIFAB_0007';
h['DaveEPIFAB_0008'] = 'DaveEPIFAB_0008';
h['DaveEPIFAB_0009'] = 'DaveEPIFAB_0009';
h['DaveEPIFAB_0010'] = 'DaveEPIFAB_0010';

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

