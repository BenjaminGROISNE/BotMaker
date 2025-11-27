package com.botmaker.project;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Handles creation of new Gradle projects with proper structure
 */
public class ProjectCreator {

    private static final Path PROJECTS_ROOT = Paths.get("projects");

    private final LibraryManager libraryManager = new LibraryManager();
    /**
     * Creates a new Gradle project with standard structure
     *
     * @param projectName The name of the project to create
     * @throws IOException if project creation fails
     * @throws IllegalArgumentException if project name is invalid or already exists
     */
    public void createProject(String projectName) throws IOException {
        // Validate project name
        validateProjectName(projectName);

        // Check if project already exists
        if (projectExists(projectName)) {
            throw new IllegalArgumentException("Project '" + projectName + "' already exists");
        }

        // Create project structure
        Path projectPath = PROJECTS_ROOT.resolve(projectName);
        createProjectStructure(projectPath, projectName);

        System.out.println("Successfully created project: " + projectName);
    }

    /**
     * Validates the project name
     */
    private void validateProjectName(String projectName) {
        if (projectName == null || projectName.trim().isEmpty()) {
            throw new IllegalArgumentException("Project name cannot be empty");
        }

        // Check for valid Java class name (starts with letter, contains only letters/digits)
        if (!projectName.matches("^[A-Z][a-zA-Z0-9]*$")) {
            throw new IllegalArgumentException(
                    "Project name must start with an uppercase letter and contain only letters and numbers"
            );
        }

        // Check length
        if (projectName.length() < 2 || projectName.length() > 50) {
            throw new IllegalArgumentException("Project name must be between 2 and 50 characters");
        }
    }

    /**
     * Checks if a project already exists
     */
    public boolean projectExists(String projectName) {
        Path projectPath = PROJECTS_ROOT.resolve(projectName);
        return Files.exists(projectPath);
    }

    /**
     * Creates the complete project structure
     */
    private void createProjectStructure(Path projectPath, String projectName) throws IOException {
        // Create directories
        Files.createDirectories(projectPath);

        String packageName = projectName.toLowerCase();
        Path srcPath = projectPath.resolve("src/main/java/com/" + packageName);
        Files.createDirectories(srcPath);

        Path gradlePath = projectPath.resolve("gradle/wrapper");
        Files.createDirectories(gradlePath);

        // Create build.gradle
        createBuildGradle(projectPath, projectName);

        // Create settings.gradle
        createSettingsGradle(projectPath, projectName);

        // Create gradle-wrapper.properties
        createGradleWrapperProperties(gradlePath);

        // Create gradlew scripts
        createGradlewScripts(projectPath);

        // Create main Java file
        createMainJavaFile(srcPath, projectName, packageName);

        // 2. Install Native Library
        libraryManager.installLibrary(projectPath);

        // 3. Create main Java file
        createMainJavaFile(srcPath, projectName, packageName);
    }

    /**
     * Creates build.gradle file
     */
    private void createBuildGradle(Path projectPath, String projectName) throws IOException {
        String packageName = projectName.toLowerCase();
                String content = String.format("""
            plugins {
                id 'java'
                id 'application'
            }
            
            group = 'com.%s'
            version = '0.0.1-SNAPSHOT'
            
            repositories {
                mavenCentral()
                google() // For Android ddmlib
            }
            
            dependencies {
                // JNA for Windows Interaction
                implementation 'net.java.dev.jna:jna:5.13.0'
                implementation 'net.java.dev.jna:jna-platform:5.13.0'
            
                // OpenCV
                implementation 'org.bytedeco:opencv-platform:4.7.0-1.5.9'
            
                // JSON processing
                implementation 'com.fasterxml.jackson.core:jackson-databind:2.15.2'
            
                // ADB Connection (ddmlib)
                // Note: You may need to adjust version based on availability
                implementation 'com.android.tools.ddms:ddmlib:30.0.0'
            }
            
            application {
                mainClass = 'com.%s.%s'
            }
            
            java {
                sourceCompatibility = JavaVersion.VERSION_17
                targetCompatibility = JavaVersion.VERSION_17
            }
            """, packageName, packageName, projectName);

        Files.writeString(projectPath.resolve("build.gradle"), content);
    }

    /**
     * Creates settings.gradle file
     */
    private void createSettingsGradle(Path projectPath, String projectName) throws IOException {
        String content = String.format("""
            
            pluginManagement {
                repositories {
                    gradlePluginPortal()
                    mavenCentral()
                }
            }
            
            
            
            rootProject.name = '%s'
            
            """, projectName);

        Files.writeString(projectPath.resolve("settings.gradle"), content);
    }

    /**
     * Creates gradle-wrapper.properties file
     */
    private void createGradleWrapperProperties(Path gradlePath) throws IOException {
        String content = """
            distributionBase=GRADLE_USER_HOME
            distributionPath=wrapper/dists
            distributionUrl=https\\://services.gradle.org/distributions/gradle-9.1.0-bin.zip
            networkTimeout=10000
            validateDistributionUrl=true
            zipStoreBase=GRADLE_USER_HOME
            zipStorePath=wrapper/dists
            """;

        Files.writeString(gradlePath.resolve("gradle-wrapper.properties"), content);
    }

    /**
     * Creates gradlew scripts (Unix and Windows)
     */
    private void createGradlewScripts(Path projectPath) throws IOException {
        // Create Unix gradlew script
        String gradlewUnix = """
            #!/bin/sh
            
            #
            # Copyright © 2015 the original authors.
            #
            # Licensed under the Apache License, Version 2.0 (the "License");
            # you may not use this file except in compliance with the License.
            # You may obtain a copy of the License at
            #
            #      https://www.apache.org/licenses/LICENSE-2.0
            #
            # Unless required by applicable law or agreed to in writing, software
            # distributed under the License is distributed on an "AS IS" BASIS,
            # WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
            # See the License for the specific language governing permissions and
            # limitations under the License.
            #
            
            ##############################################################################
            #
            #   Gradle start up script for POSIX generated by Gradle.
            #
            ##############################################################################
            
            # Attempt to set APP_HOME
            app_path=$0
            
            APP_HOME=${app_path%"${app_path##*/}"}
            APP_BASE_NAME=${0##*/}
            APP_HOME=$( cd -P "${APP_HOME:-./}" > /dev/null && printf '%s\\n' "$PWD" ) || exit
            
            DEFAULT_JVM_OPTS='"-Xmx64m" "-Xms64m"'
            
            JAVACMD=java
            if [ -n "$JAVA_HOME" ] ; then
                if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
                    JAVACMD=$JAVA_HOME/jre/sh/java
                else
                    JAVACMD=$JAVA_HOME/bin/java
                fi
            fi
            
            exec "$JAVACMD" "$@"
            """;

        Path gradlewPath = projectPath.resolve("gradlew");
        Files.writeString(gradlewPath, gradlewUnix);

        // Make it executable on Unix systems
        try {
            gradlewPath.toFile().setExecutable(true);
        } catch (Exception e) {
            System.err.println("Warning: Could not set gradlew as executable: " + e.getMessage());
        }

        // Create Windows gradlew.bat script
        String gradlewBat = """
            @rem
            @rem Copyright 2015 the original author or authors.
            @rem
            @rem Licensed under the Apache License, Version 2.0 (the "License");
            @rem you may not use this file except in compliance with the License.
            @rem You may obtain a copy of the License at
            @rem
            @rem      https://www.apache.org/licenses/LICENSE-2.0
            @rem
            @rem Unless required by applicable law or agreed to in writing, software
            @rem distributed under the License is distributed on an "AS IS" BASIS,
            @rem WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
            @rem See the License for the specific language governing permissions and
            @rem limitations under the License.
            @rem
            
            @if "%DEBUG%"=="" @echo off
            
            set DIRNAME=%~dp0
            if "%DIRNAME%"=="" set DIRNAME=.
            set APP_BASE_NAME=%~n0
            
            set DEFAULT_JVM_OPTS="-Xmx64m" "-Xms64m"
            
            set JAVA_EXE=java.exe
            if defined JAVA_HOME goto findJavaFromJavaHome
            
            %JAVA_EXE% -version >NUL 2>&1
            if %ERRORLEVEL% equ 0 goto execute
            
            :findJavaFromJavaHome
            set JAVA_HOME=%JAVA_HOME:"=%
            set JAVA_EXE=%JAVA_HOME%/bin/java.exe
            
            :execute
            "%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% "-Dorg.gradle.appname=%APP_BASE_NAME%" -jar "%DIRNAME%\\gradle\\wrapper\\gradle-wrapper.jar" %*
            
            :end
            if %ERRORLEVEL% equ 0 goto mainEnd
            
            :fail
            exit /b %ERRORLEVEL%
            
            :mainEnd
            """;

        Files.writeString(projectPath.resolve("gradlew.bat"), gradlewBat);
    }

    /**
     * Creates the main Java source file
     */
    private void createMainJavaFile(Path srcPath, String projectName, String packageName) throws IOException {
        String content = String.format("""
            package com.%s;
            
            public class %s {
                public static void main(String[] args) {
                    System.out.println("Hello from %s!");
                }
            }
            """, packageName, projectName, projectName);

        Files.writeString(srcPath.resolve(projectName + ".java"), content);
    }
}