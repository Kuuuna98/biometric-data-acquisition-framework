# biometric-data-acquisition-framework

##### The environment is `ubuntu 16.04 LTS`, `npm 6.1.0`,  `nodejs 10.6.0` and `mysql 14.14`



## Development Environment (npm, nodeJS) 

1. ##### `sudo apt-get install build-essential libssl-dev`

   - npm 및 nodejs 관련 모듈을 설치하기 위해, apt로 다음과 같은 모듈을 먼저 설치합니다.

   - libssl-dev 패키지는 SSL 개발에 필요한 라이브러리와 헤더파일등을 가지고 있으며 아래에 링크에 자세한 내용을 살펴 볼수 있습니다.

     > https://packages.debian.org/jessie/libssl-dev

   - ![install libssl](./ReadMeImage/installLibssl.png)



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

   - Ubuntu에서 AndroidStudio를 설치하기위하여 JetBrain의 Toolbox를 활용합니다.
   - 먼저 Toolbox APP의 파일확장자를 .TAR.GZ로 선택하여 다운로드합니다.
   - ![downloadToolbox](./ReadMeImage/downloadToolbox.png)



2. ##### Save and extract

   - File을 저장 해야 하므로 Save File을 선택합니다.
   - ![saveFile](./ReadMeImage/saveFile.png)
   - 저장한 File은 .tar.gz로 압축되어있습니다.
   - terminal창에서 cd./Downloads를 통해 다운받은 압축파일이 있는곳으로 이동합니다.
   - tar –xvzf [파일명].tar.gz 명령어를 입력하여 파일 압축을 해제합니다.
   - 안에 있는 실행파일을 실행합니다.
   - ![toolboxExtractAndRun](./ReadMeImage/toolboxExtractAndRun.png)

3. ##### Install Android Studio

   - Toolbox APP이 실행시키면 화면에 다운받은수있는 app들 목록이 보여집니다. 그중에서 Android Studio를 찾아 설치합니다.
   - ![install Android Studio](./ReadMeImage/installAndroidStudio.png)



4. ##### Run Android Studio

   - `Next` => `standard` => `Next` => `Next` => `Finish` 순서로 순차적으로 선택합니다.



5. ##### Setup

   - ![InstallingAndroidStudio](./ReadMeImage/InstallingAndroid.png)



6. ##### `configure` => `AVD Manager` => `Create Virtual Device`

   - Install `Pixel 2` => Download `Pie`
   - Android Studio를 실행하면 나타나는 화면의 오른쪽 아래에 위치한 configure를 선택합니다. 그리고 위의 순서대로 선택하여 가상머신을 생성합니다.
   - Android Studio에서 project를 연 상태에서는 상단에 위치한 Tools를 선택하면 AVD Manager를 찾을 수 있습니다.




# Error & Version

- ##### 해당 API가 다른 API로 대체되었고 2019년 말에는 사라질 것이라는 내용으로 추후에 해당코드를 수정해야 합니다.

  - ![android warning](./ReadMeImage/androidWarning.jpg)



- ##### `ERROR: Manifest merger failed : Attribute application@appComponentFactory value=(android.support.v4.app.CoreComponentFactory) from [com.android.support:support-compat:28.0.0] AndroidManifest.xm:22:18-91`

  - Gradle Scripts의 `gradle.properties`에 아래와 같이 추가합니다.
    - `android.useAndroidX=true`
    - `android.enableJetifier=true`

- ##### 두번째 빌드부터 발생하는 FileNotFound error

  - 첫 번째 빌드
    - ![first build](./ReadMeImage/FirstBuild.png)



  - 두 번째 빌드

  - ![second build](./ReadMeImage/NextBuild.png)

  > 첫 Run을 실행한 후 다시 Run을 실행할 경우 FileNotFoundException이 발생했습니다.  

  - ![해결 방법](./ReadMeImage/Fixgradle.PNG)

   - `build.gradle (Module: app)`에서 `compileSdkVersion`과 `targetSdkVersion`을 28로 낮춘다.

   > API 29 Platform 에서 지원하는 ThreadPoolExecutor.java 와 ForkJoinTask.java에서 오류가 발생하여 SDK를 수정하였습니다.
 - ##### Sensing data of SmartPhone have same value

accDelta와 gyroDelta가 같은 값을 갖는 오류

`LocationService.java` > `getSensor`

    float[] values = event.values;  
    // Movement  
    float x = values[0];  
    float y = values[1];  
    float z = values[2];  
    long actualTime = event.timestamp;
    accDelta++;  
    gyroDelta++;

  에서 아래와 같이 코드 수정하기 

    float[] values = event.values;  
    // Movement  
    float x = values[0];  
    float y = values[1];  
    float z = values[2];  
    long actualTime = event.timestamp;
         
    if(event.sensor.getType()==Sensor.TYPE_ACCELEROMETER) accDelta++;  
    else if(event.sensor.getType()==Sensor.TYPE_GYROSCOPE) gyroDelta++;

 > accDelta는 Accelerometer Sensor에 의한 SensorEvent 발생 시에만 증가하고
 > gyroDelta는 Gyroscope Sensor에 의한 SensorEvent 발생 시에만 증가시켜야 둘이 같은 값을 갖지 않게 됩니다.
- ##### Sensing data of Sensor have same value

>센싱 데이터가 변하면  `BluetoothLeService.java` 에서 `onCharacteristicChanged`를 통해  `ACTION_DATA_NOTIFY`이 발생했다고 알리는데, 
이 때 `SensorTagService.java`에서 `public class SensorTagReceiver`의 `onReceive`를 통해 발생한 action을 처리합니다.

> 어떤 데이터가 변화했는지 확인해야 하므로 `Point3D prevAcc`와 `Point3D prevGyro`을 이용해 값을 비교하여 Delta값 증가시켜야 `accDelta`와 `gyroDelta`가 같은 값을 갖지 않습니다.

`SensorTagServer.java` > `onReceive`

    accDelta++;
    gyroDelta++;
  아래와 같이 수정

    //prevAcc와 prevGyro를 이용해 값 비교하여 Delta값 증가  
    if(accValue.generateEvent(prevAcc)) {  
        accDelta++;  
        prevAcc = accValue;  
    }  
    else if(gyroValue.generateEvent(prevGyro)) {  
        gyroDelta++;  
        prevGyro = gyroValue;  
    }
`Point3D.java`에 다음과 같은 메소드 추가 ( 데이터 확인 후 추후에 변경 ) 

*이벤트 발생 여부를 판단하는 메소드임*

    public boolean generateEvent(Object obj){  
       
      if(obj == null) {  
        if(this != null)return false;  
        else return true;  
      }  
      if(getClass() != obj.getClass()) return false;  
      Point3D other = (Point3D) obj;  
      
      if (Double.doubleToLongBits(x) != Double.doubleToLongBits(other.x))  
        return true;  
      if (Double.doubleToLongBits(y) != Double.doubleToLongBits(other.y))  
        return true;  
      if (Double.doubleToLongBits(z) != Double.doubleToLongBits(other.z))  
        return true;  
      return false;  
    }


- ##### Spring error  

  - Mysql
  - ![mysql error](./ReadMeImage/error1.png)



- Run
  
- ![run error](./ReadMeImage/error2.png)
  
- ##### `gradle 3.1.4 -> 3.4.1 , 1.24.4 -> 1.25.4`

  - ![dependencies Version](./ReadMeImage/dependenciesVersion.png)



- ##### Module version

  - ![module Version](./ReadMeImage/moduleVersion.png)

- <img sr하기위해서`sudo apt install nginx`명령어를 입력합니다.
  
  - eImage/E4sensingApp.jpg" width="300" alt="E4 Sensing App screenshot">



# Install Nginx

- ##### Install nginx

  - Nginx를 설치합니다.  `sudo apt install nginx`
  - 웹서버를 시작시킵니다. `sudo systemctl start nignx`
  - Nginx 상태를 확인합니다. `systemctl status nginx`
  - ![install Nginx](./ReadMeImage/installNginx.png)
  - ![start Nginx](./ReadMeImage/startNginx.png)



- ##### Add ufw

   - ufw는 ubuntu의 기본적인 방화벽입니다.
   - `sudo ufw app list`  사용가능한 프로그램을 확인합니다.
    - `sudo ufw allow ‘Nginx HTTP’` 방화벽에서 nginX로 접근을 허용합니다.
  - `sudo ufw status` ufw 상태를 확인합니다.
    - 만약 inactive(비활성화)상태라면 `sudo ufw enable`를 통해 활성화 시킵니다.
    - ![add ufw](./ReadMeImage/addUfw.png)



- ##### Allow permission AVD

  - Permission denied
    - Ubuntu에서 가상머신을 실행하면  /dev/kvm에 현재 user가 접근할수없는 오류가 발생합니다.  
    - ![permission denied](./ReadMeImage/AVDPermissionDenied.png)



  - `install qemu-kvm`
      - qemu-kvm을 설치합니다.
    - ![install qemu-kvm](./ReadMeImage/installQemuKvm.png)



  - Add user kvm
    - `sudo adduser 'user name' kvm`  Use first not second
    - `ls –al /dev/kvm`으로 /dev/kvm의 그룹을 확인하면 kvm으로 설정되어있습니다.
    - `grep kvm /etc/group` 명령어를 통해서 kvm user의 정보를 보고 현재 user가 없다면 `sudo adduser [username] kvm` 명령어를 통해서 등록합니다.
      - ![add user kvm](./ReadMeImage/adduserKvm.png)



  - Permission allow
    - 가상머신을 다시 실행하면 오류가 발생하지 않음을 확인할 수 있습니다.
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
    
    
    
    > 우선 변경한  css 이미지로서 추후에 수정하기
    
    - ![changedCss](./ReadMeImage/changedCss.png)



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



- ##### `vi /etc/nginx/sites-available/default`를 통해서 다음과 같이 포워딩을 해줍니다.

  ![nginx default](./ReadMeImage/nginxDefault.png)
  
  ![siteAvailableDefault](./ReadMeImage/siteAvailableDefault.png)
  
  

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
    CREATE TABLE logs(
        id BIGINT(20) NOT NULL PRIMARY KEY AUTO_INCREMENT,
        fileID BIGINT(20), json VARCHAR(255),
        logID BIGINT(20), reg BIGINT(20) NOT NULL,
        type VARCHAR(255)
    );
    ```



- `DESC uploads`를 통해서 생성된 **uploads table**을 확인할 수 있습니다.
  
- ![DESC uploads](./ReadMeImage/descUploads.png)
  
  
  
- `DESC logs`를 통해서 생성된 **logs table**을 확인할 수 있습니다.
  
  - ![DESC logs](./ReadMeImage/descLogs.png)



- ##### setting을 조금 더 해야 정상적으로 DB와 연결을 할 수 있습니다.

  - `/var/lib/mysql`

  - 아래와 같이 root login을 허용할 것인지에 대한 사항을 yes로 변경합니다.

  - ![permit prohibit](./ReadMeImage/permitProhibit.png)

    

  - ![permit prohibit yes](./ReadMeImage/permitYes.png)



- `vi /etc/mysql/my.cnf`를 통해서 아래의 bind-address 를 주석처리해줍니다.

  ![mysql conf bind address](./ReadMeImage/mysqlConfBindAddress.png)



- 

  

- ##### 정상적으로 DB 안으로 data들이 저장되는 모습을 확인할 수 있습니다.

  - 현재로서는 휴대폰에 저장된 DB의 크기가 너무 크면 전송시 서버에서 오류가 나옵니다.
  
  > 추후에 분할전송 등 해결방법 추가하겠습니다.

  - ![DB uploads](./ReadMeImage/uploadsDB.png)

  

- ##### 3306포트로 접근하기 위해서는 포트 개방신청서를 작성해야 하므로 추후에 신청하겠습니다.

  > docker의 3306을 로컬 8080으로 접근할 수 있도록 시도를 먼저 해보겠습니다.



## 윈도우10에서 Docker로 서버 구축하기

1. ##### 우선 `win키 + x ` => `t`를 눌러서 작업관리자를 켭니다.

   

2. ##### `성능`을 통해서 파란색으로 박스쳐놓은 부분에 `가상화`가 사용으로 표시되었는지 확인하여 `cpu`가 가상화를 지원하는지를 확인합니다.

   ### ![checkVirtualize](./ReadMeImage/checkVirtualize.png)

   

3. ##### `win키 + s`를 눌러서 검색창을 띄우고 `windows 기능 켜기/끄기`를 검색합니다.

   

4. ##### `windows 기능`에서 `Hyper-V`를 찾아서 체크를 합니다.

   ![hyperV](./ReadMeImage/hyperV.png)

   

   ![adjustHyperV](./ReadMeImage/adjustHyperV.png)

   

   ![finishAdjust](./ReadMeImage/finishAdjust.png)

   

5. ##### 완료가 되었다면 `다시 시작`을 합니다.

   

6. ##### [https://docs.docker.com/](https://docs.docker.com/)에 들어갑니다.

   

7. ##### 좌측의 바에서 `Get Docker` => `Docker CE` => `Microsoft Windows` => `Download from Docker Hub` => `Please login to download` => `login`한 후에 다운을 받습니다.

   > 귀찮으시다면 [다운로드 링크](https://download.docker.com/win/stable/Docker%20for%20Windows%20Installer.exe) 통해서도 가능합니다.
   >
   > 하지만 어차피 Docker를 사용하려면 ID가 필요해서 회원가입하는 것도 좋습니다.

   

8. ##### 다운로드 후 로그아웃 되었다가 다시 로그인합니다.

   

9. ##### 작은 고래모양이 추가되있는 모습을 볼 수 있습니다.

   ![installedDocker](./ReadMeImage/installedDocker.png)

   

10. ##### 로그인을 해줍니다.

    ![loginDocker](./ReadMeImage/loginDocker.png)

    

11. ##### Docker version을 확인해보았습니다.

    ![docker version](./ReadMeImage/dockerVersion.png)

    

12. ##### Kitematic`을 설치해주는 것이 좋습니다.

    1. `kitematic`은 Docker를 관리할 수 있는 GUI 툴입니다.

       ![kitematic](./ReadMeImage/kitematic.png)

    2. 다운받아서 압축해제 후에 폴더의 이름을 `Kitematic`으로 변경해주고 `C/Program Files/Docker`로 이동해주면 됩니다.

    3. 만약 오류가 발생한다면 [kitematic](https://github.com/docker/kitematic/releases/tag/v0.17.7)에서 다운받는 방법으로 해결할 수 있습니다.

    4. 다운 받은 파일의 이름을 kitematic으로 변경한 후에 `C:\Program Files\Docker` 로 옮겨주면 실행할 수 있습니다.

       ![kitematic location](./ReadMeImage/kitematicLocataion.png)

    

13. ##### 이제 좌측 하단의 `DOCKER CLI`를 눌러서 powershell에서 작업을 할 수 있습니다.

    

14. ##### https://cloud.docker.com/u/clearlyhunch/repository/docker/clearlyhunch/iot_server2.1

    

15. ##### Follow this order to set docker iot_framework server.

    1. `docker push clearlyhunch/iot_server2.1:tagname`

    2. `docker run -it --name iot_server -p 80:80 -p 22:22 -p 3306:3306 clearlyhunch/iot_server2.1:tagname`

    3. `chmod 755 -R /var/lib/mysql` 

    4. `chown mysql:mysql -R /var/lib/mysql`

    5. `service mysql start`

    6. `service nginx start`

    7. `service ssh start`

    8. `forever start /home/serverNodeJs/app/app.js`

       

16. ##### 해당 Docker 이미지에는 DB와 연동할 수 있도록 필요한 apt 등을 모두 설치 후 세팅해두었습니다.

    

17. ##### Ubuntu server의 시간을 KST로 변경하기 위해서 `apt install tzdata`를 수행했습니다.

    ![ubuntu tzselect](./ReadMeImage/ubuntuTzselect.png)

    - 변경 후에 `UTC`가 `KST`로 변경된 것을 확인할 수 있습니다.

      

18. ##### ssh로 접근해서 사용할 수 있도록 22번 포트를 허용하고 `apt install openssh-server`를 통해서 설치를 해주었습니다.

    ![install ssh](./ReadMeImage/installOpenssh.png)

    

    - `vi /etc/ssh/sshd-config`를 통해서 `PermitRootLogin`의 값을 `yes`로 변경하였습니다.

    ![vi sshd config](./ReadMeImage/sshdConfig.png)

    - `passwd root`로 root의 passwd를 지정합니다.

      ![set root passwd](./ReadMeImage/passwdRoot.png)

    ![sshd config yes](./ReadMeImage/sshdConfigYes.png)

    

    - 외부에서 접근할 수 있는 것을 확인하였습니다.

    ![connect ssh](./ReadMeImage/connectSsh.png)
