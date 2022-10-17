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
package org.olat.restapi.support.vo.elements;

import java.util.Date;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * 
 * Initial date: 19 mai 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "testReportConfigVO")
public class TestReportConfigVO {

	private Boolean showScoreInfo;
	private String summaryPresentation;
	private Boolean showResultsAfterFinish;
	private Boolean showResultsOnHomepage;
	private String showResultsDependendOnDate;
	private Date showResultsStartDate;
	private Date showResultsEndDate;
	private Date showResultsFailedStartDate;
	private Date showResultsFailedEndDate;
	private Date showResultsPassedStartDate;
	private Date showResultsPassedEndDate;

	public TestReportConfigVO() {
		//make JAXB happy
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

	public String getSummaryPresentation() {
		return summaryPresentation;
	}

	public void setSummaryPresentation(String summaryPresentation) {
		this.summaryPresentation = summaryPresentation;
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
