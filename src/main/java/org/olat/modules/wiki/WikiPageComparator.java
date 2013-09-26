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
package org.olat.modules.wiki;

import java.io.Serializable;
import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;

import org.olat.core.util.i18n.I18nManager;

/**
 * This comparator is intended to compare two wiki pages by their page name. The
 * comparison is done with respect to the current locale. (The Serializable
 * interface is recommended.)
 * <P>
 * Initial Date: Jun 12, 2009 <br>
 * 
 * @author gwassmann
 */
public class WikiPageComparator implements Comparator<WikiPage>, Serializable {

	private static final long serialVersionUID = -107955969426497718L;

	/**
	 * Compares the name of page 'a' to that of page 'b' with respect to the
	 * current locale (different languages have different alphabets and hence
	 * different orders).
	 * 
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(WikiPage a, WikiPage b) {
		// Be aware of locale when ordering
		I18nManager mgr = I18nManager.getInstance();
		Locale userLocale = mgr.getCurrentThreadLocale();
		Collator collator = Collator.getInstance(userLocale);
		collator.setStrength(Collator.PRIMARY);

		// Undefinied order if page a or b is null
		int order = 0;
		if (a != null && b != null) {
			final String nameA = a.getPageName();
			final String nameB = b.getPageName();
			// Compare page names with the localized comparator
			order = collator.compare(nameA, nameB);
		}
		return order;
	}

}
