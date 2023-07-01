$env:JAVA_HOME=$env:JAVA_PATH
$env:PATH="$env:PATH;$env:JAVA_HOME/bin:$env:JAVA_HOME/include;$env:JAVA_HOME/include/win32"
$env:PATH="$env:PATH;$env:OpenCLPath"

Remove-Item .\out\ -Recurse -Force -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Path .\out\ -Force

$env:OpenCL_LIBRARY=$env:OpenCL86
Remove-Item .\work_dir\ -Recurse -Force -ErrorAction SilentlyContinue
cmake . -B .\work_dir\ -G "Visual Studio 17 2022" -A Win32
cmake --build .\work_dir\ --config Release
Move-Item -Path .\work_dir\Release\ScreenInMC-CPP-Bridge.dll -Destination .\out\screen-in-mc-windows-i386.dll

$env:OpenCL_LIBRARY=$env:OpenCL64
Remove-Item .\work_dir\ -Recurse -Force -ErrorAction SilentlyContinue
cmake . -B .\work_dir\ -G "Visual Studio 17 2022" -A x64
cmake --build .\work_dir\ --config Release
Move-Item -Path .\work_dir\Release\ScreenInMC-CPP-Bridge.dll -Destination .\out\screen-in-mc-windows-amd64.dll

Remove-Item .\work_dir\ -Recurse -Force -ErrorAction SilentlyContinue