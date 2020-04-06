require('./check-versions')()

var config = require('../config')
if (!process.env.NODE_ENV) {
  process.env.NODE_ENV = JSON.parse(config.dev.env.NODE_ENV)
}

var opn = require('opn')
var path = require('path')
var express = require('express')
var webpack = require('webpack')
var proxyMiddleware = require('http-proxy-middleware')
var webpackConfig = require('./webpack.dev.conf')

// default port where dev server listens for incoming traffic
var port = process.env.PORT || config.dev.port
if (process.argv[2] === '--local') {
  port = 8091
}
// <<<<<<< HEAD
port = 8093
// =======
// port = 8094
// >>>>>>> ce55a63e31a0b74836a5426d38bb75309f1fabb5
// automatically open browser, if not set will be false
var autoOpenBrowser = !!config.dev.autoOpenBrowser
// Define HTTP proxies to your custom API backend
// https://github.com/chimurai/http-proxy-middleware
var proxyTable = config.dev.proxyTable

var app = express()
var compiler = webpack(webpackConfig)

var devMiddleware = require('webpack-dev-middleware')(compiler, {
  publicPath: webpackConfig.output.publicPath,
  quiet: true
})

var hotMiddleware = require('webpack-hot-middleware')(compiler, {
  log: () => {}
})
// force page reload when html-webpack-plugin template changes
compiler.plugin('compilation', function (compilation) {
  compilation.plugin('html-webpack-plugin-after-emit', function (data, cb) {
    hotMiddleware.publish({ action: 'reload' })
    cb()
  })
})

// proxy api requests
Object.keys(proxyTable).forEach(function (context) {
  var options = proxyTable[context]
  if (typeof options === 'string') {
    options = { target: options }
  }
  app.use(proxyMiddleware(options.filter || context, options))
})

// extra api white-list for proxy.
var customProxyFilter = function (pathname, req) {
  if (config.dev.proxyReqList) {
    for (var i in config.dev.proxyReqList) {
      var prefix = config.dev.proxyReqList[i];
      if (prefix[0] === '^') {
        if (pathname.match(new RegExp(prefix))) {
          console.log(pathname);
          return true;
        }
      }
      else {
        if (pathname === prefix) {
          console.log(pathname);
          return true;
        }
      }
    }
  }
  return false;
}

if (process.argv.length > 2) {
  var host;
  if (process.argv[2] === '--local') {
    host = config.dev.localHost
  }
  else if (process.argv[2] === '--remote') {
    host = config.dev.remoteHost
  }
  if (host) {
    app.use(proxyMiddleware(customProxyFilter, {
      target: host,
      changeOrigin: true,
      autoRewrite: true,
      cookieDomainRewrite: true,
    }))
  }
}

// handle fallback for HTML5 history API
app.use(require('connect-history-api-fallback')())

// serve webpack bundle output
app.use(devMiddleware)

// enable hot-reload and state-preserving
// compilation error display
app.use(hotMiddleware)

// serve pure static assets
var staticPath = path.posix.join(config.dev.assetsPublicPath, config.dev.assetsSubDirectory)
app.use(staticPath, express.static('./static'))

var uri = 'http://localhost:' + port

var _resolve
var readyPromise = new Promise(resolve => {
  _resolve = resolve
})

console.log('> Starting dev server...')
devMiddleware.waitUntilValid(() => {
  console.log('> Listening at ' + uri + '\n')
  // when env is testing, don't need open it
  if (autoOpenBrowser && process.env.NODE_ENV !== 'testing') {
    opn(uri)
  }
  _resolve()
})

var server = app.listen(port)

module.exports = {
  ready: readyPromise,
  close: () => {
    server.close()
  }
}
