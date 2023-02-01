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
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.logging.log4j.Logger;
import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.gui.control.Event;
import org.olat.core.helpers.Settings;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.OLATRuntimeException;
import org.olat.core.logging.StartupException;
import org.olat.core.logging.Tracing;
import org.olat.core.util.ArrayHelper;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.resource.OresHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

/**
 * <h3>Description:</h3> The I18nModule initializes the localization
 * infrastructure. It offers configuration options to define the default
 * language, the available and enabled languages etc.
 * <p>
 * Initial Date: 28.08.2008 <br>
 * 
 * @author Florian Gnaegi, frentix GmbH, http://www.frentix.com
 */
@Service("i18nModule")
public class I18nModule extends AbstractSpringModule {
	
	private static final Logger log = Tracing.createLoggerFor(I18nModule.class);
	
	// Some general variables
	public static final String LOCAL_STRINGS_FILE_PREFIX = "LocalStrings_";
	public static final String LOCAL_STRINGS_FILE_POSTFIX = ".properties";
	// Location of customizing directory and i18n configuration (configured at
	// runtime)
	private static File LANG_CUSTOMIZING_DIRECTORY;
	private static File LANG_PACKS_DIRECTORY;
	private static File LANG_OVERLAY_DIRECTORY;
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
	
	@Value("${enabledLanguages}")
	private String enabledLanguages;
	@Value("${defaultlang:en}")
	private String defaultLanguage;
	@Value("${fallbacklang:en}")
	private String fallbackLanguage;

	// General configuration
	private final String overlayName = "customizing";
	private final boolean overlayEnabled = true;

	@Value("${localization.cache:true}")
	private boolean cachingEnabled;
	private boolean languageDropDownListEnabled = true;
	// Lists of the available and enabled languages and locales
	private final Set<String> availableLanguages = new HashSet<>();
	private final Set<String> translatableLanguages = new HashSet<>();
	private final Map<String, File> translatableLangAppBaseDirLookup = new HashMap<>();
	// keys: lang string, values: locale
	private static final Map<String, Locale> allLocales = new HashMap<>();
	// keys: orig Locale, values: overlay Locale
	private final Map<Locale, Locale> overlayLocales = new HashMap<>();
	private final Set<String> overlayLanguagesKeys = new HashSet<>();
	private final Set<String> enabledLanguagesKeys = new HashSet<>();
	// keys: String language code, values: gender strategy
	private final Map<Locale, GenderStrategy> genderStrategies = new HashMap<Locale,GenderStrategy>();
	// The default locale (used on loginscreen and as first fallback) and the
	// fallback (used as second fallback)
	private static Locale defaultLocale;
	private Locale fallbackLocale;
	// The available bundles
	private List<String> bundleNames; // sorted alphabetically
	
	private final String coreFallbackBundle = "org.olat.core";
	private final String applicationFallbackBundle = "org.olat";

	// Translation tool related configuration
	private final List<String> transToolReferenceLanguages = new ArrayList<>();
	
    @Value("${i18n.application.src.dir}")
	private String transToolApplicationSrcPath;
	private File transToolApplicationLanguagesDir;
	@Value("${i18n.application.opt.src.dir}")
	private String transToolApplicationOptSrcPath;
	private File transToolApplicationOptLanguagesSrcDir;
	@Value("${is.translation.server:disabled}")
	private String transToolEnabled;
	private final String referenceLanguages = "en,de";

	private final ConcurrentMap<Locale,String> localeToLocaleKey = new ConcurrentHashMap<>();
	
	// When running on a cluster, we need an event when flushing the i18n cache to do this on all machines
	private static OLATResourceable I18N_CACHE_FLUSHED_EVENT_CHANNEL;

	// Reference to instance for static methods
	private final CoordinatorManager coordinatorManager;

	@Autowired
	public I18nModule(CoordinatorManager coordinatorManager, WebappHelper webappHelper) {
		super(coordinatorManager);
		assert webappHelper != null;
		this.coordinatorManager = coordinatorManager;
	}

	@Override
	protected void initDefaultProperties() {
		// First read default configuration from the module config and then set
		// is as default in the properties
		setStringPropertyDefault(CONFIG_DEFAULT_LANG, defaultLanguage);
		setStringPropertyDefault(CONFIG_LANGUAGES_ENABLED, enabledLanguages);
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
		
		if(isTransToolEnabled()) {
			String sourcePath = WebappHelper.getSourcePath();
			if(!StringHelper.containsNonWhitespace(sourcePath) || !(new File(sourcePath).exists())) {
				log.error("Path to source wrong, translation tool may not work as expected: " + sourcePath);
			}
		}
	}

	private void doInit() {
		// A) Initialize system configuration from config file - those values
		// can't be changed at runtime and are project specific

		// Set configured reference languages
		String[] referenceAndFallbackKeys = referenceLanguages.split(",");
		// remove whitespace and check for douplicates
		for (int i = 0; i < referenceAndFallbackKeys.length; i++) {
			String langKey = referenceAndFallbackKeys[i];
			langKey = langKey.trim();
			transToolReferenceLanguages.add(langKey);
		}

		// Set caching configuration of local strings
		log.info("Localization caching set to: " + cachingEnabled);

		// Set how the list of available availableLanguages will be shown
		// (drop-down[true] or in one line[false])
		log.info("Configuring 'dropDownListEnabled = " + languageDropDownListEnabled + "'");

		// Set additional source path to load languages from and to store
		// languages when using the translation tool. Init the values even when transtool is not configured for development mode		
		if (StringHelper.containsNonWhitespace(transToolApplicationSrcPath)) {
			transToolApplicationSrcPath = transToolApplicationSrcPath.trim();
			transToolApplicationLanguagesDir = new File(transToolApplicationSrcPath);
		}
		if (StringHelper.containsNonWhitespace(transToolApplicationOptSrcPath)) {
			transToolApplicationOptSrcPath = transToolApplicationOptSrcPath.trim();
			transToolApplicationOptLanguagesSrcDir = new File(transToolApplicationOptSrcPath);
		}
		
		// Enable or disable translation tool and i18n source directories
		boolean translationToolEnabled = "enabled".equals(transToolEnabled);
		if (translationToolEnabled) {
			if (transToolApplicationLanguagesDir != null && transToolApplicationOptLanguagesSrcDir != null) {
				// Check if configuration is valid, otherwise disable translation server mode
			} else {
				// disable, pathes not configured properly
				translationToolEnabled = false;
				log.warn("Translation configuration enabled but invalid translation tool source path defined. Disabling translation tool. Fix your configuration in spring config of i18Module");
				log.warn(" transToolApplicationSrcPath::" + transToolApplicationSrcPath + " transToolApplicationI18nSrcPath::" + transToolApplicationOptSrcPath);
			}
		}

		// Get all bundles that contain i18n files
		initBundleNames();

		// Search for all available languages on the build path and initialize them
		doInitAvailableLanguages();

		// B) Initialize default language and the list of enabled languages from
		// the persisted system configuration
		doInitLanguageConfiguration();
		
		// Initialize how gendering shall be done
		doInitGenderStrategies();
		
		log.info("Configured i18nModule with default language::" + getDefaultLocale().toString() + " and the reference languages '"
				+ referenceLanguages + "' and the following enabled languages: " + enabledLanguagesKeys.toString());
	}

	@Override
	public void destroy() {
		super.destroy();
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
		// Search all availableLanguages files that exist 

		String i18nDirRelPath = File.separator + applicationFallbackBundle.replace(".", File.separator) + File.separator + I18nManager.I18N_DIRNAME;
		if (transToolApplicationLanguagesDir != null) {
			File coreSrcI18nDir = new File(transToolApplicationLanguagesDir, i18nDirRelPath);
			if (coreSrcI18nDir.exists()) {
				for (String languageCode : searchForAvailableLanguages(transToolApplicationLanguagesDir)) {
					if (availableLanguages.contains(languageCode)) {
						String path = "";
						if (transToolApplicationOptLanguagesSrcDir != null) path = transToolApplicationOptLanguagesSrcDir.getAbsolutePath();
						log.debug("Skipping duplicate or previously loaded language::" + languageCode + " found in " +path );
						continue;
					}
					log.debug("Detected translatable language " + languageCode + " in " + transToolApplicationLanguagesDir.getAbsolutePath());
					availableLanguages.add(languageCode);
					translatableLanguages.add(languageCode);
					translatableLangAppBaseDirLookup.put(languageCode, transToolApplicationLanguagesDir);
				}
			}
		}
		// 2) Add languages from the translation tool source path
		if (isTransToolEnabled()) {
			for (String languageCode : searchForAvailableLanguages(transToolApplicationOptLanguagesSrcDir)) {
				if (availableLanguages.contains(languageCode)) {
					log.debug("Skipping duplicate or previously loaded language::" + languageCode + " found in " + transToolApplicationOptLanguagesSrcDir.getAbsolutePath());
					continue;
				}
				log.debug("Detected translatable language " + languageCode + " in " + transToolApplicationOptLanguagesSrcDir.getAbsolutePath());
				availableLanguages.add(languageCode);
				translatableLanguages.add(languageCode);
				translatableLangAppBaseDirLookup.put(languageCode, transToolApplicationOptLanguagesSrcDir);
			}
		}

		String folderRoot = WebappHelper.getBuildOutputFolderRoot();
		if(StringHelper.containsNonWhitespace(folderRoot)) {
			//started from WEB-INF/classes
			File libDir = new File(WebappHelper.getBuildOutputFolderRoot());
			for (String languageCode : searchForAvailableLanguages(libDir)) {
				if (availableLanguages.contains(languageCode)) {
					log.debug("Skipping duplicate or previously loaded  language::" + languageCode + " found in " + libDir.getAbsolutePath());
					continue;
				}
				log.debug("Detected non-translatable language " + languageCode + " in " + libDir.getAbsolutePath());
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
					log.warn("Skipping duplicate or previously loaded  language::" + languageCode + " found in "
							+ LANG_PACKS_DIRECTORY.getAbsolutePath());
					continue;
				}
				log.debug("Force non-translatable language " + languageCode + " defined from enabledLanguages.");
				availableLanguages.add(languageCode);
			}
		}
		
		// 4) Add languages from the customizing lang packs
		for (String languageCode : searchForAvailableLanguages(LANG_PACKS_DIRECTORY)) {
			if (availableLanguages.contains(languageCode)) {
				log.warn("Skipping duplicate or previously loaded  language::" + languageCode + " found in "
						+ LANG_PACKS_DIRECTORY.getAbsolutePath());
				continue;
			}
			log.debug("Detected non-translatable language " + languageCode + " in " + LANG_PACKS_DIRECTORY.getAbsolutePath());
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
		List<String> toRemoveLangs = new ArrayList<>();
		//
		// Build list of all locales and the overlay locales if available
		for (String langKey : availableLanguages) {
			Locale locale = createLocale(langKey);
			if (locale == null) {
				log.error("Could not create locale for lang::" + langKey + ", skipping language and remove it from list of available languages");
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
				Locale overlayLocale = createOverlay(locale);
				// Calculate the overlay key as used as reference. Note, this is not the
				// same as overlayLocale.toString(), this would add '_' for each element
				String overlayKey = getLocaleKey(overlayLocale);
				if (overlayLocale == null) {
					log.error("Could not create overlay locale for lang::" + langKey + " (" + overlayKey + "), skipping language");
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
		// fallbackLangKey can't be null because EN is guaranteed to be available,
		// see above
		fallbackLocale = allLocales.get(fallbackLanguage);

		// Check if translation tool reference languages are available
		if (isTransToolEnabled() && transToolReferenceLanguages.size() == 0) {
			log.error("Did not find the fallback language configuration in the configuration, using language::en instead");
		} else {
			for (String langKey : transToolReferenceLanguages) {
				if (!allLocales.containsKey(langKey)) {
					log.error("The configured fallback language::" + langKey + " does not exist. Using language::en instead");
				}
			}
		}
	}
	
	/**
	 * Search for available languages in the given directory. The translation
	 * files must start with 'LocalStrings_' and end with '.properties'.
	 * Everything in between is considered a language key.
	 * <p>
	 * If the directory contains jar files, those files are opened and searched
	 * for languages files as well. In this case, the algorythm only looks for
	 * translation files that are in the org/olat/core/_i18n package
	 * 
	 * @param i18nDir
	 * @return set of language keys the system will find translations for
	 */
	Set<String> searchForAvailableLanguages(File i18nDir) {
		Set<String> foundLanguages = new TreeSet<>();
		i18nDir = new File(i18nDir.getAbsolutePath()+"/org/olat/_i18n");
		if (i18nDir.exists()) {
			// First check for locale files
			String[] langFiles = i18nDir.list(i18nFileFilter);
			for (String langFileName : langFiles) {
				String lang = langFileName.substring(I18nModule.LOCAL_STRINGS_FILE_PREFIX.length(), langFileName.lastIndexOf("."));
				foundLanguages.add(lang);
				log.debug("Adding lang::" + lang + " from filename::" + langFileName + " from dir::" + i18nDir.getAbsolutePath());
			}
		}
		return foundLanguages;
	}
	
	/**
	 * Create a local that represents the overlay locale for the given locale
	 * 
	 * @param locale The original locale
	 * @return The overlay locale
	 */
	Locale createOverlay(Locale locale) {
		String lang = locale.getLanguage();
		String country = (locale.getCountry() == null ? "" : locale.getCountry());
		String variant = createOverlayKeyForLanguage(locale.getVariant() == null ? "" : locale.getVariant());
		Locale overlay = new Locale(lang, country, variant);
		return overlay;
	}
	
	/**
	 * Add the overlay postfix to the given language key
	 * @param langKey
	 * @return
	 */
	String createOverlayKeyForLanguage(String langKey) {
		return langKey + "__" + getOverlayName();
	}
	
	/**
	 * Helper method to create a locale from a given locale key ('de', 'de_CH',
	 * 'de_CH_ZH')
	 * 
	 * @param localeKey
	 * @return the locale or NULL if no locale could be generated from this string
	 */
	Locale createLocale(String localeKey) {
		Locale aloc = null;
		// de
		// de_CH
		// de_CH_zueri
		String[] parts = localeKey.split("_");
		switch (parts.length) {
			case 1:
				aloc = new Locale(parts[0]);
				break;
			case 2:
				aloc = new Locale(parts[0], parts[1]);
				break;
			case 3:
				String lastPart = parts[2];
				// Add all remaining parts to variant, variant can contain
				// underscores according to Locale spec
				for (int i = 3; i < parts.length; i++) {
					String part = parts[i];
					lastPart = lastPart + "_" + part;
				}
				aloc = new Locale(parts[0], parts[1], lastPart);
				break;
			default:
				return null;
		}
		// Test if the locale has been constructed correctly. E.g. when the
		// language part is not existing in the ISO chart, the locale can
		// convert to something else.
		// E.g. he_HE_HE will convert automatically to iw_HE_HE
		if (aloc.toString().equals(localeKey)) {
			return aloc;
		} else {
			return null;
		}
	}
	
	/**
	 * Calculate the locale key that identifies the given locale. Adds support for
	 * the overlay mechanism.
	 * 
	 * @param locale
	 * @return
	 */
	public String getLocaleKey(Locale locale) {
		String key = localeToLocaleKey.get(locale);
		if(key == null) {
			String langKey = locale.getLanguage();
			String country = locale.getCountry();
			// Only add country when available - in case of an overlay country is
			// set to
			// an empty value
			if (StringHelper.containsNonWhitespace(country)) {
				langKey = langKey + "_" + country;
			}
			String variant = locale.getVariant();
			// Only add the _ separator if the variant contains something in
			// addition to
			// the overlay, otherways use the __ only
			if (StringHelper.containsNonWhitespace(variant)) {
				if (variant.startsWith("__" + getOverlayName())) {
					langKey += variant;
				} else {
					langKey = langKey + "_" + variant;
				}
			}
			
			key = localeToLocaleKey.putIfAbsent(locale, langKey);
			if(key == null) {
				key = langKey;
			}
			
		}
		return key;
	}
	
	private static FilenameFilter i18nFileFilter = new FilenameFilter() {
		@Override
		public boolean accept(File dir, String name) {
			// don't add overlayLocales as selectable availableLanguages
			// (LocaleStrings_de__VENDOR.properties)
			if (name.startsWith(I18nModule.LOCAL_STRINGS_FILE_PREFIX) && name.indexOf("_") != 0 && name.endsWith(I18nModule.LOCAL_STRINGS_FILE_POSTFIX)) { return true; }
			return false;
		}
	};

	private void doInitLanguageConfiguration() {
		// Set the default language
		String defaultLanguageKey = getStringPropertyValue(CONFIG_DEFAULT_LANG, false);
		Locale newDefaultLocale = allLocales.get(defaultLanguageKey);
		if (newDefaultLocale == null) {
			log.error("Could not set default locale to value::" + defaultLanguageKey + " - no such language found. Using fallback locale instead");
			newDefaultLocale = allLocales.get(transToolReferenceLanguages.get(0));
		} else if (!availableLanguages.contains(newDefaultLocale.toString())) {
			log.error("Did not find the default language::" + newDefaultLocale.toString()
					+ " in the available availableLanguages files! Using fallback locale instead");
			newDefaultLocale = allLocales.get(transToolReferenceLanguages.get(0));
		}
		defaultLocale = newDefaultLocale;
		log.info("Setting default locale::" + newDefaultLocale.toString());

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
		log.info("Enabling languages::" + enabledLanguagesConfig);
		// Make sure that the configured default language is enabled
		if (!enabledLanguagesKeys.contains(getDefaultLocale().toString())) {
			String defLang = getDefaultLocale().toString();
			enabledLanguagesKeys.add(defLang);
			log.warn("The configured default language::" + defLang + " is not in the list of enabled languages. Enabling language::" + defLang);
		}
	}
	
	private void doInitGenderStrategies() {
		for (String language : availableLanguages) {
			String langKey = language;
			Locale locale = getAllLocales().get(langKey);
			if (locale != null) {				
				String configValue = getStringPropertyValue("genderStrategy." + langKey, null);
				GenderStrategy genderStrategy = null;
				if (StringHelper.containsNonWhitespace(configValue)) {
					genderStrategy = GenderStrategy.valueOf(configValue.trim());
				}
				if (genderStrategy == null) {
					genderStrategy = GenderStrategy.star; // default;
				}				
				genderStrategies.put(locale, genderStrategy);
			} else {
				log.error("Locale for key::" + langKey + " not found in allLocales.");
			}
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
	public void setDefaultLocale(Locale newDefaultLocale) {
		if (defaultLocale == null || !defaultLocale.toString().equals(newDefaultLocale.toString())) {
			// Just set the string property here. This will fire an event and
			// call the method initFromChangedProperties()
			setStringProperty(CONFIG_DEFAULT_LANG, newDefaultLocale.toString(), true);
		}
	}

	/**
	 * @return The locale that is used when a string is not found in any other
	 *         locale
	 */
	public Locale getFallbackLocale() {
		return fallbackLocale;
	}

	/*
	 * Static getter methods, mainly used by I18nManager
	 */

	/**
	 * @return true: caching is enabled; false: caching is disabled
	 */
	public boolean isCachingEnabled() {
		return cachingEnabled;
	}
	
	public File getLangPacksDirectory() {
		return LANG_PACKS_DIRECTORY;
	}

	/**
	 * @return as keys: a Set of Strings with the supported languages (e.g. de,
	 *         de_CH, en, ...). those are the languages that are installed. See
	 *         the enabled languages to get the list of languages that are enabled
	 *         to be used
	 */
	public Set<String> getAvailableLanguageKeys() {
		return availableLanguages;
	}

	/**
	 * @return A set of language keys that can be translated by the translation
	 *         tool. Theses are the languages that are available in the source
	 *         form. Languages embedded in jars can't be edited.
	 */
	public Set<String> getTranslatableLanguageKeys() {
		return translatableLanguages;
	}

	/**
	 * @return the map (with dummy value) of all languages including
	 *         overlayLocales (as a String)
	 */
	public Map<String, Locale> getAllLocales() {
		return allLocales;
	}

	/**
	 * @return The lookup map of the overlay locales. Key: the locale; value: the
	 *         corresponding overlay
	 */
	public Map<Locale, Locale> getOverlayLocales() {
		return overlayLocales;
	}

	/**
	 * @return as keys: a List of Strings with the supported languages (e.g. de,
	 *         de_CH, en, ...). those are the languages which can be chosen by the
	 *         user
	 */
	public Collection<String> getEnabledLanguageKeys() {
		synchronized (enabledLanguagesKeys) {
			return new HashSet<>(enabledLanguagesKeys);
		}
	}

	/**
	 * @return as keys: a List of Strings with the supported languages overlay keys
	 */
	public Set<String> getOverlayLanguageKeys() {
		return overlayLanguagesKeys;
	}

	
	/**
	 * Set the languages that are enabled on the system. The change affects the
	 * system immediately and is persisted in the olatdata/system/configuration
	 * 
	 * @param newEnabledLangKeys
	 */
	public void setEnabledLanguageKeys(Collection<String> newEnabledLangKeys) {
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
			setStringProperty(CONFIG_LANGUAGES_ENABLED, newEnabledConfig.toString(), true);
			// No need to reinitialize i18n Module, setting the new property will already do this
		}
	}

	/**
	 * @return A list of language keys that are reference and fallback languages
	 */
	public List<String> getTransToolReferenceLanguages() {
		return transToolReferenceLanguages;
	}

	/**
	 * @return All bundles that contain a _i18n directory with translation files
	 */
	public List<String> getBundleNamesContainingI18nFiles() {
		return bundleNames;
	}

	/**
	 * @return The bundle name that contains the commonly used translations from
	 *         the olatcore framework
	 */
	public String getCoreFallbackBundle() {
		return coreFallbackBundle;
	}

	/**
	 * @return The bundle name that contains the commonly used translations from
	 *         the application
	 */
	public String getApplicationFallbackBundle() {
		return applicationFallbackBundle;
	}
	
	public GenderStrategy getGenderStrategy(Locale locale) {
		// remove overlay
		String langKey = getLocaleKey(locale);
		langKey = langKey.split("__")[0]; 
		locale = createLocale(langKey);
		// load from module config or use default
		GenderStrategy strategy = genderStrategies.get(locale);
		if (strategy == null) {
			strategy = GenderStrategy.star; // default;
		}
		return strategy;
	}
	
	public void setGenderStrategy(Locale locale, GenderStrategy genderStrategy) {
		// remove overlay
		String langKey = getLocaleKey(locale);
		langKey = langKey.split("__")[0];
		locale = createLocale(langKey);
		// persist in module config
		setStringProperty("genderStrategy." + langKey, genderStrategy.name(), true);
		genderStrategies.put(locale, genderStrategy);
		reInitializeAndFlushCache();
	}
	

	/**
	 * Checks if the overlay mechanism is enabled. The overlay is similar to the
	 * locale variant, but adds another layer on top of it.
	 * 
	 * @return true: enabled; false: disabled
	 */
	public boolean isOverlayEnabled() {
		return (overlayEnabled && StringHelper.containsNonWhitespace(overlayName));
	}

	/**
	 * Returns the overlay name or NULL if not configured. For an overlay file
	 * with the name 'LocalStrings_de__VENDOR.properties' it will return 'VENDOR'
	 * 
	 * @return name of the overlay
	 */
	public String getOverlayName() {
		if (isOverlayEnabled()) return overlayName;
		else return null;
	}

	/**
	 * @return true: enable the translation tool; false: disable the translation
	 *         tool
	 */
	public boolean isTransToolEnabled() {
		return "enabled".equals(transToolEnabled);
	}

	/**
	 * search for bundles that contain i18n files. Searches in the org.olat.core
	 * package
	 */
	void initBundleNames() {
		bundleNames = searchForBundleNamesContainingI18nFiles();
	}
	
	/**
	 * Search in all packages on the source patch for packages that contain an
	 * _i18n directory that can be used to store olatcore localization files
	 * 
	 * @return set of bundles that contain olatcore i18n compatible localization
	 *         files
	 */
	List<String> searchForBundleNamesContainingI18nFiles() {
		List<String> foundBundles;
		// 1) First search on normal source path of application
		String srcPath = null; 
		File applicationDir = getTransToolApplicationLanguagesSrcDir();
		if (applicationDir != null) {
			srcPath = applicationDir.getAbsolutePath();
		} else {
			// Fall back to compiled classes
			srcPath = WebappHelper.getBuildOutputFolderRoot();
		}
		if(StringHelper.containsNonWhitespace(srcPath)) {
			I18nDirectoriesVisitor srcVisitor = new I18nDirectoriesVisitor(srcPath, getTransToolReferenceLanguages());
			FileUtils.visitRecursively(new File(srcPath), srcVisitor);
			foundBundles = srcVisitor.getBundlesContainingI18nFiles();
			// 3) For jUnit tests, add also the I18n test dir
			if (Settings.isJUnitTest()) {
				Resource testres = new ClassPathResource("olat.local.properties");
				String jUnitSrcPath = null;
				try {
					jUnitSrcPath = testres.getFile().getAbsolutePath();
				} catch (IOException e) {
					throw new StartupException("Could not find classpath resource for: test-classes/olat.local.property ", e);
	  			}
	
	
				I18nDirectoriesVisitor juniSrcVisitor = new I18nDirectoriesVisitor(jUnitSrcPath, getTransToolReferenceLanguages());
				FileUtils.visitRecursively(new File(jUnitSrcPath), juniSrcVisitor);
				foundBundles.addAll(juniSrcVisitor.getBundlesContainingI18nFiles());
			}
			// Sort alphabetically
			Collections.sort(foundBundles);
		} else {
			foundBundles = new ArrayList<>();
		}
		return foundBundles;
	}

	/**
	 * Get the base source directory for the given language where the language
	 * files for this bundle are stored
	 * 
	 * @param locale
	 * @param bundleName
	 * @return The file or null if the language is not read-write configured
	 */
	public File getPropertyFilesBaseDir(Locale locale, String bundleName) {
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
		String localeKey = getLocaleKey(locale);
		return translatableLangAppBaseDirLookup.get(localeKey);
	}

	/**
	 * Reinitialize the entire i18n system
	 */
	public void reInitializeAndFlushCache() {
		// Re-initialize all local caches
		doReInitialize();
		// Notify other nodes to reInitialize the caches as well
		I18nReInitializeCachesEvent changedConfigEvent = new I18nReInitializeCachesEvent();
		CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(changedConfigEvent, I18N_CACHE_FLUSHED_EVENT_CHANNEL);				
	}

	/**
	 * Private helper that implements the reinitialization of all caches. This
	 * is decoupled from the reInitialize() to prevent endless firing of events
	 * in the cluster.
	 */
	private void doReInitialize() {		
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
			genderStrategies.clear();
			I18nManager.getInstance().clearCaches();
			// Now rebuild everything from scratch
			doInit();
		}
	}

	/**
	 * @see org.olat.core.configuration.AbstractOLATModule#event(org.olat.core.gui.control.Event)
	 */
	@Override
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
	public File getTransToolApplicationLanguagesSrcDir() {
		return transToolApplicationLanguagesDir;
	}

	/**
	 * Get the language dir for the applications i18n different than DE and EN
	 * files. Only available when in translation server mode
	 * 
	 * @return
	 */
	public File getTransToolApplicationOptLanguagesSrcDir() {
		return transToolApplicationOptLanguagesSrcDir;
	}

	/**
	 * Config parameter to define the gender strategy used for words with a combined
	 * female and male word ending.
	 * In the i18n files the second ending is writen in curly brackets. 
	 * E.g. strategy "star": "Benutzer{in}" => "Benutzer*in"
	 * 
	 * @author gnaegi
	 *
	 */
	public enum GenderStrategy {
		star, // default;
		colon,
		middleDot,
		dot,
		slash,
		slashDash,
		dash,
		camelCase
	}
}

