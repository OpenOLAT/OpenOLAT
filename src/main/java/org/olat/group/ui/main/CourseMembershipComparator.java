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
package org.olat.group.ui.main;

import java.util.Comparator;

/**
 * First author, tutor, participant, waiting and pending
 * 
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CourseMembershipComparator implements Comparator<CourseMembership> {

	@Override
	public int compare(CourseMembership m1, CourseMembership m2) {
		if(m1.isRepositoryEntryOwner()) {
			if(m2.isRepositoryEntryOwner()) {
				return 0;
			}
			return 1;
		} else if(m2.isRepositoryEntryOwner()) {
			return -1;
		}
		
		if(m1.isCurriculumElementOwner()) {
			if(m2.isCurriculumElementOwner()) {
				return 0;
			}
			return 1;
		} else if(m2.isCurriculumElementOwner()) {
			return -1;
		}
		
		if(m1.isBusinessGroupCoach()) {
			if(m2.isBusinessGroupCoach()) {
				return 0;
			}
			return 1;
		} else if(m2.isBusinessGroupCoach()) {
			return -1;
		}
		
		if(m1.isRepositoryEntryCoach()) {
			if(m2.isRepositoryEntryCoach()) {
				return 0;
			}
			return 1;
		} else if(m2.isRepositoryEntryCoach()) {
			return -1;
		}
		
		if(m1.isBusinessGroupWaiting()) {
			if(m2.isBusinessGroupWaiting()) {
				return 0;
			}
			return 1;
		} else if(m2.isBusinessGroupWaiting()) {
			return -1;
		}
		
		if(m1.isBusinessGroupParticipant()) {
			if(m2.isBusinessGroupParticipant()) {
				return 0;
			}
			return 1;
		} else if(m2.isBusinessGroupParticipant()) {
			return -1;
		}
		
		if(m1.isRepositoryEntryParticipant()) {
			if(m2.isRepositoryEntryParticipant()) {
				return 0;
			}
			return 1;
		} else if(m2.isRepositoryEntryParticipant()) {
			return -1;
		}
		return 0;
	}
}
