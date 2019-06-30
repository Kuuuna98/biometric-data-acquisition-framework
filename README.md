# biometric-data-acquisition-framework

The environment is ubuntu 16.04 LTS, npm 6.1.0, and nodejs 10.6.0.

# Development Environment (npm, nodeJS)

1. `sudo apt-get install build-essential libssl-dev`

   - ![1561377067180](./ReadMeImage/installLibssl.png)

2. `curl -o- https://raw.githubusercontent.com/creationix/nvm/v0.33.11/install.sh | bash`

   - ![1561377098138](./ReadMeImage/curlNvmInstall.png)

3. `source ~/.bashrc`

   - ![1561377119975](./ReadMeImage/sourceBashrc.png)

4. `nvm --version`

   1. `> 0.33.11` **Check**

   - ![1561377159507](./ReadMeImage/nvmInstall.png)

5. `nvm install 10.6.0`

   - ![1561377207497](./ReadMeImage/nvmVersion.png)

6. `node --version`

   1. `> 10.6.0` **Check**

   - ![1561377242602](./ReadMeImage/nodejsVersion.png)

7. `npm --version`

   - ![npmVersion](./ReadMeImage/npmVersion.png)



# Development Environment (Android)

1. `https://www.jetbrains.com/toolbox/app/?fromMenu` Download Toolbox APP

- ![downloadToolbox](./ReadMeImage/downloadToolbox.png)

2. Save and extract

   - ![saveFile](./ReadMeImage/saveFile.png)

     

   - ![toolboxExtractAndRun](./ReadMeImage/toolboxExtractAndRun.png)

3. Install Android Studio

   - ![install Android Studio](./ReadMeImage/installAndroidStudio.png)

4. Run Android Studio

   - `Next` => `standard` => `Next` => `Next` => `Finish`

5. Setup

   - ![InstallingAndroidStudio](./ReadMeImage/InstallingAndroid.png)

6. `configure` => `AVD Manager` => `Create Virtual Device`

   - Install `Pixel 2` => Download `Pie`

# Error & Version

- `ERROR: Manifest merger failed : Attribute application@appComponentFactory value=(android.support.v4.app.CoreComponentFactory) from [com.android.support:support-compat:28.0.0] AndroidManifest.xm:22:18-91`
  - `gradle.properties`에 아래와 같이 추가하기
    - `android.useAndroidX=true`
    - `android.enableJetifier=true`
- Spring error
  - Mysql
    - ![mysql error](./ReadMeImage/error1.png)
  - Run
    - ![run error](./ReadMeImage/error2.png)
- `gradle 3.1.4 -> 3.4.1 , 1.24.4 -> 1.25.4`
  - ![dependencies Version](./ReadMeImage/dependenciesVersion.png)
- Module version
  - ![module Version](./ReadMeImage/moduleVersion.png)
- Execute test
  - <img src="./ReadMeImage/E4sensingApp.jpg" width="300" alt="E4 Sensing App screenshot">

# Install Nginx

- Install nginx
  - ![install Nginx](./ReadMeImage/installNginx.png)
  - ![start Nginx](./ReadMeImage/startNginx.png)
- Add ufw
  - ![add ufw](./ReadMeImage/addUfw.png)
- Allow permission AVD
  - Permission denied
    - ![permission denied](./ReadMeImage/AVDPermissionDenied.png)
  - `install qemu-kvm`
    - ![install qemu-kvm](./ReadMeImage/installQemuKvm.png)
  - Add user kvm
    - `sudo adduser 'user name' kvm`  Use first not second
      - ![add user kvm](./ReadMeImage/adduserKvm.png)
  - Permission allow
    - ![permission allow](./ReadMeImage/permissionAllow1.png)
    - ![permission allow](./ReadMeImage/permissionAllow2.png)
  - After run AVD screenshot
    - ![AVD](./ReadMeImage/AVD1.png)
    - ![AVD](./ReadMeImage/AVD2.png)
    - ![AVD](./ReadMeImage/AVD3.png)

# Install NodeJs

- `npm init`
  - ![npm init](./ReadMeImage/npmInit.png)
- Install modules
  - `npm install express`
  - `express-fileupload`
    - ![install modules express, express-fileupload](./ReadMeImage/installModules1.png)
  - `npm install mysql`
  - `npm install socket.io`
    - ![install modules mysql, socket.io](./ReadMeImage/installModules2.png)
- Run App.js
  - ![run App.js](./ReadMeImage/runAppJs.png)
- `localhost:3000/`
  - It means index.html
  - ![index.html](./ReadMeImage/indexHtml.png)
- `localhost:3000/board`
  - It means board.html
  - ![board.html](./ReadMeImage/boardHtml.png)