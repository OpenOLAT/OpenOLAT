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
package org.olat.modules.lecture.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.olat.core.id.Identity;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.modules.lecture.AbsenceCategory;
import org.olat.modules.lecture.AbsenceNotice;
import org.olat.modules.lecture.AbsenceNoticeTarget;
import org.olat.modules.lecture.AbsenceNoticeType;
import org.olat.modules.lecture.LectureBlock;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 25 juil. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class EditAbsenceNoticeWrapper {
	
	private Identity identity;
	private Date endDate;
	private Date startDate;
	private Date currentDate;
	private String absenceReason;
	private Boolean authorized;
	private AbsenceNotice absenceNotice;
	private AbsenceCategory absenceCategory;
	private AbsenceNoticeType absenceNoticeType;
	private AbsenceNoticeTarget absenceNoticeTarget;
	private final List<AbsenceNoticeType> allowedTypeList = new ArrayList<>();
	
	private String contactSubject;
	private String contactBody;
	private List<Identity> identitiesToContact;
	
	private List<RepositoryEntry> entries;
	private List<LectureBlock> lectureBlocks;
	
	private List<LectureBlock> predefinedLectureBlocks;

	private VFSContainer tempUploadFolder;
	private VFSContainer documentContainer;
	private List<VFSItem> attachmentsToDelete = new ArrayList<>();
	
	private EditAbsenceNoticeWrapper(AbsenceNotice notice) {
		wrap(notice);
	}
	
	public void wrap(AbsenceNotice notice) {
		absenceNotice = notice;
		identity = notice.getIdentity();
		startDate = notice.getStartDate();
		endDate = notice.getEndDate();
		absenceReason = notice.getAbsenceReason();
		authorized = notice.getAbsenceAuthorized();
		absenceCategory = notice.getAbsenceCategory();
		absenceNoticeType = notice.getNoticeType();
		absenceNoticeTarget = notice.getNoticeTarget();
	}
	
	public EditAbsenceNoticeWrapper(AbsenceNoticeType... allowedTypes) {
		if(allowedTypes != null) {
			for(AbsenceNoticeType allowedType:allowedTypes) {
				if(allowedType != null) {
					absenceNoticeType = allowedType;
					allowedTypeList.add(allowedType);
				}
			}
		}
	}
	
	public static EditAbsenceNoticeWrapper valueOf(AbsenceNotice notice) {
		return new EditAbsenceNoticeWrapper(notice);
	}

	public AbsenceNotice getAbsenceNotice() {
		return absenceNotice;
	}

	public AbsenceNoticeType getAbsenceNoticeType() {
		return absenceNoticeType;
	}

	public void setAbsenceNoticeType(AbsenceNoticeType absenceNoticeType) {
		this.absenceNoticeType = absenceNoticeType;
	}

	public AbsenceNoticeTarget getAbsenceNoticeTarget() {
		return absenceNoticeTarget;
	}

	public void setAbsenceNoticeTarget(AbsenceNoticeTarget absenceNoticeTarget) {
		this.absenceNoticeTarget = absenceNoticeTarget;
	}

	public String getAbsenceReason() {
		return absenceReason;
	}

	public void setAbsenceReason(String absenceReason) {
		this.absenceReason = absenceReason;
	}

	public AbsenceCategory getAbsenceCategory() {
		return absenceCategory;
	}

	public void setAbsenceCategory(AbsenceCategory absenceCategory) {
		this.absenceCategory = absenceCategory;
	}

	public Boolean getAuthorized() {
		return authorized;
	}

	public void setAuthorized(Boolean authorized) {
		this.authorized = authorized;
	}

	public Identity getIdentity() {
		return identity;
	}

	public void setIdentity(Identity identity) {
		this.identity = identity;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public List<RepositoryEntry> getEntries() {
		return entries;
	}

	public void setEntries(List<RepositoryEntry> entries) {
		this.entries = entries;
	}

	public List<LectureBlock> getLectureBlocks() {
		return lectureBlocks;
	}

	public void setLectureBlocks(List<LectureBlock> lectureBlocks) {
		this.lectureBlocks = lectureBlocks;
	}

	public Date getCurrentDate() {
		return currentDate;
	}

	public void setCurrentDate(Date currentDate) {
		this.currentDate = currentDate;
	}

	public List<LectureBlock> getPredefinedLectureBlocks() {
		return predefinedLectureBlocks;
	}

	public void setPredefinedLectureBlocks(List<LectureBlock> predefinedLectureBlocks) {
		this.predefinedLectureBlocks = predefinedLectureBlocks;
	}

	public String getContactSubject() {
		return contactSubject;
	}

	public void setContactSubject(String contactSubject) {
		this.contactSubject = contactSubject;
	}

	public String getContactBody() {
		return contactBody;
	}

	public void setContactBody(String contactBody) {
		this.contactBody = contactBody;
	}

	public List<Identity> getIdentitiesToContact() {
		return identitiesToContact;
	}

	public void setIdentitiesToContact(List<Identity> identitiesToContact) {
		this.identitiesToContact = identitiesToContact;
	}

	public VFSContainer getTempUploadFolder() {
		return tempUploadFolder;
	}

	public void setTempUploadFolder(VFSContainer tempUploadFolder) {
		this.tempUploadFolder = tempUploadFolder;
	}

	public VFSContainer getDocumentContainer() {
		return documentContainer;
	}

	public void setDocumentContainer(VFSContainer documentContainer) {
		this.documentContainer = documentContainer;
	}
	
	public List<VFSItem> getAttachmentsToDelete() {
		return attachmentsToDelete;
	}

	public void commitChanges(AbsenceNotice notice) {
		notice.setStartDate(startDate);
		notice.setEndDate(endDate);
		notice.setAbsenceCategory(absenceCategory);
		notice.setAbsenceReason(absenceReason);
		notice.setNoticeType(absenceNoticeType);
		notice.setAbsenceAuthorized(authorized);
	}
}
