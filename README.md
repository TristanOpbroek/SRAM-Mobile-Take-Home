# SRAM-Mobile-Take-Home
<sub>Tristan Opbroek</sub>

## Installation Instructions:
Uses Android Studio Meerkat
Minimum SDK API 24
Using Kotlin DSL as build config


1. Select Code->Download Zip
2. Unzip the project
3. Open Android Studio, navigate to File->Open
4. Open the SRAM-Mobile-Take-Home-main that exists in the same folder as /.idea; You may need to exclude these file from windows defender in Android Studio
5. If the project was loaded in the correct directory, a build configuration should already be setup.
6. Click the green play arrow at the top of Android Studio.

## Issues:
There is a threading issue while recieving the URI Redirect as part of the OAuth2 process. In this case, the main thread dies after handling and saving a user's Strava access token. When the app is relaunched, the token is read from storage.
