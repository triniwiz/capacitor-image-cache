# Image Cache Core

[![npm](https://img.shields.io/npm/v/capacitor-image-cache.svg)](https://www.npmjs.com/package/capacitor-image-cache)
[![npm](https://img.shields.io/npm/dt/capacitor-image-cache.svg?label=npm%20downloads)](https://www.npmjs.com/package/capacitor-image-cache)
[![Build Status](https://travis-ci.org/triniwiz/capacitor-image-cache.svg?branch=master)](https://travis-ci.org/triniwiz/capacitor-image-cache)

`npm i capacitor-image-cache`


```ts
import { ImageCache } from 'capacitor-image-cache';

const cache = new ImageCache();

const { value } = await cache.get({src:"someSrc"}) // checks if the image is in cache and returns the image if not download, store then return

const { value } = await cache.clear() // Clear entire cache returns a boolean

const { value } = await cache.clearItem({src:"someSrc"}) // Clear item cache returns a boolean

const { value } = await cache.hasItem({src:"someSrc"}) // check if cache has item returns a boolean

```

# Cached Image

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