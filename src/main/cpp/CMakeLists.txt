cmake_minimum_required (VERSION 3.8)

project ("ScreenInMC-CPP-Bridge" CXX)


set(SOURCES
    ScreenInMC.cpp
    ScreenInMC.h
#    RDPConnector.cpp
#    RDPConnector.h
)
set(CMAKE_BUILD_TYPE Release)
set(CMAKE_CXX_STANDARD 11)
set(CMAKE_CXX_STANDARD_REQUIRED True)


if (CMAKE_HOST_WIN32)
    set(WINDOWS 1)
elseif(CMAKE_HOST_APPLE)
    set(MACOS 1)
elseif(CMAKE_HOST_UNIX)
    set(LINUX 1)
endif()


if(WINDOWS)
  set(OpenCL_LIBRARY $ENV{OpenCL_LIBRARY})
  # set(FreeRDP_INCLUDE_DIRS $ENV{FreeRDP_INCLUDE_DIRS})
  # set(FreeRDP_LIBRARY $ENV{FreeRDP_LIBRARY})
  set(JNI_INCLUDE_DIRS $ENV{JNI_INCLUDE_DIRS})
  set(OpenCL_INCLUDE_DIRS $ENV{OpenCL_INCLUDE_DIRS})
  set(CMAKE_WINDOWS_EXPORT_ALL_SYMBOLS ON)
  set(Other_LIBRARIES "ws2_32")
endif()

# 查找 JNI 库
find_package(JNI REQUIRED)
include_directories(${JNI_INCLUDE_DIRS})
if(WINDOWS)
  include_directories(${JNI_INCLUDE_DIRS}/win32)
elseif(LINUX)
  include_directories(${JNI_INCLUDE_DIRS}/linux)
endif()

# 查找 OpenCL 库
find_package(OpenCL REQUIRED)
include_directories(${OpenCL_INCLUDE_DIRS})

# 查找 FreeRDP 库
# find_package(FreeRDP REQUIRED)
# include_directories(${FreeRDP_INCLUDE_DIRS}/freerdp2)
# include_directories(${FreeRDP_INCLUDE_DIRS}/winpr2)

# link_directories(${FreeRDP_LIBRARIES})

target_link_libraries(${PROJECT_NAME} 
  ${OpenCL_LIBRARY}
  # ${FreeRDP_LIBRARY}/freerdp2.lib
  # ${FreeRDP_LIBRARY}/freerdp-client2.lib
  # ${FreeRDP_LIBRARY}/winpr2.lib
  # ${FreeRDP_LIBRARY}/winpr-tools2.lib
  ${Other_LIBRARIES}
)

add_library(${PROJECT_NAME} SHARED ${SOURCES})

# 链接 OpenCL 库


if (CMAKE_VERSION VERSION_GREATER 3.12)
  set_property(TARGET ${PROJECT_NAME} PROPERTY CXX_STANDARD 20)
endif()