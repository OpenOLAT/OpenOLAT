(function() {
	// Load the OLAT translator.
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
	
	tinymce.create('org.olat.core.gui.components.form.flexible.impl.elements.richText.plugins.olatmovieviewer', {

		/**
		 * Returns information about the plugin as a name/value array.
		 * The current keys are longname, author, authorurl, infourl and version.
		 *
		 * @returns Name/value array containing information about the plugin.
		 * @type Array 
		 */
		getInfo : function() {
			return {
				longname : 'OlatMovieViewer',
				author : 'frentix GmbH',
				authorurl : 'http://www.frentix.com',
				infourl : 'http://www.frentix.com',
				version : '2.0'
			};
		},

		/**
		 * Not used, adButton used instead
		 */
		createControl : function(n, cm) {
			return null;
		},
	
		/**
		 * Initializes the plugin, this will be executed after the plugin has been created.
		 * This call is done before the editor instance has finished it's initialization so use the onInit event
		 * of the editor instance to intercept that event.
		 *
		 * @param {tinymce.Editor} ed Editor instance that the plugin is initialized in.
		 * @param {string} url Absolute URL to where the plugin is located.
		 */
		init : function(ed, url) {
			// Register buttons
			ed.addButton('olatmovieviewer', {
				title : translator.translate('olatmovieviewer.desc'),
				cmd : 'mceOlatMovieViewer',
				image : url + '/images/movieviewer.gif'
			});
			
			// Add a node change handler, selects the button in the UI when a image is selected
			ed.onNodeChange.add(function(ed, cm, n) {
				if (n == null) {
					return;
				} else {
					cm.setActive("olatmovieviewer", (n.nodeName == "IMG" && /mceItemOlatMovieViewer/.test(ed.dom.getAttrib(n, 'class'))));
				}
			});
			
			// Link toolbar buttons to dialogs
			ed.addCommand('mceOlatMovieViewer', function() {
				ed.windowManager.open({
					file : url + '/olatmovieviewer.htm',
					width : 630,
					height : 670,
					inline : 1
				}, {
					plugin_url : url
				});
			});

			// Load Content CSS upon initialization
			ed.onInit.add(function() {
			     if (ed.settings.content_css !== false) ed.dom.loadCSS(url + "/css/content.css");
			});
			
			/** 
             * This onPreProcess handler is used to convert the placeholder &lt;img&gt; tags to the
             * &lt;embed&gt; etc. tags when saving the document.
             */
			ed.onPreProcess.add(function(editor, obj) {
				// Find all IMGs of class "mceItemOlatMovieViewer"...
				tinymce.each(editor.dom.select("img.mceItemOlatMovieViewer", obj.node), function(node) {
					// ...read the movie settings out of the IMG's title attribute...
					var movieSettingsString = node.title;
					// ...clean up a bit...
					movieSettingsString = movieSettingsString.replace(/&(#39|apos);/g, "'");
					movieSettingsString = movieSettingsString.replace(/&#quot;/g, '"');
					var movieSettings;
					// ...parse the settings...
					try {
						movieSettings = eval("x={" + movieSettingsString + "}");
					} catch (exception) {
						movieSettings = {};
					}
					var playerNode = getPlayerHtmlNode(editor, movieSettings);
					editor.dom.replace(playerNode, node, false);
				});
			});
			
			//fallback for the old movies with settings in comments
			ed.onBeforeSetContent.add(function(editor, obj) {
				if(obj.content.indexOf('--omvs::') > 0) {
					var imgUrl = tinyMCE.activeEditor.getParam("olatmovieviewer_transparentImage");
					obj.content = obj.content.replace(/\n/gi, "");
					var widthMatch = obj.content.match(/(?:<!--omvs::.*?width:')([0-9]+)(?:'.*?<!--omve-->)/i);
					var width = ((widthMatch != null) && (widthMatch.length == 2)) ? parseInt(widthMatch[1]) : 320;
					var heightMatch = obj.content.match(/(?:<!--omvs::.*?height:')([0-9]+)(?:'.*?<!--omve-->)/i);
					var height = ((heightMatch != null) && (heightMatch.length == 2)) ? parseInt(heightMatch[1]) : 240;
					obj.content = obj.content.replace(/<!--omvs::(.*?)-->(.*?)<!--omve-->/gi, '<img class="mceItemOlatMovieViewer" alt="" src="' + imgUrl + '" title="$1" width="' + width + '" height="' + height + '"/>');
				}
			});
			
			/** 
             * This onSetContent handler is used to convert the comments to placeholder images (e.g. when loading).
             */
			ed.onSetContent.add(function(editor, obj) {
				// Get the URL of the transparent placeholder image
				var imgUrl = tinyMCE.activeEditor.getParam("olatmovieviewer_transparentImage");
				tinymce.each(editor.dom.select("div.olatFlashMovieViewer,span.olatFlashMovieViewer", obj.node), function(node) {
					// ...and for each of these, create an IMG...
					var movieSettingsString = parseBPlayerScript(editor,node.innerHTML);
					var movieSettings;
					try {
						movieSettings = eval("x={" + movieSettingsString + "}");
					} catch (exception) {
						movieSettings = {};
					}
					var imgNode = editor.dom.create("img", {id:movieSettings.domIdentity,name:movieSettings.domIdentity,"class":"mceItemOlatMovieViewer", src:imgUrl, title:movieSettingsString});
					//for ie8
					imgNode.width = typeof(movieSettings.width) == 'undefined' ? 320 : movieSettings.width;
					imgNode.height = typeof(movieSettings.height) == 'undefined' ? 240 : movieSettings.height;
					//  ...and replace the div by the new img.
					editor.dom.replace(imgNode, node, false);
			    });
			});
		}
	});
	
	function parseBPlayerScript(editor,script) {
		if(script == null || script == undefined) return '';
		var startMark = 'BPlayer.insertPlayer(';
		var start = script.indexOf(startMark);
		var end = script.indexOf(');');
		if(start < 0 || end < 0) return '';

		var playerOffsetHeight = editor.getParam("olatmovieviewer_playerOffsetHeight");
		var playerOffsetWidth = editor.getParam("olatmovieviewer_playerOffsetWidth");
		var params = script.substring(start + startMark.length,end);
		var settingsArr = params.split(',');
		var pl = 'domIdentity:' + settingsArr[1] + ',';
		pl += 'address:' + settingsArr[0] + ',';
		pl += 'streamer:' + settingsArr[7] + ',';
		pl += 'starttime:' + settingsArr[4] + ',';
		pl += 'autostart:' + settingsArr[8] + ',';
		pl += 'repeat:' + settingsArr[9] + ',';
		pl += 'controlbar:' + settingsArr[10] + ',';
		pl += 'provider:' + settingsArr[6] + ',';
		pl += 'width:' + (settingsArr[2] - playerOffsetWidth) + ',';
		pl += 'height:' + (settingsArr[3] - playerOffsetHeight);
		return pl;
	};
	
	//The video player code. Only one player per page supported.
	function getPlayerHtmlNode(editor,p) {
		var h = '', n, l = '';
		// player configuration
		var playerOffsetHeight = tinyMCE.activeEditor.getParam("olatmovieviewer_playerOffsetHeight");
		var playerOffsetWidth = tinyMCE.activeEditor.getParam("olatmovieviewer_playerOffsetWidth");
		var playerWidth = typeof(p.width) != "undefined" ? (parseInt(p.width) + parseInt(playerOffsetWidth))  : '';
		var playerHeight = typeof(p.height) != "undefined" ? (parseInt(p.height) + parseInt(playerOffsetHeight))  : '';
		var starttime = typeof(p.starttime) != "undefined" ? '"' + p.starttime + '"' : 0;
		var autostart = typeof(p.autostart) != "undefined" ? p.autostart : 'false';
		var repeat = typeof(p.repeat) != "undefined" ? p.repeat : 'false';
		var controlbar = typeof(p.controlbar) != "undefined" ? p.controlbar : 'true';
		var provider = typeof(p.provider) != "undefined" ? '"' + p.provider + '"' : 'undefined';
		var streamer = typeof(p.streamer) != "undefined" ? '"' + p.streamer + '"' : 'undefined';
		var domIdentity = typeof(p.domIdentity) != "undefined" ? p.domIdentity : 'olatFlashMovieViewer0';
		var playerScriptUrl = tinyMCE.activeEditor.getParam("olatmovieviewer_playerScript");

		var h = '<script src="' + playerScriptUrl + '" type="text/javascript"></script>';
		h += '<script type="text/javascript" defer="defer">';
		h += 'BPlayer.insertPlayer("' + p.address + '","' + domIdentity + '",' + playerWidth + ',' + playerHeight + ',' + starttime + ',0,' + provider + ',' + streamer +',' + autostart + ',' + repeat + ',' + controlbar + ');';
		h += '//</script>';
		var node = editor.dom.create("span", {id:domIdentity,name:domIdentity,"class":"olatFlashMovieViewer",style:'display:block;border:solid 1px #000; width:' + playerWidth + 'px; height:' + playerHeight + 'px;'},h);
		return node;
	};

	// Register plugin
	tinymce.PluginManager.add('olatmovieviewer', org.olat.core.gui.components.form.flexible.impl.elements.richText.plugins.olatmovieviewer);
})();


