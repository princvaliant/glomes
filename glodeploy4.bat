
cd C:\Miljenko\glo

sc \\calserver04 stop Tomcat7

ping 1.1.1.1 -n 1 -w 3000 > nul


ping 1.1.1.1 -n 1 -w 10000 > nul

xcopy target\glo.war t:\ /y /f
rmdir t:\glo /S /Q

ping 1.1.1.1 -n 1 -w 10000 > nul

sc \\calserver04 start Tomcat7