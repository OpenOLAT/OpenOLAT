(function() {

	tinymce.create('org.olat.modules.edusharing.ui', {
		init : function(editor, url) {
			
			var cachedTrans, cachedCoreTrans;
			var cachedHelp;
			
			// Load the OLAT translator.
			function translator() {	
				if(cachedTrans) return cachedTrans;
				var mainWin = o_getMainWin();
				if (mainWin) {
					cachedTrans = jQuery(document).ooTranslator().getTranslator(mainWin.o_info.locale, 'org.olat.modules.edusharing.ui');
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
			
			function getStyle(esFloat) {
				switch(esFloat) {
					case 'left' :
						var style = 'display: block; float: left; margin: 5px 5px 5px 0;';
						break;
					case 'right' :
						var style = 'display: block; float: right; margin: 5px 0 5px 5px;';
						break;
					case 'inline' :
						var style = 'display: inline-block; margin: 0 5px;';
						break;
					case 'none' :
					default:
						var style = 'display: block; float: none; margin: 5px 0;';
						break;
				}
				return style;
			}

			function handles(node) {
				// only an <object>-node can possibly be edu-sharing-node
				if ( 1 != node.nodeType ) {
					return false;
				}
				// no identifier means no edu-sharing node
				if (!node.dataset.es_identifier) {
					return false;
				}
				return true;
			}
			
			function createIdentifier() {
				function s4() {
					return Math.floor((1 + Math.random()) * 0x10000)
						.toString(16)
						.substring(1);
				}
				return s4() + s4() + s4() + s4() + s4() + s4() + s4() + s4();
			}
			
			function getPreviewUrl(params) {
				var contextPath = editor.getParam("contextPath");
				var preview_url = contextPath + "/edusharing/preview",
				preview_url = preview_url.concat('?objectUrl=' + params.object_url);
				preview_url = preview_url.concat('&version=' + params.window_version);
				return preview_url;
			}
			
			function showPreview(params) {
				var mimeHelper = params != null && params.mimetype !== null? params.mimetype.substr(0, 6).toLowerCase(): null;
				if (params.mediatype == 'tool_object' || mimeHelper == 'audio/' || mimeHelper == 'video/' || mimeHelper == 'image/' || params.repotype == 'YOUTUBE') {
					return true;
				}
				return false;
			}
			
			function onResourceSelected(e) {
				// Selection returns nothing
				if (typeof e.target.params == 'undefined') {
					console.warn("edu-sharing selection returns nothing.");
					return;
				}
				 // window was closed without selection
				if (typeof e.target.params.mimetype == 'undefined') return;
				
				var params = e.target.params;
				var selectedNode = editor.selection.getNode();
				node = null;
				
				if (showPreview(params)) {
					node = document.createElement('img');
					node = selectedNode.appendChild(node);
					node.setAttribute('src', getPreviewUrl(params));
				} else {
					node = document.createElement('a');
					node = selectedNode.appendChild(node);
					node.innerHTML = params.title;
				}
				node.dataset.es_identifier = createIdentifier();
				node.dataset.es_objecturl = params.object_url;
				node.dataset.es_version = params.window_version;
				node.dataset.es_version_current = params.window_version;
				if (params.es_mimetype !== null) {
					node.dataset.es_mimetype = params.mimetype;
				}
				if (params.mediatype !== null) {
					node.dataset.es_mediatype = params.mediatype;
				}
				if (params.window_width !== null) {
					node.dataset.es_width = params.window_width;
				}
				if (params.window_height !== null) {
					node.dataset.es_height = params.window_height;
				}
				node.dataset.es_first_edit = true;

				params.esFloat = 'left';
				setAttributes(node, params);
				
				editor.selection.setCursorLocation(node);
				editor.execCommand('edusharing_edit_dialog');
			}
			
			function onConfigEdited(e) {
				console.log(e.data);
				var params = e.data;
				var node = editor.selection.getNode();
				setAttributes(node, params);
				
				if (params.window_versionshow == 'latest') {
					node.dataset.es_version = 0;
				} else {
					node.dataset.es_version = node.dataset.es_version_current;
				}
				node.dataset.es_float = params.esFloat;
				
				editor.execCommand('mceRepaint')
			}
			
			function setAttributes(node, params) {
				node.setAttribute('class', 'edusharing');
				node.setAttribute('alt', params.title);
				node.setAttribute('title', params.title);
				var width = params.window_width;
				if (width == 0)
					width = '';
				node.setAttribute('width', width);
				var height = params.window_height;
				if (height == 0)
					height = '';
				node.setAttribute('height', height);
				node.setAttribute('style', getStyle(params.esFloat));
			}
			
			editor.addButton('olatedusharing', {
				title : 'edu-sharing',
				image : url + '/img/edusharing.png',
				cmd : 'edusharing_button',
				onPostRender: function() {
					var button = this;
					editor.on('NodeChange', function(e) {
						var isEdusharing = handles(editor.selection.getNode());
						button.active(isEdusharing);
					});
				}
			});

			editor.addCommand('edusharing_button', function(ui, value) {
				var node = editor.selection.getNode();
				if ( node ) {
					if ( handles(node) ) {
						editor.execCommand('edusharing_edit_dialog', ui, value);
					} else {
						editor.execCommand('edusharing_insert_dialog', ui, value);
					}
				}
			});

			editor.addCommand('edusharing_insert_dialog', function() {
				var dialog = {
					url: editor.getParam("contextPath") + "/edusharing/search",
					width:	document.documentElement.clientWidth * 0.8,
					height:	document.documentElement.clientHeight * 0.8,
					inline:	1,
					maximizable: true,
					onClose: onResourceSelected
				};

				editor.windowManager.open(dialog);
			});

			editor.addCommand('edusharing_edit_dialog', function(ui, value) {
				var node = editor.selection.getNode();
				
				if (!handles(node)) {
					return false;
				}

				var params = {
					window_width : node.getAttribute('width'),
					window_height : node.getAttribute('height'),
					title : node.getAttribute('title'),
					esFloat: node.dataset.es_float,
					window_versionshow: node.dataset.es_version == 0? "latest": "current",
					mimetype: node.dataset.es_mimetype
				};
				
				var versionDisabled = node.dataset.es_first_edit === "true"? false: true;;
				node.dataset.es_first_edit = false;
				var hideSize = !showPreview(params);
				
				editor.windowManager.open({
					title: translator().translate('tiny.config.dialog.title'),
					body: [{
						type: 'form',
						items: [
							{ 
								name: 'title',
								type: 'textbox',
								label: translator().translate('tiny.config.title'),
								value: params.title
							},
							{ 
								name: 'window_versionshow',
								type: 'listbox',
								label: versionDisabled? null: translator().translate('tiny.config.version'), 
								'values': [
									{text: translator().translate('tiny.config.version.latest'), value: 'latest'},
									{text: translator().translate('tiny.config.version.current'), value: 'current'}
								],
								value: params.window_versionshow,
								hidden: versionDisabled
							},
							{ 
								name: 'esFloat',
								type: 'listbox',
								label: translator().translate('tiny.config.float'), 
								'values': [
									{text: translator().translate('tiny.config.float.left'), value: 'left'},
									{text: translator().translate('tiny.config.float.right'), value: 'right'},
									{text: translator().translate('tiny.config.float.inline'), value: 'inline'},
									{text: translator().translate('tiny.config.float.none'), value: 'none'}
								],
								value: params.esFloat
							},
							{
								type: 'container',
								label: hideSize? null: translator().translate('tiny.config.size'),
								layout: 'flex',
								direction: 'row',
								align: 'center',
								spacing: 5,
								items: [
									{name: 'window_width', type: 'textbox', value: params.window_width, maxLength: 4, size: 4/*, onchange: generatePreview*/},
									{type: 'label', text: 'x'},
									{name: 'window_height', type: 'textbox', value: params.window_height, maxLength: 4, size: 4/*, onchange: generatePreview*/}
								],
								hidden: hideSize
							}
						]
					}],
					onSubmit: onConfigEdited
				},
					params
				);
			});
		},

		getInfo : function() {
			return {
				longname : 'OpenOLAT edu-sharing',
				author : 'uhensler, frentix GmbH',
				authorurl : 'http://www.frentix.com',
				infourl : 'http://www.frentix.com',
				version : "1.0.0"
			};
		}
	});

	tinymce.PluginManager.add('olatedusharing', org.olat.modules.edusharing.ui);
})();
