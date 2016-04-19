
db.unit.update({parentCode:null}, {$unset : { 
'Data @ 1mA WPE mean':1, 
'Data @ 10mA WPE mean' : 1,
'Data @ 1mA FWHM (nm) mean' : 1,
'Data @ 10mA FWHM (nm) mean' : 1,
'Data @ 1mA Peak (nm) mean' : 1,
'Data @ 10mA Peak (nm) mean' : 1,
'Data @ 1mA Int Power (W) mean' : 1,
'Data @ 10mA Int Power (W) mean' : 1,
'Data @ 1mA Pulse Voltage (V) mean' : 1,
'Data @ 10mA Pulse Voltage (V) mean'  :1 }}, false, true)
