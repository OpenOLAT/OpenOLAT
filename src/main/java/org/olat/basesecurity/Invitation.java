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

package org.olat.basesecurity;

import java.util.List;

import org.olat.core.id.CreateInfo;
import org.olat.core.id.Identity;
import org.olat.modules.invitation.InvitationAdditionalInfos;
import org.olat.modules.invitation.InvitationTypeEnum;

/**
 * Description:<br>
 * Invitation to Olat.
 * 
 * <P>
 * Initial Date:  10 nov. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public interface Invitation extends CreateInfo {
	
	public Long getKey();

	public String getToken();
	
	public String getFirstName();
	
	public void setFirstName(String firstName);
	
	public String getLastName();
	
	public void setLastName(String lastName);
	
	public String getMail();
	
	public void setMail(String mail);
	
	public InvitationAdditionalInfos getAdditionalInfos();
	
	public void setAdditionalInfos(InvitationAdditionalInfos infos);
	
	public List<String> getRoleList();
	
	public void setRoleList(List<String> roles);
	
	public boolean isRegistration();

	public void setRegistration(boolean registration);
	
	public InvitationTypeEnum getType();
	
	public Group getBaseGroup();
	
	public Identity getIdentity();
}
