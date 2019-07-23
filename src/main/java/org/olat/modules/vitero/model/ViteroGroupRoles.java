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
 * 12.10.2011 by frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.modules.vitero.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  10 nov. 2011 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class ViteroGroupRoles {
	

	private final List<String> emailsOfParticipants = new ArrayList<>();
	private final Map<String, GroupRole> emailsToRole = new HashMap<>();
	private final Map<String, Integer> emailsToVmsUserId = new HashMap<>();
	
	public List<String> getEmailsOfParticipants() {
		return emailsOfParticipants;
	}
	
	public Map<String, GroupRole> getEmailsToRole() {
		return emailsToRole;
	}
	
	public Map<String, Integer> getEmailsToVmsUserId() {
		return emailsToVmsUserId;
	}
	
	public int size() {
		return emailsOfParticipants.size();
	}
}
