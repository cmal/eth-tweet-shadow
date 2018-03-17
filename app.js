var express = require('express'),
    config = require('./config'),
    http = require('http'),
    app = express(),
    server = http.createServer(app);

require('./index')(app, config);

console.log('create http server');
server.listen(config.port, function () {
  console.log('Express server listening on port ' + config.port);
});
