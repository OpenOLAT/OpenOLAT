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
*/

package org.olat.group.ui;

import java.util.Locale;

import org.olat.core.gui.translator.PackageTranslator;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.AssertException;
import org.olat.core.util.Util;
import org.olat.group.BusinessGroup;

/**
 * Description:<BR>
 * Provides a factory method to create a package translator that has a chain of
 * fallback translators.
 * <P>
 * Initial Date: Aug 31, 2004
 * 
 * @author gnaegi
 */
public class BGTranslatorFactory {
	private static final String PACKAGE_BASE = Util.getPackageName(BGTranslatorFactory.class);
	private static final String PACKAGE_BG = PACKAGE_BASE + ".buddygroup";
	private static final String PACKAGE_LG = PACKAGE_BASE + ".learninggroup";
	private static final String PACKAGE_RG = PACKAGE_BASE + ".rightgroup";

	private BGTranslatorFactory() {
	// never called
	}

	/**
	 * Creates a package translator for the given package that has a fallback
	 * translator to the group ui package translator and uses a group type
	 * specific translator. This means: in the group type properties files all
	 * strings can me overridden. If a string is not found there, olat tries to
	 * translate it using the package properties files. If the string is not in
	 * there, it uses the default group ui properties files.
	 * 
	 * @param packageName The package for which a translator should be created or
	 *          null if only the default group and the type specific translator
	 *          should be used
	 * @param groupType The group type
	 * @param locale The users locale
	 * @return A package translator with a chain of fallback translators
	 */
	public static PackageTranslator createBGPackageTranslator(String packageName, String groupType, Locale locale) {
		Translator packageTrans, groupDefaultTrans;

		// Initialize translators: default translator and group type specific
		// translator
		// Hierarchical fallback translations:
		// 3 - generic group translations
		// with fallback to olat default translator
		groupDefaultTrans = new PackageTranslator(PACKAGE_BASE, locale);
		// 2 - group package specific (edit, run, management etc) translations
		// with fallback to generic group translator (3)
		if (packageName == null) {
			// don't use additional package translator
			packageTrans = groupDefaultTrans;
		} else {
			packageTrans = new PackageTranslator(packageName, locale, groupDefaultTrans);
		}
		// 1 - group type specific translations (buddy, learning, right)
		// with fallback to package translator (2)
		if (groupType.equals(BusinessGroup.TYPE_BUDDYGROUP)) { return new PackageTranslator(PACKAGE_BG, locale, packageTrans); }
		if (groupType.equals(BusinessGroup.TYPE_LEARNINGROUP)) {
			return new PackageTranslator(PACKAGE_LG, locale, packageTrans);
		} else if (groupType.equals(BusinessGroup.TYPE_RIGHTGROUP)) {
			return new PackageTranslator(PACKAGE_RG, locale, packageTrans);
		} else {
			throw new AssertException("Unknown group type ::" + groupType);
		}
	}
}
