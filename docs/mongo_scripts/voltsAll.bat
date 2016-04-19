
mongo calserver06:27017/glo voltsAll.js

mongoexport -h calserver06:27017 -d glo -c voltsAll --csv --fields _id.serial,_id.device,_id.current,_id.wpe,_id.eqe,_id.volt -o voltsAll.csv