# ことのは Android v0.5（第一版）

這是 Android 原生輔助 App 原始碼，**不是 APK**。Android 9 以上。使用你既有的 `https://darkcloud120.github.io/kotonoha/` 網站與原本的 AI Worker，不需修改網站、重新部署 Worker 或安裝電腦擴充功能。若網站實際路徑不同，請調整 MainActivity.java 的 SITE、HOST 以及路徑檢查。

## 用 GitHub 產生 APK（不需要 Android Studio）
1. 新建獨立的 GitHub repository，例如 `kotonoha-android`，可選 Private。不要上傳到原本的歌詞網站 repository。
2. 解壓縮本 ZIP，把 `app` 資料夾、`build.gradle`、`settings.gradle`、`README.md` 上傳到 repository 根目錄，Commit changes。確認 app/src/main/... 的階層完整。`.gitignore` 可選擇上傳。
3. ZIP 中的 `.github/workflows/build.yml` 也需要上傳。若瀏覽器看不到隱藏的 `.github` 資料夾，可在 GitHub 選 Add file → Create new file，檔名輸入 `.github/workflows/build.yml`，把 ZIP 內該檔案的內容複製貼上並 Commit。另附的 build-workflow.txt 內容完全相同。
4. 點 Actions → Build Kotonoha Android APK → Run workflow → 選預設分支 → Run workflow。若沒有工作流程，確認上一步檔名正確且已提交至預設分支。若 Actions 尚未開啟，依畫面啟用。
5. 工作流程顯示綠勾後，點開該次執行紀錄，在底部 Artifacts 下載 kotonoha-android-v0.5。解壓後得到 app-debug.apk。
6. 把 APK 傳到自己的 Android 手機，開啟安裝。Android 可能要求允許目前的檔案管理器安裝未知來源的應用程式。只安裝自己的建置成果，完成後可關閉該安裝權限。

## 手機使用
1. 開啟ことのは，點頂端「授權同步」，在 Android 的通知存取設定允許ことのは・音樂同步。Android 13 以上某些手機可能限制側載 App 的通知權限，若系統提示「受限制的設定」，請先核對來源是自己的 APK；在系統設定 → 應用程式 → ことのは → 右上角 ⋮（若有）→ 允許受限制的設定，再回 App 授權。各廠牌介面不同，若仍被封鎖請回報畫面。
2. 點頂端 YT Music 或自行開啟 YouTube Music App 播歌，切回ことのは。確認網站的「跟隨 YouTube Music」勾選。
3. 測試歌名、歌手、假名、同步進度、切歌、暫停與快轉。時間戳仍依歌詞來源，翻唱需校正。
4. 若要 AI 翻譯，網站內 AI 設定需在手機輸入已成功使用的新 Worker 網址與 ACCESS_TOKEN，勿輸入 OpenAI API key。先單首測試，再開自動翻譯。

## 隱私、限制與測試狀態
- Android 通知存取權限本身範圍較廣，但此 App 的通知服務沒有讀取或儲存通知內容。只在前景輪詢 YouTube Music 的 MediaSession，不錄音、不讀訊息、不要求無障礙權限；歌曲資訊透過本機 WebView 傳給你的既有網站。原網站的外部歌詞搜尋及 AI 翻譯仍會依設定連線。
- 網站開在 App 內的 WebView，和手機 Chrome、電腦的 localStorage / sessionStorage 不共用。翻譯快取和時間軸設定需要另存，密碼也需重新設定；跨裝置同步尚未做。
- 目前僅 Android 原生 YouTube Music App，不支援 iOS / Spotify 或手機瀏覽器播放。要能背景播放，仍須符合 YouTube Music 本身的帳號、方案及播放限制。若 MediaSession 不提供完整資訊，進度可能無法準確同步。尚未可靠識別廣告。
- App 保持前景時同步並防止螢幕自動休眠；離開或鎖屏會停止輪詢，回來時重新讀取狀態。
- GitHub Actions 產生的是 debug APK；各次建置的預設簽章可能不同，後續更新若簽章不相容就得卸載重裝（本機資料會消失）。正式版應配置固定私有簽章。
- 此環境無 Android SDK/Gradle 且無法連線下載，因此未編譯 APK、未在手機實測。GitHub Actions 成功建置及真機測試後才能確認功能。
