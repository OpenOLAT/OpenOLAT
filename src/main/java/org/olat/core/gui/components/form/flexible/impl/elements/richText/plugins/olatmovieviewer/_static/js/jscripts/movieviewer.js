// Load OLAT translator
function findMainWindow(win) {
	if (win.b_jsTranslatorFactory) return win;
	else if (win.opener) return findMainWindow(opener);
	else return null;
}
var mainWin = findMainWindow(window);
var translator;
if (mainWin) {
	translator = mainWin.b_jsTranslatorFactory.getTranslator(mainWin.o_info.locale, 'org.olat.core.gui.components.form.flexible.impl.elements.richText.plugins.olatmovieviewer')	
} else {
	// implement dummy-translator
	translator = {	translate : function(key) { return key; } }
}

var oldWidth, oldHeight;

// OlatMovieViewer dialog
var MovieViewerDialog = {
	
		// Dialog initialization
	init: function() {
	
		var pl = "", f, val;
		var fe, i;
		
		tinyMCEPopup.resizeToInnerSize();
		f = document.forms[0];

		// get data from preselected item
		tinyMCEPopup.restoreSelection();
		fe = tinyMCEPopup.editor.selection.getNode();
		
		if (/mceItemOlatMovieViewer/.test(tinyMCEPopup.editor.dom.getAttrib(fe, "class"))) {
			pl = "x={" + tinyMCEPopup.editor.dom.getAttrib(fe, "title") + "};";
			document.forms[0].insert.value = translator.translate("olatmovieviewer.insert");  
		}
		
		if (pl != "") {
			// Setup form from preselected item
			pl = eval(pl);
			setStr(pl, null, 'domIdentity');
			setStr(pl, null, 'address');
			setStr(pl, null, 'streamer');
			setStr(pl, null, 'starttime');
			setBool(pl, null, 'autostart');
			setBool(pl, null, 'repeat');
			setBool(pl, null, 'controlbar');
			setStr(pl, null, 'provider');
			setStr(pl, null, 'width');
			setStr(pl, null, 'height');
			
			if ((val = tinyMCEPopup.editor.dom.getAttrib(fe, "width")) != "")
				pl.width = f.width.value = val;
			
			if ((val = tinyMCEPopup.editor.dom.getAttrib(fe, "height")) != "")
				pl.height = f.height.value = val;
			
			oldWidth = pl.width ? parseInt(pl.width) : 0;
			oldHeight = pl.height ? parseInt(pl.height) : 0;
		} else {
			// Setup from with default values
			oldWidth = oldHeight = 0;
			
			var count = 0;
			var domIdentity = "olatFlashMovieViewer";
			var placeHolders = tinyMCEPopup.editor.dom.select("img.mceItemOlatMovieViewer");
			do {
				domIdentity = "olatFlashMovieViewer" + (count++);
			} while(domIdInUse(domIdentity,placeHolders));

			var defaultPl = "x={domIdentity:'" + domIdentity + "',address:'',starttime:'00:00:00.000',autostart:false,repeat:false,controlbar:true};";
			defaultPl = eval(defaultPl);
			setStr(defaultPl, null, 'domIdentity');
			setStr(defaultPl, null, 'address');
			setStr(defaultPl, null, 'starttime');
			setBool(defaultPl, null, 'autostart');
			setBool(defaultPl, null, 'repeat');
			setBool(defaultPl, null, 'controlbar');
		}
		
		// Setup browse button	
		document.getElementById('srcbrowsercontainer').innerHTML = getBrowserHTML('srcbrowser','address','flashplayer','theme_advanced_image');
		
		TinyMCE_EditableSelects.init();
		window.setTimeout("generatePreview()", 500);
	}, 
	
	// Content insertion
	insert: function() {
		var fe, f = document.forms[0], h;
		
		if (!AutoValidator.validate(f)) {
			alert(translator.translate("olatmovieviewer.invalid_date"));
			return false;
		}
		
		f.width.value = f.width.value == "" ? 100 : f.width.value;
		f.height.value = f.height.value == "" ? 100 : f.height.value;
		
		fe = tinyMCEPopup.editor.selection.getNode();
		if (fe != null && fe != "undefined" && /mceItemOlatMovieViewer/.test(tinyMCEPopup.editor.dom.getAttrib(fe, 'class'))) {
			// change values from existing object
			if (fe.width != f.width.value || fe.height != f.width.height)
				tinyMCEPopup.editor.execCommand("mceRepaint");
			
			fe.title = serializeParameters();
			fe.width = f.width.value;
			fe.height = f.height.value;
			fe.style.width = f.width.value + (f.width.value.indexOf('%') == -1 ? 'px' : '');
			fe.style.height = f.height.value + (f.height.value.indexOf('%') == -1 ? 'px' : '');
		} else {
			// add new object
			h = '<img class="mceItemOlatMovieViewer" src="' + tinyMCEPopup.editor.getParam("olatmovieviewer_transparentImage") + '"';
			h += ' title="' + serializeParameters() + '"';
			h += ' width="' + f.width.value + '"';
			h += ' height="' + f.height.value + '"';
			h += ' />';
			tinyMCEPopup.editor.execCommand("mceInsertContent", false, h);
		}
		tinyMCEPopup.restoreSelection();
		tinyMCEPopup.close();
	}
}

function domIdInUse(domIdentity,placeHolders) {
	for(var i=0; i<placeHolders.length; i++) {
		if(placeHolders[i].title != undefined && placeHolders[i].title.indexOf(domIdentity) > 0) {
			return true;
		}
	}
	return false;
}

// Removes leading whitespaces
function LTrim( value ) {
	var re = /\s*((\S+\s*)*)/;
	return value.replace(re, "$1");	
}
// Removes ending whitespaces
function RTrim( value ) {
	var re = /((\s*\S+)*)\s*/;
	return value.replace(re, "$1");
}
// Removes leading and ending whitespaces
function trim( value ) {
	return LTrim(RTrim(value));
}

function serializeParameters() {
	var d = document, f = d.forms[0], s = '';
	s += getStr(null, 'domIdentity');
	s += getStr(null, 'address');
	s += getStr(null, 'streamer');
	s += getStr(null, 'starttime');
	s += getBool(null, 'autostart');
	s += getBool(null, 'repeat');
	s += getBool(null, 'controlbar');
	s += getStr(null, 'provider');
	s += getStr(null, 'width');
	s += getStr(null, 'height');
	s = s.length > 0 ? s.substring(0, s.length - 1) : s;
	return s;
}

function _getEmbed(p) {
	// player configuration
	var playerSrc = tinyMCEPopup.editor.getParam("olatmovieviewer_movieViewerUrl");
	var playerOffsetHeight = tinyMCEPopup.editor.getParam("olatmovieviewer_playerOffsetHeight");
	var playerOffsetWidth = tinyMCEPopup.editor.getParam("olatmovieviewer_playerOffsetWidth");
	var playerWidth = typeof(p.width) != "undefined" ? (parseInt(p.width) + parseInt(playerOffsetWidth))  : '320';
	var playerHeight = typeof(p.height) != "undefined" ? (parseInt(p.height) + parseInt(playerOffsetHeight))  : '240';
	var start = typeof(p.starttime) != "undefined" ? p.starttime : "00:00:00.000";
	var autostart = typeof(p.autostart) != "undefined" ? p.autostart : false;
	var repeat = typeof(p.repeat) != "undefined" ? p.repeat : false;
	var controlbar = typeof(p.controlbar) != "undefined" ? p.controlbar : true;
	var provider = typeof(p.provider) != "undefined" ? p.provider : undefined;
	var streamer = typeof(p.streamer) != "undefined" ? p.streamer : undefined;
	var domIdentity = typeof(p.domIdentity) != "undefined" ? p.domIdentity : 'olatFlashMovieViewer0';

	//scale the video if to big to not overlap the buttons
	var maxHeight = 400;
	var maxWidth = 560;
	if(playerHeight > maxHeight || playerWidth > maxWidth) {
		var thumbRatio = maxWidth / maxHeight;
	    var imageRatio = playerWidth / playerHeight;
	    if (thumbRatio < imageRatio) {
	    	playerHeight = (maxWidth / imageRatio);
	    	playerWidth = maxWidth;
	    }  else {
	    	playerWidth = (maxHeight * imageRatio);
	    	playerHeight = maxHeight;
	    }
	}
	
	var videoUrl = p.address
	if(p.address != undefined) {
		if(p.address.indexOf('://') < 0 && ((provider != "rtmp" && provider != "http") ||
			((provider == "rtmp" || provider == "http") && (streamer == undefined || streamer.length == 0)))) {
			videoUrl = tinyMCEPopup.editor.documentBaseURI.toAbsolute(p.address);
		}
	}
	
	if(p.address != undefined && p.address != null && p.address.length > 0) {
		BPlayer.insertPlayer(videoUrl,'prev_container',playerWidth,playerHeight,start,0,provider,streamer,autostart,repeat,controlbar);
	}
}

function generatePreview() {
	var attribs = serializeParameters();
	var pl = eval("x={" + attribs + "}");
	// create preview using generation code form editor
	_getEmbed(pl);
	
	var streaming = document.getElementById('streamer_line');
	var address = document.getElementById('address');
	var browserLink = document.getElementById('srcbrowser_link');
	if(pl.provider == "rtmp" || pl.provider == "http") {
		streaming.style.display = "";
		browserLink.style.display = "none";
		address.style.width = '300px';
	} else {
		streaming.style.display = "none";
		browserLink.style.display = "";
		address.style.width = '280px';
	}
	return;
}

tinyMCEPopup.onInit.add(MovieViewerDialog.init, MovieViewerDialog);