@echo off
echo ================================================
echo Fixing Java Version Configuration
echo ================================================
echo.

echo Step 1: Cleaning Maven project...
call mvn clean
echo.

echo Step 2: Reloading IntelliJ project...
echo Please follow these steps in IntelliJ IDEA:
echo.
echo 1. File ^> Invalidate Caches and Restart
echo 2. After restart, right-click on pom.xml ^> Maven ^> Reload Project
echo 3. File ^> Project Structure (Ctrl+Alt+Shift+S)
echo 4. Project Settings ^> Project:
echo    - Set SDK to: 17 (or download Java 17 if not available)
echo    - Set Language Level to: 17
echo 5. Project Settings ^> Modules:
echo    - Select your module
echo    - Set Language Level to: 17
echo 6. Click OK
echo.

echo Step 3: Alternative - Force Maven to use Java 17...
set JAVA_HOME=C:\Program Files\Java\jdk-17
call mvn clean compile
echo.

echo ================================================
echo Configuration Updated!
echo ================================================
echo.
echo If you still get errors, make sure:
echo 1. Java 17 is installed on your system
echo 2. JAVA_HOME environment variable points to Java 17
echo 3. IntelliJ Project SDK is set to Java 17
echo.

pause
