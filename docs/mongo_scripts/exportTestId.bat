
mongo calserver05:27017/glo exportTestId.js

mongoexport -h calserver05:27017 -d glo -c exportedTestIds --csv --fields value.code,value.testId -o exportTestId.csv