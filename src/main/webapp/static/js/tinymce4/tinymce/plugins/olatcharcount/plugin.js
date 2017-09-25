(function() {
	tinymce.create('org.olat.core.gui.components.form.flexible.impl.elements.richText.plugins.charcount', {

		/**
		 * Returns information about the plugin as a name/value array.
		 * The current keys are longname, author, authorurl, infourl and version.
		 *
		 * @returns Name/value array containing information about the plugin.
		 * @type Array 
		 */
		getInfo : function() {
			return {
				longname : 'OpenOLAT character counter',
				author : 'frentix GmbH',
				authorurl : 'http://www.frentix.com',
				infourl : 'http://www.frentix.com',
				version : '1.0.1'
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
			
			var cachedTrans;
			
			// Load the OLAT translator.
			function translator() {	
				if(cachedTrans) return cachedTrans;
				var mainWin = o_getMainWin();
				if (mainWin) {
					cachedTrans = jQuery(document).ooTranslator().getTranslator(mainWin.o_info.locale, 'org.olat.core.gui.components.form.flexible.impl.elements.richText.plugins.olatcharcount');
				} else {
					cachedTrans = {	translate : function(key) { return key; } }
				}
				return cachedTrans;
			}

			function getCharacterLength() {
	            var text = ed.getContent({format: 'html'});
	            text = text.trim().replace(/(\n)+/g, " ");
	            return text.length;
	        }

	        function update() {
	        		var count = getCharacterLength();
	        		var label = translator().translate('olatcharcount.size') + "\u00A0";
	            ed.theme.panel.find('#wordcount').text([label, count]);
	            var maxSize = ed.getParam("maxSize");
	            if(count > maxSize) {
	            		ed.theme.panel.find('#statusbar').addClass('danger');
	            } else {
	            		ed.theme.panel.find('#statusbar').removeClass('danger');
	            }
	        }
	        
			// Load Content CSS upon initialization
			ed.on('init', function() {
		        var statusbar = ed.theme.panel && ed.theme.panel.find('#statusbar')[0];
		        if (statusbar) {
		            window.setTimeout(function() {
		            		var label = translator().translate('olatcharcount.size') + "\u00A0";
		            		var tooltip = translator().translate('olatcharcount.tooltip');
		                statusbar.insert({
		                    type: 'label',
		                    name: 'wordcount',
		                    text: [label, getCharacterLength()],
		                    classes: 'wordcount',
		                    tooltip: tooltip,
		                    disabled: ed.settings.readonly
		                }, 0);
		                
		                ed.on('setcontent beforeaddundo undo redo', function() {
		                		update();
		                });
		                
		                var sizer = jQuery.periodic({period: 5000, decay:1.000, max_period: Number.MAX_VALUE}, function() {
		                		try {
		                			update();
		            			} catch (e) {
		            				sizer.cancel(); //error if the editor is removed
		            			}
		                });
		            }, 0);
		        }
			});
		}
	});

	// Register plugin
	tinymce.PluginManager.add('olatcharcount', org.olat.core.gui.components.form.flexible.impl.elements.richText.plugins.charcount);
})();