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

import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

import org.olat.modules.library.model.CatalogItem;

/**
 * 
 * Initial date: 18.12.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PublicationDateComparator extends TitleComparator implements Comparator<CatalogItem> {

	public PublicationDateComparator(Locale locale) {
		super(locale);
	}

	@Override
	public int compare(CatalogItem o1, CatalogItem o2) {
		int c = 0;

		Date p1 = o1.getPubDate();
		Date p2 = o2.getPubDate();
		if(p1 == null) {
			c = (p2 == null ? 0 : -1);
		} else if(p2 == null) {
			c = 1;
		} else {
			c = p1.compareTo(p2);
		}

		if(c == 0) {
			c = super.compare(o1, o2);
		}
		return c;
	}
}