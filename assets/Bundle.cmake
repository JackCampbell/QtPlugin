if(APPLE)
    set(CMAKE_PREFIX_PATH #MAC_PATH#/clang_64/lib/cmake/)
elseif(WIN32)
    set(CMAKE_PREFIX_PATH #WIN_PATH#\\5.8\\msvc\\lib\\cmake\\)
else()
    message("Undefined Platform")
endif()

set(CMAKE_CXX_STANDARD 11)

if(NOT ICON_NAME)
    set(ICON_NAME AppIcon)
endif()

if(APPLE)
    #set(CMAKE_OSX_SYSROOT macosx10.10)
    set(CMAKE_OSX_DEPLOYMENT_TARGET "10.10")
    set(ICON_FILE ${RESOURCE_FOLDER}/${ICON_NAME}.icns)
else()
    set(ICON_FILE ${RESOURCE_FOLDER}/${ICON_NAME}.ico)
endif()

include_directories(cmake-build-debug)
set(RESOURCE_FOLDER resources)

function(setup_deploy_target TARGET_NAME)
    if(APPLE)
        add_custom_target(Deploy
                COMMENT "Generating Deploy MacOSX"
                #COMMAND export PATH=$PATH:#MAC_PATH#/clang_64/bin
                COMMAND "#MAC_PATH#/clang_64/bin/macdeployqt" ${TARGET_NAME}.app -dmg
                WORKING_DIRECTORY ${CMAKE_RUNTIME_OUTPUT_DIRECTORY}
                DEPENDS ${TARGET_NAME})
    elseif(WIN32)
        add_custom_target(Deploy
                COMMENT "Generating Deploy Windows"
                #COMMAND set PATH="%PATH%;#WIN_PATH#/5.8/clang_64/bin"
                COMMAND "#WIN_PATH#\\5.4\\msvc2013\\bin\\windeployqt.exe" .
                WORKING_DIRECTORY ${CMAKE_RUNTIME_OUTPUT_DIRECTORY}
                DEPENDS ${TARGET_NAME})
    endif()
endfunction()

macro(qt_generator)
    # Find includes in corresponding build directories
    set(CMAKE_INCLUDE_CURRENT_DIR ON)
    # Instruct CMake to run moc automatically when needed.
    set(CMAKE_AUTOMOC ON) # For meta object compiler
    set(CMAKE_AUTORCC ON) # Resource files
    set(CMAKE_AUTOUIC ON) # UI files
endmacro()

macro(qt_bundle)
    if(APPLE)
        set(MACOSX_BUNDLE_INFO_STRING "${PROJECT_NAME} ${PROJECT_VERSION}")
        set(MACOSX_BUNDLE_BUNDLE_NAME ${PROJECT_NAME})
        set(MACOSX_BUNDLE_BUNDLE_VERSION ${PROJECT_VERSION})
        set(MACOSX_BUNDLE_LONG_VERSION_STRING ${PROJECT_VERSION})
        set(MACOSX_BUNDLE_SHORT_VERSION_STRING "${PROJECT_VERSION_MAJOR}.${PROJECT_VERSION_MINOR}")
        set(MACOSX_BUNDLE_COPYRIGHT ${COPYRIGHT})
        set(MACOSX_BUNDLE_GUI_IDENTIFIER ${IDENTIFIER})
        set(MACOSX_BUNDLE_ICON_FILE ${ICON_NAME}.icns)

        set(MACOSX_BUNDLE_RESOURCES "${CMAKE_CURRENT_BINARY_DIR}/${PROJECT_NAME}.app/Contents/Resources")
        set(MACOSX_BUNDLE_ICON "${CMAKE_SOURCE_DIR}/resources/${MACOSX_BUNDLE_ICON_FILE}")

        add_custom_target( OSX_BUNDLE
                COMMENT "Generating Bundle"
                COMMAND ${CMAKE_COMMAND} -E make_directory ${MACOSX_BUNDLE_RESOURCES}
                #COMMAND cp *.qm ${MACOSX_BUNDLE_RESOURCES}
                COMMAND ${CMAKE_COMMAND} -E copy_if_different ${MACOSX_BUNDLE_ICON} ${MACOSX_BUNDLE_RESOURCES})

        add_executable(${PROJECT_NAME} MACOSX_BUNDLE ${SOURCE_FILES} ${UI_HEADERS} ${UI_RESOURCES})
        add_dependencies(${PROJECT_NAME} OSX_BUNDLE)
        set_source_files_properties( ${ProjectName_RESOURCES} ${ProjectName_TRANSLATIONS} PROPERTIES MACOSX_PACKAGE_LOCATION Resources )
    elseif(WIN32)
        configure_file("${PROJECT_SOURCE_DIR}/cmake/windows_metafile.rc.in" "windows_metafile.rc")
        set(RES_FILES "windows_metafile.rc")
        set(CMAKE_RC_COMPILER_INIT windres)
        enable_language(rc)
        set(CMAKE_RC_COMPILE_OBJECT "<CMAKE_RC_COMPILER> <FLAGS> -O coff <DEFINES> -i <SOURCE> -o <OBJECT>")
        add_executable(${PROJECT_NAME} WIN32 ${SOURCE_FILES} ${UI_HEADERS} ${UI_RESOURCES})
        if (MSVC)
            set_target_properties(${PROJECT_NAME} PROPERTIES WIN32_EXECUTABLE YES LINK_FLAGS "/ENTRY:mainCRTStartup")
        endif()
    else()
        message("Undefined platform bundle")
    endif()
endmacro()