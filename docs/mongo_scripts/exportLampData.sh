

mongoexport -h calserver07:27017 -d glo -c measures -q '{"TestType":"test_data_visualization", "TimeRun": {"$gt": new Date(1431759600000)},"MaxAdcValue":{"$gt":2000}, "radiometric":{"$lt":0}}' --csv --fields WaferID,TimeRun,MaxAdcValue,radiometric -o measures.csv
