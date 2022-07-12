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
package org.olat.ims.lti13;

import java.util.List;

import org.olat.core.id.CreateInfo;
import org.olat.core.id.ModifiedInfo;
import org.olat.group.BusinessGroup;
import org.olat.ims.lti.LTIDisplayOptions;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 22 févr. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface LTI13ToolDeployment extends CreateInfo, ModifiedInfo {
	
	public Long getKey();
	
	/**
	 * Claim: https://purl.imsglobal.org/spec/lti/claim/deployment_id
	 * 
	 * @return
	 */
	public String getDeploymentId();
	
	public String getContextId();
	
	public String getTargetUrl();
	
	public void setTargetUrl(String url);
	
	public List<String> getSendUserAttributesList();
	
	public void setSendUserAttributesList(List<String> attributes);
	
	public String getSendCustomAttributes();
	
	public void setSendCustomAttributes(String configuration);
	
	// roles

	public String getAuthorRoles();
	
	public void setAuthorRoles(String roles);
	
	public List<String> getAuthorRolesList();
	
	public void setAuthorRolesList(List<String> roles);

	public String getCoachRoles();
	
	public void setCoachRoles(String roles);
	
	public List<String> getCoachRolesList();
	
	public void setCoachRolesList(List<String> roles);

	public String getParticipantRoles();
	
	public void setParticipantRoles(String roles);
	
	public List<String> getParticipantRolesList();
	
	public void setParticipantRolesList(List<String> roles);
	
	public boolean isAssessable();

	public void setAssessable(boolean assessable);
	
	public boolean isNameAndRolesProvisioningServicesEnabled();

	public void setNameAndRolesProvisioningServicesEnabled(boolean nameAndRolesProvisioningServices);
	
	// display options
	
	public String getDisplay();
	
	public void setDisplay(String display);
	
	public LTIDisplayOptions getDisplayOptions();
	
	public void setDisplayOptions(LTIDisplayOptions option);
	
	public String getDisplayHeight();

	public void setDisplayHeight(String height);

	public String getDisplayWidth();

	public void setDisplayWidth(String width);
	
	public boolean isSkipLaunchPage();

	public void setSkipLaunchPage(boolean skipLaunchPage);
	
	public LTI13Tool getTool();
	
	public RepositoryEntry getEntry();
	
	public String getSubIdent();
	
	public BusinessGroup getBusinessGroup();

}
