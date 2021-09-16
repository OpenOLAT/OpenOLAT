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
package org.olat.modules.immunityproof.ui.site;

import org.olat.basesecurity.Group;
import org.olat.basesecurity.GroupMembership;
import org.olat.basesecurity.manager.GroupDAO;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.navigation.SiteSecurityCallback;
import org.olat.modules.immunityproof.ImmunityProofModule;

/**
 * Initial date: 09.09.2021<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class ImmunityProofSecurityCallback implements SiteSecurityCallback {
	
	private ImmunityProofModule immunityProofModule;
	private GroupDAO groupDAO;
	
	public void setImmunityProofModule(ImmunityProofModule immunityProofModule) {
		this.immunityProofModule = immunityProofModule;
	}
	
	public void setGroupDAO(GroupDAO groupDAO) {
		this.groupDAO = groupDAO;
	}
	
	@Override
	public boolean isAllowedToLaunchSite(UserRequest ureq) {
		boolean isImmunityProofCommissioner = true;
		
		if (immunityProofModule.getCommissionersGroupKey() == null) {
			isImmunityProofCommissioner &= false;
		} else {
			Group commissionerGroup = groupDAO.loadGroup(immunityProofModule.getCommissionersGroupKey());
			
			if (commissionerGroup == null) {
				isImmunityProofCommissioner &= false;
			} else {
				GroupMembership membership = groupDAO.getMembership(commissionerGroup, ureq.getIdentity(), ImmunityProofModule.IMMUNITY_PROOF_COMMISSIONER_ROLE);
				
				isImmunityProofCommissioner &= membership != null;
			}
		}
		
		return isImmunityProofCommissioner;
	}

}
