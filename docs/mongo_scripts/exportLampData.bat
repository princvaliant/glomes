

mongoexport -h calserver06:27017 -d glo -c measures -q "{'TestType':'test_data_visualization', 'TimeRun': {'$gt': ISODate('2015-04-21T00:00:00.000Z')},'MaxAdcValue':{'$gt':2000}, 'radiometric':{'$lt':0}}" --csv --fields value.code,value.TimeRun -o measures.csv
