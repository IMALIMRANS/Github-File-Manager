<!-- ======================= HEADER ======================= -->
<div align="center">

<img src="https://capsule-render.vercel.app/api?type=waving&color=0:0D1117,50:238636,100:2F81F7&height=220&section=header&text=GitHub%20File%20Manager&fontSize=48&fontColor=ffffff&animation=fadeIn&fontAlignY=36&desc=Manage%20your%20repositories%20right%20from%20your%20phone&descSize=18&descAlignY=58" alt="GitHub File Manager banner" width="100%"/>

<a href="https://github.com/IMALIMRANS/Github-File-Manager">
  <img src="https://readme-typing-svg.demolab.com?font=Fira+Code&weight=600&size=20&pause=1200&color=2F81F7&center=true&vCenter=true&width=640&lines=Browse+public+%26+private+repositories;Edit+code+%26+commit+from+your+phone;Upload+and+extract+ZIP+files+in+one+tap;Multi-account.+Zero+servers.+100%25+open+source." alt="Typing animation of key features" />
</a>

<br/>

![Platform](https://img.shields.io/badge/Android-7.0%2B-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-100%25-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)
![Open Source](https://img.shields.io/badge/Open%20Source-Yes-238636?style=for-the-badge&logo=github&logoColor=white)

<br/>

<a href="https://github.com/IMALIMRANS/Github-File-Manager/releases/"><img src="https://img.shields.io/badge/⬇%20Download%20APK-GitHub%20Releases-238636?style=for-the-badge" alt="Download APK"/></a>
<a href="https://apkpure.com/Github-File-Manager/com.imalimrans.github.fileManager"><img src="https://img.shields.io/badge/Get%20it%20on-APKPure-1F6FEB?style=for-the-badge" alt="APKPure"/></a>
<a href="https://imalimrans.pages.dev/GithubFileManager"><img src="https://img.shields.io/badge/Visit-Official%20Website-8957E5?style=for-the-badge" alt="Website"/></a>

<br/><br/>

🌐 **English** · [বাংলা](README.bn.md)

</div>

<br/>

<!-- ======================= OVERVIEW ======================= -->
## 📖 Overview

**GitHub File Manager** is a modern, fast and secure open-source Android app that gives you full control of your GitHub repositories, files and directories — directly from your phone. Edit code, create files, upload ZIPs and manage branches without ever opening a desktop browser.

Works with both **private** and **public** repositories.

> 📦 **Package name:** `com.imalimrans.github.fileManager`

<br/>

<!-- ======================= SCREENSHOTS ======================= -->
## 📱 Screenshots

<div align="center">
<table>
  <tr>
    <td align="center"><img src="png/1.png" width="200" alt="Screenshot 1"/></td>
    <td align="center"><img src="png/2.png" width="200" alt="Screenshot 2"/></td>
    <td align="center"><img src="png/3.png" width="200" alt="Screenshot 3"/></td>
  </tr>
  <tr>
    <td align="center"><img src="png/4.png" width="200" alt="Screenshot 4"/></td>
    <td align="center"><img src="png/5.png" width="200" alt="Screenshot 5"/></td>
    <td align="center"><img src="png/6.png" width="200" alt="Screenshot 6"/></td>
  </tr>
</table>
</div>

<br/>

<!-- ======================= FEATURES ======================= -->
## ✨ Features

<table>
<tr>
<td width="50%" valign="top">

### 👥 Multi-Account Switcher
Save multiple GitHub accounts (Personal, Work, Organization) and switch between them in a single tap.

</td>
<td width="50%" valign="top">

### 🗂️ Repository Explorer
Browse all public and private repos. Search, see stars, forks, default branch and last update, and open any branch directly.

</td>
</tr>
<tr>
<td valign="top">

### 🌳 Recursive File Tree
Detailed tree with grid/list views, file search, breadcrumb navigation and file-type icons (Code, Markdown, Image, PDF, Config…).

</td>
<td valign="top">

### ✏️ Code Editor & Commits
Edit code and text files in-app, write a custom commit message and push straight to GitHub. Unsaved-changes tracking prevents lost work.

</td>
</tr>
<tr>
<td valign="top">

### 📝 Live Markdown Viewer
GitHub-style rendering for `README.md` and every `.md` file.

</td>
<td valign="top">

### 📦 ZIP Upload & Extraction
Pick a ZIP on your phone and extract + upload it into any repo folder in one go.

</td>
</tr>
<tr>
<td valign="top">

### 🗑️ Bulk Delete & File Actions
Select multiple files to delete at once. Rename files, create new files and new directories.

</td>
<td valign="top">

### ⚡ Smart Caching
In-memory caching with fast Retrofit network calls for quick loading.

</td>
</tr>
</table>

<br/>

<!-- ======================= GETTING STARTED ======================= -->
## 🚀 Getting Started

1. **Download** the latest APK from [GitHub Releases](https://github.com/IMALIMRANS/Github-File-Manager/releases/) or [APKPure](https://apkpure.com/Github-File-Manager/com.imalimrans.github.fileManager).
2. **Create a token** with all required scopes pre-selected:

   <a href="https://github.com/settings/tokens/new?scopes=repo,read:user,user:email,workflow&description=GitHub%20File%20Manager%20App">
     <img src="https://img.shields.io/badge/🔑%20Generate%20Token-Pre--configured%20scopes-F0883E?style=for-the-badge" alt="Generate token"/>
   </a>

3. **Paste** the token into the app and log in. Done 🎉

📘 Full walkthrough: [Setup Guide & Blog](https://imalimrans.pages.dev/blog-posts/GithubFileManagerapk)

<br/>

<!-- ======================= TOKEN SCOPES ======================= -->
## 🔑 Token Scopes Explained

| Scope | Why it's needed |
|:------|:----------------|
| `repo` | Full control of private and public repositories — view, edit, create and delete files. |
| `read:user` | Show your profile name, username and avatar, and verify your account. |
| `user:email` | Attach your GitHub email as Git authorship when you commit from the app. |
| `workflow` | Edit CI/CD files inside `.github/workflows`. |

<details>
<summary><b>⏳ Token expiration — what happens and how to avoid it (click to expand)</b></summary>

<br/>

**Why does GitHub set an expiry?**
For security: classic tokens default to 30/60/90 days and fine-grained tokens have a fixed lifetime, so a leaked token eventually becomes useless on its own.

**What happens when a token expires?**
1. The GitHub API returns `401 Unauthorized` / `Bad credentials`.
2. The app shows a clear "token expired" notice when loading or editing files.
3. Go to **Settings → Update Token**, paste a new token and you're instantly reconnected — no need to remove the account or reinstall.

**How to avoid expiry**
GitHub Settings → Personal Access Tokens (Classic) → **Expiration** → choose **No expiration**.

> ⚠️ Keep your token private and never share it with anyone.

</details>

<br/>

<!-- ======================= SECURITY ======================= -->
## 🔒 Security & Architecture

```mermaid
flowchart LR
    A["📱 Your Phone<br/>GitHub File Manager"] -- "HTTPS / TLS" --> B[("☁️ api.github.com<br/>Official REST API")]
    A -.- C["🔐 Private SharedPreferences<br/>Token & account metadata"]
```

| | |
|:--|:--|
| 🚫 **Zero intermediary server** | No backend or middleman database. All communication is directly between your phone and the official GitHub REST API (`https://api.github.com`), encrypted over HTTPS/TLS. |
| 🔐 **Local private storage** | Your Personal Access Token and account metadata live only in the app's private `SharedPreferences`. No other app or person can read them. |
| 🔍 **Open-source transparency** | 100% Kotlin + Jetpack Compose, fully public. Anyone can audit the code — no backdoors, no data tracking. |

<br/>

<!-- ======================= TECH STACK ======================= -->
## 🛠️ Tech Stack

<div align="center">

![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=flat-square&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=flat-square&logo=jetpackcompose&logoColor=white)
![Material 3](https://img.shields.io/badge/Material%20Design%203-757575?style=flat-square&logo=materialdesign&logoColor=white)
![Coroutines](https://img.shields.io/badge/Coroutines-7F52FF?style=flat-square&logo=kotlin&logoColor=white)
![Retrofit](https://img.shields.io/badge/Retrofit%202-48B983?style=flat-square)
![OkHttp](https://img.shields.io/badge/OkHttp%203-3E4348?style=flat-square)
![Moshi](https://img.shields.io/badge/Moshi-0E7490?style=flat-square)
![Coil](https://img.shields.io/badge/Coil%20Compose-F0883E?style=flat-square)

</div>

| Layer | Technology |
|:------|:-----------|
| Language | Kotlin 100% |
| UI | Jetpack Compose (Material Design 3) |
| Architecture | MVVM + Clean Data Layer |
| Async | Kotlin Coroutines + StateFlow / SharedFlow |
| Networking | Retrofit 2 + OkHttp 3 (Bearer Token Interceptor) |
| JSON | Moshi (Kotlin CodeGen & Reflect) |
| Image loading | Coil Compose |
| Target | Android 7.0+ (API 24+) |

<br/>

<!-- ======================= LINKS ======================= -->
## 🔗 Links

| | |
|:--|:--|
| 🌐 Website | [imalimrans.pages.dev/GithubFileManager](https://imalimrans.pages.dev/GithubFileManager) |
| 🧑‍💻 Developer portal | [imalimrans.pages.dev](https://imalimrans.pages.dev/) |
| 📘 Setup guide | [Blog post](https://imalimrans.pages.dev/blog-posts/GithubFileManagerapk) |
| 🔐 Privacy Policy | [Read](https://imalimrans.pages.dev/GithubFileManager/privacy-policy) |
| 📄 Terms & Conditions | [Read](https://imalimrans.pages.dev/GithubFileManager/terms) |
| 💻 Source code | [GitHub Repository](https://github.com/IMALIMRANS/Github-File-Manager) |
| ⬇️ Latest APK | [Releases](https://github.com/IMALIMRANS/Github-File-Manager/releases/) |

<br/>

## ⭐ Support

If this app helps you, please **star the repo** — it keeps the project going!

<div align="center">

<a href="https://github.com/IMALIMRANS/Github-File-Manager/stargazers"><img src="https://img.shields.io/github/stars/IMALIMRANS/Github-File-Manager?style=for-the-badge&logo=github&color=F0883E" alt="Stars"/></a>
<a href="https://github.com/IMALIMRANS/Github-File-Manager/network/members"><img src="https://img.shields.io/github/forks/IMALIMRANS/Github-File-Manager?style=for-the-badge&logo=github&color=2F81F7" alt="Forks"/></a>
<a href="https://github.com/IMALIMRANS/Github-File-Manager/issues"><img src="https://img.shields.io/github/issues/IMALIMRANS/Github-File-Manager?style=for-the-badge&logo=github&color=DA3633" alt="Issues"/></a>

<br/><br/>

Made with ❤️ by <a href="https://github.com/IMALIMRANS"><b>IMALIMRANS</b></a>

<img src="https://capsule-render.vercel.app/api?type=waving&color=0:2F81F7,50:238636,100:0D1117&height=110&section=footer" width="100%" alt="footer"/>

</div>
