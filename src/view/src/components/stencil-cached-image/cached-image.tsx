import { Component, Prop, Watch, Element, State } from '@stencil/core';
import { ImageCache } from 'capacitor-image-cache';


function uuid() {
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
    const r = (Math.random() * 16) | 0,
      v = c === 'x' ? r : (r & 0x3) | 0x8;
    return v.toString(16);
  });
}

@Component({
  tag: 'st-cached-image',
  styleUrl: 'cached-image.css',
  shadow: true
})

export class CachedImage {
  private cache: ImageCache;
  @State() private readonly id: string;
  constructor() {
    this.cache = new ImageCache();
    this.id = uuid();
  }

  @Prop() src: string;

  @Prop() placeHolder: string;

  @Prop() errorHolder: string;

  @Element() element :HTMLElement;


  @Watch('src')
  srcChanged() {
    this.loadImage();
  }

  render() {
    return (
      <div>
        <img id={this.id}/>
      </div>
    );
  }

  private async loadImage(){
    try {
      const { value }  = await this.cache.get({src: this.src});
      this.element.shadowRoot.querySelector(`img`).setAttribute('src',value);
    }catch (e) {
      console.log(e)
    }
  }


  componentWillUpdate() {
    this.loadImage()
  }

  componentDidLoad() {
    this.loadImage();
  }
}
