
mongo calserver06:27017/glo championDevicesAllCurrents.js

mongoexport -h calserver06:27017 -d glo -c championDevicesAllCurrents --csv --fields _id.serial,_id.device,_id.current,value.exp,value.wpe,value.eqe,value.volt,value.peak,value.fwhm,value.power -o championDevicesAllCurrents.csv