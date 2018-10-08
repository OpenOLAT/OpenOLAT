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
package org.olat.modules.curriculum.model;

import java.util.List;

import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementMembership;
import org.olat.modules.curriculum.CurriculumElementWithView;
import org.olat.repository.RepositoryEntryMyView;

/**
 * A wrapper class
 * 
 * Initial date: 30 mai 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumElementRepositoryEntryViews implements CurriculumElementWithView {
	
	private final CurriculumElement curriculumElement;
	private final List<RepositoryEntryMyView> entries;
	private final CurriculumElementMembership curriculumMembership;

	
	private boolean curriculumMember;
	private CurriculumElementRepositoryEntryViews parent;
	
	public CurriculumElementRepositoryEntryViews(CurriculumElement curriculumElement, List<RepositoryEntryMyView> entries,
			CurriculumElementMembership curriculumMembership) {
		this.curriculumElement = curriculumElement;
		this.entries = entries;
		this.curriculumMembership = curriculumMembership;
		curriculumMember = curriculumMembership != null && curriculumMembership.hasMembership();
	}
	
	@Override
	public Long getKey() {
		return curriculumElement.getKey();
	}

	public CurriculumElement getCurriculumElement() {
		return curriculumElement;
	}

	public List<RepositoryEntryMyView> getEntries() {
		return entries;
	}
	
	public CurriculumElementMembership getCurriculumMembership() {
		return curriculumMembership;
	}

	@Override
	public CurriculumElementRepositoryEntryViews getParent() {
		return parent;
	}

	public void setParent(CurriculumElementRepositoryEntryViews parent) {
		this.parent = parent;
	}

	/**
	 * @return true if the user is member of this element or one of the descendant.
	 */
	@Override
	public boolean isCurriculumMember() {
		return curriculumMember;
	}

	public void setCurriculumMember(boolean curriculumMember) {
		this.curriculumMember = curriculumMember;
	}

	@Override
	public int hashCode() {
		return curriculumElement.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof CurriculumElementRepositoryEntryViews) {
			CurriculumElementRepositoryEntryViews el = (CurriculumElementRepositoryEntryViews)obj;
			return curriculumElement.equals(el.curriculumElement);
		}
		return super.equals(obj);
	}
}
