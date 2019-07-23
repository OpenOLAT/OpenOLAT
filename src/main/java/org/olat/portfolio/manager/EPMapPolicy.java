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

package org.olat.portfolio.manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.basesecurity.Invitation;
import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;
import org.olat.group.BusinessGroup;
import org.olat.portfolio.model.structel.EPStructureElementToGroupRelation;

/**
 * 
 * Description:<br>
 * A wrapper to embedded the policies with the same logical boundary
 * (permission and duration)
 * 
 * <P>
 * Initial Date:  5 nov. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class EPMapPolicy {
	
	private Date to;
	private Date from;
	private Type type = Type.user;
	
	private Invitation invitation;
	private List<Identity> identities = new ArrayList<>();
	private List<BusinessGroup> groups = new ArrayList<>();
	private final List<EPStructureElementToGroupRelation> relations = new ArrayList<>();
	
	public Invitation getInvitation() {
		return invitation;
	}

	public void setInvitation(Invitation invitation) {
		this.invitation = invitation;
	}

	public List<EPStructureElementToGroupRelation> getRelations() {
		return relations;
	}
	
	public void addRelation(EPStructureElementToGroupRelation relation) {
		relations.add(relation);
	}

	public Date getTo() {
		return to;
	}

	public void setTo(Date to) {
		this.to = to;
	}

	public Date getFrom() {
		return from;
	}

	public void setFrom(Date from) {
		this.from = from;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}
	
	public Map<String,String> getIdentitiesValue() {
		if(identities == null) return new HashMap<>();
		
		Map<String,String> values = new HashMap<>();
		for(Identity identity:identities) {
			String login = identity.getName();
			String first = identity.getUser().getProperty(UserConstants.FIRSTNAME, null);
			String last = identity.getUser().getProperty(UserConstants.LASTNAME, null);
			values.put(last + " " + first, login);
		}
		return values;
	}

	public List<Identity> getIdentities() {
		return identities;
	}

	public void setIdentities(List<Identity> identities) {
		this.identities = identities;
	}
	
	public void addIdentities(List<Identity> identitiesToAdd) {
		if(identities == null) {
			identities = new ArrayList<>();
		}
		identities.addAll(identitiesToAdd);
	}
	
	public Map<String,String> getGroupsValues() {
		if(groups == null) return new HashMap<>();
		
		Map<String,String> values = new HashMap<>();
		for(BusinessGroup group:groups) {
			values.put(group.getName(), group.getKey().toString());
		}
		return values;
	}

	public List<BusinessGroup> getGroups() {
		return groups;
	}

	public void setGroups(List<BusinessGroup> groups) {
		this.groups = groups;
	}
	
	public void addGroup(BusinessGroup group) {
		for(BusinessGroup g:groups) {
			if(g.equals(group)) {
				return;
			}
		}
		groups.add(group);
	}
	
	public enum Type {
		user,
		group,
		invitation,
		allusers;
		
		public static String[] names() {
			String[]  names = new String[values().length];
			int i=0;
			for(Type type:values()) {
				names[i++] = type.name();
			}
			return names;
		}
	}
}
