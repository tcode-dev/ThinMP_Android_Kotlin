# ThinMP_Android_Kotlin

`ThinMP` is a simple music player for Android.

## Demo

### Google Pixel 7 Pro
<img src="https://user-images.githubusercontent.com/42083313/225055784-6f8c4b38-d009-436a-8376-2a80dabd6318.png" width="156"> <img src="https://user-images.githubusercontent.com/42083313/213877960-a8da6a71-87f6-4837-ad33-0f6a4890e116.png" width="156"> <img src="https://user-images.githubusercontent.com/42083313/213878056-d4ddf2f2-ef7d-4b44-a1ce-1cea579d50be.png" width="156"> <img src="https://user-images.githubusercontent.com/42083313/213878010-3542e7d0-2558-4bf1-b90c-edf9ec624a76.png" width="156"> <img src="https://user-images.githubusercontent.com/42083313/231498862-fc5d6384-6cba-4e82-ba50-ae32b0a456e5.png" width="156">

<img src="https://user-images.githubusercontent.com/42083313/231499147-e93e9e6f-700e-408d-84f2-5e72c2a04a18.png" width="156"> <img src="https://user-images.githubusercontent.com/42083313/231499338-70139d71-cb6c-4cfd-82b2-2935c2fa5f6e.png" width="156"> <img src="https://user-images.githubusercontent.com/42083313/231499505-f088bd73-9f6a-445e-8e5c-c39f2c846b46.png" width="156"> <img src="https://user-images.githubusercontent.com/42083313/231499593-5299f943-b468-4d60-8032-3ff3d4afc64f.png" width="156"> <img src="https://user-images.githubusercontent.com/42083313/231499656-c40e95f5-bcb1-4286-92d7-5597382c36cf.png" width="156">

### Google Pixel Tablet
<img src="https://github.com/tcode-dev/ThinMP_Android_Kotlin/assets/42083313/b37d58d3-730d-463c-b17a-218458a080d5" width="200"> <img src="https://github.com/tcode-dev/ThinMP_Android_Kotlin/assets/42083313/e894b51b-ef3c-4d81-b653-2ae3543b7c4e" width="200"> <img src="https://github.com/tcode-dev/ThinMP_Android_Kotlin/assets/42083313/a9388694-c71d-49b6-b130-68e3f99decd2" width="200"> <img src="https://github.com/tcode-dev/ThinMP_Android_Kotlin/assets/42083313/992d3c44-203d-4db2-ad08-8a362778b07a" width="200">

<img src="https://github.com/tcode-dev/ThinMP_Android_Kotlin/assets/42083313/f6942688-e68b-4f92-868d-7b3df672e0cd" width="200"> <img src="https://github.com/tcode-dev/ThinMP_Android_Kotlin/assets/42083313/9be892a2-b23c-4ef2-b5f8-4822c28ed809" width="200"> <img src="https://github.com/tcode-dev/ThinMP_Android_Kotlin/assets/42083313/f61b8008-8012-4749-b3f0-718f099289ce" width="200"> <img src="https://github.com/tcode-dev/ThinMP_Android_Kotlin/assets/42083313/6b1d47cc-f16e-4566-ac74-7d27368387fe" width="200">
## Features

* device music play
* background play
* favorite artists
* favorite songs
* playlists
* shortcuts

## Environments

* Android Studio Flamingo | 2022.2.1 Patch 1
* Kotlin
* Gradle
* minSdkVersion 33
* targetSdkVersion 33
* Android Version 13
* Google Pixel 4a
* Google Pixel 7 Pro
* Google Pixel Tablet

## Libraries

* Accompanist - https://google.github.io/accompanist
* Coil - https://github.com/coil-kt/coil
* hilt - https://developer.android.com/training/dependency-injection/hilt-android
* Jetpack Compose - https://developer.android.com/jetpack/compose
* Material Icons - https://fonts.google.com/icons?selected=Material+Icons
* Realm - https://realm.io

## Permission

* Manifest.permission.READ_MEDIA_AUDIO
* Manifest.permission.POST_NOTIFICATIONS
* Manifest.permission.FOREGROUND_SERVICE

## Architecture

### View

`View` → `ViewModel` → `Service` → `Repository` → `Model`

### Register

`View` → `ViewModel` → `Register` → `Repository`
