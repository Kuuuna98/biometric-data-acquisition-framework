# batch.js

필요한 모듈을 require을 통해 불러옵니다.   

     var fs = require('fs');
        var mysql = require("mysql");
        var unzip = require("unzip");
        var rimraf = require('rimraf');
        var sqlite3 = require('sqlite3').verbose();
        const Readable = require('stream').Readable();
        const Writable = require('stream').Writable;
        cons Transform = require('stream').Transform;

mysql에 접속할 유저의 user name과 host, password, 접속할 database를 mysqlConfig에 저장합니다.
 
	var mysqlConfig = {
		host: "168.188.127.124",
		port: 3306,
		user: "root",
		password: "rootpass12#$",
		database: "main",
		multipleStatements: true
	};
createConnection으로 mysql에 접속합니다.
	
	var connection = mysql.createConnection(mysqlConfig);

처음 호출되는 함수는 checkDupWork() 입니다. 
현재 압축 해제 중인 파일이 있는지 검사합니다.

	function checkDupWork(){
		//processed의 값은 file의 현재 상태를 나타내며, 1인 경우 현재 압축 해제 중인 file임을 의미합니다.
		connection.query("SELECT count(*) as C FROM uploads WHERE precessed = 1", 
		function(err, result){
			//C에는 현재 압축 해제 중인 file의 개수가 담겨 있습니다. 0보다 크면 mysql 접속을 종료합니다.
			if(result[0].C > 0){
				connection.end();
				return console.log("이미 작업 중 프로세스 확인 혹은 디비 확인");
			}
			//현재 압축 해제 중인 file이 없다면 start 함수를 실행합니다.
			start();
		});
	}
start 메소드는 uploads 테이블에서 압축 해제할 file을 찾아 file이 존재하는 경우 doUnzip 메소드를 호출하여 압축 해제를 실행하도록 합니다.

	function start() {
		//processed=0은 압축 해제가 안된 상태인 file이며 압축 해제는 동시에 할 수 없기 때문에 LIMIT 1로 하여 1개의 file만 가져옵니다.
		connection.query("SELECT * FROM uploads WHERE processed = 0 LIMIT 1", 
		function(err, result) {
			//file이 없는 경우, mysql 접속을 종료합니다.
			if(result == null || result.length == 0){
				console.log("작업할 파일 없음");
				connection.end();
				return;
			}
			//file 정보가 담겨있는 행을 row에 저장합니다.
			var row = result[0];
			//해당 파일이 존재하지 않는 경우 uploads 테이블에 해당 행의 processed값을 -1로 설정합니다.
			if(fs.existSync("uploads/"+row.fileName) == false){
				console.log("파일 없음 ", row.fileName);
				connection.query("UPDATE uploads SET processed =-1 WHERE id="", row.id, function(){
					process.nextTick(start);
				});
				return;
			}
			//해당 파일이 존재하는 경우 압축 해제를 실행하기 위해 processed를 1로 하여 압축 해제 중임을 알립니다.
			connection.query("UPDATE uploads SET processed =1 WHERE id=?", row.id, function(){
				//압축 해제 경로를 다음과 같이 지정한 후, 
				//압축 해제 경로의 파일을 지우고 압축 해제를 시작합니다. 
				var targetPath = "uploads/unzip/"+row.fileName;
				rimraf(targetPath, function(){
					doUnzip(row, targetPath);
				});
			});
		});
	}
doUnzip 메소드는 압축 해제 메소드입니다. 
압축 해제가 끝나면 loadSQLiteFile 메소드를 실행합니다.

	function doUnzip(row, targetPath){
		console.log("압축 해제 >> "+targetPath);
		//stream을 통해 읽어 targetPath에 압축 해제합니다.
		fs.createReadStream("uploads/"+row.fileName)
		.pipe(unzip.Extract({ path: targetPath}))
		.on("close", function() {
			console.log("완료");
			loadSQLiteFile(row, targetPath);
		});
	}
압축 해제를 완료하면 해당 폴더에는 sensors_data.db 또는 audio_guide.db 파일이 있어야 합니다.
위 파일은 안드로이드 스튜디오에서 압축하여 보낸 파일입니다.
db 파일을 읽어 데이터 베이스를 엽니다.

	function loadSQLiteFile(row, targetPath) {
		//sensors_data.db 파일이 있는 경우
		if(fs.existsSync(targetPath+"/sensors_data.db")==true){
			//sensors_data.db를 읽어 database를 엽니다.
			var db = new sqlite3.Database(targetPath+"/sensors_data.db", sqlite3.OPEN_READONLY,function(err){
				if(err){
					//데이터베이스를 여는 도중 에러가 발생하면 mysql 'uploads' table의 해당 열에 processed 값을 -2로 바꾼 후 mysql 접속을 종료합니다.
					console.log(err);
					connection.query("UPDATE uploads SET processed = -2 WHERE id=?", row.id, function(){
						connection.end();
					});
					return;
				}
				//readSQLiteDB 메소드를 호출하여 Database를 읽습니다.
				readSQLiteDB(row, targetPath, db);
			});
		}
		//audio_guide.db 파일이 있는 경우
		else if(fs.existsSync(targetPath+"/audio_guide.db")==true){
			//audio_guide.db를 읽어 database를 엽니다.
			var db = new sqlite3.Database(targetPath+"/sensors_data.db", sqlite3.OPEN_READONLY,function(err){
				if(err){
					//데이터베이스를 여는 도중 에러가 발생하면 mysql 'uploads' table의 해당 열에 processed 값을 -2로 바꾼 후 mysql 접속을 종료합니다.
					console.log(err);
					connection.query("UPDATE uploads SET processed = -2 WHERE id=?", row.id, function(){
						connection.end();
					});
					return;
				}
				//readSQLiteDB 메소드를 호출하여 Database를 읽습니다.
				readSQLiteDB(row, targetPath, db);
			});
		}
		//위 두 개의 파일이 모두 없는 경우, mysql 'uploads' table의 해당 열에 processed 값을 -2로 바꾼 후 mysql 접속을 종료합니다.
		else {
			connection.query("UPDATE uploads SET processed = -2 WHERE id=?", row.id, function(){
				connection.end();
			});
		}
	}
readSQLiteDB 메소드는 db를 읽어 mysql 'main' database 'log' table에 삽입합니다.
이 때 중복된 데이터는 삽입되지 않습니다.

	function readSQLiteDB(row, targetPath, db){
		console.log(db, row);
		
		//serialize를 통해 아래 코드가 동시에 실행되지 않도록 합니다.	
		db.serialize(function() {
			var stream = new Readable({
				objectMode: true,
				read(){}
			});
			//data가 들어오는 경우 stream을 일시 중지한 후 data를 log table에 삽입합니다.
			//이때 IGNORE를 사용하여 중복된 데이터가 삽입되지 않도록 합니다.
			stream.on('data', function(data){
				stream.pause();
				connection.query("INSERT IGNORE INTO logs (fileId, logID, type, json, reg) VALUES ?", [data], function(mserr, result){
					if(mserr) return console.log(mserr);
					stream.resume();
					//stream을 다시 시작합니다.
				});
			});
			//더 이상 소비할 데이터가 없는 경우 onFinish 메소드를 호출합니다.
			stream.on('end', () => {
				console.log('>> mysql Done');
				onFinish(row);
			});
			
			//db의 log table을 한번에 로드합니다.
			//~~~~~~~~~~~~~~~~~
			db.all("SELECT * FROM log", function(err, rows) {
				console.log("SQLITE "+row.length+" records);
				rows= rows.map(function(r){
					return [row.id, r._id, r.type, r.json, r.reg]
					}).reduce(function(list.r){
						list.push(r);
						//rows를 mapping 한 후, list에 삽입합니다.
						//list 길이가 10000 이상이면 stream에 push 합니다.
						if(list.length >= 10000) {
							stream.push(list);
							list=[];
							//다시 list를 초기화합니다.
						}
						return list;
					},[]);
					stream.push(rows);
					stream.push(null); //null을 push하면 더이상 소비할 데이터가 없다는 것 입니다.
				});
			});
			db.close();//db를 close합니다.
		}
mysql uploads table의 해당 row processed를 2로 설정하여 log table에 삽입이 완료되었음을 알립니다.
mysql 접속을 종료합니다.

		function onFinish(row){
			connection.query("UPDATE uploads SET processed =2 WHERE id=?",row.id, function(){
				connection.end();
			});
		}
batch.js는 cron으로 동작하며 호출되었을 때, checkDupWork()가 가장 먼저 실행됩니다.

		checkDupWork();
    

### processed 값에 따른 file 상태

 - processed = -2
> database를 로드하는 데 오류 발생
- processed = -1
> upload 폴더에 압축 해제할 파일이 없는 경우 
- processed = 0
> 압축된 파일, log table에 삽입되지 않음
- processed = 1
> 현재 압축 해제 진행 중인 파일
- processed = 2
> 압축 해제 완료 후 log table에 삽입 완료된 상태
