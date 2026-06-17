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
package org.olat.modules.selectus.ui.committee.list;

import org.olat.core.id.Identity;

import org.olat.modules.selectus.model.PositionRole;

/**
 * 
 * Initial date: 26 oct. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CommitteeMemberRow {

	private final boolean canRate;
	private final Identity identity;
	private final PositionRole role;

	private int numOfRatings;
	private int numOfAssignments;
	private int numOfAssignedRatings;
	
	public CommitteeMemberRow(Identity identity, PositionRole role, boolean canRate) {
		this.identity = identity;
		this.role = role;
		this.canRate = canRate;
	}
	
	public String getKey() {
		return identity.getKey().toString();
	}

	public Identity getIdentity() {
		return identity;
	}
	
	public PositionRole getRole() {
		return role;
	}
	
	public boolean isCanRate() {
		return canRate;
	}

	public int getNumOfRatings() {
		return numOfRatings;
	}

	public void setNumOfRatings(int numOfRatings) {
		this.numOfRatings = numOfRatings;
	}

	public int getNumOfAssignedRatings() {
		return numOfAssignedRatings;
	}

	public void setNumOfAssignedRatings(int numOfAssignedRatings) {
		this.numOfAssignedRatings = numOfAssignedRatings;
	}

	public int getNumOfAssignments() {
		return numOfAssignments;
	}

	public void setNumOfAssignments(int numOfAssignments) {
		this.numOfAssignments = numOfAssignments;
	}
}
