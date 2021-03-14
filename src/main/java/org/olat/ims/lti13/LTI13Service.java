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

import org.olat.core.id.Identity;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupRef;
import org.olat.ims.lti13.LTI13SharedToolService.ServiceType;
import org.olat.ims.lti13.model.LTI13SharedToolWithInfos;
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
	 * @param entry The course or the learn resource
	 * @return A non-persisted shared tool with a pre filled client-id
	 */
	public LTI13SharedTool createTransientSharedTool(RepositoryEntry entry);
	
	public LTI13SharedTool createTransientSharedTool(BusinessGroup businessGroup);
	
	public LTI13SharedTool updateSharedTool(LTI13SharedTool tool);
	
	public List<LTI13SharedToolWithInfos> getSharedToolsWithInfos(RepositoryEntryRef entry);
	
	public List<LTI13SharedToolWithInfos> getSharedToolsWithInfos(BusinessGroupRef businessGroup);
	
	public LTI13SharedTool getSharedTool(String issuer, String clientId);
	
	public LTI13SharedTool getSharedToolByKey(Long key);
	
	public LTI13SharedToolDeployment getOrCreateSharedToolDeployment(String deploymentId, LTI13SharedTool sharedTool);
	
	public void updateSharedToolServiceEndpoint(String contextId, ServiceType type, String endpointUrl, LTI13SharedToolDeployment deployment);
	
	public List<LTI13SharedToolDeployment> getSharedToolDeployments(LTI13SharedTool sharedTool);
	
	/**
	 * @return The last valid public/private key
	 */
	public LTI13Key getLastPlatformKey();
	
	public List<LTI13Key> getPlatformKeys();
	
	public LTI13Key getKey(String jwkSetUri, String kid);
	
	// Identity management
	
	public Identity loadIdentity(String sub, String issuer);
	
	public String subIdentity(Identity identity, String issuer);
	
	public Identity matchIdentity(Claims claims);
	
	public void checkMembership(Identity identity, LTI13SharedTool tool);
	
	
	public OAuth2AccessToken getAccessToken(LTI13SharedTool tool, List<String> scopes);
	
}
