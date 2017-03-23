
var h= Object;

h['JT0100'] = 'JT0100';
h['JT0101'] = 'JT0101';
h['JT0102'] = 'JT0102';
h['JT0103'] = 'JT0103';
h['JT0104'] = 'JT0104';
h['JT0105'] = 'JT0105';
h['JT0106'] = 'JT0106';
h['JT0107'] = 'JT0107';
h['JT0108'] = 'JT0108';
h['JT0109'] = 'JT0109';
h['JT0110'] = 'JT0110';
h['JT0111'] = 'JT0111';
h['JT0112'] = 'JT0112';
h['JT0113'] = 'JT0113';
h['JT0114'] = 'JT0114';
h['JT0115'] = 'JT0115';
h['JT0116'] = 'JT0116';
h['JT0117'] = 'JT0117';
h['JT0118'] = 'JT0118';
h['JT0119'] = 'JT0119';
h['JC11'] = 'JC11';
h['JC12'] = 'JC12';
h['JC13'] = 'JC13';
h['JC14'] = 'JC14';
h['JC15'] = 'JC15';
h['JC16'] = 'JC16';
h['JC17'] = 'JC17';
h['JC18'] = 'JC18';
h['JC19'] = 'JC19';

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

