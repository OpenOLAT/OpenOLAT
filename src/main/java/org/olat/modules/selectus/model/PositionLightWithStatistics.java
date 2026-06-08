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
package org.olat.modules.selectus.model;

import java.util.Date;
import java.util.Locale;

import org.olat.core.id.Organisation;

/**
 * 
 * Initial date: 20.08.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PositionLightWithStatistics implements PositionLight {
	
	private final PositionLight position;
	
	private Integer numOfApplications;
	private Integer numOfMaleApplications;
	private Integer numOfFemaleApplications;
	
	private Object[] additionalAttributesValues;
	
	private final String url;
	
	public PositionLightWithStatistics(PositionLight position,
			Integer numOfApplications, Integer numOfMaleApplications, Integer numOfFemaleApplications,
			Object[] additionalAttributesValues, String url) {
		this.url = url;
		this.position = position;
		this.numOfApplications = numOfApplications;
		this.numOfMaleApplications = numOfMaleApplications;
		this.numOfFemaleApplications = numOfFemaleApplications;
		this.additionalAttributesValues = additionalAttributesValues;
	}
	
	public String getUrl() {
		return url;
	}
	
	public Integer getNumOfApplications() {
		return numOfApplications;
	}

	public Integer getNumOfMaleApplications() {
		return numOfMaleApplications;
	}

	public Integer getNumOfFemaleApplications() {
		return numOfFemaleApplications;
	}

	@Override
	public String getPositionTitle(Locale locale) {
		return position.getPositionTitle(locale);
	}

	@Override
	public Long getKey() {
		return position.getKey();
	}

	@Override
	public Date getCreationDate() {
		return position.getCreationDate();
	}

	@Override
	public String getDepartment(Locale locale) {
		return position.getDepartment(locale);
	}

	@Override
	public String getPlaningsNumber() {
		return position.getPlaningsNumber();
	}

	@Override
	public String getDepartment() {
		return position.getDepartment();
	}

	@Override
	public String getDepartmentDe() {
		return position.getDepartmentDe();
	}

	@Override
	public String getMLDepartment(Locale locale) {
		return position.getMLDepartment(locale);
	}
	
	@Override
	public String getDepartmentFr() {
		return position.getDepartmentFr();
	}

	@Override
	public String getAvailableLanguages() {
		return position.getAvailableLanguages();
	}

	@Override
	public String getPositionTitle() {
		return position.getPositionTitle();
	}

	@Override
	public String getPositionTitleDe() {
		return position.getPositionTitleDe();
	}
	
	@Override
	public String getPositionTitleFr() {
		return position.getPositionTitleFr();
	}

	@Override
	public String getMLTitle(Locale locale) {
		return position.getMLTitle(locale);
	}

	@Override
	public Date getApplicationDeadline() {
		return position.getApplicationDeadline();
	}

	@Override
	public String getProfessorship() {
		return position.getProfessorship();
	}

	@Override
	public String getStatus() {
		return position.getStatus();
	}

	@Override
	public boolean isValid() {
		return position.isValid();
	}

	@Override
	public String toStringFull() {
		return position.toStringFull();
	}

	@Override
	public String getResourceableTypeName() {
		return position.getResourceableTypeName();
	}

	@Override
	public Long getResourceableId() {
		return position.getResourceableId();
	}

	@Override
	public Organisation getOrganisation() {
		return position.getOrganisation();
	}
	
	public Object getAdditionalValue(int index) {
		Object val = null;
		if(additionalAttributesValues != null && index >= 0 && index < additionalAttributesValues.length) {
			val = additionalAttributesValues[index];
		}
		return val;
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof PositionLightWithStatistics) {
			PositionLightWithStatistics pos = (PositionLightWithStatistics)obj;
			return pos.equals(position);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return position.hashCode();
	}
}
