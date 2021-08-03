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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.controllers.linkchooser.CustomLinkTreeModel;
import org.olat.core.dispatcher.impl.StaticMediaDispatcher;
import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.dispatcher.mapper.MapperService;
import org.olat.core.dispatcher.mapper.manager.MapperKey;
import org.olat.core.gui.components.form.flexible.impl.elements.richText.RichTextElementModule.Font;
import org.olat.core.gui.components.form.flexible.impl.elements.richText.plugins.TinyMCECustomPlugin;
import org.olat.core.gui.components.form.flexible.impl.elements.richText.plugins.TinyMCECustomPluginFactory;
import org.olat.core.gui.control.Disposable;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.themes.Theme;
import org.olat.core.gui.translator.Translator;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.UserSession;
import org.olat.core.util.Util;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.filter.Filter;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSContainerMapper;
import org.olat.core.util.vfs.VFSManager;
import org.olat.modules.edusharing.EdusharingFilter;
import org.olat.modules.edusharing.EdusharingModule;
import org.olat.modules.edusharing.EdusharingProvider;

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
	private static final Logger log = Tracing.createLoggerFor(RichTextConfiguration.class);
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
	private static final String IMPORT_SELECTOR_CONVERTER_VALUE_REMOVE_EMOTICONS = "function(selector) { if (selector.indexOf('img.b_emoticons') != -1 || selector.indexOf('img.o_emoticons') != -1) {return false;} else { return this.convertSelectorToFormat(selector); }}";
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
	private static final String EXTENDED_VALID_ELEMENTS_VALUE_FULL = "script[src|type|defer],form[*],input[*],a[*],p[*],#comment[*],figure[*],figcaption,img[*],iframe[*],map[*],area[*],textentryinteraction[*]";
	private static final String MATHML_VALID_ELEMENTS = "math[*],mi[*],mn[*],mo[*],mtext[*],mspace[*],ms[*],mrow[*],mfrac[*],msqrt[*],mroot[*],merror[*],mpadded[*],mphantom[*],mfenced[*],mstyle[*],menclose[*],msub[*],msup[*],msubsup[*],munder[*],mover[*],munderover[*],mmultiscripts[*],mtable[*],mtr[*],mtd[*],maction[*]";
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
	private static final String AUTORESIZE_BOTTOM_MARGIN = "autoresize_bottom_margin";
	private static final String AUTORESIZE_MAX_HEIGHT = "autoresize_max_height";
	private static final String AUTORESIZE_MIN_HEIGHT = "autoresize_min_height";

	//
	// Generic boolean true / false values
	private static final String VALUE_FALSE = "false";

	// Callbacks
	private static final String ONCHANGE_CALLBACK = "onchange_callback";
	private static final String ONCHANGE_CALLBACK_VALUE_TEXT_AREA_ON_CHANGE = "BTinyHelper.triggerOnChangeOnFormElement";

	private static final String FILE_BROWSER_CALLBACK = "file_browser_callback";
	private static final String FILE_BROWSER_CALLBACK_VALUE_LINK_BROWSER = "BTinyHelper.openLinkBrowser";
	private static final String URLCONVERTER_CALLBACK = "urlconverter_callback";
	private static final String URLCONVERTER_CALLBACK_VALUE_BRASATO_URL_CONVERTER = "BTinyHelper.linkConverter";

	private Map<String, String> quotedConfigValues = new HashMap<>();
	private Map<String, String> nonQuotedConfigValues = new HashMap<>();
	private List<String> onInit = new ArrayList<>();

	// Supported image and media suffixes
	private static final String[] IMAGE_SUFFIXES_VALUES = { "jpg", "gif", "jpeg", "png" };
	private static final String[] MEDIA_SUFFIXES_VALUES = { "swf", "dcr", "mov", "qt", "mpg", "mp3", "mp4", "mpeg",
			"avi", "wmv", "wm", "asf", "asx", "wmx", "wvx", "rm", "ra", "ram" };
	private static final String[] FLASH_PLAYER_SUFFIXES_VALUES = { "flv", "f4v", "mp3", "mp4", "aac", "m4v", "m4a" };

	private String[] linkBrowserImageSuffixes;
	private String[] linkBrowserMediaSuffixes;
	private String[] linkBrowserFlashPlayerSuffixes;
	private VFSContainer linkBrowserBaseContainer;
	private String linkBrowserUploadRelPath;
	private String linkBrowserRelativeFilePath;
	private String linkBrowserAbsolutFilePath;
	private boolean relativeUrls = true;
	private boolean removeScriptHost = true;
	private boolean pathInStatusBar = true;
	private boolean figCaption = true;
	private boolean allowCustomMediaFactory = true;
	private boolean sendOnBlur;
	private boolean readOnly;
	private boolean filenameUriValidation = false;
	private CustomLinkTreeModel linkBrowserCustomTreeModel;
	private CustomLinkTreeModel toolLinkTreeModel;
	// DOM ID of the flexi form element
	private String domID;

	private String mapperUri;
	private MapperKey contentMapperKey;

	private final Locale locale;
	private TinyConfig tinyConfig;

	private List<TextMode> textModes = Collections.singletonList(TextMode.formatted);
	private RichTextConfigurationDelegate additionalConfiguration;

	private Collection<Filter> valueFilters = new ArrayList<>(1);

	public RichTextConfiguration(Locale locale) {
		this.locale = locale;
		tinyConfig = TinyConfig.minimalisticConfig;
	}

	/**
	 * Constructor, only used by RichText element itself. Use
	 * richtTextElement.getEditorConfiguration() to acess this object
	 * 
	 * @param domID              The ID of the flexi element in the browser DOM
	 * @param rootFormDispatchId The dispatch ID of the root form that deals with
	 *                           the submit button
	 */
	public RichTextConfiguration(String domID, Locale locale) {
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
	}

	/**
	 * Method to add the standard configuration for the form based minimal editor
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

	public void setConfigProfileFormParagraphEditor(Theme guiTheme) {
		setConfigBasics(guiTheme);
		// Add additional plugins
		TinyMCECustomPluginFactory customPluginFactory = CoreSpringFactory.getImpl(TinyMCECustomPluginFactory.class);
		List<TinyMCECustomPlugin> enabledCustomPlugins = customPluginFactory.getCustomPlugionsForProfile();
		for (TinyMCECustomPlugin tinyMCECustomPlugin : enabledCustomPlugins) {
			setCustomPluginEnabled(tinyMCECustomPlugin);
		}
		// Don't allow javascript or iframes
		setQuotedConfigValue(INVALID_ELEMENTS, INVALID_ELEMENTS_FORM_MINIMALISTIC_VALUE_UNSAVE);

		tinyConfig = TinyConfig.paragraphEditorConfig;
	}

	public void setConfigProfileFormCompactEditor(UserSession usess, Theme guiTheme, VFSContainer baseContainer) {
		setConfigBasics(guiTheme);
		// Add additional plugins
		TinyMCECustomPluginFactory customPluginFactory = CoreSpringFactory.getImpl(TinyMCECustomPluginFactory.class);
		List<TinyMCECustomPlugin> enabledCustomPlugins = customPluginFactory.getCustomPlugionsForProfile();
		for (TinyMCECustomPlugin tinyMCECustomPlugin : enabledCustomPlugins) {
			setCustomPluginEnabled(tinyMCECustomPlugin);
		}

		// Don't allow javascript or iframes, if the file browser is there allow also
		// media elements (the full values)
		setQuotedConfigValue(INVALID_ELEMENTS, (baseContainer == null ? INVALID_ELEMENTS_FORM_SIMPLE_VALUE_UNSAVE
				: INVALID_ELEMENTS_FORM_FULL_VALUE_UNSAVE));
		tinyConfig = TinyConfig.editorCompactConfig;
		setPathInStatusBar(false);

		// Setup file and link browser
		if (baseContainer != null) {
			tinyConfig = tinyConfig.enableImageAndMedia();
			setFileBrowserCallback(baseContainer, null, null, IMAGE_SUFFIXES_VALUES, MEDIA_SUFFIXES_VALUES,
					FLASH_PLAYER_SUFFIXES_VALUES);
			// since in form editor mode and not in file mode we use null as relFilePath
			setDocumentMediaBase(baseContainer, null, usess);
		}
	}

	/**
	 * Contains only the image upload and the math plugin.
	 * 
	 * @param usess
	 * @param guiTheme
	 * @param baseContainer
	 */
	public void setConfigProfileFormVeryMinimalisticConfigEditor(UserSession usess, Theme guiTheme,
			VFSContainer baseContainer, boolean withLinks) {
		setConfigBasics(guiTheme);
		// Add additional plugins
		TinyMCECustomPluginFactory customPluginFactory = CoreSpringFactory.getImpl(TinyMCECustomPluginFactory.class);
		List<TinyMCECustomPlugin> enabledCustomPlugins = customPluginFactory.getCustomPlugionsForProfile();
		for (TinyMCECustomPlugin tinyMCECustomPlugin : enabledCustomPlugins) {
			setCustomPluginEnabled(tinyMCECustomPlugin);
		}

		// Don't allow javascript or iframes, if the file browser is there allow also
		// media elements (the full values)
		setQuotedConfigValue(INVALID_ELEMENTS, (baseContainer == null ? INVALID_ELEMENTS_FORM_SIMPLE_VALUE_UNSAVE
				: INVALID_ELEMENTS_FORM_FULL_VALUE_UNSAVE));
		if (withLinks) {
			tinyConfig = TinyConfig.veryMinimalisticWithLinksConfig;
		} else {
			tinyConfig = TinyConfig.veryMinimalisticConfig;
		}
		setPathInStatusBar(false);

		// Setup file and link browser
		if (baseContainer != null) {
			tinyConfig = tinyConfig.enableImageAndMedia();
			setFileBrowserCallback(baseContainer, null, null, IMAGE_SUFFIXES_VALUES, MEDIA_SUFFIXES_VALUES,
					FLASH_PLAYER_SUFFIXES_VALUES);
			// since in form editor mode and not in file mode we use null as relFilePath
			setDocumentMediaBase(baseContainer, null, usess);
		}
	}

	/**
	 * Method to add the standard configuration for the form based simple and full
	 * editor
	 * 
	 * @param fullProfile         true: use full profile; false: use simple profile
	 * @param usess
	 * @param externalToolbar
	 * @param guiTheme
	 * @param baseContainer
	 * @param customLinkTreeModel
	 */
	public void setConfigProfileFormEditor(boolean fullProfile, UserSession usess, Theme guiTheme,
			VFSContainer baseContainer, String relFilePath, CustomLinkTreeModel customLinkTreeModel) {
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
			// Don't allow javascript or iframes, if the file browser is there allow also
			// media elements (the full values)
			setQuotedConfigValue(INVALID_ELEMENTS, (baseContainer == null ? INVALID_ELEMENTS_FORM_SIMPLE_VALUE_UNSAVE
					: INVALID_ELEMENTS_FORM_FULL_VALUE_UNSAVE));
			tinyConfig = TinyConfig.editorConfig;
		}

		// Setup file and link browser
		if (baseContainer != null) {
			tinyConfig = tinyConfig.enableImageAndMedia();
			setFileBrowserCallback(baseContainer, customLinkTreeModel, null, IMAGE_SUFFIXES_VALUES, MEDIA_SUFFIXES_VALUES,
					FLASH_PLAYER_SUFFIXES_VALUES);
			// since in form editor mode and not in file mode we use null as relFilePath
			setDocumentMediaBase(baseContainer, relFilePath, usess);
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
	 * @param toolLinkTreeModel
	 */
	public void setConfigProfileFileEditor(UserSession usess, Theme guiTheme, VFSContainer baseContainer,
			String relFilePath, CustomLinkTreeModel customLinkTreeModel, CustomLinkTreeModel toolLinkTreeModel) {
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
		setQuotedConfigValue(EXTENDED_VALID_ELEMENTS, EXTENDED_VALID_ELEMENTS_VALUE_FULL + "," + MATHML_VALID_ELEMENTS);
		setQuotedConfigValue(INVALID_ELEMENTS, INVALID_ELEMENTS_FILE_FULL_VALUE_UNSAVE);

		setNonQuotedConfigValue(PASTE_DATA_IMAGES, "true");
		// Setup file and link browser
		if (baseContainer != null) {
			setFileBrowserCallback(baseContainer, customLinkTreeModel, toolLinkTreeModel, IMAGE_SUFFIXES_VALUES,
					MEDIA_SUFFIXES_VALUES, FLASH_PLAYER_SUFFIXES_VALUES);
			setDocumentMediaBase(baseContainer, relFilePath, usess);
		}

		tinyConfig = TinyConfig.fileEditorConfig;
	}

	/**
	 * Internal helper to generate the common configurations which are used by each
	 * profile
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

	public boolean isRelativeUrls() {
		return relativeUrls;
	}

	/**
	 * If this option is set to true, all URLs returned from the MCFileManager and
	 * linkConverter will be relative from the specified document_base_url. If it's
	 * set to false all URLs will be converted to absolute URLs.
	 * 
	 * @see https://www.tinymce.com/docs/configure/url-handling/#relative_urls
	 * 
	 * @param relativeUrls
	 */
	public void setRelativeUrls(boolean relativeUrls) {
		this.relativeUrls = relativeUrls;
	}

	public boolean isRemoveScriptHost() {
		return removeScriptHost;
	}

	/**
	 * If this option is enabled the protocol and host part of the URLs returned
	 * from the MCFileManager and linkConverter will be removed. This option is only
	 * used if the relative_urls option is set to false.
	 * 
	 * @see https://www.tinymce.com/docs/configure/url-handling/#remove_script_host
	 * 
	 * @param removeScriptHost
	 */
	public void setRemoveScriptHost(boolean removeScriptHost) {
		this.removeScriptHost = removeScriptHost;
	}

	public boolean isAllowCustomMediaFactory() {
		return allowCustomMediaFactory;
	}

	public void setAllowCustomMediaFactory(boolean allowCustomMediaFactory) {
		this.allowCustomMediaFactory = allowCustomMediaFactory;
	}

	public boolean isSendOnBlur() {
		return sendOnBlur;
	}

	/**
	 * Send the content of the rich text element on blur event.
	 * 
	 * @param sendOnBlur
	 */
	public void setSendOnBlur(boolean sendOnBlur) {
		this.sendOnBlur = sendOnBlur;
	}

	public boolean isPathInStatusBar() {
		return pathInStatusBar;
	}

	public void setPathInStatusBar(boolean pathInStatusBar) {
		this.pathInStatusBar = pathInStatusBar;
	}

	public boolean isReadOnly() {
		return readOnly;
	}

	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}

	public List<TextMode> getTextModes() {
		return new ArrayList<>(textModes);
	}

	public void setSimplestTextModeAllowed(TextMode textMode) {
		if (textMode != null) {
			List<TextMode> newModes = new ArrayList<>(3);
			for (int i = textMode.ordinal(); i <= TextMode.formatted.ordinal(); i++) {
				newModes.add(TextMode.values()[i]);
			}
			textModes = newModes;
		} else {
			textModes = Collections.singletonList(TextMode.formatted);
		}
	}

	public RichTextConfigurationDelegate getAdditionalConfiguration() {
		return additionalConfiguration;
	}

	public void setAdditionalConfiguration(RichTextConfigurationDelegate additionalConfiguration) {
		this.additionalConfiguration = additionalConfiguration;
	}

	/**
	 * Add a function name that has to be executed after initialization. <br>
	 * E.g: myFunctionName, (alert('loading successfull')) <br>
	 * Don't add something like this: function() {alert('loading successfull')}, use
	 * the following notation instead: (alert('loading successfull'))
	 * 
	 * @param functionName
	 */
	public void addOnInitCallbackFunction(String functionName) {
		if (functionName != null) {
			onInit.add(functionName);
		}
	}

	protected List<String> getOnInit() {
		return onInit;
	}

	/**
	 * Enable the tabfocus plugin
	 * 
	 * if enabled its possible to enter/leave the tinyMCE-editor with TAB-key.
	 * drawback is, that you cannot enter tabs in the editor itself or navigate over
	 * buttons! see http://bugs.olat.org/jira/browse/OLAT-6242
	 * 
	 * @param tabFocusEnabled
	 */
	private void setTabFocusEnabled(boolean tabFocusEnabled) {
		if (tabFocusEnabled) {
			setQuotedConfigValue(TABFOCUS_SETTINGS, TABFOCUS_SETTINGS_PREV_NEXT);
		}
	}

	/**
	 * Configure the tinymce windowing system
	 * 
	 * @param modalWindowsEnabled true: use modal windows; false: use non-modal
	 *                            windows
	 * @param inlinePopupsEnabled true: use inline popups; false: use browser window
	 *                            popup windows
	 */
	private void setModalWindowsEnabled(boolean modalWindowsEnabled) {
		// in both cases opt in, default values are set to non-inline windows that
		// are not modal
		if (modalWindowsEnabled) {
			setQuotedConfigValue(DIALOG_TYPE, DIALOG_TYPE_VALUE_MODAL);
		}
	}

	/**
	 * Set the language for editor interface. If no translation can be found, the
	 * system fallbacks to EN
	 * 
	 * @param loc
	 */
	private void setLanguage(Locale loc) {
		// tiny does not support country or variant codes, only language code
		String langKey = loc.getLanguage();
		String path = "/static/js/tinymce4/tinymce/langs/" + langKey + ".js";
		String realPath = WebappHelper.getContextRealPath(path);
		if (realPath == null || !(new File(realPath).exists())) {
			langKey = "en";
		}
		setQuotedConfigValue(LANGUAGE, langKey);
	}

	/**
	 * Enable or disable areas in the editor content that can't be modified at all.
	 * The areas are identified with the nonEditableCSSClass.
	 * 
	 * @param noneditableContentEnabled true: use non-editable areas; false: all
	 *                                  areas are editable
	 * @param nonEditableCSSClass       the class that identifies the non-editable
	 *                                  fields or NULL to use the default value
	 *                                  'mceNonEditable'
	 */
	private void setNoneditableContentEnabled(boolean noneditableContentEnabled, String nonEditableCSSClass) {
		if (noneditableContentEnabled && nonEditableCSSClass != null
					&& !nonEditableCSSClass.equals(NONEDITABLE_NONEDITABLE_CLASS_VALUE_MCENONEDITABLE)) {
			// Add non editable class but only when it differs from the default name
			setQuotedConfigValue(NONEDITABLE_NONEDITABLE_CLASS, nonEditableCSSClass);
		}
	}

	public void disableMedia() {
		tinyConfig = tinyConfig.disableMedia();
	}

	public void disableTinyMedia() {
		tinyConfig = tinyConfig.disableTinyMedia();
	}

	public void disableMathEditor() {
		tinyConfig = tinyConfig.disableMathEditor();
	}

	public void disableImageAndMovie() {
		tinyConfig = tinyConfig.disableImageAndMedia();
	}

	public void disableMenuAndMenuBar() {
		tinyConfig = tinyConfig.disableMenuAndMenuBar();
	}

	/**
	 * Enable / disable the full-screen plugin
	 * 
	 * @param fullScreenEnabled  true: plugin enabled; false: plugin disabled
	 * @param inNewWindowEnabled true: fullscreen opens in new window; false:
	 *                           fullscreen opens in same window.
	 * @param row                The row where to place the plugin buttons
	 */
	private void setFullscreenEnabled(boolean fullScreenEnabled, boolean inNewWindowEnabled) {
		// enabled if needed, disabled by default
		if (fullScreenEnabled && inNewWindowEnabled) {
			setNonQuotedConfigValue(FULLSCREEN_NEW_WINDOW, VALUE_FALSE);
		}
	}

	/**
	 * Enable / disable the auto-resizing of the text input field. When enabled, the
	 * editor will expand the input filed until the maxHeight is reached (if set)
	 * 
	 * @param autoResizeEnabled true: enable auto-resize; false: no auto-resize
	 *                          (default)
	 * @param maxHeight         value of max height in pixels or -1 to indicate
	 *                          infinite height
	 * @param minHeight         value of min height in pixels or -1 to indicate no
	 *                          minimum height
	 * @param bottomMargin      value of bottom margin below or -1 to use html
	 *                          editor default
	 */
	public void setAutoResizeEnabled(boolean autoResizeEnabled, int maxHeight, int minHeight, int bottomMargin) {
		if (autoResizeEnabled) {
			this.tinyConfig = this.tinyConfig.enableAutoResize();
			if (maxHeight > -1) {
				setNonQuotedConfigValue(AUTORESIZE_MAX_HEIGHT, Integer.toString(maxHeight));
			}
			if (minHeight > -1) {
				setNonQuotedConfigValue(AUTORESIZE_MIN_HEIGHT, Integer.toString(minHeight));
			}
			if (bottomMargin > -1) {
				setNonQuotedConfigValue(AUTORESIZE_BOTTOM_MARGIN, Integer.toString(bottomMargin));
			}
		} else {
			this.tinyConfig = this.tinyConfig.disableAutoResize();

		}
	}

	/**
	 * Enable / disable the date and time insert plugin
	 * 
	 * @param insertDateTimeEnabled true: plugin enabled; false: plugin disabled
	 * @param locale                the locale used to format the date and time
	 * @param row                   The row where to place the plugin buttons
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
	 * @param customPlugin        the plugin
	 * @param profil              The profile in which context the plugin is used
	 */
	private void setCustomPluginEnabled(TinyMCECustomPlugin customPlugin) {
		// Add plugin specific parameters
		Map<String, String> params = customPlugin.getPluginParameters(locale);
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
	 * @param cssPath path to CSS separated by comma or NULL to not use any specific
	 *                CSS files
	 */
	private void setContentCSS(String cssPath) {
		if (cssPath != null) {
			quotedConfigValues.put(CONTENT_CSS, cssPath);
		} else {
			if (quotedConfigValues.containsKey(CONTENT_CSS))
				quotedConfigValues.remove(CONTENT_CSS);
		}
	}

	/**
	 * Set the content CSS form the given theme. This will add the content.css from
	 * the default themen and override it with the current theme content.css
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
	 * Set the forced root element that is entered into the edit area when the area
	 * is empty. By default this is a &lt;p&gt; element
	 * 
	 * @param rootElement
	 * 
	 *                    public void setForcedRootElement(String rootElement) {
	 *                    setQuotedConfigValue(FORCED_ROOT_BLOCK, rootElement); }
	 */

	/**
	 * Disable the standard root element if no such wrapper element should be
	 * created at all
	 */
	public void disableRootParagraphElement() {
		setQuotedConfigValue(FORCED_ROOT_BLOCK, FORCED_ROOT_BLOCK_VALUE_NOROOT);
	}

	/**
	 * Set the file browser callback for the given vfs container and link tree model
	 * 
	 * @param vfsContainer           The vfs container from which the files can be
	 *                               choosen
	 * @param customLinkTreeModel    an optional custom link tree model
	 * @param toolLinkTreeModel
	 * @param supportedImageSuffixes Array of allowed image suffixes (jpg, png etc.)
	 * @param supportedMediaSuffixes Array of allowed media suffixes (mov, wav etc.)
	 */
	private void setFileBrowserCallback(VFSContainer vfsContainer, CustomLinkTreeModel customLinkTreeModel,
			CustomLinkTreeModel toolLinkTreeModel, String[] supportedImageSuffixes, String[] supportedMediaSuffixes,
			String[] supportedFlashPlayerSuffixes) {
		// Add dom ID variable using prototype curry method
		setNonQuotedConfigValue(FILE_BROWSER_CALLBACK,
				FILE_BROWSER_CALLBACK_VALUE_LINK_BROWSER + ".curry('" + domID + "')");
		linkBrowserImageSuffixes = supportedImageSuffixes;
		linkBrowserMediaSuffixes = supportedMediaSuffixes;
		linkBrowserFlashPlayerSuffixes = supportedFlashPlayerSuffixes;
		linkBrowserBaseContainer = vfsContainer;
		linkBrowserCustomTreeModel = customLinkTreeModel;
		this.toolLinkTreeModel = toolLinkTreeModel;
	}

	public void disableFileBrowserCallback() {
		linkBrowserImageSuffixes = null;
		linkBrowserMediaSuffixes = null;
		linkBrowserFlashPlayerSuffixes = null;
		linkBrowserBaseContainer = null;
		linkBrowserCustomTreeModel = null;
		toolLinkTreeModel = null;
		nonQuotedConfigValues.remove(FILE_BROWSER_CALLBACK);
	}

	/**
	 * Set an optional path relative to the vfs container of the file browser
	 * callback that is used as the upload destination when a user uploads a file
	 * and you don't whant the file to be uploaded into the vfs container itself but
	 * rather in another directory, e.g. a special media directory.
	 * 
	 * @param linkBrowserUploadRelPath
	 */
	public void setFileBrowserUploadRelPath(String linkBrowserUploadRelPath) {
		this.linkBrowserUploadRelPath = linkBrowserUploadRelPath;
	}

	/**
	 * @return The URI to the mapper or null if there isn't any mapper.
	 */
	public String getMapperURI() {
		return mapperUri;
	}

	/**
	 * Set the documents media base that is used to deliver media files referenced
	 * by the content.
	 * 
	 * @param documentBaseContainer the vfs container that contains the media files
	 * @param relFilePath           The file path of the HTML file relative to the
	 *                              documentBaseContainer
	 * @param usess                 The user session
	 */
	private void setDocumentMediaBase(final VFSContainer documentBaseContainer, String relFilePath, UserSession usess) {
		linkBrowserRelativeFilePath = relFilePath;
		// get a usersession-local mapper for the file storage (and tinymce's references
		// to images and such)
		Mapper contentMapper;
		if (StringHelper.containsNonWhitespace(relFilePath)) {
			contentMapper = new RichTextContainerMapper(documentBaseContainer, relFilePath);
		} else {
			contentMapper = new VFSContainerMapper(documentBaseContainer);
		}

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
			mapperID = this.getClass().getSimpleName() + ":" + mapperID + ":" + CodeHelper.getRAMUniqueID();
			contentMapperKey = CoreSpringFactory.getImpl(MapperService.class).register(usess, mapperID, contentMapper);
		}

		if (relFilePath != null) {
			// remove filename, path must end with slash
			int lastSlash = relFilePath.lastIndexOf('/');
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
				log.warn("Could not parse relative file path::{} in container::{}", relFilePath, containerPath);
			}
		} else {
			relFilePath = "";
			// set empty relative file path to prevent nullpointers later on
			linkBrowserRelativeFilePath = relFilePath;
		}
		mapperUri = contentMapperKey.getUrl() + "/" + relFilePath;
		setQuotedConfigValue(DOCUMENT_BASE_URL, mapperUri);
	}

	/**
	 * Set a tiny configuration value that must be quoted with double quotes
	 * 
	 * @param key   The configuration key
	 * @param value The configuration value
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

	public boolean isMathEnabled() {
		return tinyConfig.isMathEnabled();
	}

	public void enableCode() {
		tinyConfig = tinyConfig.enableCode();
	}

	public void enableCharCount() {
		tinyConfig = tinyConfig.enableCharcount();
	}

	public void enableQTITools(boolean textEntry, boolean numericalInput, boolean hottext) {
		tinyConfig = tinyConfig.enableQTITools(textEntry, numericalInput, hottext);
		setQuotedConfigValue("custom_elements", "~textentryinteraction,~hottext");
		setQuotedConfigValue(EXTENDED_VALID_ELEMENTS, "script[src|type|defer],textentryinteraction[*],hottext[*]");
	}

	public void enableEdusharing(Identity identity, EdusharingProvider provider) {
		if (identity == null || provider == null)
			return;

		EdusharingModule edusharingModule = CoreSpringFactory.getImpl(EdusharingModule.class);
		if (edusharingModule.isEnabled()) {
			tinyConfig = tinyConfig.enableEdusharing();
			EdusharingFilter filter = new EdusharingFilter(identity, provider);
			addValueFilter(filter);
		}
	}

	public EdusharingFilter getEdusharingFilter() {
		for (Filter filter : valueFilters) {
			if (filter instanceof EdusharingFilter) {
				return (EdusharingFilter) filter;
			}
		}
		return null;
	}

	public void addValueFilter(Filter filter) {
		valueFilters.add(filter);
	}

	Collection<Filter> getValueFilters() {
		return valueFilters;
	}

	/**
	 * Set a tiny configuration value that must not be quoted with quotes, e.g. JS
	 * function references or boolean values
	 * 
	 * @param key   The configuration key
	 * @param value The configuration value
	 */
	private void setNonQuotedConfigValue(String key, String value) {
		// remove quoted config values with same key
		if (quotedConfigValues.containsKey(key))
			quotedConfigValues.remove(key);
		// add or overwrite new value
		nonQuotedConfigValues.put(key, value);
	}

	public void enableEditorHeight() {
		setNonQuotedConfigValue(RichTextConfiguration.HEIGHT, "b_initialEditorHeight()");
	}

	/**
	 * @return True if the fig caption for image is enabled.
	 */
	public boolean isFigCaption() {
		return figCaption;
	}

	/**
	 * Enable or disable fig caption for image.
	 * 
	 * @param figCaption
	 */
	public void setFigCaption(boolean figCaption) {
		this.figCaption = figCaption;
	}

	public boolean isFilenameUriValidation() {
		return filenameUriValidation;
	}

	/**
	 * Enable the validation of the URI for filename base on java.net.URI
	 * 
	 * @param filenameUriValidation
	 */
	public void setFilenameUriValidation(boolean filenameUriValidation) {
		this.filenameUriValidation = filenameUriValidation;
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
	 * 
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
	 * 
	 * @return
	 */
	public String getLinkBrowserUploadRelPath() {
		return linkBrowserUploadRelPath;
	}

	/**
	 * Get the relative file path in relation to the browser base container or an
	 * empty string when on same level as base container (e.g. in form and not file
	 * mode) or NULL when the link browser and base container are not set at all
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
	 * 
	 * @return the model or NULL if not defined
	 */
	public CustomLinkTreeModel getLinkBrowserCustomLinkTreeModel() {
		return linkBrowserCustomTreeModel;
	}

	public CustomLinkTreeModel getToolLinkTreeModel() {
		return toolLinkTreeModel;
	}

	protected void appendConfigToTinyJSArray_4(StringOutput out, Translator translator) {
		// Now add the quoted values
		Map<String, String> copyValues = new HashMap<>(quotedConfigValues);

		// Now add the non-quoted values (e.g. true, false or functions)
		Map<String, String> copyNonValues = new HashMap<>(nonQuotedConfigValues);
		String converter = copyNonValues.get(URLCONVERTER_CALLBACK);
		if (converter != null) {
			copyNonValues.put(CONVERT_URLS, "true");
		}

		String contentCss = copyValues.remove(CONTENT_CSS);
		if (contentCss != null) {
			// add styles from content css and add them to format menu
			copyNonValues.put(IMPORTCSS_APPEND, "true");
			copyValues.put(CONTENT_CSS, contentCss);
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
		
		RichTextElementModule tinyMceConfig = CoreSpringFactory.getImpl(RichTextElementModule.class);

		// new with menu
		StringOutput tinyMenuSb = new StringOutput();
		tinyMenuSb
			.append("plugins: '").append(tinyConfig.getPlugins()).append("',\n")
			.append("image_advtab:true,\n")
			.append("image_caption:").append(figCaption).append(",\n")
			.append("image_title:true,\n")
			.append("relative_urls:").append(isRelativeUrls()).append(",\n")
			.append("remove_script_host:").append(isRemoveScriptHost()).append(",\n")
			.append("statusbar:").append(true).append(",\n")
			.append("resize:").append(true).append(",\n")
			.append("menubar:").append(tinyConfig.hasMenu()).append(",\n")
			.append("fontsize_formats:").append("'").append(tinyMceConfig.getFontSizes()).append("',\n")
			.append("font_formats:").append("'");

		List<Font> fonts = tinyMceConfig.getFontList();
		for(int i=0; i<fonts.size(); i++) {
			if(i > 0) {
				tinyMenuSb.append(";");
			}
			Font font = fonts.get(i);
			tinyMenuSb.append(font.getName()).append("=").append(font.getAlternative());
		}
		tinyMenuSb
				.append("',\n");
		if (isReadOnly()) {
			tinyMenuSb.append("readonly: 1,\n");
		}

		String leftAndClear = "Left and clear";
		String rightAndClear = "Right and clear";
		String leftAndClearNomargin = "Left with caption";
		if (translator != null) {
			translator = Util.createPackageTranslator(RichTextConfiguration.class, translator.getLocale(), translator);
			leftAndClear = translator.translate("left.clear");
			rightAndClear = translator.translate("right.clear");
			leftAndClearNomargin = translator.translate("left.clear.nomargin");
		}

		tinyMenuSb.append("image_class_list: [\n").append("  {title: 'Left', value: 'b_float_left'},\n")
				.append("  {title: '").append(leftAndClear).append("', value: 'b_float_left_clear'},\n")
				.append("  {title: '").append(leftAndClearNomargin)
				.append("', value: 'b_float_left_clear_nomargin'},\n")
				.append("  {title: 'Center', value: 'b_centered'},\n")
				.append("  {title: 'Right', value: 'b_float_right'},\n").append("  {title: '").append(rightAndClear)
				.append("', value: 'b_float_right_clear'},\n").append("  {title: 'Circle', value: 'b_circle'},\n")
				.append("  {title: 'Border', value: 'b_with_border'}\n").append("],\n");
		tinyMenuSb.append("link_class_list: [\n").append("  {title: '', value: ''},\n")
				.append("  {title: 'Extern', value: 'b_link_extern'},\n")
				.append("  {title: 'Mail', value: 'b_link_mailto'},\n")
				.append("  {title: 'Forward', value: 'b_link_forward'}\n").append("],\n");
		// predefined table styles selectable in a menu
		tinyMenuSb.append("table_class_list: [\n").append("  {title: 'No style', value: ''},\n")
				.append("  {title: 'Default', value: 'b_default'},\n")
				.append("  {title: 'Borderless', value: 'b_borderless'},\n")
				.append("  {title: 'Grid', value: 'b_grid'},\n").append("  {title: 'Border', value: 'b_border'},\n")
				.append("  {title: 'Full', value: 'b_full'},\n").append("  {title: 'Middle', value: 'b_middle'},\n")
				.append("  {title: 'Gray', value: 'b_gray'},\n").append("  {title: 'Red', value: 'b_red'},\n")
				.append("  {title: 'Green', value: 'b_green'},\n").append("  {title: 'Blue', value: 'b_blue'},\n")
				.append("  {title: 'Yellow', value: 'b_yellow'}\n").append("],\n");

		// default table style
		tinyMenuSb.append("table_default_attributes: { class: 'b_default' },\n");
		// prevent cloning custom elements (especially QTI related)
		tinyMenuSb.append("table_clone_elements: \"strong em b i span[data-qti!='textentryinteraction'][data-qti!='hottext'] font h1 h2 h3 h4 h5 h6 p div\",\n");

		if (tinyConfig.getTool1() != null) {
			tinyMenuSb.append("toolbar1: '").append(tinyConfig.getTool1()).append("',\n");
		} else {
			tinyMenuSb.append("toolbar:false,\n");
		}

		if (tinyConfig.hasMenu()) {
			tinyMenuSb.append("menu:{\n");
			boolean first = true;
			for (String menuItem : tinyConfig.getMenu()) {
				if (!first)
					tinyMenuSb.append("\n,");
				if (first)
					first = false;
				tinyMenuSb.append(menuItem);
			}
			tinyMenuSb.append("\n},\n");
		} else {
			tinyMenuSb.append("menu:{},\n");
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