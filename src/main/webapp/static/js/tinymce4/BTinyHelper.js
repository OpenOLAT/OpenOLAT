/*
 * Helper methods to glue the TinyMCE HTML editor to the flexi form
 * 22.04.2009
 * Florian Gnägi, www.frentix.com
 */
var BTinyHelper = {
	// Tiny on-change handler that delegates the change event to the 
	// flexi form text area on-change handler
	triggerOnChangeOnFormElement : function (tinyObj) {
		var domID = tinyObj.id;
		var domElem = jQuery('#' + domID);
		if (domElem && domElem.onchange) domElem.onchange();
	},
	
	triggerOnChange : function (domID) {
		var domElem = jQuery('#' + domID);
		if (domElem && domElem.onchange) {
			domElem.onchange();
		}
	},
	
	// contains uris to open the media popup window
	editorMediaUris : new Hashtable(),
	// Current media browser callback field
	currentField : null,
	currentFieldId: null,
	currentCallback: null,
	
	// Open link browser in new window. Only one browser window is supported at any time
	openLinkBrowser : function (editorId, callback, value, meta) {
		if(callback != null) {
			BTinyHelper.currentField = meta.fieldname;	
			BTinyHelper.currentFieldId = meta.fieldname;
			BTinyHelper.currentCallback = callback;
			var ffxhrevent = tinymce.activeEditor.settings.ffxhrevent;
			o_ffXHREvent(ffxhrevent.formNam, ffxhrevent.dispIdField, ffxhrevent.dispId, ffxhrevent.eventIdField, '2', false, false, true, 'browser', meta.filetype);
		}
	},

	// Write link from media chooser back to tiny and trigger media preview generation
	writeLinkSelectionToTiny : function (link, width, height) {
		if (link != "") {
			try {
				var infos = { "link" : link, "width": width, "height": height };
				BTinyHelper.currentCallback(link, infos);
			} catch(e) {
				if(window.console) console.log(e);
			}
		}
	},
	
	// Link converter callback: we use our own link callback because we have
	// - relative links: media and links relative to the root folder
	// - relative-absolute links: media that belong to the framework from the static dir
	// - absolute links: media an links to external sites
	linkConverter : function (url, node, on_save, name) {
		var editor = tinymce.activeEditor;
		if(editor === undefined || editor == null) {
			//do nothing
		} else {
			var settings = editor.settings;
			if (!settings.convert_urls || (node && node.nodeName == 'LINK') || url.indexOf('file:') === 0) {
				// Don't convert link href since thats the CSS files that gets loaded into the editor also skip local file URLs
			} else if (settings.relative_urls) {
				// Convert to relative, but only if not a brasato framework URL. Relative links are removed by the XSS filter.
				if (url.indexOf('/') == 0
					|| url.indexOf(o_info.uriprefix.replace(/auth/g,'url')) != -1
					|| (-1 < url.indexOf(o_info.uriprefix) < url.indexOf("/go?"))) {
					// Don't convert special brasato framework URL that are relative-absolute:
					// 1) /olat/raw/_noversion_/... or /olat/secstatic/...
					// 2) http://localhost/olat/classpath/62x/org.olat.core.gui.components.form.flexible.impl.elements.richText/js/tinymce/
					// 3) http://localhost/olat/url/RepositoryEntry/27361280/ (REST URL and permalinks)
					// 4) http://localhost/olat/auth/abc/go?xyz (old jump in URL's)
				} else if(url.indexOf('/m/') == 0) {
					// convert media
					var index = url.indexOf('/',4);
					url = url.substring(index + 1, url.lengths);//remove /m/{32 characters of mapper id}/
				} else if(url.indexOf('http://') == 0 || url.indexOf('https://') == 0) {
					url = editor.documentBaseURI.toAbsolute(url, true);
				}
			} else {
				// Convert to absolute
				url = editor.documentBaseURI.toAbsolute(url, settings.remove_script_host);			
			}
		}

		return url;
	},

	// Current form dirty observers
	formDirtyObservers : new Hashtable(),

	// Stop form dirty observers that exist for this form and element
	stopFormDirtyObserver : function(formId, elementId) {
		var observerKey = formId + '-' + elementId;
		var existingExecutor = BTinyHelper.formDirtyObservers.get(observerKey);
		if (existingExecutor != null) {
			existingExecutor.cancel();
			BTinyHelper.formDirtyObservers.remove(observerKey);
		}
	},	
	
	// The rich text element needs some special code to find out when the field is dirty. 
	// For this purpose an exector checks every second if the tiny editor is dirty. If so, 
	// the flexi form is triggered to be dirty.	
	// Make sure you called stopFormDirtyObserver() first to remove any old observers
	startFormDirtyObserver : function(formId, elementId) {
		var observerKey = formId + '-' + elementId;
		if(BTinyHelper.formDirtyObservers.containsKey(observerKey)) return;
		
		// Check for dirtyness and mark buttons accordingly, each second
		var newExecutor = jQuery.periodic({period: 500, decay:1.0, max_period: Number.MAX_VALUE}, function() {
			// first check if the html editor still exists on this page, otherwhise stop executing this code
			var elem = jQuery('#' + elementId);
			if (elem.length == 0) {
				newExecutor.cancel();
				BTinyHelper.formDirtyObservers.remove(observerKey);
			} else if (tinymce != null && tinymce.activeEditor != null && tinymce.activeEditor.initialized) {
				if (tinymce.activeEditor.isDirty()) {
					setFlexiFormDirty(formId);
				}
			}		
			elem = null; // help GC
		});	
		BTinyHelper.formDirtyObservers.put(observerKey, newExecutor);
	},
	
	// Remove the editor instance for the given DOM node ID if such an editor exists.
	// Remove all event handlers and release the memory
	removeEditorInstance : function (elementId, cmd) {
		if (tinymce) {
			try {
				tinymce.remove('#' + elementId);
			} catch(e) {
				// IE (of course) has some issues here, need to silently catch those 
			}
		}
	}
}
