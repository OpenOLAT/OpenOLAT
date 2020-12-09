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
package org.olat.course.member.wizard;

import org.olat.group.BusinessGroup;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 1 avr. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MembersContext {
	
	private final BusinessGroup group;
	private final Curriculum curriculum;
	private final RepositoryEntry repoEntry;
	private final CurriculumElement rootCurriculumElement;
	
	private final boolean overrideManaged;
	private final boolean extendedCurriculumRoles;
	private final boolean sendMailMandatory;
	
	private MembersContext(RepositoryEntry repoEntry, BusinessGroup group, Curriculum curriculum,
			CurriculumElement rootCurriculumElement, boolean overrideManaged, boolean extendedCurriculumRoles, boolean sendMailMandatory) {
		this.repoEntry = repoEntry;
		this.group = group;
		this.curriculum = curriculum;
		this.rootCurriculumElement = rootCurriculumElement;
		this.overrideManaged = overrideManaged;
		this.extendedCurriculumRoles = extendedCurriculumRoles;
		this.sendMailMandatory = sendMailMandatory;
	}
	
	public static MembersContext valueOf(BusinessGroup businessGroup) {
		return new MembersContext(null, businessGroup, null, null, false, false, false);
	}
	
	public static MembersContext valueOf(RepositoryEntry repoEntry, boolean overrideManaged) {
		return new MembersContext(repoEntry, null, null, null, overrideManaged, false, false);
	}
	
	public static MembersContext valueOf(RepositoryEntry repoEntry, BusinessGroup businessGroup, boolean overrideManaged, boolean sendMailMandatory) {
		return new MembersContext(repoEntry, businessGroup, null, null, overrideManaged, false, sendMailMandatory);
	}
	
	public static MembersContext valueOf(Curriculum curriculum, CurriculumElement rootCurriculumElement, boolean overrideManaged, boolean extendedCurriculumRoles) {
		return new MembersContext(null, null, curriculum, rootCurriculumElement, overrideManaged, extendedCurriculumRoles, false);
	}

	public BusinessGroup getGroup() {
		return group;
	}

	public Curriculum getCurriculum() {
		return curriculum;
	}

	public CurriculumElement getRootCurriculumElement() {
		return rootCurriculumElement;
	}

	public RepositoryEntry getRepoEntry() {
		return repoEntry;
	}

	public boolean isOverrideManaged() {
		return overrideManaged;
	}
	
	public boolean isExtendedCurriculumRoles() {
		return extendedCurriculumRoles;
	}
	
	public boolean isSendMailMandatory() {
		return sendMailMandatory;
	}
}
