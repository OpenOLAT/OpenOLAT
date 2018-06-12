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

import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTreeTableNode;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementRef;

/**
 * 
 * Initial date: 14 févr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumElementRow implements CurriculumElementRef, FlexiTreeTableNode {
	
	private boolean hasChildren;
	private CurriculumElementRow parent;
	private final Long parentKey;
	private final CurriculumElement element;
	
	private final FormLink toolsLink;
	
	public CurriculumElementRow(CurriculumElement element, FormLink toolsLink) {
		this.element = element;
		this.toolsLink = toolsLink;
		parentKey = element.getParent() == null ? null : element.getParent().getKey();
	}
	
	@Override
	public Long getKey() {
		return element.getKey();
	}
	
	public String getIdentifier() {
		return element.getIdentifier();
	}
	
	public String getDisplayName() {
		return element.getDisplayName();
	}
	
	public String getExternalId() {
		return element.getExternalId();
	}

	@Override
	public CurriculumElementRow getParent() {
		return parent;
	}
	
	public void setParent(CurriculumElementRow parent) {
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

	@Override
	public String getCrump() {
		return element.getDisplayName();
	}

	public FormLink getTools() {
		return toolsLink;
	}

}
