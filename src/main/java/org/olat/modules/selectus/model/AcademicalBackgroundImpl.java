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

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  22 jul. 2010 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
@Embeddable
public class AcademicalBackgroundImpl implements AcademicalBackground, Serializable {

	private static final long serialVersionUID = -4865559252546958230L;
	
	@Column(name="highestdegreetype", nullable=true, insertable=true, updatable=true)
	private String highestDegreeType;
	@Column(name="highestdegreedescr", nullable=true, insertable=true, updatable=true)
	private String highestDegreeDescription;
	@Temporal(TemporalType.DATE)
	@Column(name="highestdegreedate", nullable=true, insertable=true, updatable=true)
	private Date highestDegreeDate;
	@Column(name="highestdegreeinstitution", nullable=true, insertable=true, updatable=true)
	private String highestDegreeInstitution;
	
	@Column(name="highestdegreeworkedsince", nullable=true, insertable=true, updatable=true)
	private String workedInAcademiaSince;
	@Column(name="workedoutacademia", nullable=true, insertable=true, updatable=true)
	private String workedOutAcademiaSince;
	@Column(name="workedoutacademiacare", nullable=true, insertable=true, updatable=true)
	private String workedOutAcademiaCareSince;
	@Column(name="careerdescription", nullable=true, insertable=true, updatable=true)
	private String careerDescription;
	
	@Column(name="dissertationtitle", nullable=true, insertable=true, updatable=true)
	private String dissertationTitle;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="dissertationdate", nullable=true, insertable=true, updatable=true)
	private Date dissertationDate;
	@Column(name="dissertationinstitution", nullable=true, insertable=true, updatable=true)
	private String dissertationInstitution;
	@Column(name="dissertationkeyword1", nullable=true, insertable=true, updatable=true)
	private String dissertationKeyword1;
	@Column(name="dissertationkeyword2", nullable=true, insertable=true, updatable=true)
	private String dissertationKeyword2;
	@Column(name="dissertationkeyword3", nullable=true, insertable=true, updatable=true)
	private String dissertationKeyword3;
	
	@Column(name="habilitationtitle", nullable=true, insertable=true, updatable=true)
	private String habilitationTitle;
	@Temporal(TemporalType.DATE)
	@Column(name="habilitationdate", nullable=true, insertable=true, updatable=true)
	private Date habilitationDate;
	@Column(name="habilitationinstitution", nullable=true, insertable=true, updatable=true)
	private String habilitationInstitution;
	
	@Column(name="orcid", nullable=true, insertable=true, updatable=true)
	private String orcid;

	@Column(name="originalpublications", nullable=true, insertable=true, updatable=true)
	private Integer numberOfOriginalPublications;
	@Column(name="firstauthorships", nullable=true, insertable=true, updatable=true)
	private Integer numberOfFirstAuthorships;
	@Column(name="lastauthorships", nullable=true, insertable=true, updatable=true)
	private Integer numberOfLastAuthorships;
	@Column(name="citations", nullable=true, insertable=true, updatable=true)
	private Integer citations;
	@Column(name="impactfactor", nullable=true, insertable=true, updatable=true)
	private Double impactFactor;
	@Column(name="hfactor", nullable=true, insertable=true, updatable=true)
	private Double hFactor;
	
	public AcademicalBackgroundImpl() {
		//
	}

	@Override
	public String getHighestDegreeType() {
		return highestDegreeType;
	}

	@Override
	public void setHighestDegreeType(String highestDegreeType) {
		this.highestDegreeType = highestDegreeType;
	}

	@Override
	public String getHighestDegreeDescription() {
		return highestDegreeDescription;
	}

	@Override
	public void setHighestDegreeDescription(String highestDegreeDescription) {
		this.highestDegreeDescription = highestDegreeDescription;
	}

	@Override
	public Date getHighestDegreeDate() {
		return highestDegreeDate;
	}

	@Override
	public void setHighestDegreeDate(Date highestDegreeDate) {
		this.highestDegreeDate = highestDegreeDate;
	}

	@Override
	public String getHighestDegreeInstitution() {
		return highestDegreeInstitution;
	}

	@Override
	public void setHighestDegreeInstitution(String highestDegreeInstitution) {
		this.highestDegreeInstitution = highestDegreeInstitution;
	}

	@Override
	public String getWorkedInAcademiaSince() {
		return workedInAcademiaSince;
	}

	@Override
	public void setWorkedInAcademiaSince(String workedInAcademiaSince) {
		this.workedInAcademiaSince = workedInAcademiaSince;
	}

	@Override
	public String getWorkedOutAcademiaSince() {
		return workedOutAcademiaSince;
	}

	@Override
	public void setWorkedOutAcademiaSince(String workedOutAcademiaSince) {
		this.workedOutAcademiaSince = workedOutAcademiaSince;
	}

	@Override
	public String getWorkedOutAcademiaCareSince() {
		return workedOutAcademiaCareSince;
	}

	@Override
	public void setWorkedOutAcademiaCareSince(String workedOutAcademiaCareSince) {
		this.workedOutAcademiaCareSince = workedOutAcademiaCareSince;
	}

	@Override
	public String getCareerDescription() {
		return careerDescription;
	}

	@Override
	public void setCareerDescription(String careerDescription) {
		this.careerDescription = careerDescription;
	}

	@Override
	public String getHabilitationTitle() {
		return habilitationTitle;
	}

	@Override
	public void setHabilitationTitle(String habilitationTitle) {
		this.habilitationTitle = habilitationTitle;
	}

	@Override
	public Date getHabilitationDate() {
		return habilitationDate;
	}

	@Override
	public void setHabilitationDate(Date habilitationDate) {
		this.habilitationDate = habilitationDate;
	}

	@Override
	public String getHabilitationInstitution() {
		return habilitationInstitution;
	}

	@Override
	public void setHabilitationInstitution(String habilitationInstitution) {
		this.habilitationInstitution = habilitationInstitution;
	}

	@Override
	public String getOrcid() {
		return orcid;
	}

	@Override
	public void setOrcid(String orcid) {
		this.orcid = orcid;
	}

	@Override
	public String getDissertationTitle() {
		return dissertationTitle;
	}

	@Override
	public void setDissertationTitle(String dissertationTitle) {
		this.dissertationTitle = dissertationTitle;
	}

	@Override
	public Date getDissertationDate() {
		return dissertationDate;
	}

	@Override
	public void setDissertationDate(Date dissertationDate) {
		this.dissertationDate = dissertationDate;
	}

	@Override
	public String getDissertationInstitution() {
		return dissertationInstitution;
	}

	@Override
	public void setDissertationInstitution(String dissertationInstitution) {
		this.dissertationInstitution = dissertationInstitution;
	}

	@Override
	public String getDissertationKeyword1() {
		return dissertationKeyword1;
	}

	@Override
	public void setDissertationKeyword1(String dissertationKeyword1) {
		this.dissertationKeyword1 = dissertationKeyword1;
	}

	@Override
	public String getDissertationKeyword2() {
		return dissertationKeyword2;
	}

	@Override
	public void setDissertationKeyword2(String dissertationKeyword2) {
		this.dissertationKeyword2 = dissertationKeyword2;
	}

	@Override
	public String getDissertationKeyword3() {
		return dissertationKeyword3;
	}

	@Override
	public void setDissertationKeyword3(String dissertationKeyword3) {
		this.dissertationKeyword3 = dissertationKeyword3;
	}

	@Override
	public Integer getNumberOfOriginalPublications() {
		return numberOfOriginalPublications;
	}

	@Override
	public void setNumberOfOriginalPublications(Integer numberOfOriginalPublications) {
		this.numberOfOriginalPublications = numberOfOriginalPublications;
	}

	@Override
	public Integer getNumberOfFirstAuthorships() {
		return numberOfFirstAuthorships;
	}

	@Override
	public void setNumberOfFirstAuthorships(Integer numberOfFirstAuthorships) {
		this.numberOfFirstAuthorships = numberOfFirstAuthorships;
	}

	@Override
	public Integer getNumberOfLastAuthorships() {
		return numberOfLastAuthorships;
	}

	@Override
	public void setNumberOfLastAuthorships(Integer numberOfLastAuthorships) {
		this.numberOfLastAuthorships = numberOfLastAuthorships;
	}

	@Override
	public Integer getCitations() {
		return citations;
	}

	@Override
	public void setCitations(Integer citations) {
		this.citations = citations;
	}

	@Override
	public Double getImpactFactor() {
		return impactFactor;
	}

	@Override
	public void setImpactFactor(Double impactFactor) {
		this.impactFactor = impactFactor;
	}

	@Override
	public Double getHFactor() {
		return hFactor;
	}

	@Override
	public void setHFactor(Double hFactor) {
		this.hFactor = hFactor;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("background[")
			.append("degree=").append(highestDegreeType == null ? "null" : highestDegreeType).append(";")
			.append("date=").append(highestDegreeDate == null ? "null" : highestDegreeDate).append(";")
			.append("institution=").append(highestDegreeInstitution == null ? "null" : highestDegreeInstitution).append(";")
			.append("]");
		return sb.toString();
	}
}
