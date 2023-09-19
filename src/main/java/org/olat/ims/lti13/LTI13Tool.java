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

import org.olat.core.id.CreateInfo;
import org.olat.core.id.ModifiedInfo;

/**
 * 
 * Initial date: 18 févr. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface LTI13Tool extends CreateInfo, ModifiedInfo {
	
	public Long getKey();
	
	public LTI13ToolType getToolTypeEnum();
	
	public String getToolName();
	
	public void setToolName(String toolName);
	
	public String getToolUrl();
	
	public void setToolUrl(String url);
	
	public String getToolDomain();
	
	public String getClientId();
	
	public void setClientId(String clientId);
	
	public PublicKeyType getPublicKeyTypeEnum();
	
	public void setPublicKeyTypeEnum(PublicKeyType type);

	public String getPublicKey();
	
	public void setPublicKey(String publicKey);
	
	public String getPublicKeyUrl();
	
	public void setPublicKeyUrl(String url);

	public String getInitiateLoginUrl();
	
	public void setInitiateLoginUrl(String url);
	
	public String getRedirectUrl();

	public void setRedirectUrl(String redirectUrl);
	
	public Boolean getDeepLinking();

	public void setDeepLinking(Boolean deepLinking);
	
	public enum PublicKeyType {
		URL,
		KEY
	}
}
