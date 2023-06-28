# ThinMP_Android_Kotlin

`ThinMP` is a simple music player for Android.

## Demo
<img src="https://user-images.githubusercontent.com/42083313/225055784-6f8c4b38-d009-436a-8376-2a80dabd6318.png" width="156"> <img src="https://user-images.githubusercontent.com/42083313/213877960-a8da6a71-87f6-4837-ad33-0f6a4890e116.png" width="156"> <img src="https://user-images.githubusercontent.com/42083313/213878056-d4ddf2f2-ef7d-4b44-a1ce-1cea579d50be.png" width="156"> <img src="https://user-images.githubusercontent.com/42083313/213878010-3542e7d0-2558-4bf1-b90c-edf9ec624a76.png" width="156"> <img src="https://user-images.githubusercontent.com/42083313/231498862-fc5d6384-6cba-4e82-ba50-ae32b0a456e5.png" width="156">

<img src="https://user-images.githubusercontent.com/42083313/231499147-e93e9e6f-700e-408d-84f2-5e72c2a04a18.png" width="156"> <img src="https://user-images.githubusercontent.com/42083313/231499338-70139d71-cb6c-4cfd-82b2-2935c2fa5f6e.png" width="156"> <img src="https://user-images.githubusercontent.com/42083313/231499505-f088bd73-9f6a-445e-8e5c-c39f2c846b46.png" width="156"> <img src="https://user-images.githubusercontent.com/42083313/231499593-5299f943-b468-4d60-8032-3ff3d4afc64f.png" width="156"> <img src="https://user-images.githubusercontent.com/42083313/231499656-c40e95f5-bcb1-4286-92d7-5597382c36cf.png" width="156">

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

Manifest.permission.READ_MEDIA_AUDIO

## Architecture

### View

`View` → `ViewModel` → `Service` → `Repository` → `Model`

### Register

`View` → `ViewModel` → `Register` → `Repository`
