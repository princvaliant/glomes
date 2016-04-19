
mongo calserver05:27017/glo exportVolts.js

mongoexport -h calserver05:27017 -d glo -c voltsForFrank --csv --fields _id,value.current,value.voltage,value.exp -o voltsForFrank.csv