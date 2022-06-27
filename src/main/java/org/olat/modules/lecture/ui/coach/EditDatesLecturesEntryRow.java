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

import java.util.List;

import org.olat.core.id.Identity;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.model.LectureBlockWithTeachers;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 7 avr. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class EditDatesLecturesEntryRow {
	
	private String cssClass;
	private final RepositoryEntry entry;
	private final LectureBlockWithTeachers lectureBlockWithTeachers;
	
	public EditDatesLecturesEntryRow(LectureBlockWithTeachers lectureBlockWithTeachers) {
		this.lectureBlockWithTeachers = lectureBlockWithTeachers;
		this.entry = lectureBlockWithTeachers.getLectureBlock().getEntry();
	}
	
	public Long getLectureBlockKey() {
		return lectureBlockWithTeachers.getLectureBlock().getKey();
	}

	public String getCssClass() {
		return cssClass;
	}

	public void setCssClass(String cssClass) {
		this.cssClass = cssClass;
	}

	public RepositoryEntry getEntry() {
		return entry;
	}

	public LectureBlock getLectureBlock() {
		return lectureBlockWithTeachers.getLectureBlock();
	}
	
	public List<Identity> getTeachers() {
		return lectureBlockWithTeachers.getTeachers();
	}

	@Override
	public int hashCode() {
		Long key = getLectureBlockKey();
		return key == null ? 762438 : key.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof EditDatesLecturesEntryRow) {
			EditDatesLecturesEntryRow row = (EditDatesLecturesEntryRow)obj;
			Long key = getLectureBlockKey();
			Long rowKey = row.getLectureBlockKey();
			return key != null && key.equals(rowKey);
		}
		return super.equals(obj);
	}
}
