
var h= Object;
h['AO956162_0102'] = 'AO956162_0102';
h['AO956162_0103'] = 'AO956162_0103';
h['AO956162_0104'] = 'AO956162_0104';
h['AO956162_0202'] = 'AO956162_0202';
h['AO956162_0203'] = 'AO956162_0203';
h['AO956162_0204'] = 'AO956162_0204';


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

