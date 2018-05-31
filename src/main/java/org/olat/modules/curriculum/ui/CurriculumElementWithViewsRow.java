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
import org.olat.modules.curriculum.CurriculumElementRef;
import org.olat.repository.RepositoryEntryMyView;
import org.olat.repository.ui.PriceMethod;
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

	private boolean singleEntry;
	private Long repositoryEntryKey;
	private String repositoryEntryName;
	private OLATResource olatResource;
	
	private int access;
	private boolean isMembersOnly;
	private List<PriceMethod> accessTypes;

	private boolean member;
	
	private FormLink startLink;
	
	public CurriculumElementWithViewsRow(CurriculumElement element) {
		this.element = element;
		singleEntry = false;
		parentKey = element.getParent() == null ? null : element.getParent().getKey();
	}
	
	public CurriculumElementWithViewsRow(CurriculumElement element, RepositoryEntryMyView repositoryEntryView, boolean alone) {
		this.element = element;
		singleEntry = alone;
		parentKey = element.getParent() == null ? null : element.getParent().getKey();
		
		isMembersOnly = repositoryEntryView.isMembersOnly();
		repositoryEntryName = repositoryEntryView.getDisplayname();
		repositoryEntryKey = repositoryEntryView.getKey();
		olatResource = repositoryEntryView.getOlatResource();
		access = repositoryEntryView.getAccess();
	}
	
	@Override
	public Long getKey() {
		return element.getKey();
	}
	
	public String getCurriculumElementIdentifier() {
		return repositoryEntryName == null || singleEntry ? element.getIdentifier() : null;
	}
	
	public String getCurriculumElementDisplayName() {
		return repositoryEntryName == null || singleEntry ? element.getDisplayName() : null;
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
		return repositoryEntryKey;
	}
	
	public String getRepositoryEntryName() {
		return repositoryEntryName;
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
	
	public FormLink getStartLink() {
		return startLink;
	}
	
	public void setStartLink(FormLink startLink) {
		this.startLink = startLink;
	}

	@Override
	public String getCrump() {
		return element.getDisplayName();
	}

}
