var o = 'TAB01140519P-07';
var n = 'TAB01140521P-07';

db.testData.find({'value.code' : {$regex: '^' + o}}).forEach(function(e) {
    var parts = e.value.code.split('_');
    if (parts.length === 2)
      e.value.code = n + '_' + parts[1];
    if (parts.length === 1)
      e.value.code = n;

    db.testData.save(e);
});



