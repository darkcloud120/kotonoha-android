# Android 1.0 RC4
先更新 GitHub Pages 網站到 RC4，再更新 Android 專案。
將 app/ 完整拖曳覆蓋至 kotonoha-android Repo；另確認根目錄 .github/workflows/build.yml 內容為本 ZIP 版本（網頁上傳可能略過隱藏資料夾，請手動覆蓋）。保留原有簽章 Secrets。
如只想替換檔案，更新 app/build.gradle 與 app/src/main/java/tw/kotonoha/mobile/MainActivity.java 即可（前提為完整 RC1 原始碼已存在），並採用新版 workflow。
新版不建立任何原生工具列或浮動按鈕，網站標題列 ⋮ 會在 Android 中顯示。可存取同步權限、開啟 YT Music、重新整理、診斷。點歌詞跳轉透過 Android MediaController ACTION_SEEK_TO，需 YT Music 支援。
重新 Run workflow（main），Verify APK version 應顯示 1.0.0-rc4；下載對應 APK，先備份本機資料。若簽章不同，勿直接卸載舊版，先備份。此版本尚未編譯/真機測試。
