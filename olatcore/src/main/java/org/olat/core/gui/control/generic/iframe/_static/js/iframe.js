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

function b_sizeIframe() {
//console.log("b_sizeIframe window.name=" + window.name + " b_iframeid=" + b_iframeid);
	try {
		// Don't resize popup windows
		if (isPopUp) { return };
		// Get frame from parent window - it's not possible to assign the height
		// directly on the window element itself. Only resize the frame if it has a name. 
		// If it doesn't, we don't know what fancy thing it is and don't resize it.
		var mainWindow = b_getMainWindow(window.parent);
		var frame = (window.name == '' ? null : window.parent.document.getElementsByName(window.name)[0]);
		if (frame == null || frame == 'undefined') {
			// Try fallback: check for our iframe name in case the current window has 
			// been renamed by the local js code (e.g. as done in PTO scripts: window.name=main...)
			frame = (window.name == '' ? null : window.parent.document.getElementsByName(b_iframeid)[0]);
		}
		if (frame != null && frame != 'undefined') {
			//console.log('b_sizeIframe window.name=' + window.name + ' frame.style.display=' + frame.style.display + ' frame.style.visibility=' + frame.style.visibility + " window.scrollY=" + window.scrollY);
			if (frame.style.display != 'none' && frame.style.visibility != 'hidden') {		
				// Reset any scrollbars to 0/0 position
				window.scrollTo(0,0);				
				// Reset frame height to original frame height when this is an inline URI, otherwhise frame will never shrink again (OLAT-3325)
				if (!loaded) { // only on first resize operation after load
					if (b_isInlineUri) {
						if (parent.b_iframe_origHeight != 'undefined') frame.height = parent.b_iframe_origHeight; 
					} else parent.b_iframe_origHeight = parseInt(frame.height);
				}
				// Calculate the document height as the browser sees it
				// Use various methods for different browser and browser render modes
				var docHeight = 0;
				if (window.innerHeight && window.scrollMaxY) docHeight = window.innerHeight + window.scrollMaxY; // FF style
				if (document.documentElement && document.documentElement.scrollHeight >= document.documentElement.offsetHeight) docHeight = Math.max(docHeight, document.documentElement.scrollHeight); // Explorer 6 strict mode, Safari
				if (document.body.scrollHeight >= document.body.offsetHeight) docHeight = Math.max(docHeight, document.body.scrollHeight); // all but Explorer Mac
				docHeight = Math.max(docHeight, document.body.offsetHeight); // Explorer Mac...would also work in Mozilla and Safari
				try {
					// don't make smaller than defined min-height as a workaround for the problem with content that contains 
					// only fluid sizes that can't be calculated on a full page refresh (height=100%) (OLAT-3351)
					var minHeight = frame.style.minHeight; 
					if (minHeight != 'undefined' && minHeight.length >2) {
						minHeight = parseInt(minHeight.substring(0,minHeight.length-2));
						docHeight = Math.max(docHeight, minHeight);
					}
				} catch(e){};
				//console.log("window.innerHeight="+window.innerHeight+" window.scrollMaxY="+window.scrollMaxY+" document.documentElement.scrollHeight="+document.documentElement.scrollHeight+" document.documentElement.offsetHeight="+document.documentElement.offsetHeight+" document.body.scrollHeight="+document.body.scrollHeight+" document.body.offsetHeight="+document.body.offsetHeight+" document.body.offsetHeight="+document.body.offsetHeight+" frame.style.minHeight="+frame.style.minHeight+" "+minHeight+" Final calculated doc height="+docHeight+" (lastHeight="+lastHeight+")");
				// Add offset for potential browser scrollbars
				if (docHeight <= lastHeight) {
					// reuse last height, don't shrink page (wrong page height calculation for onclick event resizes)
					docHeight = lastHeight;
				} else {
					docHeight += 35; // add height of vertical scrollbar, biggest is IE7 with 35px
					lastHeight = docHeight;
				}
				frame.height = docHeight;
				// Update height of menu / toolbox height in main window
				if(docHeight != mainWindow.b_iframe_origHeight && mainWindow.B_ResizableColumns != 'undefined') {
					if (mainWindow.B_AjaxLogger.isDebugEnabled()) mainWindow.B_AjaxLogger.logDebug("b_sizeIframe(): executing resize command on main window","iframe.js");
					mainWindow.needsHeightAdjustment = true;
					mainWindow.B_ResizableColumns.adjustHeight();
				}
				//console.log("b_sizeIframe window.name=" + window.name + " docHeight=" + docHeight + " lastHeight=" + lastHeight + " frame.height=" + frame.height + " document.location=" + document.location);				
			}
		}
		// Resize for current window done, remove onload listener. Do this only once 
		// (resize could be triggered by child iframe content)
		if (!loaded) {
			b_removeOnloadEvent(b_sizeIframe);
		}
		// Recursively resize parent if resizer is available
		if (window != window.parent && window.parent.b_sizeIframe) {
			//console.log("b_sizeIframe recursive parent resize: window.parent.name=" + window.parent.name);
			window.parent.b_sizeIframe();
		}
		
		// Pages that contain an #anchor in the URL might have a truncated part now. Use some code to fix this (OLAT-3247)
		if (window.location.hash && window.location.hash.length > 1 && !loaded) {
			// Use timeout 0 to execute immediately after current call stack, meaning after onload has finished.
			setTimeout(function(){
				if(navigator.vendor && navigator.vendor.indexOf('Apple') != -1) {	
					// Reset anchor to fixes problem in Safari since other fix does not work.
					// As a side effect, the anchors do not work in Safari but page is not truncated.
					window.location.replace('#');
				} else {
					// Force page redrawing for FF by adding 1px to frame
					var frame = window.parent.document.getElementsByName(b_iframeid)[0];
					var frameHeight = parseInt(frame.height);
					frame.height = frameHeight + 1;
				}
			},0);
		}
		// page loaded 
		loaded=true;
	} catch(e) {
		console.log(e);
	}
}

function b_enableTooltips(){
	if (window["Ext"]) {
		Ext.QuickTips.init();
	};
};

//start highlighting glossary term inside iframe. will also generate ext-tooltips in the correct context.
function b_glossaryHighlight(){
	if (typeof(o_tm_doHighlightAll) == 'function'){
		o_tm_doHighlightAll(window.document, b_getGlossaryArray(), "");
	}
}
 
function b_hideExtMessageBox(){
// temporary hack to remove back-message in iframe
	var mainwindow = b_getMainWindow(window.parent);
	var counter = 0;
	if (mainwindow != null && mainwindow != 'undefined' && mainwindow["Ext"]) {
		var activ = setInterval(function(){
			try {
				if (!mainwindow.tinyMCE && mainwindow.Ext.MessageBox.isVisible()){
					mainwindow.Ext.MessageBox.hide();
					clearInterval(activ);
				} else if (counter == 10) {
				//stop interval after some tries
					clearInterval(activ);
				}
				counter++;
			} catch (e) {
				console.log(e);			
				clearInterval(activ);
			}
		} ,50);
	};
};

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
		console.log(e);
	}
}

function b_changeLinkTargets() {
	var anchors = document.getElementsByTagName('a');
	for (var i=0; i < anchors.length; i++) {
		var anchor = anchors[i];
		if (anchor.getAttribute('href')) {
			var target = anchor.getAttribute('target');
					
			if (anchor.getAttribute("href").indexOf("/auth/repo/go?rid=") != -1) {
				// absolute links to repository entries have to by opened in the parent frame
				anchor.target = "_parent";
			} else if (target != 'undefined' && (target == '_top' || target == '_parent')) {
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
			}
		}
	}
}