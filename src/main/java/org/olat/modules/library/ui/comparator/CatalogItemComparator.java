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

import org.olat.core.commons.modules.bc.comparators.MetaTitleComparator;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.modules.library.model.CatalogItem;

/**
 * Compares the title of two meta tagged objects.
 * 
 * <P>
 * Initial Date: Jul 9, 2009 <br>
 * 
 * @author gwassmann
 */
public class CatalogItemComparator implements Comparator<CatalogItem> {
	private final Comparator<VFSMetadata> comparator;

	public CatalogItemComparator(Collator collator) {
		comparator = new MetaTitleComparator(collator);
	}

	public CatalogItemComparator(Locale locale) {
		this(Collator.getInstance(locale));
	}

	@Override
	public int compare(CatalogItem i1, CatalogItem i2) {
		VFSMetadata m1 = i1.getMetaInfo();
		VFSMetadata m2 = i2.getMetaInfo();
		return comparator.compare(m1, m2);
	}
}
