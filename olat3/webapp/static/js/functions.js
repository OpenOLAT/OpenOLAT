/**
 * This file contains helper methods for the olatcore web app framework and the
 * learning management system OLAT
 */
//used to mark form dirty and warn user to save first.
var o2c=0;
var o3c=new Array();//array holds flexi.form id's
// o_info is a global object that contains global variables
o_info.guibusy = false;
o_info.linkbusy = false;
//debug flag for this file, to enable debugging to the olat.log set JavaScriptTracingController to level debug
o_info.debug = false;

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
		$$('head script[src]').findAll(function(s) {
			if (s.src.indexOf(jsURL) != -1) {
				notLoaded = false;
				$break;
			};
		});
		// second check for script loaded via ajax call
		if (this._ajaxLoadedJS.indexOf(jsURL) != -1) notLoaded = false;
		return !notLoaded;
	},
		
	// Load a JS file from an absolute or relative URL by using the given encoding. The last flag indicates if 
	// the script should be loaded using an ajax call (recommended) or by adding a script tag to the document 
	// head. Note that by using the script tag the JS script will be loaded asynchronous 
	loadJS : function(jsURL, encoding, useSynchronousAjaxRequest) {
		if (!this._isAlreadyLoadedJS(jsURL)) {		
			if (o_info.debug) o_log("BLoader::loadJS: loading ajax::" + useSynchronousAjaxRequest + " url::" + jsURL);
			if (useSynchronousAjaxRequest) {
				new Ajax.Request(jsURL, {
					// manually execute because prototype does not execute in global space. Refactor as soon as available
					onSuccess : function(transport) {BLoader.executeGlobalJS(transport.responseText, 'loadJS');},
					method: 'get',
					encoding: encoding,
					evalJS : false,
					asynchronous: false // wait for script to be fully loaded, otherwhise followup code might break
				});
				this._ajaxLoadedJS.push(jsURL);
			} else {
				// old school, executes asynchronous!!
				var s= new Element("script", {type : "text/javascript", src : jsURL, charset : encoding});
				$$('head')[0].appendChild( s);				
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
			if (o_info.debug) { // add webbrowser console log
				o_logerr('BLoader::executeGlobalJS: Error when executing JS code in contextDesc::' + contextDesc + ' error::"'+showerror(e)+' for: '+escape(jsString));
			}
			if (B_AjaxLogger.isDebugEnabled()) { // add ajax logger
				B_AjaxLogger.logDebug('BLoader::executeGlobalJS: Error when executing JS code in contextDesc::' + contextDesc + ' error::"'+showerror(e)+' for: '+escape(jsString), "functions.js::BLoader::executeGlobalJS::" + contextDesc);
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
					if (sh.id == 'b_theme_css') pos = i;
				}
				if (cnt > 1 && o_info.debug) o_logwarn("BLoader::loadCSS: apply styles: num of stylesheets found was not 0 or 1:"+cnt);
				if (loadAfterTheme) {
					// add at the end
					pos = sheets.length;
				}
				// H: stylesheet not yet inserted -> insert				
				var mystyle = doc.createStyleSheet(cssURL, pos);
			} else { // mozilla
				// double check: first try to remove the <link rel="stylesheet"...> tag, using the id.
				var el = $(linkid);
				if (el) {
					if (o_info.debug) o_logwarn("BLoader::loadCSS: stylesheet already found in doc when trying to add:"+cssURL+", with id "+linkid);
					return;
				}
				// create the new stylesheet and convince the browser to load the url using @import with protocol 'data'
				var styles = '@import url("'+cssURL+'");';
				var newSt = new Element('link', {rel : 'stylesheet', id : linkid, href : 'data:text/css,'+escape(styles) });
				if (loadAfterTheme) {
					var tar = $('b_fontSize_css');
					$$('head')[0].insertBefore(newSt, tar);
				} else {
					var tar = $('b_theme_css');
					$$('head')[0].insertBefore(newSt, tar);
				}
			}
		} catch(e){
			if (o_info.debug) { // add webbrowser console log
				o_logerr('BLoader::loadCSS: Error when loading CSS from URL::' + cssURL);
			}
			if (B_AjaxLogger.isDebugEnabled()) { // add ajax logger
				B_AjaxLogger.logDebug('BLoader::loadCSS: Error when loading CSS from URL::' + cssURL, "functions.js::BLoader::loadCSS");
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
						//alert("removing style for ie");
							sheets[i].disabled = true; // = null;
						} else {
							if (o_info.debug) o_logwarn("stylesheet: when removing: matching url, but already disabled! url:"+h);
						}
						// return;
					}
				}
				if (cnt != 1 && o_info.debug) o_logwarn("stylesheet: when removeing: num of stylesheets found was not 1:"+cnt);
				
			} else { // mozilla
				var el = $(linkid);
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
			if (B_AjaxLogger.isDebugEnabled()) { // add ajax logger
				B_AjaxLogger.logDebug('BLoader::unLoadCSS: Error when unloading CSS from URL::' + cssURL, "functions.js::BLoader::loadCSS");
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
			if (jsMath) { // only when js math available
				if (jsMath.loaded) { 
					jsMath.ProcessBeforeShowing.curry(domId).delay(0.01);					
				} else { // not yet loaded (autoload), load first
					jsMath.Autoload.LoadJsMath();
					// retry formatting when ready (recursively until loaded)
					BFormatter.formatLatexFormulas.delay(0.1);
				}
			} else if (B_AjaxLogger.isDebugEnabled()) { // add ajax logger
				B_AjaxLogger.logDebug('BFormatter::formatLatexFormulas: can not format latex formulas, jsMath not installed. Check your logfile', "functions.js::BFormatter::formatLatexFormulas");
			}
		} catch(e) {
			if (o_info.debug) o_log("error in BFormatter.formatLatexFormulas: "+showerror(e));
		}
	}
};


function o_init() {
	try {
		// all init-on-new-page calls here
		//return opener window
		o_getOpenWin().o_afterserver();	
	} catch(e) {
		if (o_info.debug) o_log("error in o_init: "+showerror(e));
	}	
}

function b_initEmPxFactor() {
	// read px value for 1 em from hidden div
	o_info.emPxFactor = Ext.get('b_width_1em').getWidth();
	if (o_info.emPxFactor == 0 || o_info.emPxFactor == 'undefined') {
		o_info.emPxFactor = 12; // default value for all strange settings
		if (B_AjaxLogger.isDebugEnabled()) B_AjaxLogger.logDebug('Could not read with of element b_width_1em, set o_info.emPxFactor to 12', "functions.js");
	}
}

function o_getOpenWin() {
	var w = top;
	try {
		if (w.opener && w.opener.o_info) {
			w = w.opener;
		}
	} catch (e) {}
	return w;
}

function o_beforeserver() {
//mal versuche mit Ext.onReady().. erst dann wieder clicks erlauben...
	o_info.linkbusy = true;
	showAjaxBusy.delay(0.5);
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
	// alert("busy:"+o_info.linkbusy);
	if (o_info.linkbusy) {
		return false;
	} else {
		var doreq = (o2c==0 || confirm(o_info.dirty_form));
		if (doreq) o_beforeserver();
		return doreq;
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

// b_AddOnDomReplacementFinishedCallback is used to add callback methods that are executed after
// the DOM replacement has occured. Note that when not in AJAX mode, those methods will not be 
// executed. Use this callback to execute some JS code to cleanup eventhandlers or alike
var b_onDomReplacementFinished_callbacks=new Array();//array holding js callback methods that should be executed after the next ajax call
function b_AddOnDomReplacementFinishedCallback(funct) {
	B_AjaxLogger.logDebug("callback stack size: " + b_onDomReplacementFinished_callbacks.length, "functions.js ADD"); 
	if (Ext.isGecko3 && !Ext.isSafari) { B_AjaxLogger.logDebug("stack content"+b_onDomReplacementFinished_callbacks.toSource(), "functions.js ADD") };

	b_onDomReplacementFinished_callbacks.push(funct);
	B_AjaxLogger.logDebug("push to callback stack, func: " + funct, "functions.js ADD");
}

//same as above, but with a filter to prevent adding a funct. more than once
//funct then has to be an array("identifier", funct) 
function b_AddOnDomReplacementFinishedUniqueCallback(funct) {
	if (funct.constructor == Array){
		B_AjaxLogger.logDebug("add: its an ARRAY! ", "functions.js ADD"); 
		//check if it has been added before
		if (b_onDomReplacementFinished_callbacks.search(funct[0])){
			B_AjaxLogger.logDebug("push to callback stack, already there!!: " + funct[0], "functions.js ADD");		
			return;
		} 
	}
	b_AddOnDomReplacementFinishedCallback(funct);
}

// main interpreter for ajax mode
var o_debug_trid = 0;
function o_ainvoke(r) {
	// commands
	
	o_info.inainvoke = true;
	var cmdcnt = r["cmdcnt"];
	if (cmdcnt > 0) {
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
							var hfrag = c1["hfrag"]; // html fragment of component
							var jsol = c1["jsol"]; // javascript on load
							var hdr = c1["hdr"]; // header
							if (o_info.debug) o_log("c2: redraw: "+c1["cname"]+ " ("+ciid+") "+c1["hfragsize"]+" bytes, listener(s): "+c1["clisteners"]);
							var con = hfrag.stripScripts();
							var hdrco = hdr+"\n\n"+con;
							var inscripts = hfrag.extractScripts();
							var newc = $("o_c"+ciid);
							if (newc == null) {
								if (o_info.debug) o_logwarn("could not find comp with id: o_c"+ciid+",\nname: "+c1["cname"]+",\nlistener(s): "+c1["clisteners"]+",\n\nhtml:\n"+hfrag);
								if (B_AjaxLogger.isDebugEnabled()) B_AjaxLogger.logDebug("Error in o_ainvoke(), could not find comp with id: o_c"+ciid+",\nname: "+c1["cname"]+",\nlistener(s): "+c1["clisteners"]+",\n\nhtml:\n"+hfrag, "functions.js");
							} else {
								if(civis){ // needed only for ie 6/7 bug where an empty div requires space on screen
									newc.style.display="";//reset?
								}else{
									newc.style.display="none";
								}
								// do dom replacement
								// remove listeners !! ext overwrite or prototype replace does NOT remove listeners !!
//								newc.descendants().each(function(el){if (el.stopObserving) el.stopObserving()});
								Ext.DomHelper.overwrite(newc, hdrco, false);
								newc = null;
								
								// exeucte inline scripts
								if (inscripts != "") {
									inscripts.each( function(val){ BLoader.executeGlobalJS(val, 'o_ainvoker::inscripts');} );
								}
								if (jsol != "") {
									BLoader.executeGlobalJS(jsol, 'o_ainvoker::jsol');
								}
								if (ishighlight) new Effect.Highlight('o_c'+ciid);
							}
						}
						break;
					case 3:  // createParentRedirectTo leads to a full page reload
						wi.o2c = 0;//??
						var rurl = cda["rurl"];
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
							if (Object.isString(preJsAdd)) {
								BLoader.executeGlobalJS(preJsAdd, 'o_ainvoker::preJsAdd');
							}
							// 3.2) load js file
							var url = ce["url"];
							var enc = ce["enc"];
							if (Object.isString(url)) BLoader.loadJS(url, enc, true);
							if (o_info.debug) o_log("c7: add js: "+url);
						}	
						break;	
					default:
						if (o_info.debug) o_log("?: unknown command "+co); 
						if (B_AjaxLogger.isDebugEnabled()) 	B_AjaxLogger.logDebug("Error in o_ainvoke(), ?: unknown command "+co, "functions.js");
						break;
				}		
			} else {
				if (o_info.debug) o_log ("could not find window??");
				if (B_AjaxLogger.isDebugEnabled()) B_AjaxLogger.logDebug("Error in o_ainvoke(), could not find window??", "functions.js");
			}		
		}
		// execute onDomReplacementFinished callback functions
		var stacklength = b_onDomReplacementFinished_callbacks.length;
		if (Ext.isGecko3 && !Ext.isSafari) { B_AjaxLogger.logDebug("stack content"+b_onDomReplacementFinished_callbacks.toSource(), "functions.js"); }
		
		for (mycounter = 0; stacklength > mycounter; mycounter++) { 
			if (mycounter > 50) {
				if (B_AjaxLogger.isDebugEnabled()) B_AjaxLogger.logDebug("Stopped executing DOM replacement callback functions - to many functions::" + b_onDomReplacementFinished_callbacks.length, "functions.js");
				break; // emergency break
			}
			B_AjaxLogger.logDebug("Stacksize before shift: " + b_onDomReplacementFinished_callbacks.length, "functions.js");
			var func = b_onDomReplacementFinished_callbacks.shift();
			if (typeof func.length === 'number'){
				if (func[0] == "glosshighlighter") {
					var tmpArr = func[1];
					B_AjaxLogger.logDebug("arr fct: "+ tmpArr, "functions.js");
					func = tmpArr;
				 }				
			}
			if (B_AjaxLogger.isDebugEnabled()) B_AjaxLogger.logDebug("Executing DOM replacement callback function #" + mycounter + " with timeout funct::" + func, "functions.js");
			// don't use execScript here - must be executed outside this function scope so that dom replacement elements are available
			func.delay(0.01);
			B_AjaxLogger.logDebug("Stacksize after timeout: " + b_onDomReplacementFinished_callbacks.length, "functions.js");
		}
	}
	
	o_info.inainvoke = false;
	
/* minimalistic debugger / profiler	
	BDebugger.logDOMCount();
	BDebugger.logGlobalObjCount();
	BDebugger.logGlobalOLATObjects();
	BDebugger.logManagedOLATObjects();
*/
}

function showAjaxBusy() {
	// release o_info.linkbusy only after a successful server response 
	// - otherwhise the response gets overriden by next request
	if (o_info.linkbusy) {
		// try/catch because can fail in full page refresh situation when called before DOM is ready
		try {
			$('b_ajax_busy').addClassName('b_ajax_busy');
			$('b_body').addClassName('b_ajax_busy');
		} catch (e) {}
	}
}

function removeAjaxBusy() {
	// try/catch because can fail in full page refresh situation when called before page DOM is ready
	try { 
		$('b_ajax_busy').removeClassName('b_ajax_busy');
		$('b_body').removeClassName('b_ajax_busy');
	} catch (e) {}
}

//safari destroys new added links in htmleditor see: http://bugs.olat.org/jira/browse/OLAT-3198
var htmlEditorEnabled = (Prototype.Browser.IE || Prototype.Browser.Gecko);
var scormPlayerEnabled = (Prototype.Browser.IE || Prototype.Browser.Gecko || Prototype.Browser.WebKit);

function setFormDirty(formId) {
	// sets dirty form content flag to true and renders the submit button
	// of the form with given dom id as dirty.
	// (fg) 
	o2c=1;
	// fetch the form and the forms submit button is identified via the olat 
	// form submit name
	var myForm = document.getElementById(formId);
	//TODO:gs:a why not directly accessing the submit button by an id. name="olat_fosm" send additional parameter which is unused. OLAT-1363
	if (myForm != null) {
		var mySubmit = myForm.olat_fosm_0;
		if(mySubmit == null){
			mySubmit = myForm.olat_fosm;
		}
		// set dirty css class
		if(mySubmit) mySubmit.className ="b_button b_button_dirty";
	} else {
		B_AjaxLogger.logDebug("Error in setFormDirty, myForm was null for formId=" + formId, "functions.js");
	}
}


//Pop-up window for context-sensitive help
function contextHelpWindow(URI) {
helpWindow = window.open(URI, "HelpWindow", "height=760, width=940, left=0, top=0, location=no, menubar=no, resizable=yes, scrollbars=yes, toolbar=no");
helpWindow.focus();
}

//TODO: for 5.3 add popup capability to link and table
function o_openPopUp(url, windowname, width, height, menubar) {
	// generic window popup function
	attributes = "height=" + height + ", width=" + width + ", resizable=yes, scrollbars=yes, left=100, top=100, ";
	if (menubar) {
		attributes += "location=yes, menubar=yes, status=yes, toolbar=yes";
	} else {
		attributes += "location=no, menubar=no, status=no, toolbar=no";
	}
	var win = window.open(url, windowname, attributes);
	win.focus();
}

function b_togglebox(domid, toggler) {
	// toggle the domid element and switch the toggler classes
	new Effect.toggle(domid, 'slide', {duration: 0.5}); 
	if(toggler.hasClassName('b_togglebox_closed')) {
		toggler.removeClassName('b_togglebox_closed');
		toggler.addClassName('b_togglebox_opened');
	} else {
		toggler.removeClassName('b_togglebox_opened');
		toggler.addClassName('b_togglebox_closed');
	}
}

function b_handleFileUploadFormChange(fileInputElement, fakeInputElement, saveButton) {
	// file upload forms are rendered transparent and have a fake input field that is rendered.
	// on change events of the real input field this method is triggered to display the file 
	// path in the fake input field. See the code for more info on this
	var fileName = fileInputElement.value;
	// remove unix path
	slashPos = fileName.lastIndexOf('/');
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
		saveButton.className='b_button b_button_dirty'
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
			} else {
				if (B_AjaxLogger.isDebugEnabled()) B_AjaxLogger.logDebug("Error in gotonode(), could not find main window", "functions.js");
			}			
		}
	} catch (e) {
	 alert(e);
		if (B_AjaxLogger.isDebugEnabled()) B_AjaxLogger.logDebug("Error in gotonode()::" + e.message, "functions.js");
	}
}


//TODO:: discuss with HJZ how calendar and course should be designed
function o_openUriInMainWindow(uri) {
	// get the "olatmain" window
	var w = top;
	try {
		if (w.opener && w.opener.o_info) {
		  w = w.opener;
		}
	} catch (e) {}
	
	w.focus();
	w.location.replace(uri);
}

function b_viewportHeight() {
	// based on prototype library
	var prototypeViewPortHight = document.viewport.getHeight()
	if (prototypeViewPortHight > 0) {
		return prototypeViewPortHight;
	} else {
		return 600; // fallback
	}
}

function b_getMainColumnsMaxHeight() {
	// calculate the height of the inner content area that can be used for 
	// displaying content without using scrollbars. 
	// Depends on prototype library
	// (fg)
	var col1Height = 0
	var col1 = Ext.get('b_col1_content');
	if (col1 != 'undefined' && col1 != null) col1Height = col1.getHeight();

	var col2Height = 0
	var col2 = Ext.get('b_col2_content');
	if (col2 != 'undefined' && col2 != null) col2Height = col2.getHeight();

	var col3Height = 0
	var col3 = Ext.get('b_col3_content');
	if (col3 != 'undefined' && col3 != null) col3Height = col3.getHeight();

	var mainInnerHeight = (col1Height > col2Height ? col1Height : col2Height);
	mainInnerHeight = (mainInnerHeight > col3Height ? mainInnerHeight : col3Height);

	if (mainInnerHeight > 0) {
		return mainInnerHeight;
	} 
	// fallback, try to get height of main container
	var mainHeight = 0
	var main = Ext.get('b_main');
	if (main != 'undefined' && main != null) mainHeight = main.getHeight();
	if (main > 0) {
		return main;
	} 
	// fallback to viewport height	
	return b_viewportHeight();
}
  
function b_resizeIframeToMainMaxHeight(iframeId) {
	// adjust the given iframe to use as much height as possible
	// (fg)
	var theIframe = Ext.fly(iframeId);
	if (theIframe != 'undefined' && theIframe != null) {
		var colsHeight = b_getMainColumnsMaxHeight();
		
		var potentialHeight = b_viewportHeight() - 100;// remove some padding etc.
		var elem = Ext.get('b_header');
		if (elem != 'undefined' && elem != null) potentialHeight = potentialHeight - elem.getHeight();
		elem = Ext.get('b_nav');
		if (elem != 'undefined' && elem != null) potentialHeight = potentialHeight - elem.getHeight();
		elem = Ext.get('b_footer');
		if (elem != 'undefined' && elem != null) potentialHeight = potentialHeight - elem.getHeight();
		// resize now
		var height = (potentialHeight > colsHeight ? potentialHeight : colsHeight) + "px";
		theIframe.setHeight(height);
	}
}
// for gui debug mode
var o_debu_oldcn, o_debu_oldtt;

function o_debu_show(cn, tt) {
	if (o_debu_oldcn) o_debu_hide(o_debu_oldcn, o_debu_oldtt);
	cn.style.border='3px solid #00F'; 
	cn.style.margin='0px';
	cn.style.background='#FCFCB8';
	Element.show(tt);
	o_debu_oldtt = tt;
	o_debu_oldcn = cn;
}

function o_debu_hide(cn, tt) {
	Element.hide(tt);
	cn.style.border='1px dotted black'; 
	cn.style.margin='2px';
	cn.style.background='';
}

function o_dbg_mark(elid) {
	var el = $(elid);
	if (el) { 
		//el.style.margin='0px';
		el.style.background='#FCFCB8';
		el.style.border='3px solid #00F'; 
	}
}

function o_dbg_unmark(elid) {
	var el = $(elid);
	if (el) {
		//el.style.margin='0px';
		el.style.border=''; 
		el.style.background='';
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
	var logc = $("o_debug_cons");
	if (logc) {
		if (o_log_all.length == 4000) o_log_all = o_log_all +"\n... (stripped: to long)... ";
		logc.value = o_log_all;
	}
	if(!Prototype.Browser.IE && !Object.isUndefined(window.console)){
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
function o_ffEvent (formNam, dispIdField, dispId, eventIdField, eventInt){
	//set hidden fields and submit form
	var dispIdEl = document.getElementById(dispIdField);
	var defDispId = dispIdEl.value;
	dispIdEl.value=dispId;
	var eventIdEl = document.getElementById(eventIdField);
	var defEventId = eventIdEl.value;
	eventIdEl.value=eventInt;
	// manually execute onsubmit method - calling submit itself does not trigger onsubmit event!
	if (document.forms[formNam].onsubmit()) {
		document.forms[formNam].submit();
	}
	dispIdEl.value = defDispId;
	eventIdEl.value = defEventId;
}

//
// param formId a String with flexi form id
function setFlexiFormDirtyByListener(e,target,options){
	setFlexiFormDirty(options.formId);
}

function setFlexiFormDirty(formId){

	var isRegistered = o3c.indexOf(formId) > -1;
	if(!isRegistered){
		o3c.push(formId);
	}
	var myForm = document.getElementById(formId);
	var submitButton = document.getElementById(myForm["FlexiSubmit"]);
	if(submitButton != null){
		//not all forms must have a submit element! 
		submitButton.className ="b_button b_button_dirty";
		o2c=1;
	}
}

//
//
function o_ffRegisterSubmit(formId, submElmId){
	var myForm = document.getElementById(formId);
	myForm["FlexiSubmit"] = submElmId;
}
/*
* renders an info msg that slides from top into the window
* and hides automatically
*/
function showInfoBox(title, format){
	// Factory method to create message box
	function createBox(t, s){
	        return ['<div class="b_msg-div msg">',
	                '<div class="x-box-tl"><div class="x-box-tr"><div class="x-box-tc"></div></div></div>',
	                '<div class="x-box-ml"><div class="x-box-mr"><div class="x-box-mc"><div class="b_msg_info_content b_msg_info_winicon"><h3>', t, '</h3>', s, '<br/><br/></div></div></div></div>',
	                '<div class="x-box-bl"><div class="x-box-br"><div class="x-box-bc"></div></div></div>',
	                '</div>'].join('');
	}
    var s = String.format.apply(String, Array.prototype.slice.call(arguments, 1));
    var msgCt = Ext.get('b_page').insertHtml('beforeBegin', createBox(title, s), true);
    // Hide message automatically
    var time = (s.length > 70) ? 6 : 4;
    time = (s.length > 150) ? 8 : time;
    msgCt.slideIn('t').pause(time).ghost("t", {remove:true});
    // Visually remove message box immediately when user clicks on it
    // The ghost event from above is triggered anyway. 
    msgCt.on("click", function(e, t) {
    	var msg = Ext.get(t).findParent("div.b_msg-div",10,true); 
    	if(msg) {
    		msg.hide();
    	}
    });
    // 	
    // Help GC, prevent cyclic reference from on-click closure (OLAT-5755)
    s = null;
    msgCt = null;
    time = null;
}
/*
* renders an message box which the user has to click away
* The last parameter buttonCallback is optional. if a callback js 
* function is given it will be execute when the user clicks ok or closes the message box
*/
function showMessageBox(type, title, message, buttonCallback){
	if(type == 'info'){
		showInfoBox(title, message)
	} else {
		// No memory issues, this is a singleton. Only one message box possible
		// at any time, ExtJS takes care itself of cleaning up (it reuses the
		// DOM elements for the next message)
		var msg = Ext.MessageBox.show({
			title: title,
			msg: message,
			icon: 'b_msg_'+type+'_winicon',
			minWidth: 300,
			buttons: Ext.MessageBox.OK,
			fn: buttonCallback
		});
	}
}


/*
* print command, prints iframes when available
*/
function b_doPrint() {
	// When we have an iframe, issue print command on iframe directly
	var iframes =  $$('div.b_iframe_wrapper iframe');
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
	Ext.select('span.b_translation_i18nitem').hover(function(){	
		Ext.get(this.firstChild).show();
		//if (B_AjaxLogger.isDebugEnabled()) B_AjaxLogger.logDebug("Entered i18nitem::" + this.firstChild, "functions.js:b_attach_i18n_inline_editing()");
	},function(){
		Ext.select('a.b_translation_i18nitem_launcher').hide();
		//if (B_AjaxLogger.isDebugEnabled()) B_AjaxLogger.logDebug("Leaving i18nitem::" + this, "functions.js:b_attach_i18n_inline_editing()");
	});
	// Add highlight effect on link to show which element is affected by this link
	Ext.select('a.b_translation_i18nitem_launcher').hover(function(){	
		Ext.get(this).findParent('span.b_translation_i18nitem').highlight();
	},function(){});
	// Add to on ajax ready callback for next execution
	b_AddOnDomReplacementFinishedCallback(b_attach_i18n_inline_editing);
}
 
 
/**
 * Minimalistic debugger to find ever growing list of DOM elements, 
 * global variables or OLAT managed variables. To use it, uncomment
 * lines in o_ainvoke()
 */
var BDebugger = {
	_lastDOMCount : 0,
	_lastObjCount : 0,
	_knownGlobalOLATObjects : ["o_afterserver","o_onc","o_getOpenWin","o_ainvoke","o_info","o_beforeserver","o_ffEvent","o_openPopUp","o_debu_show","o_logwarn","o_dbg_unmark","o_ffRegisterSubmit","o_clearConsole","o_init","o_log","o_allowNextClick","o_dbg_mark","o_debu_hide","o_logerr","o_debu_oldcn","o_debu_oldtt","o_openUriInMainWindow","o_debug_trid","o_log_all"],
		
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
	},
	
	logManagedOLATObjects : function() {
		var self = BDebugger;
		if (o_info.objectMap.length > 0) {
			console.log(o_info.objectMap.length + " managed OLAT objects found:");
			o_info.objectMap.eachKey(function(key){
				var item=o_info.objectMap.get(key); 
				console.log("\t" + typeof item + " \t" + key); 
				return true;
			});
		}
	}
}

 