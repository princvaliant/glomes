

mongoexport -h calserver06:27017 -d glo -c equipmentStatus -q "{'name':{'$in':['Reactor S2']}}" --csv --fields name,status,userName,comment,dateStart,dateEnd,duration -o equipmentStatusesByName.csv