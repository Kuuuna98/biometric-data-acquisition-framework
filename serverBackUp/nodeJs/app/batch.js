var fs = require('fs');
var mysql = require("mysql");
var unzip = require("unzip");
var rimraf = require('rimraf');
var sqlite3 = require('sqlite3').verbose();
const Readable = require('stream').Readable;
const Writable = require('stream').Writable;
const Transform = require('stream').Transform;

var mysqlConfig = {
	host: "127.0.0.1",
	port: 1047,
	user: "juwon",
	password: "Rootpass12#$",
	database: "main",
	multipleStatements: true
};


var connection = mysql.createConnection(mysqlConfig);

function checkDupWork() {
	connection.query("SELECT count(*) as C FROM uploads WHERE processed = 1", function(err, result) {
		if (result[0].C > 0) {
			connection.end();
			return console.log("이미 작업중 프로세스 확인 혹은 디비 확인");
		}
		start();
	});
}

function start() {
	connection.query("SELECT * FROM uploads WHERE processed = 0 LIMIT 1", function(err, result) {
		//connection.end();
		if (result == null || result.length == 0) {
			console.log("작업할 파일 없음");
			connection.end();
			return;
		}

		var row = result[0];
		if (fs.existsSync("uploads/" + row.fileName) == false) {
			console.log("파일 없음 ", row.fileName);
			connection.query("UPDATE uploads SET processed = -1 WHERE id = ?", row.id, function() {
				process.nextTick(start);
			});
			return;
		}

		//작업중으로 표시
		connection.query("UPDATE uploads SET processed = 1 WHERE id = ?", row.id, function() {
			var targetPath = "uploads/unzip/" + row.fileName;
			//이미 풀린 폴더 있으면 일단 지우고 시작
			rimraf(targetPath, function () { 
				doUnzip(row, targetPath);
			});
		}); 
	});
}

//압축해제
function doUnzip(row, targetPath) {
	console.log("압축해제 >> " + targetPath);
	fs.createReadStream("uploads/" + row.fileName)
	.pipe(unzip.Extract({ path: targetPath }))
	.on('close', function() {
		console.log("완료");
		loadSQLiteFile(row, targetPath);
	});
}

//디비파일 읽어서 넣기
function loadSQLiteFile(row, targetPath) {

    if (fs.existsSync(targetPath+"/sensors_data.db") == true) {
        var db = new sqlite3.Database(targetPath + "/sensors_data.db", sqlite3.OPEN_READONLY, function(err) {
            //console.log(err);
            if (err) {
                console.log(err);
                connection.query("UPDATE uploads SET processed = -2 WHERE id = ?", row.id, function() {
                    connection.end();
                });
                return;
            }
            readSQLiteDB(row, targetPath, db);
        });
    }
    else if(fs.existsSync(targetPath+"/audio_guide.db") == true){
        var db = new sqlite3.Database(targetPath + "/audio_guide.db", sqlite3.OPEN_READONLY, function(err) {
            //console.log(err);
            if (err) {
                console.log(err);
                connection.query("UPDATE uploads SET processed = -2 WHERE id = ?", row.id, function() {
                    connection.end();
                });
                return;
            }
            readSQLiteDB(row, targetPath, db);
        });
	}
	else{
        connection.query("UPDATE uploads SET processed = -2 WHERE id = ?", row.id, function() {
            connection.end();
        });
	}


}
//var queue = [], working = false;
function readSQLiteDB(row, targetPath, db) {
	console.log(db, row);
		
	db.serialize(function() {
		
		var stream = new Readable({
			objectMode: true,
			read() {}
		});

		stream.on('data', function(data) {
			stream.pause();
			connection.query("INSERT IGNORE INTO logs (fileId, logID, type, json, reg) VALUES ?", [data], function(mserr, result) {
				if (mserr) return console.log(mserr);
				stream.resume();
			});
		});
		stream.on('end', () => { 

			console.log('>> mysql Done');
			onFinish(row);
		});

		//each함수로 매번 insert 쿼리를 보내면 한세월이라 
		//스트리밍 기법과 큐 방식을 구현해봤으나 node가 싱글 스레드 방식이라 
		//each에서 제대로 잠기는 현상때문에 결국 메모리에 한번에 로드하고 잘라서 보내는 방식을 선택
		//로그 파일이 클수록 메모리에 부하가 가므로 실험시 서버 메모리를 충분히 확보한후 하는것을 추천
		//이런 구조상 cron batch 처리를 한번에 한 건만 하도록 막아둠
		db.all("SELECT * FROM log", function(err, rows) {
			console.log("SQLITE " + rows.length + " records");
			rows = rows.map(function(r) {
				return [row.id, r._id, r.type, r.json, r.reg];
			}).reduce(function(list, r) {
				list.push(r);
				if (list.length >= 10000) { //클수록 빠르지만 정도에 따라 패킷이 깨지거나 서버 메모리에 부하
					stream.push(list);
					list = [];
				}
				return list;
			}, []);
			stream.push(rows);
			stream.push(null);
		});
	});
	db.close();
}

function onFinish(row) {
	connection.query("UPDATE uploads SET processed = 2 WHERE id = ?", row.id, function() {
		connection.end();
	});
}
