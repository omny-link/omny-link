Vite experiment
===============

Migration steps needed to get to dev running
--------------------------------------------

- move all source up a level from `src` to root
- move sub-pages (e.g. `accounts.html` to sub-dirs (`accounts/index.html`)
- reference all js that is part of the app as module
- add imports / exports to local js instead of relying on globals
- modify support.js to extend BaseRactive and change users to extend that instead
- change file extension of partials from html to ractive
- jquery can be imported as module but ractive cannot render partials that way (at least with 0.7.3)
- .env file can obviate the need for the env.js from the server (trimming out one http call)

Further to get to a production build
------------------------------------
- copy partials, images, keycloak.json etc. into dist.

The net result of this is that only a few network calls are saved because images and partials make up the majority (43 down from 49)

- some success importing (e.g. import imgAjaxLoader from '../images/ajax-loader.gif';) but not for keycloak or partials. Maybe partials could be imported with a suitable plugin but all roads seem to lead to Vue in this regard.
