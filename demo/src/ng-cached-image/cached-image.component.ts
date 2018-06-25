import { Component, Input, OnChanges, SimpleChanges } from '@angular/core';
import { ImageCache } from 'capacitor-image-cache';
import { Observable } from 'rxjs'

function uuid() {
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function (c) {
    const r = (Math.random() * 16) | 0,
      v = c === 'x' ? r : (r & 0x3) | 0x8;
    return v.toString(16);
  });
}

@Component({
  selector: 'cached-image',
  template: `
    <div>
      <img [id]="id" [src]="source | async" width="100px">
    </div>
  `,
  styles: [`
    div {
      width: 100%;
      height: 100%;
    }

    div img {
      width: 100%;
      height: 100%;
      object-fit: fill;
    }
  `]
})
export class CachedImageComponent implements OnChanges {
  public id: string;
  private cache: ImageCache;
  public source: Observable<string>;
  @Input('src') src: string;

  constructor() {
    this.id = uuid();
    this.cache = new ImageCache();
  }

  ngOnChanges(changes: SimpleChanges): void {
    const src = changes['src'];
    if (src) {
      this.source = Observable.fromPromise(this.cache.get({src: src.currentValue})).map(v => v.value)
    }
  }
}
