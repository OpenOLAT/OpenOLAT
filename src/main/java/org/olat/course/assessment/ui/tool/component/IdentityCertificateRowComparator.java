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
package org.olat.course.assessment.ui.tool.component;

import java.util.Comparator;
import java.util.Date;

import org.olat.course.assessment.ui.tool.IdentityCertificateRow;

/**
 * 
 * Initial date: 27 mars 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class IdentityCertificateRowComparator implements Comparator<IdentityCertificateRow> {

	@Override
	public int compare(IdentityCertificateRow o1, IdentityCertificateRow o2) {
		int c = Boolean.compare(o1.isLast(), o2.isLast());
		
		if(c == 0) {
			Date d1 = o1.getCreationDate();
			Date d2 = o2.getCreationDate();
			if(d1 != null && d2 != null) {
				c = d1.compareTo(d2);
			} else if(d1 == null && d2 != null) {
				c = -1;
			} else if(d1 != null) {
				c = 1;
			}
		}
		return -c;
	}
}
