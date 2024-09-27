/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.lecture.ui;

import org.olat.basesecurity.Group;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.group.model.StatisticsBusinessGroupRow;
import org.olat.modules.curriculum.model.CurriculumElementInfos;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 27 sept. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LectureBlockParticipantGroupRow {
	
	private final long numOfParticipants;
	private boolean excluded;
	
	private final Group group;
	private RepositoryEntry entry;
	private CurriculumElementInfos curriculumElement;
	private StatisticsBusinessGroupRow businessGroup;
	
	private FormLink toolsLink;
	private FormLink titleLink;
	
	public LectureBlockParticipantGroupRow(RepositoryEntry entry, Group group, long numOfParticipants, boolean excluded) {
		this.numOfParticipants = numOfParticipants;
		this.excluded = excluded;
		this.group = group;
		this.entry = entry;
	}

	public LectureBlockParticipantGroupRow(StatisticsBusinessGroupRow businessGroup, boolean excluded) {
		this.numOfParticipants = businessGroup.getNumOfParticipants();
		this.group = businessGroup.getBaseGroup();
		this.businessGroup = businessGroup;
		this.excluded = excluded;
	}
	
	public LectureBlockParticipantGroupRow(CurriculumElementInfos curriculumElement, boolean excluded) {
		this.numOfParticipants = curriculumElement.getNumOfParticipants();
		this.group = curriculumElement.getCurriculumElement().getGroup();
		this.curriculumElement = curriculumElement;
		this.excluded = excluded;
	}
	
	public String getTitle() {
		if(entry != null) {
			return entry.getDisplayname();
		}
		if(businessGroup != null) {
			return businessGroup.getName();
		}
		if(curriculumElement != null) {
			return curriculumElement.getCurriculumElement().getDisplayName();
		}
		return null;
	}
	
	public String getExternalRef() {
		if(entry != null) {
			return entry.getExternalRef();
		}
		if(curriculumElement != null) {
			return curriculumElement.getCurriculumElement().getIdentifier();
		}
		return null;
	}
	
	public Group getGroup() {
		return group;
	}
	
	public String getIconCssClass() {
		if(entry != null) {
			return "o_icon o_icon-fw o_CourseModule_icon";
		}
		if(businessGroup != null) {
			return "o_icon o_icon-fw o_icon_group";
		}
		if(curriculumElement != null) {
			return "o_icon o_icon-fw o_icon_curriculum";
		}
		return null;
	}
	
	public RepositoryEntry getEntry() {
		return entry;
	}
	
	public CurriculumElementInfos getCurriculumElement() {
		return curriculumElement;
	}
	
	public StatisticsBusinessGroupRow getBusinessGroup() {
		return businessGroup;
	}
	
	public long getNumOfParticipants() {
		return numOfParticipants;
	}

	public boolean isExcluded() {
		return excluded;
	}
	
	public void setExcluded(boolean excluded) {
		this.excluded = excluded;
	}

	public FormLink getTitleLink() {
		return titleLink;
	}

	public void setTitleLink(FormLink titleLink) {
		this.titleLink = titleLink;
	}

	public FormLink getToolsLink() {
		return toolsLink;
	}

	public void setToolsLink(FormLink toolsLink) {
		this.toolsLink = toolsLink;
	}
}
