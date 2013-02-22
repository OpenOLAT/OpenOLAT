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
package org.olat.group.model;

import java.util.List;

import org.olat.core.gui.control.Event;

/**
 * Description:<br>
 * transport selected groups to add an identity to
 * 
 * <P>
 * Initial Date: 12.04.2011 <br>
 * 
 * @author Roman Haag, frentix GmbH, roman.haag@frentix.com
 */
public class AddToGroupsEvent extends Event {

	private static final long serialVersionUID = 6173876999047030112L;
	
	private List<Long> ownerList;
	private List<Long> participantList;

	public AddToGroupsEvent(List<Long> ownLong, List<Long> partLong) {
		super("addToGroups");
		this.ownerList = ownLong;
		this.participantList = partLong;
	}

	public List<Long> getOwnerGroupKeys() {
		return ownerList;
	}

	public List<Long> getParticipantGroupKeys() {
		return participantList;
	}
	
	public boolean isEmpty() {
		return (ownerList == null || ownerList.isEmpty())
				&& (participantList == null || participantList.isEmpty());
	}
}
