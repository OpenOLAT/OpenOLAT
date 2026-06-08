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

import org.olat.modules.selectus.model.ApplicationLight;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  1 mars 2011 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class IdComparator implements Comparator<ApplicationLight> {
	
	private final LastnameComparator lastnameComparator = new LastnameComparator();

	@Override
	public int compare(ApplicationLight a1, ApplicationLight a2) {
		if(a1 == null) return 1;
		if(a2 == null) return -1;
		
		Integer i1 = a1.getId();
		Integer i2 = a2.getId();
		if(i1 == null) return 1;
		if(i2 == null) return -1;

		int result = i1.compareTo(i2);
		if(result == 0) {
			return lastnameComparator.compare(a1, a2);
		}
		return -result;
	}
}
