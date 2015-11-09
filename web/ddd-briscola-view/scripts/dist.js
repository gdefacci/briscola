require('shelljs/global')

var path = require('path')

var target = "../../ddd-briscola-web/src/main/webapp/app"

cp("-f", "dist/*.js", target)
cp("-f", "dist/*.js.map", target)

cp("-f", "dist/*.css", target)
cp("-f", "dist/*.css.map", target)
cp("-f", "dist/*.jpg", target)



