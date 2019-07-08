# biometric-data-acquisition-framework

##### The environment is `ubuntu 16.04 LTS`, `npm 6.1.0`,  `nodejs 10.6.0` and `mysql 14.14`



## Development Environment (npm, nodeJS)

1. ##### `sudo apt-get install build-essential libssl-dev`
   - npm 및 nodejs 관련 모듈을 설치하기 위해, apt로 다음과 같은 모듈을 먼저 설치합니다.

   - libssl-dev 패키지는 SSL 개발에 필요한 라이브러리와 헤더파일등을 가지고 있으며 아래에 링크에 자세한 내용을 살펴 볼수 있습니다.

     > https://packages.debian.org/jessie/libssl-dev

   - ![1561377067180](./ReadMeImage/installLibssl.png)

   

2. ##### `curl -o- https://raw.githubusercontent.com/creationix/nvm/v0.33.11/install.sh | bash`

   - curl을 이용해서 nvm을 설치합니다. (현재 설치하는 버전은 0.33.11입니다.)

   - ![1561377098138](./ReadMeImage/curlNvmInstall.png)

   

3. ##### `source ~/.bashrc`

   - bashrc를 업데이트 합니다.

   - ![1561377119975](./ReadMeImage/sourceBashrc.png)
   
4. ##### `nvm install 10.6.0`
   - nvm을 10.6.0 버전으로 설치합니다.

   - ![1561377159507](./ReadMeImage/nvmInstall.png)
   
5. ##### `nvm --version`
   - ` 0.33.11` version이 맞게 설치되었는지 확인합니다.

   - ![1561377207497](./ReadMeImage/nvmVersion.png)

   

6. ##### `node --version`
   1. ` 10.6.0`  version이 맞게 설치되었는지 확인합니다.

   - ![1561377242602](./ReadMeImage/nodejsVersion.png)

   

7. ##### `npm --version`
   - `6.1.0` version이 맞게 설치되었는지 확인합니다.

   - ![npmVersion](./ReadMeImage/npmVersion.png)



# Development Environment (Android)

1. ##### `https://www.jetbrains.com/toolbox/app/?fromMenu`

   - Download Toolbox APP
   - ![downloadToolbox](./ReadMeImage/downloadToolbox.png)

   

2. ##### Save and extract

   - ![saveFile](./ReadMeImage/saveFile.png)

     

   - ![toolboxExtractAndRun](./ReadMeImage/toolboxExtractAndRun.png)
   
3. ##### Install Android Studio

   - ![install Android Studio](./ReadMeImage/installAndroidStudio.png)

   

4. ##### Run Android Studio

   - `Next` => `standard` => `Next` => `Next` => `Finish` 순서로 순차적으로 선택합니다.

   

5. ##### Setup

   - ![InstallingAndroidStudio](./ReadMeImage/InstallingAndroid.png)

   

6. ##### `configure` => `AVD Manager` => `Create Virtual Device`
   - Install `Pixel 2` => Download `Pie`



# Error & Version

- ##### 해당 API가 다른 API로 대체되었고 2019년 말에는 사라질 것이라는 내용으로 추후에 해당코드를 수정해야 합니다.
  
  - ![android warning](./ReadMeImage/androidWarning.jpg)



- ##### `ERROR: Manifest merger failed : Attribute application@appComponentFactory value=(android.support.v4.app.CoreComponentFactory) from [com.android.support:support-compat:28.0.0] AndroidManifest.xm:22:18-91`
  
  - `gradle.properties`에 아래와 같이 추가하기
    - `android.useAndroidX=true`
    - `android.enableJetifier=true`


- ##### 두번째 빌드부터 발생하는 FileNotFound error
  
  - 첫 번째 빌드
    - ![first build](./ReadMeImage/FirstBuild.png)
  
  
  
  - 두 번째 빌드
    
  - ![second build](./ReadMeImage/NextBuild.png)
    
     > *실제 환경에서는 해당 오류 발생하지 않음.*



- ##### Spring error  
    - Mysql
    - ![mysql error](./ReadMeImage/error1.png)

  

  - Run
    - ![run error](./ReadMeImage/error2.png)


- ##### `gradle 3.1.4 -> 3.4.1 , 1.24.4 -> 1.25.4`
  
  - ![dependencies Version](./ReadMeImage/dependenciesVersion.png)



- ##### Module version
  
  - ![module Version](./ReadMeImage/moduleVersion.png)



- ##### Execute test

  - <img src="./ReadMeImage/E4sensingApp.jpg" width="300" alt="E4 Sensing App screenshot">



# Install Nginx

- ##### Install nginx
  
  - ![install Nginx](./ReadMeImage/installNginx.png)
  - ![start Nginx](./ReadMeImage/startNginx.png)



- ##### Add ufw
  
  - ![add ufw](./ReadMeImage/addUfw.png)



- ##### Allow permission AVD
  
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

- ##### `npm init`
  
  - npm을 만듭니다.
    - ![npm init](./ReadMeImage/npmInit.png)



- ##### Install modules
  
  - 아래의 명령어를 통해서 필요한 모듈들을 전부 설치합니다.
    
  - `npm install after array-flatten arraybuffer.slice async-limiter backo2 base64-arraybuffer base64id better-assert bignumber.js blob body-parser busboy bytes callsite component-bind component-emitter component-inherit content-disposition content-type cookie cookie-signature core-util-is debug depd destroy dicer ee-first encodeurl engine.io engine.io-client engine.io-parser escape-html etag express express-fileupload finalhandler forwarded fresh has-binary2 has-cors http-errors iconv-lite indexof inherits ipaddr.js isarray media-typer merge-descriptors methods mime mime-db mime-types ms mysql string_decoder negotiator object-component on-finished parseqs parseuri parseurl path-to-regexp process-nextick-args proxy-addr qs range-parser raw-body readable-stream safe-buffer safer-buffer send serve-static setprototypeof socket.io ms socket.io-adapter socket.io-client socket.io-parser isarray sqlstring statuses streamsearch string_decoder to-array toidentifier type-is unpipe util-deprecate utils-merge vary ws xmlhttprequest-ssl yeast`
  
    
  
  - ![install modules express, express-fileupload](./ReadMeImage/installModules1.png)



- ##### Run App.js
  
  - app.js를 실행할 때는 확인은 `node app.js`로 하여도 괜찮으나 서버에서 계속 돌리고자 한다면 `forever start app.js`를 통해서 자동복구할 수 있게끔 돌려야합니다.
  - ![run App.js](./ReadMeImage/runAppJs.png)



- ##### `localhost:3000/`
  
  - 기본이 index.html으로 접근됩니다.
  - ![index.html](./ReadMeImage/indexHtml.png)



- ##### `localhost:3000/board`
  
  - `/board` 로 접근할 시 board.html로 연결됩니다.
  - ![board.html](./ReadMeImage/boardHtml.png)



## Connect Virtual Machine Web Server

- ##### 가상머신 웹서버로 nginx를 설치하였습니다.

- ##### `파일` => `호스트 네트워크 관리자 (ctrl + h)` 로 아래 화면을 설정합니다.

  - ##### 없다면 새로 만들기

  - 수동으로 어댑터 설정

  - IPv4 주소 : `192.168.56.1`

  - IPv4 서브넷 마스크 : `255.255.255.0`

    - ![virtual Box host network manager](./ReadMeImage/virtualBoxPortForwardingApater.png)



- ##### DHCP서버를 선택합니다.

  - 서버주소 : `192.168.56.100`
  - 서버 마스크 : `255.255.0`
  - 최저 주소 한계 : `192.168.56.101`
  - 최고 주소 한계 : `192.168.56.254`
    - ![virtual Box host network manager](./ReadMeImage/virtualBoxPortForwardingDhcpServer.png)



- ##### 해당 가상머신의 `설정` => `네트워크` =>`어댑터`를 아래와 같이 설정합니다.

  - `어댑터 1`은 `NAT`로 사용하고 있을 것입니다.
  - 그래서 저희는 `어댑터2`를 사용하겠습니다.
  - `다음에 연결됨`을 `호스트 전용 어댑터`로 설정합니다.
    - ![virtual machine network manager](./ReadMeImage/virtualBoxPortForwardingApater2.png)
  
  
  
  - 가상머신을 재부팅하고 주소를 확인하면 `192.168.56.102`라고 DHCP를 통해서 ip주소가 자동할당된 것을 확인할 수 있습니다.
    - ![ifconfig virtual machine](./ReadMeImage/virtualMachineIfconfig.png)
  
  
  
  - 이 주소는 local 서버의 경우이니 실제 환경에서는 고정시켜서 사용합니다.
  
    - 가상머신의 ip주소를 고정시키는 방법은 다음 주소를 참고하였습니다. *(추후에 수정)*
  
      > https://dbrang.tistory.com/1279
  
  - `http://192.168.56.102`를 통해서 가상머신의 웹서버 nginx에 접속되는 것을 확인할 수 있었습니다.
  
  - ![connect virtual machine web server](./ReadMeImage/connectVirtualMachineNginxWebPage.png)



## Connect App

- ##### 안드로이드 어플과 웹서버를 public ip `168.188.127.124`를 통해서 80번 포트로 연결한 뒤 3000포트로 포트포워딩하였습니다.

- ##### 스마트폰에서 센서를 통해서 수집한 데이터를 logcat으로 확인하였습니다.

  - ![collect smartphone sensor data](./ReadMeImage/sensorData.png)

- ##### 수집한 정보를 웹서버를 통해서 접근한 모습입니다.

  - 접근 후 시간이 일정시간 이상 경과시 **붉은 색**으로 접근이 끊겼음을 표시합니다.
    > 해당 부분은 추후에 자동 복구를 할 수 있게 수정이 필요합니다.
    
    - ![sensing data in web page](./ReadMeImage/sesorDataOnServer.png)
    - ![disconnect sensing data in web page](./ReadMeImage/sensingBoard.png)



## Install Mysql

- ##### ` sudo apt install mysql-server` 를 통해서 mysql-server를 설치합니다.

  - 설치중에 Mysql에서 사용할 root용 `password 설정`을 합니다.
    > 우선은 `Rootpass12#$`으로 설정을 해두었습니다. ( 추후에 강력하게 변경하기 )    
    > *시작시 `sudo` 조건 안주고 접근시 실행불가*

  

  - password를 잊어버리더라도 Google에 `mysql password forgot`와 같은 방식으로 검색하면 방법이 있습니다.
  - ![install Mysql Server](./ReadMeImage/installMysqlServer.png)



- ##### `sudo apt install mysql-workbench` 를 통해서 mysql-workbench를 설치합니다.

  - ![install Mysql workbench](./ReadMeImage/installMysqlWorkbench.png)



- ##### `sudo service mysql start`

  - ![Mysql start](./ReadMeImage/startMysql.png)



- ##### mysql의 ufw를 등록합니다.

  - ![add mysql ufw](./ReadMeImage/mysqlUfw.png)

  

- ##### workbench를 통해서 mysql DB에 접근합니다.

  - 접근하기 전에 3306포트에 대한 접근권한을 줍니다.
  - ![mysql workbench homepage](./ReadMeImage/mysqlWorkbench.png)



- ##### `/upload`/를 통해서 접근할 시에는 3306포트로 nginx에서 포워딩해줍니다.

  - ![nginx port forwarding](./ReadMeImage/siteAvailableDB.png)



## Make Mysql DB (uploads, logs)

- 우선 `main` DB를  만듭니다.

  - `CREATE DATABASE main;`

  
  
- ##### 아래의 명령어를 통해서 `uploads`와 `logs` table을 생성합니다.

  - ```mysql
    CREATE TABLE uploads(
            id INT(11) NOT NULL PRIMARY KEY AUTO_INCREMENT,
            udid VARCHAR(255) NOT NULL, fileName VARCHAR(255) NOT NULL,
            info LONGTEXT,
            reg TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
            processed INT(11) NOT NULL DEFAULT 0
        );
    ```

 

  - `DESC uploads`를 통해서 생성된 **uploads table**을 확인할 수 있습니다.
  
    - ![DESC uploads](./ReadMeImage/descUploads.png)

  

  - ```mysql
CREATE TABLE logs(
        id BIGINT(20) NOT NULL PRIMARY KEY AUTO_INCREMENT,
      fileID BIGINT(20), json VARCHAR(255),
        logID BIGINT(20), reg BIGINT(20) NOT NULL,
        type VARCHAR(255)
    );
    ```
    
  - `DESC logs`를 통해서 생성된 **logs table**을 확인할 수 있습니다.
    
    - ![DESC logs](./ReadMeImage/descLogs.png)

