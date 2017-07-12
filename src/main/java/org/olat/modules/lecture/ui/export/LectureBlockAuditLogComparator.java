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
package org.olat.modules.lecture.ui.export;


import java.util.Comparator;
import java.util.Date;

import org.olat.modules.lecture.LectureBlockAuditLog;

/**
 * 
 * Initial date: 11 juil. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LectureBlockAuditLogComparator implements Comparator<LectureBlockAuditLog> {

	@Override
	public int compare(LectureBlockAuditLog o1, LectureBlockAuditLog o2) {
		if(o1 == null && o2 == null) return 0;
		if(o1 == null) return -1;
		if(o2 == null) return 1;
		
		Date d1 = o1.getCreationDate();
		Date d2 = o2.getCreationDate();
		if(d1 == null && d2 == null) return 0;
		if(d1 == null) return -1;
		if(d2 == null) return 1;
		return d1.compareTo(d2);
	}

}
