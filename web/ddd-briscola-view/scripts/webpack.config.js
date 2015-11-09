var ExtractTextPlugin = require("extract-text-webpack-plugin");
var path = require("path");

var distFolder = process.env.npm_config_deployFolder !== undefined ?
      path.join(process.env.npm_config_deployFolder, ".") :
      path.join(__dirname, "../dist") ;

module.exports = {
  context: path.join(__dirname, "../src"),
  entry: './Main.tsx',
  displayErrorDetails: true,
  debug: true,
  output: {
    path: distFolder,
    filename: 'ddd-briscola-view.browser.js'
  },
  resolve: {
    extensions: ['', '.webpack.js', '.web.js', '.ts', '.tsx','.js', '.styl']
  },
  devtool: 'source-map',
  module: {
    preLoaders: [
      {
        test: /\.js$/,
        loader: "source-map-loader"
      }
    ],
    loaders: [
      { test: /\.tsx?$/, loader: 'ts-loader' },
      { test: /\.styl$/, loader: ExtractTextPlugin.extract('css-loader!stylus-loader') },
      { test: /\.jpg$/, loader: "file-loader" }
    ]
  },
  stylus: {
    use: [require('jeet')()]
  },
  plugins: [
      new ExtractTextPlugin("[name].css")
  ]
}