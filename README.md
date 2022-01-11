# MediaPlayer
[![](https://jitpack.io/v/Pisey-Nguon/MediaPlayer.svg)](https://jitpack.io/#Pisey-Nguon/MediaPlayer)

How to
To get a Git project into your build:

Step 1. Add the JitPack repository to your build file

*If your Gradle version below 7.0*
Add it in your root build.gradle at the end of repositories:

	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' } //add this line
		}
	}

*If your Gradle version from 7.0*
Add it in your root settings.gradle at the end of repositories:

    dependencyResolutionManagement {  
      repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)  
        repositories {  
		    google()  
            mavenCentral()  
            jcenter() // Warning: this repository is going to shut down soon  
		    maven { url 'https://jitpack.io' }  // add this line
     }}

Step 2. Add the dependency

dependencies {


    implementation 'com.github.Pisey-Nguon:MediaPlayer:1.0.3'

}
