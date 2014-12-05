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

/**
 * 
 * Initial date: 18.02.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CatalogEntryNodeComparator implements Comparator<CatalogEntry> {
	
	private final Collator myCollator;
	
	public CatalogEntryNodeComparator(Locale locale) {
		myCollator = Collator.getInstance(locale);
	}
	
	@Override
	public int compare(final CatalogEntry c1, final CatalogEntry c2) {
		if(c1 == null) {
			if(c2 == null) return 0;
			return -1;
		}
		if(c2 == null) return 1;
		
		String t1 = c1.getName();
		String t2 = c2.getName();
		
		if(t1 == null) {
			if(t2 == null) return 0;
			return -1;
		}
		if(t2 == null) return 1;
		
		return myCollator.compare(t1, t2);
	}
}