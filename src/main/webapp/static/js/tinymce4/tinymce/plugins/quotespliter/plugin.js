/**
 * 
 * 23.02.2010 stephane.rosse@frentix.com 
 */
(function() {
	// Create plugin object
	tinymce.create('org.olat.core.gui.components.form.flexible.impl.elements.richText.plugins.quotespliter', {
		init : function(ed, url) {
			// Add a split comment

			ed.on('KeyDown', function(e) {
				if (e == null || e.keyCode != 13 || !ed.selection.isCollapsed()) return;
				var focusEl = ed.selection.getNode();
				if(focusEl == null) return;

				if(detectQuote(focusEl)) {
					tinymce.dom.Event.cancel(e);
					split(focusEl);
				}
				
				function detectQuote(el) {
					var divQuote = ed.dom.getParent(el, 'DIV');
					var blockquote = ed.dom.getParent(el, 'BLOCKQUOTE');
					if(divQuote && blockquote && (divQuote.className == "b_quote_wrapper" || divQuote.className == "o_quote_wrapper")) {
						return true;
					}
					return false;
				}
				
				function split(focusEl) {
					//copy quote
					var parents = ed.dom.getParents(focusEl);
					var endQuote = '';
					var newQuote = '';
					for(var i=0; i<parents.length; i++) {
						var parent = parents[i];
						endQuote += '</' + parent.nodeName + '>'

						if(parent.className == "b_quote_wrapper" || parent.className == "o_quote_wrapper") {
							var quoteWrapper = '<div class="o_quote_wrapper"><div class="o_quote_author mceNonEditable">';
							for(var j=0; j<parent.childNodes.length; j++) {
								if(parent.childNodes[j].className == "o_quote_author mceNonEditable" || parent.childNodes[j].className == "b_quote_author mceNonEditable") {
									quoteWrapper += parent.childNodes[j].innerHTML;
									break;
								}
							}
							newQuote = quoteWrapper + '</div>' + newQuote;
							if(!detectQuote(parent)) {
								break;
							}
						} else {
							newQuote = '<' + parent.nodeName + ' class="' + parent.className + '">' + newQuote;
						}
					}

					var rawHtml = endQuote + '<p><span id="quote_spliter_marker"></span><br/></p>' + newQuote;
					ed.execCommand("mceInsertRawHTML",true,rawHtml);
					var marker = ed.dom.get('quote_spliter_marker');
					if (marker) {
						ed.selection.select(marker);
						ed.selection.collapse();
						ed.execCommand("mceRemoveNode",true,marker);
					}
				}
			});
		},

		// Plugin info function
		getInfo : function() {
			return {
				longname : 'Olat Quote Spliter',
				author : 'frentix GmbH',
				authorurl : 'http://www.frentix.com',
				infourl : 'http://www.frentix.com',
				version : "1.1.1"
			};
		}
	});

	// Register plugin
	tinymce.PluginManager.add('quotespliter', org.olat.core.gui.components.form.flexible.impl.elements.richText.plugins.quotespliter);
})();
