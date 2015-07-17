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
class MixcExtension {

    List<String> nativeProjects = []
    List<String> j2objcProjects = []

    void j2objcProject(String name) {
        j2objcProjects.add name
    }

    void nativeProject(String name) {
        nativeProjects.add name
    }

    static class XcodeProject {
        String name
        String projectName
        File dir
        String sdk = 'iphonesimulator'
    }

    Map<String, XcodeProject> projects = [:]

    void xcodeProject(String name, @DelegatesTo(XcodeProject) Closure cl) {
        XcodeProject newP = new XcodeProject(name: name)
        projects.put name, newP
        ConfigureUtil.configure(cl, newP)
    }
}
