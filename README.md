# privo3-android-sdk
P3 Mobile SDK's for Android

Installation:

Add it in your root build.gradle at the end of repositories:
```
	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```
Add the dependency:
```
dependencies {
		implementation 'com.github.Privo:/privo3-android-sdk:{{release}}'
	}
```

Note:
{{release}} - current release version tag. For example 0.0.1

Alternativly you can install in manually:

Open your project in Android Studio
Download the library (using Git, or a zip archive to unzip)
Go to File > Import Module and import the library as a module
Right-click your app in project view and select "Open Module Settings"
Click the "Dependencies" tab and then the '+' button
Select "Module Dependency"
Select "SDK" (not SDK Project)

To verify manual intallation, check :
1) check settings.gradle file, it should contain:
```
include ':sdk'
project(':sdk').projectDir = new File(settingsDir, '../../privo3-android-sdk/sdk')
```
2) check build.gradle file, it shoul contain:
```
dependencies {
    ...
    implementation project(':sdk')
}
```

Documentation:

https://developer.privo.com/#doc_mobilekotlin-requirements