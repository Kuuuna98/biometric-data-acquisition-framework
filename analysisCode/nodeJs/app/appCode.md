# app.js 

   require을 통해 필요한 모듈을 불러옵니다.

    var express = require('express');
    var bodyParser = require('body-parser');
    var fileUpload = require('express-fileupload');
    var fs = require('fs');
    var mysql = require("mysql");
    var app = require('express')();
    var http = require('http').Server(app);
    var io = require('socket.io').(http);
    
    
   mysql에 접속할 유저 정보 및 사용할 데이터 베이스를 mysqlConfig에 저장합니다.

    mysqlConfig = {
        host: "168.188.127.124",
        port: 3306,
        user: "root",
        password: "rootpass12#$",
        database: "main",
        multipleStatements: tru
    };
	    
body-parser과 express-fileupload 미들웨어를 사용합니다.
	
    app.use(bodyParser.json({ limit: '1000mb'}));
    app.use(fileUpload());
    
    	
'/upload/:id'의 post 요청이 들어온 경우 file을 받아 현재 경로에서 upload/fileName으로 이동시키고, mysql에 접속하여 'main' database 'uploads' table에 file을 insert합니다.
  

      app.post('/upload/:id', function(req,res,next)){
        	
		//file 객체를 저장합니다.
		var file = req.files.file; 
		//file이 null이면 에러를 리턴합니다.
		if(file == null) return res.status(500).json({ result: "ERROR"});
	
		//file 명을 다음과 같이 지정합니다.
		var t = new Date().getTime();
		var fileName = file.md5 + "_"+ t;
		
		//file을 현재 경로에서 /uploads/fileName으로 옮깁니다.
		file.mv(__dirname+"/uploads/"+fileName, function(err)){
			//에러 발생 시 error를 response 합니다.
			if(err){
				console.error(err);
				return res.status(500).json({ result : "ERROR", err : err});
			}
			//mysqlConfig로 mysql에 접속합니다.
			var c = mysql.createConnection(mysqlConfig);
			
			//query문을 이용하여 'main' database 'uploads' table에 id와 fileName, info를 삽입합니다. 
			c.query("INSERT INTO uploads (udid, fileName, info) VALUES ( ?, ?, ?)",
			[req.params.id, fileName, JSON.stringify({ headers: req.headers, body: req.body})] ,
			function(mysqlerr, mysqlres){
				//mysql 접속을 끝냅니다.
				c.end();
				//error 발생 시 error를 응답합니다.
				if(mysqlerr){
					console.error(mysqlerr);
					return res.status(500).json({ result: "ERROR", err: mysqlerr});
				}
				// emit으로 mysql에 삽입한 정보를 접속된 클라이언트들에게 보냅니다.
				io.emit('message', JSON.stringify( [req.params.id, fileName, 
				JSON.stringify({ headers:req.headers, body: req.body})]));
				// 정상 수행 시, "OK"를 응답합니다.
				return res.json({ result : "OK"});
			});
		});
	});
	
'/ack'로 post 요청이 들어온 경우, 접속된 클라이언트에게 req의 body를 파싱해 보낸 후, "OK"를 응답합니다.
	

    app.post("/ack", function(req, res) {
    		io.emit('ack', JSON.stringify(req.body));
    		res.json({ result: "OK" });
    	});
	
'/'로 get 요청이 들어온 경우, 현재 경로에서 htdoc 폴더의 index.html를 response 합니다.
	

    app.get('/' function(req, res){
    		res.sendFile(__dirname + '/htdoc/index.html');
    	});

'/board'로 get 요청이 들어온 경우, 현재 경로에서 htdoc 폴더의 board.html을 response 합니다.

    app.get('/board', function(req, res){
    	res.sendFile(__dirname+'/htdoc/board.html');
    });
	
socket을 통해 connection에 성공했을 시 실행됩니다.
	

    io.on('connection', function(socket){
    		//console.log('a user connected');
    	});

 3000 포트를 listen합니다.
	

    http.listen(3000, function(){
    		console.log('listening on *: 168.188.127.124');
	    });

