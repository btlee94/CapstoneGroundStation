var express = require('express');
var app = express();
var http = require('http').createServer(app);
var port = process.env.PORT || 3000;


http.listen( port, function () {
    console.log('listening on port', port);
});

app.use(express.static(__dirname + '/public'));

