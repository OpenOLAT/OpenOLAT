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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.controllers.linkchooser.CustomLinkTreeModel;
import org.olat.core.defaults.dispatcher.ClassPathStaticDispatcher;
import org.olat.core.defaults.dispatcher.StaticMediaDispatcher;
import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.dispatcher.mapper.MapperService;
import org.olat.core.gui.components.form.flexible.impl.elements.richText.plugins.TinyMCECustomPlugin;
import org.olat.core.gui.components.form.flexible.impl.elements.richText.plugins.TinyMCECustomPluginFactory;
import org.olat.core.gui.control.Disposable;
import org.olat.core.gui.media.ClasspathMediaResource;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.NotFoundMediaResource;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.themes.Theme;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.UserSession;
import org.olat.core.util.Util;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.vfs.VFSMediaResource;

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
	public static final String MODE = "mode";
	public static final String MODE_VALUE_EXACT = "exact";
	public static final String ELEMENTS = "elements";
	public static final String MODE_VALUE_SPECIFIC_TEXTAREAS = "specific_textareas";
	public static final String EDITOR_SELECTOR = "editor_selector";
	public static final String MODE_VALUE_NONE = "none";
	// Doctype and language
	public static final String DOCTYPE = "doctype";
	public static final String DOCTYPE_VALUE_XHTML_1_0_TRANSITIONAL = "<!DOCTYPE html PUBLIC \\\"-//W3C//DTD XHTML 1.0 Transitional//EN\\\" \\\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\\\">";
	public static final String LANGUAGE = "language";
	// Layout and theme
	public static final String THEME = "theme";
	public static final String THEME_VALUE_SIMPLE = "simple";
	public static final String THEME_VALUE_ADVANCED = "advanced";
	public static final String SKIN = "skin";
	public static final String SKIN_VALUE_DEFAULT = "default";
	public static final String SKIN_VALUE_O2K7 = "o2k7";
	public static final String SKIN_VARIANT = "skin_variant";
	public static final String SKIN_VARIANT_VALUE_SILVER = "silver";
	public static final String SKIN_VARIANT_VALUE_BLACK = "black";
	public static final String BUTTON_TILE_MAP = "button_tile_map";
	public static final String CONTENT_CSS = "content_css";
	public static final String HEIGHT = "height";	
	public static final String WIDTH = "width";	
	// Window appearance
	public static final String DIALOG_TYPE = "dialog_type";
	public static final String DIALOG_TYPE_VALUE_WINDOW = "window";
	public static final String DIALOG_TYPE_VALUE_MODAL = "modal";
	// Tool and status bar
	public static final String THEME_ADVANCED_TOOLBAR_LOCATION = "theme_advanced_toolbar_location";
	public static final String THEME_ADVANCED_TOOLBAR_LOCATION_VALUE_EXTERNAL = "external";
	public static final String THEME_ADVANCED_TOOLBAR_LOCATION_VALUE_TOP = "top";
	public static final String THEME_ADVANCED_TOOLBAR_LOCATION_VALUE_BOTTOM = "bottom";
	public static final String THEME_ADVANCED_TOOLBAR_ALIGN = "theme_advanced_toolbar_align";
	public static final String THEME_ADVANCED_TOOLBAR_ALIGN_VALUE_LEFT = "left";
	public static final String THEME_ADVANCED_TOOLBAR_ALIGN_VALUE_CENTER = "center";
	public static final String THEME_ADVANCED_TOOLBAR_ALIGN_VALUE_RIGHT = "right";
	public static final String THEME_ADVANCED_STATUSBAR_LOCATION = "theme_advanced_statusbar_location";
	public static final String THEME_ADVANCED_STATUSBAR_LOCATION_VALUE_TOP = "top";
	public static final String THEME_ADVANCED_STATUSBAR_LOCATION_VALUE_BOTTOM = "bottom";
	public static final String THEME_ADVANCED_STATUSBAR_LOCATION_VALUE_NONE = "none";
	public static final String THEME_ADVANCED_PATH__VALUE_BOOLEAN = "theme_advanced_path";
	public static final String THEME_ADVANCED_DISABLE = "theme_advanced_disable";
	public static final String THEME_ADVANCED_BUTTONS3_ADD_BEFORE = "theme_advanced_buttons3_add_before";
	public static final String THEME_ADVANCED_BUTTONS3_ADD = "theme_advanced_buttons3_add";
	public static final String THEME_ADVANCED_BUTTONS3 = "theme_advanced_buttons3";
	public static final String THEME_ADVANCED_BUTTONS2_ADD_BEFORE = "theme_advanced_buttons2_add_before";
	public static final String THEME_ADVANCED_BUTTONS2_ADD = "theme_advanced_buttons2_add";
	public static final String THEME_ADVANCED_BUTTONS2 = "theme_advanced_buttons2";
	public static final String THEME_ADVANCED_BUTTONS1_ADD_BEFORE = "theme_advanced_buttons1_add_before";
	public static final String THEME_ADVANCED_BUTTONS1_ADD = "theme_advanced_buttons1_add";
	public static final String THEME_ADVANCED_BUTTONS1 = "theme_advanced_buttons1";
	// Link configuration
	public static final String THEME_ADVANCED_LINK_TARGETS = "theme_advanced_link_targets";
	// Resizeing
	public static final String THEME_ADVANCED_RESIZEING = "theme_advanced_resizing";
	public static final String THEME_ADVANCED_RESIZE_HORIZONTAL = "theme_advanced_resize_horizontal";
	public static final String AUTO_RESIZE = "auto_resize"; // does not work with XHTML
	// Plugins
	public static final String PLUGINS = "plugins";
	// Non-Editable plugin
	public static final String NONEDITABLE_PLUGIN = "noneditable";
	public static final String NONEDITABLE_NONEDITABLE_CLASS = "noneditable_noneditable_class";
	public static final String NONEDITABLE_NONEDITABLE_CLASS_VALUE_MCENONEDITABLE = "mceNonEditable";
	// Table plugin
	public static final String TABLE_PLUGIN = "table";
	public static final String TABLE_PLUGIN_BUTTONGROUP = "tablecontrols";
	public static final String TABLE_STYLES = "table_styles";
	public static final String TABLE_CELL_STYLES = "table_cell_styles";
	public static final String TABLE_ROW_STYLES = "table_row_styles";
	// Paste plugin
	public static final String PASTE_PLUGIN = "paste";
	public static final String PASTE_PLUGIN_BUTTONGROUP = "pastetext,pasteword,selectall";
	public static final String PASTE_AUTO_CLEANUP_ON_PASTE = "paste_auto_cleanup_on_paste";
	public static final String PASTE_CONVERT_MIDDOT_LISTS = "paste_convert_middot_lists";
	// Emotions Plugin
	public static final String EMOTIONS_PLUGIN = "emotions";
	public static final String EMOTIONS_PLUGIN_BUTTON = "emotions";
	// Directionality plugin
	public static final String DIRECIONALITY_PLUGIN = "directionality";
	public static final String DIRECIONALITY_PLUGIN_BUTTONGROUP = "ltr,rtl";
	// Visual characters plugin
	public static final String VISUALCHARS_PLUGIN = "visualchars";
	public static final String VISUALCHARS_PLUGIN_BUTTON = "visualchars";
	// XHTML extras plugin
	public static final String XHTMLXTRAS_PLUGIN = "xhtmlxtras";
	public static final String XHTMLXTRAS_PLUGIN_BUTTONGROUP = "cite,ins,del,abbr,actronym";
	public static final String XHTMLXTRAS_PLUGIN_BUTTON_ATTRIBS = "attribs";
	// Insert date and time plugin
	public static final String INSERTDATETIME_PLUGIN = "insertdatetime";
	public static final String INSERTDATETIME_PLUGIN_BUTTONGROUP = "insertdate,inserttime";
	public static final String INSERTDATETIME_DATEFORMAT = "plugin_insertdate_dateFormat";
	public static final String INSERTDATETIME_TIMEFORMAT = "plugin_insertdate_timeFormat";
	// Fullscreen plugin
	public static final String FULLSCREEN_PLUGIN = "fullscreen";
	public static final String FULLSCREEN_PLUGIN_BUTTON = "fullscreen";
	public static final String FULLSCREEN_NEW_WINDOW = "fullscreen_new_window";
	public static final String FULLSCREEN_SETTINGS = "fullscreen_settings";
	// Print plugin
	public static final String PRINT_PLUGIN = "print";
	public static final String PRINT_PLUGIN_BUTTON = "print";
	// Preview plugin
	public static final String PREVIEW_PLUGIN = "preview";
	public static final String PREVIEW_PLUGIN_BUTTON = "preview";
	// Save plugin
	public static final String SAVE_PLUGIN = "save";
	public static final String SAVE_ENABLEWHENDIRTY = "save_enablewhendirty";
	public static final String SAVE_PLUGIN_BUTTONGROUP = "save,cancel";
	public static final String SAVE_PLUGIN_SAVE_BUTTON = "save";
	public static final String SAVE_PLUGIN_CANCEL_BUTTON = "cancel";
	public static final String SAVE_PLUGIN_ONSAVECALLBACK = "save_onsavecallback";
	public static final String SAVE_PLUGIN_ONSAVECALLBACK_VALUE_SAVE = "BTinyHelper.triggerSave";
	// Media plugin
	public static final String MEDIA_PLUGIN = "media";
	public static final String MEDIA_PLUGIN_BUTTON = "media";
	// Advanced image plugin
	public static final String ADVIMAGE_PLUGIN = "advimage";
	// Advanced link plugin
	public static final String ADVLINK_PLUGIN = "advlink";
	// Search replace plugin
	public static final String SEARCH_REPLACE_PLUGIN = "searchreplace";
	public static final String SEARCH_REPLACE_PLUGIN_BUTTONGROUP = "search,replace";
	// Other plugins
	public static final String CONTEXTMENU_PLUGIN = "contextmenu";
	public static final String SAFARI_PLUGIN = "safari";
	public static final String INLINEPOPUPS = "inlinepopups";
	public static final String TABFOCUS_PLUGIN = "tabfocus";
	public static final String TABFOCUS_SETTINGS = "tab_focus";
	public static final String TABFOCUS_SETTINGS_PREV_NEXT = ":prev,:next";
	//
	// Gecko only features, optional
	public static final String GECKO_SPELLCHECK = "gecko_spellcheck";
	public static final String TABLE_INLINE_EDITING = "table_inline_editing";
	// Cleanup features, optional
	public static final String NOWRAP = "nowrap";
	public static final String CONVERT_FONTS_TO_SPANS = "convert_fonts_to_spans";
	public static final String CONVERT_NEWLINES_TO_BRS = "convert_newlines_to_brs";
	public static final String ACCESSIBILITY_WARNINGS = "accessibility_warnings";
	public static final String FIX_LIST_ELEMENTS = "fix_list_elements";
	public static final String FIX_TABLE_ELEMENTS = "fix_table_elements";
	public static final String FIX_NESTING = "fix_nesting";
	public static final String FORCE_P_NEWLINES = "force_p_newlines";
	public static final String REMOVE_TRAILING_NBSP = "remove_trailing_nbsp";
	public static final String TRIM_SPAN_ELEMENTS = "trim_span_elements";
	// Valid elements
	public static final String VALID_ELEMENTS = "valid_elements";
	public static final String EXTENDED_VALID_ELEMENTS = "extended_valid_elements";
	public static final String EXTENDED_VALID_ELEMENTS_VALUE_FULL = "script[src,type,defer],form[*],input[*],a[*],p[*],#comment[*],img[*],iframe[*],map[*],area[*]";
	public static final String INVALID_ELEMENTS = "invalid_elements";
	public static final String INVALID_ELEMENTS_FORM_MINIMALISTIC_VALUE_UNSAVE = "iframe,script,@[on*],object,embed";
	public static final String INVALID_ELEMENTS_FORM_SIMPLE_VALUE_UNSAVE = "iframe,script,@[on*],object,embed";
	public static final String INVALID_ELEMENTS_FORM_FULL_VALUE_UNSAVE = "iframe,script,@[on*]";
	public static final String INVALID_ELEMENTS_FORM_FULL_VALUE_UNSAVE_WITH_SCRIPT = "iframe,@[on*]";
	public static final String INVALID_ELEMENTS_FILE_FULL_VALUE_UNSAVE = "";
	// Other optional configurations, optional
	public static final String FONT_SIZE_CLASSES = "font_size_classes";
	public static final String FONT_SIZE_STYLE_VALUES = "font_size_style_values";
	public static final String INDENTATION = "indentation";
	public static final String FORCED_ROOT_BLOCK = "forced_root_block";
	public static final String FORCED_ROOT_BLOCK_VALUE_NOROOT = "";
	public static final String VERIFY_CSS_CLASSES = "verify_css_classes";
	public static final String VERIFY_HTML = "verify_html";
	public static final String PREFORMATTED = "preformatted";
	public static final String ELEMENT_FORMAT = "element_format";
	public static final String ELEMENT_FORMAT_VALUE_HTML = "html";
	public static final String ELEMENT_FORMAT_VALUE_XHTML = "xhtml";
	public static final String DIRECTIONALITY = "directionality";
	public static final String DIRECTIONALITY_VALUE_RTL = "rtl";
	public static final String DIRECTIONALITY_VALUE_LTR = "ltr";
	public static final String DOCUMENT_BASE_URL = "document_base_url";
	public static final String TINY_BASE_CONTAINER_PATH = "brasato_tiny_base_container_path";
	//
	// Generic boolean true / false values
	public static final String VALUE_TRUE = "true";
	public static final String VALUE_FALSE = "false";
	//
	// Various Buttons and groups
	public static final String BOLD_BUTTON= "bold";
	public static final String ITALIC_BUTTON= "italic";
	public static final String UNDERLINE_BUTTON= "underline";
	public static final String STRIKETHROUGH_BUTTON= "strikethrough";
	public static final String FONT_BASIC_FORMATTING_BUTTON_GROUP= "bold,italic,underline,strikethrough";
	public static final String JUSTIFYLEFT_BUTTON= "justifyleft";
	public static final String JUSTIFYCENTER_BUTTON= "justifycenter";
	public static final String JUSTIFYRIGHT= "justifyright";
	public static final String JUSTIFYFULL= "justifyfull";
	public static final String JUSTIFY_BUTTONGROUP= "justifyleft,justifycenter,justifyright,justifyfull";
	public static final String BULLIST_BUTTON= "bullist";
	public static final String NUMLIST_BUTTON= "numlist";
	public static final String LIST_BUTTON_GROUP= "bullist,numlist";
	public static final String OUTDENT_BUTTON= "outdent";
	public static final String INDENT_BUTTON= "indent";
	public static final String INDENT_OUTDENT_BUTTON_GROUP = "indent,outdent";
	public static final String CUT_BUTTON= "cut";
	public static final String COPY_BUTTON= "copy";
	public static final String PAST_BUTTON= "paste";
	public static final String COPY_PASTE_BUTTONGROUP = "cut,copy,paste";
	public static final String UNDO_BUTTON= "undo";
	public static final String REDO_BUTTON= "redo";
	public static final String UNDO_REDO_BUTTON_GROUP= "undo,redo";
	public static final String LINK_BUTTON= "link";
	public static final String UNLINK_BUTTON= "unlink";
	public static final String LINK_UNLINK_BUTTON= "link,unlink";
	public static final String IMAGE_BUTTON= "image";
	public static final String CLEANUP_BUTTON= "cleanup";
	public static final String HELP_BUTTON= "help";
	public static final String CODE_BUTTON= "code";
	public static final String HR_BUTTON= "hr";
	public static final String REMOVEFORMAT_BUTTON= "removeformat";
	public static final String FORMATSELECT_BUTTON= "formatselect";
	public static final String FONTSELECT_BUTTON= "fontselect";
	public static final String FONTSIZESELECT_BUTTON= "fontsizeselect";
	public static final String FONT_BUTTONGROUP = "fontselect,fontsizeselect";
	public static final String STYLESELECT_BUTTON= "styleselect";
	public static final String SUB_BUTTON= "sub";
	public static final String SUP_BUTTON= "sup";
	public static final String SUB_SUP_BUTTON_GRUP= "sup,sub";
	public static final String FORECOLOR_BUTTON= "forecolor";
	public static final String BACKCOLOR_BUTTON= "backcolor";
	public static final String COLOR_BUTTONGROUP = "forecolor,backcolor";
	public static final String FORECOLORPICKER_BUTTON= "forecolorpicker";
	public static final String BACKCOLORPICKER_BUTTON= "backcolorpicker";
	public static final String CHARMAP_BUTTON= "charmap";
	public static final String VISUALAID_BUTTON= "visualaid";
	public static final String ANCHOR_BUTTON = "anchor";
	public static final String BLOCKQUOTE_BUTTON = "blockquote";
	public static final String SEPARATOR_BUTTON = "separator";
	public static final String NEWDOCUMENT_BUTTON = "newdocument";
	//
	// Callbacks
	public static final String ONCHANGE_CALLBACK = "onchange_callback";
	public static final String ONCHANGE_CALLBACK_VALUE_TEXT_AREA_ON_CHANGE = "BTinyHelper.triggerOnChangeOnFormElement";
	public static final String INIT_INSTANCE_CALLBACK = "init_instance_callback";
	public static final String INIT_INSTANCE_CALLBACK_VALUE_AUTO_HIDE_TOOLBAR = "BTinyHelper.addAutohideExternalToolbarHandler";
	public static final String SETUP_CALLBACK = "setup";	
	public static final String CLEANUP_CALLBACK = "cleanup_callback";
	public static final String EXECCOMMAND_CALLBACK = "execcommand_callback";
	public static final String FILE_BROWSER_CALLBACK = "file_browser_callback";
	public static final String FILE_BROWSER_CALLBACK_VALUE_LINK_BROWSER = "BTinyHelper.openLinkBrowser";
	public static final String HANDLE_EVENT_CALLBACK = "handle_event_callback";
	public static final String HANDLE_NODE_CHANGE_CALLBACK = "handle_node_change_callback";
	public static final String ONINIT_CALLBACK = "oninit";
	public static final String ONINIT_CALLBACK_VALUE_START_DIRTY_OBSERVER = "BTinyHelper.startFormDirtyObserver";
	public static final String ONPAGELOAD_CALLBACK = "onpageload";
	public static final String REMOVE_INSTANCE_CALLBACK = "remove_instance_callback";
	public static final String SAVE_CALLBACK = "save_callback";
	public static final String SETUPCONTENT_CALLBACK = "setupcontent_callback";
	public static final String URLCONVERTER_CALLBACK = "urlconverter_callback";
	public static final String URLCONVERTER_CALLBACK_VALUE_BRASATO_URL_CONVERTER = "BTinyHelper.linkConverter";

	private Map<String, String> quotedConfigValues = new HashMap<String, String>();
	private Map<String, String> nonQuotedConfigValues = new HashMap<String, String>();
	private List<String> plugins = new ArrayList<String>();
	private List<String> oninit = new ArrayList<String>();
	private List<String> theme_advanced_buttons1 = new ArrayList<String>();
	private List<String> theme_advanced_buttons2 = new ArrayList<String>();
	private List<String> theme_advanced_buttons3 = new ArrayList<String>();
	private List<String> theme_advanced_disable = new ArrayList<String>();

	// Supported image and media suffixes
	public static final String[] IMAGE_SUFFIXES_VALUES = { "jpg", "gif", "jpeg", "png" };
	public static final String[] MEDIA_SUFFIXES_VALUES = { "swf", "dcr", "mov", "qt", "mpg", "mp3", "mp4", "mpeg", "avi", "wmv", "wm", "asf",
			"asx", "wmx", "wvx", "rm", "ra", "ram" };
	public static final String[] FLASH_PLAYER_SUFFIXES_VALUES = {"flv","f4v","mp3","mp4","aac","m4v","m4a"};
	
	// Configuration profiles when used in a form context (stored in a database, e.g. a forum posting)
	public static final int CONFIG_PROFILE_FORM_EDITOR_MINIMALISTIC = 0;
	public static final int CONFIG_PROFILE_FORM_EDITOR_SIMPLE = 1;
	public static final int CONFIG_PROFILE_FORM_EDITOR_SIMPLE_WITH_MEDIABROWSER = 2;
	public static final int CONFIG_PROFILE_FORM_EDITOR_FULL = 3;
	public static final int CONFIG_PROFILE_FORM_EDITOR_FULL_WITH_MEDIABROWSER  = 4;
	// Configuration profiles when used in a file context (stored in a file with a head and body tag)
	public static final int CONFIG_PROFILE_FILE_EDITOR_FULL = 12;
	public static final int CONFIG_PROFILE_FILE_EDITOR_FULL_WITH_MEDIABROWSER  = 13;

	// Plugin factory for loading of custom plugins
	private static final TinyMCECustomPluginFactory customPluginFactory = (TinyMCECustomPluginFactory) CoreSpringFactory.getBean(TinyMCECustomPluginFactory.class);
	private List<TinyMCECustomPlugin> enabledCustomPlugins = new ArrayList<TinyMCECustomPlugin>();
	
	private String[] linkBrowserImageSuffixes;
	private String[] linkBrowserMediaSuffixes;
	private String[] linkBrowserFlashPlayerSuffixes;
	private VFSContainer linkBrowserBaseContainer;
	private String linkBrowserUploadRelPath;
	private String linkBrowserRelativeFilePath;
	private CustomLinkTreeModel linkBrowserCustomTreeModel;	
	// DOM ID of the flexi form element
	private String domID;
	
	private Mapper contentMapper;

	/**
	 * Constructor, only used by RichText element itself. Use
	 * richtTextElement.getEditorConfiguration() to acess this object
	 * 
	 * @param domID The ID of the flexi element in the browser DOM
	 * @param rootFormDispatchId The dispatch ID of the root form that deals with the submit button
	 */
	RichTextConfiguration(String domID, String rootFormDispatchId) {
		this.domID = domID;
		// use exact mode that only applies to this DOM element
		setQuotedConfigValue(MODE, MODE_VALUE_EXACT);
		setQuotedConfigValue(ELEMENTS, domID);
		// set the on change handler to delegate to flexi element on change handler
		setQuotedConfigValue(ONCHANGE_CALLBACK, ONCHANGE_CALLBACK_VALUE_TEXT_AREA_ON_CHANGE);
		// set custom url converter to deal with framework and content urls properly
		setNonQuotedConfigValue(URLCONVERTER_CALLBACK, URLCONVERTER_CALLBACK_VALUE_BRASATO_URL_CONVERTER);
		// use modal windows, all OLAT workflows are implemented to work this way
		setModalWindowsEnabled(true, true);
		// use advanced theme per default
		setQuotedConfigValue(THEME, THEME_VALUE_ADVANCED);
		// use skinned interface, looks better than default
		setQuotedConfigValue(SKIN, SKIN_VALUE_O2K7);
		setQuotedConfigValue(SKIN_VARIANT, SKIN_VARIANT_VALUE_SILVER);
		// set default doctype
		// - no doctype is specified, see http://tinymce.moxiecode.net/punbb/viewtopic.php?id=10745
		// - related issues: OLAT-4230, OLAT-4347
		// setDocType(DOCTYPE_VALUE_XHTML_1_0_TRANSITIONAL);
		// set default plugins
		plugins.add(SAFARI_PLUGIN);
		// set base path to tiny resources
		setQuotedConfigValue(TINY_BASE_CONTAINER_PATH, ClassPathStaticDispatcher.getInstance().getMapperBasePath(RichTextConfiguration.class) + "/js/tinymce/");
		// Start observing of diry richt text element and trigger calling of setFlexiFormDirty() method
		// This check is initialized after the editor has fully loaded
		//addOnInitCallbackFunction(ONINIT_CALLBACK_VALUE_START_DIRTY_OBSERVER + ".curry('" + rootFormDispatchId + "','" + domID + "')");
		addOnInitCallbackFunction(ONINIT_CALLBACK_VALUE_START_DIRTY_OBSERVER + "('" + rootFormDispatchId + "','" + domID + "')");
	}

	/**
	 * Method to add the standard configuration for the form based minimal
	 * editor
	 * 
	 * @param usess
	 * @param externalToolbar
	 * @param guiTheme
	 */
	public void setConfigProfileFormEditorMinimalistic(UserSession usess, boolean externalToolbar, Theme guiTheme) {
		setConfigBasics(usess, externalToolbar, guiTheme);
		// Set the plugins and buttons we want to enable
		// bold, italic, underline, strikethrough, alignments, text and background color, ordered and unordered list, undo, redo 
		theme_advanced_buttons1.add(FONT_BASIC_FORMATTING_BUTTON_GROUP);
		theme_advanced_buttons1.add(SEPARATOR_BUTTON);
		theme_advanced_buttons1.add(JUSTIFY_BUTTONGROUP);
		theme_advanced_buttons1.add(SEPARATOR_BUTTON);		
		setColorsEnabled(true, 1);
		theme_advanced_buttons1.add(SEPARATOR_BUTTON);		
		theme_advanced_buttons1.add(LIST_BUTTON_GROUP);
		theme_advanced_buttons1.add(SEPARATOR_BUTTON);
		theme_advanced_buttons1.add(LINK_UNLINK_BUTTON);	// not advanced link, no onmouseover methods 
		theme_advanced_buttons1.add(SEPARATOR_BUTTON);
		theme_advanced_buttons1.add(UNDO_REDO_BUTTON_GROUP);		
		setAdvancedPasteEnabled(true, false, true, true, 1); // invisible plugin
		// Add additional plugins
		List<TinyMCECustomPlugin> enabledCustomPlugins = customPluginFactory.getCustomPlugionsForProfile(CONFIG_PROFILE_FORM_EDITOR_MINIMALISTIC);
		if (enabledCustomPlugins.size() > 0) theme_advanced_buttons1.add(SEPARATOR_BUTTON);
		for (TinyMCECustomPlugin tinyMCECustomPlugin : enabledCustomPlugins) {
			setCustomPluginEnabled(true, tinyMCECustomPlugin, CONFIG_PROFILE_FORM_EDITOR_MINIMALISTIC);
		}
		// Disable context menu
		setContextMenuEnabled(false);
		// Don't allow javascript or iframes
		setQuotedConfigValue(INVALID_ELEMENTS, INVALID_ELEMENTS_FORM_MINIMALISTIC_VALUE_UNSAVE);
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
	public void setConfigProfileFormEditor(boolean fullProfile, UserSession usess, boolean externalToolbar, Theme guiTheme, VFSContainer baseContainer, CustomLinkTreeModel customLinkTreeModel) {
		setConfigBasics(usess, externalToolbar, guiTheme);
		// Set the plugins and buttons we want to enable
		if (fullProfile) {
			// Line 1
			theme_advanced_buttons1.add(FONT_BASIC_FORMATTING_BUTTON_GROUP);
			theme_advanced_buttons1.add(SEPARATOR_BUTTON);
			theme_advanced_buttons1.add(JUSTIFY_BUTTONGROUP);
			theme_advanced_buttons1.add(SEPARATOR_BUTTON);
			theme_advanced_buttons1.add(FORMATSELECT_BUTTON);
			setFontsEnabled(true, 1);
			setColorsEnabled(true, 1);
			// Line 2
			setCopyPasteEnabled(true, 2);
			setAdvancedPasteEnabled(true, true, true, true, 2);
			theme_advanced_buttons2.add(SEPARATOR_BUTTON);
			theme_advanced_buttons2.add(LIST_BUTTON_GROUP);
			theme_advanced_buttons2.add(SEPARATOR_BUTTON);
			theme_advanced_buttons2.add(INDENT_OUTDENT_BUTTON_GROUP);
			theme_advanced_buttons2.add(SEPARATOR_BUTTON);
			theme_advanced_buttons2.add(UNDO_REDO_BUTTON_GROUP);		
			theme_advanced_buttons2.add(SEPARATOR_BUTTON);
			theme_advanced_buttons2.add(SUB_SUP_BUTTON_GRUP);
			theme_advanced_buttons2.add(SEPARATOR_BUTTON);
			theme_advanced_buttons2.add(LINK_UNLINK_BUTTON);	// not advanced link, no onmouseover methods 
			theme_advanced_buttons2.add(SEPARATOR_BUTTON);
			theme_advanced_buttons2.add(IMAGE_BUTTON); 			// not advanced images, no onmouseover methods 
			if (baseContainer != null) setMediaEnabled(true, 2);
			// Line 3
			setTableEnabled(true, 3);
			theme_advanced_buttons3.add(SEPARATOR_BUTTON);
			theme_advanced_buttons3.add(REMOVEFORMAT_BUTTON);
			theme_advanced_buttons3.add(VISUALAID_BUTTON);
			setVisualCharsEnabled(true, 3);
			theme_advanced_buttons3.add(SEPARATOR_BUTTON);
			theme_advanced_buttons3.add(HR_BUTTON);
			theme_advanced_buttons3.add(CHARMAP_BUTTON);
			// Add additional plugins
			int profile;
			if (baseContainer == null) profile = CONFIG_PROFILE_FORM_EDITOR_FULL;
			else profile = CONFIG_PROFILE_FORM_EDITOR_FULL_WITH_MEDIABROWSER;
			List<TinyMCECustomPlugin> enabledCustomPlugins = customPluginFactory.getCustomPlugionsForProfile(profile);
			for (TinyMCECustomPlugin tinyMCECustomPlugin : enabledCustomPlugins) {
				setCustomPluginEnabled(true, tinyMCECustomPlugin, profile);
			}		
			// Don't allow javascript or iframes			
			setQuotedConfigValue(INVALID_ELEMENTS, INVALID_ELEMENTS_FORM_FULL_VALUE_UNSAVE);
		} else {
			// Line 1
			theme_advanced_buttons1.add(FONT_BASIC_FORMATTING_BUTTON_GROUP);
			theme_advanced_buttons1.add(SEPARATOR_BUTTON);
			theme_advanced_buttons1.add(JUSTIFY_BUTTONGROUP);
			theme_advanced_buttons1.add(FORMATSELECT_BUTTON);
			setFontsEnabled(true, 1);
			setColorsEnabled(true, 1);
			// Line 2
			setAdvancedPasteEnabled(true, false, true, true,2); // invisible plugin
			theme_advanced_buttons2.add(LIST_BUTTON_GROUP);
			theme_advanced_buttons2.add(SEPARATOR_BUTTON);
			theme_advanced_buttons2.add(INDENT_OUTDENT_BUTTON_GROUP);
			theme_advanced_buttons2.add(SEPARATOR_BUTTON);
			theme_advanced_buttons2.add(UNDO_REDO_BUTTON_GROUP);		
			theme_advanced_buttons2.add(SEPARATOR_BUTTON);
			theme_advanced_buttons2.add(REMOVEFORMAT_BUTTON);
			setVisualCharsEnabled(true, 2);
			theme_advanced_buttons2.add(SEPARATOR_BUTTON);
			theme_advanced_buttons2.add(SUB_SUP_BUTTON_GRUP);
			theme_advanced_buttons2.add(SEPARATOR_BUTTON);
			theme_advanced_buttons2.add(HR_BUTTON);
			theme_advanced_buttons2.add(CHARMAP_BUTTON);
			theme_advanced_buttons2.add(SEPARATOR_BUTTON);
			theme_advanced_buttons2.add(LINK_UNLINK_BUTTON);	// not advanced link, no onmouseover methods 
			theme_advanced_buttons2.add(SEPARATOR_BUTTON);
			theme_advanced_buttons2.add(IMAGE_BUTTON); 			// not advanced images, no onmouseover methods 
			if (baseContainer != null) setMediaEnabled(true, 2);
			// Add additional plugins
			int profile;
			if (baseContainer == null) profile =  CONFIG_PROFILE_FORM_EDITOR_SIMPLE;
			else profile = CONFIG_PROFILE_FORM_EDITOR_SIMPLE_WITH_MEDIABROWSER;
			List<TinyMCECustomPlugin> enabledCustomPlugins = customPluginFactory.getCustomPlugionsForProfile(profile);
			for (TinyMCECustomPlugin tinyMCECustomPlugin : enabledCustomPlugins) {
				setCustomPluginEnabled(true, tinyMCECustomPlugin, profile);
			}		
			// Don't allow javascript or iframes, if the file browser is there allow also media elements (the full values)
			setQuotedConfigValue(INVALID_ELEMENTS, (baseContainer == null ? INVALID_ELEMENTS_FORM_SIMPLE_VALUE_UNSAVE : INVALID_ELEMENTS_FORM_FULL_VALUE_UNSAVE));
		}
		// Setup file and link browser
		if (baseContainer != null) {
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
	public void setConfigProfileFileEditor(UserSession usess, boolean externalToolbar, Theme guiTheme, VFSContainer baseContainer, String relFilePath, CustomLinkTreeModel customLinkTreeModel) {
		setConfigBasics(usess, externalToolbar, guiTheme);
		// Line 1
		setSaveButtonEnabled(true, 1);
		theme_advanced_buttons1.add(FONT_BASIC_FORMATTING_BUTTON_GROUP);
		theme_advanced_buttons1.add(SEPARATOR_BUTTON);
		theme_advanced_buttons1.add(JUSTIFY_BUTTONGROUP);
		theme_advanced_buttons1.add(SEPARATOR_BUTTON);
		theme_advanced_buttons1.add(STYLESELECT_BUTTON);
		theme_advanced_buttons1.add(FORMATSELECT_BUTTON);
		setFontsEnabled(true, 1);
		setColorsEnabled(true, 1);
		// Line 2
		setCopyPasteEnabled(true, 2);
		setAdvancedPasteEnabled(true, true, true, true,2);
		theme_advanced_buttons2.add(SEPARATOR_BUTTON);
		setSearchReplaceEnabled(true, 2);
		theme_advanced_buttons2.add(SEPARATOR_BUTTON);
		theme_advanced_buttons2.add(LIST_BUTTON_GROUP);
		theme_advanced_buttons2.add(SEPARATOR_BUTTON);
		theme_advanced_buttons2.add(INDENT_OUTDENT_BUTTON_GROUP);
		theme_advanced_buttons2.add(SEPARATOR_BUTTON);
		theme_advanced_buttons2.add(UNDO_REDO_BUTTON_GROUP);		
		theme_advanced_buttons2.add(SEPARATOR_BUTTON);
		setXhtmlXtrasEnabled(true, true, 2);
		theme_advanced_buttons2.add(SEPARATOR_BUTTON);
		theme_advanced_buttons2.add(SUB_SUP_BUTTON_GRUP);
		theme_advanced_buttons2.add(SEPARATOR_BUTTON);
		setAdvancedLinkEditingEnabled(true, 2);
		theme_advanced_buttons2.add(ANCHOR_BUTTON);
		theme_advanced_buttons2.add(SEPARATOR_BUTTON);
		setAdvancedImageEditingEnabled(true, 2);
		setMediaEnabled(true, 2);
		// Line 3
		setTableEnabled(true, 3);
		theme_advanced_buttons3.add(SEPARATOR_BUTTON);
		theme_advanced_buttons3.add(REMOVEFORMAT_BUTTON);
		theme_advanced_buttons3.add(CLEANUP_BUTTON);			
		theme_advanced_buttons3.add(VISUALAID_BUTTON);
		setVisualCharsEnabled(true, 3);
		theme_advanced_buttons3.add(SEPARATOR_BUTTON);
		setPrintEnabled(true, 3);
		setFullscreenEnabled(true, false, 3);
		theme_advanced_buttons3.add(CODE_BUTTON);		
		theme_advanced_buttons3.add(SEPARATOR_BUTTON);
		setInsertDateTimeEnabled(true, usess.getLocale(), 3);
		theme_advanced_buttons3.add(HR_BUTTON);
		theme_advanced_buttons3.add(CHARMAP_BUTTON);
		// Plugins without buttons
		setNoneditableContentEnabled(true, null);
		setContextMenuEnabled(true);		
		// Add additional plugins
		int profile;
		if (baseContainer == null) profile = CONFIG_PROFILE_FILE_EDITOR_FULL;
		else profile = CONFIG_PROFILE_FILE_EDITOR_FULL_WITH_MEDIABROWSER;
		List<TinyMCECustomPlugin> enabledCustomPlugins = customPluginFactory.getCustomPlugionsForProfile(profile);
		for (TinyMCECustomPlugin tinyMCECustomPlugin : enabledCustomPlugins) {
			setCustomPluginEnabled(true, tinyMCECustomPlugin, profile);
		}
		
		// Allow editing of all kind of HTML elements and attributes
		setQuotedConfigValue(EXTENDED_VALID_ELEMENTS, EXTENDED_VALID_ELEMENTS_VALUE_FULL);
		setQuotedConfigValue(INVALID_ELEMENTS, INVALID_ELEMENTS_FILE_FULL_VALUE_UNSAVE);

		
		// Setup file and link browser
		if (baseContainer != null) {
			setFileBrowserCallback(baseContainer, customLinkTreeModel, IMAGE_SUFFIXES_VALUES, MEDIA_SUFFIXES_VALUES, FLASH_PLAYER_SUFFIXES_VALUES);
			setDocumentMediaBase(baseContainer, relFilePath, usess);			
		}
	}

	/**
	 * Internal helper to generate the common configurations which are used by
	 * each profile
	 * 
	 * @param usess
	 * @param externalToolbar
	 * @param guiTheme
	 */
	private void setConfigBasics(UserSession usess, boolean externalToolbar, Theme guiTheme) {
		// Toolbar and resize configuration
		setToolbar(externalToolbar ? THEME_ADVANCED_TOOLBAR_LOCATION_VALUE_EXTERNAL : THEME_ADVANCED_TOOLBAR_LOCATION_VALUE_TOP, true,
				THEME_ADVANCED_TOOLBAR_ALIGN_VALUE_LEFT);
		setResizeingEnabled(true);
		// Use users current language
		Locale loc = I18nManager.getInstance().getCurrentThreadLocale();
		setLanguage(loc);
		// Use theme content css
		setContentCSSFromTheme(guiTheme);
		// Set link targets
		Translator trans = Util.createPackageTranslator(this.getClass(), loc);
		String sameWinTranslated = trans.translate("richText.element.target.window.alwayssame");
		setLinkTargets(sameWinTranslated + "=_olatpopup");		
		// Plugins without buttons
		setNoneditableContentEnabled(true, null);
		setContextMenuEnabled(true);
		setTabFocusEnabled(true);
	}

	
	/*
	 * Use methods below to do custom configurations
	 */
	
	
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

	/**
	 * Enable the tabfocus plugin
	 * 
	 * if enabled its possible to enter/leave the tinyMCE-editor with TAB-key.
	 * drawback is, that you cannot enter tabs in the editor itself or navigate over buttons!
	 * see http://bugs.olat.org/jira/browse/OLAT-6242
	 * @param tabFocusEnabled
	 */
	public void setTabFocusEnabled(boolean tabFocusEnabled){
		if (tabFocusEnabled){
			setQuotedConfigValue(TABFOCUS_SETTINGS, TABFOCUS_SETTINGS_PREV_NEXT);
			plugins.add(TABFOCUS_PLUGIN);
		} else {
			plugins.remove(TABFOCUS_PLUGIN);
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
	public void setModalWindowsEnabled(boolean modalWindowsEnabled, boolean inlinePopupsEnabled) {
		// in both cases opt in, default values are set to non-inline windows that
		// are not modal
		if (modalWindowsEnabled) {
			setQuotedConfigValue(DIALOG_TYPE, DIALOG_TYPE_VALUE_MODAL);
		}
		if (inlinePopupsEnabled) {
			plugins.add(INLINEPOPUPS);
		} else {
			plugins.remove(INLINEPOPUPS);
		}
	}

	/**
	 * Configure the editor toolbar
	 * 
	 * @param position
	 *            The location of the toolbar. Use
	 *            THEME_ADVANCED_TOOLBAR_LOCATION_* values
	 * @param autoHide
	 *            true: try to hide toolbar when mouse leafs the editor area;
	 *            false, never hide toolbar (only applicable when position is
	 *            external)
	 * @param alignment
	 *            The alignment of the toolbar, use
	 *            THEME_ADVANCED_TOOLBAR_ALIGN_VALUE_* values
	 */
	public void setToolbar(String position, boolean autoHide, String alignment) {
		if (position.equals(THEME_ADVANCED_TOOLBAR_LOCATION_VALUE_EXTERNAL) || position.equals(THEME_ADVANCED_TOOLBAR_LOCATION_VALUE_BOTTOM)
				|| position.equals(THEME_ADVANCED_TOOLBAR_LOCATION_VALUE_TOP)) {
			// valid config
			setQuotedConfigValue(THEME_ADVANCED_TOOLBAR_LOCATION, position);
			if (position.equals(THEME_ADVANCED_TOOLBAR_LOCATION_VALUE_EXTERNAL) && autoHide) {
				// set the on-init callback and add the auto-hide toolbar listener
				//setNonQuotedConfigValue(INIT_INSTANCE_CALLBACK, INIT_INSTANCE_CALLBACK_VALUE_AUTO_HIDE_TOOLBAR + ".curry('" + domID + "')");
				setNonQuotedConfigValue(INIT_INSTANCE_CALLBACK, INIT_INSTANCE_CALLBACK_VALUE_AUTO_HIDE_TOOLBAR + "('" + domID + "')");
			}
		} else {
			throw new AssertException("Invalid configuration parameters, use RichTextConfigurationConstants");
		}
		// add alignment config
		if (alignment.equals(THEME_ADVANCED_TOOLBAR_ALIGN_VALUE_LEFT) || alignment.equals(THEME_ADVANCED_TOOLBAR_ALIGN_VALUE_CENTER)
				|| alignment.equals(THEME_ADVANCED_TOOLBAR_ALIGN_VALUE_RIGHT)) {
			// valid config
			setQuotedConfigValue(THEME_ADVANCED_TOOLBAR_ALIGN, alignment);
		} else {
			throw new AssertException("Invalid configuration parameters, use RichTextConfigurationConstants");
		}
	}

	/**
	 * Enable or disable editor area resizeing
	 * 
	 * @param resizeingEnabled
	 */
	public void setResizeingEnabled(boolean resizeingEnabled) {
		if (resizeingEnabled) {
			// enable resizeing, but limit to vertical resizeing
			setNonQuotedConfigValue(THEME_ADVANCED_RESIZEING, VALUE_TRUE);
			setNonQuotedConfigValue(THEME_ADVANCED_RESIZE_HORIZONTAL, VALUE_FALSE);
		} else {
			setNonQuotedConfigValue(THEME_ADVANCED_RESIZEING, VALUE_FALSE);
		}
		// Status bar needed for resizeing, but handy in any case
		setQuotedConfigValue(THEME_ADVANCED_STATUSBAR_LOCATION, THEME_ADVANCED_STATUSBAR_LOCATION_VALUE_BOTTOM);
	}

	/**
	 * Set a specific doctype. Note that this can trigger buggy IE effects.
	 * Don't use this unless you really need this!
	 * <br>
	 * Regardless of the doctype, tiny will always generate XHTML code.
	 * 
	 * @param docType
	 */
	public void setDocType(String docType) {
		setQuotedConfigValue(DOCTYPE, docType);
		if (docType.equals(DOCTYPE_VALUE_XHTML_1_0_TRANSITIONAL)) {
			// use tile images for faster loading, does not work with all doctypes
			setNonQuotedConfigValue(BUTTON_TILE_MAP, VALUE_TRUE);
		}
	}

	/**
	 * Set the language for editor interface. If no translation can be found,
	 * the system fallbacks to EN
	 * 
	 * @param loc
	 */
	public void setLanguage(Locale loc) {
		// tiny does not support country or vairant codes, only language code
		String langKey = loc.getLanguage();
		ClasspathMediaResource resource = new ClasspathMediaResource(this.getClass(), "_static/js/tinymce/langs/" + langKey + ".js");
		if (resource.getInputStream() == null) {
			// fallback to EN
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
	public void setNoneditableContentEnabled(boolean noneditableContentEnabled, String nonEditableCSSClass) {
		if (noneditableContentEnabled) {
			plugins.add(NONEDITABLE_PLUGIN);
			if (nonEditableCSSClass != null && !nonEditableCSSClass.equals(NONEDITABLE_NONEDITABLE_CLASS_VALUE_MCENONEDITABLE)) {
				// Add non editable class but only when it differs from the default name
				setQuotedConfigValue(NONEDITABLE_NONEDITABLE_CLASS, nonEditableCSSClass);
			}
		} else {
			plugins.remove(NONEDITABLE_PLUGIN);
		}
	}

	/**
	 * Enable / disable the table plugin
	 * 
	 * @param tableEnabled
	 *            true: plugin enabled; false: plugin disabled
	 * @param row
	 *            The row where to place the plugin buttons
	 */
	public void setTableEnabled(boolean tableEnabled, int row) {
		List<String> buttonRow = getButtonRowFor(row);
		if (tableEnabled) {
			plugins.add(TABLE_PLUGIN);
			buttonRow.add(TABLE_PLUGIN_BUTTONGROUP);
		} else {
			plugins.remove(TABLE_PLUGIN);
			buttonRow.remove(TABLE_PLUGIN_BUTTONGROUP);
		}
	}

	/**
	 * Enable / disable the advanced copy/past plugin
	 * 
	 * @param pasteEnabled
	 *            true: plugin enabled; false: plugin disabled
	 * @param showPasteButtons
	 *            true: show past buttons; false: hide past buttons (paste via
	 *            keyboard shortcuts still works
	 * @param cleanupOnPastEnabled
	 *            true: cleanup pasted code before inserting into the DOM;
	 *            false: don't cleanup
	 * @param convertWordLists
	 *            true: also convert word list; false: don't convert word lists
	 * @param row
	 *            The row where to place the plugin buttons
	 */
	public void setAdvancedPasteEnabled(boolean pasteEnabled, boolean showPasteButtons, boolean cleanupOnPastEnabled, boolean convertWordLists, int row) {
		List<String> buttonRow = getButtonRowFor(row);
		if (pasteEnabled) {
			plugins.add(PASTE_PLUGIN);
			if (showPasteButtons) buttonRow.add(PASTE_PLUGIN_BUTTONGROUP);
			// enabled by default, disable on request
			if (!cleanupOnPastEnabled) setNonQuotedConfigValue(PASTE_AUTO_CLEANUP_ON_PASTE, VALUE_FALSE);
			if (!convertWordLists) setNonQuotedConfigValue(PASTE_AUTO_CLEANUP_ON_PASTE, VALUE_FALSE);
		} else {
			plugins.remove(PASTE_PLUGIN);
			buttonRow.remove(PASTE_PLUGIN_BUTTONGROUP);
		}
	}
	
	/**
	 * Enable / disable the media plugin
	 * 
	 * @param mediaEnabled
	 *            true: plugin enabled; false: plugin disabled
	 * @param row
	 *            The row where to place the plugin buttons
	 */
	public void setMediaEnabled(boolean mediaEnabled, int row) {
		List<String> buttonRow = getButtonRowFor(row);
		if (mediaEnabled) {
			plugins.add(MEDIA_PLUGIN);
			buttonRow.add(MEDIA_PLUGIN_BUTTON);
		} else {
			plugins.remove(MEDIA_PLUGIN);
			buttonRow.remove(MEDIA_PLUGIN_BUTTON);
		}
	}
	
	/**
	 * Enable / disable the advanced image plugin
	 * 
	 * @param advimageEnabled
	 *            true: plugin enabled; false: plugin disabled
	 * @param row
	 *            The row where to place the plugin buttons
	 */
	public void setAdvancedImageEditingEnabled(boolean advimageEnabled, int row) {
		List<String> buttonRow = getButtonRowFor(row);
		if (advimageEnabled) {
			buttonRow.add(IMAGE_BUTTON);
			plugins.add(ADVIMAGE_PLUGIN);
		} else {
			buttonRow.remove(IMAGE_BUTTON);
			plugins.remove(ADVIMAGE_PLUGIN);
		}		
	}

	/**
	 * Enable / disable the advanced link plugin
	 * 
	 * @param advlinkEnabled
	 *            true: plugin enabled; false: plugin disabled
	 * @param row
	 *            The row where to place the plugin buttons
	 */
	public void setAdvancedLinkEditingEnabled(boolean advlinkEnabled, int row) {
		List<String> buttonRow = getButtonRowFor(row);
		if (advlinkEnabled) {
			buttonRow.add(LINK_UNLINK_BUTTON);
			plugins.add(ADVLINK_PLUGIN);
		} else {
			buttonRow.add(LINK_UNLINK_BUTTON);
			plugins.remove(ADVLINK_PLUGIN);
		}		
	}

	/**
	 * Enable / disable the context menu plugin
	 * 
	 * @param contextMenuEnabled
	 *            true: plugin enabled; false: plugin disabled
	 */
	public void setContextMenuEnabled(boolean contextMenuEnabled) {
		if (contextMenuEnabled) {
			plugins.add(CONTEXTMENU_PLUGIN);
		} else {
			plugins.remove(CONTEXTMENU_PLUGIN);
		}
	}

	/**
	 * Enable / disable the directionality plugin for RTL / LRT support
	 * 
	 * @param directionalityEnabled
	 *            true: plugin enabled; false: plugin disabled
	 * @param row
	 *            The row where to place the plugin buttons
	 */
	public void setDirectionalityEnabled(boolean directionalityEnabled, int row) {
		List<String> buttonRow = getButtonRowFor(row);
		if (directionalityEnabled) {
			plugins.add(DIRECIONALITY_PLUGIN);
			buttonRow.add(DIRECIONALITY_PLUGIN_BUTTONGROUP);
		} else {
			plugins.remove(DIRECIONALITY_PLUGIN);
			buttonRow.remove(DIRECIONALITY_PLUGIN_BUTTONGROUP);
		}
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
	public void setFullscreenEnabled(boolean fullScreenEnabled, boolean inNewWindowEnabled, int row) {
		List<String> buttonRow = getButtonRowFor(row);
		if (fullScreenEnabled) {
			plugins.add(FULLSCREEN_PLUGIN);
			buttonRow.add(FULLSCREEN_PLUGIN_BUTTON);
			// enabled if needed, disabled by default
			if (inNewWindowEnabled) setNonQuotedConfigValue(FULLSCREEN_NEW_WINDOW, VALUE_FALSE);
		} else {
			plugins.remove(FULLSCREEN_PLUGIN);
			buttonRow.remove(FULLSCREEN_PLUGIN_BUTTON);
		}
	}

	/**
	 * Enable / disable the visual characters plugin
	 * 
	 * @param visualCharsEnabled
	 *            true: plugin enabled; false: plugin disabled
	 * @param row
	 *            The row where to place the plugin buttons
	 */
	public void setVisualCharsEnabled(boolean visualCharsEnabled, int row) {
		List<String> buttonRow = getButtonRowFor(row);
		if (visualCharsEnabled) {
			plugins.add(VISUALCHARS_PLUGIN);
			buttonRow.add(VISUALCHARS_PLUGIN_BUTTON);
		} else {
			plugins.remove(VISUALCHARS_PLUGIN);
			buttonRow.remove(VISUALCHARS_PLUGIN_BUTTON);
		}
	}

	/**
	 * Enable / disable the print plugin
	 * 
	 * @param printEnabled
	 *            true: plugin enabled; false: plugin disabled
	 * @param row
	 *            The row where to place the plugin buttons
	 */
	public void setPrintEnabled(boolean printEnabled, int row) {
		List<String> buttonRow = getButtonRowFor(row);
		if (printEnabled) {
			plugins.add(PRINT_PLUGIN);
			buttonRow.add(PRINT_PLUGIN_BUTTON);
		} else {
			plugins.remove(PRINT_PLUGIN);
			buttonRow.remove(PRINT_PLUGIN_BUTTON);
		}
	}

	/**
	 * Enable / disable the preview plugin. Be aware that this can cause side
	 * effects when used with special link providers! Do not use if not really
	 * necessary
	 * 
	 * @param previewEnabled
	 *            true: plugin enabled; false: plugin disabled
	 * @param row
	 *            The row where to place the plugin buttons
	 */
	public void setPreviewEnabled(boolean previewEnabled, int row) {
		List<String> buttonRow = getButtonRowFor(row);
		if (previewEnabled) {
			plugins.add(PREVIEW_PLUGIN);
			buttonRow.add(PREVIEW_PLUGIN_BUTTON);
		} else {
			plugins.remove(PREVIEW_PLUGIN);
			buttonRow.remove(PREVIEW_PLUGIN_BUTTON);
		}
	}

	/**
	 * Enable / disable the fonts buttons
	 * 
	 * @param fontsEnabled
	 *            true: show buttons; false: hide buttons
	 * @param row
	 *            The row where to place the buttons
	 */
	public void setFontsEnabled(boolean fontsEnabled, int row) {
		List<String> buttonRow = getButtonRowFor(row);
		if (fontsEnabled) {
			buttonRow.add(FONT_BUTTONGROUP);
		} else {
			buttonRow.remove(FONT_BUTTONGROUP);
		}
	}

	/**
	 * Enable / disable the colors buttons
	 * 
	 * @param colorsEnabled
	 *            true: show buttons; false: hide buttons
	 * @param row
	 *            The row where to place the buttons
	 */
	public void setColorsEnabled(boolean colorsEnabled, int row) {
		List<String> buttonRow = getButtonRowFor(row);
		if (colorsEnabled) {
			buttonRow.add(COLOR_BUTTONGROUP);
		} else {
			buttonRow.remove(COLOR_BUTTONGROUP);
		}
	}

	/**
	 * Enable / disable the copy&paste buttons
	 * 
	 * @param copyPasteEnabled
	 *            true: show buttons; false: hide buttons
	 * @param row
	 *            The row where to place the buttons
	 */
	public void setCopyPasteEnabled(boolean copyPasteEnabled, int row) {
		List<String> buttonRow = getButtonRowFor(row);
		if (copyPasteEnabled) {
			buttonRow.add(COPY_PASTE_BUTTONGROUP);
		} else {
			buttonRow.remove(COPY_PASTE_BUTTONGROUP);
		}
	}

	/**
	 * Enable / disable the search & replace plugin
	 * 
	 * @param searchReplaceEnabled
	 *            true: plugin enabled; false: plugin disabled
	 * @param row
	 *            The row where to place the plugin buttons
	 */
	public void setSearchReplaceEnabled(boolean searchReplaceEnabled, int row) {
		List<String> buttonRow = getButtonRowFor(row);
		if (searchReplaceEnabled) {
			plugins.add(SEARCH_REPLACE_PLUGIN);
			buttonRow.add(SEARCH_REPLACE_PLUGIN_BUTTONGROUP);
		} else {
			plugins.remove(SEARCH_REPLACE_PLUGIN);
			buttonRow.remove(SEARCH_REPLACE_PLUGIN_BUTTONGROUP);
		}
	}

	/**
	 * Enable / disable the xhtml extras plugin
	 * 
	 * @param xhtmlXtrasEnabled
	 *            true: plugin enabled; false: plugin disabled
	 * @param elementAttributesEnabled
	 *            true: also show the element attributes view; false: don't use
	 *            the elements attributes view
	 * @param row
	 *            The row where to place the plugin buttons
	 */
	public void setXhtmlXtrasEnabled(boolean xhtmlXtrasEnabled, boolean elementAttributesEnabled, int row) {
		List<String> buttonRow = getButtonRowFor(row);
		if (xhtmlXtrasEnabled) {
			plugins.add(XHTMLXTRAS_PLUGIN);
			buttonRow.add(XHTMLXTRAS_PLUGIN_BUTTONGROUP);
			// enable also element attribute inspector
			if (elementAttributesEnabled) buttonRow.add(XHTMLXTRAS_PLUGIN_BUTTON_ATTRIBS);
		} else {
			plugins.remove(XHTMLXTRAS_PLUGIN);
			buttonRow.remove(XHTMLXTRAS_PLUGIN_BUTTONGROUP);
			buttonRow.remove(XHTMLXTRAS_PLUGIN_BUTTON_ATTRIBS);
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
	public void setInsertDateTimeEnabled(boolean insertDateTimeEnabled, Locale locale, int row) {
		List<String> buttonRow = getButtonRowFor(row);
		if (insertDateTimeEnabled) {
			plugins.add(INSERTDATETIME_PLUGIN);
			buttonRow.add(INSERTDATETIME_PLUGIN_BUTTONGROUP);
			// use date format defined in org.olat.core package
			Formatter formatter = Formatter.getInstance(locale);
			String dateFormat = formatter.getSimpleDatePatternForDate();
			setQuotedConfigValue(INSERTDATETIME_DATEFORMAT, dateFormat);
			// for the time format we use the standard format (same for all languages)
		} else {
			plugins.remove(INSERTDATETIME_PLUGIN);
			buttonRow.remove(INSERTDATETIME_PLUGIN_BUTTONGROUP);
		}
	}
	
	/**
	 * Enable / disable the save & cancel plugin
	 * 
	 * @param saveButtonEnabled
	 *            true: plugin enabled; false: plugin disabled
	 * @param row
	 *            The row where to place the plugin buttons
	 */
	public void setSaveButtonEnabled(boolean saveButtonEnabled, int row) {
		List<String> buttonRow = getButtonRowFor(row);
		if (saveButtonEnabled) {
			plugins.add(SAVE_PLUGIN);
			buttonRow.add(SAVE_PLUGIN_BUTTONGROUP);
			setNonQuotedConfigValue(SAVE_ENABLEWHENDIRTY, VALUE_TRUE);
		} else {
			plugins.remove(SAVE_PLUGIN);
			buttonRow.remove(SAVE_PLUGIN_BUTTONGROUP);
			nonQuotedConfigValues.remove(SAVE_ENABLEWHENDIRTY);
		}
	}

	/**
	 * Enable / disable a TinyMCECustomPlugin plugin
	 * 
	 * @param customPluginEnabled
	 *            true: plugin enabled; false: plugin disabled
	 * @param customPlugin
	 *            the plugin
	 * @param profil
	 *            The profile in which context the plugin is used
	 */
	public void setCustomPluginEnabled(boolean customPluginEnabled, TinyMCECustomPlugin customPlugin, int profil) {
		// custom plugins must have a '-' in front of the plugin name
		String pluginName = "-" + customPlugin.getPluginName();
		String buttons = customPlugin.getPluginButtons();
		int row = customPlugin.getPluginButtonsRowForProfile(profil);
		if (customPluginEnabled) {
			enabledCustomPlugins.add(customPlugin);
			plugins.add(pluginName);
			// Now add the buttons provided by the plugin
			if (StringHelper.containsNonWhitespace(buttons)) {
				List<String> buttonRow = getButtonRowFor(row);
				buttonRow.add(buttons);
			}
			// Add plugin specific parameters
			Map<String,String> params = customPlugin.getPluginParameters();
			if (params != null) {
				for (Entry<String, String> param : params.entrySet()) {
					// don't use pluginName var, don't add the '-' char for params
					String paramName = customPlugin.getPluginName() + "_" + param.getKey();
					String value = param.getValue();
					setQuotedConfigValue(paramName, value);
				}
			}
		} else {
			enabledCustomPlugins.remove(customPlugin);
			plugins.remove(pluginName);
			if (StringHelper.containsNonWhitespace(buttons)) {
				List<String> buttonRow = getButtonRowFor(row);
				buttonRow.remove(buttons);
			}
		}		
	}

	/**
	 * Set the target list that can be used by author in the target dropdown of
	 * links. E.g. 'displayname=targetname,displayname2=targetname2'. The
	 * standard targets like _blank must not be added, use only the application
	 * specific targets here.
	 * 
	 * @param targetList
	 *            coma separated list of GUIname=target or NULL to reset the
	 *            list
	 */
	public void setLinkTargets(String targetList) {
		// displayname=targetname,displayname2=targetname2
		if (targetList != null) {
			quotedConfigValues.put(THEME_ADVANCED_LINK_TARGETS, targetList);
		} else {
			if (quotedConfigValues.containsKey(THEME_ADVANCED_LINK_TARGETS)) quotedConfigValues.remove(THEME_ADVANCED_LINK_TARGETS);
		}
	}

	/**
	 * Set the path to content css files used to format the content.
	 * 
	 * @param cssPath
	 *            path to CSS separated by comma or NULL to not use any specific
	 *            CSS files
	 */
	public void setContentCSS(String cssPath) {
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
		if (theme.getIdentifyer().equals("openolat")) {
			setContentCSS(theme.getBaseURI() + "all/content.css");			
		} else {
			StringOutput cssFiles = new StringOutput();
			StaticMediaDispatcher.renderStaticURI(cssFiles, "themes/openolat/all/content.css");
			cssFiles.append(",");
			cssFiles.append(theme.getBaseURI()).append("all/content.css");
			setContentCSS(cssFiles.toString());
		}
	}
	
	/**
	 * Disable the buttons with the given name
	 * 
	 * @param disabledButtons
	 */
	public void disableButton(String disabledButtons) {
		theme_advanced_disable.add(disabledButtons);
	}

	/**
	 * Set the forced root element that is entered into the edit area when the
	 * area is empty. By default this is a &lt;p&gt; element
	 * 
	 * @param rootElement
	 */
	public void setForcedRootElement(String rootElement) {
		setQuotedConfigValue(FORCED_ROOT_BLOCK, rootElement);
	}

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
	public void setFileBrowserCallback(VFSContainer vfsContainer, CustomLinkTreeModel customLinkTreeModel, String[] supportedImageSuffixes, String[] supportedMediaSuffixes, String[] supportedFlashPlayerSuffixes) {
		// Add dom ID variable using prototype curry method
		//TODO jquery setNonQuotedConfigValue(FILE_BROWSER_CALLBACK, FILE_BROWSER_CALLBACK_VALUE_LINK_BROWSER + ".curry('" + domID + "')");
		setNonQuotedConfigValue(FILE_BROWSER_CALLBACK, FILE_BROWSER_CALLBACK_VALUE_LINK_BROWSER + "('" + domID + "')");
		linkBrowserImageSuffixes = supportedImageSuffixes;
		linkBrowserMediaSuffixes = supportedMediaSuffixes;
		linkBrowserFlashPlayerSuffixes = supportedFlashPlayerSuffixes;
		linkBrowserBaseContainer = vfsContainer;
		linkBrowserCustomTreeModel = customLinkTreeModel;
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
	public void setDocumentMediaBase(final VFSContainer documentBaseContainer, String relFilePath, UserSession usess) {
		linkBrowserRelativeFilePath = relFilePath;
		// get a usersession-local mapper for the file storage (and tinymce's references to images and such)
		contentMapper = new Mapper() {
			public MediaResource handle(String relPath, HttpServletRequest request) {
				VFSItem vfsItem = documentBaseContainer.resolve(relPath);
				MediaResource mr;
				if (vfsItem == null || !(vfsItem instanceof VFSLeaf)) mr = new NotFoundMediaResource(relPath);
				else mr = new VFSMediaResource((VFSLeaf) vfsItem);
				return mr;
			}
		};
		// Register mapper for this user. This mapper is cleaned up in the
		// dispose method (RichTextElementImpl will clean it up)

		String uri;
		
		// Register mapper as cacheable
		String mapperID = VFSManager.getRealPath(documentBaseContainer);
		if (mapperID == null) {
			// Can't cache mapper, no cacheable context available
			uri = CoreSpringFactory.getImpl(MapperService.class).register(usess, contentMapper);
		} else {
			// Add classname to the file path to remove conflicts with other
			// usages of the same file path
			mapperID = this.getClass().getSimpleName() + ":" + mapperID;
			uri = CoreSpringFactory.getImpl(MapperService.class).register(usess, mapperID, contentMapper);				
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
		String fulluri = uri + "/" + relFilePath;
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
	public void setQuotedConfigValue(String key, String value) {
		// remove non-quoted config values with same key
		if (nonQuotedConfigValues.containsKey(key)) nonQuotedConfigValues.remove(key);
		// add or overwrite new value
		quotedConfigValues.put(key, value);
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
	public void setNonQuotedConfigValue(String key, String value) {
		// remove quoted config values with same key
		if (quotedConfigValues.containsKey(key)) quotedConfigValues.remove(key);
		// add or overwrite new value
		nonQuotedConfigValues.put(key, value);
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
	
	/**
	 * Get the optional custom link browser tree model
	 * @return the model or NULL if not defined
	 */
	public CustomLinkTreeModel getLinkBrowserCustomLinkTreeModel() {
		return linkBrowserCustomTreeModel;
	}


	/**
	 * Get the config value for the given key
	 * @param key
	 * @return
	 */
	public String getConfigValue(String key) {
		if (quotedConfigValues.containsKey(key)) {
			return quotedConfigValues.get(key);
		} else {
			return nonQuotedConfigValues.get(key);
		}
	}

	/**
	 * append all configurations to the given string buffer as js array
	 * key-value pairs. The creation of the array is not part of this method.
	 * 
	 * @param sb
	 *            The string buffer where to append the array values
	 */
	void appendConfigToTinyJSArray(StringOutput sb) {
		// Add plugins first
		int i = appendValuesFromList(sb, PLUGINS, plugins);
		// Add toolbars
		if (i > 0) sb.append(",");
		if (theme_advanced_buttons1.size() == 0) {
			sb.append(THEME_ADVANCED_BUTTONS1).append(":\"\"");
		} else {
			i += appendValuesFromList(sb, THEME_ADVANCED_BUTTONS1, theme_advanced_buttons1);			
		}
		if (i > 0) sb.append(",");			
		if (theme_advanced_buttons2.size() == 0) {
			sb.append(THEME_ADVANCED_BUTTONS2).append(":\"\"");
		} else {
			i += appendValuesFromList(sb, THEME_ADVANCED_BUTTONS2, theme_advanced_buttons2);			
		}
		if (i > 0) sb.append(",");			
		if (theme_advanced_buttons3.size() == 0) {
			sb.append(THEME_ADVANCED_BUTTONS3).append(":\"\"");			
		} else {
			i += appendValuesFromList(sb, THEME_ADVANCED_BUTTONS3, theme_advanced_buttons3);						
		}
		// Now add disabled buttons
		if (i > 0 && theme_advanced_disable.size() > 0) sb.append(",");
		i += appendValuesFromList(sb, THEME_ADVANCED_DISABLE, theme_advanced_disable);
		// Now add the quoted values
		for (Map.Entry<String, String> entry : quotedConfigValues.entrySet()) {
			if (i > 0) sb.append(",");
			sb.append(entry.getKey()).append(": \"").append(entry.getValue()).append("\"");
			i++;
		}
		// Now add the non-quoted values (e.g. true, false or functions)
 		for (Map.Entry<String, String> entry : nonQuotedConfigValues.entrySet()) {
			if (i > 0) sb.append(",");
			sb.append(entry.getKey()).append(": ").append(entry.getValue()).append("");
			i++;
		}
 		// Now add on init callback functions
 		if (oninit.size() > 0) {
 			if (i > 0) sb.append(",");
 			sb.append(ONINIT_CALLBACK).append(": ");
 			if (oninit.size() == 1) {
 				sb.append(oninit.iterator().next());
 			} else {
 				Iterator<String> iter = oninit.iterator();
 				sb.append("function(){");
 				while (iter.hasNext()) {
					String function = iter.next();
					sb.append(function);
					if(function.endsWith(")")) {
						sb.append(";");
					} else {
						sb.append("();");
					}
					
					
					
				}
 				sb.append("}");
 			}
 			i++;
 		}
	}

	/**
	 * Append the js commands to load all configured custom plugins to the editor
	 * 
	 * @param The string buffer where to append the js load commands
	 */
	void appendLoadCustomModulesFromConfig(StringOutput sb) {
		for (TinyMCECustomPlugin plugin : enabledCustomPlugins) {
			String pluginName = plugin.getPluginName();
			String pluginURL = plugin.getPluginURL();
			sb.append("tinymce.PluginManager.load('").append(pluginName).append("', '").append(pluginURL).append("');");					
		}
	}
	
	
	/**
	 * Internal helper to append config values to a string buffer
	 * @param sb
	 * @param key
	 * @param values
	 * @return
	 */
	private int appendValuesFromList(StringOutput sb, String key, List<String> values) {
		if (values.size() == 0) return 0;
		sb.append(key).append(": \"");
		int i = 0;
		for (String value : values) {
			if (i > 0) sb.append(",");
			sb.append(value);
			i++;
		}
		sb.append("\"");
		return i;
	}


	/**
	 * Internal helper to get the button row list for the given row
	 * 
	 * @param row
	 * @return
	 */
	private List<String> getButtonRowFor(int row) {
		switch (row) {
		case 1:
			return theme_advanced_buttons1;
		case 2:
			return theme_advanced_buttons2;
		case 3:
			return theme_advanced_buttons3;
		default:
			return theme_advanced_buttons1;
		}
	}

	/**
	 * @see org.olat.core.gui.control.Disposable#dispose()
	 */
	public void dispose() {
		if (contentMapper != null) {
			CoreSpringFactory.getImpl(MapperService.class).cleanUp(Collections.singletonList(contentMapper));
			contentMapper = null;
		}		
	}
}