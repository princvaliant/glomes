0
.300
cd C:\Miljenko\glo

sc \\calserver03 stop Tomcat7

ping 1.1.1.1 -n 1 -w 3000 > nul


ping 1.1.1.1 -n 1 -w 10000 > nul

xcopy target\glo.war u:\ /y /f
rmdir u:\glo /S /Q

ping 1.1.1.1 -n 1 -w 10000 > nul

sc \\calserver03 start Tomcat7