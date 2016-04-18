
cd C:\Users\aleksandar.v\IdeaProjects\glo

sc \\calserver02 stop Tomcat7

ping 1.1.1.1 -n 1 -w 3000 > nul


ping 1.1.1.1 -n 1 -w 10000 > nul

xcopy target\glo.war v:\ /y /f
rmdir v:\glo /S /Q

ping 1.1.1.1 -n 1 -w 10000 > nul

sc \\calserver02 start Tomcat7