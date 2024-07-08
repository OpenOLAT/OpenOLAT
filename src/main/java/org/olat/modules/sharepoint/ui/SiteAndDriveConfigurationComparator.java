/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.sharepoint.ui;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;

import org.olat.modules.sharepoint.model.SiteAndDriveConfiguration;

/**
 * 
 * Initial date: 8 juil. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SiteAndDriveConfigurationComparator implements Comparator<SiteAndDriveConfiguration> {

	private final Collator collator;
	
	public SiteAndDriveConfigurationComparator(Locale locale) {
		collator = Collator.getInstance(locale);
	}

	@Override
	public int compare(SiteAndDriveConfiguration o1, SiteAndDriveConfiguration o2) {
		int c = compareStrings(o1.getSiteDisplayName(), o2.getSiteDisplayName());
		if(c == 0) {
			c = compareStrings(o1.getSiteName(), o2.getSiteName());
		}

		if(c == 0) {
			c = compareStrings(o1.getDriveName(), o2.getDriveName());
		}
		return c;
	}
	
	private final int compareStrings(String s1, String s2) {
		int c;
		if(s1 == null || s2 == null) {
			c = compareNullObjects(s1, s2);
		} else {
			c = collator.compare(s1, s2);
		}
		return c;
	}
	
	private final int compareNullObjects(final Object a, final Object b) {
		boolean ba = (a == null);
		boolean bb = (b == null);
		return ba? (bb? 0: -1):(bb? 1: 0);
	}
}
