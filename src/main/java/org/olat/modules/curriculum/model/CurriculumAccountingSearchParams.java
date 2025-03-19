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
package org.olat.modules.curriculum.model;

import java.util.Date;
import java.util.List;

import org.olat.core.id.Identity;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumRef;

/**
 * 
 * Initial date: 12 févr. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumAccountingSearchParams {
	
	private Identity identity;
	private Curriculum curriculum;
	private CurriculumElement curriculumElement;
	private Date fromDate;
	private Date toDate;
	private List<CurriculumRef> curriculums;
	private boolean excludeDeletedCurriculumElements;

	public Identity getIdentity() {
		return identity;
	}
	
	public void setIdentity(Identity identity) {
		this.identity = identity;
	}
	
	public Curriculum getCurriculum() {
		return curriculum;
	}
	
	public void setCurriculum(Curriculum curriculum) {
		this.curriculum = curriculum;
	}
	
	public CurriculumElement getCurriculumElement() {
		return curriculumElement;
	}
	
	public void setCurriculumElement(CurriculumElement curriculumElement) {
		this.curriculumElement = curriculumElement;
	}

	public void setFromDate(Date fromDate) {
		this.fromDate = fromDate;
	}

	public Date getFromDate() {
		return fromDate;
	}

	public void setToDate(Date toDate) {
		this.toDate = toDate;
	}

	public Date getToDate() {
		return toDate;
	}

	public void setCurriculums(List<CurriculumRef> curriculums) {
		this.curriculums = curriculums;
	}

	public List<CurriculumRef> getCurriculums() {
		return curriculums;
	}

	public boolean isExcludeDeletedCurriculumElements() {
		return excludeDeletedCurriculumElements;
	}

	public void setExcludeDeletedCurriculumElements(boolean excludeDeletedCurriculumElements) {
		this.excludeDeletedCurriculumElements = excludeDeletedCurriculumElements;
	}
}
