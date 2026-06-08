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

import org.olat.core.util.StringHelper;

import org.olat.modules.selectus.model.ApplicationLight;
import org.olat.modules.selectus.model.Person;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  1 mars 2011 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class TitleComparator implements Comparator<ApplicationLight> {
	
	private final LastnameComparator lastnameComparator = new LastnameComparator();

	@Override
	public int compare(ApplicationLight a1, ApplicationLight a2) {
		if(a1 == null) return 1;
		if(a2 == null) return -1;
		
		Person p1 = a1.getPerson();
		Person p2 = a2.getPerson();
		if(p1 == null) return 1;
		if(p2 == null) return -1;
		
		String t1 = p1.getTitle();
		String t2 = p2.getTitle();
		if(!StringHelper.containsNonWhitespace(t1)) return 1;
		if(!StringHelper.containsNonWhitespace(t2)) return -1;
		int result = t1.compareToIgnoreCase(t2);
		if(result == 0) {
			return lastnameComparator.compare(a1, a2);
		}
		return result;
	}
}
