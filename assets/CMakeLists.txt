cmake_minimum_required(VERSION 3.7)

set(CMAKE_MODULE_PATH ${CMAKE_CURRENT_SOURCE_DIR}/cmake)
include(cmake/Bundle.cmake)

project(#PROJECT# VERSION 1.4)
set(COMPANY "#COMPANY#")
set(COPYRIGHT "Copyright (c) #YEAR# #AUTHOR#. All rights reserved.")
set(IDENTIFIER "#IDENTIFIER#")

#PACKAGES#

set(CMAKE_INCLUDE_CURRENT_DIR ON)
qt_generator()

file(GLOB SOURCE_FILES *.h *.cpp)
file(GLOB UI_HEADERS *.ui)
file(GLOB UI_RESOURCES *.qrc)

#qt5_wrap_ui(UI_HEADERS ${UI_LIST})
#qt5_add_resources(UI_RESOURCES resources.qrc)

#set(SOURCE_FILES ${SRC_LIST})
#set(SOURCE_FILES ${SRC_LIST} ${UI_HEADERS} ${UI_RESOURCES})

qt_bundle()

target_link_libraries(#PROJECT# ${QT_LIBRARIES})
qt5_use_modules(#PROJECT# #MODULE#)

setup_deploy_target(#PROJECT#)