New-Item -ItemType Directory -Path FreeRDP -Force
cd FreeRDP
pip install meson
git clone -b v2.3.1 --depth=1 https://github.com/cisco/openh264.git
git clone -b OpenSSL_1_1_1t --depth=1 https://github.com/openssl/openssl.git
git clone -b 2.10.0 --depth=1 https://github.com/FreeRDP/freerdp.git
New-Item -ItemType Directory -Path openh264_build -Force
New-Item -ItemType Directory -Path openssl_build -Force
New-Item -ItemType Directory -Path freerdp_build -Force

New-Item -ItemType Directory -Path openh264_lib -Force
New-Item -ItemType Directory -Path openssl_lib -Force
New-Item -ItemType Directory -Path freerdp_lib -Force


meson setup openh264 openh264_build
$absolutePath1 = Convert-Path openh264_lib
meson configure openh264_build -Dprefix="${absolutePath1}" -Dbuildtype=release
cd openh264_build
ninja install
cd ../openssl_build
$absolutePath2 = Convert-Path ../openssl_lib
perl ..\openssl\Configure shared --prefix="${absolutePath2}" VC-WIN64A
nmake depend
nmake install_dev
cd ..
$env:PATH = "${env:PATH};${absolutePath1};${absolutePath2}"
$absolutePath3 = Convert-Path ./freerdp_lib
cmake -GNinja -Bfreerdp_build -Hfreerdp -DCMAKE_INSTALL_PREFIX="${absolutePath3}" -DCMAKE_BUILD_TYPE=Release -DCHANNEL_URBDRC=OFF
cmake --build ./freerdp_build --target install