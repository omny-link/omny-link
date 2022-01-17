module.exports = {
  dev: {
    js: {
      minify: false
    },
    css: {
      cssnano: false
    },
    apiServerUrl: 'http://localhost:8080',
    configServerUrl: 'http://localhost:8080'
  },
  stage: {
    js: {
      minify: false
    },
    css: {
      cssnano: true
    },
    apiServerUrl: 'https://api.example.com',
    configServerUrl: 'http://localhost:8080',
    server: {
      host: 'example.com',
      usr: 'usr',
      dir: '/var/www-app',
      privateKey: '/home/usr/.ssh/id_rsa'
    }
  },
  prod: {
    js: {
      minify: false
    },
    css: {
      cssnano: true
    },
    apiServerUrl:  'https://api.example.com',
    configServerUrl: '',
    server: {
      host: 'example.com',
      usr: 'usr',
      dir: '/var/www-app',
      privateKey: '/home/usr/.ssh/id_rsa'
    }
  }
};
