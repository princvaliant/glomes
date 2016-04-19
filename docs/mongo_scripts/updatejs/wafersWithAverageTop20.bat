
mongo calserver04:27017/glo wafersWithAverageTop20s.js

mongoexport -h calserver04:27017 -d glo -c wafersWithAverageTop20 --csv --fields _id,value.exp,value.run,value.wpe,value.eqe,value.volt,value.peak,value.fwhm,value.power -o champions.csv