/**
 * Javascript code used by the text marker infrastructure.
 * This file contains code to highlight text and ext-tooltips.
 * it gets used in tmContainer OR in iframe. its counterpart is glossaryhelper.js
 *
 * Some code is borrowed from http://www.nsftools.com/misc/SearchAndHighlight.htm
 *
 * Florian Gnägi, JGS goodsolutions GmbH
 * 2006/07/14
 * Guido Schnider, OLAT
 * 2008/02/25
 * Roman Haag, frentix GmbH, www.frentix.com
 * 2009/03/01
 */
//has to be false in IE (no console there)!
var debug = false; 
 
debug ? console.log("started") : null;
	var foundTerms = new Array(); 
	var isHighlighting = false;
	var workedOnSpans = new Array();
	var isExecuting = false; 
	var workedOnDom = new Array();

//either execute function in mainwindow or reference from iframe to mainwindow
	var isInIframe;
	var mainwindow;
function init(){
	if (typeof (b_getMainWindow) == 'function') {
		isInIframe = true;
		debug ? console.log("highlighter running in iframe"): null;
		mainwindow = b_getMainWindow(window.parent);
	} else {
		isInIframe = false;
		debug ? console.log("highlighter running in tmContainer directly"): null;
		mainwindow = this;
	}
}
	init();

// some helper methods
//////////////////////

// get glossaryArray from last active domID (if multiple glossaries would be active)
// used from iframe.js
function b_getGlossaryArray(){
	//in case this method is called before loading has completed... do init()
	init();
	debug ? console.log("mainwindow at initial time: " + mainwindow): null;
	if (typeof(mainwindow.o_gloss_getLastActiveGlossArray) == 'function') 
		return mainwindow.o_gloss_getLastActiveGlossArray();
}

// set in glossaryhelper = context from mainwindow
function setLastActiveGlossary(glossaryId){
	mainwindow.o_gloss_setLastActiveGlossary(glossaryId);
}

// receive from tmContainer 
function o_gloss_getDefinitionMapperPath(){
	return mainwindow.b_glossaryDefinitionMapperPath ;
}

// receive from tmContainer
function o_gloss_getGlossaryId(){
	return mainwindow.b_glossaryId; 
}

/*
 * highlighting for multiple glossaries would be possible. 
 * each has its own glossaryId, domId for place to apply on it.
 *
 * March 2009  Roman Haag, roman.haag@frentix.com
 */
function o_tm_highlightFromArray(glossaryId, domId) {
	//primitive semaphore
	if (!isExecuting){
	isExecuting = true;
	if ( workedOnDom.indexOf(domId) != -1){
//		return;
	} workedOnDom.push(domId);
	
	setLastActiveGlossary(glossaryId);
	debug ? console.log("running highlightFromArray(" + glossaryId + ", " + domId + ")") : null;
	
	try {
	
	//if (B_AjaxLogger.isDebugEnabled()) B_AjaxLogger.logDebug("running highlightFromArray(" + glossaryId + ", " + domId + ")" ,"glossarymarker.js");
	
		markerArray = new Array();
		markerArray = eval(o_glossaries[glossaryId]);
	
		// do the highlighting on the given dom element
		o_tm_doHighlightAll(document, markerArray, domId);

	} catch (e) {
	  	// catch any exception that might happen and do nothing. just in case
	  	// something unexpected happens, make sure the text marker code does not break
	  	// any other javascript code
	}

	//var domID = domId;
	var GlossaryHighlightCallback = {
		highlightAfterDomReplace : function(){
			o_tm_highlightFromArray(glossaryId, domID);
		}
	}
	  
	setTimeout(function() {  b_AddOnDomReplacementFinishedUniqueCallback( new Array("glosshighlighter", GlossaryHighlightCallback.highlightAfterDomReplace) ); },0);
//	b_AddOnDomReplacementFinishedUniqueCallback( function(){ workedOnDom = []; } );

	isExecuting = false;	
	}//isExecuting
}
 
 /*
 * Searches for Terms given in markerArray. Highlights them for domId in currentDocument.
 * Adds all found occurrences of all terms to a stack for later processing with ext-js. 
 *
 * currentDocument: a browser html document
 * marker array: an array that contains arrays. each of those arrays
 *               represent a glossary term/synonym/flexion
 * domId: the DOM element id on which the marking should be applied
 *        the domId can be null or empty, the marking will then be 
 *        applied to the whole body element
 *
 * August 2006 Florian Gnägi
 * March 2009  Roman Haag, roman.haag@frentix.com
 */
function o_tm_doHighlightAll(currentDocument, markerArray, domId) {
	//primitive semaphore
	if (!isHighlighting){
		isHighlighting = true;
		debug ? console.log("Higlighting in " + currentDocument) : null;
		try {
	//		if (B_AjaxLogger.isDebugEnabled()) B_AjaxLogger.logDebug("running doHighlightAll(" + Object.toHTML(currentDocument) + ", " + domId + ")" ,"glossarymarker.js");
			var searchNode;
			if (domId == null || domId == "null" || domId == "") {
		  		// search over whole body
				searchNode = currentDocument.getElementsByTagName("body")[0];
			} else {
		  		// search only in given dom ID
		   		searchNode = $(domId);
		  	}
		  	if (searchNode == null || typeof(searchNode.innerHTML) == "undefined") {
				// do access to innerHTML, we have to exit here
				isHighlighting = false;
		    	return false;
		  	}
		  	// get the text we have to search through
		  	var searchText = searchNode.innerHTML;
		  	
		  	// go through the array and parse the search text for each glossary term/synonym/flexion
		  	// reset already found Term-array
		  	foundTerms = [];
		  	foundTerms = new Array();
		  	workedOnSpans = [];
		
		  	
		  	//enable/disable to have a hash as cache
			/*  	o_info["glosshash"] = [];
				  	var myhash = null;
			  		myhash = $H(); 
			  		o_info["glosshash"] = myhash; */
			//
		  	
		  	
		  	var highlightString = ""; 
		  	for (var i = 0; i < markerArray.length; i++) {
		  		var allTerms = markerArray[i];
		  		var glossaryMainTerm = markerArray[i][0];
		  		for (var j = 0; j < allTerms.length; j++) {
		  			highlightString = allTerms[j];
			   		searchText = o_tm_doHighlightSingle(searchText, glossaryMainTerm, highlightString);
		  		}
		  	}
	  	
		  	// replace original text with highlighted text
		  	if (foundTerms.length != 0) {
		  		//Ext.DomHelper.overwrite(Ext.get(domId),searchText,true);
			  	searchNode.innerHTML = searchText;  	
			  	
			  	//add tooltips after inserting spans with ids for each Term
			  	debug ? console.log("# element in foundTerms: " + foundTerms.length) : null;
			  	for (var j=0; j < foundTerms.length; j++){
			  		var glossaryMainTerm = foundTerms[j][0];
			  		var highlightString = foundTerms[j][1];
			  		var occurrence = foundTerms[j][2];
			   		o_tm_addExtToolTip(glossaryMainTerm,highlightString,occurrence);			
			  	}			  			  	
			  	debug ? console.log("fertig") : null;
		  	}  
		  	
		  }
		  catch(e) {
		  	// catch any exception that might happen and do nothing. just in case
		  	// something unexpected happens, make sure the text marker code does not break
		  	// any other javascript code
		  }
	isHighlighting = false;
	} //if isHighlighting
}
 
/**
 * creates tooltips for each occurrence of a found term.
 * has to be done after parsing document to prevent adding tips multiple times. 
 *
 * March 2009  Roman Haag, roman.haag@frentix.com
 */
function o_tm_addExtToolTip(glossaryMainTerm, highlightString, occurrence){
	try {
		debug ? console.log("######### new tooltip for " + glossaryMainTerm + " and occurrence #" + occurrence + " of word " + highlightString): null;
		debug ? console.log("Ext is ready: " + Ext.isReady): null;
		var mapperPath = o_gloss_getDefinitionMapperPath() + "/" + o_gloss_getGlossaryId() + '/';
		var targetId = 'gloss' + glossaryMainTerm + '' + highlightString + '' + occurrence;
		targetId = mainwindow.o_gloss_getUniqueTargetId(targetId);
		
		//prevent adding tip twice or more!
		if (workedOnSpans.indexOf(targetId) == -1){
				workedOnSpans.push(targetId);
			
			var tipname = targetId + '_tip';
			var tip = $(tipname)
			debug ? console.log("existing tip: " + tip): null;
			
			var oldtip = Ext.getCmp(tipname);
			debug ? console.log("old tip: " + oldtip): null;
			if (oldtip) {
				oldtip.destroy();
				debug ? console.log("destroy tip!!"): null; 
				var oldtip_d = Ext.getCmp(tipname);
				debug ? console.log("destroyed tip: " + oldtip_d): null;
			}
	
			var targetChk = $(targetId);
			var targetChkExt = Ext.get(targetId);
			debug ? console.log("targetChk : " + targetChkExt): null;
			debug ? console.log("scope " + document.title): null;
			if (targetChk){
				debug ? console.log("tooltip for " + glossaryMainTerm + " and targetId " + targetId + " of path " + mapperPath): null;
				targetChkExt.removeAllListeners();
				//targetChkExt.setOpacity(1);
				
				//allows caching for browser. ext otherwise appends "&garbage=12345" to URL				
				Ext.Ajax.disableCaching = false;			    
			    
			    var neutip = new Ext.ToolTip({
			        target: targetId,
			        id: targetId + '_tip',
			        minWidth: 250,
			        dismissDelay: 0,
			        //   html: 'This is just a static test content... blabliblubb... <b>cool</b>'
			        autoLoad: {url: mapperPath + glossaryMainTerm + '.html', nocache: false }
			    });
				debug ? console.log("neutip ID: " + neutip.getId()): null;
				debug ? console.log("target ev id after creating tip: " + targetChk._prototypeEventID): null;
		    } 		    
		} else {
			debug ? console.log("already worked on " + targetId): null;
		}
    }
    catch(e){
    	debug ? console.log("error: " + e): null;
    }
   
} 
 
 
/*
 * Parse the given text for the search term and apply the highlighting
 *
 * bodyText: text to search throuth (HTML code)
 * searchTerm: term to highlight in text
 *
 * August 2006 Florian Gnägi
 */
function o_tm_doHighlightSingle(bodyText, glossaryMainTerm, searchTerm) {
	highlightEndTag  = "</span>";
  
  // find all occurences of the search term in the given text,
  // and add some "highlight" tags to them (we're not using a
  // regular expression search, because we want to filter out
  // matches that occur within HTML tags and script blocks, so
  // we have to do a little extra validation)
  var newText = "", junkBefore = "", char = "";
  var i = -1;
  var lcSearchTerm = searchTerm.toLowerCase();
  var lcBodyText = bodyText.toLowerCase();
  
  var occurrence = 0;
  // prevent highlighting in buggy IE when a movieplayer is used or in some TinyMCE-cases
  // see OLAT-5447 for more infos
  if(bodyText.indexOf('BGlossarIgnore') > 0  || ( Ext.isIE && bodyText.indexOf('olatFlashMovieViewer') > 0)) {
  	return bodyText;
  }
  
  while (bodyText.length > 0) {
	i = lcBodyText.indexOf(lcSearchTerm, i+1);
	// Finish when search term is not found
	if (i < 0) {
		newText += bodyText;
		bodyText = "";
		break;
	}
	// Skip anything inside an HTML tag (attributes). Tag detected when the next closing 
	// tag comes before the next opening tag
	if (lcBodyText.indexOf(">", i) < lcBodyText.indexOf("<", i) ) 
		continue;
	    	
	// Search for explicit words and not within a word. A word is charactarized as something
	// that is surrounded by whitespace or special characters. HTML entities are also treated
	// like whitespace (<b>, <h3>, <br>, <p>...)
	if (i>0) {	 
		// check character right before the the search term
		char = lcBodyText.charAt(i-1);	
		if ( ! (char == " " || char == "\t" || char == "\n" || char == "\r" || char == "\v" || char == "\f" 
			|| char == ">" || char == "\"" || char == "'" || char == "`" || char == "(" || char == "["  || char == "{"  || char == "-") )		
			continue;
	}
	if (lcBodyText.length > i + searchTerm.length) {
		// check character right after the the search term
		char = lcBodyText.charAt(i + searchTerm.length);
		if ( ! (char == " " || char == "\t" || char == "\n" || char == "\r" || char == "\v" || char == "\f" 
			|| char == "," || char == "."  || char == "!" || char == "?"  || char == ":" || char == ";"  
			|| char == "<" || char == "\"" || char == "'" || char == "`" || char == ")" || char == "]"  || char == "}"  || char == "-" ) )
			continue;
	}
		
	// Do not mark text within textarea, form input option or javascript elements.
	// Prevent to highlight twice, by looking after highlight-tags
	junkBefore = lcBodyText.substring(0,i);
	if ( junkBefore.lastIndexOf("</textarea") < junkBefore.lastIndexOf("<textarea")
		|| junkBefore.lastIndexOf("</embed") < junkBefore.lastIndexOf("<embed")
		|| junkBefore.lastIndexOf("</object") < junkBefore.lastIndexOf("<object")
		|| junkBefore.lastIndexOf("</option") < junkBefore.lastIndexOf("<option") 
		|| junkBefore.lastIndexOf("</script") < junkBefore.lastIndexOf("<script") 
		|| junkBefore.lastIndexOf("</span") < junkBefore.lastIndexOf("<span class=\"o_tm_glossary\"") ) 
		continue;
		
	// Finally replace surround search text with glossary term
	newText += bodyText.substring(0, i) + getHighlightStartTag(glossaryMainTerm, searchTerm, occurrence) + bodyText.substr(i, searchTerm.length) + highlightEndTag;
	occurrence++;
	bodyText = bodyText.substr(i + searchTerm.length);
	lcBodyText = bodyText.toLowerCase();
	i = -1;
  }
  
  return newText;
}


/*
 * creates an identifier to use for highlighting 
 *
 * March 2009  Roman Haag, roman.haag@frentix.com
 */
function getHighlightStartTag(glossaryMainTerm, searchTerm, occurrence){
	glossaryMainTerm = glossaryMainTerm.replace(" ","+");
	searchTerm = searchTerm.replace(" ","+");
	var identifier = "gloss" + glossaryMainTerm + "" + searchTerm + "" + occurrence ;
	identifier = mainwindow.o_gloss_getUniqueTargetId(identifier);
	highlightStartTag = "<span class=\"o_tm_glossary\" id=\"" + identifier + "\">";

	//to later know, which ext-tooltips should be added.
	var tmpArr = new Array(glossaryMainTerm,searchTerm,occurrence);
	debug ? console.log("highlighting and push to foundTerms: " + glossaryMainTerm + ":" + searchTerm + ":" + occurrence): null;
	foundTerms.push(tmpArr);	
	return highlightStartTag;
}
