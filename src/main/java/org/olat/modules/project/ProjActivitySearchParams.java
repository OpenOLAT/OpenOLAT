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
import org.olat.modules.project.ProjActivity.Action;
import org.olat.modules.project.ProjActivity.ActionTarget;

/**
 * 
 * Initial date: 17 Jan 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ProjActivitySearchParams {
	
	private Collection<Action> actions;
	private Collection<ActionTarget> targets;
	private Long doerKey;
	private Collection<Long> projectKeys;
	private Collection<Long> artefactKeys;
	private List<ProjDateRange> createdDateRanges;

	public Collection<Action> getActions() {
		return actions;
	}

	public void setActions(Collection<Action> actions) {
		this.actions = actions;
	}
	
	public Collection<ActionTarget> getTargets() {
		return targets;
	}

	public void setTargets(Collection<ActionTarget> targets) {
		this.targets = targets;
	}

	public Long getDoerKey() {
		return doerKey;
	}
	
	public void setDoer(IdentityRef doer) {
		this.doerKey = doer.getKey();
	}
	
	public Collection<Long> getProjectKeys() {
		return projectKeys;
	}
	
	public void setProject(ProjProjectRef project) {
		this.projectKeys = List.of(project.getKey());
	}
	
	public void setProjects(Collection<? extends ProjProjectRef> projects) {
		this.projectKeys = projects.stream().map(ProjProjectRef::getKey).collect(Collectors.toSet());
	}

	public Collection<Long> getArtefactKeys() {
		return artefactKeys;
	}

	public void setArtefacts(Collection<? extends ProjArtefactRef> artefacts) {
		this.artefactKeys = artefacts.stream().map(ProjArtefactRef::getKey).collect(Collectors.toList());
	}

	public List<ProjDateRange> getCreatedDateRanges() {
		return createdDateRanges;
	}

	public void setCreatedDateRanges(List<ProjDateRange> createdDateRanges) {
		this.createdDateRanges = createdDateRanges;
	}

}
