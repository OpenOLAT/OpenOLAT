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
	if (isExecuting) return;
	
	isExecuting = true;
	workedOnDom.push(domId);
	setLastActiveGlossary(glossaryId);
	try {
		markerArray = new Array();
		markerArray = eval(jQuery(document).data("o_glossaries")[glossaryId]);
		// do the highlighting on the given dom element
		o_tm_doHighlightAll(document, markerArray, domId);
	} catch (e) {
	  	// catch any exception that might happen and do nothing. just in case
	  	// something unexpected happens, make sure the text marker code does not break
	  	// any other javascript code
	}
	isExecuting = false;
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
	if (isHighlighting) return;
	
	isHighlighting = true;
	try {
		var searchNode;
		if (domId == null || domId == "null" || domId == "") {
	  		// search over whole body
			searchNode = currentDocument.getElementsByTagName("body")[0];
		} else {
	  		// search only in given dom ID
	   		searchNode = jQuery('#' + domId);
	   		if(searchNode && searchNode.length > 0) {
	   			searchNode = searchNode[0];
	   		}
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
		  	searchNode.innerHTML = searchText;  	
		  	for (var j=0; j < foundTerms.length; j++){
		  		var glossaryMainTerm = foundTerms[j][0];
		  		var highlightString = foundTerms[j][1];
		  		var occurrence = foundTerms[j][2];
		   		o_tm_addExtToolTip(glossaryMainTerm,highlightString,occurrence);			
		  	}
	  	}  
	} catch(e) {
	  	// catch any exception that might happen and do nothing. just in case
	  	// something unexpected happens, make sure the text marker code does not break
	  	// any other javascript code
	} finally {
		isHighlighting = false;
	}
}
 
/**
 * creates tooltips for each occurrence of a found term.
 * has to be done after parsing document to prevent adding tips multiple times. 
 *
 * March 2009  Roman Haag, roman.haag@frentix.com
 */
function o_tm_addExtToolTip(glossaryMainTerm, highlightString, occurrence){
	try {
		var mapperPath = o_gloss_getDefinitionMapperPath() + "/" + o_gloss_getGlossaryId() + '/';
		var targetId = 'gloss' + glossaryMainTerm + '' + highlightString + '' + occurrence;
		targetId = mainwindow.o_gloss_getUniqueTargetId(targetId);
		
		//prevent adding tip twice or more!
		if (workedOnSpans.indexOf(targetId) == -1){
			workedOnSpans.push(targetId);
			
			var targetChk = jQuery('#' + targetId);
			if (targetChk) {
				var glossUrl = mapperPath + glossaryMainTerm + '.html';
				targetChk.tooltip({
					//bootstrap tooltip
					html: true,
					container:'body',
					placement: function() {
						var inIframe = (window.self !== window.top);
						if(inIframe) {
							var target = jQuery('#' + targetId);
							var offset = target.offset();
							var top = (offset.top < 0 ? 0 : offset.top);
							var body = document.body,
								html = document.documentElement;
							var docHeight = Math.max( body.scrollHeight, body.offsetHeight, html.clientHeight, html.scrollHeight, html.offsetHeight );
							return (docHeight / 2.0) < top ? 'top' : 'bottom';
						}
						return 'auto';
					},
					title: function() {
				        var elem = jQuery(this);
						var glossaryContentsBase64 = elem.data('glossaryContentsBase64');
						if (glossaryContentsBase64) {
							return Base64.decode(glossaryContentsBase64);
						}
				        jQuery.ajax(glossUrl).always(function(data, textStatus, jqXHR) {
				        	if (data != null && data !== '' && !glossaryContentsBase64) {
				        		jQuery('.tooltip').remove();
								try {
									data = Base64.encode(data);
								} catch (error) {
									console.error(error);
								}
								elem.data('glossaryContentsBase64', data);
				        		var tool = elem.tooltip('show');
				        		tool.data('bs.tooltip').tip().addClass('o_gloss_tooltip');
				        	}
				         });
				    }
				});
		    }
		}
    } catch(e) {
    	//console.log("error: " + e);
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
  var highlightEndTag  = "</span>";

  // find all occurences of the search term in the given text,
  // and add some "highlight" tags to them (we're not using a
  // regular expression search, because we want to filter out
  // matches that occur within HTML tags and script blocks, so
  // we have to do a little extra validation)
  var newText = "";
  var junkBefore = "";
  var ch = "";
  var i = -1;
  var lcSearchTerm = searchTerm.toLowerCase();
  var lcBodyText = bodyText.toLowerCase();
  
  var occurrence = 0;
  // prevent highlighting in buggy IE when a movieplayer is used or in some TinyMCE-cases
  // see OLAT-5447 for more infos
  if(bodyText.indexOf('BGlossarIgnore') > 0  || (navigator.userAgent.indexOf('MSIE') && bodyText.indexOf('olatFlashMovieViewer') > 0)) {
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
		ch = lcBodyText.charAt(i-1);	
		if ( ! (ch == " " || ch == "\t" || ch == "\n" || ch == "\r" || ch == "\v" || ch == "\f" 
			|| ch == ">" || ch == "\"" || ch == "'" || ch == "`" || ch == "(" || ch == "["  || ch == "{"  || ch == "-") )		
			continue;
	}

	if (lcBodyText.length > i + searchTerm.length) {
		// check character right after the the search term
		ch = lcBodyText.charAt(i + searchTerm.length);
		if ( ! (ch == " " || ch == "\t" || ch == "\n" || ch == "\r" || ch == "\v" || ch == "\f" 
			|| ch == "," || ch == "."  || ch == "!" || ch == "?"  || ch == ":" || ch == ";"  
			|| ch == "<" || ch == "\"" || ch == "'" || ch == "`" || ch == ")" || ch == "]"  || ch == "}"  || ch == "-" ) )
			continue;
	}
		
	// Do not mark text within textarea, form input option or javascript elements.
	// Prevent to highlight twice, by looking after highlight-tags
	junkBefore = lcBodyText.substring(0,i);
	if ( junkBefore.lastIndexOf("</textarea") < junkBefore.lastIndexOf("<textarea")
		//only a because aside, abbr, adress tags breaks the glossary
		|| junkBefore.lastIndexOf("</a") < junkBefore.lastIndexOf("<a")
		|| junkBefore.lastIndexOf("</button") < junkBefore.lastIndexOf("<button")
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
	highlightStartTag = "<span class='o_tm_glossary' id='" + identifier + "'>";

	//to later know, which ext-tooltips should be added.
	var tmpArr = new Array(glossaryMainTerm,searchTerm,occurrence);
	debug ? console.log("highlighting and push to foundTerms: " + glossaryMainTerm + ":" + searchTerm + ":" + occurrence): null;
	foundTerms.push(tmpArr);	
	return highlightStartTag;
}
