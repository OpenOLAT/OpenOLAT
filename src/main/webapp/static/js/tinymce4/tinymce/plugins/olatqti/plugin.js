(function() {
	tinymce.create('org.olat.ims.qti21.ui.editor', {

		/**
		 * Returns information about the plugin as a name/value array.
		 * The current keys are longname, author, authorurl, infourl and version.
		 *
		 * @returns Name/value array containing information about the plugin.
		 * @type Array 
		 */
		getInfo : function() {
			return {
				longname : 'OpenOLATQTI',
				author : 'frentix GmbH',
				authorurl : 'http://www.frentix.com',
				infourl : 'http://www.frentix.com',
				version : '1.0.0'
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
			
			var $ = ed.$, selection = ed.selection;
			var cachedTrans, cachedCoreTrans;
			var cachedHelp;
			var lastSelectedGap;
			
			// Load the OLAT translator.
			function translator() {	
				if(cachedTrans) return cachedTrans;
				var mainWin = o_getMainWin();
				if (mainWin) {
					cachedTrans = jQuery(document).ooTranslator().getTranslator(mainWin.o_info.locale, 'org.olat.ims.qti21.ui.editor');
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
	
			function showTextDialog(e) {
				showDialog(e, "string")
			}
			
			function showNumericalDialog(e) {
				showDialog(e, "float")
			}

			function showDialog(e, gapType) {
				var newEntry = false;
				var newSelectedText = null;
				var responseIdentifier;
				if(typeof lastSelectedGap != 'undefined') {
					responseIdentifier = jQuery(lastSelectedGap).attr('data-qti-gap-identifier')
				} else {
					var counter = 1;
					newSelectedText = ed.selection.getContent({format: 'text'})
					
					tinymce.each(ed.dom.select("img[data-qti]"), function(node) {
						var identifier = jQuery(node).attr('data-qti-gap-identifier');
						if(identifier.lastIndexOf("RESPONSE_", 0) == 0) {
							var id = parseInt(identifier.substring(9, identifier.length));
							if(id > counter) {
								counter = id;
							}	
						}
				    });
					
					var responseIdentifier = "RESPONSE_" + (counter + 1);
					var placeholder = createPlaceholder(responseIdentifier, gapType);
					var holderHtml = new tinymce.html.Serializer().serialize(placeholder);
					ed.insertContent(holderHtml);
					newEntry = true;
				}
				
				var ffxhrevent = ed.getParam("ffxhrevent");
				o_ffXHREvent(ffxhrevent.formNam, ffxhrevent.dispIdField, ffxhrevent.dispId, ffxhrevent.eventIdField, 2, false, false,
						'cmd', 'gapentry', 'responseIdentifier', responseIdentifier, 'newEntry', newEntry, 'selectedText', newSelectedText, 'gapType', gapType);
			}

			ed.addButton('olatqtifibtext', {
				title : translator().translate('new.fib'),
				icon : 'gaptext',
				stateSelector: ['img[data-qti-gap-type=string]', 'span[data-qti-gap-type=string]'],
				onclick: showTextDialog
			});
			
			ed.addButton('olatqtifibnumerical', {
				title : translator().translate('new.fib') + ' Numerical',
				icon : 'gapnumerical',
				stateSelector: ['img[data-qti-gap-type=float]', 'span[data-qti-gap-type=float]'],
				onclick: showNumericalDialog
			});
			
			ed.addButton('editgap', {
				title: 'edit',
				icon: 'edit',
				onclick: showDialog
			});
			
			ed.addMenuItem('olatqtifibtext', {
				text : translator().translate('new.fib'),
				icon : 'gapnumerical',
				stateSelector: ['img[data-qti-gap-type=string]', 'span[data-qti-gap-type=string]'],
				onclick: showNumericalDialog
			});
			
			ed.addMenuItem('olatqtifibnumerical', {
				text : translator().translate('new.fib.numerical') + ' Numerical',
				icon : 'gaptext',
				stateSelector: ['img[data-qti-gap-type=float]', 'span[data-qti-gap-type=float]'],
				onclick: showTextDialog
			});
			
			ed.on('NodeChange', function(e) {
				if (lastSelectedGap && lastSelectedGap.id != e.element.src) {
					lastSelectedGap = undefined;
				}
				
				if (isEditableGapEntry(e.element)) {
					lastSelectedGap = e.element;
				}
			});
			
			function isEditableGapEntry(img) {
				return ed.dom.is(img, 'img[data-qti]');
			}
			
			function createPlaceholder(responseIdentifier, gapType) {
				var placeholder = new tinymce.html.Node('img', 1);
				placeholder.attr({
					width: "32",
					height: "16",
					src : tinymce.Env.transparentSrc,
					"data-qti": "textentryinteraction",
					"data-qti-gap-identifier": responseIdentifier,
					"data-qti-gap-type": gapType,
					"data-mce-placeholder": "",
					"data-mce-resize" : "false",
					"data-textentryinteraction": "empty",
					"class": "mce-shim textentryinteraction"
				});
				return placeholder;
			}

			// Load Content CSS upon initialization
			ed.on('init', function() {
				if (ed.settings.content_css !== false) {
					ed.dom.loadCSS(url + "/css/content.css");
				}
				jQuery("img.textentryinteraction", ed.getBody()).each(function(index, el) {
					var imgEl = el;
					jQuery(imgEl).click(function() {
						var ffxhrevent = ed.getParam("ffxhrevent");
						var responseIdentifier = jQuery(imgEl).attr('data-qti-gap-identifier');
						o_ffXHREvent(ffxhrevent.formNam, ffxhrevent.dispIdField, ffxhrevent.dispId, ffxhrevent.eventIdField, 2, false, false,
							'cmd', 'gapentry', 'responseIdentifier', responseIdentifier);
					});
				});
			});
			
			ed.on('preInit', function() {
				ed.parser.addNodeFilter('textentryinteraction', function(nodes) {
					var i = nodes.length, node, placeHolder, videoScript;
					while (i--) {
						node = nodes[i];
						if (node.name == 'textentryinteraction') {
							var responseIdentifier = node.attr('responseidentifier');
							var gapType = node.attr('openolattype');
							if(typeof gapType === "undefined") {
								gapType = "string";
							}
							var placeHolder = createPlaceholder(responseIdentifier, gapType);
							node.replace(placeHolder);
						}
					}
				});
			});
			
			/** 
             * This onSetContent handler is used to convert the comments to placeholder images (e.g. when loading).
             */
			ed.on('PreProcess', function(e) {
				tinymce.each(ed.dom.select("img[data-qti]"), function(node) {
					var identifier = jQuery(node).attr('data-qti-gap-identifier');
					var textNode = ed.dom.create("textEntryInteraction", {
						responseIdentifier: identifier
					});
					
					ed.dom.replace(textNode, node, false);
			    });
			});
		}
	});

	// Register plugin
	tinymce.PluginManager.add('olatqti', org.olat.ims.qti21.ui.editor);
})();