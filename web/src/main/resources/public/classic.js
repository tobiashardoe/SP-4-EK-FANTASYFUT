'use strict';
const classicMenu=document.querySelector('#desktop-menu');
const classicBar=document.querySelector('#classic-screen-bar');
const screenTitles={squad:'CREATE YOUR TEAM',gameweek:'SIMULATE GAMEWEEK',league:'LEAGUE STANDINGS'};
function classicScreen(name) {
  const menu=name==='menu';
  document.body.dataset.screen=name;
  classicMenu.hidden=!menu;
  classicBar.hidden=menu;
  if(!menu) document.querySelector('#classic-screen-name').textContent=screenTitles[name];
  document.title=(menu?'EK Football Fantasy':screenTitles[name]);
}
document.querySelectorAll('.view').forEach(el=>el.hidden=true);
document.querySelectorAll('[data-screen-target]').forEach(button=>button.addEventListener('click',()=>{
  view(button.dataset.screenTarget);
  classicScreen(button.dataset.screenTarget);
  window.scrollTo({top:0,behavior:'smooth'});
}));
document.querySelector('#classic-back').addEventListener('click',()=>{
  document.querySelectorAll('.view').forEach(el=>el.hidden=true);
  classicScreen('menu');window.scrollTo({top:0,behavior:'smooth'});
});
// Saving a team also changes views in the shared game code.
const classicViews=new MutationObserver(()=>{
  const visible=[...document.querySelectorAll('.view')].find(el=>!el.hidden);
  if(visible) classicScreen(visible.id.replace('-view',''));
});
document.querySelectorAll('.view').forEach(el=>classicViews.observe(el,{attributes:true,attributeFilter:['hidden']}));
document.querySelector('#auth-open').textContent='LOGIN / CREATE';
classicScreen('menu');
