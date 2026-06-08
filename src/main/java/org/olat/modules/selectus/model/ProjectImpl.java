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

import java.io.Serializable;
import java.util.Date;

import org.olat.core.util.StringHelper;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 * 
 * Initial date: 10 janv. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Embeddable
public class ProjectImpl implements Project, Serializable {
	
	private static final long serialVersionUID = 254185392207090679L;
	
	@Column(name="projecttitle", nullable=true, insertable=true, updatable=true)
	private String title;
	@Column(name="projectfinancialimpact", nullable=true, insertable=true, updatable=true)
	private String financialImpact1;
	@Column(name="projectfinancialimpact_2", nullable=true, insertable=true, updatable=true)
	private String financialImpact2;
	@Column(name="projectfinancialimpact_3", nullable=true, insertable=true, updatable=true)
	private String financialImpact3;
	@Column(name="projectfinancialimpact_4", nullable=true, insertable=true, updatable=true)
	private String financialImpact4;
	@Column(name="projectfinancialimpact_5", nullable=true, insertable=true, updatable=true)
	private String financialImpact5;
	@Column(name="projectstartdate", nullable=true, insertable=true, updatable=true)
	private Date startDate;
	@Column(name="projectduration", nullable=true, insertable=true, updatable=true)
	private String duration;
	@Column(name="projectdescription", nullable=true, insertable=true, updatable=true)
	private String description;
	@Column(name="acronym", nullable=true, insertable=true, updatable=true)
	private String acronym;
	@Column(name="keywords", nullable=true, insertable=true, updatable=true)
	private String keywords;
	@Column(name="disciplines", nullable=true, insertable=true, updatable=true)
	private String disciplines;

	@Override
	public boolean hasData() {
		return StringHelper.containsNonWhitespace(getTitle())
				|| StringHelper.containsNonWhitespace(getFinancialImpact1())
				|| StringHelper.containsNonWhitespace(getFinancialImpact2())
				|| StringHelper.containsNonWhitespace(getFinancialImpact3())
				|| StringHelper.containsNonWhitespace(getFinancialImpact4())
				|| StringHelper.containsNonWhitespace(getFinancialImpact5())
				|| startDate != null
				|| StringHelper.containsNonWhitespace(getDuration())
				|| StringHelper.containsNonWhitespace(getDescription())
				|| StringHelper.containsNonWhitespace(getAcronym())
				|| StringHelper.containsNonWhitespace(getKeywords())
				|| StringHelper.containsNonWhitespace(getDisciplines());
		
	}
	
	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public void setTitle(String title) {
		this.title = title;
	}

	@Override
	public String getFinancialImpact1() {
		return financialImpact1;
	}

	@Override
	public void setFinancialImpact1(String financialImpact1) {
		this.financialImpact1 = financialImpact1;
	}

	@Override
	public String getFinancialImpact2() {
		return financialImpact2;
	}

	@Override
	public void setFinancialImpact2(String financialImpact2) {
		this.financialImpact2 = financialImpact2;
	}

	@Override
	public String getFinancialImpact3() {
		return financialImpact3;
	}

	@Override
	public void setFinancialImpact3(String financialImpact3) {
		this.financialImpact3 = financialImpact3;
	}

	@Override
	public String getFinancialImpact4() {
		return financialImpact4;
	}

	@Override
	public void setFinancialImpact4(String financialImpact4) {
		this.financialImpact4 = financialImpact4;
	}

	@Override
	public String getFinancialImpact5() {
		return financialImpact5;
	}

	@Override
	public void setFinancialImpact5(String financialImpact5) {
		this.financialImpact5 = financialImpact5;
	}

	@Override
	public Date getStartDate() {
		return startDate;
	}

	@Override
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	@Override
	public String getDuration() {
		return duration;
	}

	@Override
	public void setDuration(String duration) {
		this.duration = duration;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String getAcronym() {
		return acronym;
	}

	@Override
	public void setAcronym(String acronym) {
		this.acronym = acronym;
	}

	@Override
	public String getKeywords() {
		return keywords;
	}

	@Override
	public void setKeywords(String keywords) {
		this.keywords = keywords;
	}

	@Override
	public String getDisciplines() {
		return disciplines;
	}

	@Override
	public void setDisciplines(String discipines) {
		this.disciplines = discipines;
	}
}
