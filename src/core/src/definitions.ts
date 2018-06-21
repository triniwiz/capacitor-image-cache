declare global {
  interface PluginRegistry {
    ImageCachePlugin?: IImageCache;
  }
}

export interface IImageCache {
  get(options: { src: string , overwrite?: boolean}): Promise<{value: string}>;
  hasItem(options: { src: string }):Promise<{value: boolean}>;
  clearItem(options: { src: string }):Promise<{value: boolean}>;
  clear():Promise<{value: boolean}>;
}
