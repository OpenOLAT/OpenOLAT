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
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumRef;
import org.olat.modules.curriculum.model.CurriculumInfos;

/**
 * 
 * Initial date: 13 févr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumRow implements CurriculumRef {
	
	private final Curriculum curriculum;
	private final long numOfElements;
	private final boolean canManage;
	private final boolean active;
	
	private final FormLink toolsLink;
	
	public CurriculumRow(Curriculum curriculum, boolean active) {
		this.curriculum = curriculum;
		this.active = active;
		numOfElements = -1l;
		toolsLink = null;
		canManage = false;
	}
	
	public CurriculumRow(CurriculumInfos infos) {
		this.curriculum = infos.getCurriculum();
		numOfElements = infos.getNumOfElements();
		toolsLink = null;
		canManage = false;
		active = false;
	}
	
	public CurriculumRow(CurriculumInfos infos, FormLink toolsLink, boolean canManage) {
		curriculum = infos.getCurriculum();
		numOfElements = infos.getNumOfElements();
		this.toolsLink = toolsLink;
		this.canManage = canManage;
		active = false;
	}
	
	@Override
	public Long getKey() {
		return curriculum.getKey();
	}
	
	public String getDisplayName() {
		return curriculum.getDisplayName();
	}
	
	public String getIdentifier() {
		return curriculum.getIdentifier();
	}
	
	public String getExternalId() {
		return curriculum.getExternalId();
	}
	
	public String getOrganisation() {
		if(curriculum.getOrganisation() != null) {
			return curriculum.getOrganisation().getDisplayName();
		}
		return null;
	}
	
	public long getNumOfElements() {
		return numOfElements;
	}
	
	public FormLink getTools() {
		return toolsLink;
	}
	
	public boolean canManage() {
		return canManage;
	}
	
	/**
	 * The value must be explicitly loaded.
	 * 
	 * @return True if active
	 */
	public boolean isActive() {
		return active;
	}
	
	public boolean isLecturesEnabled() {
		return curriculum.isLecturesEnabled();
	}

	@Override
	public int hashCode() {
		return curriculum.getKey().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof CurriculumRow) {
			CurriculumRow row = (CurriculumRow)obj;
			return curriculum.equals(row.curriculum);
		}
		return false;
	}
}
