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
package org.olat.modules.project;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.id.OrganisationRef;

/**
 * 
 * Initial date: 22 Nov 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ProjProjectSearchParams {
	
	private IdentityRef identity;
	private List<OrganisationRef> projectOrganisations;
	private Collection<Long> projectKeys;
	private Collection<ProjectStatus> status;
	
	public IdentityRef getIdentity() {
		return identity;
	}

	public void setIdentity(IdentityRef identity) {
		this.identity = identity;
	}

	public List<OrganisationRef> getProjectOrganisations() {
		return projectOrganisations;
	}

	public void setProjectOrganisations(List<OrganisationRef> projectOrganisations) {
		this.projectOrganisations = projectOrganisations;
	}

	public Collection<Long> getProjectKeys() {
		return projectKeys;
	}
	
	public void setProjectKeys(Collection<? extends ProjProjectRef> projects) {
		this.projectKeys = projects.stream().map(ProjProjectRef::getKey).collect(Collectors.toList());
	}

	public Collection<ProjectStatus> getStatus() {
		return status;
	}

	public void setStatus(Collection<ProjectStatus> status) {
		this.status = status;
	}
	
}
