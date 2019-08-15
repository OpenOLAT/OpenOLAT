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
package org.olat.modules.lecture.ui.coach;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.id.Identity;
import org.olat.modules.lecture.AbsenceNotice;
import org.olat.modules.lecture.AbsenceNoticeRef;
import org.olat.modules.lecture.LectureBlock;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 26 juil. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AbsenceNoticeRow implements AbsenceNoticeRef {
	
	private AbsenceNotice absenceNotice;
	private Identity absentIdentity;
	private final List<Identity> teachers = new ArrayList<>();
	private final List<LectureBlock> lectureBlocks = new ArrayList<>();
	private final List<RepositoryEntry> entriesList = new ArrayList<>();
	
	private FormLink typeLink;
	private FormLink toolsLink;
	private FormLink detailsLink;
	private FormLink entriesLink;
	
	public AbsenceNoticeRow(AbsenceNotice absenceNotice, Identity absentIdentity) {
		this.absenceNotice = absenceNotice;
		this.absentIdentity = absentIdentity;
	}
	
	public Identity getAbsentIdentity() {
		return absentIdentity;
	}
	
	public Long getIdentityKey() {
		return absentIdentity.getKey();
	}

	public String getIdentityName() {
		return absentIdentity.getName();
	}
	
	public AbsenceNotice getAbsenceNotice() {
		return absenceNotice;
	}
	
	@Override
	public Long getKey() {
		return absenceNotice.getKey();
	}
	
	public Date getStartDate() {
		return absenceNotice.getStartDate();
	}
	
	public Date getEndDate() {
		return absenceNotice.getEndDate();
	}

	public FormLink getTypeLink() {
		return typeLink;
	}

	public void setTypeLink(FormLink type) {
		this.typeLink = type;
	}

	public FormLink getToolsLink() {
		return toolsLink;
	}

	public void setToolsLink(FormLink tools) {
		this.toolsLink = tools;
	}

	public FormLink getDetailsLink() {
		return detailsLink;
	}

	public void setDetailsLink(FormLink details) {
		this.detailsLink = details;
	}
	
	public FormLink getEntriesLink() {
		return entriesLink;
	}

	public void setEntriesLink(FormLink entriesLink) {
		this.entriesLink = entriesLink;
	}

	public List<RepositoryEntry> getEntriesList() {
		return entriesList;
	}
	
	public void addEntries(Collection<RepositoryEntry> entries) {
		entriesList.addAll(entries);
	}
	
	public List<LectureBlock> getLectureBlocks() {
		return lectureBlocks;
	}
	
	public void addLectureBlock(LectureBlock lectureBlock) {
		lectureBlocks.add(lectureBlock);
	}

	public List<Identity> getTeachers() {
		return teachers;
	}
	
	public void addTeachers(List<Identity> identities) {
		if(identities != null) {
			for(Identity identity:identities) {
				if(!teachers.contains(identity)) {
					teachers.add(identity);
				}
			}
		}
	}
}
