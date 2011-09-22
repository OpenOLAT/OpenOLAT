/**
 * The math edtor plugin offers a latex-formula real-time authoring environment.
 * See how it looks as you type. The plugin uses jsMath for format the latex
 * forulas
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
		translator = mainWin.b_jsTranslatorFactory.getTranslator(mainWin.o_info.locale, 'org.olat.core.gui.components.form.flexible.impl.elements.richText.plugins.olatmatheditor')	
	} else {
		// implement dummy-translator
		translator = {	translate : function(key) { return key; } }
	}

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
			// Register the command so that it can be invoked by using tinyMCE.activeEditor.execCommand('mceJsmath');
			ed.addCommand('mceJsmath', function() {
				ed.windowManager.open({
					file : url + '/jsmathdialog.htm',
					width : 400,
					height : 400,
					inline : 1
				}, {
					plugin_url : url // Plugin absolute URL
				});
			});

			// Register plugin button
			ed.addButton('olatmatheditor', {
				title : translator.translate('olatmatheditor.desc'),
				cmd : 'mceJsmath',
				image : url + '/img/sigma.png'
			});
			
			;

			// Add a node change handler, selects the button in the UI when a image is selected
			ed.onNodeChange.add(function(ed, cm, n) {
				cm.setActive('olatmatheditor', (n.nodeName == 'IMG') && (/mceItemJSMath/.test(ed.dom.getAttrib(n, 'class'))));
			});
			
			ed.onInit.add(function() {
			     if (ed.settings.content_css !== false) ed.dom.loadCSS(url + "/css/content.css");
			});

            /** 
             * This setContent handler is used to convert the <span class="math"> spans
             * to the placeholder <img> tags when loading the document.
             */
    		ed.onSetContent.add(function(editor, obj) {

				// Find all SPANs of class "math"...
				tinymce.each(editor.dom.select("span.math", obj.node), function(node) {
    		         // ...and for each of these, create an IMG...
					 var latex = node.innerHTML;
					 var img = editor.dom.create("img", {"class" : "mceItemJSMath", width : "32", height : "32", src : ed.getParam("olatsmileys_transparentImage"), title : latex, alt : node.title});
					 //  ...and replace the SPAN by the IMG.
					 editor.dom.replace(img, node);
			     });
    		});

            /** 
              * This onPreProcess handler is used to convert the placeholder &lt;img&gt; tags back to the
              * &lt;span class="math"&gt; tags when saving the document.
              */
    		ed.onPreProcess.add(function(editor, obj) {
				
				// Find all IMGs of class "mceItemJSMath"...
				tinymce.each(editor.dom.select("img.mceItemJSMath", obj.node), function(node) {
					// ...and for each of these, create a SPAN...
					var span = editor.dom.create("span", {"class" : "math", title : node.alt}, node.title);
					// ...and replace the IMG by the SPAN.
					editor.dom.replace(span, node);
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
				longname : 'Olat Math Editor',
				author : 'frentix GmbH',
				authorurl : 'http://www.frentix.com',
				infourl : 'http://www.frentix.com',
				version : "1.0"
			};
		}
	});

	// Register plugin
	tinymce.PluginManager.add('olatmatheditor', org.olat.core.gui.components.form.flexible.impl.elements.richText.plugins.olatmatheditor);
})();
