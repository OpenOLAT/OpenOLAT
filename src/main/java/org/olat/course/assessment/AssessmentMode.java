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
package org.olat.course.assessment;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.olat.core.id.CreateInfo;
import org.olat.core.id.ModifiedInfo;
import org.olat.course.assessment.model.AssessmentModeManagedFlag;
import org.olat.course.assessment.model.SafeExamBrowserConfiguration;
import org.olat.modules.lecture.LectureBlock;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 12.12.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface AssessmentMode extends ModifiedInfo, CreateInfo {
	
	public Long getKey();
	
	public String getName();

	public void setName(String name);

	public String getDescription();

	public void setDescription(String description);
	
	public RepositoryEntry getRepositoryEntry();
	
	public LectureBlock getLectureBlock();
	
	public String getExternalId();
	
	public void setExternalId(String externalId);
	
	public AssessmentModeManagedFlag[] getManagedFlags();
	
	public String getManagedFlagsString();
	
	public void setManagedFlagsString(String managedFlagsString);
	
	public Status getStatus();

	public void setStatus(Status status);
	
	public EndStatus getEndStatus();
	
	public void setEndStatus(EndStatus status);
	
	public boolean isManualBeginEnd();
	
	public void setManualBeginEnd(boolean manualBeginEnd);

	public Date getBegin();
	
	public int getLeadTime();

	public void setLeadTime(int leadTime);

	public void setBegin(Date begin);
	
	public Date getBeginWithLeadTime();

	public Date getEnd();

	public void setEnd(Date end);

	public int getFollowupTime();

	public void setFollowupTime(int followupTime);

	public Date getEndWithFollowupTime();

	public void setEndWithFollowupTime(Date endWithFollowup);

	public Target getTargetAudience();

	public void setTargetAudience(Target target);
	
	public Set<AssessmentModeToGroup> getGroups();
	
	public Set<AssessmentModeToArea> getAreas();
	
	public Set<AssessmentModeToCurriculumElement> getCurriculumElements();

	public boolean isRestrictAccessElements();

	public void setRestrictAccessElements(boolean restrictAccessElements);

	public String getElementList();

	public void setElementList(String elementList);
	
	/**
	 * 
	 * @return A list of course elements identifiers or null if not defined
	 */
	public List<String> getElementAsList();
	
	public String getStartElement();

	public void setStartElement(String startElement);

	public boolean isRestrictAccessIps();

	public void setRestrictAccessIps(boolean restrictAccessIps);

	public String getIpList();

	public void setIpList(String ipList);

	public boolean isSafeExamBrowser();

	public void setSafeExamBrowser(boolean safeExamBrowser);

	public String getSafeExamBrowserKey();

	public void setSafeExamBrowserKey(String safeExamBrowserKey);
	
	public SafeExamBrowserConfiguration getSafeExamBrowserConfiguration();
	
	public void setSafeExamBrowserConfiguration(SafeExamBrowserConfiguration configuration);
	
	public String getSafeExamBrowserConfigPList();

	public String getSafeExamBrowserConfigPListKey();
	
	public boolean isSafeExamBrowserConfigDownload();

	public void setSafeExamBrowserConfigDownload(boolean safeExamBrowserConfigDownload);

	public String getSafeExamBrowserHint();

	public void setSafeExamBrowserHint(String safeExamBrowserHint);

	public boolean isApplySettingsForCoach();

	public void setApplySettingsForCoach(boolean applySettingsForCoach);
	

	public enum Target {
		courseAndGroups,//it's in fact course, groups and curriculum elements
		course,
		groups,
		curriculumEls
	}
	
	public enum Status {
		none("o_as_mode_none"),
		leadtime("o_as_mode_leadtime"),
		assessment("o_as_mode_assessment"),
		followup("o_as_mode_followup"),
		end("o_as_mode_closed");
		
		private final String cssClass;
		
		private Status(String cssClass) {
			this.cssClass = cssClass;
		}
		
		public String cssClass() {
			return cssClass;
		}
	}
	
	public enum EndStatus {
		all,
		withoutDisadvantage
	}
}
