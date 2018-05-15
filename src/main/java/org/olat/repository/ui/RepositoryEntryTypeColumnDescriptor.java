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
package org.olat.repository.ui;

import java.util.Locale;

import org.olat.core.gui.components.table.CustomRenderColumnDescriptor;
import org.olat.repository.RepositoryEntry;

/**
 * Description:<br>
 * This repository entry type column descriptor displays a CSS icon to represent
 * the resource type. The underlying data model must provide an object of type
 * RepositoryEntry
 * 
 * <P>
 * Initial Date: 16.04.2008 <br>
 * 
 * @author Florian Gnägi, http://www.frentix.com
 */
public class RepositoryEntryTypeColumnDescriptor extends CustomRenderColumnDescriptor {

	/**
	 * Constructor for this repo entry type column descriptor.
	 * 
	 * @param headerKey
	 * @param dataColumn
	 * @param action
	 * @param locale
	 * @param aligment
	 */
	public RepositoryEntryTypeColumnDescriptor(String headerKey, int dataColumn, String action, Locale locale, int aligment) {
		super(headerKey, dataColumn, action, locale, aligment, new RepositoryEntryIconRenderer(locale));
	}

	/**
	 * We override the compare method because we want to sort on the resourceable
	 * type name of the contained olat resource and not on the object.
	 * Alternatively we could have implemented the Comparable interface on the
	 * Repository entry, however this would have been missleading because this
	 * compare does not compare the repository entries itself but only the
	 * resource type names.
	 * 
	 * @see org.olat.core.gui.components.table.DefaultColumnDescriptor#compareTo(int,
	 *      int)
	 */
	@Override
	public int compareTo(int rowa, int rowb) {
		RepositoryEntry a = (RepositoryEntry) table.getTableDataModel().getValueAt(rowa, dataColumn);
		RepositoryEntry b = (RepositoryEntry) table.getTableDataModel().getValueAt(rowb, dataColumn);
		// compare is based on repository entries resourceable type name
		if (a == null || b == null) {
			boolean ba = (a == null);
			boolean bb = (b == null);
			int res = ba ? (bb ? 0 : -1) : (bb ? 1 : 0);
			return res;
		}
		return collator.compare(a.getOlatResource().getResourceableTypeName(), b.getOlatResource().getResourceableTypeName());
	}
}