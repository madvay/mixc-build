# mixc-build
Simple execution of Xcode iOS/Mac OS X builds in Gradle projects.

## Overview
This plugin lets you hook up Xcode projects into your Gradle projects and toolchain,
including adding dependencies from those Xcode projects to other Gradle projects,
such as those built using the
[Objective-C plugin](https://docs.gradle.org/current/userguide/nativeBinaries.html)
or other native build plugins.

If you have 1 or more Java Gradle projects you want to build as native Objective-C,
consider [j2objc](https://github.com/google/j2objc) (which converts Java to Objective-C)
and [j2objc-gradle](https://github.com/j2objc-contrib/j2objc-gradle)
(which incorporates j2objc in your Gradle builds).  Once you have j2objc-gradle projects,
you can hook them up to your Xcode builds too - which was indeed the original author's
use of this plugin.

## Usage
The plugin requires Gradle 2.5.  If using j2objc-gradle, version 0.3.0-alpha or higher is needed.

```
apply plugin: 'com.madvay.tools.build.mixc'

model {
    mixc {
        j2objcProject ':common1'
        j2objcProject ':common2'

        nativeProject ':native-objc1'
        nativeProject ':native-cpp2'

        xcodeProject 'ios', {
            // Do NOT put the Gradle project and the Xcode project in the same directory./
            // The build/ subdirectories will conflict.
            dir = file('../app-ios')
            projectName = 'app-ios'
            // Will not run tests by default.
        }

        xcodeProject 'osx', {
            dir = file('../app-mac')
            projectName = 'app-mac'
            sdk = 'macosx'
            // Will also run this target for unit tests.
            testTarget = 'app-macUnitTests'
            // Will depend on the 'ios' build above.
            dependsOnXcodeProject 'ios'
        }
    }
}
```

This will create tasks such as:

```
xcodePreBuildDebug
xcodePreBuildRelease
xcodePreBuild

xcodeIosBuildDebug
xcodeIosBuildRelease

xcodeIosCleanDebug
xcodeIosCleanRelease

xcodeOsxBuildDebug
xcodeOsxBuildRelease

xcodeOsxCleanDebug
xcodeOsxCleanRelease

xcodeOsxTestDebug
xcodeOsxTestRelease
```

The build tasks are made children of `assemble`, the clean tasks of `clean`,
and the test tasks of `check`.

## License
See [LICENSE](LICENSE).
See also the [NOTICE](NOTICE) file, per section 4 d of the License.

```
Copyright (c) 2015 by Advay Mengle.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this software except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
