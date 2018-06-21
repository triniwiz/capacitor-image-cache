import { registerWebPlugin } from '@capacitor/core';

export * from './web';

import { ImageCachePlugin } from './web';
registerWebPlugin(ImageCachePlugin)