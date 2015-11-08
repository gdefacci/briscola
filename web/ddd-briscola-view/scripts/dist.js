require('shelljs/global')

var path = require('path')

var targetBase = "../../ddd-briscola-web/src/main/webapp"
var targetJs = path.join(targetBase, "./js")
var targetCss = path.join(targetBase, "./css")

cp("-f", "dist/*.js", targetJs)
cp("-f", "dist/*.js.map", targetJs)

cp("-f", "dist/*.css", targetCss)
cp("-f", "dist/*.css.map", targetCss)
cp("-f", "dist/*.jpg", targetCss)



