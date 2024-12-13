/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.curriculum.ui.wizard;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.id.Identity;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.curriculum.ui.member.MembershipModification;
import org.olat.resource.OLATResource;

/**
 * 
 * Initial date: 11 déc. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class EditMembersContext {
	
	private final Curriculum curriculum;
	private final CurriculumElement curriculumElement;
	private final List<CurriculumElement> descendants;
	private final List<CurriculumRoles> baseRoles;
	private final List<Identity> identities;
	
	private List<CurriculumRoles> roles;
	private List<MembershipModification> modifications;
	
	public EditMembersContext(List<Identity> identities, List<CurriculumRoles> baseRoles,
			Curriculum curriculum, CurriculumElement curriculumElement, List<CurriculumElement> descendants) {
		this.curriculum = curriculum;
		this.curriculumElement = curriculumElement;
		this.descendants = descendants == null ? List.of() : new ArrayList<>(descendants);
		this.baseRoles = baseRoles;
		this.identities = identities;
	}

	public List<CurriculumRoles> getBaseRoles() {
		return baseRoles;
	}
	
	public List<Identity> getIdentities() {
		return identities;
	}

	public Curriculum getCurriculum() {
		return curriculum;
	}

	public CurriculumElement getCurriculumElement() {
		return curriculumElement;
	}

	public List<CurriculumElement> getDescendants() {
		return descendants;
	}
	
	public List<CurriculumElement> getAllCurriculumElements() {
		List<CurriculumElement> elements = new ArrayList<>();
		elements.add(curriculumElement);
		if(descendants != null && !descendants.isEmpty()) {
			elements.addAll(descendants);
		}
		return elements;
	}
	
	public List<OLATResource> getAllCurriculumElementsResources() {
		List<OLATResource> elements = new ArrayList<>();
		elements.add(curriculumElement.getResource());
		if(descendants != null && !descendants.isEmpty()) {
			for(CurriculumElement descendant:descendants) {
				elements.add(descendant.getResource());
			}
		}
		return elements;
	}

	public List<CurriculumRoles> getRoles() {
		return roles;
	}

	public void setRoles(List<CurriculumRoles> roles) {
		this.roles = roles;
	}

	public List<MembershipModification> getModifications() {
		return modifications;
	}

	public void setModifications(List<MembershipModification> modifications) {
		this.modifications = modifications;
	}
}
