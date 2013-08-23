/**
 * The math edtor plugin offers a latex-formula real-time authoring environment.
 * See how it looks as you type. The plugin uses jsMath for format the latex
 * forulas
 * 
 * 18.06.2009 timo.wuersch@frentix.com
 */
(function() {
	tinymce.create('org.olat.core.gui.components.form.flexible.impl.elements.richText.plugins.olatmatheditor', {
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
					cachedTrans = jQuery(document).ooTranslator().getTranslator(mainWin.o_info.locale, 'org.olat.core.gui.components.form.flexible.impl.elements.richText.plugins.olatmatheditor')	
				} else {
					cachedTrans = {	translate : function(key) { return key; } }
				}
				return cachedTrans;
			}
			
			function insertLatex() {
		        // Initialize local variables
		        var latexCode = win.find('#latex')[0].value();
		        var contentNode = ed.selection.getNode();

		        // Check whether the selection is a jsMath objet by looking at its class attribute
		        if ((contentNode != null) && (/mceItemJSMath/.test(ed.dom.getAttrib(contentNode, "class")))) {
		            ed.dom.setAttrib(contentNode, "title", latexCode);
		            ed.dom.setAttrib(contentNode, "alt", escape(latexCode));
		            ed.execCommand("mceRepaint");
		        } else {
		        	var htmlCode = '<img src="' + ed.getParam("olatsmileys_transparentImage") + '" class="mceItemJSMath" title="' + latexCode + '" alt="' + escape(latexCode) + '" width="32" height="32"/>';
		            ed.execCommand("mceInsertContent", false, htmlCode);
		        }
			}
			
			function updatePreview() {
			    // Get the offscreen preview element and the latex code.
			    var offscreenPreview = jQuery("#mathpreviewOffscreen");
			    var latexCode = win.find('#latex')[0].value();
			    
			    // Copy the latex code into the offscreen preview DIV.
			    offscreenPreview.text(latexCode);
			    // Set the class of the offscreen preview DIV to "math" so that jsMath will recognize it.
			    offscreenPreview.addClass("math");

			    BFormatter.formatLatexFormulas('mathpreviewOffscreen');
			    
			    // After the offscreen preview has been rendered, copy it onscreen
			    setTimeout(function() {
			        jsMath.Synchronize(copyPreviewToScreen);
			    }, 100);
			}
			
			function copyPreviewToScreen() {
			    // Get the offscreen and onscreen previews
			    var offscreenPreview = jQuery("#mathpreviewOffscreen");
			    var offscreenPreviewDown = jQuery("#mathpreviewOffscreen *:first-child");
			    var preview = jQuery("#mathpreviewFormula");
			    var errorMessage = jQuery("#mathpreviewErrorMessage");

			    // Check wheter we have a rendered formula, an error message, or nothing
			    if (offscreenPreviewDown.length > 0) {
			        if (offscreenPreviewDown.get(0).nodeName == "NOBR") {
			            // We have a formula
			            preview.html(offscreenPreview.html());
			            errorMessage.html("");
			        } else if (offscreenPreviewDown.nodeName == "SPAN") {
			            if (offscreenPreviewDown.hasClass("error")) {
			                errorMessage.html(offscreenPreview.html());
			            }
			        }
			    } else {
			        // We have nothing.
			        preview.html(offscreenPreview.html());
			    }
			}
			
			function showDialog() {
				win = ed.windowManager.open({
					title: translator().translate('olatmatheditor.formulaTabTitle'),
					minWidth: 540,
					body: [{
							//type: 'form',
					    	type: 'panel',
					    	layout: 'flex',
							direction: 'column',
							align: 'stretch',
							padding: 10,
							spacing: 10,
					    	items: [
					    	   { type: 'label', text: translator().translate('olatmatheditor.latexGroupTitle') },
					    	   { name: 'latex', type: 'textbox', multiline:true, flex:1, minHeight:120, onkeyup: updatePreview },
					    	   { name: 'preview', type: 'panel', label: '', flex:1, minHeight:120,
					    		 html:'<div id="mathpreviewFormula" class="math" style="width:100%; height=100%;"></div><div id="mathpreviewErrorMessage" class="math"></div><div id="mathpreviewOffscreen" class="math" style="display:none;"></div>'
							   }
					    	]
					   }],
					onSubmit: insertLatex
				});
				
				var selectedNode = ed.selection.getNode();
		        if ((selectedNode.nodeName.toLowerCase() == "img") && (selectedNode.className == "mceItemJSMath")) {
		            var latexCode = unescape(selectedNode.alt);
		            win.find('#latex')[0].value(latexCode);
		            updatePreview();
		        }
			}

			// Register plugin button
			ed.addButton('olatmatheditor', {
				title : translator().translate('olatmatheditor.desc'),
				//cmd : 'mceJsmath',
				image : url + '/img/sigma.png',
				onclick: showDialog,
				onPostRender: function() {
			        var ctrl = this;
			        ed.on('NodeChange', function(e) {
						var test = (e.element.nodeName == 'IMG') && (/mceItemJSMath/.test(ed.dom.getAttrib(e.element, 'class')));
						ctrl.active(test);
						if(test) {
							e.preventDefault(true);
							e.stopImmediatePropagation();
						}
					});
				}
			});
			
			ed.on('init', function() {
			     if (ed.settings.content_css !== false) {
			    	 ed.dom.loadCSS(url + "/css/content.css");
			     }
			});

            /** 
             * This setContent handler is used to convert the <span class="math"> spans
             * to the placeholder <img> tags when loading the document.
             */
    		ed.on('LoadContent',function(e) {
				// Find all SPANs of class "math"...
				tinymce.each(ed.dom.select("span.math"), function(node) {
    		         // ...and for each of these, create an IMG...
					 var latex = node.innerHTML;
					 var img = ed.dom.create("img", {"class" : "mceItemJSMath", width : "32", height : "32", src : ed.getParam("olatsmileys_transparentImage"), title : latex, alt : node.title});
					 //  ...and replace the SPAN by the IMG.
					 ed.dom.replace(img, node);
			     });
    		});

            /** 
              * This onPreProcess handler is used to convert the placeholder &lt;img&gt; tags back to the
              * &lt;span class="math"&gt; tags when saving the document.
              */
    		ed.on('PreProcess',function(e) {
				// Find all IMGs of class "mceItemJSMath"...
				tinymce.each(ed.dom.select("img.mceItemJSMath"), function(node) {
					// ...and for each of these, create a SPAN...
					var span = ed.dom.create("span", {"class" : "math", title : node.alt}, node.title);
					// ...and replace the IMG by the SPAN.
					ed.dom.replace(span, node);
				});
    		});
		},

		/**
		 * Creates control instances based in the incomming name. This method is normally not
		 * needed since the addButton method of the tinymce.Editor class is a more easy way of adding buttons
		 * but you sometimes need to create more complex controls like listboxes, split buttons etc then this
		 * method can be used to create those.
		 *
		 * @param {String} n Name of the control to create.
		 * @param {tinymce.ControlManager} cm Control manager to use inorder to create new control.
		 * @return {tinymce.ui.Control} New control instance or null if no control was created.
		 */
		createControl : function(n, cm) {
			return null;
		},

		/**
		 * Returns information about the plugin as a name/value array.
		 * The current keys are longname, author, authorurl, infourl and version.
		 *
		 * @return {Object} Name/value array containing information about the plugin.
		 */
		getInfo : function() {
			return {
				longname : 'OpenOLAT Math Editor',
				author : 'frentix GmbH',
				authorurl : 'http://www.frentix.com',
				infourl : 'http://www.frentix.com',
				version : "1.1"
			};
		}
	});

	// Register plugin
	tinymce.PluginManager.add('olatmatheditor', org.olat.core.gui.components.form.flexible.impl.elements.richText.plugins.olatmatheditor);
})();
