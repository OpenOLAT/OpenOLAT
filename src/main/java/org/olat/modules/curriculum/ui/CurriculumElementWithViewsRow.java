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
package org.olat.modules.curriculum.ui;

import java.util.List;

import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTreeTableNode;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementMembership;
import org.olat.modules.curriculum.CurriculumElementRef;
import org.olat.repository.RepositoryEntryMyView;
import org.olat.repository.ui.PriceMethod;
import org.olat.repository.ui.RepositoyUIFactory;
import org.olat.resource.OLATResource;

/**
 * 
 * Initial date: 14 févr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumElementWithViewsRow implements CurriculumElementRef, FlexiTreeTableNode {
	
	private boolean hasChildren;
	private CurriculumElementWithViewsRow parent;
	
	
	private final Long parentKey;
	private final CurriculumElement element;
	private final CurriculumElementMembership curriculumMembership;
	private boolean curriculumMember;

	private boolean singleEntry;
	private OLATResource olatResource;
	private RepositoryEntryMyView repositoryEntry;
	
	private int access;
	private boolean isMembersOnly;
	private List<PriceMethod> accessTypes;

	private boolean member;
	
	private FormLink startLink;
	private FormLink detailsLink;
	
	public CurriculumElementWithViewsRow(CurriculumElement element, CurriculumElementMembership curriculumMembership) {
		this.element = element;
		this.curriculumMembership = curriculumMembership;
		curriculumMember = (curriculumMembership == null ? false : curriculumMembership.hasMembership());
		singleEntry = false;
		parentKey = element.getParent() == null ? null : element.getParent().getKey();
	}
	
	public CurriculumElementWithViewsRow(CurriculumElement element, CurriculumElementMembership curriculumMembership,
			RepositoryEntryMyView repositoryEntryView, boolean alone) {
		this.element = element;
		this.curriculumMembership = curriculumMembership;
		curriculumMember = (curriculumMembership == null ? false : curriculumMembership.hasMembership());
		singleEntry = alone;
		parentKey = element.getParent() == null ? null : element.getParent().getKey();
		
		isMembersOnly = repositoryEntryView.isMembersOnly();
		repositoryEntry = repositoryEntryView;
		olatResource = repositoryEntryView.getOlatResource();
		access = repositoryEntryView.getAccess();
	}
	
	@Override
	public Long getKey() {
		return element.getKey();
	}
	
	public boolean isCurriculumElementOnly() {
		return element != null && repositoryEntry == null;
	}
	
	public boolean isRepositoryEntryOnly() {
		return element != null && repositoryEntry != null && !singleEntry;
	}
	
	public boolean isCurriculumElementWithEntry() {
		return element != null && repositoryEntry != null && singleEntry;
	}
	
	public String getCurriculumElementIdentifier() {
		return repositoryEntry == null || singleEntry ? element.getIdentifier() : null;
	}
	
	public String getCurriculumElementDisplayName() {
		return repositoryEntry == null || singleEntry ? element.getDisplayName() : null;
	}
	
	public String getMaterializedPathKeys() {
		return element.getMaterializedPathKeys();
	}
	
	public boolean isSingleEntry() {
		return singleEntry;
	}
	
	public int getAccess() {
		return access;
	}
	
	public boolean isMembersOnly() {
		return isMembersOnly;
	}
	
	public Long getRepositoryEntryKey() {
		return repositoryEntry == null ? null : repositoryEntry.getKey();
	}
	
	public String getRepositoryEntryDisplayName() {
		return repositoryEntry == null ? null : repositoryEntry.getDisplayname();
	}
	
	public String getRepositoryEntryExternalRef() {
		return repositoryEntry == null ? null : repositoryEntry.getExternalRef();
	}
	
	public String getRepositoryEntryCssClass() {
		return olatResource == null ? "" : RepositoyUIFactory.getIconCssClass(olatResource.getResourceableTypeName());
	}
	
	public OLATResource getOlatResource() {
		return olatResource;
	}
	
	/**
	 * Is member if the row as some type of access control
	 * @return
	 */
	public boolean isMember() {
		return member;
	}
	
	public void setMember(boolean member) {
		this.member = member;
	}

	public CurriculumElementMembership getCurriculumMembership() {
		return curriculumMembership;
	}

	public boolean isCurriculumMember() {
		return curriculumMember;
	}

	public void setCurriculumMember(boolean curriculumMember) {
		this.curriculumMember = curriculumMember;
	}

	public List<PriceMethod> getAccessTypes() {
		return accessTypes;
	}

	public void setAccessTypes(List<PriceMethod> accessTypes) {
		this.accessTypes = accessTypes;
	}

	@Override
	public CurriculumElementWithViewsRow getParent() {
		return parent;
	}
	
	public void setParent(CurriculumElementWithViewsRow parent) {
		this.parent = parent;
		if(parent != null) {
			parent.hasChildren = true;
		}
	}
	
	public boolean hasChildren() {
		return hasChildren;
	}

	public Long getParentKey() {
		return parentKey;
	}
	
	public String getStartLinkName() {
		return startLink == null ? null : startLink.getComponent().getComponentName();
	}
	
	public FormLink getStartLink() {
		return startLink;
	}
	
	public void setStartLink(FormLink startLink) {
		this.startLink = startLink;
	}
	
	public String getDetailsLinkName() {
		return detailsLink == null ? null : detailsLink.getComponent().getComponentName();
	}

	public FormLink getDetailsLink() {
		return detailsLink;
	}

	public void setDetailsLink(FormLink detailsLink) {
		this.detailsLink = detailsLink;
	}

	@Override
	public String getCrump() {
		return element.getDisplayName();
	}

}
