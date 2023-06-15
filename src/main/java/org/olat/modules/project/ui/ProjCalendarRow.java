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
package org.olat.modules.project.ui;

import java.util.Date;
import java.util.Set;

import org.olat.commons.calendar.model.KalendarEvent;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.id.Identity;
import org.olat.modules.project.ProjAppointment;
import org.olat.modules.project.ProjMilestone;

/**
 * 
 * Initial date: 14 Jun 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ProjCalendarRow {
	
	private final String type;
	private String translatedType;
	private KalendarEvent kalendarEvent;
	private final Long key;
	private final Date creationDate;
	private final Date contentModifiedDate;
	private final Identity contentModifiedBy;
	private String contentModifiedByName;
	private final Date deletedDate;
	private final Identity deletedBy;
	private String deletedByName;
	private Set<Long> memberKeys;
	private String displayName;
	private Date startDate;
	private Date endDate;
	private String modified;
	private Set<Long> tagKeys;
	private String formattedTags;
	private Component userPortraits;
	private FormLink toolsLink;
	
	public ProjCalendarRow(ProjAppointment appointment, KalendarEvent event) {
		this.type = ProjAppointment.TYPE;
		this.key = appointment.getKey();
		this.creationDate = appointment.getCreationDate();
		this.contentModifiedDate = appointment.getArtefact().getContentModifiedDate();
		this.contentModifiedBy = appointment.getArtefact().getContentModifiedBy();
		this.deletedDate = appointment.getArtefact().getDeletedDate();
		this.deletedBy = appointment.getArtefact().getDeletedBy();
		this.kalendarEvent = event;
	}
	
	public ProjCalendarRow(ProjMilestone milestone) {
		this.type = ProjMilestone.TYPE;
		this.key = milestone.getKey();
		this.creationDate = milestone.getCreationDate();
		this.contentModifiedDate = milestone.getArtefact().getContentModifiedDate();
		this.contentModifiedBy = milestone.getArtefact().getContentModifiedBy();
		this.deletedDate = milestone.getArtefact().getDeletedDate();
		this.deletedBy = milestone.getArtefact().getDeletedBy();
	}

	public String getType() {
		return type;
	}

	public String getTranslatedType() {
		return translatedType;
	}

	public void setTranslatedType(String translatedType) {
		this.translatedType = translatedType;
	}

	public KalendarEvent getKalendarEvent() {
		return kalendarEvent;
	}

	public void setEvent(KalendarEvent event) {
		this.kalendarEvent = event;
	}

	public Long getKey() {
		return key;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public Date getContentModifiedDate() {
		return contentModifiedDate;
	}

	public Identity getContentModifiedBy() {
		return contentModifiedBy;
	}

	public String getContentModifiedByName() {
		return contentModifiedByName;
	}

	public void setContentModifiedByName(String contentModifiedByName) {
		this.contentModifiedByName = contentModifiedByName;
	}

	public String getDeletedByName() {
		return deletedByName;
	}

	public void setDeletedByName(String deletedByName) {
		this.deletedByName = deletedByName;
	}

	public Date getDeletedDate() {
		return deletedDate;
	}

	public Identity getDeletedBy() {
		return deletedBy;
	}

	public Set<Long> getMemberKeys() {
		return memberKeys;
	}

	public void setMemberKeys(Set<Long> memberKeys) {
		this.memberKeys = memberKeys;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public String getModified() {
		return modified;
	}

	public void setModified(String modified) {
		this.modified = modified;
	}
	
	public Set<Long> getTagKeys() {
		return tagKeys;
	}

	public void setTagKeys(Set<Long> tagKeys) {
		this.tagKeys = tagKeys;
	}

	public String getFormattedTags() {
		return formattedTags;
	}

	public void setFormattedTags(String formattedTags) {
		this.formattedTags = formattedTags;
	}

	public Component getUserPortraits() {
		return userPortraits;
	}

	public void setUserPortraits(Component userPortraits) {
		this.userPortraits = userPortraits;
	}

	public FormLink getToolsLink() {
		return toolsLink;
	}

	public void setToolsLink(FormLink toolsLink) {
		this.toolsLink = toolsLink;
	}

}
