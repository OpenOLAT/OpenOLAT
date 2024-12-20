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
package org.olat.modules.lecture.ui.addwizard;

import java.util.List;

import org.olat.core.id.Identity;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.lecture.LectureBlock;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 8 oct. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AddLectureContext {

	private RepositoryEntry entry;
	private List<Identity> teachers;
	private LectureBlock lectureBlock;
	private CurriculumElement curriculumElement;
	
	private final Curriculum curriculum;
	private final CurriculumElement rootElement;
	
	public AddLectureContext(Curriculum curriculum, CurriculumElement rootElement) {
		this.curriculum = curriculum;
		this.rootElement = rootElement;
	}
	
	public Curriculum getCurriculum() {
		return curriculum;
	}

	public CurriculumElement getRootElement() {
		return rootElement;
	}

	public RepositoryEntry getEntry() {
		return entry;
	}

	public void setEntry(RepositoryEntry entry) {
		this.entry = entry;
	}

	public CurriculumElement getCurriculumElement() {
		return curriculumElement;
	}

	public void setCurriculumElement(CurriculumElement curriculumElement) {
		this.curriculumElement = curriculumElement;
	}

	public List<Identity> getTeachers() {
		return teachers;
	}

	public void setTeachers(List<Identity> teachers) {
		this.teachers = teachers;
	}

	public LectureBlock getLectureBlock() {
		return lectureBlock;
	}

	public void setLectureBlock(LectureBlock lectureBlock) {
		this.lectureBlock = lectureBlock;
	}
}
