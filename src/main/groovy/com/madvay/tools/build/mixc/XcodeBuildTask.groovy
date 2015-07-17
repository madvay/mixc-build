/*
 * Copyright (c) 2015 by Advay Mengle.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.madvay.tools.build.mixc
import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.*

class XcodeBuildTask extends DefaultTask {

    @Input
    List<String> j2objcProjects

    @Input
    List<String> nativeProjects

    @InputFiles
    @SuppressWarnings("GroovyUnusedDeclaration")
    FileCollection getInputBuildDirs() {
        List<String> dirs = []
        dirs.addAll j2objcProjects.collect({ String projectName ->
            "${project.project(projectName).buildDir}/j2objcOutput"
        })
        dirs.addAll nativeProjects.collect({ String projectName ->
            "${project.project(projectName).buildDir}/binaries"
        })
        dirs.addAll nativeProjects.collect({ String projectName ->
            "${project.project(projectName).buildDir}/packedBinaries"
        })
        return project.files(dirs)
    }

    @InputFiles
    @Optional
    @SuppressWarnings("GroovyUnusedDeclaration")
    FileCollection additionalInputFiles

    @SuppressWarnings("GroovyUnusedDeclaration")
    @InputFiles
    FileCollection getInputXcodeFiles() {
        return project.fileTree(dir, {
            include '**/*'
            exclude '**/build'
        })
    }

    @Input
    String config
    @Input
    String taskType = 'build'
    @Input
    String sdk
    @Input
    String xcodeProject
    @InputDirectory
    File dir
    @Input
    @Optional
    String target
    @Input
    @Optional
    String scheme

    @Input
    String getBuildDirectoryName() {
        if (sdk == 'macosx') {
            return config
        } else {
            return "$config-$sdk"
        }
    }

    @OutputDirectory
    @SuppressWarnings("GroovyUnusedDeclaration")
    File getOutputDirMeta() {
        return new File(dir, "build/${xcodeProject}.build/$buildDirectoryName")
    }

    @OutputDirectory
    @SuppressWarnings("GroovyUnusedDeclaration")
    File getOutputDirMain() {
        return new File(dir, "build/$buildDirectoryName")
    }

    @TaskAction
    @SuppressWarnings("GroovyUnusedDeclaration")
    void build() {
        def output = new ByteArrayOutputStream()
        try {
            project.exec {
                workingDir dir
                executable 'xcrun'
                args 'xcodebuild'
                if (target != null) {
                    args '-target', target
                } else {
                    args '-alltargets'
                }
                if (scheme != null) {
                    args '-scheme', scheme
                }
                args '-configuration', config
                args '-sdk', sdk
                args '-parallelizeTargets', taskType
                standardOutput output
                errorOutput output
                environment 'WITHIN_IOS_APP_GRADLE_BUILD', 'YES'
            }
        } catch (e) {
            logger.error 'Failed to run xcodebuild:'
            logger.error output.toString()
            throw e
        }
    }
}
