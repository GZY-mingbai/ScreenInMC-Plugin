export JAVA_HOME="/usr/lib/jvm/java-17-openjdk-amd64/"
export PATH=/usr/bin/:/usr/lib/:/usr/lib/jvm/java-17-openjdk-amd64/include/:/usr/lib/jvm/java-17-openjdk-amd64/include/linux/
rm -rf work_dir
mkdir -p work_dir
/bin/cmake . -B work_dir -G "Unix Makefiles"
/bin/cmake --build work_dir --config Release
mv ./work_dir/libScreenInMC-CPP-Bridge.so ./out/screen-in-mc-linux-amd64.so

export CFLAGS="-m32"
export CXXFLAGS="-m32"
export LDFLAGS="-m32"
export LIBRARY_PATH="/usr/lib/i386-linux-gnu"
export LD_LIBRARY_PATH="/usr/lib/i386-linux-gnu"
rm -rf work_dir
mkdir -p work_dir
/bin/cmake . -B work_dir -G "Unix Makefiles"
/bin/cmake --build work_dir --config Release
mv ./work_dir/libScreenInMC-CPP-Bridge.so ./out/screen-in-mc-linux-i386.so
rm -rf work_dir