(function(root,factory){const api=factory();if(typeof module==='object')module.exports=api;else root.SystemEngine=api;})(typeof globalThis!=='undefined'?globalThis:this,function(){
'use strict';
const META='[ERROR: System cannot process real-world metadata.]';
const MODULES=['planner','timetable','budget','levelup','focus'];
const ALIASES=['Nova-07','Echo-12','Orion-03','Astra-09'];
const SHIELDS=['Aegis','Prism','Orbit'];
const CATEGORIES=['Food','Travel','Learning','Personal','Other'];
const QUESTS=['Study','Practice','Read','Build','Exercise','Organize'];
const EVENTS=['Class','Study','Work','Activity','Break'];
const CURRENCIES=['SGD','USD','EUR','GBP','INR'];
const EFFORT={small:20,medium:40,large:60};
function fail(msg='Invalid choice.'){throw new Error(msg);}
function keys(o,allowed){if(!o||typeof o!=='object'||Array.isArray(o)||Object.keys(o).some(k=>!allowed.includes(k)))fail(META);}
function pick(v,allowed){if(!allowed.includes(v))fail(META);return v;}
function integer(v,min,max){if(!Number.isSafeInteger(v)||v<min||v>max)fail('Enter a whole number in range.');return v;}
function date(v){if(typeof v!=='string'||!/^\d{4}-\d{2}-\d{2}$/.test(v))fail('Choose a valid date.');const d=new Date(v+'T12:00:00Z');if(Number.isNaN(d.getTime())||d.toISOString().slice(0,10)!==v)fail('Choose a valid date.');return v;}
function today(){const d=new Date();return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}`;}
function initial(){return {version:1,settings:null,quests:[],events:[],expenses:[],rewards:[],nextId:1};}
function validateSettings(s){keys(s,['modules','alias','shield','currency','monthlyLimit','limits','reminder']);if(!Array.isArray(s.modules)||s.modules.length<1||s.modules.length>5||new Set(s.modules).size!==s.modules.length)fail('Choose at least one module.');s.modules.forEach(m=>pick(m,MODULES));pick(s.alias,ALIASES);pick(s.shield,SHIELDS);pick(s.currency,CURRENCIES);integer(s.monthlyLimit,0,100000000);keys(s.limits,CATEGORIES);if(Object.keys(s.limits).length!==CATEGORIES.length)fail();let sum=0;CATEGORIES.forEach(c=>{sum+=integer(s.limits[c],0,100000000);});if(sum>s.monthlyLimit)fail('Category limits must fit the monthly ceiling.');pick(s.reminder,['off','18:00']);return structuredClone(s);}
function validateState(s){keys(s,['version','settings','quests','events','expenses','rewards','nextId']);if(s.version!==1)fail('Unsupported data version.');integer(s.nextId,1,1000000000);if(s.settings!==null)validateSettings(s.settings);
 const ids=new Set();function id(n){integer(n,1,s.nextId-1);if(ids.has(n))fail('Duplicate record.');ids.add(n);}
 for(const name of ['quests','events','expenses','rewards'])if(!Array.isArray(s[name])||s[name].length>2000)fail('Local record capacity reached.');
 s.quests.forEach(q=>{keys(q,['id','kind','effort','day','done']);id(q.id);pick(q.kind,QUESTS);pick(q.effort,Object.keys(EFFORT));date(q.day);if(typeof q.done!=='boolean')fail();});
 s.events.forEach(e=>{keys(e,['id','kind','slot','weekday','start','end']);id(e.id);pick(e.kind,EVENTS);integer(e.slot,1,99);integer(e.weekday,0,6);integer(e.start,0,1439);integer(e.end,e.start+1,1440);});
 s.expenses.forEach(e=>{keys(e,['id','category','amount','day']);id(e.id);pick(e.category,CATEGORIES);integer(e.amount,1,100000000);date(e.day);});
 const sources=new Set();s.rewards.forEach(r=>{keys(r,['id','source','ref','xp','gold','day']);id(r.id);pick(r.source,['quest','focus']);integer(r.ref,0,1000000000);integer(r.xp,0,60);integer(r.gold,0,6);date(r.day);if(r.source==='quest'){const q=s.quests.find(q=>q.id===r.ref&&q.done);if(!q||q.day!==r.day||r.xp>EFFORT[q.effort])fail('Invalid quest reward.');const key='q'+r.ref;if(sources.has(key))fail('Duplicate reward.');sources.add(key);}else if(r.ref!==0)fail();if(r.gold!==Math.floor(r.xp/10))fail();});
 for(const day of new Set(s.rewards.map(r=>r.day)))if(s.rewards.filter(r=>r.day===day).reduce((v,r)=>v+r.xp,0)>300)fail('Daily reward cap exceeded.');return s;
}
class Engine {
 constructor(saved,clock=()=>performance.now()){this.state=structuredClone(saved||initial());validateState(this.state);this.clock=clock;this.focus=null;}
 snapshot(){return structuredClone(this.state);}
 stats(){const xp=this.state.rewards.reduce((n,r)=>n+r.xp,0);return {xp,level:1+Math.floor(xp/200),gold:this.state.rewards.reduce((n,r)=>n+r.gold,0),progress:xp%200};}
 require(module){if(!this.state.settings?.modules.includes(module))fail('Enable this module in settings first.');}
 capacity(name,count=1){if(this.state[name].length+count>2000)fail('Local record capacity reached.');}
 id(){return this.state.nextId++;}
 reward(source,ref,xp,day){if(!this.state.settings.modules.includes('levelup'))return;this.capacity('rewards');const earned=this.state.rewards.filter(r=>r.day===day).reduce((n,r)=>n+r.xp,0);xp=Math.max(0,Math.min(xp,300-earned));this.state.rewards.push({id:this.id(),source,ref,xp,gold:Math.floor(xp/10),day});}
 dispatch(action){keys(action,['type','payload']);const p=action.payload;const type=action.type;
 // All mutations are transactional; failed validation cannot leave a partially changed state.
 const before=this.snapshot();const oldFocus=this.focus;
 try{switch(type){
 case 'settings':{const next=validateSettings(p);this.state.settings=next;if(!next.modules.includes('focus'))this.focus=null;break;}
 case 'addQuest':{this.require('planner');keys(p,['kind','effort','day']);pick(p.kind,QUESTS);pick(p.effort,Object.keys(EFFORT));date(p.day);this.capacity('quests');this.state.quests.push({id:this.id(),kind:p.kind,effort:p.effort,day:p.day,done:false});break;}
 case 'completeQuest':{this.require('planner');keys(p,['id']);integer(p.id,1,1000000000);const q=this.state.quests.find(q=>q.id===p.id);if(!q||q.done)fail('Quest already completed or unavailable.');if(q.day!==today())fail('Only today’s quests can be completed.');q.done=true;this.reward('quest',q.id,EFFORT[q.effort],q.day);break;}
 case 'deleteQuest':{this.require('planner');keys(p,['id']);integer(p.id,1,1000000000);const q=this.state.quests.find(q=>q.id===p.id);if(!q||q.done)fail('Only unfinished quests can be removed.');this.state.quests=this.state.quests.filter(q=>q.id!==p.id);break;}
 case 'addEvent':{this.require('timetable');keys(p,['kind','slot','weekday','start','end']);pick(p.kind,EVENTS);integer(p.slot,1,99);integer(p.weekday,0,6);integer(p.start,0,1439);integer(p.end,p.start+1,1440);if(this.state.events.some(e=>e.weekday===p.weekday&&p.start<e.end&&p.end>e.start))fail('This time overlaps another block.');this.capacity('events');this.state.events.push({id:this.id(),...p});break;}
 case 'deleteEvent':{this.require('timetable');keys(p,['id']);integer(p.id,1,1000000000);if(!this.state.events.some(e=>e.id===p.id))fail();this.state.events=this.state.events.filter(e=>e.id!==p.id);break;}
 case 'addExpense':{this.require('budget');keys(p,['category','amount','day']);pick(p.category,CATEGORIES);integer(p.amount,1,100000000);date(p.day);this.capacity('expenses');this.state.expenses.push({id:this.id(),...p});break;}
 case 'deleteExpense':{this.require('budget');keys(p,['id']);integer(p.id,1,1000000000);if(!this.state.expenses.some(e=>e.id===p.id))fail();this.state.expenses=this.state.expenses.filter(e=>e.id!==p.id);break;}
 case 'startFocus':{this.require('focus');keys(p,['minutes']);integer(p.minutes,1,120);if(this.focus)fail('A focus session is already running.');this.focus={start:this.clock(),minutes:p.minutes};break;}
 case 'cancelFocus':{keys(p,[]);this.focus=null;break;}
 case 'finishFocus':{this.require('focus');keys(p,[]);if(!this.focus||this.clock()-this.focus.start<this.focus.minutes*60000)fail('Finish the full timer to earn a reward.');this.reward('focus',0,Math.min(60,this.focus.minutes*2),today());this.focus=null;break;}
 default:fail('Unsupported action. Rewards cannot be edited.');}
 validateState(this.state);return this.snapshot();
 }catch(e){this.state=before;this.focus=oldFocus;throw e;}}
 remaining(){return this.focus?Math.max(0,Math.ceil((this.focus.minutes*60000-(this.clock()-this.focus.start))/1000)):null;}
 budget(month){if(!/^\d{4}-\d{2}$/.test(month))fail();const spent=Object.fromEntries(CATEGORIES.map(c=>[c,0]));this.state.expenses.filter(e=>e.day.slice(0,7)===month).forEach(e=>spent[e.category]+=e.amount);const total=Object.values(spent).reduce((a,b)=>a+b,0);return {spent,total,remaining:(this.state.settings?.monthlyLimit||0)-total};}
}
return {Engine,initial,validateSettings,validateState,today,MODULES,ALIASES,SHIELDS,CATEGORIES,QUESTS,EVENTS,CURRENCIES,META};
});
