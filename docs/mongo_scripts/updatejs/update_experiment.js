
var h = new Object;

h['XB26602529']='XB26602529';
h['XB26604303']='XB26604303';

var old = 'D230';
var exp = 'EXPD230';


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
