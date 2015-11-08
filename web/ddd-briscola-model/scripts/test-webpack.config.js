var path = require("path");

module.exports = {  
  context: path.join(__dirname, "../src"),
  entry: './test/test.ts',
  displayErrorDetails: true,
  debug: true,
  output: {
    filename: 'ddd-briscola-model-test.browser.js'
  },
  resolve: {
    extensions: ['', '.webpack.js', '.web.js', '.ts', '.js']
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
      { test: /\.ts$/, loader: 'ts-loader' }
    ]
  }
}