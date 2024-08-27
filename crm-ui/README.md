CRM user interface
==================

Getting started
---------------

1. Install `npm`
2. Install `gulp`
   ```
   npm install --global gulp-cli
   ```
3. Configure the different environments
   ```
   cp config.example.js config.js 
   ```
   and edit the paths and user as necessary

4. Deploy
   ```
   gulp deploy --env=stage
