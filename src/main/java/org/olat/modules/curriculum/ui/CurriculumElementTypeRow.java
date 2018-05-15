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
	
	public CurriculumElementType getType() {
		return type;
	}

	public FormLink getToolsLink() {
		return toolsLink;
	}
	
	public void setToolsLink(FormLink toolsLink) {
		this.toolsLink = toolsLink;
	}
}
