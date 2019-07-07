var express = require('express');//, json = require('express-json');
var bodyParser = require('body-parser');
var fileUpload = require('express-fileupload');
var fs = require('fs');
var mysql = require("mysql");
var mysqlConfig = {
	//host: "127.0.0.1",
	host: "168.188.127.124",
	port: 3306,
	user: "root",
	password: "Rootpass12#$",
	database: "main",
	multipleStatements: true
};

var app = require('express')();
var http = require('http').Server(app);
var io = require('socket.io')(http);

app.use(bodyParser.json({ limit: '1000mb' }));
app.use(fileUpload());

app.post('/upload/:id', function(req, res, next) {
	var file = req.files.file;
	if (file == null) return res.status(500).json({ result: "ERROR" });
	
	var t = new Date().getTime();
	var fileName = file.md5 + "_" + t;
	file.mv(__dirname + "/uploads/" + fileName, function(err) {
		if (err) {
			console.error(err); 
			return res.status(500).json({ result: "ERROR", err: err });
		}
		
		var c = mysql.createConnection(mysqlConfig);
		c.query("INSERT INTO uploads (udid, fileName, info) VALUES ( ?, ?, ? )", 
			[ req.params.id, fileName, JSON.stringify({ headers: req.headers, body: req.body }) ], 
		function(mysqlerr, mysqlres) {
			c.end();
			if (mysqlerr) {
				console.error(mysqlerr);
				return res.status(500).json({ result: "ERROR", err: mysqlerr });
			}
			io.emit('message', JSON.stringify([ req.params.id, fileName, JSON.stringify({ headers: req.headers, body: req.body }) ]));
			return res.json({ result: "OK" });
		});
	});
});

app.post("/ack", function(req, res) {
	
	io.emit('ack', JSON.stringify(req.body));
	res.json({ result: "OK" });
});


app.get('/', function(req, res){
	res.sendFile(__dirname + '/htdoc/index.html');
});

app.get('/board', function(req, res){
	res.sendFile(__dirname + '/htdoc/board.html');
});


io.on('connection', function(socket){
	//console.log('a user connected');
	//socket.on('disconnect', function(){
	//	console.log('user disconnected');
	//});
	
	/*socket.on('message', function(msg){
		console.log('message: ' + msg);
		io.emit('message', msg);
	});*/
});
http.listen(3000, function(){	
	console.log('listening on *:3000');
});


