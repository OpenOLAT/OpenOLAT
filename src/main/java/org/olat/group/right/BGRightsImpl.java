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
package org.olat.group.right;

import java.util.ArrayList;
import java.util.List;

import org.olat.basesecurity.Group;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class BGRightsImpl implements BGRights {
	
	private final Group group;
	private final List<String> rights = new ArrayList<>();
	private final BGRightsRole role;
	
	public BGRightsImpl(Group group, BGRightsRole role) {
		this.group = group;
		this.role = role;
	}
	
	@Override
	public Group getBaseGroup() {
		return group;
	}

	@Override
	public BGRightsRole getRole() {
		return role;
	}

	@Override
	public List<String> getRights() {
		return rights;
	}

	@Override
	public int hashCode() {
		return group == null ? 49872 : group.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == this) {
			return true;
		}
		if(obj instanceof BGRightsImpl) {
			BGRightsImpl rImpl = (BGRightsImpl)obj;
			return group != null && group.equals(rImpl.group)
					&& role != null && role.equals(rImpl.role);
		}
		return super.equals(obj);
	}
}