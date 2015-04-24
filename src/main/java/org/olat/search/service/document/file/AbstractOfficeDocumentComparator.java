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
package org.olat.search.service.document.file;

import java.util.Comparator;

import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 22.04.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public abstract class AbstractOfficeDocumentComparator implements Comparator<String> {
	
	protected int comparePosition(String f1, String f2, String prefix) {
		boolean l1;
		String p1;
		if(f1.length() > prefix.length() && f1.endsWith(".xml")) {
			p1 = f1.substring(prefix.length(), f1.indexOf(".xml"));
			l1 = StringHelper.isLong(p1);
		} else {
			p1 = null;
			l1 = false;
		}
		
		boolean l2;
		String p2;
		if(f2.length() > prefix.length() && f2.endsWith(".xml")) {
			p2 = f2.substring(prefix.length(), f2.indexOf(".xml"));
			l2 = StringHelper.isLong(p2);
		} else {
			p2 = null;
			l2 = false;
		}
		
		int c = 0;
		if(l1 && l2) {
			try {
				Long pl1 = Long.parseLong(p1);
				long pl2 = Long.parseLong(p2);
				return (int) (pl2 - pl1);
			} catch (NumberFormatException e) {
				//can happen
			}
		} else if(l1) {
			return -1;
		} else if(l2) {
			return 1;
		}
		return c;
	}

}
