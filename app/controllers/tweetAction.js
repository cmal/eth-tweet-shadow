var exprss = require('express');
var router = exprss.Router();

module.exports = function(app) {
  app.use('/', router);
}

router.get('/tweet', (req, res, next) => {
  res.render('tweet/index', {
    title: 'Tweets App'
  })
});
