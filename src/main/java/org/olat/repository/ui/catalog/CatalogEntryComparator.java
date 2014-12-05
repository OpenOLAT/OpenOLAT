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
package org.olat.repository.ui.catalog;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;

import org.olat.repository.CatalogEntry;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 21.11.2012<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CatalogEntryComparator implements Comparator<CatalogEntry> {
	
	private final Collator myCollator;
	
	public CatalogEntryComparator(Locale locale) {
		myCollator = Collator.getInstance(locale);
	}
	
	@Override
	public int compare(final CatalogEntry c1, final CatalogEntry c2) {
		String c1Title, c2Title;
		if (c1.getType() == CatalogEntry.TYPE_LEAF) {
			final RepositoryEntry repoEntry = c1.getRepositoryEntry();
			if (repoEntry != null) {
				c1Title = repoEntry.getDisplayname();
			} else {
				c1Title = c1.getName();
			}
		} else {
			c1Title = c1.getName();
		}
		if (c2.getType() == CatalogEntry.TYPE_LEAF) {
			final RepositoryEntry repoEntry = c2.getRepositoryEntry();
			if (repoEntry != null) {
				c2Title = repoEntry.getDisplayname();
			} else {
				c2Title = c2.getName();
			}
		} else {
			c2Title = c2.getName();
		}
		// Sort now based on users locale
		
		return myCollator.compare(c1Title, c2Title);
	}
}