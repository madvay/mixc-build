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

import org.gradle.util.ConfigureUtil

/**
 *
 */
class MixcModel {

    boolean debugEnabled = true
    boolean releaseEnabled = true

    List<String> nativeProjects = []
    List<String> j2objcProjects = []

    /** Adds the name of a j2objc project this Xcode project depends on */
    void j2objcProject(String name) {
        j2objcProjects.add name
    }

    /** Adds the name of a native project this Xcode project depends on */
    void nativeProject(String name) {
        nativeProjects.add name
    }

    static class XcodeProject {
        /** Name of the project for Gradle purposes */
        String name
        /** Xcode project name (ex. projectName.xcproj) */
        String projectName
        /** Directory containing the project and source files */
        File dir
        /** Sdk type for the build. */
        String sdk = 'iphonesimulator'
        /**
         * If non-null will run the test target with the scheme
         * of the same name.`
         */
        String testTarget = null
        /**
         * names of other XcodeProject this project depends on.
         */
        List<String> dependsOnXcodeProjects = []
        /**
         * Adds name of an XcodeProject this project depends on.
         */
        void dependsOnXcodeProject(String name) {
            dependsOnXcodeProjects.add name
        }
    }

    Map<String, XcodeProject> projects = [:]

    /** Defines a new Xcode project and configures it. */
    void xcodeProject(String name, @DelegatesTo(XcodeProject) Closure cl) {
        XcodeProject newP = new XcodeProject(name: name)
        projects.put name, newP
        ConfigureUtil.configure(cl, newP)
    }
}
