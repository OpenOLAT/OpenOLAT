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

package org.olat.core.util.i18n;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.olat.core.configuration.AbstractOLATModule;
import org.olat.core.configuration.Destroyable;
import org.olat.core.configuration.PersistedProperties;
import org.olat.core.gui.control.Event;
import org.olat.core.helpers.Settings;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.OLATRuntimeException;
import org.olat.core.util.ArrayHelper;
import org.olat.core.util.StringHelper;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.resource.OresHelper;

/**
 * <h3>Description:</h3> The I18nModule initializes the localization
 * infrastructure. It offers configuration options to define the default
 * language, the available and enabled languages etc.
 * <p>
 * Initial Date: 28.08.2008 <br>
 * 
 * @author Florian Gnaegi, frentix GmbH, http://www.frentix.com
 */

public class I18nModule extends AbstractOLATModule implements Destroyable {
	// Some general variables
	public static final String LOCAL_STRINGS_FILE_PREFIX = "LocalStrings_";
	public static final String LOCAL_STRINGS_FILE_POSTFIX = ".properties";
	// Location of customizing directory and i18n configuration (configured at
	// runtime)
	public static File LANG_CUSTOMIZING_DIRECTORY;
	public static File LANG_PACKS_DIRECTORY;
	public static File LANG_OVERLAY_DIRECTORY;
	// Constants for the translation statistics scheduler job
	public static final String SCHEDULER_NAME = "i18n.status.generator";

	// User GUI prefs keys
	public static final String GUI_PREFS_PREFERRED_COMPARE_LANG = "REFERRED_COMPARE_LANG";
	public static final String GUI_PREFS_PREFERRED_REFERENCE_LANG = "PREFERRED_REFERENCE_LANG";
	public static final String GUI_PREFS_COMPARE_LANG_ENABLED = "COMPARE_LANG_ENABLED";
	public static final String GUI_PREFS_INLINE_TRANSLATION_ENABLED = "INLINE_TRANSLATION_ENABLED";

	// Configuration parameter read from i18nmanager spring file (configured at system
	// setup time)
	private static final String CONFIG_LANGUAGES_ENABLED = "enabledLanguages";
	private static final String CONFIG_LANGUAGES_ENABLED_ALL = "all";
	private static final String CONFIG_DEFAULT_LANG = "defaultLanguage";
	private static final String CONFIG_FALLBACK_LANG = "fallbackLanguage";
	private static final String CONFIG_LANGUAGES_REFERENCES = "transToolReferenceLanguages";
	private static final String CONFIG_OVERLAY = "overlayName";
	private static final String CONFIG_OVERLAY_ENABLED = "overlayEnabled";
	private static final String CONFIG_CACHING_ENABLED = "cachingEnabled";
	private static final String CONFIG_LANGUAGE_LIST_ENABLED = "dropDownListEnabled";
	private static final String CONFIG_APPLICATION_FALLBACK_BUNDLE = "applicationFallbackBundle";
	private static final String CONFIG_CORE_FALLBACK_BUNDLE = "coreFallbackBundle";
	private static final String CONFIG_TRANS_TOOL_ENABLED = "transToolEnabled";
	private static final String CONFIG_TRANS_TOOL_APPLICATION_SRC_PATH = "transToolApplicationSrcPath";
	private static final String CONFIG_TRANS_TOOL_APPLICATION_OPT_SRC_PATH = "transToolApplicationOptSrcPath";

	// General configuration
	private static String overlayName;
	private static boolean overlayEnabled;
	private static boolean cachingEnabled;
	private static boolean languageDropDownListEnabled;
	// Lists of the available and enabled languages and locales
	private static final Set<String> availableLanguages = new HashSet<String>();
	private static final Set<String> translatableLanguages = new HashSet<String>();
	private static final Map<String, File> translatableLangAppBaseDirLookup = new HashMap<String, File>();
	// keys: lang string, values: locale
	private static final Map<String, Locale> allLocales = new HashMap<String, Locale>();
	// keys: orig Locale, values: overlay Locale
	private static final Map<Locale, Locale> overlayLocales = new HashMap<Locale, Locale>();
	private static final Set<String> overlayLanguagesKeys = new HashSet<String>();
	private static final Set<String> enabledLanguagesKeys = new HashSet<String>();
	// The default locale (used on loginscreen and as first fallback) and the
	// fallback (used as second fallback)
	private static Locale defaultLocale;
	private static Locale fallbackLocale;
	// The available bundles
	private static List<String> bundleNames = null; // sorted alphabetically
	private static String coreFallbackBundle = null;
	private static String applicationFallbackBundle = null;
	// Translation tool related configuration
	private static final List<String> transToolReferenceLanguages = new ArrayList<String>();
	private static File transToolApplicationLanguagesDir = null;
	private static File transToolApplicationOptLanguagesSrcDir = null;
	private static boolean transToolEnabled = false;
	// When running on a cluster, we need an event when flushing the i18n cache to do this on all machines
	private static OLATResourceable I18N_CACHE_FLUSHED_EVENT_CHANNEL;

	// Reference to instance for static methods
	private static I18nModule INSTANCE;
	private CoordinatorManager coordinatorManager;

	/**
	 * [spring]
	 */
	private I18nModule(CoordinatorManager coordinatorManager) {
		super();
		this.coordinatorManager = coordinatorManager;
		//if (INSTANCE != null && !Settings.isJUnitTest()) { throw new OLATRuntimeException("Tried to construct I18nModule, but module was already loaded!", null); }
		INSTANCE = this;
	}

	@Override
	protected void initDefaultProperties() {
		// First read default configuration from the module config and then set
		// is as default in the properties
		String defaultLanguageKey = getStringConfigParameter(CONFIG_DEFAULT_LANG, "en", false);
		setStringPropertyDefault(CONFIG_DEFAULT_LANG, defaultLanguageKey);
		String enabledLanguagesConfig = getStringConfigParameter(CONFIG_LANGUAGES_ENABLED, CONFIG_LANGUAGES_ENABLED_ALL, false);
		setStringPropertyDefault(CONFIG_LANGUAGES_ENABLED, enabledLanguagesConfig);
	}

	@Override
	protected void initFromChangedProperties() {
		doReInitialize();
	}

	/**
	 * @see org.olat.core.configuration.AbstractOLATModule#init()
	 */
	@Override
	public void init() {
		// Check that necessary directories are there
		LANG_CUSTOMIZING_DIRECTORY = new File(WebappHelper.getUserDataRoot() + "/customizing/lang/");
		LANG_PACKS_DIRECTORY = new File(LANG_CUSTOMIZING_DIRECTORY, "/packs/");
		LANG_OVERLAY_DIRECTORY = new File(LANG_CUSTOMIZING_DIRECTORY, "/overlay/");
		
		LANG_CUSTOMIZING_DIRECTORY.mkdirs();
		LANG_OVERLAY_DIRECTORY.mkdirs();
		LANG_PACKS_DIRECTORY.mkdirs();
		// Initialize configuration
		doInit();
		// Register on the event channel to get cache flushes of other nodes
		I18N_CACHE_FLUSHED_EVENT_CHANNEL = OresHelper.createOLATResourceableType(this.getClass().getSimpleName() + "I18N_CACHE_FLUSHED_EVENT_CHANNEL");
		coordinatorManager.getCoordinator().getEventBus().registerFor(this, null, I18N_CACHE_FLUSHED_EVENT_CHANNEL);
	}

	private void doInit() {
		// A) Initialize system configuration from config file - those values
		// can't be changed at runtime and are project specific

		// Set configured reference languages
		String referenceLanguagesConfig = getStringConfigParameter(CONFIG_LANGUAGES_REFERENCES, "en", false);
		String[] referenceAndFallbackKeys = referenceLanguagesConfig.split(",");
		// remove whitespace and check for douplicates
		for (int i = 0; i < referenceAndFallbackKeys.length; i++) {
			String langKey = referenceAndFallbackKeys[i];
			langKey = langKey.trim();
			transToolReferenceLanguages.add(langKey);
		}

		// Language overlay: used to override a language with some custom wording
		overlayName = getStringConfigParameter(CONFIG_OVERLAY, "customizing", false);
		overlayEnabled = getBooleanConfigParameter(CONFIG_OVERLAY_ENABLED, true);

		// Set caching configuration of local strings
		cachingEnabled = getBooleanConfigParameter(CONFIG_CACHING_ENABLED, true);
		logInfo("Localization caching set to: " + cachingEnabled, null);

		// Set how the list of available availableLanguages will be shown
		// (drop-down[true] or in one line[false])
		languageDropDownListEnabled = getBooleanConfigParameter(CONFIG_LANGUAGE_LIST_ENABLED, true);
		logInfo("Configuring 'dropDownListEnabled = " + languageDropDownListEnabled + "'", null);

		// Set additional source path to load languages from and to store
		// languages when using the translation tool. Init the values even when transtool is not configured for development mode		
		String appSrc = getStringConfigParameter(CONFIG_TRANS_TOOL_APPLICATION_SRC_PATH, "", false);
		if (StringHelper.containsNonWhitespace(appSrc)) {
			appSrc = appSrc.trim();
			transToolApplicationLanguagesDir = new File(appSrc);
		}
		String optAppSrc = getStringConfigParameter(CONFIG_TRANS_TOOL_APPLICATION_OPT_SRC_PATH, "", false);
		if (StringHelper.containsNonWhitespace(optAppSrc)) {
			optAppSrc = optAppSrc.trim();
			transToolApplicationOptLanguagesSrcDir = new File(optAppSrc);
		}
		
		// Enable or disable translation tool and i18n source directories
		transToolEnabled = getBooleanConfigParameter(CONFIG_TRANS_TOOL_ENABLED, false);
		if (transToolEnabled) {
			//
			if (transToolApplicationLanguagesDir != null && transToolApplicationOptLanguagesSrcDir != null) {
				// Check if configuration is valid, otherwise disable translation server mode
			} else {
				// disable, pathes not configured properly
				transToolEnabled = false;
			}
			// error handling, notify on console about disabled translation tool
			if (!transToolEnabled) {
				logWarn(
						"Translation configuration enabled but invalid translation tool source path defined. Disabling translation tool. Fix your configuration in spring config of i18Module",
						null);
				logWarn(" transToolApplicationSrcPath::" + appSrc + " transToolApplicationI18nSrcPath::" + optAppSrc, null);
			}
		}

		I18nManager i18nMgr = I18nManager.getInstance();
		i18nMgr.setCachingEnabled(cachingEnabled);

		// Get all bundles that contain i18n files
		initBundleNames();
		// Set the base bundles for olatcore and the application. When a key is
		// not found, the manager looks it up in the application base and the in
		// the core base bundle before it gives up
		applicationFallbackBundle = getStringConfigParameter(CONFIG_APPLICATION_FALLBACK_BUNDLE, "org.olat", false);
		coreFallbackBundle = getStringConfigParameter(CONFIG_CORE_FALLBACK_BUNDLE, "org.olat.core", false);

		// Search for all available languages on the build path and initialize them
		doInitAvailableLanguages();

		// B) Initialize default language and the list of enabled languages from
		// the persisted system configuration
		doInitLanguageConfiguration();

		logInfo("Configured i18nModule with default language::" + getDefaultLocale().toString() + " and the reference languages '"
				+ referenceLanguagesConfig + "' and the following enabled languages: " + enabledLanguagesKeys.toString(), null);
	}

	/**
	 * 
	 * @see org.olat.core.configuration.Destroyable#destroy()
	 */
	public void destroy() {
		// remove from event channel
		if (I18N_CACHE_FLUSHED_EVENT_CHANNEL != null) {
			coordinatorManager.getCoordinator().getEventBus().deregisterFor(this, I18N_CACHE_FLUSHED_EVENT_CHANNEL);
			I18N_CACHE_FLUSHED_EVENT_CHANNEL = null;
		}
	}

	/**
	 * Initialize the available languages and load all locales
	 */
	private void doInitAvailableLanguages() {
		I18nManager i18nMgr = I18nManager.getInstance();
		// Search all availableLanguages files that exist 

		String i18nDirRelPath = File.separator + applicationFallbackBundle.replace(".", File.separator) + File.separator + I18nManager.I18N_DIRNAME;
		if (transToolApplicationLanguagesDir != null) {
			File coreSrcI18nDir = new File(transToolApplicationLanguagesDir, i18nDirRelPath);
			if (coreSrcI18nDir.exists()) {
				for (String languageCode : i18nMgr.searchForAvailableLanguages(transToolApplicationLanguagesDir)) {
					if (availableLanguages.contains(languageCode)) {
						String path = "";
						if (transToolApplicationOptLanguagesSrcDir != null) path = transToolApplicationOptLanguagesSrcDir.getAbsolutePath();
						logDebug("Skipping duplicate or previously loaded language::" + languageCode + " found in " +path , null);
						continue;
					}
					logDebug("Detected translatable language " + languageCode + " in " + transToolApplicationLanguagesDir.getAbsolutePath(), null);
					availableLanguages.add(languageCode);
					translatableLanguages.add(languageCode);
					translatableLangAppBaseDirLookup.put(languageCode, transToolApplicationLanguagesDir);
				}
			}
		}
		// 2) Add languages from the translation tool source path
		if (isTransToolEnabled()) {
			for (String languageCode : i18nMgr.searchForAvailableLanguages(transToolApplicationOptLanguagesSrcDir)) {
				if (availableLanguages.contains(languageCode)) {
					logDebug("Skipping duplicate or previously loaded language::" + languageCode + " found in " + transToolApplicationOptLanguagesSrcDir.getAbsolutePath(), null);
					continue;
				}
				logDebug("Detected translatable language " + languageCode + " in " + transToolApplicationOptLanguagesSrcDir.getAbsolutePath(), null);
				availableLanguages.add(languageCode);
				translatableLanguages.add(languageCode);
				translatableLangAppBaseDirLookup.put(languageCode, transToolApplicationOptLanguagesSrcDir);
			}
		}

		String folderRoot = WebappHelper.getBuildOutputFolderRoot();
		if(StringHelper.containsNonWhitespace(folderRoot)) {
			//started from WEB-INF/classes
			File libDir = new File(WebappHelper.getBuildOutputFolderRoot());
			for (String languageCode : i18nMgr.searchForAvailableLanguages(libDir)) {
				if (availableLanguages.contains(languageCode)) {
					logDebug("Skipping duplicate or previously loaded  language::" + languageCode + " found in " + libDir.getAbsolutePath(), null);
					continue;
				}
				logDebug("Detected non-translatable language " + languageCode + " in " + libDir.getAbsolutePath(), null);
				availableLanguages.add(languageCode);
				// don't add to translatable languages nor to source lookup maps - those
				// langs are read only
			}
		} else {
			//started from jar (like weblogic does) -> load from the configuration
			String enabledLanguagesConfig = getStringPropertyValue(CONFIG_LANGUAGES_ENABLED, false);
			String[] enabledLanguages = enabledLanguagesConfig.split(",");
			for (String languageCode : enabledLanguages) {
				if (availableLanguages.contains(languageCode)) {
					logWarn("Skipping duplicate or previously loaded  language::" + languageCode + " found in "
							+ LANG_PACKS_DIRECTORY.getAbsolutePath(), null);
					continue;
				}
				logDebug("Force non-translatable language " + languageCode + " defined from enabledLanguages.", null);
				availableLanguages.add(languageCode);
			}
		}
		
		// 4) Add languages from the customizing lang packs
		for (String languageCode : i18nMgr.searchForAvailableLanguages(LANG_PACKS_DIRECTORY)) {
			if (availableLanguages.contains(languageCode)) {
				logWarn("Skipping duplicate or previously loaded  language::" + languageCode + " found in "
						+ LANG_PACKS_DIRECTORY.getAbsolutePath(), null);
				continue;
			}
			logDebug("Detected non-translatable language " + languageCode + " in " + LANG_PACKS_DIRECTORY.getAbsolutePath(), null);
			availableLanguages.add(languageCode);
			// don't add to translatable languages nor to source lookup maps - those
			// langs are read only
		}
		// 
		// Finished detecting available languages
		//
		// Proceed with some sanity checks
		if (availableLanguages.size() == 0 || !availableLanguages.contains(Locale.ENGLISH.toString())) { throw new OLATRuntimeException(
			"Did not find any language files, not even 'en'! At least 'en' must be available.", null); }
		List<String> toRemoveLangs = new ArrayList<String>();
		//
		// Build list of all locales and the overlay locales if available
		for (String langKey : availableLanguages) {
			Locale locale = i18nMgr.createLocale(langKey);
			if (locale == null) {
				logError("Could not create locale for lang::" + langKey + ", skipping language and remove it from list of available languages",
						null);
				toRemoveLangs.add(langKey);
				continue;
			}
			// Don't add same language twice
			if (!allLocales.containsKey(langKey)) {
				allLocales.put(langKey, locale);
			}
			//
			// Add overlay
			if (isOverlayEnabled()) {
				Locale overlayLocale = i18nMgr.createOverlay(locale);
				// Calculate the overlay key as used as reference. Note, this is not the
				// same as overlayLocale.toString(), this would add '_' for each element
				String overlayKey = i18nMgr.getLocaleKey(overlayLocale);
				if (overlayLocale == null) {
					logError("Could not create overlay locale for lang::" + langKey + " (" + overlayKey + "), skipping language", null);
					continue;
				}
				// Don't add same overlay twice
				if (!allLocales.containsKey(overlayKey)) {
					overlayLanguagesKeys.add(overlayKey);
					allLocales.put(overlayKey, overlayLocale);
					overlayLocales.put(locale, overlayLocale);
					// Add translation tool base dir for overlay local to customizing dir
					translatableLangAppBaseDirLookup.put(overlayKey, LANG_OVERLAY_DIRECTORY);
				}
			}
		}
		// Remove langs that failed to be created
		for (String langKey : toRemoveLangs) {
			availableLanguages.remove(langKey);
			translatableLanguages.remove(langKey);
			translatableLangAppBaseDirLookup.remove(langKey);
		}
		// Set fallback locale from configuration
		String fallbackLangKey = getStringConfigParameter(CONFIG_FALLBACK_LANG, Locale.ENGLISH.toString(), false);
		// fallbackLangKey can't be null because EN is guaranteed to be available,
		// see above
		fallbackLocale = allLocales.get(fallbackLangKey);

		// Check if translation tool reference languages are available
		if (isTransToolEnabled() && transToolReferenceLanguages.size() == 0) {
			logError("Did not find the fallback language configuration in the configuration, using language::en instead", null);
		} else {
			for (String langKey : transToolReferenceLanguages) {
				if (!allLocales.containsKey(langKey)) {
					logError("The configured fallback language::" + langKey + " does not exist. Using language::en instead", null);
				}
			}
		}
	}

	private void doInitLanguageConfiguration() {
		// Set the default language
		String defaultLanguageKey = getStringPropertyValue(CONFIG_DEFAULT_LANG, false);
		Locale newDefaultLocale = allLocales.get(defaultLanguageKey);
		if (newDefaultLocale == null) {
			logError("Could not set default locale to value::" + defaultLanguageKey + " - no such language found. Using fallback locale instead",
					null);
			newDefaultLocale = allLocales.get(transToolReferenceLanguages.get(0));
		} else if (!availableLanguages.contains(newDefaultLocale.toString())) {
			logError("Did not find the default language::" + newDefaultLocale.toString()
					+ " in the available availableLanguages files! Using fallback locale instead", null);
			newDefaultLocale = allLocales.get(transToolReferenceLanguages.get(0));
		}
		defaultLocale = newDefaultLocale;
		logInfo("Setting default locale::" + newDefaultLocale.toString(), null);

		// Enabling configured languages (a subset of the available languages)
		String[] enabledLanguages;
		String enabledLanguagesConfig = getStringPropertyValue(CONFIG_LANGUAGES_ENABLED, false);
		if (!StringHelper.containsNonWhitespace(enabledLanguagesConfig) || enabledLanguagesConfig.equals(CONFIG_LANGUAGES_ENABLED_ALL)) {
			enabledLanguages = ArrayHelper.toArray(availableLanguages);
		} else {
			enabledLanguages = enabledLanguagesConfig.split(",");
		}
		enabledLanguagesKeys.clear(); // reset first
		for (String langKey : enabledLanguages) {
			langKey = langKey.trim();
			if (StringHelper.containsNonWhitespace(langKey) && availableLanguages.contains(langKey)) {
				enabledLanguagesKeys.add(langKey);
			} // else skip this entry
		}
		logInfo("Enabling languages::" + enabledLanguagesConfig, null);
		// Make sure that the configured default language is enabled
		if (!enabledLanguagesKeys.contains(getDefaultLocale().toString())) {
			String defLang = getDefaultLocale().toString();
			enabledLanguagesKeys.add(defLang);
			logWarn("The configured default language::" + defLang + " is not in the list of enabled languages. Enabling language::" + defLang,
					null);
		}
	}

	//
	// Getters and Setters
	//	

	/**
	 * @return The default locale configured for this web app
	 */
	public static Locale getDefaultLocale() {
		return defaultLocale;
	}

	/**
	 * Method to set new default locale
	 * 
	 * @param newDefaultLocale
	 */
	public static void setDefaultLocale(Locale newDefaultLocale) {
		if (defaultLocale == null || !defaultLocale.toString().equals(newDefaultLocale.toString())) {
			// Just set the string property here. This will fire an event and
			// call the method initFromChangedProperties()
			INSTANCE.setStringProperty(CONFIG_DEFAULT_LANG, newDefaultLocale.toString(), true);
		}
	}

	/**
	 * @return The locale that is used when a string is not found in any other
	 *         locale
	 */
	public static Locale getFallbackLocale() {
		return fallbackLocale;
	}

	/*
	 * Static getter methods, mainly used by I18nManager
	 */

	/**
	 * @return true: caching is enabled; false: caching is disabled
	 */
	static boolean isCachingEnabled() {
		return cachingEnabled;
	}

	/**
	 * @return as keys: a Set of Strings with the supported languages (e.g. de,
	 *         de_CH, en, ...). those are the languages that are installed. See
	 *         the enabled languages to get the list of languages that are enabled
	 *         to be used
	 */
	public static Set<String> getAvailableLanguageKeys() {
		return availableLanguages;
	}

	/**
	 * @return A set of language keys that can be translated by the translation
	 *         tool. Theses are the languages that are available in the source
	 *         form. Languages embedded in jars can't be edited.
	 */
	public static Set<String> getTranslatableLanguageKeys() {
		return translatableLanguages;
	}

	/**
	 * @return the map (with dummy value) of all languages including
	 *         overlayLocales (as a String)
	 */
	public static Map<String, Locale> getAllLocales() {
		return allLocales;
	}

	/**
	 * @return The lookup map of the overlay locales. Key: the locale; value: the
	 *         corresponding overlay
	 */
	public static Map<Locale, Locale> getOverlayLocales() {
		return overlayLocales;
	}

	/**
	 * @return as keys: a List of Strings with the supported languages (e.g. de,
	 *         de_CH, en, ...). those are the languages which can be chosen by the
	 *         user
	 */
	public static Collection<String> getEnabledLanguageKeys() {
		synchronized (enabledLanguagesKeys) {
			return new HashSet<String>(enabledLanguagesKeys);
		}
	}

	/**
	 * @return as keys: a List of Strings with the supported languages overlay keys
	 */
	public static Set<String> getOverlayLanguageKeys() {
			return overlayLanguagesKeys;
	}

	
	/**
	 * Set the languages that are enabled on the system. The change affects the
	 * system immediately and is persisted in the olatdata/system/configuration
	 * 
	 * @param newEnabledLangKeys
	 */
	public static void setEnabledLanguageKeys(Collection<String> newEnabledLangKeys) {
		if (!newEnabledLangKeys.equals(enabledLanguagesKeys)) {
			String newEnabledConfig = "";
			for (String enabledKey : newEnabledLangKeys) {
				newEnabledConfig += enabledKey + ",";
			}
			if (newEnabledConfig.length() > 0) {
				// remove last comma
				newEnabledConfig = newEnabledConfig.substring(0, newEnabledConfig.length() - 1);
			}

			// Just set the string property here. This will fire an event and
			// call the method initFromChangedProperties()
			INSTANCE.setStringProperty(CONFIG_LANGUAGES_ENABLED, newEnabledConfig.toString(), true);
			// No need to reinitialize i18n Module, setting the new property will already do this
		}
	}

	/**
	 * @return A list of language keys that are reference and fallback languages
	 */
	public static List<String> getTransToolReferenceLanguages() {
		return transToolReferenceLanguages;
	}

	/**
	 * @return All bundles that contain a _i18n directory with translation files
	 */
	public static List<String> getBundleNamesContainingI18nFiles() {
		return bundleNames;
	}

	/**
	 * @return The bundle name that contains the commonly used translations from
	 *         the olatcore framework
	 */
	public static String getCoreFallbackBundle() {
		return coreFallbackBundle;
	}

	/**
	 * @return The bundle name that contains the commonly used translations from
	 *         the application
	 */
	public static String getApplicationFallbackBundle() {
		return applicationFallbackBundle;
	}

	/**
	 * Checks if the overlay mechanism is enabled. The overlay is similar to the
	 * locale variant, but adds another layer on top of it.
	 * 
	 * @return true: enabled; false: disabled
	 */
	public static boolean isOverlayEnabled() {
		return (overlayEnabled && StringHelper.containsNonWhitespace(overlayName));
	}

	/**
	 * Returns the overlay name or NULL if not configured. For an overlay file
	 * with the name 'LocalStrings_de__VENDOR.properties' it will return 'VENDOR'
	 * 
	 * @return name of the overlay
	 */
	public static String getOverlayName() {
		if (isOverlayEnabled()) return overlayName;
		else return null;
	}

	/**
	 * @return true: enable the translation tool; false: disable the translation
	 *         tool
	 */
	public static boolean isTransToolEnabled() {
		return transToolEnabled;
	}

	/**
	 * search for bundles that contain i18n files. Searches in the org.olat.core
	 * package
	 */
	static void initBundleNames() {
		I18nManager i18nMgr = I18nManager.getInstance();
		bundleNames = i18nMgr.searchForBundleNamesContainingI18nFiles();
	}

	/**
	 * Get the base source directory for the given language where the language
	 * files for this bundle are stored
	 * 
	 * @param locale
	 * @param bundleName
	 * @return The file or null if the language is not read-write configured
	 */
	public static File getPropertyFilesBaseDir(Locale locale, String bundleName) {
		// 1) Special case, junit test files are not in olat source path
		// We don't want translator to translate those files!
		if (Settings.isJUnitTest() && bundleName.startsWith("org.olat.core.util.i18n.junittestdata") ) {
			return new File(transToolApplicationLanguagesDir, "/../../test/java");
		}

		// 2) Metadata file: metadata file is always on the core / application
		// source path
		if (locale == null) {
			return transToolApplicationLanguagesDir;
		}
		// 3) Locale files from core or application
		String localeKey = I18nManager.getInstance().getLocaleKey(locale);
		return translatableLangAppBaseDirLookup.get(localeKey);
	}

	/**
	 * Reinitialize the entire i18n system
	 */
	public static void reInitializeAndFlushCache() {
		synchronized (enabledLanguagesKeys) {
			// Re-initialize all local caches
			doReInitialize();
			// Notify other nodes to reInitialize the caches as well
			I18nReInitializeCachesEvent changedConfigEvent = new I18nReInitializeCachesEvent();
			CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(changedConfigEvent, I18N_CACHE_FLUSHED_EVENT_CHANNEL);				
		}
	}

	/**
	 * Private helper that implements the reinitialization of all caches. This
	 * is decoupled from the reInitialize() to prevent endless firing of events
	 * in the cluster.
	 */
	private static void doReInitialize() {		
		synchronized (enabledLanguagesKeys) {
			// Clear everything
			availableLanguages.clear();
			translatableLanguages.clear();
			translatableLangAppBaseDirLookup.clear();
			allLocales.clear();
			overlayLanguagesKeys.clear();
			overlayLocales.clear();
			enabledLanguagesKeys.clear();
			transToolReferenceLanguages.clear();
			I18nManager.getInstance().clearCaches();
			// Now rebuild everything from scratch
			INSTANCE.doInit();
		}
	}

	/**
	 * @see org.olat.core.configuration.AbstractOLATModule#event(org.olat.core.gui.control.Event)
	 */
	public void event(Event event) {
		// First delegate to AbstractOLATModule
		super.event(event);
		// Take care of our own things
		if (event instanceof I18nReInitializeCachesEvent) {
			// Fired when the cache is flushed on another node without any config changes
			I18nReInitializeCachesEvent i18nInitEvent = (I18nReInitializeCachesEvent) event;
			if (!i18nInitEvent.isEventOnThisNode()) {
				doReInitialize();
			}
		}
	}

	/**
	 * Get the language dir for the applications DE and EN files. Only available when in
	 * translation server mode
	 * 
	 * @return
	 */
	public static File getTransToolApplicationLanguagesSrcDir() {
		return transToolApplicationLanguagesDir;
	}

	/**
	 * Get the language dir for the applications i18n different than DE and EN
	 * files. Only available when in translation server mode
	 * 
	 * @return
	 */
	public static File getTransToolApplicationOptLanguagesSrcDir() {
		return transToolApplicationOptLanguagesSrcDir;
	}

	@Override
	public void setPersistedProperties(PersistedProperties persistedProperties) {
		this.moduleConfigProperties = persistedProperties;
	}

}
