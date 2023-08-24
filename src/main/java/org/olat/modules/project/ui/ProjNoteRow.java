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

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.id.Identity;
import org.olat.modules.project.ProjNoteInfo;
import org.olat.modules.project.ProjNoteRef;

/**
 * 
 * Initial date: 16 Dez 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ProjNoteRow implements ProjNoteRef {
	
	private final Long key;
	private final Date creationDate;
	private final Date contentModifiedDate;
	private final Identity contentModifiedBy;
	private String contentModifiedByName;
	private final Date deletedDate;
	private final Identity deletedBy;
	private String deletedByName;
	private final Set<Identity> members;
	private Set<Long> memberKeys;
	private String displayName;
	private String text;
	private String modified;
	private Set<Long> tagKeys;
	private String formattedTags;
	private Component userPortraits;
	private FormLink selectLink;
	private FormLink toolsLink;
	
	public ProjNoteRow(ProjNoteInfo info) {
		this.key = info.getNote().getKey();
		this.creationDate = info.getNote().getCreationDate();
		this.contentModifiedDate = info.getNote().getArtefact().getContentModifiedDate();
		this.contentModifiedBy = info.getNote().getArtefact().getContentModifiedBy();
		this.deletedDate = info.getNote().getArtefact().getDeletedDate();
		this.deletedBy = info.getNote().getArtefact().getDeletedBy();
		this.members = info.getMembers();
	}

	@Override
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

	public Set<Identity> getMembers() {
		return members;
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

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
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

	public String getUserPortraitsName() {
		return userPortraits != null? userPortraits.getComponentName(): null;
	}

	public Component getUserPortraits() {
		return userPortraits;
	}

	public void setUserPortraits(Component userPortraits) {
		this.userPortraits = userPortraits;
	}

	public String getSelectLinkName() {
		return selectLink != null? selectLink.getComponent().getComponentName(): null;
	}

	public FormLink getSelectLink() {
		return selectLink;
	}

	public void setSelectLink(FormLink selectLink) {
		this.selectLink = selectLink;
	}
	
	public String getToolsLinkName() {
		return toolsLink != null? toolsLink.getComponent().getComponentName(): null;
	}

	public FormLink getToolsLink() {
		return toolsLink;
	}

	public void setToolsLink(FormLink toolsLink) {
		this.toolsLink = toolsLink;
	}
	
}
