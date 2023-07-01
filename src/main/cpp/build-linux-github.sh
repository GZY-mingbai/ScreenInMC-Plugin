export JAVA_HOME="/usr/lib/jvm/java-17-openjdk-amd64/"
export PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/snap/bin:/usr/lib/jvm/java-17-openjdk-amd64/include/:/usr/lib/jvm/java-17-openjdk-amd64/include/linux/
make --version
gcc --version
g++ --version
cmake --version
rm -rf work_dir
mkdir -p work_dir
export OpenCL_LIBRARY=/lib/x86_64-linux-gnu/libOpenCL.so
/bin/cmake . -B work_dir
/bin/cmake --build work_dir --config Release
mv ./work_dir/libScreenInMC-CPP-Bridge.so ./out/screen-in-mc-linux-amd64.so
export CFLAGS="-m32"
export CXXFLAGS="-m32"
export LDFLAGS="-m32"
export LIBRARY_PATH="/usr/lib/i386-linux-gnu"
export LD_LIBRARY_PATH="/usr/lib/i386-linux-gnu"
export OpenCL_LIBRARY=/lib/i386-linux-gnu/libOpenCL.so
rm -rf work_dir
mkdir -p work_dir
/bin/cmake . -B work_dir
/bin/cmake --build work_dir --config Release
mv ./work_dir/libScreenInMC-CPP-Bridge.so ./out/screen-in-mc-linux-i386.so
rm -rf work_dir