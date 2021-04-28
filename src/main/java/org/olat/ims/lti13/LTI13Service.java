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

import java.security.PublicKey;
import java.util.List;

import org.olat.basesecurity.GroupRoles;
import org.olat.core.id.Identity;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupRef;
import org.olat.ims.lti13.LTI13SharedToolService.ServiceType;
import org.olat.ims.lti13.model.LTI13PlatformWithInfos;
import org.olat.ims.lti13.model.json.LineItem;
import org.olat.ims.lti13.model.json.Result;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;

import com.github.scribejava.core.model.OAuth2AccessToken;

import io.jsonwebtoken.Claims;

/**
 * 
 * Initial date: 22 févr. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface LTI13Service {
	
	public static final String LTI_PROVIDER = "LTI";
	
	public static final String LTI_GROUP_TYPE = "lti";
	
	public LTI13Tool createExternalTool(String toolName, String toolUrl, String clientId, String initiateLoginUrl, LTI13ToolType type);

	public List<LTI13Tool> getTools(LTI13ToolType type);
	
	public LTI13Tool getToolByKey(Long key);
	
	public LTI13Tool getToolBy(String toolUrl, String clientId);
	
	public List<LTI13Tool> getToolsByClientId(String clientId);
	
	public String newClientId();
	
	public LTI13Tool updateTool(LTI13Tool tool);
	
	public void deleteToolsAndDeployments(RepositoryEntryRef ref, String subIdent);
	

	public LTI13ToolDeployment createToolDeployment(String targetUrl, LTI13Tool tool, RepositoryEntry entry, String subIdent);
	
	public LTI13ToolDeployment updateToolDeployment(LTI13ToolDeployment deployment);
	
	public LTI13ToolDeployment getToolDeployment(RepositoryEntryRef entry, String subIdent);
	
	public LTI13ToolDeployment getToolDeploymentByKey(Long key);
	
	public List<LTI13ToolDeployment> getToolDeployments(LTI13Tool tool);
	
	/**
	 * @return A non-persisted platform - tool with a pre filled client-id
	 */
	public LTI13Platform createTransientPlatform(LTI13PlatformScope type);
	
	public LTI13Platform updatePlatform(LTI13Platform tool);
	
	public LTI13Platform getPlatform(String issuer, String clientId);
	
	public LTI13Platform getPlatformByKey(Long key);
	
	public List<LTI13PlatformWithInfos> getPlatformWithInfos();
	
	public List<LTI13Platform> getPlatforms();
	
	/**
	 * 
	 * @param deploymentId The deployment ID
	 * @param platform  The platform
	 * @param repositoryEntry The repository entry
	 * @param businessGroup The group
	 * @return A deployment
	 */
	public LTI13SharedToolDeployment createSharedToolDeployment(String deploymentId, LTI13Platform platform,
			RepositoryEntry repositoryEntry, BusinessGroup businessGroup);
	
	public LTI13SharedToolDeployment updateSharedToolDeployment(LTI13SharedToolDeployment deployment);
	
	public LTI13SharedToolDeployment getSharedToolDeployment(String deploymentId, LTI13Platform platform);
	
	public void updateSharedToolServiceEndpoint(String contextId, ServiceType type, String endpointUrl, LTI13SharedToolDeployment deployment);
	
	public List<LTI13SharedToolDeployment> getSharedToolDeployments(LTI13Platform sharedTool);
	
	public List<LTI13SharedToolDeployment> getSharedToolDeployments(RepositoryEntryRef entry);
	
	public List<LTI13SharedToolDeployment> getSharedToolDeployments(BusinessGroupRef businessGroup);
	
	
	/**
	 * @return The last valid public/private key
	 */
	public LTI13Key getLastPlatformKey();
	
	public List<LTI13Key> getPlatformKeys();
	
	public PublicKey getPlatformPublicKey(String kid);
	
	public LTI13Key getKey(String jwkSetUri, String kid);
	
	// Identity management
	
	public Identity loadIdentity(String sub, String issuer);
	
	public String subIdentity(Identity identity, String issuer);
	
	public Identity matchIdentity(Claims claims, LTI13Platform platform);
	
	public void checkMembership(Identity identity, GroupRoles role, LTI13SharedToolDeployment deployment);
	
	
	public OAuth2AccessToken getAccessToken(LTI13Platform tool, List<String> scopes);
	
	// Assignment and grading
	
	/**
	 * The line item link to this deployment (course + course element) or null.
	 * 
	 * @param deployment The deployment
	 * @return A line item
	 */
	public LineItem getLineItem(LTI13ToolDeployment deployment);
	
	public Result getResult(String userId, Identity assessedId, LTI13ToolDeployment deployment);
	
	public List<Result> getResults(LTI13ToolDeployment deployment, int firstResult, int maxResults);
	
	
	
}
