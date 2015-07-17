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
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.language.base.plugins.LifecycleBasePlugin
import org.gradle.model.*

/**
 *
 */
class MixcPlugin extends RuleSource implements Plugin<Project> {

    @Model("mixc")
    MixcModel mixc() { return new MixcModel() }

    // This is guaranteed to run after the configs are finalized,
    // but before the tasks are finalized.
    @Mutate
    void taskConfig(ModelMap<Task> tasks,
                    @Path("mixc") MixcModel mixcConfig) {
        // Prebuild phase.  We need all native builds (whether j2objc or custom) to occur
        // before our own xcode builds.
        tasks.create 'xcodePreBuildDebug', DefaultTask, {
            it.group = 'build'
            mixcConfig.j2objcProjects.each { String projName ->
                dependsOn "$projName:j2objcBuildDebug"
            }
            mixcConfig.nativeProjects.each { String projName ->
                dependsOn "$projName:build"
            }
        }
        tasks.create 'xcodePreBuildRelease', DefaultTask, {
            it.group = 'build'
            mixcConfig.j2objcProjects.each { String projName ->
                dependsOn "$projName:j2objcBuildRelease"
            }
            mixcConfig.nativeProjects.each { String projName ->
                dependsOn "$projName:build"
            }
        }
        tasks.create 'xcodePreBuild', DefaultTask, {
            it.dependsOn 'xcodePreBuildDebug'
            it.dependsOn 'xcodePreBuildRelease'
            it.group = 'build'
        }

        mixcConfig.projects.entrySet().each {
            def name = it.key
            def nameFirstUpper = "${name.toUpperCase()[0]}${name.substring(1)}"
            def val = it.value

            tasks.create "xcode${nameFirstUpper}BuildDebug", XcodeBuildTask, {
                config = 'Debug'
                dirPath = val.dir.absolutePath
                dependsOn 'xcodePreBuildDebug'
                xcodeProject = val.projectName
                sdk = val.sdk
                j2objcProjects = mixcConfig.j2objcProjects
                nativeProjects = mixcConfig.nativeProjects
                group = 'build'
            }

            tasks.create "xcode${nameFirstUpper}BuildRelease", XcodeBuildTask, {
                config = 'Release'
                dirPath = val.dir.absolutePath
                dependsOn 'xcodePreBuildRelease'
                xcodeProject = val.projectName
                sdk = val.sdk
                j2objcProjects = mixcConfig.j2objcProjects
                nativeProjects = mixcConfig.nativeProjects
                group = 'build'
            }

            tasks.get('assemble').dependsOn(
                    "xcode${nameFirstUpper}BuildDebug",
                    "xcode${nameFirstUpper}BuildRelease")

            tasks.create "xcode${nameFirstUpper}CleanDebug", XcodeBuildTask, {
                config = 'Debug'
                dirPath = val.dir.absolutePath
                taskType 'clean'
                xcodeProject = val.projectName
                sdk = val.sdk
                j2objcProjects = mixcConfig.j2objcProjects
                nativeProjects = mixcConfig.nativeProjects
                group = 'build'
            }

            tasks.create "xcode${nameFirstUpper}CleanRelease", XcodeBuildTask, {
                config = 'Release'
                dirPath = val.dir.absolutePath
                taskType 'clean'
                xcodeProject = val.projectName
                sdk = val.sdk
                j2objcProjects = mixcConfig.j2objcProjects
                nativeProjects = mixcConfig.nativeProjects
                group = 'build'
            }

            tasks.get('clean').dependsOn(
                    "xcode${nameFirstUpper}CleanDebug",
                    "xcode${nameFirstUpper}CleanRelease")

            if (val.testTarget != null) {
                tasks.create "xcode${nameFirstUpper}TestDebug", XcodeBuildTask, {
                    dependsOn "xcode${nameFirstUpper}BuildDebug"
                    config = 'Debug'
                    dirPath = val.dir.absolutePath
                    taskType 'test'
                    xcodeProject = val.projectName
                    sdk = val.sdk
                    j2objcProjects = mixcConfig.j2objcProjects
                    nativeProjects = mixcConfig.nativeProjects
                    group = 'verification'
                    scheme = val.testTarget
                    target = val.testTarget
                }

                tasks.create "xcode${nameFirstUpper}TestRelease", XcodeBuildTask, {
                    dependsOn "xcode${nameFirstUpper}BuildRelease"
                    config = 'Release'
                    dirPath = val.dir.absolutePath
                    taskType 'test'
                    xcodeProject = val.projectName
                    sdk = val.sdk
                    j2objcProjects = mixcConfig.j2objcProjects
                    nativeProjects = mixcConfig.nativeProjects
                    group = 'verification'
                    scheme = val.testTarget
                    target = val.testTarget
                }

                tasks.get('check').dependsOn(
                        "xcode${nameFirstUpper}TestDebug",
                        "xcode${nameFirstUpper}TestRelease")
            }
        }
    }

    @Override
    void apply(Project project) {
        // We need clean, assemble, check, and build tasks.
        project.pluginManager.apply(LifecycleBasePlugin)
    }
}
