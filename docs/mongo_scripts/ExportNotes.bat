

mongoexport -h calserver06:27017 -d glo -c history -q "{'parentCode':null,'audit.pctg':'nwLED','audit.tkey':'n_contact_liftoff','audit.SheetResistanceWitnessnContactDep': {$exists : 1}}" --csv --fields code,note -o notes.csv