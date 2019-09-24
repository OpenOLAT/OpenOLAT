/**
 * This file contains helper methods for the olatcore web app framework and the
 * learning management system OLAT
 */

/** OpenOLAT namespace **/
OPOL = {};

//used to mark form dirty and warn user to save first.
var o2c=0;
var o3c=new Array();//array holds flexi.form id's
// o_info is a global object that contains global variables
o_info.guibusy = false;
o_info.linkbusy = false;
o_info.scrolling = false;
//debug flag for this file, to enable debugging to the olat.log set JavaScriptTracingController to level debug
o_info.debug = true;
// o_info.drake is supervised and linked to .o_drake DOM element
o_info.drakes = new Array();

/**
 * The BLoader object can be used to :
 * - dynamically load and unload CSS files
 * - dynamically load JS files
 * - execute javascript code in a global context, meaning on window level
 *
 * 03.04.2009 gnaegi@frentix.com 
 */
var BLoader = {
	// List of js files loaded via AJAX call.
	_ajaxLoadedJS : new Array(),
		
	// Internal mehod to check if a JS file has already been loaded on the page
	_isAlreadyLoadedJS: function(jsURL) {
		var notLoaded = true;
		// first check for scrips loaded via HTML head
		jQuery('head script[src]').each(function(s,t) {
			if (jQuery(t).attr('src').indexOf(jsURL) != -1) {
				notLoaded = false;
			}
		});
		// second check for script loaded via ajax call
		if (jQuery.inArray(jsURL, this._ajaxLoadedJS) != -1) notLoaded = false;
		return !notLoaded;
	},
		
	// Load a JS file from an absolute or relative URL by using the given encoding. The last flag indicates if 
	// the script should be loaded using an ajax call (recommended) or by adding a script tag to the document 
	// head. Note that by using the script tag the JS script will be loaded asynchronous 
	loadJS : function(jsURL, encoding, useSynchronousAjaxRequest) {
		if (!this._isAlreadyLoadedJS(jsURL)) {		
			if (o_info.debug) o_log("BLoader::loadJS: loading ajax::" + useSynchronousAjaxRequest + " url::" + jsURL);
			if (useSynchronousAjaxRequest) {
				jQuery.ajax(jsURL, {
					async: false,
					dataType: 'script',
					cache: true,
					success: function(script, textStatus, jqXHR) {
						//BLoader.executeGlobalJS(script, 'loadJS');
					}
				});
				this._ajaxLoadedJS.push(jsURL);
			} else {
				jQuery.getScript(jsURL);			
			}
			if (o_info.debug) o_log("BLoader::loadJS: loading DONE url::" + jsURL);
		} else {
			if (o_info.debug) o_log("BLoader::loadJS: already loaded url::" + jsURL);			
		}
	},

	// Execute the given string as java script code in a global context. The contextDesc is a string that can be 
	// used to describe execution context verbally, this is only used to improve meaninfull logging
	executeGlobalJS : function(jsString, contextDesc) {
		try{
			// FIXME:FG refactor as soon as global exec available in prototype
			// https://prototype.lighthouseapp.com/projects/8886/tickets/433-provide-an-eval-that-works-in-global-scope 
			if (window.execScript) window.execScript(jsString); // IE style
			else window.eval(jsString);
		} catch(e){
			if(window.console) console.log(contextDesc, 'cannot execute js', jsString);
			if (o_info.debug) { // add webbrowser console log
				o_logerr('BLoader::executeGlobalJS: Error when executing JS code in contextDesc::' + contextDesc + ' error::"'+showerror(e)+' for: '+escape(jsString));
			}
			// Parsing of JS script can fail in IE for unknown reasons (e.g. tinymce gets 8002010 error)
			// Try to do a 'full page refresh' and load everything via page header, this normally works
			if (window.location.href.indexOf('o_winrndo') != -1) window.location.reload();
			else window.location.href = window.location.href + (window.location.href.indexOf('?') != -1 ? '&' : '?' ) + 'o_winrndo=1';
		}		
	},
	
	// Load a CSS file from the given URL. The linkid represents the DOM id that is used to identify this CSS file
	loadCSS : function (cssURL, linkid, loadAfterTheme) {
		var doc = window.document;
		try {
			if(doc.createStyleSheet) { // IE
				// double check: server side should do so, but to make sure that we don't have duplicate styles
				var sheets = doc.styleSheets;
				var cnt = 0;
				var pos = 0;
				for (i = 0; i < sheets.length; i++) {
					var sh = sheets[i];
					var h = sh.href; 
					if (h == cssURL) {
						cnt++;
						if (sh.disabled) {
							// enable a previously disabled stylesheet (ie cannot remove sheets? -> we had to disable them)
							sh.disabled = false;
							return;
						} else {
							if (o_info.debug) o_logwarn("BLoader::loadCSS: style: "+cssURL+" already in document and not disabled! (duplicate add)");
							return;
						}
					}
					// add theme position, theme has to move one down
					if (sh.id == 'o_theme_css') pos = i;
				}
				if (cnt > 1 && o_info.debug) o_logwarn("BLoader::loadCSS: apply styles: num of stylesheets found was not 0 or 1:"+cnt);
				if (loadAfterTheme) {
					// add at the end
					pos = sheets.length;
				}
				// H: stylesheet not yet inserted -> insert				
				doc.createStyleSheet(cssURL, pos);
			} else { // mozilla
				// double check: first try to remove the <link rel="stylesheet"...> tag, using the id.
				var el = jQuery('#' +linkid);
				if (el && el.length > 0) {
					if (o_info.debug) o_logwarn("BLoader::loadCSS: stylesheet already found in doc when trying to add:"+cssURL+", with id "+linkid);
				} else {
					// create the new stylesheet and convince the browser to load the url using @import with protocol 'data'
					//var styles = '@import url("'+cssURL+'");';
					//var newSt = new Element('link', {rel : 'stylesheet', id : linkid, href : 'data:text/css,'+escape(styles) });
					var newSt = jQuery('<link id="' + linkid + '" rel="stylesheet" href="' + cssURL+ '">');
					if (loadAfterTheme) {
						newSt.insertBefore(jQuery('#o_fontSize_css'));
					} else {
						newSt.insertBefore(jQuery('#o_theme_css'));
					}
				}
			}
		} catch(e){
			if(window.console)  console.log(e);
			if (o_info.debug) { // add webbrowser console log
				o_logerr('BLoader::loadCSS: Error when loading CSS from URL::' + cssURL);
			}
		}				
	},

	// Unload a CSS file from the given URL. The linkid represents the DOM id that is used to identify this CSS file
	unLoadCSS : function (cssURL, linkid) {
		var doc = window.document;
		try {
			if(doc.createStyleSheet) { // IE
				var sheets = doc.styleSheets;
				var cnt = 0;
				// calculate relative style url because IE does keep only a 
				// relative URL when the stylesheet is loaded from a relative URL
				var relCssURL = cssURL;
				// calculate base url: protocol, domain and port https://your.domain:8080
				var baseURL = window.location.href.substring(0, window.location.href.indexOf("/", 8)); 
				if (cssURL.indexOf(baseURL) == 0) {
					//remove the base url form the style url
					relCssURL = cssURL.substring(baseURL.length);
				}
				for (i = 0; i < sheets.length; i++) {
					var h = sheets[i].href;
					if (h == cssURL || h == relCssURL) {
						cnt++;
						if (!sheets[i].disabled) {
							sheets[i].disabled = true; // = null;
						} else {
							if (o_info.debug) o_logwarn("stylesheet: when removing: matching url, but already disabled! url:"+h);
						}
					}
				}
				if (cnt != 1 && o_info.debug) o_logwarn("stylesheet: when removeing: num of stylesheets found was not 1:"+cnt);
				
			} else { // mozilla
				var el = jQuery('#' +linkid);
				if (el) {
					el.href = ""; // fix unload problem in safari
					el.remove();
					el = null;
					return;
				} else {
					if (o_info.debug) o_logwarn("no link with id found to remove, id:"+linkid+", url "+cssURL);
				}
			}
		} catch(e){
			if (o_info.debug) { // add webbrowser console log
				o_logerr('BLoader::unLoadCSS: Error when unloading CSS from URL::' + cssURL);
			}
		}				
	}
};

/**
 * The BFormatter object can be used to :
 * - formatt latex formulas using jsMath
 *
 * 18.06.2009 gnaegi@frentix.com 
 */
var BFormatter = {
	// process element with given dom id using jsmath
	formatLatexFormulas : function(domId) {
		try {
			if(typeof MathJax === "undefined") {
				o_mathjax();//will render the whole page
			} else if (MathJax && MathJax.isReady) {
				jQuery(function() {
					MathJax.Hub.Queue(function() {
						if(jQuery('#' + domId + ' .MathJax').length == 0) {
							MathJax.Hub.Typeset(domId)
						}
					});
				})
			} else { // not yet loaded (autoload), load first
				setTimeout(function() {
					BFormatter.formatLatexFormulas(domId);
				}, 100);
			}
		} catch(e) {
			if (window.console) console.log("error in BFormatter.formatLatexFormulas: ", e);
		}
	},
	// Align columns of different tables with the same column count
	// Note: it is best to set the width of the fixed sized colums via css 
	// (e.g. to 1% to make them as small as possible). Table must set to max-size:100% 
	// to not overflow. New width of table can be larger than before because the largest
	// width of each column is applied to all tables. With max-size the browsers magically
	// fix this overflow problem.
	alignTableColumns : function(tableArray) {
		try {
			var cellWidths = new Array();
			// find all widest cells
			jQuery(tableArray).each(function() {
				for(j = 0; j < jQuery(this)[0].rows[0].cells.length; j++){
					var cell = jQuery(this)[0].rows[0].cells[j];
					if(!cellWidths[j] || cellWidths[j] < cell.clientWidth) {
						cellWidths[j] = cell.clientWidth;
					}
				}
			});
			// set same width to columns of all tables
			jQuery(tableArray).each(function() {
				for(j = 0; j < jQuery(this)[0].rows[0].cells.length; j++){
					jQuery(this)[0].rows[0].cells[j].style.width = cellWidths[j]+'px';
				}
			});
		} catch(e) {
			if (window.console) console.log("error in BFormatter.alignTableColumns: ", e);
		}	
	}
};

function o_init() {
	try {
		// all init-on-new-page calls here
		//return opener window
		o_getMainWin().o_afterserver();
		// initialize the business path and social media
		if(window.location.href && window.location.href != null && window.location.href.indexOf('%3A') < 0) {
			var url = window.location.href;
			if(url != null && !(url.lastIndexOf("http", 0) === 0) && !(url.lastIndexOf("https", 0) === 0)) {
				url = o_info.serverUri + url;
			}
			o_info.businessPath = url;
			if(!(typeof o_shareActiveSocialUrl === "undefined")) {
				o_shareActiveSocialUrl();	
			}
		}
	} catch(e) {
		if (o_info.debug) o_log("error in o_init: "+showerror(e));
	}	
}

function o_initEmPxFactor() {
	// read px value for 1 em from hidden div
	o_info.emPxFactor = jQuery('#o_width_1em').width();
	if (o_info.emPxFactor == 0 || o_info.emPxFactor == 'undefined') {
		o_info.emPxFactor = 12; // default value for all strange settings
	}
}

function o_getMainWin() {
	try {
		if (window.OPOL) {
			// other cases the current window is the main window
			return window;
		} else if (window.opener && window.opener.OPOL) {
			// use the opener when opener window is an OpenOLAT window
			return window.opener;
		} 
	} catch (e) {
		if (o_info.debug) { // add webbrowser console log
			o_logerr('Exception while getting main window. rror::"'+showerror(e));
		}
		if (window.console) { // add ajax logger
			console.log('Exception while getting main window. rror::"'+showerror(e), "functions.js");
			console.log(e);
		}	
	}
	throw "Can not find main OpenOLAT window";
}


function o_beforeserver() {
//mal versuche mit jQuery().ready.. erst dann wieder clicks erlauben...
	o_info.linkbusy = true;
	showAjaxBusy();
	// execute iframe specific onunload code on the iframe
	if (window.suppressOlatOnUnloadOnce) {
		// don't call olatonunload this time, reset variable for next time
		window.suppressOlatOnUnloadOnce = false;
	} else if (window.olatonunload) {
		olatonunload();
	}
}

function o_afterserver() {
	o2c = 0;
	o_info.linkbusy = false;
	removeAjaxBusy();
}

function o2cl() {
	try {
		if (o_info.linkbusy) {
			return false;
		} else {
			var doreq = (o2c==0 || confirm(o_info.dirty_form));
			if (doreq) o_beforeserver();
			return doreq;
		}
	} catch(e) {
		if(window.console) console.log(e);
		return false;
	}
}

// the method doesn't set the busy flag
function o2cl_dirtyCheckOnly() {
	try {
		if (o_info.linkbusy) {
			return false;
		} else {
			return (o2c==0 || confirm(o_info.dirty_form));
		}
	} catch(e) {
		if(window.console) console.log(e);
		return false;
	}
}

//for flexi tree
function o2cl_noDirtyCheck() {
	if (o_info.linkbusy) {
		return false;
	} else {
		o_beforeserver();
		return true;
	}
}

function o3cl(formId) {
	if (o_info.linkbusy) {
		return false;
	} else {
		//detect if another flexi form on the screen is dirty too
		var isRegistered = o3c1.indexOf(formId) > -1;
		var flexiformdirty = (isRegistered && o3c1.length > 1) || o3c1.length > 0;
		//check if no other flexi form is dirty
		//otherwise ask if changes should be discarded.
		var doreq = ( !flexiformdirty || confirm(o_info.dirty_form));
		if (doreq) o_beforeserver();
		return doreq;
	}
}

// on ajax poll complete
function o_onc(response) {
	var te = response.responseText;
	BLoader.executeGlobalJS("o_info.last_o_onc="+te+";", 'o_onc');
	//asynchronous! from polling
	o_ainvoke(o_info.last_o_onc,false);
}

function o_allowNextClick() {
	o_info.linkbusy = false;
	removeAjaxBusy();
}

//remove busy after clicking a download link in non-ajax mode
//use LinkFactory.markDownloadLink(Link) to make a link call this method.
function removeBusyAfterDownload(e,target,options){
	o2c = 0;
	o_afterserver();
}

Array.prototype.search = function(s,q){
  var len = this.length;
  for(var i=0; i<len; i++){
    if(this[i].constructor == Array){
      if(this[i].search(s,q)){
        return true;
        break;
      }
     } else {
       if(q){
         if(this[i].indexOf(s) != -1){
           return true;
           break;
         }
      } else {
        if(this[i]==s){
          return true;
          break;
        }
      }
    }
  }
  return false;
}

if(!Function.prototype.curry) {
	Function.prototype.curry = function() {
	    if (arguments.length<1) {
	        return this; //nothing to curry with - return function
	    }
	    var __method = this;
	    var args = Array.prototype.slice.call(arguments);
	    return function() {
	        return __method.apply(this, args.concat(Array.prototype.slice.call(arguments)));
	    }
	}
}

if(!Array.prototype.indexOf) {
	Array.prototype.indexOf = function (searchElement /*, fromIndex */ ) {
		"use strict";
		if (this == null) {
			throw new TypeError();
        }
        var t = Object(this);
        var len = t.length >>> 0;
        if (len === 0) {
            return -1;
        }
        var n = 0;
        if (arguments.length > 1) {
            n = Number(arguments[1]);
            if (n != n) { // shortcut for verifying if it's NaN
                n = 0;
            } else if (n != 0 && n != Infinity && n != -Infinity) {
                n = (n > 0 || -1) * Math.floor(Math.abs(n));
            }
        }
        if (n >= len) {
            return -1;
        }
        var k = n >= 0 ? n : Math.max(len - Math.abs(n), 0);
        for (; k < len; k++) {
            if (k in t && t[k] === searchElement) {
                return k;
            }
        }
        return -1;
	}
}


// b_AddOnDomReplacementFinishedCallback is used to add callback methods that are executed after
// the DOM replacement has occured. Note that when not in AJAX mode, those methods will not be 
// executed. Use this callback to execute some JS code to cleanup eventhandlers or alike
//DEPRECATED: listen to event "oo.dom.replacement.after"
var b_onDomReplacementFinished_callbacks=new Array();//array holding js callback methods that should be executed after the next ajax call
function b_AddOnDomReplacementFinishedCallback(funct) {
	b_onDomReplacementFinished_callbacks.push(funct);
}

var b_changedDomEl=new Array();

//same as above, but with a filter to prevent adding a funct. more than once
//funct then has to be an array("identifier", funct) 
// DEPRECATED: listen to event "oo.dom.replacement.after"
function b_AddOnDomReplacementFinishedUniqueCallback(funct) {
	if (funct.constructor == Array){
		//check if it has been added before
		if (b_onDomReplacementFinished_callbacks.search(funct[0])){
			return;
		} 
	}
	b_AddOnDomReplacementFinishedCallback(funct);
}

// main interpreter for ajax mode
var o_debug_trid = 0;
function o_ainvoke(r) {

	// commands
	if(r == undefined) {
		return;
	}
	
	o_info.inainvoke = true;
	var cmdcnt = r["cmdcnt"];
	if (cmdcnt > 0) {
		// let everybody know dom replacement has started
		jQuery(document).trigger("oo.dom.replacement.before");

		b_changedDomEl = new Array();
		
		if (o_info.debug) { o_debug_trid++; }
		var cs = r["cmds"];
		for (var i=0; i<cmdcnt; i++) {
			var acmd = cs[i];
			var co = acmd["cmd"];
			var cda = acmd["cda"];
			var wid = acmd["w"];
			var wi = this.window; // for cross browser window: o_info.wins[wid]; 
			var out;
			if (wi) {
				switch (co) {
					case 1: // Excecute JavaScript Code
						var jsexec = cda["e"];
						BLoader.executeGlobalJS(jsexec, 'o_ainvoker::jsexec');
						if (o_info.debug) o_log("c1: execute jscode: "+jsexec);
					case 2:  // redraw components command
						var cnt = cda["cc"];
						var ca = cda["cps"];
						for (var j=0;  j<cnt; j++) {
							var c1 = ca[j];
							var ciid = c1["cid"]; // component id
							var civis = c1["cidvis"];// component visibility
							var withWrapper = c1["cw"]; // component has a wrapper element, replace only inner content
							var hfrag = c1["hfrag"]; // html fragment of component
							var jsol = c1["jsol"]; // javascript on load
							var hdr = c1["hdr"]; // header
							if (o_info.debug) o_log("c2: redraw: "+c1["cname"]+ " ("+ciid+") "+c1["hfragsize"]+" bytes, listener(s): "+c1["clisteners"]);
							//var con = jQuery(hfrag).find('script').remove(); //Strip scripts
							var hdrco = hdr+"\n\n"+hfrag;
							
							var replaceElement = false;
							var newcId = "o_c"+ciid;
							var newc = jQuery('#' + newcId);
							if (newc == null || newc.length == 0) {
								//not a container, perhaps an element
								newcId = "o_fi"+ciid;
								newc = jQuery('#' + newcId);
								replaceElement = true;
							}
							if (newc != null) {
								var eds = jQuery('div.o_richtext_mce textarea', newc);
								for(var t=0; t<eds.length; t++) {
									try {
										var edId = jQuery(eds.get(t)).attr('id');
										if(typeof top.tinymce != undefined) {
											top.tinymce.remove('#' + edId);
										}
									} catch(e) {
										if(window.console) console.log(e);
									}
								}
								
								var bTooltips = jQuery('body>div.tooltip.in');
								for(var u=0; u<bTooltips.length; u++) {
									try {
										jQuery(bTooltips.get(u)).remove();
									} catch(e) {
										if(window.console) console.log(e);
									}
								}
								
								var jTooltips = jQuery('body>div.ui-tooltip');
								for(var v=0; v<jTooltips.length; v++) {
									try {
										jQuery(jTooltips.get(v)).remove();
									} catch(e) {
										if(window.console) console.log(e);
									}
								}
								
								if(civis) { // needed only for ie 6/7 bug where an empty div requires space on screen
									newc.css('display','');//.style.display="";//reset?
								} else {
									newc.css('display','none'); //newc.style.display="none";
								}
								
								if(replaceElement || !withWrapper) {
									// replace entire DOM element 
									newc.replaceWith(hdrco);	
								} else {
									try{
										newc.empty().html(hdrco);
										//check if the operation is a success especially for IE8
										if(hdrco.length > 0 && newc.get(0).innerHTML == "") {
											newc.get(0).innerHTML = hdrco;
										}
									} catch(e) {
										if(window.console) console.log(e);
										if(window.console) console.log('Fragment',hdrco);
									}
									b_changedDomEl.push(newcId);
								}
								newc = null;

								checkDrakes();
								
								if (jsol != "") {
									BLoader.executeGlobalJS(jsol, 'o_ainvoker::jsol');
								}
							}
						}
						break;
					case 3:  // createParentRedirectTo leads to a full page reload
						wi.o2c = 0;//??
						var rurl = cda["rurl"];
						wi.o_afterserver();
						wi.document.location.replace(rurl);
						break;
					case 5: // create redirect for external resource mapper
						wi.o2c = 0;//??
						var rurl = cda["rurl"];
						//in case of a mapper served media resource (xls,pdf etc.)
						wi.o_afterserver();
						wi.document.location.replace(rurl);//opens non-inline media resource
						break;
					case 6: // createPrepareClientCommand
						wi.o2c = 0;
						wi.o_afterserver();
						break;
					case 7: // JSCSS: handle dynamic insertion of js libs and dynamic insertion/deletion of css stylesheets
						// css remove, add, js add order should makes no big difference? except js calling/modifying css? 
						var loc = wi.document.location;
						var furlp = loc.protocol+"//"+loc.hostname; // e.g. http://my.server.com:8000
						if (loc.port != "" ) furlp += ":"+ loc.port; 
						// 1. unload css file
						var cssrm = cda["cssrm"];
						for (j = 0; j<cssrm.length; j++) {
							var ce = cssrm[j];
							var id = ce["id"];
							var url = furlp + ce["url"];
							BLoader.unLoadCSS(url, id);
							if (o_info.debug) o_log("c7: rm css: id:"+id+" ,url:'"+url+"'");
						}
						// 2) load css file
						var cssadd = cda["cssadd"];
						for (k = 0; k<cssadd.length; k++) {
							var ce = cssadd[k];
							var id = ce["id"];
							var url = furlp + ce["url"];
							var pt = ce["pt"];
							BLoader.loadCSS(url,id,pt);
							if (o_info.debug) o_log("c7: add css: id:"+id+" ,url:'"+url+"'");
						}
						
						// 3) js lib adds
						var jsadd = cda["jsadd"];
						for (l=0; l<jsadd.length; l++) {
							var ce = jsadd[l];
							// 3.1) execute before AJAX-code
							var preJsAdd = ce["before"];
							if (jQuery.type(preJsAdd) === "string") {
								BLoader.executeGlobalJS(preJsAdd, 'o_ainvoker::preJsAdd');
							}
							// 3.2) load js file
							var url = ce["url"];
							var enc = ce["enc"];
							if (jQuery.type(url) === "string") BLoader.loadJS(url, enc, true);
							if (o_info.debug) o_log("c7: add js: "+url);
						}	
						break;	
					default:
						if (o_info.debug) o_log("?: unknown command "+co); 
						break;
				}		
			} else {
				if (o_info.debug) o_log("could not find window??");
			}		
		}

		// BEGIN DEPRECATED DOM REPLACEMENT CALLBACK: new style below
		// execute onDomReplacementFinished callback functions
		var stacklength = b_onDomReplacementFinished_callbacks.length;
		for (mycounter = 0; stacklength > mycounter; mycounter++) {
			
			if (mycounter > 50) {
				break; // emergency break
			}
			var func = b_onDomReplacementFinished_callbacks.shift();
			if (typeof func.length === 'number'){
				if (func[0] == "glosshighlighter") {
					var tmpArr = func[1];
					func = tmpArr;
				 }				
			}
			// don't use execScript here - must be executed outside this function scope so that dom replacement elements are available
			
			//func.delay(0.01);
			func();//TODO jquery
		}
		// END DEPRECATED DOM REPLACEMENT CALLBACK: new style on next line
		
		// let everybody know dom replacement has finished
		jQuery(document).trigger("oo.dom.replacement.after");
	}
	o_info.inainvoke = false;
	
/* minimalistic debugger / profiler	
	BDebugger.logDOMCount();
	BDebugger.logGlobalObjCount();
	BDebugger.logGlobalOLATObjects();
*/
}
/**
 * Method to remove the ajax-busy stuff and let the user click links again. This
 * should only be called from the ajax iframe onload method to make sure the UI
 * does not freeze when the server for whatever reason does not respond as expected.
 */
function clearAfterAjaxIframeCall() {
	if (o_info.linkbusy) {
		// A normal ajax call will clear the linkbusy, so something went wrong in 
		// the ajax channel, e.g. error message from apache or no response from server
		// Call afterserver to remove busy icon clear the linkbusy flag
		o_afterserver();
	}
}

function showAjaxBusy() {
	// release o_info.linkbusy only after a successful server response 
	// - otherwhise the response gets overriden by next request
	setTimeout(function(){
		if (o_info.linkbusy) {
			// try/catch because can fail in full page refresh situation when called before DOM is ready
			try {
				//don't set 2 layers
				if(jQuery('#o_ajax_busy_backdrop').length == 0) {
					jQuery('#o_body').addClass('o_ajax_busy');
					jQuery('#o_ajax_busy').modal({show: true, backdrop: 'static', keyboard: 'false'});
					// fix modal conflic with modal dialogs, make ajax busy appear always above modal dialogs
					jQuery('#o_ajax_busy').after('<div id="o_ajax_busy_backdrop" class="modal-backdrop in"></div>');
					jQuery('#o_ajax_busy>.modal-backdrop').remove();
					jQuery('#o_ajax_busy_backdrop').css({'z-index' : 1200});
				}
			} catch (e) {
				if(window.console) console.log(e);
			}
		}
	}, 700);
}

function removeAjaxBusy() {
	// try/catch because can fail in full page refresh situation when called before page DOM is ready
	try {
		jQuery('#o_body').removeClass('o_ajax_busy');
		jQuery('#o_ajax_busy_backdrop').remove();
		jQuery('#o_ajax_busy').modal('hide');
	} catch (e) {
		if(window.console) console.log(e);
	}
}

function setFormDirty(formId) {
	// sets dirty form content flag to true and renders the submit button
	// of the form with given dom id as dirty.
	// (fg) 
	o2c=1;
	// fetch the form and the forms submit button is identified via the olat 
	// form submit name
	var myForm = document.getElementById(formId);
	if (myForm != null) {
		var mySubmit = myForm.olat_fosm_0;
		if(mySubmit == null){
			mySubmit = myForm.olat_fosm;
		}
		// set dirty css class
		if(mySubmit) mySubmit.className ="btn o_button_dirty";
	}
}


//Pop-up window for context-sensitive help
function contextHelpWindow(URI) {
	helpWindow = window.open(URI, "HelpWindow", "height=760, width=940, left=0, top=0, location=no, menubar=no, resizable=yes, scrollbars=yes, toolbar=no");
	helpWindow.focus();
}

function o_openPopUp(url, windowname, width, height, menubar) {
	// generic window popup function
	attributes = "height=" + height + ", width=" + width + ", resizable=yes, scrollbars=yes, left=100, top=100, ";
	if (menubar) {
		attributes += "location=yes, menubar=yes, status=yes, toolbar=yes";
	} else {
		attributes += "location=no, menubar=no, status=no, toolbar=no";
	}

	var win;
	try {
		win = window.open(url, windowname, attributes);
	} catch(e) {
		win = window.open(url, 'OpenOLAT', attributes);
	}
	
	win.focus();
	if (o_info.linkbusy) {
		o_afterserver();
	}
}

function o_openTab(url) {
	var win = window.open(url, '_blank');
	win.focus();
	if (o_info.linkbusy) {
		o_afterserver();
	}
}

function b_handleFileUploadFormChange(fileInputElement, fakeInputElement, saveButton) {

	fileInputElement.setCustomValidity('');

	if (fileInputElement.hasAttribute('data-max-size')) {
		// check if the file selected does satisfy the max-size constraint
		var maxSize = fileInputElement.getAttribute('data-max-size');
		if (maxSize) {
			var fileSize = formInputFileSize(fileInputElement);
			if (fileSize > maxSize) {
				// show a validation error message, reset the fileInputElement and stop processing
				// to prevent unneeded uploads of potentially really big files
				var trans = jQuery(document).ooTranslator().getTranslator(o_info.locale, 'org.olat.modules.forms.ui');
				var msgLimitExceeded = trans.translate('file.upload.error.limit.exeeded');
				var msgUploadLimit = trans.translate('file.upload.limit');
				var maxSizeFormatted;
				if(maxSize < 250 * 1024) {
					maxSizeFormatted = (maxSize / 1024).toFixed(1) + " KB";
				} else if(maxSize < 250 * 1024 * 1024) {
					maxSizeFormatted = (maxSize / 1024 / 1024).toFixed(1) + " MB";
				} else {
					maxSizeFormatted = (maxSize / 1024 / 1024 / 1024).toFixed(1) + " GB";
				}
				fileInputElement.setCustomValidity(msgLimitExceeded
						+ " (" + msgUploadLimit + ": " + maxSizeFormatted + ")");
			}
		}
	}

	// file upload forms are rendered transparent and have a fake input field that is rendered.
	// on change events of the real input field this method is triggered to display the file 
	// path in the fake input field. See the code for more info on this
	var fileName = fileInputElement.value;
	// remove unix path
	var slashPos = fileName.lastIndexOf('/');
	if (slashPos != -1) {
		fileName=fileName.substring(slashPos + 1); 
	}
	// remove windows path
	slashPos = fileName.lastIndexOf('\\');	
	if (slashPos != -1) {
		fileName=fileName.substring(slashPos + 1); 
	}
	fakeInputElement.value=fileName;
	// mark save button as dirty
	if (saveButton) {
		saveButton.className='o_button_dirty'
	}
	// set focus to next element if available
	var elements = fileInputElement.form.elements;
	for (i=0; i < elements.length; i++) {
		var elem = elements[i];
		if (elem.name == fakeInputElement.name && i+1 < elements.length) {
			elements[i+1].focus();
		}
	}
}

// Return the file size of the selected file in bytes. Returns -1 when API is not working or
// no file was selected.
function formInputFileSize(fileInputElement) {
	try {
		if (!window.FileReader) {
			// file API is not supported do proceed as if the file satisfies the constraint
			return -1;
		}
		if (!fileInputElement || !fileInputElement.files) {
			// missing input element parameter or element is not a file input
			return -1;
		}
		var file = fileInputElement.files[0];
		if (!file) {
			// no file selected!
			return -1;
		}
		return file.size;
	} catch (e) {
		o_logerr('form input file size check failed: ' + e);
	}
	return -1;
}

// goto node must be in global scope to support content that has been opened in a new window 
// with the clone controller - real implementation is moved to course run scope o_activateCourseNode()
function gotonode(nodeid) {
	try {
		// check if o_activateCourseNode method is available in this window
		if (typeof o_activateCourseNode != 'undefined') {
			o_activateCourseNode(nodeid);
		} else {
			// must be content opened using the clone controller - search in opener window
			if (opener && typeof opener.o_activateCourseNode != 'undefined') {
			  opener.o_activateCourseNode(nodeid);
			}		
		}
	} catch (e) {
		alert('Goto node error:' + e);
	}
}

function o_viewportHeight() {
	// based on prototype library
	var prototypeViewPortHeight = jQuery(document).height()
	if (prototypeViewPortHeight > 0) {
		return prototypeViewPortHeight;
	} else {
		return 600; // fallback
	}
}


/**
 *  calculate the height of the inner content area that can be used for 
 *  displaying content without using scrollbars. The height includes the 
 *  margin, border and padding of the main columns
 *  @dependencies: prototype library, jQuery
 *  @author: Florian Gnaegi
 */
OPOL.getMainColumnsMaxHeight =  function(){
	var col1Height = 0,
	col2Height = 0,
	col3Height = 0,
	mainInnerHeight = 0,
	mainHeight = 0,
	mainDomElement,
	col1DomElement = jQuery('#o_main_left_content'),
	col2DomElement = jQuery('#o_main_right_content'),
	col3DomElement = jQuery('#o_main_center_content');
	
	if (col1DomElement != 'undefined' && col1DomElement != null) {
		col1Height = col1DomElement.outerHeight(true);
	}
	if (col2DomElement != 'undefined' && col2DomElement != null){
		col2Height = col2DomElement.outerHeight(true);
	}
	if (col3DomElement != 'undefined' && col3DomElement != null){
		col3Height = col3DomElement.outerHeight(true);
	}

	mainInnerHeight = (col1Height > col2Height ? col1Height : col2Height);
	mainInnerHeight = (mainInnerHeight > col3Height ? mainInnerHeight : col3Height);
	if (mainInnerHeight > 0) {
		return mainInnerHeight;
	} 
	
	// fallback, try to get height of main container
	mainDomElement = jQuery('#o_main');
	if (mainDomElement != 'undefined' && mainDomElement != null) { 
		mainHeight = mainDomElement.height();
	}
	if (mainDomElement > 0) {
		return mainDomElement;
	} 
	// fallback to viewport height	
	return o_viewportHeight();
};

OPOL.adjustHeight = function() {
	// Adjust the height of col1 and 3 based on the max column height. 
	// This is necessary to implement layouts where the two columns have different
	// backgrounds and to enlarge the menu and content area to always show the whole 
	// content. It is also required by the left menu off-canvas feature.
	try {
		var col1El = jQuery('#o_main_left_content');
		var col1 = col1El.length == 0 ? 0 : col1El.outerHeight(true);
		var col2El = jQuery('#o_main_right_content');
		var col2 = col2El.length == 0 ? 0 : col2El.outerHeight(true);
		var col3El = jQuery('#o_main_center_content');
		var col3 = col3El.length == 0 ? 0 : col3El.outerHeight(true);

		var contentHeight = Math.max(col1, col2, col3);
		// Assign new column height
		if (col1El.length > 0) {
			jQuery('#o_main_left').css({'min-height' : contentHeight + "px"});
		}
		if (col2El.length > 0) {
			jQuery('#o_main_right').css({'min-height' : contentHeight + "px"});
		}
		if (col3El.length > 0) {
			jQuery('#o_main_center').css({'min-height' : contentHeight + "px"});
		}
	} catch (e) {
		if(window.console)	console.log(e);
	}
};

/* Set the container page width to full width of the window or use standard page width */
OPOL.setContainerFullWidth = function(full) {
	if (full) {
		jQuery('body').addClass('o_width_full');				
	} else {
		jQuery('body').removeClass('o_width_full');		
	}
	// Update navbar calculations of sites and tabs
	jQuery.proxy(OPOL.navbar.onPageWidthChangeCallback,OPOL.navbar)();
}

/* Register to resize event and fire an event when the resize is finished */
jQuery(window).resize(function() {
	clearTimeout(o_info.resizeId);
	o_info.resizeId = setTimeout(function() {
		jQuery(document).trigger("oo.window.resize.after");
	}, 500);
});

// execute after each DOM replacement cycle and on initial document load
jQuery(document).on("oo.window.resize.after", OPOL.adjustHeight);
jQuery(document).on("oo.dom.replacement.after", OPOL.adjustHeight);
jQuery().ready(OPOL.adjustHeight);


function o_scrollToElement(elem) {
	try {
		o_info.scrolling = true;
		jQuery('html, body').animate({
			scrollTop : jQuery(elem).offset().top
		}, 333, function(e, el) {
			o_info.scrolling = false;
		});
	} catch (e) {
		//console.log(e);
	}
}

function o_popover(id, contentId, loc) {
	if(typeof(loc)==='undefined') loc = 'bottom';
	
	jQuery('#' + id).popover({
    	placement : loc,
    	html: true,
    	trigger: 'click',
    	container: 'body',
    	content: function() { return jQuery('#' + contentId).clone().html(); }
	}).on('shown.bs.popover', function () {
		var clickListener = function (e) {
			jQuery('#' + id).popover('hide');
			jQuery('body').unbind('click', clickListener);
		};
		setTimeout(function() {
			jQuery('body').on('click', clickListener);
		},5);
	});
}

function o_popoverWithTitle(id, contentId, title, loc) {
	if(typeof(loc)==='undefined') loc = 'bottom';
	
	var popover = jQuery('#' + id).popover({
    	placement : loc,
    	html: true,
    	title: title,
    	trigger: 'click',
    	container: 'body',
    	content: function() { return jQuery('#' + contentId).clone().html(); }
	});
	popover.on('shown.bs.popover', function () {
		var clickListener = function (e) {
			jQuery('#' + id).popover('hide');
			jQuery('body').unbind('click', clickListener);
		};
		setTimeout(function() {
			jQuery('body').on('click', clickListener);
		},5);
	});
	return popover;
}

function o_shareLinkPopup(id, text, loc) {
	if(typeof(loc)==='undefined') loc = 'top';
	var elem = jQuery('#' + id);
	elem.popover({
    	placement : loc,
    	html: true,
    	trigger: 'click',
    	container: 'body',
    	content: text
	}).on('shown.bs.popover', function () {
		var clickListener = function (e) {	
			if (jQuery(e.target).data('toggle') !== 'popover' && jQuery(e.target).parents('.popover.in').length === 0) { 
				jQuery('#' + id).popover('hide');
				jQuery('body').unbind('click', clickListener);
			}
		};
		setTimeout(function() {
			jQuery('body').on('click', clickListener);
		}, 5);
	});
	// make mouse over link text work again
	elem.attr('title',elem.attr('data-original-title'));
}

function o_QRCodePopup(id, text, loc) {	
	if(typeof(loc)==='undefined') loc = 'top';
	var elem = jQuery('#' + id);
	elem.popover({
    	placement : loc,
    	html: true,
    	trigger: 'click',
    	container: 'body',
    	content: '<div id="' + id + '_pop" class="o_qrcode"></div>'
	 }).on('shown.bs.popover', function () {
		 o_info.qr = o_QRCode(id + '_pop', (jQuery.isFunction(text) ? text() : text));
		 var clickListener = function (e) {
			 if (jQuery(e.target).data('toggle') !== 'popover' && jQuery(e.target).parents('.popover.in').length === 0) { 
				 jQuery("#" + id).popover('hide');
				 jQuery('body').unbind('click', clickListener);
			 }
		};
		setTimeout(function() {
			jQuery('body').on('click', clickListener);
		}, 5);
	 }).on('hidden.bs.popover', function () {
		 try {
			 o_info.qr.clear();
			 delete o_info.qr;			 
		 } catch(e) {}
	});
	// make mouse over link text work again
	elem.attr('title',elem.attr('data-original-title'));
}
function o_QRCode(id, text) {
	// dynamically load qr code library
	try {
		 BLoader.loadJS(o_info.o_baseURI + "/js/jquery/qrcodejs/qrcode.min.js", 'utf8', true);
		 return new QRCode(document.getElementById(id), text);
	} catch(e) {
		return null;
	}
}

function b_resizeIframeToMainMaxHeight(iframeId) {
	// adjust the given iframe to use as much height as possible
	// (fg)
	var theIframe = jQuery('#' + iframeId);
	if (theIframe != 'undefined' && theIframe != null) {
		var colsHeight = OPOL.getMainColumnsMaxHeight() - 110;
		var potentialHeight = o_viewportHeight() - 100;// remove some padding etc.
		potentialHeight = potentialHeight - theIframe.offset().top;
		// resize now
		var height = (potentialHeight > colsHeight ? potentialHeight : colsHeight);
		theIframe.height(height);
	}
}
// for gui debug mode
var o_debu_oldcn, o_debu_oldtt;

function o_debu_show(cn, tt) {
	if (o_debu_oldcn){
		o_debu_hide(o_debu_oldcn, o_debu_oldtt);
	}
	jQuery(cn).addClass('o_dev_m');
	jQuery(tt).show();

	o_debu_oldtt = tt;
	o_debu_oldcn = cn;
}

function o_debu_hide(cn, tt) {
	jQuery(tt).hide();
	jQuery(cn).removeClass('o_dev_m');
}

function o_dbg_mark(elid) {
	var el = jQuery('#' + elid);
	if (el) {
		el.css('background-color','#FCFCB8');
		el.css('border','3px solid #00F'); 
	}
}

function o_dbg_unmark(elid) {
	var el = jQuery('#' + elid);
	if (el) {
		el.css('border',''); 
		el.css('background-color','');
	}
}

function o_clearConsole() {
 o_log_all="";
 o_log(null);
}

var o_log_all = "";
function o_log(str) {
	if (str) {	
		o_log_all = "\n"+o_debug_trid+"> "+str + o_log_all;
		o_log_all = o_log_all.substr(0,4000);
	}
	var logc = jQuery("#o_debug_cons");
	if (logc) {
		if (o_log_all.length == 4000) o_log_all = o_log_all +"\n... (stripped: to long)... ";
		logc.value = o_log_all;
	}
	if(!jQuery.type(window.console) === "undefined"){
		//firebug log window
		window.console.log(str);
	}
}

function o_logerr(str) {
	o_log("ERROR:"+str);
}

function o_logwarn(str) {
	o_log("WARN:"+str);
}


function showerror(e) {
	var r = "";
    for (var p in e) r += p + ": " + e[p] + "\n";
    return "error detail:\n"+r;
}




// Each flexible.form item with an javascript 'on...' configured calls this fn.
// It is called at least if a flexible.form is submitted.
// It submits the component id as hidden parameters. This specifies which 
// form item should be dispatched by the flexible.form container. A second
// parameter submitted is the action value triggering the submit.
// A 'submit' is not the same as 'submit and validate'. if the form should validate
// is defined by the triggered component.
function o_ffEvent(formNam, dispIdField, dispId, eventIdField, eventInt){
	//set hidden fields and submit form
	var dispIdEl, defDispId,eventIdEl,defEventId;

	dispIdEl = document.getElementById(dispIdField);
	defDispId = dispIdEl.value;
	dispIdEl.value=dispId;
	eventIdEl = document.getElementById(eventIdField);
	defEventId = eventIdEl.value;
	eventIdEl.value=eventInt;
	// manually execute onsubmit method - calling submit itself does not trigger onsubmit event!
	var form = jQuery('#' + formNam);
	var formValid = true;
	jQuery('#' + formNam + ' input[type=file]')
		.filter(function(index, element) {return !element.checkValidity()})
		.each(function(index, element) {
			var valErrorElementId = element.getAttribute('id') + "_validation_error";
			var valErrorElement = document.getElementById(valErrorElementId);
			if (!valErrorElement) {
				valErrorElement = document.createElement('div');
				valErrorElement.setAttribute('class','o_error');
				valErrorElement.setAttribute('id', valErrorElementId);
				element.parentNode.parentNode.appendChild(valErrorElement);
			}
			valErrorElement.innerHTML = element.validationMessage;
			formValid = false;
		});
	if (formValid) {
		var enctype = form.attr('enctype');
		if(enctype && enctype.indexOf("multipart") == 0) {
			o_XHRSubmitMultipart(formNam);
		} else if (document.forms[formNam].onsubmit()) {
			document.forms[formNam].submit();
		}
	}
	dispIdEl.value = defDispId;
	eventIdEl.value = defEventId;
}

function o_IQEvent(formNam){
	if (document.forms[formNam].onsubmit()) {
		document.forms[formNam].submit();
	}
}

function o_TableMultiActionEvent(formNam, action){
	var mActionIdEl = jQuery('#o_mai_' + formNam);
	mActionIdEl.val(action);
	if (document.forms[formNam].onsubmit()) {
		document.forms[formNam].submit();
	}
	mActionIdEl.val('');
}

function o_XHRSubmit(formNam) {
	if(o_info.linkbusy) {
		return false;
	}

	o_beforeserver();
	var push = true;
	var form = jQuery('#' + formNam);
	var enctype = form.attr('enctype');
	if(enctype && enctype.indexOf("multipart") == 0) {
		var iframeName = "openolat-submit-" + ("" + Math.random()).substr(2);
		var iframe = o_createIFrame(iframeName);
		document.body.appendChild(iframe);
		form.attr('target', iframe.name);
		return true;
	} else {
		var data = form.serializeArray();
		if(arguments.length > 1) {
			var argLength = arguments.length;
			for(var i=1; i<argLength; i=i+2) {
				if(argLength > i+1) {
					var argData = new Object();
					argData["name"] = arguments[i];
					argData["value"] = arguments[i+1];
					data[data.length] = argData;
				}
			}
		}

		var targetUrl = form.attr("action");
		jQuery.ajax(targetUrl,{
			type:'POST',
			data: data,
			cache: false,
			dataType: 'json',
			success: function(data, textStatus, jqXHR) {
				try {
					o_ainvoke(data);
					if(push) {
						var businessPath = data['businessPath'];
						var documentTitle = data['documentTitle'];
						var historyPointId = data['historyPointId'];
						if(businessPath) {
							o_pushState(historyPointId, documentTitle, businessPath);
						}
					}
				} catch(e) {
					if(window.console) console.log(e);
				} finally {
					o_afterserver();
				}
			},
			error: o_onXHRError
		});
		return false;
	}
}

function o_XHRSubmitMultipart(formNam) {
	var form = jQuery('#' + formNam);
	var iframeName = "openolat-submit-" + ("" + Math.random()).substr(2);
	var iframe = o_createIFrame(iframeName);
	document.body.appendChild(iframe);
	form.attr('target', iframe.name);
	form.submit();
	form.attr('target','');
}

function o_createIFrame(iframeName) {
	var $iframe = jQuery('<iframe name="'+iframeName+'" id="'+iframeName+'" src="about:blank" style="position: absolute; top: -9999px; left: -9999px;"></iframe>');
	return $iframe[0];
}

function o_removeIframe(id) {
	jQuery('#' + id).remove();
}

/**
 * Opens the form-dirty dialog. Use the callback to add code that should be executed in case the user 
 * presses the "ignore button" (Code that executes the original action the user initiated)
 */
function o_showFormDirtyDialog(onIgnoreCallback) {
	// open our form-dirty dialog
	o_scrollToElement('#o_top');
	jQuery("#o_form_dirty_message").modal('show');
	jQuery("#o_form_dirty_message .o_form_dirty_ignore").on("click", function() {
		// Remove dialog and all listeners for dirty button
		jQuery("#o_form_dirty_message").modal('hide');
		jQuery("#o_form_dirty_message .o_form_dirty_ignore").off();
		// Execute the ignore callback with original user action
		onIgnoreCallback();
	});
	return false;
}

function o_ffXHREvent(formNam, dispIdField, dispId, eventIdField, eventInt, dirtyCheck, push, submit) {
	if(dirtyCheck && o2c==1) {
		// Copy function arguments and set the dirtyCheck to false for execution in callback.
		// Note that the argument list is dynamic, there are potentially more arguments than
		// listed in the function (e.g. in QTI2)
		var callbackArguments = Array.prototype.slice.call(arguments);
		callbackArguments[5] = false; 		
		var onIgnoreCallback = function() {
			// fire original event when the "ok, delete anyway" button was pressed
			o_ffXHREvent.apply(window, callbackArguments);
		}
		return o_showFormDirtyDialog(onIgnoreCallback);
	} else {
		if(!o2cl_noDirtyCheck()) return false;
	}	
	// Start event execution, start server to prevend concurrent executions of other events. 
	// o_afterserver() called when AJAX call terminates
	o_beforeserver();
	
	var data = new Object();
	if(submit) {
		var form = jQuery('#' + formNam);
		var formData = form.serializeArray();
		var formLength = formData.length;
		for(var i=0; i<formLength; i++) {
			var nameValue = formData[i];//dispatchuri and dispatchevent will be overriden
			if(nameValue.name != 'dispatchuri' && nameValue.name != 'dispatchevent') {
				data[nameValue.name] = nameValue.value;
			}
		}
	}
	
	data['dispatchuri'] = dispId;
	data['dispatchevent'] = eventInt;
	if(arguments.length > 8) {
		var argLength = arguments.length;
		for(var i=8; i<argLength; i=i+2) {
			if(argLength > i+1) {
				data[arguments[i]] = arguments[i+1];
			}
		}
	}
	
	var targetUrl = jQuery('#' + formNam).attr("action");
	jQuery.ajax(targetUrl,{
		type:'POST',
		data: data,
		cache: false,
		dataType: 'json',
		success: function(data, textStatus, jqXHR) {
			try {
				o_ainvoke(data);
				if(push) {
					var businessPath = data['businessPath'];
					var documentTitle = data['documentTitle'];
					var historyPointId = data['historyPointId'];
					if(businessPath) {
						o_pushState(historyPointId, documentTitle, businessPath);
					}
				}
			} catch(e) {
				if(window.console) console.log(e);
			} finally {
				o_afterserver();
			}
		},
		error: o_onXHRError
	})
}

function o_ffXHRNFEvent(formNam, dispIdField, dispId, eventIdField, eventInt) {
	var data = new Object();
	data['dispatchuri'] = dispId;
	data['dispatchevent'] = eventInt;
	if(arguments.length > 5) {
		var argLength = arguments.length;
		for(var i=5; i<argLength; i=i+2) {
			if(argLength > i+1) {
				data[arguments[i]] = arguments[i+1];
			}
		}
	}
	
	var targetUrl = jQuery('#' + formNam).attr("action");
	jQuery.ajax(targetUrl,{
		type:'POST',
		data: data,
		cache: false,
		dataType: 'json',
		success: function(data, textStatus, jqXHR) {
			//no response
		}
	})
}

function o_XHRWikiEvent(link) {
	var href = jQuery(link).attr('href');
	if(href.indexOf(o_info.serverUri) == 0) {
		href = href.substring(o_info.serverUri.length, href.length);
	}
	o_XHREvent(href, false, true);
	return false;
}

function o_XHREvent(targetUrl, dirtyCheck, push) {
	if(dirtyCheck && o2c==1) {
		// Copy function arguments and set the dirtyCheck to false for execution in callback.
		// Note that the argument list is dynamic, there are potentially more arguments than
		// listed in the function
		var callbackArguments = Array.prototype.slice.call(arguments);
		callbackArguments[1] = false; 		
		var onIgnoreCallback = function() {
			// fire original event when the "ok, delete anyway" button was pressed
			o_XHREvent.apply(window, callbackArguments);
		}
		return o_showFormDirtyDialog(onIgnoreCallback);		
	} else {
		if(!o2cl_noDirtyCheck()) return false;
	}
	// Start event execution, start server to prevend concurrent executions of other events. 
	// o_afterserver() called when AJAX call terminates
	o_beforeserver();
	
	var data = new Object();
	if(arguments.length > 3) {
		var argLength = arguments.length;
		for(var i=3; i<argLength; i=i+2) {
			if(argLength > i+1) {
				data[arguments[i]] = arguments[i+1];
			}
		}
	}
	
	jQuery.ajax(targetUrl,{
		type:'POST',
		data: data,
		cache: false,
		dataType: 'json',
		success: function(data, textStatus, jqXHR) {
			try {
				
				if(push) {
					try {
						var businessPath = data['businessPath'];
						var documentTitle = data['documentTitle'];
						var historyPointId = data['historyPointId'];
						if(businessPath) {
							// catch separately - nothing must fail here!
							o_pushState(historyPointId, documentTitle, businessPath);
						}
					} catch(e) {
						if(window.console) console.log(e);
					}
				}
				o_ainvoke(data);
			} catch(e) {
				if(window.console) console.log(e);
			} finally {
				o_afterserver();
			}
		},
		error: o_onXHRError
	})
	
	return false;
}

//by pass every check and don't wait a response from the response
//typically used to send GUI settings back to the server
function o_XHRNFEvent(targetUrl) {
	var data = new Object();
	if(arguments.length > 1) {
		var argLength = arguments.length;
		for(var i=1; i<argLength; i=i+2) {
			if(argLength > i+1) {
				data[arguments[i]] = arguments[i+1];
			}
		}
	}
	
	jQuery.ajax(targetUrl,{
		type:'POST',
		data: data,
		cache: false,
		dataType: 'json',
		success: function(data, textStatus, jqXHR) {
			//ok
		},
		error: o_onXHRError
	})
}

function o_onXHRError(jqXHR, textStatus, errorThrown) {
	o_afterserver();
	if(401 == jqXHR.status) {
		var msg = o_info.oo_noresponse.replace("reload.html", window.document.location.href);
		showMessageBox('error', o_info.oo_noresponse_title, msg, undefined);
	} else if(window.console) {
		console.log('Error status 2', textStatus, errorThrown, jqXHR.responseText);
		console.log(jqXHR);
	}
}

function o_pushState(historyPointId, title, url) {
	try {
		var data = new Object();
		data['businessPath'] = url;
		data['historyPointId'] = historyPointId;
		
		if(url != null && !(url.lastIndexOf("http", 0) === 0) && !(url.lastIndexOf("https", 0) === 0)) {
			url = o_info.serverUri + url;
		}
		o_info.businessPath = url;
		if(!(typeof o_shareActiveSocialUrl === "undefined")) {
			o_shareActiveSocialUrl();	
		}
		if(window.history && !(typeof window.history === "undefined") && window.history.pushState) {
			window.history.pushState(data, title, url);
		} else {
			window.location.hash = historyPointId;
		}
	} catch(e) {
		if(window.console) console.log(e, url);
	}
}

function o_toggleMark(el) {
	var current = jQuery('i', el).attr('class');
	if(current.indexOf('o_icon_bookmark_add') >= 0) {
		jQuery('i', el).removeClass('o_icon_bookmark_add').addClass('o_icon_bookmark');
	} else {
		jQuery('i', el).removeClass('o_icon_bookmark').addClass('o_icon_bookmark_add');
	}
}

/**
 * Register a dragula object, the object will be associated
 * with a DOM element with the .o_drake class. If the class
 * is absent of the DOM, all drakes will be desstroyed.
 * 
 * @param drake
 * @returns drake
 */
function registerDrake(drake) {
	o_info.drakes.push(drake);
	return drake;
}

function destroyDrakes() {
	if(o_info.drakes !== "undefined" && o_info.drakes != null && o_info.drakes.length > 0) {
		for(var i=o_info.drakes.length; i-->0; ) {
			try {
				o_info.drakes[i].destroy();
			} catch(e) {
				if(window.console) console.log(e);
			}
			o_info.drakes.pop();
		}
	}
}

function checkDrakes() {
	if(o_info.drakes !== "undefined" && o_info.drakes != null && o_info.drakes.length > 0) {
		if(jQuery(".o_drake").length == 0) {
			destroyDrakes();
		}
	}
}

//try to mimic the FileUtils.normalizeFilename method
function o_normalizeFilename(filename) {
	filename = filename.replace(/\s/g, "_")
	var replaceByUnderscore = [ "/", ",", ":", "(", ")" ];
	for(var i=replaceByUnderscore.length; i-->0; ) {
		filename = filename.split(replaceByUnderscore[i]).join("_");
	}

	var beautifyGermanUnicode = [ "\u00C4", "\u00D6", "\u00DC", "\u00E4", "\u00F6", "\u00E6", "\u00FC", "\u00DF", "\u00F8", "\u2205" ],
		beautifyGermanReplacement = [ "Ae", "Oe",     "Ue",     "ae",     "oe",     "ae",     "ue",     "ss",     "o",      "o" ];
	for(var i=beautifyGermanUnicode.length; i-->0; ) {
		filename = filename.split(beautifyGermanUnicode[i]).join(beautifyGermanReplacement[i]);
	}

	try {//if something is not supported by the browser
		filename = filename.normalize('NFKD');
		filename = filename.replace("/\p{InCombiningDiacriticalMarks}+/g","");
		filename = filename.replace("/\W+/g", "");
	} catch(e) {
		if(window.console) console.log(e);
	}
	return filename;
}

//
// param formId a String with flexi form id
function setFlexiFormDirtyByListener(e){
	setFlexiFormDirty(e.data.formId, e.data.hideMessage);
}

function setFlexiFormDirty(formId, hideMessage){
	var isRegistered = o3c.indexOf(formId) > -1;
	if(!isRegistered){
		o3c.push(formId);
	}
	jQuery('#'+formId).each(function() {
		var submitId = jQuery(this).data('FlexiSubmit');
		if(submitId != null) {
			jQuery('#'+submitId).addClass('btn o_button_dirty');
			o2c = (hideMessage ? 0 : 1);
		}
	});
}

//
//
function o_ffRegisterSubmit(formId, submElmId){
	jQuery('#'+formId).data('FlexiSubmit', submElmId);
}

function dismissInfoBox(uuid) {
	javascript:jQuery('#' + uuid).remove();
	return true;
}
/*
* renders an info msg that slides from top into the window
* and hides automatically
*/
function showInfoBox(title, content){
	// Factory method to create message box
	var uuid = Math.floor(Math.random() * 0x10000 /* 65536 */).toString(16);
	var info = '<div id="' + uuid
	     + '" class="o_alert_info"><div class="alert alert-info clearfix o_sel_info_message"><a class="o_alert_close o_sel_info_close" href="javascript:;" onclick="dismissInfoBox(\'' + uuid + '\')"><i class="o_icon o_icon_close"> </i></a><h3><i class="o_icon o_icon_info"> </i> '
		 + title + '</h3><p>' + content + '</p></div></div>';
    var msgCt = jQuery('#o_messages').prepend(info);
    // Hide message automatically based on content length
    var time = (content.length > 150) ? 8000 : ((content.length > 70) ? 6000 : 4000);

    // Callback to remove after reading
    var cleanup = function() {
    	jQuery('#' + uuid)
    		.transition({top : '-100%'}, 333, function() {
    			jQuery('#' + uuid).remove();
    		});    	
    };
    // Show info box now
    o_info.scrolling = true;
    jQuery('#' + uuid).show().transition({ top: 0 }, 333);
    // Visually remove message box immediately when user clicks on it
    jQuery('#' + uuid).click(function(e) {
    	cleanup();
    });
	o_scrollToElement('#o_top');
	
    // Help GC, prevent cyclic reference from on-click closure
    title = null;
    content = null;
    msgCt = null;
    
    setTimeout(function(){
		try {
			cleanup();
		} catch(e) {
			//possible if the user has closed the window
		}
	}, time);
}
/*
* renders an message box which the user has to click away
* The last parameter buttonCallback is optional. if a callback js 
* function is given it will be execute when the user clicks ok or closes the message box
*/
function showMessageBox(type, title, message, buttonCallback) {
	if(type == 'info'){
		showInfoBox(title, message);
		return null;
	} else {
		var content = '<div id="myFunctionalModal" class="modal fade" role="dialog"><div class="modal-dialog"><div class="modal-content">';
		content += '<div class="modal-header"><button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>';
        content += '<h4 class="modal-title">' + title + '</h4></div>';	
		content += '<div class="modal-body alert ';
		if("warn" == type) {
			content += 'alert-warning';
		} else if("error" == type) {
			content += 'alert-danger';
		} else {
			content += 'alert-info';
		}
		content += '"><p>' + message + '</p></div></div></div></div>';
		jQuery('#myFunctionalModal').remove();
		jQuery('body').append(content);
		               
		var msg = jQuery('#myFunctionalModal').modal('show').on('hidden.bs.modal', function (e) {
			jQuery('#myFunctionalModal').remove();
		});
		o_scrollToElement('#o_top');
		return msg;
	}
}

/*
 * For standard tables
 */
function o_table_toggleCheck(ref, checked) {
	var tb_checkboxes = document.forms[ref].elements["tb_ms"];
	len = tb_checkboxes.length;
	if (typeof(len) == 'undefined') {
		tb_checkboxes.checked = checked;
	}
	else {
		var i;
		for (i=0; i < len; i++) {
			tb_checkboxes[i].checked=checked;
		}
	}
}

/*
 * For menu tree
 */
function onTreeStartDrag(event, ui) {
	jQuery(event.target).addClass('o_dnd_proxy');
}

function onTreeStopDrag(event, ui) {
	jQuery(event.target).removeClass('o_dnd_proxy');
}

function onTreeDrop(event, ui) {
	var dragEl = jQuery(ui.draggable[0]);
	var el = jQuery(this);
	el.css({position:'', width:''});
	var url =  el.droppable('option','endUrl');
	if(url.lastIndexOf('/') == (url.length - 1)) {
		url = url.substring(0,url.length-1);
	}
	var dragId = dragEl.attr('id')
	var targetId = dragId.substring(2, dragId.length);
	url += '%3Atnidle%3A' + targetId;

	var droppableId = el.attr('id');
	if(droppableId.indexOf('ds') == 0) {
		url += '%3Asne%3Ayes';
	} else if(droppableId.indexOf('dt') == 0) {
		url += '%3Asne%3Aend';
	}
	jQuery('.ui-droppable').each(function(index, el) {
		jQuery(el).droppable( "disable" );
	});
	o_XHREvent(url + '/', false, false);
}

function treeAcceptDrop(el) {
	return true;
}

function treeAcceptDrop_notWithChildren(el) {
	var accept = false;
	
	var dragEl = jQuery(el);
	var dragElId = dragEl.attr('id');
	if(dragElId != undefined && (dragElId.indexOf('dd') == 0 ||
		dragElId.indexOf('ds') == 0 || dragElId.indexOf('dt') == 0 ||
		dragElId.indexOf('da') == 0 || dragElId.indexOf('row') == 0)) {

		var dropEl = jQuery(this)
		var dropElId = dropEl.attr('id');//dropped
		var dragNodeId = dragElId.substring(2, dragElId.length);
		var dropId = dropElId.substring(2, dropElId.length);
		if(dragNodeId != dropId) {
			var containerEl = jQuery('#dd' + dragNodeId).parents('li');
			if(containerEl.length > 0 && jQuery(containerEl.get(0)).find('#dd' + dropId).length == 0) {
				accept = true;
			}
		}
	}
	
	return accept;
}

function treeAcceptDrop_portfolio(el) {
	var accept = false;
	
	var dragEl = jQuery(el);
	var dragElId = dragEl.attr('id');
	if(treeNode_isDragNode(dragElId)) {
		var dropEl = jQuery(this);
		var dropElId = dropEl.attr('id');//dropped
		var dragNodeId = dragElId.substring(2, dragElId.length);
		var dropId = dropElId.substring(2, dropElId.length);
		var sibling = dragElId.indexOf('ds') == 0 || dragElId.indexOf('dt') == 0;
		if(dragNodeId != dropId) {
			var dragType = treeNode_portfolioType(dragEl);
			var dropType = treeNode_portfolioType(dropEl);
			if(dragType == "artefact") {
				if(dropType == "page" || dropType == "struct" || dropType == "artefact") {
					accept = true;
				}
			} else if(dragType == "struct") {
				if(dropType == "page" || dropType == "struct") {
					accept = true;
				}
			} else if(dragType == "page") {
				if(dropType == "map" || dropType == "page") {
					accept = true;
				}
			}
		}
	}

	return accept;
}

function treeNode_portfolioType(el) {
	var nodeEl = jQuery(el.get(0));
	var type = treeNode_portfolioTypes(nodeEl);
	if(type == null) {
		var parentLink = nodeEl.parent('a');
		if(parentLink.length > 0) {
			type = treeNode_portfolioTypes(jQuery(parentLink.get(0)));
		} else if(nodeEl.attr('id').indexOf('ds') == 0) {
			var prevEl = nodeEl.prev('div');
			if(prevEl.length > 0) {
				type = treeNode_portfolioTypes(prevEl);
			}
		} else if(nodeEl.attr('id').indexOf('dt') == 0) {
			var prevEl = nodeEl.next('div');
			if(prevEl.length > 0) {
				type = treeNode_portfolioTypes(prevEl);
			}
		}
	}
	return type;
}

function treeNode_portfolioTypes(nodeEl) {
	if(nodeEl.find === undefined) {
		return null;
	} else if(nodeEl.find(".o_ep_icon_struct").length > 0 || nodeEl.hasClass('o_ep_icon_struct')) {
		return "struct";
	} else if(nodeEl.find(".o_ep_icon_page").length > 0 || nodeEl.hasClass('o_ep_icon_page')) {
		return "page";
	} else if(nodeEl.find(".o_ep_icon_map").length > 0 || nodeEl.hasClass('o_ep_icon_map')) {
		return "map";
	} else if(nodeEl.find(".o_ep_artefact").length > 0 || nodeEl.hasClass('o_ep_artefact')) {
		return "artefact";
	}
	return null;
}

function treeNode_isDragNode(elId) {
	if(elId != undefined && (elId.indexOf('dd') == 0 ||
			elId.indexOf('ds') == 0 || elId.indexOf('dt') == 0 ||
			elId.indexOf('da') == 0 || elId.indexOf('row') == 0)) {
		return true;
	}
	return false;
}

/*
 * For checkbox
 */
function o_choice_toggleCheck(ref, checked) {
	var checkboxes = document.forms[ref].elements;
	len = checkboxes.length;
	if (typeof(len) == 'undefined') {
		checkboxes.checked = checked;
	}
	else {
		var i;
		for (i=0; i < len; i++) {
			if (checkboxes[i].type == 'checkbox' && checkboxes[i].getAttribute('class') == 'o_checkbox') {
				checkboxes[i].checked=checked;
			}
		}
	}
}

/*
 * For briefcase
 */
function b_briefcase_isChecked(ref, warning_text) {
	var i;
	var myElement = document.getElementById(ref);
	var numselected = 0;
	for (i=0; myElement.elements[i]; i++) {
		if (myElement.elements[i].type == 'checkbox' && myElement.elements[i].name == 'paths' && myElement.elements[i].checked) {
			numselected++;
		}
	}
	
	if (numselected < 1) {
		alert(warning_text);
		return false;
	}
	return true;
}
function b_briefcase_toggleCheck(ref, checked) {
	var myElement = document.getElementById(ref);
	len = myElement.elements.length;
	var i;
	for (i=0; i < len; i++) {
		if (myElement.elements[i].name=='paths') {
			myElement.elements[i].checked=checked;
		}
	}
}


/*
 * print command, prints iframes when available
 */
function o_doPrint() {
	// When we have an iframe, issue print command on iframe directly
	var iframes =  jQuery('div.o_iframedisplay iframe');
	if (iframes.length > 0) {
		try {
			var iframe = iframes[0];
			frames[iframe.name].focus();
			frames[iframe.name].print();
			return;
		} catch (e) {
			// When iframe content renames the window, the method above does not work.
			// We use best guess code to find the target iframe in the window frames list
			for (i=0; frames.length > i; i++) {
				iframe = frames[i];
				if (iframe.name == 'oaa0') continue; // skip ajax iframe
				var domFrame = document.getElementsByName(iframe.name)[0];
				if (domFrame && domFrame.getAttribute('class') == 'ext-shim') continue; // skip ext shim iframe
				// Buest guess is that this is our renamed target iframe			
				if (iframe.name != '') {
					try {
						frames[iframe.name].focus();
						frames[iframe.name].print();				
					} catch (e) {
						// fallback to window print
						window.print()
					}
					return;
				}
			}		
			// fallback to window print
			window.print()
		}
	} else {
		// no iframes found, print window
		window.print()
	}
}


/*
 * Attach event listeners to enable inline translation tool hover links
 */ 
function b_attach_i18n_inline_editing() {
	// Add hover handler to display inline edit links
	jQuery('span.o_translation_i18nitem').hover(function() {
		jQuery(this.firstChild).show();
	},function(){
		jQuery('a.o_translation_i18nitem_launcher').hide();
	});
	// Add highlight effect on link to show which element is affected by this link
	jQuery('a.o_translation_i18nitem_launcher').hover(function() {	
		var parent = jQuery(this).parent('span.o_translation_i18nitem')
		parent.effect("highlight");
	});
	// Add to on ajax ready callback for next execution
	b_AddOnDomReplacementFinishedCallback(b_attach_i18n_inline_editing);
}

function b_hideExtMessageBox() {
	//for compatibility
}
 
 
/**
 * Minimalistic debugger to find ever growing list of DOM elements, 
 * global variables or OLAT managed variables. To use it, uncomment
 * lines in o_ainvoke()
 */
var BDebugger = {
	_lastDOMCount : 0,
	_lastObjCount : 0,
	_knownGlobalOLATObjects : ["o_afterserver","o_onc","o_getMainWin","o_ainvoke","o_info","o_beforeserver","o_ffEvent","o_openPopUp","o_debu_show","o_logwarn","o_dbg_unmark","o_ffRegisterSubmit","o_clearConsole","o_init","o_log","o_allowNextClick","o_dbg_mark","o_debu_hide","o_logerr","o_debu_oldcn","o_debu_oldtt","o_debug_trid","o_log_all"],
		
	_countDOMElements : function() {
		return document.getElementsByTagName('*').length;
	},
	_countGlobalObjects : function() {
			var objCount=0; 
			for (prop in window) {
				objCount++;
			} 
			return objCount;
	},
	
	logDOMCount : function() {
		var self = BDebugger;
		var DOMCount=self._countDOMElements();
		var diff = DOMCount - self._lastDOMCount;
		console.log( (diff > 0 ? "+" : "") + diff + " \t" + DOMCount + " \tDOM element count after DOM replacement");
		self._lastDOMCount = DOMCount;
		DOMCount = null;
	},

	logGlobalObjCount : function() {	
		var self = BDebugger;
		var objCount = self._countGlobalObjects();
		var diff = objCount - self._lastObjCount;
		console.log( (diff > 0 ? "+" : "") + diff + " \t" + objCount + " \tGlobal object count after DOM replacement");
		self._lastObjCount = objCount;
		objCount = null;
	},
	
	logGlobalOLATObjects : function() {
		var self = BDebugger;
		var OLATObjects = new Array();
		for (prop in window) {
			if (prop.indexOf("o_") == 0 && self._knownGlobalOLATObjects.indexOf(prop) == -1) {
				OLATObjects.push(prop);
			}
		} 	
		if (OLATObjects.length > 0) {
			console.log(OLATObjects.length + " global OLAT objects found:");
			OLATObjects.each(function(o){
				console.log("\t" + typeof window[o] + " \t" + o);
			});
		}
	}
}

var OOEdusharing = {
		
	start: function() {
		if (o_info.edusharing_enabled) {
			OOEdusharing.render();
			jQuery(document).on("oo.dom.replacement.after", OOEdusharing.render);
			OOEdusharing.enableMetadataToggler();
		}
	},
		
	replaceWithSpinner: function(node, width, height) {
		var spinnerHtml = "<div style='";
		if (width > 0) {
			spinnerHtml += "width:" + width + "px;";
		}
		if (height > 0) {
			spinnerHtml += "height:" + height + "px;";
		}
		spinnerHtml += "'>";
		spinnerHtml += "<div class='edusharing_spinner_inner'><div class='edusharing_spinner1'></div></div>";
		spinnerHtml += "<div class='edusharing_spinner_inner'><div class='edusharing_spinner2'></div></div>";
		spinnerHtml += "<div class='edusharing_spinner_inner'><div class='edusharing_spinner3'></div></div>";
		spinnerHtml += "</div>";
		
		var spinner = jQuery(spinnerHtml);
		node.before(spinner);
		node.remove();
		return spinner;
	},
	
	replaceGoTo: function(html, identifier) {
		var url = o_info.uriprefix.replace("auth", "edusharing") + "goto?identifier=" + identifier;
		html = html.replace("{{{LMS_INLINE_HELPER_SCRIPT}}}", url)
		return html;
	},
	
	replaceWithRendered: function(node, identifier, width, height, esClass, showLicense, showInfos, isIFrame) {
		var url = o_info.uriprefix.replace("auth", "edusharing") + "render?identifier=" + identifier;
		if (width > 0) {
			url = url + "&width=" + width;
		}
		if (height) {
			url = url + "&height=" + height;
		}
		
		var containerHtml = "<div class='o_edusharing_container";
		if (typeof esClass != 'undefined') {
			containerHtml += " " + esClass;
		}
		if (isIFrame) {
			containerHtml += " o_in_iframe";
		}
		if ('hide' === showLicense) {
			containerHtml += " o_hide_license";
		}
		if ('hide' === showInfos) {
			containerHtml += " o_hide_infos";
		}
		containerHtml += "'>";
		containerHtml += "</div>";
		
		var container = jQuery(containerHtml);
		
		jQuery.ajax({
			type: "GET",
			url: url,
			dataType : 'html',
			success : function(data){
				var goToData = OOEdusharing.replaceGoTo(data, identifier);
				var esNode = container.append(goToData);
				node.replaceWith(esNode);
			},
			error : function(XMLHttpRequest, textStatus, errorThrown) {
				node.replaceWith("<div class='o_warning'>edu-sharing not available</div>");
			}
		})
	},
		
	replace: function(node, isIFrame) {
		var identifier = node.data("es_identifier");
		var width = node.attr("width");
		var height = node.attr("height");
		var esClass = node.attr('class');
		var showLicense = node.data("es_show_license");
		var showInfos = node.data("es_show_infos");
		
		var spinner = OOEdusharing.replaceWithSpinner(node, width, height);
		OOEdusharing.replaceWithRendered(spinner, identifier, width, height, esClass, showLicense, showInfos, isIFrame);
	},
	
	/**
	 * Replace the edu-sharing nodes with the real resources from the edu-sharing rendering service.
	 */
	render: function() {
		var esNodes = jQuery("[data-es_identifier]");
		if (esNodes.length > 0) {
			esNodes.each(function() {
				var node = jQuery( this );
				OOEdusharing.replace(node, false);
			});
		}
		// Handle inside internal iFrames as well
		var iFrames = jQuery(".o_iframe_rel");
		if (iFrames.length > 0) {
			iFrames.each(function() {
				var iFrame = jQuery( this );
				iFrame.on('load', function(){
					iFrame.contents().on('click', OOEdusharing.toggleMetadata);
					var iFrameEsNodes = iFrame.contents().find("[data-es_identifier]");
					if (iFrameEsNodes.length > 0) {
						iFrameEsNodes.each(function() {
							var iFrameEsNode = jQuery( this );
							OOEdusharing.replace(iFrameEsNode, true);
						});
					}
				});
			});
		}
	},
	
	/**
	 * Toggle edu-sharing metadata.
	 * see https://github.com/edu-sharing/plugin-moodle/blob/master/filter/edusharing/amd/src/edu.js
	 */
	toggleMetadata: function (e) {
		if (jQuery(e.target).closest(".edusharing_metadata").length) {
			//clicked inside ".edusharing_metadata" - do nothing
		} else if (jQuery(e.target).closest(".edusharing_metadata_toggle_button").length) {
			jQuery(".edusharing_metadata").hide();
			toggle_button = jQuery(e.target);
			metadata = toggle_button.parent().find(".edusharing_metadata");
			if (metadata.hasClass('open')) {
				metadata.toggleClass('open');
				metadata.hide();
			} else {
				jQuery(".edusharing_metadata").removeClass('open');
				metadata.toggleClass('open');
				metadata.show();
			}
		} else {
			jQuery(".edusharing_metadata").hide();
			jQuery(".edusharing_metadata").removeClass('open');
		}
	},
	enableMetadataToggler: function() {
		jQuery(document).click(OOEdusharing.toggleMetadata);
	}
}

jQuery( document ).ready(function() {
	OOEdusharing.start();
});

