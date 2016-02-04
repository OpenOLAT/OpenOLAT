/**
 * OLAT custom smileys plugin that uses css classes rather than hardcoded image
 * urls to support the OLAT theaming mechanism.
 * Most of the code is based on the standard smileys plugin that comes bundled
 * with TinyMCE
 * 
 * 18.06.2009 timo.wuersch@frentix.com 
 */
(function() {

	// Create plugin object
	tinymce.create('org.olat.core.gui.components.form.flexible.impl.elements.richText.plugins.olatsmileys', {
		init : function(ed, url) {

			var cachedTrans, cachedCoreTrans;
			var cachedHelp;
			
			// Load the OLAT translator.
			function translator() {	
				if(cachedTrans) return cachedTrans;
				var mainWin = o_getMainWin();
				if (mainWin) {
					cachedTrans = jQuery(document).ooTranslator().getTranslator(mainWin.o_info.locale, 'org.olat.core.gui.components.form.flexible.impl.elements.richText.plugins.olatsmileys');
				} else {
					cachedTrans = {	translate : function(key) { return key; } }
				}
				return cachedTrans;
			}
			function coreTranslator() {	
				if(cachedCoreTrans) return cachedCoreTrans;
				var mainWin = o_getMainWin();
				if (mainWin) {
					cachedCoreTrans = jQuery(document).ooTranslator().getTranslator(mainWin.o_info.locale, 'org.olat.core');
				} else {
					cachedCoreTrans = {	translate : function(key) { return key; } }
				}
				return cachedCoreTrans;
			}
			
			function getHtml() {
				var smileyNames = [
				  ["smile","sad","blushing","confused","cool","cry"],
				  ["devil","grin","kiss","ohoh","angry","sick"],
				  ["angel","tongue","ugly","weird","wink","worried"]
				];

				var transparentImg = ed.getParam("olatsmileys_transparentImage");
				var emoticonsHtml = '<table border="0" cellspacing="4" cellpadding="4" id="smileystable">';

				for (var row=0; row<smileyNames.length; row++) {
					emoticonsHtml += "<tr>";
					for (var col=0; col<smileyNames[row].length; col++) {
						var n = smileyNames[row][col];
						// use OLAT translator for OALT image
						var altText = translator().translate('olatsmileys.icon.' + n);
						emoticonsHtml += "<td><a href='#'>";
						emoticonsHtml += "<img class='o_emoticons_" + n + "' src='" + transparentImg +"' width='18' height='18' border='0' ";
						emoticonsHtml += "alt='" + n + "' title='" + altText + "' /></a></td>";
					}
					emoticonsHtml += "</tr>";
				}
				
				emoticonsHtml += '</table>';
				return emoticonsHtml;
			}
		
			// Register button
			ed.addButton('olatsmileys', {
                title : 'Smileys',
                image : url + '/img/smiley-smile.gif',
                type: 'panelbutton',
        		popoverAlign: 'bc-tl',
                panel: {
        			autohide: true,
        			html: getHtml,
        			onclick: function(e) {
        				var type = jQuery(e.target).attr('class');
        				if (type) {
        					var tag = '<img src="' + ed.getParam("olatsmileys_transparentImage") + '" class="' + type + '">';
        					ed.insertContent(tag);
        					this.hide();
        				}
        			}
        		},
        		tooltip: 'Smileys'
            });
			
			// Register button
			ed.addMenuItem('olatsmileys', {
                text : 'Smileys',
                image : url + '/img/smiley-smile.gif',
                context: 'insert',
                menu: [{
					type: 'container',
					html: getHtml(),
					onclick: function(e) {
        				var type = jQuery(e.target).attr('class');
        				if (type) {
        					var tag = '<img src="' + ed.getParam("olatsmileys_transparentImage") + '" class="' + type + '">';
        					ed.insertContent(tag);
        					this.parent().cancel();//close parent menu
        				}
        			}
        		}]
            });
		},

		// Plugin info function
		getInfo : function() {
			return {
				longname : 'OpenOLAT Smileys',
				author : 'frentix GmbH',
				authorurl : 'http://www.frentix.com',
				infourl : 'http://www.frentix.com',
				version : "1.1.2"
			};
		}
	});

	// Register plugin
	tinymce.PluginManager.add('olatsmileys', org.olat.core.gui.components.form.flexible.impl.elements.richText.plugins.olatsmileys);
})();
