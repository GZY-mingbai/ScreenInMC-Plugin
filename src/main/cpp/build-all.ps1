$env:JAVA_HOME="C:/Users/27857/.jdks/corretto-17.0.4"
$env:PATH="$env:PATH;$env:JAVA_HOME/bin:$env:JAVA_HOME/include;$env:JAVA_HOME/include/win32"
$env:PATH="$env:PATH;C:/Program Files/NVIDIA GPU Computing Toolkit/CUDA/v12.0/include"

Remove-Item .\out\ -Recurse -Force
New-Item -ItemType Directory -Path .\out\ -Force

$env:OpenCL_LIBRARY="C:/Program Files/NVIDIA GPU Computing Toolkit/CUDA/v12.0/lib/Win32"
Remove-Item .\work_dir\ -Recurse -Force
cmake . -B .\work_dir\ -G "Visual Studio 17 2022" -A Win32
cmake --build .\work_dir\ --config Release
Move-Item -Path .\work_dir\Release\ScreenInMC-CPP-Bridge.dll -Destination .\out\screen-in-mc-windows-i386.dll

$env:OpenCL_LIBRARY="C:/Program Files/NVIDIA GPU Computing Toolkit/CUDA/v12.0/lib/x64"
Remove-Item .\work_dir\ -Recurse -Force
cmake . -B .\work_dir\ -G "Visual Studio 17 2022" -A x64
cmake --build .\work_dir\ --config Release
Move-Item -Path .\work_dir\Release\ScreenInMC-CPP-Bridge.dll -Destination .\out\screen-in-mc-windows-amd64.dll

Remove-Item .\work_dir\ -Recurse -Force
wsl bash build-linux.sh
