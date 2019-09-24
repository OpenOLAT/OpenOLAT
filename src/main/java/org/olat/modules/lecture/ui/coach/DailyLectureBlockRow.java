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
package org.olat.modules.lecture.ui.coach;

import java.util.Date;

import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTreeTableNode;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureBlockRef;
import org.olat.modules.lecture.model.LectureBlockBlockStatistics;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 25 juil. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class DailyLectureBlockRow implements LectureBlockRef, FlexiTreeTableNode {
	
	private boolean iamTeacher;
	private Date separatorStartDate;
	private Date separatorEndDate;
	private LectureBlockBlockStatistics lectureBlockStatistics;
	
	private FormLink tools;
	private String separatorName;
	
	private int numOfChildren = 0;
	private DailyLectureBlockRow parent;
	
	public DailyLectureBlockRow(String separatorName, Date separatorStartDate, Date separatorEndDate) {
		this.separatorName = separatorName;
		this.separatorStartDate = separatorStartDate;
		this.separatorEndDate = separatorEndDate;
	}
	
	public DailyLectureBlockRow(String separatorName, DailyLectureBlockRow parent, Date separatorStartDate, Date separatorEndDate) {
		this.separatorName = separatorName;
		this.separatorStartDate = separatorStartDate;
		this.separatorEndDate = separatorEndDate;
		this.parent = parent;
	}
	
	public DailyLectureBlockRow(LectureBlockBlockStatistics lectureBlockStatistics, boolean iamTeacher) {
		this.lectureBlockStatistics = lectureBlockStatistics;
		this.iamTeacher = iamTeacher;
	}
	
	@Override
	public Long getKey() {
		return lectureBlockStatistics == null ? null : lectureBlockStatistics.getLectureBlock().getKey();
	}

	@Override
	public DailyLectureBlockRow getParent() {
		return parent;
	}
	
	public void setParent(DailyLectureBlockRow parent) {
		this.parent = parent;
	}

	@Override
	public String getCrump() {
		return null;
	}

	public boolean isSeparator() {
		return separatorName != null;
	}
	
	public int getNumOfChildren() {
		return numOfChildren;
	}
	
	public void incrementNumOfChildren() {
		numOfChildren++;
	}
	
	public boolean isIamTeacher() {
		return iamTeacher;
	}
	
	public LectureBlock getLectureBlock() {
		return lectureBlockStatistics == null ? null : lectureBlockStatistics.getLectureBlock();
	}
	
	public Date getLectureStartDate() {
		return lectureBlockStatistics.getLectureBlock().getStartDate();
	}
	
	public Date getLectureEndDate() {
		return lectureBlockStatistics.getLectureBlock().getEndDate();
	}
	
	public String getEntryDisplayname() {
		return lectureBlockStatistics.getEntry().getDisplayname();
	}
	
	public String getEntryExternalRef() {
		return lectureBlockStatistics.getEntry().getExternalRef();
	}
	
	public RepositoryEntry getEntry() {
		return lectureBlockStatistics.getEntry();
	}
	
	public long getNumOfParticipants() {
		return lectureBlockStatistics.getNumOfParticipants();
	}
	
	public long getNumOfPresences() {
		return lectureBlockStatistics.getNumOfPresents();
	}
	
	public long getNumOfAbsences() {
		return lectureBlockStatistics.getNumOfAbsents();
	}
	
	public LectureBlockBlockStatistics getLectureBlockBlockStatistics() {
		return lectureBlockStatistics;
	}

	public FormLink getTools() {
		return tools;
	}

	public void setTools(FormLink tools) {
		this.tools = tools;
	}

	public String getSeparatorName() {
		return separatorName;
	}

	public void setSeparatorName(String separatorName) {
		this.separatorName = separatorName;
	}

	public Date getSeparatorStartDate() {
		return separatorStartDate;
	}

	public void setSeparatorStartDate(Date separatorStartDate) {
		this.separatorStartDate = separatorStartDate;
	}

	public Date getSeparatorEndDate() {
		return separatorEndDate;
	}

	public void setSeparatorEndDate(Date separatorEndDate) {
		this.separatorEndDate = separatorEndDate;
	}

	@Override
	public int hashCode() {
		if(isSeparator()) {
			return separatorName.hashCode();
		}
		return getKey().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == this) {
			return true;
		}
		if(obj instanceof DailyLectureBlockRow) {
			DailyLectureBlockRow row = (DailyLectureBlockRow)obj;
			if(isSeparator() && row.isSeparator()) {
				return separatorName.equals(row.separatorName);
			} else if(!isSeparator() && !row.isSeparator()) {
				return getKey().equals(row.getKey());
			}
		}
		return false;
	}
}
