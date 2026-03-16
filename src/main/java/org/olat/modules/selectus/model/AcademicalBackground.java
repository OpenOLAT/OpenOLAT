/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.model;

import java.util.Date;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  22 jul. 2010 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public interface AcademicalBackground {
	
	public String getHighestDegreeType();

	public void setHighestDegreeType(String highestDegreeType);
	
	public String getHighestDegreeDescription();

	public void setHighestDegreeDescription(String description);

	public Date getHighestDegreeDate();

	public void setHighestDegreeDate(Date highestDegreeDate);

	public String getHighestDegreeInstitution();

	public void setHighestDegreeInstitution(String highestDegreeInstitution);
	
	public String getWorkedInAcademiaSince();

	public void setWorkedInAcademiaSince(String workedInAcademiaSince);

	public String getWorkedOutAcademiaSince();

	public void setWorkedOutAcademiaSince(String workedOutAcademiaSince);

	public String getWorkedOutAcademiaCareSince();

	public void setWorkedOutAcademiaCareSince(String workedOutAcademiaCareSince);

	public String getCareerDescription();

	public void setCareerDescription(String careerDescription);
	
	
	public String getHabilitationTitle();

	public void setHabilitationTitle(String habilitationTitle);

	public Date getHabilitationDate();

	public void setHabilitationDate(Date habilitationDate);

	public String getHabilitationInstitution();

	public void setHabilitationInstitution(String habilitationInstitution);
	
	public String getOrcid();

	public void setOrcid(String orcid);
	
	
	public String getDissertationTitle();

	public void setDissertationTitle(String dissertationTitle);

	public Date getDissertationDate();

	public void setDissertationDate(Date dissertationDate);

	public String getDissertationInstitution();

	public void setDissertationInstitution(String dissertationInstitution);
	
	public String getDissertationKeyword1();
	
	public void setDissertationKeyword1(String keyword);
	
	public String getDissertationKeyword2();

	public void setDissertationKeyword2(String keyword);

	public String getDissertationKeyword3();

	public void setDissertationKeyword3(String keyword);
	
	
	public Integer getNumberOfOriginalPublications();

	public void setNumberOfOriginalPublications(Integer numberOfOriginalPublications);

	public Integer getNumberOfFirstAuthorships();

	public void setNumberOfFirstAuthorships(Integer numberOfFirstAuthorships);

	public Integer getNumberOfLastAuthorships();

	public void setNumberOfLastAuthorships(Integer numberOfLastAuthorships);

	public Integer getCitations();

	public void setCitations(Integer citations);

	public Double getImpactFactor();

	public void setImpactFactor(Double impactFactor);

	public Double getHFactor();

	public void setHFactor(Double hFactor);
}
