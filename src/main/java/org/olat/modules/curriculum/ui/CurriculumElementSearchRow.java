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

import java.util.Date;

import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementRef;
import org.olat.modules.curriculum.CurriculumElementType;

/**
 * 
 * Initial date: 10 Oct 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumElementSearchRow implements CurriculumElementRef {
	
	private final CurriculumElement element;
	private final CurriculumElementType elementType;
	
	private final FormLink toolsLink;
	private final FormLink resourcesLink;
	
	public CurriculumElementSearchRow(CurriculumElement element,
			FormLink resourcesLink, FormLink toolsLink) {
		this.element = element;
		elementType = element.getType();
		this.toolsLink = toolsLink;
		this.resourcesLink = resourcesLink;
	}
	
	@Override
	public Long getKey() {
		return element.getKey();
	}
	
	public Curriculum getCurriculum() {
		return element.getCurriculum();
	}
	
	public String getCurriculumDisplayName() {
		return element.getCurriculum().getDisplayName();
	}
	
	public String getDisplayName() {
		return element.getDisplayName();
	}
	
	public String getIdentifier() {
		return element.getIdentifier();
	}
	
	public String getExternalID() {
		return element.getExternalId();
	}
	
	public Date getBeginDate() {
		return element.getBeginDate();
	}
	
	public Date getEndDate() {
		return element.getEndDate();
	}
	
	public String getCurriculumElementTypeDisplayName() {
		return elementType == null ? null : elementType.getDisplayName();
	}
	
	public FormLink getToolsLink() {
		return toolsLink;
	}

	public FormLink getResourcesLink() {
		return resourcesLink;
	}

	@Override
	public int hashCode() {
		return element.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof CurriculumElementSearchRow) {
			CurriculumElementSearchRow row = (CurriculumElementSearchRow)obj;
			return element.equals(row.element);
		}
		return false;
	}

}
