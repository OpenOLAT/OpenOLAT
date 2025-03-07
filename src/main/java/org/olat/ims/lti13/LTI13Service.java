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
import java.util.Collection;
import java.util.List;

import org.olat.basesecurity.GroupRoles;
import org.olat.core.id.Identity;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupRef;
import org.olat.ims.lti13.LTI13SharedToolService.ServiceType;
import org.olat.ims.lti13.model.JwtToolBundle;
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
	
	public LTI13Tool createExternalTool(String toolName, String toolUrl, String clientId, String initiateLoginUrl, String redirectUrls, LTI13ToolType type);

	public List<LTI13Tool> getTools(LTI13ToolType type);
	
	public LTI13Tool getToolByKey(Long key);
	
	public LTI13Tool getToolBy(Collection<String> toolUrl, String clientId);
	
	public List<LTI13Tool> getToolsByClientId(String clientId);
	
	public String newClientId();
	
	public LTI13Tool updateTool(LTI13Tool tool);
	
	public void deleteToolsDeploymentsAndContexts(RepositoryEntryRef ref, String subIdent);
	
	public LTI13ToolDeployment createToolDeployment(String targetUrl, LTI13ToolDeploymentType type, String deploymentId, LTI13Tool tool);
	
	
	public LTI13ToolDeployment updateToolDeployment(LTI13ToolDeployment deployment);
	
	public LTI13ToolDeployment getToolDeploymentByKey(Long key);
	
	public LTI13ToolDeployment getToolDeploymentByDeploymentId(String deploymentId);
	
	public List<LTI13ToolDeployment> getToolDeploymentByTool(LTI13Tool tool);
	
	
	public LTI13Context createContext(String targetUrl, LTI13ToolDeployment deployment,
			RepositoryEntry entry, String subIdent, BusinessGroup businessGroup);
	
	public LTI13Context copyContext(LTI13Context context, RepositoryEntry entry, String subIdent, BusinessGroup businessGroup);

	public LTI13Context updateContext(LTI13Context deployment);

	public LTI13Context getContextByKey(Long key);
	
	public LTI13Context getContext(RepositoryEntryRef entry, String subIdent);

	public List<LTI13Context> getContexts(RepositoryEntryRef entry);

	public LTI13Context getContextByContextId(String contextId);

	public LTI13Context getContextBackwardCompatibility(String deploymentKey, RepositoryEntryRef entry, String subIdent);

	/**
	 * For deployment with a single context only.
	 * 
	 * @param key
	 * @return
	 */
	public LTI13Context getContextByToolDeploymentByKey(Long key);
	
	public List<LTI13Context> getContextsByTool(LTI13Tool tool);
	
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
	
	/**
	 * Reload the deployment with a lot of fetch.
	 * 
	 * @param deployment The deployment to reload
	 * @return The deployment or null if not found.
	 */
	public LTI13SharedToolDeployment getSharedToolDeployment(LTI13SharedToolDeployment deployment);

	public void deleteSharedToolDeployment(LTI13SharedToolDeployment deployment);
	
	public void updateSharedToolServiceEndpoint(String contextId, ServiceType type, String endpointUrl, LTI13SharedToolDeployment deployment);
	
	public List<LTI13SharedToolDeployment> getSharedToolDeployments(LTI13Platform sharedTool);
	
	public List<LTI13SharedToolDeployment> getSharedToolDeployments(RepositoryEntryRef entry);
	
	public List<LTI13SharedToolDeployment> getSharedToolDeployments(BusinessGroupRef businessGroup);
	
	
	/**
	 * @return The last valid public/private key
	 */
	public LTI13Key getLastPlatformKey();
	
	public LTI13Key getPlatformKey(String algorithm, String keyId);
	
	public List<LTI13Key> getPlatformKeys();
	
	public PublicKey getPlatformPublicKey(String kid);
	
	public List<LTI13Key> getKeys(String jwkSetUri, String alg, String kid);
	
	// Identity management
	
	public Identity loadIdentity(String sub, String issuer);
	
	public String subIdentity(Identity identity, String issuer);
	
	public Identity matchIdentity(Claims claims, LTI13Platform platform);
	
	public void checkMembership(Identity identity, GroupRoles role, LTI13SharedToolDeployment deployment);
	
	public String getNonce();
	
	public JwtToolBundle getAndVerifyClientAssertion(String clientAssertion);
	
	public OAuth2AccessToken getAccessToken(LTI13Platform tool, List<String> scopes);
	
	// Assignment and grading
	
	/**
	 * The line item link to this deployment (course + course element) or null.
	 * 
	 * @param deployment The deployment
	 * @return A line item
	 */
	public LineItem getLineItem(LTI13Context ltiContext);
	
	public Result getResult(String userId, Identity assessedId, LTI13Context ltiContext);
	
	public List<Result> getResults(LTI13Context ltiContext, int firstResult, int maxResults);
	
	// LTI 2.0 Deep Linking
	public List<LTI13ContentItem> createContentItems(Claims body, LTI13ToolDeployment deployment, LTI13Context ltiContext);
	
	public List<LTI13ContentItem> getContentItems(LTI13Context context);
	
	public List<LTI13ContentItem> reorderContentItems(List<LTI13ContentItem> items, List<Long> preferedOrder, int position);
	
	public LTI13ContentItem getContentItemByKey(Long key);

	public LTI13ContentItem updateContentItem(LTI13ContentItem item);
	
	public void deleteContentItem(LTI13ContentItem item);
	
}
