'use strict';
const sources=['https://cdn.jsdelivr.net/npm/kuromoji@0.1.2','https://unpkg.com/kuromoji@0.1.2'];
let tokenizerPromise,base=sources[0],loaded=new Set();
// kuromoji's path.join is for file paths, not URLs. Route the dictionary
// filenames to an absolute CDN URL after its internal path.join has run.
const originalOpen=XMLHttpRequest.prototype.open;
XMLHttpRequest.prototype.open=function(method,url,...args){
 const match=String(url).match(/^\/?__kotonoha_dict__\/([a-z_]+\.dat\.gz)$/);
 if(match){const file=match[1];url=base+'/dict/'+file;this.addEventListener('load',()=>{if(this.status===200){loaded.add(file);self.postMessage({progress:loaded.size,total:12});}});}
 return originalOpen.call(this,method,url,...args);
};
function build(){return new Promise((resolve,reject)=>{let timer=setTimeout(()=>reject(new Error('字典下載逾時')),40000);try{self.kuromoji.builder({dicPath:'/__kotonoha_dict__/'}).build((error,t)=>{clearTimeout(timer);error?reject(new Error(error.message||error.statusText||String(error))):resolve(t);});}catch(e){clearTimeout(timer);reject(e);}});}
async function init(){const errors=[];for(const source of sources){base=source;loaded=new Set();self.postMessage({progress:0,total:12,source:new URL(source).hostname});try{if(!self.kuromoji)importScripts(source+'/build/kuromoji.js');if(!self.kuromoji)throw Error('找不到 kuromoji');return await build();}catch(e){errors.push(new URL(source).hostname+': '+(e.message||String(e)));}}throw Error(errors.join('；'));}
function tokenizer(){if(!tokenizerPromise)tokenizerPromise=init().catch(e=>{tokenizerPromise=null;throw e;});return tokenizerPromise;}
self.onmessage=async({data})=>{try{const t=await tokenizer();self.postMessage({id:data.id,tokens:data.texts.map(x=>t.tokenize(x).map(({surface_form,reading})=>({surface_form,reading})))});}catch(e){self.postMessage({id:data.id,error:e.message||String(e)});}};
