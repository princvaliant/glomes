
mongo calserver06:27017/glo exportCurrents.js

mongoexport -h calserver06:27017 -d glo -c currentsForBrad --csv --fields _id,value.current,value.voltage,value.exp -o exportCurrents.csv