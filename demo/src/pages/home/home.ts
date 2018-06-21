import { Component } from '@angular/core';
import { NavController } from 'ionic-angular';
const data = require('./data.json');
@Component({
  selector: 'page-home',
  templateUrl: 'home.html'
})
export class HomePage {

  constructor(public navCtrl: NavController) {}

  get items():Array<string>{
    return data['items'];
  }

}
