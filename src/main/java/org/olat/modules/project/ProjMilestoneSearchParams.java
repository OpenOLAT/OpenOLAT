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
import java.util.Date;
import java.util.stream.Collectors;

/**
 * 
 * Initial date: 9 Mar 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ProjMilestoneSearchParams {
	
	private ProjProjectRef project;
	private Collection<Long> milestoneKeys;
	private Collection<String> identifiers;
	private Collection<Long> artefactKeys;
	private Collection<ProjectStatus> status;
	private Date createdAfter;
	private Boolean dueDateNull;

	public ProjProjectRef getProject() {
		return project;
	}

	public void setProject(ProjProjectRef project) {
		this.project = project;
	}

	public Collection<Long> getMilestoneKeys() {
		return milestoneKeys;
	}
	
	public void setMilestones(Collection<? extends ProjMilestoneRef> milestones) {
		this.milestoneKeys = milestones.stream().map(ProjMilestoneRef::getKey).collect(Collectors.toList());
	}

	public Collection<String> getIdentifiers() {
		return identifiers;
	}

	public void setIdentifiers(Collection<String> identifiers) {
		this.identifiers = identifiers;
	}

	public Collection<Long> getArtefactKeys() {
		return artefactKeys;
	}
	
	public void setArtefacts(Collection<ProjArtefact> artefacts) {
		this.artefactKeys = artefacts.stream().map(ProjArtefact::getKey).collect(Collectors.toSet());
	}

	public Collection<ProjectStatus> getStatus() {
		return status;
	}

	public void setStatus(Collection<ProjectStatus> status) {
		this.status = status;
	}

	public Date getCreatedAfter() {
		return createdAfter;
	}

	public void setCreatedAfter(Date createdAfter) {
		this.createdAfter = createdAfter;
	}

	public Boolean getDueDateNull() {
		return dueDateNull;
	}

	public void setDueDateNull(Boolean dueDateNull) {
		this.dueDateNull = dueDateNull;
	}
	
}
