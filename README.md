# FlexChatBox Android

## What It Is

`Flexchatbox` is an Android SDK developed with the intention to reduce the effort for the developers
to build the chat functionality in their applications. The developers can simply integrate this SDK
into their applications and use the features provided such as sending text messages and also sharing
the media such as gallery, captured photos and videos and sharing the current location and mobile
contacts and voice recording clips and all types of files from the file manager.

## Importing the Library

On your root `build.gradle`, add `mavenCentral()` to the `allprojects` section.
On your module `build.gradle`, add

    dependencies {
        implementation 'in.tilicho.android:flexchatbox:1.0.0'
    }

## Usage(</>)

```
    ChatBox(
            context = context,
            source = selectedFlex,
            selectedPhotosOrVideos = {
                /* Selected photos or videos callback */
            },
            recordedAudio = {
                /* recorded audio callback */
            },
            onClickSend = { 
                /* Chat text callback */
            },
            currentLocation = {
                /* current location callback */
            },
            selectedContacts = {
                /* selected contacts callback */
            },
            selectedFiles = {
                /* selected files callback */
            },
            camera = {
                /* captured image or video callback */
            }
        )
```

## Developer instructions

- From this SDK the developer can integrate only one type of media item at a time.
- The developer can place the SDK UI item anywhere on the screen.

## Features

The feature set includes:

- Can get text messages and integrated type of media message from the SDK. We have integrated
  different types of media messages which are required to chat conversation.
  ### `Media message types:`
    - `TextField`
        - The user can send a text message single line or multiple lines
        - ![chatText](https://user-images.githubusercontent.com/113417724/231669426-692018a6-7826-4f8f-a018-9dd84b4649a0.gif)

    - `Camera`
        - The user can expect the Uri of the captured photo or video from this callback.

        - In this feature we have the default device preview for captured image and video
        - ![camera](https://user-images.githubusercontent.com/113417724/231696519-5b3668c6-0591-48aa-bdce-cb128b996a94.gif)

    - `Gallery`
        - The user can expect the list of Uri's from this callback.

        - In this feature, we have implemented a custom gallery preview to show the glance of
          selected gallery items.
        - ![gallery](https://user-images.githubusercontent.com/113417724/231698598-22bc6c45-3156-4ff8-987e-1daaf5a8d8ef.gif)

    - `Voice recording`
        - The user can expect the file with recorded audio.
        - In this audio recording feature we record the audio with long-press the microphone icon
        - There is also recorded audio preview. In this preview user can decide whether to send or
          delete.
        - ![audio](https://user-images.githubusercontent.com/113417724/231706822-3b588a94-62af-4584-8f27-37eb0190faed.gif)

    - `Current location`
        - The user can expect the custom location object that contains the location object provided
          by android os and the embedded link with the current latitude and longitude values that
          redirects to the google maps.
        - ![location](https://user-images.githubusercontent.com/113417724/231700998-84d77183-dd9f-4a0c-ad63-69c48f2a5a94.gif)

    - `Contacts`
        - The user can expect the list of ContactData that consists of name, id, mobile number etc.
        - In this feature we have implemented custom contacts preview.
        - ![contacts](https://user-images.githubusercontent.com/113417724/231708586-58618da9-d408-477b-85a9-fe377512cba2.gif)

    - `Files`
        - The user can expect the list of Uri's of the selected files from the file manager of the
          device.
        - ![files](https://user-images.githubusercontent.com/113417724/231709421-a552e8a3-c707-4fc4-9713-06b7b11e42b1.gif)

## Licence

`MIT License`

Copyright (c) 2023 Tilicho Labs

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.