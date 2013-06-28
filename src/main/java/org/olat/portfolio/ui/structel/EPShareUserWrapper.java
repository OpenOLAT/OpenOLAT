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
package org.olat.portfolio.ui.structel;

import java.util.List;

import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.id.Identity;
import org.olat.user.UserManager;

/**
 * 
 * Initial date: 28.06.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class EPShareUserWrapper {
	private FormLink removeIdentity;
	private Identity identity;
	private EPSharePolicyWrapper policyWrapper;
	
	public EPShareUserWrapper(EPSharePolicyWrapper policyWrapper, Identity identity, FormLink removeIdentity) {
		this.identity = identity;
		this.removeIdentity = removeIdentity;
		this.policyWrapper = policyWrapper;
	}
	
	public String getName() {
		return UserManager.getInstance().getUserDisplayName(identity);
	}
	
	public String getRemoveLinkName() {
		return removeIdentity.getComponent().getComponentName();
	}
	
	public void remove() {
		policyWrapper.getIdentities().remove(identity);
		@SuppressWarnings("unchecked")
		List<EPShareUserWrapper> wrappers = (List<EPShareUserWrapper>)policyWrapper.getUserListBox().contextGet("identities");
		wrappers.remove(this);
	}
}