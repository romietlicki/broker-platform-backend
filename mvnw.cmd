@echo off

set ERROR_CODE=0

@REM ==== VALIDACAO JAVA_HOME ====
if not "%JAVA_HOME%" == "" goto OkJHome

echo.
echo Erro: JAVA_HOME nao encontrado. Configure a variavel de ambiente JAVA_HOME. >&2
echo Exemplo: $env:JAVA_HOME = "C:\Program Files\Java\jdk-17.0.11_windows-x64_bin\jdk-17.0.11" >&2
echo.
goto error

:OkJHome
if exist "%JAVA_HOME%\bin\java.exe" goto init

echo.
echo Erro: JAVA_HOME aponta para um diretorio invalido. >&2
echo JAVA_HOME = "%JAVA_HOME%" >&2
echo.
goto error

:init

set WRAPPER_JAR=%~dp0.mvn\wrapper\maven-wrapper.jar
set WRAPPER_LAUNCHER=org.apache.maven.wrapper.MavenWrapperMain
set DOWNLOAD_URL=https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.3.2/maven-wrapper-3.3.2.jar

FOR /F "usebackq tokens=1,2 delims==" %%A IN ("%~dp0.mvn\wrapper\maven-wrapper.properties") DO (
    IF "%%A"=="wrapperUrl" SET DOWNLOAD_URL=%%B
)

if exist "%WRAPPER_JAR%" goto execute

echo Baixando Maven Wrapper de %DOWNLOAD_URL%...
powershell -Command "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; (New-Object Net.WebClient).DownloadFile('%DOWNLOAD_URL%', '%WRAPPER_JAR%')"

if not exist "%WRAPPER_JAR%" (
    echo Erro: Falha ao baixar maven-wrapper.jar >&2
    goto error
)
echo Download concluido.

:execute
set MAVEN_PROJECTBASEDIR=%~dp0
if "%MAVEN_PROJECTBASEDIR:~-1%"=="\" set MAVEN_PROJECTBASEDIR=%MAVEN_PROJECTBASEDIR:~0,-1%

"%JAVA_HOME%\bin\java.exe" -classpath "%WRAPPER_JAR%" -Dmaven.multiModuleProjectDirectory="%MAVEN_PROJECTBASEDIR%" %WRAPPER_LAUNCHER% %*

if ERRORLEVEL 1 goto error
goto end

:error
set ERROR_CODE=1

:end
exit /B %ERROR_CODE%
