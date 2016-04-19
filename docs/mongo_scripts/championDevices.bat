
mongo calserver06:27017/glo championDevices.js

mongoexport -h calserver06:27017 -d glo -c championDevices --csv --fields _id.serial,_id.device,value.exp,value.run,value.wpe,value.eqe,value.volt,value.peak,value.fwhm,value.power -o champions.csv