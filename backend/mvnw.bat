@echo off
set "JAVA_HOME=C:\Program Files\Microsoft\jdk-21.0.13.102-hotspot"
set "PATH=%JAVA_HOME%\bin;%~dp0..\.tools\apache-maven-3.9.9\bin;%PATH%"
"%~dp0..\.tools\apache-maven-3.9.9\bin\mvn.cmd" %*
