var exprss = require('express');
var router = exprss.Router();

module.exports = function(app) {
  app.use('/', router);
}

router.get('/hello', (req, res, next) => {
  console.log('get hello');
  res.send('Hello World');
});
