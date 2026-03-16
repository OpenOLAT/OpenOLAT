/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.model;

import java.util.Date;
import java.util.Set;

import org.olat.core.id.CreateInfo;
import org.olat.core.id.ModifiedInfo;

/**
 * 
 * Initial date: 21 avr. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface ApplicationsFeedbackConfiguration extends CreateInfo, ModifiedInfo {
	
	Long getKey();
	
	boolean isEnabled();
	
	void setEnabled(boolean enabled);
	
	String getConfigurationName();
	
	Date getDeadline();

	void setDeadline(Date deadline);

	String getMailTemplate();

	void setMailTemplate(String mailTemplate);
	
	String getMailLetter();

	void setMailLetter(String configuration);

	Set<String> getDocuments();

	void setDocuments(Set<String> docs);
	
	Set<String> getFields();

	void setFields(Set<String> fields);
	
	boolean isRefereesDocs();

	void setRefereesDocs(boolean refereesDocs);
	
	boolean isExpertsDocs();

	void setExpertsDocs(boolean expertsDocs);
	
	boolean isExpertsComparativeAssessmentDocs();
	
	void setExpertsComparativeAssessmentDocs(boolean expertsDocs);

}
