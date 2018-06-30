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
package org.olat.modules.quality.ui.wizard;

import java.util.ArrayList;
import java.util.Collection;

import org.olat.basesecurity.GroupRoles;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 29.06.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CourseContext extends IdentityContext {

	private Collection<RepositoryEntry> repositoryEntries;
	private Collection<GroupRoles> roles;

	public Collection<RepositoryEntry> getRepositoryEntries() {
		if (repositoryEntries == null) {
			repositoryEntries = new ArrayList<>(0);
		}
		return repositoryEntries;
	}

	public void setRepositoryEntries(Collection<RepositoryEntry> repositoryEntries) {
		this.repositoryEntries = repositoryEntries;
	}

	public Collection<GroupRoles> getRoles() {
		if (roles == null) {
			roles = new ArrayList<>(0);
		}
		return roles;
	}

	public void setRoles(Collection<GroupRoles> roles) {
		this.roles = roles;
	}

}
