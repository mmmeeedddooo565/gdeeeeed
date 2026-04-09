@echo off
where gradle >nul 2>nul
if %errorlevel% neq 0 (
  echo Gradle is not installed in this environment.
  exit /b 1
)
gradle %*
