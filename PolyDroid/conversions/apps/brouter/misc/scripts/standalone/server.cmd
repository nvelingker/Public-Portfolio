@echo off

REM BRouter standalone server
REM java -cp brouter.jar btools.brouter.RouteServer <segmentdir> <profile-map> <customprofiledir> <port> <maxthreads>

REM maxRunningTime is the request timeout in seconds, set to 0 to disable timeout
set JAVA_OPTS=-Xmx128M -Xms128M -Xmn8M -DmaxRunningTime=300
set CLASSPATH=../brouter.jar

java %JAVA_OPTS% -cp %CLASSPATH% btools.server.RouteServer ..\segments4 ..\profiles2 ..\customprofiles 17777 1
