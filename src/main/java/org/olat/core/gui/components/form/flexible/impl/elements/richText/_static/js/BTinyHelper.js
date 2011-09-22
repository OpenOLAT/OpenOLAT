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
		var domElem = $(domID);
		if (domElem && domElem.onchange) domElem.onchange();
	},
	
	// This methods adds event handlers to auto-hide the external toolbar 
	// when the curser leaves the editor area
	addAutohideExternalToolbarHandler : function (domID) {
		var tinyExtTB = $(domID + '_external');
		if (tinyExtTB) {
			Ext.get(domID + '_parent').hover( 
					function(){ /* do nothing, will be added alread on click events by tiny itself */ },
					function(){ tinymce.DOM.hide(domID + '_external'); }
			);
			tinyExtTB.setStyle({width : $(domID + '_tbl').getWidth()-2 + 'px'});
		}
		tinyExtTB = null; // Help GC, break circular reference;
	},
	
	// contains uris to open the media popup window
	editorMediaUris : new Hash(),
	
	// Current media browser callback field
	currentField : null,
	
	// Open link browser in new window. Only one browser window is supported at any time
	openLinkBrowser : function (formitemId, field_name, url, type, win) {
		BTinyHelper.currentField = win.document.forms[0].elements[field_name];	
		BTinyHelper.currentWindow = win;
		
		var currentMediaUrl = BTinyHelper.editorMediaUris.get(formitemId);
		var currentField = win.document.forms[0].elements[field_name];	
		o_openPopUp(currentMediaUrl + type + '?url=' + encodeURIComponent(url), "chooser", 800, 700, false);
	},

	// Write link from media chooser back to tiny and trigger media preview generation
	writeLinkSelectionToTiny : function (link) {		
		if (link != "") {
			BTinyHelper.currentField.value = link;
			// update image preview
			if (BTinyHelper.currentWindow.ImageDialog && BTinyHelper.currentWindow.ImageDialog.showPreviewImage) {
				BTinyHelper.currentWindow.ImageDialog.showPreviewImage(link);
			} else if (BTinyHelper.currentWindow.generatePreview) {
				BTinyHelper.currentWindow.generatePreview(link);
			}
		}
	},
	
	// Link converter callback: we use our own link callback because we have
	// - relative links: media and links relative to the root folder
	// - relative-absolute links: media that belong to the framework from the static dir
	// - absolute links: media an links to external sites
	linkConverter : function (url, node, on_save) {
		var orig = url + '';
		var editor = tinyMCE.activeEditor;
		var settings = editor.settings;
		if (!settings.convert_urls || (node && node.nodeName == 'LINK') || url.indexOf('file:') === 0) {
			// Don't convert link href since thats the CSS files that gets loaded into the editor also skip local file URLs
		} else if (settings.relative_urls) {
			// Convert to relative, but only if not a brasato framework URL. Relative links are removed by the XSS filter.
			if (url.indexOf('/') == 0 
				|| url.indexOf(settings.brasato_tiny_base_container_path) != -1 
				|| url.indexOf(o_info.uriprefix.replace(/auth/g,'url')) != -1
				|| (-1 < url.indexOf(o_info.uriprefix) < url.indexOf("/go?"))) {
				// Don't convert special brasato framework URL that are relative-absolute:
				// 1) /olat/raw/_noversion_/... or /olat/secstatic/...
				// 2) http://localhost/olat/classpath/62x/org.olat.core.gui.components.form.flexible.impl.elements.richText/js/tinymce/
				// 3) http://localhost/olat/url/RepositoryEntry/27361280/ (REST URL and permalinks)
				// 4) http://localhost/olat/auth/abc/go?xyz (old jump in URL's)
			} else {
				// convert to relative path using TinyMCE standard conversion
				url = editor.documentBaseURI.toRelative(url);
			}
		} else {
			// Convert to absolute
			url = editor.documentBaseURI.toAbsolute(url, settings.remove_script_host);			
		}

		return url;
	},

	// Current form dirty observers
	formDirtyObservers : new Hash(),

	// Stop form dirty observers that exist for this form and element
	stopFormDirtyObserver : function(formId, elementId) {
		var observerKey = formId + '-' + elementId;
		var existingExecutor = BTinyHelper.formDirtyObservers.get(observerKey);
		if (existingExecutor != null) {
			existingExecutor.stop();
			BTinyHelper.formDirtyObservers.unset(observerKey);
		}
	},	
	
	// The rich text element needs some special code to find out when the field is dirty. 
	// For this purpose an exector checks every second if the tiny editor is dirty. If so, 
	// the flexi form is triggered to be dirty.	
	// Make sure you called stopFormDirtyObserver() first to remove any old observers
	startFormDirtyObserver : function(formId, elementId) {
		var observerKey = formId + '-' + elementId;
		// Check for dirtyness and mark buttons accordingly, each second
		var newExecutor = new PeriodicalExecuter(function(executor) {
			// first check if the html editor still exists on this page, otherwhise stop executing this code
			var elem = $(elementId);
			if (!elem) {
				executor.stop();
				BTinyHelper.formDirtyObservers.unset(observerKey);
				return;
			}
			if (tinyMCE && tinyMCE.activeEditor) {
				if (tinyMCE.activeEditor.isDirty()) {
					setFlexiFormDirty(formId);
				}
			}		
			elem = null; // help GC
		},0.5);	
		BTinyHelper.formDirtyObservers.set(observerKey, newExecutor);
	},
	
	// Remove the editor instance for the given DOM node ID if such an editor exists.
	// Remove all event handlers and release the memory
	removeEditorInstance : function (elementId) {
		if (tinyMCE) {
			var oldE = tinyMCE.get(elementId);
			if (oldE != null) { 
				try { 					
					// first try to remove and cleanup editor instance itself
					// in some situations this call fails, we ignore this cases, no big deal.
					oldE.remove(); 
				} catch(e) {
					// IE (of course) has some issues here, need to silently catch those 
					//console.log('could not removeEditorInstance::' + e.message)
				}
				try { 					
					// second remove editor instance from tiny editorManager 
					tinyMCE.remove(oldE); 
				} catch(e) {
					// IE (of course) has some issues here, need to silently catch those 
					//console.log('could not removeEditorInstance::' + e.message)
				}
			}
			oldE = null;
		}
	}
	
}
