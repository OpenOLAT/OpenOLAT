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
package org.olat.modules.curriculum.model;

import java.util.List;

import org.olat.basesecurity.IdentityRef;
import org.olat.modules.certificationprogram.CertificationProgram;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementRef;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.modules.curriculum.CurriculumRef;
import org.olat.repository.RepositoryEntryRef;

/**
 * 
 * Initial date: 14 oct. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumElementInfosSearchParams {
	
	private RepositoryEntryRef entry;
	private List<? extends CurriculumRef> curriculums;
	private List<CurriculumElementRef> curriculumElements;
	private CurriculumElement parentElement;
	private boolean parentElementInclusive;
	
	private boolean implementationsOnly;
	private List<CurriculumElementStatus> statusList;
	
	private final IdentityRef identity;
	
	private CertificationProgram certificationProgram;
	
	public CurriculumElementInfosSearchParams(IdentityRef identity) {
		this.identity = identity;
	}
	
	public static final CurriculumElementInfosSearchParams searchDescendantsOf(IdentityRef identity, CurriculumElement parentElement) {
		CurriculumElementInfosSearchParams params = new CurriculumElementInfosSearchParams(identity);
		params.setParentElement(parentElement, true);
		return params;
	}
	
	public static final CurriculumElementInfosSearchParams searchElementsOf(IdentityRef identity, CurriculumRef curriculum) {
		CurriculumElementInfosSearchParams params = new CurriculumElementInfosSearchParams(identity);
		params.setCurriculum(curriculum);
		return params;
	}
	
	public static final CurriculumElementInfosSearchParams searchElementsOf(IdentityRef identity, RepositoryEntryRef entry) {
		CurriculumElementInfosSearchParams params = new CurriculumElementInfosSearchParams(identity);
		params.setEntry(entry);
		return params;
	}
	
	public static final CurriculumElementInfosSearchParams searchElements(IdentityRef identity, List<CurriculumElementRef> elements) {
		CurriculumElementInfosSearchParams params = new CurriculumElementInfosSearchParams(identity);
		params.setCurriculumElements(elements);
		return params;
	}
	
	public IdentityRef getIdentity() {
		return identity;
	}

	public List<? extends CurriculumRef> getCurriculums() {
		return curriculums;
	}

	public void setCurriculum(CurriculumRef curriculum) {
		this.curriculums = List.of(curriculum);
	}
	
	public void setCurriculums(List<? extends CurriculumRef> curriculums) {
		this.curriculums = List.copyOf(curriculums);
	}

	public RepositoryEntryRef getEntry() {
		return entry;
	}

	public void setEntry(RepositoryEntryRef entry) {
		this.entry = entry;
	}

	public List<CurriculumElementRef> getCurriculumElements() {
		return curriculumElements;
	}

	public void setCurriculumElements(List<CurriculumElementRef> curriculumElements) {
		this.curriculumElements = curriculumElements;
	}
	
	public boolean isParentElementInclusive() {
		return parentElementInclusive;
	}

	public CurriculumElement getParentElement() {
		return parentElement;
	}

	public void setParentElement(CurriculumElement parentElement, boolean parentElementInclusive) {
		this.parentElement = parentElement;
		this.parentElementInclusive = parentElementInclusive;
	}

	public boolean isImplementationsOnly() {
		return implementationsOnly;
	}

	public void setImplementationsOnly(boolean implementationsOnly) {
		this.implementationsOnly = implementationsOnly;
	}

	public List<CurriculumElementStatus> getStatusList() {
		return statusList;
	}

	public void setStatusList(List<CurriculumElementStatus> statusList) {
		this.statusList = statusList;
	}

	public CertificationProgram getCertificationProgram() {
		return certificationProgram;
	}

	public void setCertificationProgram(CertificationProgram certificationProgram) {
		this.certificationProgram = certificationProgram;
	}

}
