@echo off
set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-25.0.3.9-hotspot
set JAVAC=%JAVA_HOME%\bin\javac.exe
set JAR_TOOL=%JAVA_HOME%\bin\jar.exe
set CURL=%JAVA_HOME%\bin\curl.exe

echo Baixando Gson...
if not exist "libs" mkdir libs
curl -L "https://repo1.maven.org/maven2/com/google/code/gson/gson/2.10.1/gson-2.10.1.jar" -o "libs\gson.jar"

echo Compilando...
if not exist "build\classes" mkdir build\classes

"%JAVAC%" -source 21 -target 21 ^
  -cp "HytaleServer.jar;libs\gson.jar" ^
  -d "build\classes" ^
  src\main\java\com\nexify\hytale\*.java

if %ERRORLEVEL% neq 0 (
    echo ERRO na compilacao!
    pause
    exit /b 1
)

echo Copiando resources...
copy "src\main\resources\manifest.json" "build\classes\manifest.json"
copy "src\main\resources\config.json" "build\classes\config.json"

echo Criando JAR...
"%JAR_TOOL%" cf "NexifyHytale.jar" -C "build\classes" .

echo.
echo PRONTO! Arquivo gerado: NexifyHytale.jar
pause
