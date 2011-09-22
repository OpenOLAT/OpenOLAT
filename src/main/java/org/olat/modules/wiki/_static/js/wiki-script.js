var clientPC = navigator.userAgent.toLowerCase(); // Get client info
var is_gecko = ((clientPC.indexOf('gecko')!=-1) && (clientPC.indexOf('spoofer')==-1)
                && (clientPC.indexOf('khtml') == -1) && (clientPC.indexOf('netscape/7.0')==-1));
var is_safari = ((clientPC.indexOf('applewebkit')!=-1) && (clientPC.indexOf('spoofer')==-1));
var is_khtml = (navigator.vendor == 'KDE' || ( document.childNodes && !document.all && !navigator.taintEnabled ));
if (clientPC.indexOf('opera') != -1) {
	var is_opera = true;
	var is_opera_preseven = (window.opera && !document.childNodes);
	var is_opera_seven = (window.opera && document.childNodes);
}


// apply tagOpen/tagClose to selection in textarea,
// use sampleText instead of selection if there is none
// copied and adapted from phpBB
function insertTags(tagOpen, tagClose, sampleText) {
	if (document.editform)
		var txtarea = document.editform.wpTextbox1;
	else {
		// some alternate form? take the first one we can find
		var areas = document.getElementsByTagName('textarea');
		var txtarea = areas[0];
	}

	// IE
	if (document.selection  && !is_gecko) {
		var theSelection = document.selection.createRange().text;
		if (!theSelection)
			theSelection=sampleText;
		txtarea.focus();
		if (theSelection.charAt(theSelection.length - 1) == " ") { // exclude ending space char, if any
			theSelection = theSelection.substring(0, theSelection.length - 1);
			document.selection.createRange().text = tagOpen + theSelection + tagClose + " ";
		} else {
			document.selection.createRange().text = tagOpen + theSelection + tagClose;
		}

	// Mozilla
	} else if(txtarea.selectionStart || txtarea.selectionStart == '0') {
		var replaced = false;
		var startPos = txtarea.selectionStart;
		var endPos = txtarea.selectionEnd;
		if (endPos-startPos)
			replaced = true;
		var scrollTop = txtarea.scrollTop;
		var myText = (txtarea.value).substring(startPos, endPos);
		if (!myText)
			myText=sampleText;
		if (myText.charAt(myText.length - 1) == " ") { // exclude ending space char, if any
			subst = tagOpen + myText.substring(0, (myText.length - 1)) + tagClose + " ";
		} else {
			subst = tagOpen + myText + tagClose;
		}
		txtarea.value = txtarea.value.substring(0, startPos) + subst +
			txtarea.value.substring(endPos, txtarea.value.length);
		txtarea.focus();
		//set new selection
		if (replaced) {
			var cPos = startPos+(tagOpen.length+myText.length+tagClose.length);
			txtarea.selectionStart = cPos;
			txtarea.selectionEnd = cPos;
		} else {
			txtarea.selectionStart = startPos+tagOpen.length;
			txtarea.selectionEnd = startPos+tagOpen.length+myText.length;
		}
		txtarea.scrollTop = scrollTop;

	// All other browsers get no toolbar.
	// There was previously support for a crippled "help"
	// bar, but that caused more problems than it solved.
	}
	// reposition cursor if possible
	if (txtarea.createTextRange)
		txtarea.caretPos = document.selection.createRange().duplicate();
}

function insertMediaTag(mediaName){
	var name = mediaName.toLowerCase();
	if(name.indexOf("jpeg") != -1 || name.indexOf("jpg") != -1 || name.indexOf("gif") != -1 || name.indexOf("png") != -1){
		insertTags('[[Image:',']]',mediaName);
	} else {
		insertTags('[[Media:',']]',mediaName);
	}
}

var o_search_word = null;
function searchWikiArticle(linkElement, textValue) {
	if(textValue.substring(0, 2).indexOf("..") != -1) return false;
	if(textValue.indexOf("/") != -1) return false;
	if(o_search_word == null) {
		o_search_word = textValue;
		var attr = linkElement.getAttribute("href");
		linkElement.setAttribute("href", attr+textValue);
		return true;
	}
	return false;
}