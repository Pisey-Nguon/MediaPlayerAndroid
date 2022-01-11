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
            jcenter()  
            maven { url 'https://jitpack.io' }  //add this line
     }}

Step 2. Add the dependency

dependencies {


    implementation 'com.github.Pisey-Nguon:MediaPlayer:1.0.4'

}

Step 3. let see on the example project to understand about the implementation

## Preview
![photo_2022-01-11 16 41 31](https://user-images.githubusercontent.com/47247206/148918695-ca4a3bec-5c3b-4abd-9d38-10faa1d7f5c8.jpeg)

![photo_2022-01-11 16 41 33](https://user-images.githubusercontent.com/47247206/148918723-bbb901e5-2575-41e7-9273-ee1371de58bf.jpeg)

![photo_2022-01-11 16 44 50](https://user-images.githubusercontent.com/47247206/148919258-cf515ce2-323f-4dc5-bc04-406eef43848f.jpeg)

