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
package org.olat.modules.todo.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.olat.core.id.Identity;
import org.olat.modules.todo.ToDoRole;
import org.olat.modules.todo.ToDoTaskMembers;

/**
 * 
 * Initial date: 29 Mar 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ToDoTaskMembersImpl implements ToDoTaskMembers {
	
	private Set<Identity> identities = new HashSet<>(1);
	private Map<String, Set<Identity>> roleNameToIdentity = new HashMap<>(1);
	private Map<Identity, Set<ToDoRole>> identityToRole = new HashMap<>(1);

	@Override
	public Set<Identity> getMembers() {
		return identities;
	}
	
	@Override
	public Set<Identity> getMembers(ToDoRole role) {
		return roleNameToIdentity.getOrDefault(role.name(), Set.of());
	}
	
	@Override
	public Set<ToDoRole> getRoles(Identity member) {
		return identityToRole.getOrDefault(member, Set.of());
	}
	
	public void add(String role, Identity identity) {
		identities.add(identity);
		
		Set<Identity> identities = roleNameToIdentity.computeIfAbsent(role, a -> new HashSet<>(1));
		identities.add(identity);
		
		Set<ToDoRole> roles = identityToRole.computeIfAbsent(identity, a -> new HashSet<>(1));
		roles.add(ToDoRole.valueOf(role));
	}

}
