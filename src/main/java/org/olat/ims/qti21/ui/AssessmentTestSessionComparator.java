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
 * The comparator places first running sessions, then terminated sessions
 * and cancelled or exploded at the last places.
 * 
 * 
 * Initial date: 24 nov. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentTestSessionComparator implements Comparator<AssessmentTestSession> {
	
	private final boolean checkCancelled;
	
	public AssessmentTestSessionComparator() {
		this(true);
	}
	
	public AssessmentTestSessionComparator(boolean checkCancelled) {
		this.checkCancelled = checkCancelled;
	}

	@Override
	public int compare(AssessmentTestSession a1, AssessmentTestSession a2) {
		int c = 0;
		if(checkCancelled) {
			boolean v1 = !a1.isExploded() && !a1.isCancelled();
			boolean v2 = !a2.isExploded() && !a2.isCancelled();
			if(v1 && !v2) {
				c = 1;
			} else if(!v1 && v2) {
				c = -1;
			}
		}
		
		if(c == 0) {
			Date t1 = getValidDate(a1);
			Date t2 = getValidDate(a2);
			if(t1 == null && t2 == null) {
				c = 0;
			} else if(t2 == null) {
				c = 1;
			} else if(t1 == null) {
				c = -1;
			} else {
				c = t1.compareTo(t2);
			}
		}
		
		if(c == 0) {
			Date c1 = a1.getCreationDate();
			Date c2 = a2.getCreationDate();
			if(c1 == null && c2 == null) {
				c = 0;
			} else if(c2 == null) {
				c = -1;
			} else if(c1 == null) {
				c = 1;
			} else {
				c = c1.compareTo(c2);
			}
		}
		
		if(c == 0) {
			c = a1.getKey().compareTo(a2.getKey());
		}
		
		return -c;
	}
	
	private static final Date getValidDate(AssessmentTestSession a) {
		if(a.isExploded() || a.isCancelled()) {
			return null;
		}
		
		Date t = a.getTerminationTime();
		if(t == null) {
			t = a.getFinishTime();
		}
		if(t == null) {
			t = a.getCreationDate();
		}
		return t;
	}
}