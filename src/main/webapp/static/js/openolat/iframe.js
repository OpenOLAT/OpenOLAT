// code yanked from the Yahoo media player. Thanks, Yahoo.
// Graceful Degradation of Firebug Console Object for IE browsers...
// http://ajaxian.com/archives/graceful-degradation-of-firebug-console-object
if (!("console" in window)) {
    var names = ["log", "debug", "info", "warn", "error", "assert", "dir", "dirxml", "group"
                 , "groupEnd", "time", "timeEnd", "count", "trace", "profile", "profileEnd"];
    window.console = {};
    for (var i = 0; i <names.length; ++i) window.console[names[i]] = function() {};
}

// loaded true: page has finished loading and resize has been performed for the first time
var loaded = false;
// remember the last calculated page height
var lastHeight = 0;
// detect if the content was opened in a popup window and thus does not need to be resized
var isPopUp = ( (opener == null || opener == 'undefined') ? false : true );

function b_getMainWindow(mywin) {
	if (mywin == null) {
		return null;
	}
	// check if current window contains our iframe
	var iframe = mywin.document.getElementById(b_iframeid)
	if (iframe != null && iframe != 'undefined') {
		return mywin;		
	}
	// check if we are already on top - no more parent left
	if (mywin == mywin.parent) {
		return null;
	}
	// call recursion on our parent
	return b_getMainWindow(mywin.parent);
}

function b_hideExtMessageBox() {
	//for compatibility
}

//start highlighting glossary term inside iframe. will also generate ext-tooltips in the correct context.
function b_glossaryHighlight(){
	if (typeof(o_tm_doHighlightAll) == 'function'){
		o_tm_doHighlightAll(window.document, b_getGlossaryArray(), "");
	}
}

function b_sendNewUriEventToParent() {
//console.log("b_sendNewUriEventToParent window.name=" + window.name + " b_iframeid=" + b_iframeid + " window.location=" + window.location);
	try {
		// Don't notify new uri events in popup windows
		if (isPopUp) { return; }
		// First remove listener on window for subsequent requests
		b_removeOnloadEvent(b_sendNewUriEventToParent);
		// Fire new uri event to OLAT main window
		var mainwindow = b_getMainWindow(window.parent);
		if (mainwindow == null || mainwindow == 'undefined') {
			// main window not found - exit
			return;
		}
		// Only sent new uri event when loaded directly in our frame. Don't forward new uri events into subframes
		if (mainwindow != window.parent) {
			return;
		}
		// Suppress the execution of the olatunload method in the main window
		mainwindow.suppressOlatOnUnloadOnce = true;
		// Forward new uri event to main window
		// Extract page anchor first and add it as parameter
		var hash = window.location.hash;
		if (hash.indexOf('#') == 0) {
			var docHref = decodeURI(document.location.href);//double encode by IE
			mainwindow.newUriEvent(encodeURI(docHref.substring(0, docHref.length-hash.length)) + '?hash=' + hash.substring(1));			
		} else {
			// scroll to top of main window (OLAT-3325) first
			mainwindow.scrollTo(0,0);
			var docHref = decodeURI(document.location.href);//double encode by IE
			mainwindow.newUriEvent(encodeURI(docHref));
		}
	} catch(e) {
		//console.log(e);
	}
}

function b_addOnloadEvent(fnc){
	//console.log("b_addOnloadEvent window.name=" + window.name + " b_iframeid=" + b_iframeid + " fnc=" + fnc);
	try {
		// Only continue if we can locate the main window
		var mainwindow = b_getMainWindow(window.parent);
		if (mainwindow == null) {
			// main window not found - exit
			return;
		}
			
		if ( typeof window.addEventListener != "undefined" )
			window.addEventListener( "load", fnc, false );
		else if ( typeof window.attachEvent != "undefined" ) {
			window.attachEvent( "onload", fnc );
		}
		else {
		if ( window.onload != null ) {
			var oldOnload = window.onload;
			window.onload = function ( e ) {
				oldOnload( e );
				window[fnc]();
			};
		} else 
			window.onload = fnc;
		}
	} catch(e) {
		//console.log(e);
	}
}
//only for firefox
function b_anchorFirefoxWorkaround() {
	var anchors = document.getElementsByTagName('a');
	for (var i=0; i < anchors.length; i++) {
		var anchor = anchors[i];
		var href = anchor.getAttribute('href');
		if(href && href[0] == "#") {
      		anchor.addEventListener('click', function(el) {
      			try {
      				var href = el.target.getAttribute('href');
      	      		var name = href.substring(1);
      				var nameElement = document.getElementsByName(name);
      				
      				var element = null;
      				if(nameElement != null && nameElement.length > 0) {
             			element = nameElement[0];
      				} else {
      					var idElement = document.getElementById(name);
      					if(idElement != null) { 
      						element = idElement;
      					}
      				}
      				if(element && window && window.parent) {
              			var offset = b_anchorFirefoxWorkaroundCumulativeOffset(element);
              			window.parent.scrollTo(offset[0], offset[1]);
      				}
      			} catch(e) {
      				//console.log(e);
      			}
      			return true;
      		});
		}
	}
}
//only for firefox
function b_anchorFirefoxWorkaroundCumulativeOffset(element) {
    var valueT = 0, valueL = 0;
    if (element.parentNode) {
      do {
        valueT += element.offsetTop  || 0;
        valueL += element.offsetLeft || 0;
        element = element.offsetParent;
      } while (element);
    }
    return [valueL, valueT];
}

function b_removeOnloadEvent(fnc) {
	try {
		if (window.removeEventListener)
			window.removeEventListener("load", fnc, false);
		else
			window.detachEvent("onload", fnc);
	} catch(e) {
		//console.log(e);
	}
}

function b_addOnclickEvent(myFnc) {
//console.log("b_addOnclickEvent window.name=" + window.name + " b_iframeid=" + b_iframeid);
	// Execute outside this execution stack to not execute when clicked on links that load a 
	// new page anyway. Reduces flickering.
	var fnc = function(){setTimeout(myFnc,0);};
	try {
		// Only continue if we can locate the main window
		var mainwindow = b_getMainWindow(window.parent);
		if (mainwindow == null) {
			// main window not found - exit
			return;
		}
	
		if ( typeof window.addEventListener != "undefined" )
			window.addEventListener( "click", fnc, false );
		else if ( typeof window.attachEvent != "undefined" ) {
			window.attachEvent( "onclick", fnc );
		}
		else {
		if ( window.click != null ) {
			var oldClick = window.click;
			window.click = function ( e ) {
				oldClick( e );
				window[fnc]();
			};
		} else 
			window.click = fnc;
		}
	} catch(e) {
		//console.log(e);
	}
}

function b_changeLinkTargets() {
	var anchors = document.getElementsByTagName('a');
	for (var i=0; i < anchors.length; i++) {
		var anchor = anchors[i];
		if (anchor.getAttribute('href')) {
			var target = anchor.getAttribute('target');
			var href = anchor.getAttribute('href');
			if (target != null && target != undefined && (target == '_top' || target == '_parent')) {
				// fix broken legacy links that try to open content in top window
				// iframe content must always stay within iframe 
				var mainwindow = b_getMainWindow(window.parent);
				if (mainwindow == null || mainwindow == 'undefined') return;
				if (mainwindow == window.parent) {
					// don't open content in main window, remove target and open in current window which is our iframe
					anchor.removeAttribute('target');
				} else {
					// best guess is to open in parent window of current frameset / window
					anchor.target = "_parent";				
				}
			} else if((target === "undefined" || target == null) && (
						href.indexOf("/repo/go?rid=") != -1 
						|| href.indexOf("/RepositoryEntry/") != -1 
						|| href.indexOf("/BusinessGroup/") != -1 
						|| href.indexOf("Site/") != -1
						|| href.indexOf("/CatalogEntry/") != -1										
						|| href.indexOf("/Portal/") != -1
						|| href.indexOf("/CatalogAdmin/") != -1
						|| href.indexOf("/GMCMenuTree/") != -1
					)) {
				anchor.target = "_parent";	
			}
		}
	}
}

// OO-10015, OO-10045: links to OpenOlat in pages delivered by the content domain. The
// page cannot call parent.gotonode() because the parent has another origin, and links
// like /auth/RepositoryEntry/123 resolve against the content domain. The decorator:
// - posts a message to the OpenOlat window for a plain click in a frame
//   (olat-gotonode for an element of the same course, else olat-gotopath),
// - points the link to the main domain for all other clicks (new tab, middle click,
//   context menu) and lets the browser handle them.
function b_registerContentLinkBridge(server_origin, content_origin, context_path, legacy_context) {
	const jsRe = /^javascript:\s*(?:window\.)?(?:parent|top)\.(gotonode|gototool)\(\s*['"]?([\w-]{1,64})['"]?\s*\)\s*;?\s*$/;
	const nodeRe = /^\/(?:auth|url)\/RepositoryEntry\/(\d+)\/CourseNode\/([\w-]{1,64})\/?$/;
	context_path = context_path || '';

	// Send the message to all ancestors, only the windows of the main domain receive it.
	// window.top is not enough if OpenOlat itself is embedded in a frame.
	function post(msg) {
		let w = window;
		for (let i = 0; i < 16; i++) {
			let p = w.parent;
			if (!p || p === w) break;
			try {
				p.postMessage(msg, server_origin);
			} catch (e) {
				// ignore
			}
			w = p;
		}
	}

	// Path of an OpenOlat page (/auth/... or /url/... with query and hash) or null
	function olatPath(a) {
		let url;
		try {
			url = new URL(a.getAttribute('href'), document.baseURI);
		} catch (e) {
			return null;
		}
		if (url.origin !== server_origin && url.origin !== content_origin) return null;
		let path = url.pathname;
		if (context_path.length > 0 && path.indexOf(context_path + '/') === 0) {
			path = path.substring(context_path.length);
		} else if (legacy_context && path.indexOf(legacy_context) === 0) {
			path = path.substring(legacy_context.length - 1);
		}
		let m = /^\/(auth|url)\/([^\/]*)/.exec(path);
		// component URLs of OpenOlat windows (window:timestamp:component) are not links
		if (!m || /:|%3a/i.test(m[2])) return null;
		return path + url.search + url.hash;
	}

	function findAnchor(e) {
		return e.target && e.target.closest ? e.target.closest('a[href]') : null;
	}

	// Point the link to the main domain, the browser opens it there
	function rewrite(a, path) {
		let mainUrl = server_origin + context_path + path;
		if (a.href !== mainUrl) {
			a.href = mainUrl;
		}
	}

	document.addEventListener('click', function(e) {
		let a = findAnchor(e);
		if (!a) return;
		let m = jsRe.exec(a.getAttribute('href'));
		if (m) {
			e.preventDefault();
			post({ type: 'olat-' + m[1], id: m[2] });
			return;
		}

		let path = olatPath(a);
		if (!path) return;
		rewrite(a, path);
		if (e.button !== 0 || e.metaKey || e.ctrlKey || e.shiftKey || e.altKey) return;
		if ((a.getAttribute('target') || '').toLowerCase() === '_blank') return;
		if (window.parent === window) return; // own tab, the browser opens the main domain

		e.preventDefault();
		let nm = nodeRe.exec(path.split(/[?#]/)[0]);
		if (nm && typeof o_info !== 'undefined' && o_info.repository_entry_key && nm[1] === o_info.repository_entry_key) {
			post({ type: 'olat-gotonode', id: nm[2] });
		} else {
			post({ type: 'olat-gotopath', path: path });
		}
	}, true);

	function rewriteOnly(e) {
		let a = findAnchor(e);
		if (!a) return;
		let path = olatPath(a);
		if (path) {
			rewrite(a, path);
		}
	}
	document.addEventListener('auxclick', rewriteOnly, true);
	document.addEventListener('contextmenu', rewriteOnly, true);
	document.addEventListener('mousedown', rewriteOnly, true);
}