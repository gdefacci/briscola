var path = require("path");

module.exports = {  
  context: path.join(__dirname, "../src"),
  entry: [
    './test/app_test.spec.ts'
  ],  displayErrorDetails: true,
  debug: true,
  output: {
    filename: 'ddd-briscola-app-test.browser.js'
  },
  resolve: {
    extensions: ['', '.webpack.js', '.web.js', '.ts', '.js']
  },
  resolveLoader: {
  root: path.join(__dirname, 'node_modules')
},
  devtool: 'source-map',
  module: {
    /*
    preLoaders: [
      {
        test: /\.js$/,
        loader: "source-map-loader"
      }
    ],
    */
    loaders: [
      { test: /\.ts$/, loader: 'ts-loader' }
    ]
  }
}