
mongo calserver06:27017/glo HardwarePerRunNumber.js

mongoexport -h calserver06:27017 -d glo -c hardwarePerRunNumber --csv --fields _id.run,_id.code,_id.product -o hardwarePerRunNumber.csv