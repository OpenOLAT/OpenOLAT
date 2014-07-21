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

import org.olat.group.BusinessGroupMembership;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BusinessGroupMembershipComparator implements Comparator<BusinessGroupMembership> {

	@Override
	public int compare(BusinessGroupMembership m1, BusinessGroupMembership m2) {
		if(m1 == null) {
			if(m2 == null) {
				return 0;
			} else {
				return -1;
			}
		} else if(m2 == null) {
			return 1;
		}
		
		if(m1.isOwner()) {
			if(m2.isOwner()) {
				return 0;
			}
			return 1;
		} else if(m2.isOwner()) {
			return -1;
		}
		
		if(m1.isParticipant() || m1.isParticipant()) {
			if(m2.isParticipant() || m2.isParticipant()) {
				return 0;
			}
			return 1;
		} else if(m2.isParticipant() || m2.isParticipant()) {
			return -1;
		}
		
		if(m1.isWaiting()) {
			if(m2.isWaiting()) {
				return 0;
			}
			return 1;
		} else if(m2.isWaiting()) {
			return -1;
		}
		return 0;
	}
}
