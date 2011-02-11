/**
 * OLAT custom smileys plugin that uses css classes rather than hardcoded image
 * urls to support the OLAT theaming mechanism.
 * Most of the code is based on the standard smileys plugin that comes bundled
 * with TinyMCE
 * 
 * 18.06.2009 timo.wuersch@frentix.com 
 */
(function() {
	// Load OLAT translator
	function findMainWindow(win) {
		if (win.b_jsTranslatorFactory) return win;
		else if (win.opener) return findMainWindow(opener);
		else return null;
	}
	var mainWin = findMainWindow(window);
	var translator;
	if (mainWin) {
		translator = mainWin.b_jsTranslatorFactory.getTranslator(mainWin.o_info.locale, 'org.olat.core.gui.components.form.flexible.impl.elements.richText.plugins.olatsmileys')	
	} else {
		// implement dummy-translator
		translator = {	translate : function(key) { return key; } }
	}

	
	// Create plugin object
	tinymce.create('org.olat.core.gui.components.form.flexible.impl.elements.richText.plugins.olatsmileys', {
		init : function(ed, url) {
		
			// Register commands
			ed.addCommand('mceSmileys', function() {
				
				// Open dialog when button gets clicked
				ed.windowManager.open({
					file : url + '/smileys.htm',
					width : 250,
					height : 160,
					inline : 1
				}, {
					plugin_url : url
				});
			});

			// Register button
			ed.addButton('olatsmileys', {
                title : translator.translate('olatsmileys.desc'),
                cmd : 'mceSmileys',
                image : url + '/img/smiley-smile.gif'
            });
		},

		// Plugin info function
		getInfo : function() {
			return {
				longname : 'Olat Smileys',
				author : 'frentix GmbH',
				authorurl : 'http://www.frentix.com',
				infourl : 'http://www.frentix.com',
				version : "1.0"
			};
		}
	});

	// Register plugin
	tinymce.PluginManager.add('olatsmileys', org.olat.core.gui.components.form.flexible.impl.elements.richText.plugins.olatsmileys);
})();
