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
package org.olat.modules.quality.analysis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.olat.core.id.OrganisationRef;
import org.olat.modules.curriculum.CurriculumElementRef;
import org.olat.modules.curriculum.CurriculumRef;
import org.olat.repository.RepositoryEntryRef;

/**
 * 
 * Initial date: 04.09.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class AnalysisSearchParameter {
	
	private RepositoryEntryRef formEntryRef;
	private Date dateRangeFrom;
	private Date dateRangeTo;
	private List<? extends OrganisationRef> organisationRefs;
	private Collection<? extends CurriculumRef> curriculumRefs;
	private List<? extends CurriculumElementRef> curriculumElementRefs;

	public RepositoryEntryRef getFormEntryRef() {
		return formEntryRef;
	}

	public void setFormEntryRef(RepositoryEntryRef formEntryRef) {
		this.formEntryRef = formEntryRef;
	}

	public Date getDateRangeFrom() {
		return dateRangeFrom;
	}

	public void setDateRangeFrom(Date dateRangeFrom) {
		this.dateRangeFrom = dateRangeFrom;
	}

	public Date getDateRangeTo() {
		return dateRangeTo;
	}

	public void setDateRangeTo(Date dateRangeTo) {
		this.dateRangeTo = dateRangeTo;
	}

	public List<? extends OrganisationRef> getOrganisationRefs() {
		return organisationRefs;
	}

	public void setOrganisationRefs(List<? extends OrganisationRef> organisationRefs) {
		this.organisationRefs = organisationRefs;
	}

	public Collection<? extends CurriculumRef> getCurriculumRefs() {
		return curriculumRefs;
	}

	public void setCurriculumRefs(Collection<? extends CurriculumRef> curriculumRefs) {
		this.curriculumRefs = curriculumRefs;
	}
	
	public List<? extends CurriculumElementRef> getCurriculumElementRefs() {
		return curriculumElementRefs;
	}

	public void setCurriculumElementRefs(List<? extends CurriculumElementRef> curriculumElementRefs) {
		this.curriculumElementRefs = curriculumElementRefs;
	}

	@Override
	public AnalysisSearchParameter clone() {
		AnalysisSearchParameter clone = new AnalysisSearchParameter();
		clone.formEntryRef = this.formEntryRef;
		clone.dateRangeFrom = this.dateRangeFrom;
		clone.organisationRefs = this.organisationRefs != null? new ArrayList<>(this.organisationRefs): null;
		clone.curriculumRefs = this.curriculumRefs != null? new ArrayList<>(this.curriculumRefs): null;
		clone.curriculumElementRefs = this.curriculumElementRefs != null? new ArrayList<>(this.curriculumElementRefs): null;
		return clone;
	}

}
