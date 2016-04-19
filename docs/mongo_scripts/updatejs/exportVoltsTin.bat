
mongo calserver05:27017/glo exportVoltsTin.js

mongoexport -h calserver05:27017 -d glo -c voltsForTin --csv --fields value.code,value.voltage,value.current -o voltsForTin.csv