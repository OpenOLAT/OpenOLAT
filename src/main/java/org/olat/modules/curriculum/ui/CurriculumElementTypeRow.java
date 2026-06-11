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
import org.olat.modules.curriculum.CurriculumElementType;
import org.olat.modules.curriculum.CurriculumElementTypeRef;

/**
 *
 * Initial date: 11 mai 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumElementTypeRow implements CurriculumElementTypeRef {

	private FormLink toolsLink;
	private FormLink parentsLink;
	private List<CurriculumElementType> parentTypes;
	private int numParents;
	private FormLink childrenLink;
	private List<CurriculumElementType> childTypes;
	private int numChildren;
	private String forUseAsLabel;
	private String contentLabel;
	private FormLink usesLink;
	private int numUses;
	private final CurriculumElementType type;

	public CurriculumElementTypeRow(CurriculumElementType type) {
		this.type = type;
	}

	@Override
	public Long getKey() {
		return type.getKey();
	}

	public String getIdentifier() {
		return type.getIdentifier();
	}

	public String getDisplayName() {
		return type.getDisplayName();
	}

	public String getExternalId() {
		return type.getExternalId();
	}

	public String getForUseAsLabel() {
		return forUseAsLabel;
	}

	public void setForUseAsLabel(String forUseAsLabel) {
		this.forUseAsLabel = forUseAsLabel;
	}

	public FormLink getUsesLink() {
		return usesLink;
	}

	public void setUsesLink(FormLink usesLink) {
		this.usesLink = usesLink;
	}

	public int getNumUses() {
		return numUses;
	}

	public void setNumUses(int numUses) {
		this.numUses = numUses;
	}

	public String getContentLabel() {
		return contentLabel;
	}

	public void setContentLabel(String contentLabel) {
		this.contentLabel = contentLabel;
	}

	public CurriculumElementType getType() {
		return type;
	}

	public FormLink getToolsLink() {
		return toolsLink;
	}

	public void setToolsLink(FormLink toolsLink) {
		this.toolsLink = toolsLink;
	}

	public FormLink getParentsLink() {
		return parentsLink;
	}

	public void setParentsLink(FormLink parentsLink) {
		this.parentsLink = parentsLink;
	}

	public int getNumParents() {
		return numParents;
	}

	public void setNumParents(int numParents) {
		this.numParents = numParents;
	}

	public List<CurriculumElementType> getParentTypes() {
		return parentTypes;
	}

	public void setParentTypes(List<CurriculumElementType> parentTypes) {
		this.parentTypes = parentTypes;
	}

	public FormLink getChildrenLink() {
		return childrenLink;
	}

	public void setChildrenLink(FormLink childrenLink) {
		this.childrenLink = childrenLink;
	}

	public int getNumChildren() {
		return numChildren;
	}

	public void setNumChildren(int numChildren) {
		this.numChildren = numChildren;
	}

	public List<CurriculumElementType> getChildTypes() {
		return childTypes;
	}

	public void setChildTypes(List<CurriculumElementType> childTypes) {
		this.childTypes = childTypes;
	}
}
