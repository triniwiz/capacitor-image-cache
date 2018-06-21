[![npm](https://img.shields.io/npm/v/st-cached-image.svg)](https://www.npmjs.com/package/st-cached-image)
[![npm](https://img.shields.io/npm/dt/st-cached-image.svg?label=npm%20downloads)](https://www.npmjs.com/package/st-cached-image)
[![Build Status](https://travis-ci.org/triniwiz/st-cached-image.svg?branch=master)](https://travis-ci.org/triniwiz/st-cached-image)

# Cached Image 

## Install
**[Capacitor Image Cache](https://www.npmjs.com/package/capacitor-image-cache)** is require for this package
* `npm i capacitor-image-cache st-cached-image`

## Usage



```html
<st-cached-image src="https://i.annihil.us/u/prod/marvel/i/mg/e/e0/537bafa34baa9.jpg"></st-cached-image>
```

**If using with ionic**

```
import 'st-cached-image/dist/cached-image';
```

Update the following file since ionic ignores the file

`youAppDir/node_modules/@ionic/app-scripts/config/copy.config.js`


```js
copyStCachedImage: {
    src: ['{{ROOT}}/node_modules/st-cached-image/dist/cached-image**/*'],
    dest: '{{BUILD}}'
  }
```