
mongo calserver06:27017/glo exportVolts.js

mongoexport -h calserver06:27017 -d glo -c voltsForFrank --csv --fields _id,value.current,value.voltage,value.experimentId -o voltsForFrank.csv