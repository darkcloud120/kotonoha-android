# ことのは Android 1.0 RC1

Android 9+，沿用原本 https://darkcloud120.github.io/kotonoha/ 網站及 YT Music 系統播放資訊。一般 YouTube 偵測是電腦擴充新功能，Android 保持 YT Music。新版包含 v0.6 小選單、新圖示與純歌詞模式留白。

## 更新（不是 APK）
將 ZIP 解壓縮，把 app/ 整個拖曳到原本 kotonoha-android repository 根目錄的 Upload files 區域，保留完整路徑。原本根目錄 build.gradle 和 settings.gradle 可保持不變。若要固定簽章，另將 `.github/workflows/build.yml` 用網頁編輯器更新（內容也有獨立 TXT），原本成功的 workflow 仍可用來產生臨時 debug APK。

## 固定 APK 簽章（推薦在分享前完成，可選）
Android 更新需同一簽章。之前 GitHub Actions 產生的臨時 debug 金鑰可能已丟失，新的固定簽章可能需要最後一次卸載重裝。**不要先卸載**；先確定手機既有 AI 翻譯已同步到雲端，重要本機時間軸／收藏／假名資料另做備份（Android WebView 下載可能不支援）。

1. 在自己的電腦安裝 JDK 17（若已安裝則不用）。開 PowerShell，進到一個不會上傳 GitHub 的私人資料夾。
2. 生成固定 PKCS12 金鑰；密碼自己設定，不要傳給別人：

```powershell
keytool -genkeypair -v -keystore kotonoha.p12 -storetype PKCS12 -alias kotonoha -keyalg RSA -keysize 2048 -validity 10000
```

3. 把金鑰檔轉成 Base64 複製到剪貼簿：

```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes((Resolve-Path .\kotonoha.p12).Path)) | Set-Clipboard
```

4. 建議 repository 設 Private。GitHub Settings → Secrets and variables → Actions → New repository secret，建立 `KOTO_KEYSTORE_BASE64`（貼上剪貼簿），以及 `KOTO_STORE_PASSWORD`（步驟 2 的密碼）。不要把金鑰或密碼放入原始碼、commit、截圖。金鑰原檔及密碼請自己安全備份。剪貼簿清除。
5. 將新版 `.github/workflows/build.yml` 覆蓋舊 workflow。執行 Run workflow，下載 artifact 裡的 app-debug.apk；它仍叫 debug 但若兩個 secret 都有設定，會使用固定的私人簽章。
6. 後續每次用相同金鑰與更高 versionCode 編譯，就可正常覆蓋更新（除非 Android 另有安裝限制）。如果兩個 secrets 都未設定，仍可臨時測試，但不保證覆蓋更新。

不要把固定簽章金鑰傳給朋友；只分享已簽 APK，朋友也需要允許通知存取及自行設定 AI（可不設定）。

原始碼及 XML/YAML 已檢查，尚未實際編譯或真機測試；固定簽章需 GitHub 實際建置驗證。不要卸載現有正常 App 直到新 APK 已成功建置且完成資料備份。
