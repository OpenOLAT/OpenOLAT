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
package org.olat.modules.certificationprogram.ui.component;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;

import org.olat.modules.creditpoint.CreditPointSystem;

/**
 * 
 * Initial date: 7 janv. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CreditPointSystemNameComparator implements Comparator<CreditPointSystem> {

	private final Collator collator; 
	
	public CreditPointSystemNameComparator(Locale locale) {
		collator = Collator.getInstance(locale);
	}

	@Override
	public int compare(CreditPointSystem o1, CreditPointSystem o2) {
		String n1 = o1.getName();
		String n2 = o2.getName();
		
		int c = 0;
		if(n1 == null || n2 == null) {
			boolean n2n = n2 == null;
			c = n1 == null ? (n2n ? 0: 1) : (n2n ? -1: 0);
		} else if(n1 != null && n2 != null) {
			c = collator.compare(n1, n2);
		}
		
		if(c == 0) {
			c = o1.getKey().compareTo(o2.getKey());
		}
		return c;
	}
}
