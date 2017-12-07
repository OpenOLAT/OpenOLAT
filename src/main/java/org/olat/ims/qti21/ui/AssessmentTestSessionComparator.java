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
package org.olat.ims.qti21.ui;

import java.util.Comparator;
import java.util.Date;

import org.olat.ims.qti21.AssessmentTestSession;

/**
 * 
 * Initial date: 24 nov. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentTestSessionComparator  implements Comparator<AssessmentTestSession> {

	@Override
	public int compare(AssessmentTestSession a1, AssessmentTestSession a2) {
		Date t1 = a1.getTerminationTime();
		if(t1 == null) {
			t1 = a1.getFinishTime();
		}
		Date t2 = a2.getTerminationTime();
		if(t2 == null) {
			t2 = a2.getFinishTime();
		}
		
		int c;
		if(t1 == null && t2 == null) {
			c = 0;
		} else if(t2 == null) {
			return 1;
		} else if(t1 == null) {
			return -1;
		} else {
			c = t1.compareTo(t2);
		}
		
		if(c == 0) {
			Date c1 = a1.getCreationDate();
			Date c2 = a2.getCreationDate();
			if(c1 == null && c2 == null) {
				c = 0;
			} else if(c2 == null) {
				return -1;
			} else if(c1 == null) {
				return 1;
			} else {
				c = c1.compareTo(c2);
			}
		}
		return -c;
	}
}