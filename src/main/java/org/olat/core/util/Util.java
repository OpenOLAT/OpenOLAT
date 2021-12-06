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

package org.olat.core.util;

import java.util.Locale;

import org.olat.core.gui.translator.PackageTranslator;
import org.olat.core.gui.translator.Translator;


/**
 * Description: <br>
 * 
 * @author Felix Jost
 */
public class Util {

	/**
	 * @deprecated please use createPackageTranslator....
	 * Returns the package name for this class
	 * 
	 * @param clazz
	 * @return the package name
	 */
	public static String getPackageName(Class<?> clazz) {
		return clazz.getPackage().getName();
	}

	/**
	 * Converts the package name to the velocity root path by replacing .'s with
	 * /'s. The prefix org will be stripped away. Use this to set velocity pages
	 * in a velocity container.
	 * 
	 * @param clazz
	 * @return the velocity root path
	 */
	public static String getPackageVelocityRoot(Class<?> clazz) {
		return getPackageVelocityRoot(getPackageName(clazz));
	}

	/**
	 * Converts the package name to the velocity root path by replacing .'s with
	 * /'s. The prefix org will be stripped away. Use this to set velocity pages
	 * in a velocity container.
	 * 
	 * @param packageName
	 * @return the velocity root path
	 */
	public static String getPackageVelocityRoot(String packageName) {
		//TASK:fj:b compress velocity code with ant task. for debug mode: use _content, for productive mode, use _cleanedcontent or _compressedcontent (which does not mean gzip, but removing unnecessary whitespaces and such)
		//	ch.goodsolutions.bla -> ch/goodsolutions/bla/_content where the velocity pages are found
		return packageName.replace('.', '/') + "/_content";
	}

	public static Translator createPackageTranslator(Class<?> baseClass, Locale locale) {
		return createPackageTranslator(baseClass, locale, null);
	}
	
	public static Translator createPackageTranslator(Class<?> baseClass, Class<?> fallbackClass, Locale locale) {
		String fallbackpackage = Util.getPackageName(fallbackClass);
		Translator fallback = new PackageTranslator(fallbackpackage, locale);
		String transpackage = Util.getPackageName(baseClass);
		return new PackageTranslator(transpackage, locale, fallback);
	}
	
	public static Translator createPackageTranslator(Translator baseClass, Class<?> fallbackClass, Locale locale) {
		String fallbackpackage = Util.getPackageName(fallbackClass);
		Translator fallback = new PackageTranslator(fallbackpackage, locale);
		return new PackageTranslator(baseClass.getPackageName(), locale, fallback);
	}
	
	public static Translator createPackageTranslator(Translator translator, Translator fallback, Locale locale) {
		return new PackageTranslator(((PackageTranslator)translator).getPackageName(), locale, fallback);
	}
	
	/**
	 * 
	 * returns a Translator for the given baseclass and locale
	 * @param baseClass the location of the class will be taken to resolve the relative resource "_i18n/LocalStrings_(localehere).properties"
	 * @param locale
	 * @param fallback The fallback translator that should be used
	 * @return
	 */
	public static Translator createPackageTranslator(Class<?> baseClass, Locale locale, Translator fallback) {
		String transpackage = Util.getPackageName(baseClass);
		return createPackageTranslator(transpackage, locale, fallback);
	}
	
	/**
	 * 
	 * Returns a Translator for the given package and locale
	 * @param transpackage the location of the package will be taken to resolve the relative resource "_i18n/LocalStrings_(localehere).properties"
	 * @param locale
	 * @param fallback The fallback translator that should be used
	 * @return
	 */
	public static Translator createPackageTranslator(String transpackage, Locale locale, Translator fallback) {
		Translator trans;
		if(fallback != null) {
			trans = new PackageTranslator(transpackage, locale, fallback);
		} else {
			trans = new PackageTranslator(transpackage, locale);
		}
		return trans;
	}
}