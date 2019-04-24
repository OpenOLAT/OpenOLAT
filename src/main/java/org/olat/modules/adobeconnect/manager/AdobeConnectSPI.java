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
package org.olat.modules.adobeconnect.manager;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.olat.basesecurity.Authentication;
import org.olat.core.id.Identity;
import org.olat.modules.adobeconnect.AdobeConnectMeeting;
import org.olat.modules.adobeconnect.model.AdobeConnectErrors;
import org.olat.modules.adobeconnect.model.AdobeConnectPrincipal;
import org.olat.modules.adobeconnect.model.AdobeConnectSco;
import org.olat.modules.adobeconnect.model.BreezeSession;

/**
 * 
 * Initial date: 17 avr. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */

public interface AdobeConnectSPI {
	
	public String getId();
	
	public String getName();
	
	public AdobeConnectSco createScoMeeting(String name, String description, String folderScoId,
			String templateId, Date startDate, Date endDate, Locale locale, AdobeConnectErrors error);
	
	public boolean updateScoMeeting(String scoId, String name, String description,
			String templateId, Date startDate, Date endDate, AdobeConnectErrors error);
	
	public AdobeConnectSco getScoMeeting(AdobeConnectMeeting meeting, AdobeConnectErrors error);
	
	public boolean deleteScoMeeting(AdobeConnectMeeting meeting, AdobeConnectErrors error);
	
	public AdobeConnectSco createFolder(String name, AdobeConnectErrors errors);
	
	/**
	 * Search folders under the root folder of the administration user. The
	 * method doesn't search the sub-tree. The name is an exact match.
	 * 
	 * @param name The name of the folder
	 * @param errors Errors if something bad happens
	 * @return A list of folders
	 */
	public List<AdobeConnectSco> getFolderByName(String name, AdobeConnectErrors errors);
	
	public List<AdobeConnectSco> getTemplates();
	
	public List<AdobeConnectSco> getRecordings(AdobeConnectMeeting meeting, AdobeConnectErrors error);
	
	public boolean setPermissions(String scoId, boolean allAccess, AdobeConnectErrors error);

	public boolean isMember(String scoId, String principalId, String permission, AdobeConnectErrors error);
	
	public boolean setMember(String scoId, String principalId, String permission, AdobeConnectErrors error);
	
	public AdobeConnectPrincipal getPrincipalByLogin(String login, AdobeConnectErrors error);
	
	public AdobeConnectPrincipal createPrincipal(Identity identity, String login, String password, AdobeConnectErrors error);
	
	public AdobeConnectPrincipal adminCommonInfo(AdobeConnectErrors error);
	
	public BreezeSession commonInfo(Authentication authentication, AdobeConnectErrors error);
	

}
