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

import java.util.Date;
import java.util.List;

import org.olat.core.id.Organisation;
import org.olat.modules.quality.generator.QualityGeneratorRef;

/**
 * Initial date: 20.08.2018<br>
 * @author  uhensler, urs.hensler@frentix.com, http://www.frentix.com
 */
public class SearchParameters {
	
	private QualityGeneratorRef generatorRef;
	private List<Organisation> organisations;
	private Long ceTypeKey;
	private Date from;
	private Date to;
	private boolean startDate;

	public SearchParameters(QualityGeneratorRef generatorRef, List<Organisation> organisations, Long ceTypeKey,
			Date from, Date to) {
		this.generatorRef = generatorRef;
		this.organisations = organisations;
		this.ceTypeKey = ceTypeKey;
		this.from = from;
		this.to = to;
	}

	public QualityGeneratorRef getGeneratorRef() {
		return generatorRef;
	}

	public void setGeneratorRef(QualityGeneratorRef generatorRef) {
		this.generatorRef = generatorRef;
	}

	public List<Organisation> getOrganisations() {
		return organisations;
	}

	public void setOrganisations(List<Organisation> organisations) {
		this.organisations = organisations;
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
}