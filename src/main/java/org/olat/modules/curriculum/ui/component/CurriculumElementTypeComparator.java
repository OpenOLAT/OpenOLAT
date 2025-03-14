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
package org.olat.modules.curriculum.ui.component;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;

import org.olat.modules.curriculum.CurriculumElementType;

/**
 * 
 * Initial date: 14 mars 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CurriculumElementTypeComparator implements Comparator<CurriculumElementType> {
	
	private final Collator collator;
	
	public CurriculumElementTypeComparator(Locale locale) {
		collator = Collator.getInstance(locale);
	}

	@Override
	public int compare(CurriculumElementType o1, CurriculumElementType o2) {
		if(o1 == null || o2 == null) {
			return compareNullObjects(o1, o2);
		}
		
		int c = 0;
		String dn1 = o1.getDisplayName();
		String dn2 = o2.getDisplayName();
		if(dn1 == null || dn2 == null) {
			c = compareNullObjects(dn1, dn2);
		} else {
			c = collator.compare(dn1, dn2);
		}
		
		if(c == 0) {
			c = o1.getKey().compareTo(o2.getKey());
		}
		return c;
	}
	
	private static final int compareNullObjects(final Object a, final Object b) {
		boolean ba = (a == null);
		boolean bb = (b == null);
		return ba? (bb? 0: -1):(bb? 1: 0);
	}
}
