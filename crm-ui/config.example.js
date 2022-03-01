module.exports = {
  dev: {
    js: {
      minify: false
    },
    css: {
      cssnano: false
    },
    apiServerUrl: 'http://localhost:8080',
    configServerUrl: 'https://TODO.knowprocess.com',
  },
  previous: {
    js: {
      minify: true
    },
    css: {
      cssnano: true
    },
    apiServerUrl: 'https://TODO.knowprocess.com',
    configServerUrl: 'https://TODO.knowprocess.com',
    server: {
      host: 'TODO.com',
      usr: 'TODO',
      dir: '/var/www-TODO',
      privateKey: '/home/TODO/.ssh/id_rsa'
    }
  },
  stage: {
    js: {
      minify: true
    },
    css: {
      cssnano: true
    },
    apiServerUrl: 'https://TODO.knowprocess.com',
    configServerUrl: 'https://TODO.knowprocess.com',
    server: {
      host: 'TODO.com',
      usr: 'TODO',
      dir: '/var/www-TODO',
      privateKey: '/home/TODO/.ssh/id_rsa'
    }
  },
  prod: {
    js: {
      minify: false
    },
    css: {
      cssnano: true
    },
    apiServerUrl:  'https://TODO.knowprocess.com',
    configServerUrl: '',
    server: {
      host: 'TODO.com',
      usr: 'TODO',
      dir: '/var/www-TODO',
      privateKey: '/home/TODO/.ssh/id_rsa'
    }
  }
};
