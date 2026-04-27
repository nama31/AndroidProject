# Opening & running AlarmX in Android Studio

> A step-by-step guide for someone who has **never opened Android Studio before**.
> If you already build Android apps daily, jump straight to [§4 Sync Gradle](#4-sync-gradle) and the [Troubleshooting](#troubleshooting) section.

---

## What you'll end up with

By the end of this guide:

- Android Studio installed and configured.
- The AlarmX project opened, indexed, and built.
- An Android emulator (or your real phone) running the app.
- The dismiss flow working: a real alarm fires, takes over the lock screen, plays a ringtone, and only stops once you solve an arithmetic problem.

It usually takes **20–40 minutes** the first time, mostly waiting for downloads.

---

## 1. Install Android Studio

1. Go to **<https://developer.android.com/studio>** and download the **latest stable** Android Studio for your operating system (Windows, macOS, or Linux).
   - **Recommended version:** Android Studio Ladybug Feature Drop (`2024.2.2`) **or newer**. AlarmX uses Kotlin `2.2.10` and Compose BOM `2024.09.00`, which need a reasonably recent Studio.
   - The installer comes with a **bundled JDK 21** — you do **not** need to install Java yourself.
2. Run the installer and accept the defaults. On the first launch, the **Setup Wizard** will:
   - Download the latest **Android SDK** (`compileSdk = 36`, `minSdk = 24`).
   - Download a default **system image** (used by the emulator).
   - Create an `.android` folder in your home directory.

   Click **Next → Next → Finish** and let it run. Expect **5–10 GB of downloads** the first time.
3. When you reach the **Welcome to Android Studio** window, you're ready.

---

## 2. Get the source code

Pick **one** of:

### Option A — clone via Android Studio (easy, recommended)

1. On the welcome screen click **Get from VCS** (or **File → New → Project from Version Control** if a project is already open).
2. Paste the repo URL: `https://github.com/nama31/AndroidProject.git`
3. Pick a directory on disk and click **Clone**.
4. When Studio asks **"Trust this project?"** click **Trust Project**.

### Option B — clone with `git` and open later

```bash
git clone https://github.com/nama31/AndroidProject.git
```

Continue with [§3 Open the project](#3-open-the-project).

---

## 3. Open the project

> ⚠️ **Don't drag-and-drop the folder onto Android Studio.** That doesn't bind the Gradle build — you'll see a generic editor with red files. Always use **File → Open**.

1. **File → Open…** (or **Open** on the welcome screen).
2. Navigate to the cloned `AndroidProject` directory.
3. Select the **root folder** (the one that contains `settings.gradle.kts`, `gradle/`, `app/`). **Do not** drill into `app/`.
4. Click **OK**.
5. If prompted, choose **Trust Project**.

Android Studio will now open in a fresh window, show a **"Loading project…"** banner, and start indexing. After a few seconds the status bar at the bottom should switch to **"Gradle: sync started"**.

---

## 4. Sync Gradle

This is the most important step. Gradle needs to download every dependency declared in `gradle/libs.versions.toml` (Room, Compose, WorkManager, Coroutines, …) and the matching Android Gradle Plugin.

### How to tell sync is running

- **Bottom status bar** — shows `Gradle: …` with a spinning indicator. Hover for details.
- **Build tool window** (bottom-left, "Build" tab — `⌥6` on macOS, `Alt+6` on Windows/Linux) — opens a tree titled **"Sync"** with progress lines.
- **Notifications panel** (bell icon, top-right) — may show "Project requires JDK 21" or similar prompts. Always click **"Use embedded JDK"** when offered.

### How to tell sync **finished**

You're done when **all** of these are true:

- The bottom status bar reads **"Sync finished"** or shows nothing alarming.
- The Build tool window's Sync tab shows green checkmarks and no red errors.
- The **Project view** (left panel, `⌘1` / `Alt+1`) switches from a flat list of files to the structured tree (`app`, `Gradle Scripts`, etc.) — that's the cue Gradle has finished modelling the build.
- Files are no longer marked red in the Project view.

### How long it takes

- **First sync on a fresh machine:** 5–15 minutes (downloading AGP, Kotlin, Compose, Room, …).
- **Subsequent syncs (already cached):** 10–30 seconds.

### If sync fails

Don't panic — see the [Troubleshooting](#troubleshooting) section below. The most common causes are JDK mismatch and missing internet access.

---

## 5. Set up a device

You need either an **emulator (AVD)** or a **physical Android device**. Either works — pick whichever is more convenient.

### 5A — Create an Android emulator (AVD)

1. **Tools → Device Manager** (or click the phone icon in the top-right toolbar).
2. Click **+ Create Virtual Device**.
3. Pick a phone profile. **Pixel 7** or **Pixel 8** is a safe default — they have realistic screen sizes and good performance.
4. Click **Next**, then pick a system image:
   - Recommended: **API 34 (UpsideDownCake)** or **API 35 (VanillaIceCream)** — these are close to AlarmX's `targetSdk = 36` and reproduce the real `USE_FULL_SCREEN_INTENT` and `POST_NOTIFICATIONS` runtime behaviour.
   - If the image hasn't been downloaded yet, click the **⬇ Download** link next to it. This takes 5–15 minutes.
5. Click **Next → Finish**.
6. Back in **Device Manager**, click the **▶ Play** icon next to your new AVD. The emulator window opens — wait until you see the home screen.

> 💡 **Tip:** Emulators run noticeably faster on a machine with hardware virtualization enabled (Intel HAXM / Hyper-V on Windows, KVM on Linux, Hypervisor Framework on macOS — auto-enabled on M-series Macs).

### 5B — Use a real phone over USB (recommended for the alarm flow)

The full alarm experience — lock-screen takeover, full-screen intent, ringtone — is **best tested on a real device**. Emulators have quirks around `setShowWhenLocked` and audio routing.

1. **On the phone:** open **Settings → About phone**, find **Build number**, and tap it **7 times** until you see "You are now a developer!". This unlocks **Developer options**.
2. **Settings → System → Developer options → USB debugging** → toggle **ON**.
3. Plug the phone into your computer with a **data-capable** USB cable (some cables are charge-only — if Android Studio doesn't see the device, swap the cable).
4. On the phone, tap **"Allow USB debugging"** when the prompt appears, and check **"Always allow from this computer"**.
5. In Android Studio, look at the **device picker** at the top of the toolbar — your phone's model name should appear within ~15 seconds.

### 5C — Use a real phone over Wi-Fi (Android 11+)

1. Pair the phone via the cable first (steps 1–4 in §5B).
2. **Developer options → Wireless debugging** → turn on. Tap **"Pair device with pairing code"** to get an IP, port, and code.
3. In Android Studio: **Device Manager → Pair using Wi-Fi** → enter the pairing code.
4. From now on you can run wirelessly even after unplugging the cable.

---

## 6. Run the app

1. In the toolbar, the **device picker** (left of the green Run button) should show your AVD or phone. Pick it if it's not already selected.
2. Click the green **Run ▶** button (or press **Shift+F10** / **Ctrl+R**).
3. Watch the **Run** tool window at the bottom — you'll see:
   - `Executing tasks: [:app:assembleDebug]` (Gradle build)
   - `Installing APK 'app-debug.apk' on '…'`
   - `Launching activity com.example.mytest/.MainActivity`
4. The app appears on the device. The first launch shows an **empty alarm list**.

### Granting permissions on first launch

On Android 13 (API 33) and newer, the app immediately requests **POST_NOTIFICATIONS** — tap **Allow**. Without it the alarm notification (and full-screen intent) won't appear.

On Android 14 (API 34) and newer, **`USE_FULL_SCREEN_INTENT`** may default to **off** for non-default-launcher apps. If the dismiss UI doesn't appear when an alarm fires:

- **Settings → Apps → AlarmX → Notifications → "Full screen notifications"** → toggle **ON**.

### Testing the alarm flow

1. In the app, tap the **+** floating action button. This is a prototype shortcut: it schedules an alarm for **`now + 1 minute`**.
2. Lock the device.
3. Wait up to a minute.
4. The screen should turn on, the keyguard should be bypassed, the dismiss UI should appear, and the ringtone should play.
5. Solve the arithmetic problem to dismiss. Try a wrong answer first — you should see a horizontal shake and a fresh problem.

---

## Troubleshooting

### A. "Module SDK is not defined" / "Project SDK is not defined"

**Symptom:** All Kotlin files are marked red. Studio shows a banner asking for an SDK.

**Fix:** **File → Project Structure → SDK Location**:

- **Gradle JDK** → pick **Embedded JDK (jbr-21)**. AlarmX uses AGP 9.x which requires JDK 17+; the embedded JDK is the safest choice.
- **Android SDK Location** → should auto-fill. If not, click **Edit** and pick `~/Android/Sdk` (Linux/macOS) or `%LOCALAPPDATA%\Android\Sdk` (Windows).

Then **File → Sync Project with Gradle Files**.

---

### B. Gradle sync failed: "Unsupported class file major version" or "Could not target platform"

**Symptom:** sync fails immediately with a class-file or JVM-target error.

**Cause:** Studio is using a JDK older than 17. AGP 9 requires JDK 17 minimum; AlarmX is built and tested with the embedded JDK 21.

**Fix:** **File → Settings → Build, Execution, Deployment → Build Tools → Gradle** (on macOS: **Android Studio → Settings → …**). Set **Gradle JDK** to **Embedded JDK (jbr-21)** or any installed JDK 17+. Click **OK**, then **File → Sync Project with Gradle Files**.

---

### C. Gradle sync failed: "Could not resolve … (network error)"

**Symptom:** Gradle stalls or fails on a dependency download.

**Causes & fixes:**

1. **No internet / firewall** — Gradle needs `https://repo1.maven.org`, `https://dl.google.com`, and `https://plugins.gradle.org`. Make sure your network allows these.
2. **Corporate proxy** — **Settings → Appearance & Behavior → System Settings → HTTP Proxy** → set **Auto-detect** or **Manual** with your proxy details. Tick **"Apply settings to Gradle"**.
3. **Offline mode is on** — open the **Gradle** tool window (right edge), click the **🔌 Toggle Offline Mode** icon to turn it **off**.
4. **Stale cache** — close Studio, delete `~/.gradle/caches/modules-2`, reopen, sync again.

---

### D. Build failed: KSP / Room "cannot find symbol" or "schema export"

**Symptom:** First build after a fresh clone fails with errors mentioning `RoomDatabase`, `*_Impl`, or `Cannot find implementation for AppDatabase`.

**Cause:** KSP (Kotlin Symbol Processing) hasn't generated Room's implementation classes yet. This sometimes happens when Studio tries to compile before sync completes.

**Fix (in order):**

1. **Build → Clean Project**, then **Build → Rebuild Project**.
2. If it still fails: **File → Invalidate Caches… → Invalidate and Restart**.
3. If it *still* fails: from the project root run `./gradlew :app:kspDebugKotlin --info` in a terminal and read the actual error. Most often it's a Kotlin / KSP version mismatch — the project pins these in `gradle/libs.versions.toml` (`kotlin = "2.2.10"`, `ksp = "2.2.10-2.0.2"`); they must match.

---

### E. "Hilt / Dagger annotation not found" or "Cannot resolve symbol HiltAndroidApp"

AlarmX uses **Hilt** (`com.google.dagger.hilt.android 2.54`). If Android Studio highlights `@HiltAndroidApp`, `@HiltViewModel`, or `@AndroidEntryPoint` as unresolved after a fresh clone:

1. Sync Gradle — Hilt's annotation processor runs via KSP, and symbols aren't generated until the first successful sync.
2. If it still fails: **Build → Clean Project → Rebuild Project**.
3. If KSP is complaining: from the project root run `./gradlew :app:kspDebugKotlin --info` and read the actual error. Most Hilt issues come from a missing binding — the error message names the exact interface that has no `@Binds`/`@Provides` provider.

---

### F. Run failed: "INSTALL_FAILED_OLDER_SDK"

**Symptom:** Run completes the build but installation on the device fails.

**Cause:** the device's API level is below `minSdk = 24` (Android 7.0). AlarmX explicitly does not support older Androids.

**Fix:** use a device or emulator running **Android 7.0 or newer**. For the full alarm flow, prefer **Android 13+** (API 33+).

---

### G. "Cannot connect to daemon" / Gradle daemon crashes

**Symptom:** Gradle prints `Gradle build daemon disappeared unexpectedly` or hangs.

**Fix:**

1. Run `./gradlew --stop` in a terminal at the project root (this kills any stale daemons).
2. Increase the heap size: open `gradle.properties` at the project root and ensure it contains:
   ```
   org.gradle.jvmargs=-Xmx4g -XX:+HeapDumpOnOutOfMemoryError
   ```
3. Re-sync.

---

### H. The alarm fires but nothing appears on the lock screen

**Possible causes:**

1. **`POST_NOTIFICATIONS` denied** (API 33+) → **Settings → Apps → AlarmX → Permissions → Notifications → Allow**.
2. **`USE_FULL_SCREEN_INTENT` not granted** (API 34+) → **Settings → Apps → AlarmX → Notifications → "Full screen notifications" → ON**.
3. **OEM battery saver** killed the foreground service — Xiaomi, Huawei, OPPO and Samsung are notorious. **Settings → Apps → AlarmX → Battery → Allow background activity / Don't optimize**.
4. **Alarm scheduled in the past** — the FAB schedules `now + 1 min`, so the device clock must be reasonably accurate.
5. **Emulator quirks** — some emulators don't respect `setShowWhenLocked`. Test on a real device.

---

### I. The ringtone doesn't stop after I dismiss

**Cause:** the foreground service didn't receive the stop signal.

**Fix path to debug:**

1. Open **Logcat** in Android Studio (bottom toolbar) and filter by `tag:AlarmRingtoneService` and `tag:AndroidRingingController`.
2. On a correct answer you should see `Stop requested` in the service log.
3. If you see the log but the ringtone keeps playing, the `MediaPlayer.stop()` call is failing — file an issue with the Logcat output.

If as a last resort you need to silence a stuck ringtone, force-stop the app: **Settings → Apps → AlarmX → Force stop**.

---

### J. Compose preview doesn't render

**Symptom:** `@Preview` panes show "Render problem" with a red exclamation icon.

**Fix:** the in-IDE Compose preview is sensitive to versions and is **not required to run the app**.

1. Build the project at least once with **Build → Make Project**.
2. Click **Build & Refresh** at the top of the preview pane.
3. If it still fails, switch to **Interactive Preview** or **Run on Device** instead — those execute real Compose so they work even when the static preview chokes.

---

## Going further

- **Roadmap:** see [README → Roadmap](../README.md#roadmap).
- **Source map:** see [README → Project structure](../README.md#project-structure).
- **Architecture deep-dive:** see the original design doc (Sections 1–12) — the project was built directly against it.
