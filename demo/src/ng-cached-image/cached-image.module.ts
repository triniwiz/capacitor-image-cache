import { NgModule } from '@angular/core';
import { CachedImageComponent } from './cached-image.component';
import { BrowserModule } from '@angular/platform-browser';

@NgModule({
  imports:[
    BrowserModule,
  ],
  declarations: [CachedImageComponent],
  exports: [CachedImageComponent],
})

export class CachedImageModule {}
