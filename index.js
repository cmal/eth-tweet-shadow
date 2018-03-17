var express = require('express');
var glob = require('glob');

module.exports = function(app, config) {
  app.use(express.static(config.root + '/target'));

  // views
  app.set('views', config.root + '/app/views');
  app.set('view engine', 'ejs');

  // controllers
  var controllersPath = config.root + '/app/controllers/*.js';
  var controllers = glob.sync(controllersPath);
  controllers.forEach(function(controller) {
    require(controller)(app);
  });
}
