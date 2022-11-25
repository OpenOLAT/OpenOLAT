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
package org.olat.modules.library.ui.comparator;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;

import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.util.StringHelper;
import org.olat.modules.library.model.CatalogItem;

/**
 * 
 * Initial date: 18.12.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TitleComparator implements Comparator<CatalogItem> {
	
	protected final Collator collator;
	
	public TitleComparator(Locale locale) {
		collator = Collator.getInstance(locale);
	}

	@Override
	public int compare(CatalogItem o1, CatalogItem o2) {
		VFSMetadata m1 = o1.getMetaInfo();
		VFSMetadata m2 = o2.getMetaInfo();
		if(m1 == null) {
			return m2 == null ? 0 : -1;
		} else if(m2 == null) {
			return 1;
		}
		
		String t1 = m1.getTitle();
		if(!StringHelper.containsNonWhitespace(t1)) {
			t1 = o1.getDisplayName();
		}
		String t2 = m2.getTitle();
		if(!StringHelper.containsNonWhitespace(t2)) {
			t2 = o2.getDisplayName();
		}
		
		if(t1 == null) {
			return t2 == null ? 0 : -1;
		} else if(t2 == null) {
			return 1;
		}
		return collator.compare(t1, t2);
	}
}
