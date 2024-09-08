# Multiplayer Mod - LR2oraja \~Endless Dream\~
A fork of [Endless Dream](https://github.com/seraxis/lr2oraja-endlessdream) that includes lobby multiplayer functionality and which aims to be non-intrusive and simple to use.

**Note: As of now, Discord Rich Presence has been removed and will not work in this fork due to me struggling to compile the game with it.**

### Multiplayer Mod Key Features
* Multiplayer menu found in the Endless Dream mod menu (F5/Insert)
* Peer-to-peer TCP connectivity
  * Automatically sync song select
  * Live score display with leaderboard
* Support for unlimited players in a lobby

### Planned Features
* Transfer host with support for multiple hosts
* Integration with other Endless Dream mods (such as same randoms or displaying rate)
* Live opponent target score
* Support for standalone servers
    * Arena with rank system
    * Browse public lobbies
* [LR2Arena](https://github.com/SayakaIsBaka/LR2Arena) crossplay (big maybe!)

### Downloads
Be sure to open port '5730' in order for it to work.
Download the latest release under the 'Releases' tab [here](https://github.com/TylerKeonine/lr2oraja-endlessdream/releases)

### Installing from scratch
If you don't have an existing beatoraja installation download the latest [`beatoraja-0.8.7 JRE`](https://mocha-repository.info/download/beatoraja0.8.7-jre-win64.zip) bundled version, or choose a version yourself from the [release page](https://mocha-repository.info/download.php).

## Building from source
A JDK 8 **with javafx** is required to build and run. Consider using [liberica JDK](https://bell-sw.com/pages/downloads/#jdk-8-lts)
Clone this repository with submodules
```sh
git clone --recurse-submodules git@github.com:seraxis/lr2oraja-endlessdream.git
```
Run the gradle wrapper for your operating system and specify your desired platform as a [gradle system property](https://docs.gradle.org/current/userguide/build_environment.html#sec:gradle_system_properties)

**Windows:**
```powershell
.\gradlew.bat core:shadowJar -Dplatform=windows
```
**Linux:**
```sh
./gradlew core:shadowJar -Dplatform=linux
```

This task will create a jar located in `dist/` that can be used with any working installation of the game.
### Testing changes
Use of an IDE, such as [Intellij](https://www.jetbrains.com/idea/download/other.html), is recommended for working on Endless Dream.

The gradle `core:runShadow` task can be used to quickly test and debug changes made to the project.

Configure the `runDir` system property to point to a beatoraja install or leave blank to have it run in the assets folder

**Windows:**
```powershell
.\gradlew.bat core:runShadow -Dplatform=windows -DrunDir="C:\beatoraja0.8.7"
```
