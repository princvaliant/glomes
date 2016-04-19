

mongoexport -h calserver06:27017 -d glo -c dataReport -q "{'code': {'$in' : ['LA11A053975','LA11A053974']}}" --csv --fields code,value.test_queue.actualStart,value.test_queue.end -o startend.csv
