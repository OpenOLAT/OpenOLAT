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
package org.olat.repository.ui.admin;

import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.repository.RepositoryEntryEducationalType;

/**
 * 
 * Initial date: 18 Jan 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class EducationalTypeRow {
	
	private final RepositoryEntryEducationalType educationalType;
	private String translation;
	private Long numberOfCourse;
	private MultipleSelectionElement presetMyCoursesEl;

	public EducationalTypeRow(RepositoryEntryEducationalType educationalType) {
		this.educationalType = educationalType;
	}

	public RepositoryEntryEducationalType getEducationalType() {
		return educationalType;
	}

	public String getTranslation() {
		return translation;
	}

	public void setTranslation(String translation) {
		this.translation = translation;
	}

	public Long getNumberOfCourse() {
		return numberOfCourse;
	}

	public void setNumberOfCourse(Long numberOfCourse) {
		this.numberOfCourse = numberOfCourse;
	}

	public MultipleSelectionElement getPresetMyCoursesEl() {
		return presetMyCoursesEl;
	}

	public void setPresetMyCoursesEl(MultipleSelectionElement presetMyCoursesEl) {
		this.presetMyCoursesEl = presetMyCoursesEl;
	}
}
