# One Button Player

**One Button Player** is an Android app that turns a *single button press* into full control over a
sequence of audio tracks. It is built to run in tandem with the
**[One Button Remote](https://github.com/devsoft-co-za/One_Button_Remote)** — a simple ESP32-C3
Bluetooth foot switch — so performers, presenters and accessibility users can trigger audio cues
without ever touching their phone.

> 📣 Read the announcement blog post:
> [Introducing One Button Player and One Button Remote](https://www.circusscientist.com/2025/10/31/introducing-one-button-player-and-one-button-remote/)

## How it works

1. **Create your shows** — pick MP3 files from your device and build a numbered list of events.
2. **Event types** — each event is a *start*, *stop*, *pause* or *resume* action for a chosen track.
   Start events can optionally **loop** their track.
3. **Save & organise** — keep multiple shows; the last used show is loaded automatically on launch.
4. **Play one-handed** — every press of the remote (or the on-screen button) fires the next event in
   the sequence.
5. **No remote?** Use the large button on the **Play** screen instead.

The app presents a clean three-screen interface:

| Screen | Purpose |
| --- | --- |
| **Play** | Shows the current status and the single on-screen trigger button. |
| **Configure** | Build and edit a show: select files, add/undo events, save or clear. |
| **Load** | Browse, load and delete saved shows. |

## Features

- Single-button control of an entire audio sequence.
- Start / stop / pause / resume events, with per-event looping.
- Multiple saved shows with persistent storage.
- Foreground media service with media-session support — lock-screen and notification controls.
- Bluetooth media-button support (the One Button Remote enumerates as a media button).
- Runs in tandem with the [One Button Remote](https://github.com/devsoft-co-za/One_Button_Remote)
  hardware (ESP32-C3 Super Mini, single debounced button).

## Requirements

- Android Studio (or the command line with a compatible Android SDK)
- JDK 17
- Android SDK **compileSdk 36** / **minSdk 25** / **targetSdk 36**

## Building

```bash
# Debug build
./gradlew assembleDebug

# Release build (unsigned — configure your own signing config to publish)
./gradlew assembleRelease
```

The debug APK is written to `app/build/outputs/apk/debug/`.

## Project layout

```
app/src/main/java/com/circusscientist/onebuttonplayer/
├── MainActivity.java            # Hosts the three-screen ViewPager2 UI
├── PlayFragment.java            # Play screen + on-screen button
├── ConfigureFragment.java       # Show editor (files, events, undo)
├── LoadFragment.java            # Saved-show browser
├── AudioPlayerManager.java      # MediaPlayer pool / playback engine
├── ShowManager.java             # Show persistence (JSON)
├── Show.java / ShowEvent.java / ShowFile.java
└── player/
    ├── PlayerService.java               # Foreground media service
    └── PlayerNotificationManager.java   # Media notification
```

## Related projects

- **One Button Remote** (hardware/firmware): <https://github.com/devsoft-co-za/One_Button_Remote>
- **Blog post**: <https://www.circusscientist.com/2025/10/31/introducing-one-button-player-and-one-button-remote/>

## License

This project is licensed under the **GNU General Public License v3.0 or later** — see the
[LICENSE](LICENSE) file for details.

## Author

Built by the **CircusScientist** — <https://www.circusscientist.com>.
If you find this useful, consider supporting the work on
[Patreon](https://www.patreon.com/c/CircusScientist).
