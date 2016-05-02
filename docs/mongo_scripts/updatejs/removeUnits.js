
var h= Object;
h['AAF13150152P-11_'] = 'AAF13150152P-11_';
h['TAB03150129P-24_'] = 'TAB03150129P-24_';
h['TAB03150129P-23_'] = 'TAB03150129P-23_';


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

