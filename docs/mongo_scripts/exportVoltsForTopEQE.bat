
mongo calserver06:27017/glo exportVoltsForTopEQE.js

mongoexport -h calserver06:27017 -d glo -c exportVoltsForTopEQE --csv --fields _id.serial,_id.device,value.eqe,value.top5IF1V,value.top5IF2V,value.top5IF3V,value.top5IF4V,value.top20IF1V -o exportVoltsForTopEQE.csv