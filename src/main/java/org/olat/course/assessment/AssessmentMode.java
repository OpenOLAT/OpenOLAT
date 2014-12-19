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
import java.util.Set;

import org.olat.core.id.CreateInfo;
import org.olat.core.id.ModifiedInfo;
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

	public Date getBegin();

	public void setBegin(Date begin);

	public Date getEnd();

	public void setEnd(Date end);

	public int getLeadTime();

	public void setLeadTime(int leadTime);

	public Target getTargetAudience();

	public void setTargetAudience(Target target);
	
	public Set<AssessmentModeToGroup> getGroups();
	
	public Set<AssessmentModeToArea> getAreas();

	public boolean isRestrictAccessElements();

	public void setRestrictAccessElements(boolean restrictAccessElements);

	public String getElementList();

	public void setElementList(String elementList);

	public boolean isRestrictAccessIps();

	public void setRestrictAccessIps(boolean restrictAccessIps);

	public String getIpList();

	public void setIpList(String ipList);

	public boolean isSafeExamBrowser();

	public void setSafeExamBrowser(boolean safeExamBrowser);

	public String getSafeExamBrowserKey();

	public void setSafeExamBrowserKey(String safeExamBrowserKey);

	public String getSafeExamBrowserHint();

	public void setSafeExamBrowserHint(String safeExamBrowserHint);

	public boolean isApplySettingsForCoach();

	public void setApplySettingsForCoach(boolean applySettingsForCoach);
	

	public enum Target {
		courseAndGroups,
		course,
		groups
		
	}
}
