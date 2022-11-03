@echo off
echo. >remapping_.bat
del  /F /Q /S build\libs\ScreenInMC-obf.jar > nul
rd  /Q /S build\libs\remapped > nul
echo cd /d %~dp0 >> remapping_.bat
echo java -cp %USERPROFILE%\.m2\repository\net\md-5\SpecialSource\1.11.0\SpecialSource-1.11.0-shaded.jar;%USERPROFILE%\.m2\repository\org\spigotmc\spigot\1.19.2-R0.1-SNAPSHOT\spigot-1.19.2-R0.1-SNAPSHOT-remapped-mojang.jar net.md_5.specialsource.SpecialSource --live -i build\libs\ScreenInMC.jar -o build\libs\ScreenInMC-obf.jar -m %USERPROFILE%\.m2\repository\org\spigotmc\minecraft-server\1.19.2-R0.1-SNAPSHOT\minecraft-server-1.19.2-R0.1-SNAPSHOT-maps-mojang.txt --reverse >> remapping_.bat
echo java -cp %USERPROFILE%\.m2\repository\net\md-5\SpecialSource\1.11.0\SpecialSource-1.11.0-shaded.jar;%USERPROFILE%\.m2\repository\org\spigotmc\spigot\1.19.2-R0.1-SNAPSHOT\spigot-1.19.2-R0.1-SNAPSHOT-remapped-obf.jar net.md_5.specialsource.SpecialSource --live -i build\libs\ScreenInMC-obf.jar -o build\libs\remapped\ScreenInMC.jar -m %USERPROFILE%\.m2\repository\org\spigotmc\minecraft-server\1.19.2-R0.1-SNAPSHOT\minecraft-server-1.19.2-R0.1-SNAPSHOT-maps-spigot.csrg >> remapping_.bat
echo del  /F /Q /S remapping_.bat^>nul ^& exit >> remapping_.bat
remapping_.bat