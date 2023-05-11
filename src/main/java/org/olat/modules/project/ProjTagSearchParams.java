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
import java.util.stream.Collectors;

/**
 * 
 * Initial date: 13 Mar 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ProjTagSearchParams {
	
	private Long projectKey;
	private Collection<Long> artefactKeys;
	private Collection<String> artefactTypes;
	private Collection<ProjectStatus> artefactStatus;

	public Long getProjectKey() {
		return projectKey;
	}

	public void setProject(ProjProjectRef project) {
		this.projectKey = project.getKey();
	}
	
	public Collection<Long> getArtefactKeys() {
		return artefactKeys;
	}
	
	public void setArtefacts(Collection<? extends ProjArtefactRef> artefacts) {
		this.artefactKeys = artefacts.stream().map(ProjArtefactRef::getKey).collect(Collectors.toSet());
	}
	
	public Collection<String> getArtefactTypes() {
		return artefactTypes;
	}

	public void setArtefactTypes(Collection<String> artefactTypes) {
		this.artefactTypes = artefactTypes;
	}

	public Collection<ProjectStatus> getArtefactStatus() {
		return artefactStatus;
	}

	public void setArtefactStatus(Collection<ProjectStatus> artefactStatus) {
		this.artefactStatus = artefactStatus;
	}
	
}
