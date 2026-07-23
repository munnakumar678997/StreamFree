# StreamFree

**Ad-free, privacy-first YouTube streaming — NewPipe clone for Android**

[![Build & Release APK](https://github.com/munnakumar678997/StreamFree/actions/workflows/build.yml/badge.svg)](https://github.com/munnakumar678997/StreamFree/actions/workflows/build.yml)

---

## ⬇️ Download Latest APK

<p align="center">
  <a href="https://github.com/munnakumar678997/StreamFree/releases/download/latest/StreamFree-latest.apk">
    <img src="https://img.shields.io/badge/⬇%20Download%20APK-Latest%20Build-brightgreen?style=for-the-badge&logo=android&logoColor=white" alt="Download APK" height="60"/>
  </a>
</p>

> **हर बार जब भी `main` branch पर कुछ push होगा, GitHub Actions अपने आप नया APK build करेगा और यह बटन हमेशा latest APK download करेगा।**

---

## 📲 Install करने का तरीका

1. ऊपर **Download APK** बटन दबाएं
2. APK phone में डाउनलोड होगा
3. Phone Settings → **Unknown sources** (या *Install unknown apps*) ON करें
4. APK file open करें → Install करें
5. Done! 🎉

---

## 🚫 Ad-Blocking कैसे काम करता है

NewPipe की exact technique copy की है:

```
YouTube को request जाती है →
  clientName: "ANDROID"
  clientVersion: "21.03.36"
  X-YouTube-Client-Name: 3

Response में आता है →
  streamingData.adaptiveFormats  ✅ (हम यही पढ़ते हैं)
  adPlacements                   ❌ (ignore)
  adSlots                        ❌ (ignore)
  playerAds                      ❌ (ignore)

→ कोई भी ad server contact नहीं होता
→ कोई भी ad नहीं आता
```

---

## ✨ Features

| Feature | Status |
|---------|--------|
| 🚫 Zero Ads | ✅ |
| ▶️ Background Play | ✅ |
| 📱 Picture-in-Picture | ✅ |
| 🪟 Popup Floating Player | ✅ |
| ⬇️ Video Download | ✅ |
| ⏭️ SponsorBlock (skip sponsors/intros) | ✅ |
| 👎 Return YouTube Dislike | ✅ |
| 📋 Watch History (local only) | ✅ |
| 🔔 Subscriptions (offline, no login) | ✅ |
| 🔖 Bookmarks / Watch Later | ✅ |
| 🔍 Search | ✅ |
| 🌙 AMOLED Dark Theme | ✅ |
| 🔗 YouTube URL deep links | ✅ |

---

## 🛠️ Auto Build System

हर `git push` पर GitHub Actions यह करता है:

```
Push to main
     │
     ▼
GitHub Actions (ubuntu-latest, JDK 17)
     │
     ├─ gradle assembleDebug
     │
     ├─ APK rename → StreamFree-latest.apk
     │
     └─ GitHub Release "latest" update
              │
              └─ Download बटन → हमेशा latest APK
```

**Manual build trigger:** [Actions tab → Run workflow](https://github.com/munnakumar678997/StreamFree/actions/workflows/build.yml)

---

## 🏗️ Tech Stack

- **Language:** Kotlin + Java
- **Extractor:** [NewPipeExtractor](https://github.com/TeamNewPipe/NewPipeExtractor)
- **Player:** Media3 / ExoPlayer
- **Async:** RxJava 3
- **Database:** Room
- **DI:** Koin
- **Network:** OkHttp + Retrofit
- **Images:** Glide
- **Navigation:** Navigation Component + SafeArgs

---

## 📦 Build खुद करना हो तो

```bash
git clone https://github.com/munnakumar678997/StreamFree.git
cd StreamFree
./gradlew assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk
```

**Requirements:** JDK 17, Android SDK (compileSdk 35, minSdk 23)

---

## 🔒 Privacy

- कोई login नहीं
- कोई Google account नहीं
- सब data सिर्फ आपके phone पर (Room DB)
- Cloud backup disabled
- Network requests: YouTube Innertube, SponsorBlock API, Return YouTube Dislike API — और कुछ नहीं

---

*NewPipe के open-source काम से inspired। StreamFree NewPipe का fork नहीं है, पर same privacy-first approach follow करता है।*
