@echo off
setlocal
cd /d "%~dp0"
java -jar "target\teacher-management-1.0.0.jar" 1>"backend_run.out.log" 2>"backend_run.err.log"
