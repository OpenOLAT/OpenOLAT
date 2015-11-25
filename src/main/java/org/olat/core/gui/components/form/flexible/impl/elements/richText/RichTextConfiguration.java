/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.core.gui.components.form.flexible.impl.elements.richText;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.controllers.linkchooser.CustomLinkTreeModel;
import org.olat.core.dispatcher.impl.StaticMediaDispatcher;
import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.dispatcher.mapper.MapperService;
import org.olat.core.dispatcher.mapper.manager.MapperKey;
import org.olat.core.gui.components.form.flexible.impl.elements.richText.plugins.TinyMCECustomPlugin;
import org.olat.core.gui.components.form.flexible.impl.elements.richText.plugins.TinyMCECustomPluginFactory;
import org.olat.core.gui.control.Disposable;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.themes.Theme;
import org.olat.core.gui.translator.Translator;
import org.olat.core.helpers.Settings;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Formatter;
import org.olat.core.util.UserSession;
import org.olat.core.util.Util;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSContainerMapper;
import org.olat.core.util.vfs.VFSManager;

/**
 * Description:<br>
 * 
 * This configuration object is used to configure the features of the TinyMCE
 * HTML editor. Use the addXYZConfiguration() methods to add some default
 * settings. You can also manually tweak the configuration, make sure you
 * understand how Tiny works. See http://wiki.moxiecode.com for more information
 * 
 * <P>
 * Initial Date: 21.04.2009 <br>
 * 
 * @author gnaegi
 */
public class RichTextConfiguration implements Disposable {
	private static final OLog log = Tracing.createLoggerFor(RichTextConfiguration.class); 
	private static final String MODE = "mode";
	private static final String MODE_VALUE_EXACT = "exact";
	private static final String ELEMENTS = "elements";

	// Doctype and language
	private static final String LANGUAGE = "language";
	// Layout and theme
	private static final String CONTENT_CSS = "content_css";
	private static final String CONVERT_URLS = "convert_urls";
	private static final String IMPORTCSS_APPEND = "importcss_append";
	private static final String IMPORT_SELECTOR_CONVERTER = "importcss_selector_converter";
	private static final String IMPORT_SELECTOR_CONVERTER_VALUE_REMOVE_EMOTICONS ="function(selector) { if (selector.indexOf('img.b_emoticons') != -1 || selector.indexOf('img.o_emoticons') != -1) {return false;} else { return this.convertSelectorToFormat(selector); }}";
	private static final String IMPORTCSS_SELECTOR_FILTER = "importcss_selector_filter";
	private static final String IMPORTCSS_GROUPS = "importcss_groups";
	private static final String IMPORTCSS_GROUPS_VALUE_MENU = "[{title: 'Paragraph', filter: /^(p)\\./},{title: 'Div', filter: /^(div|p)\\./},{title: 'Table', filter: /^(table|th|td|tr)\\./},{title: 'Url', filter: /^(a)\\./},{title: 'Style'}]";
	private static final String HEIGHT = "height";
	// Window appearance
	private static final String DIALOG_TYPE = "dialog_type";
	private static final String DIALOG_TYPE_VALUE_MODAL = "modal";

	// Non-Editable plugin
	private static final String NONEDITABLE_NONEDITABLE_CLASS = "noneditable_noneditable_class";
	private static final String NONEDITABLE_NONEDITABLE_CLASS_VALUE_MCENONEDITABLE = "mceNonEditable";
	// Fullscreen plugin
	private static final String FULLSCREEN_NEW_WINDOW = "fullscreen_new_window";
	// Other plugins
	private static final String TABFOCUS_SETTINGS = "tabfocus_elements";
	private static final String TABFOCUS_SETTINGS_PREV_NEXT = ":prev,:next";
	// Valid elements
	private static final String EXTENDED_VALID_ELEMENTS = "extended_valid_elements";
	private static final String EXTENDED_VALID_ELEMENTS_VALUE_FULL = "script[src|type|defer],form[*],input[*],a[*],p[*],#comment[*],img[*],iframe[*],map[*],area[*]";
	private static final String INVALID_ELEMENTS = "invalid_elements";
	private static final String INVALID_ELEMENTS_FORM_MINIMALISTIC_VALUE_UNSAVE = "iframe,script,@[on*],object,embed";
	private static final String INVALID_ELEMENTS_FORM_SIMPLE_VALUE_UNSAVE = "iframe,script,@[on*],object,embed";
	private static final String INVALID_ELEMENTS_FORM_FULL_VALUE_UNSAVE = "iframe,script,@[on*]";
	public static final String INVALID_ELEMENTS_FORM_FULL_VALUE_UNSAVE_WITH_SCRIPT = "iframe,@[on*]";
	private static final String INVALID_ELEMENTS_FILE_FULL_VALUE_UNSAVE = "";
	// Other optional configurations, optional
	private static final String FORCED_ROOT_BLOCK = "forced_root_block";
	private static final String FORCED_ROOT_BLOCK_VALUE_NOROOT = "";
	private static final String DOCUMENT_BASE_URL = "document_base_url";
	private static final String PASTE_DATA_IMAGES = "paste_data_images";
	//
	// Generic boolean true / false values
	private static final String VALUE_FALSE = "false";

	// Callbacks
	private static final String ONCHANGE_CALLBACK = "onchange_callback";
	private static final String ONCHANGE_CALLBACK_VALUE_TEXT_AREA_ON_CHANGE = "BTinyHelper.triggerOnChangeOnFormElement";

	private static final String FILE_BROWSER_CALLBACK = "file_browser_callback";
	private static final String FILE_BROWSER_CALLBACK_VALUE_LINK_BROWSER = "BTinyHelper.openLinkBrowser";
	private static final String ONINIT_CALLBACK_VALUE_START_DIRTY_OBSERVER = "BTinyHelper.startFormDirtyObserver";
	private static final String URLCONVERTER_CALLBACK = "urlconverter_callback";
	private static final String URLCONVERTER_CALLBACK_VALUE_BRASATO_URL_CONVERTER = "BTinyHelper.linkConverter";

	private Map<String, String> quotedConfigValues = new HashMap<String, String>();
	private Map<String, String> nonQuotedConfigValues = new HashMap<String, String>();
	private List<String> oninit = new ArrayList<String>();

	// Supported image and media suffixes
	private static final String[] IMAGE_SUFFIXES_VALUES = { "jpg", "gif", "jpeg", "png" };
	private static final String[] MEDIA_SUFFIXES_VALUES = { "swf", "dcr", "mov", "qt", "mpg", "mp3", "mp4", "mpeg", "avi", "wmv", "wm", "asf",
			"asx", "wmx", "wvx", "rm", "ra", "ram" };
	private static final String[] FLASH_PLAYER_SUFFIXES_VALUES = {"flv","f4v","mp3","mp4","aac","m4v","m4a"};

	private String[] linkBrowserImageSuffixes;
	private String[] linkBrowserMediaSuffixes;
	private String[] linkBrowserFlashPlayerSuffixes;
	private VFSContainer linkBrowserBaseContainer;
	private String linkBrowserUploadRelPath;
	private String linkBrowserRelativeFilePath;
	private String linkBrowserAbsolutFilePath;
	private boolean allowCustomMediaFactory = true;
	private CustomLinkTreeModel linkBrowserCustomTreeModel;	
	// DOM ID of the flexi form element
	private String domID;
	
	private MapperKey contentMapperKey;
	
	private final Locale locale;
	private TinyConfig tinyConfig;

	/**
	 * Constructor, only used by RichText element itself. Use
	 * richtTextElement.getEditorConfiguration() to acess this object
	 * 
	 * @param domID The ID of the flexi element in the browser DOM
	 * @param rootFormDispatchId The dispatch ID of the root form that deals with the submit button
	 */
	RichTextConfiguration(String domID, String rootFormDispatchId, Locale locale) {
		this.domID = domID;
		this.locale = locale;
		// use exact mode that only applies to this DOM element
		setQuotedConfigValue(MODE, MODE_VALUE_EXACT);
		setQuotedConfigValue(ELEMENTS, domID);
		// set the on change handler to delegate to flexi element on change handler
		setQuotedConfigValue(ONCHANGE_CALLBACK, ONCHANGE_CALLBACK_VALUE_TEXT_AREA_ON_CHANGE);
		// set custom url converter to deal with framework and content urls properly
		setNonQuotedConfigValue(URLCONVERTER_CALLBACK, URLCONVERTER_CALLBACK_VALUE_BRASATO_URL_CONVERTER);
		setNonQuotedConfigValue("allow_script_urls", "true");
		// use modal windows, all OLAT workflows are implemented to work this way
		setModalWindowsEnabled(true);
		// Start observing of diry richt text element and trigger calling of setFlexiFormDirty() method
		// This check is initialized after the editor has fully loaded
		addOnInitCallbackFunction(ONINIT_CALLBACK_VALUE_START_DIRTY_OBSERVER + "('" + rootFormDispatchId + "','" + domID + "')");
		addOnInitCallbackFunction("tinyMCE.get('" + domID + "').focus()");
	}

	/**
	 * Method to add the standard configuration for the form based minimal
	 * editor
	 * 
	 * @param usess
	 * @param externalToolbar
	 * @param guiTheme
	 */
	public void setConfigProfileFormEditorMinimalistic(Theme guiTheme) {
		setConfigBasics(guiTheme);
		// Add additional plugins
		TinyMCECustomPluginFactory customPluginFactory = CoreSpringFactory.getImpl(TinyMCECustomPluginFactory.class);
		List<TinyMCECustomPlugin> enabledCustomPlugins = customPluginFactory.getCustomPlugionsForProfile();
		for (TinyMCECustomPlugin tinyMCECustomPlugin : enabledCustomPlugins) {
			setCustomPluginEnabled(tinyMCECustomPlugin);
		}
		// Don't allow javascript or iframes
		setQuotedConfigValue(INVALID_ELEMENTS, INVALID_ELEMENTS_FORM_MINIMALISTIC_VALUE_UNSAVE);
		
		tinyConfig = TinyConfig.minimalisticConfig;
	}


	/**
	 * Method to add the standard configuration for the form based simple and
	 * full editor
	 * 
	 * @param fullProfile
	 *            true: use full profile; false: use simple profile
	 * @param usess
	 * @param externalToolbar
	 * @param guiTheme
	 * @param baseContainer
	 * @param customLinkTreeModel
	 */
	public void setConfigProfileFormEditor(boolean fullProfile, UserSession usess, Theme guiTheme,
			VFSContainer baseContainer, CustomLinkTreeModel customLinkTreeModel) {
		setConfigBasics(guiTheme);

		TinyMCECustomPluginFactory customPluginFactory = CoreSpringFactory.getImpl(TinyMCECustomPluginFactory.class);
		List<TinyMCECustomPlugin> enabledCustomPlugins = customPluginFactory.getCustomPlugionsForProfile();
		for (TinyMCECustomPlugin tinyMCECustomPlugin : enabledCustomPlugins) {
			setCustomPluginEnabled(tinyMCECustomPlugin);
		}		
		
		if (fullProfile) {	
			// Don't allow javascript or iframes			
			setQuotedConfigValue(INVALID_ELEMENTS, INVALID_ELEMENTS_FORM_FULL_VALUE_UNSAVE);
			tinyConfig = TinyConfig.editorFullConfig;
		} else {
			// Don't allow javascript or iframes, if the file browser is there allow also media elements (the full values)
			setQuotedConfigValue(INVALID_ELEMENTS, (baseContainer == null ? INVALID_ELEMENTS_FORM_SIMPLE_VALUE_UNSAVE : INVALID_ELEMENTS_FORM_FULL_VALUE_UNSAVE));
			tinyConfig = TinyConfig.editorConfig;
		}

		// Setup file and link browser
		if (baseContainer != null) {
			tinyConfig = tinyConfig.enableImageAndMedia();
			setFileBrowserCallback(baseContainer, customLinkTreeModel, IMAGE_SUFFIXES_VALUES, MEDIA_SUFFIXES_VALUES, FLASH_PLAYER_SUFFIXES_VALUES);
			// since in form editor mode and not in file mode we use null as relFilePath
			setDocumentMediaBase(baseContainer, null, usess);			
		}
	}

	/**
	 * Method to add the standard configuration for the file based full editor
	 * 
	 * @param usess
	 * @param externalToolbar
	 * @param guiTheme
	 * @param baseContainer
	 * @param relFilePath
	 * @param customLinkTreeModel
	 */
	public void setConfigProfileFileEditor(UserSession usess, Theme guiTheme, VFSContainer baseContainer, String relFilePath, CustomLinkTreeModel customLinkTreeModel) {
		setConfigBasics(guiTheme);
		// Line 1
		setFullscreenEnabled(true, false);
		setInsertDateTimeEnabled(true, usess.getLocale());
		// Plugins without buttons
		setNoneditableContentEnabled(true, null);		
		TinyMCECustomPluginFactory customPluginFactory = CoreSpringFactory.getImpl(TinyMCECustomPluginFactory.class);
		List<TinyMCECustomPlugin> enabledCustomPlugins = customPluginFactory.getCustomPlugionsForProfile();
		for (TinyMCECustomPlugin tinyMCECustomPlugin : enabledCustomPlugins) {
			setCustomPluginEnabled(tinyMCECustomPlugin);
		}
		
		// Allow editing of all kind of HTML elements and attributes
		setQuotedConfigValue(EXTENDED_VALID_ELEMENTS, EXTENDED_VALID_ELEMENTS_VALUE_FULL);
		setQuotedConfigValue(INVALID_ELEMENTS, INVALID_ELEMENTS_FILE_FULL_VALUE_UNSAVE);
		
		setNonQuotedConfigValue(PASTE_DATA_IMAGES, "true");
		// Setup file and link browser
		if (baseContainer != null) {
			setFileBrowserCallback(baseContainer, customLinkTreeModel, IMAGE_SUFFIXES_VALUES, MEDIA_SUFFIXES_VALUES, FLASH_PLAYER_SUFFIXES_VALUES);
			setDocumentMediaBase(baseContainer, relFilePath, usess);			
		}
		
		tinyConfig = TinyConfig.fileEditorConfig;
	}

	/**
	 * Internal helper to generate the common configurations which are used by
	 * each profile
	 * 
	 * @param usess
	 * @param externalToolbar
	 * @param guiTheme
	 */
	private void setConfigBasics(Theme guiTheme) {
		// Use users current language
		Locale loc = I18nManager.getInstance().getCurrentThreadLocale();
		setLanguage(loc);
		// Use theme content css
		setContentCSSFromTheme(guiTheme);
		// Plugins without buttons
		setNoneditableContentEnabled(true, null);
		setTabFocusEnabled(true);
	}

	public boolean isAllowCustomMediaFactory() {
		return allowCustomMediaFactory;
	}

	public void setAllowCustomMediaFactory(boolean allowCustomMediaFactory) {
		this.allowCustomMediaFactory = allowCustomMediaFactory;
	}

	/**
	 * Add a function name that has to be executed after initialization. <br>
	 * E.g: myFunctionName, (alert('loading successfull')) <br>
	 * Don't add something like this: function() {alert('loading successfull')},
	 * use the following notation instead: (alert('loading successfull'))
	 * 
	 * @param functionName
	 */
	public void addOnInitCallbackFunction(String functionName) {
		if(functionName != null) {
			oninit.add(functionName);
		}
	}
	
	protected List<String> getOnInit() {
		return oninit;
	}

	/**
	 * Enable the tabfocus plugin
	 * 
	 * if enabled its possible to enter/leave the tinyMCE-editor with TAB-key.
	 * drawback is, that you cannot enter tabs in the editor itself or navigate over buttons!
	 * see http://bugs.olat.org/jira/browse/OLAT-6242
	 * @param tabFocusEnabled
	 */
	private void setTabFocusEnabled(boolean tabFocusEnabled){
		if (tabFocusEnabled){
			setQuotedConfigValue(TABFOCUS_SETTINGS, TABFOCUS_SETTINGS_PREV_NEXT);
		}
	}
	
	/**
	 * Configure the tinymce windowing system
	 * 
	 * @param modalWindowsEnabled
	 *            true: use modal windows; false: use non-modal windows
	 * @param inlinePopupsEnabled
	 *            true: use inline popups; false: use browser window popup
	 *            windows
	 */
	private void setModalWindowsEnabled(boolean modalWindowsEnabled) {
		// in both cases opt in, default values are set to non-inline windows that
		// are not modal
		if (modalWindowsEnabled) {
			setQuotedConfigValue(DIALOG_TYPE, DIALOG_TYPE_VALUE_MODAL);
		}
	}

	/**
	 * Set the language for editor interface. If no translation can be found,
	 * the system fallbacks to EN
	 * 
	 * @param loc
	 */
	private void setLanguage(Locale loc) {
		// tiny does not support country or variant codes, only language code
		String langKey = loc.getLanguage();
		String path = "/static/js/tinymce4/tinymce/langs/" + langKey + ".js";
		String realPath = WebappHelper.getContextRealPath(path);
		if(realPath == null || !(new File(realPath).exists())) {
			langKey = "en";
		}
		setQuotedConfigValue(LANGUAGE, langKey);
	}

	/**
	 * Enable or disable areas in the editor content that can't be modified at
	 * all. The areas are identified with the nonEditableCSSClass.
	 * 
	 * @param noneditableContentEnabled
	 *            true: use non-editable areas; false: all areas are editable
	 * @param nonEditableCSSClass
	 *            the class that identifies the non-editable fields or NULL to
	 *            use the default value 'mceNonEditable'
	 */
	private void setNoneditableContentEnabled(boolean noneditableContentEnabled, String nonEditableCSSClass) {
		if (noneditableContentEnabled) {
			if (nonEditableCSSClass != null && !nonEditableCSSClass.equals(NONEDITABLE_NONEDITABLE_CLASS_VALUE_MCENONEDITABLE)) {
				// Add non editable class but only when it differs from the default name
				setQuotedConfigValue(NONEDITABLE_NONEDITABLE_CLASS, nonEditableCSSClass);
			}
		}
	}
	
	public void disableMedia() {
		tinyConfig = tinyConfig.disableMedia();
	}
	
	public void disableMathEditor() {
		tinyConfig = tinyConfig.disableMathEditor();
	}
	
	public void disableImageAnMovie() {
		tinyConfig = tinyConfig.disableImageAndMedia();
	}

	/**
	 * Enable / disable the full-screen plugin
	 * 
	 * @param fullScreenEnabled
	 *            true: plugin enabled; false: plugin disabled
	 * @param inNewWindowEnabled
	 *            true: fullscreen opens in new window; false: fullscreen opens
	 *            in same window.
	 * @param row
	 *            The row where to place the plugin buttons
	 */	
	private void setFullscreenEnabled(boolean fullScreenEnabled, boolean inNewWindowEnabled) {
		if (fullScreenEnabled) {
			// enabled if needed, disabled by default
			if (inNewWindowEnabled) setNonQuotedConfigValue(FULLSCREEN_NEW_WINDOW, VALUE_FALSE);
		}
	}

	/**
	 * Enable / disable the date and time insert plugin
	 * 
	 * @param insertDateTimeEnabled
	 *            true: plugin enabled; false: plugin disabled
	 * @param locale
	 *            the locale used to format the date and time
	 * @param row
	 *            The row where to place the plugin buttons
	 */
	private void setInsertDateTimeEnabled(boolean insertDateTimeEnabled, Locale locale) {
		if (insertDateTimeEnabled) {
			// use date format defined in org.olat.core package
			Formatter formatter = Formatter.getInstance(locale);
			String dateFormat = formatter.getSimpleDatePatternForDate();
			setQuotedConfigValue("insertdatetime_dateformat", dateFormat);
			setNonQuotedConfigValue("insertdatetime_formats", "['" + dateFormat + "','%H:%M:%S']");
		}
	}

	/**
	 * Enable / disable a TinyMCECustomPlugin plugin
	 * 
	 * @param customPluginEnabled true: plugin enabled; false: plugin disabled
	 * @param customPlugin the plugin
	 * @param profil
	 *            The profile in which context the plugin is used
	 */
	private void setCustomPluginEnabled(TinyMCECustomPlugin customPlugin) {
		// Add plugin specific parameters
		Map<String,String> params = customPlugin.getPluginParameters(locale);
		if (params != null) {
			for (Entry<String, String> param : params.entrySet()) {
				// don't use pluginName var, don't add the '-' char for params
				String paramName = customPlugin.getPluginName() + "_" + param.getKey();
				String value = param.getValue();
				setQuotedConfigValue(paramName, value);
			}
		}
	}

	/**
	 * Set the path to content css files used to format the content.
	 * 
	 * @param cssPath
	 *            path to CSS separated by comma or NULL to not use any specific
	 *            CSS files
	 */
	private void setContentCSS(String cssPath) {
		if (cssPath != null) {
			quotedConfigValues.put(CONTENT_CSS, cssPath);
		} else {
			if (quotedConfigValues.containsKey(CONTENT_CSS)) quotedConfigValues.remove(CONTENT_CSS);
		}
	}

	/**
	 * Set the content CSS form the given theme. This will add the content.css
	 * from the default themen and override it with the current theme
	 * content.css
	 * 
	 * @param theme
	 */
	public void setContentCSSFromTheme(Theme theme) {
		// Always use default content css, then add the one from the theme
		if (theme.getIdentifyer().equals(Theme.DEFAULTTHEME)) {
			setContentCSS(theme.getBaseURI() + "content.css");
		} else {
			StringOutput cssFiles = new StringOutput();
			StaticMediaDispatcher.renderStaticURI(cssFiles, "themes/" + Theme.DEFAULTTHEME + "/content.css");
			cssFiles.append(",");
			cssFiles.append(theme.getBaseURI()).append("content.css");
			setContentCSS(cssFiles.toString());
		}
	}

	/**
	 * Set the forced root element that is entered into the edit area when the
	 * area is empty. By default this is a &lt;p&gt; element
	 * 
	 * @param rootElement
	 
	public void setForcedRootElement(String rootElement) {
		setQuotedConfigValue(FORCED_ROOT_BLOCK, rootElement);
	}*/

	/**
	 * Disable the standard root element if no such wrapper element should be
	 * created at all
	 */
	public void disableRootParagraphElement() {
		setQuotedConfigValue(FORCED_ROOT_BLOCK, FORCED_ROOT_BLOCK_VALUE_NOROOT);
	}


	/**
	 * Set the file browser callback for the given vfs container and link tree
	 * model
	 * 
	 * @param vfsContainer
	 *            The vfs container from which the files can be choosen
	 * @param customLinkTreeModel
	 *            an optional custom link tree model
	 * @param supportedImageSuffixes
	 *            Array of allowed image suffixes (jpg, png etc.)
	 * @param supportedMediaSuffixes
	 *            Array of allowed media suffixes (mov, wav etc.)
	 */
	private void setFileBrowserCallback(VFSContainer vfsContainer, CustomLinkTreeModel customLinkTreeModel, String[] supportedImageSuffixes, String[] supportedMediaSuffixes, String[] supportedFlashPlayerSuffixes) {
		// Add dom ID variable using prototype curry method
		setNonQuotedConfigValue(FILE_BROWSER_CALLBACK, FILE_BROWSER_CALLBACK_VALUE_LINK_BROWSER + ".curry('" + domID + "')");
		linkBrowserImageSuffixes = supportedImageSuffixes;
		linkBrowserMediaSuffixes = supportedMediaSuffixes;
		linkBrowserFlashPlayerSuffixes = supportedFlashPlayerSuffixes;
		linkBrowserBaseContainer = vfsContainer;
		linkBrowserCustomTreeModel = customLinkTreeModel;
	}
	
	public void disableFileBrowserCallback() {
		linkBrowserImageSuffixes = null;
		linkBrowserMediaSuffixes = null;
		linkBrowserFlashPlayerSuffixes = null;
		linkBrowserBaseContainer = null;
		linkBrowserCustomTreeModel = null;
		nonQuotedConfigValues.remove(FILE_BROWSER_CALLBACK);
	}

	/**
	 * Set an optional path relative to the vfs container of the file browser
	 * callback that is used as the upload destination when a user uploads a
	 * file and you don't whant the file to be uploaded into the vfs container
	 * itself but rather in another directory, e.g. a special media directory.
	 * 
	 * @param linkBrowserUploadRelPath
	 */
	public void setFileBrowserUploadRelPath(String linkBrowserUploadRelPath) {
		this.linkBrowserUploadRelPath = linkBrowserUploadRelPath;
	}

	/**
	 * Set the documents media base that is used to deliver media files
	 * referenced by the content.
	 * 
	 * @param documentBaseContainer
	 *            the vfs container that contains the media files
	 * @param relFilePath
	 *            The file path of the HTML file relative to the
	 *            documentBaseContainer
	 * @param usess
	 *            The user session
	 */
	private void setDocumentMediaBase(final VFSContainer documentBaseContainer, String relFilePath, UserSession usess) {
		linkBrowserRelativeFilePath = relFilePath;
		// get a usersession-local mapper for the file storage (and tinymce's references to images and such)
		Mapper contentMapper = new VFSContainerMapper(documentBaseContainer);
		// Register mapper for this user. This mapper is cleaned up in the
		// dispose method (RichTextElementImpl will clean it up)

		
		
		// Register mapper as cacheable
		String mapperID = VFSManager.getRealPath(documentBaseContainer);
		if (mapperID == null) {
			// Can't cache mapper, no cacheable context available
			contentMapperKey = CoreSpringFactory.getImpl(MapperService.class).register(usess, contentMapper);
		} else {
			// Add classname to the file path to remove conflicts with other
			// usages of the same file path
			mapperID = this.getClass().getSimpleName() + ":" + mapperID;
			contentMapperKey = CoreSpringFactory.getImpl(MapperService.class).register(usess, mapperID, contentMapper);				
		}
		
		if (relFilePath != null) {
			// remove filename, path must end with slash
			int lastSlash = relFilePath.lastIndexOf("/");
			if (lastSlash == -1) {
				relFilePath = "";
			} else if (lastSlash + 1 < relFilePath.length()) {
				relFilePath = relFilePath.substring(0, lastSlash + 1);
			} else {
				String containerPath = documentBaseContainer.getName();
				// try to get more information if it's a local folder impl
				if (documentBaseContainer instanceof LocalFolderImpl) {
					LocalFolderImpl folder = (LocalFolderImpl) documentBaseContainer;
					containerPath = folder.getBasefile().getAbsolutePath();
				}
				log.warn("Could not parse relative file path::" + relFilePath + " in container::" + containerPath);
			}
		} else {
			relFilePath = "";
			// set empty relative file path to prevent nullpointers later on
			linkBrowserRelativeFilePath = relFilePath;
		}
		String fulluri = contentMapperKey.getUrl() + "/" + relFilePath;
		setQuotedConfigValue(DOCUMENT_BASE_URL, fulluri);
	}
	
	/**
	 * Set a tiny configuration value that must be quoted with double quotes
	 * 
	 * @param key
	 *            The configuration key
	 * @param value
	 *            The configuration value
	 */
	private void setQuotedConfigValue(String key, String value) {
		// remove non-quoted config values with same key
		if (nonQuotedConfigValues.containsKey(key)) {
			nonQuotedConfigValues.remove(key);
		}
		// add or overwrite new value
		quotedConfigValues.put(key, value);
	}
	
	public void setInvalidElements(String elements) {
		setQuotedConfigValue(RichTextConfiguration.INVALID_ELEMENTS, elements);
	}
	
	public void setExtendedValidElements(String elements) {
		setQuotedConfigValue(RichTextConfiguration.EXTENDED_VALID_ELEMENTS, elements);
	}
	
	public void enableCode() {
		tinyConfig = tinyConfig.enableCode();
	}

	/**
	 * Set a tiny configuration value that must not be quoted with quotes, e.g.
	 * JS function references or boolean values
	 * 
	 * @param key
	 *            The configuration key
	 * @param value
	 *            The configuration value
	 */
	private void setNonQuotedConfigValue(String key, String value) {
		// remove quoted config values with same key
		if (quotedConfigValues.containsKey(key)) quotedConfigValues.remove(key);
		// add or overwrite new value
		nonQuotedConfigValues.put(key, value);
	}
	
	public void enableEditorHeight() {
		setNonQuotedConfigValue(RichTextConfiguration.HEIGHT, "b_initialEditorHeight()");
	}

	
	/**
	 * Get the image suffixes that are supported
	 * 
	 * @return
	 */
	public String[] getLinkBrowserImageSuffixes() {
		return linkBrowserImageSuffixes;
	}

	/**
	 * Get the media suffixes that are supported
	 * 
	 * @return
	 */
	public String[] getLinkBrowserMediaSuffixes() {
		return linkBrowserMediaSuffixes;
	}
	
	/**
	 * Get the formats supported by the flash player
	 * @return
	 */
	public String[] getLinkBrowserFlashPlayerSuffixes() {
		return linkBrowserFlashPlayerSuffixes;
	}

	/**
	 * Get the vfs base container for the file browser
	 * 
	 * @return
	 */
	public VFSContainer getLinkBrowserBaseContainer() {
		return linkBrowserBaseContainer;
	}

	/**
	 * Get the upload dir relative to the file browser
	 * @return
	 */
	public String getLinkBrowserUploadRelPath() {
		return linkBrowserUploadRelPath;
	}

	/**
	 * Get the relative file path in relation to the browser base container or
	 * an empty string when on same level as base container (e.g. in form and
	 * not file mode) or NULL when the link browser and base container are not
	 * set at all
	 * 
	 * @return
	 */
	public String getLinkBrowserRelativeFilePath() {
		return linkBrowserRelativeFilePath;
	}
	
	public String getLinkBrowserAbsolutFilePath() {
		return linkBrowserAbsolutFilePath;
	}

	public void setLinkBrowserAbsolutFilePath(String linkBrowserAbsolutFilePath) {
		this.linkBrowserAbsolutFilePath = linkBrowserAbsolutFilePath;
	}

	/**
	 * Get the optional custom link browser tree model
	 * @return the model or NULL if not defined
	 */
	public CustomLinkTreeModel getLinkBrowserCustomLinkTreeModel() {
		return linkBrowserCustomTreeModel;
	}

	protected void appendConfigToTinyJSArray_4(StringOutput out, Translator translator) {
		// Now add the quoted values
		Map<String,String> copyValues = new HashMap<String,String>(quotedConfigValues);

		// Now add the non-quoted values (e.g. true, false or functions)
		Map<String,String> copyNonValues = new HashMap<String,String>(nonQuotedConfigValues);
		String converter = copyNonValues.get(URLCONVERTER_CALLBACK);
		if(converter != null) {
			copyNonValues.put(CONVERT_URLS, "true");
		}
		
		String contentCss = copyValues.remove(CONTENT_CSS);
		if(contentCss != null) {
			// add styles from content css and add them to format menu
			copyNonValues.put(IMPORTCSS_APPEND, "true");
			copyValues.put("content_css", contentCss);
			// filter emoticons classes from content css
			copyNonValues.put(IMPORT_SELECTOR_CONVERTER, IMPORT_SELECTOR_CONVERTER_VALUE_REMOVE_EMOTICONS);
			// group imported css classes to paragraph, div, table and style menu
			copyNonValues.put(IMPORTCSS_GROUPS, IMPORTCSS_GROUPS_VALUE_MENU);
			// add css class filters if available to minimise classes the user sees
			String selectorFilter = Settings.getHtmlEditorContentCssClassPrefixes();
			if (selectorFilter != null) {
				if (selectorFilter.startsWith("/") && selectorFilter.endsWith("/")) {
					// a (multi) prefix filter witten as JS regexp pattern
					copyNonValues.put(IMPORTCSS_SELECTOR_FILTER, selectorFilter);				
				} else {
					// a simple prefix filter without JS regexp syntax
					copyValues.put(IMPORTCSS_SELECTOR_FILTER, selectorFilter);								
				}				
			}
		}

 		//new with menu
 		StringOutput tinyMenuSb = new StringOutput();
 		tinyMenuSb.append("plugins: '").append(tinyConfig.getPlugins()).append("',\n")
 		  .append("image_advtab:true,\n")
		  .append("statusbar:true,\n")
		  .append("menubar:").append(tinyConfig.hasMenu()).append(",\n");
 		
 		String leftAndClear = "Left and clear";
 		String rightAndClear = "Right and clear";
 		if(translator != null) {
 			translator = Util.createPackageTranslator(RichTextConfiguration.class, translator.getLocale(), translator);
 			leftAndClear = translator.translate("left.clear");
 			rightAndClear = translator.translate("right.clear");
 		}
 		
 		tinyMenuSb.append("image_class_list: [\n")
 		  .append("  {title: 'Left', value: 'b_float_left'},\n")
 		  .append("  {title: '").append(leftAndClear).append("', value: 'b_float_left_clear'},\n")
 		  .append("  {title: 'Center', value: 'b_centered'},\n")
 		  .append("  {title: 'Right', value: 'b_float_right'},\n")
 		  .append("  {title: '").append(rightAndClear).append("', value: 'b_float_right_clear'},\n")
 		  .append("  {title: 'Circle', value: 'b_circle'},\n")
 		  .append("  {title: 'Border', value: 'b_with_border'}\n")
 		  .append("],\n");
 		tinyMenuSb.append("link_class_list: [\n")
		  .append("  {title: '', value: ''},\n")
		  .append("  {title: 'Extern', value: 'b_link_extern'},\n")
		  .append("  {title: 'Mail', value: 'b_link_mailto'},\n")
		  .append("  {title: 'Forward', value: 'b_link_forward'}\n")
		  .append("],\n");
 		tinyMenuSb.append("table_class_list: [\n")
 		  .append("  {title: 'Borderless', value: 'b_borderless'},\n")
		  .append("  {title: 'Grid', value: 'b_grid'},\n")
		  .append("  {title: 'Border', value: 'b_border'},\n")
		  .append("  {title: 'Full', value: 'b_full'},\n")
		  .append("  {title: 'Middle', value: 'b_middle'}\n")
		  .append("],\n");
 		
		if (tinyConfig.getTool1() != null) {
			tinyMenuSb.append("toolbar1: '").append(tinyConfig.getTool1()).append("',\n");
		}
		
		if(tinyConfig.hasMenu()) {
			tinyMenuSb.append("menu:{\n");
			boolean first = true;
			for (String menuItem: tinyConfig.getMenu()) {
				if(!first) tinyMenuSb.append("\n,");
				if(first) first = false;
				tinyMenuSb.append(menuItem);
			}
			tinyMenuSb.append("\n},\n");
		}
		
		for (Map.Entry<String, String> entry : copyValues.entrySet()) {
			tinyMenuSb.append(entry.getKey()).append(": \"").append(entry.getValue()).append("\",\n");
		}
 		for (Map.Entry<String, String> entry : copyNonValues.entrySet()) {
 			tinyMenuSb.append(entry.getKey()).append(": ").append(entry.getValue()).append(",\n");
		}
 		out.append(tinyMenuSb);
	}

	/**
	 * @see org.olat.core.gui.control.Disposable#dispose()
	 */
	@Override
	public void dispose() {
		if (contentMapperKey != null) {
			CoreSpringFactory.getImpl(MapperService.class).cleanUp(Collections.singletonList(contentMapperKey));
			contentMapperKey = null;
		}		
	}
}