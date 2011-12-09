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

import java.util.Locale;

/**
 * <h3>Description:</h3> The I18nItem represents a unique identifiable
 * translation item within the system consisting of
 * <ul>
 * <li>the bundle (org.olat.core)</li>
 * <li>the key (my.example.title)</li>
 * <li>the locale (de)</li>
 * </ul>
 * To get the translated value for such a string, use the
 * i18nManager.getI18nValue() method
 * <p>
 * Initial Date: 08.09.2008 <br>
 * 
 * @author Florian Gnaegi, frentix GmbH, http://www.frentix.com
 */

public class I18nItem {
	private final String bundleName;
	private final String key;
	private final Locale locale;
	private final int bundlePriority;
	private final int keyPriority;

	/**
	 * Constructor. Use the I18nManager methods to create instances of I18nItems
	 * 
	 * @param packageName
	 * @param key
	 * @param locale
	 * @param bundlePriority
	 * @param keyPriority
	 */
	public I18nItem(String packageName, String key, Locale locale, int bundlePriority, int keyPriority) {
		this.bundleName = packageName;
		this.key = key;
		this.locale = locale;
		this.bundlePriority = bundlePriority;
		this.keyPriority = keyPriority;
	}

	/**
	 * @return the package name of this item
	 */
	public String getBundleName() {
		return bundleName;
	}

	/**
	 * @return The bundles priority that can be used for sorting between bundles
	 */
	public int getBundlePriority() {
		return bundlePriority;
	}

	/**
	 * @return The key for this item which is valid within the package
	 */
	public String getKey() {
		return key;
	}

	/**
	 * @return The keys priority that can be used for sorting with in a bundle
	 */
	public int getKeyPriority() {
		return keyPriority;
	}

	/**
	 * @return The locale for this item
	 */
	public Locale getLocale() {
		return locale;
	}
}
