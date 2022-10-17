/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/
package org.olat.restapi.support.vo.elements;

import java.util.Date;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * 
 * Description:<br>
 * test course node configuration
 * 
 * <P>
 * Initial Date:  27.07.2010 <br>
 * @author skoeber
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "testConfigVO")
public class TestConfigVO {
	
	private Boolean showNavigation;
	private Boolean allowNavigation;
	private Boolean showSectionsOnly;
	private String sequencePresentation;
	private Boolean allowCancel;
	private Boolean allowSuspend;
	private Boolean showQuestionTitle;
	private String summeryPresentation;
	private Integer numAttempts;
	private Boolean showQuestionProgress;
	private Boolean showScoreProgress;
	private Boolean showScoreInfo;
	private Boolean showResultsAfterFinish;
	private Boolean showResultsOnHomepage;
	private String showResultsDependendOnDate;
	private Date showResultsStartDate;
	private Date showResultsEndDate;
	private Date showResultsFailedStartDate;
	private Date showResultsFailedEndDate;
	private Date showResultsPassedStartDate;
	private Date showResultsPassedEndDate;

	public TestConfigVO() {
		//make JAXB happy
	}

	public Boolean getShowNavigation() {
		return showNavigation;
	}

	public void setShowNavigation(Boolean showNavigation) {
		this.showNavigation = showNavigation;
	}

	public Boolean getAllowNavigation() {
		return allowNavigation;
	}

	public void setAllowNavigation(Boolean allowNavigation) {
		this.allowNavigation = allowNavigation;
	}

	public Boolean getShowSectionsOnly() {
		return showSectionsOnly;
	}

	public void setShowSectionsOnly(Boolean showSectionsOnly) {
		this.showSectionsOnly = showSectionsOnly;
	}

	public String getSequencePresentation() {
		return sequencePresentation;
	}

	public void setSequencePresentation(String sequencePresentation) {
		this.sequencePresentation = sequencePresentation;
	}

	public Boolean getAllowCancel() {
		return allowCancel;
	}

	public void setAllowCancel(Boolean allowCancel) {
		this.allowCancel = allowCancel;
	}

	public Boolean getAllowSuspend() {
		return allowSuspend;
	}

	public void setAllowSuspend(Boolean allowSuspend) {
		this.allowSuspend = allowSuspend;
	}

	public Boolean getShowQuestionTitle() {
		return showQuestionTitle;
	}

	public void setShowQuestionTitle(Boolean showQuestionTitle) {
		this.showQuestionTitle = showQuestionTitle;
	}

	public String getSummeryPresentation() {
		return summeryPresentation;
	}

	public void setSummeryPresentation(String summeryPresentation) {
		this.summeryPresentation = summeryPresentation;
	}

	public Integer getNumAttempts() {
		return numAttempts;
	}

	public void setNumAttempts(Integer numAttempts) {
		this.numAttempts = numAttempts;
	}
	
	public Boolean getShowQuestionProgress() {
		return showQuestionProgress;
	}

	public void setShowQuestionProgress(Boolean showQuestionProgress) {
		this.showQuestionProgress = showQuestionProgress;
	}

	public Boolean getShowScoreProgress() {
		return showScoreProgress;
	}

	public void setShowScoreProgress(Boolean showScoreProgress) {
		this.showScoreProgress = showScoreProgress;
	}

	public Boolean getShowScoreInfo() {
		return showScoreInfo;
	}

	public void setShowScoreInfo(Boolean showScoreInfo) {
		this.showScoreInfo = showScoreInfo;
	}

	public Boolean getShowResultsAfterFinish() {
		return showResultsAfterFinish;
	}

	public void setShowResultsAfterFinish(Boolean showResultsAfterFinish) {
		this.showResultsAfterFinish = showResultsAfterFinish;
	}

	public Boolean getShowResultsOnHomepage() {
		return showResultsOnHomepage;
	}

	public void setShowResultsOnHomepage(Boolean showResultsOnHomepage) {
		this.showResultsOnHomepage = showResultsOnHomepage;
	}

	public String getShowResultsDependendOnDate() {
		return showResultsDependendOnDate;
	}

	public void setShowResultsDependendOnDate(String showResultsDependendOnDate) {
		this.showResultsDependendOnDate = showResultsDependendOnDate;
	}

	public Date getShowResultsStartDate() {
		return showResultsStartDate;
	}

	public void setShowResultsStartDate(Date showResultsStartDate) {
		this.showResultsStartDate = showResultsStartDate;
	}

	public Date getShowResultsEndDate() {
		return showResultsEndDate;
	}

	public void setShowResultsEndDate(Date showResultsEndDate) {
		this.showResultsEndDate = showResultsEndDate;
	}

	public Date getShowResultsFailedStartDate() {
		return showResultsFailedStartDate;
	}

	public void setShowResultsFailedStartDate(Date showResultsFailedStartDate) {
		this.showResultsFailedStartDate = showResultsFailedStartDate;
	}

	public Date getShowResultsFailedEndDate() {
		return showResultsFailedEndDate;
	}

	public void setShowResultsFailedEndDate(Date showResultsFailedEndDate) {
		this.showResultsFailedEndDate = showResultsFailedEndDate;
	}

	public Date getShowResultsPassedStartDate() {
		return showResultsPassedStartDate;
	}

	public void setShowResultsPassedStartDate(Date showResultsPassedStartDate) {
		this.showResultsPassedStartDate = showResultsPassedStartDate;
	}

	public Date getShowResultsPassedEndDate() {
		return showResultsPassedEndDate;
	}

	public void setShowResultsPassedEndDate(Date showResultsPassedEndDate) {
		this.showResultsPassedEndDate = showResultsPassedEndDate;
	}
}
