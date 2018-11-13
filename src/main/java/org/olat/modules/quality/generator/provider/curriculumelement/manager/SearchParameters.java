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
package org.olat.modules.quality.generator.provider.curriculumelement.manager;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.olat.core.id.Organisation;
import org.olat.core.id.OrganisationRef;
import org.olat.modules.curriculum.CurriculumElementRef;
import org.olat.modules.quality.generator.QualityGeneratorRef;

/**
 * Initial date: 20.08.2018<br>
 * 
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 */
public class SearchParameters {

	private QualityGeneratorRef generatorRef;
	private List<? extends OrganisationRef> organisationRefs;
	private Long ceTypeKey;
	private Collection<? extends CurriculumElementRef> curriculumElementRefs;
	private Date from;
	private Date to;
	private boolean startDate;

	public QualityGeneratorRef getGeneratorRef() {
		return generatorRef;
	}

	public void setGeneratorRef(QualityGeneratorRef generatorRef) {
		this.generatorRef = generatorRef;
	}

	public List<? extends OrganisationRef> getOrganisationRefs() {
		return organisationRefs;
	}

	public void setOrganisationRefs(List<Organisation> organisations) {
		this.organisationRefs = organisations;
	}

	public Collection<? extends CurriculumElementRef> getCurriculumElementRefs() {
		return curriculumElementRefs;
	}

	public void setCurriculumElementRefs(Collection<? extends CurriculumElementRef> curriculumElementRefs) {
		this.curriculumElementRefs = curriculumElementRefs;
	}

	public Long getCeTypeKey() {
		return ceTypeKey;
	}

	public void setCeTypeKey(Long ceTypeKey) {
		this.ceTypeKey = ceTypeKey;
	}

	public Date getFrom() {
		return from;
	}

	public void setFrom(Date from) {
		this.from = from;
	}

	public Date getTo() {
		return to;
	}

	public void setTo(Date to) {
		this.to = to;
	}

	public boolean isStartDate() {
		return startDate;
	}

	public void setStartDate(boolean startDate) {
		this.startDate = startDate;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("SearchParameters [generatorRef=");
		builder.append(generatorRef);
		builder.append(", organisationRefs (keys)=[");
		builder.append(organisationRefs.stream().map(OrganisationRef::getKey).map(k -> k.toString())
				.collect(Collectors.joining(", ")));
		builder.append("]");
		builder.append(", ceTypeKey=");
		builder.append(ceTypeKey);
		builder.append(", curriculumElementRefs=");
		builder.append(curriculumElementRefs);
		builder.append(", from=");
		builder.append(from);
		builder.append(", to=");
		builder.append(to);
		builder.append(", startDate=");
		builder.append(startDate);
		builder.append("]");
		return builder.toString();
	}
}