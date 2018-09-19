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

import org.olat.basesecurity.IdentityRef;
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
	private Collection<? extends IdentityRef> topicIdentityRefs;
	private List<? extends OrganisationRef> topicOrganisationRefs;
	private Collection<? extends CurriculumRef> topicCurriculumRefs;
	private List<? extends CurriculumElementRef> topicCurriculumElementRefs;
	private List<? extends RepositoryEntryRef> topicRepositoryRefs;
	private List<? extends OrganisationRef> contextOrganisationRefs;
	private Collection<? extends CurriculumRef> contextCurriculumRefs;
	private List<? extends CurriculumElementRef> contextCurriculumElementRefs;
	private boolean withUserInfosOnly;

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

	public Collection<? extends IdentityRef> getTopicIdentityRefs() {
		return topicIdentityRefs;
	}

	public void setTopicIdentityRefs(Collection<? extends IdentityRef> topicIdentityRefs) {
		this.topicIdentityRefs = topicIdentityRefs;
	}

	public List<? extends OrganisationRef> getTopicOrganisationRefs() {
		return topicOrganisationRefs;
	}

	public void setTopicOrganisationRefs(List<? extends OrganisationRef> topicOrganisationRefs) {
		this.topicOrganisationRefs = topicOrganisationRefs;
	}

	public Collection<? extends CurriculumRef> getTopicCurriculumRefs() {
		return topicCurriculumRefs;
	}

	public void setTopicCurriculumRefs(Collection<? extends CurriculumRef> topicCurriculumRefs) {
		this.topicCurriculumRefs = topicCurriculumRefs;
	}

	public List<? extends CurriculumElementRef> getTopicCurriculumElementRefs() {
		return topicCurriculumElementRefs;
	}

	public void setTopicCurriculumElementRefs(List<? extends CurriculumElementRef> topicCurriculumElementRefs) {
		this.topicCurriculumElementRefs = topicCurriculumElementRefs;
	}

	public List<? extends RepositoryEntryRef> getTopicRepositoryRefs() {
		return topicRepositoryRefs;
	}

	public void setTopicRepositoryRefs(List<? extends RepositoryEntryRef> topicRepositoryRefs) {
		this.topicRepositoryRefs = topicRepositoryRefs;
	}

	public List<? extends OrganisationRef> getContextOrganisationRefs() {
		return contextOrganisationRefs;
	}

	public void setContextOrganisationRefs(List<? extends OrganisationRef> contextOrganisationRefs) {
		this.contextOrganisationRefs = contextOrganisationRefs;
	}

	public Collection<? extends CurriculumRef> getContextCurriculumRefs() {
		return contextCurriculumRefs;
	}

	public void setContextCurriculumRefs(Collection<? extends CurriculumRef> contextCurriculumRefs) {
		this.contextCurriculumRefs = contextCurriculumRefs;
	}
	
	public List<? extends CurriculumElementRef> getContextCurriculumElementRefs() {
		return contextCurriculumElementRefs;
	}

	public void setContextCurriculumElementRefs(List<? extends CurriculumElementRef> contextCurriculumElementRefs) {
		this.contextCurriculumElementRefs = contextCurriculumElementRefs;
	}

	public boolean isWithUserInfosOnly() {
		return withUserInfosOnly;
	}

	public void setWithUserInfosOnly(boolean withUserInfosOnly) {
		this.withUserInfosOnly = withUserInfosOnly;
	}

	@Override
	public AnalysisSearchParameter clone() {
		AnalysisSearchParameter clone = new AnalysisSearchParameter();
		clone.formEntryRef = this.formEntryRef;
		clone.dateRangeFrom = this.dateRangeFrom;
		clone.dateRangeTo = this.dateRangeTo;
		clone.topicIdentityRefs = this.topicIdentityRefs != null
				? new ArrayList<>(this.topicIdentityRefs)
				: null;
		clone.topicOrganisationRefs = this.topicOrganisationRefs != null
				? new ArrayList<>(this.topicOrganisationRefs)
				: null;
		clone.topicCurriculumRefs = this.topicCurriculumRefs != null
				? new ArrayList<>(this.topicCurriculumRefs)
				: null;
		clone.topicCurriculumElementRefs = this.topicCurriculumElementRefs != null
				? new ArrayList<>(this.topicCurriculumElementRefs)
				: null;
		clone.topicRepositoryRefs = this.topicRepositoryRefs != null
				? new ArrayList<>(this.topicRepositoryRefs)
				: null;
		clone.contextOrganisationRefs = this.contextOrganisationRefs != null
				? new ArrayList<>(this.contextOrganisationRefs)
				: null;
		clone.contextCurriculumRefs = this.contextCurriculumRefs != null
				? new ArrayList<>(this.contextCurriculumRefs)
				: null;
		clone.contextCurriculumElementRefs = this.contextCurriculumElementRefs != null
				? new ArrayList<>(this.contextCurriculumElementRefs)
				: null;
		clone.withUserInfosOnly = this.withUserInfosOnly;
		return clone;
	}

}
