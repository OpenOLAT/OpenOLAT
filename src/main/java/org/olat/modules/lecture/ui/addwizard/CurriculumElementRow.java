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

import java.util.Date;

import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.site.ComparableCurriculumElementRow;

/**
 * 
 * Initial date: 8 oct. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumElementRow implements ComparableCurriculumElementRow {
	
	private CurriculumElementRow parent;
	private final Long parentElementKey;
	private final CurriculumElement curriculumElement;
	
	public CurriculumElementRow(CurriculumElement curriculumElement) {
		this.curriculumElement = curriculumElement;
		parentElementKey = curriculumElement.getParent() == null ? null : curriculumElement.getParent().getKey();
	}

	@Override
	public CurriculumElementRow getParent() {
		return parent;
	}
	
	public void setParent(CurriculumElementRow parent) {
		this.parent = parent;
	}

	@Override
	public String getCrump() {
		return getDisplayName();
	}

	@Override
	public Long getKey() {
		return curriculumElement.getKey();
	}
	
	public CurriculumElement getCurriculumElement() {
		return curriculumElement;
	}
	
	@Override
	public Integer getPos() {
		return curriculumElement.getPos();
	}

	@Override
	public Integer getPosCurriculum() {
		return curriculumElement.getPosCurriculum();
	}

	@Override
	public Long getParentKey() {
		return parentElementKey;
	}

	@Override
	public String getDisplayName() {
		return curriculumElement.getDisplayName();
	}

	@Override
	public String getIdentifier() {
		return curriculumElement.getIdentifier();
	}

	@Override
	public Date getBeginDate() {
		return curriculumElement.getBeginDate();
	}
	
	public Date getEndDate() {
		return curriculumElement.getEndDate();
	}
	
	@Override
	public int hashCode() {
		return curriculumElement.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof CurriculumElementRow row) {
			return curriculumElement != null && curriculumElement.equals(row.curriculumElement);
		}
		return false;
	}
}