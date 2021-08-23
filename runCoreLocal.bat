@rem # Run Local Script
@rem # Builds the current project and copies the files to the
@rem # plugins folder of a pre installed minecraft server and
@rem # starts the server afterwards interactively
@echo off

set "ServerLocation=D:\Minecraft\Bukkit16"
set "ArtifactName=*-all.jar"
set "ServerJar=paper.jar"
set "JavaFile=C:\Users\gempl\.jdks\openjdk-16.0.1\bin\java.exe"
set "InitialDirectory=D:\Projects\Hopper"

set "Gradle=%InitialDirectory%\gradlew.bat"
set "Artifact=core\build\libs\%ArtifactName%"
set "PluginsFolder=%ServerLocation%\plugins\"
set "ServerStart=%ServerLocation%\%ServerJar%"

:init
echo Executing RunLocal Script
cd /D %InitialDirectory%
goto build

:build
echo Cleaning up previous Artifacts
call %Gradle% clean
echo Building shadowed Jar
call %Gradle% shadowJar
goto copy

:copy
echo Copying built artifact to plugins folder
xcopy "%Artifact%" "%PluginsFolder%" /Y
goto start

:start
cd /D %ServerLocation%
call "%JavaFile%" -Duser.dir=%ServerLocation% -jar "%ServerStart%" nogui
goto end

:fail
cd /D %InitialDirectory%
pause > nul

:end
cd /D %InitialDirectory%