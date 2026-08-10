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
package org.olat.user.ui.organisation;

import java.text.Collator;
import java.util.Locale;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTreeNodeComparator;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTreeTableNode;

/**
 *
 * Initial date: 10 Aug 2026<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class OrganisationRowComparator extends FlexiTreeNodeComparator {

	private final Collator collator;

	public OrganisationRowComparator(Locale locale) {
		collator = Collator.getInstance(locale);
	}

	@Override
	protected int compareNodes(FlexiTreeTableNode o1, FlexiTreeTableNode o2) {
		OrganisationRow row1 = (OrganisationRow)o1;
		OrganisationRow row2 = (OrganisationRow)o2;

		int c = compareDisplayNames(row1, row2);
		if(c == 0) {
			c = Long.compare(row1.getKey().longValue(), row2.getKey().longValue());
		}
		return c;
	}

	private int compareDisplayNames(OrganisationRow row1, OrganisationRow row2) {
		String name1 = row1.getDisplayName();
		String name2 = row2.getDisplayName();
		if(name1 == null || name2 == null) {
			return compareNullObjects(name1, name2);
		}
		return collator.compare(name1, name2);
	}

}
