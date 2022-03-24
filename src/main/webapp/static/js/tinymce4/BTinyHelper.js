/*
 * Helper methods to glue the TinyMCE HTML editor to the flexi form
 * 22.04.2009
 * Florian Gnägi, www.frentix.com
 */
var BTinyHelper = {
	
	// Current media browser callback field
	currentCallback: null,
	
	// Open link browser in new window. Only one browser window is supported at any time
	openLinkBrowser : function (editorId, callback, value, meta) {
		if(callback != null) {
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
	}
}
