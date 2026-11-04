'use strict';
const $ = selector => document.querySelector(selector);
const $$ = selector => [...document.querySelectorAll(selector)];
const escapeHTML = value => String(value).replace(/[&<>"']/g, c => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
const positions = ['GK','DEF','MID','FWD'];
const slots = ['GK',...Array(4).fill('DEF'),...Array(4).fill('MID'),...Array(2).fill('FWD')];
let state, selection = Array(11).fill(null), activeSlot = null, positionFilter = 'ALL', busy = false, mode = 'register', toastTimer;
const player = id => state?.players.find(p => p.id === id);
const locked = () => Boolean(state?.user && state.user.week > 1);
const initials = name => name.split(' ').filter(Boolean).map(n=>n[0]).slice(0,2).join('');
const surname = name => name.split(' ').slice(-1)[0];
function toast(message, error = false) { clearTimeout(toastTimer); $('#toast').textContent=message; $('#toast').classList.toggle('error',error); $('#toast').hidden=false; toastTimer=setTimeout(()=>$('#toast').hidden=true,5500); }
async function request(path, body) {
  const response=await fetch(path,{method:body===undefined?'GET':'POST',headers:body===undefined?{}:{'Content-Type':'application/json','X-CSRF-Token':state?.csrf||''},body:body===undefined?undefined:JSON.stringify(body),credentials:'same-origin'});
  const data=await response.json().catch(()=>({message:'The server could not complete this request.'}));
  if(!response.ok) throw new Error(data.message || 'The request failed. Try again.');
  return data;
}
async function load(keepDraft=false) {
  const next=await request('/api/state');
  const previousUser=state?.user?.id; state=next;
  if(!keepDraft || previousUser!==state.user?.id) {
    selection=Array(11).fill(null);
    for(const id of state.team||[]) { const p=player(id); const index=slots.findIndex((pos,i)=>pos===p?.position&&!selection[i]); if(index>=0) selection[index]=id; }
  }
  $('#load-error').hidden=true; render();
}
function view(name) {
  $$('.view').forEach(el=>el.hidden=el.id!==`${name}-view`);
  $$('.tab').forEach(el=>{const active=el.dataset.view===name;el.classList.toggle('active',active); if(active) el.setAttribute('aria-current','page'); else el.removeAttribute('aria-current');});
}
function render() {
  const user=state.user, weeks=state.weeks||[], total=weeks.reduce((sum,w)=>sum+w.points,0);
  const rank=state.leaderboard.findIndex(u=>u.id===user?.id);
  $('#manager-name').hidden=!user; $('#manager-name').textContent=user?`↗ ${user.username}`:'';
  $('#auth-open').hidden=Boolean(user); $('#logout').hidden=!user;
  $('#week-label').innerHTML=`${String(Math.min(user?.week||1,38)).padStart(2,'0')} <span>/ 38</span>`;
  $('#season-caption').textContent=user?.week>38?'Season complete. Well played.':'Your next gameweek';
  $('#points-stat').textContent=user?total:'—'; $('#last-stat').textContent=weeks.length?`${weeks.at(-1).points} pts`:'—';
  $('#rank-stat').textContent=user?(rank>=0?`#${rank+1}`:'100+'): '—';
  $('#squad-hint').textContent=locked()?'Your starting XI is locked for this season.':'Select a position on the pitch to find your player.';
  $('#save-hint').textContent=locked()?'Lineup locked. Head to the match centre to play.':user?'Your lineup locks when you play your first gameweek.':'Join or sign in to save your squad and start a season.';
  $('#save-team').textContent=locked()?'Go to match centre →':'Save starting XI →';
  $('#quick-pick').disabled=locked()||busy;
  $('#simulate').disabled=busy||Boolean(user?.week>38);
  $('#simulate').textContent=user?.week>38?'Season complete':busy?'Playing…':`Simulate gameweek ${user?.week||1} →`;
  $('#reset-open').disabled=!user||busy;
  renderPitch(); renderPlayers(); renderMatches(); renderLeague();
}
function renderPitch() {
  for(const [pos,container] of Object.entries({GK:'goalkeeper',DEF:'defenders',MID:'midfielders',FWD:'forwards'})) {
    $('#'+container).innerHTML=slots.map((p,i)=>({p,i})).filter(x=>x.p===pos).map(({p,i})=>{
      const person=player(selection[i]);
      return `<button class="player-slot ${person?'selected':''} ${activeSlot===i?'active':''}" data-slot="${i}" aria-label="${person?escapeHTML(person.name)+', replace '+p:'Choose '+p}"><span class="shirt">${person?escapeHTML(initials(person.name)):'+'}</span><span class="slot-name">${person?escapeHTML(surname(person.name)):'Add player'}</span><span class="slot-pos">${p}${person&&locked()?` · ${(state.playerStats||[]).find(s=>s.id===person.id)?.points||0} PTS`:''}</span></button>`;
    }).join('');
  }
  const count=selection.filter(Boolean).length;
  $('#squad-stat').innerHTML=`${count} <small>/ 11</small>`;
  $('#selection-count').textContent=`${count} of 11 players selected`;
}
function renderPlayers() {
  const search=$('#search').value.trim().toLocaleLowerCase();
  const filtered=state.players.filter(p=>(positionFilter==='ALL'||p.position===positionFilter)&&(!search||`${p.name} ${p.club}`.toLocaleLowerCase().includes(search)));
  $('#player-count').textContent=filtered.length+' PLAYERS';
  $$('#position-filters button').forEach(b=>{const active=b.dataset.position===positionFilter;b.classList.toggle('active',active);b.setAttribute('aria-pressed',String(active));});
  $('#player-list').innerHTML=filtered.length?filtered.map(p=>`<button class="player-row ${selection.includes(p.id)?'chosen':''}" data-player="${p.id}" ${locked()?'disabled':''} aria-label="${selection.includes(p.id)?'Selected: ':'Choose '}${escapeHTML(p.name)}, ${escapeHTML(p.club)}, ${p.position}"><span class="initials">${escapeHTML(initials(p.name))}</span><span class="player-info"><strong>${escapeHTML(p.name)}</strong><small>${escapeHTML(p.club)}</small></span><span class="position-badge">${selection.includes(p.id)?'✓ ':''}${p.position}</span></button>`).join(''):'<p class="empty">No players found. Try another name, club or position.</p>';
}
function renderMatches() {
  const weeks=state.weeks||[], latest=weeks.at(-1), events=state.lastEvents||[];
  $('#match-summary').textContent=latest?`Gameweek ${latest.week} complete. Your starting XI earned ${latest.points} points.${state.user.week>38?' Your 38-gameweek season is complete.':''}`:'Save your starting XI, then play your first gameweek.';
  $('#fixtures').innerHTML=(state.fixtures||[]).map(m=>`<div class="fixture"><span>${escapeHTML(m.homeName)}</span><strong>${m.homeGoals} – ${m.awayGoals}</strong><span>${escapeHTML(m.awayName)}</span></div>`).join('');
  $('#results-week').textContent=latest?`GAMEWEEK ${latest.week}`:'No gameweeks yet';
  const mine=events.filter(e=>(state.team||[]).includes(e.id));
  $('#performance-body').innerHTML=mine.map(e=>`<tr><td>${escapeHTML(player(e.id)?.name||'Player')} <small>${escapeHTML(player(e.id)?.position||'')}</small></td><td>${e.minutes}</td><td>${e.goals}</td><td>${e.assists}</td><td>${e.yellow}</td><td>${e.red}</td><td>${e.points}</td></tr>`).join('');
  $('#performance-empty').hidden=mine.length>0;
}
function renderLeague() {
  $('#league-body').innerHTML=state.leaderboard.map((u,i)=>`<tr class="${u.id===state.user?.id?'you':''}"><td>${String(i+1).padStart(2,'0')}</td><td><button class="text-button" data-manager="${u.id}">${escapeHTML(u.username)}</button>${u.id===state.user?.id?' <small>YOU</small>':''}</td><td>${u.played} / 38</td><td>${u.points}</td></tr>`).join('');
  $('#league-empty').hidden=state.leaderboard.length>0;
}
function choose(id) {
  if(locked()) return toast('Your squad is locked. Reset your season to choose a new team.');
  if(selection.includes(id)) {
    const index=selection.indexOf(id); selection[index]=null; activeSlot=index;positionFilter=slots[index];renderPitch();renderPlayers(); return;
  }
  const p=player(id);
  let slot=activeSlot;
  if(slot===null||slots[slot]!==p.position) slot=slots.findIndex((pos,i)=>pos===p.position&&!selection[i]);
  if(slot<0) return toast(`Your ${p.position} positions are full. Select a shirt on the pitch to replace a player.`,true);
  selection[slot]=id; activeSlot=null;renderPitch();renderPlayers();
}
function openAuth() { if(!state) return toast('The game is still loading. Try again in a moment.');$('#auth-error').textContent='';$('#auth-dialog').showModal(); }
function authMode() {
  const join=mode==='register';
  $('#auth-title').textContent=join?'Your season starts here.':'Welcome back, manager.';
  $('#auth-copy').textContent=join?'Create your manager account to save a squad and play.':'Sign in to pick up where you left off.';
  $('#auth-submit').textContent=join?'Create manager account →':'Sign in →';
  $('#auth-toggle').textContent=join?'Already a manager? Sign in':'New here? Create an account';
  $('#password').minLength=join?8:1;$('#password').autocomplete=join?'new-password':'current-password';$('#password-note').hidden=!join;$('#auth-error').textContent='';
}
async function action(task) {
  if(busy)return;
  busy=true;document.body.classList.add('busy');if(state)render();
  try { await task(); } catch(e) { toast(e.message,true); } finally {busy=false;document.body.classList.remove('busy');if(state)render();}
}
$$('.tab').forEach(b=>b.addEventListener('click',()=>view(b.dataset.view)));
$('#search').addEventListener('input',()=>state&&renderPlayers());
$('#position-filters').addEventListener('click',e=>{const b=e.target.closest('[data-position]');if(!b)return;positionFilter=b.dataset.position;activeSlot=null;if(state){renderPitch();renderPlayers();}});
$('#pitch').addEventListener('click',e=>{
  const b=e.target.closest('[data-slot]');if(!b||!state)return;
  if(locked())return toast('This is your lineup for the season. See your scores in the match centre.');
  activeSlot=Number(b.dataset.slot);positionFilter=slots[activeSlot];$('#search').value='';renderPitch();renderPlayers();
  if(window.innerWidth<721)$('#search').scrollIntoView({behavior:'smooth',block:'center'});
  $('#search').focus({preventScroll:true});
});
$('#player-list').addEventListener('click',e=>{const b=e.target.closest('[data-player]');if(b&&!busy)choose(Number(b.dataset.player));});
$('#quick-pick').addEventListener('click',()=>{
  if(!state||locked())return;
  const pools=Object.fromEntries(positions.map(pos=>[pos,state.players.filter(p=>p.position===pos).map(p=>p.id)]));
  selection=slots.map(pos=>{const pool=pools[pos];return pool.splice(Math.floor(Math.random()*pool.length),1)[0];});
  activeSlot=null;renderPitch();renderPlayers();toast('Starting XI picked. Save your team when you’re ready.');
});
$('#save-team').addEventListener('click',()=>{
  if(!state)return;if(!state.user)return openAuth();if(locked())return view('gameweek');
  if(selection.some(id=>!id))return toast('Fill all 11 positions before saving your team.',true);
  action(async()=>{await request('/api/team',{players:selection});await load();toast('Starting XI saved. Your season is ready.');view('gameweek');});
});
$('#simulate').addEventListener('click',()=>{
  if(!state)return;if(!state.user)return openAuth();
  if((state.team||[]).length!==11)return toast('Save a complete starting XI before simulating.',true);
  action(async()=>{await request('/api/simulate',{week:state.user.week});await load();toast('Full time. Your gameweek results are in.');});
});
$('#auth-open').addEventListener('click',openAuth);
$('#auth-toggle').addEventListener('click',()=>{mode=mode==='register'?'login':'register';authMode();});
$('#auth-form').addEventListener('submit',async e=>{
  e.preventDefault();if(busy)return;
  const button=$('#auth-submit');button.disabled=true;$('#auth-error').textContent='';
  await action(async()=>{try {
    const draft=[...selection];
    await request('/api/'+mode,{username:$('#username').value.trim(),password:$('#password').value});
    await load();
    if(!state.team?.length&&draft.some(Boolean)){selection=draft;render();}
    $('#auth-dialog').close();$('#password').value='';toast(`Welcome, ${state.user.username}. Your dugout is ready.`);
  }catch(error){$('#auth-error').textContent=error.message;}});
  button.disabled=false;
});
$('#logout').addEventListener('click',()=>action(async()=>{await request('/api/logout',{});await load();view('squad');toast('Signed out. See you next matchday.');}));
$$('.close-dialog').forEach(b=>b.addEventListener('click',()=>b.closest('dialog').close()));
$('#reset-open').addEventListener('click',()=>{if(!state?.user)return openAuth();$('#reset-confirmation').value='';$('#reset-error').textContent='';$('#reset-dialog').showModal();});
$('#reset-form').addEventListener('submit',async e=>{
  e.preventDefault();if(busy)return;
  const button=$('#reset-form button[type=submit]');button.disabled=true;
  await action(async()=>{try{await request('/api/reset',{confirmation:$('#reset-confirmation').value});await load();$('#reset-dialog').close();view('squad');toast('A fresh season. Your lineup is ready to edit.');}catch(error){$('#reset-error').textContent=error.message;}});
  button.disabled=false;
});
$('#league-body').addEventListener('click',async e=>{
  const b=e.target.closest('[data-manager]');if(!b)return;
  try{
    const data=await request(`/api/managers/${b.dataset.manager}/team`);
    $('#public-manager').textContent=data.manager.username+'’s starting XI';
    const team=data.team.map(player).filter(Boolean).sort((a,b)=>positions.indexOf(a.position)-positions.indexOf(b.position));
    $('#public-team').innerHTML=team.length?team.map(p=>`<div class="manager-player"><div>${escapeHTML(p.name)}<small>${p.position} · ${escapeHTML(p.club)}</small></div><strong>${data.stats.find(s=>s.id===p.id)?.points||0} pts</strong></div>`).join(''):'<p>This manager is still choosing a squad.</p>';
    $('#manager-dialog').showModal();
  }catch(error){toast(error.message,true);}
});
$('#retry').addEventListener('click',()=>load().catch(()=>toast('Still unable to connect. Please try again later.',true)));
load().catch(()=>{$('#load-error').hidden=false;$('#player-list').innerHTML='<p class="empty">The game could not be loaded. Use the retry button above.</p>';});
