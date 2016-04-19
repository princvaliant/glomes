
var h = new Object;

h['TAB01140536P-30']='TAB01140536P-30';
h['TAB01140536P-29']='TAB01140536P-29';
h['AAF17150073P-23']='AAF17150073P-23';
h['AAF17150073P-11']='AAF17150073P-11';
h['J255104242']='J255104242';


var old = 'D154';
var exp = 'EXPD154';


for (k in h){


  db.unit.update(
  {
    'code' : k
  },
  {
    $set:{
      'experimentId': exp
    },
    $pull : {
      'tagsCustom' : old
    }
  },
  false,
  true
  );

  db.history.update(
  {
    'code' : k,
    'audit.tkey' : 'epi_growth',
    'audit.din.experimentId' : old
  },
  {
    $set : {
      'experimentid' : exp,
      'audit.$.din.experimentId' : exp
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
    $set : {
      'value.epi_growth.experimentid' : exp,
      'value.experimentId' : exp
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
    $push : {
      'tagsCustom' : exp
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
      'experimentId': exp
    }
  },
  false,
  true
);
}
