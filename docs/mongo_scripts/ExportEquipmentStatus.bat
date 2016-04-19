

mongoexport -h calserver07:27017 -d glo -c konica -q "{'code':{'$in':['150120075AH','']}}" --csv --fields code,testId,red_current,green_current,blue_current,center_lv,unif_13pt,avg_13pt,ciex_13pt,ciey_13pt,unif_50pt,avg_50pt,unif_69pt,avg_69pt,ciex_69pt,ciey_69pt,color_shift_arbitrary_135pt,color_shift_adjacent_135pt -o konica.csv
