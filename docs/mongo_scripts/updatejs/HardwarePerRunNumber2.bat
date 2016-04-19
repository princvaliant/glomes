

mongoexport -h calserver05:27017 -d glo -c hardwarePerRunNumber2 --csv --fields run,pos,code,equipment,supplier,product,productCode,weight,start,end -o hardwarePerRunNumber2.csv