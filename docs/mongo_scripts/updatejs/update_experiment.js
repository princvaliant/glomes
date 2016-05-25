
var h = new Object;

h['JB0085']='JB0085';
h['JB0036']='JB0036';
h['JB0084']='JB0084';
h['JB0091']='JB0091';

var old = 'EXPJ017';
var exp = 'EXPJ018';


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
