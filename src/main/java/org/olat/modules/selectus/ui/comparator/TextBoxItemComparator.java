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
package org.olat.modules.selectus.ui.comparator;

import java.util.Comparator;

import org.olat.core.gui.components.textboxlist.TextBoxItem;

/**
 * 
 * Initial date: 14 mars 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TextBoxItemComparator implements Comparator<TextBoxItem> {
	
	@Override
	public int compare(TextBoxItem o1, TextBoxItem o2) {
		if(o1 == null || o2 == null) {
			return compareNulls(o1, o2);
		}

		String l1 = o1.getLabel();
		String l2 = o2.getLabel();
		if(l1 == null || l2 == null) {
			return compareNulls(l1, l2);
		}
		
		int c = l1.compareToIgnoreCase(l2);
		if(c == 0) {
			String v1 = o1.getValue();
			String v2 = o2.getValue();
			if(v1 == null || v2 == null) {
				c = compareNulls(v1, v2);
			} else {
				c = v1.compareToIgnoreCase(v2);
			}
		}
		return c;
	}
	
	private int compareNulls(Object o1, Object o2) {
		if(o1 == null && o2 == null) {
			return 0;
		}
		return o1 == null ? -1 : 1;
	}
}
