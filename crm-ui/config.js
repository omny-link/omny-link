module.exports = {
  dev: {
    js: {
      minify: false
    },
    css: {
      cssnano: false
    },
    apiServerUrl: 'http://localhost:8080'
  },
  stage: {
    js: {
      minify: false
    },
    css: {
      cssnano: true
    },
    apiServerUrl: 'https://api.knowprocess.com'
  },
  prod: {
    js: {
      minify: false
    },
    css: {
      cssnano: true
    },
    apiServerUrl:  'https://v3.knowprocess.com'
  }
};
