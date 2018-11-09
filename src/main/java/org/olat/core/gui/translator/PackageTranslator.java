/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.  
* <p>
*/ 

package org.olat.core.gui.translator;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Locale;

import org.apache.log4j.Level;
import org.olat.core.CoreSpringFactory;
import org.olat.core.helpers.Settings;
import org.olat.core.logging.OLATRuntimeException;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.i18n.I18nModule;

/**
 * @author Felix Jost
 */
public class PackageTranslator implements Translator {
	
	private static final OLog log = Tracing.createLoggerFor(PackageTranslator.class);
	
	private final boolean fallBack;
	private Translator fallBackTranslator;
	private final String packageName;
	private Locale locale;
	
	private transient I18nModule i18nModule;
	private transient I18nManager i18nManager;
	

	private PackageTranslator(String packageName, Locale locale, boolean fallBack, Translator fallBackTranslator) {
		this.locale = locale;
		this.packageName = packageName;
		this.fallBackTranslator = fallBackTranslator;
		this.fallBack = fallBack;
		i18nManager = CoreSpringFactory.getImpl(I18nManager.class);
		i18nModule = CoreSpringFactory.getImpl(I18nModule.class);
	}
	
	private Object readResolve() {
		i18nManager = CoreSpringFactory.getImpl(I18nManager.class);
		i18nModule = CoreSpringFactory.getImpl(I18nModule.class);
		return this;
	}

	/**
	 * @param packageName
	 * @param locale
	 * @param fallBack
	 */
	private PackageTranslator(String packageName, Locale locale, boolean fallBack) {
		this(packageName, locale, fallBack, null);
	}
	
	/**
	 * cascade two translators
	 * do not use this with multiple cascaded translators! they are lost with this method!
	 * 
	 * @param main
	 * @param fallback
	 * @return
	 */
	public static Translator cascadeTranslators(PackageTranslator main, Translator fallback){
		return new PackageTranslator(main.packageName, main.locale, fallback);
	}
	
	/**
	 * recursively cascade with all fallbacks up to maxDeep levels
	 * @param main
	 * @param fallback
	 * @return
	 */
	public Translator cascadeTranslatorsWithAllFallback(PackageTranslator main, Translator fallback){
		if (this.fallBackTranslator instanceof PackageTranslator && main.fallBackTranslator != fallback && this.fallBackTranslator != fallback){
			PackageTranslator tempTrans = (PackageTranslator) this.fallBackTranslator;
			PackageTranslator oldPos = this;
			int maxDeep = 4;
			while (tempTrans != null && maxDeep > 0) {
				oldPos = tempTrans;
				tempTrans = (PackageTranslator) tempTrans.fallBackTranslator;
				maxDeep--;
			}
			if (fallback != oldPos.fallBackTranslator && oldPos != oldPos.fallBackTranslator) {
				oldPos.fallBackTranslator = fallback;
			}
			return main;
		} 
		return cascadeTranslators(main, fallback);		
	}

	/**
	 * @param packageName only the package use "class.getPackage().getName()" for it!
	 * @param locale
	 * @param fallBackTranslator
	 */
	public PackageTranslator(String packageName, Locale locale, Translator fallBackTranslator) {
		this(packageName, locale, false, fallBackTranslator);
	}

	/**
	 * default with fallback mode
	 * 
	 * @param packageName only the package use "class.getPackage().getName()" for it!
	 * @param locale
	 */
	public PackageTranslator(String packageName, Locale locale) {
		this(packageName, locale, true);
	}

	/**
	 * Translates the string from the packageName localization file.
	 * 
	 * @param key The key to translate
	 * @return The internationalized strings
	 */
	@Override
	public String translate(String key) {
		return translate(key, null);
	}
	
	@Override
	public String translate(String key, String[] args) {
		return translate(key, args, Level.WARN);
	}

	@Override
	public String translate(String key, String[] args, Level missingTranslationLogLevel) {
		String val = translate(key,args, 0, false);		
		// if still null -> fallback to default locale (if not in debug mode)
		if (val == null) {
			if (Settings.isDebuging()) {
				val = getErrorMessage(key);
			} else {
				// try with fallBackToDefaultLocale 
				val = translate(key, args, 0, true);
			}
		}

		// else value got translated or there is at least an error message telling
		// which key was not found.
		// Note: val may be null if there is a localstrings file missing in the default language. use the online translation tool to double-check
		
		// Error: ! even in default language: missing translation key! 
		if (val == null) {
			val = getErrorMessage(key);
			// Workaround to prevent the warning about shibboleth-attribute
			if (!packageName.startsWith("org.olat.course.condition")
					&& missingTranslationLogLevel!=null
					&& !missingTranslationLogLevel.equals(Level.OFF)) {
				if (missingTranslationLogLevel.equals(Level.ERROR)) {
					log.error(val);
				} else if (missingTranslationLogLevel.equals(Level.WARN)) {
					log.warn(val);
				} else if (missingTranslationLogLevel.equals(Level.INFO)) {
					log.info(val);
				}
			}
			// don't use error message in GUI for production, use key instead (OLAT-5896)
			if (!Settings.isDebuging()) { 
				val = key;
			}
		}
		return val;
	}

  /**
   * Recursive lookup for a key. Used in translate(String key, String[] args). 
   * Should not be called directly, use translate(String key, String[] args).
   * Must be public, because the definition is in an interface. 
   * @see org.olat.core.gui.translator.Translator#translate(java.lang.String, java.lang.String[], boolean)
   */
	@Override
	public String translate(String key, String[] args, int recursionLevel, boolean fallBackToDefaultLocale) {
		boolean overlayEnabled = i18nModule.isOverlayEnabled();
		String val = i18nManager.getLocalizedString(packageName, key, args, locale, overlayEnabled, fallBackToDefaultLocale);
		if (val == null) {
			// if not found, try the fallBackTranslator
			if (fallBackTranslator != null && recursionLevel < 10) {
				val = fallBackTranslator.translate(key, args, recursionLevel+1, fallBackToDefaultLocale);
			} else if (fallBack) { // both fallback and fallbacktranslator does not
				// make sense; latest translator in chain should
				// fallback to application fallback.
				val = i18nManager.getLocalizedString(i18nModule.getApplicationFallbackBundle(), key, args, locale, overlayEnabled, fallBackToDefaultLocale);
				if (val == null) {
					// lastly fall back to brasato framework fallback
					val = i18nManager.getLocalizedString(i18nModule.getCoreFallbackBundle(), key, args, locale, overlayEnabled, fallBackToDefaultLocale);
				}
			}
		} 
		return val;
	}
	
	/**
	 * Internal helper to format an error message for the given key
	 * @param key
	 * @return
	 */
	private String getErrorMessage(String key) {

		StringBuilder sb = new StringBuilder(150);
		sb.append(NO_TRANSLATION_ERROR_PREFIX).append(key)
		  .append(": in ").append(packageName)
		  .append(" (fallback:").append(fallBack);

		String babel;
		if (fallBackTranslator instanceof PackageTranslator) {
			babel = ((PackageTranslator)fallBackTranslator).packageName + " " + fallBackTranslator.toString();
		} else {
			babel = fallBackTranslator == null ? "-" : fallBackTranslator.toString();
		}
		sb.append(", fallBackTranslator:").append(babel);
		sb.append(") for locale ").append(locale);
		OLATRuntimeException ore = new OLATRuntimeException("transl dummy",null);
		//use stracktrace to find out more where the missing translation comes from
		final Writer result = new StringWriter();
		final PrintWriter printWriter = new PrintWriter(result);
		ore.printStackTrace(printWriter);
		
		sb.append(result.toString());
		return sb.toString();
	}

	/**
	 * @see org.olat.core.gui.translator.Translator#getLocale()
	 */
	public Locale getLocale() {
		return this.locale;
	}

	/**
	 * Not used normally. Sets the locale. Use only if e.g. a translator (which
	 * should then be an instance variable of a controller) is needed in the DMZ
	 * area where no user is logged in yet
	 * 
	 * @param locale The locale to set
	 */
	@Override
	public void setLocale(Locale locale) {
		this.locale = locale;
		if (fallBackTranslator != null) {
			fallBackTranslator.setLocale(locale);
		}
	}

	/**
	 * The package of this package translator
	 * @return
	 */
	public String getPackageName() {
		return packageName;
	}
	
	@Override
	public String toString(){		
		return "PackageTranslator for package: " + packageName + " is fallback: " + fallBack + " next child if any: \n " + ((this.fallBackTranslator != null && this.fallBackTranslator == this) ? "recurse itself !" : this.fallBackTranslator);
	}
	
	public boolean isStacked(){
		return this.fallBackTranslator != null;
	}
	
}