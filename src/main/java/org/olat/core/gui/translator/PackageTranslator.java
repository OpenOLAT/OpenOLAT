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

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Locale;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.helpers.Settings;
import org.olat.core.logging.OLATRuntimeException;
import org.olat.core.logging.Tracing;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.i18n.I18nModule;

/**
 * @author Felix Jost
 */
public class PackageTranslator implements Translator {
	
	private static final Logger log = Tracing.createLoggerFor(PackageTranslator.class);
	private static final String[] EMPTY_ARR = new String[0];
	
	private Translator fallBackTranslator;
	private final String packageName;
	private Locale locale;
	
	private transient I18nModule i18nModule;
	private transient I18nManager i18nManager;
	

	/**
	 * default with fallback mode
	 * 
	 * @param packageName only the package use "class.getPackage().getName()" for it!
	 * @param locale
	 */
	public PackageTranslator(String packageName, Locale locale) {
		this(packageName, locale, null);
	}
	
	public PackageTranslator(String packageName, Locale locale, Translator fallBackTranslator) {
		this.locale = locale;
		this.packageName = packageName;
		if(fallBackTranslator != null
				&& packageName.equals(fallBackTranslator.getPackageName())
				&& fallBackTranslator instanceof PackageTranslator) {
			this.fallBackTranslator = ((PackageTranslator)fallBackTranslator).fallBackTranslator;
		} else {
			this.fallBackTranslator = fallBackTranslator;
		}

		i18nManager = CoreSpringFactory.getImpl(I18nManager.class);
		i18nModule = CoreSpringFactory.getImpl(I18nModule.class);
	}
	
	private Object readResolve() {
		i18nManager = CoreSpringFactory.getImpl(I18nManager.class);
		i18nModule = CoreSpringFactory.getImpl(I18nModule.class);
		return this;
	}
	
	/**
	 * cascade two translators
	 * do not use this with multiple cascaded translators! they are lost with this method!
	 * 
	 * @param main
	 * @param fallback
	 * @return
	 */
	public static Translator cascadeTranslators(PackageTranslator main, Translator fallback) {
		if(main.packageName.equals(fallback.getPackageName()) && fallback instanceof PackageTranslator) {
			fallback = ((PackageTranslator)fallback).fallBackTranslator;
		}
		return new PackageTranslator(main.packageName, main.locale, fallback);
	}

	/**
	 * Translates the string from the packageName localization file.
	 * 
	 * @param key The key to translate
	 * @return The internationalized strings
	 */
	@Override
	public String translate(String key) {
		return translate(key, EMPTY_ARR);
	}
	
	@Override
	public String translate(String key, String... args) {
		return translate(key, args, Level.WARN);
	}

	@Override
	public String translate(String key, String[] args, Level missingTranslationLogLevel) {
		String val = translate(key, args, 0, false);		
		// if still null -> fallback to default locale (if not in debug mode)
		if (val == null) {
			if (Settings.isDebuging()) {
				val = getErrorMessageWithTrace(key);
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
					&& missingTranslationLogLevel != null && !missingTranslationLogLevel.equals(Level.OFF)) {
				log.log(missingTranslationLogLevel, getErrorMessage(key), new OLATRuntimeException("transl dummy", null));
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
			if (fallBackTranslator != null) {
				if(recursionLevel < 10) {
					val = fallBackTranslator.translate(key, args, recursionLevel+1, fallBackToDefaultLocale);
				}
			} else { // both fallback and fallbacktranslator does not
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
	private String getErrorMessageWithTrace(String key) {
		String msg = getErrorMessage(key);
		OLATRuntimeException ore = new OLATRuntimeException("transl dummy");
		//use stracktrace to find out more where the missing translation comes from
		try(Writer result = new StringWriter();
				PrintWriter printWriter = new PrintWriter(result)) {
			printWriter.write(msg);
			ore.printStackTrace(printWriter);
			return result.toString();
		} catch(IOException e) {
			log.error("", e);
			return msg;
		}
	}
	
	private String getErrorMessage(String key) {
		StringBuilder sb = new StringBuilder(256);
		sb.append(NO_TRANSLATION_ERROR_PREFIX).append(key)
		  .append(": in ").append(packageName);

		String babel;
		if (fallBackTranslator instanceof PackageTranslator) {
			babel = ((PackageTranslator)fallBackTranslator).packageName + " " + fallBackTranslator.toString();
		} else {
			babel = fallBackTranslator == null ? "-" : fallBackTranslator.toString();
		}
		sb.append(", fallBackTranslator:").append(babel);
		sb.append(") for locale ").append(locale);
		return sb.toString();
	}

	@Override
	public Locale getLocale() {
		return locale;
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
	@Override
	public String getPackageName() {
		return packageName;
	}
	
	@Override
	public String toString(){		
		return "PackageTranslator for package: " + packageName + " is fallback: " + (fallBackTranslator == null) + " next child if any: " + ((fallBackTranslator != null && fallBackTranslator == this) ? "recurse itself !" : fallBackTranslator);
	}
	
	public boolean isStacked(){
		return this.fallBackTranslator != null;
	}

	@Override
	public int hashCode() {
		return locale.hashCode() + packageName.hashCode()
			+ (fallBackTranslator == null ? -14 : fallBackTranslator.hashCode());
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof PackageTranslator) {
			PackageTranslator translator = (PackageTranslator)obj;
			return locale.equals(translator.locale)
					&& packageName.equals(translator.packageName)
					&& ((fallBackTranslator == null && translator.fallBackTranslator == null)
							|| (fallBackTranslator != null && fallBackTranslator.equals(translator.fallBackTranslator)));
		}
		return false;
	}
}