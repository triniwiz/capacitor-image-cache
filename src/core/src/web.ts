import { WebPlugin } from '@capacitor/core';
import { IImageCache } from './definitions';

export class ImageCachePluginWeb extends WebPlugin implements IImageCache {

  constructor() {
    super({
      name: 'ImageCachePlugin',
      platforms: ['web']
    });
  }

    clear(): Promise<{ value: boolean }> {
        return new Promise(()=>{});
    }

    clearItem(options: { src: string }): Promise<{ value: boolean }> {
        return new Promise(()=>{
          console.log(options);
        });
    }

    hasItem(options: { src: string }): Promise<{ value: boolean }> {
        return new Promise(()=>{
            console.log(options);
        });
    }

    get(options: { src: string, overwrite?: boolean }): Promise<{ value: string }> {
        return new Promise((resolve)=>{
            // return default src for now
            resolve({value:options.src});
        });
    }
}

const ImageCachePlugin = new ImageCachePluginWeb();

export { ImageCachePlugin };

