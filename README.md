# NFC 讀卡機 App - 用 GitHub Actions 雲端自動建置 APK

這個資料夾是一個完整的 Android Gradle 專案,內建 GitHub Actions 設定
(`.github/workflows/build.yml`),推上 GitHub 後會自動用 Google 官方建置
環境編出 APK,不需要在自己電腦裝 Android Studio。

## 步驟

### 1. 建立一個新的 GitHub Repository
到 https://github.com/new 建立一個新的(可以是 Private)repository,
例如叫 `nfc-reader-app`。

### 2. 把這個資料夾的內容全部上傳
方法 A - 用網頁介面(最簡單,不用裝 git):
1. 開啟你剛建立的 repo 頁面
2. 點 "uploading an existing file"
3. 把這個 zip 解壓縮後的**所有檔案和資料夾**拖曳上傳(要保留資料夾結構)
4. Commit 送出

方法 B - 用命令列(如果你電腦有裝 git):
```bash
cd nfc_reader_project   # 解壓縮後的資料夾
git init
git add .
git commit -m "Initial commit"
git branch -M main
git remote add origin https://github.com/你的帳號/nfc-reader-app.git
git push -u origin main
```

### 3. 等待自動建置
上傳完成後(或每次 push),到 repo 頁面上方的 **Actions** 分頁,
會看到一個叫 "Build APK" 的工作流程正在執行,通常 3-5 分鐘完成。

### 4. 下載 APK
建置完成後(綠色勾勾 ✅),點進該次執行紀錄,頁面最下方
**Artifacts** 區塊會有一個 `nfc-reader-apk` 檔案,點擊下載,
解壓縮後就是 `app-debug.apk`,傳到手機安裝即可
(手機需開啟「安裝未知來源應用程式」權限)。

## 專案內容說明

- `app/src/main/java/.../MainActivity.kt`:核心邏輯,讀取NFC UID並顯示
  十六進位、十進位(Big-Endian / Little-Endian 兩種)
- `app/src/main/res/layout/activity_main.xml`:畫面版面
- `app/src/main/AndroidManifest.xml`:NFC權限與感應設定
- `.github/workflows/build.yml`:GitHub Actions 自動建置設定(核心)

## 常見問題

**Actions 分頁沒有反應 / 找不到 workflow?**
確認 `.github/workflows/build.yml` 這個檔案(含隱藏的 `.github` 資料夾)
有確實上傳到 repo 裡,網頁拖曳上傳有時會漏掉以 `.` 開頭的資料夾,
建議用命令列方式上傳比較保險。

**建置失敗 (Build failed)?**
點進失敗的執行紀錄看紅字錯誤訊息,把訊息貼給我,我可以幫你排查。

**以後想改程式碼再重新build怎麼做?**
直接在 GitHub 網頁上編輯檔案並 commit,或本機改完後 `git push`,
Actions 會自動重新觸發建置,不需要手動做任何事。
