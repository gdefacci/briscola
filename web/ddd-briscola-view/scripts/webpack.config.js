var ExtractTextPlugin = require("extract-text-webpack-plugin");
var path = require("path");

module.exports = {
  context: path.join(__dirname, "../src"),
  entry: './Main.tsx',
  displayErrorDetails: true,
  debug: true,
  output: {
    path: path.join(__dirname, "../dist"),
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