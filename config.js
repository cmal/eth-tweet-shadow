var path = require('path');
var rootPath = path.normalize(__dirname);

var config = {
  root: rootPath,
  port: 6379,
  app: {
    name: 'tweet'
  }
}


module.exports = config;
